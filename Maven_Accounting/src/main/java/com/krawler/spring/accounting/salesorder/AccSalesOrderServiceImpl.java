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

import com.krawler.accounting.integration.common.IntegrationCommonService;
import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.CommonIndonesianNumberToWords;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.inspection.InspectionForm;
import com.krawler.inventory.model.inspection.InspectionFormDetails;
import com.krawler.inventory.model.stockout.StockAdjustmentDetail;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.account.accCusVenMapDAO;
import com.krawler.spring.accounting.account.accVendorCustomerProductDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.groupcompany.AccGroupCompanyDAO;
import com.krawler.spring.accounting.handler.*;
import com.krawler.spring.accounting.invoice.*;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.product.accProductControllerCMN;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.service.AccProductService;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.purchaseorder.service.AccPurchaseOrderModuleService;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.ws.service.WSUtilService;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.*;
import com.krawler.spring.exportFuctionality.ExportRecordHandler;
import com.krawler.spring.mainaccounting.service.AccCustomerMainAccountingService;
import com.krawler.spring.mrp.contractmanagement.MRPContractDetails;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class AccSalesOrderServiceImpl implements AccSalesOrderServiceDAO {

    private accSalesOrderDAO accSalesOrderDAOobj;
    private accCurrencyDAO accCurrencyobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accTaxDAO accTaxObj;
    private accAccountDAO accAccountDAOobj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private accProductDAO accProductObj;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private accGoodsReceiptDAO accGoodsReceiptDAOobj;
    private CommonEnglishNumberToWords EnglishNumberToWordsOjb = new CommonEnglishNumberToWords();
    private CommonIndonesianNumberToWords IndonesianNumberToWordsOjb = new CommonIndonesianNumberToWords();
    private AccCommonTablesDAO accCommonTablesDAO;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private fieldDataManager fieldDataManagercntrl;
    private AccLinkDataDao accLinkDataDao;
    private authHandlerDAO authHandlerDAOObj;
    private MessageSource messageSource;
    private AccProductService AccProductService;
    private accCusVenMapDAO accCusVenMapDAOObj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private accVendorCustomerProductDAO accVendorCustomerProductDAOobj;
    private AccGroupCompanyDAO accGroupCompanyDAO;
    private AccPurchaseOrderModuleService accPurchaseOrderModuleServiceObj;
    private WSUtilService wsUtilService;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;
    private IntegrationCommonService integrationCommonService;
    private AccSalesOrderServiceDAO accSalesOrderServiceDAOobj;
     private AccCustomerMainAccountingService accCustomerMainAccountingService;

    public void setIntegrationCommonService(IntegrationCommonService integrationCommonService) {
        this.integrationCommonService = integrationCommonService;
    }
    public void setaccCustomerMainAccountingService(AccCustomerMainAccountingService accCustomerMainAccountingService) {
        this.accCustomerMainAccountingService = accCustomerMainAccountingService;
    }
 
    public accVendorCustomerProductDAO getAccVendorCustomerProductDAOobj() {
        return accVendorCustomerProductDAOobj;
    }

    public void setAccVendorCustomerProductDAOobj(accVendorCustomerProductDAO accVendorCustomerProductDAOobj) {
        this.accVendorCustomerProductDAOobj = accVendorCustomerProductDAOobj;
    }

    public void setaccGroupCompanyDAO(AccGroupCompanyDAO accGroupCompanyDAO) {
        this.accGroupCompanyDAO = accGroupCompanyDAO;
    }
    
    public void setAccPurchaseOrderModuleServiceObj(AccPurchaseOrderModuleService accPurchaseOrderModuleServiceObj) {
        this.accPurchaseOrderModuleServiceObj = accPurchaseOrderModuleServiceObj;
    }
    
     public void setwsUtilService(WSUtilService wsUtilService) {
        this.wsUtilService = wsUtilService;
    }
    
     public void setaccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
     
    public void setAccLinkDataDao(AccLinkDataDao accLinkDataDao) {
        this.accLinkDataDao = accLinkDataDao;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyobj = accCurrencyobj;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    
    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    } 
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }  
    
    public void setAccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    
    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }
    
    public void setAccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptDAOobj) {
        this.accGoodsReceiptDAOobj = accGoodsReceiptDAOobj;
    }
    
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
   
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    
    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
    
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setAccProductService(AccProductService AccProductService) {
        this.AccProductService = AccProductService;
    }

    public void setAccCusVenMapDAOObj(accCusVenMapDAO accCusVenMapDAOObj) {
        this.accCusVenMapDAOObj = accCusVenMapDAOObj;
    }
    
    public void setAccMasterItemsDAOobj(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public AccInvoiceServiceDAO getAccInvoiceServiceDAO() {
        return accInvoiceServiceDAO;
    }

    public void setAccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }
    
    public void getProductQuanityJSONForJWO(AccSalesOrderServiceDAO accSalesOrderServiceDAOobj) {
        this.accSalesOrderServiceDAOobj = accSalesOrderServiceDAOobj;
    }
    
           
    public JSONArray getQuotationsJson(HashMap<String, Object> requestParams, List list, JSONArray jArr) throws ServiceException {
//        JSONArray jArr = new JSONArray();
        try {
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get("companyid");
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            DateFormat userdf = (DateFormat) requestParams.get(Constants.userdf);
            
            boolean closeflag = !requestParams.containsKey("closeflag") ? false :requestParams.containsKey("closeflag")&&requestParams.get("closeflag")==null?false:Boolean.TRUE.parseBoolean(requestParams.get("closeflag").toString());
            boolean soflag = !requestParams.containsKey("sopolinkflag") ? false : requestParams.containsKey("sopolinkflag")&&requestParams.get("sopolinkflag")==null?false:Boolean.FALSE.parseBoolean(requestParams.get("sopolinkflag").toString());
            boolean linkFlagInSO = !requestParams.containsKey("linkFlagInSO") ? false : requestParams.containsKey("linkFlagInSO")&&requestParams.get("linkFlagInSO")==null?false:Boolean.FALSE.parseBoolean(requestParams.get("linkFlagInSO").toString());  // Check wether quotation is link with SO 
            boolean linkFlagInInv = !requestParams.containsKey("linkFlagInInv") ? false : requestParams.containsKey("linkFlagInInv")&&requestParams.get("linkFlagInInv")==null?false:Boolean.FALSE.parseBoolean(requestParams.get("linkFlagInInv").toString());  // Check wether quotation is link with  Invoice
            boolean isLeaseFixedAsset = !requestParams.containsKey("isLeaseFixedAsset") ? false : (!StringUtil.isNullOrEmpty(requestParams.get("isLeaseFixedAsset").toString())) ? Boolean.parseBoolean(requestParams.get("isLeaseFixedAsset").toString()) : false;
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();

            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, isLeaseFixedAsset?Constants.Acc_Lease_Quotation:Constants.Acc_Customer_Quotation_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences!=null && extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            boolean isLineLevelTermFlag = false;//To Check Whether LinelevelTerms are applicable or not.
            if(extraCompanyPreferences != null && extraCompanyPreferences.getLineLevelTermFlag()==1){
                isLineLevelTermFlag = true;//If LineLevelTerms are applicable, then update the flag.
            }
            boolean isForTemplate = (requestParams.containsKey("isForTemplate") && Boolean.parseBoolean(requestParams.get("isForTemplate").toString()))?true:false;
            /*------customerQuotationsWithInvoiceAndDOStatus parameter comes when we apply linking filter in Quotation Report----- */
            int customerQuotationsWithInvoiceAndDOStatus = (requestParams.containsKey("customerQuotationsWithInvoiceAndDOStatus") && requestParams.get("customerQuotationsWithInvoiceAndDOStatus") != null) ? Integer.parseInt(requestParams.get("customerQuotationsWithInvoiceAndDOStatus").toString()) : 0;
            boolean isMalaysian = extraCompanyPreferences != null ? extraCompanyPreferences.getCompany().getCountry().getID().equalsIgnoreCase("137") : false;
            String compids[] = Constants.Companyids_Chkl_And_Marubishi.split(",");
            boolean isFromChklorMarubishi = false;
            for (int cnt = 0; cnt < compids.length; cnt++) {
                String compid = compids[cnt];
                if (compid.equalsIgnoreCase(companyid)) {
                    isFromChklorMarubishi = true;
                }
            }
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                String qid = (String) itr.next();
                if (!StringUtil.isNullOrEmpty(qid)) {
                    KwlReturnObject reqResult = accountingHandlerDAOobj.getObject(Quotation.class.getName(), qid);
                    Quotation salesOrder = (Quotation) reqResult.getEntityList().get(0);
                        Set<QuotationDetail> rows=salesOrder.getRows();
                    String status = "";
                    String ss = "";
                    boolean addFlag = true;   

                    /*----------- Code for Loading Data in Quotation Grid as per Applied filter of Quotation Linking with DO & Invoice---------------- */
                    if (customerQuotationsWithInvoiceAndDOStatus != 0) {
                      
                        boolean statusFlag = false;
                        HashMap requestparams = new HashMap();
                        requestparams.put("quotationObject", salesOrder);
                        requestparams.put("filterApplied", customerQuotationsWithInvoiceAndDOStatus);

                        statusFlag = quotationsApplicableForFilter(requestparams);
                        

                        /*-------Only relevant Data will load as per applied filter----------- */
                        if (!statusFlag) {
                            continue;
                        }

                    }
                    
                    KWLCurrency currency = null;
                    Customer customer = salesOrder.getCustomer();
                    if (salesOrder.getCurrency() != null) {
                        currency = salesOrder.getCurrency();
                    } else {
                        currency = customer.getAccount().getCurrency() == null ? kwlcurrency : customer.getAccount().getCurrency();
                    }
                    
                    //Get Attachment details
                    HashMap<String,Object> hashMap=new HashMap<String, Object>();
                    hashMap.put("invoiceID",salesOrder.getID());
                    hashMap.put("companyid",salesOrder.getCompany().getCompanyID());
                    KwlReturnObject object=accInvoiceDAOobj.getinvoiceDocuments(hashMap);
                    int attachemntcount=object.getRecordTotalCount();
                    SimpleDateFormat sdf = new SimpleDateFormat();
                    String newdateStr = sdf.format(new Date());
                    String browsertz=requestParams.containsKey("browsertz")&&requestParams.get("browsertz") !=null?requestParams.get("browsertz").toString():"";
                    
                    if (browsertz != null && !StringUtil.isNullOrEmpty(browsertz)) {
                        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + browsertz));
                    }
                    Date currentDate = sdf.parse(newdateStr);
                    currentDate=CompanyPreferencesCMN.removeTimefromDate(currentDate);
                    currentDate=CompanyPreferencesCMN.removeTimefromDate(currentDate);
                    
                    //KWLCurrency currency=salesOrder.getCustomer().getAccount().getCurrency()==null?kwlcurrency:salesOrder.getCustomer().getAccount().getCurrency();
                  
                    JSONObject obj = new JSONObject();
                    obj.put("isQuotation", true);
                    KwlReturnObject linkRresult = accLinkDataDao.checkEntryForTransactionInLinkingTableForForwardReference("Quotation",salesOrder.getID());
                    list = linkRresult.getEntityList();
                    if (list != null && !list.isEmpty()) {
                         obj.put(Constants.IS_LINKED_TRANSACTION, true);
                    }else{
                         obj.put(Constants.IS_LINKED_TRANSACTION, false);
                    }

                    /**
                     * Put GST document history.
                     */
                    if (salesOrder.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                        obj.put("refdocid", salesOrder.getID());
                        fieldDataManagercntrl.getGSTDocumentHistory(obj);
                    }
                    obj.put("crmquoteid", salesOrder.getCrmquoteid());
                    obj.put("billid", salesOrder.getID());
                    obj.put("companyid", salesOrder.getCompany().getCompanyID());
                    obj.put("companyname", salesOrder.getCompany().getCompanyName());
                    obj.put("personid", customer.getID());
                    obj.put("aliasname", customer.getAliasname());
                    obj.put(Constants.PERSONCODE, customer.getAcccode());
                    if(salesOrder.isIsopen() && salesOrder.getValiddate()!=null && !(salesOrder.getValiddate().compareTo(currentDate) >=0 )){
                         obj.put("status","Expired");   
                    }else{
                         obj.put("status", salesOrder.isIsopen()?"Open":"Closed");                    
                    }
//                    MasterItem gstRegistrationType = customer != null ? customer.getGSTRegistrationType() : null;
//                    if (gstRegistrationType != null && gstRegistrationType.getDefaultMasterItem() != null) {
//                        obj.put("GSTINRegTypeDefaultMstrID", gstRegistrationType.getDefaultMasterItem().getID());
//                    }
                    obj.put(Constants.isMerchantExporter, salesOrder.isIsMerchantExporter());
                   /* For Chekcbox status in Quaotation entry form*/
                    obj.put("isReserveStockQuantityflag", salesOrder.isReserveStockQuantityFlag());
                    obj.put("personemail", customer == null ? "" : customer.getEmail());
                    obj.put("billno", salesOrder.getquotationNumber());
                    obj.put("contract", (salesOrder.getContract() != null) ? salesOrder.getContract().getID() : "");
                    obj.put("duedate", df.format(salesOrder.getDueDate()));
                    obj.put("date", df.format(salesOrder.getQuotationDate()));
                    obj.put("validtilldateinuserformat",salesOrder.getValiddate()== null ? "" : userdf.format(salesOrder.getValiddate()));
                    obj.put("duedateinuserformat", userdf.format(salesOrder.getDueDate()));
                    obj.put("dateinuserformat", userdf.format(salesOrder.getQuotationDate()));
                    obj.put("shipdate", salesOrder.getShipdate() == null ? "" : df.format(salesOrder.getShipdate()));
                    obj.put("shipdateinuserformat", salesOrder.getShipdate() == null ? "" : userdf.format(salesOrder.getShipdate()));
                    obj.put("validdate", salesOrder.getValiddate() == null ? "" : df.format(salesOrder.getValiddate()));
                    obj.put("preparedBy", salesOrder.getCreatedby() != null ? salesOrder.getCreatedby().getFullName() : "");
                    obj.put("shipvia", salesOrder.getShipvia() == null ? "" : salesOrder.getShipvia());
                    obj.put("fob", salesOrder.getFob() == null ? "" : salesOrder.getFob());
                    obj.put("archieve", salesOrder.getArchieve());
                    obj.put("gstapplicable", salesOrder.isIsIndGSTApplied());
                    obj.put("billto", salesOrder.getBillTo());
                    obj.put("shipto", salesOrder.getShipTo());
                    obj.put(Constants.HAS_ACCESS, customer.isActivate());
                    obj.put("shippingterm", salesOrder.getShippingTerm());
                    obj.put("deleted", salesOrder.isDeleted());
                    obj.put("salesPerson", salesOrder.getSalesperson() != null ? salesOrder.getSalesperson().getID() : "");
                    obj.put("salespersonname", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getValue());
                    obj.put("salespersondesignation", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getDesignation());
                    obj.put("salesPersonCode", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getCode());
                    obj.put("salesPersonEmail", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getEmailID()==null?"":salesOrder.getSalesperson().getEmailID());
                    obj.put("salesPersonTel", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getContactNumber()==null?"":salesOrder.getSalesperson().getContactNumber());
                    obj.put("isfavourite", salesOrder.isFavourite());
                    obj.put("isprinted", salesOrder.isPrinted());
                    obj.put("isEmailSent", salesOrder.isIsEmailSent());
                    obj.put("termdetails", getTermDetails(salesOrder.getID(), false));
//                    obj.put("termamount", CommonFunctions.getTotalTermsAmount(getTermDetails(salesOrder.getID(), false)));
                    obj.put("discountval", (salesOrder.getDiscount() == 0) ? 0 : salesOrder.getDiscount());
                    obj.put("gstIncluded", salesOrder.isGstIncluded());
                    obj.put("shiplengthval", salesOrder.getShiplength());
                    obj.put("invoicetype", salesOrder.getInvoicetype());
                    obj.put("formtypeid", salesOrder.getFormtype());
                    obj.put("isInterstateParty", salesOrder.getCustomer().isInterstateparty());
                    obj.put("quotationtype", salesOrder.getQuotationType());
                    obj.put("fixedAssetLeaseInvoice", salesOrder.isLeaseQuotation());
                    obj.put("termid", salesOrder.getTerm()!=null?salesOrder.getTerm().getID():"");
                    obj.put("termname", salesOrder.getTerm()!=null?salesOrder.getTerm().getTermname():"");
                    obj.put(Constants.IsRoundingAdjustmentApplied, salesOrder.isIsRoundingAdjustmentApplied());
//                    obj.put("approvalstatusinfo", salesOrder.getApprovestatuslevel() == -1 ? "Rejected" : salesOrder.getApprovestatuslevel() < 11 ? "Waiting for Approval at Level - " + salesOrder.getApprovestatuslevel() : "Approved");
                    obj.put("approvalstatus", salesOrder.getApprovestatuslevel());
                    obj.put("customerporefno",salesOrder.getCustomerPORefNo());
                    obj.put("totalprofitmargin",salesOrder.getTotalProfitMargin());
                    obj.put("totalprofitmarginpercent",salesOrder.getTotalProfitMarginPercent());
                    obj.put("isDraft", salesOrder.isDraft());
                    obj.put("gtaapplicable", salesOrder.isRcmapplicable()); // Get RCM applicable Check - Used for INDIA only
                    BillingShippingAddresses addresses = salesOrder.getBillingShippingAddresses();
                    AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                    obj.put(Constants.SEQUENCEFORMATID, salesOrder.getSeqformat() == null ? "" : salesOrder.getSeqformat().getID());
                    obj.put("attachment",attachemntcount);
                    obj.put("createdby", salesOrder.getCreatedby() == null ? "" : StringUtil.getFullName(salesOrder.getCreatedby()));
                    String approvalStatus="";
                    if(salesOrder.getApprovestatuslevel() < 0){
                        approvalStatus="Rejected";
                    }else if(salesOrder.getApprovestatuslevel() < 11){
                        String ruleid = "",userRoleName="";
                        HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                        qdDataMap.put("companyid", companyid);
                        qdDataMap.put("level",salesOrder.getApprovestatuslevel() );
                        qdDataMap.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                        KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                        Iterator ruleitr = flowresult.getEntityList().iterator();
                        while (ruleitr.hasNext()) {
                            Object[] rulerow = (Object[]) ruleitr.next();
                            ruleid = rulerow[0].toString();
                        }
                        if (!StringUtil.isNullOrEmpty(ruleid)) {
                            qdDataMap.put("ruleid", ruleid);
                            KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(qdDataMap);
                            Iterator useritr = userResult.getEntityList().iterator();
                            while (useritr.hasNext()) {
                                Object[] userrow = (Object[]) useritr.next();
                                String userId = userrow[0].toString();
                                String userName = userrow[1].toString();
                                KwlReturnObject kmsg = null;
                                String roleName="Company User";
                                kmsg = permissionHandlerDAOObj.getRoleofUser(userId);
                                Iterator ite2 = kmsg.getEntityList().iterator();
                                while (ite2.hasNext()) {
                                    Object[] row = (Object[]) ite2.next();
                                    roleName = row[1].toString();
                                }
                                userRoleName += roleName+" "+userName + ",";
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(userRoleName)) {
                            userRoleName = userRoleName.substring(0, userRoleName.length() - 1);
                        }
                        approvalStatus="Pending Approval by "+userRoleName+" at Level - "+salesOrder.getApprovestatuslevel();
                    } else {
                        approvalStatus="Approved";
                    }
                    obj.put("approvalstatusinfo",approvalStatus);
                    
                    boolean incProTax = false;
                    Iterator itrRow = rows.iterator();
                    double amount = 0, amountinbase = 0, totalDiscount = 0, discountPrice = 0;
                    double rowTaxAmt=0d,rowDiscountAmt = 0d, rowOtherTermNonTaxableAmount = 0d;
                    double productTotalAmount = 0d;
                    double subtotal = 0d;
                    Set<String> uniqueProductTaxList = new HashSet<String>();
                    while (itrRow.hasNext()) {
                        QuotationDetail sod = (QuotationDetail) itrRow.next();
                        if (sod.getTax() != null) {
                            incProTax = true;
                            uniqueProductTaxList.add(sod.getTax().getID());
                        }

                        double qrate = authHandler.roundUnitPrice(sod.getRate(), companyid);
                        double quantity = authHandler.roundQuantity(sod.getQuantity(), companyid);
                        boolean gstIncluded = salesOrder.isGstIncluded();
                        if (gstIncluded) {
                            qrate = sod.getRateincludegst();
                        }
                        double quotationPrice = authHandler.round(quantity * qrate, companyid);
                        productTotalAmount += quotationPrice;
                        subtotal += quotationPrice;
                        double discountQD = authHandler.round(sod.getDiscount(), companyid);



                        if (sod.getDiscountispercent() == 1) {
                            double discountAmount=authHandler.round((quotationPrice * discountQD / 100), companyid);
                            discountPrice = (quotationPrice) - authHandler.round((quotationPrice * discountQD / 100), companyid);
                            rowDiscountAmt += authHandler.round((quotationPrice * discountQD/100), companyid);
                        } else {
                            discountPrice = quotationPrice - discountQD;
                            rowDiscountAmt += discountQD;
                        }

                        
                        rowTaxAmt+=sod.getRowTaxAmount();
                        amount += discountPrice;
                        //amount = amount - (sod.getQuantity() * sod.getRate() * sod.getDiscount()/100);
                        if (!gstIncluded) {
                            amount += authHandler.round(sod.getRowTaxAmount(), companyid);
                        }
                        if (isLineLevelTermFlag) {
                            rowTaxAmt+=sod.getRowtermamount();
                            // Append OtherTermNonTaxableAmount for rach row.
                            rowOtherTermNonTaxableAmount += sod.getOtherTermNonTaxableAmount();
                                 /**
                                 * ERP-34717
                                 * If GST Include, no need to add Tax Amount. 
                                 * Amount is already with tax 
                                 */
                            if(!gstIncluded){
                                amount += authHandler.round(sod.getRowtermamount(), companyid);
                                amount += authHandler.round(sod.getOtherTermNonTaxableAmount(), companyid);
                            }
                        }
                    }
                     obj.put("productTotalAmount", productTotalAmount);
                    double discountQ = authHandler.round(salesOrder.getDiscount(), companyid);
                    obj.put("includeprotax", incProTax);
                    if (salesOrder.getModifiedby() != null) {
                        obj.put("lasteditedby", StringUtil.getFullName(salesOrder.getModifiedby()));
                    }
                    if (discountQ != 0) {
                        if (salesOrder.isPerDiscount()) {
                            totalDiscount = amount * discountQ / 100;
                            amount = amount - totalDiscount;
                        } else {
                            amount = amount - discountQ;
                            totalDiscount = discountQ;
                        }
                        obj.put("discounttotal", discountQ);
                    } else {
                        obj.put("discounttotal", 0);
                    }
//                    obj.put("discount", totalDiscount);
                    obj.put("discount", rowDiscountAmt);
                    obj.put("discountispertotal", salesOrder.isPerDiscount());
                    obj.put("ispercentdiscount", salesOrder.isPerDiscount());
                    obj.put("discountinbase", salesOrder.getDiscountinbase());

                    obj.put("amount", amount);
                    obj.put("currencysymbol", currency.getSymbol()==null?"":currency.getSymbol());
                    obj.put("currencycode", currency.getCurrencyCode()==null?"":currency.getCurrencyCode());
                    obj.put("currencyname", currency.getName()==null?"":currency.getName());
                    obj.put("externalcurrencyrate", salesOrder.getExternalCurrencyRate());
                    obj.put("taxid", salesOrder.getTax() == null ? "" : salesOrder.getTax().getID());
                    obj.put("taxname", salesOrder.getTax() == null ? "" : salesOrder.getTax().getName());
                    
                    boolean isApplyTaxToTerms=salesOrder.isApplyTaxToTerms();
                    obj.put("isapplytaxtoterms", isApplyTaxToTerms);
                    
                    double taxPercent = 0;
                    double totalTermAmount = 0;
                    double taxableamount = 0;
                    double totalTermTaxAmount = 0;
                    
                    List cqTermMapList = kwlCommonTablesDAOObj.getSummationOfTermAmtAndTermTaxAmt(Constants.quotationtermmap, salesOrder.getID());
                    if(cqTermMapList != null && !cqTermMapList.isEmpty()){
                        Iterator termItr = cqTermMapList.iterator();
                        while (termItr.hasNext()) {
                            Object[] termObj = (Object[]) termItr.next();
                            /* 
                            * [0] : Sum of termamount  
                            * [1] : Sum of termamountinbase 
                            * [2] : Sum of termTaxamount 
                            * [3] : Sum of termTaxamountinbase 
                            * [4] : Sum of termAmountExcludingTax 
                            * [5] : Sum of termAmountExcludingTaxInBase
                            */ 
                            if (salesOrder.isGstIncluded()) {
                                if(termObj[4] != null){
                                    totalTermAmount += authHandler.round((Double) termObj[4],companyid);
                                }
                            } else {
                                if(termObj[0] != null){
                                    totalTermAmount += authHandler.round((Double) termObj[0],companyid);
                                }
                            }
                            if(termObj[2] != null){
                                totalTermTaxAmount += authHandler.round((Double) termObj[2],companyid);
                            }
                        }
                    }
                    totalTermAmount = authHandler.round(totalTermAmount, companyid);
                    totalTermTaxAmount = authHandler.round(totalTermTaxAmount, companyid);
                    obj.put("termamount", totalTermAmount);
                    KwlReturnObject termbAmtTax = accCurrencyobj.getCurrencyToBaseAmount(requestParams, totalTermAmount, currency.getCurrencyID(), salesOrder.getQuotationDate(), salesOrder.getExternalCurrencyRate());
                    double termamountinBase = authHandler.round((Double) termbAmtTax.getEntityList().get(0), companyid);
                    if (salesOrder.getTax() != null) {
                        requestParams.put("transactiondate", salesOrder.getQuotationDate());
                        requestParams.put("taxid", salesOrder.getTax().getID());
                        if (requestParams.containsKey(Constants.ss) && requestParams.get(Constants.ss) != null) {
                          ss = (String) requestParams.get("ss");
                          requestParams.remove("ss"); //serch string unnecessary getting added while getting tax list, no need to pass ss string here
                        }
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        requestParams.put("ss", !StringUtil.isNullOrEmpty(ss) ? ss : "");
                        List taxList = result.getEntityList();
                        if (taxList != null && !taxList.isEmpty()) {
                            Object[] taxObj = (Object[]) taxList.get(0);
                            taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        }
                    }
                    double orderAmount = amount;//(Double) bAmt.getEntityList().get(0);
                    double ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round(((orderAmount + taxableamount) * taxPercent / 100), companyid));
                    double taxAmt=rowTaxAmt+ordertaxamount;// either row level tax will be avvailable or invoice level
                    obj.put("amountbeforegst", amount-rowTaxAmt); // Amount before both kind of tax row level or transaction level
                    
                    if (salesOrder.isIsRoundingAdjustmentApplied()) {
                        double totalAmountwithtax=0d;
                        totalAmountwithtax = ordertaxamount + amount + totalTermAmount + totalTermTaxAmount + salesOrder.getRoundingadjustmentamount();
                        if (!currencyid.equals(currency.getCurrencyID())) {//If not base curreny then need to convert in base
                            KwlReturnObject bAmtTax = accCurrencyobj.getCurrencyToBaseAmount(requestParams, totalAmountwithtax, currency.getCurrencyID(), salesOrder.getQuotationDate(), salesOrder.getExternalCurrencyRate());
                            totalAmountwithtax = authHandler.round((Double) bAmtTax.getEntityList().get(0), companyid);
                        }
                        obj.put("amountinbase", totalAmountwithtax);
                    } else {
                        KwlReturnObject bAmtTax = accCurrencyobj.getCurrencyToBaseAmount(requestParams, ordertaxamount, currency.getCurrencyID(), salesOrder.getQuotationDate(), salesOrder.getExternalCurrencyRate());
                        double ordertaxamountBase = authHandler.round((Double) bAmtTax.getEntityList().get(0), companyid);

                        double totalQuotationAmtInBase =0d;
                        totalQuotationAmtInBase = amount + totalTermAmount + totalTermTaxAmount;
                        KwlReturnObject tempBaseAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, authHandler.round(totalQuotationAmtInBase, companyid), currency.getCurrencyID(), salesOrder.getQuotationDate(), salesOrder.getExternalCurrencyRate());
                        totalQuotationAmtInBase = authHandler.round((Double) tempBaseAmt.getEntityList().get(0), companyid);
                        obj.put("amountinbase", totalQuotationAmtInBase + ordertaxamountBase);
                    }

                    obj.put("taxpercent", taxPercent);
                    obj.put("taxamount",taxAmt + totalTermTaxAmount);// Tax Amount
                    if(salesOrder.isGstIncluded()) {
                        subtotal = productTotalAmount - rowDiscountAmt-(taxAmt);
                    } else {
                        subtotal = productTotalAmount - rowDiscountAmt;
                    }
                    obj.put("amountBeforeTax",authHandler.formattingDecimalForAmount((subtotal+totalTermAmount),companyid));
                    obj.put("subtotal", subtotal);
                    if (isLineLevelTermFlag) {
                        // If LineLevelTerm is applicable then add the value in JSON Object.
                        obj.put("OtherTermNonTaxableAmount", rowOtherTermNonTaxableAmount);
                    }
                    amount+=totalTermAmount+totalTermTaxAmount;
                    orderAmount+=totalTermAmount + totalTermTaxAmount;
                    obj.put("orderamount", orderAmount);
                    double totalAmt = orderAmount + ordertaxamount;
                    if (salesOrder.isIsRoundingAdjustmentApplied()) {
                        totalAmt += salesOrder.getRoundingadjustmentamount();
                    }
                    obj.put("orderamountwithTax", totalAmt);
                    obj.put("amountinWords",currency.getName() + " " + EnglishNumberToWordsOjb.convert(orderAmount + ordertaxamount, currency,countryLanguageId) + " only.");
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("personname", customer.getName());
                    obj.put("personcode", customer.getAcccode()==null?"":customer.getAcccode());
                    obj.put("billtoaddress", CommonFunctions.getBillingShippingAddress(addresses, true));
                    obj.put("shiptoaddress", CommonFunctions.getBillingShippingAddress(addresses, false));
                    obj.put("memo", salesOrder.getMemo());
                    obj.put("posttext", salesOrder.getPostText());
                    /**
                     * Get Module template and its mapped Unit details for company if Line level term flag ON 
                     */
                    if(isForTemplate){
                        HashMap<String, Object> ModuleTempParams = new HashMap<>();
                        ModuleTempParams.put("modulerecordid", salesOrder.getID());
                        ModuleTempParams.put("companyid", companyid);
                        /** Get Module template  from invoice id . In module template Quotation id add as modulerecordid */
                        KwlReturnObject ModuleTempObj = accountingHandlerDAOobj.getModuleTemplates(ModuleTempParams);    
                        if(ModuleTempObj!=null && ModuleTempObj.getEntityList().size() > 0){
                            ModuleTemplate moduleTemp = (ModuleTemplate) ModuleTempObj.getEntityList().get(0);
                            obj.put("companyunitid", moduleTemp.getCompanyUnitid());
                            obj.put("populateproducttemplate", moduleTemp.isPopulateproductintemp());
                            obj.put("populatecustomertemplate", moduleTemp.isPopulatecustomerintemp());

                            HashMap tmpHashMap = new HashMap();
                            tmpHashMap.put("companyunitid", moduleTemp.getCompanyUnitid());
                            tmpHashMap.put(Constants.companyKey, companyid);
                            /* Get Company Unit details from companyunitid mapped with module template */
                            KwlReturnObject exciseTemp = accountingHandlerDAOobj.getExciseTemplatesMap(tmpHashMap);
                            if (isLineLevelTermFlag && exciseTemp != null && exciseTemp.getEntityList().size() > 0) {
                                ExciseDetailsTemplateMap ExcisemoduleTemp = (ExciseDetailsTemplateMap) exciseTemp.getEntityList().get(0);
                                if (ExcisemoduleTemp != null) {
                                    obj.put("registrationType", ExcisemoduleTemp.getRegistrationType());
                                    obj.put("UnitName", ExcisemoduleTemp.getUnitname());
                                    obj.put("ECCNo", ExcisemoduleTemp.getECCNo());
                                }
                            }
                        }
                    }
                    KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(QuotationCustomData.class.getName(), salesOrder.getID());
                    boolean linkFlag = requestParams.get("linkFlagInSO") == null ? false : Boolean.parseBoolean(requestParams.get("linkFlagInSO").toString());
                    if (custumObjresult.getEntityList().size() > 0) {
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        QuotationCustomData quotationCustomData = (QuotationCustomData) custumObjresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(quotationCustomData, FieldMap, replaceFieldMap, variableMap);
                        boolean isExport = (requestParams.get(Constants.isExport) == null) ? false : true;
                        JSONObject params = new JSONObject();
                        params.put("companyid", companyid);
                        params.put(Constants.isExport, isExport);
                        params.put("userdf", userdf);
                        if ((linkFlag  || linkFlagInInv)) {
                            int moduleId = !linkFlagInInv?Constants.Acc_Sales_Order_ModuleId:Constants.Acc_Invoice_ModuleId;
                            if (isLeaseFixedAsset){
                                moduleId=Constants.Acc_Lease_Order_ModuleId;
                            }
                            params.put("linkModuleId", moduleId);
                            params.put("isLink", true);
                            params.put("companyid", companyid);
                            params.put("customcolumn", 0);
                        }
//                        if (requestParams.containsKey("browsertz") && requestParams.get("browsertz") != null) {
//                            params.put("browsertz", requestParams.get("browsertz").toString());
//                        }
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                        
//                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
//
//                            String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
//                            if (customFieldMap.containsKey(varEntry.getKey())) {
//                                
//                                String value = "";
//                                String Ids[] = coldata.split(",");
//                                for (int i = 0; i < Ids.length; i++) {
//                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
//                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
//                                    if (fieldComboData != null) {
//                                        if ((fieldComboData.getField().getFieldtype() == 12 || fieldComboData.getField().getFieldtype() == 7) && !isExport && !linkFlag) {
//                                            value += Ids[i] != null ? Ids[i] + "," : ",";
//                                        } else {
//                                            value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
//                                        }
//
//                                    }
//                                }
//                                if (!StringUtil.isNullOrEmpty(value)) {
//                                    value = value.substring(0, value.length() - 1);
//                                }
//                                obj.put(varEntry.getKey(), value);
//                            } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
//                                DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                                obj.put(varEntry.getKey(), sdf.format(Long.parseLong(coldata)));
//                            } else {
//                                if (!StringUtil.isNullOrEmpty(coldata)) {
//                                    String[] coldataArray = coldata.split(",");
//                                    String Coldata = "";
//                                    for (int countArray = 0; countArray < coldataArray.length; countArray++) {
//                                        Coldata += "'" + coldataArray[countArray] + "',";
//                                    }
//                                    Coldata = Coldata.substring(0, Coldata.length() - 1);
//                                    String ColValue = accAccountDAOobj.getfieldcombodatabyids(Coldata);
//                                    obj.put(varEntry.getKey(), coldata);
//                                    obj.put(varEntry.getKey() + "_Values", ColValue);
//                                }
//                            }
//                        }
                    }
//                    boolean addFlag = true;
//                    if (closeflag && status.equalsIgnoreCase("Closed")) {
//                        addFlag = false;
//                    }
                     /*
                     zeroQtyFlag = dont allow quotation with zero qty in linking case
                     */
//                    if (linkFlag || linkFlagInInv) {
//                        zeroQtyFlag = zeroQtyFlag;
//                    } else {
//                        zeroQtyFlag = false;   //SDP-10772
//                    }
                    
//                    if (addFlag && !zeroQtyFlag) {
                    if (addFlag || linkFlag || linkFlagInInv) {
                        jArr.put(obj);
                    }
            }
          }     
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getQuotationsJson : " + ex.getMessage(), ex);
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
                    jsonobj.put("termamountinbase", SalesOrderTermMap.getTermamountinbase());
                    jsonobj.put("termtaxamount", SalesOrderTermMap.getTermtaxamount());
                    jsonobj.put("termtaxamountinbase", SalesOrderTermMap.getTermtaxamountinbase());
                    jsonobj.put("termAmountExcludingTax", SalesOrderTermMap.getTermAmountExcludingTax());
                    jsonobj.put("termAmountExcludingTaxInBase", SalesOrderTermMap.getTermAmountExcludingTaxInBase());
                    jsonobj.put("termtax", SalesOrderTermMap.getTermtax()!=null ? SalesOrderTermMap.getTermtax().getID():"");
                    jsonobj.put("linkedtaxname", SalesOrderTermMap.getTermtax()!=null ? SalesOrderTermMap.getTermtax().getName():"");
                    jsonobj.put("isActivated", SalesOrderTermMap.getTermtax()!=null ? SalesOrderTermMap.getTermtax().isActivated():false);
                    if(SalesOrderTermMap.getTermtax()!=null){
                        jsonobj.put("linkedtaxpercentage", accInvoiceDAOobj.getPercentageFromTaxid(SalesOrderTermMap.getTermtax().getID(), mt.getCompany().getCompanyID()));
                    }else{
                        jsonobj.put("linkedtaxpercentage", 0);
                    }
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
                    jsonobj.put("termamountinbase", quotationTermMap.getTermamountinbase());
                    jsonobj.put("termtaxamount", quotationTermMap.getTermtaxamount());
                    jsonobj.put("termtaxamountinbase", quotationTermMap.getTermtaxamountinbase());
                    jsonobj.put("termAmountExcludingTax", quotationTermMap.getTermAmountExcludingTax());
                    jsonobj.put("termAmountExcludingTaxInBase", quotationTermMap.getTermAmountExcludingTaxInBase());
                    jsonobj.put("termtax", quotationTermMap.getTermtax()!=null ? quotationTermMap.getTermtax().getID():"");
                    jsonobj.put("linkedtaxname", quotationTermMap.getTermtax()!=null ? quotationTermMap.getTermtax().getName():"");
                    jsonobj.put("isActivated", quotationTermMap.getTermtax()!=null ? quotationTermMap.getTermtax().isActivated():false);
                    if(quotationTermMap.getTermtax()!=null){
                        jsonobj.put("linkedtaxpercentage", accInvoiceDAOobj.getPercentageFromTaxid(quotationTermMap.getTermtax().getID(), mt.getCompany().getCompanyID()));
                    }else{
                        jsonobj.put("linkedtaxpercentage", 0);
                    }
                    jArr.put(jsonobj);
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }

    public double getQuotationDetailStatusSO(QuotationDetail quod) throws ServiceException {
        double result = quod.getQuantity();
//        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), quod.getCompany().getCompanyID());
//        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
        KwlReturnObject idresult = accInvoiceDAOobj.getSODFromQD(quod.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0.0;
        while (ite1.hasNext()) {
            SalesOrderDetail sod = (SalesOrderDetail) ite1.next();
            qua += sod.getQuantity();

        }
        result = quod.getQuantity() - qua;
        return result;
    }

    public double getQuotationDetailStatusINV(QuotationDetail quod) throws ServiceException {
        double result = quod.getQuantity();
//        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), quod.getCompany().getCompanyID());
//        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
        KwlReturnObject idresult = accInvoiceDAOobj.getINVDFromQD(quod.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0.0;
        while (ite1.hasNext()) {
            InvoiceDetail inv = (InvoiceDetail) ite1.next();
            qua += inv.getInventory().getQuantity();

        }
        result = quod.getQuantity() - qua;
        return result;
    }

    @Override
    public JSONObject getQuotationRows(HashMap<String, Object> requestParams) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
         String[] sos=null;
        try {
            String companyid = (String) requestParams.get("companyid");
            String gcurrencyid = (String) requestParams.get("gcurrencyid");
            DateFormat df = (DateFormat) requestParams.get("dataFormatValue");
            DateFormat userdf = (DateFormat) requestParams.get(Constants.userdf);
            String userid = (String) requestParams.get(Constants.useridKey);
            String description="";
            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = companyObj!=null ? (Company) companyObj.getEntityList().get(0):null;
            int countryid = ( company != null && company.getCountry() != null) ? Integer.parseInt(company.getCountry().getID()) : 0;
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), (String) requestParams.get("gcurrencyid"));
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            String closeflag = (String) requestParams.get("closeflag");
            boolean soflag = requestParams.containsKey("sopolinkflag")&&requestParams.get("sopolinkflag")==null?false:Boolean.FALSE.parseBoolean((String)requestParams.get("sopolinkflag"));
           
            boolean isForLinking = false;
            if (requestParams.containsKey("isForLinking") && requestParams.get("isForLinking") != null) {
                isForLinking = (Boolean) requestParams.get("isForLinking");
            }
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(requestParams.get("isLeaseFixedAsset").toString())) ? Boolean.parseBoolean(requestParams.get("isLeaseFixedAsset").toString()) : false;
            KwlReturnObject cpresult = accountingHandlerDAOobj.loadObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            
            KwlReturnObject extracapresult = accountingHandlerDAOobj.loadObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            
             Map<String, Object> ProductFieldsRequestParams = new HashMap();
            ProductFieldsRequestParams.put("companyid", companyid);
            ProductFieldsRequestParams.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);

            List masterFieldsResultList = CommonFunctions.getproductmastersFieldsToShowLineLevel(ProductFieldsRequestParams, accountingHandlerDAOobj);
            if(!StringUtil.isNullOrEmpty((String)requestParams.get("bills")))
            {
            sos = (String[]) ((String) requestParams.get("bills")).split(",");
            }
            String dType = (String) requestParams.get("dtype");
            boolean isOrder = (Boolean) requestParams.get("isOrder");
            boolean isReport = false;
            boolean customIsReport = false;
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(dType) && StringUtil.equal(dType, "report")) {
                isReport = true;
                customIsReport = true;
            }
            boolean isCopy = (requestParams.containsKey("copyInvoice") ? Boolean.parseBoolean((String) requestParams.get("copyInvoice")) : false);
//            if (!StringUtil.isNullOrEmpty((String)requestParams.get("copyInvoice")) && Boolean.parseBoolean((String)requestParams.get("copyInvoice"))) {
//                isReport = true;
//            }
            if (requestParams.containsKey(Constants.isExport) && requestParams.get(Constants.isExport) != null) {
                isExport = (boolean) requestParams.get(Constants.isExport);
            }
            int i = 0;
            JSONArray jArr = new JSONArray();
            double addobj = 1;

            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, isLeaseFixedAsset?Constants.Acc_Lease_Quotation:Constants.Acc_Customer_Quotation_ModuleId, 1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("quotation.ID");
            order_by.add("srno");
            order_type.add("asc");
            soRequestParams.put("filter_names", filter_names);
            soRequestParams.put("filter_params", filter_params);
            soRequestParams.put("order_by", order_by);
            soRequestParams.put("order_type", order_type);

            while (sos != null && i < sos.length) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(Quotation.class.getName(), sos[i]);
                Quotation so = (Quotation) result.getEntityList().get(0);
                KWLCurrency currency = null;

                if (so.getCurrency() != null) {
                    currency = so.getCurrency();
                } else {
                    currency = so.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : so.getCustomer().getAccount().getCurrency();
                }
                //KWLCurrency currency=so.getCustomer().getAccount().getCurrency()==null?kwlcurrency:so.getCustomer().getAccount().getCurrency();
                filter_params.clear();
                filter_params.add(so.getID());
                KwlReturnObject podresult = accSalesOrderDAOobj.getQuotationDetails(soRequestParams);
                Iterator itr = podresult.getEntityList().iterator();
                while (itr.hasNext()) {
                    QuotationDetail row = (QuotationDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    Product rowProduct = row.getProduct();
                    
                    CommonFunctions.getterMethodForProductsData(row.getProduct(), masterFieldsResultList, obj);
                    obj.put("billid", so.getID());
                    obj.put("billno", so.getquotationNumber());
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("originalTransactionRowid", row.getID());
                    obj.put("productid",rowProduct.getID());
                    obj.put("purchasetaxId", rowProduct.getPurchasetaxid());
                    obj.put("salestaxId", rowProduct.getSalestaxid());
                    obj.put("hasAccess",rowProduct.isIsActive());
                    obj.put("productname", rowProduct.getName());
                    obj.put("isLocationForProduct", rowProduct.isIslocationforproduct());
                    obj.put("isWarehouseForProduct", rowProduct.isIswarehouseforproduct());
                    obj.put("isBatchForProduct", rowProduct.isIsBatchForProduct());
                    obj.put("isSerialForProduct", rowProduct.isIsSerialForProduct());
                    obj.put("isRowForProduct", rowProduct.isIsrowforproduct());
                    obj.put("isRackForProduct", rowProduct.isIsrackforproduct());
                    obj.put("isBinForProduct", rowProduct.isIsbinforproduct());
                    obj.put("location", rowProduct.getLocation() != null ? rowProduct.getLocation().getId() : "");
                    obj.put("warehouse", rowProduct.getWarehouse() != null ? rowProduct.getWarehouse().getId() : "");
                    obj.put("maxorderingquantity", rowProduct.getMaxOrderingQuantity()); 
                    obj.put("minorderingquantity", rowProduct.getMinOrderingQuantity());
                    
                    String uom = row.getUom() != null ? row.getUom().getNameEmptyforNA() : rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getNameEmptyforNA();
                    obj.put("unitname", uom);
                    obj.put("uomname", uom);
                    obj.put("baseuomname", rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("multiuom", rowProduct.isMultiuom());
                    
                    if(!StringUtil.isNullOrEmpty(row.getDescription())){
                        description=row.getDescription();
                    }else if(!StringUtil.isNullOrEmpty(row.getProduct().getDescription())){
                        description=row.getProduct().getDescription();
                    }else{
                        description="";
                    }
                    obj.put("desc",StringUtil.DecodeText(description));                    
                    if(rowProduct.isAsset()){  //For Fixed Asset Group, type will be "Asset"
                        obj.put("type", "Asset");
                    } else {
                        obj.put("type", rowProduct.getProducttype() == null ? "" :rowProduct.getProducttype().getName());
                        obj.put("prodtype",rowProduct.getProducttype() == null ? "" :rowProduct.getProducttype().getID());// Put producttype for GST calculation in lease
                    }                    
                    obj.put("pid", rowProduct.getProductid());
                    obj.put("memo", row.getRemark());
                    obj.put("invstore", (StringUtil.isNullOrEmpty(row.getInvstoreid())) ? "" : row.getInvstoreid());
                    obj.put("invlocation", (StringUtil.isNullOrEmpty(row.getInvlocid())) ? "" : row.getInvlocid());
                    
                    double totalcost = -1;
                    if(extraCompanyPreferences.isActivateProfitMargin()){
                        KwlReturnObject qdVenRes = accSalesOrderDAOobj.getQuotationDetailsVendorMapping(row.getID());
                        if(qdVenRes.getEntityList().size()>0){
                            QuotationDetailsVendorMapping qdVendObj = (QuotationDetailsVendorMapping)qdVenRes.getEntityList().get(0);
                            obj.put("vendorid", qdVendObj.getVendor()!=null? qdVendObj.getVendor().getID() :"");
                            obj.put("vendorunitcost", qdVendObj.getUnitcost());
                            obj.put("vendorcurrexchangerate", qdVendObj.getExchangerate());
                            obj.put("vendorcurrencyid", qdVendObj.getVendor()!=null ? ( qdVendObj.getVendor().getCurrency()!=null ? qdVendObj.getVendor().getCurrency().getCurrencyID(): gcurrencyid) : "");
                            obj.put("vendorcurrencysymbol", qdVendObj.getVendor()!=null ? ( qdVendObj.getVendor().getCurrency()!=null ? qdVendObj.getVendor().getCurrency().getSymbol(): kwlcurrency.getSymbol()) : "");
                            obj.put("totalcost", qdVendObj.getTotalcost());
                            
                            totalcost = qdVendObj.getTotalcost();
                        }
                    }
                    
                    if(extraCompanyPreferences!=null && extraCompanyPreferences.getUomSchemaType()==Constants.PackagingUOM && rowProduct !=null){
                            Product product=rowProduct;
                            obj.put("caseuom", (product.getPackaging()!=null && product.getPackaging().getCasingUoM()!=null)?product.getPackaging().getCasingUoM().getID():"");
                            obj.put("caseuomvalue", (product.getPackaging()!=null && product.getPackaging().getCasingUoM()!=null)?product.getPackaging().getCasingUomValue():1);
                            obj.put("inneruom", (product.getPackaging()!=null && product.getPackaging().getInnerUoM()!=null)?product.getPackaging().getInnerUoM().getID():"");
                            obj.put("inneruomvalue", (product.getPackaging()!=null && product.getPackaging().getInnerUoM()!=null)?product.getPackaging().getInnerUomValue():1);
                            obj.put("stockuom", (product.getUnitOfMeasure()!=null)?product.getUnitOfMeasure().getID():"");
                    }
                    
                    String productsBaseUomId = (rowProduct.getUnitOfMeasure() == null) ? "" : rowProduct.getUnitOfMeasure().getID();
                    String selectedUomId = (row.getUom() != null) ? row.getUom().getID() : "";

                    if (rowProduct.isblockLooseSell() && !productsBaseUomId.equals(selectedUomId)) {
                        // Get Available Quantity of Product For Selected UOM

                        KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(rowProduct.getID(), selectedUomId);
                        double availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                        obj.put("availableQtyInSelectedUOM", availableQuantity);
                        obj.put("isAnotherUOMSelected", true);
                        
                        // Getting Open PO/SO count
                        
                        HashMap<String, Object> orderParams=new HashMap<String, Object>();            
                        orderParams.put("companyid", companyid);
                        orderParams.put("gcurrencyid", gcurrencyid);
                        orderParams.put("df", df);            
                        orderParams.put("pendingapproval", false);
                        orderParams.put("startdate" ,authHandler.getDates(preferences.getFinancialYearFrom(), true));
                        orderParams.put("enddate" ,authHandler.getDates(preferences.getFinancialYearFrom(), false));
                        orderParams.put("currentuomid" ,selectedUomId);
                        orderParams.put("productId" ,rowProduct.getID());
                        
                        double pocountinselecteduom = getPOCount(orderParams);
                        
                        double socountinselecteduom = getSOCount(orderParams);
                        
                        obj.put("pocountinselecteduom", pocountinselecteduom);
                        obj.put("socountinselecteduom", socountinselecteduom);

                    } else {
                        /**
                         * If isblockLooseSell check from company preference is
                         * false then get the actual quantity of product.
                         */
                        KwlReturnObject Qtyresult = accProductObj.getQuantity(rowProduct.getID());
                        obj.put("availableQtyInSelectedUOM", (Qtyresult.getEntityList().get(0) == null ? 0 : Qtyresult.getEntityList().get(0)));
                    }
                    //obj.put("discountispercent", row.getDiscountispercent());
                    //obj.put("prdiscount", row.getDiscount());
                    obj.put("availablequantity", rowProduct != null ? rowProduct.getAvailableQuantity() : 0);   //SDP-12591
                    
                    if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {  //This is sats specific code 	 
                        obj.put("invoicetype", so.getInvoicetype());

                        obj.put("dependentType", row.getDependentType() == null ? "" : row.getDependentType());
                        
                            obj.put("showquantity", StringUtil.DecodeText(row.getShowquantity() == null ? "" : row.getShowquantity()));
                        
                        obj.put("inouttime", StringUtil.isNullOrEmpty(row.getInouttime()) ? "" : row.getInouttime().replaceAll("%20", " "));
                        if (!StringUtil.isNullOrEmpty(row.getInouttime())) {
                            try {
                                String interVal = getTimeIntervalForProduct(row.getInouttime());
                                obj.put("timeinterval", interVal);
                            } catch (ParseException ex) {
                                Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                        try {
                            String numberVal = row.getDependentType();
                            obj.put("dependentTypeNo", Integer.parseInt(numberVal));
                        } catch (Exception e) {
                        }

                        obj.put("parentid", ((rowProduct.getParent() != null) ?rowProduct.getParent().getID() : ""));
                        obj.put("parentname", ((rowProduct.getParent() != null) ? rowProduct.getParent().getName() : ""));
                        if (rowProduct.getParent() != null) {
                            obj.put("issubproduct", true);
                        }
                        if (rowProduct.getChildren().size() > 0) {
                            obj.put("isparentproduct", true);
                        } else {
                            obj.put("isparentproduct", false);
                        }
                        
                            obj.put("showquantity", StringUtil.DecodeText(row.getShowquantity() == null ? "" : row.getShowquantity()));
                        
                    }
                    if (row.getVendorquotationdetails() != null) {
                        KwlReturnObject vqdetailsresult = accSalesOrderDAOobj.getVendorQuotationDetails(row.getVendorquotationdetails(), companyid);
                        if (!vqdetailsresult.getEntityList().isEmpty()) {
                            Object vq[] = (Object[]) vqdetailsresult.getEntityList().get(0);
                            obj.put("linkto", vq[1]);
                            obj.put("linkid", vq[0]);
                            /*
                                 ERM-1037
                                 Field used for comparing dates to restrict linking of future doument date in Customer Quotation document editing
                             */
                            obj.put("linkDate", vq[2]);
                            obj.put("rowid", row.getVendorquotationdetails());
                            obj.put("savedrowid", row.getID());
                            obj.put("linktype", 0);
                        }
                    } else if(row.getProductReplacementDetail() != null){
                            obj.put("linkto", row.getProductReplacementDetail().getProductReplacement().getReplacementRequestNumber());
                            obj.put("linkid", row.getProductReplacementDetail().getProductReplacement().getId());
                            obj.put("rowid", row.getProductReplacementDetail().getId());
                            obj.put("savedrowid", row.getProductReplacementDetail().getId());
                            obj.put("linktype", 1);
                    }
                    double rowTaxPercent = 0;
                    double rowTaxAmount = 0;
                    boolean isRowTaxApplicable = false;
                    obj.put("recTermAmount",row.getRowtermamount());
                    obj.put("OtherTermNonTaxableAmount",row.getOtherTermNonTaxableAmount());
                    JSONArray TermdetailsjArr = new JSONArray();
                    if(extraCompanyPreferences.getLineLevelTermFlag()==1){ // For India Country 
                        if (extraCompanyPreferences.isAvalaraIntegration()) {
                            JSONObject paramsForTaxJobj = new JSONObject();
                            paramsForTaxJobj.put(IntegrationConstants.parentRecordID, row.getID());
                            TermdetailsjArr = integrationCommonService.getTransactionDetailTaxMapping(paramsForTaxJobj);
                        } else {
                            HashMap<String, Object> quotationDetailParams = new HashMap<String, Object>();
                            quotationDetailParams.put("quotationDetailId", row.getID());
                            KwlReturnObject quotationTermMapresult = accSalesOrderDAOobj.getQuotationDetailTermMap(quotationDetailParams);
                            List<QuotationDetailTermMap> quotationDetailTermsMapList = quotationTermMapresult.getEntityList();
                            for (QuotationDetailTermMap invoicedetailTermMap : quotationDetailTermsMapList) {
                                LineLevelTerms mt = invoicedetailTermMap.getTerm();
                                com.krawler.utils.json.base.JSONObject jsonObj = new com.krawler.utils.json.base.JSONObject();
                                jsonObj.put("id", mt.getId());
                                jsonObj.put("termid", invoicedetailTermMap.getTerm().getId());
                                /**
                                 * ERP-32829 
                                 */
                                jsonObj.put("productentitytermid", invoicedetailTermMap.getEntitybasedLineLevelTermRate()!=null?invoicedetailTermMap.getEntitybasedLineLevelTermRate().getId():"");
                                jsonObj.put("isDefault", invoicedetailTermMap.isIsGSTApplied());
                                jsonObj.put("term", mt.getTerm());
                                jsonObj.put("formulaids", mt.getFormula());
                                jsonObj.put("termamount", invoicedetailTermMap.getTermamount());
                                jsonObj.put("termpercentage", invoicedetailTermMap.getPercentage());
                                jsonObj.put("originalTermPercentage", mt.getPercentage());
                                jsonObj.put("glaccountname", mt.getAccount().getName());
                                jsonObj.put("IsOtherTermTaxable", mt.isOtherTermTaxable());
                                jsonObj.put("accountid", mt.getAccount().getID());
                                jsonObj.put("glaccount", mt.getAccount().getID());
                                jsonObj.put("assessablevalue",invoicedetailTermMap.getAssessablevalue());
                                jsonObj.put("purchasevalueorsalevalue",invoicedetailTermMap.getPurchaseValueOrSaleValue());
                                jsonObj.put("deductionorabatementpercent",invoicedetailTermMap.getDeductionOrAbatementPercent());
                                jsonObj.put("taxtype",invoicedetailTermMap.getTaxType());
                                jsonObj.put("formType", !StringUtil.isNullOrEmpty(mt.getFormType())?mt.getFormType():"1");
    //                            jsonObj.put("taxvalue",invoicedetailTermMap.getTaxType()==0 ? invoicedetailTermMap.getTermamount() : invoicedetailTermMap.getPercentage());
                                jsonObj.put("taxvalue",invoicedetailTermMap.getPercentage());
                                jsonObj.put("termtype",invoicedetailTermMap.getTerm().getTermType());
                                jsonObj.put("termsequence",invoicedetailTermMap.getTerm().getTermSequence());
                                jsonObj.put(IndiaComplianceConstants.GST_CESS_TYPE, invoicedetailTermMap.getEntitybasedLineLevelTermRate()!=null && invoicedetailTermMap.getEntitybasedLineLevelTermRate().getCessType()!=null ? invoicedetailTermMap.getEntitybasedLineLevelTermRate().getCessType().getId() : "");
                                jsonObj.put(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT, invoicedetailTermMap.getEntitybasedLineLevelTermRate()!=null ? invoicedetailTermMap.getEntitybasedLineLevelTermRate().getValuationAmount() : 0.0);
                                jsonObj.put(IndiaComplianceConstants.DEFAULT_TERMID, invoicedetailTermMap.getTerm()!=null && invoicedetailTermMap.getTerm().getDefaultTerms()!=null ? invoicedetailTermMap.getTerm().getDefaultTerms().getId() : "");
                                TermdetailsjArr.put(jsonObj);
                            }
                        }
                        /**
                         * Put GST Tax Class History.
                         */
                        if (row.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                            obj.put("refdocid", row.getID());
                            fieldDataManagercntrl.getGSTTaxClassHistory(obj);
                        }
                    }
                    
                    if(countryid == Constants.indian_country_id || countryid == Constants.USA_country_id){ // For India Country 
                        // Excise AND VAT special Rate type TAX ------ START-------                        
                        boolean carryin = true;
                        String uomid = (row == null) ? "" : (row.getUom() == null) ? "" : row.getUom().getID();
                        if (extraCompanyPreferences.isExciseApplicable() && extraCompanyPreferences.isExciseTariffdetails()) {
                            String reortingUOM= (row.getReportingUOMExcise()!=null)?row.getReportingUOMExcise().getID():"";
                            String valuationType=!StringUtil.isNullOrEmpty(row.getExciseValuationType())?row.getExciseValuationType():"";
                            obj.put("valuationType", valuationType);
                            if ((Constants.QUENTITY).equals(valuationType)) {
                                obj.put("compairwithUOM", 1);
                                obj.put("reortingUOMExcise", reortingUOM);
                                
                                if (row.getReportingSchemaTypeExcise() != null && !reortingUOM.equals(uomid)) {
                                    String reportinguomschema = row.getReportingSchemaTypeExcise().getID();
                                    obj.put("reortingUOMSchemaExcise", reportinguomschema);
                                    HashMap<String, Object> hsMap = new HashMap<String, Object>();
                                    hsMap.put("uomschematypeid", reportinguomschema);
                                    hsMap.put("currentuomid", uomid);
                                    hsMap.put("carryin", carryin);
                                    hsMap.put("companyid", companyid);
                                    KwlReturnObject convertor = accProductObj.getProductBaseUOMRate(hsMap);
                                    List list = convertor.getEntityList();
                                    Iterator itrList = list.iterator();
                                    if (itrList.hasNext()) {
                                        UOMSchema rowPOExcise = (UOMSchema) itrList.next();
                                        if (rowPOExcise != null) {
                                            obj.put("compairwithUOM", rowPOExcise.getBaseuomrate());
                                        }
                                    }
                                }
                            } else if ((Constants.MRP).equals(valuationType)) {
                               obj.put("productMRP",row.getMrpIndia());
                            }
                        }
                        if (extraCompanyPreferences.isEnableVatCst()) {
                            String reortingUOMVAT= (row.getReportingUOMVAT()!=null)? row.getReportingUOMVAT().getID():"";
                            String valuationTypeVAT=!StringUtil.isNullOrEmpty(row.getVatValuationType())?row.getVatValuationType():"";
                            obj.put("valuationTypeVAT", valuationTypeVAT);
                            if ((Constants.QUENTITY).equals(valuationTypeVAT)) {
                                obj.put("compairwithUOMVAT", 1);
                                obj.put("reportingUOMVAT", reortingUOMVAT);
                                if (row.getReportingSchemaVAT() != null && !reortingUOMVAT.equals(uomid)) {
                                    String reportinguomschema = row.getReportingSchemaVAT().getID();
                                    obj.put("reportingUOMSchemaVAT", reportinguomschema);
                                    HashMap<String, Object> hsMap = new HashMap<String, Object>();
                                    hsMap.put("uomschematypeid", reportinguomschema);
                                    hsMap.put("currentuomid", uomid);
                                    hsMap.put("carryin", carryin);
                                    hsMap.put("companyid", companyid);
                                    KwlReturnObject convertor = accProductObj.getProductBaseUOMRate(hsMap);
                                    List list = convertor.getEntityList();
                                    Iterator itrList = list.iterator();
                                    if (itrList.hasNext()) {
                                        UOMSchema rowPOVAT = (UOMSchema) itrList.next();
                                        if (rowPOVAT != null) {
                                            obj.put("compairwithUOMVAT", rowPOVAT.getBaseuomrate());
                                        }
                                    }
                                }

                            } else if ((Constants.MRP).equals(valuationTypeVAT)) {
                                obj.put("productMRP",row.getMrpIndia());
                            }
                        }
                        // Excise AND VAT special type TAX ------ END-------    
                    
                    }
                    obj.put("LineTermdetails",TermdetailsjArr);
                    
                    if (row.getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), so.getQuotationDate(), row.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                        isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                        if (isRowTaxApplicable) {
                            rowTaxAmount = row.getRowTaxAmount();
                        }
                    }
                    /**
                     * If GST tax terms used then get tax amount Row term amount
                     * column
                     */
                    if (extraCompanyPreferences != null && extraCompanyPreferences.isIsNewGST()) {
                        rowTaxAmount = row.getRowtermamount();
                    }
                    obj.put("prtaxpercent", rowTaxPercent);
                    obj.put("rowTaxAmount", rowTaxAmount);

                    double amountWithoutTax = row.getRate();
                    if (row.getQuantity() != 0) {
                        amountWithoutTax = authHandler.round(row.getRate() * row.getQuantity(), companyid);
                    }
                    obj.put("rateIncludingGst", authHandler.roundUnitPrice(row.getRateincludegst(), companyid));
                    obj.put("israteIncludingGst", so.isGstIncluded());
                    obj.put("prtaxid", row.getTax() == null ? "None" : row.getTax().getID());
//                    obj.put("prtaxid", row.getTax() != null ? (isCopy || isForLinking ? (row.getTax().isActivated() ? row.getTax().getID() : "") : row.getTax().getID()) : "None");//ERP-38656
                    obj.put("priceSource", row.getPriceSource() != null ? row.getPriceSource() : "");
                    if (row.getPricingBandMasterid() != null) {
                        KwlReturnObject PricebandResult = accountingHandlerDAOobj.getObject(PricingBandMaster.class.getName(), row.getPricingBandMasterid());
                        PricingBandMaster pricingBandMaster = PricebandResult != null ? (PricingBandMaster) PricebandResult.getEntityList().get(0) : null;
                        obj.put("pricingbandmasterid", pricingBandMaster != null ? pricingBandMaster.getID() : "");
                        obj.put("pricingbandmastername", pricingBandMaster != null ? pricingBandMaster.getName() : "");
                    }

                    obj.put(Constants.isUserModifiedTaxAmount, row.isIsUserModifiedTaxAmount());
                    /**
                     * below code will execute when amend price fuctionality is
                     * activated.
                     */
                    String uomidForamendprice = (row.getUom() != null) ? row.getUom().getID() : rowProduct.getUnitOfMeasure() != null ? rowProduct.getUnitOfMeasure().getID() : "";
                    accProductObj.getamendingPurchaseprice(rowProduct.getID(),userid,row.getQuotation().getQuotationDate(),currency.getCurrencyID(), uomidForamendprice,obj);    
                    /**
                     * get the volume discount discount for the given product
                     * according its quantity.
                     */
                    HashMap<String, Object> pricingDiscountRequestParams = new HashMap<String, Object>();
                    pricingDiscountRequestParams.put("isPricePolicyUseDiscount", true);
                    pricingDiscountRequestParams.put("productID", row.getProduct().getID());
                    pricingDiscountRequestParams.put("isPurchase", false);
                    pricingDiscountRequestParams.put("companyID", companyid);
                    pricingDiscountRequestParams.put("currencyID", gcurrencyid);
                    Double qty = Double.valueOf(row.getQuantity());
                    pricingDiscountRequestParams.put("quantity", Integer.valueOf(qty.intValue()));
                    /**
                     * check Volume discount matches with qty
                     */
                    KwlReturnObject volDiscresult = accProductObj.getProductPriceFromPriceListVolumeDiscount(pricingDiscountRequestParams);
                    if (volDiscresult != null && volDiscresult.getEntityList() != null && !volDiscresult.getEntityList().isEmpty()) {
                        Object[] rowObj = (Object[]) volDiscresult.getEntityList().get(0);
                        KwlReturnObject detailResult = accountingHandlerDAOobj.getObject(PricingBandMasterDetail.class.getName(), (String) rowObj[5]);
                        PricingBandMasterDetail pricingBandMasterDetail = detailResult != null ? (PricingBandMasterDetail) detailResult.getEntityList().get(0) : null;
                        if (pricingBandMasterDetail != null) {
                            obj.put("volumdiscountid", pricingBandMasterDetail.getPricingBandMaster().getID());
                        }
                    }
                    
                    double discountValueForExcel = 0, amountForExcelFile = 0;
                    double rowamountwithgst = 0;
                    if (row.getQuotation().isGstIncluded()) {//if gstincluded is the case
                        rowamountwithgst = authHandler.round(row.getRateincludegst() * row.getQuantity(), companyid);
                        discountValueForExcel = (row.getDiscountispercent() == 1) ? rowamountwithgst * row.getDiscount() / 100 : row.getDiscount();
                        obj.put("discountvalue", (row.getDiscountispercent() == 1) ? (row.getRateincludegst() * row.getQuantity()) * row.getDiscount() / 100 : row.getDiscount());
                        amountForExcelFile = rowamountwithgst - discountValueForExcel;
                        obj.put(Constants.amountForExcelFile, authHandler.formattedAmount(amountForExcelFile, companyid));
                    } else {
                        discountValueForExcel = (row.getDiscountispercent() == 1) ? authHandler.round(((row.getRate() * row.getQuantity()) * row.getDiscount() / 100), companyid) : row.getDiscount();
                        obj.put("discountvalue", (row.getDiscountispercent() == 1) ? (row.getRate() * row.getQuantity()) * row.getDiscount() / 100 : row.getDiscount());
                        amountForExcelFile = (row.getRate() * row.getQuantity()) - discountValueForExcel;
                        amountForExcelFile = amountForExcelFile + rowTaxAmount;
                        obj.put(Constants.amountForExcelFile, authHandler.formattedAmount(amountForExcelFile, companyid));
                    }

                    // Below block is commented as per SDP-4827. Sales order shound not show discounted unit price.
//                    if (!isReport && row.getDiscount() > 0 && isOrder) {//In Sales order creation, we need to display Unit Price including row discount
//                        double discount = (row.getDiscountispercent() == 1) ? (row.getRate() * (row.getDiscount() / 100)) : row.getDiscount();
//                        obj.put("rate", (row.getRate() - discount));
//                        obj.put("discountispercent", 1);
//                        obj.put("prdiscount", 0);
//                    } else {
                        obj.put("rate", row.getRate());
                        obj.put("discountispercent", row.getDiscountispercent());
                        obj.put("prdiscount", row.getDiscount());
                        obj.put("discountjson", row.getDiscountJson() != null ? row.getDiscountJson() : "");                //getting json of multiple discount applied on product ERM-68
//                    }

                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    QuotationDetailCustomData quotationDetailCustomData = (QuotationDetailCustomData) row.getQuotationDetailCustomData();
                    AccountingManager.setCustomColumnValues(quotationDetailCustomData, FieldMap, replaceFieldMap, variableMap);
                    if (quotationDetailCustomData != null) {
                        JSONObject params = new JSONObject();
//                        boolean isExport = false;
//                        if (customIsReport) {
//                            isExport = true;
//                        }
                        if (requestParams.get("sopolinkflag") != null) {
                            isOrder = Boolean.FALSE.parseBoolean(requestParams.get("sopolinkflag").toString());
                        }
                        params.put(Constants.isExport, isExport);
                        params.put("isForReport", isReport);
                        params.put("userdf", userdf);
                        if ((isForLinking || isOrder)) {
                            int moduleId = isOrder ? Constants.Acc_Sales_Order_ModuleId : Constants.Acc_Invoice_ModuleId;
                            params.put("linkModuleId", moduleId);
                            params.put("isLink", isForLinking);
                            params.put("companyid", companyid);
                        }
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                    
                    
//                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
//                        String coldata = varEntry.getValue().toString();
//                        String valueForReport = "";
//                        if (customFieldMap.containsKey(varEntry.getKey()) && customIsReport && coldata != null) {
//                            try {
//                                String[] valueData = coldata.split(",");
//                                for (String value : valueData) {
//                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
//                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
//                                    if (fieldComboData != null) {
//                                        valueForReport += fieldComboData.getValue() + ",";
//                                    }
//                                }
//                                if (valueForReport.length() > 1) {
//                                    valueForReport = valueForReport.substring(0, valueForReport.length() - 1);
//                                }
//                                if ((isForLinking || isOrder)) {
//                                    int moduleId = Constants.Acc_Sales_Order_ModuleId;
//                                    valueForReport = fieldDataManagercntrl.getValuesForLinkRecords(moduleId, companyid, varEntry.getKey(), valueForReport);
//                                }
//                                obj.put(varEntry.getKey(), valueForReport);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
//                            } catch (Exception ex) {
//                                obj.put(varEntry.getKey(), coldata);
//                            }
//                        } else if (customDateFieldMap.containsKey(varEntry.getKey()) && customIsReport) {
//                            DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
//                            long milliSeconds = Long.parseLong(coldata);
//                            coldata = df2.format(milliSeconds);
//                            obj.put(varEntry.getKey(), coldata);
//                        } else {
//                            if (!StringUtil.isNullOrEmpty(coldata)) {
//                                obj.put(varEntry.getKey(), coldata);
//                            }
//                        }
//                    }

//                    KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), so.getQuotationDate(), 0);
                    
                    // Get Product level Custom field data
                    HashMap<String, Object> fieldrequestParamsProduct = new HashMap();
                    HashMap<String, String> customProductFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customProductDateFieldMap = new HashMap<String, String>();
                    Map<String, Object> variableMapProduct = new HashMap<String, Object>();
                    fieldrequestParamsProduct.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParamsProduct.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                    HashMap<String, String> replaceFieldMapProduct = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMapProduct = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParamsProduct, replaceFieldMapProduct, customProductFieldMap, customProductDateFieldMap);
                    QuotationDetailsProductCustomData quotationDetailProductCustomData = (QuotationDetailsProductCustomData) row.getQuotationDetailProductCustomData();
                    AccountingManager.setCustomColumnValues(quotationDetailProductCustomData, FieldMapProduct, replaceFieldMapProduct, variableMapProduct);
                    
                    if (quotationDetailProductCustomData != null) {
                        JSONObject params = new JSONObject();
//                        boolean isExport = false;
//                        if (customIsReport) {
//                            isExport = true;
//                        }
                        if (requestParams.get("sopolinkflag") != null) {
                            isOrder = Boolean.FALSE.parseBoolean(requestParams.get("sopolinkflag").toString());
                        }
                        if (isForLinking) {
                            isExport = false;
                        }
                        params.put(Constants.isExport, isExport);
                        params.put("isForReport", isReport);
                        params.put("userdf", userdf);
                        if ((isForLinking || isOrder)) {      // No need to send linkmoduleId as fieldid is same for all relatedmodules.
                            int moduleId = Constants.Acc_Product_Master_ModuleId;
                            params.put("linkModuleId", moduleId);
                            params.put("isLink", isForLinking);
                            params.put("companyid", companyid);
                            params.put("customcolumn", 0);
                        }
                        fieldDataManagercntrl.getLineLevelCustomData(variableMapProduct, customProductFieldMap, customProductDateFieldMap, obj, params);
                    }
                   
                    obj.put(Constants.unitpriceForExcelFile, row.getRate());//obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
                    double baseuomrate = row.getBaseuomrate();
                    double quantity = 0;
                    double invoiceRowProductQty = authHandler.calculateBaseUOMQuatity(row.getQuantity(),baseuomrate, companyid);
                    double remainedQty = invoiceRowProductQty;// which has not been linked yet
                    if (closeflag != null) {
                        addobj = (soflag ||isLeaseFixedAsset) ? getQuotationDetailStatusSO(row) : getQuotationDetailStatusINV(row);
                        quantity = addobj;
                        obj.put("quantity", addobj);
                        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(addobj, baseuomrate, companyid));
                        remainedQty = authHandler.calculateBaseUOMQuatity(addobj,baseuomrate, companyid);

                    } else {
                        quantity = row.getQuantity();
                        obj.put("quantity", quantity);
                        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, baseuomrate, companyid));

                    }
                    if (isForLinking) {// in case of linking in normal transactions not lease consignment etc.
                        if(row.getTax()!=null && invoiceRowProductQty>0){
                            double taxAmt = (rowTaxAmount/invoiceRowProductQty)*remainedQty;
                            obj.put("rowTaxAmount", taxAmt);
                            obj.put("taxamount", taxAmt);
                        }
                    }
                    if (row.getUom() != null) {
                        obj.put("uomid", row.getUom().getID());
                        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, row.getBaseuomrate(), companyid));
                        obj.put("baseuomrate", row.getBaseuomrate());
                    } else {
                        obj.put("uomid", rowProduct.getUnitOfMeasure() != null ? rowProduct.getUnitOfMeasure().getID() : "");
                        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, row.getBaseuomrate(), companyid));
                        obj.put("baseuomrate", row.getBaseuomrate());
                    }
                                                            
                    JSONObject jObj = new JSONObject();
                    if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                        jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                    }
                    if (jObj.has("isDisplayUOM") && jObj.get("isDisplayUOM") != null && (Boolean) jObj.get("isDisplayUOM") != false) {
                        obj = accProductObj.getProductDisplayUOM(row.getProduct(), quantity, baseuomrate, false, obj);
                    }
                    // To get margin cost from cross link document
                    double marginCost = -1;
                    String marginCostCurrency = "";
                    HashMap<String, Object> unitPriceParams = new HashMap<>();
                    unitPriceParams.put("qDetailID", row.getID());
                    KwlReturnObject unitPriceResult = accSalesOrderDAOobj.getMarginCostForCrossLinkedTransactions(unitPriceParams);
                    if (!unitPriceResult.getEntityList().isEmpty() && unitPriceResult.getEntityList().get(0) != null) {
                        Object[] objArr = (Object[]) unitPriceResult.getEntityList().get(0);
                        obj.put("marginCost", objArr[0]);
                        obj.put("marginExchangeRate", objArr[1]);
                        
                        marginCost = objArr[0] != null ? (Double) objArr[0] : 0;
                        marginCostCurrency = objArr[2] != null ? (String) objArr[2] : "";
                    }
                    
                    // For getting Cost and Margin
                    double amount = authHandler.round(row.getRate() * row.getQuantity(), companyid);
                    double cost = 0;
                    if (totalcost != -1) { // If Unit Cost is enterted then cost is totalcost
                        cost = totalcost;
                    } else if (marginCost != -1) { // if CQ is linked to VQ then cost is marginCost
                        KwlReturnObject costResult = accCurrencyobj.getOneCurrencyToOther(requestParams, marginCost, marginCostCurrency, row.getQuotation().getCurrency().getCurrencyID(), row.getQuotation().getQuotationDate(), 0);
                        marginCost = costResult.getEntityList()!= null ? (costResult.getEntityList().get(0)!=null ? (Double) costResult.getEntityList().get(0) : 0 ): 0;
                        cost = authHandler.round(marginCost * quantity, companyid);
                    } else { // if above case are not satisfied then cost is products purchase price
                        KwlReturnObject purchase = accProductObj.getProductPrice(row.getProduct().getID(), true, null, "", "");
                        double purchaseprice = 0; 
                        if (purchase.getEntityList()!= null && !purchase.getEntityList().isEmpty() && purchase.getEntityList().get(0) != null) {//Addde null check 
                            purchaseprice = (Double) purchase.getEntityList().get(0);
                        }
                        cost = authHandler.round(purchaseprice * quantity, companyid);
                    }
                    double margin = amount - cost;
                    obj.put("cost", cost);
                    obj.put("margin", margin);
                    
//                    if (addobj > 0) {
//                        jArr.put(obj); //SDP-10772
//                    }
                    jArr.put(obj);
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (ParseException ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }/* catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        return jobj;
    }

    public String getTimeIntervalForProduct(String inouttime) throws ParseException, java.text.ParseException {
        Date date = new Date();
        if (!StringUtil.isNullOrEmpty(inouttime)) {
            inouttime = inouttime.replaceAll("%20", " ");
        }
        inouttime = inouttime.replaceAll("%25", " ");
        String inoutTime = inouttime;
        String inoutTimeArray[] = inoutTime.split(",");
        String inTimeArray[] = inoutTimeArray[0].split(" ");
        String outTimeArray[] = inoutTimeArray[1].split(" ");
        int inHour = 0;
        String inDateValue = "";
        String outDateValue = "";
        int inMinutes = 0;
        int outHour = 0;
        int outMinutes = 0;
        if (inoutTimeArray.length > 1) {
            inDateValue = inTimeArray[0];
            outDateValue = outTimeArray[0];
            inHour = Integer.parseInt(inTimeArray[1].split(":")[0]);
            inMinutes = Integer.parseInt(inTimeArray[1].split(":")[1]);
            outHour = Integer.parseInt(outTimeArray[1].split(":")[0]);
            outMinutes = Integer.parseInt(outTimeArray[1].split(":")[1]);
        }
        DateFormat df = new SimpleDateFormat(Constants.yyyyMMdd);
//        GregorianCalendar calendar= new GregorianCalendar();
        Date inDate = df.parse(inDateValue);
        Date outDate = df.parse(outDateValue);
        inDate.setHours(inHour);
        inDate.setMinutes(inMinutes);
        inDate.setSeconds(00);

        outDate.setHours(outHour);
        outDate.setMinutes(outMinutes);
        outDate.setSeconds(00);
        double timeDiff = ((double) (outDate.getTime() - inDate.getTime())) / 3600000;
        DecimalFormat df1 = new DecimalFormat("#.##");
        timeDiff = Double.valueOf(df1.format(timeDiff));
        return timeDiff + " Hrs";

    }
    
    
    @Override
    public double getPOCount(HashMap<String, Object> orderParams) throws ServiceException, JSONException, SessionExpiredException {

        String currentuomid = (String) orderParams.get("currentuomid");
        String productId = (String) orderParams.get("productId");
        
        try {
            if (orderParams.get("startdate") != null) {
                orderParams.put("startdate", authHandler.getDateOnlyFormat().parse(orderParams.get("startdate").toString()));
            }
            if (orderParams.get("enddate") != null) {
                orderParams.put("enddate", authHandler.getDateOnlyFormat().parse(orderParams.get("enddate").toString()));
            }
        } catch (ParseException ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        double productCount = AccProductService.getOutstandingPoSoProductsCount(orderParams, true, productId, accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj,  accSalesOrderDAOobj, accPurchaseOrderobj, currentuomid);

        return productCount;
    }
    
    @Override
    public double getSOCount(HashMap<String, Object> orderParams) throws ServiceException, JSONException, SessionExpiredException {

        String currentuomid = (String) orderParams.get("currentuomid");
        String productId = (String) orderParams.get("productId");
        
        try {
            if (orderParams.get("startdate") != null) {
                orderParams.put("startdate", authHandler.getDateOnlyFormat().parse(orderParams.get("startdate").toString()));
            }
            if (orderParams.get("enddate") != null) {
                orderParams.put("enddate", authHandler.getDateOnlyFormat().parse(orderParams.get("enddate").toString()));
            }
        } catch (ParseException ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        double productCount = AccProductService.getOutstandingPoSoProductsCount(orderParams, false, productId, accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj,  accSalesOrderDAOobj, accPurchaseOrderobj, currentuomid);
        return productCount;
    }
    
    @Override
    public HashMap<String, Object> getSalesOrdersMap (HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        //checking filetype to print all records for csv,print & pdf
        if (StringUtil.isNullOrEmpty(request.getParameter("filetype"))) {
            requestParams.put(Constants.start, request.getParameter(Constants.start));
            requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        }
        requestParams.put(Constants.df,authHandler.getDateOnlyFormat(request));
        requestParams.put(Constants.userdf,authHandler.getUserDateFormatter(request));
        requestParams.put(CCConstants.REQ_costCenterId,request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.REQ_customerId,request.getParameter(Constants.REQ_customerId));        
        requestParams.put(Constants.REQ_startdate ,request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate ,request.getParameter(Constants.REQ_enddate));
        requestParams.put(Constants.MARKED_FAVOURITE ,request.getParameter(Constants.MARKED_FAVOURITE));
        requestParams.put(InvoiceConstants.newcustomerid, request.getParameter(InvoiceConstants.newcustomerid));
        requestParams.put(InvoiceConstants.productid, request.getParameter(InvoiceConstants.productid));
        requestParams.put(InvoiceConstants.productCategoryid, request.getParameter(InvoiceConstants.productCategoryid));
        requestParams.put(Constants.isRepeatedFlag, request.getParameter(Constants.isRepeatedFlag));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        requestParams.put("orderforcontract", request.getParameter("orderForContract")!=null?Boolean.parseBoolean(request.getParameter("orderForContract")):false);
        requestParams.put(Constants.ValidFlag, request.getParameter(Constants.ValidFlag));
        requestParams.put(Constants.BillDate ,request.getParameter(Constants.BillDate));
        requestParams.put("pendingapproval" ,(request.getParameter("pendingapproval") != null)? Boolean.parseBoolean(request.getParameter("pendingapproval")): false);
        requestParams.put("istemplate" ,(request.getParameter("istemplate") != null)? Integer.parseInt(request.getParameter("istemplate")): 0);
        requestParams.put("currencyid",request.getParameter("currencyid"));
        requestParams.put("exceptFlagINV" ,request.getParameter("exceptFlagINV"));
        requestParams.put("exceptFlagORD" ,request.getParameter("exceptFlagORD"));
        requestParams.put("linkFlagInSO" ,request.getParameter("linkFlagInSO"));
        requestParams.put("linkFlagInInv" ,request.getParameter("linkFlagInInv"));
        requestParams.put(Constants.Acc_Search_Json ,request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria ,request.getParameter(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid ,request.getParameter(Constants.moduleid));
        requestParams.put("currencyfilterfortrans", (request.getParameter("currencyfilterfortrans") == null)? "" : request.getParameter("currencyfilterfortrans"));
        requestParams.put("isOpeningBalanceOrder", request.getParameter("isOpeningBalanceOrder")!=null?Boolean.parseBoolean(request.getParameter("isOpeningBalanceOrder")):false);
        requestParams.put("isLeaseFixedAsset", request.getParameter("isLeaseFixedAsset")!=null?Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")):false);
        requestParams.put("isConsignment", request.getParameter("isConsignment")!=null?Boolean.parseBoolean(request.getParameter("isConsignment")):false);
        requestParams.put("custWarehouse", (request.getParameter("custWarehouse") == null)? "" : request.getParameter("custWarehouse"));
        requestParams.put(CCConstants.REQ_customerId,request.getParameter(CCConstants.REQ_customerId));
        requestParams.put(Constants.customerCategoryid, request.getParameter(Constants.customerCategoryid));
        requestParams.put("billId",request.getParameter(Constants.billid));
        requestParams.put(Constants.checksoforcustomer,StringUtil.isNullOrEmpty(request.getParameter(Constants.checksoforcustomer)) ? false : Boolean.parseBoolean(request.getParameter(Constants.checksoforcustomer)));
        if(request.getParameter("includingGSTFilter")!=null){
            requestParams.put("includingGSTFilter",Boolean.parseBoolean(request.getParameter("includingGSTFilter")));
        }
        if (request.getParameter(Constants.generatedSource) != null) {
            requestParams.put(Constants.generatedSource, (!StringUtil.isNullOrEmpty(request.getParameter(Constants.generatedSource))) ? Integer.parseInt(request.getParameter(Constants.generatedSource)) : null);
        }
            
        return requestParams;
    }
    
    @Override
    public HashMap<String, Object> getSalesOrdersMapJson (JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
        requestParams.put(Constants.ss, StringUtil.DecodeText(paramJobj.optString(Constants.ss)));
        //checking filetype to print all records for csv,print & pdf
        if (StringUtil.isNullOrEmpty(paramJobj.optString("filetype",null))) {
            if(paramJobj.has(Constants.start) && paramJobj.has(Constants.limit)){
            requestParams.put(Constants.start, paramJobj.getString(Constants.start));
            requestParams.put(Constants.limit, paramJobj.getString(Constants.limit));
            }
        }
        requestParams.put(Constants.df,authHandler.getDateOnlyFormat());
        requestParams.put(Constants.userdf,authHandler.getUserDateFormatterJson(paramJobj));
        requestParams.put(CCConstants.REQ_costCenterId,paramJobj.optString(CCConstants.REQ_costCenterId,null));
        requestParams.put(Constants.REQ_customerId,paramJobj.optString(Constants.REQ_customerId,null));        
        requestParams.put(Constants.REQ_startdate ,paramJobj.optString(Constants.REQ_startdate,null)!=null?StringUtil.DecodeText((String) paramJobj.get(Constants.REQ_startdate)):null);
        requestParams.put(Constants.REQ_enddate ,paramJobj.optString(Constants.REQ_enddate,null)!=null?StringUtil.DecodeText((String) paramJobj.get(Constants.REQ_enddate)):null);
        requestParams.put(Constants.MARKED_FAVOURITE ,paramJobj.optString(Constants.MARKED_FAVOURITE,null));
        requestParams.put(InvoiceConstants.newcustomerid, paramJobj.optString(InvoiceConstants.newcustomerid,null));
        requestParams.put(InvoiceConstants.productid, paramJobj.optString(InvoiceConstants.productid,null));
        requestParams.put(InvoiceConstants.productCategoryid, paramJobj.optString(InvoiceConstants.productCategoryid,null));
        requestParams.put(Constants.isRepeatedFlag, paramJobj.optString(Constants.isRepeatedFlag,null));
        requestParams.put("deleted", paramJobj.optString("deleted",null));
        requestParams.put("nondeleted", paramJobj.optString("nondeleted",null));
        requestParams.put("orderforcontract", paramJobj.optString("orderForContract",null)!=null?Boolean.parseBoolean(paramJobj.getString("orderForContract")):false);
        requestParams.put(Constants.ValidFlag, paramJobj.optString(Constants.ValidFlag,null));
        requestParams.put(Constants.BillDate ,paramJobj.optString(Constants.BillDate,null));
        requestParams.put("pendingapproval" ,(paramJobj.optString("pendingapproval",null) != null)? Boolean.parseBoolean(paramJobj.getString("pendingapproval")): false);
        requestParams.put("istemplate" ,(paramJobj.optString("istemplate",null) != null)? Integer.parseInt(paramJobj.getString("istemplate")): 0);
        requestParams.put("currencyid",paramJobj.optString("currencyid",null));
        requestParams.put("exceptFlagINV" ,paramJobj.optString("exceptFlagINV",null));
        requestParams.put("exceptFlagORD" ,paramJobj.optString("exceptFlagORD",null));
        requestParams.put("linkFlagInSO" ,paramJobj.optString("linkFlagInSO",null));
        requestParams.put("linkFlagInInv" ,paramJobj.optString("linkFlagInInv",null));
        requestParams.put("linkflag" ,paramJobj.optString("linkflag",null));
        requestParams.put(Constants.Acc_Search_Json ,paramJobj.optString(Constants.Acc_Search_Json,null));
        requestParams.put(Constants.Filter_Criteria ,paramJobj.optString(Constants.Filter_Criteria,null));
        requestParams.put(Constants.moduleid ,paramJobj.optString(Constants.moduleid,null));
        requestParams.put("currencyfilterfortrans", (paramJobj.optString("currencyfilterfortrans",null) == null)? "" : paramJobj.getString("currencyfilterfortrans"));
        requestParams.put("isOpeningBalanceOrder", paramJobj.optString("isOpeningBalanceOrder",null)!=null?Boolean.parseBoolean(paramJobj.getString("isOpeningBalanceOrder")):false);
        requestParams.put("isLeaseFixedAsset", paramJobj.optString("isLeaseFixedAsset",null)!=null?Boolean.parseBoolean(paramJobj.getString("isLeaseFixedAsset")):false);
        requestParams.put("isConsignment", paramJobj.optString("isConsignment",null)!=null?Boolean.parseBoolean(paramJobj.getString("isConsignment")):false);
        requestParams.put("isJobWorkOrderReciever", paramJobj.optString("isJobWorkOrderReciever",null)!=null?Boolean.parseBoolean(paramJobj.getString("isJobWorkOrderReciever")):false);
        requestParams.put("isMRPSalesOrder", paramJobj.optString("isMRPSalesOrder",null)!=null?Boolean.parseBoolean(paramJobj.getString("isMRPSalesOrder")):false);
        requestParams.put("custWarehouse", (paramJobj.optString("custWarehouse",null) == null)? "" : paramJobj.getString("custWarehouse"));
        requestParams.put("movementtype", (paramJobj.optString("movementtype",null)==null)? "" : paramJobj.optString("movementtype",""));
        requestParams.put(CCConstants.REQ_customerId,paramJobj.optString(CCConstants.REQ_customerId,null));
        requestParams.put(Constants.customerCategoryid, paramJobj.optString(Constants.customerCategoryid,null));
        requestParams.put("billId",paramJobj.optString(Constants.billid,null));
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("dir", null))&& !StringUtil.isNullOrEmpty(paramJobj.optString("sort", null)) ) {
            requestParams.put("dir", paramJobj.getString("dir"));
            requestParams.put("sort", paramJobj.getString("sort"));
        }
        requestParams.put("blockedDocuments",paramJobj.optString("blockedDocuments",null));
        requestParams.put("unblockedDocuments",paramJobj.optString("unblockedDocuments",null));
        requestParams.put(Constants.checksoforcustomer,StringUtil.isNullOrEmpty(paramJobj.optString(Constants.checksoforcustomer,null)) ? false : Boolean.parseBoolean(paramJobj.getString(Constants.checksoforcustomer)));
        if(paramJobj.optString("includingGSTFilter",null)!=null){
            requestParams.put("includingGSTFilter",Boolean.parseBoolean(paramJobj.getString("includingGSTFilter")));
        }
        if (paramJobj.optString("requestModuleid",null)!= null) {
            requestParams.put("requestModuleid",Boolean.parseBoolean(paramJobj.getString("requestModuleid")));
        }
        requestParams.put("isDraft", (paramJobj.optString("isDraft",null) != null) ? Boolean.parseBoolean(paramJobj.optString("isDraft","")) : false);   
        requestParams.put("joborderitem", (paramJobj.optString("joborderitem",null) != null) ? Boolean.parseBoolean(paramJobj.optString("joborderitem","")) : false);   
        requestParams.put(Constants.generatedSource, (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.generatedSource,null))) ? Integer.parseInt(paramJobj.optString(Constants.generatedSource,Constants.RECORD_WEB_Application)) :null);
            
        return requestParams;
    }
    @Override
    //Modified get SalesOrdersJsonMerged
     public JSONArray getSalesOrdersJsonMerged(JSONObject paramJobj, List<Object[]> list, JSONArray jArr) throws ServiceException {
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMapJson(paramJobj);
            boolean closeflag = (paramJobj.optString("closeflag",null)!=null)?true:false;
            boolean isLeaseSO=(paramJobj.optString("isLeaseFixedAsset",null)!=null)?Boolean.FALSE.parseBoolean((String)paramJobj.get("isLeaseFixedAsset")):false;
            boolean isConsignment= (paramJobj.optString("isConsignment",null)!=null)?Boolean.FALSE.parseBoolean((String)paramJobj.get("isConsignment")):false;
            boolean includepending= (paramJobj.optString("includepending",null)!=null)?Boolean.FALSE.parseBoolean((String)paramJobj.get("includepending")):false;
            boolean getlineItemDetailsflag= (paramJobj.optString(Constants.getlineItemDetailsflag,null)!=null)?Boolean.FALSE.parseBoolean((String)paramJobj.get(Constants.getlineItemDetailsflag)):false;
            boolean isJobWorkOrderReciever=false;    
            if (requestParams.containsKey("isJobWorkOrderReciever")) {
                isJobWorkOrderReciever = Boolean.parseBoolean(requestParams.get("isJobWorkOrderReciever").toString());
            } 
            DateFormat userDateFormat=null;
            if(paramJobj.has(Constants.userdateformat) && (paramJobj.get(Constants.userdateformat) != null) ){
                userDateFormat=new SimpleDateFormat((String)paramJobj.get(Constants.userdateformat));
            }
            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            String companyid = paramJobj.getString(Constants.companyKey);
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String startDate =(paramJobj.has(Constants.REQ_startdate))? StringUtil.DecodeText((String) paramJobj.getString(Constants.REQ_startdate)):"";
            String toDate = (paramJobj.has(Constants.REQ_enddate))?StringUtil.DecodeText((String) paramJobj.getString(Constants.REQ_enddate)):"";
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            DateFormat userdf = (DateFormat) requestParams.get(Constants.userdf);
            String requestStatus=paramJobj.optString("requestStatus",null);
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid,isLeaseSO ?Constants.Acc_Lease_Order_ModuleId:isConsignment?Constants.Acc_ConsignmentRequest_ModuleId:isJobWorkOrderReciever?Constants.VENDOR_JOB_WORKORDER_MODULEID:Constants.Acc_Sales_Order_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
            boolean isOutstanding = (paramJobj.has("isOutstanding") && paramJobj.get("isOutstanding")!=null)?Boolean.parseBoolean((String)paramJobj.get("isOutstanding")):false;
            boolean isOutstandingproduct = paramJobj.optString("isOuststandingproduct",null)!=null?Boolean.parseBoolean(paramJobj.getString("isOuststandingproduct")):false; 
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            boolean isForTemplate = paramJobj.optBoolean("isForTemplate",false); 
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            /*
            created DocumentEmailSetting Object for getting customershippingaddress flag
            */
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), companyid);
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            boolean isLineLevelTermFlag = false;//To Check Whether LinelevelTerms are applicable or not.
            if (extraCompanyPreferences != null && extraCompanyPreferences.getLineLevelTermFlag() == 1) {
                isLineLevelTermFlag = true;//If LineLevelTerms are applicable, then update the flag.
            }
            
            Map<String, String> statusMp = new HashMap<String,String>();
            if(!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(toDate)){
              statusMp=  accSalesOrderDAOobj.getApprovalStatusofSO(companyid, df.parse(startDate),df.parse(toDate),requestStatus);
            }
           
            for (Object[] oj : list) {
                JSONObject obj = new JSONObject();
                String orderid = oj[0].toString();
                //Withoutinventory 0 for normal, 1 for billing
                boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), orderid);
                    SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
                    if (!StringUtil.isNullOrEmpty(requestStatus) && "Rejected".equals(requestStatus) && statusMp != null && statusMp.size() > 0) {
                    if (!statusMp.containsKey(salesOrder.getSalesOrderNumber())) {
                        continue;
                    }
                    }
                    KWLCurrency currency = null;
                    if(salesOrder.getCurrency() != null){
                        currency = salesOrder.getCurrency();
                    } else {
                        currency=salesOrder.getCustomer().getAccount().getCurrency()==null?kwlcurrency:salesOrder.getCustomer().getAccount().getCurrency();
                    }
                    //KWLCurrency currency=salesOrder.getCustomer().getAccount().getCurrency()==null?kwlcurrency:salesOrder.getCustomer().getAccount().getCurrency();
                    HashMap<String,Object> hashMap=new HashMap<String, Object>();
                    hashMap.put("invoiceID",salesOrder.getID());
                    hashMap.put(Constants.companyKey,salesOrder.getCompany().getCompanyID());
                    KwlReturnObject object=accInvoiceDAOobj.getinvoiceDocuments(hashMap);
                    int attachemntcount=object.getRecordTotalCount();
                    Customer customer=salesOrder.getCustomer();
                   
                    obj.put(Constants.billid, salesOrder.getID());
                    obj.put(IntegrationConstants.totalShippingCost, salesOrder.getTotalShippingCost());
                    obj.put(Constants.companyKey, salesOrder.getCompany().getCompanyID());
                    /**
                     * Put GST document history.
                     */
                    if (salesOrder.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                        obj.put("refdocid", salesOrder.getID());
                        fieldDataManagercntrl.getGSTDocumentHistory(obj);
                        /**
                         * Put Merchant Exporter Check
                         */
                        obj.put(Constants.isMerchantExporter, salesOrder.isIsMerchantExporter());
                    }
                    //get LineItem Details for Rest IntegrationService
                    if (getlineItemDetailsflag) {
                        JSONArray DataRowsArr = new JSONArray();
                        JSONObject jobj = new JSONObject();
                        paramJobj.put(Constants.billid,salesOrder.getID());
                        jobj = getSalesOrderRows(paramJobj);
                        DataRowsArr = jobj.getJSONArray(Constants.RES_data);
                        obj.put(Constants.lineItemDetails, DataRowsArr);
                    }
                    
                //used in Rest Services --This are the dataindex of all import
                if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    obj.put("salesOrderNumber", salesOrder.getSalesOrderNumber());
                    obj.put("OrderDate", authHandler.getDateOnlyFormat().format(salesOrder.getOrderDate()));
                    obj.put(Constants.customerName, customer.getID());
                    obj.put(Constants.customerNameValue, customer.getName());
                    obj.put(Constants.currencyName, currency.getCurrencyID());
                    obj.put(Constants.currencyNameValue, currency.getName());
                    obj.put(Constants.costcenter, salesOrder.getCostcenter() == null ? "" : salesOrder.getCostcenter().getID());
                    obj.put(Constants.costcenterValue, salesOrder.getCostcenter() == null ? "" : salesOrder.getCostcenter().getName());
                    obj.put(Constants.salesperson, salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getID());
                    obj.put(Constants.salespersonValue, salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getValue());
                    obj.put("customerPORefNo", salesOrder.getCustomerPORefNo());
                    obj.put("customerCode", customer.getAcccode() == null ? "" : customer.getAcccode());
                    obj.put(Constants.sequenceformat, salesOrder.getSeqformat() == null ? "NA" : salesOrder.getSeqformat().getID());
                    
                    
                    if (salesOrder.getSeqformat() != null) {
                        KwlReturnObject seqObj = accountingHandlerDAOobj.getObject(SequenceFormat.class.getName(), salesOrder.getSeqformat().getID());
                        SequenceFormat seqFormat = (SequenceFormat) seqObj.getEntityList().get(0);
                        if (seqFormat != null) {
                            String formatName = seqFormat.getName();
                            if (seqFormat.isDateBeforePrefix() && seqFormat.isShowDateFormatAfterSuffix()) {
                                formatName = seqFormat.getDateformatinprefix() + seqFormat.getName() + seqFormat.getDateFormatAfterSuffix();
                            } else if (seqFormat.isDateBeforePrefix()) {
                                formatName = seqFormat.getDateformatinprefix() + seqFormat.getName();
                            } else if (seqFormat.isShowDateFormatAfterSuffix()) {
                                formatName = seqFormat.getName() + seqFormat.getDateFormatAfterSuffix();
                            }
                            obj.put(Constants.sequenceformatValue, salesOrder.getSeqformat() == null ? "NA" : formatName);
                        } else {
                            obj.put(Constants.sequenceformatValue, salesOrder.getSeqformat() == null ? "NA" : salesOrder.getSeqformat().getName());
                        }
                    } else {
                        obj.put(Constants.sequenceformatValue,"NA");
                    }
                    
                    obj.put("term", salesOrder.getTerm()!=null?salesOrder.getTerm().getID():"");
                    obj.put("termValue", salesOrder.getTerm()!=null?salesOrder.getTerm().getTermname():"");
                    obj.put("islockQuantity", salesOrder.isLockquantityflag());  //for getting locked flag of indivisual so
                
                } else {
                    obj.put(Constants.billno, salesOrder.getSalesOrderNumber());
                    obj.put("date", authHandler.getDateOnlyFormat().format(salesOrder.getOrderDate()));
                    obj.put("customerporefno", salesOrder.getCustomerPORefNo());
                    obj.put("customercode", customer.getAcccode() == null ? "" : customer.getAcccode());
                    obj.put(Constants.PERSONCODE, customer.getAcccode() == null ? "" : customer.getAcccode());
                    obj.put("costcenterid", salesOrder.getCostcenter() == null ? "" : salesOrder.getCostcenter().getID());
                    obj.put(Constants.SEQUENCEFORMATID, salesOrder.getSeqformat() == null ? "" : salesOrder.getSeqformat().getID());
                    obj.put(Constants.currencyKey, currency.getCurrencyID());
                    obj.put("salesPerson", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getID());
                    obj.put("personname", customer.getName());
                    obj.put("costcenterName", salesOrder.getCostcenter() == null ? "" : salesOrder.getCostcenter().getName());
                    obj.put("salespersonname", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getValue());
                    obj.put("termid", salesOrder.getTerm()!=null?salesOrder.getTerm().getID():"");
                    obj.put("islockQuantityflag", salesOrder.isLockquantityflag());  //for getting locked flag of indivisual so
                }
                    obj.put(Constants.IsRoundingAdjustmentApplied, salesOrder.isIsRoundingAdjustmentApplied());
                    obj.put("aliasname", customer.getAliasname());
                    obj.put("gstapplicable", salesOrder.isIsIndGSTApplied());
                    obj.put("isfavourite", salesOrder.isFavourite());
                    obj.put("isprinted", salesOrder.isPrinted());
                    obj.put("isEmailSent", salesOrder.isIsEmailSent());
                    obj.put("billto", salesOrder.getBillTo());
                    obj.put("shipto", salesOrder.getShipTo());
                    obj.put("salesPersonCode", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getCode());
                    obj.put("createdby", StringUtil.getFullName(salesOrder.getCreatedby()));
                    obj.put("createdbyid", salesOrder.getCreatedby().getUserID());
                    obj.put("deleted", salesOrder.isDeleted());
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("currencycode", currency.getCurrencyCode());
                    obj.put("personemail", customer.getEmail());
                    obj.put(Constants.memo, salesOrder.getMemo());
                    obj.put(Constants.posttext, salesOrder.getPostText());
                    obj.put(Constants.isDraft, salesOrder.isIsDraft());
                    obj.put("companyname", salesOrder.getCompany().getCompanyName());
                    obj.put("externalcurrencyrate", salesOrder.getExternalCurrencyRate());
                    obj.put("withoutinventory", withoutinventory);
                    obj.put("personid", customer.getID());
                    obj.put(Constants.HAS_ACCESS, customer.isActivate());
                    obj.put(Constants.duedate, authHandler.getDateOnlyFormat().format(salesOrder.getDueDate()));
                    obj.put("duedateinuserformat", userdf.format(salesOrder.getDueDate()));
                    obj.put("dateinuserformat", userdf.format(salesOrder.getOrderDate()));
                    obj.put(Constants.shipdate, salesOrder.getShipdate()==null? "" : authHandler.getDateOnlyFormat().format(salesOrder.getShipdate()));
                    obj.put("shipdateinuserformat", salesOrder.getShipdate()==null? "" :userdf.format(salesOrder.getShipdate()));
                    obj.put(Constants.shipvia, salesOrder.getShipvia());
                    obj.put(Constants.fob, salesOrder.getFob());
                    obj.put("isOpeningBalanceTransaction", salesOrder.isIsOpeningBalanceSO());
                    obj.put("isConsignment", salesOrder.isIsconsignment());
                    obj.put("statusforcrosslinkage", salesOrder.isDisabledSOforPO()?"Closed":"Open");
                    obj.put("closedmanually", salesOrder.isIsSOClosed()?"Yes":"N/A");
                    obj.put("parentso", salesOrder.getParentSO()==null?"":salesOrder.getParentSO().getID());
                    obj.put("gtaapplicable", salesOrder.isRcmapplicable()); // Get RCM applicable Check - Used for INDIA only
                    obj.put("isSOPOBlock", salesOrder.isLinkedPOBlocked());
                    obj.put("isdropshipchecked", salesOrder.isIsDropshipDocument());
                    KwlReturnObject linkRresult = accLinkDataDao.checkEntryForTransactionInLinkingTableForForwardReference("SalesOrder",salesOrder.getID());
                    list = linkRresult.getEntityList();
                    if (list != null && !list.isEmpty()) {
                        /*
                         *This block of code is added to check cross link case.
                         * i.e. SO --> PO. crossLinkingTransaction on this check handle enable/disable cases of Generate PI and Generate GR button.
                         * SDP-14034
                         */
                        Iterator iterator = list.iterator();
                        boolean crossLinking = false;
                        boolean normalLinking = false;
                        while (iterator.hasNext()) {
                            SalesOrderLinking solink = (SalesOrderLinking) iterator.next();
                            if (solink.getModuleID() == Constants.Acc_Purchase_Order_ModuleId) {
                                crossLinking = true;
                            } else {
                                normalLinking = true;
                            }
                            }
                        if (crossLinking && !normalLinking) {
                            obj.put("crossLinkingTransaction", crossLinking);
                        }
                    }
                    KwlReturnObject linkRresult1 = accSalesOrderDAOobj.checkEntryForReceiptInLinking("SalesOrder",salesOrder.getID());
                    List<Object[]> transactionlist1 = linkRresult1.getEntityList();
                    if (transactionlist1 != null && !transactionlist1.isEmpty()) {
                        /*
                         *This block of code is added to check cross link case.
                         * i.e. SO -->Receipt
                         */
                        Iterator iterator = transactionlist1.iterator();
                        boolean crossLinking = false;
                        boolean normalLinking = false;
                        while (iterator.hasNext()) {
                            SalesOrderLinking solink1 = (SalesOrderLinking) iterator.next();
                          
                            if (solink1.getModuleID() == Constants.Acc_Receive_Payment_ModuleId) {
                                 obj.put("linkedpayment", solink1.getLinkedDocNo());
                                 KwlReturnObject res=accountingHandlerDAOobj.getObject(ReceiptAdvanceDetail.class.getName(),solink1.getLinkedDocID());
                                 ReceiptAdvanceDetail advancedetail=(ReceiptAdvanceDetail)res.getEntityList().get(0);         
                                if (advancedetail != null && advancedetail.getReceipt() != null) {
                                    obj.put("linkedpaymentID", advancedetail.getReceipt().getID());
                                }
                            }
                        }
                        if (crossLinking && !normalLinking) {
                            obj.put("crossLinkingTransaction", crossLinking);
                        }
                    }
                    if (list != null && !list.isEmpty()) {
                         obj.put(Constants.IS_LINKED_TRANSACTION, true);
                    }else{
                         obj.put(Constants.IS_LINKED_TRANSACTION, false);
                    }
                    if (salesOrder.getCustWarehouse() != null) {
                        obj.put("custWarehouse", salesOrder.getCustWarehouse().getId());
                        obj.put("custWarehousename", salesOrder.getCustWarehouse().getName());
                    }
                    if (salesOrder.isIsconsignment()) {
                        obj.put("todate",salesOrder.getTodate()==null? "" : authHandler.getDateOnlyFormat().format(salesOrder.getTodate()));
                        obj.put("fromdate",salesOrder.getFromdate()==null? "" : authHandler.getDateOnlyFormat().format(salesOrder.getFromdate()));
                        obj.put("movementtype", salesOrder.getMovementType()!=null?salesOrder.getMovementType().getID():"");
                        obj.put("movementtypename", salesOrder.getMovementType()!=null?salesOrder.getMovementType().getValue():"");
                        obj.put("requestWarehouse", salesOrder.getRequestWarehouse()!=null?salesOrder.getRequestWarehouse().getId():"");
                        obj.put("requestLocation", salesOrder.getRequestLocation()!=null?salesOrder.getRequestLocation().getId():"");
                        obj.put("autoapproveflag", salesOrder.isAutoapproveflag());
                    }
                    obj.put("requestWarehousename", salesOrder.getRequestWarehouse() != null ? salesOrder.getRequestWarehouse().getName() : "");
                    obj.put("requestLocationname", salesOrder.getRequestLocation() != null ? salesOrder.getRequestLocation().getName() : "");
                    boolean gstIncluded = salesOrder.isGstIncluded();
                    obj.put("gstIncluded", gstIncluded);
                    
                    obj.put("leaseOrMaintenanceSo",salesOrder.getLeaseOrMaintenanceSO());
                    obj.put("totalprofitmargin",salesOrder.getTotalProfitMargin());
                    obj.put("totalprofitmarginpercent",salesOrder.getTotalProfitMarginPercent());
                    obj.put("maintenanceId", salesOrder.getMaintenance()==null?"":salesOrder.getMaintenance().getId());
                    obj.put("termname", salesOrder.getTerm()==null ? "":salesOrder.getTerm().getTermname());
                    obj.put("costcenterName", salesOrder.getCostcenter() !=null ? salesOrder.getCostcenter().getName():"");
                    BillingShippingAddresses addresses=salesOrder.getBillingShippingAddresses();
                    if (documentEmailSettings != null && documentEmailSettings.isCustShippingAddressInPurDoc()) {
                      AccountingAddressManager.getTransactionAddressJSONForPOFromSO(obj, addresses, false);   
                    }else{
                      AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                    }
                    obj.put("attachment",attachemntcount);
                    obj.put("approvalstatus", salesOrder.getApprovestatuslevel());
                    String approvalStatus="";
                    ScriptEngineManager mgr = new ScriptEngineManager();
                    ScriptEngine engine = mgr.getEngineByName("JavaScript");
                    double totalProfitMargin = 0;
                    double amountinbase = salesOrder.getTotalamountinbase();
                    totalProfitMargin=salesOrder.getTotalProfitMargin(); 
                    String multipleRuleids="";
                    if(salesOrder.getApprovestatuslevel() < 0){
                        approvalStatus="Rejected";
                    }else if(salesOrder.getApprovestatuslevel() < 11){
                        String ruleid = "",userRoleName="";
                        HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                        qdDataMap.put("companyid", companyid);
                        qdDataMap.put("level",salesOrder.getApprovestatuslevel() );
                        qdDataMap.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                        KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                        List<Object[]> ruleList = flowresult.getEntityList();
                        for (Object[] rulerow : ruleList) {
                            String rule = "";
                            if (rulerow[2] != null) {
                                rule = rulerow[2].toString();
                        }
                            String discountRule = "";
                            if (rulerow[7] != null) {
                                discountRule = rulerow[7].toString();
                            }
                            ruleid = (String) rulerow[0];
                            boolean sendForApproval = false;
                            boolean flag=false;
                            int appliedUpon = Integer.parseInt(rulerow[5].toString());
                            if (appliedUpon == 3) {
                                rule = rule.replaceAll("[$$]+", String.valueOf(totalProfitMargin));
                                /*
                                Added to check if record falls in pending approval of Credit Limit approval rule
                                */
                            } else if (appliedUpon == Constants.SO_CREDIT_LIMIT) {
                                /*
                                 * Check If Rule is apply on SO Credit limit
                                 * category from multiapproverule window ERM-396
                                 */

                                /*
                                 Handled for So Credit limit 
                                 */
                                boolean isLimitExceeding = false;
                                double customerCreditLimit = salesOrder.getCustomer().getCreditlimit();
                                paramJobj.put("customer", salesOrder.getCustomer().getID());
                                paramJobj.put("totalSUM", salesOrder.getTotalamountinbase());
                                JSONObject data = accCustomerMainAccountingService.getCustomerExceedingCreditLimit(paramJobj);
                                double amountDueOfCustomer = 0.0;
                                if (data.has("data") && data.getJSONArray("data").length() > 0) {
                                    amountDueOfCustomer = data.getJSONArray("data").getJSONObject(0).optDouble("totalAmountDueOfCustomer");
                                }

                                if ((amountDueOfCustomer > customerCreditLimit)) {
                                    isLimitExceeding = true;
                                }
                                if (isLimitExceeding) {
                                    sendForApproval = true;
                                }
                            }else if(appliedUpon == Constants.Total_Amount) {
                                /*
                                 Added to get condition of approval rule i.e set when creating approval rule 
                                 */
                                rule = rule.replaceAll("[$$]+", String.valueOf(amountinbase));
                            }else if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount || appliedUpon == Constants.Specific_Products_Category) {
                                /*
                                 Handled for Product,product discount And product category
                                 */
                                HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParamsJson(paramJobj);
                                JSONArray productDiscountJArr = new JSONArray();
                                Set<SalesOrderDetail> salesOrderDetails = salesOrder.getRows();
                                for (SalesOrderDetail soDetail : salesOrderDetails) {
                                    String productId = soDetail.getProduct().getID();
                                    double discountVal = soDetail.getDiscount();
                                    int isDiscountPercent = soDetail.getDiscountispercent();
                                    if (isDiscountPercent == 1) {
                                        discountVal = (soDetail.getQuantity() * soDetail.getRate()) * (discountVal / 100);
                                    }
                                    KwlReturnObject dAmount = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, salesOrder.getOrderDate(), salesOrder.getExternalCurrencyRate());
                                    double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                                    discAmountinBase = authHandler.round(discAmountinBase, companyid);
                                    JSONObject productDiscountObj = new JSONObject();
                                    productDiscountObj.put("productId", productId);
                                    productDiscountObj.put("discountAmount", discAmountinBase);
                                    productDiscountJArr.put(productDiscountObj);
                                }
                                if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount) {
                                    if (productDiscountJArr != null) {
                                        sendForApproval = AccountingManager.checkForProductAndProductDiscountRule(productDiscountJArr, appliedUpon, rule, discountRule);
                                    }
                                } else if (appliedUpon == Constants.Specific_Products_Category) {
                                    /*
                                     * Check If Rule is apply on product
                                     * category from multiapproverule window
                                     */
                                    sendForApproval = accountingHandlerDAOobj.checkForProductCategoryForProduct(productDiscountJArr, appliedUpon, rule);
                                }
                            }
                            /*
                            Added to check if record falls in total amount approval rule 
                            */
                            if (StringUtil.isNullOrEmpty(rule) || sendForApproval || (!StringUtil.isNullOrEmpty(rule) && appliedUpon == Constants.Total_Amount && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                                multipleRuleids += ruleid + ",";
                            }
                        }
                        /*
                        Added to get multiple ruleid if record falls in multiple approval rule 
                        */
                        String[] multipleRuleidsArray = multipleRuleids.split(",");
                        for (int multiRule = 0; multiRule < multipleRuleidsArray.length; multiRule++) {
                            ruleid = multipleRuleidsArray[multiRule];
                        if (!StringUtil.isNullOrEmpty(ruleid)) {
                            qdDataMap.put("ruleid", ruleid);
                            KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(qdDataMap);
                            List<Object[]> useritr = userResult.getEntityList();
                            for (Object[] userrow : useritr) {
                                String userId = userrow[0].toString();
                                String userName = userrow[1].toString();
                                    /*
                                    Addded so duplicate approve's can be eleminated 
                                    */
                                    if (userRoleName.contains(userName)) {
                                        break;
                                    }
                                KwlReturnObject kmsg = null;
                                    String roleName = "Company User";
                                kmsg = permissionHandlerDAOObj.getRoleofUser(userId);
                                Iterator ite2 = kmsg.getEntityList().iterator();
                                while (ite2.hasNext()) {
                                    Object[] row = (Object[]) ite2.next();
                                    roleName = row[1].toString();
                                }
                                    userRoleName += roleName + " " + userName + ",";
                            }
                        }
                        }
                        if (!StringUtil.isNullOrEmpty(userRoleName)) {
                            userRoleName = userRoleName.substring(0, userRoleName.length() - 1);
                        }
                        approvalStatus="Pending Approval" + ( StringUtil.isNullOrEmpty(userRoleName) ? "" : " by "+userRoleName )+" at Level - "+salesOrder.getApprovestatuslevel();
                    } else {
                        approvalStatus="Approved";
                    }
                    obj.put("formtypeid", salesOrder.getFormtype() != null ? salesOrder.getFormtype() : 0);
                    obj.put("isInterstateParty", (salesOrder.getCustomer() !=null ? salesOrder.getCustomer().isInterstateparty() : false));
                    obj.put("approvalstatusinfo", approvalStatus);
                    Set<String> uniqueProductTaxList = new HashSet<String>();
                    double availableQuantity=0;
                    double balanceQuantity=0;
                    boolean redColor=false;
                    boolean greenColor=false;
                    boolean yellowColor=false;
                    String taxname="";
                    Set<SalesOrderDetail> salesOrderDetails =salesOrder.getRows();
                    double amount = 0,totalDiscount = 0, discountPrice = 0;
                    double rowTaxAmt = 0d, rowDiscountAmt = 0d, rowOtherTermNonTaxableAmount= 0d;
                    int rejectedCount=0;
                    boolean includeprotax = false;
                    double sobalanceqty=0;
                    double subtotal = 0d;
                    double productTotalAmount = 0d;
                    for(SalesOrderDetail sod:salesOrderDetails) {
                        if(sod.getTax()!=null){
                            includeprotax = true;
                            taxname += sod.getTax().getName() + ", ";
                             uniqueProductTaxList.add(sod.getTax().getID());
                        }
                        sobalanceqty+=sod.getBalanceqty();
                        /**
                         * To Close SO at the time of invoice if only Service type product is used in SO.
                         * Ticket SDP-9960. 'sobalanceqty updated only if DO is done against Purchase Order'
                         */
                        if (sod.getProduct() != null && sod.getProduct().getProducttype() != null && sod.getProduct().getProducttype().getName().equalsIgnoreCase(Producttype.SERVICE_Name) && sod.getBalanceqty() > 0) {
                            KwlReturnObject idresult = accInvoiceDAOobj.getInvoiceDetailFromSOD(sod.getID());
                            List list1 = idresult.getEntityList();
                            if (!list1.isEmpty()) { // if invoice is created using sales order
                                Iterator ite1 = list1.iterator();
                                while (ite1.hasNext()) {
                                    InvoiceDetail ge = (InvoiceDetail) ite1.next();
                                    /**
                                     * if Linking case SO->SI->DO then no need
                                     * check SO Balance Quantity.
                                     */
                                    KwlReturnObject doResult = accInvoiceDAOobj.getDODetailfromInvoiceDetailID(ge.getID(), companyid);
                                    if (!(!StringUtil.isNullObject(doResult) && !StringUtil.isNullObject(doResult.getEntityList()) && !doResult.getEntityList().isEmpty())) {
                                        sobalanceqty -= ge.getInventory().getQuantity();
                                    }
                                }
                            }
                        }
                         if(isConsignment && !includepending){
                             if(sod.getRejectedQuantity()==sod.getBaseuomquantity()){
                                 rejectedCount++;
                             }else if(sod.getApprovedQuantity()>0){
                                 obj.put("isrequesteditable", true);
                             }
                         }
                     
                                   
                        double sorate=authHandler.roundUnitPrice(sod.getRate(), companyid);
                        if(gstIncluded) {
                            sorate = sod.getRateincludegst();
                        }
                        double quantity=authHandler.roundQuantity(sod.getQuantity(), companyid);

                        double quotationPrice = authHandler.roundUnitPrice(quantity * sorate, companyid);
                        subtotal += quotationPrice;
                        productTotalAmount += authHandler.round(quotationPrice, companyid);
                        double discountSOD=authHandler.round(sod.getDiscount(), companyid);
                        
                        if(sod.getDiscountispercent() == 1) {
//                            discountPrice = (quotationPrice) - authHandler.round((quotationPrice * discountSOD/100), companyid);
                            discountPrice = authHandler.round((quotationPrice) - authHandler.round((quotationPrice * discountSOD/100), companyid), companyid);
                            rowDiscountAmt += authHandler.round((quotationPrice * discountSOD/100), companyid);
                        } else {
                            discountPrice = quotationPrice - discountSOD;
                            rowDiscountAmt += discountSOD;
                        }
                        rowTaxAmt+=sod.getRowTaxAmount();
                        amount += discountPrice;//amount += discountPrice + (discountPrice * rowTaxPercent/100);
                        if(!gstIncluded) {
                            amount += authHandler.round(sod.getRowTaxAmount(), companyid);
                        }
                        if(isLineLevelTermFlag){
                            rowTaxAmt+=sod.getRowtermamount();
                            // Append OtherTermNonTaxableAmount for rach row.
                            rowOtherTermNonTaxableAmount += sod.getOtherTermNonTaxableAmount();
                                 /**
                                 * ERP-34717
                                 * If GST Include, no need to add Tax Amount. 
                                 * Amount is already with tax 
                                 */
                            if(!gstIncluded){
                                amount += authHandler.round(sod.getRowtermamount(), companyid);
                                amount += authHandler.round(sod.getOtherTermNonTaxableAmount(), companyid); 
                            }
                        }// For Line Level terms as tax

                        if (isOutstanding) {
                            Product product = sod.getProduct();
                            if (sod.getBalanceqty() == 0) {
                                continue;
                            }
                            /*  Calculation for Outstanding SO report */
                            availableQuantity = product.getAvailableQuantity();
                            balanceQuantity = sod.getBalanceqty();
                            if (availableQuantity >= balanceQuantity) {
                                greenColor = true;
                            } else if (balanceQuantity > availableQuantity && availableQuantity > 0) {
                                yellowColor = true;
                            } else {
                                redColor = true;
                            }
                        }
                    }  

                obj.put("productTotalAmount", authHandler.formattingDecimalForAmount(productTotalAmount, companyid));
                if (isOutstanding) {
                    /* Sending parameter to client side for showing Outstanding SO in Color a/c to their status*/
                    if (yellowColor) {
                        obj.put("yellowColor", yellowColor);
                    } else if (redColor & greenColor) {
                        obj.put("yellowColor", true);
                    } else if (greenColor) {
                        obj.put("greenColor", greenColor);
                    } else {
                        obj.put("redColor", redColor);
                    }
                }

             
                    double discountSO=authHandler.round(salesOrder.getDiscount(), companyid);
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
                    obj.put("amount", authHandler.formattingDecimalForAmount(amount, companyid)); 
                   if(salesOrder.isPerDiscount()){
                        obj.put("ispercentdiscount", salesOrder.isPerDiscount());
                       if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                           if (discountSO != 0.0 && discountSO != 0) {//if overall discount value is given.
                               obj.put("discount", totalDiscount);
                           } 
                       } else {
                           obj.put("discountval", discountSO);
                       }
                    }else{
                       if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                           if (discountSO != 0.0 && discountSO != 0) {//if overall discount is given then replace the case.
                               obj.put("discount", totalDiscount);
                           }
                       } else {
                           obj.put("discountval", totalDiscount);    //obj.put("discountval", salesOrder.getDiscount());
                       }
                    }
                   obj.put("discountinbase", salesOrder.getDiscountinbase());
                    try {
                        obj.put("creditDays", salesOrder.getTerm().getTermdays());
                    } catch(Exception ex) {
                        obj.put("creditDays", 0);
                    }

                    RepeatedSalesOrder repeatedSO = salesOrder.getRepeateSO();
                    obj.put("isRepeated", repeatedSO==null?false:true);
                    if(repeatedSO!=null){
                        obj.put("repeateid",repeatedSO.getId());
                        obj.put("interval",repeatedSO.getIntervalUnit());
                        obj.put("intervalType",repeatedSO.getIntervalType());
                        SimpleDateFormat sdf=new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
                        obj.put("startDate",sdf.format(repeatedSO.getStartDate()));
                        obj.put("NoOfpost",repeatedSO.getNoOfSOpost());
                        obj.put("NoOfRemainpost",repeatedSO.getNoOfRemainSOpost());
                        obj.put("nextDate",sdf.format(repeatedSO.getNextDate()));
                        obj.put("isactivate", repeatedSO.isIsActivate());
                        obj.put("ispendingapproval", repeatedSO.isIspendingapproval());
                        obj.put("approver", repeatedSO.getApprover());
                        obj.put("expireDate",repeatedSO.getExpireDate()==null?"":sdf.format(repeatedSO.getExpireDate()));
                        requestParams.put("parentSOId", salesOrder.getID());
                        KwlReturnObject details = accSalesOrderDAOobj.getRepeateSalesOrderDetails(requestParams);
                        List detailsList = details.getEntityList();
                        obj.put("childCount", detailsList.size());
                    }
                   
                    boolean isApplyTaxToTerms=salesOrder.isApplyTaxToTerms();
                    obj.put("isapplytaxtoterms", isApplyTaxToTerms);
                   
                    double totalTermAmount=0;
                    double taxableTermamount = 0;
                    double totalTermTaxAmount = 0d;
                    
                    List soTermMapList = kwlCommonTablesDAOObj.getSummationOfTermAmtAndTermTaxAmt(Constants.salesordertermmap, salesOrder.getID());
                    if(soTermMapList != null && !soTermMapList.isEmpty()){
                        Iterator termItr = soTermMapList.iterator();
                        while (termItr.hasNext()) {
                            Object[] termObj = (Object[]) termItr.next();
                            /* 
                            * [0] : Sum of termamount  
                            * [1] : Sum of termamountinbase 
                            * [2] : Sum of termTaxamount 
                            * [3] : Sum of termTaxamountinbase 
                            * [4] : Sum of termAmountExcludingTax 
                            * [5] : Sum of termAmountExcludingTaxInBase
                            */ 
                            if (salesOrder.isGstIncluded()) {
                                if(termObj[4] != null){
                                    totalTermAmount += authHandler.round((Double) termObj[4],companyid);
                                }
                            } else {
                                if(termObj[0] != null){
                                    totalTermAmount += authHandler.round((Double) termObj[0],companyid);
                                }
                            }
                            if(termObj[2] != null){
                                totalTermTaxAmount += authHandler.round((Double) termObj[2],companyid);
                            }
                        }
                    }
                    totalTermAmount=authHandler.round(totalTermAmount, companyid);
                    totalTermTaxAmount=authHandler.round(totalTermTaxAmount, companyid);
                    obj.put("taxid", salesOrder.getTax() == null ? "" : salesOrder.getTax().getID());
                    if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                        obj.put(Constants.taxidValue, salesOrder.getTax() == null ? "" : salesOrder.getTax().getName());
                    } else if (includeprotax) {
                        List<String> taxList = new ArrayList(Arrays.asList(taxname.split(", ")));
                        Collections.sort(taxList);
                        String taxname1 = "";
                        for (String str : taxList) {
                            taxname1 += str + ", ";
                        }
                        obj.put("taxname", taxname1.substring(0, taxname1.length() > 1 ? taxname1.length() - 2 : taxname1.length()));
                    } else {
                        obj.put("taxname", salesOrder.getTax() == null ? "" : salesOrder.getTax().getName());
                    }
                    
                    obj.put("shiplengthval", salesOrder.getShiplength());
                    obj.put("invoicetype", salesOrder.getInvoicetype());
                    double  taxPercent=0;
                    if(salesOrder.getTax()!=null){
                        HashMap<String, Object> reqParam = AccountingManager.getGlobalParams(paramJobj);
                        reqParam.put("transactiondate", salesOrder.getOrderDate());
                        reqParam.put("taxid", salesOrder.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(reqParam);
                        List taxList = result.getEntityList();
                        if(taxList.size()>0){
                            Object[] taxObj=(Object[]) taxList.get(0);
                            taxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                        }
                    }
                    double orderAmount=amount;//(Double) bAmt.getEntityList().get(0);
                    double ordertaxamount=(taxPercent==0?0:authHandler.round(((orderAmount + taxableTermamount)*taxPercent/100), companyid));
                    double taxAmt=rowTaxAmt+ordertaxamount;// either row level tax will be avvailable or invoice level
                    
                    obj.put("amountbeforegst", amount-rowTaxAmt); // Amount before both kind of tax row level or transaction level
                    obj.put("taxpercent", taxPercent);
                    obj.put("taxamount",taxAmt + totalTermTaxAmount);// Tax Amount
                    
                    if (gstIncluded) {
                         subtotal = productTotalAmount - rowDiscountAmt - (taxAmt);
                    } else {
                         subtotal = productTotalAmount - rowDiscountAmt;
                    }
                    obj.put("amountBeforeTax", authHandler.formattingDecimalForAmount((subtotal + totalTermAmount), companyid));
                    obj.put("subtotal", authHandler.formattingDecimalForAmount(subtotal,companyid));
                    if(isLineLevelTermFlag){
                        // If LineLevelTerm is applicable then add the value in JSON Object.
                        obj.put(Constants.OtherTermNonTaxableAmount,rowOtherTermNonTaxableAmount );// Tax Amount
                    }
                    amount=amount+totalTermAmount+ordertaxamount+totalTermTaxAmount;
                    orderAmount+=totalTermAmount + totalTermTaxAmount;

                    if (salesOrder.isIsRoundingAdjustmentApplied()) {
                        amount += salesOrder.getRoundingadjustmentamount();
                        orderAmount += salesOrder.getRoundingadjustmentamount();
                    }
                    obj.put("orderamount",orderAmount );
                    obj.put("orderamountwithTax",amount);// Total Amount
                    obj.put("amountInWodrs", currency.getName() + " " + EnglishNumberToWordsOjb.convert(authHandler.round(amount, companyid), salesOrder.getCurrency(),countryLanguageId));
          
                    KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amount, salesOrder.getCurrency().getCurrencyID(), salesOrder.getOrderDate(), salesOrder.getExternalCurrencyRate());
                    double totalAmountinBase= (Double)bAmt.getEntityList().get(0);
                    obj.put("amountinbase", authHandler.round(totalAmountinBase, companyid)); //Total Amount in base
                    obj.put("billtoaddress", CommonFunctions.getBillingShippingAddress(addresses, true));
                    obj.put("shiptoaddress", CommonFunctions.getBillingShippingAddress(addresses, false));
                    obj.put("archieve", 0);
                     /**
                     * Get Module template and its mapped Unit details for company if Line level term flag ON 
                     */
                    if(isForTemplate){
                        HashMap<String, Object> ModuleTempParams = new HashMap<>();
                        ModuleTempParams.put("modulerecordid", salesOrder.getID());
                        ModuleTempParams.put("companyid", companyid);
                        /** Get Module template  from invoice id . In module template Sales order id add as modulerecordid */
                        KwlReturnObject ModuleTempObj = accountingHandlerDAOobj.getModuleTemplates(ModuleTempParams);    
                        if(ModuleTempObj!=null && ModuleTempObj.getEntityList().size() > 0){
                            ModuleTemplate moduleTemp = (ModuleTemplate) ModuleTempObj.getEntityList().get(0);
                            obj.put("companyunitid", moduleTemp.getCompanyUnitid());
                            obj.put("populateproducttemplate", moduleTemp.isPopulateproductintemp());
                            obj.put("populatecustomertemplate", moduleTemp.isPopulatecustomerintemp());
                            HashMap tmpHashMap = new HashMap();
                            tmpHashMap.put("companyunitid", moduleTemp.getCompanyUnitid());
                            tmpHashMap.put(Constants.companyKey, companyid);
                            /* Get Company Unit details from companyunitid mapped with module template */
                            KwlReturnObject exciseTemp = accountingHandlerDAOobj.getExciseTemplatesMap(tmpHashMap);
                            if (isLineLevelTermFlag && exciseTemp != null && exciseTemp.getEntityList().size() > 0) {
                                ExciseDetailsTemplateMap ExcisemoduleTemp = (ExciseDetailsTemplateMap) exciseTemp.getEntityList().get(0);
                                if (ExcisemoduleTemp != null) {
                                    obj.put("registrationType", ExcisemoduleTemp.getRegistrationType());
                                    obj.put("UnitName", ExcisemoduleTemp.getUnitname());
                                    obj.put("ECCNo", ExcisemoduleTemp.getECCNo());
                                }
                            }
                        }
                    }
                   if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    obj.put("includeprotax", includeprotax==true?"true":"false");
                    obj.put("includeprotaxValue", includeprotax == true ? "Yes" : "No");
                   } else {
                    obj.put("includeprotax", includeprotax);
                   }
                    if(salesOrder.getModifiedby()!=null){
                        obj.put("lasteditedby",StringUtil.getFullName(salesOrder.getModifiedby()));
                    }
                    obj.put("termdetails",  getTermDetails(salesOrder.getID(),true));
                    if(salesOrder.getTermsincludegst()!=null) {
                        obj.put(Constants.termsincludegst, salesOrder.getTermsincludegst());
                    }
                    obj.put("termamount", totalTermAmount);  

                    String status=SalesOrder.STATUS_CLOSED;
                    String statustype=SalesOrder.STATUS_TYPE_CLOSED_BY_DO;
                    if(statusMp!=null&&statusMp.size()>0){
                        if(statusMp.containsKey(salesOrder.getSalesOrderNumber())){
                            obj.put("approvalstatus",statusMp.get(salesOrder.getSalesOrderNumber()));
                        }
                    }
                    if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                        obj.put("approvalstatus", salesOrder.getApprovestatuslevel());
                    }
                    
                    //ERP-41214:Show asterisk to unit price and amount 
                    //Handled for mobile Apps
                    if (paramJobj.optBoolean(Constants.isdefaultHeaderMap) && paramJobj.has(Constants.displayUnitPriceAndAmountInSalesDocument) && !paramJobj.optBoolean(Constants.displayUnitPriceAndAmountInSalesDocument)) {
                        obj.put("orderamountwithTax", CustomDesignerConstants.UNIT_PRICE_AND_AMOUNT_AS_STARS);// Total Amount 
                        obj.put("orderamount", CustomDesignerConstants.UNIT_PRICE_AND_AMOUNT_AS_STARS);
                        obj.put("amountBeforeTax", CustomDesignerConstants.UNIT_PRICE_AND_AMOUNT_AS_STARS);
                        obj.put("subtotal", CustomDesignerConstants.UNIT_PRICE_AND_AMOUNT_AS_STARS);
                        obj.put("amount", CustomDesignerConstants.UNIT_PRICE_AND_AMOUNT_AS_STARS);
                    }
                    if(isConsignment && !includepending && salesOrder.isFreeze()){
                        obj.put("status",status);
                         obj.put("statustype",(sobalanceqty==0)?statustype:SalesOrder.STATUS_TYPE_CLOSED_MANUALLY);
                    }else{
                        status = getSOStatus(salesOrder, pref,extraCompanyPreferences);
                        if (isConsignment && !includepending && salesOrder.getRows().size() == rejectedCount) {
                            obj.put("status", SalesOrder.STATUS_REJECTED);
                            status = SalesOrder.STATUS_REJECTED;
                            obj.put("statustype", SalesOrder.STATUS_REJECTED);
                        } else {
                            obj.put("status", (salesOrder.isIsSOClosed() ? SalesOrder.STATUS_CLOSED: status));
                            obj.put("statustype", (salesOrder.isIsSOClosed() ? SalesOrder.STATUS_TYPE_CLOSED_MANUALLY : status.equalsIgnoreCase(SalesOrder.STATUS_CLOSED) ? SalesOrder.STATUS_TYPE_CLOSED_BY_DO: SalesOrder.STATUS_OPEN));
                        }
                    }
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    SalesOrderCustomData jeDetailCustom = (SalesOrderCustomData) salesOrder.getSoCustomData();
                    replaceFieldMap = new HashMap<String, String>();
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        boolean isExport = (paramJobj.optString(Constants.isExport,null) == null) ? false : true;
                        JSONObject params = new JSONObject();
                        params.put(Constants.companyKey, companyid);
                        params.put(Constants.isExport, isExport);
                        params.put(Constants.userdf,userdf);
                        params.put(Constants.isdefaultHeaderMap,paramJobj.optBoolean(Constants.isdefaultHeaderMap, false));
                        params.put(Constants.userdf,userDateFormat);
                        
                        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.browsertz,""))) {
                            params.put(Constants.browsertz, paramJobj.getString(Constants.browsertz));
                        }
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                    if (!isOutstanding &&!isOutstandingproduct && (!closeflag || (closeflag && status.equalsIgnoreCase("open")))) {
                            if (isConsignment && !includepending) {
                                double quntity = getConsignmentRequestinCDO(salesOrder);
                                if (quntity > 0 && status.equalsIgnoreCase("Open")) {
                                    obj.put("status", "Partially Delivered");
                                    obj.put("statustype", "Partially Delivered");
                                }
                            }
                     }
                  
                if (isOutstanding && status.equalsIgnoreCase("open")) {
                    jArr.put(obj);
                } 
                else if(isOutstandingproduct && status.equalsIgnoreCase("open")){
                    jArr.put(obj);
                }else if (!isOutstanding&&!isOutstandingproduct && (!closeflag || (closeflag && status.equalsIgnoreCase("open"))) && (StringUtil.isNullOrEmpty(requestStatus) || "All".equals(requestStatus))) {
                    jArr.put(obj);
                } else if (isConsignment && !StringUtil.isNullOrEmpty(requestStatus) && !requestStatus.equals("All")) {
                    if (requestStatus.equals(obj.getString("status"))) {
                        jArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getSalesOrdersJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }
     
      public String getSalesOrderStatus(SalesOrder so) throws ServiceException {
        String result = "Closed";
        try{
        Set<SalesOrderDetail> orderDetail = so.getRows();
        Iterator ite = orderDetail.iterator();
        KwlReturnObject kwlresult = null;
        JSONObject saDetailJson = new JSONObject();
        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), so.getCompany().getCompanyID());
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
        HashMap<String, Object> SADetailParams = new HashMap<String, Object>();
        List<String> saDetailIds = null;
        if (so.isIsJobWorkOrder()) {
            SADetailParams.put("soid", so.getID());
            kwlresult = accProductObj.getSADetailsForSO(SADetailParams); // fetching SA details 
            saDetailIds = kwlresult.getEntityList();
            saDetailJson = getProductQuanityJSONForJWO(saDetailIds); // Creating JSON For product and its quantity mapped
        }
                    
        boolean fullInv = false;
        while(ite.hasNext()){            
            SalesOrderDetail soDetail = (SalesOrderDetail)ite.next();
//            String query = "from InvoiceDetail ge where ge.salesorderdetail.ID = ?";
//            List list =  HibernateUtil.executeQuery(session, query,pDetail.getID());
            double qua = 0;
            double rejectedQuantity=0;
            double quantPartTt = soDetail.getQuantity() * 100 ;
            double quantPartTtInv = 0.0 ;
            if (so.isIsJobWorkOrder()) {  // If SO is Job Work Order then checking its product's sub product's quantity for checking if SO is open or close depending on how much quantity is stocked In
                if (saDetailJson != null) {
                    HashMap<String, Object> assemblyParams = new HashMap<String, Object>();
                    assemblyParams.put("productid", soDetail.getProduct().getID());
                    assemblyParams.put("bomdetailid", soDetail.getBomcode().getID());
                    
                    /*
                    *    Fetching Sub Products for an assembly item. 
                    *    Passing Product Id and bom Detail ID in assemblyParams
                    */
                    KwlReturnObject assemblyItem = accProductObj.getAssemblyItems(assemblyParams); // fetching sub products
                    
                    /*
                    *    Checking If assemblyItem is Empty or not
                    */
                    if (!assemblyItem.getEntityList().isEmpty()) {
                        Iterator assembltItr = assemblyItem.getEntityList().iterator();
                        /*
                        *   While for sub-product of assembly product
                        */
                         while (assembltItr.hasNext()) {
                            Object[] assemblyItemObject = (Object[]) assembltItr.next();
                            ProductAssembly passembly = (ProductAssembly) assemblyItemObject[0];
                            /*
                            *  checking id Product object is not NUll
                            */
                            if (passembly.getSubproducts() != null) {
                                /*
                                *   Checking is sub-product is of Cutomer Inventory Type only
                                */
                                if (passembly.getSubproducts().getProducttype() != null && passembly.getSubproducts().getProducttype().getID().equals(Producttype.CUSTOMER_INVENTORY)) {
                                    
                                    /*
                                    * Fetching In Quantity and quantity required for assembly, for subproduct
                                    */
                                    double inQuantity = saDetailJson.optDouble(passembly.getSubproducts().getID(), 0.0);
                                    double subPrdQuantity = soDetail.getQuantity() * passembly.getActualQuantity();
                                    qua = subPrdQuantity - inQuantity;
                                    
                                    /*
                                    *   If qua is greater than zero then status is open other wise Close
                                    */
                                    if (qua > 0) {
                                        result = "Open";
                                        break;
                                    }
                                
                                }
                            }
                        }
                    }     
                }
            } else {
                if(pref.isWithInvUpdate()){ //In Trading Flow                 
                    KwlReturnObject doresult = accInvoiceDAOobj.getDeliveryOrderDFromSOD(soDetail.getID(),pref.getCompany().getCompanyID());
                    List list = doresult.getEntityList();
                    if(list.size()>0){
                        Iterator ite1 = list.iterator();                
                        while(ite1.hasNext()){                        
                            String orderid = (String)ite1.next();
                            KwlReturnObject res=accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(),orderid);
                            DeliveryOrderDetail deliveryOrderDetail=(DeliveryOrderDetail)res.getEntityList().get(0);                        
                            fullInv = true;
                            qua += deliveryOrderDetail.getDeliveredQuantity();
                        }
                    }
                    if (so.isIsconsignment() ) {//&& so.isLockquantityflag()
    //                    KwlReturnObject serialResult = accSalesOrderDAOobj.getSerialsFormDocumentid(soDetail.getID(), so.getCompany().getCompanyID());
    //                    Iterator serialItr = serialResult.getEntityList().iterator();
    //                    while (serialItr.hasNext()) {
    //                        SerialDocumentMapping documentMapping = (SerialDocumentMapping) serialItr.next();
    //                        if (documentMapping != null && documentMapping.getRequestApprovalStatus() == RequestApprovalStatus.REJECTED) {
    //                           rejectedQuantity++;
    //                        }
    //                    }
                        rejectedQuantity=(soDetail.getRejectedQuantity()/soDetail.getBaseuomrate());
                    }
                }else{ //In Non Trading Flow 

                    KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSOD(soDetail.getID());
                    List list = idresult.getEntityList();
                    Iterator ite1 = list.iterator();            
                    while(ite1.hasNext()){
                        InvoiceDetail ge = (InvoiceDetail) ite1.next();
                        if(ge.getInvoice().isPartialinv()) {
    //                        double quantity = ge.getInventory().isInvrecord()? ge.getInventory().getQuantity() : ge.getInventory().getActquantity() ;
                            double quantity = ge.getInventory().getQuantity();
                            quantPartTtInv += quantity * ge.getPartamount();
                        } else {
                            fullInv = true;
    //                        qua += ge.getInventory().isInvrecord()? ge.getInventory().getQuantity() : ge.getInventory().getActquantity() ;
                            qua += ge.getInventory().getQuantity();
                        }
                    }               
                }
                if (fullInv) {
                    if (so.isIsconsignment()) {//&& so.isLockquantityflag()
                        qua = qua + rejectedQuantity;//In partial Case we have handle this by adding rejected quantity to used quatity in CDO
                    }
                    if (qua < soDetail.getQuantity()) {
                        result = "Open";
                        break;
                    }

                } else if (quantPartTt > quantPartTtInv) {
                    result = "Open";
                    break;
                }
            }
        }
        } catch (Exception ex){
            throw ServiceException.FAILURE("accSalesOrderControllerCWN.getSalesOrderStatus : "+ex.getMessage(), ex);
        }
        return result;
    }
      
    public String getSalesOrderStatusNew(SalesOrder so, Set<SalesOrderDetail> orderDetail, CompanyAccountPreferences pref, String companyid) throws ServiceException {
        String result = "Closed";
        try {
            Iterator ite = orderDetail.iterator();
            boolean fullInv = false;
            while (ite.hasNext()) {
                SalesOrderDetail soDetail = (SalesOrderDetail) ite.next();
                double qua = 0;
                double rejectedQuantity = 0;
                double quantPartTt = soDetail.getQuantity() * 100;
                double quantPartTtInv = 0.0;
                if (pref.isWithInvUpdate()) { //In Trading Flow                 
                    KwlReturnObject doresult = accInvoiceDAOobj.getDeliveryOrderDFromSODOptimized(soDetail.getID(), companyid);
                    List list = doresult.getEntityList();
                    if (list.size() > 0) {
                        Iterator ite1 = list.iterator();
                        while (ite1.hasNext()) {
                            String orderid = (String) ite1.next();
                            KwlReturnObject res = accountingHandlerDAOobj.loadObject(DeliveryOrderDetail.class.getName(), orderid);
                            DeliveryOrderDetail deliveryOrderDetail = (DeliveryOrderDetail) res.getEntityList().get(0);
                            fullInv = true;
                            qua += deliveryOrderDetail.getDeliveredQuantity();
                        }
                    }
                    if (so.isIsconsignment() && so.isLockquantityflag()) {
                        KwlReturnObject serialResult = accSalesOrderDAOobj.getSerialsFormDocumentid(soDetail.getID(), companyid);
                        Iterator serialItr = serialResult.getEntityList().iterator();
                        while (serialItr.hasNext()) {
                            SerialDocumentMapping documentMapping = (SerialDocumentMapping) serialItr.next();
                            if (documentMapping != null && documentMapping.getRequestApprovalStatus() == RequestApprovalStatus.REJECTED) {
                                rejectedQuantity++;
                            }
                        }
                        rejectedQuantity = (rejectedQuantity / soDetail.getBaseuomrate()) * 100;
                    }
                } else { //In Non Trading Flow 
                    KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSOD(soDetail.getID());
                    List list = idresult.getEntityList();
                    Iterator ite1 = list.iterator();
                    while (ite1.hasNext()) {
                        InvoiceDetail ge = (InvoiceDetail) ite1.next();
                        if (ge.getInvoice().isPartialinv()) {
                            double quantity = ge.getInventory().getQuantity();
                            quantPartTtInv += quantity * ge.getPartamount();
                        } else {
                            fullInv = true;
                            qua += ge.getInventory().getQuantity();
                        }
                    }
                }
                if (fullInv) {
                    if (so.isIsconsignment() && so.isLockquantityflag()) {
                        qua = qua + rejectedQuantity;//In partial Case we have handle this by adding rejected quantity to used quatity in CDO
                    }
                    if (qua < soDetail.getQuantity()) {
                        result = "Open";
                        break;
                    }

                } else if (quantPartTt > quantPartTtInv) {
                    result = "Open";
                    break;
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderControllerCWN.getSalesOrderStatus : " + ex.getMessage(), ex);
        }
        return result;
    }
      public double getConsignmentRequestinCDO(SalesOrder so) throws ServiceException {
        double quantity = 0;
        try{
        Set<SalesOrderDetail> orderDetail = so.getRows();
        Iterator ite = orderDetail.iterator(); 
        while(ite.hasNext()){            
            SalesOrderDetail soDetail = (SalesOrderDetail)ite.next();              
                KwlReturnObject doresult = accInvoiceDAOobj.getDeliveryOrderDFromSOD(soDetail.getID(),so.getCompany().getCompanyID());
                List list = doresult.getEntityList();
                if(list.size()>0){
                    Iterator ite1 = list.iterator();                
                    while(ite1.hasNext()){                        
                        String orderid = (String)ite1.next();
                        KwlReturnObject res=accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(),orderid);
                        DeliveryOrderDetail deliveryOrderDetail=(DeliveryOrderDetail)res.getEntityList().get(0);                        
                        quantity += deliveryOrderDetail.getDeliveredQuantity();
                    }
                }                                    
        }
        } catch (Exception ex){
            throw ServiceException.FAILURE("accSalesOrderServiceImpl.getConsignmentRequestinCDO : "+ex.getMessage(), ex);
        }
        return quantity;
    }
   
    @Override
         public JSONObject getSalesOrderRows(JSONObject paramJobj) throws SessionExpiredException, ServiceException, ParseException {
        JSONObject jobj=new JSONObject();
        try {
            HashMap<String,Object> requestParams = new HashMap<String, Object>();
            HashMap<String, Object> dataParams = AccountingManager.getGlobalParamsJson(paramJobj);
            requestParams.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
            requestParams.put(Constants.globalCurrencyKey,paramJobj.getString(Constants.globalCurrencyKey));
            boolean isExplodeAssemblyPrd=(StringUtil.isNullOrEmpty(paramJobj.optString("isExplodeAssemblyPrd","")))?false:Boolean.parseBoolean((String)paramJobj.get("isExplodeAssemblyPrd"));
            boolean isJobWorkInReciever=(StringUtil.isNullOrEmpty(paramJobj.optString("isJobWorkInReciever","")))?false:Boolean.parseBoolean((String)paramJobj.get("isJobWorkInReciever"));
            boolean isVendorJobWorkOrder=(StringUtil.isNullOrEmpty(paramJobj.optString("isVendorJobWorkOrder","")))?false:Boolean.parseBoolean((String)paramJobj.get("isVendorJobWorkOrder"));
            boolean isConsignment=(StringUtil.isNullOrEmpty(paramJobj.optString("isConsignment","")))?false:Boolean.parseBoolean((String)paramJobj.get("isConsignment"));
            boolean isLeaseFixedAsset = (StringUtil.isNullOrEmpty(paramJobj.optString("isLeaseFixedAsset","")))?false:Boolean.parseBoolean((String)paramJobj.get("isLeaseFixedAsset"));
            boolean isOutstandingproduct = (StringUtil.isNullOrEmpty(paramJobj.optString("isOuststandingproduct","")))?false:Boolean.parseBoolean((String)paramJobj.get("isOuststandingproduct"));
            String companyid = paramJobj.getString(Constants.companyKey);
            boolean isForInvoice =Boolean.FALSE.parseBoolean(paramJobj.optString("isForInvoice",""));
            boolean isFromContract =Boolean.FALSE.parseBoolean(paramJobj.optString("isFromContract",""));   //ERP-41234 : Used to identify Sales Contract call
            boolean doflag =(paramJobj.optString("doflag",null)!=null)?true:false;
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(),paramJobj.getString(Constants.globalCurrencyKey));
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            dataParams.put("kwlcurrency", kwlcurrency);
            dataParams.put("isExplodeAssemblyPrd", isExplodeAssemblyPrd);
            DateFormat userDateFormat=null;
            if(paramJobj.has(Constants.userdateformat) && (paramJobj.get(Constants.userdateformat) != null) ){
                userDateFormat=new SimpleDateFormat((String)paramJobj.get(Constants.userdateformat));
            }
//            int i=0;
            String[] sos=null;
            if(paramJobj.optString(Constants.billid,null)!=null){
                String temp=paramJobj.optString(Constants.billid,"").toString();
                sos = (String[]) temp.split(",");
            }else{
                sos = (String[]) paramJobj.optString("bills","").split(",");
            }
            JSONArray jArr=new JSONArray();
            double quantity = 1;
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            dataParams.put("preferences", preferences);
    
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            dataParams.put("extraCompanyPreferences", extraCompanyPreferences);
            
            HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("salesOrder.ID");
            order_by.add("srno");
            order_type.add("asc");
            soRequestParams.put("filter_names", filter_names);
            soRequestParams.put("filter_params", filter_params);
            soRequestParams.put("order_by", order_by);
            soRequestParams.put("order_type", order_type);
            
            HashMap<String, Object> fieldrequestParams1 = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
            fieldrequestParams1.put(Constants.filter_values, Arrays.asList(companyid, isConsignment?Constants.Acc_ConsignmentRequest_ModuleId: isLeaseFixedAsset?Constants.Acc_Lease_Order_ModuleId:isVendorJobWorkOrder?Constants.VENDOR_JOB_WORKORDER_MODULEID:Constants.Acc_Sales_Order_ModuleId,1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams1, replaceFieldMap, customFieldMap, customDateFieldMap);
            dataParams.put("fieldMap", FieldMap);
            dataParams.put("customFieldMap", customFieldMap);
            dataParams.put("customDateFieldMap", customDateFieldMap);
            for (int i = 0; sos != null && i < sos.length; i++) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), sos[i]);
                SalesOrder so = (SalesOrder) result.getEntityList().get(0);

                KWLCurrency currency = null;
                if(so!=null){
                if(so.getCurrency() != null){
                        currency = so.getCurrency();
                    } else {
                    currency=so.getCustomer().getAccount().getCurrency()==null?kwlcurrency:so.getCustomer().getAccount().getCurrency();
                    }

                    Map<String, Object> ProductFieldsRequestParams = new HashMap();
                    ProductFieldsRequestParams.put(Constants.companyKey, companyid);
                    ProductFieldsRequestParams.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                    List masterFieldsResultList = CommonFunctions.getproductmastersFieldsToShowLineLevel(ProductFieldsRequestParams, accountingHandlerDAOobj);

                    dataParams.put("salesOrderObject", so);
                    dataParams.put("currencyObject", currency);
                    dataParams.put("masterFieldsResultList", masterFieldsResultList);

                    filter_params.clear();
                    filter_params.add(so.getID());
                    KwlReturnObject podresult = accSalesOrderDAOobj.getSalesOrderDetails(soRequestParams);
                    List<SalesOrderDetail> salesOrderDetails = podresult.getEntityList();
                    StringBuilder productIDs = new StringBuilder();
                    for (SalesOrderDetail salesOrderDetail : salesOrderDetails) {
                        String productid = salesOrderDetail.getProduct().getID();
                        if (productIDs.indexOf(productid) == -1) {
                            productIDs.append("'" + productid + "'").append(",");
                        }
                    }
                    Map<String, Double> socountMap = new HashMap<>();
                    if (productIDs.length() > 0 && (preferences.getNegativeStockSO() == 1 || preferences.getNegativeStockSO() == 2) || extraCompanyPreferences.isOpenPOandSO()) {
                        Map<String, Object> reqMap = new HashMap<>();
                        reqMap.put("productIds", productIDs.substring(0, productIDs.length() - 1));
                        reqMap.put(Constants.companyKey, companyid);
                        reqMap.put("isopeningbalenceso", so.isIsOpeningBalanceSO() ? 'T' : 'F');
                        reqMap.put("isconsignment", so.isIsconsignment() ? 'T' : 'F');
                        socountMap = accSalesOrderDAOobj.getOutstandingQuantityCountForProductMap(reqMap);
                    }
                    for (SalesOrderDetail salesOrderDetail : salesOrderDetails) {
                        dataParams.put("salesOrderDetailObject", salesOrderDetail);
                        Product product = salesOrderDetail.getProduct();
                    if (isOutstandingproduct && salesOrderDetail.getBalanceqty() == 0) {
                            continue;
                        }
                        if ((isExplodeAssemblyPrd && product.getProducttype() != null && product.getProducttype().getID().equals(Producttype.ASSEMBLY)) || (isJobWorkInReciever && product.getProducttype().getID().equals(Producttype.CUSTOMER_ASSEMBLY))) {//When product is inventory prodct
                        getAssemblySubProductRows(dataParams, paramJobj, jArr,socountMap);
                        } else {
                            dataParams.put("rowProductObject", product);
                        /*Calculating Row Details*/
                        JSONObject obj = getSalesOrderRowsDetails(dataParams, paramJobj,socountMap);
                            quantity = obj.optDouble("quantity", 0);
                        String status=obj.getString("status");
                            if (doflag || isForInvoice || isFromContract) {//Loading only fully / partially open sales order rows in DO or Invoice or sales Contact under Manufacturing
                                /**
                             * If Allow zero quantity from companypreferences is true then we show the sales orders with zero quantity
                             * otherwise not.
                                 */
                            if ((extraCompanyPreferences.isAllowZeroQuantityInSO()?quantity >= 0:quantity > 0) && status.equalsIgnoreCase("N/A")) {
                                    jArr.put(obj);
                                }
                            } else {
                            if (extraCompanyPreferences.isAllowZeroQuantityInSO()?quantity >= 0:quantity > 0) {
                                    jArr.put(obj);
                                }
                            }

                        }
                    }
                }
//                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    // Method to get advance payments that are linked with SO.
    @Override
    public JSONObject getLinkedAdvancePayments(JSONObject paramJobj) throws ServiceException{
        JSONObject jobj = new JSONObject();
        JSONObject params = new JSONObject();
        try {
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("billid"))) {
                String companyid = paramJobj.optString("companyid");
                params.put("docid", "'"+paramJobj.optString("billid")+"'");
                params.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                KwlReturnObject podresult = accSalesOrderDAOobj.getLinkedDocByModuleId(params);
                String linkAdvancePayments = "";
                JSONArray dataArr = new JSONArray();
                JSONObject obj = null;
                if (podresult.getEntityList() != null && !podresult.getEntityList().isEmpty()) {
                    List<Object[]> list = podresult.getEntityList();
                    for (Object[] advanceDetail : list) {
                        obj = new JSONObject();
                        obj.put("billno", advanceDetail[4]);
                        obj.put("billid", advanceDetail[3]);
                        obj.put("amountdue", authHandler.round(((Double)advanceDetail[2]), companyid));
                        obj.put("totalamount", authHandler.round(((Double)advanceDetail[1]), companyid));

                        dataArr.put(obj);
                    }
                }
                jobj.put("data", dataArr);
                jobj.put("linkedAdvancePayments", linkAdvancePayments);
            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }
    
    public void getAssemblySubProductRows(Map<String, Object> requestParams, JSONObject paramJobj, JSONArray jArr, Map<String, Double> socountMap) throws ServiceException, JSONException, UnsupportedEncodingException, ParseException, SessionExpiredException {
        SalesOrderDetail row = (SalesOrderDetail) requestParams.get("salesOrderDetailObject");
        String soid = row.getSalesOrder().getID();
        String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
        String bomid = "";
        KwlReturnObject result = null;
        JSONObject saDetailJson = new JSONObject();
        boolean isJobWorkInReciever = false;
        String companyid = paramJobj.optString("companyid");
        isJobWorkInReciever = paramJobj.optBoolean("isJobWorkInReciever",false);
        HashMap<String, Object> assemblyParams = new HashMap<String, Object>();
        HashMap<String, Object> SADetailParams = new HashMap<String, Object>();
        if (isJobWorkInReciever) { // On Job work in flag true, adding bomdetail id in params so that only ingredientys of that BOM can only load.
            bomid = row.getBomcode() != null ? row.getBomcode().getID() : "";
            if (!StringUtil.isNullOrEmpty(bomid)) {
                assemblyParams.put("bomdetailid", bomid);
            }
            SADetailParams.put("soid", soid);
            /*
            *  fetching SA details 
            */
            result = accProductObj.getSADetailsForSO(SADetailParams); 
            List<String> saDetailIds = result.getEntityList();
            /*
            *   Creating JSON For product and its quantity mapped
            */
            saDetailJson = getProductQuanityJSONForJWO(saDetailIds); 
        }
        if (StringUtil.isNullOrEmpty(bomid)) {
            assemblyParams.put("isdefaultbom", true);
        }
        assemblyParams.put("productid", row.getProduct().getID());
        assemblyParams.put("currencyid", currencyid);
        KwlReturnObject assemblyItem = accProductObj.getAssemblyItems(assemblyParams);
        if (!assemblyItem.getEntityList().isEmpty()) {
            Iterator assembltItr = assemblyItem.getEntityList().iterator();
            while (assembltItr.hasNext()) {
                Object[] assemblyItemObject = (Object[]) assembltItr.next();
                ProductAssembly passembly = (ProductAssembly) assemblyItemObject[0];
                if (passembly.getSubproducts() != null) {
                    if (isJobWorkInReciever) {
                        if (passembly.getSubproducts().getProducttype().getID().equals(Producttype.CUSTOMER_INVENTORY)) {
                            requestParams.put("rowProductObject", passembly.getSubproducts());
                            JSONObject assemblyobj = getSalesOrderRowsDetails(requestParams, paramJobj, socountMap);
                            double quantity = assemblyobj.optDouble("quantity", 0);
                            /*
                            * Fetching In quantity for sub product
                            */
                            double inQuantity = saDetailJson.optDouble(passembly.getSubproducts().getID(), 0.0);
                            if (quantity > 0) {
                                double subPrdQuantity = quantity * passembly.getActualQuantity();
                                /*
                                *   Subtracting in quantity ffrom total quantity
                                */
                                assemblyobj.put("quantity", subPrdQuantity - inQuantity); 
                                assemblyobj.put("copyquantity", subPrdQuantity);
                                
                                /*
                                * Added parent Product id and name in jSON
                                */
                                assemblyobj.put("parentproductid", row.getProduct().getID());
                                assemblyobj.put("parentproductname", row.getProduct().getName());
                                /*
                                * Added BOM code and Name in JSON
                                */
                                try {
                                    if (!StringUtil.isNullOrEmpty(bomid)) {
                                        KwlReturnObject bomObj = accountingHandlerDAOobj.getObject(BOMDetail.class.getName(), bomid);
                                        BOMDetail bom = bomObj != null ? (BOMDetail) bomObj.getEntityList().get(0) : null;
                                        if (bom != null) {
                                            assemblyobj.put("bomname", bom.getBomName());
                                            assemblyobj.put("bomcode", bom.getBomCode());
                                        } else {
                                            assemblyobj.put("bomname", "");
                                            assemblyobj.put("bomcode", "");
                                        }
                                    } else {
                                        assemblyobj.put("bomname", "");
                                        assemblyobj.put("bomcode", "");
                                    }
                                } catch (Exception ex) {
                                    assemblyobj.put("bomname", "");
                                    assemblyobj.put("bomcode", "");
                                }
                                assemblyobj.put("dquantity", subPrdQuantity);
                                assemblyobj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(subPrdQuantity, row.getBaseuomrate(), companyid));
                                assemblyobj.put("baseuomrate", row.getBaseuomrate());
                                /*
                                * If quantity is greater than zero then only put product in JSON otherwise no.
                                */
                                if ((subPrdQuantity - inQuantity) > 0) {
                                    jArr.put(assemblyobj);
                                }
                            }
                        }
                    } else {
                        requestParams.put("rowProductObject", passembly.getSubproducts());
                        JSONObject assemblyobj = getSalesOrderRowsDetails(requestParams, paramJobj, socountMap);
                        double quantity = assemblyobj.optDouble("quantity", 0);
                        if (quantity > 0) {
                            double subPrdQuantity = quantity * passembly.getActualQuantity();
                            assemblyobj.put("quantity", subPrdQuantity);
                            assemblyobj.put("copyquantity", subPrdQuantity);
                            assemblyobj.put("dquantity", subPrdQuantity);
                            assemblyobj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(subPrdQuantity, row.getBaseuomrate(), companyid));
                            assemblyobj.put("baseuomrate", row.getBaseuomrate());
                            jArr.put(assemblyobj);
                        }
                    }
                }
            }
        }
    }
    /*
    * Creating JSON of a product and its stock in quantity for a job work order
    */
    @Override
    public JSONObject getProductQuanityJSONForJWO(List<String> saDetailIds){
        JSONObject retJobj = new JSONObject();
        StockAdjustmentDetail saDetailObj = null;
        String productId = "";
        try {
            /*
            *   For Loop for Stock Adjustment Detail
            *   Creating a map for Stock adjustment detail product and Its Quantity Stocked IN
            */
            for (String saDetailId  : saDetailIds) {
                saDetailObj = (StockAdjustmentDetail) kwlCommonTablesDAOObj.getClassObject(StockAdjustmentDetail.class.getName(), saDetailId);
                productId = saDetailObj.getStockAdjustment().getProduct().getID();
                retJobj.put(productId, saDetailObj.getQuantity() + retJobj.optDouble(productId,0.0));
            }
        } catch (Exception ex) {
            
        }
        return  retJobj;
    }
    
     public Map<String, List<Object[]>> getBatchDetailsMap(Map<String,Object> requestParams) {
        Map<String, List<Object[]>> baMap = new HashMap<>();
        try {
            boolean linkingFlag=false;
            if(requestParams.containsKey("linkingFlag")){
                linkingFlag=Boolean.parseBoolean(requestParams.get("linkingFlag").toString());
            }
            boolean isEdit=false;
            if(requestParams.containsKey("isEdit")){
                isEdit=Boolean.parseBoolean(requestParams.get("isEdit").toString());
            }
            boolean isConsignment=false;
            if(requestParams.containsKey(Constants.isConsignment)){
                isConsignment=Boolean.parseBoolean(requestParams.get(Constants.isConsignment).toString());
            }
            String moduleID="";
            if(requestParams.containsKey("moduleID")){
                moduleID=requestParams.get("moduleID").toString();
            }
            String documentIds="";
            if (requestParams.containsKey("documentIds")) {
                documentIds = requestParams.get("documentIds").toString();
            }    
            KwlReturnObject kmsg = null;
            if(isConsignment && linkingFlag){
                kmsg= accCommonTablesDAO.getConsignmentBatchSerialDetails("", true, linkingFlag, moduleID, false, isEdit, documentIds);
            }else{
                kmsg= accCommonTablesDAO.getBatchSerialDetails("", true, linkingFlag, moduleID, false, isEdit, documentIds);
            }
                    
            List<Object[]> batchserialdetails = kmsg.getEntityList();
            for (Object[] objects : batchserialdetails) {
                if (objects.length >= 20 && objects[20] != null) {
                    if(baMap.containsKey(objects[20].toString())){
                        List<Object[]> details =baMap.get(objects[20].toString());
                        details.add(objects);
                        baMap.put(objects[20].toString(), details);
                    }else{
                        List<Object[]> details = new ArrayList<>();
                        details.add(objects);
                        baMap.put(objects[20].toString(), details);
                    }
                    
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return baMap;
    }
     
     private String getNewBatchDetailJson(Map<String, Object> requestParams, Product product, String documentid, HashMap<String, Integer> fieldMap, Map<String, List<Object[]>> baMap, HashMap<String, String> replaceFieldMap1, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap) throws ServiceException, SessionExpiredException, JSONException {
        JSONArray jSONArray = new JSONArray();
        try {
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            KwlReturnObject kmsg = null;
            boolean linkingFlag = false;
            if (requestParams.containsKey("linkingFlag")) {
                linkingFlag = Boolean.parseBoolean(requestParams.get("linkingFlag").toString());
            }
            boolean isEdit = false;
            if (requestParams.containsKey("isEdit")) {
                isEdit = Boolean.parseBoolean(requestParams.get("isEdit").toString());
            }
            boolean readOnly = false;
            if (requestParams.containsKey("readOnly")) {
                readOnly = Boolean.parseBoolean(requestParams.get("readOnly").toString());
            }
            boolean isConsignment = false;
            if (requestParams.containsKey(Constants.isConsignment)) {
                isConsignment = Boolean.parseBoolean(requestParams.get(Constants.isConsignment).toString());
            }
            boolean srflag = false;
            if (requestParams.containsKey("srflag")) {
                srflag = Boolean.parseBoolean(requestParams.get("srflag").toString());
            }
            String moduleID = "";
            if (requestParams.containsKey("moduleID")) {
                moduleID = requestParams.get("moduleID").toString();
            }
            String companyid = requestParams.get(Constants.companyKey).toString();
            boolean isBatch = false;
            List<Object[]> batchserialdetails = null;
            if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && !product.isIsrowforproduct() && !product.isIsrackforproduct() && !product.isIsbinforproduct() && product.isIsSerialForProduct()) {
                kmsg = accCommonTablesDAO.getOnlySerialDetails(documentid, linkingFlag, moduleID, isConsignment, isEdit);
                batchserialdetails = kmsg.getEntityList();
            } else {
                isBatch = true;
                kmsg = accCommonTablesDAO.getBatchDetails(documentid, linkingFlag, moduleID, isConsignment, isEdit, "");
                batchserialdetails = kmsg.getEntityList();
            }
            double ActbatchQty = 1;
            double batchQty = 0;
            double avlquantity = 0;
            if (batchserialdetails != null) {
                for (Object[] objArr : batchserialdetails) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", objArr[0] != null ? (String) objArr[0] : "");
                    obj.put("batch", objArr[1] != null ? (String) objArr[1] : "");
                    obj.put("batchname", objArr[1] != null ? (String) objArr[1] : "");
                    obj.put("location", objArr[2] != null ? (String) objArr[2] : "");
                    obj.put("warehouse", objArr[3] != null ? (String) objArr[3] : "");
                    if (isBatch) {
                        obj.put("row", objArr[15] != null ? (String) objArr[15] : "");
                        obj.put("rack", objArr[16] != null ? (String) objArr[16] : "");
                        obj.put("bin", objArr[17] != null ? (String) objArr[17] : "");
                    }
                    KwlReturnObject objresult = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), objArr[0].toString());
                    NewProductBatch batch = (NewProductBatch) objresult.getEntityList().get(0);
                    
                    if(batch != null){
                         obj.put("isBatchForProduct", product.isIsBatchForProduct());
                        obj.put("isSerialForProduct", product.isIsSerialForProduct());
                        obj.put("isRowForProduct", product.isIsrowforproduct());
                        obj.put("isRackForProduct", product.isIsrackforproduct());
                        obj.put("isBinForProduct", product.isIsbinforproduct());
                        obj.put("storeName", batch.getWarehouse() != null ? batch.getWarehouse().getName():"");
                        obj.put("locationName", batch.getLocation() != null ? batch.getLocation().getName():"");
                        obj.put("rowName", batch.getRow() != null ? batch.getRow().getName() : "");
                        obj.put("rackName", batch.getRack() != null ? batch.getRack().getName() : "");
                        obj.put("binName", batch.getBin() != null ? batch.getBin().getName() : "");
                        obj.put("row", batch.getRow() != null ? batch.getRow().getId() : "");
                        obj.put("rack", batch.getRack() != null ? batch.getRack().getId() : "");
                        obj.put("bin", batch.getBin() != null ? batch.getBin().getId() : "");
//                        obj.put("avialblequantity", stock.getQuantity());
                        obj.put("quantity", "");
                        //obj.put("serialNames", (stock.getSerialNames() != null) ? stock.getSerialNames().replace(",", ", ") : "");
                        obj.put("batchName", batch.getBatchname());
                    }
                    
                    obj.put("wastageQuantityType", objArr[18] != null ? objArr[18] : "");
                    //obj.put("stocktype", (product.isIsBatchForProduct() && !product.isIsSerialForProduct())?(objArr[21] != null ? objArr[21] : ""):(objArr[22] != null ? objArr[22] : ""));
                    obj.put("wastageQuantity", objArr[19] != null ? objArr[19] : "");
                   
                    obj.put("stocktype", product.isIsBatchForProduct()?(objArr[21] != null ? objArr[21] : ""):(objArr[22] != null ? objArr[22] : ""));
                    String stocktype = "" + (product.isIsBatchForProduct() ? (objArr[21] != null ? objArr[21] : "") : (objArr[22] != null ? objArr[22] : ""));

                    
                    
                    if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct()) && product.isIsSerialForProduct()) {
                        int stkType=1;
                        if(!StringUtil.isNullOrEmpty(stocktype)){
                            stkType=Integer.parseInt(stocktype);
                            ActbatchQty = accCommonTablesDAO.getBatchQuantity(documentid, (String) objArr[0],stkType);
                        }else{
                            ActbatchQty = accCommonTablesDAO.getBatchQuantity(documentid, (String) objArr[0]);
                        }
                        obj.put("avialblequantity", ActbatchQty);
                        obj.put("quantity", ActbatchQty);

                    } else {
                        obj.put("isreadyonly", false);
                        obj.put("quantity", ActbatchQty);
                    }
                    if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && !product.isIsrowforproduct() && !product.isIsrackforproduct() && !product.isIsbinforproduct() && product.isIsSerialForProduct()) {
                        obj.put("mfgdate", "");
                        obj.put("expdate", "");
                    } else {
                        obj.put("mfgdate", objArr[4] != null ? df.format(objArr[4]) : "");
                        obj.put("expdate", objArr[5] != null ? df.format(objArr[5]) : ""); //product.isIsSerialForProduct()? objArr[10] != null ? df.format(objArr[10]) : "":
                    }
                    if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct()) && !product.isIsSerialForProduct()) {
                        obj.put("quantity", objArr[11] != null ? objArr[11] : "");
                    }
                    obj.put("balance", 0);
                    if ((product.isIslocationforproduct() || product.isIswarehouseforproduct()) && linkingFlag && isConsignment && srflag && objArr[0] != null) {
                        String dobatchId = objArr[0] != null ? (String) objArr[0] : "";
                        String purchaseBatchId = "";
                        if (!StringUtil.isNullOrEmpty(dobatchId)) {
                            purchaseBatchId = accCommonTablesDAO.getpurchaseBatchIdForDo(dobatchId, documentid);
                        }
                        obj.put("purchasebatchid", purchaseBatchId);
                    } else {
                        obj.put("purchasebatchid", objArr[0] != null ? (String) objArr[0] : "");
                    }
                    obj.put("documentid", documentid != null ? documentid : "");
                    obj.put("productid", product != null ? product.getID() : "");
                    String locationid = objArr[2] != null ? (String) objArr[2] : "";
                    String warehouseid = objArr[3] != null ? (String) objArr[3] : "";
                    String purchasebatchid = objArr[0] != null ? (String) objArr[0] : "";
                    String productid = product != null ? product.getID() : "";
                    String tempModuleId=moduleID;
                    if(!isConsignment && srflag){
                        tempModuleId=String.valueOf(Constants.Acc_Sales_Return_ModuleId);
                    }
                    avlquantity = accInvoiceServiceDAO.getNewBatchRemainingQuantity(locationid, warehouseid, companyid, productid, purchasebatchid, moduleID, isEdit, documentid);
//                    avlquantity = accInvoiceServiceDAO.getNewBatchRemainingQuantity(locationid, warehouseid, companyid, productid, purchasebatchid, tempModuleId, isEdit, documentid,stocktype, readOnly);
                    obj.put("avlquantity", avlquantity);
                    
                    if (product.isIsSerialForProduct() && batch != null) {
                        String serialno = "";
                        JSONArray serialDetails = new JSONArray();
                        KwlReturnObject resultEdit = accMasterItemsDAOobj.getNewSerialDocumentMapping(documentid, batch.getId(), Integer.parseInt(moduleID));
                        List<SerialDocumentMapping> listEdit = resultEdit.getEntityList();
                        for (SerialDocumentMapping serialDocumentMapping : listEdit) {
                            NewBatchSerial batchSerial = serialDocumentMapping.getSerialid();

                            if (!StringUtil.isNullOrEmpty(serialno)) {
                                serialno = serialno + "," + batchSerial.getSerialname();
                            } else {
                                serialno = batchSerial.getSerialname();
                            }
                            JSONObject jObject = new JSONObject();
                            jObject.put("id", batchSerial.getId());
                            jObject.put("serialno", batchSerial.getSerialname());
                            jObject.put("serialnoid", batchSerial.getId());
                            jObject.put("expstart", batchSerial.getExpfromdate() != null ? df.format(batchSerial.getExpfromdate()) : "");
                            jObject.put("expend", batchSerial.getExptodate() != null ? df.format(batchSerial.getExptodate()) : "");
                            jObject.put("purchaseserialid", batchSerial.getId());
                            jObject.put("purchasebatchid", (batchSerial.getBatch() != null) ? batchSerial.getBatch().getId() : "");
                            jObject.put("skufield", batchSerial.getSkufield());
                            
                            obj.put("stocktype", serialDocumentMapping.getStockType());
                    

                            int transType1 = Constants.Acc_Delivery_Order_ModuleId;
                            String docId = "";
                            KwlReturnObject reusablecountobj = accCommonTablesDAO.getSerialsReusableCount(productid, batchSerial.getSerialname(), companyid, transType1, false, docId, batchSerial.getBatch() != null ? batchSerial.getBatch().getId() : null);
                            if (reusablecountobj.getEntityList() != null && !reusablecountobj.getEntityList().isEmpty()) {
                                if (reusablecountobj.getEntityList().get(0) != null) {
                                    double sumCount = Double.parseDouble(reusablecountobj.getEntityList().get(0).toString());
                                    jObject.put("reusablecount", sumCount);
                                } else {
                                    jObject.put("reusablecount", 0);
                                }
                            } else {
                                jObject.put("reusablecount", 0);
                            }
                            serialDetails.put(jObject);
                        }
                        obj.put("serialno", serialno);
                        obj.put("serialDetails", serialDetails.toString());
                    }
                    jSONArray.put(obj);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return jSONArray.toString();
    }
    
    public JSONObject getSalesOrderRowsDetails(Map<String, Object> requestParams,JSONObject paramJobj,Map<String,Double> socountMap) throws JSONException, UnsupportedEncodingException, ServiceException, ParseException, SessionExpiredException {
        JSONObject obj = new JSONObject();
        String gcurrencyid = (String) requestParams.get(Constants.globalCurrencyKey);
        DateFormat userdef=paramJobj.has("userdateformat")?new SimpleDateFormat(String.valueOf(paramJobj.get("userdateformat"))):null;
        String companyid = (String) requestParams.get(Constants.companyid);
        SalesOrder so = (SalesOrder) requestParams.get("salesOrderObject");
        KWLCurrency currency=(KWLCurrency) requestParams.get("currencyObject");
        SalesOrderDetail row=(SalesOrderDetail) requestParams.get("salesOrderDetailObject");
        Product rowProduct=(Product) requestParams.get("rowProductObject");
        CompanyAccountPreferences preferences=(CompanyAccountPreferences)requestParams.get("preferences");
        ExtraCompanyPreferences extraCompanyPreferences=(ExtraCompanyPreferences) requestParams.get("extraCompanyPreferences");
        KWLCurrency kwlcurrency=(KWLCurrency) requestParams.get("kwlcurrency");
        HashMap<String, Integer> FieldMap=(HashMap<String, Integer>) requestParams.get("fieldMap");
        HashMap<String, String> customFieldMap=(HashMap<String, String>) requestParams.get("customFieldMap");
        HashMap<String, String> customDateFieldMap=(HashMap<String, String>) requestParams.get("customDateFieldMap");
        boolean isExplodeAssemblyPrd=false;
        if(requestParams.containsKey("isExplodeAssemblyPrd") && requestParams.get("isExplodeAssemblyPrd") != null){
            isExplodeAssemblyPrd=(boolean) requestParams.get("isExplodeAssemblyPrd");
        }
        String dtype = paramJobj.optString("dtype",null);
        String closeflag = paramJobj.optString("closeflag",null);
        int requestModuleID=-1; // moduleID from which the function has been called
        if (paramJobj.optString("requestModuleid",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("requestModuleid",""))) {
            requestModuleID = Integer.parseInt(paramJobj.getString("requestModuleid")); 
        }
        DateFormat userDateFormat=null;
        if(requestParams.containsKey(Constants.userdf) && requestParams.get(Constants.userdf)!=null){
            userDateFormat=(DateFormat)requestParams.get(Constants.userdf);
        }
        boolean doflag = paramJobj.optString("doflag",null)!=null?true:false;
        boolean posoflag = (StringUtil.isNullOrEmpty(paramJobj.optString(Constants.POSOFLAG,"")))?false:Boolean.parseBoolean((String)paramJobj.get(Constants.POSOFLAG));
        boolean isFromContract = paramJobj.optString("isFromContract",null)!=null?true:false;
        boolean isForDOGROLinking = (StringUtil.isNullOrEmpty(paramJobj.optString("isForDOGROLinking","")))?false:Boolean.parseBoolean(paramJobj.optString("isForDOGROLinking",""));
        boolean isForm = !StringUtil.isNullOrEmpty(paramJobj.optString("isForm","")) ? Boolean.parseBoolean(paramJobj.optString("isForm","")) : false;
        boolean isConsignment=(StringUtil.isNullOrEmpty(paramJobj.optString("isConsignment","")))?false:Boolean.parseBoolean(paramJobj.optString("isConsignment",""));
        boolean isLeaseFixedAsset = (StringUtil.isNullOrEmpty(paramJobj.optString("isLeaseFixedAsset","")))?false:Boolean.parseBoolean(paramJobj.optString("isLeaseFixedAsset",""));
        boolean isForInvoice = Boolean.FALSE.parseBoolean(paramJobj.optString("isForInvoice",""));
        boolean isForLinking = Boolean.FALSE.parseBoolean(paramJobj.optString("isForLinking",""));
        boolean isForReport=false;
        boolean isCopy = !StringUtil.isNullOrEmpty(paramJobj.optString("iscopy","")) ? Boolean.parseBoolean(paramJobj.optString("iscopy","")) : false;
        KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = companyObj!=null ? (Company) companyObj.getEntityList().get(0):null;
        int countryid = ( company != null && company.getCountry() != null) ? Integer.parseInt(company.getCountry().getID()) : 0;
        String description="";
        if (!StringUtil.isNullOrEmpty(dtype) && dtype.equals("report")) {
            isForReport = true;
        }
        boolean isExport = false;
        if (paramJobj.optString(Constants.isExport,null) != null) {
            isExport = Boolean.parseBoolean(paramJobj.optString(Constants.isExport,""));
        }
        List masterFieldsResultList=Collections.EMPTY_LIST;
        if(requestParams.containsKey("masterFieldsResultList")){
            masterFieldsResultList=(List) requestParams.get("masterFieldsResultList");;
        }
        
        HashMap<String, Object> serialModuleParams = new HashMap();
        serialModuleParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
        serialModuleParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.SerialWindow_ModuleId, 1));
        HashMap<String, String> replaceFieldMap2 = new HashMap<String, String>();
        HashMap<String, String> customFieldMap2 = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap2 = new HashMap<String, String>();
        HashMap<String, Integer> serialModulefieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(serialModuleParams, replaceFieldMap2, customFieldMap2, customDateFieldMap2);

        StringBuilder documentIDs = new StringBuilder();
        StringBuilder documentIDsforWithotWLBSP = new StringBuilder();
        for (SalesOrderDetail deliveryOrderDetail : so.getRows()) {
            Product product = deliveryOrderDetail.getProduct();
            if (product != null && !StringUtil.isNullOrEmpty(product.getID())) {
                if ((preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory())) {  //check if company level option is on then only we will check productt level
                    if (product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct() || product.isIsSerialForProduct()) {
                        documentIDs.append("'" + deliveryOrderDetail.getID() + "'").append(",");
                    } else {
                        documentIDsforWithotWLBSP.append("'" + deliveryOrderDetail.getID() + "'").append(",");
                    }
                }
            }
        }

        Map<String, List<Object[]>> baMap = new HashMap<>();
                Map<String, Object> batchSerialReqMap = new HashMap<>();
                batchSerialReqMap.put(Constants.companyKey, companyid);
//                batchSerialReqMap.put(Constants.df, df);
                batchSerialReqMap.put("linkingFlag", isForLinking);
                batchSerialReqMap.put("isEdit", false);
//                batchSerialReqMap.put("readOnly",readOnly);
                batchSerialReqMap.put(Constants.isConsignment, isConsignment);
                batchSerialReqMap.put("srflag", false);
                batchSerialReqMap.put("moduleID", requestModuleID);
                if (documentIDs.length() > 0) {
                    batchSerialReqMap.put("documentIds", documentIDs.substring(0, documentIDs.length() - 1));
                    baMap = getBatchDetailsMap(batchSerialReqMap);
                }
        

        obj.put(Constants.billid, so.getID());
        obj.put("dorowid", row.getID());
        obj.put("joborderitem", row.isJobOrderItem());
        obj.put("billno", so.getSalesOrderNumber());
        obj.put("salesPerson", so.getSalesperson() != null ? so.getSalesperson().getID() : "");
        obj.put("currencysymbol", currency.getSymbol());
        obj.put("srno", row.getSrno());
        obj.put("rowid", row.getID());
        obj.put("originalTransactionRowid", row.getID());
        obj.put("productid", rowProduct.getID());
        obj.put("purchasetaxId", rowProduct.getPurchasetaxid());
        obj.put("salestaxId", rowProduct.getSalestaxid());
        obj.put("hasAccess", rowProduct.isIsActive());
        obj.put("customeridforshippingaddress", so.getCustomer()!= null ? so.getCustomer().getID() : "");
        obj.put("sourcepurchaseorderdetailid", row.getSourcePurchaseOrderDetailsid()!= null ? row.getSourcePurchaseOrderDetailsid() : "");
        obj.put("prodtype",rowProduct.getProducttype().getID());
        
        CommonFunctions.getterMethodForProductsData(rowProduct, masterFieldsResultList, obj);

       
        String uom = row.getUom() != null ? row.getUom().getNameEmptyforNA() : rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getNameEmptyforNA();
        if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
            obj.put("uomname",  rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getID());
            obj.put("uomnameValue", row.getUom() != null ? row.getUom().getNameEmptyforNA() : rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getNameEmptyforNA());
//            String uomname=row.getUom() != null ? row.getUom().getName() : rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getName();
//            obj.put("conversionfactor","1 "+uomname+" =1 "+uomname );
        } else {
            obj.put("unitname", uom);
            obj.put("uomname", uom);
        }
        obj.put("baseuomid", rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getID());
        obj.put("baseuomname", rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getNameEmptyforNA());
        obj.put("multiuom", rowProduct.isMultiuom());
        
        if (isExplodeAssemblyPrd && !StringUtil.isNullOrEmpty(rowProduct.getDescription())){
            description = rowProduct.getDescription();
        }else if (!StringUtil.isNullOrEmpty(row.getDescription())) {
            description = row.getDescription();
        }else if (!StringUtil.isNullOrEmpty(rowProduct.getDescription())) {
            description = rowProduct.getDescription();
        } else {
            description = "";
        }
        String linedesc="";//Description is encoded for Web-application & Mobile Apps
        try {
            linedesc = StringUtil.DecodeText(description);
        } catch (Exception ex) {
            linedesc = description;
        }
        obj.put("desc", linedesc);
        obj.put("description", linedesc);
        
        if (rowProduct.isAsset()) {  //For Fixed Asset Group, type will be "Asset"
            obj.put("type", "Asset");
        } else {
            obj.put("type", rowProduct.getProducttype() == null ? "" : rowProduct.getProducttype().getName());
        }
        obj.put("status", (row.isIsLineItemClosed() ||( isConsignment && row.isIsLineItemRejected()))?"Yes":"N/A");
         if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
            obj.put("productID", rowProduct.getProductid());
            obj.put("productidValue", rowProduct.getName());
            obj.put("remark", row.getRemark());
         } else {
            obj.put("pid", rowProduct.getProductid());
            obj.put("productname", rowProduct.getName());
            obj.put("memo", row.getRemark());
        }
       /**
        * Get inspection template and inspection form with details
        */
        obj.put("inspectionTemplate", (row.getInspectionTemplate() == null) ? "None" : row.getInspectionTemplate().getId());
        obj.put("inspectionForm", (row.getInspectionForm() == null) ? "" : row.getInspectionForm().getId());
       /**
        * Get inspection form from SO line level
        */        
        InspectionForm inspectionForm = row.getInspectionForm();
        if(inspectionForm != null){
            JSONArray detailsJArr = new JSONArray();
            Set<InspectionFormDetails> insFormDetailSet = inspectionForm.getRows();
            //get inspection form details which are stored at SO line level
            for(InspectionFormDetails insFormDetail : insFormDetailSet){
                JSONObject insFormDetailJobj = new JSONObject();
                insFormDetailJobj.put("templateId", (row.getInspectionTemplate() == null) ? "" : row.getInspectionTemplate().getId());
                insFormDetailJobj.put("areaId", insFormDetail.getInspectionArea() == null ? "" : insFormDetail.getInspectionArea().getId());
                insFormDetailJobj.put("areaName", insFormDetail.getInspectionAreaValue());
                insFormDetailJobj.put("status", insFormDetail.getInspectionStatus());
                insFormDetailJobj.put("faults", insFormDetail.getFaults());
                insFormDetailJobj.put("passingValue", insFormDetail.getPassingValue() == null ? "" : insFormDetail.getPassingValue());

                detailsJArr.put(insFormDetailJobj);
            }
            obj.put("inspectionAreaDetails", detailsJArr.toString());
        }
        
        obj.put("invstore", (StringUtil.isNullOrEmpty(row.getInvstoreid())) ? "" : row.getInvstoreid());
        obj.put("invlocation", (StringUtil.isNullOrEmpty(row.getInvlocid())) ? "" : row.getInvlocid());

        if (extraCompanyPreferences.isActivateProfitMargin()) {
            KwlReturnObject sodVenRes = accSalesOrderDAOobj.getSalesOrderDetailsVendorMapping(row.getID());
            if (sodVenRes.getEntityList().size() > 0) {
                SODetailsVendorMapping sodVendObj = (SODetailsVendorMapping) sodVenRes.getEntityList().get(0);
                obj.put("vendorid", sodVendObj.getVendor() != null ? sodVendObj.getVendor().getID() : "");
                obj.put("vendorunitcost", sodVendObj.getUnitcost());
                obj.put("vendorcurrexchangerate", sodVendObj.getExchangerate());
                obj.put("vendorcurrencyid", sodVendObj.getVendor() != null ? (sodVendObj.getVendor().getCurrency() != null ? sodVendObj.getVendor().getCurrency().getCurrencyID() : gcurrencyid) : "");
                obj.put("vendorcurrencysymbol", sodVendObj.getVendor() != null ? (sodVendObj.getVendor().getCurrency() != null ? sodVendObj.getVendor().getCurrency().getSymbol() : kwlcurrency.getSymbol()) : "");
                obj.put("totalcost", sodVendObj.getTotalcost());
            }
        }

        String productsBaseUomId = (rowProduct.getUnitOfMeasure() == null) ? "" : rowProduct.getUnitOfMeasure().getID();
        String selectedUomId = (row.getUom() != null) ? row.getUom().getID() : "";

        if (rowProduct.isblockLooseSell() && !productsBaseUomId.equals(selectedUomId)) {
            // Get Available Quantity of Product For Selected UOM
            KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(rowProduct.getID(), selectedUomId);
            double availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
            obj.put("availableQtyInSelectedUOM", availableQuantity);
            obj.put("isAnotherUOMSelected", true);

            // Getting Open PO/SO count
            HashMap<String, Object> orderParams = new HashMap<String, Object>();
            orderParams.put("companyid", companyid);
            orderParams.put("gcurrencyid", gcurrencyid);
            orderParams.put("df", authHandler.getDateOnlyFormat());            
            orderParams.put("pendingapproval", false);
            orderParams.put("startdate", authHandler.getDates(preferences.getFinancialYearFrom(), true));
            orderParams.put("enddate", authHandler.getDates(preferences.getFinancialYearFrom(), false));
            orderParams.put("currentuomid", selectedUomId);
            orderParams.put("productId", rowProduct.getID());

            double pocountinselecteduom = getPOCount(orderParams);

            double socountinselecteduom = getSOCount(orderParams);

            obj.put("pocountinselecteduom", pocountinselecteduom);
            obj.put("socountinselecteduom", socountinselecteduom);
            obj.put("socount", socountinselecteduom);
        } else {
            KwlReturnObject result = accProductObj.getQuantity(rowProduct.getID());
            obj.put("availableQtyInSelectedUOM", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));
            obj.put("availablequantity", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));
            if ((preferences.getNegativeStockSO() == 1 || preferences.getNegativeStockSO() == 2 || extraCompanyPreferences.isOpenPOandSO()) && socountMap!=null && socountMap.containsKey(rowProduct.getID()) && socountMap.size() > 0) {
                double socount = socountMap.get(rowProduct.getID());// accProductControllerCMN.getOutstandingPoSoProductsCount(requestMap, false, rowProduct.getID(), accountingHandlerDAOobj, accGoodsReceiptDAOobj, accInvoiceDAOobj, accSalesOrderDAOobj, accPurchaseOrderobj, productuomId);//List list,
                obj.put("socount", socount);
                obj.put("socountinselecteduom", socount);
            }
        }
        if ((preferences.getNegativeStockSICS() == 1 || preferences.getNegativeStockSICS() == 2) && isForm && requestModuleID == Constants.Acc_Invoice_ModuleId) {
            HashMap<String, Object> requestMap = new HashMap<String, Object>();
            requestMap.put("companyid", companyid);
            requestMap.put("gcurrencyid", gcurrencyid);
            requestMap.put("df", authHandler.getDateFormatter(paramJobj));
            requestMap.put("pendingapproval", false);
            requestMap.put("productId", rowProduct.getID());
            double invoiceProductCount = accProductControllerCMN.getOutstandingSICount(requestMap, accInvoiceDAOobj);
            obj.put("sicount", invoiceProductCount);
        }
        if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {  //This is sats specific code 	 
            obj.put("invoicetype", so.getInvoicetype());

            obj.put("dependentType", row.getDependentType() == null ? "" : row.getDependentType());
          
                obj.put("showquantity", StringUtil.DecodeText(row.getShowquantity() == null ? "" : row.getShowquantity()));
            
            obj.put("inouttime", StringUtil.isNullOrEmpty(row.getInouttime()) ? "" : row.getInouttime().replaceAll("%20", " "));
            if (!StringUtil.isNullOrEmpty(row.getInouttime())) {
                try {
                    String interVal = getTimeIntervalForProduct(row.getInouttime());
                    obj.put("timeinterval", interVal);
                } catch (ParseException ex) {
                    Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            try {
                String numberVal = row.getDependentType();
                obj.put("dependentTypeNo", Integer.parseInt(numberVal));
            } catch (Exception e) {
            }

            obj.put("parentid", ((rowProduct.getParent() != null) ? rowProduct.getParent().getID() : ""));
            obj.put("parentname", ((rowProduct.getParent() != null) ? rowProduct.getParent().getName() : ""));
            if (rowProduct.getParent() != null) {
                obj.put("issubproduct", true);
            }
            if (rowProduct.getChildren().size() > 0) {
                obj.put("isparentproduct", true);
            } else {
                obj.put("isparentproduct", false);
            }
            
                obj.put("showquantity",  StringUtil.DecodeText(row.getShowquantity() == null ? "" : row.getShowquantity()));
           
        }

        //used in Rest Services --This are the dataindex of all imports
        if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
            obj.put("discountType", row.getDiscountispercent());
            obj.put("discountTypeValue", row.getDiscountispercent()==1?"Percentage":"Flat");
            obj.put("discount", row.getDiscount());
        } else {
            obj.put("discountispercent", row.getDiscountispercent());
            obj.put("prdiscount", row.getDiscount());
        }
        obj.put("lockquantity", row.getLockquantity()); //for getting locked  quantity of indivisual so
        obj.put("lockquantitydue", row.getLockquantitydue()); //for getting locked  quantity due of indivisual so
        obj.put("islockQuantityflag", so.isLockquantityflag()); //for getting locked flag of indivisual so
        obj.put("isConsignment", so.isIsconsignment()); //for getting is consignment request
        if (so.isIsconsignment() && so.getCustWarehouse() != null) {
            obj.put("custWarehouse", so.getCustWarehouse().getId()); //for getting customer warehouse
        }
        double prodavlqty = 0.0;
        if (so.isIsconsignment()) {
            obj.put("todate", so.getTodate() == null ? "" : authHandler.getDateOnlyFormat().format(so.getTodate()));
            obj.put("fromdate", so.getFromdate() == null ? "" : authHandler.getDateOnlyFormat().format(so.getFromdate()));
            obj.put("requestWarehouse", so.getRequestWarehouse() != null ? so.getRequestWarehouse().getId() : "");
            obj.put("requestLocation", so.getRequestLocation() != null ? so.getRequestLocation().getId() : "");
            obj.put("autoapproveflag", so.isAutoapproveflag());
            obj.put("isrejected", row.isIsLineItemRejected());
            obj.put("rejectionreason", row.getRejectionreason() != null ? row.getRejectionreason() :"");

            if (rowProduct != null && so.getRequestWarehouse() != null && so.getRequestLocation() != null) {
                if (!StringUtil.isNullOrEmpty(so.getRequestWarehouse().getId()) && !StringUtil.isNullOrEmpty(so.getRequestLocation().getId())) {
                    prodavlqty = accProductObj.getProductQuantityUnderParticularStoreLocation(rowProduct.getID(), so.getRequestWarehouse().getId(), so.getRequestLocation().getId(), companyid);
                    obj.put("prodavlqty", prodavlqty < 0 ? row.getBaseuomquantity() : prodavlqty);
                }

            } else {
                obj.put("prodavlqty", rowProduct.getAvailableQuantity() < 0 ? row.getBaseuomquantity() : rowProduct.getAvailableQuantity());
            }
        }
        boolean isLocationForProduct=false;
        boolean isWarehouseForProduct=false;
        boolean isBatchForProduct=false;
        boolean isSerialForProduct=false;
        boolean isRowForProduct=false;
        boolean isRackForProduct=false;
        boolean isBinForProduct=false;
        boolean isAutoAssembly=false;
        double minqty = 0.0;
        double maxqty = 0.0;
        String productsDefaultLocation="",productsDefaultWarehouse="";
        
        if (!StringUtil.isNullOrEmpty(rowProduct.getID())) {
            Product product = rowProduct;
            isLocationForProduct = product.isIslocationforproduct();
            isWarehouseForProduct = product.isIswarehouseforproduct();
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
            isRowForProduct = product.isIsrowforproduct();
            isRackForProduct = product.isIsrackforproduct();
            isBinForProduct = product.isIsbinforproduct();
            maxqty = product.getMaxOrderingQuantity();
            minqty = product.getMinOrderingQuantity();
            isAutoAssembly=product.isAutoAssembly();
            productsDefaultLocation = (product.getLocation() != null && product.getLocation().getId() != null) ? product.getLocation().getId() : "";
            productsDefaultWarehouse = (product.getWarehouse() != null && product.getWarehouse().getId() != null) ? product.getWarehouse().getId() : "";
            if (extraCompanyPreferences != null && extraCompanyPreferences.getUomSchemaType() == Constants.PackagingUOM) {
                obj.put("caseuom", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUoM().getID() : "");
                obj.put("caseuomvalue", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUomValue() : 1);
                obj.put("inneruom", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUoM().getID() : "");
                obj.put("inneruomvalue", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUomValue() : 1);
                obj.put("stockuom", (product.getUnitOfMeasure() != null) ? product.getUnitOfMeasure().getID() : "");
                obj.put("stockuomvalue", (product.getPackaging() != null && product.getPackaging().getStockUoM()!= null) ? product.getPackaging().getStockUomValue(): 1);
            } 
            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                if (product.getPackaging() != null) {
                    String packagingString = (product.getPackaging() != null && product.getPackaging().toString() != null) ? product.getPackaging().toString() : "";
                    obj.put(Constants.packaging, packagingString);
                } else {
                    obj.put(Constants.packaging, "");
                }
            }
        }
        obj.put("isLocationForProduct", isLocationForProduct);
        obj.put("isWarehouseForProduct", isWarehouseForProduct);
        obj.put("isBatchForProduct", isBatchForProduct);
        obj.put("isSerialForProduct", isSerialForProduct);
        obj.put("isRowForProduct", isRowForProduct);
        obj.put("isRackForProduct", isRackForProduct);
        obj.put("isBinForProduct", isBinForProduct);
        obj.put("location", productsDefaultLocation);
        obj.put("warehouse", productsDefaultWarehouse);
        obj.put("isAutoAssembly", isAutoAssembly);
        obj.put("maxorderingquantity", maxqty); 
        obj.put("minorderingquantity", minqty);
       
        //Cheking Batch details if lockquantity flag is set. if not found and item having Location warehouse then we checking for Default batch JSON
        
        paramJobj.put("isForLinking", isForLinking);
        if ((preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) && !isCopy) {  //check if company level option is on then only we will check productt level
            if (isBatchForProduct || isSerialForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct) {  //product level batch and serial no on or not
                String batchdetails = so.isLockquantityflag() ? getNewBatchJson(rowProduct,paramJobj, row.getID()) : "";
                if (!StringUtil.isNullOrEmpty(batchdetails) && (!extraCompanyPreferences.isAutoFillBatchDetails() || isConsignment) && !(isCopy && (isBatchForProduct || isSerialForProduct))) {
                    obj.put("batchdetails", batchdetails);
                }else if(extraCompanyPreferences.isAutoFillBatchDetails() && !isConsignment&& !isLeaseFixedAsset && !(isCopy && (isBatchForProduct || isSerialForProduct))){
                     obj.put("batchdetails", getNewBatchDetailJson(batchSerialReqMap, rowProduct, row.getID(), serialModulefieldMap, baMap, replaceFieldMap2, customFieldMap2, customDateFieldMap2));
                } 
                else if (isForDOGROLinking && ((isLocationForProduct && !StringUtil.isNullOrEmpty(productsDefaultLocation)) || (isWarehouseForProduct && !StringUtil.isNullOrEmpty(productsDefaultWarehouse))) && !isBatchForProduct && !isSerialForProduct) {
                    obj.put("batchdetails", getdefaultBatchJson(rowProduct,row.getID(), (row.getQuantity()*row.getBaseuomrate()), so, prodavlqty,doflag));
                }
            }
        }
        obj.put("isLocationForProduct", isLocationForProduct);
        obj.put("isWarehouseForProduct", isWarehouseForProduct);
        obj.put("isBatchForProduct", isBatchForProduct);
        obj.put("isSerialForProduct", isSerialForProduct);
        double rejectedCount = 0, pendingCount = 0;
        if (isConsignment && so.isLockquantityflag() && !isForReport && rowProduct != null && rowProduct.isIsSerialForProduct()) {//for Consignment request not blocked quantity.
            KwlReturnObject result = accSalesOrderDAOobj.getSerialsFormDocumentid(row.getID(), row.getCompany().getCompanyID());
            Iterator serialItr = result.getEntityList().iterator();
            while (serialItr.hasNext()) {
                SerialDocumentMapping documentMapping = (SerialDocumentMapping) serialItr.next();
                if (documentMapping != null && documentMapping.getRequestApprovalStatus() == RequestApprovalStatus.REJECTED) {
                    rejectedCount += 1;
                }
                if (documentMapping != null && documentMapping.getRequestApprovalStatus() == RequestApprovalStatus.PENDING) {
                    pendingCount += 1;
                }
            }
        }
        if (isConsignment && so.isAutoapproveflag() && !row.getApproverSet().isEmpty()) {//for Consignment request not blocked quantity.
            if (row.getRejectedQuantity() == row.getBaseuomquantity()) {
                obj.put("approvalstatus", "Rejected");
                obj.put("approvedserials", "  -");
            } else if ((row.getApprovedQuantity() + row.getRejectedQuantity()) == row.getBaseuomquantity()) {
                obj.put("approvalstatus", "Approved");
                obj.put("approvedserials", "  -");
            } else if ((row.getApprovedQuantity() + row.getRejectedQuantity()) == 0) {
                obj.put("approvalstatus", "Pending Approval");
                obj.put("approvedserials", "  -");
            } else if ((row.getApprovedQuantity() + row.getRejectedQuantity()) < row.getBaseuomquantity()) {
                obj.put("approvalstatus", "Partially Approved");
                obj.put("approvedserials", "  -");
            }
            rejectedCount = row.getRejectedQuantity();
            pendingCount = row.getBaseuomquantity() - (row.getApprovedQuantity() + row.getRejectedQuantity());

        } else if (isConsignment) {//for Consignment request not blocked quantity.
            obj.put("approvalstatus", "  -");
            obj.put("approvedserials", "  -");
        }
        if (row.getSalesOrder().getLeaseOrMaintenanceSO() == 1) {// if it is a lease SO
            getAssetDetailJsonObject(paramJobj, row, obj);
        }

        if (!StringUtil.isNullOrEmpty(row.getPurchaseorderdetailid())) {
            KwlReturnObject podetailresult = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(), row.getPurchaseorderdetailid());
            PurchaseOrderDetail purchaseOrderDetail = (PurchaseOrderDetail) podetailresult.getEntityList().get(0);
            if (purchaseOrderDetail != null) {
                obj.put("linkto", purchaseOrderDetail.getPurchaseOrder().getPurchaseOrderNumber());
                obj.put("linkid", purchaseOrderDetail.getPurchaseOrder().getID());
                /*
                     ERM-1037
                     Field used for comparing dates to restrict linking of future doument date in Sales Order document editing
                 */
                obj.put("linkDate", purchaseOrderDetail.getPurchaseOrder().getOrderDate());
                obj.put("billblockstatus", purchaseOrderDetail.getPurchaseOrder().isDisabledPOforSO()); //Passing the Status of linked Purchase order whether it is blocked Or not for edit view and copy case of Sales order ERP-35541 
                obj.put("rowid", purchaseOrderDetail.getID());
                obj.put("savedrowid", row.getID());
                obj.put("docrowid", row.getID());
                obj.put("linktype", 4);
            }
        } else if (!StringUtil.isNullOrEmpty(row.getMrpcontractdetailid())) {
            KwlReturnObject podetailresult = accountingHandlerDAOobj.getObject(MRPContractDetails.class.getName(), row.getMrpcontractdetailid());
            MRPContractDetails mrpContractDetails = (MRPContractDetails) podetailresult.getEntityList().get(0);
            if (mrpContractDetails != null) {
                obj.put("linkto", mrpContractDetails.getMrpcontract().getContractid());
                obj.put("linkid", mrpContractDetails.getMrpcontract().getID());
                /*
                     ERM-1037
                     Field used for comparing dates to restrict linking of future doument date in Sales Order document editing
                 */
                obj.put("linkDate", mrpContractDetails.getMrpcontract().getCreationdate());
                obj.put("rowid", mrpContractDetails.getID());
                obj.put("savedrowid", row.getID());
                obj.put("docrowid", row.getID());
                obj.put("linktype", 10); //For Master Contract
            }
        } else if (row.getQuotationDetail() != null) {
            obj.put("linkto", row.getQuotationDetail().getQuotation().getquotationNumber());
            obj.put("linkid", row.getQuotationDetail().getQuotation().getID());
            /*
                 ERM-1037
                 Field used for comparing dates to restrict linking of future doument date in Sales Order document editing
             */
            obj.put("linkDate", row.getQuotationDetail().getQuotation().getQuotationDate());
            obj.put("rowid", row.getQuotationDetail().getID());
            obj.put("savedrowid", row.getID());
            obj.put("docrowid", row.getID());
            obj.put("linktype", (so.getLeaseOrMaintenanceSO() == 1) ? 0 : 2);// if lease SO then link type will be 0 but for normal it will be 2. as defined in Invoice.js and FixedAssetInvoice.js
        } else if (row.getProductReplacementDetail() != null) {
            obj.put("linkto", row.getProductReplacementDetail().getProductReplacement().getReplacementRequestNumber());
            obj.put("linkid", row.getProductReplacementDetail().getProductReplacement().getId());
            obj.put("rowid", row.getProductReplacementDetail().getId());
            obj.put("savedrowid", row.getID());
            obj.put("docrowid", row.getID());
            obj.put("linktype", (so.getLeaseOrMaintenanceSO() == 1) ? 1 : 3);// if lease SO then link type will be 1 but for normal it will be 3. as defined in Invoice.js and FixedAssetInvoice.js
        } else {
            obj.put("linkto", "");
            obj.put("linkid", "");
            obj.put("linktype", -1);
        }
        obj.put("recTermAmount",row.getRowtermamount());
        obj.put("OtherTermNonTaxableAmount",row.getOtherTermNonTaxableAmount());
        JSONArray TermdetailsjArr = new JSONArray();
        if(extraCompanyPreferences.getLineLevelTermFlag()==1){ // For India Country 
            if(!posoflag){
                if (extraCompanyPreferences.isAvalaraIntegration()) {
                    JSONObject paramsForTaxJobj = new JSONObject();
                    paramsForTaxJobj.put(IntegrationConstants.parentRecordID, row.getID());
                    TermdetailsjArr = integrationCommonService.getTransactionDetailTaxMapping(paramsForTaxJobj);
                } else {
                    HashMap<String, Object> quotationDetailParams = new HashMap<String, Object>();
                    quotationDetailParams.put("salesOrderDetailId", row.getID());
                    KwlReturnObject salesOrderTermMapresult = accSalesOrderDAOobj.getSalesOrderDetailTermMap(quotationDetailParams);
                    List<SalesOrderDetailTermMap> salesOrderDetailTermsMapList = salesOrderTermMapresult.getEntityList();
                    for (SalesOrderDetailTermMap invoicedetailTermMap : salesOrderDetailTermsMapList) {
                        LineLevelTerms mt = invoicedetailTermMap.getTerm();
                        com.krawler.utils.json.base.JSONObject jsonObj = new com.krawler.utils.json.base.JSONObject();
                        jsonObj.put("id", mt.getId());
                        jsonObj.put("termid", invoicedetailTermMap.getTerm().getId());
                        /**
                         * ERP-32829 
                         */
                        jsonObj.put("productentitytermid", invoicedetailTermMap.getEntitybasedLineLevelTermRate()!=null?invoicedetailTermMap.getEntitybasedLineLevelTermRate().getId():"");
                        jsonObj.put("isDefault", invoicedetailTermMap.isIsGSTApplied());
                        jsonObj.put("term", mt.getTerm());
                        jsonObj.put("formulaids", mt.getFormula());
                        jsonObj.put("termamount", invoicedetailTermMap.getTermamount());
                        jsonObj.put("termpercentage", invoicedetailTermMap.getPercentage());
                        jsonObj.put("originalTermPercentage", mt.getPercentage());
                        jsonObj.put("glaccountname", mt.getAccount().getName());
                        jsonObj.put("accountid", mt.getAccount().getID());
                        jsonObj.put("glaccount", mt.getAccount().getID());
                        jsonObj.put("formType", !StringUtil.isNullOrEmpty(mt.getFormType())?mt.getFormType():"1");
                        jsonObj.put("IsOtherTermTaxable", mt.isOtherTermTaxable());
                        jsonObj.put("assessablevalue", invoicedetailTermMap.getAssessablevalue());
                        jsonObj.put("purchasevalueorsalevalue", invoicedetailTermMap.getPurchaseValueOrSaleValue());
                        jsonObj.put("deductionorabatementpercent", invoicedetailTermMap.getDeductionOrAbatementPercent());
                        jsonObj.put("taxtype", invoicedetailTermMap.getTaxType());
                        jsonObj.put("taxvalue", invoicedetailTermMap.getPercentage());
                        jsonObj.put("termtype", invoicedetailTermMap.getTerm().getTermType());
                        jsonObj.put("termsequence", invoicedetailTermMap.getTerm().getTermSequence());
                        jsonObj.put(IndiaComplianceConstants.GST_CESS_TYPE, invoicedetailTermMap.getEntitybasedLineLevelTermRate()!=null && invoicedetailTermMap.getEntitybasedLineLevelTermRate().getCessType()!=null ? invoicedetailTermMap.getEntitybasedLineLevelTermRate().getCessType().getId() : "");
                        jsonObj.put(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT, invoicedetailTermMap.getEntitybasedLineLevelTermRate()!=null ? invoicedetailTermMap.getEntitybasedLineLevelTermRate().getValuationAmount() : 0.0);
                        jsonObj.put(IndiaComplianceConstants.DEFAULT_TERMID, invoicedetailTermMap.getTerm()!=null && invoicedetailTermMap.getTerm().getDefaultTerms()!=null ? invoicedetailTermMap.getTerm().getDefaultTerms().getId() : "");
                        TermdetailsjArr.put(jsonObj);
                    }
                }
            } else{
                boolean isDefault = true;
                Map<String,Object> mapData=new HashMap<String,Object>();
                mapData.put("productid", rowProduct.getID());
                mapData.put("salesOrPurchase", false);
                if(isDefault){
                    mapData.put("isDefault", isDefault);
                }
                KwlReturnObject result6 = accProductObj.getProductTermDetails(mapData);
                if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                    ArrayList<ProductTermsMap> productTermDetail = (ArrayList<ProductTermsMap>) result6.getEntityList();
                    TermdetailsjArr = CommonFunctions.fetchProductTermMapDetails(productTermDetail);
                }
                if(isDefault){
                    mapData.put("isDefault", false);
                }
                KwlReturnObject result7 = accProductObj.getProductTermDetails(mapData);
                if (result7.getEntityList() != null && result7.getEntityList().size() > 0 && result7.getEntityList().get(0) != null) {
                    ArrayList<ProductTermsMap> productTermDetail = (ArrayList<ProductTermsMap>) result7.getEntityList();
                    JSONArray productTermJsonArry = CommonFunctions.fetchProductTermMapDetails(productTermDetail);
                    obj.put("uncheckedTermdetails", productTermJsonArry.toString());
                }
                obj.put("recTermAmount",0);
            }
            if (row.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                /**
                 * Put GST Tax Class History.
                 */
                obj.put("refdocid", row.getID());
                fieldDataManagercntrl.getGSTTaxClassHistory(obj);
            }
        }

        if(countryid == Constants.indian_country_id){ // For India Country 
        // Excise AND VAT special Rate type TAX ------ START-------                        
            boolean carryin = true;
            String uomid = (row == null) ? "" : (row.getUom() == null) ? "" : row.getUom().getID();
            if (extraCompanyPreferences.isExciseApplicable()) {
                 String reortingUOM= (row.getReportingUOMExcise()!=null)?row.getReportingUOMExcise().getID():"";
                 String valuationType=!StringUtil.isNullOrEmpty(row.getExciseValuationType())?row.getExciseValuationType():"";
                obj.put("valuationType", valuationType);
                if ((Constants.QUENTITY).equals(valuationType)) {
                    obj.put("compairwithUOM", 1);
                    obj.put("reortingUOMExcise", reortingUOM);
                    
                    if (row.getReportingSchemaTypeExcise() != null && !reortingUOM.equals(uomid)) {
                        String reportinguomschema = row.getReportingSchemaTypeExcise().getID();
                        obj.put("reortingUOMSchemaExcise", reportinguomschema);
                        HashMap<String, Object> hsMap = new HashMap<String, Object>();
                        hsMap.put("uomschematypeid", reportinguomschema);
                        hsMap.put("currentuomid", uomid);
                        hsMap.put("carryin", carryin);
                        hsMap.put("companyid", companyid);
                        KwlReturnObject convertor = accProductObj.getProductBaseUOMRate(hsMap);
                        List list = convertor.getEntityList();
                        Iterator itrList = list.iterator();
                        if (itrList.hasNext()) {
                            UOMSchema rowPOExcise = (UOMSchema) itrList.next();
                            if (rowPOExcise != null) {
                                obj.put("compairwithUOM", rowPOExcise.getBaseuomrate());
                            }
                        }
                    }
                } else if ((Constants.MRP).equals(valuationType)) {
                    obj.put("productMRP",row.getMrpIndia());
                }
            }
            if (extraCompanyPreferences.isEnableVatCst()) {
                String reortingUOMVAT= (row.getReportingUOMVAT()!=null)? row.getReportingUOMVAT().getID():"";
                String valuationTypeVAT=!StringUtil.isNullOrEmpty(row.getVatValuationType())?row.getVatValuationType():"";
                obj.put("valuationTypeVAT", valuationTypeVAT);
                if ((Constants.QUENTITY).equals(valuationTypeVAT)) {
                    obj.put("compairwithUOMVAT", 1);
                    obj.put("reportingUOMVAT", reortingUOMVAT);
                    if (row.getReportingSchemaVAT() != null && !reortingUOMVAT.equals(uomid)) {
                        String reportinguomschema = row.getReportingSchemaVAT().getID();
                        obj.put("reportingUOMSchemaVAT", reportinguomschema);
                        HashMap<String, Object> hsMap = new HashMap<String, Object>();
                        hsMap.put("uomschematypeid", reportinguomschema);
                        hsMap.put("currentuomid", uomid);
                        hsMap.put("carryin", carryin);
                        hsMap.put("companyid", companyid);
                        KwlReturnObject convertor = accProductObj.getProductBaseUOMRate(hsMap);
                        List list = convertor.getEntityList();
                        Iterator itrList = list.iterator();
                        if (itrList.hasNext()) {
                            UOMSchema rowPOVAT = (UOMSchema) itrList.next();
                            if (rowPOVAT != null) {
                                obj.put("compairwithUOMVAT", rowPOVAT.getBaseuomrate());
                            }
                        }
                    }

                } else if ((Constants.MRP).equals(valuationTypeVAT)) {
                    obj.put("productMRP",row.getMrpIndia());
                }
            }
            // Excise AND VAT special type TAX ------ END-------          
        }
        obj.put("LineTermdetails", TermdetailsjArr);

        double rowTaxPercent = 0;
        double rowTaxAmount = 0;
        boolean isRowTaxApplicable = false;
        if (row.getTax() != null) {
            KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), so.getOrderDate(), row.getTax().getID());
            rowTaxPercent = (Double) perresult.getEntityList().get(0);
            isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
            if (isRowTaxApplicable) {
                rowTaxAmount = row.getRowTaxAmount();
            }
        }
        obj.put("prtaxpercent", rowTaxPercent);
        obj.put("rowTaxAmount", extraCompanyPreferences.getLineLevelTermFlag() == 1 ? row.getRowtermamount(): rowTaxAmount);
        obj.put("taxamount", rowTaxAmount);
        obj.put("rowTaxPercent", rowTaxPercent);
        obj.put("prtaxid", row.getTax() == null ? "None" : row.getTax().getID());
        obj.put(Constants.isUserModifiedTaxAmount, row.isIsUserModifiedTaxAmount());
//        obj.put("prtaxid", row.getTax() != null ? (isCopy || isForDOGROLinking || isForLinking ? (row.getTax().isActivated() ? row.getTax().getID() : "") : row.getTax().getID()) : "None");//ERP-38656
         if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
            obj.put(Constants.prtaxidValue, row.getTax() == null ? "" : row.getTax().getName());
        }
        double amountWithoutTax=0.0;
         /*These keys are used while exporting details */
        double rowamountwithgst = 0;
        double discountValueForExcel = 0, amountForExcelFile = 0;
        if (row.getSalesOrder().isGstIncluded()) {//if gstincluded is the case
            rowamountwithgst = authHandler.round(row.getRateincludegst() * row.getQuantity(), companyid);
            discountValueForExcel = (row.getDiscountispercent() == 1) ? rowamountwithgst * row.getDiscount() / 100 : row.getDiscount();
            obj.put("discountvalue", (row.getDiscountispercent() == 1) ? (row.getRateincludegst() * row.getQuantity()) * row.getDiscount() / 100 : row.getDiscount());
            amountForExcelFile = rowamountwithgst - discountValueForExcel;
            amountWithoutTax = amountForExcelFile;
            obj.put(Constants.amountForExcelFile, authHandler.formattedAmount(amountForExcelFile, companyid));
        } else {
            discountValueForExcel = (row.getDiscountispercent() == 1) ? authHandler.round(((row.getRate() * row.getQuantity()) * row.getDiscount() / 100), companyid) : row.getDiscount();
            obj.put("discountvalue", (row.getDiscountispercent() == 1) ? (row.getRate() * row.getQuantity()) * row.getDiscount() / 100 : row.getDiscount());
            amountForExcelFile = (row.getRate() * row.getQuantity()) - discountValueForExcel;
            amountWithoutTax = amountForExcelFile;
            amountForExcelFile = amountForExcelFile + rowTaxAmount;
            obj.put(Constants.amountForExcelFile, authHandler.formattedAmount(amountForExcelFile, companyid));
        }
        
        obj.put("balanceamount", authHandler.formattedAmount(((amountForExcelFile/row.getQuantity())*row.getBalanceqty()), companyid)); // Balance Amount
        obj.put("amount", authHandler.formattedAmount(amountForExcelFile, companyid));

        if (isForLinking && isForInvoice && so.getContract() != null) {
            double unitPricePerInvoice = row.getRate();
            int numberOfPeriods = so.getContract().getNumberOfPeriods();
            if (numberOfPeriods > 0) {
                unitPricePerInvoice = row.getRate() / numberOfPeriods;
            }

            obj.put("rate", unitPricePerInvoice);
//            obj.put("purchaseprice", unitPricePerInvoice);
        } else {
            obj.put("rate", row.getRate());
//            obj.put("purchaseprice", row.getRate());
        }
        
        /**
         * below code will execute when amend price fuctionality is activated.
         */
        String uomidForamendprice = (row.getUom() != null) ? row.getUom().getID() : rowProduct.getUnitOfMeasure() != null ?  rowProduct.getUnitOfMeasure().getID() : "" ;
        accProductObj.getamendingPurchaseprice(rowProduct.getID(),paramJobj.optString("userid"),row.getSalesOrder().getOrderDate(),currency.getCurrencyID(), uomidForamendprice,obj);
        obj.put("priceSource", row.getPriceSource() != null ? row.getPriceSource() : "");
        obj.put("isAsset", rowProduct.isAsset());
         if (row.getPricingBandMasterid() != null) {
            KwlReturnObject PricebandResult = accountingHandlerDAOobj.getObject(PricingBandMaster.class.getName(), row.getPricingBandMasterid());
            PricingBandMaster pricingBandMaster = PricebandResult != null ? (PricingBandMaster) PricebandResult.getEntityList().get(0) : null;
            obj.put("pricingbandmasterid", pricingBandMaster != null ? pricingBandMaster.getID() : "");
            obj.put("pricingbandmastername", pricingBandMaster != null ? pricingBandMaster.getName() : "");
        }
        /**
         * get the volume discount discount for the given product according its
         * quantity.
         */
        HashMap<String, Object> pricingDiscountRequestParams = new HashMap<String, Object>();
        pricingDiscountRequestParams.put("isPricePolicyUseDiscount", true);
        pricingDiscountRequestParams.put("productID", row.getProduct()!=null?row.getProduct().getID():"");
        pricingDiscountRequestParams.put("isPurchase", false);
        pricingDiscountRequestParams.put("companyID", companyid);
        pricingDiscountRequestParams.put("currencyID", gcurrencyid);
        Double qty = Double.valueOf(row.getQuantity());
        pricingDiscountRequestParams.put("quantity", Integer.valueOf(qty.intValue()));
//        }
        // check use discount matches with entered qty
        KwlReturnObject volDiscresult = accProductObj.getProductPriceFromPriceListVolumeDiscount(pricingDiscountRequestParams);
        if (volDiscresult!=null && volDiscresult.getEntityList() != null && !volDiscresult.getEntityList().isEmpty()) {
            Object[] rowObj = (Object[]) volDiscresult.getEntityList().get(0);
            KwlReturnObject detailResult = accountingHandlerDAOobj.getObject(PricingBandMasterDetail.class.getName(), (String) rowObj[5]);
            PricingBandMasterDetail pricingBandMasterDetail = detailResult != null ? (PricingBandMasterDetail) detailResult.getEntityList().get(0) : null;
            if (pricingBandMasterDetail != null) {
                obj.put("volumdiscountid", pricingBandMasterDetail.getPricingBandMaster().getID());
            }
        }
        
        HashMap<String, Object> fieldrequestParamsProduct = new HashMap();
        Map<String, Object> variableMapProduct = new HashMap<String, Object>();
        fieldrequestParamsProduct.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
        fieldrequestParamsProduct.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
        HashMap<String, String> replaceFieldMapProduct = new HashMap<String, String>();
        HashMap<String, Integer> FieldMapProduct = accAccountDAOobj.getFieldParamsMap(fieldrequestParamsProduct, replaceFieldMapProduct);

        KwlReturnObject resultProduct = accountingHandlerDAOobj.getObject(SalesOrderDetailProductCustomData.class.getName(), row.getID());
        SalesOrderDetailProductCustomData objProduct = (SalesOrderDetailProductCustomData) resultProduct.getEntityList().get(0);

        if (objProduct != null) {
            JSONObject params = new JSONObject();
            params.put(Constants.isExport, isExport);
            params.put(Constants.userdf, userDateFormat);
            
            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                params.put("isForReport", true);
            } else {
                params.put("isForReport", isForReport);
            }

            setCustomColumnValuesForProduct(objProduct, FieldMapProduct, replaceFieldMapProduct, variableMapProduct, params);
              for (Map.Entry<String, Object> varEntry : variableMapProduct.entrySet()) {                
                String coldata = varEntry.getValue().toString();
                if (!StringUtil.isNullOrEmpty(coldata)) {                    
                    obj.put(varEntry.getKey(), coldata);
                    obj.put("key", varEntry.getKey());
                     if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {                                                
                            obj.put(varEntry.getKey() + "Value", coldata);                        
                    }
                }
            }
            }
        obj.put(Constants.unitpriceForExcelFile, row.getRate());//obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
        double baseuomrate = row.getBaseuomrate();
        if (row.getUom() != null) {
            obj.put("uomid", row.getUom().getID());
        } else {
            obj.put("uomid", rowProduct.getUnitOfMeasure() != null ? rowProduct.getUnitOfMeasure().getID() : "");
        }
        double quantity = 0;
        double invoiceRowProductQty = row.getQuantity() * baseuomrate;
        double remainedQty = invoiceRowProductQty;// which has not been linked yet
        /*
         * In case of cross linking i.e SO->PO else part of the code will get execute
         */
        boolean isPartialInv = false;
        double remainingPartAmt = 0.0;
        double sumOfAllPrevPartialAmt = 0.0;
        if (closeflag != null && (doflag || isForInvoice)) {
            double addobj = 0.0;
            JSONObject statusJobj = new JSONObject();
            if (doflag) {//SO->DO linking
                addobj = row.getBalanceqty();
            } else if (isForInvoice) {//SO->Invoice linking
                statusJobj = getSalesOrderDetailStatusforPartialInv(row);
                addobj = statusJobj.getDouble("result");
                if (statusJobj.getDouble("amount") != 0) {
                   
                    /*originalamount-> Remaining Amount Of SO that is still unused*/
                    double originalamount = Double.parseDouble(authHandler.formattedAmount(amountForExcelFile, companyid)) - (statusJobj.getDouble("amount"));
                    
                    /* Remaining Part amount of SO unused */ 
                    remainingPartAmt = statusJobj.getDouble("remainingPartialAmount");
                    sumOfAllPrevPartialAmt = statusJobj.getDouble("sumOfAllPrevPartialAmt");
                    
                    /* Calculating Tax on remaining amount of SO(unused)*/
                    rowTaxAmount = (amountWithoutTax * (remainingPartAmt / 100)) * (rowTaxPercent / 100);
                    
                    obj.put("amount", originalamount);
                    obj.put(Constants.amountForExcelFile, originalamount);
                    obj.put("rowTaxAmount", rowTaxAmount);
                    obj.put("taxamount", rowTaxAmount);
                    isPartialInv = true;
                }
                obj.put("isPartialInv", isPartialInv);
                obj.put("remainingPartAmt", authHandler.round(statusJobj.getDouble("remainingPartialAmount"), companyid));
                obj.put("sumOfAllPrevPartialAmt", authHandler.round(statusJobj.getDouble("sumOfAllPrevPartialAmt"), companyid));
            }
                    
            if (isConsignment && so.isAutoapproveflag() && !isForReport && rowProduct != null) {
                addobj = addobj - ((rejectedCount + pendingCount) / baseuomrate);
            }
            quantity = addobj;
            obj.put("quantity", addobj);
            obj.put("copyquantity", addobj);
            obj.put("availablequantity", rowProduct != null ? rowProduct.getAvailableQuantity() : 0); // Required this value while linking case to check available qty
            //as we are putting those quantity which is va
            if (isConsignment && (addobj > prodavlqty) && prodavlqty != 0) {
                obj.put("dquantity", prodavlqty < 0 ? row.getBaseuomquantity() : prodavlqty);
            } else {
                obj.put("dquantity", addobj);
            }
            obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(addobj, baseuomrate, companyid));
            obj.put("baseuomrate", baseuomrate);
            remainedQty = authHandler.calculateBaseUOMQuatity(addobj, baseuomrate, companyid);
        } else {
            quantity = row.getQuantity();
            obj.put("quantity", quantity);
            obj.put("copyquantity", quantity);
            obj.put("dquantity", quantity);
            obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, baseuomrate, companyid));
            obj.put("baseuomrate", baseuomrate);
        }
        /* Need Client side to calculate Partial Amount*/
//        obj.put("isPartialInv", isPartialInv);
//        obj.put("remainingPartAmt", authHandler.round(remainingPartAmt, companyid));
//        obj.put("sumOfAllPrevPartialAmt", authHandler.round(sumOfAllPrevPartialAmt, companyid));
        
         /*This function is use to calculate sum of all last partial line level invoice discont.For Flat and Percentage discount type*/
        if (isPartialInv) {
            KwlReturnObject partialInvoiceResult = accSalesOrderDAOobj.getLineLevelDiscountSumOfPartialInvoice(row.getID(), companyid);
            double sumOfAllLastInvDiscount = 0.0;
            for (Iterator it = partialInvoiceResult.getEntityList().iterator(); it.hasNext();) {
                Object[] object = (Object[]) it.next();
                /*
                 * [0] : Partial Amount
                 * [1] : Discount Value (In Flat or Percentage)
                 * [2] : Discount Type( 1 = Percenatage and 0 = Flat)
                 */
                double partAmount = (double) object[0];
                double lastDiscount = (double) object[1];
                int discoutType = (int) object[2];
                if (discoutType == 1) {
                    sumOfAllLastInvDiscount += authHandler.round((((row.getRate() * row.getQuantity()) * lastDiscount) / 100) * partAmount / 100, companyid);
                } else {
                    sumOfAllLastInvDiscount += authHandler.round(lastDiscount * (partAmount / 100), companyid);
                }
            }
            obj.put("sumOfAllLastInvDiscount", sumOfAllLastInvDiscount);
        }

        if (isForLinking || isForDOGROLinking) {// in case of linking in normal transactions not lease consignment etc.
            if (row.getTax() != null && invoiceRowProductQty > 0) {
                double taxAmt = (rowTaxAmount / invoiceRowProductQty) * remainedQty;
                obj.put("rowTaxAmount", isPartialInv ? rowTaxAmount : taxAmt);
                obj.put("taxamount", isPartialInv ? rowTaxAmount : taxAmt);
            }
        }
        
        obj.put("rateIncludingGst", authHandler.roundUnitPrice(row.getRateincludegst(), companyid));
        obj.put("israteIncludingGst", so.isGstIncluded());
        //ERP-41214:Show asterisk to unit price and amount 
        //Handled for mobile Apps
        if (paramJobj.optBoolean(Constants.isdefaultHeaderMap) && paramJobj.has(Constants.displayUnitPriceAndAmountInSalesDocument) && !paramJobj.optBoolean(Constants.displayUnitPriceAndAmountInSalesDocument)) {
            obj.put("amount", CustomDesignerConstants.UNIT_PRICE_AND_AMOUNT_AS_STARS);
            obj.put("rate", CustomDesignerConstants.UNIT_PRICE_AND_AMOUNT_AS_STARS);
            obj.put("rateIncludingGst", CustomDesignerConstants.UNIT_PRICE_AND_AMOUNT_AS_STARS);
        }
        if (row.getUom() != null) {
            obj.put("uomid", row.getUom().getID());
            obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, row.getBaseuomrate(), companyid));
            obj.put("baseuomrate", row.getBaseuomrate());
        } else {
            obj.put("uomid", rowProduct.getUnitOfMeasure() != null ? rowProduct.getUnitOfMeasure().getID() : "");
            obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, row.getBaseuomrate(), companyid));
            obj.put("baseuomrate", row.getBaseuomrate());
        }
        /**
         * Send uomschematypeid for Android 
         */
        if (rowProduct.getUomSchemaType() != null) {
            obj.put("uomschematypeid", rowProduct.getUomSchemaType().getID());
        }
        JSONObject jObj = new JSONObject();
        if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
            jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
        }
        if (jObj.has("isDisplayUOM") && jObj.get("isDisplayUOM") != null && (Boolean) jObj.get("isDisplayUOM") != false) {
            obj = accProductObj.getProductDisplayUOM(row.getProduct(), quantity, baseuomrate, false, obj);
        }        
        obj.put("balanceQuantity", row.getBalanceqty());
        
       /*
        * Commented below check to export the shortfallQuantity in Excel
        */
//        if (!isExport) {
            double shortfallQuantity = 0;
            HashMap balanceQuantityParams = new HashMap();

            balanceQuantityParams.put("companyObj", company);
            balanceQuantityParams.put("productObj", row.getProduct());
            balanceQuantityParams.put("companyid", companyid);
            balanceQuantityParams.put("extraCompanyPreferences", extraCompanyPreferences);
            balanceQuantityParams.put("soBalanceQuantity", row.getBalanceqty());

            /* ---Get shortfall Quantity of Product------- */
            shortfallQuantity = getShortfallQuantity(balanceQuantityParams);

            obj.put("shortfallQuantity", shortfallQuantity);
//        }
      

        /*
        * Provided try catch block as id is present in bomcode but its data is not present in BomDetail Table.
        */
        try {
            obj.put("bomcode", row.getBomcode() != null ? row.getBomcode().getBomCode(): "");
            obj.put("bomid", row.getBomcode() != null ? row.getBomcode().getID(): "");
        } catch (Exception ex) {
            obj.put("bomcode",  "");
            obj.put("bomid",  "");
        }
        
        HashMap<String, Object> unitPriceParams = new HashMap<>();
        unitPriceParams.put("soDetailID", row.getID());
        if (!StringUtil.isNullOrEmpty(row.getPurchaseorderdetailid())) {
            unitPriceParams.put("isPOLinked", true);
        }
        KwlReturnObject unitPriceResult = accSalesOrderDAOobj.getMarginCostForCrossLinkedTransactions(unitPriceParams);
        if (!unitPriceResult.getEntityList().isEmpty() && unitPriceResult.getEntityList().get(0) != null) {
            Object[] objArr = (Object[]) unitPriceResult.getEntityList().get(0);
            obj.put("marginCost", objArr[0]);
            obj.put("marginExchangeRate", objArr[1]);
        }
        
        Map<String, Object> variableMap = new HashMap<String, Object>();
        SalesOrderDetailsCustomData jeDetailCustom = (SalesOrderDetailsCustomData) row.getSoDetailCustomData();
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
        
        if (jeDetailCustom != null) {
            JSONObject params = new JSONObject();
            params.put(Constants.isExport, isExport);
            params.put(Constants.userdf,userdef);
            params.put("isForReport", isForReport);
            params.put(Constants.isdefaultHeaderMap,paramJobj.optBoolean(Constants.isdefaultHeaderMap, false));
            boolean getlineItemDetailsflag = (paramJobj.optString("getlineItemDetailsflag", null) != null) ? Boolean.FALSE.parseBoolean((String) paramJobj.get("getlineItemDetailsflag")) : false;
            /*--- generateInvoiceFromTransactionForms ->Flag is true if invoice is generated from SO form------*/
            boolean generateInvoiceFromTransactionForms = !StringUtil.isNullOrEmpty(paramJobj.optString("generateInvoiceFromTransactionForms","")) ? Boolean.parseBoolean(paramJobj.optString("generateInvoiceFromTransactionForms","")) : false;
            /*
             'isPOfromSO' flag is used to get custom field or dimension while generating PO from SO
            */
            boolean isPOfromSO = paramJobj.optBoolean("isPOfromSO",false);
            if ((isForDOGROLinking || isForLinking || isFromContract || getlineItemDetailsflag) && !isForReport || generateInvoiceFromTransactionForms || isPOfromSO) {
                int moduleId = isForDOGROLinking ? Constants.Acc_Delivery_Order_ModuleId : (isForInvoice || generateInvoiceFromTransactionForms) ? Constants.Acc_Invoice_ModuleId : Constants.Acc_Purchase_Order_ModuleId;
                if (isLeaseFixedAsset) {
                    moduleId = Constants.Acc_Lease_DO;
                } else if (isConsignment) {
                    moduleId = Constants.Acc_ConsignmentDeliveryOrder_ModuleId;
                } else if(isFromContract){
                    moduleId = Constants.Acc_Contract_Order_ModuleId;
                }else if(isPOfromSO){
                    moduleId = Constants.Acc_Purchase_Order_ModuleId;
                }
                params.put("linkModuleId", moduleId);
                params.put("isLink", true);
                params.put("companyid", companyid);
                params.put(Constants.userdf,userDateFormat);
            }
            if (!getlineItemDetailsflag) {
                fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
            } else {
                fieldDataManagercntrl.getLineLevelCustomDataWithKey(variableMap, customFieldMap, customDateFieldMap, obj, params);
            }
        }
        obj.put("discountjson", row.getDiscountJson() != null ? row.getDiscountJson() : "");                //getting json of multiple discount applied on product ERM-68
        return obj;
    }
    
    /*--------Function is used to get shortfall Quantity----------  */
    public double getShortfallQuantity(HashMap balanceQuantityParams) throws ServiceException {

        double shortfallQuantity = 0;
        try {

            double soBalanceQuantity = (double) balanceQuantityParams.get("soBalanceQuantity");

            List inventoryQuantitiesList = AccProductService.getListOfInventoryQuantitiesOfProduct(balanceQuantityParams);

            double productBalanceQuantityInInventory = 0;

            /*------Code to Calculate "shortfallQuantity" ---------*/
            if (inventoryQuantitiesList != null && inventoryQuantitiesList.size() > 0) {
                productBalanceQuantityInInventory = (double) inventoryQuantitiesList.get(0);
                if ((soBalanceQuantity - productBalanceQuantityInInventory) > 0) {
                    shortfallQuantity = soBalanceQuantity - productBalanceQuantityInInventory;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return shortfallQuantity;
    }
    
    @Override
   public double getSalesOrderBalanceQuantity(SalesOrderDetail salesOrderDetail){
       double result = 0;
        try{
       
        
        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), salesOrderDetail.getSalesOrder().getCompany().getCompanyID());
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
                    
            boolean fullInv = false;
            double qua = 0;
            double quantPartTt = salesOrderDetail.getQuantity() * 100 ;
            double quantPartTtInv = 0.0 ;
            if(pref.isWithInvUpdate()){ //In Trading Flow                 
                KwlReturnObject doresult = accInvoiceDAOobj.getDeliveryOrderDFromSOD(salesOrderDetail.getID(),pref.getCompany().getCompanyID());
                List list = doresult.getEntityList();
                if(list.size()>0){
                    Iterator ite1 = list.iterator();                
                    while(ite1.hasNext()){                        
                        String orderid = (String)ite1.next();
                        KwlReturnObject res=accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(),orderid);
                        DeliveryOrderDetail deliveryOrderDetail=(DeliveryOrderDetail)res.getEntityList().get(0);                        
                        fullInv = true;
                        qua += deliveryOrderDetail.getDeliveredQuantity();
                    }
                }                
            }else{ //In Non Trading Flow 
            
                KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSOD(salesOrderDetail.getID());
                List list = idresult.getEntityList();
                Iterator ite1 = list.iterator();            
                while(ite1.hasNext()){
                    InvoiceDetail ge = (InvoiceDetail) ite1.next();
                    if(ge.getInvoice().isPartialinv()) {
//                        double quantity = ge.getInventory().isInvrecord()? ge.getInventory().getQuantity() : ge.getInventory().getActquantity() ;
                        double quantity = ge.getInventory().getQuantity();
                        quantPartTtInv += quantity * ge.getPartamount();
                    } else {
                        fullInv = true;
//                        qua += ge.getInventory().isInvrecord()? ge.getInventory().getQuantity() : ge.getInventory().getActquantity() ;
                        qua += ge.getInventory().getQuantity();
                    }
                }
            }
            if(fullInv){              
                    result = qua;
  
            } else if(quantPartTt > quantPartTtInv){
                result = quantPartTtInv;
               
            }
        
        } catch (Exception ex){
           
        }
        return result;
    }
    
    public String getdefaultBatchJson(Product product, String documentid, double quantity, SalesOrder so, double prodavlqty, boolean doflag) throws JSONException, ServiceException {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        String movmenttype = "";
        if (so.isIsconsignment()) {
            movmenttype = so.getMovementType() != null ? so.getMovementType().getID() : "";
        }
        if (!StringUtil.isNullOrEmpty(movmenttype) && so.getRequestLocation() != null) {
            jobj.put("location", so.getRequestLocation().getId());
        } else if (product.getLocation() != null && !StringUtil.isNullOrEmpty(product.getLocation().getId())) {
            jobj.put("location", product.getLocation().getId());
        }
        if (!StringUtil.isNullOrEmpty(movmenttype) && so.getRequestWarehouse() != null) {
            jobj.put("warehouse", so.getRequestWarehouse().getId());
        } else if (product.getWarehouse() != null && !StringUtil.isNullOrEmpty(product.getWarehouse().getId())) {
            jobj.put("warehouse", product.getWarehouse().getId());
        }
        jobj.put("documentid", "");
        if (!StringUtil.isNullOrEmpty(product.getID())) {
            jobj.put("productid", product.getID());
        }
        double doQty = 0;
        if (doflag) {
            doQty = accInvoiceDAOobj.getDeliveryOrderQuantityFromSOD(documentid, so.getCompany().getCompanyID(), false);
            quantity = quantity - doQty;
        }
        if (so.isIsconsignment() && (quantity > prodavlqty) && prodavlqty != 0) {
            jobj.put("quantity", prodavlqty);
        } else {
            jobj.put("quantity", quantity);
        }

        jobj.put("purchasebatchid", "");
        jarr.put(jobj);
        return jarr.toString();
    }
    @Override
     public String getNewBatchJson(Product product,JSONObject paramJobj, String documentid) throws ServiceException, SessionExpiredException, JSONException {
        JSONArray jSONArray = new JSONArray();
        DateFormat df = authHandler.getDateFormatter(paramJobj);
        KwlReturnObject kmsg = null;
        boolean linkingFlag = (StringUtil.isNullOrEmpty(paramJobj.optString("linkingFlag",null))) ? false : Boolean.parseBoolean((String)paramJobj.get("linkingFlag"));
        boolean isForLinking =paramJobj.optBoolean("isForLinking",false);
        boolean isEdit=(StringUtil.isNullOrEmpty(paramJobj.optString("isEdit",null)))?false:Boolean.parseBoolean((String)paramJobj.get("isEdit"));
        boolean isConsignment=(StringUtil.isNullOrEmpty(paramJobj.optString("isConsignment",null)))?false:Boolean.parseBoolean((String)paramJobj.get("isConsignment"));
        String moduleID = paramJobj.optString("moduleid",null);
          boolean isBatch=false;
        if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && !product.isIsrowforproduct() && !product.isIsrackforproduct() && !product.isIsbinforproduct() && product.isIsSerialForProduct()) {
            kmsg = accCommonTablesDAO.getOnlySerialDetails(documentid, linkingFlag, moduleID,isConsignment,isEdit);
        } else {
              isBatch=true;
            kmsg = accCommonTablesDAO.getBatchSerialDetails(documentid, !product.isIsSerialForProduct(), linkingFlag, moduleID,isConsignment,isEdit,"");
        }
        
        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList((String)paramJobj.get(Constants.companyKey), Constants.SerialWindow_ModuleId, 1));
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
        // HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
        HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
//        product.getName()
         double ActbatchQty = 1;
        double approvedSerialQty = 0;
        double batchQty = 0;
        List batchserialdetails = kmsg.getEntityList();
        Iterator iter = batchserialdetails.iterator();
        while (iter.hasNext()) {
            Object[] objArr = (Object[]) iter.next();
            JSONObject obj = new JSONObject();
            obj.put("id", objArr[0] != null ? (String) objArr[0] : "");
            obj.put("location", objArr[2] != null ? (String) objArr[2] : "");
            obj.put("warehouse", objArr[3] != null ? (String) objArr[3] : "");
            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                obj.put("purchasebatchidValue", objArr[1] != null ? (String) objArr[1] : "");
            } else {
                obj.put("batch", objArr[1] != null ? (String) objArr[1] : "");
                obj.put("batchname", objArr[1] != null ? (String) objArr[1] : "");
            }
           if (isBatch){
                obj.put("row", objArr[15] != null ? (String) objArr[15] : "");
                obj.put("rack", objArr[16] != null ? (String) objArr[16] : "");
                obj.put("bin", objArr[17] != null ? (String) objArr[17] : "");
         }
            if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct()) && product.isIsSerialForProduct()) {
                if (isConsignment && linkingFlag) {
                      ActbatchQty = accCommonTablesDAO.getApprovedSerialQty(documentid, (String) objArr[0],isEdit);
                } else {
                     ActbatchQty = accCommonTablesDAO.getBatchQuantity(documentid, (String) objArr[0]);
                }

                if (batchQty == 0) {
                    batchQty =  ActbatchQty;
                }
                if (batchQty == ActbatchQty) {
                    obj.put("isreadyonly", false);
                    obj.put("quantity", ActbatchQty);
                } else {
                    obj.put("isreadyonly", true);
                    obj.put("quantity", "");
                }

            } else {
                obj.put("isreadyonly", false);
                obj.put("quantity", ActbatchQty);
            }
            if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && !product.isIsrowforproduct() && !product.isIsrackforproduct() && !product.isIsbinforproduct() && product.isIsSerialForProduct()) {
                obj.put("mfgdate", "");
                obj.put("expdate", "");
            } else {
                obj.put("mfgdate", objArr[4] != null ? df.format(objArr[4]) : "");
                obj.put("expdate", objArr[5] != null ? df.format(objArr[5]) : "");
            }

//            obj.put("quantity", objArr[6] != null ? objArr[6] : "");
//            obj.put("quantity",ActbatchQty);
            obj.put("lockquantity", objArr[12] != null ? objArr[12] : "");
            
            if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct()) && !product.isIsSerialForProduct()) {
                obj.put("quantity", objArr[11] != null ? objArr[11] : "");
            }

            if (!StringUtil.isNullOrEmpty(product.getID())) {
                obj.put("productid", product.getID());
            }
            obj.put("balance", 0);
            obj.put("asset", "");
            obj.put("serialnoid", objArr[7] != null ? (String) objArr[7] : "");
            if (paramJobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                 obj.put("purchaseserialidValue", objArr[8] != null ? (String) objArr[8] : "");
            } else {
                obj.put("serialno", objArr[8] != null ? (String) objArr[8] : "");
            }
            
            obj.put("skufield", objArr[13] != null ? (String) objArr[13] : "");
            obj.put("purchasebatchid", objArr[0] != null ? (String) objArr[0] : "");
            obj.put("purchaseserialid", objArr[7] != null ? (String) objArr[7] : "");
            obj.put("expstart", (objArr[9] != null && !objArr[9].toString().equalsIgnoreCase("")) ? df.format(objArr[9]) : "");
            obj.put("expend", (objArr[10] != null && !objArr[10].toString().equalsIgnoreCase(""))  ? df.format(objArr[10]) : "");
            obj.put("documentid", documentid != null ? documentid : "");
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("invoiceID", objArr[0]);
            hashMap.put(Constants.companyKey, product.getCompany().getCompanyID());
            /**
             * Get document count attached to batch
             */
            KwlReturnObject object = accMasterItemsDAOobj.getBatchDocuments(hashMap);
            if (object.getEntityList() != null) {
                obj.put("attachment", object.getEntityList().size());
            }
            if((linkingFlag || isForLinking )  && !isEdit ){ //For geting only unused Serial batch details in DO
                if(product.isIsSerialForProduct() && objArr[7] != null ){
                    KwlReturnObject result1 = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), objArr[7].toString());
                    NewBatchSerial newBatchSerial = (NewBatchSerial) result1.getEntityList().get(0);
                    if(newBatchSerial != null && newBatchSerial.getQuantitydue()==0){
    //                    batchQty--;
                        continue;
                    }
                }else if(product.isIsBatchForProduct() && ! product.isIsSerialForProduct() && objArr[0] != null){
                    KwlReturnObject result1 = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), objArr[0].toString());
                    NewProductBatch newProductBatch = (NewProductBatch) result1.getEntityList().get(0);
                    if(newProductBatch !=null && newProductBatch.getQuantitydue()==0){
                        continue;
                    }
                }
            }
            if (objArr[14] != null && !objArr[14].toString().equalsIgnoreCase("")) {
                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(SerialDocumentMapping.class.getName(), objArr[14].toString());
                SerialDocumentMapping sdm = (SerialDocumentMapping) result1.getEntityList().get(0);
                Map<String, Object> variableMap = new HashMap<String, Object>();
                SerialCustomData serialCustomData = (SerialCustomData) sdm.getSerialCustomData();
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                AccountingManager.setCustomColumnValues(serialCustomData, fieldMap, replaceFieldMap, variableMap);
                DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateFromDB=null;
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
                                      valueForReport += value + ",";
                                }
                            }
                            if (valueForReport.length() > 1) {
                                valueForReport = valueForReport.substring(0, valueForReport.length() - 1);
                            }
                            obj.put(varEntry.getKey(), valueForReport);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                        } catch (Exception ex) {
                            obj.put(varEntry.getKey(), coldata);
                        }
                    } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            dateFromDB = defaultDateFormat.parse(coldata);
                            coldata = df2.format(dateFromDB);

                        } catch (Exception e) {
                        }

                        obj.put(varEntry.getKey(), coldata);
                    } else {
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            obj.put(varEntry.getKey(), coldata);
                        }
                    }
                }
            }
            String serialNoId = objArr[7] != null ? (String) objArr[7] : "";
            int transType = Constants.Acc_ConsignmentSalesReturn_ModuleId;
            String docId="";
           KwlReturnObject reusablecountobj = accCommonTablesDAO.getSerialsReusableCount(product.getID(), objArr[8] != null ? (String) objArr[8] : "",product.getCompany().getCompanyID(),transType, false, docId, objArr[0] != null ? (String) objArr[0] :"");
            if (reusablecountobj.getEntityList() != null && !reusablecountobj.getEntityList().isEmpty()) {
                if (reusablecountobj.getEntityList().get(0) != null) {
                    double sumCount = Double.parseDouble(reusablecountobj.getEntityList().get(0).toString());
                    obj.put("reusablecount",  sumCount);
                } else {
                    obj.put("reusablecount", 0);
                }
            } else {
                obj.put("reusablecount", 0);
            }
            jSONArray.put(obj);
             batchQty--;

        }


        return jSONArray.toString();
    }
 
        private void getAssetDetailJsonObject(JSONObject paramJobj, SalesOrderDetail row, JSONObject obj) throws ServiceException, JSONException, SessionExpiredException {
        String companyid =paramJobj.getString(Constants.companyKey);
        DateFormat df = authHandler.getDateFormatter(paramJobj);

        JSONArray assetDetailsJArr = new JSONArray();
        HashMap<String, Object> assetDetailsParams = new HashMap<String, Object>();
        assetDetailsParams.put("companyid", companyid);
        assetDetailsParams.put("invoiceDetailId", row.getID());
        assetDetailsParams.put("moduleId", Constants.Acc_Sales_Order_ModuleId);
     
        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(),companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        boolean isFixedAssetDO=true;
        KwlReturnObject assetInvMapObj = accProductObj.getAssetInvoiceDetailMapping(assetDetailsParams);
        List assetInvMapList = assetInvMapObj.getEntityList();
        Iterator assetInvMapListIt = assetInvMapList.iterator();

        while (assetInvMapListIt.hasNext()) {
            AssetInvoiceDetailMapping invoiceDetailMapping = (AssetInvoiceDetailMapping) assetInvMapListIt.next();
            AssetDetails assetDetails = invoiceDetailMapping.getAssetDetails();
            JSONObject assetDetailsJOBJ = new JSONObject();

            assetDetailsJOBJ.put("assetId", assetDetails.getId());

            assetDetailsJOBJ.put("assetdetailId", assetDetails.getId());
            assetDetailsJOBJ.put("sellAmount", assetDetails.getSellAmount());
            assetDetailsJOBJ.put("assetName", assetDetails.getAssetId());
            assetDetailsJOBJ.put("location", (assetDetails.getLocation() != null) ? assetDetails.getLocation().getId() : "");
            assetDetailsJOBJ.put("department", (assetDetails.getDepartment() != null) ? assetDetails.getDepartment().getId() : "");
            assetDetailsJOBJ.put("assetdescription", (assetDetails.getAssetDescription() != null) ? assetDetails.getAssetDescription() : "");
            assetDetailsJOBJ.put("assetUser", (assetDetails.getAssetUser() != null) ? assetDetails.getAssetUser().getUserID() : "");
            assetDetailsJOBJ.put("cost", assetDetails.getCost());
            assetDetailsJOBJ.put("salvageRate", assetDetails.getSalvageRate());
            assetDetailsJOBJ.put("salvageValue", assetDetails.getSalvageValue());
            assetDetailsJOBJ.put("accumulatedDepreciation", assetDetails.getAccumulatedDepreciation());
//            assetDetailsJOBJ.put("wdv", assetDetails.getWdv());
            assetDetailsJOBJ.put("assetLife", assetDetails.getAssetLife());
            assetDetailsJOBJ.put("elapsedLife", assetDetails.getElapsedLife());
            assetDetailsJOBJ.put("nominalValue", assetDetails.getElapsedLife());
            assetDetailsJOBJ.put("installationDate", df.format(assetDetails.getInstallationDate()));
            assetDetailsJOBJ.put("purchaseDate", df.format(assetDetails.getPurchaseDate()));
//           if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
//                KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), row.getProduct().getID());
//                Product product = (Product) prodresult.getEntityList().get(0);
//                isBatchForProduct = product.isIsBatchForProduct();
//                isSerialForProduct = product.isIsSerialForProduct();
//            }
//            if (pref.isIsBatchCompulsory() || pref.isIsSerialCompulsory()) {  //check if company level option is on then only we will check productt level
//                if (isBatchForProduct || isSerialForProduct) {
//                    assetDetailsJOBJ.put("batchdetails", (assetDetails.getBatch() == null) ? "" : getBatchJson(assetDetails.getBatch(), isFixedAssetDO, pref.isIsBatchCompulsory(), isBatchForProduct, pref.isIsSerialCompulsory(), isSerialForProduct, request));
//                }
                
            if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
                KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), row.getProduct().getID());
                Product product = (Product) prodresult.getEntityList().get(0);
                isBatchForProduct = product.isIsBatchForProduct();
                isSerialForProduct = product.isIsSerialForProduct();
                isLocationForProduct = product.isIslocationforproduct();
                isWarehouseForProduct = product.isIswarehouseforproduct();
                isRowForProduct=product.isIsrowforproduct();
                isRackForProduct=product.isIsrackforproduct();
                isBinForProduct=product.isIsbinforproduct();
            }
            if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory()  || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {  //check if company level option is on then only we will check productt level
                if (isBatchForProduct || isSerialForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct  || isRowForProduct || isRackForProduct || isBinForProduct) {  //product level batch and serial no on or not
                    assetDetailsJOBJ.put("batchdetails", getNewBatchJson(row.getProduct(), paramJobj, assetDetails.getId()));
                }
            }
            assetDetailsJArr.put(assetDetailsJOBJ);
        }
        obj.put("assetDetails", assetDetailsJArr.toString());
    }
    @Override
   public double getSalesOrderDetailStatusForDO(SalesOrderDetail sod) throws ServiceException {
        double result = sod.getQuantity();
        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sod.getCompany().getCompanyID());
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
        KwlReturnObject idresult = accInvoiceDAOobj.getDOIDFromSOD(sod.getID(),"");
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0;
        while (ite1.hasNext()) {
            DeliveryOrderDetail ge = (DeliveryOrderDetail) ite1.next();
            qua += ge.getInventory().getQuantity();
        }
        result = sod.getQuantity() - qua;
        return result;
    }
 
    @Override
     public double getSalesOrderDetailStatus(SalesOrderDetail sod) throws ServiceException {
        double result = sod.getQuantity();
        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sod.getCompany().getCompanyID());
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
        KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSOD(sod.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0.0;
        boolean fullInv = false;
        double quantPartTtInv = 0.0 ;
        while (ite1.hasNext()) {
            InvoiceDetail ge = (InvoiceDetail) ite1.next();
            if(ge.getInvoice().isPartialinv()){
//                Need to test properly.
//                double quantity = ge.getInventory().isInvrecord() ? ge.getInventory().getQuantity() : ge.getInventory().getActquantity();
                double quantity = ge.getInventory().getQuantity();
                quantPartTtInv +=  quantity * ge.getPartamount();
            } else {
                fullInv = true;
//                qua += ge.getInventory().isInvrecord() ? ge.getInventory().getQuantity() : ge.getInventory().getActquantity();
                qua += ge.getInventory().getQuantity();
            }    
        }
        
        if(fullInv) {
            result = sod.getQuantity() - qua;
        } else {
            if(sod.getQuantity() * 100 > quantPartTtInv){
                result = sod.getQuantity() - qua;
            } else {
                result = 0;
            }
        }
        
        return result;
    }
     
    /*  Function to check satus of line level Product of SO while linking with Invoice 
     
     *1.While Linking with Invoice Fully
     *2.While linking with Invoice Partially
     
     */    
    public JSONObject getSalesOrderDetailStatusforPartialInv(SalesOrderDetail sod) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {

            double result = sod.getQuantity();
            double amount = 0.0;
            double remainingPartialAmount=0.0;
            double sumOfAllPrevPartialAmt=0.0;

            KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSOD(sod.getID());
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            double qua = 0.0;
            boolean fullInv = false;
            double quantPartTtInv = 0.0;
            while (ite1.hasNext()) {
                InvoiceDetail ge = (InvoiceDetail) ite1.next();
               /* Checking If any SO is partially linked with Invoice */
                if (ge.getInvoice().isPartialinv()) {
                    double quantity = ge.getInventory().getQuantity();
                    quantPartTtInv += quantity * ge.getPartamount();
                    amount += (quantity * (ge.getRate())) * (ge.getPartamount() / 100);
                    if (ge.getDiscount() != null) {
                        amount = amount - (ge.getDiscount().getDiscountValue());
                    }  
                    amount = amount + ((ge.getRowTaxAmount()+ge.getRowTermTaxAmount()));
                    
                    /*Partial amount of Product of SO used in Invoice */

                    remainingPartialAmount += ge.getPartamount();
                } else {
                    fullInv = true;

                    qua += ge.getInventory().getQuantity();
                }

            }

            if (fullInv) {//Not partial case
                result = sod.getQuantity() - qua;
            } else {
                if (sod.getQuantity() * 100 > quantPartTtInv) {
                    result = sod.getQuantity() - qua;
                } else {
                    result = 0;
                }
            }
         
            /*remaining amount of product of SO which can be linked with Invoice again */
            sumOfAllPrevPartialAmt = remainingPartialAmount;
            remainingPartialAmount = (100 - remainingPartialAmount);
            jobj.put("remainingPartialAmount", remainingPartialAmount);
            jobj.put("sumOfAllPrevPartialAmt", sumOfAllPrevPartialAmt);
            jobj.put("amount", amount);
            jobj.put("result", result);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("AccSalesOrderServiceImpl.getSalesOrderDetailStatusforPartialInv : " + ex.getMessage(), ex);
        }

        return jobj;
    }
     
        private void setCustomColumnValuesForProduct(SalesOrderDetailProductCustomData soDetailsProductCustomData, HashMap<String, Integer> fieldMap, Map<String, String> replaceFieldMap,
            Map<String, Object> variableMap, JSONObject params) {
        for (Map.Entry<String, Integer> field : fieldMap.entrySet()) {
            boolean isForReport = params.optBoolean("isForReport", false);
            boolean isExport = params.optBoolean(Constants.isExport, false);
            Integer colnumber = field.getValue();
            if (colnumber > 0) { // colnumber will be 0 if key is part of reference map
                Integer isref = fieldMap.get(field.getKey() + "#" + colnumber);// added '#' while creating map collection for custom fields.
                // Without this change, it creates problem if two custom columns having name like XYZ and XYZ1
                String coldata = null;
                if (isref != null) {
                    try {
                        if (soDetailsProductCustomData != null) {
                            coldata = soDetailsProductCustomData.getCol(colnumber);
                        }
//                        if (StringUtil.isNullOrEmpty(coldata)) {
//                            coldata = customData.getCol(colnumber);
//                        }
//                        String coldataVal = null;
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            if (coldata.length() > 1) {
                                if (isref == 1) {
                                } else if (isref == 0 || isref == 7) {
                                    if (isForReport) {
                                        String valueForReport = "";
                                        String[] valueData = coldata.split(",");
                                        for (String value : valueData) {
                                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                                            FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                            if (fieldComboData != null) {
                                                valueForReport += fieldComboData.getValue() + ",";
                                            }
                                        }
                                        if (valueForReport.length() > 1) {
                                            coldata = valueForReport.substring(0, valueForReport.length() - 1);
                                        }
                                    } else {
                                        coldata = coldata;
                                    }
                                } else if (isref == 3 && isExport) {
                                    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                                    DateFormat defaultDateFormat = new SimpleDateFormat(Constants.MMMMdyyyy);
                                    DateFormat userDateFormat = params.has(Constants.userdf) ? (DateFormat) params.get(Constants.userdf) : null;
                                    Date dateFromDB = null;
                                    try {
                                        dateFromDB = defaultDateFormat.parse(coldata);
                                        coldata = userDateFormat != null ? userDateFormat.format(dateFromDB) : df2.format(dateFromDB);
                                    } catch (Exception e) {
                                    }
                                }
                            }
                            variableMap.put(field.getKey(), coldata);
                        }
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    public JSONArray getSalesOrderJsonForLinking(JSONArray jsonArray, List salesorders, KWLCurrency currency, DateFormat df) {
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            Iterator iterator = salesorders.iterator();
            String companyid = "";
            while (iterator.hasNext()) {
                Object[] oj = (Object[]) iterator.next();
                SalesOrder salesOrder = (SalesOrder) oj[0];
                if(salesOrder.getCompany()!=null){
                    companyid = salesOrder.getCompany().getCompanyID();
                }
                int type = (int) oj[1];
                JSONObject obj = new JSONObject();
                KWLCurrency doccurrency = null;
                if (salesOrder.getCurrency() != null) {
                    doccurrency = salesOrder.getCurrency();
                } else {
                    doccurrency = salesOrder.getCustomer().getAccount().getCurrency() == null ? currency : salesOrder.getCustomer().getAccount().getCurrency();
                }
                requestParams.put("companyid", salesOrder.getCompany()!=null?salesOrder.getCompany().getCompanyID():""); // Companyid needed for getTax method
                Customer customer = salesOrder.getCustomer();
                obj.put("billid", salesOrder.getID());
                obj.put("companyid", salesOrder.getCompany().getCompanyID());
                obj.put("companyname", salesOrder.getCompany().getCompanyName());
                obj.put("withoutinventory", false);
                obj.put("externalcurrencyrate", salesOrder.getExternalCurrencyRate());
                obj.put("personid", customer.getID());
                obj.put("aliasname", customer.getAliasname());
                obj.put("customercode", customer.getAcccode() == null ? "" : customer.getAcccode());
                obj.put("billno", salesOrder.getSalesOrderNumber());
                obj.put("lasteditedby", salesOrder.getModifiedby() == null ? "" : (salesOrder.getModifiedby().getFirstName() + " " + salesOrder.getModifiedby().getLastName()));
                obj.put("duedate", df.format(salesOrder.getDueDate()));
                obj.put("shipdate", salesOrder.getShipdate() == null ? "" : df.format(salesOrder.getShipdate()));
                obj.put("shipvia", salesOrder.getShipvia());
                obj.put("fob", salesOrder.getFob());
                obj.put("isOpeningBalanceTransaction", salesOrder.isIsOpeningBalanceSO());
                obj.put("isConsignment", salesOrder.isIsconsignment());
                obj.put("parentso", salesOrder.getParentSO() == null ? "" : salesOrder.getParentSO().getID());
                if (salesOrder.getCustWarehouse() != null) {
                    obj.put("custWarehouse", salesOrder.getCustWarehouse().getId());
                }
                obj.put("date", df.format(salesOrder.getOrderDate()));
                obj.put("isfavourite", salesOrder.isFavourite());
                obj.put("isprinted", salesOrder.isPrinted());
                obj.put("billto", salesOrder.getBillTo());
                obj.put("shipto", salesOrder.getShipTo());
                obj.put("salesPerson", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getID());
                obj.put("salespersonname", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getValue());
                obj.put("salesPersonCode", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getCode());
                obj.put("createdby", StringUtil.getFullName(salesOrder.getCreatedby()));
                obj.put("createdbyid", salesOrder.getCreatedby().getUserID());
                obj.put("deleted", salesOrder.isDeleted());
                obj.put(Constants.IsRoundingAdjustmentApplied, salesOrder.isIsRoundingAdjustmentApplied());
                boolean gstIncluded = salesOrder.isGstIncluded();
                obj.put("gstIncluded", gstIncluded);
                obj.put("islockQuantityflag", salesOrder.isLockquantityflag());  //for getting locked flag of indivisual so
                obj.put("leaseOrMaintenanceSo", salesOrder.getLeaseOrMaintenanceSO());
                obj.put("customerporefno", salesOrder.getCustomerPORefNo());
                obj.put("totalprofitmargin", salesOrder.getTotalProfitMargin());
                obj.put("totalprofitmarginpercent", salesOrder.getTotalProfitMarginPercent());
                obj.put("maintenanceId", salesOrder.getMaintenance() == null ? "" : salesOrder.getMaintenance().getId());
                obj.put("termname", salesOrder.getTerm() == null ? "" : salesOrder.getTerm().getTermname());
                obj.put("gtaapplicable", salesOrder.isRcmapplicable()); // Get RCM applicable Check - Used for INDIA only
                BillingShippingAddresses addresses = salesOrder.getBillingShippingAddresses();
                AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                obj.put(Constants.SEQUENCEFORMATID, salesOrder.getSeqformat() == null ? "" : salesOrder.getSeqformat().getID());
                Iterator itrRow = salesOrder.getRows().iterator();
                double amount = 0, totalDiscount = 0, discountPrice = 0;
                double rowTaxAmt = 0d, rowDiscountAmt = 0d;
                while (itrRow.hasNext()) {
                    SalesOrderDetail sod = (SalesOrderDetail) itrRow.next();
                    if (sod.getTax() != null) {
                        requestParams.put("transactiondate", salesOrder.getOrderDate());
                        requestParams.put("taxid", sod.getTax().getID());
                    }
                    double sorate = authHandler.round(sod.getRate(), companyid);
                    if (gstIncluded) {
                        sorate = sod.getRateincludegst();
                    }
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
                    amount += discountPrice;//amount += discountPrice + (discountPrice * rowTaxPercent/100);
                    if (!gstIncluded) {
                        amount += authHandler.round(sod.getRowTaxAmount(), companyid);
                    }
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
                try {
                    obj.put("termid", salesOrder.getTerm().getID());
                } catch (Exception ex) {
                    obj.put("termid", "");
                }
                obj.put("currencysymbol", doccurrency.getSymbol());
                obj.put("currencyid", doccurrency.getCurrencyID());
                obj.put("currencycode", doccurrency.getCurrencyCode());
                obj.put("taxid", salesOrder.getTax() == null ? "" : salesOrder.getTax().getID());
                obj.put("taxname", salesOrder.getTax() == null ? "" : salesOrder.getTax().getName());
                obj.put("shiplengthval", salesOrder.getShiplength());
                obj.put("invoicetype", salesOrder.getInvoicetype());
                double taxPercent = 0;
                if (salesOrder.getTax() != null) {
                    requestParams.put("transactiondate", salesOrder.getOrderDate());
                    requestParams.put("taxid", salesOrder.getTax().getID());
                    KwlReturnObject resulttax = accTaxObj.getTax(requestParams);
                    List taxList = resulttax.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                }
                double orderAmount = amount;//(Double) bAmt.getEntityList().get(0);
                double ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round((orderAmount * taxPercent / 100), companyid));
                double taxAmt = rowTaxAmt + ordertaxamount;// either row level tax will be avvailable or invoice level
                obj.put("amountbeforegst", amount - rowTaxAmt); // Amount before both kind of tax row level or transaction level
                obj.put("taxpercent", taxPercent);
                obj.put("taxamount", taxAmt);// Tax Amount
                obj.put(Constants.transactionNo, salesOrder.getSalesOrderNumber());
                obj.put("duedate", df.format(salesOrder.getDueDate()));
                obj.put("date", df.format(salesOrder.getOrderDate()));
                obj.put("personname", customer.getName());
                if (salesOrder.isIsconsignment()) {
                    obj.put("mergedCategoryData", "Consignment Request");
                } else if (salesOrder.getLeaseOrMaintenanceSO() == 1) {
                    obj.put("mergedCategoryData", "Lease Order");  //type of data  
                } else {
                    obj.put("mergedCategoryData", "Sales Order");  //type of data
                }
                
                obj.put("type", type);
                jsonArray.put(obj);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return jsonArray;
        }
    }
    /*Function to be used to get the Customer Quotation Json for Linking 
     linkType=4  CQ->SO
     linkType=7 CQ->SI
     */
    public JSONArray getCustomerQuotationJsonForLinking(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, DateFormat df,int linkType) {
        try {
            
             Quotation quotation=null;
            Iterator itrcq = listcq.iterator();
            while (itrcq.hasNext()) {
     
                if (linkType == 6) {//Type ==6 means we are getting json for CQ linked with VQ from Vendor Quotation Report.
                    String quotationId = (String) itrcq.next();
                    KwlReturnObject curresult = accountingHandlerDAOobj.getObject(Quotation.class.getName(), quotationId);
                    quotation = (Quotation) curresult.getEntityList().get(0);

                } else {
                    quotation = (Quotation) itrcq.next();
                }

                if (quotation != null) {
                    Customer customer = quotation.getCustomer();
//                    int type = 4; // CQ->SO
                    JSONObject obj = new JSONObject();
                    obj.put("billid", quotation.getID());
                    obj.put(Constants.IsRoundingAdjustmentApplied, quotation.isIsRoundingAdjustmentApplied());
                    obj.put("companyid", quotation.getCompany().getCompanyID());
                    obj.put("companyname", quotation.getCompany().getCompanyName());
                    obj.put("personid", customer.getID());
                    obj.put("aliasname", customer.getAliasname());
                    obj.put("status", quotation.isIsopen() ? "Open" : "Closed");
                    obj.put("personemail", customer == null ? "" : customer.getEmail());
                    obj.put("billno", quotation.getquotationNumber());
                    obj.put(Constants.transactionNo, quotation.getquotationNumber());
                    obj.put("contract", (quotation.getContract() != null) ? quotation.getContract().getID() : "");
                    obj.put("duedate", df.format(quotation.getDueDate()));
                    obj.put("date", df.format(quotation.getQuotationDate()));
                    obj.put("validtilldateinuserformat", quotation.getValiddate() == null ? "" : userdf.format(quotation.getValiddate()));
                    obj.put("duedateinuserformat", userdf.format(quotation.getDueDate()));
                    obj.put("dateinuserformat", userdf.format(quotation.getQuotationDate()));
                    obj.put("shipdate", quotation.getShipdate() == null ? "" : df.format(quotation.getShipdate()));
                    obj.put("shipdateinuserformat", quotation.getShipdate() == null ? "" : userdf.format(quotation.getShipdate()));
                    obj.put("validdate", quotation.getValiddate() == null ? "" : df.format(quotation.getValiddate()));
                    obj.put("preparedBy", quotation.getCreatedby() != null ? quotation.getCreatedby().getFullName() : "");
                    obj.put("shipvia", quotation.getShipvia() == null ? "" : quotation.getShipvia());
                    obj.put("fob", quotation.getFob() == null ? "" : quotation.getFob());
                    obj.put("archieve", quotation.getArchieve());
                    obj.put("billto", quotation.getBillTo());
                    obj.put("shipto", quotation.getShipTo());
                    obj.put("shippingterm", quotation.getShippingTerm());
                    obj.put("deleted", quotation.isDeleted());
                    obj.put("salesPerson", quotation.getSalesperson() != null ? quotation.getSalesperson().getID() : "");
                    obj.put("salespersonname", quotation.getSalesperson() == null ? "" : quotation.getSalesperson().getValue());
                    obj.put("salespersondesignation", quotation.getSalesperson() == null ? "" : quotation.getSalesperson().getDesignation());
                    obj.put("salesPersonCode", quotation.getSalesperson() == null ? "" : quotation.getSalesperson().getCode());
                    obj.put("salesPersonEmail", quotation.getSalesperson() == null ? "" : quotation.getSalesperson().getEmailID() == null ? "" : quotation.getSalesperson().getEmailID());
                    obj.put("salesPersonTel", quotation.getSalesperson() == null ? "" : quotation.getSalesperson().getContactNumber() == null ? "" : quotation.getSalesperson().getContactNumber());
                    obj.put("isfavourite", quotation.isFavourite());
                    obj.put("isprinted", quotation.isPrinted());
//                        obj.put("termdetails", getTermDetails(salesOrder.getID(), false));
//                        obj.put("termamount", CommonFunctions.getTotalTermsAmount(getTermDetails(salesOrder.getID(), false)));
                    obj.put("discountval", (quotation.getDiscount() == 0) ? 0 : quotation.getDiscount());
                    obj.put("gstIncluded", quotation.isGstIncluded());
                    obj.put("shiplengthval", quotation.getShiplength());
                    obj.put("invoicetype", quotation.getInvoicetype());
                    obj.put("quotationtype", quotation.getQuotationType());
                    obj.put("fixedAssetLeaseInvoice", quotation.isLeaseQuotation());
                    obj.put("termid", quotation.getTerm() != null ? quotation.getTerm().getID() : "");
                    obj.put("termname", quotation.getTerm() != null ? quotation.getTerm().getTermname() : "");
//                    obj.put("approvalstatusinfo", salesOrder.getApprovestatuslevel() == -1 ? "Rejected" : salesOrder.getApprovestatuslevel() < 11 ? "Waiting for Approval at Level - " + salesOrder.getApprovestatuslevel() : "Approved");
                    obj.put("approvalstatus", quotation.getApprovestatuslevel());
                    obj.put("customerporefno", quotation.getCustomerPORefNo());
                    obj.put("totalprofitmargin", quotation.getTotalProfitMargin());
                    obj.put("totalprofitmarginpercent", quotation.getTotalProfitMarginPercent());
                    BillingShippingAddresses addresses = quotation.getBillingShippingAddresses();
                    AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                    obj.put(Constants.SEQUENCEFORMATID, quotation.getSeqformat() == null ? "" : quotation.getSeqformat().getID());
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("personname", customer.getName());
                    obj.put("personcode", customer.getAcccode() == null ? "" : customer.getAcccode());
                    obj.put("billtoaddress", CommonFunctions.getBillingShippingAddress(addresses, true));
                    obj.put("shiptoaddress", CommonFunctions.getBillingShippingAddress(addresses, false));
                    obj.put("memo", quotation.getMemo());
                    obj.put("posttext", quotation.getPostText());
                    obj.put("gtaapplicable", quotation.isRcmapplicable()); // Get RCM applicable Check - Used for INDIA only
                    if (quotation.isLeaseQuotation()) {
                        obj.put("mergedCategoryData", "Lease Quotation");  //type of data 
                    } else {
                        obj.put("mergedCategoryData", "Customer Quotation");  //type of data
                    }

                    obj.put("type", linkType);
                    jsonArray.put(obj);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return jsonArray;
        }
    }

    @Override
    public JSONArray getVendorQuotationJsonForLinking(JSONArray jsonArray, List vendorquotation, KWLCurrency currency, DateFormat df, String companyid) {
        try {

            Iterator iterator = vendorquotation.iterator();
            while (iterator.hasNext()) {

                String vqid = (String) iterator.next();
                KwlReturnObject curresult = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), vqid);
                VendorQuotation purchaseOrder = (VendorQuotation) curresult.getEntityList().get(0);
                Set<VendorQuotationDetail> rows = purchaseOrder.getRows();

                Vendor vendor = purchaseOrder.getVendor();
                if (purchaseOrder.getCurrency() != null) {
                    currency = purchaseOrder.getCurrency();
                } else {
                    currency = vendor.getAccount().getCurrency() == null ? currency : vendor.getAccount().getCurrency();
                }

                JSONObject obj = new JSONObject();
                obj.put("billid", purchaseOrder.getID());
                obj.put("sequenceformatid", purchaseOrder.getSeqformat() != null ? purchaseOrder.getSeqformat().getID() : null);
                obj.put("companyid", purchaseOrder.getCompany().getCompanyID());
                obj.put("companyname", purchaseOrder.getCompany().getCompanyName());
                obj.put("externalcurrencyrate", purchaseOrder.getExternalCurrencyRate());
                obj.put("personid", vendor.getID());
                obj.put("aliasname", vendor.getAliasname());
                obj.put("personemail", vendor == null ? "" : vendor.getEmail());
                obj.put("billno", purchaseOrder.getQuotationNumber());
                obj.put("gtaapplicable", purchaseOrder.isGtaapplicable());
                obj.put(Constants.IsRoundingAdjustmentApplied, purchaseOrder.isIsRoundingAdjustmentApplied());

                obj.put("duedate", df.format(purchaseOrder.getDueDate()));
                obj.put("billtoaddress", purchaseOrder.getBillingShippingAddresses() == null ? "" : CommonFunctions.getBillingShippingAddress(purchaseOrder.getBillingShippingAddresses(), true));
                obj.put("shiptoaddress", purchaseOrder.getBillingShippingAddresses() == null ? "" : CommonFunctions.getBillingShippingAddress(purchaseOrder.getBillingShippingAddresses(), false));
                obj.put("createdby", purchaseOrder.getCreatedby() == null ? "" : StringUtil.getFullName(purchaseOrder.getCreatedby()));
                obj.put("dateinuserformat", df.format(purchaseOrder.getQuotationDate()));
                obj.put("shipdateinuserformat", purchaseOrder.getShipdate() == null ? "" : df.format(purchaseOrder.getShipdate()));
                obj.put("date", df.format(purchaseOrder.getQuotationDate()));
                obj.put("shipdate", purchaseOrder.getShipdate() == null ? "" : df.format(purchaseOrder.getShipdate()));
                obj.put("validdate", purchaseOrder.getValiddate() == null ? "" : df.format(purchaseOrder.getValiddate()));
                obj.put("shipvia", purchaseOrder.getShipvia() == null ? "" : purchaseOrder.getShipvia());
                obj.put("fob", purchaseOrder.getFob() == null ? "" : purchaseOrder.getFob());
                obj.put("archieve", purchaseOrder.getArchieve());
                obj.put("isfavourite", purchaseOrder.isFavourite());
                obj.put("isprinted", purchaseOrder.isPrinted());
                obj.put("termdetails", getTermDetails(purchaseOrder.getID(), false));
                obj.put(Constants.transactionNo, purchaseOrder.getQuotationNumber());
                if (purchaseOrder.getTermsincludegst() != null) {
                    obj.put(Constants.termsincludegst, purchaseOrder.getTermsincludegst());
                }
                obj.put("termamount", CommonFunctions.getTotalTermsAmount(getTermDetails(purchaseOrder.getID(), false)));
                obj.put("termid", purchaseOrder.getTerm() != null ? purchaseOrder.getTerm().getID() : "");
                obj.put("termname", purchaseOrder.getTerm() != null ? purchaseOrder.getTerm().getTermname() : "");
                obj.put("termdays", purchaseOrder.getTerm() != null ? purchaseOrder.getTerm().getTermdays() : "");
                obj.put("discountval", purchaseOrder.getDiscount());
                obj.put("billto", purchaseOrder.getBillTo() == null ? "" : purchaseOrder.getBillTo());
                obj.put("shipto", purchaseOrder.getShipTo() == null ? "" : purchaseOrder.getShipTo());
                obj.put("deleted", purchaseOrder.isDeleted());
                obj.put("agent", purchaseOrder.getMasteragent() != null ? purchaseOrder.getMasteragent().getID() : "");
                obj.put("agentname", purchaseOrder.getMasteragent() != null ? purchaseOrder.getMasteragent().getValue() : "");
                obj.put("gstIncluded", purchaseOrder.isGstIncluded());
                obj.put("shiplengthval", purchaseOrder.getShiplength());
                obj.put("invoicetype", purchaseOrder.getInvoicetype());
                obj.put("approvalstatus", purchaseOrder.getApprovestatuslevel());
                obj = AccountingAddressManager.getTransactionAddressJSON(obj, purchaseOrder.getBillingShippingAddresses(), true);

                if (purchaseOrder.getModifiedby() != null) {
                    obj.put("lasteditedby", StringUtil.getFullName(purchaseOrder.getModifiedby()));
                }
                obj.put("discountispertotal", purchaseOrder.isPerDiscount());
                obj.put("ispercentdiscount", purchaseOrder.isPerDiscount());
                double totalTermAmount = 0;
                HashMap<String, Object> requestParam = new HashMap();
                HashMap<String, Object> filterrequestParams = new HashMap();
                requestParam.put("vendorQuotation", purchaseOrder.getID());
                KwlReturnObject vendorQuotationResult = null;
                filterrequestParams.put("taxid", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getID());
                vendorQuotationResult = accPurchaseOrderobj.getVendorQuotationTermMap(requestParam);
                List<VendorQuotationTermMap> termMap = vendorQuotationResult.getEntityList();
                for (VendorQuotationTermMap vendorQuotationTermMap : termMap) {
                    filterrequestParams.put("term", vendorQuotationTermMap.getTerm() == null ? "" : vendorQuotationTermMap.getTerm().getId());
                    InvoiceTermsSales mt = vendorQuotationTermMap.getTerm();
                    double termAmnt = vendorQuotationTermMap.getTermamount();
                    totalTermAmount += authHandler.round(termAmnt, companyid);
                }
                totalTermAmount = authHandler.round(totalTermAmount, companyid);

                obj.put("currencysymbol", currency.getSymbol());
                obj.put("currencycode", currency.getCurrencyCode() == null ? "" : currency.getCurrencyCode());
                obj.put("taxid", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getID());
                obj.put("taxname", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getName());
                obj.put("currencyid", currency.getCurrencyID());
                obj.put("personname", vendor.getName());
                obj.put("personcode", vendor.getAcccode() == null ? "" : vendor.getAcccode());
                obj.put("memo", purchaseOrder.getMemo());
                obj.put("posttext", purchaseOrder.getPostText());
                if(purchaseOrder.isFixedAssetVQ())
                {
                    obj.put("mergedCategoryData","Fixed Asset Vendor Quotation");                         
                }else{
                   obj.put("mergedCategoryData", "Vendor Quotation");
                }
                obj.put("type", 5);

                jsonArray.put(obj);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return jsonArray;
        }
    }

    @Override
    public JSONArray getPurchaseOrderJsonForLinking(JSONArray jsonArray, List purchaseorder, KWLCurrency currency, DateFormat df) {
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            Iterator iterator = purchaseorder.iterator();
            while (iterator.hasNext()) {
                PurchaseOrder purchaseorderobj = (PurchaseOrder) iterator.next();
                JSONObject obj = new JSONObject();
                if (purchaseorderobj.getCurrency() != null) {
                    currency = purchaseorderobj.getCurrency();
                }
                Vendor vendor = purchaseorderobj.getVendor();
                obj.put("billid", purchaseorderobj.getID());
                obj.put("companyid", purchaseorderobj.getCompany().getCompanyID());
                obj.put("companyname", purchaseorderobj.getCompany().getCompanyName());
                obj.put("withoutinventory", "");
                obj.put(Constants.transactionNo, purchaseorderobj.getPurchaseOrderNumber());   //Purchase order no
                obj.put("duedate", df.format(purchaseorderobj.getDueDate()));
                obj.put("date", purchaseorderobj.getOrderDate() != null ? df.format(purchaseorderobj.getOrderDate()) : "");
                if (purchaseorderobj.isIsconsignment()) {
                    obj.put("mergedCategoryData", "Consignment Purchase Order");  //type of data
                } else if (purchaseorderobj.isFixedAssetPO()) {
                    obj.put("mergedCategoryData", "Fixed Asset Purchase Order");  //type of data
                } else {
                    obj.put("mergedCategoryData", "Purchase Order");  //type of data
                }
                obj.put("gtaapplicable", purchaseorderobj.isGtaapplicable());
                obj.put(Constants.IsRoundingAdjustmentApplied, purchaseorderobj.isIsRoundingAdjustmentApplied());
                obj.put("personname", vendor.getName());
                obj.put("personid", vendor.getID());
                obj.put("companyid", purchaseorderobj.getCompany().getCompanyID());
                obj.put("companyname", purchaseorderobj.getCompany().getCompanyName());
                obj.put("externalcurrencyrate", purchaseorderobj.getExternalCurrencyRate());
                obj.put("isOpeningBalanceTransaction", purchaseorderobj.isIsOpeningBalancePO());
                obj.put("currencyid", currency.getCurrencyID());
                obj.put("currencycode", currency.getCurrencyCode() == null ? "" : currency.getCurrencyCode());
                obj.put("personid", vendor.getID());
                obj.put("aliasname", vendor.getAliasname());
                obj.put("personcode", vendor.getAcccode() == null ? "" : vendor.getAcccode());
                obj.put("createdby", purchaseorderobj.getCreatedby() == null ? "" : StringUtil.getFullName(purchaseorderobj.getCreatedby()));
                obj.put("billtoaddress", purchaseorderobj.getBillingShippingAddresses() == null ? "" : CommonFunctions.getBillingShippingAddress(purchaseorderobj.getBillingShippingAddresses(), true));
                obj.put("shiptoaddress", purchaseorderobj.getBillingShippingAddresses() == null ? "" : CommonFunctions.getBillingShippingAddress(purchaseorderobj.getBillingShippingAddresses(), false));
                obj.put("personemail", vendor.getEmail());
                obj.put("billno", purchaseorderobj.getPurchaseOrderNumber());
                obj.put("duedate", df.format(purchaseorderobj.getDueDate()));
                obj.put("date", df.format(purchaseorderobj.getOrderDate()));
                obj.put("dateinuserformat", df.format(purchaseorderobj.getOrderDate()));
                obj.put("shipdate", purchaseorderobj.getShipdate() == null ? "" : df.format(purchaseorderobj.getShipdate()));
                obj.put("shipdateinuserformat", purchaseorderobj.getShipdate() == null ? "" : df.format(purchaseorderobj.getShipdate()));
                obj.put("shipvia", purchaseorderobj.getShipvia() == null ? "" : purchaseorderobj.getShipvia());
                obj.put("fob", purchaseorderobj.getFob() == null ? "" : purchaseorderobj.getFob());
                obj.put("isfavourite", purchaseorderobj.isFavourite());
                obj.put("isprinted", purchaseorderobj.isPrinted());
                obj.put("deleted", purchaseorderobj.isDeleted());
                obj.put("billto", purchaseorderobj.getBillTo() == null ? "" : purchaseorderobj.getBillTo());
                obj.put("shipto", purchaseorderobj.getShipTo() == null ? "" : purchaseorderobj.getShipTo());
                obj.put("agent", purchaseorderobj.getMasteragent() == null ? "" : purchaseorderobj.getMasteragent().getID());
                if (purchaseorderobj.getApprover() != null) {
                    obj.put("approver", StringUtil.getFullName(purchaseorderobj.getApprover()));
                }
                boolean gstIncluded = purchaseorderobj.isGstIncluded();
                obj.put("gstIncluded", gstIncluded);
                obj.put("isConsignment", purchaseorderobj.isIsconsignment());
                obj.put("termid", purchaseorderobj.getTerm() == null ? "" : purchaseorderobj.getTerm().getID());
                obj = AccountingAddressManager.getTransactionAddressJSON(obj, purchaseorderobj.getBillingShippingAddresses(), true);
                obj.put("termdays", purchaseorderobj.getTerm() == null ? 0 : purchaseorderobj.getTerm().getTermdays());
                obj.put("termname", purchaseorderobj.getTerm() == null ? 0 : purchaseorderobj.getTerm().getTermname());
                obj.put("termdetails", getTermDetails(purchaseorderobj.getID(), true));
                if (purchaseorderobj.getTermsincludegst() != null) {
                    obj.put(Constants.termsincludegst, purchaseorderobj.getTermsincludegst());
                }
                obj.put("termamount", CommonFunctions.getTotalTermsAmount(getTermDetails(purchaseorderobj.getID(), true)));
                obj.put(Constants.SEQUENCEFORMATID, purchaseorderobj.getSeqformat() == null ? "" : purchaseorderobj.getSeqformat().getID());
                obj.put("memo", purchaseorderobj.getMemo());
                obj.put("posttext", purchaseorderobj.getPostText());
                obj.put("taxid", purchaseorderobj.getTax() == null ? "" : purchaseorderobj.getTax().getID());
                obj.put("taxname", purchaseorderobj.getTax() == null ? "" : purchaseorderobj.getTax().getName());
                obj.put("costcenterid", purchaseorderobj.getCostcenter() == null ? "" : purchaseorderobj.getCostcenter().getID());
                obj.put("costcenterName", purchaseorderobj.getCostcenter() == null ? "" : purchaseorderobj.getCostcenter().getName());
                obj.put("shiplengthval", purchaseorderobj.getShiplength());
                obj.put("invoicetype", purchaseorderobj.getInvoicetype());
                obj.put("archieve", 0);
                obj.put("type", 2);
                obj.put("isexpenseinv", purchaseorderobj.isIsExpenseType());
                jsonArray.put(obj);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return jsonArray;
        }
    }
   
    // ERP-41133  - Outstanding SO Issue
    /**
     * Description : Method is used to get value for the Status for Sales Order  Report
     * @param <SalesOrder> Object of class SalesOrder
     * @param <CompanyAccountPreferences> Object of class CompanyAccountPreferences
     * @param <ExtraCompanyPreferences> Object of class ExtraCompanyPreferences
     * @return :status
     */
    public String getSOStatus(SalesOrder so, CompanyAccountPreferences pref, ExtraCompanyPreferences extraCompanyPreferences) throws ServiceException {
        String status = SalesOrder.STATUS_CLOSED;
        boolean checkServiceProductFlag = true; //by default check true in extracompanypreferences to check both service and inventory type
        try {
            //System Control option-Undelivered Service Item will keep SO Open
            JSONObject jObj = new JSONObject();
            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                if (jObj.has(Constants.columnPref.undeliveredServiceSOOpen.get()) && jObj.get(Constants.columnPref.undeliveredServiceSOOpen.get()) != null) {
                    checkServiceProductFlag = jObj.optBoolean(Constants.columnPref.undeliveredServiceSOOpen.get());
                }
            }

            Set<SalesOrderDetail> orderDetail = so.getRows();
            boolean fullInv = false;
            for (SalesOrderDetail salesOrderDetail : orderDetail) {
                if (salesOrderDetail.getProduct() != null && salesOrderDetail.getProduct().getProducttype() != null && salesOrderDetail.getProduct().getProducttype().getName().equalsIgnoreCase(Producttype.SERVICE_Name)) {
                    double qua = 0.0;
                    double quantPartTtInv = 0.0;

                    if (checkServiceProductFlag) {
                        double resultValue = salesOrderDetail.getQuantity();
                        KwlReturnObject idresult = accInvoiceDAOobj.getInvoiceDetailFromSOD(salesOrderDetail.getID());
                        List list = idresult.getEntityList();
                        if (!list.isEmpty()) { // if invoice is created using sales order
                            Iterator ite1 = list.iterator();
                            while (ite1.hasNext()) {
                                InvoiceDetail ge = (InvoiceDetail) ite1.next();
                                if (ge.getInvoice().isPartialinv()) {
                                    double quantity = ge.getInventory().getQuantity();
                                    quantPartTtInv += quantity * ge.getPartamount();
                                } else {
                                    fullInv = true;
                                    qua += ge.getInventory().getQuantity();
                                }
                            }
                            if (fullInv) {
                                resultValue = salesOrderDetail.getQuantity() - qua;
                            } else {
                                if (salesOrderDetail.getQuantity() * 100 > quantPartTtInv) {
                                    resultValue = salesOrderDetail.getQuantity() - qua;
                                } else {
                                    resultValue = 0;
                                }
                            }
                            if (resultValue > 0) {
                                status = SalesOrder.STATUS_OPEN;
                                break;
                            }
                        } else { // check if DO is created using sales order
                            double rejectedQuantity = 0;
                            double quantPartTt = salesOrderDetail.getQuantity() * 100;
                            if (pref.isWithInvUpdate()) { //In Trading Flow                 
                                KwlReturnObject doresult = accInvoiceDAOobj.getDeliveryOrderIDFromSOD(salesOrderDetail.getID(), pref.getCompany().getCompanyID());
                                List list1 = doresult.getEntityList();
                                if (list1.size() > 0) {
                                    Iterator ite1 = list1.iterator();
                                    while (ite1.hasNext()) {
                                        String orderid = (String) ite1.next();
                                        KwlReturnObject res = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), orderid);
                                        DeliveryOrderDetail deliveryOrderDetail = (DeliveryOrderDetail) res.getEntityList().get(0);
                                        fullInv = true;
                                        qua += deliveryOrderDetail.getDeliveredQuantity();
                                    }
                                }
                                if (so.isIsconsignment()) {//&& so.isLockquantityflag()
                                    rejectedQuantity = (salesOrderDetail.getRejectedQuantity() / salesOrderDetail.getBaseuomrate());
                                }
                            } else { //In Non Trading Flow 
                                idresult = accInvoiceDAOobj.getInvoiceDetailFromSOD(salesOrderDetail.getID());
                                List list1 = idresult.getEntityList();
                                Iterator ite1 = list1.iterator();
                                while (ite1.hasNext()) {
                                    InvoiceDetail ge = (InvoiceDetail) ite1.next();
                                    if (ge.getInvoice().isPartialinv()) {
                                        double quantity = ge.getInventory().getQuantity();
                                        quantPartTtInv += quantity * ge.getPartamount();
                                    } else {
                                        fullInv = true;
                                        qua += ge.getInventory().getQuantity();
                                    }
                                }
                            }
                            if (fullInv) {
                                if (so.isIsconsignment()) {//&& so.isLockquantityflag()
                                    qua = qua + rejectedQuantity;//In partial Case we have handle this by adding rejected quantity to used quatity in CDO
                                }
                                if (qua < salesOrderDetail.getQuantity()) {
                                    status = SalesOrder.STATUS_OPEN;
                                    break;
                                }
                            } else if (quantPartTt > quantPartTtInv) {
                                status = SalesOrder.STATUS_OPEN;
                                break;
                            }
                        }
                    }
                } else { // if product not service then SO will get closed only after creating DO
                    double qua = 0;
                    double rejectedQuantity = 0;
                    double quantPartTt = salesOrderDetail.getQuantity() * 100;
                    double quantPartTtInv = 0.0;
                    double sreturn = 0;
                    if (pref.isWithInvUpdate()) { //In Trading Flow                 
                        KwlReturnObject doresult = accInvoiceDAOobj.getDeliveryOrderIDFromSOD(salesOrderDetail.getID(), pref.getCompany().getCompanyID());
                        List list = doresult.getEntityList();
                        if (list.size() > 0) {
                            Iterator ite1 = list.iterator();
                            while (ite1.hasNext()) {
                                String orderid = (String) ite1.next();
                                KwlReturnObject res = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), orderid);
                                DeliveryOrderDetail deliveryOrderDetail = (DeliveryOrderDetail) res.getEntityList().get(0);
                                fullInv = true;
                                qua += deliveryOrderDetail.getDeliveredQuantity();
                                KwlReturnObject srResult = accInvoiceDAOobj.getSalesReturnDFromDOD(orderid);
                                List<SalesReturnDetail> srDetails = srResult.getEntityList();
                                if (srDetails != null) {
                                    /**
                                     * Minus sales return from delivered
                                     * quantity to get proper status of SO.
                                     * Ticket SDP-9059.
                                     */
                                    for (SalesReturnDetail srd : srDetails) {
                                        sreturn += srd.getReturnQuantity();
                                    }
                                }
                                qua = qua - sreturn;
                            }
                        }
                        if (so.isIsconsignment()) {//&& so.isLockquantityflag()
                            rejectedQuantity = (salesOrderDetail.getRejectedQuantity() / salesOrderDetail.getBaseuomrate());
                        }
                    } else { //In Non Trading Flow 
                        KwlReturnObject idresult = accInvoiceDAOobj.getInvoiceDetailFromSOD(salesOrderDetail.getID());
                        List list = idresult.getEntityList();
                        Iterator ite1 = list.iterator();
                        while (ite1.hasNext()) {
                            InvoiceDetail ge = (InvoiceDetail) ite1.next();
                            if (ge.getInvoice().isPartialinv()) {
                                double quantity = ge.getInventory().getQuantity();
                                quantPartTtInv += quantity * ge.getPartamount();
                            } else {
                                fullInv = true;
                                qua += ge.getInventory().getQuantity();
                            }
                        }
                    }
                    if (fullInv) {
                        if (so.isIsconsignment()) {//&& so.isLockquantityflag()
                            qua = qua + rejectedQuantity;//In partial Case we have handle this by adding rejected quantity to used quatity in CDO
                        }
                        if (qua < salesOrderDetail.getQuantity()) {
                            status = SalesOrder.STATUS_OPEN;
                            break;
                        }
                    } else if (quantPartTt > quantPartTtInv) {
                        status = SalesOrder.STATUS_OPEN;
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accSalesOrderControllerCWN.getSOStatus : " + ex.getMessage(), ex);
        }
        return status;
    }
    
    /*Get DetailJSON For document designer and mailnotification*/
       public JSONArray getSODetailsItemJSON(JSONObject requestObj, String SOID, HashMap<String, Object> paramMap) {
             
        JSONArray jArr = new JSONArray();
        double globalLevelExchangedRateTotalAmount = 0, globalLevelExchangedRateSubTotal = 0, globalLevelExchangedRateTotalTax = 0, globalLevelExchangedRateSubTotalwithDiscount = 0, globalLevelExchangedRateTermAmount = 0;
        try {
            HashMap<String, Integer> FieldMap = (HashMap<String, Integer>) paramMap.get(Constants.fieldMap);
            HashMap<String, String> replaceFieldMap = (HashMap<String, String>) paramMap.get(Constants.replaceFieldMap);
            HashMap<String, Integer> DimensionFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.dimensionFieldMap);
            HashMap<String, Integer> LineLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.lineLevelCustomFieldMap);
            HashMap<String, Integer> ProductLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.productLevelCustomFieldMap);
            String companyid = requestObj.optString(Constants.companyKey);
            String CQref="";
            String allTerms="", customerTitle = "";
            StringBuilder appendtermString = new StringBuilder();
            java.util.Date entryDate = null, transactionDate = null;
            boolean isgstincluded=false;
            String billAddr = "", shipAddr = "", mainTaxName = "", createdby = "", POref = "", POrefdate = "", globallevelcustomfields = "", globalleveldimensions = "", updatedby = "", uomForTotalQuantity="";
            Tax mainTax = null;
            boolean isLocationForProduct = false,isbatchforproduct=false,isserialforproduct=false,isWarehouseForProduct=false;
            int quantitydigitafterdecimal=2,amountdigitafterdecimal=2,unitpricedigitafterdecimal=2;
            PdfTemplateConfig config=null;
            double totaltax = 0,totalAmount = 0,subtotal=0,totalDiscount=0,taxPercent = 0, days = 0;
            int noofdays=0;
            double totalQuantity = 0;
            DateFormat datef=authHandler.getDateOnlyFormat();
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(requestObj);
            HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), SOID);
            SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), requestObj.optString(Constants.globalCurrencyKey));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = currencyid = (salesOrder.getCurrency()==null)? currency.getCurrencyID() : salesOrder.getCurrency().getCurrencyID();;
            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(requestObj);//User Date Formatter
            filter_names.add("salesOrder.ID");
            filter_params.add(salesOrder.getID());
            soRequestParams.put("filter_names", filter_names);
            soRequestParams.put("filter_params", filter_params);
            soRequestParams.put("order_by", order_by);
            soRequestParams.put("order_type", order_type);
            KwlReturnObject podresult = accSalesOrderDAOobj.getSalesOrderDetails(soRequestParams);
            List<SalesOrderDetail> list = podresult.getEntityList();
            int rowcnt = 0;
            createdby = salesOrder.getCreatedby() != null ? salesOrder.getCreatedby().getFullName() : "";
            updatedby = salesOrder.getModifiedby() != null ? salesOrder.getModifiedby().getFullName() : "";
            JSONObject summaryData = new JSONObject();
            //document currency
            if (salesOrder != null && salesOrder.getCurrency() != null && !StringUtil.isNullOrEmpty(salesOrder.getCurrency().getCurrencyID())) {
                summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, salesOrder.getCurrency().getCurrencyID());
            }
            /**
             * get customer title (Mr./Mrs.)
             */
            customerTitle = salesOrder.getCustomer().getTitle();
            if(!StringUtil.isNullOrEmpty(customerTitle)){
                KwlReturnObject masterItemResult = kwlCommonTablesDAOObj.getObject(MasterItem.class.getName(), customerTitle);
                MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                customerTitle = masterItem.getValue();
            }
            /*ExchangeRate values*/
            double externalCurrencyRate = salesOrder.getExternalCurrencyRate();
            double revExchangeRate = 0.0;
            if (externalCurrencyRate != 0) {
                revExchangeRate = 1 / externalCurrencyRate;
            }
          
            KwlReturnObject resultcompany = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) resultcompany.getEntityList().get(0);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            /*
            * Declaration of Variables to fetch Line level taxes and its information like
            * Tax Code, Tax Amount etc.
            */ 
            String allLineLevelTax="", allLineLevelTaxAmount = "", allLineLevelTaxBasic = "", lineLevelTaxSign = "";
            List<String> lineLevelTaxesGST = new ArrayList<>();
            Map<String, Object> lineLevelTaxAmountGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxPercentGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxBasicGST = new HashMap<String, Object>();
            double TotalLineLevelTaxAmount = 0, subTotalWithDiscount = 0;
            Map<String,Double> lineLevelTaxNames = new LinkedHashMap<String,Double>();
            int countryid = 0;
            if( extraCompanyPreferences.getCompany()!= null && extraCompanyPreferences.getCompany().getCountry() != null ){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            //get Company PostText
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, requestObj.optInt("moduleid"));
            if (templateConfig.getEntityList().size() > 0) {
                config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            if (salesOrder.getTodate() != null && salesOrder.getFromdate() != null) {
                Calendar todatecal = Calendar.getInstance();
                Calendar fromdatecal = Calendar.getInstance();
                todatecal.setTime(salesOrder.getTodate());
                fromdatecal.setTime(salesOrder.getFromdate());
                
                Date fromDate = fromdatecal.getTime();
                try {
                    String fdate = datef.format(fromDate);
                    fromDate=datef.parse(fdate);
                } catch (ParseException e) {
                    fromDate = fromdatecal.getTime();
                }
                
                Date toDate = todatecal.getTime();
                try {
                    String tdate = datef.format(toDate);
                    toDate=datef.parse(tdate);
                } catch (ParseException e) {
                    toDate = todatecal.getTime();
                }
                
                days = authHandler.diffDays(fromDate, toDate);
                noofdays= (int) days;
            }

            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);

                if (decimalcontact[1] != null) {
                    quantitydigitafterdecimal = (Integer) decimalcontact[1];
                }
                if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                }
                if (decimalcontact[3] != null) {
                    unitpricedigitafterdecimal = Integer.parseInt(decimalcontact[3].toString());
                }
            }
            Set<String> uniqueProductTaxList = new HashSet<String>();
            JSONObject detailsTableData = new JSONObject();
            JSONArray gstTaxSummaryDetailsTableDataArr = new JSONArray();
            for(SalesOrderDetail row:list) {//product row
                String proddesc="",rowTaxName="",discountname="",basqtyuom="",BaseQtyWithUOM="";
                JSONObject obj = new JSONObject();
                JSONObject gstTaxSummaryDetailsTableData = new JSONObject();
                double rowamountwithouttax=0,rowamountwithtax = 0,rowTaxAmt=0,quantity = 0,rate = 0,rowdiscountvalue=0,rowTaxPercent=0,baseqty=0;
                double rowamountwithgst = 0;
                rowcnt++;
                Product prod = row.getProduct();

                obj.put(CustomDesignerConstants.SrNO, rowcnt);// Sr No
                obj.put(CustomDesignerConstants.ProductName, prod.getName().replaceAll("\n", "<br>"));// productname
                obj.put("type",prod.getProducttype()==null?"":prod.getProducttype().getName());
                proddesc=StringUtil.isNullOrEmpty(row.getDescription())?(StringUtil.isNullOrEmpty(row.getProduct().getDescription())?"":row.getProduct().getDescription()):row.getDescription();
                proddesc =  StringUtil.DecodeText(proddesc);
                obj.put(CustomDesignerConstants.ProductDescription,proddesc.replaceAll("\n", "<br>"));//Product Description
                obj.put(CustomDesignerConstants.AdditionalDescription, prod.getAdditionalDesc() != null ? prod.getAdditionalDesc().replaceAll("\n", "<br>") :"");  //product Addtional description
                obj.put(CustomDesignerConstants.IN_ProductCode,prod.getProductid());
                obj.put(CustomDesignerConstants.HSCode, (prod.getHSCode()!=null) ? prod.getHSCode().replaceAll("\n", "<br>") : "" );//get HSCode of ProductGrid
                String baseUrl = URLUtil.getDomainURL(requestObj.optString("cdomain"), false);
                /*
                 * Following code is to check whether the image is predent for product or not. 
                 * If Image is not present sent s.gif instead of product id
                 */
                String fileName = null;
                fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages+prod.getID() + ".png";
                File file = new File(fileName);
                FileInputStream in = null; 
                String filePathString = "";
                try {
                    in = new FileInputStream(file);
                } catch(java.io.FileNotFoundException ex) {
                    //catched exception if file not found, and to continue for rest of the products
                   filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                if(in != null){
                    filePathString = baseUrl + "productimage?fname=" + prod.getID() + ".png&isDocumentDesignerPrint=true";
                } else{
                    filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                obj.put(CustomDesignerConstants.imageTag,filePathString);
//Serial Number,Batch Number & Location
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
                    KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), row.getProduct().getID());
                    Product product = (Product) prodresult.getEntityList().get(0);
                    isLocationForProduct = product.isIslocationforproduct();
                    isbatchforproduct = product.isIsBatchForProduct();
                    isserialforproduct = product.isIsSerialForProduct();
                    isWarehouseForProduct = product.isIswarehouseforproduct();
                }
                KwlReturnObject productCategories = null;
                productCategories = accProductObj.getProductCategoryForDetailsReport(prod.getID());
                List productCategoryList = productCategories.getEntityList();
                String cateogry = "";
                Iterator catIte = productCategoryList.iterator();
                while (catIte.hasNext()) {
                    ProductCategoryMapping pcm = (ProductCategoryMapping) catIte.next();
                    String categoryName = pcm.getProductCategory() != null ? pcm.getProductCategory().getValue().toString() : "";
                    cateogry += categoryName + " ";
                } 
                if ( StringUtil.isNullOrEmpty(cateogry)) {
                    cateogry = "None";
                }
                obj.put("productCategory", cateogry);
                obj.put("productType", prod.getProducttype().getName());
                HashMap<String, Object> params = new HashMap<String, Object>();
                filter_names = new ArrayList();
                filter_params = new ArrayList();
                filter_names.add("companyid");
                filter_names.add("fieldtype");
                filter_names.add("customcolumn");
                filter_names.add("moduleid");
                filter_params.add(companyid);
                filter_params.add(4);
                filter_params.add(1);
                filter_params.add(requestObj.optInt("moduleid"));
                params.put("filter_names", filter_names);
                params.put("filter_values", filter_params);
                
                KwlReturnObject fieldparams =  accAccountDAOobj.getFieldParams(params);
                List fieldParamsList = fieldparams.getEntityList();
                
                JSONArray jsonarr = new JSONArray();
                for(int cnt=0; cnt < fieldParamsList.size(); cnt++){
                    FieldParams fieldParamsObj = (FieldParams) fieldParamsList.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("comboName", fieldParamsObj.getFieldname());
                    jsonarr.put(tempObj);
                }
                
                obj.put("groupingComboList", jsonarr);
                
                if (salesOrder.isLockquantityflag() && (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory()
                        || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory()
                        || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory())) {  //check if company level option is on then only we will check productt level
                    if (isbatchforproduct || isserialforproduct || isLocationForProduct || isWarehouseForProduct) {  //product level batch and serial no on or not
//                            obj.put("batchdetails", getNewBatchJson(row.getProduct(), request, row.getID()));
                        String batchdetails = getNewBatchJson(row.getProduct(), requestObj, row.getID());
                        JSONArray locjArr = new JSONArray(batchdetails);
                        String location = "";
                        String locationName = "", batchname = "", warehouse = "", serialnumber = "", serialexpdate = "",batchexpdate="",batchmfgdate="";
                        String locationnamenew = "", batchnamenew = "", warehousenew = "", serialnumbernew = "", serialexpdatenew = "",batchexpdatenew="",batchesmfgdatenew = "";
                        Set<String> batchnames = new LinkedHashSet<String>();
                        Set<String> serialnumbers = new LinkedHashSet<String>();
                        Set<String> locationnames = new LinkedHashSet<String>();
                        Set<String> warehouses = new LinkedHashSet<String>();
                        LinkedList<String> serialsexpdate = new LinkedList();
                        LinkedList<String> batchesexpirydate = new LinkedList();
                        LinkedList<String> batchesmfgdate = new LinkedList();

                        for (int i = 0; i < locjArr.length(); i++) {
                            JSONObject jSONObject = new JSONObject(locjArr.get(i).toString());
                            location = jSONObject.optString("location", "");
                            batchname = jSONObject.optString("batchname", "");
                            warehouse = jSONObject.optString("warehouse", "");
                            serialnumber = jSONObject.optString("serialno", "");
                            serialexpdate = jSONObject.optString("expend", "");
                            batchexpdate = jSONObject.optString("expdate", "");
                            batchmfgdate = jSONObject.optString("mfgdate", "");
                            Date date = null;
                            if (!StringUtil.isNullOrEmpty(serialexpdate)) {
                                date = datef.parse(serialexpdate);
                                serialexpdate = df.format(date);
                                serialsexpdate.add(serialexpdate);
                            } else {
                                serialsexpdate.add(" ");
                            }
                            if (!StringUtil.isNullOrEmpty(batchexpdate)) {
                                date = datef.parse(batchexpdate);
                                batchexpdate = df.format(date);
                                if (batchesexpirydate.contains(batchexpdate)) {
                                     batchesexpirydate.add(" ");
                                } else {
                                    batchesexpirydate.add(batchexpdate);
                                }
                            } else {
                                batchesexpirydate.add(" ");
                            }
                            if (!StringUtil.isNullOrEmpty(batchmfgdate)) {
                                date = datef.parse(batchmfgdate);
                                batchmfgdate = df.format(date);
                                if (batchesmfgdate.contains(batchmfgdate)) {
                                    batchesmfgdate.add(" ");
                                } else {
                                    batchesmfgdate.add(batchmfgdate);
                                }
                            } else {
                                batchesmfgdate.add(" ");
                            }
                            serialnumbers.add(serialnumber);
                            batchnames.add(batchname);
                            warehouses.add(warehouse);

                            if (!StringUtil.isNullOrEmpty(location)) {
                                KwlReturnObject loc = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), location);
                                InventoryLocation localist = (InventoryLocation) loc.getEntityList().get(0);
                                locationName = localist.getName();
                                locationnames.add(locationName);
//                                locationName = locationName.concat(",");
                            }

                            if (!StringUtil.isNullOrEmpty(batchname)) {
                                batchname = batchname.concat("!##");
                            }
                            if (!StringUtil.isNullOrEmpty(serialnumber)) {
                                serialnumber = serialnumber.concat("!##");
                            }
                        }

                        for (String str : batchnames) {
                            String bno = "";
                            bno = str;
                            if (!StringUtil.isNullOrEmpty(bno) && !bno.equals(" ")) {
                                batchnamenew += bno.concat("!##");
                            }
                        }

                        for (String str : serialnumbers) {
                            String sno = "";
                            sno = str;
                            if (!StringUtil.isNullOrEmpty(sno) && !sno.equals(" ")) {
                                serialnumbernew += sno.concat("!##");
                            }
                        }
                        for (String str : locationnames) {
                            String lno = "";
                            lno = str;
                            if (!StringUtil.isNullOrEmpty(lno) && !lno.equals(" ")) {
                                locationnamenew += lno.concat("!##");
                            }
                        }
                        
                        for (String str : serialsexpdate) {
                            String sexp = "";
                            sexp = str;
                            if (!StringUtil.isNullOrEmpty(sexp) && !sexp.equals(" ")) {
                                serialexpdatenew += sexp.concat("!##");
                            }
                        }
                        for (String str : batchesexpirydate) {
                            String bexp = "";
                            bexp = str;
                            if (!StringUtil.isNullOrEmpty(bexp) && !bexp.equals(" ")) {
                                batchexpdatenew += bexp.concat("!##");
                            }
                        }
                        for (String str : batchesmfgdate) {
                            String bmfg = "";
                            bmfg = str;
                            if (!StringUtil.isNullOrEmpty(bmfg) && !bmfg.equals(" ")) {
                                batchesmfgdatenew += bmfg.concat("!##");
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(batchesmfgdatenew)) {
                            batchesmfgdatenew = batchesmfgdatenew.substring(0, batchesmfgdatenew.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(serialexpdatenew)) {
                            serialexpdatenew = serialexpdatenew.substring(0, serialexpdatenew.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(batchexpdatenew)) {
                            batchexpdatenew = batchexpdatenew.substring(0, batchexpdatenew.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(locationnamenew.toString())) {
                            locationnamenew = locationnamenew.substring(0, locationnamenew.length() - 3);
                        }

                        if (!StringUtil.isNullOrEmpty(serialnumbernew.toString())) {
                            serialnumbernew = serialnumbernew.substring(0, serialnumbernew.length() - 3);
                        }

                        if (!StringUtil.isNullOrEmpty(batchnamenew.toString())) {
                            batchnamenew = batchnamenew.substring(0, batchnamenew.length() - 3);
                        }
                        obj.put(CustomDesignerConstants.SerialNumber, serialnumbernew);
                        obj.put(CustomDesignerConstants.SerialNumberExp, serialexpdatenew);
                        obj.put(CustomDesignerConstants.BatchNumber, batchnamenew);
                        obj.put(CustomDesignerConstants.BatchNumberExp, batchexpdatenew);
                        obj.put(CustomDesignerConstants.IN_Loc, locationnamenew);
                        obj.put(CustomDesignerConstants.ManufacturingDate, batchesmfgdatenew);
                    } else { //if not activated location for product level then take default location for product.
                        obj.put(CustomDesignerConstants.IN_Loc, row.getProduct().getLocation() == null ? "" : row.getProduct().getLocation().getName());
                    }
                } else { //if not activated location for product level then take default location for product.
                    obj.put(CustomDesignerConstants.IN_Loc, row.getProduct().getLocation() == null ? "" : row.getProduct().getLocation().getName());
                }

                String uom = row.getUom() == null ? (row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA()) : row.getUom().getNameEmptyforNA();
                uomForTotalQuantity = uom;

                if (row.getSalesOrder().isGstIncluded()) {//if gstincluded is the case.
                    isgstincluded = true;
                }
                rate = row.getRate();

                quantity = row.getQuantity();
                rowamountwithouttax = rate*quantity;//Subtotal

                /*
                 * In include GST case calculations are in reverse order
                 * In other cases calculations are in forward order
                 */
                if (row.getSalesOrder().isGstIncluded()) {//if gstincluded is the case
                    rowamountwithgst = row.getRateincludegst() * quantity;
                    rowdiscountvalue = (row.getDiscountispercent() == 1)? authHandler.round((rowamountwithgst * row.getDiscount()/100), companyid) : row.getDiscount();
                    obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(rowamountwithgst - row.getRowTaxAmount(), companyid));//Subtotal
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount,authHandler.formattingDecimalForAmount((rowamountwithgst - rowdiscountvalue - row.getRowTaxAmount()),companyid));//Subtotal
                    subtotal += authHandler.round(rowamountwithgst - row.getRowTaxAmount(), companyid);
                    subTotalWithDiscount = authHandler.round((rowamountwithgst - rowdiscountvalue - row.getRowTaxAmount()),companyid);
                } else{
                    rowdiscountvalue = (row.getDiscountispercent() == 1)? authHandler.round((rowamountwithouttax*row.getDiscount()/100), companyid) : row.getDiscount();
                    obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(rowamountwithouttax, companyid));//Subtotal
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount,authHandler.formattingDecimalForAmount((rowamountwithouttax-rowdiscountvalue), companyid));//Subtotal
                    subtotal += authHandler.round(rowamountwithouttax, companyid);//rounded becuase totaltax & amount are rounded and saved in db
                    subTotalWithDiscount = authHandler.round((rowamountwithouttax-rowdiscountvalue), companyid);
                }
                obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(rate, companyid));// Rate
                obj.put(CustomDesignerConstants.RATEINCLUDINGGST, authHandler.formattingDecimalForUnitPrice(row.getRateincludegst(), companyid));// Rate Including GST
                KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                
                 //Calculating base qty with UOM
                basqtyuom=prod.getUnitOfMeasure()==null?"":prod.getUnitOfMeasure().getNameEmptyforNA();
                double baseuomqty = row.getBaseuomrate()*quantity;
                /*
                 * Balance Qty
                 */
                double balanceQty = row.getBalanceqty();
                
                if(!basqtyuom.equals(uom)){
                    BaseQtyWithUOM =authHandler.formattingDecimalForQuantity(baseuomqty, companyid)+ " " + basqtyuom; 
                }else{
                  BaseQtyWithUOM=authHandler.formattingDecimalForQuantity(quantity, companyid)+ " " + uom;
                  baseuomqty=quantity;
                }

                obj.put(CustomDesignerConstants.BaseQty, authHandler.formattingDecimalForQuantity(baseuomqty, companyid));
                obj.put(CustomDesignerConstants.BaseQtyWithUOM, BaseQtyWithUOM);
                /*
                 * Balance Qty
                 */
                obj.put(CustomDesignerConstants.BalanceQty, authHandler.formattingDecimalForQuantity(balanceQty, companyid));
                /*
                 * Balance Qty With UOM
                 */
                obj.put(CustomDesignerConstants.BalanceQtyWithUOM, authHandler.formattingDecimalForQuantity(balanceQty, companyid) + " " + uom);
//                double rateInCurr = (Double) bAmt.getEntityList().get(0);
//                rate=rateInCurr;

               /*Discount Section*/
                totalDiscount +=authHandler.round(rowdiscountvalue, companyid);
                if(row.getDiscountispercent() == 1){
                  discountname=CustomDesignHandler.getAmountinCommaDecimal(row.getDiscount(),0,countryid)+"%";//to return 0 no of zeros
                }else{
                 discountname=salesOrder.getCurrency().getSymbol()+" "+authHandler.formattingDecimalForAmount(row.getDiscount(), companyid);//to show as it is in UI
                }
                totalQuantity+=quantity;
                obj.put(CustomDesignerConstants.IN_Discount, authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));// Discount
                obj.put(CustomDesignerConstants.Discountname, discountname);// Discount
                obj.put(CustomDesignerConstants.QuantitywithUOM, authHandler.formattingDecimalForQuantity(quantity, companyid)+" "+uom); // Quantity
                obj.put(CustomDesignerConstants.IN_Currency, salesOrder.getCurrency().getCurrencyCode());
                obj.put(CustomDesignerConstants.IN_Quantity,authHandler.formattingDecimalForQuantity(quantity, companyid));
                obj.put(CustomDesignerConstants.IN_UOM,uom);
                obj.put(CustomDesignerConstants.PRODUCTNETWEIGHT, authHandler.formattingDecimalForAmount(prod.getProductweight(), companyid));//Product Weight
                obj.put("currencysymbol",salesOrder.getCurrency().getSymbol());
                obj.put("currencycode",salesOrder.getCurrency().getCurrencyCode());
                obj.put("isGstIncluded",isgstincluded);

                entryDate = salesOrder.getOrderDate();
                /*Row Tax Section*/
                if (row!= null&&row.getTax() != null) {
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", row.getTax().getID());
                    uniqueProductTaxList.add(row.getTax().getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj=(Object[]) taxList.get(0);
                    rowTaxPercent=taxObj[1]==null?0:(Double) taxObj[1];
                    rowTaxName=row.getTax().getName();
                    rowTaxAmt=row.getRowTaxAmount();
                }
                totaltax +=authHandler.round(rowTaxAmt,companyid);//Calculate tax amount from line item
                obj.put(CustomDesignerConstants.IN_Tax, rowTaxAmt);// Tax
                obj.put(CustomDesignerConstants.IN_ProductTax, rowTaxName);//Tax Name

                if (row.getSalesOrder().isGstIncluded()) {
                    /*
                     * In case of including GST Tax value is substacted from original amount that will amount of product
                     */
                    rowamountwithouttax = (row.getRateincludegst() * quantity) - rowdiscountvalue - row.getRowTaxAmount();
                    rowamountwithtax = rowamountwithouttax + row.getRowTaxAmount(); //Amount will be equal to rowamountwithouttax because tax gets added in gst
                } else {
                    if (rowdiscountvalue != 0) {
                        rowamountwithouttax -= rowdiscountvalue;//deducting discount if any
                    }
                    rowamountwithtax = rowamountwithouttax + rowTaxAmt;
                }
                
                String lineLevelTax = "";
                String lineLevelTaxPercent = "";
                String lineLevelTaxAmount = "";
                double lineLevelTaxAmountTotal = 0;
                if(extraCompanyPreferences.isIsNewGST()){ // for New gst check
                    HashMap<String, Object> SalesOrderDetailParams = new HashMap<String, Object>();
                    SalesOrderDetailParams.put("salesOrderDetailId", row.getID());
                    SalesOrderDetailParams.put("orderbyadditionaltax", true);
                    // GST
                    SalesOrderDetailParams.put("termtype", 7);
                    KwlReturnObject grdTermMapresult = accSalesOrderDAOobj.getSalesOrderDetailTermMap(SalesOrderDetailParams);
                    List<SalesOrderDetailTermMap> gst = grdTermMapresult.getEntityList();
                    obj.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.CESSPERCENT, 0);
                    obj.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.TAXABLE_VALUE, subTotalWithDiscount);
                    
                    for (SalesOrderDetailTermMap salesorderdetailTermMap : gst) {
                        LineLevelTerms mt = salesorderdetailTermMap.getTerm();
                        if (mt.getTerm().contains(CustomDesignerConstants.CGST)) {
                            obj.put(CustomDesignerConstants.CGSTPERCENT, salesorderdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.CGSTAMOUNT, salesorderdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, salesorderdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, salesorderdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.IGST)) {
                            obj.put(CustomDesignerConstants.IGSTPERCENT, salesorderdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.IGSTAMOUNT, salesorderdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, salesorderdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, salesorderdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.SGST)) {
                            obj.put(CustomDesignerConstants.SGSTPERCENT, salesorderdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.SGSTAMOUNT, salesorderdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, salesorderdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, salesorderdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.UTGST)) {
                            obj.put(CustomDesignerConstants.UTGSTPERCENT, salesorderdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.UTGSTAMOUNT, salesorderdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, salesorderdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, salesorderdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.CESS)) {
                            obj.put(CustomDesignerConstants.CESSPERCENT, salesorderdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.CESSAMOUNT, salesorderdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, salesorderdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, salesorderdetailTermMap.getTermamount());
                        }
                        lineLevelTax += mt.getTerm();
                        lineLevelTax += "!## ";
                        lineLevelTaxPercent += authHandler.formattingDecimalForAmount(salesorderdetailTermMap.getPercentage(), companyid);
                        lineLevelTaxPercent += "!## ";
                        lineLevelTaxAmount += CustomDesignHandler.getAmountinCommaDecimal(salesorderdetailTermMap.getTermamount(), amountdigitafterdecimal,countryid);
                        lineLevelTaxAmount += "!## ";
                        /*
                         * calculating total of line level taxes
                         */
                        lineLevelTaxAmountTotal += salesorderdetailTermMap.getTermamount();
                        if (lineLevelTaxNames.containsKey(mt.getTerm()) && lineLevelTaxNames.get(mt.getTerm()) != null) {
                            double value = lineLevelTaxNames.get(mt.getTerm());
                            lineLevelTaxNames.put(mt.getTerm(), salesorderdetailTermMap.getTermamount() + value);
                        } else {
                            lineLevelTaxNames.put(mt.getTerm(), salesorderdetailTermMap.getTermamount());
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTax)) {
                        lineLevelTax = lineLevelTax.substring(0, lineLevelTax.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxPercent)) {
                        lineLevelTaxPercent = lineLevelTaxPercent.substring(0, lineLevelTaxPercent.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxAmount)) {
                        lineLevelTaxAmount = lineLevelTaxAmount.substring(0, lineLevelTaxAmount.length() - 4);
                    }
                    obj.put(CustomDesignerConstants.LineLevelTax, lineLevelTax);
                    obj.put(CustomDesignerConstants.LineLevelTaxAmount, lineLevelTaxAmount);
                    obj.put(CustomDesignerConstants.LineLevelTaxPercent, lineLevelTaxPercent);
                    /*
                     * putting subtotal+tax
                     */
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax), companyid) + authHandler.round(lineLevelTaxAmountTotal, companyid));       
                    //set product level "Product Tax Class" dimension value in HSN/SAC Code field
                    ExportRecordHandler.setHsnSacProductDimensionField(prod, obj, companyid, accAccountDAOobj, kwlCommonTablesDAOObj);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.HSN_SAC_CODE, obj.optString(CustomDesignerConstants.HSN_SAC_CODE, ""));
                    gstTaxSummaryDetailsTableDataArr.put(gstTaxSummaryDetailsTableData);
                } else {
                    /*
                    * Fetching distinct taxes used at line level, feetched in the set
                    * Also, fetched the information related to tax in different maps
                    */
                    boolean isRowTaxApplicable = false;
                    double rowTaxPercentGST = 0.0;
                    if (row.getTax() != null) {
                        String taxCode = row.getTax().getTaxCode();
                        if (!lineLevelTaxesGST.contains(taxCode)) {
                            lineLevelTaxesGST.add(taxCode);
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, salesOrder.getOrderDate(), row.getTax().getID());
                            rowTaxPercentGST = (Double) perresult.getEntityList().get(0);
                            lineLevelTaxPercentGST.put(taxCode, rowTaxPercentGST);
                            lineLevelTaxAmountGST.put(taxCode, 0.0);
                            lineLevelTaxBasicGST.put(taxCode, 0.0);
                        }
                        lineLevelTaxAmountGST.put(taxCode,(Double) lineLevelTaxAmountGST.get(taxCode) + row.getRowTaxAmount());
                        lineLevelTaxBasicGST.put(taxCode, (Double) lineLevelTaxBasicGST.get(taxCode) + obj.optDouble(CustomDesignerConstants.SUBTOTAL,0.0) - obj.optDouble(CustomDesignerConstants.IN_Discount,0.0));
                    }
                    /*
                     * putting subtotal+tax
                     */
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax), companyid) + authHandler.round(row.getRowTaxAmount(), companyid)); 
                }
                
                obj.put(CustomDesignerConstants.SpecificCurrencyExchangeRate, 1);
                obj.put("gstCurrencyRate", "");
                summaryData.put("gstCurrencyRate", "");
                //Date transactionDate = invoice.getJournalEntry().getEntryDate();
                 transactionDate = salesOrder.getOrderDate();
                //Date transactionDate=new SimpleDateFormat("dd/MM/yyyy").parse(invoice.getJournalEntry().getEntryDate());
                 obj.put("transactiondate", transactionDate); // Amount
                 summaryData.put("transactiondate", transactionDate); // AmountentryDate
                 double exchangerateunitprice = 0, exchangeratelineitemsubtotal = 0, exchangeratelineitemdiscount = 0, exchangeratelineitemamount = 0, exchangeratelineitemtax = 0, exchangeratelineitemsubtotalwithdiscount = 0;               
                /*<--------------------Base Currency Unit Price,Base currency Subtotal ----------->*/
                /*Exchange Rate Section---- We have not given amount with tax because it is matched with UI*/
                if (externalCurrencyRate != 0) {
                    //Unit Price   
                    exchangerateunitprice = authHandler.round((rate * revExchangeRate), companyid);  //exchanged rate unit rate
                    //SUbTotal (rate*quantity)
                    exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                    //Exhanged Rate Discount
                    exchangeratelineitemdiscount = authHandler.round((rowdiscountvalue * revExchangeRate), companyid);//exchange rate total discount
                    if (row.getSalesOrder().isGstIncluded()) {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round(exchangeratelineitemsubtotal, companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                    } else {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                    }
                    
                    //Exhanged Rate Tax Amount
                    exchangeratelineitemtax = extraCompanyPreferences.isIsNewGST() ? authHandler.round((lineLevelTaxAmountTotal * revExchangeRate), companyid) : authHandler.round((rowTaxAmt * revExchangeRate), companyid);
                    
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);
                    
                    obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);

                    //Total Exchanged Rate (RATE*Quantity)
                    globalLevelExchangedRateSubTotal = globalLevelExchangedRateSubTotal + exchangeratelineitemsubtotal;
                    //Overall Subtotal with Discount
                    globalLevelExchangedRateSubTotalwithDiscount = globalLevelExchangedRateSubTotalwithDiscount + exchangeratelineitemsubtotalwithdiscount;
                    //Overall Total Tax
                    globalLevelExchangedRateTotalTax = globalLevelExchangedRateTotalTax + exchangeratelineitemtax;

                } else {
                    //Unit Price   
                    exchangerateunitprice = authHandler.round(rate, companyid);  //exchanged rate unit rate
                    //SUbTotal (rate*quantity)
                    exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                    //Exhanged Rate Discount
                    exchangeratelineitemdiscount = authHandler.round(rowdiscountvalue, companyid);//exchange rate total discount
                    if (row.getSalesOrder().isGstIncluded()) {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round(exchangeratelineitemsubtotal, companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                    } else {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                    }
                        
                    //Exhanged Rate Tax Amount
                    exchangeratelineitemtax = extraCompanyPreferences.isIsNewGST() ? authHandler.round(lineLevelTaxAmountTotal, companyid) : authHandler.round(rowTaxAmt, companyid);
                    
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);
                    
                    obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);

                    //Total Exchanged Rate (RATE*Quantity)
                    globalLevelExchangedRateSubTotal = globalLevelExchangedRateSubTotal + exchangeratelineitemsubtotal;
                    //Overall Subtotal with Discount
                    globalLevelExchangedRateSubTotalwithDiscount = globalLevelExchangedRateSubTotalwithDiscount + exchangeratelineitemsubtotalwithdiscount;
                    //Overall Total Tax
                    globalLevelExchangedRateTotalTax = globalLevelExchangedRateTotalTax + exchangeratelineitemtax;
                }
                
                
                
                 obj.put(CustomDesignerConstants.Amount,authHandler.formattingDecimalForAmount(rowamountwithtax, companyid)); // Amount of single product

                if (row.getQuotationDetail() != null) {
                      if(CQref.indexOf(row.getQuotationDetail().getQuotation().getquotationNumber())==-1){
                            CQref+=row.getQuotationDetail().getQuotation().getquotationNumber()+", ";
                    }
                } else if (row.getPurchaseorderdetailid() != null) {  //Purchase Order Linked Reference Number
                    KwlReturnObject podetailsitr = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(), row.getPurchaseorderdetailid());
                    PurchaseOrderDetail pod = (PurchaseOrderDetail) podetailsitr.getEntityList().get(0);
                    if (pod != null) {
                        KwlReturnObject poresult = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), pod.getPurchaseOrder().getID());
                        PurchaseOrder po = (PurchaseOrder) poresult.getEntityList().get(0);
                        if (POref.indexOf(po.getPurchaseOrderNumber()) == -1) {
                            POref += po.getPurchaseOrderNumber() + ",";
                            POrefdate += po.getOrderDate() + ",";
                        }
                    }
                } 
                
                
                /*
                 * get custom line data
                 */
                Map<String, Object> variableMap = new HashMap<String, Object>();
                SalesOrderDetailsCustomData jeDetailCustom = (SalesOrderDetailsCustomData) row.getSoDetailCustomData();
                replaceFieldMap = new HashMap<String, String>();
                if (jeDetailCustom != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, jeDetailCustom, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }
                
                /*Product Level Custom Fields Evaluation*/
                KwlReturnObject resultProduct = accountingHandlerDAOobj.getObject(SalesOrderDetailProductCustomData.class.getName(), row.getID());
                SalesOrderDetailProductCustomData qProductDetailCustom = (SalesOrderDetailProductCustomData) resultProduct.getEntityList().get(0);
                replaceFieldMap = new HashMap<String, String>();
                if (qProductDetailCustom != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, qProductDetailCustom, ProductLevelCustomFieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }

                /*
                 * Set All Line level Dimension & All LIne level Custom Field
                 * Values
                 */
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap, variableMap, obj, false);//for dimensions
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap, variableMap, obj, true);//for customfields
                JSONObject jObj = new JSONObject();
                if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                    jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                }
                if (jObj.has("isDisplayUOM") && jObj.get("isDisplayUOM") != null && (Boolean) jObj.get("isDisplayUOM") != false) {
                    obj = accProductObj.getProductDisplayUOM(prod, quantity,row.getBaseuomrate(), false, obj);
                }
                
                jArr.put(obj);
            }
            detailsTableData.put("gsttaxsummary", gstTaxSummaryDetailsTableDataArr);
            detailsTableData.put("isDetailsTableData", true);
            jArr.put(detailsTableData);
            
            if (!StringUtil.isNullOrEmpty(SOID)) {
                List soTermMapList = kwlCommonTablesDAOObj.getSummationOfTermAmtAndTermTaxAmt(Constants.salesordertermmap, SOID);
                if(soTermMapList != null && !soTermMapList.isEmpty()){
                    Iterator termItr = soTermMapList.iterator();
                    while (termItr.hasNext()) {
                        Object[] obj = (Object[]) termItr.next();
                        /* 
                         * [0] : Sum of termamount  
                         * [1] : Sum of termamountinbase 
                         * [2] : Sum of termTaxamount 
                         * [3] : Sum of termTaxamountinbase 
                         * [4] : Sum of termAmountExcludingTax 
                         * [5] : Sum of termAmountExcludingTaxInBase
                         */                     
                        if(obj[2] != null){
                            totaltax += (Double) obj[2];
                        }
                    }
                }
            }
            mainTax = salesOrder.getTax();
            if (mainTax != null) { //Get tax percent
                requestParams.put("transactiondate", entryDate);
                requestParams.put("taxid", mainTax.getID());
                KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                List taxList = result1.getEntityList();
                Object[] taxObj = (Object[]) taxList.get(0);
                taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                mainTaxName = mainTax.getName();
                totalAmount = subtotal - totalDiscount;
                totaltax += (taxPercent == 0 ? 0 : totalAmount * taxPercent / 100);//overall tax calculate
                if (externalCurrencyRate != 0) {
                    globalLevelExchangedRateTotalTax = authHandler.round((totaltax * revExchangeRate), companyid);//exchanged rate total tax amount 
                } else {
                    globalLevelExchangedRateTotalTax = authHandler.round((totaltax), companyid);//exchanged rate total tax amount 
                }
            }
            totalAmount = subtotal + totaltax - totalDiscount;
             /*
             * Invoice Terms Calculation
             */
            //JSONObject summaryData = new JSONObject();
            double totalTermAmount = 0;
            String term = "",termsName="";
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("salesOrder", salesOrder.getID());
            KwlReturnObject salesOrderResult = null;
            
            double lineleveltermTaxAmount = 0;
            Map<String, Object> taxListParams = new HashMap<String, Object>();
            taxListParams.put("companyid", companyid);
            boolean isApplyTaxToTerms=salesOrder.isApplyTaxToTerms();
            
            HashMap<String, Object> filterrequestParams = new HashMap();
            filterrequestParams.put("taxid", salesOrder.getTax()==null?"":salesOrder.getTax().getID());
//            boolean isTaxTermMapped = false;
//            double termAmountBeforeTax = 0;
//            double termAmountAfterTax = 0;
            salesOrderResult = accSalesOrderDAOobj.getSalesOrderTermMap(requestParam);
            List<SalesOrderTermMap> termMap = salesOrderResult.getEntityList();
            for (SalesOrderTermMap salesOrderTermMap : termMap) {
                double termAmnt = 0;
                if(salesOrder.isGstIncluded()){
                    termAmnt = salesOrderTermMap.getTermAmountExcludingTax();
                }else{
                    termAmnt = salesOrderTermMap.getTermamount();
                }
                
                filterrequestParams.put("term", salesOrderTermMap.getTerm()==null?"":salesOrderTermMap.getTerm().getId());
                filterrequestParams.put("companyid", companyid);
                                                
                InvoiceTermsSales mt = salesOrderTermMap.getTerm();
                termsName += mt.getTerm() + " " + salesOrderTermMap.getPercentage() + "%, ";

                totalTermAmount += termAmnt;
                double exchangeratetermamount = 0;
                if (externalCurrencyRate != 0) {
                    exchangeratetermamount = (termAmnt * revExchangeRate);
                } else {
                    exchangeratetermamount = termAmnt;
                }
                globalLevelExchangedRateTermAmount += exchangeratetermamount;
                String termName = (mt.getTerm() + (termAmnt > 0 ? "(+)" : "(-)"));
                summaryData.put(mt.getTerm(), termAmnt);
                summaryData.put(CustomDesignerConstants.BaseCurrency + mt.getTerm(), (termAmnt * revExchangeRate));//Base currency exchange rate term value-ERP-13451
                double tempTermValue = (termAmnt > 0 ? termAmnt : (termAmnt * -1));
                term += "<div> <table><tr><td>" + termName + " : </td><td>" + Double.toString(tempTermValue) + "</td></tr></table></div><br>";
                if (!StringUtil.isNullOrEmpty(String.valueOf(termAmnt))) {
                        String allTermsPlaceholder = CustomDesignerConstants.AllTermsKeyValuePair;
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsLabel, termName);
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsValue, CustomDesignHandler.getAmountinCommaDecimal(termAmnt, amountdigitafterdecimal, countryid));
                        appendtermString.append(allTermsPlaceholder);
                }
            }
            
            if (!StringUtil.isNullOrEmpty(termsName)) {
                termsName = termsName.substring(0, termsName.length() - 2);
            }
            totalAmount = totalAmount + totalTermAmount;            
             globalLevelExchangedRateTermAmount = authHandler.round(globalLevelExchangedRateTermAmount, companyid);
            /* Base Currency Total Amount*/
            globalLevelExchangedRateTotalAmount = globalLevelExchangedRateSubTotalwithDiscount + globalLevelExchangedRateTotalTax + globalLevelExchangedRateTermAmount;
             
             if(!StringUtil.isNullOrEmpty(appendtermString.toString())){
                allTerms=appendtermString.toString();
            }
             
            Date date = new Date();
            String printedOn = df.format(date);
            
            
            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            addrRequestParams.put("customerid", salesOrder.getCustomer().getID());
            addrRequestParams.put("companyid", companyid);
            KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);
            List<AddressDetails> addressResultList = addressResult.getEntityList();
            CommonFunctions.getAddressSummaryData(addressResultList, summaryData, companyAccountPreferences, extraCompanyPreferences);
            
            billAddr = CommonFunctions.getTotalBillingShippingAddress(salesOrder.getBillingShippingAddresses(), true);
            shipAddr=CommonFunctions.getTotalBillingShippingAddress(salesOrder.getBillingShippingAddresses(), false);
            /*
             * All Global Section Custom Field and DImensions
             */
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put("billid", salesOrder.getID());
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            if(extraCompanyPreferences.isIsNewGST()){ // for New gst check 
                    if (!lineLevelTaxNames.isEmpty()) {
                        Iterator lineTax = lineLevelTaxNames.entrySet().iterator();
                        while (lineTax.hasNext()) {
                            Map.Entry tax = (Map.Entry) lineTax.next();
                            allLineLevelTax += tax.getKey();
                            allLineLevelTax += "!## ";
                            double taxamount = (double) tax.getValue();
                            allLineLevelTaxAmount += tax.getValue().toString();
                            TotalLineLevelTaxAmount += taxamount;
                            allLineLevelTaxAmount += "!## ";
                            if (taxamount > 0) {
                                lineLevelTaxSign += "+";
                                lineLevelTaxSign += "!## ";
                            } else {
                                lineLevelTaxSign += "-&nbsp;";
                                lineLevelTaxSign += "!## ";
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(allLineLevelTax)) {
                        allLineLevelTax = allLineLevelTax.substring(0, allLineLevelTax.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(allLineLevelTaxAmount)) {
                        allLineLevelTaxAmount = allLineLevelTaxAmount.substring(0, allLineLevelTaxAmount.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxSign)) {
                        lineLevelTaxSign = lineLevelTaxSign.substring(0, lineLevelTaxSign.length() - 4);
                    }
                
                totalAmount = totalAmount + TotalLineLevelTaxAmount;
                totaltax = TotalLineLevelTaxAmount;
            } else {
                /*
                * Putting all line taxes and its information in summary data separated by !##
                */
                for (String key : lineLevelTaxesGST) {
                    allLineLevelTax += key + "!##";
                    allLineLevelTaxAmount += lineLevelTaxAmountGST.get(key).toString() + "!##";
                    allLineLevelTaxBasic += lineLevelTaxBasicGST.get(key).toString() + "!##";
                }
            }
            /**
             * SDP-13772
             * Add Rounding Adjustment in Total Amount
             */
            if(salesOrder.isIsRoundingAdjustmentApplied()){
                totalAmount = totalAmount + salesOrder.getRoundingadjustmentamount();
            }
            returnvalues.clear();
            //global level dimensionfields
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 0);
            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globalleveldimensions = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            extraparams.put("approvestatuslevel", salesOrder.getApprovestatuslevel());
            //Details like company details,base currency
            CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);
            String userId = requestObj.optString(Constants.useridKey);
            KwlReturnObject userresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) userresult.getEntityList().get(0);
            
            if(countryid == Constants.indian_country_id) { // For India Country 
                String buyerexcRegNo="", buyetinNo="", buyerpanNo="", buyerRange="", buyerDivision="", buyerComrate="", buyerServiceTaxRegNo="";
                
                // Customer Related indian details
                String panStatus = "", IECNo = "", CSTDateStr = "", VATDateStr = "", dealerTypeStr = "";
                String ImporterECCNo = "",  typeOfSales = "";

                if (salesOrder.getCustomer().getPanStatus() != null && !StringUtil.isNullOrEmpty(salesOrder.getCustomer().getPanStatus())) {
                    panStatus = salesOrder.getCustomer().getPanStatus().equals(IndiaComplianceConstants.PAN_STATUS_PANNOTAVBL) ? IndiaComplianceConstants.PAN_NOT_AVAILABLE : salesOrder.getCustomer().getPanStatus().equals(IndiaComplianceConstants.PAN_STATUS_APPLIEDFOR) ? IndiaComplianceConstants.PAN_APPLIED_FOR : "";
                }
                if(salesOrder.getCustomer().getDefaultnatureOfPurchase()!= null && !StringUtil.isNullOrEmpty(salesOrder.getCustomer().getDefaultnatureOfPurchase())){
                    KwlReturnObject naturetyperesult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), salesOrder.getCustomer().getDefaultnatureOfPurchase());
                    MasterItem natureTypeObj = (MasterItem) naturetyperesult.getEntityList().get(0);
                    if(natureTypeObj != null){
                        typeOfSales = natureTypeObj.getValue();
                    }
                }

                if (salesOrder.getCustomer().getDealertype() != null && !StringUtil.isNullOrEmpty(salesOrder.getCustomer().getDealertype())) {
                    String dealerType = salesOrder.getCustomer().getDealertype();
                    if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_REGISTERED)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_REGISTERED_STR;
                    } else if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_UNREGISTERED)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_UNREGISTERED_STR;
                    } else if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_3_3A_4)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_3_3A_4_STR;
                    } else if (dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_1_2)) {
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_1_2_STR;
                    }
                }
                if (salesOrder.getCustomer().getCSTRegDate() != null) {
                    Date CSTDate = salesOrder.getCustomer().getCSTRegDate();
                    CSTDateStr = CSTDate != null ? authHandler.getUserDateFormatterJson(requestObj).format(CSTDate) : "";
                }
                if (salesOrder.getCustomer().getVatregdate() != null) {
                    Date VATDate = salesOrder.getCustomer().getVatregdate();
                    VATDateStr = VATDate != null ? authHandler.getUserDateFormatterJson(requestObj).format(VATDate) : "";
                }

                buyerComrate = salesOrder.getCustomer().getCommissionerate() != null ? salesOrder.getCustomer().getCommissionerate() : "";
                buyerDivision = salesOrder.getCustomer().getDivision() != null ? salesOrder.getCustomer().getDivision() : "";
                buyerRange = salesOrder.getCustomer().getRangecode() != null ? salesOrder.getCustomer().getRangecode() : "";
                buyetinNo = salesOrder.getCustomer().getVATTINnumber() != null ? salesOrder.getCustomer().getVATTINnumber() : "";
                buyerexcRegNo = salesOrder.getCustomer().getECCnumber() != null ? salesOrder.getCustomer().getECCnumber() : "";
                buyerpanNo = salesOrder.getCustomer().getPANnumber() != null ? salesOrder.getCustomer().getPANnumber() : "";
                buyerServiceTaxRegNo = salesOrder.getCustomer().getSERVICEnumber() != null ? salesOrder.getCustomer().getSERVICEnumber() : "";
                ImporterECCNo = (salesOrder.getCustomer() != null && salesOrder.getCustomer().getImporterECCNo() != null) ? salesOrder.getCustomer().getImporterECCNo() : "";
                IECNo = (salesOrder.getCustomer() != null && salesOrder.getCustomer().getIECNo() != null) ? salesOrder.getCustomer().getIECNo() : "";

                // ************************   Customer Related Information **********************************************
                summaryData.put(CustomDesignerConstants.CUSTOMER_PAN_NO, buyerpanNo);
                summaryData.put(CustomDesignerConstants.CustomerVendor_PAN_STATAUS, panStatus);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, buyetinNo);
                summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, salesOrder.getCustomer().getCSTTINnumber() != null ? salesOrder.getCustomer().getCSTTINnumber() : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_CST_REG_DATE, CSTDateStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_REG_DATE, VATDateStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_DEALER_TYPE, dealerTypeStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_INTERSTATEPARTY, salesOrder.getCustomer().isInterstateparty() ? "Yes" : "No");
                summaryData.put(CustomDesignerConstants.CustomerVendor_C_FORM_APPLICABLE, salesOrder.getCustomer().isCformapplicable() ? "Yes" : "No");
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_TYPE_OF_SALES, typeOfSales);
                summaryData.put(CustomDesignerConstants.CustomerVendor_ECC_NO, buyerexcRegNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_IMPORTER_ECC_NUMBER, ImporterECCNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_IEC_NUMBER, IECNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_RANGE_CODE, buyerRange);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_DIVISION_CODE, buyerDivision);
                summaryData.put(CustomDesignerConstants.CUSTOMER_EXCISE_COMMISSIONERATE_CODE, buyerComrate);
                summaryData.put(CustomDesignerConstants.CUSTOMER_SERVICE_TAX_REG_NO, buyerServiceTaxRegNo);
                summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, salesOrder.getCustomer().getGSTIN() != null ? salesOrder.getCustomer().getGSTIN() : "");
                // ****************************************************************************************************
            }
            String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAmount)), currency,countryLanguageId);
            /*
             * Get amount in indonesian words.
             */
            String indonesianAmountInWords = "";
            if(countryid == Constants.INDONESIAN_COUNTRY_ID){
                KWLCurrency indoCurrency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), Constants.CountryIndonesianCurrencyId);
                indonesianAmountInWords = IndonesianNumberToWordsOjb.indonesiaConvert(Double.parseDouble(String.valueOf(totalAmount)), indoCurrency);
            }
            
            String soStatus = "Closed";
            double count = accSalesOrderDAOobj.getSOStatusOnBalanceQty(salesOrder.getID(), companyid);
            if (count > 0) {
                soStatus = "Open";
            }
            summaryData.put(CustomDesignerConstants.SO_Status, soStatus.equalsIgnoreCase("Closed") ? "Closed" : (salesOrder.isIsSOClosed() ? "Closed" : "Open"));
            
            String gstAmountInWords = EnglishNumberToWordsOjb.convert(totaltax, currency, countryLanguageId);
            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, authHandler.formattedAmount(authHandler.round(totalAmount, companyid), companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, authHandler.formattedAmount(subtotal, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, authHandler.formattedAmount((subtotal-totalDiscount), companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, authHandler.formattedAmount(((subtotal - totalDiscount) + totaltax), companyid)); //ERP-25162
            summaryData.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, authHandler.formattedAmount(totalDiscount, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, authHandler.formattedAmount(totaltax, companyid));
            summaryData.put(CustomDesignerConstants.SummaryTaxPercent, taxPercent);
            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword+" Only.");
            summaryData.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, indonesianAmountInWords);
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, config == null ? "" : config.getPdfPostText());
            summaryData.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId,CQref.equals("")?"":CQref.substring(0, CQref.length()-2) );
            summaryData.put(CustomDesignerConstants.CustomerVendor_Term, salesOrder.getCustomer().getCreditTerm() != null ? (salesOrder.getCustomer().getCreditTerm().getTermname() != null ? salesOrder.getCustomer().getCreditTerm().getTermname() : "") : "");
            summaryData.put(CustomDesignerConstants.CustomerVendor_AccountCode, salesOrder.getCustomer().getAccount() != null ? (salesOrder.getCustomer().getAccount().getAcccode() != null ? salesOrder.getCustomer().getAccount().getAcccode() : "") : "");
            summaryData.put(CustomDesignerConstants.CustomerVendor_Code, salesOrder.getCustomer().getAcccode() != null ? salesOrder.getCustomer().getAcccode() : "");
            summaryData.put(CustomDesignerConstants.BillTo, billAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.ShipTo, shipAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.NoOfDays, noofdays == 1 ? noofdays + " Day" : noofdays + " Days");
            summaryData.put(CustomDesignerConstants.Createdby,createdby);
            summaryData.put(CustomDesignerConstants.Updatedby,updatedby);
            
            /*Exchanged Rate Overall*/
            summaryData.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, revExchangeRate);
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTotalAmount_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateTotalAmount, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotal_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateSubTotal, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTotalTax_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateTotalTax, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotalwithDiscount_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateSubTotalwithDiscount, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTermAmount_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateTermAmount, companyid));
            
            
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyExchangeRate, 1);
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyAmount, authHandler.formattedAmount(globalLevelExchangedRateTotalAmount, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotal, authHandler.formattedAmount(globalLevelExchangedRateSubTotal, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyTaxAmount, authHandler.formattedAmount(globalLevelExchangedRateTotalTax, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotalWithDicount, authHandler.formattedAmount(globalLevelExchangedRateSubTotalwithDiscount, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyTermAmount, authHandler.formattedAmount(globalLevelExchangedRateTermAmount, companyid));
            
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTermsDescription_fieldTypeId, term);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTermsName_fieldTypeId, termsName);
            summaryData.put(CustomDesignerConstants.CustomDesignSummaryTermsValue_fieldTypeId, totalTermAmount);
            summaryData.put(CustomDesignerConstants.TotalQuantity, totalQuantity);
            summaryData.put(CustomDesignerConstants.Total_Quantity_UOM, authHandler.formattingDecimalForQuantity(totalQuantity, companyid) +" "+ uomForTotalQuantity);
            summaryData.put(CustomDesignerConstants.CustomDesignTemplate_Print, printedOn);
            summaryData.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId,POref.equals("")?"":POref.substring(0, POref.length()-1));  
            summaryData.put(CustomDesignerConstants.CSUTOMDESIGNER_PO_REF_DATE,POrefdate.equals("")?"":POrefdate.substring(0, POrefdate.length()-1));  
            summaryData.put(CustomDesignerConstants.CustomDesignTemplate_ItemsNo,rowcnt);  
            summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
            summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
            summaryData.put(CustomDesignerConstants.CustomDesignCreditTerm_fieldTypeId, salesOrder.getTerm() != null ? salesOrder.getTerm().getTermdays() + " Days" : "0 Day");
            summaryData.put(CustomDesignerConstants.AllTerms, allTerms);
            summaryData.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, authHandler.formattedAmount(((subtotal-totalDiscount) + totalTermAmount), companyid));
            summaryData.put(CustomDesignerConstants.CurrentUserFirstName, !StringUtil.isNullOrEmpty(user.getFirstName()) ? user.getFirstName() : "");
            summaryData.put(CustomDesignerConstants.CurrentUserLastName, !StringUtil.isNullOrEmpty(user.getLastName()) ? user.getLastName() : "");
            summaryData.put(CustomDesignerConstants.CurrentUserFullName, !StringUtil.isNullOrEmpty(user.getFullName()) ? user.getFullName() : "");
            summaryData.put(CustomDesignerConstants.CurrentUserEmail, !StringUtil.isNullOrEmpty(user.getEmailID()) ? user.getEmailID() : "");
            summaryData.put(CustomDesignerConstants.CurrentUserAddress, !StringUtil.isNullOrEmpty(user.getAddress()) ? user.getAddress().replaceAll(Constants.REGEX_LINE_BREAK, "<br>") : "");
            summaryData.put(CustomDesignerConstants.CurrentUserContactNumber, !StringUtil.isNullOrEmpty(user.getContactNumber()) ? user.getContactNumber() : "");
            summaryData.put(CustomDesignerConstants.CUSTOMER_TITLE, customerTitle);
//            if(countryid == Constants.indian_country_id) {
            summaryData.put(CustomDesignerConstants.AllLineLevelTax, allLineLevelTax);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxAmount, allLineLevelTaxAmount);
            summaryData.put(CustomDesignerConstants.AllLineLevelTermSigns, lineLevelTaxSign);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxBasic, allLineLevelTaxBasic);
            summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, salesOrder.getCustomer().getVATTINnumber()!= null ? salesOrder.getCustomer().getVATTINnumber() : "");
            summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, salesOrder.getCustomer().getCSTTINnumber()!= null ? salesOrder.getCustomer().getCSTTINnumber() : "");
            summaryData.put(CustomDesignerConstants.Company_VAT_TIN_NO, extraCompanyPreferences.getVatNumber()!= null ? extraCompanyPreferences.getVatNumber() : "");
            summaryData.put(CustomDesignerConstants.Company_CST_TIN_NO, extraCompanyPreferences.getCstNumber()!= null ? extraCompanyPreferences.getCstNumber() : "");
            int deliveryDate = 0;
            String deliveryDateVal = "";
            String driver = "";
            String vehicleNo = "";
            String deliveryTime = "";
            if (extraCompanyPreferences.isDeliveryPlanner()) {
                deliveryDate = salesOrder.getCustomer() != null ? salesOrder.getCustomer().getDeliveryDate():-1;
                deliveryDateVal = getDeliverDayVal(deliveryDate);
                deliveryTime = (salesOrder.getCustomer() != null && salesOrder.getCustomer().getDeliveryTime() != null) ? salesOrder.getCustomer().getDeliveryTime():"";
                driver = (salesOrder.getCustomer() != null && salesOrder.getCustomer().getDriver() != null)? salesOrder.getCustomer().getDriver().getValue():"";
                vehicleNo = (salesOrder.getCustomer() != null && salesOrder.getCustomer().getVehicleNo() != null)? salesOrder.getCustomer().getVehicleNo().getValue():""; 
            }
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDeliveryDate, deliveryDateVal);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDeliveryTime, deliveryTime);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerDriver, driver);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerVehicleNo,vehicleNo);
            
            //Consignment Request module fields
            summaryData.put(CustomDesignerConstants.CustomDesignConsignmentRequestType, salesOrder.getMovementType() != null ? salesOrder.getMovementType().getValue() : "");
            summaryData.put(CustomDesignerConstants.GSTAmountInWords, gstAmountInWords + Constants.ONLY);
//            }

            jArr.put(summaryData);
        } catch(Exception ex ) {
              Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }    
       
    /**
     * Get DetailJSON For Job Order Flow in document designer
     * @return HashMap of string id as key and JSONArray as value
     * @Author Ashish Mohite
     */
    @Override
    public HashMap<String, JSONArray> getSODetailsJobOrderFlowItemJSON(JSONObject requestObj, String SOID, HashMap<String, Object> paramMap) {
        HashMap<String, JSONArray>  itemDataSO = new HashMap<String, JSONArray>();
        try {
            HashMap<String, Integer> FieldMap = (HashMap<String, Integer>) paramMap.get(Constants.fieldMap);
            HashMap<String, String> replaceFieldMap = (HashMap<String, String>) paramMap.get(Constants.replaceFieldMap);
            HashMap<String, Integer> DimensionFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.dimensionFieldMap);
            HashMap<String, Integer> LineLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.lineLevelCustomFieldMap);
            HashMap<String, Integer> ProductLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.productLevelCustomFieldMap);
            
            String companyid = requestObj.optString(Constants.companyKey);
            JSONArray recordids = new JSONArray();
            String createdby = "", customerName = "";
            String templateSubType = requestObj.optString("templatesubtype", "0");
            
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), SOID);
            SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
            
            createdby = salesOrder.getCreatedby().getFullName();
            customerName = salesOrder.getCustomer().getName();
            
            for(SalesOrderDetail soDetailsRow : salesOrder.getRows()){
                if(soDetailsRow.isJobOrderItem()){
                    DateFormat df = authHandler.getDateOnlyFormat();
                    JSONArray jArr = new JSONArray();
                    JSONObject summaryData = new JSONObject();

                    summaryData.put("summarydata", true);
                    // Product level data
                    summaryData.put(CustomDesignerConstants.IN_ProductCode, soDetailsRow.getProduct().getProductid());
                    summaryData.put(CustomDesignerConstants.ProductName, soDetailsRow.getProduct().getProductName());
                    summaryData.put(CustomDesignerConstants.ProductDescription, soDetailsRow.getDescription());
                    String baseUrl = com.krawler.common.util.URLUtil.getDomainURL(requestObj.optString("cdomain"), false);
                    /*
                    * Following code is to check whether the image is predent for product or not. 
                    * If Image is not present sent s.gif instead of product id
                    */
                    String fileName = null;
                    fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages+soDetailsRow.getProduct().getID() + ".png";
                    File file = new File(fileName);
                    FileInputStream in = null; 
                    String filePathString = "";
                    try {
                        in = new FileInputStream(file);
                    } catch(java.io.FileNotFoundException ex) {
                        //catched exception if file not found, and to continue for rest of the products
                    filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                    }
                    if(in != null){
                        filePathString = baseUrl + "productimage?fname=" + soDetailsRow.getProduct().getID() + ".png&isDocumentDesignerPrint=true";
                    } else{
                        filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                    }
                    summaryData.put(CustomDesignerConstants.imageTag, "<img src=\""+filePathString+"\" width='100' height='100' />");
                    // Global level data
                    summaryData.put(CustomDesignerConstants.Createdby, createdby);
                    summaryData.put(CustomDesignerConstants.CUSTOMER_NAME, customerName);
                    summaryData.put(CustomDesignerConstants.SALES_ORDER_NO, salesOrder.getSalesOrderNumber());
                    summaryData.put(CustomDesignerConstants.SalesOrderDate, df.format(salesOrder.getOrderDate()));
                    summaryData.put(CustomDesignerConstants.SHIP_DATE, salesOrder.getShipdate() != null ? df.format(salesOrder.getShipdate()) : "");

                    /*
                    * All Global Section Custom Field and Dimensions
                    */
                    HashMap<String, Object> returnvalues = new HashMap<String, Object>();
                    HashMap<String, Object> extraparams = new HashMap<String, Object>();
//                    df = authHandler.getUserDateFormatter(request);//User Date Formatter
                    extraparams.put(Constants.companyid, companyid);
                    extraparams.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                    extraparams.put(Constants.customcolumn, 0);
                    extraparams.put(Constants.customfield, 1);
                    extraparams.put(CustomDesignerConstants.isCustomfield, "true");
                    extraparams.put("billid", salesOrder.getID());
                    returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
                    if (returnvalues.containsKey("summaryData")) {
                        summaryData = (JSONObject) returnvalues.get("summaryData");
                    }
                    //global level dimensionfields
                    extraparams.put(Constants.customcolumn, 0);
                    extraparams.put(Constants.customfield, 0);
                    extraparams.put(CustomDesignerConstants.isCustomfield, "false");
                    returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
                    if (returnvalues.containsKey("summaryData")) {
                        summaryData = (JSONObject) returnvalues.get("summaryData");
                    }

                    /*
                    * get custom line data
                    */
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    SalesOrderDetailsCustomData jeDetailCustom = (SalesOrderDetailsCustomData) soDetailsRow.getSoDetailCustomData();
                    replaceFieldMap = new HashMap<String, String>();
                    if (jeDetailCustom != null) {
                        ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, jeDetailCustom, FieldMap, summaryData, kwlCommonTablesDAOObj, variableMap);
                    }

                    /*Product Level Custom Fields Evaluation*/
                    KwlReturnObject resultProduct = accountingHandlerDAOobj.getObject(SalesOrderDetailProductCustomData.class.getName(), soDetailsRow.getID());
                    SalesOrderDetailProductCustomData qProductDetailCustom = (SalesOrderDetailProductCustomData) resultProduct.getEntityList().get(0);
                    replaceFieldMap = new HashMap<String, String>();
                    if (qProductDetailCustom != null) {
                        ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, qProductDetailCustom, ProductLevelCustomFieldMap, summaryData, kwlCommonTablesDAOObj, variableMap);
                    }

                    /*
                    * Set All Line level Dimension & All LIne level Custom Field
                    * Values
                    */
                    summaryData = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap, variableMap, summaryData, false);//for dimensions
                    summaryData = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap, variableMap, summaryData, true);//for customfields

                    if(jeDetailCustom != null && templateSubType.equals(Constants.SUBTYPE_JOB_ORDER_LABEL)){ // for Job Order Label Flow
                        if(FieldMap.containsKey("Custom_Size")){
                            String sizeValue = jeDetailCustom.getCol(FieldMap.get("Custom_Size"));
                            String[] sizeArr = sizeValue.split("\\n");
                            for(int ind = 0; ind < sizeArr.length; ind++){
                                jArr = new JSONArray();
                                String size = sizeArr[ind];
                                JSONObject newSummaryData = new JSONObject(summaryData.toString());

                                newSummaryData.put("col"+FieldMap.get("Custom_Size"), size);
                                newSummaryData.put("Custom_Size", size);

                                jArr.put(newSummaryData);
                                itemDataSO.put(SOID+"_"+soDetailsRow.getSrno()+""+ind, jArr);
                                recordids.put(SOID+"_"+soDetailsRow.getSrno()+""+ind);
                            }
                        } else{
                            jArr.put(summaryData);
                            itemDataSO.put(SOID+"_"+soDetailsRow.getSrno(), jArr);
                            recordids.put(SOID+"_"+soDetailsRow.getSrno());
                        }
                    } else{ // for Job Order Flow
                        jArr.put(summaryData);
                        itemDataSO.put(SOID+"_"+soDetailsRow.getSrno(), jArr);
                        recordids.put(SOID+"_"+soDetailsRow.getSrno());
                    }
                    //document currency
                    if (salesOrder != null && salesOrder.getCurrency() != null && !StringUtil.isNullOrEmpty(salesOrder.getCurrency().getCurrencyID())) {
                        summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, salesOrder.getCurrency().getCurrencyID());
                    }
                    
                    itemDataSO.put("recordids", recordids);
                }
            }
            
        } catch(Exception ex ) {
                Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return itemDataSO;
    }

    // This Function is not used except in test package
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
        if (!fromCreate) {
            String thisUser = currentUser;
            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
            User user = (User) userclass.getEntityList().get(0);

            if (AccountingManager.isCompanyAdmin(user)) {
                hasAuthority = true;
            } else {
                hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(soApproveMap);
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
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                HashMap<String, Object> recMap = new HashMap();
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
                } else if(appliedUpon ==Constants.Specific_Products_Category){
                     /*
                     * Check If Rule is apply on product
                     * category from multiapproverule window
                     */
                    sendForApproval = accountingHandlerDAOobj.checkForProductCategoryForProduct(productDiscountMapList, appliedUpon, rule);
                }else if(appliedUpon ==Constants.SO_CREDIT_LIMIT){
                     /*
                     * Check If Rule is apply on SO Credit limit
                     * category from multiapproverule window ERM-396
                     */
                    sendForApproval = true;
                }else {
                    rule = rule.replaceAll("[$$]+", amount);
                }
                if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && appliedUpon != Constants.Specific_Products && appliedUpon != Constants.Specific_Products_Discount && appliedUpon !=Constants.Specific_Products_Category && Boolean.parseBoolean(engine.eval(rule).toString())) || sendForApproval) {
                    // send emails
                    boolean hasApprover = Boolean.parseBoolean(row[3].toString());
                    if (isMailApplicable) {
                        sendMailToApprover(companyid, row[0].toString(), prNumber, fromName, hasApprover, Constants.Acc_Sales_Order_ModuleId, soObj.getCreatedby().getUserID());
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
    
    public void sendMailToApprover(String companyid, String ruleId, String prNumber, String fromName, boolean hasApprover, int moduleid, String createdby) throws ServiceException {
        KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
        String transactionName = "";
        String transactionNo = "";
        switch (moduleid) {
            case Constants.Acc_Sales_Order_ModuleId:
                transactionName = "Sales Order";
                transactionNo = "Sales Order Number";
                break;
            case Constants.Acc_Customer_Quotation_ModuleId:
                transactionName = "Customer Quotation";
                transactionNo = "Customer Quotation Number";
                break;
        }
        String requisitionApprovalSubject = transactionName + ": %s - Approval Notification";
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
                + "<p>%s has created " + transactionName + " %S and sent it to you for approval.</p>"
                + "<p>Please review and approve it (" + transactionNo + ": %s).</p>"
                + "<p></p>"
                + "<p>Thanks</p>"
                + "<p>This is an auto generated email. Do not reply<br>";
        String requisitionApprovalPlainMsg = "Hi All,\n\n"
                + "%s has created " + transactionName + " %S and sent it to you for approval.\n"
                + "Please review and approve it (" + transactionNo + ": %s).\n\n"
                + "Thanks\n\n"
                + "This is an auto generated email. Do not reply\n";
        try {
            if (hasApprover && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) returnObject.getEntityList().get(0);
                String fromEmailId = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                String subject = String.format(requisitionApprovalSubject, prNumber);
                String htmlMsg = String.format(requisitionApprovalHtmlMsg, fromName, prNumber, prNumber, prNumber);
                String plainMsg = String.format(requisitionApprovalPlainMsg, fromName, prNumber, prNumber, prNumber);
                ArrayList<String> emailArray = new ArrayList<String>();
                String[] emails = {};
                String userDepartment = null;
                KwlReturnObject returnObjectRes = null;

                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                dataMap.put("ruleid", ruleId);
                dataMap.put("companyid", companyid);
                dataMap.put("checkdeptwiseapprover", true);

                KwlReturnObject userResult1 = accMultiLevelApprovalDAOObj.checkDepartmentWiseApprover(dataMap);
                if (userResult1 != null && userResult1.getEntityList() != null && userResult1.getEntityList().size() > 0) {
                    User user = null;
                    if (!StringUtil.isNullObject(createdby)) {
                        returnObjectRes = accountingHandlerDAOobj.getObject(User.class.getName(), createdby);
                        user = (User) returnObjectRes.getEntityList().get(0);
                    }
                    if (user != null && !StringUtil.isNullObject(user.getDepartment())) {
                        userDepartment = user.getDepartment();
                        dataMap.put("userdepartment", userDepartment);
                    }
                }

                KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(dataMap);

                if (userResult.getEntityList() != null && userResult.getEntityList().size() <= 0 && !StringUtil.isNullOrEmpty(userDepartment)) {
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
    public JSONObject getDailySalesReportByCustomer(HttpServletRequest request, Map<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        Locale requestcontextutilsobj = null;
        try {

            int count = 0;//result.getRecordTotalCount();

            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }
//            flag for chart type
            String chartType = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.CHART_TYPE))) {
                chartType = request.getParameter(Constants.CHART_TYPE);
            }
            JSONArray DataJArr = new JSONArray();

            boolean isForExport = false;
            if (requestParams.containsKey("isForExport") && !StringUtil.isNullOrEmpty(requestParams.get("isForExport").toString())) {
                isForExport = Boolean.parseBoolean(requestParams.get("isForExport").toString());
            }
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            HashMap invRequestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            
            DateFormat df = authHandler.getDateOnlyFormat();
            Date startDate = df.parse(request.getParameter("stdate"));
            Date endDate = df.parse(request.getParameter("enddate"));
            
            /*
             *Calculation of Date Ranges for the Month.
             */
            LocalDate localStartDate = new LocalDate(startDate);
            LocalDate localEndDate = new LocalDate(endDate);
            
            startDate = localStartDate.toDateTimeAtCurrentTime().dayOfMonth().withMinimumValue().toDate();
            endDate = localEndDate.toDateTimeAtCurrentTime().dayOfMonth().withMaximumValue().toDate();
            
            startDate.setHours(00);
            startDate.setMinutes(00);
            startDate.setSeconds(00);

            endDate.setHours(23);
            endDate.setMinutes(59);
            endDate.setSeconds(59);

            int startDayOfMonth = startDate.getDate();
            int endDayOfMonth = endDate.getDate();
            String monthName = localStartDate.toString("MMM");
            int selectedYear = 1900 + startDate.getYear();

            invRequestParams.put(Constants.REQ_startdate, df.format(startDate));
            invRequestParams.put(Constants.REQ_enddate, df.format(endDate));
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                invRequestParams.put("companyid", companyid);
                invRequestParams.put("gcurrencyid", gcurrencyid);

                KwlReturnObject result =accSalesOrderDAOobj.getSalesOrdersMerged(invRequestParams);
                List list = result.getEntityList();
                DataJArr = getSalesOrderForDailySalesReport(request, list, DataJArr);
            }

            KwlReturnObject currencyResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), gcurrencyid);
            KWLCurrency currency = (KWLCurrency) currencyResult.getEntityList().get(0);

            // Data Structures required for Column Model
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject jobjTemp = new JSONObject();

            /*
             * Created Record to print the daily sales report.
             */
            String StoreRec = "customerid,customername,";

            /*
             * Created Headers with Respect to Month.
             */
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "");
            jobjTemp.put("dataIndex", "customerid");    //"Customer ID"
            jobjTemp.put("hidden", true);
            jobjTemp.put("hideable", false);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.dailySalesReport.customername", null, requestcontextutilsobj) + "<b>"); //"Customer Name"
            jobjTemp.put("dataIndex", "customername");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            DecimalFormat decimalFormat = new DecimalFormat("00");
            List dayList = new ArrayList<>();

            for (int i = startDayOfMonth; i <= endDayOfMonth; i++) {
                /*
                 *Added dataindex in record
                 */
                StoreRec += "amount_" + i + ",";

                String dateStr = decimalFormat.format(i) + " " + monthName + " " + selectedYear;
                /*
                 * Added dataindex in Header
                 */
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "<b>" + dateStr + "<b>");
                jobjTemp.put("dataIndex", "amount_" + i);
                jobjTemp.put("renderer", "WtfGlobal.currencyRenderer");
                jobjTemp.put("align", "right");
                jobjTemp.put("width", 100);
                jobjTemp.put("pdfwidth", 100);
                jarrColumns.put(jobjTemp);

                dayList.add(dateStr);
            }

            StoreRec += "total";
            dayList.add("Total");

            /*
             * Adding dataindex into the record.
             */
            String[] recArr = StoreRec.split(",");
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.dailySalesReport.total", null, requestcontextutilsobj) + "<b>"); //"Total"
            jobjTemp.put("dataIndex", "total");
            jobjTemp.put("renderer", "WtfGlobal.currencyRenderer");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 120);
            jobjTemp.put("pdfwidth", 120);
            jarrColumns.put(jobjTemp);


            Map customerMap = new HashMap();
            JSONObject jSONObjectTotal = new JSONObject();
            for (int invIndex = 0; invIndex < DataJArr.length(); invIndex++) {
                JSONObject salesOrder = DataJArr.getJSONObject(invIndex);
                String customerid = salesOrder.getString("personid");
                Double salesOrderAmount = salesOrder.getDouble("amountinbase");
                LocalDate salesOrderDate = new LocalDate(new Date(salesOrder.getString("date")));
                String salesOrderDay = salesOrderDate.toString("dd MMM YYYY");
                /*
                 * Checking whether the Customer of salesOrder is already present
                 * in the Map or Not, If yes then put the salesOrder Amount with
                 * the Date in Key in the Respective Date of Map.
                 */
                if (customerMap.containsKey(customerid)) {
                    Map dailySalesMap = (HashMap) customerMap.get(customerid);
                    if (dailySalesMap == null) {
                        dailySalesMap = new HashMap();
                        dailySalesMap.put(salesOrderDay, salesOrderAmount);
                    } else {
                        Double monthlySalesAmount = (Double) dailySalesMap.get(salesOrderDay);
                        if (monthlySalesAmount == null) {
                            dailySalesMap.put(salesOrderDay, salesOrderAmount);
                        } else {
                            double amount = monthlySalesAmount.doubleValue();
                            amount += salesOrderAmount.doubleValue();
                            dailySalesMap.put(salesOrderDay, new Double(amount));
                        }
                    }
                    /*
                     * Adding total column for Export case
                     */
                    if (isForExport) {
                        Map monthlySalesTotalMap = new HashMap();
                        if (customerMap.containsKey("Total")) {
                            monthlySalesTotalMap = (HashMap) customerMap.get("Total");
                            if (monthlySalesTotalMap.containsKey(salesOrderDay)) {
                                double totalAmount = (Double) monthlySalesTotalMap.get(salesOrderDay);
                                totalAmount += salesOrderAmount;
                                monthlySalesTotalMap.put(salesOrderDay, totalAmount);
                            } else {
                                monthlySalesTotalMap.put(salesOrderDay, salesOrderAmount);
                            }
                            customerMap.put("Total", monthlySalesTotalMap);
                        } else {
                            monthlySalesTotalMap = new HashMap();
                            monthlySalesTotalMap.put(salesOrderDay, salesOrderAmount);
                            customerMap.put("Total", monthlySalesTotalMap);
                        }
                    }
                } else {
                    /*
                     * If Customer is Not Present in the Existing Map then Add
                     * NEW record in the Map for Respective Customer.
                     */
                    Map dailySalesMap = new HashMap();
                    dailySalesMap.put(salesOrderDay, salesOrderAmount);
                    customerMap.put(customerid, dailySalesMap);

                    /*
                     * Adding total column for Export case
                     */
                    if (isForExport) {
                        Map monthlySalesTotalMap = new HashMap();
                        if (customerMap.containsKey("Total")) {
                            monthlySalesTotalMap = (HashMap) customerMap.get("Total");
                            if (monthlySalesTotalMap.containsKey(salesOrderDay)) {
                                double totalAmount = (Double) monthlySalesTotalMap.get(salesOrderDay);
                                totalAmount += salesOrderAmount;
                                monthlySalesTotalMap.put(salesOrderDay, totalAmount);
                            } else {
                                monthlySalesTotalMap.put(salesOrderDay, salesOrderAmount);
                            }
                            customerMap.put("Total", monthlySalesTotalMap);
                        } else {
                            monthlySalesTotalMap = new HashMap();
                            monthlySalesTotalMap.put(salesOrderDay, salesOrderAmount);
                            customerMap.put("Total", monthlySalesTotalMap);
                        }
                    }
                }
            }

            Set customerSet = customerMap.keySet();
            int size = dayList.size();
            double[] monthlyTotal = new double[size];
            double grandTotal = 0.0;
            for (Object object : customerSet) {
                String customerid = object.toString();
                Map dailySalesMap = (HashMap) customerMap.get(customerid);
                JSONObject obj = new JSONObject();
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerid);
                Customer customer = (Customer) objItr.getEntityList().get(0);
                obj.put("customerid", customerid);
                obj.put("customername", customer != null ? customer.getName() : "");
                double totalSalesAmount = 0.00;

                for (int i = startDayOfMonth; i <= endDayOfMonth; i++) {
                    String salesOrderDay = (String) dayList.get(i - 1);
                    if (dailySalesMap.containsKey(salesOrderDay)) {
                        double monthlySalesAmount = ((Double) dailySalesMap.get(salesOrderDay)).doubleValue();
                        totalSalesAmount += authHandler.round(monthlySalesAmount, companyid);
                        if (isForExport) {
                            obj.put("amount_" + i, authHandler.formattedAmount(monthlySalesAmount, companyid));
                        } else {
                            obj.put("amount_" + i, authHandler.round(monthlySalesAmount, companyid));
                        }
                        monthlyTotal[i] = 0.0 + monthlyTotal[i] + monthlySalesAmount;    //add month total for all customer
                    } else {
                        if (isForExport) {
                            obj.put("amount_" + i, authHandler.formattedAmount(0, companyid));
                        } else {
                            obj.put("amount_" + i, authHandler.formattedAmount(0, companyid));
                        }
                    }
                }
                /*
                 * Calculating the Sub Totals and Grand Total and Putting
                 * JSONObj into dataJArr.
                 */
                grandTotal = grandTotal + totalSalesAmount;                                       //grand total for all customer   
                if (isForExport) {
                    obj.put("total", authHandler.formattedAmount(totalSalesAmount, companyid));
                } else {
                    obj.put("total", authHandler.round(totalSalesAmount, companyid));
                }
                if (obj.optString("customerid", "").equals("Total")) {
                    obj.put("customername", "Total" + " (" + currency.getCurrencyCode() + ")");
                    jSONObjectTotal = obj;
                } else {
                    dataJArr.put(obj);
                    count++;
                }
            }
            /*
             * Skipping the Sub Total of each column as we are calculating in
             * the next functions.
             */
            if (!isForExport) {
                JSONObject obj1 = new JSONObject();
                obj1.put("customername", "Total" + " (" + currency.getCurrencyCode() + ")");
                for (int j = startDayOfMonth; j <= endDayOfMonth; j++) {
                    obj1.put("amount_" + j, authHandler.round(monthlyTotal[j], companyid));
                }
                obj1.put("total", authHandler.round(grandTotal, companyid));
                dataJArr.put(obj1);
                count++;
            }

            if (isForExport) {
                dataJArr.put(jSONObjectTotal);
            }
            JSONArray pagedJson = dataJArr;
            String start = request.getParameter(Constants.start);
            String limit = request.getParameter(Constants.limit);
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }

            commData.put("success", true);
            commData.put("coldata", pagedJson);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", dataJArr.length());
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

            jobj.put("valid", true);
            if (isForExport) {
                jobj.put("data", dataJArr);
            } else if(StringUtil.equalIgnoreCase(chartType, Constants.LINE_CHART)) {
                JSONArray chartDataArr = new JSONArray();
//                getting only last total json object from dataJArr
                JSONObject jtemp = dataJArr.getJSONObject(dataJArr.length() - 1);
                for(int i = 1; i <= endDayOfMonth; i++) {
                    JSONObject temp = new JSONObject();
                    temp.put("day", i);
                    temp.put("amountinbase", jtemp.getString("amount_" + i));
                    chartDataArr.put(temp);
                }
                jobj.put("data", chartDataArr);
            } else if(StringUtil.equalIgnoreCase(chartType, Constants.PIE_CHART)) {
                //Remove Total from JSON array
                if (dataJArr.length() > 0) {
                    dataJArr.remove(dataJArr.length() - 1);
                }
                 //Sort customers on amount
                dataJArr = AccountingManager.sortJsonArrayOnIntegerValues(dataJArr, "total", "desc");
                
                //If there are more than 10 records calculate sum of other recs and add as Others
                if(dataJArr.length() > Constants.MAX_LIMIT_FOR_PIE) {
                    double otherAmount = 0.0;
                    JSONObject otherData = new JSONObject();
                    otherData.put("customername", "Others");
                    for (int i = 9; i < dataJArr.length(); i++) {
                        otherAmount += authHandler.round(dataJArr.optJSONObject(i).optDouble("total", 0.0), companyid);
                        dataJArr.remove(i);
                        i--;
                    }
                    otherData.put("total", authHandler.round(otherAmount, companyid));
                    dataJArr.put(otherData);
                }
                jobj.put(Constants.data, dataJArr);
                
            } else {
                jobj.put("data", commData);
            }
        } catch (ParseException ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    @Override
    public JSONObject getMonthlySalesOrdesByCustomer(HttpServletRequest request, Map<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        Locale requestcontextutilsobj = null;
        try {

            int count = 0;

            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }
//            flag for chart type
            String chartType = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.CHART_TYPE))) {
                chartType = request.getParameter(Constants.CHART_TYPE);
            }
            JSONArray DataJArr = new JSONArray();

            boolean isForExport = false;
            if (requestParams.containsKey("isForExport") && !StringUtil.isNullOrEmpty(requestParams.get("isForExport").toString())) {
                isForExport = Boolean.parseBoolean(requestParams.get("isForExport").toString());
            }
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            HashMap invRequestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            
            DateFormat df = authHandler.getDateOnlyFormat();
            Date startDate = df.parse(request.getParameter("stdate"));
            Date endDate = df.parse(request.getParameter("enddate"));
            
            /*
             *Calculation of Date Ranges for the Month.
             */
            LocalDate localStartDate = new LocalDate(startDate);
            LocalDate localEndDate = new LocalDate(endDate);
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            int startMonth = cal.get(Calendar.MONTH);
            int startYear = cal.get(Calendar.YEAR);
            cal.setTime(endDate);
            int endMonth = cal.get(Calendar.MONTH);
            int endYear = cal.get(Calendar.YEAR);
            boolean isOneMonth = false;
            int monthCount = 0;
            
             // if user selects same month & year for both start & end fields, we get the last date of the month to populate into the endDate
            if (startMonth == endMonth && startYear == endYear) {
                DateTime date = localEndDate.toDateTime(LocalTime.MIDNIGHT);
                date = date.plusSeconds(86399);
                endDate = date.dayOfMonth().withMaximumValue().toDate();
                isOneMonth = true;
                localEndDate = new LocalDate(endDate);
                monthCount = 1;
            }

            if (!isOneMonth) {
                while (localStartDate.isBefore(localEndDate)) {
                    localStartDate = localStartDate.plus(Period.months(1));
                    monthCount++;
                }
            }
            
            invRequestParams.put(Constants.REQ_startdate, df.format(startDate));
            invRequestParams.put(Constants.REQ_enddate, df.format(endDate));
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                invRequestParams.put("companyid", companyid);
                invRequestParams.put("gcurrencyid", gcurrencyid);

                KwlReturnObject result =accSalesOrderDAOobj.getSalesOrdersMerged(invRequestParams);
                List list = result.getEntityList();
                DataJArr = getSalesOrderForDailySalesReport(request, list, DataJArr);
            }

            KwlReturnObject currencyResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), gcurrencyid);
            KWLCurrency currency = (KWLCurrency) currencyResult.getEntityList().get(0);

            // Data Structures required for Column Model
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject jobjTemp = new JSONObject();

            /*
             * Created Record to print the daily sales report.
             */
            String StoreRec = "customerid,customername,";

            /*
             * Created Headers with Respect to Month.
             */
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "");
            jobjTemp.put("dataIndex", "customerid");    //"Customer ID"
            jobjTemp.put("hidden", true);
            jobjTemp.put("hideable", false);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.dailySalesReport.customername", null, requestcontextutilsobj) + "<b>"); //"Customer Name"
            jobjTemp.put("dataIndex", "customername");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            List<String> monthList = new ArrayList();
            localStartDate = new LocalDate(startDate);
            localEndDate = new LocalDate(endDate);
            // just a trick to include the last month as well
            if (!isOneMonth) {
                localEndDate = localEndDate.plus(Period.months(1));
            } else {
                localEndDate = new LocalDate(endDate);
            }
            while (localStartDate.isBefore(localEndDate)) {
                String monthName = localStartDate.toString("MMM yyyy");
                localStartDate = localStartDate.plus(Period.months(1));
                monthList.add(monthName);
            }
            // the first object would be the months array
            int monthlist = monthList.size();
            JSONArray monthArray = new JSONArray();
            JSONObject monthObj;
            for (int i = 0; i < monthlist; i++) {
                monthObj = new JSONObject();
                monthObj.put("monthname", monthList.get(i));
                monthArray.put(monthObj);
            }

            for (int i = 0; i < monthCount; i++) {
                /*
                 *Added dataindex in record
                 */
                StoreRec += "amount_" + i + ",";

                String dateStr = monthList.get(i);
                /*
                 * Added dataindex in Header
                 */
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "<b>" + dateStr + "<b>");
                jobjTemp.put("dataIndex", "amount_" + i);
                jobjTemp.put("renderer", "WtfGlobal.currencyRenderer");
                jobjTemp.put("align", "right");
                jobjTemp.put("width", 100);
                jobjTemp.put("pdfwidth", 100);
                jarrColumns.put(jobjTemp);
            }

            StoreRec += "total";
            monthList.add("Total");

            /*
             * Adding dataindex into the record.
             */
            String[] recArr = StoreRec.split(",");
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.dailySalesReport.total", null, requestcontextutilsobj) + "<b>"); //"Total"
            jobjTemp.put("dataIndex", "total");
            jobjTemp.put("renderer", "WtfGlobal.currencyRenderer");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 120);
            jobjTemp.put("pdfwidth", 120);
            jarrColumns.put(jobjTemp);


            Map customerMap = new HashMap();
            JSONObject jSONObjectTotal = new JSONObject();
            for (int invIndex = 0; invIndex < DataJArr.length(); invIndex++) {
                JSONObject salesOrder = DataJArr.getJSONObject(invIndex);
                String customerid = salesOrder.getString("personid");
                Double salesOrderAmount = salesOrder.getDouble("amountinbase");
                LocalDate salesOrderDate = new LocalDate(new Date(salesOrder.getString("date")));
                String salesOrderMonth = salesOrderDate.toString("MMM YYYY");
                /*
                 * Checking whether the Customer of salesOrder is already present
                 * in the Map or Not, If yes then put the salesOrder Amount with
                 * the Date in Key in the Respective Date of Map.
                 */
                if (customerMap.containsKey(customerid)) {
                    Map monthlySalesMap = (HashMap) customerMap.get(customerid);
                    if (monthlySalesMap == null) {
                        monthlySalesMap = new HashMap();
                        monthlySalesMap.put(salesOrderMonth, salesOrderAmount);
                    } else {
                        Double monthlySalesAmount = (Double) monthlySalesMap.get(salesOrderMonth);
                        if (monthlySalesAmount == null) {
                            monthlySalesMap.put(salesOrderMonth, salesOrderAmount);
                        } else {
                            double amount = monthlySalesAmount.doubleValue();
                            amount += salesOrderAmount.doubleValue();
                            monthlySalesMap.put(salesOrderMonth, new Double(amount));
                        }
                    }
                    /*
                     * Adding total column for Export case
                     */
                    if (isForExport) {
                        Map monthlySalesTotalMap = new HashMap();
                        if (customerMap.containsKey("Total")) {
                            monthlySalesTotalMap = (HashMap) customerMap.get("Total");
                            if (monthlySalesTotalMap.containsKey(salesOrderMonth)) {
                                double totalAmount = (Double) monthlySalesTotalMap.get(salesOrderMonth);
                                totalAmount += salesOrderAmount;
                                monthlySalesTotalMap.put(salesOrderMonth, totalAmount);
                            } else {
                                monthlySalesTotalMap.put(salesOrderMonth, salesOrderAmount);
                            }
                            customerMap.put("Total", monthlySalesTotalMap);
                        } else {
                            monthlySalesTotalMap = new HashMap();
                            monthlySalesTotalMap.put(salesOrderMonth, salesOrderAmount);
                            customerMap.put("Total", monthlySalesTotalMap);
                        }
                    }
                } else {
                    /*
                     * If Customer is Not Present in the Existing Map then Add
                     * NEW record in the Map for Respective Customer.
                     */
                    Map monthlySalesMap = new HashMap();
                    monthlySalesMap.put(salesOrderMonth, salesOrderAmount);
                    customerMap.put(customerid, monthlySalesMap);

                    /*
                     * Adding total column for Export case
                     */
                    if (isForExport) {
                        Map monthlySalesTotalMap = new HashMap();
                        if (customerMap.containsKey("Total")) {
                            monthlySalesTotalMap = (HashMap) customerMap.get("Total");
                            if (monthlySalesTotalMap.containsKey(salesOrderMonth)) {
                                double totalAmount = (Double) monthlySalesTotalMap.get(salesOrderMonth);
                                totalAmount += salesOrderAmount;
                                monthlySalesTotalMap.put(salesOrderMonth, totalAmount);
                            } else {
                                monthlySalesTotalMap.put(salesOrderMonth, salesOrderAmount);
                            }
                            customerMap.put("Total", monthlySalesTotalMap);
                        } else {
                            monthlySalesTotalMap = new HashMap();
                            monthlySalesTotalMap.put(salesOrderMonth, salesOrderAmount);
                            customerMap.put("Total", monthlySalesTotalMap);
                        }
                    }
                }
            }

            Set customerSet = customerMap.keySet();
            int size = monthList.size();
            double[] monthlyTotal = new double[size];
            double grandTotal = 0.0;
            for (Object object : customerSet) {
                String customerid = object.toString();
                Map monthlySalesMap = (HashMap) customerMap.get(customerid);
                JSONObject obj = new JSONObject();
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerid);
                Customer customer = (Customer) objItr.getEntityList().get(0);
                obj.put("customerid", customerid);
                obj.put("customername", customer != null ? customer.getName() : "");
                double totalSalesAmount = 0.00;

                for (int i = 0; i < monthCount; i++) {
                    String salesOrderMonth = (String) monthList.get(i);
                    if (monthlySalesMap.containsKey(salesOrderMonth)) {
                        double monthlySalesAmount = ((Double) monthlySalesMap.get(salesOrderMonth)).doubleValue();
                        totalSalesAmount += authHandler.round(monthlySalesAmount, companyid);
                        if (isForExport) {
                            obj.put("amount_" + i, authHandler.formattedAmount(monthlySalesAmount, companyid));
                        } else {
                            obj.put("amount_" + i, authHandler.round(monthlySalesAmount, companyid));
                        }
                        monthlyTotal[i] = 0.0 + monthlyTotal[i] + monthlySalesAmount;    //add month total for all customer
                    } else {
                        if (isForExport) {
                            obj.put("amount_" + i, authHandler.formattedAmount(0, companyid));
                        } else {
                            obj.put("amount_" + i, authHandler.formattedAmount(0, companyid));
                        }
                    }
                }
                /*
                 * Calculating the Sub Totals and Grand Total and Putting
                 * JSONObj into dataJArr.
                 */
                grandTotal = grandTotal + totalSalesAmount;                                       //grand total for all customer   
                if (isForExport) {
                    obj.put("total", authHandler.formattedAmount(totalSalesAmount, companyid));
                } else {
                    obj.put("total", authHandler.round(totalSalesAmount, companyid));
                }
                if (obj.optString("customerid", "").equals("Total")) {
                    obj.put("customername", "Total" + " (" + currency.getCurrencyCode() + ")");
                    jSONObjectTotal = obj;
                } else {
                    dataJArr.put(obj);
                    count++;
                }
            }
            /*
             * Skipping the Sub Total of each column as we are calculating in
             * the next functions.
             */
            if (!isForExport) {
                JSONObject obj1 = new JSONObject();
                obj1.put("customername", "Total" + " (" + currency.getCurrencyCode() + ")");
                for (int j = 0; j <= monthCount; j++) {
                    obj1.put("amount_" + j, authHandler.round(monthlyTotal[j], companyid));
                }
                obj1.put("total", authHandler.round(grandTotal, companyid));
                dataJArr.put(obj1);
                count++;
            }

            if (isForExport) {
                dataJArr.put(jSONObjectTotal);
            }

            commData.put("success", true);
            commData.put("coldata", dataJArr);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", count);
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

            jobj.put("valid", true);
            if (isForExport) {
                jobj.put("data", dataJArr);
            } else if (StringUtil.equalIgnoreCase(chartType, Constants.BAR_CHART)) {
                JSONArray chartDataArr = new JSONArray();
                for (int i = 0; i < monthCount; i++) {
                    JSONObject temp = new JSONObject();
                    temp.put("monthname", monthArray.getJSONObject(i).getString("monthname"));
                    temp.put("amountinbase", dataJArr.getJSONObject(dataJArr.length() - 1).getString("amount_" + i));
                    chartDataArr.put(temp);
                }
                jobj.put("data", chartDataArr);
            } else if (StringUtil.equalIgnoreCase(chartType, Constants.PIE_CHART)) {
                //Remove Total from JSON array
                if (dataJArr.length() > 0) {
                    dataJArr.remove(dataJArr.length() - 1);
                }
                //Sort customers on amount
                dataJArr = AccountingManager.sortJsonArrayOnIntegerValues(dataJArr, "total", "desc");

                //If there are more than 10 records calculate sum of other recs and add as Others
                if (dataJArr.length() > Constants.MAX_LIMIT_FOR_PIE) {
                    double otherAmount = 0.0;
                    JSONObject otherData = new JSONObject();
                    otherData.put("customername", "Others");
                    for (int i = 9; i < dataJArr.length(); i++) {
                        otherAmount += authHandler.round(dataJArr.optJSONObject(i).optDouble("total", 0.0), companyid);
                        dataJArr.remove(i);
                        i--;
                    }
                    otherData.put("total", authHandler.round(otherAmount, companyid));
                    dataJArr.put(otherData);
                }
                jobj.put(Constants.data, dataJArr);
            } else {
                jobj.put("data", commData);
            }
        } catch (ParseException ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    @Override
    public JSONObject getYearlySalesOrdersByCustomer(HttpServletRequest request, Map<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        Locale requestcontextutilsobj = null;
        try {

            int count = 0;

            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }
//            flag for chart type
            String chartType = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.CHART_TYPE))) {
                chartType = request.getParameter(Constants.CHART_TYPE);
            }
            JSONArray DataJArr = new JSONArray();

            boolean isForExport = false;
            if (requestParams.containsKey("isForExport") && !StringUtil.isNullOrEmpty(requestParams.get("isForExport").toString())) {
                isForExport = Boolean.parseBoolean(requestParams.get("isForExport").toString());
            }
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            HashMap invRequestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            
            DateFormat df = authHandler.getDateOnlyFormat();
            Date startDate = df.parse(request.getParameter("stdate"));
            Date endDate = df.parse(request.getParameter("enddate"));
            
            /*
             *Calculation of Date Ranges for the Month.
             */
            LocalDate localStartDate = new LocalDate(startDate);
            LocalDate localEndDate = new LocalDate(endDate);
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            int startMonth = cal.get(Calendar.MONTH);
            int startYear = cal.get(Calendar.YEAR);
            cal.setTime(endDate);
            int endMonth = cal.get(Calendar.MONTH);
            int endYear = cal.get(Calendar.YEAR);
            boolean isOneMonth = false;
            int monthCount = 0;
            
             // if user selects same month & year for both start & end fields, we get the last date of the month to populate into the endDate
            if (startMonth == endMonth && startYear == endYear) {
                DateTime date = localEndDate.toDateTime(LocalTime.MIDNIGHT);
                date = date.plusSeconds(86399);
                endDate = date.dayOfMonth().withMaximumValue().toDate();
                isOneMonth = true;
                localEndDate = new LocalDate(endDate);
                monthCount = 1;
            }

            if (!isOneMonth) {
                while (localStartDate.isBefore(localEndDate)) {
                    localStartDate = localStartDate.plus(Period.years(1));
                    monthCount++;
                }
            }
            
            invRequestParams.put(Constants.REQ_startdate, df.format(startDate));
            invRequestParams.put(Constants.REQ_enddate, df.format(endDate));
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                invRequestParams.put("companyid", companyid);
                invRequestParams.put("gcurrencyid", gcurrencyid);

                KwlReturnObject result =accSalesOrderDAOobj.getSalesOrdersMerged(invRequestParams);
                List list = result.getEntityList();
                DataJArr = getSalesOrderForDailySalesReport(request, list, DataJArr);
            }

            KwlReturnObject currencyResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), gcurrencyid);
            KWLCurrency currency = (KWLCurrency) currencyResult.getEntityList().get(0);

            // Data Structures required for Column Model
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject jobjTemp = new JSONObject();

            /*
             * Created Record to print the daily sales report.
             */
            String StoreRec = "customerid,customername,";

            /*
             * Created Headers with Respect to Month.
             */
            jobjTemp = new JSONObject();
            jobjTemp.put("header", "");
            jobjTemp.put("dataIndex", "customerid");    //"Customer ID"
            jobjTemp.put("hidden", true);
            jobjTemp.put("hideable", false);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.dailySalesReport.customername", null, requestcontextutilsobj) + "<b>"); //"Customer Name"
            jobjTemp.put("dataIndex", "customername");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            List<String> monthList = new ArrayList();
            localStartDate = new LocalDate(startDate);
            localEndDate = new LocalDate(endDate);
            // just a trick to include the last month as well
            if (!isOneMonth) {
                localEndDate = localEndDate.plus(Period.years(1));
            } else {
                localEndDate = new LocalDate(endDate);
            }
            while (localStartDate.isBefore(localEndDate)) {
                String monthName = localStartDate.toString("yyyy");
                localStartDate = localStartDate.plus(Period.years(1));
                monthList.add(monthName);
            }
            // the first object would be the months array
            int monthlist = monthList.size();
            JSONArray monthArray = new JSONArray();
            JSONObject monthObj;
            for (int i = 0; i < monthlist; i++) {
                monthObj = new JSONObject();
                monthObj.put("monthname", monthList.get(i));
                monthArray.put(monthObj);
            }

            for (int i = 0; i < monthCount; i++) {
                /*
                 *Added dataindex in record
                 */
                StoreRec += "amount_" + i + ",";

                String dateStr = monthList.get(i);
                /*
                 * Added dataindex in Header
                 */
                jobjTemp = new JSONObject();
                jobjTemp.put("header", "<b>" + dateStr + "<b>");
                jobjTemp.put("dataIndex", "amount_" + i);
                jobjTemp.put("renderer", "WtfGlobal.currencyRenderer");
                jobjTemp.put("align", "right");
                jobjTemp.put("width", 100);
                jobjTemp.put("pdfwidth", 100);
                jarrColumns.put(jobjTemp);
            }

            StoreRec += "total";
            monthList.add("Total");

            /*
             * Adding dataindex into the record.
             */
            String[] recArr = StoreRec.split(",");
            for (String rec : recArr) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", rec);
                jarrRecords.put(jobjTemp);
            }

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "<b>" + messageSource.getMessage("acc.dailySalesReport.total", null, requestcontextutilsobj) + "<b>"); //"Total"
            jobjTemp.put("dataIndex", "total");
            jobjTemp.put("renderer", "WtfGlobal.currencyRenderer");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 120);
            jobjTemp.put("pdfwidth", 120);
            jarrColumns.put(jobjTemp);


            Map customerMap = new HashMap();
            JSONObject jSONObjectTotal = new JSONObject();
            for (int invIndex = 0; invIndex < DataJArr.length(); invIndex++) {
                JSONObject salesOrder = DataJArr.getJSONObject(invIndex);
                String customerid = salesOrder.getString("personid");
                Double salesOrderAmount = salesOrder.getDouble("amountinbase");
                LocalDate salesOrderDate = new LocalDate(new Date(salesOrder.getString("date")));
                String salesOrderMonth = salesOrderDate.toString("YYYY");
                /*
                 * Checking whether the Customer of salesOrder is already present
                 * in the Map or Not, If yes then put the salesOrder Amount with
                 * the Date in Key in the Respective Date of Map.
                 */
                if (customerMap.containsKey(customerid)) {
                    Map monthlySalesMap = (HashMap) customerMap.get(customerid);
                    if (monthlySalesMap == null) {
                        monthlySalesMap = new HashMap();
                        monthlySalesMap.put(salesOrderMonth, salesOrderAmount);
                    } else {
                        Double monthlySalesAmount = (Double) monthlySalesMap.get(salesOrderMonth);
                        if (monthlySalesAmount == null) {
                            monthlySalesMap.put(salesOrderMonth, salesOrderAmount);
                        } else {
                            double amount = monthlySalesAmount.doubleValue();
                            amount += salesOrderAmount.doubleValue();
                            monthlySalesMap.put(salesOrderMonth, new Double(amount));
                        }
                    }
                    /*
                     * Adding total column for Export case
                     */
                    if (isForExport) {
                        Map monthlySalesTotalMap = new HashMap();
                        if (customerMap.containsKey("Total")) {
                            monthlySalesTotalMap = (HashMap) customerMap.get("Total");
                            if (monthlySalesTotalMap.containsKey(salesOrderMonth)) {
                                double totalAmount = (Double) monthlySalesTotalMap.get(salesOrderMonth);
                                totalAmount += salesOrderAmount;
                                monthlySalesTotalMap.put(salesOrderMonth, totalAmount);
                            } else {
                                monthlySalesTotalMap.put(salesOrderMonth, salesOrderAmount);
                            }
                            customerMap.put("Total", monthlySalesTotalMap);
                        } else {
                            monthlySalesTotalMap = new HashMap();
                            monthlySalesTotalMap.put(salesOrderMonth, salesOrderAmount);
                            customerMap.put("Total", monthlySalesTotalMap);
                        }
                    }
                } else {
                    /*
                     * If Customer is Not Present in the Existing Map then Add
                     * NEW record in the Map for Respective Customer.
                     */
                    Map monthlySalesMap = new HashMap();
                    monthlySalesMap.put(salesOrderMonth, salesOrderAmount);
                    customerMap.put(customerid, monthlySalesMap);

                    /*
                     * Adding total column for Export case
                     */
                    if (isForExport) {
                        Map monthlySalesTotalMap = new HashMap();
                        if (customerMap.containsKey("Total")) {
                            monthlySalesTotalMap = (HashMap) customerMap.get("Total");
                            if (monthlySalesTotalMap.containsKey(salesOrderMonth)) {
                                double totalAmount = (Double) monthlySalesTotalMap.get(salesOrderMonth);
                                totalAmount += salesOrderAmount;
                                monthlySalesTotalMap.put(salesOrderMonth, totalAmount);
                            } else {
                                monthlySalesTotalMap.put(salesOrderMonth, salesOrderAmount);
                            }
                            customerMap.put("Total", monthlySalesTotalMap);
                        } else {
                            monthlySalesTotalMap = new HashMap();
                            monthlySalesTotalMap.put(salesOrderMonth, salesOrderAmount);
                            customerMap.put("Total", monthlySalesTotalMap);
                        }
                    }
                }
            }

            Set customerSet = customerMap.keySet();
            int size = monthList.size();
            double[] monthlyTotal = new double[size];
            double grandTotal = 0.0;
            for (Object object : customerSet) {
                String customerid = object.toString();
                Map monthlySalesMap = (HashMap) customerMap.get(customerid);
                JSONObject obj = new JSONObject();
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerid);
                Customer customer = (Customer) objItr.getEntityList().get(0);
                obj.put("customerid", customerid);
                obj.put("customername", customer != null ? customer.getName() : "");
                double totalSalesAmount = 0.00;

                for (int i = 0; i < monthCount; i++) {
                    String salesOrderMonth = (String) monthList.get(i);
                    if (monthlySalesMap.containsKey(salesOrderMonth)) {
                        double monthlySalesAmount = ((Double) monthlySalesMap.get(salesOrderMonth)).doubleValue();
                        totalSalesAmount += authHandler.round(monthlySalesAmount, companyid);
                        if (isForExport) {
                            obj.put("amount_" + i, authHandler.formattedAmount(monthlySalesAmount, companyid));
                        } else {
                            obj.put("amount_" + i, authHandler.round(monthlySalesAmount, companyid));
                        }
                        monthlyTotal[i] = 0.0 + monthlyTotal[i] + monthlySalesAmount;    //add month total for all customer
                    } else {
                        if (isForExport) {
                            obj.put("amount_" + i, authHandler.formattedAmount(0, companyid));
                        } else {
                            obj.put("amount_" + i, authHandler.formattedAmount(0, companyid));
                        }
                    }
                }
                /*
                 * Calculating the Sub Totals and Grand Total and Putting
                 * JSONObj into dataJArr.
                 */
                grandTotal = grandTotal + totalSalesAmount;                                       //grand total for all customer   
                if (isForExport) {
                    obj.put("total", authHandler.formattedAmount(totalSalesAmount, companyid));
                } else {
                    obj.put("total", authHandler.round(totalSalesAmount, companyid));
                }
                if (obj.optString("customerid", "").equals("Total")) {
                    obj.put("customername", "Total" + " (" + currency.getCurrencyCode() + ")");
                    jSONObjectTotal = obj;
                } else {
                    dataJArr.put(obj);
                    count++;
                }
            }
            /*
             * Skipping the Sub Total of each column as we are calculating in
             * the next functions.
             */
            if (!isForExport) {
                JSONObject obj1 = new JSONObject();
                obj1.put("customername", "Total" + " (" + currency.getCurrencyCode() + ")");
                for (int j = 0; j <= monthCount; j++) {
                    obj1.put("amount_" + j, authHandler.round(monthlyTotal[j], companyid));
                }
                obj1.put("total", authHandler.round(grandTotal, companyid));
                dataJArr.put(obj1);
                count++;
            }

            if (isForExport) {
                dataJArr.put(jSONObjectTotal);
            }

            commData.put("success", true);
            commData.put("coldata", dataJArr);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", count);
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

            jobj.put("valid", true);
            if (isForExport) {
                jobj.put("data", dataJArr);
            } else if (StringUtil.equalIgnoreCase(chartType, Constants.BAR_CHART)) {
                JSONArray chartDataArr = new JSONArray();
                for (int i = 0; i < monthCount; i++) {
                    JSONObject temp = new JSONObject();
                    temp.put("year", monthArray.getJSONObject(i).getString("monthname"));
                    temp.put("total", dataJArr.getJSONObject(dataJArr.length() - 1).getString("amount_" + i));
                    chartDataArr.put(temp);
                }
                jobj.put("data", chartDataArr);
            } else if (StringUtil.equalIgnoreCase(chartType, Constants.PIE_CHART)) {
                //Remove Total from JSON array
                if (dataJArr.length() > 0) {
                    dataJArr.remove(dataJArr.length() - 1);
                }
                //Sort customers on amount
                dataJArr = AccountingManager.sortJsonArrayOnIntegerValues(dataJArr, "total", "desc");

                //If there are more than 10 records calculate sum of other recs and add as Others
                if (dataJArr.length() > Constants.MAX_LIMIT_FOR_PIE) {
                    double otherAmount = 0.0;
                    JSONObject otherData = new JSONObject();
                    otherData.put("customername", "Others");
                    for (int i = 9; i < dataJArr.length(); i++) {
                        otherAmount += authHandler.round(dataJArr.optJSONObject(i).optDouble("total", 0.0), companyid);
                        dataJArr.remove(i);
                        i--;
                    }
                    otherData.put("total", authHandler.round(otherAmount, companyid));
                    dataJArr.put(otherData);
                }
                jobj.put(Constants.data, dataJArr);
            } else {
                jobj.put("data", commData);
            }
        } catch (ParseException ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    public JSONArray getSalesOrderForDailySalesReport(HttpServletRequest request, List<Object[]> list, JSONArray jArr) throws SessionExpiredException, ServiceException {
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean ignoreZero = request.getParameter("ignorezero") != null ? Boolean.parseBoolean(request.getParameter("ignorezero")) : false;
            // Load salesOrders in map 
            List<String> idsList = new ArrayList();
            for (Object[] oj : list) {
                idsList.add(oj[0].toString());
            }
            Map<String, SalesOrderInfo> salesOrderObjectMap = accSalesOrderDAOobj.getSalesOrderList(idsList);
            for (Object[] oj : list) {
                String invid = oj[0].toString();
                if (!salesOrderObjectMap.containsKey(invid)) {
                    continue;
                }
                SalesOrderInfo salesOrderinfo = salesOrderObjectMap.get(invid);
                SalesOrder salesOrder = salesOrderinfo.getSalesOrder();

                Date salesOrderCreationDate = salesOrder.getOrderDate();
                Customer customer = salesOrderinfo.getCustomer();
                Account account = null;

                JSONObject obj = new JSONObject();
                obj.put("billid", salesOrder.getID());
                obj.put("personid", customer == null ? account.getID() : customer.getID());
                obj.put("date", df.format(salesOrderCreationDate));
                obj.put("duedate", df.format(salesOrder.getDueDate()));
                obj.put("duedateInUserDateFormat", authHandler.getUserDateFormatterWithoutTimeZone(request).format(salesOrder.getDueDate()));
                obj.put("personname", customer == null ? account.getName() : customer.getName());
                
                boolean gstIncluded = salesOrder.isGstIncluded();
                 Set<SalesOrderDetail> salesOrderDetails =salesOrder.getRows();
                    double amount = 0, totalDiscount = 0, discountPrice = 0;
                    double rowTaxAmt = 0d, rowDiscountAmt = 0d;

                    for(SalesOrderDetail sod:salesOrderDetails) {
                        double sorate=authHandler.round(sod.getRate(), companyid);
                        if(gstIncluded) {
                            sorate = sod.getRateincludegst();
                        }
                        double quantity=authHandler.roundQuantity(sod.getQuantity(), companyid);

                        double quotationPrice = authHandler.round(quantity * sorate, companyid);
                        double discountSOD=authHandler.round(sod.getDiscount(), companyid);
                        
                        if(sod.getDiscountispercent() == 1) {
                            discountPrice = (quotationPrice) - authHandler.round((quotationPrice * discountSOD/100), companyid);
                            rowDiscountAmt += authHandler.round((quotationPrice * discountSOD/100), companyid);
                        } else {
                            discountPrice = quotationPrice - discountSOD;
                            rowDiscountAmt += discountSOD;
                        }
                        rowTaxAmt+=sod.getRowTaxAmount();
                        amount += discountPrice;//amount += discountPrice + (discountPrice * rowTaxPercent/100);
                        if(!gstIncluded) {
                            amount += authHandler.round(sod.getRowTaxAmount(), companyid);
                        }
                    }                    
                    double discountSO=authHandler.round(salesOrder.getDiscount(), companyid);
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

                   double totalTermAmount=0;
                   double taxableTermamount = 0;
                    HashMap<String, Object> requestParam = new HashMap();
                    HashMap<String, Object> filterrequestParams = new HashMap();
                    requestParam.put("salesOrder", salesOrder.getID());
                    KwlReturnObject salesOrderResult =null;
                    filterrequestParams.put("taxid", salesOrder.getTax()==null?"":salesOrder.getTax().getID());
                    salesOrderResult=accSalesOrderDAOobj.getSalesOrderTermMap(requestParam);
                    List<SalesOrderTermMap> termMap = salesOrderResult.getEntityList();
                    for(SalesOrderTermMap salesOrderTermMap : termMap) {
                        filterrequestParams.put("term", salesOrderTermMap.getTerm() == null ? "" : salesOrderTermMap.getTerm().getId());
                        double termAmnt = salesOrderTermMap.getTermamount();
                        totalTermAmount += authHandler.round(termAmnt, companyid);

                        boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);

                        if (isTermMappedwithTax) {
                            taxableTermamount += termAmnt;
                        }
                    }
                    totalTermAmount=authHandler.round(totalTermAmount, companyid);

                    JSONObject paramJobj=StringUtil.convertRequestToJsonObject(request);
                    HashMap<String, Object> requestParams = getSalesOrdersMapJson(paramJobj);
                    double  taxPercent=0;
                    if(salesOrder.getTax()!=null){
                        requestParams.put("billId", salesOrder.getID());
                        requestParams.put("transactiondate", salesOrder.getOrderDate());
                        requestParams.put("taxid", salesOrder.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj=(Object[]) taxList.get(0);
                        taxPercent=taxObj[1]==null?0:(Double) taxObj[1];

                    }
                    double orderAmount=amount;//(Double) bAmt.getEntityList().get(0);
                    double ordertaxamount=(taxPercent==0?0:authHandler.round(((orderAmount + taxableTermamount)*taxPercent/100), companyid));
                    double taxAmt=rowTaxAmt+ordertaxamount;// either row level tax will be avvailable or salesOrder level
                    
                    obj.put("amountbeforegst", amount-rowTaxAmt); // Amount before both kind of tax row level or transaction level
                    obj.put("taxpercent", taxPercent);
                    obj.put("taxamount",taxAmt );// Tax Amount
                    amount=amount+totalTermAmount+ordertaxamount;
                    orderAmount+=totalTermAmount;
                    obj.put("orderamount",orderAmount );
                    obj.put("orderamountwithTax",amount);// Total Amount
                  
                    KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amount, salesOrder.getCurrency().getCurrencyID(), salesOrder.getOrderDate(), salesOrder.getExternalCurrencyRate());
                    double totalAmountinBase= (Double)bAmt.getEntityList().get(0);
                    obj.put("amountinbase", authHandler.round(totalAmountinBase, companyid)); //Total Amount in base

                if (!(ignoreZero && authHandler.round(amount, companyid) <= 0)) {
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("AccSalesOrderServiceImpl.getSalesOrderForDailySalesReport : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    
    /*----------- Function used to check Linking of Quotation with DO & Invoice-----------*/
    boolean quotationsApplicableForFilter(HashMap requestParams) throws ServiceException {
        boolean statusFlag = false;
             
        int filterApplied = (Integer) requestParams.get("filterApplied");
            
        try {

            switch (filterApplied) {

                case 4://CQ without DO , Invoice

                    statusFlag = isQuotationwithoutDOAndInvoice(requestParams);
                    break;

                case 5://CQ without DO ,with Invoice

                    statusFlag = isQuotationWithoutDOWithInvoice(requestParams);
                    break;

                case 6://CQ with DO , without Invoice

                    statusFlag = isQuotationWithDOWithoutInvoice(requestParams);
                    break;

                case 7://CQ with DO , with Invoice

                    statusFlag = isQuotationWithDOWithInvoice(requestParams);
                    break;

            }
         
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccSalesOrderServiceImpl.quotationsApplicableForFilter : " + ex.getMessage(), ex);
        }
        return statusFlag;
    }

    boolean isQuotationwithoutDOAndInvoice(HashMap requestParams) throws ServiceException {
        boolean isQuotationwithoutDOAndInvoice = false;
        boolean statusFlag = false;

        String returnID = "";

        Quotation quotationObject = (Quotation) requestParams.get("quotationObject");
        int linkFlag = quotationObject.getLinkflag();
        List quotationList = null;

        /* CQ linking 
         1.linkFlag=0 No linking
         2.linkFlag=1 CQ->SI
         3.linkFlag=2 CQ->SO
         */
        /* SO linking 
         1.linkFlag=0 No linking
         2.linkFlag=1 SO->SI
         3.linkFlag=2 SO->DO
         */
        if (linkFlag == 0) {
            isQuotationwithoutDOAndInvoice = true;
        } else if (linkFlag == 2) {//CQ->SO

            quotationList = accSalesOrderDAOobj.getSalesOrderLinkedWithQuotation(quotationObject.getID());//get List of SO id linked with Quotation

            returnID = (String) quotationList.get(0);

            if (!returnID.isEmpty()) {

                String[] soidArr = returnID.split(",");

                /*------ Iterating on SO id ------*/
                for (int i = 0; i < soidArr.length; i++) {

                    KwlReturnObject extracompanyprefresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soidArr[i]);
                    SalesOrder salesOrderObj = (SalesOrder) extracompanyprefresult.getEntityList().get(0);

                    if (salesOrderObj.getLinkflag() == 0) {//No linking of SO
                        isQuotationwithoutDOAndInvoice = true;
                    } else if (salesOrderObj.getLinkflag() == 2) {//SO->DO
                        isQuotationwithoutDOAndInvoice = false;
                        break;
                    } else if (salesOrderObj.getLinkflag() == 1) {//SO->Invoice
                        isQuotationwithoutDOAndInvoice = false;
                        break;
                    }

                }

            }
        }
        statusFlag = isQuotationwithoutDOAndInvoice;

        return statusFlag;
    }

    boolean isQuotationWithoutDOWithInvoice(HashMap requestParams) throws ServiceException {
        boolean isQuotationWithoutDOWithInvoice = false;
        boolean statusFlag = false;

        String returnID = "";

        Quotation quotationObject = (Quotation) requestParams.get("quotationObject");
        int linkFlag = quotationObject.getLinkflag();
        List quotationList = null;

        /* CQ linking 
         1.linkFlag=0 No linking
         2.linkFlag=1 CQ->SI
         3.linkFlag=2 CQ->SO
         */
        /* SO linking 
         1.linkFlag=0 No linking
         2.linkFlag=1 SO->SI
         3.linkFlag=2 SO->DO
         */
        if (linkFlag == 2) {

            quotationList = accSalesOrderDAOobj.getSalesOrderLinkedWithQuotation(quotationObject.getID());

            returnID = (String) quotationList.get(0);

            if (!returnID.isEmpty()) {

                String[] soidArr = returnID.split(",");

                for (int i = 0; i < soidArr.length; i++) {

                    KwlReturnObject extracompanyprefresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soidArr[i]);
                    SalesOrder salesOrderObj = (SalesOrder) extracompanyprefresult.getEntityList().get(0);

                    if (salesOrderObj.getLinkflag() == 1) {//SO->SI

                        isQuotationWithoutDOWithInvoice = true;

                        quotationList = accSalesOrderDAOobj.getInvoiceLinkedWithSourceDocument(salesOrderObj.getID());//get list of id of invoice linked with SO
                        returnID = (String) quotationList.get(0);

                        if (!returnID.isEmpty()) {

                            String[] invidArr = returnID.split(",");

                            for (int invoice = 0; invoice < invidArr.length; invoice++) {
                                quotationList = accSalesOrderDAOobj.getDeliveryOrderLinkedWithSourceDocument(invidArr[invoice]);//get list of id of DO linked with Invoice
                                returnID = (String) quotationList.get(0);

                                /*----------if atleat one linking present like CQ->SO->Invoice->DO then get out of the loop with flag  isQuotationWithoutDOWithInvoice of value false-----------------  */
                                if (!returnID.isEmpty()) {
                                    isQuotationWithoutDOWithInvoice = false;
                                    break;
                                }
                            }
                        }

                    } else if (salesOrderObj.getLinkflag() == 2) {//Need to check CQ->SO->DO if present then get out of the loop with flag  isQuotationWithoutDOWithInvoice of value false;
                        isQuotationWithoutDOWithInvoice = false;
                        break;

                    }

                }
            }

        } else if (linkFlag == 1) {//CQ->SI

            isQuotationWithoutDOWithInvoice = true;

            quotationList = accSalesOrderDAOobj.getInvoiceLinkedWithSourceDocument(quotationObject.getID());//get list of id of invoice linked with quotation 
            returnID = (String) quotationList.get(0);
            if (!returnID.isEmpty()) {
                String[] invidArr = returnID.split(",");

                for (int invoice = 0; invoice < invidArr.length; invoice++) {
                    quotationList = accSalesOrderDAOobj.getDeliveryOrderLinkedWithSourceDocument(invidArr[invoice]);//get list of id of DO linked with Invoice 

                    returnID = (String) quotationList.get(0);

                    /*----------if atleat one linking present like CQ->Invoice->DO then get out of the loop with flag  isQuotationWithoutDOWithInvoice of value false-----------------  */
                    if (!returnID.isEmpty()) {
                        isQuotationWithoutDOWithInvoice = false;
                        break;
                    }
                }
            }

        }
        statusFlag = isQuotationWithoutDOWithInvoice;

        return statusFlag;
    }

    boolean isQuotationWithDOWithoutInvoice(HashMap requestParams) throws ServiceException {

        boolean isQuotationWithDOWithoutInvoice = false;
        boolean statusFlag = false;

        String returnID = "";

        Quotation quotationObject = (Quotation) requestParams.get("quotationObject");
        int linkFlag = quotationObject.getLinkflag();
        List quotationList = null;

        /* CQ linking 
         1.linkFlag=0 No linking
         2.linkFlag=1 CQ->SI
         3.linkFlag=2 CQ->SO
         */
        /* SO linking 
         1.linkFlag=0 No linking
         2.linkFlag=1 SO->SI
         3.linkFlag=2 SO->DO
         */
        if (linkFlag == 2) {
            quotationList = accSalesOrderDAOobj.getSalesOrderLinkedWithQuotation(quotationObject.getID());

            returnID = (String) quotationList.get(0);

            if (!returnID.isEmpty()) {
                String[] soidArr = returnID.split(",");

                for (int soCount = 0; soCount < soidArr.length; soCount++) {

                    KwlReturnObject extracompanyprefresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soidArr[soCount]);
                    SalesOrder salesOrderObj = (SalesOrder) extracompanyprefresult.getEntityList().get(0);

                    /*------If CQ->SO->Invoice the get out of the loop with flag  isQuotationWithDOWithoutInvoice of value false---------- */
                    if (salesOrderObj.getLinkflag() == 1) {
                        isQuotationWithDOWithoutInvoice = false;
                        break;
                    } else if (salesOrderObj.getLinkflag() == 2) {
                        isQuotationWithDOWithoutInvoice = true;
                        quotationList = accSalesOrderDAOobj.getDeliveryOrderLinkedWithSourceDocument(salesOrderObj.getID());

                        returnID = (String) quotationList.get(0);

                        if (!returnID.isEmpty()) {
                            String[] doidArr = returnID.split(",");

                            for (int doCount = 0; doCount < doidArr.length; doCount++) {

                                quotationList = accSalesOrderDAOobj.getInvoiceLinkedWithSourceDocument(doidArr[doCount]);//Check CQ->SO Linking

                                returnID = (String) quotationList.get(0);

                                /*------If CQ->SO->DO->Invoice  the get out of the loop with flag  isQuotationWithDOWithoutInvoice of value false---------- */
                                if (!returnID.isEmpty()) {
                                    isQuotationWithDOWithoutInvoice = false;
                                    break;
                                }

                            }
                        }
                    }

                }

            }
        }
        statusFlag = isQuotationWithDOWithoutInvoice;
        return statusFlag;
    }

    boolean isQuotationWithDOWithInvoice(HashMap requestParams) throws ServiceException {
        boolean isQuotationWithDOWithInvoice = false;
        boolean statusFlag = false;

        String returnID = "";

        Quotation quotationObject = (Quotation) requestParams.get("quotationObject");
        int linkFlag = quotationObject.getLinkflag();
        List quotationList = null;

        /* CQ linking 
         1.linkFlag=0 No linking
         2.linkFlag=1 CQ->SI
         3.linkFlag=2 CQ->SO
         */
        /* SO linking 
         1.linkFlag=0 No linking
         2.linkFlag=1 SO->SI
         3.linkFlag=2 SO->DO
         */

        /* If CQ->SO then check whole whole hierarchy CQ->SO->Invoice->DO or CQ->SO->DO->Invoice*/
        if (linkFlag == 2) {
            quotationList = accSalesOrderDAOobj.getSalesOrderLinkedWithQuotation(quotationObject.getID());

            returnID = (String) quotationList.get(0);
            if (!returnID.isEmpty()) {
                String[] soidArr = returnID.split(",");

                for (int soCount = 0; soCount < soidArr.length; soCount++) {

                    KwlReturnObject extracompanyprefresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soidArr[soCount]);
                    SalesOrder salesOrderObj = (SalesOrder) extracompanyprefresult.getEntityList().get(0);

                    if (salesOrderObj.getLinkflag() == 1) {

                        quotationList = accSalesOrderDAOobj.getInvoiceLinkedWithSourceDocument(salesOrderObj.getID());
                        returnID = (String) quotationList.get(0);

                        if (!returnID.isEmpty()) {
                            String[] invidArr = returnID.split(",");

                            for (int invoice = 0; invoice < invidArr.length; invoice++) {
                                quotationList = accSalesOrderDAOobj.getDeliveryOrderLinkedWithSourceDocument(invidArr[invoice]);

                                returnID = (String) quotationList.get(0);

                                if (!returnID.isEmpty()) {
                                    isQuotationWithDOWithInvoice = true;
                                    break;
                                }
                            }
                        }

                    } else if (salesOrderObj.getLinkflag() == 2) {
                        quotationList = accSalesOrderDAOobj.getDeliveryOrderLinkedWithSourceDocument(salesOrderObj.getID());
                        returnID = (String) quotationList.get(0);

                        if (!returnID.isEmpty()) {
                            String[] invidArr = returnID.split(",");

                            for (int invoice = 0; invoice < invidArr.length; invoice++) {
                                quotationList = accSalesOrderDAOobj.getInvoiceLinkedWithSourceDocument(invidArr[invoice]);
                                returnID = (String) quotationList.get(0);

                                if (!returnID.isEmpty()) {
                                    isQuotationWithDOWithInvoice = true;
                                    break;
                                }
                            }

                        }

                    }

                }
            }

        } else if (linkFlag == 1) {//checked CQ->Invoice->DO

            quotationList = accSalesOrderDAOobj.getInvoiceLinkedWithSourceDocument(quotationObject.getID());
            returnID = (String) quotationList.get(0);

            if (!returnID.isEmpty()) {
                String[] invidArr = returnID.split(",");

                for (int invoice = 0; invoice < invidArr.length; invoice++) {
                    quotationList = accSalesOrderDAOobj.getDeliveryOrderLinkedWithSourceDocument(invidArr[invoice]);
                    returnID = (String) quotationList.get(0);
                    if (!returnID.isEmpty()) {
                        isQuotationWithDOWithInvoice = true;
                        break;
                    }
                }

            }
        }

        statusFlag = isQuotationWithDOWithInvoice;
        return statusFlag;
    }
    
    /**
     * Below method gets estimated Shipping cost for Sales Order and updates the value in database
     * Shipping cost is estimated by sending a request to UPS web service via REST
     * Estimated shipping cost is saved under column 'totalShippingCost' in SalesOrder table
     */
    @Override
    public JSONObject calculateAndUpdateTotalShippingCost(JSONArray dataJArr, JSONObject requestJobj) throws ServiceException {
        JSONObject Jobj = new JSONObject();
        JSONObject upsErrorJSON = new JSONObject();
        try {
            String recordIDForCostCalculation = requestJobj.optString("recordIDForCostCalculation");//Id of packing record selected for cost calculation
            if (!StringUtil.isNullOrEmpty(recordIDForCostCalculation)) {
                for (int i = 0; i < dataJArr.length(); i++) {
                    JSONObject recordJobj = dataJArr.getJSONObject(i);
                    if (recordIDForCostCalculation.equals(recordJobj.optString(Constants.billid))) {
                        requestJobj.put(IntegrationConstants.integrationOperationIdKey, IntegrationConstants.ups_costEstimation);
                        requestJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_UPS);
                        JSONObject costingDetailJSON = integrationCommonService.processIntegrationRequest(requestJobj);
                        if (costingDetailJSON.optJSONObject("ShipConfirmResponse") == null) {//If request to UPS fails, read error to display on UI
                            JSONObject ErrorJSON = costingDetailJSON.optJSONObject("Fault");
                            JSONObject ErrorDetailJSON = ErrorJSON.optJSONObject("detail");
                            JSONObject ErrorDetailErrorsJSON = ErrorDetailJSON.optJSONObject("Errors");
                            JSONObject ErrorDetails = ErrorDetailErrorsJSON.optJSONObject("ErrorDetail");
                            JSONObject ErrorDetailsPrimaryErrorCode = ErrorDetails.optJSONObject("PrimaryErrorCode");
                            upsErrorJSON.put("ErrorSeverity", ErrorDetails.optString("Severity"));
                            upsErrorJSON.put("ErrorCode", ErrorDetailsPrimaryErrorCode.optString("Code"));
                            upsErrorJSON.put("ErrorDescription", ErrorDetailsPrimaryErrorCode.optString("Description"));
                        } else {//If request to UPS is successful, read response
                            JSONObject FreightShipResponse = costingDetailJSON.optJSONObject("ShipConfirmResponse");
                            JSONObject ShipmentResults = FreightShipResponse.optJSONObject("ShipmentResults");
                            JSONObject ShipmentCharges = ShipmentResults.optJSONObject("ShipmentCharges");
                            JSONObject TotalShipmentCharge = ShipmentCharges.optJSONObject("TotalCharges");
                            double totalShippingCost = TotalShipmentCharge.optDouble("MonetaryValue");;

                            if (totalShippingCost != 0.0) {//Save total shipping cost into database
                                JSONObject dataMapJobj = new JSONObject();
                                dataMapJobj.put(IntegrationConstants.totalShippingCost, totalShippingCost);
                                dataMapJobj.put("SOid", recordIDForCostCalculation);
                                accSalesOrderDAOobj.updateSalesOrder(dataMapJobj, null);
                            }
                            recordJobj.put(IntegrationConstants.totalShippingCost, totalShippingCost);
                        }
                        break;
                    }
                    dataJArr.put(i, recordJobj);
                }
            }
            Jobj.put(Constants.data, dataJArr);
            Jobj.put("upsErrorJSON", upsErrorJSON);
        } catch (JSONException ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccSalesOrderServiceImpl.calculateAndUpdateTotalShippingCost : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccSalesOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Jobj;
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getCQLinkedInTransaction(JSONObject paramJObj) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String billid = paramJObj.optString(Constants.billid);
            String companyid =  paramJObj.optString(Constants.companyKey);
            DateFormat df = authHandler.getDateOnlyFormat();
            String currencyid = paramJObj.optString(Constants.globalCurrencyKey);
            if (!StringUtil.isNullOrEmpty(billid)) {
                //VQ Linked in Purcahse Order
                KwlReturnObject resultso = accSalesOrderDAOobj.getSalesOrderLinkedWithCQ(billid, companyid);
                List<String> listso = resultso.getEntityList();
                for (String orderid:listso ) {
                    JSONObject obj = new JSONObject();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), orderid);
                    SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
                    KWLCurrency currency = null;
                    if (salesOrder.getCurrency() != null) {
                        currency = salesOrder.getCurrency();
                    }
                    SalesOrder salesorder = (SalesOrder) objItr.getEntityList().get(0);
                    Customer customer = salesorder.getCustomer();
                    obj.put("billid", salesorder.getID());
                    obj.put("companyid", salesorder.getCompany().getCompanyID());
                    obj.put("companyname", salesorder.getCompany().getCompanyName());
                    obj.put("withoutinventory", "");
                    obj.put(Constants.transactionNo, salesorder.getSalesOrderNumber());   //Sales order no
                    obj.put("duedate", salesorder.getDueDate() != null ? df.format(salesorder.getDueDate()) : "");
                    obj.put("date", salesorder.getOrderDate() != null ? df.format(salesorder.getOrderDate()) : "");
                    if (salesorder.isIsconsignment()) {
                        obj.put("mergedCategoryData", "Consignment Sales Order");  //type of data
                    } else if (salesorder.getLeaseOrMaintenanceSO() == 1) {
                        obj.put("mergedCategoryData", "Lease Order");  //type of data
                    } else {
                        obj.put("mergedCategoryData", "Sales Order");  //type of data
                    }
                    obj.put("personname", customer.getName());
                    obj.put("personid", customer.getID());
                    obj.put("companyname", salesorder.getCompany().getCompanyName());
                    obj.put("externalcurrencyrate", salesorder.getExternalCurrencyRate());
                    obj.put(Constants.IsRoundingAdjustmentApplied, salesOrder.isIsRoundingAdjustmentApplied());
                    obj.put("personid", customer.getID());
                    obj.put("aliasname", customer.getAliasname());
                    obj.put("customercode", customer.getAcccode() == null ? "" : customer.getAcccode());
                    obj.put("billno", salesorder.getSalesOrderNumber());
                    obj.put("duedate", authHandler.getDateOnlyFormat().format(salesorder.getDueDate()));
                    obj.put("duedateinuserformat", df.format(salesorder.getDueDate()));
                    obj.put("dateinuserformat", df.format(salesorder.getOrderDate()));
                    obj.put("shipdate", salesorder.getShipdate() == null ? "" : authHandler.getDateOnlyFormat().format(salesorder.getShipdate()));
                    obj.put("shipdateinuserformat", salesorder.getShipdate() == null ? "" : df.format(salesorder.getShipdate()));
                    obj.put("shipvia", salesorder.getShipvia());
                    obj.put("fob", salesorder.getFob());
                    obj.put("isOpeningBalanceTransaction", salesorder.isIsOpeningBalanceSO());
                    obj.put("isConsignment", salesorder.isIsconsignment());
                    obj.put("parentso", salesorder.getParentSO() == null ? "" : salesorder.getParentSO().getID());
                    if (salesorder.getCustWarehouse() != null) {
                        obj.put("custWarehouse", salesorder.getCustWarehouse().getId());
                    }
                    if (salesorder.isIsconsignment()) {
                        obj.put("todate", salesorder.getTodate() == null ? "" : authHandler.getDateOnlyFormat().format(salesorder.getTodate()));
                        obj.put("fromdate", salesorder.getFromdate() == null ? "" : authHandler.getDateOnlyFormat().format(salesorder.getFromdate()));
                        obj.put("movementtype", salesorder.getMovementType() != null ? salesorder.getMovementType().getID() : "");
                        obj.put("movementtypename", salesorder.getMovementType() != null ? salesorder.getMovementType().getValue() : "");
                        obj.put("requestWarehouse", salesorder.getRequestWarehouse() != null ? salesorder.getRequestWarehouse().getId() : "");
                        obj.put("requestLocation", salesorder.getRequestLocation() != null ? salesorder.getRequestLocation().getId() : "");
                        obj.put("autoapproveflag", salesorder.isAutoapproveflag());
                    }
                    obj.put("date", authHandler.getDateOnlyFormat().format(salesorder.getOrderDate()));
                    obj.put("isfavourite", salesorder.isFavourite());
                    obj.put("isprinted", salesorder.isPrinted());
                    obj.put("billto", salesorder.getBillTo());
                    obj.put("shipto", salesorder.getShipTo());
                    obj.put("salesPerson", salesorder.getSalesperson() == null ? "" : salesorder.getSalesperson().getID());
                    obj.put("salespersonname", salesorder.getSalesperson() == null ? "" : salesorder.getSalesperson().getValue());
                    obj.put("salesPersonCode", salesorder.getSalesperson() == null ? "" : salesorder.getSalesperson().getCode());
                    obj.put("createdby", StringUtil.getFullName(salesorder.getCreatedby()));
                    obj.put("createdbyid", salesorder.getCreatedby().getUserID());
                    obj.put("deleted", salesorder.isDeleted());
                    boolean gstIncluded = salesorder.isGstIncluded();
                    obj.put("gstIncluded", gstIncluded);
                    obj.put("islockQuantityflag", salesorder.isLockquantityflag());  //for getting locked flag of indivisual so
                    obj.put("leaseOrMaintenanceSo", salesorder.getLeaseOrMaintenanceSO());
                    obj.put("customerporefno", salesorder.getCustomerPORefNo());
                    obj.put("totalprofitmargin", salesorder.getTotalProfitMargin());
                    obj.put("totalprofitmarginpercent", salesorder.getTotalProfitMarginPercent());
                    obj.put("maintenanceId", salesorder.getMaintenance() == null ? "" : salesorder.getMaintenance().getId());
                    obj.put("termname", salesorder.getTerm() == null ? "" : salesorder.getTerm().getTermname());
                    obj.put("termid", salesorder.getTerm() != null ? salesorder.getTerm().getID() : "");
                    BillingShippingAddresses addresses = salesorder.getBillingShippingAddresses();
                    AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                    obj.put(Constants.SEQUENCEFORMATID, salesorder.getSeqformat() == null ? "" : salesorder.getSeqformat().getID());
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("currencycode", currency.getCurrencyCode());
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("personname", customer.getName());
                    obj.put("personemail", customer.getEmail());
                    obj.put("memo", salesOrder.getMemo());
                    obj.put("posttext", salesOrder.getPostText());
                    obj.put("costcenterid", salesOrder.getCostcenter() == null ? "" : salesOrder.getCostcenter().getID());
                    obj.put("costcenterName", salesOrder.getCostcenter() == null ? "" : salesOrder.getCostcenter().getName());
                    obj.put("billtoaddress", CommonFunctions.getBillingShippingAddress(addresses, true));
                    obj.put("shiptoaddress", CommonFunctions.getBillingShippingAddress(addresses, false));
                    obj.put("taxid", salesOrder.getTax() == null ? "" : salesOrder.getTax().getID());
                    obj.put("taxname", salesOrder.getTax() == null ? "" : salesOrder.getTax().getName());
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
                    obj.put("type", 4);//CQ->SO
                    jArr.put(obj);
                }
                //CQ Linked in Sales Invoice
                KwlReturnObject siresult = accSalesOrderDAOobj.getSILinkedWithCQ(billid, companyid);
                List<String> listsi = siresult.getEntityList();
                KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJObj.optString(Constants.globalCurrencyKey,currencyid));
                KWLCurrency currency = (KWLCurrency) curresult1.getEntityList().get(0);
                for (String orderid:listsi) {
                    JSONObject obj = new JSONObject();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), orderid);
                    Invoice invoice = (Invoice) objItr.getEntityList().get(0);
                    Customer customer = invoice.getCustomer();
                    JournalEntry je = invoice.getJournalEntry();
                    JournalEntryDetail d = invoice.getCustomerEntry();
                    Account account = d.getAccount();
                    obj.put("billid", invoice.getID());
                    obj.put("companyid", invoice.getCompany().getCompanyID());
                    obj.put("withoutinventory", "");
                    obj.put(Constants.transactionNo, invoice.getInvoiceNumber());   //delivery order no
//                    obj.put("date", df.format(invoice.getJournalEntry().getEntryDate()));  //date of invoice
                    obj.put("date", df.format(invoice.getCreationDate()));  //date of invoice
                    obj.put("journalEntryId", invoice.getJournalEntry().getID());
                    obj.put("journalEntryNo", invoice.getJournalEntry().getEntryNumber());  //journal entry no
//                    obj.put("date", df.format(je.getEntryDate()));  //date 
                    obj.put("mergedCategoryData", "Customer Invoice");  //type of data
                    obj.put("billid", invoice.getID());
                    obj.put("billno", invoice.getInvoiceNumber());
                    obj.put(Constants.IsRoundingAdjustmentApplied, invoice.isIsRoundingAdjustmentApplied());
                    obj.put("isOpeningBalanceTransaction", false);
                    obj.put("partialinv", false);
                    obj.put("personid", invoice.getCustomer() == null ? account.getID() : invoice.getCustomer().getID());// account.getID());
                    obj.put("personemail", invoice.getCustomer() == null ? "" : invoice.getCustomer().getEmail());
                    obj.put("customername", invoice.getCustomer() == null ? "" : invoice.getCustomer().getName());
                    obj.put("currencyid", currencyid);
                    obj.put("currencyidval", authHandlerDAOObj.getCurrency(paramJObj.optString(Constants.globalCurrencyKey,currencyid)));
                    obj.put("currencysymbol", (invoice.getCurrency() == null ? currency.getSymbol() : invoice.getCurrency().getSymbol()));
                    obj.put("currencyname", (invoice.getCurrency() == null ? currency.getName() : invoice.getCurrency().getName()));
                    obj.put("currencycode", (invoice.getCurrency() == null ? currency.getCurrencyCode() : invoice.getCurrency().getCurrencyCode()));
                    obj.put("companyaddress", invoice.getCompany().getAddress());
                    obj.put("isfavourite", invoice.isFavourite());
                    obj.put("billto", invoice.getBillTo());
                    obj.put("shipto", invoice.getShipTo());
                    obj.put("porefno", invoice.getPoRefNumber());
                    obj.put("journalentryid", je.getID());
                    obj.put("entryno", je.getEntryNumber());
                    obj.put("externalcurrencyrate", je.getExternalCurrencyRate());
                    obj.put("date", df.format(je.getEntryDate()));
                    obj.put("shipdate", invoice.getShipDate() == null ? "" : df.format(invoice.getShipDate()));
                    obj.put("duedate", df.format(invoice.getDueDate()));
                    obj.put("personname", invoice.getCustomer() == null ? account.getName() : invoice.getCustomer().getName());
                    obj.put("salesPerson", invoice.getMasterSalesPerson() == null ? "" : invoice.getMasterSalesPerson().getID());
                    obj.put("taxamount", invoice.getTaxEntry() == null ? 0 : invoice.getTaxEntry().getAmount());
                    obj.put("taxincluded", invoice.getTax() == null ? false : true);
                    obj.put("taxid", invoice.getTax() == null ? "" : invoice.getTax().getID());
                    obj.put("taxname", invoice.getTax() == null ? "" : invoice.getTax().getName());
                    obj.put("memo", invoice.getMemo());
                    obj.put("termname", customer == null ? "" : ((customer.getCreditTerm() == null) ? "" : customer.getCreditTerm().getTermname()));
                    obj.put("termid", customer == null ? "" : ((customer.getCreditTerm() == null) ? "" : customer.getCreditTerm().getID()));
                    obj.put("deleted", invoice.isDeleted());
                    obj.put("discount", invoice.getDiscount() == null ? 0 : invoice.getDiscount().getDiscountValue());
                    obj.put("ispercentdiscount", invoice.getDiscount() == null ? false : invoice.getDiscount().isInPercent());
                    obj.put("discountval", invoice.getDiscount() == null ? 0 : invoice.getDiscount().getDiscount());
                    obj.put("shipvia", invoice.getShipvia() == null ? "" : invoice.getShipvia());
                    obj.put("posttext", invoice.getPostText() == null ? "" : invoice.getPostText());
                    obj.put("costcenterid", (je != null ? je.getCostcenter() == null ? "" : je.getCostcenter().getID() : ""));
                    obj.put("costcenterName", (je != null ? je.getCostcenter() == null ? "" : je.getCostcenter().getName() : ""));
                    obj.put("archieve", 0);
                    obj.put("cashtransaction", invoice.isCashtransaction());
                    boolean includeprotax = false;
                    Set<InvoiceDetail> invoiceDetails = invoice.getRows();
                    for (InvoiceDetail invoiceDetail : invoiceDetails) {
                        if (invoiceDetail.getTax() != null) {
                            includeprotax = true;
                            break;
                        }
                    }
                    obj.put("includeprotax", includeprotax);
                    if (invoice.getModifiedby() != null) {
                        obj.put("lasteditedby", StringUtil.getFullName(invoice.getModifiedby()));
                    }

                    obj.put("fob", invoice.getFob() == null ? "" : invoice.getFob());
                    BillingShippingAddresses addresses = invoice.getBillingShippingAddresses();
                    AccountingAddressManager.getTransactionAddressJSON(obj, addresses, false);
                    obj.put("sequenceformatid", invoice.getSeqformat() == null ? "" : invoice.getSeqformat().getID());

                    if (invoice.isIsconsignment()) {
                        obj.put("mergedCategoryData", "Consignment Customer Invoice");  //type of data
                    } else if (invoice.isFixedAssetInvoice()) {
                        obj.put("mergedCategoryData", "Fixed Asset Disposal Invoice");  //type of data
                    } else if (invoice.isFixedAssetLeaseInvoice()) {
                        obj.put("mergedCategoryData", "Lease Invoice");  //type of data
                    } else {
                        obj.put("mergedCategoryData", "Customer Invoice");  //type of data
                    }
                    obj.put("personname", customer.getName());
                    obj.put("type", 7);//CQ->SI

                    jArr.put(obj);
                }
                //CQ Linked with Vendor Quotation
                HashMap<String, Object> requestparams = new HashMap<String, Object>();
                requestparams.put("quotationid", billid);
                requestparams.put("companyid", companyid);
                KwlReturnObject vqresult = accSalesOrderDAOobj.getVQLinkedWithCQ(requestparams);
                List vendorquotation = vqresult.getEntityList();
                if (vendorquotation != null && vendorquotation.size() > 0) {
                    jArr = getVendorQuotationJsonForLinking(jArr, vendorquotation, currency, df, companyid);
                }

                jobj.put(Constants.RES_count, jArr.length());
                jobj.put(Constants.data, jArr);
            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }
    
    public String getDeliverDayVal(int day) {
       String returnVAl = "";
       switch (day) {
                case 0:
                    returnVAl = "Every Sunday";
                    break;
                case 1:
                    returnVAl = "Every Monday";
                    break;
                case 2:
                    returnVAl = "Every Tuesday";
                    break;
                case 3:
                    returnVAl = "Every Wednesday";
                    break;
                case 4:
                    returnVAl = "Every Thursday";
                    break;
                case 5:
                    returnVAl = "Every Friday";
                    break;
                case 6:
                    returnVAl = "Every Saturday";
                    break;
                case 7:
                    returnVAl = "Next Day";
                    break;
       }
       return returnVAl;
   }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, JSONException.class, AccountingException.class,ServiceException.class})
    public JSONObject generatePOFromMultipleSO(JSONObject paramJObj) throws ServiceException, SessionExpiredException, JSONException {
        JSONObject returnObj = new JSONObject();
        JSONArray billidjArray=new JSONArray();
        DateFormat df = authHandler.getDateOnlyFormat();
        StringBuilder billidString=new StringBuilder();
        String successMsg = null;
        String failureMsg = null;
        boolean isSuccess = false;
        try {
            String companyid = paramJObj.getString(Constants.companyKey);
            String userid = paramJObj.getString(Constants.useridKey);
            String companyCurrency = paramJObj.getString(Constants.globalCurrencyKey);
            
            JSONObject sequenceFormatJSON = wsUtilService.getSequenceFormatId(paramJObj, String.valueOf(Constants.Acc_Purchase_Order_ModuleId));
            if (StringUtil.isNullOrEmpty(sequenceFormatJSON.optString(Constants.sequenceformat, null))) {
                throw ServiceException.FAILURE(messageSource.getMessage("acc.common.setSeqNoPurchaseOrder", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))), "", false);
            }
            boolean islinkedflag=false;
            HashSet<String> billidPOSet=new HashSet();
            StringBuilder billnoString = new StringBuilder();
            String records = paramJObj.optString("recs", null);
            if (!StringUtil.isNullOrEmpty(records)) {
                billidjArray = new JSONArray(records);
                for (int i = 0; i < billidjArray.length(); i++) {
                    JSONObject jobj = billidjArray.getJSONObject(i);
                    if (jobj.has(Constants.billid) && jobj.get(Constants.billid) != null) {
                        billidString.append(jobj.get(Constants.billid) + ",");
                    }
                }
                String selectedBillIds = null;
                if (billidString.length() > 0) {
                    selectedBillIds = billidString.toString();
                    selectedBillIds = selectedBillIds.substring(0, selectedBillIds.length() - 1);
                }

                if (!StringUtil.isNullOrEmpty(selectedBillIds)) {
 
                    Map<String, Object> returnHashMap = accGroupCompanyDAO.checkEntryForSalesOrderInPO(selectedBillIds,companyid);
                    if (returnHashMap.containsKey("sourcePOBillidSet") && returnHashMap.get("sourcePOBillidSet") != null) {
                        billidPOSet = (HashSet) returnHashMap.get("sourcePOBillidSet");
                        if (billidPOSet.size() > 0) {
                            islinkedflag = true;
                        }
                    }

                    if (islinkedflag) {//If it is already linked then showing the message PO is already autogenerated with selected SalesOrder
                        for (String grid : billidPOSet) {
                            KwlReturnObject poObjReturn = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), grid);
                            PurchaseOrder poObj = (PurchaseOrder) poObjReturn.getEntityList().get(0);
                            if (billnoString.length() > 0) {
                                billnoString.append(",");
                            }
                            billnoString.append(poObj.getPurchaseOrderNumber());
                        }
                        String msgBillno = billnoString.toString().substring(0, billnoString.toString().length());
                        String modifiedString = "Purchase Order <b>" + msgBillno + "</b> has already been autogenerated with selected Sales Order. Please select another record.";
                        throw ServiceException.FAILURE(modifiedString, "", false);
                    }
                    
                    if (!islinkedflag) {//If it is not autogenerated already
                        Map<String, Object> detailParams = new HashMap<String, Object>();
                        paramJObj.put(GroupCompanyProcessMapping.DESTINATION_MODULE, Constants.Acc_Purchase_Order_ModuleId);
                        paramJObj.put(GroupCompanyProcessMapping.SOURCE_MODULE, Constants.Acc_Sales_Order_ModuleId);
                        detailParams.put("selectedBillIds", selectedBillIds);
                        Map<String, Object> detailsMap = accGroupCompanyDAO.getDetailsProductsid(paramJObj, detailParams);

                        JSONArray detailsArray = new JSONArray();
                        if (detailsMap.containsKey(Constants.detail) && detailsMap.get(Constants.detail) != null) {
                            String detailsid = (String) detailsMap.get(Constants.detail);
                            detailsArray = new JSONArray(detailsid);
                        }
                        Set<String> vendorIdSet = new HashSet<String>();
                        if (detailsMap.containsKey("vendorIdList") && detailsMap.get("vendorIdList") != null) {
                            List vendorIdList = (List) detailsMap.get("vendorIdList");
                            vendorIdSet = new HashSet(vendorIdList);// Convert list into set
                        }

                        if (detailsArray.length() > 0 && vendorIdSet.size() > 0) {
                            StringBuilder msgStringBuilder = new StringBuilder();// used for msgbuilder

                            Object[] venObj = vendorIdSet.toArray();

                            for (Object vendid : venObj) {

                                JSONArray modifiedDetailJsonArray = new JSONArray();//preparing lineitemdetail for saving purchase order
                                JSONObject requestJson = new JSONObject();
                                boolean globalLevelFieldsFlag = false; // flag to check whether global fields detials have been saved successfully or not
                                boolean isGstIncluded = false;//gst included case
                                boolean includeProTax = false;//include product Tax flag
                                boolean globalLevelTax = false;//global level Tax flag
                                HashSet<String> billidsSet = new HashSet<String>();

                                String vendTaxId = null;//Vendor Tax Id
                                double subTotal = 0.0;
                                double lineLevelTax = 0.0;
                                for (int i = 0; i < detailsArray.length(); i++) {
                                    JSONObject detailsjobj = detailsArray.getJSONObject(i);
                                    if (detailsjobj.has(Constants.vendorid) && detailsjobj.get(Constants.vendorid) != null && detailsjobj.getString(Constants.vendorid).equalsIgnoreCase((String) vendid)) {
                                        JSONObject modifiedDetailJson = new JSONObject();

                                        if (!globalLevelFieldsFlag) {
                                            requestJson.put(Constants.companyKey, companyid);
                                            requestJson = wsUtilService.getSequenceFormatId(requestJson, String.valueOf(Constants.Acc_Purchase_Order_ModuleId));
                                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(requestJson.optString(Constants.companyKey), StaticValues.AUTONUM_PURCHASEORDER, requestJson.optString(Constants.sequenceformat), false, new Date());
                                            if (!seqNumberMap.isEmpty()) {
                                                requestJson.put("number", (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER));
                                            } else {
                                                requestJson.put("number", "");
                                            }

                                            requestJson.put("isEdit", "false");
                                            requestJson.put("vendor", (String) vendid);
                                            requestJson.put(Constants.useridKey, userid);
                                            requestJson.put(Constants.globalCurrencyKey, companyCurrency);
                                            requestJson.put(Constants.userfullname, paramJObj.getString(Constants.userfullname));
                                            requestJson.put(Constants.COMPANY_SUBDOMAIN, paramJObj.getString(Constants.COMPANY_SUBDOMAIN));
                                            requestJson.put(Constants.language, paramJObj.getString(Constants.language));
                                            requestJson.put(Constants.remoteIPAddress, paramJObj.getString(Constants.remoteIPAddress));
                                            requestJson.put(Constants.reqHeader, paramJObj.getString(Constants.reqHeader));

                                            KwlReturnObject vendorReturnObj = accountingHandlerDAOobj.getObject(Vendor.class.getName(), (String) vendid);
                                            Vendor vendorObj = (Vendor) vendorReturnObj.getEntityList().get(0);
                                            String vendCurr = vendorObj.getCurrency().getCurrencyID();
                                            vendTaxId = vendorObj.getTaxid();
                                            requestJson.put(Constants.currencyKey, vendCurr);

                                            if (vendorObj.getDebitTerm() != null && !StringUtil.isNullOrEmpty(vendorObj.getDebitTerm().getID())) {
                                                requestJson.put("terms", vendorObj.getDebitTerm().getID());
                                                requestJson.put("term", vendorObj.getDebitTerm().getID());
                                                requestJson.put("termid", vendorObj.getDebitTerm().getID());
                                            }
                                            if (vendCurr.equalsIgnoreCase(companyCurrency)) {
                                                requestJson.put(Constants.externalcurrencyrate, 1);
                                            } else {
                                                JSONObject exchangeRateJson = new JSONObject();
                                                exchangeRateJson.put("transactionDate", vendorObj.getCreatedOn());
                                                exchangeRateJson.put("currId", vendCurr);
                                                exchangeRateJson.put(Constants.globalCurrencyKey, companyCurrency);
                                                exchangeRateJson.put(Constants.companyKey, companyid);
                                                double exchangeRateForSpecificFields = accInvoiceServiceDAO.getExchangeRateForSpecificCurrency(exchangeRateJson);
                                                requestJson.put(Constants.externalcurrencyrate, exchangeRateForSpecificFields);
                                            }
                                            globalLevelFieldsFlag = true;
                                        }//end of globalLevelFieldsFlag

                                        SalesOrder salesOrderObj = null;
                                        if (detailsjobj.has(Constants.billid) && detailsjobj.get(Constants.billid) != null) {
                                            KwlReturnObject salesorderObj = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), (String) detailsjobj.get(Constants.billid));
                                            salesOrderObj = (SalesOrder) salesorderObj.getEntityList().get(0);
                                            if (!isGstIncluded) {
                                                isGstIncluded = salesOrderObj.isGstIncluded();
                                                if (isGstIncluded) {
                                                    includeProTax = true;
                                                }
                                            }

                                            if (salesOrderObj.getTax() != null && !StringUtil.isNullOrEmpty(salesOrderObj.getTax().getID()) && !globalLevelTax) {
                                                globalLevelTax = true;
                                                if (!StringUtil.isNullOrEmpty(vendTaxId)) {
                                                    requestJson.put(Constants.TAXID, vendTaxId);
                                                }
                                            }

                                            if (includeProTax || isGstIncluded) {
                                                globalLevelTax = false;
                                                requestJson.put(Constants.TAXID, "");
                                            }
                                        }

                                        billidsSet.add(salesOrderObj.getID());
                                        if (billidsSet.size()!=1) {
                                            requestJson.put(Constants.memo, "");
                                            requestJson.put(Constants.fob, "");
                                            requestJson.put(Constants.shipdate, df.format(new Date()));
                                            requestJson.put(Constants.posttext, "");
                                            requestJson.put(Constants.BillDate, df.format(new Date()));
                                            requestJson.put(Constants.duedate, df.format(new Date()));
                                            requestJson.put("costcenter", "");
                                            requestJson.put(Constants.shipvia,"");
                                        } else if (billidsSet.size()==1) {//for one product
                                            if (salesOrderObj != null) {
                                                requestJson.put(Constants.memo, salesOrderObj.getMemo());
                                                requestJson.put(Constants.fob, salesOrderObj.getFob());
                                                if (salesOrderObj.getShipdate() != null) {
                                                    requestJson.put(Constants.shipdate, df.format(salesOrderObj.getShipdate()));
                                                }else{
                                                    requestJson.put(Constants.shipdate,""); 
                                                }
                                                if (salesOrderObj.getOrderDate() != null) {
                                                    requestJson.put(Constants.BillDate, df.format(salesOrderObj.getOrderDate()));
                                                } else {
                                                    requestJson.put(Constants.BillDate, df.format(new Date()));
                                                }

                                                if (salesOrderObj.getDueDate() != null) {
                                                    requestJson.put(Constants.duedate, df.format(salesOrderObj.getDueDate()));
                                                } else {
                                                    requestJson.put(Constants.duedate, df.format(new Date()));
                                                }
                                                requestJson.put(Constants.posttext, salesOrderObj.getPostText());
                                                requestJson.put(Constants.shipvia, !StringUtil.isNullOrEmpty(salesOrderObj.getShipvia())?salesOrderObj.getShipvia():"");
                                                requestJson.put("costcenter", salesOrderObj.getCostcenter() != null ? salesOrderObj.getCostcenter().getID() : "");
                                            }
                                        }

                                        if (detailsjobj.has(Constants.productid) && detailsjobj.get(Constants.productid) != null) {
                                            modifiedDetailJson.put(Constants.productid, (String) detailsjobj.get(Constants.productid));
                                        }

                                        if (detailsjobj.has("detailid") && detailsjobj.get("detailid") != null) {
                                            modifiedDetailJson.put("savedrowid", (String) detailsjobj.get("detailid"));
                                            modifiedDetailJson.put("rowid", (String) detailsjobj.get("detailid"));//linkdetailid
                                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), (String) detailsjobj.get("detailid"));
                                            SalesOrderDetail sodetails = (SalesOrderDetail) rdresult.getEntityList().get(0);
                                            if (sodetails != null) {
                                                modifiedDetailJson.put(Constants.externalcurrencyrate, requestJson.optString(Constants.externalcurrencyrate));
                                                modifiedDetailJson.put("linkto", salesOrderObj.getSalesOrderNumber());
                                                modifiedDetailJson.put("rate", sodetails.getRate());
                                                modifiedDetailJson.put("rateIncludingGst", sodetails.getRateincludegst());
                                                modifiedDetailJson.put("quantity", sodetails.getQuantity());
                                                modifiedDetailJson.put("baseuomquantity", sodetails.getBaseuomquantity());
                                                modifiedDetailJson.put("baseuomrate", sodetails.getBaseuomrate());
                                                modifiedDetailJson.put("uomid", sodetails.getUom().getID());
                                                modifiedDetailJson.put("remark", sodetails.getRemark());
                                                modifiedDetailJson.put("desc", sodetails.getDescription());
                                                modifiedDetailJson.put("prdiscount", sodetails.getDiscount());
                                                modifiedDetailJson.put("discountispercent", sodetails.getDiscountispercent());

                                                int discountType = sodetails.getDiscountispercent();
                                                double prdiscount = sodetails.getDiscount();
                                                double baseuomquantity = sodetails.getBaseuomquantity();
                                                double rate = sodetails.getRate();
                                                modifiedDetailJson.put("rate", sodetails.getRate());
                                                if (isGstIncluded) {
                                                    rate = sodetails.getRateincludegst() != 0.0 ? sodetails.getRateincludegst() : rate;
                                                }
                                                modifiedDetailJson.put("rateIncludingGst", rate);
                                                double rowamount = (rate * baseuomquantity);
                                                double rowdiscountvalue = (discountType == 1) ? rowamount * prdiscount / 100 : prdiscount;
                                                rowamount = rowamount - rowdiscountvalue;
                                                subTotal += rowamount; //calculating subtotal


                                                String producttaxid = sodetails.getTax() != null ? sodetails.getTax().getID() : "";
                                                if (!StringUtil.isNullOrEmpty(producttaxid)) {
                                                    includeProTax = true;
                                                }

                                                if (includeProTax && !StringUtil.isNullOrEmpty(vendTaxId) && !StringUtil.isNullOrEmpty(producttaxid)) {
                                                    modifiedDetailJson.put("prtaxid", vendTaxId); //putting vendor tax id at linelevel
                                                    Map<String, Object> taxParams = new HashMap<String, Object>();
                                                    taxParams.put(Constants.companyKey, companyid);
                                                    taxParams.put("taxid", vendTaxId);//sodetails tax

                                                    //To get tax percent of mapped tax
                                                    KwlReturnObject taxPresult = accTaxObj.getTax(taxParams);
                                                    List<Object[]> list = taxPresult.getEntityList();
                                                    double taxpercent = 0.0;
                                                    if (list != null && !list.isEmpty()) {
                                                        for (Object[] row : list) {
                                                            taxpercent = (Double) row[1];
                                                        }
                                                    }
                                                    double taxamount = 0.0;

                                                    if (isGstIncluded) {
                                                        if (taxpercent != 0) {
                                                            rate = (rate * 100) / (100 + taxpercent);
                                                            taxamount = (taxpercent * rate) / 100;
                                                            modifiedDetailJson.put("taxamount", taxamount);
                                                        } else {
                                                            modifiedDetailJson.put("taxamount", 0);
                                                        }
                                                    } else {
                                                        if (taxpercent != 0) {
                                                            taxamount = (taxpercent * rowamount) / 100;
                                                            modifiedDetailJson.put("taxamount", taxamount);
                                                        } else {
                                                            modifiedDetailJson.put("taxamount", 0);
                                                        }
                                                    }
                                                    lineLevelTax += taxamount;
                                                } else {
                                                    modifiedDetailJson.put("prtaxid", ""); //putting vendor tax id at linelevel
                                                    modifiedDetailJson.put("taxamount", 0.0);
                                                }//end of include pro tax
                                                modifiedDetailJsonArray.put(modifiedDetailJson);
                                            }
                                        }
                                    }
                                }
                                if (requestJson.length() > 0 && modifiedDetailJsonArray.length() > 0) {//also checking lineitem details
                                    if (globalLevelTax && !StringUtil.isNullOrEmpty(vendTaxId)) {//global level tax
                                        requestJson.put("taxid", vendTaxId); //putting vendor tax id at global level 
                                        Map<String, Object> taxParams = new HashMap<String, Object>(); //calculating global level tax id
                                        taxParams.put(Constants.companyKey, companyid);
                                        taxParams.put("taxid", vendTaxId);//sodetails tax
                                        //To get tax percent of mapped tax
                                        KwlReturnObject taxPresult = accTaxObj.getTax(taxParams);
                                        List<Object[]> list = taxPresult.getEntityList();
                                        double taxpercent = 0.0;
                                        double taxamount = 0;
                                        if (list != null && !list.isEmpty()) {
                                            for (Object[] row : list) {
                                                taxpercent = (Double) row[1];
                                            }
                                        }
                                        if (taxpercent != 0) {
                                            taxamount = (taxpercent * subTotal) / 100;
                                            requestJson.put("taxamount", taxamount);
                                        } else {
                                            requestJson.put("taxamount", 0);
                                        }
                                    } else {
                                        requestJson.put("taxamount", 0);
                                    } //end of include protax
                                    requestJson.put("detail", modifiedDetailJsonArray.toString());
                                    requestJson.put("subTotal", subTotal);
                                    requestJson.put("includeprotax", String.valueOf(includeProTax));

                                    if (billidsSet.size() > 0) {
                                        //if linkedmoduleid is not null and linked document has some value
                                        StringBuilder linkNumberBuilderString = new StringBuilder();//to track linked documentsid
                                        for (String linkid : billidsSet) {
                                            if (linkNumberBuilderString.length() > 0) {
                                                linkNumberBuilderString.append("," + linkid);
                                            } else {
                                                linkNumberBuilderString.append(linkid);
                                            }
                                        }
                                        requestJson.put(GroupCompanyProcessMapping.linkedTransactionBillid, linkNumberBuilderString.toString());
                                        requestJson.put(GroupCompanyProcessMapping.LinkModule_Combo, Constants.SALESORDER);
                                    }
                                    requestJson.put(Constants.PAGE_URL, paramJObj.optString(Constants.PAGE_URL));
                                    returnObj = accPurchaseOrderModuleServiceObj.savePurchaseOrderJSON(requestJson);
                                    if (returnObj.has("billno") && !StringUtil.isNullOrEmpty(returnObj.optString("billno", null)) && returnObj.has(Constants.billid) && !StringUtil.isNullOrEmpty(returnObj.optString(Constants.billid, null))) {
                                        KwlReturnObject poreturnObj = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), returnObj.optString(Constants.billid));
                                        PurchaseOrder poObj = (PurchaseOrder) poreturnObj.getEntityList().get(0);
                                        if (poObj != null && poObj.getVendor() != null) {
                                            KwlReturnObject vendorReturnObj = accountingHandlerDAOobj.getObject(Vendor.class.getName(), poObj.getVendor().getID());
                                            Vendor vendorObj = (Vendor) vendorReturnObj.getEntityList().get(0);
                                            if (vendorObj != null) {
                                                if (msgStringBuilder.length() > 0) {
                                                    msgStringBuilder.append(" and "); //showing message for multiple vendors along with name 
                                                }
                                                msgStringBuilder.append("Purchase Order " + returnObj.optString("billno") + " with Vendor " + vendorObj.getName());
                                                isSuccess = true;
                                            } else {
                                                throw ServiceException.FAILURE(messageSource.getMessage("acc.groupCompany.consolidate.error", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))), "", false);
                                            }//end of vendor obj null
                                        }//end of poObj != null && poObj.getVendor() != null
                                    }//end of billid & billno if block check
                                }//end of requestJson.length() > 0 && modifiedDetailJsonArray.length() > 0
                            } //end of for (Object vendid : venObj)
                            successMsg = messageSource.getMessage("acc.po.save1", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))) + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))) + ": <b>" + msgStringBuilder.toString() + "</b>";
                        }// end of if (detailsArray.length() > 0 && vendorIdSet.size() > 0
                        else {
                            throw ServiceException.FAILURE("Products are not mapped to any Vendor.", "", false);
                    }
                    }
                }//end of if (!StringUtil.isNullOrEmpty(selectedBillIds)
            } else {
                throw ServiceException.FAILURE(messageSource.getMessage("acc.groupCompany.consolidate.error", null, Locale.forLanguageTag(paramJObj.getString(Constants.language))), "", false);
            }
        } catch (ServiceException ex) {
            failureMsg=ex.getMessage();
        } catch (Exception ex) {
            failureMsg=ex.getMessage();
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnObj.put(Constants.RES_msg,!StringUtil.isNullOrEmpty(successMsg)?successMsg:(!StringUtil.isNullOrEmpty(failureMsg)?failureMsg:""));
            returnObj.put(Constants.RES_success, isSuccess);
        }
        return returnObj;
    }
    public JSONArray getSalesOrdersJson(HashMap<String, Object> requestParams, JSONArray dataJArr,JSONObject paramJobj) throws ServiceException {
        KwlReturnObject result = null;
        try {
            result = accSalesOrderDAOobj.getSalesOrdersMerged(requestParams);
            dataJArr = getSalesOrdersJsonMerged(paramJobj, result.getEntityList(), dataJArr);
        } catch (Exception ex) {

        }
        return dataJArr;
    }
}
