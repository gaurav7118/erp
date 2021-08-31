/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.repeatedtransaction;

import com.krawler.accounting.integration.common.IntegrationCommonService;
import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.AccountingAddressManager;
import com.krawler.common.admin.BillingShippingAddresses;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.RepeatedInvoices;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Cheque;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.ExcludedOutstandingOrders;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.InvoiceContractMapping;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.InvoiceTermsMap;
import com.krawler.hql.accounting.InvoiceTermsSales;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.RepeatedJE;
import com.krawler.hql.accounting.RepeatedJEChequeDetail;
import com.krawler.hql.accounting.RepeatedJEMemo;
import com.krawler.hql.accounting.RepeatedSalesOrder;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.SalesOrderDetail;
import com.krawler.hql.accounting.SalesOrderTermMap;
import com.krawler.hql.accounting.SequenceFormat;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.Term;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.discount.accDiscountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.JournalEntryConstants;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderService;
import com.krawler.hql.accounting.InvoiceDetailTermsMap;
import com.krawler.hql.accounting.SalesOrderDetailTermMap;
import com.krawler.hql.accounting.invoice.service.AccInvoiceModuleService;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerControllerCMNService;
import com.krawler.spring.accounting.customer.accCustomerControllerCMNServiceImpl;
import com.krawler.spring.accounting.entitygst.AccEntityGstDao;
import com.krawler.spring.accounting.entitygst.AccEntityGstService;
import java.util.ArrayList;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class AccRepeateInvoiceServiceImpl implements MessageSourceAware, AccRepeateInvoiceService {

    private accInvoiceDAO accInvoiceDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDiscountDAO accDiscountobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private MessageSource messageSource;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccJournalEntryModuleService journalEntryModuleServiceobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private authHandlerDAO authHandlerDAOObj;
    private auditTrailDAO auditTrailObj;
    private AccInvoiceModuleService accInvoiceModuleServiceObj;
    private accSalesOrderService accSalesOrderServiceobj;
    private fieldDataManager fieldDataManagercntrl;
    private AccEntityGstService accEntityGstService;
    private AccEntityGstDao accEntityGstDao;
    private accCurrencyDAO accCurrencyDAOobj;
    private accCustomerControllerCMNService accCustomerControllerCMNServiceObj;
    private IntegrationCommonService integrationCommonService;

    public void setIntegrationCommonService(IntegrationCommonService integrationCommonService) {
        this.integrationCommonService = integrationCommonService;
    }

    public AccInvoiceModuleService getAccInvoiceModuleServiceObj() {
        return accInvoiceModuleServiceObj;
    }
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    public void setAccInvoiceModuleServiceObj(AccInvoiceModuleService accInvoiceModuleServiceObj) {
        this.accInvoiceModuleServiceObj = accInvoiceModuleServiceObj;
    }

    public accSalesOrderService getaccSalesOrderServiceObj() {
        return accSalesOrderServiceobj;
    }

    public void setAccSalesOrderServiceobj(accSalesOrderService accSalesOrderServiceobj) {
        this.accSalesOrderServiceobj = accSalesOrderServiceobj;
    }

    public AccCommonTablesDAO getAccCommonTablesDAO() {
        return accCommonTablesDAO;
    }

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setaccDiscountDAO(accDiscountDAO accDiscountobj) {
        this.accDiscountobj = accDiscountobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setJournalEntryModuleServiceobj(AccJournalEntryModuleService journalEntryModuleServiceobj) {
        this.journalEntryModuleServiceobj = journalEntryModuleServiceobj;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    public void setAccEntityGstService(AccEntityGstService accEntityGstService) {
        this.accEntityGstService = accEntityGstService;
    }

    public void setAccEntityGstDao(AccEntityGstDao accEntityGstDao) {
        this.accEntityGstDao = accEntityGstDao;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccCustomerControllerCMNServiceObj(accCustomerControllerCMNService accCustomerControllerCMNServiceObj) {
        this.accCustomerControllerCMNServiceObj = accCustomerControllerCMNServiceObj;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {ServiceException.class, AccountingException.class})
    public String processSalesOrder(SalesOrder SalesOrderObj, HashMap<String, Object> requestParams) throws ServiceException {
        String msg = "", failed = "";
        try {
            String entryno = "";
            User adminUser = null;
            SalesOrder repeatedSO = repeateSalesOrder(SalesOrderObj);
            msg += repeatedSO.getSalesOrderNumber() + ",";
            adminUser = repeatedSO.getCompany().getCreator() != null ? repeatedSO.getCompany().getCreator() : repeatedSO.getCreatedby();
            entryno = repeatedSO.getSalesOrderNumber();
            if (SalesOrderObj.getRepeateSO() != null) {
                updateRepeateInfoForSO(SalesOrderObj.getRepeateSO());
            }
            //Add Audit Trail Entry for SO
            requestParams.put("userid", adminUser.getUserID());
//                auditTrailObj.insertRecurringAuditLog(AuditAction.ADD_RECURRING_SO_ENTRY, "System has generated a recurring Sales Order - " + entryno, request, entryno, adminUser);
            auditTrailObj.insertAuditLog(AuditAction.ADD_RECURRING_SO_ENTRY, "System has generated a recurring Sales Order - " + entryno, requestParams, entryno);
        } catch (Exception ex) {
            failed += SalesOrderObj.getSalesOrderNumber() + "[" + SalesOrderObj.getID() + "][B]: " + ex.getMessage() + ";";
            System.out.println("\n***** Exception in Recurring Sales Order:*****\n" + failed);
            //SDP-9651 Sends mail to Sagar A. Sir in case of Recurring failure
            HashMap<String, Object> exMailParams = new HashMap();
            String baseURLFormat=ConfigReader.getinstance().get("base_urlformat");
            exMailParams.put("documentObj", SalesOrderObj);
            exMailParams.put("failed", "\n" + failed+" URL : "+baseURLFormat);
            getExceptionMail(exMailParams);

            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        }
        return msg;
    }

    public SalesOrder repeateSalesOrder(SalesOrder SalesOrderObj) throws ServiceException {
        SalesOrder repeatedSO = null;
        HashMap<String, Object> soDataMap = new HashMap<String, Object>();
        try {
            String companyid = SalesOrderObj.getCompany().getCompanyID();
            String currencyid = SalesOrderObj.getCurrency().getCurrencyID();
            String Memo = "";
            soDataMap.put("customerid", SalesOrderObj.getCustomer() == null ? "" : SalesOrderObj.getCustomer().getID());
            String nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_SALESORDER);

            boolean nextInv = false;
            SequenceFormat prevSeqFormat = null;
            String nextAutoNoInt = "";
            String datePrefix = "";
            String dateafterPrefix = "";
            String dateSuffix = "";
            String entryNumber = "";//nextAutoNo;
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) returnObject.getEntityList().get(0);
            if (extraPref != null && extraPref.isDefaultsequenceformatforrecinv() && SalesOrderObj.getSeqformat() != null && SalesOrderObj.getSeqformat().isIsactivate()) {
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_SALESORDER, SalesOrderObj.getSeqformat().getID(), false, SalesOrderObj.getOrderDate());
                nextAutoNo = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                nextAutoNoInt = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                dateafterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part

                String invoiceSeqFormatId = SalesOrderObj.getSeqformat().getID();
                soDataMap.put(Constants.SEQFORMAT, invoiceSeqFormatId);
                soDataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                soDataMap.put(Constants.DATEPREFIX, datePrefix);
                soDataMap.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                soDataMap.put(Constants.DATESUFFIX, dateSuffix);
                entryNumber = nextAutoNo;
            } else {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("parentSOId", SalesOrderObj.getID());
                KwlReturnObject details = accSalesOrderDAOobj.getRepeateSalesOrderDetails(requestParams);
                List detailsList = details.getEntityList();
                int repInvoiceCount = detailsList.size() + Constants.RECURRING_INVOICE_01_APPEND_START_FROM;
                while (nextInv == false) {
                    entryNumber = SalesOrderObj.getSalesOrderNumber() + "-" + repInvoiceCount;
                    details = accSalesOrderDAOobj.getSalesOrderCount(entryNumber, companyid);
                    int nocount = details.getRecordTotalCount();
                    if (nocount > 0) {
                        repInvoiceCount++;
                        continue;
                    } else {
                        nextInv = true;
                    }
                }
            }
            int noOfRemainpost = SalesOrderObj.getRepeateSO().getNoOfRemainSOpost();
            noOfRemainpost++;
            try {
                HashMap<String, Object> requestParamsMemo = new HashMap<String, Object>();
                requestParamsMemo.put("repeatedJEMemoID", SalesOrderObj.getRepeateSO().getId());
                requestParamsMemo.put("noOfJERemainpost", noOfRemainpost);
                requestParamsMemo.put("columnName", "RepeatedSOID");
                KwlReturnObject RepeatedJEMemo = accJournalEntryobj.getRepeateJEMemo(requestParamsMemo);
                RepeatedJEMemo RM = (RepeatedJEMemo) RepeatedJEMemo.getEntityList().get(0);
                Memo = RM.getMemo();
                if (StringUtil.isNullOrEmpty(Memo)) {
                    Memo = SalesOrderObj.getMemo();
                }
            } catch (Exception ex) {
                Memo = SalesOrderObj.getMemo();
            }
            soDataMap.put("entrynumber", entryNumber);
            soDataMap.put("autogenerated", nextAutoNo.equals(entryNumber));
            soDataMap.put("memo", Memo);
            soDataMap.put("isOpeningBalanceOrder", SalesOrderObj.isIsOpeningBalanceSO());
            soDataMap.put("billto", SalesOrderObj.getBillTo());
            soDataMap.put("shipaddress", SalesOrderObj.getShipTo());
            soDataMap.put("companyid", companyid);
            soDataMap.put("currencyid", currencyid);
            soDataMap.put("perDiscount", SalesOrderObj.isPerDiscount());
            soDataMap.put("discount", SalesOrderObj.getDiscount());
            soDataMap.put("orderdate", SalesOrderObj.getOrderDate());
            soDataMap.put("posttext", SalesOrderObj.getPostText());
            soDataMap.put("billto", SalesOrderObj.getBillTo());
            soDataMap.put("shipvia", SalesOrderObj.getShipvia());
            soDataMap.put("fob", SalesOrderObj.getFob());
            if (SalesOrderObj.getCostcenter() != null) {
                soDataMap.put("costCenterId", SalesOrderObj.getCostcenter().getID());
            }
            soDataMap.put("isfavourite", SalesOrderObj.isFavourite());
            if (SalesOrderObj.getSalesperson() != null) {
                soDataMap.put("salesPerson", SalesOrderObj.getSalesperson().getID());
            }
            String addressID = "";
            if (extraPref != null && extraPref.isPickAddressFromMaster()) {
                Map<String, Object> addressParams = new HashMap<String, Object>();
                addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(SalesOrderObj.getCustomer().getID(), companyid, accountingHandlerDAOobj);
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                if (bsa != null) {
                    addressID = bsa.getID();
                }
            } else {
                addressID = SalesOrderObj.getBillingShippingAddresses() != null ? SalesOrderObj.getBillingShippingAddresses().getID() : "";
            }

            if (!StringUtil.isNullOrEmpty(addressID)) {
                soDataMap.put("billshipAddressid", addressID);
            }

            //Find company's timezone   ERP-16203
            String companyTZDiff = SalesOrderObj.getCompany().getTimeZone().getDifference();
            DateFormat compdf = authHandler.getCompanyTimezoneDiffFormat(companyTZDiff);
            SimpleDateFormat sd = new SimpleDateFormat("MMMM d, yyyy");

            Calendar cal = Calendar.getInstance();
//            cal.set(Calendar.HOUR_OF_DAY, 12);    //ERP-16203
//            cal.set(Calendar.MINUTE, 00);
//            cal.set(Calendar.SECOND, 00);
            Date BillDate = cal.getTime();

            Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(BillDate);
            if (SalesOrderObj.getCustomer() != null) {
                if (SalesOrderObj.getCustomer().getCreditTerm() != null) {
                    int termDays = SalesOrderObj.getCustomer().getCreditTerm().getTermdays();
                    dueDate.add(Calendar.DATE, termDays);
                }
            }          
            if (SalesOrderObj.getShipdate() != null) {
                soDataMap.put("shipdate", SalesOrderObj.getShipdate());
            }           
            soDataMap.put("duedate", sd.parse(compdf.format(dueDate.getTime())));
            User createdby = SalesOrderObj.getCreatedby();
            if (createdby != null) {
                soDataMap.put("createdby", createdby.getUserID());
            }
            User modifiedby = SalesOrderObj.getModifiedby();
            if (modifiedby != null) {
                soDataMap.put("modifiedby", modifiedby.getUserID());
            }
            Tax TAX = SalesOrderObj.getTax();
            if (TAX != null) {
                soDataMap.put("taxid", TAX.getID());
            }
            Term termid = SalesOrderObj.getTerm();
            if (termid != null) {
                soDataMap.put("termid", termid.getID());
            }
            if (SalesOrderObj.getRepeateSO() != null) {
                String calString = authHandler.getDateOnlyFormat().format(SalesOrderObj.getRepeateSO().getNextDate());
                BillDate = authHandler.getDateOnlyFormat().parse(calString);
                Date entryDate = sd.parse(compdf.format(BillDate));
                soDataMap.put("orderdate", entryDate);
            }
            soDataMap.put("totalAmount",SalesOrderObj.getTotalamount());
            soDataMap.put("totalAmountInBase",SalesOrderObj.getTotalamountinbase());
            soDataMap.put(Constants.isApplyTaxToTerms, SalesOrderObj.isApplyTaxToTerms());
            KwlReturnObject result = accSalesOrderDAOobj.saveSalesOrder(soDataMap);
            repeatedSO = (SalesOrder) result.getEntityList().get(0);//Create sales order without sales order-details.
            if (SalesOrderObj.getSoCustomData() != null) {
                 int NoOFRecords = accSalesOrderDAOobj.saveCustomDataForRecurringSO(repeatedSO.getID(), SalesOrderObj.getID(), true);
            }
            Set SALES_ORDER_DETAILS = SalesOrderObj.getRows();
            HashSet<SalesOrderDetail> sodetails = new HashSet<SalesOrderDetail>();
            Iterator itr = SALES_ORDER_DETAILS.iterator();
            /**
             * Check GST history for customer or product tax class changed nor not
             * This case apply for India only.
             */
            boolean isGSThisChanged = false;
            int uniqueCase = 0;
            JSONArray dimArr = new JSONArray();

            /**
             * Indian GST related code which includes logic for history.
             */
            if (repeatedSO.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                String productids = "";
                while (itr.hasNext()) {
                    SalesOrderDetail ivd = (SalesOrderDetail) itr.next();
                    productids += "'" + ivd.getProduct().getID() + "',";
                }
                /**
                 * Check whether GST history changed or not.
                 */
                JSONObject tempParams = new JSONObject();
                tempParams.put("masterid", SalesOrderObj.getCustomer().getID());
                tempParams.put("isCustomer", true);
                tempParams.put("productids", productids);
                tempParams.put("applydate", authHandler.getDateOnlyFormat().format(SalesOrderObj.getOrderDate()));
                tempParams.put("transactiondate", authHandler.getDateOnlyFormat().format(new Date()));
                JSONObject temp = accEntityGstService.getGSTFieldsChangedStatus(tempParams);
                isGSThisChanged = temp.optBoolean("isdatachanged", false);

                JSONObject jSONObject = new JSONObject();
                if (isGSThisChanged) {
                    /**
                     * If History changed then get customer GST details as per current date.
                     */
                    tempParams.put("customerid", SalesOrderObj.getCustomer().getID());
                    tempParams.put("returnalldata", true);
                    tempParams.put("isfortransaction", true);
                    tempParams.put("transactiondate", authHandler.getDateOnlyFormat().format(new Date()));
                    jSONObject = accCustomerControllerCMNServiceObj.getCustomerGSTHistory(tempParams);
                    jSONObject = jSONObject.optJSONArray("data").optJSONObject(0);
                    uniqueCase = jSONObject.optInt("uniqueCase");
                } else {
                    /**
                     * IF history does not changed then get customer details from parent document.
                     */
                    jSONObject.put("refdocid", SalesOrderObj.getID());
                    fieldDataManagercntrl.getGSTDocumentHistory(jSONObject);
                }
                /**
                 * Save Customer GST details at document level.
                 */
                jSONObject.remove("gstdochistoryid");
                jSONObject.put("docid", repeatedSO.getID());
                jSONObject.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                fieldDataManagercntrl.createRequestMapToSaveDocHistory(jSONObject);
                if (isGSThisChanged) {
                    /**
                     * Create dimension array to calculate GST for each product
                     * i.e Using State and Entity value.
                     */
                    dimArr = createDimensionArrayToCalculateGSTForSO(SalesOrderObj,companyid);
                }
            }

            repeatedSO.setApprovestatuslevel(SalesOrderObj.getApprovestatuslevel()); //Set approval status for recurring SO
            //Sales Order Details
            double finaltaxamt = 0d;
            itr = SALES_ORDER_DETAILS.iterator();
            while (itr.hasNext()) {
                SalesOrderDetail ivd = (SalesOrderDetail) itr.next();
                HashMap<String, Object> requestParamsSODetails = new HashMap<String, Object>();
                requestParamsSODetails.put("soid", repeatedSO.getID());
                requestParamsSODetails.put("srno", ivd.getSrno());
                requestParamsSODetails.put("rate", ivd.getRate());
                requestParamsSODetails.put("uomid", ivd.getUom() != null ? ivd.getUom().getID() : "");
                requestParamsSODetails.put("baseuomquantity", ivd.getBaseuomquantity());
                requestParamsSODetails.put("baseuomrate", ivd.getBaseuomrate());
                requestParamsSODetails.put("quantity", ivd.getQuantity());
                requestParamsSODetails.put("remark", ivd.getRemark());
                requestParamsSODetails.put("desc", ivd.getDescription());
                requestParamsSODetails.put("discount", ivd.getDiscount());
                requestParamsSODetails.put("discountispercent", ivd.getDiscountispercent());
                requestParamsSODetails.put("remark", ivd.getRemark());
                if (ivd.getProduct() != null) {
                    requestParamsSODetails.put("productid", ivd.getProduct().getID());
                }
                requestParamsSODetails.put("companyid", ivd.getCompany().getCompanyID());
                if (ivd.getTax() != null) {
                    requestParamsSODetails.put("rowtaxid", ivd.getTax().getID());
                }
                requestParamsSODetails.put("rowTaxAmount", ivd.getRowTaxAmount());
                if (ivd.getQuotationDetail() != null) {
                    requestParamsSODetails.put("quotationdetailid", ivd.getQuotationDetail().getID());
                }
                requestParamsSODetails.put("recTermAmount", ivd.getRowtermamount());
                result = accSalesOrderDAOobj.saveSalesOrderDetails(requestParamsSODetails);
                SalesOrderDetail SOD = (SalesOrderDetail) result.getEntityList().get(0);
                if (ivd.getSoDetailCustomData() != null) {
                    int NoOFRecords = accSalesOrderDAOobj.saveCustomDataForRecurringSO(SOD.getID(), ivd.getID(), false);
                }
                HashMap<String, Object> requestParamsSODetails1 = new HashMap<String, Object>();
                requestParamsSODetails1.put("id", SOD.getID());
                requestParamsSODetails1.put("salesordercustomdataref", SOD.getID());
                KwlReturnObject result1 = accSalesOrderDAOobj.saveSalesOrderDetails(requestParamsSODetails1);
                SalesOrderDetail jed1 = (SalesOrderDetail) result1.getEntityList().get(0);
                sodetails.add(jed1);
                if (extraPref.getLineLevelTermFlag() == 1) {
                    /**
                     * We save SalesOrderDetailTermMap in case When Avalara Integration is Disabled
                     */
                    if (!extraPref.isAvalaraIntegration()) {
                        JSONObject jSONObject = new JSONObject();
                        if (isGSThisChanged) {
                            /**
                             * If history changed then calculate Term for each product
                             * Save in the term table.
                             */

                            double quantity = ivd.getQuantity();
                            double rate = ivd.getRate();
                            double discount = ivd.getDiscount();
                            double subtotal = quantity * rate;
                            if (ivd.getDiscountispercent() == 1) {
                                discount = (subtotal * ivd.getDiscount()) / 100;
                            }
                            subtotal = subtotal - discount;
                            HashMap<String, Object> requestParams = new HashMap();
                            requestParams.put("df", authHandler.getOnlyDateFormat());
                            jSONObject.put("productids", ivd.getProduct().getID());
                            jSONObject.put("transactiondate", authHandler.getOnlyDateFormat().format(new Date()));
                            jSONObject.put("termSalesOrPurchaseCheck", true);
                            jSONObject.put("uniqueCase", uniqueCase);
                            jSONObject.put("dimArr", dimArr);
                            jSONObject.put("companyid", companyid);
                            jSONObject = accEntityGstService.getGSTForProduct(jSONObject, requestParams);
                            if (jSONObject.optBoolean("success")) {
                                String prodTermArray = jSONObject.optJSONArray("prodTermArray").getJSONObject(0).optString("LineTermdetails");
                                String taxclass = jSONObject.optJSONArray("prodTermArray").getJSONObject(0).optString("taxclass");
                                JSONArray termarr = new JSONArray(prodTermArray);
                                double rowttermamount = 0d;
                                    for (int i = 0; i < termarr.length(); i++) {
                                        JSONObject termObj = termarr.optJSONObject(i);
                                        JSONObject jedTermjson = new JSONObject();
                                        double termamt = subtotal * termObj.optDouble("taxvalue") / 100;
                                        termamt = authHandler.round(termamt, companyid);
                                        rowttermamount += termamt;
                                        termObj.put("assessablevalue", subtotal);
                                        termObj.put("termamount", termamt);
                                        HashMap<String, Object> salesOrderDetailsTermsMap = new HashMap<String, Object>();
                                        salesOrderDetailsTermsMap.put("term", termObj.optString("termid"));
                                        salesOrderDetailsTermsMap.put("termamount", termObj.optDouble("termamount"));
                                        salesOrderDetailsTermsMap.put("termpercentage", termObj.optDouble("taxvalue"));
                                        salesOrderDetailsTermsMap.put("taxtype", termObj.optInt("taxtype"));
                                        salesOrderDetailsTermsMap.put("salesOrderDetailID", jed1.getID());
                                        salesOrderDetailsTermsMap.put("productentitytermid", termObj.optString("productentitytermid"));
                                        salesOrderDetailsTermsMap.put("product", jed1.getProduct().getID());
                                        salesOrderDetailsTermsMap.put(Constants.useridKey, SalesOrderObj.getCreatedby().getUserID());
                                        accSalesOrderDAOobj.saveSalesOrderDetailsTermMap(salesOrderDetailsTermsMap);
                                    }
                                jSONObject = new JSONObject();
                                jSONObject.put("taxclass", taxclass);
                                jed1.setRowtermamount(rowttermamount);
                                finaltaxamt += rowttermamount;
                            }
                        } else {

                            /**
                             * Save GST details in Term table by coping from parent table.
                             */
                                HashMap<String, Object> quotationDetailParams = new HashMap<String, Object>();
                                quotationDetailParams.put("salesOrderDetailId", ivd.getID());
                                KwlReturnObject salesOrderTermMapresult = accSalesOrderDAOobj.getSalesOrderDetailTermMap(quotationDetailParams);
                                List<SalesOrderDetailTermMap> salesOrderDetailTermsMapList = salesOrderTermMapresult.getEntityList();
                                for (SalesOrderDetailTermMap salesOrderDetailTermMap : salesOrderDetailTermsMapList) {
                                    HashMap<String, Object> salesOrderDetailsTermsMap = new HashMap<String, Object>();
                                    salesOrderDetailsTermsMap.put("term", salesOrderDetailTermMap.getTerm().getId());
                                    salesOrderDetailsTermsMap.put("termamount", salesOrderDetailTermMap.getTermamount());
                                    salesOrderDetailsTermsMap.put("termpercentage", salesOrderDetailTermMap.getPercentage());
                                    salesOrderDetailsTermsMap.put("purchasevalueorsalevalue", salesOrderDetailTermMap.getPurchaseValueOrSaleValue());
                                    salesOrderDetailsTermsMap.put("deductionorabatementpercent", salesOrderDetailTermMap.getDeductionOrAbatementPercent());
                                    salesOrderDetailsTermsMap.put("taxtype", salesOrderDetailTermMap.getTaxType());
                                    salesOrderDetailsTermsMap.put("termamount", salesOrderDetailTermMap.getTermamount());
                                    salesOrderDetailsTermsMap.put("salesOrderDetailID", jed1.getID());
                                    salesOrderDetailsTermsMap.put("isDefault", salesOrderDetailTermMap.isIsGSTApplied());
                                    salesOrderDetailsTermsMap.put("productentitytermid", salesOrderDetailTermMap.getEntitybasedLineLevelTermRate().getId());
                                    salesOrderDetailsTermsMap.put("product", jed1.getProduct().getID());
                                    salesOrderDetailsTermsMap.put(Constants.useridKey, SalesOrderObj.getCreatedby().getUserID());
                                    accSalesOrderDAOobj.saveSalesOrderDetailsTermMap(salesOrderDetailsTermsMap);
                                }
                            /**
                             * Get tax class details from parent document.
                             */
                            jSONObject.put("refdocid", ivd.getID());
                            fieldDataManagercntrl.getGSTTaxClassHistory(jSONObject);
                        }
                        /**
                         * Save GST History Tax Class.
                         */
                        jSONObject.remove("taxclasshistoryid");
                        jSONObject.put("detaildocid", jed1.getID());
                        jSONObject.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                        fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jSONObject);
                    }
                }
            }
            HashMap<String, Object> termParam = new HashMap();
            termParam.put("salesOrder", SalesOrderObj.getID());
            KwlReturnObject salesOrderResult = accSalesOrderDAOobj.getSalesOrderTermMap(termParam);

            JSONArray invoiceTermsMapJarr = new JSONArray();
            List<SalesOrderTermMap> termMap = salesOrderResult.getEntityList();
            for (SalesOrderTermMap SalesOrderTermsMap : termMap) {
                JSONObject SalesOrderTermsMapJson = new JSONObject();
                InvoiceTermsSales invoiceTermsSalesObj = SalesOrderTermsMap.getTerm();
                SalesOrderTermsMapJson.put("id", invoiceTermsSalesObj.getId());
                SalesOrderTermsMapJson.put("termamount", SalesOrderTermsMap.getTermamount());
                SalesOrderTermsMapJson.put("termpercentage", SalesOrderTermsMap.getPercentage());
                SalesOrderTermsMapJson.put("termtax", SalesOrderTermsMap.getTermtax()!= null ? SalesOrderTermsMap.getTermtax().getID() : "");
                SalesOrderTermsMapJson.put("termamountinbase", SalesOrderTermsMap.getTermamountinbase());
                SalesOrderTermsMapJson.put("termtaxamount", SalesOrderTermsMap.getTermtaxamount());
                SalesOrderTermsMapJson.put("termtaxamountinbase", SalesOrderTermsMap.getTermtaxamountinbase());
                SalesOrderTermsMapJson.put("termAmountExcludingTax", SalesOrderTermsMap.getTermAmountExcludingTax());
                SalesOrderTermsMapJson.put("termAmountExcludingTaxInBase", SalesOrderTermsMap.getTermAmountExcludingTaxInBase());
                
                invoiceTermsMapJarr.put(SalesOrderTermsMapJson);
            }

            accSalesOrderServiceobj.mapInvoiceTerms(invoiceTermsMapJarr.toString(), repeatedSO.getID(), SalesOrderObj.getCreatedby().getUserID(), false);
            JSONObject json = new JSONObject();
            json.put("SOid", repeatedSO.getID());
            json.put("parentid", SalesOrderObj.getID());
            json.put("salesordercustomdataref", repeatedSO.getID());
            result = accSalesOrderDAOobj.updateSalesOrder(json, sodetails);
            
            if (extraPref.getLineLevelTermFlag() == 1 && extraPref.isAvalaraIntegration()) {
                /**
                 * When Avalara Integration is enabled, we save tax details in
                 * Avalara tax mapping table'TransactionDetailAvalaraTaxMapping' So
                 * there is no need to save SalesOrderDetailTermMap in case Avalara
                 * Integration is enabled
                 */
                JSONObject paramsJobj = new JSONObject();
                paramsJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
                paramsJobj.put(Constants.companyKey, companyid);
                if (integrationCommonService.isTaxCalculationEnabled(paramsJobj)) {
                    paramsJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
                    accSalesOrderServiceobj.saveTaxToAvalara(paramsJobj, repeatedSO, repeatedSO.getSalesOrderNumber(), companyid, "");
                }
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        } catch (AccountingException ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        }

        return repeatedSO;
    }
    public JSONArray createDimensionArrayToCalculateGSTForSO(SalesOrder SalesOrderObj, String companyid) throws JSONException, ServiceException {
        JSONArray dimArr = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        JSONObject tempParams = new JSONObject();
        tempParams.put("companyid", companyid);
        tempParams.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
        List returnList = accEntityGstDao.getGSTDimensionsDetailsForGSTCalculations(tempParams);
        Iterator iterator = returnList.iterator();
        String selectstate = "";
        String selectentity = "";
        while (iterator.hasNext()) {
            jSONObject = new JSONObject();
            Object[] object = (Object[]) iterator.next();
            int gstconfigtype = Integer.parseInt(object[2].toString());
            jSONObject.put("fieldname", (String) object[0]);
            jSONObject.put("gstmappingcolnum", Integer.parseInt(object[3].toString()));
            jSONObject.put("gstconfigtype", Integer.parseInt(object[2].toString()));
            dimArr.put(jSONObject);
            if (gstconfigtype == 1) {
                selectentity = "Col" + Integer.parseInt(object[1].toString());
            } else {
                selectstate = "Col" + Integer.parseInt(object[1].toString());
            }
        }
        /**
         * Get data from custom tables.
         */
        tempParams.put("selectstate", selectstate);
        tempParams.put("selectentity", selectentity);
        tempParams.put("customtable", "salesordercustomdata");
        tempParams.put("primarykey", "soID");
        tempParams.put("primaryid", SalesOrderObj.getID());
        returnList = accEntityGstDao.getGSTDimensionDataFromCustomTableForGSTCalculations(tempParams);
        iterator = returnList.iterator();
        while (iterator.hasNext()) {
            Object[] object = (Object[]) iterator.next();
            for (int i = 0; i < dimArr.length(); i++) {
                jSONObject = dimArr.optJSONObject(i);
                if (jSONObject.optInt("gstconfigtype") == 1) {
                    dimArr.getJSONObject(i).put("dimvalue", (String) object[0]);
                } else {
                    dimArr.getJSONObject(i).put("dimvalue", (String) object[1]);
                }
            }
        }
        return dimArr;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {ServiceException.class, AccountingException.class, Exception.class})
    public String processSalesInvoice(Invoice invoice, HashMap<String, Object> requestParams, Iterator Excludeditr, List ExcludedIDlist) throws ServiceException, SessionExpiredException {
        String msg = "", failed = "";
        boolean excludeFlag = false;
        String entryno = "";
        int contractstatus = 0;
        User adminUser = null;
        try {
            Set<InvoiceContractMapping> contractMappings = invoice.getContractMappings();
            if (contractMappings != null && !contractMappings.isEmpty()) {
                for (InvoiceContractMapping invoiceContractMapping : contractMappings) {
                    if (invoiceContractMapping.getContract() != null && invoiceContractMapping.getContract().getCstatus() == 2) {
                        contractstatus = invoiceContractMapping.getContract().getCstatus();
                    }
                }
            }
            if (contractstatus == 2 || contractstatus == 3) {
                excludeFlag = true;
            }

            Excludeditr = ExcludedIDlist.iterator();
            while (Excludeditr.hasNext()) {
                ExcludedOutstandingOrders ExcludedObj = (ExcludedOutstandingOrders) Excludeditr.next();
                if (ExcludedObj.getInvoice().getID().equals(invoice.getID())) {
                    excludeFlag = true;
                    break;
                }
            }
            if (!excludeFlag) {
                Invoice repeatedInvoice = repeateInvoice(invoice);
                msg += repeatedInvoice.getInvoiceNumber() + ",";
                adminUser = repeatedInvoice.getCompany().getCreator() != null ? repeatedInvoice.getCompany().getCreator() : repeatedInvoice.getCreatedby();
                entryno = repeatedInvoice.getInvoiceNumber();
                if (invoice.getRepeateInvoice() != null) {
                    updateRepeateInfo(invoice.getRepeateInvoice());
                }
            }
            //Add Audit Trail Entry for SI
            requestParams.put("userid", adminUser.getUserID());
//                auditTrailObj.insertRecurringAuditLog(AuditAction.ADD_RECURRING_SALES_INVOICE_ENTRY, "System has generated a recurring Sales Invoice - " + entryno, request, entryno, adminUser);
            auditTrailObj.insertAuditLog(AuditAction.ADD_RECURRING_SALES_INVOICE_ENTRY, "System has generated a recurring Sales Invoice - " + entryno, requestParams, entryno);
        } catch (ServiceException ex) {
            failed += invoice.getInvoiceNumber() + "[" + invoice.getID() + "]: " + ex.getMessage() + ";";
            HashMap<String, Object> exMailParams = new HashMap();                 //SDP-9651 Sends mail to Sagar A. Sir in case of Recurring failure
            String baseURLFormat=ConfigReader.getinstance().get("base_urlformat");
            exMailParams.put("documentObj", invoice);
            exMailParams.put("failed", "\n" + failed+"URL : "+baseURLFormat);
            getExceptionMail(exMailParams);
            System.out.println("\n***** Exception in Recurring Invoices Entry:*****\n" + failed);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return msg;
    }

    public Invoice repeateInvoice(Invoice invoice) throws ServiceException {
        Invoice repeatedInvoice = null;
        JSONObject invjson = new JSONObject();
        try {
            Map<Inventory, List<HashMap>> FinalTerm = new HashMap<Inventory, List<HashMap>>();
            String companyid = invoice.getCompany().getCompanyID();
            String currencyid = invoice.getCurrency().getCurrencyID();
            String Memo = "";
            invjson.put("customerid", invoice.getCustomer() == null ? invoice.getCustomerEntry().getAccount().getID() : invoice.getCustomer().getID());
            String nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_INVOICE);
            boolean nextInv = false;
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            SequenceFormat prevSeqFormat = null;
            String nextAutoNoInt = "";
            String entryNumber = "";//nextAutoNo;
            String datePrefix = "";
            String dateafterPrefix = "";
            String dateSuffix = "";

            //ERP-16203
            String companyTZDiff = invoice.getCompany().getTimeZone() != null ? invoice.getCompany().getTimeZone().getDifference() : "+00:00";
            DateFormat comdf = authHandler.getCompanyTimezoneDiffFormat(companyTZDiff);
            SimpleDateFormat sd = new SimpleDateFormat("MMMM d, yyyy");
            Date repeateInvoiceNextDate = invoice.getRepeateInvoice().getNextDate();

            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) returnObject.getEntityList().get(0);

            /**
             * If Avalara Integration is enabled and tax calculation in Avalara
             * Integration is enabled then we make below method call which
             * creates and adds tax details in requestJson This is used when
             * transaction is created by means other than UI; for example
             * import, REST etc
             */
            JSONObject invoiceJobj = new JSONObject();
            JSONArray invoiceDetailsJarr = new JSONArray();
            double taxAmountForAvalara = 0;
            double taxAmountInBaseForAvalara = 0;
            double exchangeRateForAvalara = 1.0d;
            if (extraPref != null && extraPref.isAvalaraIntegration()) {
                JSONObject paramsJobj = new JSONObject();
                paramsJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
                paramsJobj.put(Constants.companyKey, companyid);
                if (integrationCommonService.isTaxCalculationEnabled(paramsJobj)) {
                    Date repeateInvoiceJEDate = sd.parse(comdf.format(repeateInvoiceNextDate));
                    invoiceJobj = accInvoiceModuleServiceObj.createJsonFromInvoiceObject(new JSONObject(), invoice, invoice.getID(), invoice.getInvoiceNumber(), repeateInvoiceJEDate, companyid, false);
                    invoiceJobj = integrationCommonService.createAvalaraTaxDetails(invoiceJobj, Constants.Acc_Invoice_ModuleId);
                    if (!StringUtil.isNullOrEmpty(invoiceJobj.optString(Constants.detail))) {
                        invoiceDetailsJarr = new JSONArray(invoiceJobj.optString(Constants.detail));
                    }
                    taxAmountForAvalara = invoiceJobj.optDouble("taxamount", 0d);
                    exchangeRateForAvalara = invoiceJobj.optDouble("exchangeRate", 1.0d);
                    taxAmountInBaseForAvalara = taxAmountForAvalara * exchangeRateForAvalara;
                }
            }

            if (extraPref != null && extraPref.isDefaultsequenceformatforrecinv() && invoice.getSeqformat() != null && invoice.getSeqformat().isIsactivate()) {
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_INVOICE, invoice.getSeqformat().getID(), false, null);//when creation date is system date in that case sending null
                nextAutoNo = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                nextAutoNoInt = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                dateafterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part

                String invoiceSeqFormatId = invoice.getSeqformat().getID();
                invjson.put(Constants.SEQFORMAT, invoiceSeqFormatId);
                invjson.put(Constants.SEQNUMBER, nextAutoNoInt);
                invjson.put(Constants.DATEPREFIX, datePrefix);
                invjson.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                invjson.put(Constants.DATESUFFIX, dateSuffix);
                entryNumber = nextAutoNo;
            } else {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("parentInvoiceId", invoice.getID());
                KwlReturnObject details = accInvoiceDAOobj.getRepeateInvoicesDetails(requestParams);
                List detailsList = details.getEntityList();
                int repInvoiceCount = detailsList.size() + Constants.RECURRING_INVOICE_01_APPEND_START_FROM;
                while (nextInv == false) {
                    entryNumber = invoice.getInvoiceNumber() + "-" + repInvoiceCount;
                    details = accInvoiceDAOobj.getInvoiceCount(entryNumber, companyid);
                    int nocount = details.getRecordTotalCount();
                    if (nocount > 0) {
                        repInvoiceCount++;
                        continue;
                    } else {
                        nextInv = true;
                    }
                }
            }
            int noOfRemainpost = invoice.getRepeateInvoice().getNoOfRemainInvoicespost();
            noOfRemainpost++;
            try {
                HashMap<String, Object> requestParamsMemo = new HashMap<String, Object>();
                requestParamsMemo.put("repeatedJEMemoID", invoice.getRepeateInvoice().getId());
                requestParamsMemo.put("noOfJERemainpost", noOfRemainpost);
                requestParamsMemo.put("columnName", "RepeatedInvoiceID");
                KwlReturnObject RepeatedJEMemo = accJournalEntryobj.getRepeateJEMemo(requestParamsMemo);
                RepeatedJEMemo RM = (RepeatedJEMemo) RepeatedJEMemo.getEntityList().get(0);
                Memo = RM.getMemo();
                if (StringUtil.isNullOrEmpty(Memo)) {
                    Memo = invoice.getMemo();
                }
            } catch (Exception ex) {
                Memo = invoice.getMemo();
            }
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            invjson.put("entrynumber", entryNumber);
            invjson.put("autogenerated", nextAutoNo.equals(entryNumber));
            invjson.put("memo", Memo);
            invjson.put("billto", invoice.getBillTo());
            invjson.put("shipaddress", invoice.getShipTo());
            invjson.put("createdby", (invoice.getCreatedby() != null) ? invoice.getCreatedby().getUserID() : "");
            invjson.put("modifiedby", (invoice.getCreatedby() != null) ? invoice.getCreatedby().getUserID() : "");
            invjson.put("createdon", createdon);
            invjson.put("updatedon", updatedon);
            invjson.put("companyid", companyid);
            invjson.put("currencyid", currencyid);
            invjson.put("externalCurrencyRate", invoice.getExternalCurrencyRate());
            if (invoice.getExchangeRateDetail() != null) {
                invjson.put("erdid", invoice.getExchangeRateDetail().getID());
            }
            if (invoice.getMasterSalesPerson()!= null) {
                invjson.put("salesPerson", invoice.getMasterSalesPerson().getID());
            }
            invjson.put("fob", invoice.getFob());
            invjson.put("isOpeningBalenceInvoice", invoice.isIsOpeningBalenceInvoice());
            invjson.put("conversionRateFromCurrencyToBase", true);
            invjson.put("originalOpeningBalanceAmount", invoice.getOriginalOpeningBalanceAmount());
            invjson.put("openingBalanceAmountDue", invoice.getOpeningBalanceAmountDue());
            // Store Invoice amount in base currency
            invjson.put(Constants.originalOpeningBalanceBaseAmount, invoice.getOriginalOpeningBalanceBaseAmount());
            invjson.put(Constants.openingBalanceBaseAmountDue, invoice.getOpeningBalanceBaseAmountDue());

            invjson.put("exchangeRateForOpeningTransaction", invoice.getExchangeRateForOpeningTransaction());
            invjson.put("posttext", invoice.getPostText());
            invjson.put("isfavourite", invoice.isFavourite());
            invjson.put("isFixedAsset", invoice.isFixedAssetInvoice());
            invjson.put("incash", invoice.isCashtransaction());
            invjson.put("isLeaseFixedAsset", invoice.isFixedAssetLeaseInvoice());
            invjson.put(Constants.isApplyTaxToTerms, invoice.isApplyTaxToTerms());
            invjson.put(Constants.discountAmount, invoice.getDiscountAmount());
            invjson.put(Constants.discountAmountInBase, invoice.getDiscountAmountInBase());

//            Calendar cal = Calendar.getInstance();
//            cal.set(Calendar.HOUR_OF_DAY, 12);   //ERP-16203
//            cal.set(Calendar.MINUTE, 00);
//            cal.set(Calendar.SECOND, 00);
//            Date BillDate = cal.getTime();
            String calString = authHandler.getDateOnlyFormat().format(repeateInvoiceNextDate);
            Date BillDate = authHandler.getDateOnlyFormat().parse(calString);

            //We need to add 1 day in bill date to avoid timezone issue. 
            //Timezone problem getting B'cz we are not maintainig Time. ERP-8708
//            Calendar caldr = Calendar.getInstance();
//            caldr.setTime(cal.getTime());
//            caldr.add(Calendar.DAY_OF_YEAR, 1);
//            Date BillDate = caldr.getTime();
            Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(BillDate);  //No need to do changes. It did later.
            if (invoice.getCustomer() != null) {
                if (invoice.getCustomer().getCreditTerm() != null) {
                    int termDays = invoice.getCustomer().getCreditTerm().getTermdays();
                    dueDate.add(Calendar.DATE, termDays);
                }
            }
            if (invoice.getShipDate() != null) {
                invjson.put("shipdate", invoice.getShipDate());
            }
            invjson.put("duedate", sd.parse(comdf.format(dueDate.getTime())));

            Discount DSC = invoice.getDiscount();
            if (DSC != null) {
                JSONObject discjson = new JSONObject();
                discjson.put("discount", DSC.getDiscount());
                discjson.put("inpercent", DSC.isInPercent());
                discjson.put("originalamount", DSC.getOriginalAmount());
                discjson.put("companyid", companyid);
                KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                Discount discount = (Discount) dscresult.getEntityList().get(0);
                invjson.put("discountid", discount.getID());
            }

            // Create Journal Entry
            String jeentryNumber = "";
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            boolean jeautogenflag = true;
            Date entryDate = sd.parse(comdf.format(BillDate));
            synchronized (this) {
                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", "autojournalentry");
                JEFormatParams.put("companyid", companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                jeIntegerPart = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                jeSeqFormatId = format.getID();
            }
            JournalEntry OLD_JE = invoice.getJournalEntry();
            Map<String, Object> jeDataMap = new HashMap<String, Object>();
            jeDataMap.put("DontCheckYearLock", true);
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
            jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
            jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
            jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
            jeDataMap.put("entrydate", entryDate);
            jeDataMap.put("companyid", companyid);
            jeDataMap.put("memo", OLD_JE.getMemo());
            if (OLD_JE.getCostcenter() != null) {
                jeDataMap.put("costcenterid", OLD_JE.getCostcenter().getID());
            }
            jeDataMap.put("currencyid", currencyid);
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            String jeid = journalEntry.getID();
            invjson.put("journalerentryid", jeid);
            jeDataMap.put("jeid", jeid);
            if (OLD_JE.getAccBillInvCustomData() != null) {
                int NoOFRecords = accJournalEntryobj.saveCustomDataForRecurringJE(jeid, OLD_JE.getID(), true);
            }

            Tax TAX = invoice.getTax();
            if (TAX != null) {
                invjson.put("taxid", TAX.getID());
            }

            Term termid = invoice.getTermid();
            if (termid != null) {
                invjson.put("termid", termid.getID());
            }
            Account accountid = invoice.getAccount();
            if (accountid != null) {
                invjson.put("accountid", accountid.getID());
            }

            String addressID = "";
            if (extraPref != null && extraPref.isPickAddressFromMaster()) {
                Map<String, Object> addressParams = new HashMap<String, Object>();
                addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(invoice.getCustomer().getID(), companyid, accountingHandlerDAOobj);
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                if (bsa != null) {
                    addressID = bsa.getID();
                }
            } else {
                addressID = invoice.getBillingShippingAddresses() != null ? invoice.getBillingShippingAddresses().getID() : "";
            }

            if (!StringUtil.isNullOrEmpty(addressID)) {
                invjson.put("billshipAddressid", addressID);
            }

            invjson.put("approvallevel", invoice.getApprovallevel());
            invjson.put("shipvia", invoice.getShipvia());
            invjson.put("creationDate", entryDate);
            invjson.put("partialinv", invoice.isPartialinv());
            if (invoice.getPoRefNumber() != null) {
                invjson.put("porefno", invoice.getPoRefNumber());
            }
            if (invoice.getCustomerBankAccountType() != null) {
                invjson.put("customerBankAccountTypeId", invoice.getCustomerBankAccountType().getID());
            }
            if (invoice.getTermsincludegst() != null) {
                invjson.put(Constants.termsincludegst, invoice.getTermsincludegst());
            }
            if (invoice.getDeliveryTime() != null) {
                invjson.put("deliveryTime", invoice.getDeliveryTime());
            }
            invjson.put(Constants.Checklocktransactiondate, invoice.getShipDate());
            KwlReturnObject result = accInvoiceDAOobj.addInvoice(invjson, new HashSet());
            repeatedInvoice = (Invoice) result.getEntityList().get(0);//Create Invoice without invoice-details.
            Set INVOICE_DETAILS = invoice.getRows();
            HashSet<InvoiceDetail> invcdetails = new HashSet<InvoiceDetail>();
            Iterator itr = INVOICE_DETAILS.iterator();
            /**
             * Check GST history for customer or product tax class changed nor
             * not This case apply for India only.
             */
            boolean isGSThisChanged = false;
            int uniqueCase = 0;
            JSONArray dimArr = new JSONArray();
            /**
             * Indian GST related code which includes logic for history.
             */
            if (repeatedInvoice.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                String productids = "";
                while (itr.hasNext()) {
                    InvoiceDetail ivd = (InvoiceDetail) itr.next();
                    productids += "'" + ivd.getInventory().getProduct().getID() + "',";
                }
                /**
                 * Check whether GST history changed or not.
                 */
                JSONObject tempParams = new JSONObject();
                tempParams.put("masterid", invoice.getCustomer().getID());
                tempParams.put("isCustomer", true);
                tempParams.put("productids", productids);
                tempParams.put("applydate", authHandler.getDateOnlyFormat().format(invoice.getCreationDate()));
                tempParams.put("transactiondate", authHandler.getDateOnlyFormat().format(new Date()));
                JSONObject temp = accEntityGstService.getGSTFieldsChangedStatus(tempParams);
                isGSThisChanged = temp.optBoolean("isdatachanged", false);
                JSONObject jSONObject = new JSONObject();
                if (isGSThisChanged) {
                    /**
                     * If History changed then get customer GST details as per
                     * current date.
                     */
                    tempParams.put("customerid", invoice.getCustomer().getID());
                    tempParams.put("returnalldata", true);
                    tempParams.put("isfortransaction", true);
                    tempParams.put("transactiondate", authHandler.getDateOnlyFormat().format(new Date()));
                    jSONObject = accCustomerControllerCMNServiceObj.getCustomerGSTHistory(tempParams);
                    jSONObject = jSONObject.optJSONArray("data").optJSONObject(0);
                    uniqueCase = jSONObject.optInt("uniqueCase");
                } else {
                    /**
                     * IF history does not changed then get customer details
                     * from parent document.
                     */
                    jSONObject.put("refdocid", invoice.getID());
                    fieldDataManagercntrl.getGSTDocumentHistory(jSONObject);
                }
                /**
                 * Save Customer GST details at document level.
                 */
                jSONObject.remove("gstdochistoryid");
                jSONObject.put("docid", repeatedInvoice.getID());
                jSONObject.put("moduleid", repeatedInvoice.isFixedAssetInvoice() ? Constants.Acc_FixedAssets_DisposalInvoice_ModuleId : Constants.Acc_Invoice_ModuleId);
                fieldDataManagercntrl.createRequestMapToSaveDocHistory(jSONObject);

                if (isGSThisChanged) {
                    /**
                     * Create dimension array to calculate GST for each product
                     * i.e Using State and Entity value.
                     */
                    dimArr = createDimensionArrayToCalculateGSTForInvoice(invoice, companyid);
                }
            }

            jeDataMap.put("transactionModuleid", Constants.Acc_Invoice_ModuleId);
            jeDataMap.put("transactionId", repeatedInvoice.getID());
            jeDataMap.put(JournalEntryConstants.JEID, journalEntry.getID());
            KwlReturnObject updatejeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//update journalentry
            JournalEntry updatejournalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            String customerAccountID = repeatedInvoice.getCustomer().getAccount().getID();
            //Invoice Details
            JSONObject detailsAmtObj = new JSONObject();
            double finaltaxamt = 0d;
            itr = INVOICE_DETAILS.iterator();
            HashSet<JournalEntryDetail> jeDetails = new HashSet<JournalEntryDetail>();
            while (itr.hasNext()) {
                InvoiceDetail ivd = (InvoiceDetail) itr.next();
                InvoiceDetail row = new InvoiceDetail();
                row.setSrno(ivd.getSrno());
                row.setSalesorderdetail(ivd.getSalesorderdetail());
                row.setCompany(ivd.getCompany());
                row.setRate(ivd.getRate());
                row.setInvoice(repeatedInvoice);
                row.setPartamount(ivd.getPartamount());
                row.setDescription(ivd.getDescription());
                row.setDeferredJeDetailId(ivd.getDeferredJeDetailId());
                row.setDeliveryOrderDetail(ivd.getDeliveryOrderDetail());
                row.setQuotationDetail(ivd.getQuotationDetail());
                row.setRowTaxAmount(ivd.getRowTaxAmount());
                row.setRowTermTaxAmount(ivd.getRowTermTaxAmount());
                row.setRowTaxAmountInBase(ivd.getRowTaxAmountInBase());
                row.setRowExcludingGstAmount(ivd.getRowExcludingGstAmount());
                row.setRowExcludingGstAmountInBase(ivd.getRowExcludingGstAmountInBase());
                row.setRowTermTaxAmount(ivd.getRowTermTaxAmount());
                if (!extraPref.isAvalaraIntegration()) {
                    row.setRowTermAmount(ivd.getRowTermAmount());   //ERP-38046 - Set Term amount
                } else {
                    double rowTermAmount = 0;
                    for (int i = 0; i < invoiceDetailsJarr.length(); i++) {
                        JSONObject invoiceDetailJobj = invoiceDetailsJarr.optJSONObject(i) != null ? invoiceDetailsJarr.optJSONObject(i) : new JSONObject();
                        if (StringUtil.equal(ivd.getID(), invoiceDetailJobj.optString(IntegrationConstants.parentRecordID, null))) {
                            rowTermAmount = invoiceDetailJobj.optDouble("recTermAmount");
                            break;
                        }
                    }
                    row.setRowTermAmount(rowTermAmount);
                }
                row.setWasRowTaxFieldEditable(ivd.isWasRowTaxFieldEditable());
                row.setInvstoreid(ivd.getInvstoreid());
                row.setInvlocid(ivd.getInvlocid());
                row.setPriceSource(ivd.getPriceSource());

                if (ivd.getTax() != null) {
                    row.setTax(ivd.getTax());
                }
                if (ivd.getRateincludegst() > 0) {
                    row.setRateincludegst(ivd.getRateincludegst());
                }
                Inventory ID_INVENTORY = ivd.getInventory();
                if (ID_INVENTORY != null) {
                    JSONObject inventoryjson = new JSONObject();
                    inventoryjson.put("productid", ID_INVENTORY.getProduct().getID());
                    inventoryjson.put("quantity", ID_INVENTORY.getQuantity());
                    inventoryjson.put("baseuomquantity", ID_INVENTORY.getBaseuomquantity());
                    inventoryjson.put("baseuomrate", ID_INVENTORY.getBaseuomrate());
                    if (ID_INVENTORY.getUom() != null) {
                        inventoryjson.put("uomid", ID_INVENTORY.getUom().getID());
                    }
                    inventoryjson.put("description", ID_INVENTORY.getDescription());
                    inventoryjson.put("carryin", false);
                    inventoryjson.put("defective", false);
                    inventoryjson.put("newinventory", false);
                    inventoryjson.put("companyid", companyid);
                    inventoryjson.put("updatedate", ID_INVENTORY.getUpdateDate());
                    KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                    Inventory inventory = (Inventory) invresult.getEntityList().get(0);
                    row.setInventory(inventory);
                }

                Discount ID_DISCOUNT = ivd.getDiscount();
                if (ID_DISCOUNT != null) {
                    JSONObject discjson = new JSONObject();
                    discjson.put("discount", ID_DISCOUNT.getDiscount());
                    discjson.put("inpercent", ID_DISCOUNT.isInPercent());
                    discjson.put("originalamount", ID_DISCOUNT.getOriginalAmount());
                    discjson.put("companyid", companyid);
                    KwlReturnObject dscresult = accDiscountobj.addDiscount(discjson);
                    Discount discount = (Discount) dscresult.getEntityList().get(0);
                    row.setDiscount(discount);
                }

                if (ivd.getSalesJED() != null) {
                    JournalEntryDetail OLD_JED = ivd.getSalesJED();
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jeDetails.size() + 1);
                    jedjson.put("companyid", OLD_JED.getCompany().getCompanyID());
                    jedjson.put("amount", OLD_JED.getAmount());
                    jedjson.put("accountid", OLD_JED.getAccount().getID());
                    jedjson.put("debit", OLD_JED.isDebit());
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    row.setSalesJED(jed);
                    jeDetails.add(jed);
                    if (OLD_JED.getAccJEDetailCustomData() != null) {
                        int NoOFRecords = accJournalEntryobj.saveCustomDataForRecurringJE(jed.getID(), OLD_JED.getID(), false);
                    }
                }

                if (ivd.getTax() != null) {
                    JSONObject jedjson = new JSONObject();
                    jedjson = new JSONObject();
                    jedjson.put("srno", jeDetails.size() + 1);
                    jedjson.put("companyid", ivd.getCompany().getCompanyID());
                    jedjson.put("amount", ivd.getRowTaxAmount());
                    jedjson.put("accountid", ivd.getTax().getAccount().getID());
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jeDetails.add(jed);

                    if(row.getRowTermTaxAmount()!=0) {
                        jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put("companyid", ivd.getCompany().getCompanyID());
                        jedjson.put("amount", ivd.getRowTermTaxAmount());
                        jedjson.put("accountid", ivd.getTax().getAccount().getID());
                        jedjson.put("debit", false);
                        jedjson.put("jeid", jeid);
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jeDetails.add(jed);
                    }
                }
                /*
                 ERP-38046 : Below piece of code has written to get the list of Map Invoice Details Term Map.
                 */
                HashSet<InvoiceDetailTermsMap> invcdetailTermMap = new HashSet<InvoiceDetailTermsMap>();
                if (extraPref.getLineLevelTermFlag() == 1) {//If line level term is applicable then we will consider GST Account
                    JSONObject jSONObject = new JSONObject();
                    List termlist = new ArrayList();
                    if (isGSThisChanged) {
                        /**
                         * If history changed then calculate Term for each
                         * product Save in the term table.
                         */
                        double quantity = ivd.getInventory().getQuantity();
                        double rate = ivd.getRate();
                        double discount = ivd.getDiscount() != null ? ivd.getDiscount().getDiscount() : 0d;
                        double subtotal = quantity * rate;
                        if (ivd.getDiscount() != null && ivd.getDiscount().isInPercent()) {
                            discount = (subtotal * ivd.getDiscount().getDiscount()) / 100;
                        }
                        subtotal = subtotal - discount;
                        HashMap<String, Object> requestParams = new HashMap();
                        requestParams.put("df", authHandler.getOnlyDateFormat());
                        jSONObject.put("productids", ivd.getInventory().getProduct().getID());
                        jSONObject.put("transactiondate", authHandler.getOnlyDateFormat().format(new Date()));
                        jSONObject.put("termSalesOrPurchaseCheck", true);
                        jSONObject.put("isFixedAsset", invoice.isFixedAssetInvoice());
                        jSONObject.put("uniqueCase", uniqueCase);
                        jSONObject.put("dimArr", dimArr);
                        jSONObject.put("companyid", companyid);
                        jSONObject = accEntityGstService.getGSTForProduct(jSONObject, requestParams);
                        if (jSONObject.optBoolean("success")) {
                            String prodTermArray = jSONObject.optJSONArray("prodTermArray").getJSONObject(0).optString("LineTermdetails");
                            String taxclass = jSONObject.optJSONArray("prodTermArray").getJSONObject(0).optString("taxclass");
                            JSONArray termarr = new JSONArray(prodTermArray);
                            double rowttermamount = 0d;
                            for (int i = 0; i < termarr.length(); i++) {
                                JSONObject termObj = termarr.optJSONObject(i);
                                JSONObject jedTermjson = new JSONObject();
                                double termamt = subtotal * termObj.optDouble("taxvalue") / 100;
                                termamt = authHandler.round(termamt, companyid);
                                rowttermamount += termamt;
                                jedTermjson.put("srno", jeDetails.size() + 1);
                                jedTermjson.put("companyid", ivd.getCompany().getCompanyID());
                                jedTermjson.put("amount", subtotal * termObj.optDouble("taxvalue") / 100);
                                jedTermjson.put("accountid", termObj.optString("accountid")); //GST Account ID
                                jedTermjson.put("debit", false);
                                jedTermjson.put("jeid", jeid);
                                KwlReturnObject jedresultobj = accJournalEntryobj.addJournalEntryDetails(jedTermjson);
                                JournalEntryDetail jedobj = (JournalEntryDetail) jedresultobj.getEntityList().get(0);
                                jeDetails.add(jedobj);
                                termObj.put("assessablevalue", subtotal);
                                termObj.put("termamount", termamt);
                                List list = mapInvoiceDetailTerms(termObj, ivd.getInvoice().getCreatedby().getUserID());
                                termlist.addAll(list);
                            }
                            FinalTerm.put(row.getInventory(), termlist);
                            jSONObject = new JSONObject();
                            jSONObject.put("taxclass", taxclass);
                            row.setRowTermAmount(rowttermamount);
                            finaltaxamt += rowttermamount;
                        }
                    } else {
                        /**
                         * Save GST details in Term table by coping from parent
                         * table.
                         */
                        if (!extraPref.isAvalaraIntegration()) {
                            List rslist = accInvoiceDAOobj.getInvoiceDetailsTermMap(ivd.getID());
                            Iterator rsitr = rslist.iterator();
                            while (rsitr.hasNext()) {
                                InvoiceDetailTermsMap idt = (InvoiceDetailTermsMap) rsitr.next();
                                JSONObject jedTermjson = new JSONObject();
                                jedTermjson.put("srno", jeDetails.size() + 1);
                                jedTermjson.put("companyid", ivd.getCompany().getCompanyID());
                                jedTermjson.put("amount", idt.getTermamount());
                                jedTermjson.put("accountid", idt.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID()); //GST Account ID
                                jedTermjson.put("debit", false);
                                jedTermjson.put("jeid", jeid);
                                KwlReturnObject jedresultobj = accJournalEntryobj.addJournalEntryDetails(jedTermjson);
                                JournalEntryDetail jedobj = (JournalEntryDetail) jedresultobj.getEntityList().get(0);
                                jeDetails.add(jedobj);

                                //Get Invoice Detail Term Map
                                List list = mapInvoiceDetailTerms(idt, row.getInventory(), idt.getCreator().getUserID(), extraPref.isAvalaraIntegration());
                                termlist.addAll(list);
                            }
                            FinalTerm.put(row.getInventory(), termlist);
                        } else {
                            for (int i = 0; i < invoiceDetailsJarr.length(); i++) {
                                JSONObject invoiceDetailJobj = invoiceDetailsJarr.optJSONObject(i) != null ? invoiceDetailsJarr.optJSONObject(i) : new JSONObject();
                                if (StringUtil.equal(ivd.getID(), invoiceDetailJobj.optString(IntegrationConstants.parentRecordID, null))) {
                                    JSONArray lineTermDetailsJarr = !StringUtil.isNullOrEmpty(invoiceDetailJobj.optString("LineTermdetails")) ? new JSONArray(invoiceDetailJobj.optString("LineTermdetails")) : new JSONArray();
                                    for (int j = 0; j < lineTermDetailsJarr.length(); j++) {
                                        JSONObject lineTermDetailJobj = lineTermDetailsJarr.optJSONObject(j);
                                        if (lineTermDetailJobj != null && lineTermDetailJobj.optDouble("termamount") > 0d) {
                                            JSONObject jedTermjson = new JSONObject();
                                            jedTermjson.put("srno", jeDetails.size() + 1);
                                            jedTermjson.put("companyid", ivd.getCompany().getCompanyID());
                                            jedTermjson.put("amount", lineTermDetailJobj.optDouble("termamount") * exchangeRateForAvalara);
                                            jedTermjson.put("accountid", lineTermDetailJobj.optString("glaccount"));
                                            jedTermjson.put("debit", false);
                                            jedTermjson.put("jeid", jeid);
                                            KwlReturnObject jedresultobj = accJournalEntryobj.addJournalEntryDetails(jedTermjson);
                                            JournalEntryDetail jedobj = (JournalEntryDetail) jedresultobj.getEntityList().get(0);
                                            jeDetails.add(jedobj);
                                        }
                                    }
                                }
                            }
                        }
                        /**
                         * Get tax class details from parent document.
                         */

                        jSONObject.put("refdocid", ivd.getID());
                        fieldDataManagercntrl.getGSTTaxClassHistory(jSONObject);
                    }
                    /**
                     * Save GST History Tax Class.
                     */
                    jSONObject.remove("taxclasshistoryid");
                    jSONObject.put("detaildocid", row.getInventory().getID());
                    jSONObject.put("moduleid", repeatedInvoice.isFixedAssetInvoice() ? Constants.Acc_FixedAssets_DisposalInvoice_ModuleId : Constants.Acc_Invoice_ModuleId);
                    fieldDataManagercntrl.createRequestMapToSaveTaxClassHistory(jSONObject);
                }
                invcdetails.add(row);
            }

            JSONObject customerjedjson = new JSONObject();
            customerjedjson.put("srno", jeDetails.size() + 1);
            customerjedjson.put("companyid", OLD_JE.getCompany().getCompanyID());
            if (isGSThisChanged) {
                /**
                 * Recalculate amount if GST history changed.
                 */
                customerjedjson.put("amount", invoice.getExcludingGstAmount() + finaltaxamt);
            } else {
                customerjedjson.put("amount", invoice.getInvoiceamount());
            }
            customerjedjson.put("accountid", customerAccountID);
            customerjedjson.put("debit", true);
            customerjedjson.put("jeid", jeid);
            KwlReturnObject res = accJournalEntryobj.addJournalEntryDetails(customerjedjson);
            JournalEntryDetail customerJED = (JournalEntryDetail) res.getEntityList().get(0);
            jeDetails.add(customerJED);
            invjson.put("customerentryid", customerJED.getID());

            if (invoice.getDiscountAmount() > 0) {                              //to handle in discount case
                JSONObject discjedjson = new JSONObject();
                discjedjson.put("srno", jeDetails.size() + 1);
                discjedjson.put("companyid", OLD_JE.getCompany().getCompanyID());
                discjedjson.put("amount", invoice.getDiscountAmount());
                discjedjson.put("accountid", preferences.getDiscountGiven().getID());
                discjedjson.put("debit", true);
                discjedjson.put("jeid", jeid);
                KwlReturnObject descJEDres = accJournalEntryobj.addJournalEntryDetails(discjedjson);
                JournalEntryDetail descJED = (JournalEntryDetail) descJEDres.getEntityList().get(0);
                jeDetails.add(descJED);
            }

            HashMap<String, Object> termParam = new HashMap();
            termParam.put("invoiceid", invoice.getID());
            KwlReturnObject curresult = accInvoiceDAOobj.getInvoiceTermMap(termParam);

            JSONArray invoiceTermsMapJarr = new JSONArray();
            List<InvoiceTermsMap> termMap = curresult.getEntityList();
            for (InvoiceTermsMap invoiceTermsMap : termMap) {
                double termAmnt =0d;
                double termTaxAmnt =0d;
                if(invoice.isGstIncluded()){
                    termAmnt = invoiceTermsMap.getTermAmountExcludingTax();
                }else{
                    termAmnt = invoiceTermsMap.getTermamount();
                }
                JSONObject termjedjson = new JSONObject();
                termjedjson.put("srno", jeDetails.size() + 1);
                termjedjson.put("companyid", OLD_JE.getCompany().getCompanyID());
                termjedjson.put("amount", Math.abs(termAmnt));              //SDP-9651
                termjedjson.put("accountid", invoiceTermsMap.getTerm().getAccount().getID());
                termjedjson.put("debit", termAmnt > 0 ? false : true);
                termjedjson.put("jeid", jeid);
                KwlReturnObject termJEDres = accJournalEntryobj.addJournalEntryDetails(termjedjson);
                JournalEntryDetail termJED = (JournalEntryDetail) termJEDres.getEntityList().get(0);
                jeDetails.add(termJED);
                
                termTaxAmnt = invoiceTermsMap.getTermtaxamount();
                if (invoiceTermsMap.getTermtax() != null) {
                    termjedjson = new JSONObject();
                    termjedjson.put("srno", jeDetails.size() + 1);
                    termjedjson.put(Constants.companyKey, companyid);
                    termjedjson.put("amount", Math.abs(termTaxAmnt));
                    termjedjson.put("accountid", invoiceTermsMap.getTermtax().getAccount().getID());
                    termjedjson.put("debit", termTaxAmnt > 0 ? false : true);
                    termjedjson.put("jeid", jeid);
                    termJEDres = accJournalEntryobj.addJournalEntryDetails(termjedjson);
                    termJED = (JournalEntryDetail) termJEDres.getEntityList().get(0);
                    jeDetails.add(termJED);
                }
                
                JSONObject invoiceTermsMapJson = new JSONObject();
                InvoiceTermsSales invoiceTermsSalesObj = invoiceTermsMap.getTerm();
                invoiceTermsMapJson.put("id", invoiceTermsSalesObj.getId());
                invoiceTermsMapJson.put("termamount", invoiceTermsMap.getTermamount());
                invoiceTermsMapJson.put("termpercentage", invoiceTermsMap.getPercentage());                
                invoiceTermsMapJson.put("termtax", invoiceTermsMap.getTermtax()!= null ? invoiceTermsMap.getTermtax().getID() : "");
                invoiceTermsMapJson.put("termamountinbase", invoiceTermsMap.getTermamountinbase());
                invoiceTermsMapJson.put("termtaxamount", invoiceTermsMap.getTermtaxamount());
                invoiceTermsMapJson.put("termtaxamountinbase", invoiceTermsMap.getTermtaxamountinbase());
                invoiceTermsMapJson.put("termAmountExcludingTax", invoiceTermsMap.getTermAmountExcludingTax());
                invoiceTermsMapJson.put("termAmountExcludingTaxInBase", invoiceTermsMap.getTermAmountExcludingTaxInBase());
                invoiceTermsMapJarr.put(invoiceTermsMapJson);
            }

            accInvoiceModuleServiceObj.mapInvoiceTerms(invoiceTermsMapJarr.toString(), repeatedInvoice.getID(), invoice.getCreatedby().getUserID(), false);

            if (invoice.getTaxEntry() != null) {
                JSONObject taxjedjson = new JSONObject();
                taxjedjson.put("srno", jeDetails.size() + 1);
                taxjedjson.put("companyid", OLD_JE.getCompany().getCompanyID());
                taxjedjson.put("amount", invoice.getTaxEntry().getAmount());
                taxjedjson.put("accountid", invoice.getTaxEntry().getAccount().getID());
                taxjedjson.put("debit", false);
                taxjedjson.put("jeid", jeid);
                KwlReturnObject taxJEDres = accJournalEntryobj.addJournalEntryDetails(taxjedjson);
                JournalEntryDetail taxJED = (JournalEntryDetail) taxJEDres.getEntityList().get(0);
                jeDetails.add(taxJED);
                invjson.put("taxentryid", taxJED.getID());
            }

            jeDataMap.put("accjecustomdataref", jeid);
            jeDataMap.put("jedetails", jeDetails);
            jeDataMap.put("externalCurrencyRate", OLD_JE.getExternalCurrencyRate());
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            Set newJEDetails = journalEntry.getDetails();
            Set JE_DETAILS = invoice.getJournalEntry().getDetails();
            HashMap<String, String> oldNnewJEDid = new HashMap<String, String>();
            Iterator jeditr = JE_DETAILS.iterator();
            Iterator newjeditr = newJEDetails.iterator();
            while (jeditr.hasNext()) {
                JournalEntryDetail newJEDetail = null;
                JournalEntryDetail oldJEDetail = (JournalEntryDetail) jeditr.next();
                if (newjeditr.hasNext()) {
                    newJEDetail = (JournalEntryDetail) newjeditr.next();
                    JSONObject jedjson1 = new JSONObject();
                    jedjson1.put("jedid", newJEDetail.getID());
                    jedjson1.put("accjedetailcustomdata", newJEDetail.getID());
                    KwlReturnObject jedresult1 = accJournalEntryobj.updateJournalEntryDetails(jedjson1);
                    JournalEntryDetail jed1 = (JournalEntryDetail) jedresult1.getEntityList().get(0);
                    oldNnewJEDid.put(oldJEDetail.getID(), jed1.getID());
                }

            }

//            JournalEntryDetail CUST_JED = invoice.getCustomerEntry();
//            if (CUST_JED != null) {
//                invjson.put("customerentryid", oldNnewJEDid.get(CUST_JED.getID()));
//            }
            JournalEntryDetail SHIP_JED = invoice.getShipEntry();
            if (SHIP_JED != null) {
                invjson.put("shipentryid", oldNnewJEDid.get(SHIP_JED.getID()));
            }

            JournalEntryDetail OTHER_JED = invoice.getOtherEntry();
            if (OTHER_JED != null) {
                invjson.put("otherentryid", oldNnewJEDid.get(OTHER_JED.getID()));
            }

//            JournalEntryDetail TAX_JED = invoice.getTaxEntry();
//            if (TAX_JED != null) {
//                invjson.put("taxentryid", oldNnewJEDid.get(TAX_JED.getID()));
//            }
            //Invoice Contract Mapping
            Set INVOICE_CONTRACT_MAPPING = invoice.getContractMappings();
            HashSet<InvoiceContractMapping> invoiceContract = new HashSet<InvoiceContractMapping>();
            Iterator itrInvoiceContract = INVOICE_CONTRACT_MAPPING.iterator();
            while (itrInvoiceContract.hasNext()) {
                InvoiceContractMapping oldInvoiceComMapobj = (InvoiceContractMapping) itrInvoiceContract.next();
                InvoiceContractMapping newInvoiceComMapobj = new InvoiceContractMapping();
                newInvoiceComMapobj.setCompany(oldInvoiceComMapobj.getCompany());
                newInvoiceComMapobj.setContract(oldInvoiceComMapobj.getContract());
                newInvoiceComMapobj.setDeliveryOrder(oldInvoiceComMapobj.getDeliveryOrder());
                newInvoiceComMapobj.setInvoice(repeatedInvoice);
                invoiceContract.add(newInvoiceComMapobj);
            }//AccountingManager.getGlobalParams(request);

            if (!invoiceContract.isEmpty()) {
                HashMap<String, Object> invoiceContractSet = new HashMap<String, Object>();
                invoiceContractSet.put("id", repeatedInvoice.getID());
                invoiceContractSet.put("contractMappings", invoiceContract);
                accInvoiceDAOobj.updateInvoiceUsingSet(invoiceContractSet);
            }

            invjson.put("invoiceid", repeatedInvoice.getID());
            invjson.put("parentid", invoice.getID());
            invjson.put(Constants.invoiceamountdue, invoice.getInvoiceamount());
            invjson.put(Constants.invoiceamountdueinbase, invoice.getInvoiceamountinbase());
            invjson.put(Constants.invoiceamount, invoice.getInvoiceamount());
            invjson.put(Constants.invoiceamountinbase, invoice.getInvoiceamountinbase());
            invjson.put("gstIncluded", invoice.isGstIncluded());
            invjson.put("excludingGstAmount", invoice.getExcludingGstAmount());
            invjson.put("excludingGstAmountInBase", invoice.getExcludingGstAmountInBase());
            if (!extraPref.isAvalaraIntegration()) {
                invjson.put("taxAmount", invoice.getTaxamount());
                invjson.put("taxAmountInBase", invoice.getTaxamountinbase());
            } else {
                invjson.put("taxAmount", taxAmountForAvalara);
                invjson.put("taxAmountInBase", taxAmountInBaseForAvalara);
            }
            invjson.put("conversionRateFromCurrencyToBase", invoice.isConversionRateFromCurrencyToBase());
            if (isGSThisChanged) {
                /**
                 * Recalculate amount if GST history changed.
                 */
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, invoice.getCompany().getCompanyID());
                requestParams.put("gcurrencyid", invoice.getCompany().getCurrency().getCurrencyID());
                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, invoice.getExcludingGstAmount() + finaltaxamt, invoice.getCurrency().getCurrencyID(), BillDate, invoice.getJournalEntry().getExternalCurrencyRate());
                double invoiceamountdueinbase = (Double) bAmt.getEntityList().get(0);
                invjson.put(Constants.invoiceamountdue, invoice.getExcludingGstAmount() + finaltaxamt);
                invjson.put(Constants.invoiceamount, invoice.getExcludingGstAmount() + finaltaxamt);
                invjson.put(Constants.invoiceamountdueinbase, invoiceamountdueinbase);
                invjson.put(Constants.invoiceamountinbase, invoiceamountdueinbase);
            }
            result = accInvoiceDAOobj.updateInvoice(invjson, invcdetails);
            repeatedInvoice = (Invoice) result.getEntityList().get(0);//Add invoice details
            accInvoiceDAOobj.updateRecDetailId(repeatedInvoice);

            /**
             * When Avalara Integration is enabled, we save tax details in
             * Avalara tax mapping table 'TransactionDetailAvalaraTaxMapping' So
             * there is no need to save InvoiceDetailTermMap in case Avalara
             * Integration is enabled. ERP-38046 : Below piece of code has
             * written to get the list of Map Invoice Details Term Map.
             */
            if (extraPref.getLineLevelTermFlag() == 1) {
                if (extraPref.isAvalaraIntegration()) {
                    JSONObject paramsJobj = new JSONObject();
                    paramsJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
                    paramsJobj.put(Constants.companyKey, companyid);
                    /**
                     * Commit is to be done only if committing is enabled in
                     * Avalara integration settings and if invoice is neither
                     * draft, nor template, nor pending for approval
                     */
                    boolean isCommit = integrationCommonService.isTaxCommittingEnabled(paramsJobj);
                    isCommit = isCommit && (!repeatedInvoice.isDraft() && (repeatedInvoice.getIstemplate() != 2) && repeatedInvoice.getApprovestatuslevel() == 11);
                    if (integrationCommonService.isTaxCalculationEnabled(paramsJobj)) {
                        /**
                         * Commit taxes to Avalara and save taxes only if Tax
                         * Committing is enabled in System Controls If Tax
                         * Committing is disabled but Tax Calculation is
                         * enabled, then we only calculate the taxes again
                         * (without committing) to save
                         */
                        invoiceJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
                        accInvoiceModuleServiceObj.commitTaxToAvalaraAndSave(invoiceJobj, repeatedInvoice, repeatedInvoice.getID(), repeatedInvoice.getInvoiceNumber(), companyid, false, "", isCommit);
                    }
                } else {
                    Set<InvoiceDetail> invoiceDetailsSet = repeatedInvoice.getRows();
                    for (InvoiceDetail invoiceDetail : invoiceDetailsSet) {
                        if (invoiceDetail.getInventory() != null && FinalTerm != null && ((List) FinalTerm.get(invoiceDetail.getInventory()) != null)) {
                            List ll2 = (List) FinalTerm.get(invoiceDetail.getInventory());
                            Iterator itr2 = ll2.iterator();
                            while (itr2.hasNext()) {
                                HashMap<String, Object> termHashMap = (HashMap<String, Object>) itr2.next();
                                termHashMap.put("invoiceDetail", invoiceDetail);
                                accInvoiceDAOobj.saveInvoiceDetailTermMap(termHashMap);
                            }
                        }
                    }
                }
            }

            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        } catch (AccountingException ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        }

        return repeatedInvoice;
    }
    /**
     * Function to create require Map from json object
     * @param termObj
     * @param userid
     * @return
     * @throws ServiceException
     */
    public List mapInvoiceDetailTerms(JSONObject termObj, String userid) throws ServiceException {
        List ll = new ArrayList();
        try {
            HashMap<String, Object> termMap = new HashMap<String, Object>();
            termMap.put("term", termObj.optString("termid"));
            termMap.put("termamount", termObj.optDouble("termamount"));
            termMap.put("termpercentage", termObj.optDouble("taxvalue"));
            termMap.put("assessablevalue", termObj.optDouble("assessablevalue"));
            termMap.put("creationdate", new Date());
            termMap.put("accountid", termObj.optString("accountid"));
            termMap.put("userid", userid);
            termMap.put("productentitytermid", termObj.optString("productentitytermid"));
            termMap.put("taxtype", termObj.optInt("taxtype"));
            ll.add(termMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return ll;
    }
    /**
     * Function to create dimension array
     * @param invoice
     * @param companyid
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONArray createDimensionArrayToCalculateGSTForInvoice(Invoice invoice, String companyid) throws JSONException, ServiceException {
        JSONArray dimArr = new JSONArray();
        JSONObject tempParams = new JSONObject();
        JSONObject jSONObject = new JSONObject();
        tempParams.put("companyid", companyid);
        tempParams.put("moduleid", Constants.Acc_Invoice_ModuleId);
        List returnList = accEntityGstDao.getGSTDimensionsDetailsForGSTCalculations(tempParams);
        Iterator iterator = returnList.iterator();
        String selectstate = "";
        String selectentity = "";
        while (iterator.hasNext()) {
            jSONObject = new JSONObject();
            Object[] object = (Object[]) iterator.next();
            int gstconfigtype = Integer.parseInt(object[2].toString());
            jSONObject.put("fieldname", (String) object[0]);
            jSONObject.put("gstmappingcolnum", Integer.parseInt(object[3].toString()));
            jSONObject.put("gstconfigtype", Integer.parseInt(object[2].toString()));
            dimArr.put(jSONObject);
            if (gstconfigtype == 1) {
                selectentity = "Col" + Integer.parseInt(object[1].toString());
            } else {
                selectstate = "Col" + Integer.parseInt(object[1].toString());
            }
        }
        /**
         * Get data from custom tables.
         */
        tempParams.put("selectstate", selectstate);
        tempParams.put("selectentity", selectentity);
        tempParams.put("customtable", "accjecustomdata");
        tempParams.put("primarykey", "journalentryId");
        tempParams.put("primaryid", invoice.getJournalEntry().getID());
        returnList = accEntityGstDao.getGSTDimensionDataFromCustomTableForGSTCalculations(tempParams);
        iterator = returnList.iterator();
        while (iterator.hasNext()) {
            Object[] object = (Object[]) iterator.next();
            for (int i = 0; i < dimArr.length(); i++) {
                jSONObject = dimArr.optJSONObject(i);
                if (jSONObject.optInt("gstconfigtype") == 1) {
                    dimArr.getJSONObject(i).put("dimvalue", (String) object[0]);
                } else {
                    dimArr.getJSONObject(i).put("dimvalue", (String) object[1]);
                }
            }
        }
        return dimArr;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = {ServiceException.class, AccountingException.class})
    public String processJournalEntry(JournalEntry JEObj, HashMap<String, Object> requestParams) throws ServiceException {
        String msg = "", failed = "";
        User adminUser = null;
        String jeentryno = "";
        try {
            JournalEntry repeatedJE = repeateJE(JEObj, JEObj.getRepeateJE());
            msg += repeatedJE.getEntryNumber() + ",";
            adminUser = repeatedJE.getCompany().getCreator() != null ? repeatedJE.getCompany().getCreator() : repeatedJE.getCreatedby();
            jeentryno += repeatedJE.getEntryNumber();
            if (JEObj.getRepeateJE() != null) {
                updateRepeateInfoForJE(JEObj.getRepeateJE());
            }
            //Add Audit Trail Entry for JE
            requestParams.put("userid", adminUser.getUserID());
//                auditTrailObj.insertRecurringAuditLog(AuditAction.ADD_RECURRING_JOURNAL_ENTRY_ENTRY, "System has generated a recurring Journal Entry - " + jeentryno, request, jeentryno, adminUser);
            auditTrailObj.insertAuditLog(AuditAction.ADD_RECURRING_JOURNAL_ENTRY_ENTRY, "System has generated a recurring Journal Entry - " + jeentryno, requestParams, jeentryno);
        } catch (Exception ex) {
            failed += JEObj.getEntryNumber() + "[" + JEObj.getID() + "][B]: " + ex.getMessage() + ";";
            System.out.println("\n***** Exception in Recurring Journal Entry:*****\n" + failed);
            HashMap<String, Object> exMailParams = new HashMap();               //SDP-9651 Sends mail to Sagar A. Sir in case of Recurring failure
            String baseURLFormat=ConfigReader.getinstance().get("base_urlformat");
            exMailParams.put("documentObj", JEObj);
            exMailParams.put("failed", "\n" + failed+" URL : "+baseURLFormat);
            getExceptionMail(exMailParams);
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        }
        return msg;
    }

    public JournalEntry repeateJE(JournalEntry OLD_JE, RepeatedJE repeatedJEObject) throws ServiceException {
        JournalEntry journalEntry = null;
        try {
            String companyid = OLD_JE.getCompany().getCompanyID();
            String createdby = OLD_JE.getCreatedby().getUserID().toString();
            String currencyid = OLD_JE.getCurrency().getCurrencyID();
            String Memo = "";
            boolean nextInv = false;
            Date chequeDate = null;
            String chequeNumber = "";
            boolean isAutoGenerateChequeNumber = false;
            String companyTZDiff = OLD_JE.getCompany().getTimeZone() != null ? OLD_JE.getCompany().getTimeZone().getDifference() : "+00:00";
            DateFormat comdf = authHandler.getCompanyTimezoneDiffFormat(companyTZDiff);
            SimpleDateFormat sd = new SimpleDateFormat("MMMM d, yyyy");
            Calendar cal = Calendar.getInstance();
            Date BillDate = cal.getTime();
            Date entryDate = sd.parse(comdf.format(BillDate));
            Map<String, Object> jeDataMap = new HashMap<String, Object>();
            String jeentryNumber = "";
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            String entryNumber = "";//nextAutoNo;
            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) returnObject.getEntityList().get(0);
            if (extraPref != null && extraPref.isDefaultsequenceformatforrecinv() && OLD_JE.getSeqformat() != null && OLD_JE.getSeqformat().isIsactivate()) {
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, OLD_JE.getSeqformat().getID(), false, entryDate);
                jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                jeIntegerPart = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                jeSeqFormatId = OLD_JE.getSeqformat().getID();
                jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                entryNumber = jeentryNumber;
            } else {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("parentJEId", OLD_JE.getID());
                KwlReturnObject details = accJournalEntryobj.getRepeateJEDetails(requestParams);
                List detailsList = details.getEntityList();
                int repInvoiceCount = detailsList.size() + Constants.RECURRING_INVOICE_01_APPEND_START_FROM;
                while (nextInv == false) {
                    entryNumber = OLD_JE.getEntryNumber() + "-" + repInvoiceCount;
                    details = accJournalEntryobj.getJECount(entryNumber, companyid);
                    int nocount = details.getRecordTotalCount();
                    if (nocount > 0) {
                        repInvoiceCount++;
                        continue;
                    } else {
                        nextInv = true;
                    }
                }
            }
            int noOfJERemainpost = OLD_JE.getRepeateJE().getNoOfRemainJEpost();
            noOfJERemainpost++;
            try {
                HashMap<String, Object> requestParamsMemo = new HashMap<String, Object>();
                requestParamsMemo.put("repeatedJEMemoID", OLD_JE.getRepeateJE().getId());
                requestParamsMemo.put("noOfJERemainpost", noOfJERemainpost);
                requestParamsMemo.put("columnName", "RepeatedJEID.id");
                KwlReturnObject RepeatedJEMemo = accJournalEntryobj.getRepeateJEMemo(requestParamsMemo);
                RepeatedJEMemo RM = (RepeatedJEMemo) RepeatedJEMemo.getEntityList().get(0);
                Memo = RM.getMemo();
                if (StringUtil.isNullOrEmpty(Memo)) {
                    Memo = OLD_JE.getMemo();
                }
            } catch (Exception ex) {
                Memo = OLD_JE.getMemo();
            }

            Calendar dueDate = Calendar.getInstance();
            dueDate.setTime(BillDate);

            if (OLD_JE.getTypeValue() == Constants.FundTransfer_Journal_Entry && OLD_JE.getPaymentMethod() != null && OLD_JE.getPaymentMethod().getDetailType() == Constants.bank_detail_type) {
                try {
                    HashMap<String, Object> requestParamsCheque = new HashMap<String, Object>();
                    requestParamsCheque.put("repeatedJEID", OLD_JE.getRepeateJE().getId());
                    requestParamsCheque.put("noOfJERemainpost", noOfJERemainpost);
                    KwlReturnObject RepeatedJEChequeDetail = accJournalEntryobj.getRepeateJEChequeDetail(requestParamsCheque);
                    RepeatedJEChequeDetail R = (RepeatedJEChequeDetail) RepeatedJEChequeDetail.getEntityList().get(0);
                    chequeDate = R.getChequeDate();
                    chequeNumber = R.getChequeNumber();
                    if (chequeDate == null) {
                        chequeDate = sd.parse(comdf.format(BillDate));
                    }
                } catch (Exception ex) {
                    chequeDate = sd.parse(comdf.format(BillDate));
                }
                isAutoGenerateChequeNumber = repeatedJEObject.isAutoGenerateChequeNumber();
            }

            // Create Journal Entry
            boolean jeautogenflag = true;

//            Map<String, Object> jeDataMap = new HashMap<String, Object>();
            jeDataMap.put("DontCheckYearLock", true);
            jeDataMap.put("entrynumber", entryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put("entrydate", entryDate);
            jeDataMap.put("companyid", companyid);
            jeDataMap.put("createdby", createdby);
            jeDataMap.put("memo", Memo);
            jeDataMap.put("reversejournalentry", OLD_JE.getReverseJournalEntry());
            jeDataMap.put("isreverseje", OLD_JE.isIsReverseJE());

            jeDataMap.put("pendingapproval", OLD_JE.getPendingapproval());
            jeDataMap.put("isReval", OLD_JE.getIsReval());
            jeDataMap.put("revalInvoiceId", OLD_JE.getRevalInvoiceId());
            jeDataMap.put("istemplate", 0);
            jeDataMap.put("typevalue", OLD_JE.getTypeValue());
            jeDataMap.put("partlyJeEntryWithCnDn", OLD_JE.getPartlyJeEntryWithCnDn());
            if (OLD_JE.getAccBillInvCustomData() != null) {
                jeDataMap.put("accjecustomdataref", OLD_JE.getID());
            }
            if (OLD_JE.getCostcenter() != null) {
                jeDataMap.put("costcenterid", OLD_JE.getCostcenter().getID());
            }
            jeDataMap.put("currencyid", currencyid);
            if (OLD_JE.getPaymentMethod() != null) {
                jeDataMap.put("pmtmethod", OLD_JE.getPaymentMethod().getID());
            }
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            String jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);

            Set JE_DETAILS = OLD_JE.getDetails();
            HashSet<JournalEntryDetail> jeDetails = new HashSet<JournalEntryDetail>();
            Iterator jeditr = JE_DETAILS.iterator();
            while (jeditr.hasNext()) {
                JournalEntryDetail OLD_JED = (JournalEntryDetail) jeditr.next();
                JSONObject jedjson = new JSONObject();
                jedjson.put("srno", OLD_JED.getSrno());
                jedjson.put("companyid", OLD_JED.getCompany().getCompanyID());
                jedjson.put("amount", OLD_JED.getAmount());
                jedjson.put("accountid", OLD_JED.getAccount().getID());
                jedjson.put("debit", OLD_JED.isDebit());
                jedjson.put("jeid", jeid);
                jedjson.put("description", OLD_JED.getDescription());
                jedjson.put("exchangeratefortransaction", OLD_JED.getExchangeRateForTransaction());
                if (OLD_JED.getAccJEDetailCustomData() != null) {
                    jedjson.put("accjedetailcustomdata", OLD_JED.getID());
                }
                if (OLD_JED.getAccJEDetailsProductCustomData() != null) {
                    jedjson.put("accjedetailproductcustomdataref", OLD_JED.getID());
                }
                KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jeDetails.add(jed);
            }
            if (OLD_JE.getTypeValue() == Constants.FundTransfer_Journal_Entry && OLD_JE.getPaymentMethod() != null && OLD_JE.getPaymentMethod().getDetailType() == Constants.bank_detail_type) {
                HashMap chequehm = new HashMap();
                String chequeNO = "";
                if (isAutoGenerateChequeNumber || StringUtil.isNullOrEmpty(chequeNumber)) {
                    chequeNO = journalEntryModuleServiceobj.getNextChequeNumberForRecurredJE(companyid, OLD_JE.getPaymentMethod().getAccount().getID());
                } else {
                    chequeNO = chequeNumber;
                }
                chequehm.put("chequeno", chequeNO);
                chequehm.put("companyId", companyid);
                chequehm.put("createdFrom", 1);
                chequehm.put("bankAccount", (OLD_JE.getPaymentMethod().getAccount() != null) ? OLD_JE.getPaymentMethod().getAccount().getID() : "");
                chequehm.put("description", OLD_JE.getCheque().getDescription());
                chequehm.put("bankname", OLD_JE.getCheque().getBankName());
                chequehm.put("duedate", chequeDate);
                KwlReturnObject cqresult = journalEntryModuleServiceobj.addCheque(chequehm);
                Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
                if (cheque != null) {
                    jeDataMap.put("chequeid", cheque.getID());
                }
            }
            jeDataMap.put("jedetails", jeDetails);
            jeDataMap.put("externalCurrencyRate", OLD_JE.getExternalCurrencyRate());
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            JSONObject json = new JSONObject();
            json.put("JEid", jeid);
            json.put("parentid", OLD_JE.getID());
            accJournalEntryobj.updateJE(json, jeDetails);

            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        } catch (AccountingException ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("" + ex.getMessage(), ex);
        }

        return journalEntry;
    }

    public void updateRepeateInfo(RepeatedInvoices repeatedInvoicesObj) throws ServiceException, SessionExpiredException {
        Date nextDate = RepeatedInvoices.calculateNextDate(repeatedInvoicesObj.getNextDate(), repeatedInvoicesObj.getIntervalUnit(), repeatedInvoicesObj.getIntervalType());
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("NoOfRemainpost", repeatedInvoicesObj.getNoOfRemainInvoicespost() + 1);
        dataMap.put("id", repeatedInvoicesObj.getId());
        dataMap.put("nextDate", nextDate);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        c.setTime(nextDate);
        c.add(Calendar.DATE, (-repeatedInvoicesObj.getAdvanceNoofdays())); // subtracting advance no of days to next date
        String output = sdf.format(c.getTime());
        System.out.println(output);
        dataMap.put("advanceDate", c.getTime());
        c.add(Calendar.DATE, (-1)); // subtracting one day from advance date for sending mail
        output = sdf.format(c.getTime());
        System.out.println(output);
        dataMap.put("prevDate", c.getTime());
        accInvoiceDAOobj.saveRepeateInvoiceInfo(dataMap);
    }

    public void updateRepeateInfoForSO(RepeatedSalesOrder rinfo) throws ServiceException, SessionExpiredException {
        Date nextDate = RepeatedInvoices.calculateNextDate(rinfo.getNextDate(), rinfo.getIntervalUnit(), rinfo.getIntervalType());
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("NoOfRemainpost", rinfo.getNoOfRemainSOpost() + 1);
        dataMap.put("id", rinfo.getId());
        dataMap.put("nextDate", nextDate);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        c.setTime(nextDate);
        c.add(Calendar.DATE, (-1)); // subtracting one day from Next date for sending mail
        dataMap.put("prevDate", c.getTime());
        accSalesOrderDAOobj.saveRepeateSalesOrderInfo(dataMap);
    }

    public void updateRepeateInfoForJE(RepeatedJE rinfo) throws ServiceException, SessionExpiredException {
        Date nextDate = RepeatedJE.calculateNextDate(rinfo.getNextDate(), rinfo.getIntervalUnit(), rinfo.getIntervalType());
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("NoOfRemainJEpost", rinfo.getNoOfRemainJEpost() + 1);
//        dataMap.put("NoOfJEpost", rinfo.getNoOfJEpost() - 1);
        dataMap.put("id", rinfo.getId());
        dataMap.put("nextDate", nextDate);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        c.setTime(nextDate);
        c.add(Calendar.DATE, (-1)); // subtracting one day from Next date for sending mail
        dataMap.put("prevDate", c.getTime());
        accJournalEntryobj.saveRepeateJEInfo(dataMap);
    }

    public HashMap<String, Object> getExceptionMail(HashMap<String, Object> mailParams) throws ServiceException {
        String loggedUserid = "";
        String number = "";
        try {
            if (mailParams.get("documentObj") != null) {
                if (mailParams.get("documentObj") instanceof JournalEntry) {
                    JournalEntry Obj = (JournalEntry) mailParams.get("documentObj");
                    loggedUserid = Obj != null ? (Obj.getCreatedby().getUserID()) : "";
                    number = Obj != null ? (Obj.getEntryNumber()) : "";
                } else if (mailParams.get("documentObj") instanceof SalesOrder) {
                    SalesOrder Obj = (SalesOrder) mailParams.get("documentObj");
                    loggedUserid = Obj != null ? (Obj.getCreatedby().getUserID()) : "";
                    number = Obj != null ? (Obj.getSalesOrderNumber()) : "";
                } else if (mailParams.get("documentObj") instanceof Invoice) {
                    Invoice Obj = (Invoice) mailParams.get("documentObj");
                    loggedUserid = Obj != null ? (Obj.getCreatedby().getUserID()) : "";
                    number = Obj != null ? (Obj.getInvoiceNumber()) : "";
                }
            }
            if (!StringUtil.isNullOrEmpty(loggedUserid)) {
                String htmlMailContent = mailParams.get("failed") != null ? mailParams.get("failed").toString() : "";
                mailParams.put("htmlMailContent", "<br/>" + htmlMailContent);
                String plainMailContent = mailParams.get("failed") != null ? mailParams.get("failed").toString() : "";
                mailParams.put("plainMailContent", plainMailContent);
                mailParams.put("loginUserId", loggedUserid);
                mailParams.put("isFromGetExceptionMail", true);
                sendMail(mailParams);       //SDP-9651 Sends mail to Sagar A. Sir in case of Recurring failure
            }
        } catch (Exception ex) {
            Logger.getLogger(AccRepeateInvoiceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mailParams;
    }

    //Send notification mail 
    public void sendMail(HashMap requestParams) throws ServiceException {
        String loginUserId = (String) requestParams.get("loginUserId");
        User user = (User) accJournalEntryobj.getUserObject(loginUserId);
        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), user.getCompany().getCompanyID());
        Company company = (Company) returnObject.getEntityList().get(0);
        String sendorInfo = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        String htmlMailContent = (String) requestParams.get("htmlMailContent");
        String plainMailContent = (String) requestParams.get("plainMailContent");
        SimpleDateFormat sdf = new SimpleDateFormat();
        String cEmail = user.getEmailID() != null ? user.getEmailID() : "";
        boolean isFromGetExceptionMail = requestParams.get("isFromGetExceptionMail") != null ? Boolean.parseBoolean(requestParams.get("isFromGetExceptionMail").toString()) : false;
        if (isFromGetExceptionMail) {
            String subject = "Alert Recurring Failure";
            try {
                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                //please comment below line at the time of testing recurring
                SendMailHandler.postMail(new String[]{"sagar.ahire@deskera.com","sagar.mahamuni@deskera.com","suresh.sonawane@deskera.com"}, subject, htmlMailContent, plainMailContent, sendorInfo, smtpConfigMap);
//                SendMailHandler.postMail(new String[]{"ajay.motwani@deskera.com"}, subject, htmlMailContent, plainMailContent, sendorInfo, smtpConfigMap);
            } catch (Exception ex) {
                throw ServiceException.FAILURE("" + ex.getMessage(), ex);
            }
        } else {
            if (!StringUtil.isNullOrEmpty(cEmail)) {
                try {
                    String subject = "Recurring Alert Notification";
                    //String sendorInfo = "admin@deskera.com";
                    String htmlTextC = "";
                    htmlTextC += "<br/>Hello " + user.getFirstName() + "<br/>";
                    htmlTextC = htmlTextC + htmlMailContent;

                    htmlTextC += "<br/>Regards,<br/>";
                    htmlTextC += "<br/>ERP System<br/>";
                    htmlTextC += "<br/><br/>";
                    htmlTextC += "<br/>This is an auto generated email. Do not reply<br/>";

                    String plainMsgC = "";
                    plainMsgC += "\nHello " + user.getFirstName() + "\n";
                    plainMsgC = plainMsgC + plainMailContent;

                    plainMsgC += "\nRegards,\n";
                    plainMsgC += "\nERP System\n";
                    plainMsgC += "\n\n";
                    plainMsgC += "\nThis is an auto generated email. Do not reply.\n";

                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(new String[]{cEmail}, subject, htmlTextC, plainMsgC, sendorInfo, smtpConfigMap);
                } catch (Exception ex) {
                    throw ServiceException.FAILURE("" + ex.getMessage(), ex);
                }
            }
        }
    }//sendMail

    
    public List mapInvoiceDetailTerms(InvoiceDetailTermsMap idt, Inventory invObj, String userid, boolean isAvalaraIntegration) throws ServiceException {
        List ll = new ArrayList();
        try {
            HashMap<String, Object> termMap = new HashMap<String, Object>();
            if (!isAvalaraIntegration) {
                termMap.put("term", idt.getEntitybasedLineLevelTermRate().getLineLevelTerms().getId()); //Confirm this
            }
            termMap.put("termamount", idt.getTermamount());
            termMap.put("termpercentage", idt.getPercentage());
            termMap.put("assessablevalue", idt.getAssessablevalue());
            termMap.put("creationdate", new Date());
            termMap.put("accountid", idt.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
            termMap.put("userid", userid);
            termMap.put("purchasevalueorsalevalue", idt.getPurchaseValueOrSaleValue());
            termMap.put("deductionorabatementpercent", idt.getDeductionOrAbatementPercent());
            termMap.put("isDefault", idt.getEntitybasedLineLevelTermRate().getLineLevelTerms().isIsDefault());
            termMap.put("productentitytermid", idt.getEntitybasedLineLevelTermRate().getId());
            termMap.put("taxtype", idt.getTaxType());

            //Confirm below code
            if (idt.getTaxType() == 0) { // If Flat
                termMap.put("termamount", idt.getTermamount());
            } else { // Else Percentage
                termMap.put("termpercentage", idt.getPercentage());
            }

            ll.add(termMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return ll;
    }
}
