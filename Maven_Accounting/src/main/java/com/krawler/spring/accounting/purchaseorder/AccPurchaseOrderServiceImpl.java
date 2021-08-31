/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.purchaseorder;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.CommonIndonesianNumberToWords;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.ist.InterStoreTransferRequest;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.groupcompany.AccGroupCompanyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.handler.CommonEnglishNumberToWords;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.salesorder.AccSalesOrderServiceDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import com.krawler.spring.accounting.handler.AccLinkDataDao;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFuctionality.ExportRecordHandler;
import com.krawler.spring.mrp.WorkOrder.WorkOrderComponentDetails;
import java.io.File;
import java.io.FileInputStream;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 *
 * @author krawler
 */
public class AccPurchaseOrderServiceImpl implements AccPurchaseOrderServiceDAO {

    private accPurchaseOrderDAO accPurchaseOrderobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accVendorDAO accVendorDAOobj;
    private StockService stockService;
    private StoreService storeService;
    private accAccountDAO accAccountDAOobj;
    private accTaxDAO accTaxObj;
    private String successView;
    private accProductDAO accProductObj;
    private AccSalesOrderServiceDAO accSalesOrderServiceDAOobj;
    private CommonEnglishNumberToWords EnglishNumberToWordsOjb = new CommonEnglishNumberToWords();
    private CommonIndonesianNumberToWords IndonesianNumberToWordsOjb = new CommonIndonesianNumberToWords();
    private AccCommonTablesDAO accCommonTablesDAO;
    private accInvoiceDAO accInvoiceDAOobj;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private fieldDataManager fieldDataManagercntrl;
    private AccLinkDataDao accLinkDataDao;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private AccGroupCompanyDAO accGroupCompanyDAO;

    public void setaccGroupCompanyDAO(AccGroupCompanyDAO accGroupCompanyDAO) {
        this.accGroupCompanyDAO = accGroupCompanyDAO;
    }
    public void setAccLinkDataDao(AccLinkDataDao accLinkDataDao) {
        this.accLinkDataDao = accLinkDataDao;
    }
    
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }

    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }
    
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }

    public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    
    public void setaccSalesOrderServiceDAO(AccSalesOrderServiceDAO accSalesOrderServiceDAOobj) {
        this.accSalesOrderServiceDAOobj = accSalesOrderServiceDAOobj;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }
@Override
    public HashMap<String, Object> getPurchaseOrderMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.start, request.getParameter(Constants.start));
        requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        requestParams.put(Constants.REQ_costCenterId, request.getParameter(Constants.REQ_costCenterId));
        requestParams.put(Constants.REQ_vendorId, request.getParameter(Constants.REQ_vendorId));
        requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
        requestParams.put(Constants.userdf,authHandler.getUserDateFormatterWithoutTimeZone(request));
        requestParams.put(InvoiceConstants.newvendorid, request.getParameter(InvoiceConstants.newvendorid));
        requestParams.put(InvoiceConstants.productid, request.getParameter(InvoiceConstants.productid));
        requestParams.put(InvoiceConstants.productCategoryid, request.getParameter(InvoiceConstants.productCategoryid));
        requestParams.put("doflag", request.getParameter("doflag") != null ? true : false);
        boolean closeflag = request.getParameter("closeflag") != null ? Boolean.parseBoolean(request.getParameter("closeflag")) : false;
        requestParams.put("closeflag", closeflag);
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        requestParams.put(Constants.ValidFlag, request.getParameter(Constants.ValidFlag));
        requestParams.put(Constants.BillDate, request.getParameter(Constants.BillDate));
        requestParams.put("pendingapproval", (request.getParameter("pendingapproval") != null) ? Boolean.parseBoolean(request.getParameter("pendingapproval")) : false);
        requestParams.put("istemplate", (request.getParameter("istemplate") != null) ? Integer.parseInt(request.getParameter("istemplate")) : 0);
        requestParams.put(Constants.MARKED_FAVOURITE, request.getParameter(Constants.MARKED_FAVOURITE));
        requestParams.put("currencyid", request.getParameter("currencyid"));
        requestParams.put("exceptFlagINV", request.getParameter("exceptFlagINV"));
        requestParams.put("exceptFlagORD", request.getParameter("exceptFlagORD"));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        requestParams.put("currencyfilterfortrans", (request.getParameter("currencyfilterfortrans") == null) ? "" : request.getParameter("currencyfilterfortrans"));
        requestParams.put("isOpeningBalanceOrder", request.getParameter("isOpeningBalanceOrder") != null ? Boolean.parseBoolean(request.getParameter("isOpeningBalanceOrder")) : false);
        requestParams.put(CCConstants.REQ_vendorId, request.getParameter(CCConstants.REQ_vendorId));
        requestParams.put("isConsignment", request.getParameter("isConsignment")!=null?Boolean.parseBoolean(request.getParameter("isConsignment")):false);
        requestParams.put("isFixedAsset", (request.getParameter("isFixedAsset") != null) ? Boolean.parseBoolean(request.getParameter("isFixedAsset")) : false);
        return requestParams;
    }
@Override
   public JSONArray getPurchaseOrdersJsonMerged(HashMap<String, Object> requestParams, List list, JSONArray jArr) throws ServiceException {
        try {            
            boolean closeflag = false;
            String ss = "";
            if(requestParams.get("closeflag")!=null){
                closeflag = (Boolean) requestParams.get("closeflag");
            }
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = requestParams.get("companyid").toString();
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            boolean isFixedAsset = false;
            if(requestParams.get("isFixedAsset")!=null){
                isFixedAsset= Boolean.FALSE.parseBoolean(requestParams.get("isFixedAsset").toString());
            }
            boolean isForJobWorkOut = false;
            if (requestParams.get("isForJobWorkOut") != null) {
                isForJobWorkOut = Boolean.FALSE.parseBoolean(requestParams.get("isForJobWorkOut").toString());
            }                        
             boolean isOutstanding = false;          
            if (requestParams.get("isOutstanding") != null) {
                isOutstanding = Boolean.FALSE.parseBoolean(requestParams.get("isOutstanding").toString());
            }
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            boolean isConsignment=false;
            if(requestParams.get("isConsignment")!=null){
                isConsignment= Boolean.FALSE.parseBoolean(requestParams.get("isConsignment").toString());
            }
            
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid,isForJobWorkOut ? Constants.JOB_WORK_OUT_ORDER_MODULEID:isConsignment?Constants.Acc_ConsignmentVendorRequest_ModuleId
                                                :(!isFixedAsset?Constants.Acc_Purchase_Order_ModuleId:Constants.Acc_FixedAssets_Purchase_Order_ModuleId)));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
          
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat userdf = (DateFormat) requestParams.get(Constants.userdf);
            
            Iterator itr = list.iterator();
            boolean isForTemplate = (requestParams.containsKey("isForTemplate") && Boolean.parseBoolean(requestParams.get("isForTemplate").toString()))?true:false;
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            boolean isMalaysian = extraCompanyPreferences != null ? extraCompanyPreferences.getCompany().getCountry().getID().equalsIgnoreCase("137") : false;
            /*
            created DocumentEmailSetting Object for getting customershippingaddress flag
            */
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), companyid);
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences != null && extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            boolean isLineLevelTermFlag = false;//To Check Whether LinelevelTerms are applicable or not.
            if(extraCompanyPreferences != null && extraCompanyPreferences.getLineLevelTermFlag()==1){
                isLineLevelTermFlag = true;//If LineLevelTerms are applicable, then update the flag.
            }
            String compids[] = Constants.Companyids_Chkl_And_Marubishi.split(",");
            boolean isFromChklorMarubishi = false;
            for (int cnt = 0; cnt < compids.length; cnt++) {
                String compid = compids[cnt];
                if (compid.equalsIgnoreCase(companyid)) {
                    isFromChklorMarubishi = true;
                }
            }
            while (itr.hasNext()) {
                Object[] oj = (Object[])itr.next();                
                String orderid = oj[0].toString();
                //Withoutinventory 0 for normal, 1 for billing
                boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());
                {

                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), orderid);
                    PurchaseOrder purchaseOrder = (PurchaseOrder) objItr.getEntityList().get(0);
                    HashMap<String,Object> hashMap=new HashMap<String, Object>();
                    hashMap.put("invoiceID",purchaseOrder.getID());
                    hashMap.put("companyid",companyid);
                    KwlReturnObject object=accInvoiceDAOobj.getinvoiceDocuments(hashMap);
                    int attachemntcount=object.getRecordTotalCount();
                    Vendor vendor = purchaseOrder.getVendor();
                    KWLCurrency currency = null;
                    if(purchaseOrder.getCurrency() != null){
                        currency = purchaseOrder.getCurrency();
                    } else {
                        currency=purchaseOrder.getVendor().getAccount().getCurrency()==null?kwlcurrency:purchaseOrder.getVendor().getAccount().getCurrency();
                    }
                    //KWLCurrency currency=purchaseOrder.getVendor().getAccount().getCurrency()==null?kwlcurrency:purchaseOrder.getVendor().getAccount().getCurrency();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", purchaseOrder.getID());
                    obj.put("companyid", purchaseOrder.getCompany().getCompanyID());
                    obj.put("companyname", purchaseOrder.getCompany().getCompanyName());
                    obj.put("externalcurrencyrate", purchaseOrder.getExternalCurrencyRate());
                    obj.put("isOpeningBalanceTransaction", purchaseOrder.isIsOpeningBalancePO());
                    obj.put("withoutinventory", withoutinventory);
                    obj.put("isJobWorkOrderReciever", purchaseOrder.isIsJobWorkOutOrder());
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("currencycode", currency.getCurrencyCode()==null?"":currency.getCurrencyCode());
                    obj.put("personid", vendor.getID());
                    obj.put("gstapplicable", purchaseOrder.isIsIndGSTApplied());
                    obj.put("aliasname", vendor.getAliasname());
                    obj.put("personcode", vendor.getAcccode()==null?"":vendor.getAcccode());
                    obj.put("createdby", purchaseOrder.getCreatedby()==null?"":StringUtil.getFullName(purchaseOrder.getCreatedby()));
                    obj.put("billtoaddress", purchaseOrder.getBillingShippingAddresses()==null? "":CommonFunctions.getBillingShippingAddress(purchaseOrder.getBillingShippingAddresses(), true));
                    obj.put("shiptoaddress", purchaseOrder.getBillingShippingAddresses()==null? "":CommonFunctions.getBillingShippingAddress(purchaseOrder.getBillingShippingAddresses(), false));
                    obj.put("personemail", vendor.getEmail());
                    obj.put(Constants.HAS_ACCESS, vendor.isActivate());
//                    MasterItem gstRegistrationType = vendor != null ? vendor.getGSTRegistrationType() : null;
//                    if (gstRegistrationType != null && gstRegistrationType.getDefaultMasterItem() != null) {
//                        obj.put("GSTINRegTypeDefaultMstrID", gstRegistrationType.getDefaultMasterItem().getID());
//                    }
                    obj.put("billno", purchaseOrder.getPurchaseOrderNumber());
                    /**
                     * Put Merchant Exporter Check
                     */
                    obj.put(Constants.isMerchantExporter, purchaseOrder.isIsMerchantExporter());
                    obj.put("number", purchaseOrder.getPurchaseOrderNumber());
                    obj.put("duedate", df.format(purchaseOrder.getDueDate()));
                    obj.put("date", df.format(purchaseOrder.getOrderDate()));
                    obj.put("billdate", df.format(purchaseOrder.getOrderDate()));
                    obj.put("dateinuserformat", userdf.format(purchaseOrder.getOrderDate()));
                    obj.put("duedateinuserformat", userdf.format(purchaseOrder.getDueDate()));
                    obj.put("shipdate", purchaseOrder.getShipdate()==null? "" : df.format(purchaseOrder.getShipdate()));
                    obj.put("shipdateinuserformat", purchaseOrder.getShipdate()==null? "" : userdf.format(purchaseOrder.getShipdate()));
                    obj.put("shipvia", purchaseOrder.getShipvia()==null? "" : purchaseOrder.getShipvia());
                    obj.put("fob", purchaseOrder.getFob()==null?"" : purchaseOrder.getFob());
                    obj.put("isfavourite", purchaseOrder.isFavourite());
                    obj.put("isprinted", purchaseOrder.isPrinted());
                    obj.put("isEmailSent", purchaseOrder.isIsEmailSent());
                    obj.put("deleted", purchaseOrder.isDeleted());
                    obj.put("billto", purchaseOrder.getBillTo()==null?"":purchaseOrder.getBillTo());
                    obj.put("shipto", purchaseOrder.getShipTo()==null?"":purchaseOrder.getShipTo());
                    obj.put("agent", purchaseOrder.getMasteragent()==null ? "" : purchaseOrder.getMasteragent().getID());
                    obj.put("agentname", purchaseOrder.getMasteragent()==null ? "" : purchaseOrder.getMasteragent().getValue());
                    obj.put("isSOPOBlock", purchaseOrder.isLinkedSOBlocked());
                    obj.put("isdropshipchecked", purchaseOrder.isIsDropshipDocument());
                    obj.put(Constants.isDraft, purchaseOrder.isIsDraft());
                    obj.put(Constants.IsRoundingAdjustmentApplied, purchaseOrder.isIsRoundingAdjustmentApplied());
                    if (purchaseOrder.getApprover() != null) {
                        obj.put("approver", StringUtil.getFullName(purchaseOrder.getApprover()));
                    }
                    boolean gstIncluded = purchaseOrder.isGstIncluded();
                    obj.put("gstIncluded", gstIncluded);
                    obj.put("isConsignment", purchaseOrder.isIsconsignment());
                    obj.put("fixedAssetInvoice", purchaseOrder.isFixedAssetPO());
                    obj.put("termid", purchaseOrder.getTerm()==null?"":purchaseOrder.getTerm().getID());
                    /*
                     IF 'Show Customer Shipping Address in Purchase Doc' option is true get SO transactional level address
                    */
                    
                    /*----If dropship type PO----------  */
                    if(purchaseOrder.isIsDropshipDocument()){
                         AccountingAddressManager.getTransactionAddressJSONForDropShipDoc(obj, purchaseOrder.getBillingShippingAddresses());
                    } else if (documentEmailSettings != null && documentEmailSettings.isCustShippingAddressInPurDoc()) {
                         obj.put("isTransactionLevelAddress", true);
                        AccountingAddressManager.getTransactionAddressJSONForPOFromSO(obj, purchaseOrder.getBillingShippingAddresses(), true);
                    } else {
                        AccountingAddressManager.getTransactionAddressJSON(obj, purchaseOrder.getBillingShippingAddresses(), true);
                    }   
                  
                    obj.put("termdays", purchaseOrder.getTerm()==null ? 0:purchaseOrder.getTerm().getTermdays());
                    obj.put("termname", purchaseOrder.getTerm()==null ? 0:purchaseOrder.getTerm().getTermname());
                    obj.put("termdetails", getTermDetails(purchaseOrder.getID(),true));
                    obj.put("statusforcrosslinkage", purchaseOrder.isDisabledPOforSO()?"Closed":"Open");
                    obj.put("closedmanually", purchaseOrder.isIsPOClosed()?"Yes":"N/A");
                    obj.put("isexpenseinv", purchaseOrder.isIsExpenseType());
                    obj.put("purchaseordertype", purchaseOrder.isIsExpenseType() ? "Expense" : "Product"); //SDP-11100
                    obj.put("formtypeid", purchaseOrder.getFormtype());
                    obj.put("isInterstateParty", purchaseOrder.getVendor().isInterstateparty());
                    obj.put("gtaapplicable", purchaseOrder.isGtaapplicable());
                    obj.put("vattinno", !StringUtil.isNullOrEmpty(vendor.getVATTINnumber())?vendor.getVATTINnumber():"");
                    obj.put("csttinno", !StringUtil.isNullOrEmpty(vendor.getCSTTINnumber())?vendor.getCSTTINnumber():"");
                    obj.put("eccno", !StringUtil.isNullOrEmpty(vendor.getECCnumber())?vendor.getECCnumber():"");
                    obj.put("panno", !StringUtil.isNullOrEmpty(vendor.getPANnumber())?vendor.getPANnumber():"");
                    obj.put("servicetaxno", !StringUtil.isNullOrEmpty(vendor.getSERVICEnumber())?vendor.getSERVICEnumber():"");
                    obj.put("tanno", !StringUtil.isNullOrEmpty(vendor.getTANnumber())?vendor.getTANnumber():"");
                    if(purchaseOrder.getTermsincludegst()!=null) {
                        obj.put(Constants.termsincludegst, purchaseOrder.getTermsincludegst());
                    }
//                    obj.put("termamount", CommonFunctions.getTotalTermsAmount(getTermDetails(purchaseOrder.getID(),true)));  
                    obj.put(Constants.SEQUENCEFORMATID,purchaseOrder.getSeqformat()==null?"":purchaseOrder.getSeqformat().getID()); 
                    obj.put(Constants.SUPPLIERINVOICENO, purchaseOrder.getSupplierInvoiceNo() != null ? purchaseOrder.getSupplierInvoiceNo() : "");
                    
                    KwlReturnObject linkRresult = accLinkDataDao.checkEntryForTransactionInLinkingTableForForwardReference("PurchaseOrder",purchaseOrder.getID());
                    list = linkRresult.getEntityList();
                    if (list != null && !list.isEmpty()) {
                        /*
                         *This block of code is added to check cross link case.
                         * i.e. PO --> SO. crossLinkingTransaction on this check handle enable/disable cases of Generate SI and Generate DO button.
                         * SDP-14034
                         */
                        Iterator iterator = list.iterator();
                        boolean crossLinking = false;
                        boolean normalLinking = false;
                        while (iterator.hasNext()) {
                            PurchaseOrderLinking polink = (PurchaseOrderLinking) iterator.next();
                            if (polink.getModuleID() == Constants.Acc_Sales_Order_ModuleId) {
                                crossLinking = true;
                            } else {
                                normalLinking = true;
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
                    /**
                     * Put GST document history.
                     */
                    if (purchaseOrder.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                        obj.put("refdocid", purchaseOrder.getID());
                        fieldDataManagercntrl.getGSTDocumentHistory(obj);

                    }
                    obj.put("approvalstatus", purchaseOrder.getApprovestatuslevel());
                    String approvalStatus="";
                    ScriptEngineManager mgr = new ScriptEngineManager();
                    ScriptEngine engine = mgr.getEngineByName("JavaScript");
                    double amountinbase = purchaseOrder.getTotalamountinbase(); 
                    String multipleRuleids="";
                    if(purchaseOrder.getApprovestatuslevel() < 0){
                        approvalStatus="Rejected";
                    }else if(purchaseOrder.getApprovestatuslevel() < 11){
                        String ruleid = "",userRoleName="";
                        HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                        qdDataMap.put("companyid", companyid);
                        qdDataMap.put("level",purchaseOrder.getApprovestatuslevel() );
                        qdDataMap.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
                        KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                        Iterator ruleitr = flowresult.getEntityList().iterator();
                        while (ruleitr.hasNext()) {
                            Object[] rulerow = (Object[]) ruleitr.next();
                            /*
                             Added to get condition of approval rule i.e set when creating approval rule 
                             */
                            String rule = "";
                            String discountRule = "";
                            if (rulerow[2] != null) {
                                rule = rulerow[2].toString();
                            }
                            if (rulerow[7] != null) {
                                discountRule = rulerow[7].toString();
                            }
                            boolean flag = false;
                            ruleid = (String) rulerow[0];
                            boolean sendForApproval = false;
                            int appliedUpon = Integer.parseInt(rulerow[5].toString());
                            if (appliedUpon == Constants.Total_Amount) {
                                rule = rule.replaceAll("[$$]+", String.valueOf(amountinbase));
                            } else if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount || appliedUpon == Constants.Specific_Products_Category) {
                                 /*
                                 Handled for Product,product discount And product category
                                 */
                                JSONArray productDiscountJArr = new JSONArray();
                                Set<PurchaseOrderDetail> purchaseOrderDetail = purchaseOrder.getRows();
                                for (PurchaseOrderDetail poDetail : purchaseOrderDetail) {
                                    String productId = poDetail.getProduct().getID();
                                    double discountVal = poDetail.getDiscount();
                                    int isDiscountPercent = poDetail.getDiscountispercent();
                                    if (isDiscountPercent == 1) {
                                        discountVal = (poDetail.getQuantity() * poDetail.getRate()) * (discountVal / 100);
                                    }
                                    KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, discountVal, currencyid, purchaseOrder.getOrderDate(), purchaseOrder.getExternalCurrencyRate());
                                    double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                                    discAmountinBase = authHandler.round(discAmountinBase, companyid);
                                    JSONObject productDiscountObj = new JSONObject();
                                    productDiscountObj.put("productId", productId);
                                    productDiscountObj.put("discountAmount", discAmountinBase);
                                    productDiscountJArr.put(productDiscountObj);
                                }
                                if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount) {
                                    /*
                                     * Check If Rule is apply on specefic product
                                     *  and Specific product discount from multiapproverule window
                                     */
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
                        String[] multipleRuleidsArray = multipleRuleids.split(",");
                        for (int multiRule = 0; multiRule < multipleRuleidsArray.length; multiRule++) {
                            ruleid = multipleRuleidsArray[multiRule];
                        if (!StringUtil.isNullOrEmpty(ruleid)) {
                            qdDataMap.put("ruleid", ruleid);
                            KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(qdDataMap);
                            Iterator useritr = userResult.getEntityList().iterator();
                            while (useritr.hasNext()) {
                                Object[] userrow = (Object[]) useritr.next();
                                String userId = userrow[0].toString();
                                String fname = userrow[1].toString();
                                String lname = userrow[2].toString();
                                String userName = fname +" "+ lname;
                                
                                    /*
                                     Addded so duplicate approve's can be eleminated 
                                     */
                                    if(userRoleName.contains(userName)){
                                        continue;
                                    }
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
                        }
                        if (!StringUtil.isNullOrEmpty(userRoleName)) {
                            userRoleName = userRoleName.substring(0, userRoleName.length() - 1);
                        }
                        approvalStatus="Pending Approval" + ( StringUtil.isNullOrEmpty(userRoleName) ? "" : " by "+userRoleName )+" at Level - "+purchaseOrder.getApprovestatuslevel();
                    } else {
                        approvalStatus="Approved";
                    }
                    obj.put("approvalstatusinfo",approvalStatus);
                    Set<String> uniqueProductTaxList = new HashSet<String>();
                    double amount = 0,totalRowTaxAmt = 0d, totalDiscount = 0, discountPrice = 0,rowDiscountAmt = 0d, rowOtherTermNonTaxableAmount= 0d;
                    boolean includeprotax = false;
                    String taxname="";
                    double pobalncequantity=0;
                    double pobalnceamount = 0;
                    double subtotal = 0d;
                    double productTotalAmount = 0d;
//                    double termAmount = CommonFunctions.getTotalTermsAmount(getTermDetails(purchaseOrder.getID(),true));
                    if (purchaseOrder.isIsExpenseType()) {//** For Expense grid
                        for (ExpensePODetail epod : purchaseOrder.getExpenserows()) {
                            double rowAmount = epod.getRate();//this is user enter amount gainst column Amount on which discount can applied
                            double rowTax = 0;
                            double rowDiscount = 0;
//                            if (!epod.getPurchaseOrder().isGstIncluded()) {
//                                productTotalAmount +=epod.isIsdebit()?epod.getRate() : -(epod.getRate());
//                            } else {
//                             // productTotalAmount += epod.isIsdebit()?epod.getRateIncludingGst(): -(epod.getRateIncludingGst()); //used getRateIncludingGst() but it returns RateExcludingGst amount. 

//                            }
                             productTotalAmount += epod.isIsdebit()?epod.getRate(): -(epod.getRate());
                            //Discount
                            if (epod.getDiscount() != null) {
                                rowDiscount = epod.getDiscount().getDiscountValue();// adding discount to get total row level discount amount
                            }
                            //Tax
                            if (epod.getTax() != null) {//line level tax is given
                                includeprotax = true;
                                taxname += epod.getTax().getName() + ", ";
                                rowTax = epod.getRowTaxAmount();
                            }

                            if (gstIncluded) {//in including gst, tax already included so no need to add seperetaly
                                if (epod.isIsdebit()) {//if debit type then amount will be added
                                    amount += (rowAmount - rowDiscount);//subtracting discount to calculate total amount from all line level    
                                } else {//id debit type then amount will be subtracted
                                    amount -= (rowAmount - rowDiscount);//subtracting discount to calculate total amount from all line level    
                                }
                            } else {
                                if (epod.isIsdebit()) {//if debit type then amount will be added
                                    amount += (rowAmount - rowDiscount + rowTax);//subtracting discount and then sum tax to find total amount from all line level    
                                } else {//if debit type then amount will be subtracted
                                    amount -= (rowAmount - rowDiscount + rowTax);//subtracting discount and then sum tax to find total amount from all line level    
                                }
                            }
                            pobalnceamount += epod.getBalAmount(); 
                            if (epod.isIsdebit()) {
                                totalRowTaxAmt += rowTax;// sum up total line level tax
                                rowDiscountAmt += rowDiscount;// sum up total discount given at line level
                            } else {
                                totalRowTaxAmt -= rowTax;// sum up total line level tax
                                rowDiscountAmt -= rowDiscount;// sum up total discount given at line level
                            }
                        }
                        subtotal=productTotalAmount-rowDiscountAmt;
                        obj.put("productTotalAmount", productTotalAmount);
//                        obj.put("amountBeforeTax",authHandler.formattingDecimalForAmount((subtotal+termAmount),companyid));
                        obj.put("subtotal", subtotal);
			KwlReturnObject bAmtsub = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, subtotal, purchaseOrder.getCurrency().getCurrencyID(), purchaseOrder.getOrderDate(), purchaseOrder.getExternalCurrencyRate());
                        obj.put("subtotalinbase", authHandler.round((Double)bAmtsub.getEntityList().get(0), companyid));	//SDP-10058
                        obj.put("baseCurrencyId", extraCompanyPreferences.getCompany().getCurrency().getCurrencyID());	//SDP-10058
                        obj.put("baseCurrencyCode", extraCompanyPreferences.getCompany().getCurrency().getCurrencyCode());	//SDP-10058
                    } else {//** For Product Grid
                        
                        double availableQuantity = 0;
                        double balanceQuantity = 0;
                        boolean redColor = false;
                        boolean greenColor = false;
                        boolean yellowColor = false;
                        for (PurchaseOrderDetail pod : purchaseOrder.getRows()) {
                            double rate = authHandler.roundUnitPrice(pod.getRate(), companyid);
                            if (gstIncluded) {
                                rate = pod.getRateincludegst();
                            }
                            double quantity = authHandler.roundQuantity(pod.getQuantity(), companyid);
                            productTotalAmount += authHandler.round(rate * quantity, companyid);
                            double poPrice = authHandler.round(quantity * rate, companyid);
                            double discountPOD = authHandler.round(pod.getDiscount(), companyid);
                            if (pod.getDiscountispercent() == 1) {
                                discountPrice = (poPrice) - authHandler.round((poPrice * discountPOD / 100), companyid);
                                rowDiscountAmt += authHandler.round((poPrice * discountPOD / 100), companyid);
                            } else {
                                discountPrice = poPrice - discountPOD;
                                rowDiscountAmt += discountPOD;
                            }

                            //amount = amount - (sod.getQuantity() * sod.getRate() * sod.getDiscount()/100);
                            amount += discountPrice;
                            if (!gstIncluded) {
                                amount += authHandler.round(pod.getRowTaxAmount(), companyid);
                            }
                            if (pod.getTax() != null) {//line level tax is given
                                includeprotax = true;
                                taxname += pod.getTax().getName() + ", ";
                                totalRowTaxAmt += pod.getRowTaxAmount();
                                uniqueProductTaxList.add(pod.getTax().getID());
                            }
                            if (isLineLevelTermFlag) {
                                /**
                                 * ERP-34717
                                 * If GST Include, no need to add Tax Amount. 
                                 * Amount is already with tax 
                                 */
                                if (!gstIncluded){
                                    amount += authHandler.round(pod.getRowTermAmount(), companyid);
                                    amount += authHandler.round(pod.getOtherTermNonTaxableAmount(), companyid);
                                }
                                // Append OtherTermNonTaxableAmount for rach row.
                                rowOtherTermNonTaxableAmount += pod.getOtherTermNonTaxableAmount();
                                totalRowTaxAmt += pod.getRowTermAmount();
                            }// For Line level terms as tax
                            pobalncequantity += pod.getBalanceqty();
                            if (isOutstanding) {
                                Product product = pod.getProduct();
                                if (pod.getBalanceqty() == 0) {
                                    continue;
                                }

                                availableQuantity = product.getAvailableQuantity();
                                balanceQuantity = pod.getBalanceqty();
                                /*  Calculation for Outstanding PO report */
                                if (availableQuantity >= balanceQuantity) {
                                    greenColor = true;
                                } else if (balanceQuantity > availableQuantity && availableQuantity > 0) {
                                    yellowColor = true;
                                } else {
                                    redColor = true;
                                }
                            }
                     
                        }
                        obj.put("productTotalAmount", productTotalAmount);      //For purchase order
                        
                        if (isOutstanding) {
                            /* Sending parameter to client side for showing Outstanding PO in Color a/c to their status*/
                            if (yellowColor) {
                                obj.put("color", "Y");
                            } else if (redColor & greenColor) {
                                obj.put("color", "Y");
                            } else if (greenColor) {
                                obj.put("color", "G");
                            } else {
                                obj.put("color", "R");
                            }
                        }
                    }
                    
                    double discountPO=authHandler.round(purchaseOrder.getDiscount(), companyid);
                    if (purchaseOrder.getDiscount() != 0) {
                        if (purchaseOrder.isPerDiscount()) {
                            totalDiscount = authHandler.round((amount * discountPO / 100), companyid);
                            amount = amount - totalDiscount;
                        } else {
                            amount = amount - discountPO;
                            totalDiscount = discountPO;
                        }
                        obj.put("discounttotal", discountPO);
                    } else {
                        obj.put("discounttotal", 0);
                    }
//                    obj.put("discount", totalDiscount);
                    obj.put("discount", rowDiscountAmt);
                    obj.put("discountinbase", purchaseOrder.getDiscountinbase());
//                    obj.put("discountval", totalDiscount);
                    obj.put("discountispertotal", purchaseOrder.isPerDiscount());
                    if(purchaseOrder.isPerDiscount()){
                        obj.put("ispercentdiscount", purchaseOrder.isPerDiscount());
                        obj.put("discountval", discountPO);
                    }else{
                        obj.put("discountval", totalDiscount);
                    }
                    boolean isApplyTaxToTerms=purchaseOrder.isApplyTaxToTerms();
                    obj.put("isapplytaxtoterms", isApplyTaxToTerms);
                    
                    
                    double totalTermAmount=0;
                    double taxableTermamount = 0;
                    double totalTermTaxAmount=0;
                    List poTermMapList = kwlCommonTablesDAOObj.getSummationOfTermAmtAndTermTaxAmt(Constants.purchaseordertermmap, purchaseOrder.getID());
                    if(poTermMapList != null && !poTermMapList.isEmpty()){
                        Iterator termItr = poTermMapList.iterator();
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
                            if (purchaseOrder.isGstIncluded()) {
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
                    totalTermTaxAmount = authHandler.round(totalTermTaxAmount, companyid);
                    obj.put("termamount", totalTermAmount);
                    obj.put("currencysymbol", currency.getSymbol());
                    double  taxPercent=0;
                    if(purchaseOrder.getTax()!=null){
                        requestParams.put("transactiondate", purchaseOrder.getOrderDate());
                        requestParams.put("taxid", purchaseOrder.getTax().getID());
                        if (requestParams.containsKey(Constants.ss) && requestParams.get(Constants.ss) != null) {
                          ss = (String) requestParams.get("ss");
                          requestParams.remove("ss"); //serch string unnecessary getting added while getting tax list, no need to pass ss string here
                        }
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        requestParams.put("ss", !StringUtil.isNullOrEmpty(ss) ? ss : "");
                        List taxList = result.getEntityList();
                        if (!taxList.isEmpty()) {
                            Object[] taxObj = (Object[]) taxList.get(0);
                            taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        }
                    }
                    double orderAmount=amount;//double orderAmount=(Double) bAmt.getEntityList().get(0);
                    double ordertaxamount=(taxPercent==0?0:authHandler.round(((orderAmount + taxableTermamount)*taxPercent/100), companyid));
                    obj.put("amountbeforegst", amount-totalRowTaxAmt); // Amount before both kind of tax row level or transaction level
                    obj.put("taxpercent", taxPercent);

                    amount=amount+totalTermAmount+ordertaxamount+totalTermTaxAmount;
                    orderAmount+=totalTermAmount + totalTermTaxAmount;
                    
                    obj.put("orderamount",orderAmount );
                    double totalAmt = orderAmount + ordertaxamount;
                    if (purchaseOrder.isIsRoundingAdjustmentApplied()) {
                        totalAmt += purchaseOrder.getRoundingadjustmentamount();
                        amount += purchaseOrder.getRoundingadjustmentamount();
                    }
                    obj.put("orderamountwithTax",totalAmt);
                    obj.put("amount", amount); 
                    obj.put("amountInWodrs", currency.getName() + " " + EnglishNumberToWordsOjb.convert(authHandler.round(amount, companyid), purchaseOrder.getCurrency(),countryLanguageId));
                    
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, purchaseOrder.getCurrency().getCurrencyID(), purchaseOrder.getOrderDate(), purchaseOrder.getExternalCurrencyRate());
                    obj.put("amountinbase", authHandler.round((Double)bAmt.getEntityList().get(0), companyid));
                    obj.put("personname", vendor.getName());
                    obj.put("vendor", vendor.getName());
                    obj.put("memo", purchaseOrder.getMemo());
                    obj.put("posttext",purchaseOrder.getPostText());
                    obj.put("taxid", purchaseOrder.getTax()==null?"":purchaseOrder.getTax().getID());
                    obj.put("costcenterid", purchaseOrder.getCostcenter()==null?"":purchaseOrder.getCostcenter().getID());
                    obj.put("costcenterName", purchaseOrder.getCostcenter()==null?"":purchaseOrder.getCostcenter().getName());
                    obj.put("shiplengthval", purchaseOrder.getShiplength());
                    obj.put("invoicetype", purchaseOrder.getInvoicetype());
                    obj.put("archieve", 0);
                    obj.put("attachment",attachemntcount);                
                  
                    List<String> taxList = new ArrayList(Arrays.asList(taxname.split(", ")));
                    Collections.sort(taxList);
                    String taxname1 = "";
                    for (String str : taxList) {
                        taxname1 += str + ", ";
                    }
                    obj.put("includeprotax", includeprotax);
                    if (includeprotax) {
                        obj.put("taxname", taxname1.substring(0, taxname1.length() > 1 ? taxname1.length() - 2 : taxname1.length()));
                    } else {
                        obj.put("taxname", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getName());
                    }
                    
                    double taxAmt=0;
                    //As either row level tax will be available or invoice level
                    if (isLineLevelTermFlag) {
                        // If LineLevelTerm is applicable then add the value in JSON Object.
                        obj.put(Constants.OtherTermNonTaxableAmount ,rowOtherTermNonTaxableAmount);
                        taxAmt = totalRowTaxAmt;
                    } else {
                        if (purchaseOrder.getTax() == null) {// means global level tax is not given
                            taxAmt = totalRowTaxAmt;
                        } else {
                            taxAmt = ordertaxamount;
                        }
                    }
                    
                    if (gstIncluded) {
                        subtotal = productTotalAmount - rowDiscountAmt - (taxAmt);
                    } else {
                        subtotal = productTotalAmount - rowDiscountAmt;
                    }
                    obj.put("amountBeforeTax", authHandler.formattingDecimalForAmount((subtotal+totalTermAmount),companyid));      //For purchase order
                    obj.put("subtotal", subtotal); 
		    KwlReturnObject bAmtsub = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, subtotal, purchaseOrder.getCurrency().getCurrencyID(), purchaseOrder.getOrderDate(), purchaseOrder.getExternalCurrencyRate());
                    obj.put("subtotalinbase", authHandler.round((Double)bAmtsub.getEntityList().get(0), companyid));	//SDP-10058                   
                    obj.put("baseCurrencyId", extraCompanyPreferences.getCompany().getCurrency().getCurrencyID());	//SDP-10058
                    obj.put("baseCurrencyCode", extraCompanyPreferences.getCompany().getCurrency().getCurrencyCode());	//SDP-10058
                    obj.put("taxamount",taxAmt + totalTermTaxAmount);// Tax Amount
                    
                    if(purchaseOrder.getModifiedby()!=null){
                        obj.put("lasteditedby",StringUtil.getFullName(purchaseOrder.getModifiedby()));
                    }   
                    String status = getPurchaseOrderStatus(purchaseOrder);
                    if (!purchaseOrder.isIsExpenseType()) {
                        /**
                         * Commented below line as the status will be fetched bases on query result below
                         */
//                      obj.put("status", (pobalncequantity <= 0 || purchaseOrder.isIsPOClosed() || purchaseOrder.isDeleted()) ? "Closed" : "Open");

                        /**
                         * The status of PO bases on query result
                         */
                        String poStatus = "Closed";
                        double count = accPurchaseOrderobj.getPOStatusOnBalanceQty(purchaseOrder.getID(), companyid);
                        if (count > 0) {
                            poStatus = "Open";
                        }
                        obj.put("status", poStatus.equalsIgnoreCase("Closed") ? "Closed" : (purchaseOrder.isIsPOClosed() ? "Closed" : "Open"));
                    } else if (purchaseOrder.isIsExpenseType()) {
                        /**
                         * to Update Status of 'Expese PO' according to 'Balance
                         * Amount '.
                         */
                        obj.put("status", (pobalnceamount <= 0 || purchaseOrder.isIsPOClosed() || purchaseOrder.isDeleted()) ? "Closed" : "Open");
                    }
                    boolean isExport=(requestParams.get("isExport")==null)?false:true;                  
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    PurchaseOrderCustomData jeDetailCustom = (PurchaseOrderCustomData) purchaseOrder.getPoCustomData();
                    replaceFieldMap = new HashMap<String, String>();
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put("companyid", companyid);
                        params.put("isExport", isExport);
                        params.put(Constants.userdf, userdf);
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                    /**
                     * Get Module template and its mapped Unit details for company if Line level term flag ON 
                     */
                    if(isForTemplate){
                        HashMap<String, Object> ModuleTempParams = new HashMap<>();
                        ModuleTempParams.put("modulerecordid", purchaseOrder.getID());
                        ModuleTempParams.put("companyid", companyid);
                        /** Get Module template  from invoice id . In module template purchase id add as modulerecordid */
                        KwlReturnObject ModuleTempObj = accountingHandlerDAOobj.getModuleTemplates(ModuleTempParams);    
                        if(ModuleTempObj!=null && ModuleTempObj.getEntityList().size() > 0){
                            ModuleTemplate moduleTemp = (ModuleTemplate) ModuleTempObj.getEntityList().get(0);
                            obj.put("companyunitid", moduleTemp.getCompanyUnitid());

                            HashMap tmpHashMap = new HashMap();
                            tmpHashMap.put("companyunitid", moduleTemp.getCompanyUnitid());
                            obj.put("populateproducttemplate", moduleTemp.isPopulateproductintemp());
                            obj.put("populatecustomertemplate", moduleTemp.isPopulatecustomerintemp());
                            tmpHashMap.put(Constants.companyKey, companyid);
                            /* Get Company Unit details from companyunitid mapped with module template */
                            KwlReturnObject exciseTemp = accountingHandlerDAOobj.getExciseTemplatesMap(tmpHashMap);
                        }
                    }
                    if(status.equalsIgnoreCase("QA Failed") || status.equalsIgnoreCase("Pending QA Approval")){
                        jArr.put(obj);
                    }else{
                        if ((!closeflag&&(!requestParams.containsKey("isOutstanding")||(requestParams.containsKey("isOutstanding")&&!(Boolean)requestParams.get("isOutstanding")))) || (requestParams.containsKey("isOutstanding")&&(Boolean)requestParams.get("isOutstanding") && status.equalsIgnoreCase("open"))) {
                                 jArr.put(obj);
                        }
                    }

                    KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                    Company company = (Company) cmp.getEntityList().get(0);
                    int countryid = company.getCountry() != null ? Integer.parseInt(company.getCountry().getID()) : 0;
                    if (Constants.indian_country_id == countryid && isFixedAsset) {
                        List<ExciseDetailsAssets> ed = null;
                        if (extraCompanyPreferences.isExciseApplicable()) {
                            KwlReturnObject exciseDetails = accGoodsReceiptobj.getExciseDetailsAssetPurchaseOrder(purchaseOrder.getID());
                            ed = exciseDetails.getEntityList();
                        }
                        if (extraCompanyPreferences.isExciseApplicable() && ed.size() > 0) {
                            obj.put("assetExciseid", ed.get(0).getId());
                            obj.put("suppliers", ed.get(0).getSupplier());
                            obj.put("supplierTINSalesTAXNo", ed.get(0).getSupplierTINSalesTaxNo());
                            obj.put("supplierExciseRegnNo", ed.get(0).getSupplierExciseRegnNo());
                            obj.put("cstnumber", ed.get(0).getCstnumber());
                            obj.put("supplierRange", ed.get(0).getSupplierRange());
                            obj.put("supplierCommissionerate", ed.get(0).getSupplierCommissioneRate());
                            obj.put("supplierAddress", ed.get(0).getSupplierAddress());
                            obj.put("supplierImporterExporterCode", ed.get(0).getSupplierImporterExporterCode());
                            obj.put("supplierDivision", ed.get(0).getSupplierDivision());
                            obj.put("manufacturername", ed.get(0).getManufacturerName());
                            obj.put("manufacturerExciseRegnNo", ed.get(0).getManufacturerExciseregnNo());
                            obj.put("manufacturerRange", ed.get(0).getManufacturerRange());
                            obj.put("manufacturerCommissionerate", ed.get(0).getManufacturerCommissionerate());
                            obj.put("manufacturerDivision", ed.get(0).getManufacturerDivision());
                            obj.put("manufacturerAddress", ed.get(0).getManufacturerAddress());
                            obj.put("manufacturerImporterExporterCode", ed.get(0).getManufacturerImporterexporterCode());
                            obj.put("InvoicenoManuFacture", ed.get(0).getInvoicenoManufacture());
                            obj.put("InvoiceDateManuFacture", ed.get(0).getInvoiceDateManufacture());
                            obj.put("supplierState", ed.get(0).getSupplierstate());

                        }
                    }  
                }            
                }            
//            jobj.put("data", jArr);
        } catch (Exception ex){
            throw ServiceException.FAILURE("getPurchaseOrdersJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }
/**
 * To get json for security get entry report
 * @param requestParams
 * @param list
 * @param jArr
 * @return
 * @throws ServiceException 
 */
    public JSONArray getSecurityGateEntryJsonMerged(HashMap<String, Object> requestParams, List list, JSONArray jArr) throws ServiceException {
        try {
            boolean closeflag = false;
            if (requestParams.get("closeflag") != null) {
                closeflag = (Boolean) requestParams.get("closeflag");
            }
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = requestParams.get("companyid").toString();
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();

            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_SecurityGateEntry_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat userdf = (DateFormat) requestParams.get(Constants.userdf);

            Iterator itr = list.iterator();
            boolean isForTemplate = (requestParams.containsKey("isForTemplate") && Boolean.parseBoolean(requestParams.get("isForTemplate").toString())) ? true : false;
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            boolean isLineLevelTermFlag = false;//To Check Whether LinelevelTerms are applicable or not.
            if (extraCompanyPreferences != null && extraCompanyPreferences.getLineLevelTermFlag() == 1) {
                isLineLevelTermFlag = true;//If LineLevelTerms are applicable, then update the flag.
            }
            for (Object obj1 : list) {
                String orderid = obj1.toString();
                {

                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SecurityGateEntry.class.getName(), orderid);
                    SecurityGateEntry securityGateEntry = (SecurityGateEntry) objItr.getEntityList().get(0);
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", securityGateEntry.getID());
                    hashMap.put("companyid", companyid);
                    KwlReturnObject object = accInvoiceDAOobj.getinvoiceDocuments(hashMap);
                    int attachemntcount = object.getRecordTotalCount();
                    Vendor vendor = securityGateEntry.getVendor();
                    KWLCurrency currency = null;
                    if (securityGateEntry.getCurrency() != null) {
                        currency = securityGateEntry.getCurrency();
                    } else {
                        currency = securityGateEntry.getVendor().getAccount().getCurrency() == null ? kwlcurrency : securityGateEntry.getVendor().getAccount().getCurrency();
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("billid", securityGateEntry.getID());
                    obj.put("companyid", securityGateEntry.getCompany().getCompanyID());
                    obj.put("companyname", securityGateEntry.getCompany().getCompanyName());
                    obj.put("externalcurrencyrate", securityGateEntry.getExternalCurrencyRate());
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("currencycode", currency.getCurrencyCode() == null ? "" : currency.getCurrencyCode());
                    obj.put("personid", vendor.getID());
                    obj.put("aliasname", vendor.getAliasname());
                    obj.put("personcode", vendor.getAcccode() == null ? "" : vendor.getAcccode());
                    obj.put("createdby", securityGateEntry.getCreatedby() == null ? "" : StringUtil.getFullName(securityGateEntry.getCreatedby()));
                    obj.put("billtoaddress", securityGateEntry.getBillingShippingAddresses() == null ? "" : CommonFunctions.getBillingShippingAddress(securityGateEntry.getBillingShippingAddresses(), true));
                    obj.put("shiptoaddress", securityGateEntry.getBillingShippingAddresses() == null ? "" : CommonFunctions.getBillingShippingAddress(securityGateEntry.getBillingShippingAddresses(), false));
                    obj.put("personemail", vendor.getEmail());
                    obj.put(Constants.HAS_ACCESS, vendor.isActivate());
                    obj.put("billno", securityGateEntry.getSecurityNumber());
                    obj.put("duedate", df.format(securityGateEntry.getDueDate()));
                    obj.put("date", df.format(securityGateEntry.getSecurityDate()));
                    obj.put("dateinuserformat", userdf.format(securityGateEntry.getSecurityDate()));
                    obj.put("duedateinuserformat", userdf.format(securityGateEntry.getDueDate()));
                    obj.put("shipdate", securityGateEntry.getShipdate() == null ? "" : df.format(securityGateEntry.getShipdate()));
                    obj.put("shipdateinuserformat", securityGateEntry.getShipdate() == null ? "" : userdf.format(securityGateEntry.getShipdate()));
                    obj.put("shipvia", securityGateEntry.getShipvia() == null ? "" : securityGateEntry.getShipvia());
                    obj.put("fob", securityGateEntry.getFob() == null ? "" : securityGateEntry.getFob());
                    obj.put("isfavourite", securityGateEntry.isFavourite());
                    obj.put("isEmailSent", securityGateEntry.isIsEmailSent());
                    obj.put("deleted", securityGateEntry.isDeleted());
                    obj.put("billto", securityGateEntry.getBillTo() == null ? "" : securityGateEntry.getBillTo());
                    obj.put("shipto", securityGateEntry.getShipTo() == null ? "" : securityGateEntry.getShipTo());
                    obj.put("agent", securityGateEntry.getMasteragent() == null ? "" : securityGateEntry.getMasteragent().getID());
                    obj.put("agentname", securityGateEntry.getMasteragent() == null ? "" : securityGateEntry.getMasteragent().getValue());
                    if (securityGateEntry.getApprover() != null) {
                        obj.put("approver", StringUtil.getFullName(securityGateEntry.getApprover()));
                    }
                    boolean gstIncluded = securityGateEntry.isGstIncluded();
                    obj.put("gstIncluded", gstIncluded);
                    obj.put("termid", securityGateEntry.getTerm() == null ? "" : securityGateEntry.getTerm().getID());
                    obj = AccountingAddressManager.getTransactionAddressJSON(obj, securityGateEntry.getBillingShippingAddresses(), true);
                    obj.put("termdays", securityGateEntry.getTerm() == null ? 0 : securityGateEntry.getTerm().getTermdays());
                    obj.put("termname", securityGateEntry.getTerm() == null ? 0 : securityGateEntry.getTerm().getTermname());
                    obj.put("termdetails", getTermDetails(securityGateEntry.getID(), true));
                    obj.put("statusforcrosslinkage", securityGateEntry.isDisabledPOforSO() ? "Closed" : "Open");
                    obj.put("closedmanually", securityGateEntry.isIsPOClosed() ? "Yes" : "N/A");
                    obj.put("isInterstateParty", securityGateEntry.getVendor().isInterstateparty());
                    obj.put("vattinno", !StringUtil.isNullOrEmpty(vendor.getVATTINnumber()) ? vendor.getVATTINnumber() : "");
                    obj.put("csttinno", !StringUtil.isNullOrEmpty(vendor.getCSTTINnumber()) ? vendor.getCSTTINnumber() : "");
                    obj.put("eccno", !StringUtil.isNullOrEmpty(vendor.getECCnumber()) ? vendor.getECCnumber() : "");
                    obj.put("panno", !StringUtil.isNullOrEmpty(vendor.getPANnumber()) ? vendor.getPANnumber() : "");
                    obj.put("servicetaxno", !StringUtil.isNullOrEmpty(vendor.getSERVICEnumber()) ? vendor.getSERVICEnumber() : "");
                    obj.put("tanno", !StringUtil.isNullOrEmpty(vendor.getTANnumber()) ? vendor.getTANnumber() : "");
                    if (securityGateEntry.getTermsincludegst() != null) {
                        obj.put(Constants.termsincludegst, securityGateEntry.getTermsincludegst());
                    }
                    obj.put("termamount", CommonFunctions.getTotalTermsAmount(getTermDetails(securityGateEntry.getID(), true)));
                    obj.put(Constants.SEQUENCEFORMATID, securityGateEntry.getSeqformat() == null ? "" : securityGateEntry.getSeqformat().getID());
                    obj.put(Constants.SUPPLIERINVOICENO, securityGateEntry.getSupplierInvoiceNo() != null ? securityGateEntry.getSupplierInvoiceNo() : "");
                    obj.put("approvalstatus", securityGateEntry.getApprovestatuslevel());
                    obj.put("approvalstatusinfo", "Approved");
                    double amount = 0, totalRowTaxAmt = 0d, totalDiscount = 0, discountPrice = 0, rowDiscountAmt = 0d, rowOtherTermNonTaxableAmount = 0d;
                    boolean includeprotax = false;
                    String taxname = "";
                    double subtotal = 0d;

                    /**
                     * Put GST document history.
                     */
                    if (securityGateEntry.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                        obj.put("refdocid", securityGateEntry.getID());
                        fieldDataManagercntrl.getGSTDocumentHistory(obj);

                    }
                    
                    for (SecurityGateDetails pod : securityGateEntry.getRows()) {
                        double rate = authHandler.roundUnitPrice(pod.getRate(), companyid);
                        if (gstIncluded) {
                            rate = pod.getRateincludegst();
                        }
                        double quantity = authHandler.roundQuantity(pod.getQuantity(), companyid);
                        subtotal += rate * quantity;
                        double poPrice = authHandler.round(quantity * rate, companyid);
                        double discountPOD = authHandler.round(pod.getDiscount(), companyid);
                        if (pod.getDiscountispercent() == 1) {
                            discountPrice = (poPrice) - authHandler.round((poPrice * discountPOD / 100), companyid);
                            rowDiscountAmt += authHandler.round((poPrice * discountPOD / 100), companyid);
                        } else {
                            discountPrice = poPrice - discountPOD;
                            rowDiscountAmt += discountPOD;
                        }

                        //amount = amount - (sod.getQuantity() * sod.getRate() * sod.getDiscount()/100);
                        amount += discountPrice;
                        if (!gstIncluded) {
                            amount += authHandler.round(pod.getRowTaxAmount(), companyid);
                        }
                        if (pod.getTax() != null) {//line level tax is given
                            includeprotax = true;
                            taxname += pod.getTax().getName() + ", ";
                            totalRowTaxAmt += pod.getRowTaxAmount();
                        }
                        if (isLineLevelTermFlag) {
                            amount += authHandler.round(pod.getRowTermAmount(), companyid);
                            amount += authHandler.round(pod.getOtherTermNonTaxableAmount(), companyid);
                            // Append OtherTermNonTaxableAmount for rach row.
                            rowOtherTermNonTaxableAmount += pod.getOtherTermNonTaxableAmount();
                            totalRowTaxAmt += pod.getRowTermAmount();
                        }// For Line level terms as tax
                    }
                    obj.put("subtotal", subtotal);
                    double discountPO = authHandler.round(securityGateEntry.getDiscount(), companyid);
                    if (securityGateEntry.getDiscount() != 0) {
                        if (securityGateEntry.isPerDiscount()) {
                            totalDiscount = authHandler.round((amount * discountPO / 100), companyid);
                            amount = amount - totalDiscount;
                        } else {
                            amount = amount - discountPO;
                            totalDiscount = discountPO;
                        }
                        obj.put("discounttotal", discountPO);
                    } else {
                        obj.put("discounttotal", 0);
                    }
                    obj.put("discount", rowDiscountAmt);
                    obj.put("discountinbase", securityGateEntry.getDiscountinbase());
                    obj.put("discountispertotal", securityGateEntry.isPerDiscount());
                    if (securityGateEntry.isPerDiscount()) {
                        obj.put("ispercentdiscount", securityGateEntry.isPerDiscount());
                        obj.put("discountval", discountPO);
                    } else {
                        obj.put("discountval", totalDiscount);
                    }

                    double totalTermAmount = 0;
                    double taxableTermamount = 0;
                    HashMap<String, Object> requestParam = new HashMap();
                    HashMap<String, Object> filterrequestParams = new HashMap();
                    requestParam.put("securityGateEntry", securityGateEntry.getID());
                    KwlReturnObject purchaseOrderResult = null;
                    filterrequestParams.put("taxid", securityGateEntry.getTax() == null ? "" : securityGateEntry.getTax().getID());
                    totalTermAmount = authHandler.round(totalTermAmount, companyid);
                    obj.put("currencysymbol", currency.getSymbol());
                    double taxPercent = 0;
                    if (securityGateEntry.getTax() != null) {
                        requestParams.put("transactiondate", securityGateEntry.getSecurityDate());
                        requestParams.put("taxid", securityGateEntry.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    }
                    double orderAmount = amount;//double orderAmount=(Double) bAmt.getEntityList().get(0);
                    double ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round(((orderAmount + taxableTermamount) * taxPercent / 100), companyid));
                    obj.put("amountbeforegst", amount - totalRowTaxAmt); // Amount before both kind of tax row level or transaction level
                    obj.put("taxpercent", taxPercent);
                    amount = amount + totalTermAmount + ordertaxamount;
                    orderAmount += totalTermAmount;
                    obj.put("orderamount", orderAmount);
                    obj.put("orderamountwithTax", orderAmount + ordertaxamount);
                    obj.put("amount", amount);
                    obj.put("amountInWodrs", currency.getName() + " " + EnglishNumberToWordsOjb.convert(authHandler.round(amount, companyid), securityGateEntry.getCurrency(), countryLanguageId));

                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, securityGateEntry.getCurrency().getCurrencyID(), securityGateEntry.getSecurityDate(), securityGateEntry.getExternalCurrencyRate());
                    obj.put("amountinbase", authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                    obj.put("personname", vendor.getName());
                    obj.put("memo", securityGateEntry.getMemo());
                    obj.put("posttext", securityGateEntry.getPostText());
                    obj.put("taxid", securityGateEntry.getTax() == null ? "" : securityGateEntry.getTax().getID());
                    obj.put("costcenterid", securityGateEntry.getCostcenter() == null ? "" : securityGateEntry.getCostcenter().getID());
                    obj.put("costcenterName", securityGateEntry.getCostcenter() == null ? "" : securityGateEntry.getCostcenter().getName());
                    obj.put("shiplengthval", securityGateEntry.getShiplength());
                    obj.put("archieve", 0);
                    obj.put("attachment", attachemntcount);

                    List<String> taxList = new ArrayList(Arrays.asList(taxname.split(", ")));
                    Collections.sort(taxList);
                    String taxname1 = "";
                    for (String str : taxList) {
                        taxname1 += str + ", ";
                    }
                    obj.put("includeprotax", includeprotax);
                    if (includeprotax) {
                        obj.put("taxname", taxname1.substring(0, taxname1.length() > 1 ? taxname1.length() - 2 : taxname1.length()));
                    } else {
                        obj.put("taxname", securityGateEntry.getTax() == null ? "" : securityGateEntry.getTax().getName());
                    }

                    double taxAmt = 0;
                    //As either row level tax will be available or invoice level
                    if (isLineLevelTermFlag) {
                        // If LineLevelTerm is applicable then add the value in JSON Object.
                        obj.put(Constants.OtherTermNonTaxableAmount, rowOtherTermNonTaxableAmount);
                        taxAmt = totalRowTaxAmt;
                    } else {
                        if (securityGateEntry.getTax() == null) {// means global level tax is not given
                            taxAmt = totalRowTaxAmt;
                        } else {
                            taxAmt = ordertaxamount;
                        }
                    }

                    obj.put("taxamount", taxAmt);

                    if (securityGateEntry.getModifiedby() != null) {
                        obj.put("lasteditedby", StringUtil.getFullName(securityGateEntry.getModifiedby()));
                    }
                    boolean isExport = (requestParams.get("isExport") == null) ? false : true;
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    replaceFieldMap = new HashMap<String, String>();
                    /**
                     * Get Module template and its mapped Unit details for
                     * company if Line level term flag ON
                     */
                    if (isForTemplate) {
                        HashMap<String, Object> ModuleTempParams = new HashMap<>();
                        ModuleTempParams.put("modulerecordid", securityGateEntry.getID());
                        ModuleTempParams.put("companyid", companyid);
                        /**
                         * Get Module template from invoice id . In module
                         * template purchase id add as modulerecordid
                         */
                        KwlReturnObject ModuleTempObj = accountingHandlerDAOobj.getModuleTemplates(ModuleTempParams);
                        if (ModuleTempObj != null && ModuleTempObj.getEntityList().size() > 0) {
                            ModuleTemplate moduleTemp = (ModuleTemplate) ModuleTempObj.getEntityList().get(0);
                            obj.put("companyunitid", moduleTemp.getCompanyUnitid());

                            HashMap tmpHashMap = new HashMap();
                            tmpHashMap.put("companyunitid", moduleTemp.getCompanyUnitid());
                            obj.put("populateproducttemplate", moduleTemp.isPopulateproductintemp());
                            obj.put("populatecustomertemplate", moduleTemp.isPopulatecustomerintemp());
                            tmpHashMap.put(Constants.companyKey, companyid);
                            /*
                             * Get Company Unit details from companyunitid
                             * mapped with module template
                             */
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
                    /*
                    This block is added when custom field/dimensions implemented for Security Gate Entry module
                    */
                    
                    SecurityGateEntryCustomData securityGateEntryCustomData=(SecurityGateEntryCustomData)securityGateEntry.getSgeCustomData();
                    if (securityGateEntryCustomData != null) {
                        AccountingManager.setCustomColumnValues(securityGateEntryCustomData, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put(Constants.companyKey, companyid);
                        params.put(Constants.isExport, isExport);      
                        if (!StringUtil.isNullOrEmpty((String)requestParams.get(Constants.browsertz))) {
                            params.put(Constants.browsertz, requestParams.get(Constants.browsertz));
                        }
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
    //---------------------------------------------------------------------------------------------------------------
                    if ((!closeflag && (!requestParams.containsKey("isOutstanding") || (requestParams.containsKey("isOutstanding") && !(Boolean) requestParams.get("isOutstanding")))) || (requestParams.containsKey("isOutstanding") && (Boolean) requestParams.get("isOutstanding"))) {
                        jArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getSecurityGateEntryJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

@Override
    public String getBillingPurchaseOrderStatus(BillingPurchaseOrder po) throws ServiceException {
        Set<BillingPurchaseOrderDetail> orderDetail = po.getRows();
        Iterator ite = orderDetail.iterator();
        String result = "Closed";
        while (ite.hasNext()) {
            BillingPurchaseOrderDetail pDetail = (BillingPurchaseOrderDetail) ite.next();
            KwlReturnObject bgrresult = accGoodsReceiptobj.getBRDFromBPOD(pDetail.getID());
            Iterator ite1 = bgrresult.getEntityList().iterator();
            double qua = 0;
            while (ite1.hasNext()) {
                BillingGoodsReceiptDetail ge = (BillingGoodsReceiptDetail) ite1.next();
                qua += ge.getQuantity();
            }
            if (qua < pDetail.getQuantity()) {
                result = "Open";
                break;
            }
        }
        return result;
    }
@Override
    public String getPurchaseOrderStatus(PurchaseOrder po) throws ServiceException {
        String result = "Closed";
        try{                             
        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), po.getCompany().getCompanyID());
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
        
            int status=getQAPOStatus(po);
            if(status==Constants.Pending_QA_Approval){
                result="Pending QA Approval";
                return result;
            }else if(status==Constants.QA_Rejected){
                result="QA Failed";
                return result;
            }
        Set<PurchaseOrderDetail> orderDetail = po.getRows();
        Iterator ite = orderDetail.iterator();
        while(ite.hasNext()){
            PurchaseOrderDetail pDetail = (PurchaseOrderDetail)ite.next();
           double qua = 0;
           double preturn=0;
           if(pDetail.getQastatus()!=Constants.QA_Rejected){                      
            if(pref.isWithInvUpdate()){ //In Trading Flow                  
                KwlReturnObject groresult = accGoodsReceiptobj.getReceiptOrderDFromPODOptimized(pDetail.getID(),pref.getCompany().getCompanyID());
                List list = groresult.getEntityList();
                if(list.size()>0){
                    Iterator ite1 = list.iterator();                
                    while(ite1.hasNext()){                        
                        String orderid = (String)ite1.next();
                        KwlReturnObject res=accountingHandlerDAOobj.getObject(GoodsReceiptOrderDetails.class.getName(),orderid);
                        GoodsReceiptOrderDetails goodsReceiptOrderDetails=(GoodsReceiptOrderDetails)res.getEntityList().get(0);     
                        qua += goodsReceiptOrderDetails.getDeliveredQuantity();
                        KwlReturnObject groresult1 = accGoodsReceiptobj.getPurchaseReturnFormGoodsReceipt(orderid);
                        List<PurchaseReturnDetail> prdeDetails = groresult1.getEntityList();
                        if (prdeDetails != null) {
                            /**
                             * Minus purchase return from delivered quantity to get proper status of PO. 
                             * Ticket SDP-9059.
                             */
                            for (PurchaseReturnDetail prd : prdeDetails) {
                                preturn += prd.getReturnQuantity();
                            }
                        }
                        qua = qua - preturn;
                    }
                }              
            }else{ //In Non Trading Flow 
                KwlReturnObject grresult = accGoodsReceiptobj.getReceiptDFromPOD(pDetail.getID());
                List list = grresult.getEntityList();
                Iterator ite1 = list.iterator();                
                while(ite1.hasNext()){
                    GoodsReceiptDetail ge = (GoodsReceiptDetail)ite1.next();
                    qua += ge.getInventory().getQuantity();
                }
            }            
            if(qua < pDetail.getQuantity()){
                result = "Open";
                break;
            }
           }
        }
        } catch (Exception ex){
            throw ServiceException.FAILURE("accPurchaseOrderControllerCWN.getPurchaseOrderStatus : "+ex.getMessage(), ex);
        }
        return result;
    }
@Override
    public String getPurchaseOrderStatusUsingPODetails(CompanyAccountPreferences preferences, String companyid, Set<PurchaseOrderDetail> orderDetail) throws ServiceException {
        String result = "Closed";
        try {
            //Check Integration and QA Approval flow then check status
            int status = getQAPOStatusUsingPODetails(orderDetail);
            if (status == Constants.Pending_QA_Approval) {
                result = "Pending QA Approval";
                return result;
            } else if (status == Constants.QA_Rejected) {
                result = "QA Failed";
                return result;
            }
            for (PurchaseOrderDetail pDetail : orderDetail) {
                double qua = 0;
                if (pDetail.getQastatus() != Constants.QA_Rejected) {
                    if (preferences.isWithInvUpdate()) { //In Trading Flow                  
                        KwlReturnObject groresult = accGoodsReceiptobj.getReceiptOrderDFromPODOptimized(pDetail.getID(), companyid);
                        List list = groresult.getEntityList();
              
                        if (list.size() > 0) {
                            Iterator ite1 = list.iterator();
                            while (ite1.hasNext()) {
                                String orderid = (String) ite1.next();
                                KwlReturnObject res = accountingHandlerDAOobj.getObject(GoodsReceiptOrderDetails.class.getName(), orderid);
                                GoodsReceiptOrderDetails goodsReceiptOrderDetails = (GoodsReceiptOrderDetails) res.getEntityList().get(0);
                                qua += goodsReceiptOrderDetails.getDeliveredQuantity();
                            }
                        }
                    } else { //In Non Trading Flow 
                        KwlReturnObject grresult = accGoodsReceiptobj.getReceiptDFromPOD(pDetail.getID());
                        List list = grresult.getEntityList();
                        Iterator ite1 = list.iterator();
                        while (ite1.hasNext()) {
                            GoodsReceiptDetail ge = (GoodsReceiptDetail) ite1.next();
                            qua += ge.getInventory().getQuantity();
                        }
                    }
                    if (qua < pDetail.getQuantity()) {
                        result = "Open";
                        break;
                    }
                }
            }
          
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPurchaseOrderControllerCWN.getPurchaseOrderStatus : " + ex.getMessage(), ex);
        }
        return result;
    }
@Override
public int getQAPOStatus(PurchaseOrder po) throws ServiceException {
        int status=Constants.QA_Rejected;
         try {
             Set<PurchaseOrderDetail> orderDetail = po.getRows();
             for (PurchaseOrderDetail purchaseOrderDetail : orderDetail) {
                 if(purchaseOrderDetail.getQastatus()!=Constants.QA_Rejected){
                     if(purchaseOrderDetail.getQastatus()==Constants.Pending_QA_Approval){
                         status=Constants.Pending_QA_Approval;
                         break;
                     }else{
                         status=Constants.APPROVED;
                     }                    
                 }
             }                          
         } catch (Exception ex) {
             throw ServiceException.FAILURE("accPurchaseOrderControllerCWN.getQAPOStatus : " + ex.getMessage(), ex);
         }
         
        return status;
     }
    @Override
    public int getQAPOStatusUsingPODetails(Set<PurchaseOrderDetail> orderDetail) throws ServiceException {
        int status = Constants.QA_Rejected;
        try {
            for (PurchaseOrderDetail purchaseOrderDetail : orderDetail) {
                if (purchaseOrderDetail.getQastatus() != Constants.QA_Rejected) {
                    if (purchaseOrderDetail.getQastatus() == Constants.Pending_QA_Approval) {
                        status = Constants.Pending_QA_Approval;
                        break;
                    } else {
                        status = Constants.APPROVED;
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accPurchaseOrderControllerCWN.getQAPOStatusUsingPODetails : " + ex.getMessage(), ex);
        }

        return status;
    }
@Override
    public JSONArray getTermDetails(String id, boolean isOrder) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParam = new HashMap();
            if (isOrder) {
                requestParam.put("purchaseOrder", id);
                KwlReturnObject curresult = accPurchaseOrderobj.getPurchaseOrderTermMap(requestParam);
                List<PurchaseOrderTermMap> termMap = curresult.getEntityList();
                for (PurchaseOrderTermMap purchaseOrderTermMap : termMap) {
                    InvoiceTermsSales mt = purchaseOrderTermMap.getTerm();
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("id", mt.getId());
                    jsonobj.put("term", mt.getTerm());
                    jsonobj.put("glaccount", mt.getAccount().getID());
                    jsonobj.put("sign", mt.getSign());
                    jsonobj.put("formula", mt.getFormula());
                    jsonobj.put("termpercentage", purchaseOrderTermMap.getPercentage());
                    jsonobj.put("termamount", purchaseOrderTermMap.getTermamount());
                    jsonobj.put("termamountinbase", purchaseOrderTermMap.getTermamountinbase());
                    jsonobj.put("termtaxamount", purchaseOrderTermMap.getTermtaxamount());
                    jsonobj.put("termtaxamountinbase", purchaseOrderTermMap.getTermtaxamountinbase());
                    jsonobj.put("termAmountExcludingTax", purchaseOrderTermMap.getTermAmountExcludingTax());
                    jsonobj.put("termAmountExcludingTaxInBase", purchaseOrderTermMap.getTermAmountExcludingTaxInBase());
                    jsonobj.put("termtax", purchaseOrderTermMap.getTermtax()!=null ? purchaseOrderTermMap.getTermtax().getID():"");
                    jsonobj.put("linkedtaxname", purchaseOrderTermMap.getTermtax()!=null ? purchaseOrderTermMap.getTermtax().getName():"");
                    jsonobj.put("isActivated", purchaseOrderTermMap.getTermtax()!=null ? purchaseOrderTermMap.getTermtax().isActivated():false);
                    if(purchaseOrderTermMap.getTermtax()!=null){
                        jsonobj.put("linkedtaxpercentage", accInvoiceDAOobj.getPercentageFromTaxid(purchaseOrderTermMap.getTermtax().getID(), mt.getCompany().getCompanyID()));
                    }else{
                        jsonobj.put("linkedtaxpercentage", 0);
                    }
                    jArr.put(jsonobj);
                }
            } else {
                requestParam.put("vendorQuotation", id);
                KwlReturnObject curresult = accPurchaseOrderobj.getVendorQuotationTermMap(requestParam);
                List<VendorQuotationTermMap> termMap = curresult.getEntityList();
                for (VendorQuotationTermMap vendorQuotationTermMap : termMap) {
                    InvoiceTermsSales mt = vendorQuotationTermMap.getTerm();
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("id", mt.getId());
                    jsonobj.put("term", mt.getTerm());
                    jsonobj.put("glaccount", mt.getAccount().getID());
                    jsonobj.put("sign", mt.getSign());
                    jsonobj.put("formula", mt.getFormula());
                    jsonobj.put("termpercentage", vendorQuotationTermMap.getPercentage());
                    jsonobj.put("termamount", vendorQuotationTermMap.getTermamount());
                    jsonobj.put("termamountinbase", vendorQuotationTermMap.getTermamountinbase());
                    jsonobj.put("termtaxamount", vendorQuotationTermMap.getTermtaxamount());
                    jsonobj.put("termtaxamountinbase", vendorQuotationTermMap.getTermtaxamountinbase());
                    jsonobj.put("termAmountExcludingTax", vendorQuotationTermMap.getTermAmountExcludingTax());
                    jsonobj.put("termAmountExcludingTaxInBase", vendorQuotationTermMap.getTermAmountExcludingTaxInBase());
                    jsonobj.put("termtax", vendorQuotationTermMap.getTermtax()!=null ? vendorQuotationTermMap.getTermtax().getID():"");
                    jsonobj.put("linkedtaxname", vendorQuotationTermMap.getTermtax()!=null ? vendorQuotationTermMap.getTermtax().getName():"");
                    if(vendorQuotationTermMap.getTermtax()!=null){
                        jsonobj.put("linkedtaxpercentage", accInvoiceDAOobj.getPercentageFromTaxid(vendorQuotationTermMap.getTermtax().getID(), mt.getCompany().getCompanyID()));
                    }else{
                        jsonobj.put("linkedtaxpercentage", 0);
                    }
                    jArr.put(jsonobj);
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    @Override
    public JSONArray getPOVersiovTermDetails(String id, boolean isOrder) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("purchaseOrderVersion", id);
            KwlReturnObject curresult = accPurchaseOrderobj.getPurchaseOrderVersionTermMap(requestParam);
            List<PurchaseOrderVersionTermMap> termMap = curresult.getEntityList();
            for (PurchaseOrderVersionTermMap purchaseOrderTermMap : termMap) {
                InvoiceTermsSales mt = purchaseOrderTermMap.getTerm();
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("id", mt.getId());
                jsonobj.put("term", mt.getTerm());
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("sign", mt.getSign());
                jsonobj.put("formula", mt.getFormula());
                jsonobj.put("termpercentage", purchaseOrderTermMap.getPercentage());
                jsonobj.put("termamount", purchaseOrderTermMap.getTermamount());
                jArr.put(jsonobj);
            }
        } catch (JSONException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    
    
    public String getPurchaseRequisitionStatus(PurchaseRequisition pReq) throws ServiceException {
        Set<PurchaseRequisitionDetail> pReqDetails = pReq.getRows();
        Iterator itr = pReqDetails.iterator();

        String result = "Closed";
        while (itr.hasNext()) {
            PurchaseRequisitionDetail pReqDetail = (PurchaseRequisitionDetail) itr.next();
            KwlReturnObject idresult = accPurchaseOrderobj.getIDFromPurchaseRequisitionDetails(pReqDetail.getID());
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            double qua = 0;
            while (ite1.hasNext()) {
                VendorQuotationDetail vqDetail = (VendorQuotationDetail) ite1.next();
                qua += vqDetail.getQuantity();
            }
            if (qua < pReqDetail.getQuantity()) {
                result = "Open";
                break;
            }
        }
        return result;
    }
  
    @Override
    public JSONArray getPurchaseOrderRows(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray jArr = new JSONArray();
        DateFormat userdf=null;
        try {
            boolean isConsignment = false;
            if(requestParams.containsKey(Constants.userdf) && requestParams.get(Constants.userdf) != null){
                userdf = (DateFormat) requestParams.get(Constants.userdf);
            }
            if (requestParams.get("isConsignment") != null) {
                isConsignment = Boolean.FALSE.parseBoolean(requestParams.get("isConsignment").toString());
            }
            boolean isFixedAsset = false;
            if (requestParams.get("isFixedAsset") != null) {
                isFixedAsset = Boolean.FALSE.parseBoolean(requestParams.get("isFixedAsset").toString());
            }
            boolean isJobWorkStockOut = false;
            if (requestParams.containsKey("isJobWorkStockOut")) {
                isJobWorkStockOut = Boolean.FALSE.parseBoolean(requestParams.get("isJobWorkStockOut").toString());
            }
            String storeId="";
            if(requestParams.containsKey("storeId")){
                storeId=(String)requestParams.get("storeId");
            }
            boolean FA_POlinkToFA_PI = false;
            boolean isJobWorkOutRemain = false;
            if (requestParams.get("FA_POlinkToFA_PI") != null) {
                FA_POlinkToFA_PI = Boolean.FALSE.parseBoolean(requestParams.get("FA_POlinkToFA_PI").toString());
            }
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get("companyid");
            DateFormat df = (DateFormat) requestParams.get("dateFormatValue");
           
            Map<String, Object> ProductFieldsRequestParams = new HashMap();
            ProductFieldsRequestParams.put("companyid", companyid);
            ProductFieldsRequestParams.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
            List masterFieldsResultList = CommonFunctions.getproductmastersFieldsToShowLineLevel(ProductFieldsRequestParams, accountingHandlerDAOobj);
           
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            boolean doflag = requestParams.containsKey("doflag") ? (Boolean) requestParams.get("doflag") : false;
            String[] pos = (String[]) requestParams.get("bills");
            /*
             *ProdIds contains ids which are selected items from Aged order work report.
             */
             
            String[] jobWorkOutId = (String[]) requestParams.get("prodIds");
            if (requestParams.containsKey("isJobWorkOutRemain") && requestParams.get("isJobWorkOutRemain") != null) {
                isJobWorkOutRemain = Boolean.parseBoolean( requestParams.get("isJobWorkOutRemain").toString());
            }
            double addobj = 1;
            String closeflag = (String) requestParams.get("closeflag");

            boolean isForDOGROLinking = false;
            boolean isForInvoice = false;
            if (requestParams.containsKey("isForInvoice") && requestParams.get("isForInvoice") != null) {
                isForInvoice = (Boolean) requestParams.get("isForInvoice");
            }
             /*
                isForSGELink flag is used in case of linking PO in SGE
             */
            boolean isForSGELinking = false;
            if (requestParams.containsKey("isForSGELinking") && requestParams.get("isForSGELinking") != null) {
                isForSGELinking = (Boolean) requestParams.get("isForSGELinking");
            } 
            /*
             * isForJobWorkOut , isJobWorkOutLinkedWithGRN , isJobWorkOutLinkedWithPI these flags are used when call is for Job work out order.
            */
            boolean isForJobWorkOut = false;
            if (requestParams.containsKey("isForJobWorkOut") && requestParams.get("isForJobWorkOut") != null) { //isForJobWorkOut
                isForJobWorkOut = (Boolean) requestParams.get("isForJobWorkOut");
            }  
            boolean isJobWorkOutLinkedWithGRN = false;
            if (requestParams.containsKey("isJobWorkOutLinkedWithGRN") && requestParams.get("isJobWorkOutLinkedWithGRN") != null) { //isJobWorkOutLinkedWithGRN
                isJobWorkOutLinkedWithGRN = (Boolean) requestParams.get("isJobWorkOutLinkedWithGRN");
            }   
            boolean isJobWorkOutLinkedWithPI = false;
            if (requestParams.containsKey("isJobWorkOutLinkedWithPI") && requestParams.get("isJobWorkOutLinkedWithPI") != null) { //isJobWorkOutLinkedWithPI
                isJobWorkOutLinkedWithPI = (Boolean) requestParams.get("isJobWorkOutLinkedWithPI");
            }            
            if (requestParams.containsKey("isForDOGROLinking") && requestParams.get("isForDOGROLinking") != null) {
                isForDOGROLinking = (Boolean) requestParams.get("isForDOGROLinking");
            }
            int moduleid = -1;
            if (requestParams.containsKey("requestModuleid")) {
                moduleid = Integer.parseInt(requestParams.get("requestModuleid").toString());
            }
            if (requestParams.containsKey("prodIds")) {
                moduleid = Integer.parseInt(requestParams.get("requestModuleid").toString());
            }
            
            boolean isForLinking = false;
            
            if (requestParams.containsKey("isForLinking") && requestParams.get("isForLinking") != null) {// True in case of linking with VI
                isForLinking = (Boolean) requestParams.get("isForLinking");
            }
            
            int moduId=0;
            if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null) {// True in case of linking with VI
                moduId = Integer.parseInt(requestParams.get("moduleid").toString());
            }
            
            boolean linkingFlag = false;
            if (requestParams.containsKey("linkingFlag") && requestParams.get("linkingFlag") != null) { // True in case of Asset linking with VQ
                linkingFlag = (Boolean) requestParams.get("linkingFlag");
            }
            boolean isExport = false;
            if (requestParams.containsKey("isExport") && requestParams.get("isExport") != null) { // True in case of Export
                isExport = (Boolean) requestParams.get("isExport");
            }
            boolean sopolinkflag = false;
            if (requestParams.containsKey(Constants.POSOFLAG) && requestParams.get(Constants.POSOFLAG) != null) { // True in case of crosslink of PO and SO 
                sopolinkflag = (Boolean) requestParams.get(Constants.POSOFLAG);
            }
            boolean isCopy = (boolean) (requestParams.containsKey("iscopy") ? requestParams.get("iscopy") : false);
            
            boolean isForReport = false;
            boolean isBatchForProduct = false;
            boolean isSerialForProduct = false;
            boolean isLocationForProduct = false;
            boolean isWarehouseForProduct = false;
            boolean isRowForProduct = false;
            boolean isRackForProduct = false;
            boolean isBinForProduct = false;
            /*
             * if call is for "Job Work Out Order" then this check will be true
            */
            isForJobWorkOut = (isForJobWorkOut || isJobWorkOutLinkedWithGRN || isJobWorkOutLinkedWithPI);            
            String description="";
            double minqty = 0.0;
            double maxqty = 0.0;
            String productsDefaultLocation="",productsDefaultWarehouse="";
            if (requestParams.containsKey("isForReport")) {
                isForReport = (Boolean) requestParams.get("isForReport");
            }

            /**
             * *****<-- Need To Discuss with respect to save quotation rows  --->
             */
            // Get line-level CUSTOMFIELDS for Purchase Order
            HashMap<String, Object> fieldrequestParams1 = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            int poCustomModuleid = isForJobWorkOut?Constants.JOB_WORK_OUT_ORDER_MODULEID:isConsignment?Constants.Acc_ConsignmentVendorRequest_ModuleId:(!isFixedAsset?Constants.Acc_Purchase_Order_ModuleId:Constants.Acc_FixedAssets_Purchase_Order_ModuleId);
            
            fieldrequestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams1.put(Constants.filter_values, Arrays.asList(companyid, poCustomModuleid, 1));   // For line-level PO on dropdown
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMapRow = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams1, replaceFieldMap, customFieldMap, customDateFieldMap);
            HashMap<String, Object> poRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("purchaseOrder.ID");
            order_by.add("srno");
            order_type.add("asc");
            poRequestParams.put("filter_names", filter_names);
            poRequestParams.put("filter_params", filter_params);
            poRequestParams.put("order_by", order_by);
            poRequestParams.put("order_type", order_type);
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            capresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) capresult.getEntityList().get(0);
            Country country =  extraCompanyPreferences.getCompany().getCountry();
            String stdate = authHandler.getDates(preferences.getFinancialYearFrom(), true);
            String enddate = authHandler.getDates(preferences.getFinancialYearFrom(), false);
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
            /*
             * isJobWorkOutRemain is true if sales invoice is creating from Aged order work report.
             */
            if (isJobWorkOutRemain) {
                KwlReturnObject idresult = null;
                if (pos != null) {
                    /*
                     * getJobWorkOutPurchaseOrderSinglePO---- Its used when seleting items same type of job work order from Aged order work report.
                     */
                    idresult = accPurchaseOrderobj.getJobWorkOutPurchaseOrderSinglePO(companyid, pos[0]);
                } else {
                    /*
                     * getJobWorkOutPurchaseOrder---- when  all records are fetching in Aged order work report.
                     */
                    idresult = accPurchaseOrderobj.getJobWorkOutPurchaseOrder(companyid);
                }
                List list = idresult.getEntityList();
                Iterator ite1 = list.iterator();
                int i = 0;
                double qua = 0;
                filter_names.clear();
                filter_names.add("id");
                PurchaseOrder po = (PurchaseOrder) ite1.next();
                while (i < jobWorkOutId.length) {
                    filter_params.clear();
                    poRequestParams.clear();
                    filter_params.add(jobWorkOutId[i]);
                    poRequestParams.put("filter_names", filter_names);
                    poRequestParams.put("filter_params", filter_params);

                    KwlReturnObject podresult = accPurchaseOrderobj.getSelectedPurchaseOrderDetails(poRequestParams);
                    List<InterStoreTransferRequest> interStoreTransferRequest = podresult.getEntityList();
                    for (InterStoreTransferRequest row : interStoreTransferRequest) {
                        if (isJobWorkStockOut) {
                            /**
                             * if request from Job Work stock Transfer
                             */
                            Map<String, Object> reqMap = new HashMap();
                            reqMap.put("masterFieldsResultList", masterFieldsResultList);
                            reqMap.put("df", df);
                            reqMap.put("masterFieldsResultList", masterFieldsResultList);
                            reqMap.put("preferences", preferences);
                            reqMap.put("extraCompanyPreferences", extraCompanyPreferences);
                            reqMap.put("storeId", storeId);
                            JSONObject params = new JSONObject();
                            params.put("gcurrencyid", currencyid);
                            params.put("stdate", stdate);
                            params.put("enddate", enddate);
                            reqMap.put("selectedJobStockOutid", jobWorkOutId[i]);
                            reqMap.put("orderedQty", row.getOrderedQty());

                            reqMap.put("purchaseOrderDetailObject", row.getPurchaseOrderDetail());
                            reqMap.put("rowProductObject", row.getProduct());
                            reqMap.put("rowProductId", row.getProduct().getID());
                            reqMap.put("purchaseOrderObject", po);
                            params.put("isJobWorkStockOut", isJobWorkStockOut);
                            reqMap.put("isJobWorkOutRemain", isJobWorkOutRemain);
                            /*
                             * To Get asssembly details of selected job work out from Aged order work report.
                             */
                            getAssemblySubProductRowsDetails(reqMap, params, jArr, po.getPurchaseOrderNumber(), po.getVendor().getName(), po.getDueDate(), row.getProduct().getName());
                                    }
                                }
                    i++;
                }
                
            }else{
            for (int i = 0; pos != null && i < pos.length; i++) {
                KwlReturnObject poresult = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), pos[i]);
                PurchaseOrder po = (PurchaseOrder) poresult.getEntityList().get(0);
                KWLCurrency currency = null;
                if (po.getCurrency() != null) {
                    currency = po.getCurrency();
                } else {
                    currency = po.getVendor().getAccount().getCurrency() == null ? kwlcurrency : po.getVendor().getAccount().getCurrency();
                }
                filter_params.clear();
                filter_params.add(po.getID());
                if (po.isIsExpenseType()) {// when PO is expense type then we need to fetch data from ExpensePODetail
                    requestParams.put(Constants.userdf,userdf);
                    KwlReturnObject podresult = accPurchaseOrderobj.getExpensePurchaseOrderDetails(poRequestParams);
                    List<ExpensePODetail> expensepodetails = podresult.getEntityList();
                    jArr=getExpenseDetailRows(jArr,requestParams,po,expensepodetails,FieldMapRow,customFieldMap,customDateFieldMap);
                } else {//For other case data will be fetched from PurchaseOrderDetail
                    KwlReturnObject podresult = accPurchaseOrderobj.getPurchaseOrderDetails(poRequestParams);
                    List<PurchaseOrderDetail> podetails = podresult.getEntityList();
                    for (PurchaseOrderDetail row : podetails) {
                        JSONObject obj = new JSONObject();
                        if (isJobWorkStockOut) {
                            /**
                             * if request from Job Work stock Transfer
                             */
                            Map<String, Object> reqMap = new HashMap();
                            reqMap.put("masterFieldsResultList", masterFieldsResultList);
                            reqMap.put("df", df);
                            reqMap.put("masterFieldsResultList", masterFieldsResultList);
                            reqMap.put("preferences", preferences);
                            reqMap.put("extraCompanyPreferences", extraCompanyPreferences);
                            reqMap.put("storeId", storeId);
                            JSONObject params = new JSONObject();
                            params.put("gcurrencyid", currencyid);
                            params.put("stdate", stdate);
                            params.put("enddate", enddate);

                            reqMap.put("purchaseOrderDetailObject", row);
                            reqMap.put("rowProductObject", row.getProduct());
                            reqMap.put("purchaseOrderObject", row.getPurchaseOrder());
                            params.put("isJobWorkStockOut", isJobWorkStockOut);
                            getAssemblySubProductRows(reqMap, params,jArr);
                        }
                        else{
                        CommonFunctions.getterMethodForProductsData(row.getProduct(), masterFieldsResultList, obj);
                        obj.put("billid", po.getID());
                        obj.put("billno", po.getPurchaseOrderNumber());
                        obj.put("bomid", row.getBomcode() != null ? row.getBomcode().getID() : "");
                        obj.put("bomcode", row.getBomcode() != null ? row.getBomcode().getBomCode() : "");
                        obj.put("currencysymbol", currency.getSymbol());
                        obj.put("joborderdetail", row.getID());
                        obj.put("isJobWorkOutProd", row.getPurchaseOrder().isIsJobWorkOutOrder());
                        obj.put("currencyCode", currency.getCurrencyCode() == null ? "" : currency.getCurrencyCode());
                        obj.put("srno", row.getSrno());
                        obj.put("rowid", row.getID());
                        obj.put("productid", row.getProduct().getID());
                        obj.put("purchasetaxId", row.getProduct().getPurchasetaxid());
                        obj.put("salestaxId", row.getProduct().getSalestaxid());
                        obj.put("isAsset", row.getProduct().isAsset());
                        obj.put("hasAccess", row.getProduct().isIsActive());
                        obj.put("productname", row.getProduct().getName());
                        obj.put("leadtime", row.getProduct().getLeadTimeInDays());
                        obj.put("type", row.getProduct().getProducttype() == null ? "" : row.getProduct().getProducttype().getName());
                        obj.put("typeid", row.getProduct().getProducttype() == null ? "" : row.getProduct().getProducttype().getID());
                        obj.put("pid", row.getProduct().getProductid());
                        obj.put("supplierpartnumber", StringUtil.isNullOrEmpty(row.getSupplierpartnumber()) ? "" : row.getSupplierpartnumber());
                        obj.put("status", row.isIsLineItemClosed() ? "Yes" : "N/A");
                        if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                            description = row.getDescription();
                        } else if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                            description = row.getProduct().getDescription();
                        } else {
                            description = "";
                        }
                        obj.put("desc",StringUtil.DecodeText(description));
                        obj.put("description", StringUtil.DecodeText(description));
                        obj.put("unitname", row.getUom() != null ? row.getUom().getNameEmptyforNA() : row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                        obj.put("uomname", row.getUom() != null ? row.getUom().getNameEmptyforNA() : row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                        obj.put("uomschematypeid", (row.getProduct() == null ? "" : (row.getProduct().getUomSchemaType() == null ? "" :  row.getProduct().getUomSchemaType().getID())));
                        obj.put("baseuomid", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getID());
                        obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                        obj.put("multiuom", row.getProduct().isMultiuom());
                        obj.put("memo", row.getRemark());
                        obj.put("invstore", (StringUtil.isNullOrEmpty(row.getInvstoreid())) ? "" : row.getInvstoreid());
                        obj.put("invlocation", (StringUtil.isNullOrEmpty(row.getInvlocid())) ? "" : row.getInvlocid());
                        obj.put("shelfLocation", row.getShelfLocation());
                        obj.put("isConsignment", po.isIsconsignment()); //for getting is consignment request
                        obj.put("permit", row.getPermit() == null ? "" : row.getPermit());
                        obj.put("includeprotax", row.getTax()!=null?true:false);
                        String productsBaseUomId = (row.getProduct().getUnitOfMeasure() == null) ? "" : row.getProduct().getUnitOfMeasure().getID();
                        String selectedUomId = (row.getUom() != null) ? row.getUom().getID() : "";
                        if (row.getProduct().isblockLooseSell() && !productsBaseUomId.equals(selectedUomId)) {
                            // Get Available Quantity of Product For Selected UOM
                            KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(row.getProduct().getID(), selectedUomId);
                            double availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                            obj.put("availableQtyInSelectedUOM", availableQuantity);
                            obj.put("isAnotherUOMSelected", true);
                            // Getting Open PO/SO count
                            HashMap<String, Object> orderParams = new HashMap<String, Object>();
                            orderParams.put("companyid", companyid);
                            orderParams.put("gcurrencyid", currencyid);
                            orderParams.put("df", df);
                            orderParams.put("pendingapproval", false);
                            orderParams.put("startdate", stdate);
                            orderParams.put("enddate", enddate);
                            orderParams.put("currentuomid", selectedUomId);
                            orderParams.put("productId", row.getProduct().getID());

                            double pocountinselecteduom = accSalesOrderServiceDAOobj.getPOCount(orderParams);

                            double socountinselecteduom = accSalesOrderServiceDAOobj.getSOCount(orderParams);

                            obj.put("pocountinselecteduom", pocountinselecteduom);
                            obj.put("socountinselecteduom", socountinselecteduom);
                        } else {
                                /**
                                 * If isblockLooseSell check from company preference is false then get the actual quantity of product.
                                 */
                                KwlReturnObject result = accProductObj.getQuantity(row.getProduct().getID());
                                obj.put("availableQtyInSelectedUOM", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));
                        }
                        
                        obj.put("availablequantity", row.getProduct()!= null ? row.getProduct().getAvailableQuantity() : 0 );
                        
                        if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {  //This is sats specific code 	 
                            obj.put("invoicetype", po.getInvoicetype());

                            obj.put("dependentType", row.getDependentType() == null ? "" : row.getDependentType());
                            
                                obj.put("showquantity",StringUtil.DecodeText(row.getShowquantity() == null ? "" : row.getShowquantity()));
                            
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

                            obj.put("parentid", ((row.getProduct().getParent() != null) ? row.getProduct().getParent().getID() : ""));
                            obj.put("parentname", ((row.getProduct().getParent() != null) ? row.getProduct().getParent().getName() : ""));
                            if (row.getProduct().getParent() != null) {
                                obj.put("issubproduct", true);
                            }
                            if (row.getProduct().getChildren().size() > 0) {
                                obj.put("isparentproduct", true);
                            } else {
                                obj.put("isparentproduct", false);
                            }
                        }

                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        PurchaseOrderDetailsCustomData jeDetailCustom = (PurchaseOrderDetailsCustomData) row.getPoDetailCustomData();
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMapRow, replaceFieldMap, variableMap);
                        if (jeDetailCustom != null) {
                            JSONObject params = new JSONObject();
                            params.put("isExport", isExport);
                            params.put("isForReport", isForReport);
                            params.put(Constants.userdf,userdf);
                            boolean generateInvoiceFromTransactionForms = false;
                            boolean isSOfromPO = false;
                              /*--- generateInvoiceFromTransactionForms ->Flag is true if invoice is generated from PO form------*/
                            if (requestParams.containsKey("generateInvoiceFromTransactionForms") && requestParams.get("generateInvoiceFromTransactionForms") != null) {
                                generateInvoiceFromTransactionForms = (Boolean) requestParams.get("generateInvoiceFromTransactionForms");
                            }
                            /*
                             'isSOfromPO' flag is used to get custom field or dimension while generating SO from PO
                            */
                            if (requestParams.containsKey("isSOfromPO") && requestParams.get("isSOfromPO") != null) {
                                isSOfromPO = (Boolean) requestParams.get("isSOfromPO");
                            }
             
                            if ((isForDOGROLinking || isForLinking || FA_POlinkToFA_PI) && !isForReport || generateInvoiceFromTransactionForms || isForSGELinking || isSOfromPO || isJobWorkOutLinkedWithGRN || isJobWorkOutLinkedWithPI) {
                                params.put("isLink", true);                                
                                int moduleId = isForDOGROLinking ? Constants.Acc_Goods_Receipt_ModuleId : isConsignment ? Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId : (isForInvoice || generateInvoiceFromTransactionForms) ? Constants.Acc_Vendor_Invoice_ModuleId : Constants.Acc_Sales_Order_ModuleId;
                                params.put("companyid", companyid);
                                if (FA_POlinkToFA_PI) {
                                    moduleId = Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId;
                                }
                                if (isConsignment) {
                                    moduleId = Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId;
                                }
                                if(isForSGELinking){
                                    moduleId=Constants.Acc_SecurityGateEntry_ModuleId;
                                }
                                if(isSOfromPO){
                                   moduleId=Constants.Acc_Sales_Order_ModuleId; 
                                }
                                if(isJobWorkOutLinkedWithGRN){
                                    moduId=Constants.Acc_Goods_Receipt_ModuleId; // linking Job Work Order in GRN
                                }
                                if(isJobWorkOutLinkedWithPI){
                                    moduId=Constants.Acc_Vendor_Invoice_ModuleId; // linking Job Work Order in PI
                                }                                
                                params.put("linkModuleId", moduleId);
                            }
                            fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                        }

                        variableMap = new HashMap<String, Object>();
                        replaceFieldMap = new HashMap<String, String>();
                        HashMap<String, Object> podrequestParams = new HashMap<String, Object>();
                        podrequestParams.put("podetailId", row.getID());
                        podrequestParams.put("companyId", (String) requestParams.get("companyid"));
                        KwlReturnObject idcustresultForProduct = accPurchaseOrderobj.getPODetailsCustomDataForProduct(podrequestParams);
                        PurchaseOrderDetailProductCustomData poDetailProductCustomData = null;
                        if (idcustresultForProduct.getEntityList().size() > 0) {
                            poDetailProductCustomData = (PurchaseOrderDetailProductCustomData) idcustresultForProduct.getEntityList().get(0);
                        }
                        if (poDetailProductCustomData != null) {
                            JSONObject params = new JSONObject();
                            params.put("isExport", isExport);
                            params.put("isForReport", isForReport);
                            params.put(Constants.userdf,userdf);
                            setCustomColumnValuesForProduct(poDetailProductCustomData, FieldMap, replaceFieldMap, variableMap, params);
                            for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                String coldata = varEntry.getValue().toString();
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    obj.put(varEntry.getKey(), coldata);
                                }
                            }
                        }
                            double unitprice = row.getRate();
                            if (row.getPurchaseOrder().isIsJobWorkOutOrder() && row.getProduct().getProducttype().getID().equalsIgnoreCase(Producttype.ASSEMBLY)&&linkingFlag) {
                                /**
                                 * If GR is Job Work Out then return purchase
                                 * price = Sum of Purchase Price of All
                                 * components (Which are define while Assembly )
                                 */
                                unitprice = accProductObj.getSumOfPurchasePriceForSubassebmblyItems(row.getProduct().getID());
                            }
                            obj.put("rate", unitprice);
                        obj.put("priceSource", row.getPriceSource() != null ? row.getPriceSource() : "");
                        obj.put("discountispercent", row.getDiscountispercent());
                            if (row.getPricingBandMasterid() != null) {
                                KwlReturnObject PricebandResult = accountingHandlerDAOobj.getObject(PricingBandMaster.class.getName(), row.getPricingBandMasterid());
                                PricingBandMaster pricingBandMaster = PricebandResult != null ? (PricingBandMaster) PricebandResult.getEntityList().get(0) : null;
                                obj.put("pricingbandmasterid", pricingBandMaster != null ? pricingBandMaster.getID() : "");
                                obj.put("pricingbandmastername", pricingBandMaster != null ? pricingBandMaster.getName() : "");
                            }
                       
                        /**
                        * get the volume discount discount for the given
                        * product according its quantity.
                        */
                        HashMap<String, Object> pricingDiscountRequestParams = new HashMap<String, Object>();
                        pricingDiscountRequestParams.put("isPricePolicyUseDiscount", true);
                        pricingDiscountRequestParams.put("productID", row.getProduct()!=null?row.getProduct().getID():"");
                        pricingDiscountRequestParams.put("isPurchase", true);
                        pricingDiscountRequestParams.put("companyID", companyid);
                        pricingDiscountRequestParams.put("currencyID", currencyid);
                        Double qty = Double.valueOf(row.getQuantity());
                        pricingDiscountRequestParams.put("quantity", Integer.valueOf(qty.intValue()));
                        /**
                         * check Volume discount matches with qty
                         */
                        KwlReturnObject volDiscresult = accProductObj.getProductPriceFromPriceListVolumeDiscount(pricingDiscountRequestParams);
                        if (volDiscresult!=null && volDiscresult.getEntityList() != null && !volDiscresult.getEntityList().isEmpty()) {
                            Object[] rowObj = (Object[]) volDiscresult.getEntityList().get(0);
                            KwlReturnObject detailResult = accountingHandlerDAOobj.getObject(PricingBandMasterDetail.class.getName(), (String) rowObj[5]);
                            PricingBandMasterDetail pricingBandMasterDetail = detailResult != null ? (PricingBandMasterDetail) detailResult.getEntityList().get(0) : null;
                            if (pricingBandMasterDetail != null) {
                                obj.put("volumdiscountid", pricingBandMasterDetail.getPricingBandMaster().getID());
                            }
                        }
                        /* In exported file showing discount value with proper % or currency symbol 
                         same as expander
                        
                         */
                        
                       if (isExport) {
                            obj.put("prdiscount", row.getDiscountispercent() == 1 ? (row.getDiscount() != 0 ? (row.getDiscount() + "%") : row.getDiscount()) : currency.getSymbol()+" " + row.getDiscount());
                        } else {
                            obj.put("prdiscount", row.getDiscount());
                        }
                      
                        if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
                            Product product = row.getProduct();// (Product) prodresult.getEntityList().get(0);
                            isLocationForProduct = product.isIslocationforproduct();
                            isWarehouseForProduct = product.isIswarehouseforproduct();
                            isBatchForProduct = product.isIsBatchForProduct();
                            isSerialForProduct = product.isIsSerialForProduct();
                            isRowForProduct = product.isIsrowforproduct();
                            isRackForProduct = product.isIsrackforproduct();
                            isBinForProduct = product.isIsbinforproduct();
                            productsDefaultLocation = (product.getLocation() != null && product.getLocation().getId() != null) ? product.getLocation().getId() : "";
                            maxqty = product.getMaxOrderingQuantity();
                            minqty = product.getMinOrderingQuantity();
                            productsDefaultWarehouse = (product.getWarehouse() != null && product.getWarehouse().getId() != null) ? product.getWarehouse().getId() : "";
                            if (extraCompanyPreferences != null && extraCompanyPreferences.getUomSchemaType() == Constants.PackagingUOM) {
                                obj.put("caseuom", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUoM().getID() : "");
                                obj.put("caseuomvalue", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUomValue() : 1);
                                obj.put("inneruom", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUoM().getID() : "");
                                obj.put("inneruomvalue", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUomValue() : 1);
                                obj.put("stockuom", (product.getUnitOfMeasure() != null) ? product.getUnitOfMeasure().getID() : "");
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
                        obj.put("maxorderingquantity", maxqty); 
                        obj.put("minorderingquantity", minqty);
                         
                        if ((preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory())) {  //check if company level option is on then only we will check productt level
                            if (isForDOGROLinking && ((isLocationForProduct && !StringUtil.isNullOrEmpty(productsDefaultLocation)) || (isWarehouseForProduct && !StringUtil.isNullOrEmpty(productsDefaultWarehouse))) && !isBatchForProduct && !isSerialForProduct) {
                                obj.put("batchdetails", getdefaultBatchJson(row.getProduct(), row.getID(), row.getQuantity()));
                            }
                        }
                        double rowTaxPercent = 0;
                        double rowTaxAmount = 0;
                        boolean isRowTaxApplicable = false;
                        if (row.getTax() != null) {
                            KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), po.getOrderDate(), row.getTax().getID());
                            rowTaxPercent = (Double) perresult.getEntityList().get(0);
                            isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                            if (isRowTaxApplicable) {
                                rowTaxAmount = row.getRowTaxAmount();
                            }
                        }
                        obj.put("prtaxpercent", rowTaxPercent);
                        obj.put("rowTaxAmount", extraCompanyPreferences.getLineLevelTermFlag() == 1 ? row.getRowTermAmount() : rowTaxAmount);
                        obj.put("taxamount", rowTaxAmount);
                        obj.put("recTermAmount", row.getRowTermAmount());
                        obj.put("OtherTermNonTaxableAmount", row.getOtherTermNonTaxableAmount());
                        obj.put("rowTaxPercent", rowTaxPercent);
                        obj.put("prtaxid", row.getTax() == null ? "None" : row.getTax().getID());
                        obj.put(Constants.isUserModifiedTaxAmount, row.isIsUserModifiedTaxAmount());
//                        obj.put("prtaxid", row.getTax() != null ? (isCopy || linkingFlag || isForDOGROLinking || isForLinking ? (row.getTax().isActivated() ? row.getTax().getID() : "") : row.getTax().getID()) : "None");//ERP-38656

                        /*These keys are used while exporting details */
                        double rowamountwithgst = 0;
                        double discountValueForExcel = 0, amountForExcelFile = 0;
                        if (row.getPurchaseOrder().isGstIncluded()) {//if gstincluded is the case
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
                        
                        obj.put(Constants.unitpriceForExcelFile, row.getRate());//obj.put(Constants.unitpriceForExcelFile, (Double) bAmt.getEntityList().get(0));
                        obj.put("marginExchangeRate", row.getPurchaseOrder() != null ? row.getPurchaseOrder().getExternalCurrencyRate() : 0);
                        double baseuomrate = row.getBaseuomrate();
                        if (row.getUom() != null) {
                            obj.put("uomid", row.getUom().getID());
                        } else {
                            obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
                        }
                        double quantity = 0;
                        double invoiceRowProductQty = row.getQuantity() * baseuomrate;
                        double remainedQty = invoiceRowProductQty;// which has not been linked yet
                        if (closeflag != null && moduleid != Constants.Acc_Sales_Order_ModuleId) {
                            addobj = (doflag || isForSGELinking )? row.getBalanceqty() : getPurchaseOrderDetailStatus(row);//When PO is linking with SGE Balance qty will be displayed.ERM-1099
                            quantity = addobj;
                            obj.put("quantity", addobj);
                            obj.put("copyquantity", addobj);
                            obj.put("dquantity", addobj);
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

                    // Added vendor related information for Profit Margin Calculation
                    JSONObject jObj = new JSONObject();
                    if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                        jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                    }
                    if (jObj.has("isDisplayUOM") && jObj.get("isDisplayUOM") != null && (Boolean) jObj.get("isDisplayUOM") != false) {
                        obj = accProductObj.getProductDisplayUOM(row.getProduct(), quantity, baseuomrate, true, obj);
                    }
                    if(extraCompanyPreferences.isActivateProfitMargin() && isForLinking){
                        obj.put("vendorid", (row.getPurchaseOrder()!=null && row.getPurchaseOrder().getVendor () !=null) ? row.getPurchaseOrder().getVendor().getID():"");
                        obj.put("vendorcurrexchangerate", row.getPurchaseOrder()!=null && row.getPurchaseOrder().getExternalCurrencyRate()!=0 ? (1/row.getPurchaseOrder().getExternalCurrencyRate()) : 1);
                        obj.put("vendorcurrencyid", (row.getPurchaseOrder()!=null && row.getPurchaseOrder().getCurrency() !=null) ? row.getPurchaseOrder().getCurrency().getCurrencyID() : "");
                        obj.put("vendorcurrencysymbol", (row.getPurchaseOrder()!=null && row.getPurchaseOrder().getCurrency() !=null) ? row.getPurchaseOrder().getCurrency().getSymbol() : "");
                        obj.put("vendorunitcost", row.getRate());
                    }
                    
                        if (isForLinking || isForDOGROLinking) {// in case of linking in normal transactions not lease consignment etc.
                            if (row.getTax() != null && invoiceRowProductQty > 0) {
                                double taxAmt = (rowTaxAmount / invoiceRowProductQty) * remainedQty;
                                obj.put("rowTaxAmount", taxAmt);
                                obj.put("taxamount", taxAmt);
                            }
                        }
                        obj.put("rateIncludingGst", authHandler.roundUnitPrice(row.getRateincludegst(), companyid));
                        obj.put("israteIncludingGst", po.isGstIncluded());
                        obj.put("balanceQuantity", row.getBalanceqty());
                        if (!StringUtil.isNullOrEmpty(row.getSalesorderdetailid())) {
                            KwlReturnObject sodetailresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), row.getSalesorderdetailid());
                            SalesOrderDetail salesOrderDetail = (SalesOrderDetail) sodetailresult.getEntityList().get(0);
                            if (salesOrderDetail != null) {
                                obj.put("linkto", salesOrderDetail.getSalesOrder().getSalesOrderNumber());
                                obj.put("linkid", salesOrderDetail.getSalesOrder().getID());
                                /*
                                     ERM-1037
                                     Field used for comparing dates to restrict linking of future doument date in Purchase Order document editing
                                 */
                                obj.put("linkDate", salesOrderDetail.getSalesOrder().getOrderDate());
                                obj.put("rowid", salesOrderDetail.getID());
                                obj.put("savedrowid", row.getID());
                                obj.put("docrowid", row.getID());
                                obj.put("linktype", 0);
                               
                                /*
                                 In Edit case need Sales Order customer id and isPOFromSO flag
                                */
                                if (salesOrderDetail.getSalesOrder().getCustomer() != null) {
                                    obj.put("customeridforshippingaddress", salesOrderDetail.getSalesOrder().getCustomer().getID());
                                    obj.put("isPOfromSO", true);
                                }
                            }
                        } else if (row.getVqdetail() != null) {
                            VendorQuotationDetail vqDetail = row.getVqdetail();
                            obj.put("linkto", vqDetail.getVendorquotation().getQuotationNumber());
                            obj.put("linkid", vqDetail.getVendorquotation().getID());
                            /*
                                 ERM-1037
                                 Field used for comparing dates to restrict linking of future doument date in Purchase Order document editing
                             */
                            obj.put("linkDate", vqDetail.getVendorquotation().getQuotationDate());
                            obj.put("rowid", vqDetail.getID());
                            obj.put("savedrowid", row.getID());
                            obj.put("docrowid", row.getID());
                            obj.put("linktype", 2);
                        } else if (!StringUtil.isNullOrEmpty(row.getPurchaseRequisitionDetailId())) {
                            KwlReturnObject sodetailresult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), row.getPurchaseRequisitionDetailId());
                            PurchaseRequisitionDetail purchaseRequisitionDetail = (PurchaseRequisitionDetail) sodetailresult.getEntityList().get(0);
                            if(purchaseRequisitionDetail!=null){
                            obj.put("linkto", purchaseRequisitionDetail.getPurchaserequisition().getPrNumber());
                            obj.put("linkid", purchaseRequisitionDetail.getPurchaserequisition().getID());
                            /*
                                 ERM-1037
                                 Field used for comparing dates to restrict linking of future doument date in Purchase Order document editing
                             */
                            obj.put("linkDate", purchaseRequisitionDetail.getPurchaserequisition().getRequisitionDate());
                            obj.put("rowid", purchaseRequisitionDetail.getID());
                             }
                            obj.put("savedrowid", row.getID());
                            obj.put("docrowid", row.getID());
                            obj.put("linktype", 5);
                            } else if (!StringUtil.isNullOrEmpty(row.getWorkorderdetailid())) {
                                KwlReturnObject wcdetailresult = accountingHandlerDAOobj.getObject(WorkOrderComponentDetails.class.getName(), row.getWorkorderdetailid());
                                WorkOrderComponentDetails workOrderComponentDetails = (WorkOrderComponentDetails) wcdetailresult.getEntityList().get(0);
                                if (workOrderComponentDetails != null) {
                                    obj.put("linkto", workOrderComponentDetails.getWorkOrder().getWorkOrderID());
                                    obj.put("linkid", workOrderComponentDetails.getWorkOrder().getID());
                                    obj.put("linkDate", workOrderComponentDetails.getWorkOrder().getWorkOrderDate());
                                    obj.put("rowid", workOrderComponentDetails.getID());
                                }
                                obj.put("savedrowid", row.getID());
                                obj.put("docrowid", row.getID());
                                obj.put("linktype", 11);
                            } else {
                            obj.put("linkto", "");
                            obj.put("linkid", "");
                            obj.put("linktype", -1);
                        }

                        if (po.isFixedAssetPO()) {
                            getAssetDetailForPORows(obj, companyid, linkingFlag, row, df);
                        }
                        //Check Integration and QA Approval flow then check status
                        boolean rejected = false;
                        if (row.getQastatus() == Constants.QA_Rejected) {
                            rejected = true;
                        }
                        String salesOrPurchase = requestParams.containsKey("termSalesOrPurchaseCheck") ? requestParams.get("termSalesOrPurchaseCheck").toString() : "false";
                        if (extraCompanyPreferences.getLineLevelTermFlag()==1) { // Fetch Vat term details of Product
                            if(!sopolinkflag){
                                Map<String, Object> mapData = new HashMap<String, Object>();
                                mapData.put("productid", row.getProduct().getID());
                                mapData.put("salesOrPurchase", salesOrPurchase);
                                mapData.put("podetails", row.getID());
                                KwlReturnObject result6 = accPurchaseOrderobj.getPurchaseOrderDetailsTermMap(mapData);
                                if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                                    ArrayList<PurchaseOrderDetailsTermMap> productTermDetail = (ArrayList<PurchaseOrderDetailsTermMap>) result6.getEntityList();
                                    JSONArray productTermJsonArry = new JSONArray();
                                    for (PurchaseOrderDetailsTermMap productTermsMapObj : productTermDetail) {
                                        JSONObject productTermJsonObj = new JSONObject();
                                        productTermJsonObj.put("id", productTermsMapObj.getId());
                                        productTermJsonObj.put("productid", productTermsMapObj.getProduct().getID());
                                        productTermJsonObj.put("termid", productTermsMapObj.getTerm().getId());
                                        /**
                                         * ERP-32829 
                                         */
                                        productTermJsonObj.put("productentitytermid", productTermsMapObj.getEntitybasedLineLevelTermRate()!=null?productTermsMapObj.getEntitybasedLineLevelTermRate().getId():"");
                                        productTermJsonObj.put("isDefault", productTermsMapObj.isIsGSTApplied());
                                        productTermJsonObj.put("term", productTermsMapObj.getTerm().getTerm());
                                        productTermJsonObj.put("formula", productTermsMapObj.getTerm().getFormula());
                                        productTermJsonObj.put("formulaids", productTermsMapObj.getTerm().getFormula());
                                        productTermJsonObj.put("termpercentage", productTermsMapObj.getPercentage());
                                        productTermJsonObj.put("originalTermPercentage", productTermsMapObj.getTerm().getPercentage()); // For Service Tax Abatemnt calculation
                                        productTermJsonObj.put("termamount", productTermsMapObj.getTermamount());
                                        productTermJsonObj.put("glaccountname", productTermsMapObj.getTerm().getAccount().getAccountName());
                                        productTermJsonObj.put("glaccount", productTermsMapObj.getTerm().getAccount().getID());
                                        productTermJsonObj.put("IsOtherTermTaxable", productTermsMapObj.getTerm().isOtherTermTaxable());
                                        productTermJsonObj.put("sign", productTermsMapObj.getTerm().getSign());
                                        productTermJsonObj.put("purchasevalueorsalevalue", productTermsMapObj.getPurchaseValueOrSaleValue());
                                        productTermJsonObj.put("deductionorabatementpercent", productTermsMapObj.getDeductionOrAbatementPercent());
                                        productTermJsonObj.put("assessablevalue", productTermsMapObj.getAssessablevalue());
                                        productTermJsonObj.put("taxtype", productTermsMapObj.getTaxType());
                                        productTermJsonObj.put("taxvalue", productTermsMapObj.getPercentage());
                                        productTermJsonObj.put("termtype", productTermsMapObj.getTerm().getTermType());
                                        productTermJsonObj.put("termsequence", productTermsMapObj.getTerm().getTermSequence());
                                        productTermJsonObj.put("formType", productTermsMapObj.getTerm().getFormType());
                                        productTermJsonObj.put("creditnotavailedaccount", productTermsMapObj.getTerm().getCreditNotAvailedAccount() != null ? productTermsMapObj.getTerm().getCreditNotAvailedAccount().getID() : "");
                                        productTermJsonObj.put("payableaccountid", productTermsMapObj.getTerm().getPayableAccount() != null ? productTermsMapObj.getTerm().getPayableAccount().getID() : "");
                                        productTermJsonObj.put(IndiaComplianceConstants.GST_CESS_TYPE, productTermsMapObj.getEntitybasedLineLevelTermRate()!=null && productTermsMapObj.getEntitybasedLineLevelTermRate().getCessType()!=null ? productTermsMapObj.getEntitybasedLineLevelTermRate().getCessType().getId() : "");
                                        productTermJsonObj.put(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT, productTermsMapObj.getEntitybasedLineLevelTermRate()!=null ? productTermsMapObj.getEntitybasedLineLevelTermRate().getValuationAmount():0.0);
                                        productTermJsonObj.put(IndiaComplianceConstants.DEFAULT_TERMID, productTermsMapObj.getTerm()!=null && productTermsMapObj.getTerm().getDefaultTerms()!=null ? productTermsMapObj.getTerm().getDefaultTerms().getId() : "");
                                        productTermJsonArry.put(productTermJsonObj);
                                    }
                                    obj.put("LineTermdetails", productTermJsonArry.toString());
                                }
                            }else{
                                boolean isDefault = true;
                                Map<String,Object> mapData=new HashMap<String,Object>();
                                mapData.put("productid", row.getProduct().getID());
                                mapData.put("salesOrPurchase", true);
                                obj.put("recTermAmount",0);
                                if(isDefault){
                                    mapData.put("isDefault", isDefault);
                                }
                                KwlReturnObject result6 = accProductObj.getProductTermDetails(mapData);
                                if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                                    ArrayList<ProductTermsMap> productTermDetail = (ArrayList<ProductTermsMap>) result6.getEntityList();
                                    JSONArray productTermJsonArry = CommonFunctions.fetchProductTermMapDetails(productTermDetail);
                                    obj.put("LineTermdetails", productTermJsonArry.toString());
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
                            }
                                if (row.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                                    /**
                                     * Put GST Tax Class History.
                                     */
                                    obj.put("refdocid", row.getID());
                                    fieldDataManagercntrl.getGSTTaxClassHistory(obj);
                                }
                            }
                        
                        if (country.getID().equals(String.valueOf(Constants.indian_country_id))) { // Fetch Vat term details of Product
                            // Excise AND VAT special Rate type TAX ------ START-------

                            boolean carryin = StringUtil.isNullOrEmpty(salesOrPurchase) ? true : Boolean.parseBoolean(salesOrPurchase);
                            String uomid = (row == null) ? "" : (row.getUom() != null ? row.getUom().getID() : "");
                            if (extraCompanyPreferences.isExciseApplicable()) {
                                String reortingUOM = (row.getReportingUOMExcise() != null) ? row.getReportingUOMExcise().getID() : "";
                                String valuationType = !StringUtil.isNullOrEmpty(row.getExciseValuationType()) ? row.getExciseValuationType() : "";
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
                                    obj.put("productMRP", row.getMrpIndia());
                                }
                            }
                            if (extraCompanyPreferences.isEnableVatCst()) {
                                String reortingUOMVAT = (row.getReportingUOMVAT() != null) ? row.getReportingUOMVAT().getID() : "";
                                String valuationTypeVAT = !StringUtil.isNullOrEmpty(row.getVatValuationType()) ? row.getVatValuationType() : "";
                                obj.put("valuationTypeVAT", valuationTypeVAT);
                                if ((Constants.QUENTITY).equals(valuationTypeVAT)) {
                                    obj.put("reportingUOMVAT", reortingUOMVAT);
                                    obj.put("compairwithUOMVAT", 1);

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
                                    obj.put("productMRP", row.getMrpIndia());
                                }
                            }
                            // Excise AND VAT special type TAX ------ END-------          
                        }
                        if (doflag || isForInvoice) {//Loading only open purchase order rows in GR or Invoice
                            if (!rejected && (extraCompanyPreferences.isAllowZeroQuantityInPO()?addobj >= 0:addobj > 0) && !row.isIsLineItemClosed()) {
                                jArr.put(obj);
                            }
                        } else {
                            if (!rejected && (extraCompanyPreferences.isAllowZeroQuantityInPO()?addobj >= 0:addobj > 0)) {
                                jArr.put(obj);
                            }
                        }
                    }
                    }
                }
//                i++;
            }
        }
        } catch (Exception je) {
            throw ServiceException.FAILURE("getPurchaseOrderRows : " + je.getMessage(), je);
        }
        return jArr;
    }
/**
 * To get line level data for security get entry form
 * @param requestParams
 * @return
 * @throws ServiceException 
 */    
    @Override
    public JSONArray getSecurityGateEbtryOrderRows(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {

            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get("companyid");
            DateFormat df = (DateFormat) requestParams.get("dateFormatValue");

            Map<String, Object> ProductFieldsRequestParams = new HashMap();
            ProductFieldsRequestParams.put("companyid", companyid);
            ProductFieldsRequestParams.put("moduleid", Constants.Acc_SecurityGateEntry_ModuleId);
            List masterFieldsResultList = CommonFunctions.getproductmastersFieldsToShowLineLevel(ProductFieldsRequestParams, accountingHandlerDAOobj);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            boolean doflag = requestParams.containsKey("doflag") ? (Boolean) requestParams.get("doflag") : false;
            String[] pos = (String[]) requestParams.get("bills");
            double addobj = 1;
            String closeflag = (String) requestParams.get("closeflag");

            boolean isForInvoice = false;
            if (requestParams.containsKey("isForInvoice") && requestParams.get("isForInvoice") != null) {
                isForInvoice = (Boolean) requestParams.get("isForInvoice");
            }
            boolean isForLinking = false;

            if (requestParams.containsKey("isForLinking") && requestParams.get("isForLinking") != null) {// True in case of linking with VI
                isForLinking = (Boolean) requestParams.get("isForLinking");
            }

            int moduId = 0;
            if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null) {// True in case of linking with VI
                moduId = Integer.parseInt(requestParams.get("moduleid").toString());
            }

            boolean linkingFlag = false;
            if (requestParams.containsKey("linkingFlag") && requestParams.get("linkingFlag") != null) { // True in case of Asset linking with VQ
                linkingFlag = (Boolean) requestParams.get("linkingFlag");
            }
            boolean isForDOGROLinking = false;
            if (requestParams.containsKey("isForDOGROLinking") && requestParams.get("isForDOGROLinking") != null) {
                isForDOGROLinking = (Boolean) requestParams.get("isForDOGROLinking");
                /*
                true in case SGE is linked with GRO
                */
            }
            boolean isExport = false;
            if (requestParams.containsKey("isExport") && requestParams.get("isExport") != null) { // True in case of Export
                isExport = (Boolean) requestParams.get("isExport");
            }
            boolean sopolinkflag = false;
            if (requestParams.containsKey(Constants.POSOFLAG) && requestParams.get(Constants.POSOFLAG) != null) { // True in case of crosslink of PO and SO 
                sopolinkflag = (Boolean) requestParams.get(Constants.POSOFLAG);
            }

            boolean isForReport = false;
            boolean isBatchForProduct = false;
            boolean isSerialForProduct = false;
            boolean isLocationForProduct = false;
            boolean isWarehouseForProduct = false;
            boolean isRowForProduct = false;
            boolean isRackForProduct = false;
            boolean isBinForProduct = false;
            String description = "";
            double minqty = 0.0;
            double maxqty = 0.0;
            String productsDefaultLocation = "", productsDefaultWarehouse = "";
            if (requestParams.containsKey("isForReport")) {
                isForReport = (Boolean) requestParams.get("isForReport");
            }
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Object> poRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("securityGateEntry.ID");
            order_by.add("srno");
            order_type.add("asc");
            poRequestParams.put("filter_names", filter_names);
            poRequestParams.put("filter_params", filter_params);
            poRequestParams.put("order_by", order_by);
            poRequestParams.put("order_type", order_type);
            
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            capresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) capresult.getEntityList().get(0);
            Country country = extraCompanyPreferences.getCompany().getCountry();
            String stdate = authHandler.getDates(preferences.getFinancialYearFrom(), true);
            String enddate = authHandler.getDates(preferences.getFinancialYearFrom(), false);
            
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_SecurityGateEntry_ModuleId, 1));
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            
            for (int i = 0; pos != null && i < pos.length; i++) {
                KwlReturnObject poresult = accountingHandlerDAOobj.getObject(SecurityGateEntry.class.getName(), pos[i]);
                SecurityGateEntry po = (SecurityGateEntry) poresult.getEntityList().get(0);
                KWLCurrency currency = null;
                if (po.getCurrency() != null) {
                    currency = po.getCurrency();
                } else {
                    currency = po.getVendor().getAccount().getCurrency() == null ? kwlcurrency : po.getVendor().getAccount().getCurrency();
                }
                
                filter_params.clear();
                filter_params.add(po.getID());
                KwlReturnObject podresult = accPurchaseOrderobj.getSecurityGateDetails(poRequestParams);
                List<SecurityGateDetails> podetails = podresult.getEntityList();
                
                for (SecurityGateDetails row : podetails) {
                    JSONObject obj = new JSONObject();
                    CommonFunctions.getterMethodForProductsData(row.getProduct(), masterFieldsResultList, obj);
                    obj.put("billid", po.getID());
                    obj.put("billno", po.getSecurityNumber());
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("currencyCode", currency.getCurrencyCode() == null ? "" : currency.getCurrencyCode());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", row.getProduct().getID());
                    obj.put("hasAccess", row.getProduct().isIsActive());
                    obj.put("productname", row.getProduct().getName());
                    obj.put("type", row.getProduct().getProducttype() == null ? "" : row.getProduct().getProducttype().getName());
                    obj.put("typeid", row.getProduct().getProducttype() == null ? "" : row.getProduct().getProducttype().getID());
                    obj.put("pid", row.getProduct().getProductid());
                    obj.put("supplierpartnumber", StringUtil.isNullOrEmpty(row.getSupplierpartnumber()) ? "" : row.getSupplierpartnumber());
                    obj.put("status", row.isIsLineItemClosed() ? "Yes" : "N/A");
                    if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                        description = row.getDescription();
                    } else if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                        description = row.getProduct().getDescription();
                    } else {
                        description = "";
                    }
                    obj.put("desc", StringUtil.DecodeText(description));
                    obj.put("description", StringUtil.DecodeText(description));
                    obj.put("unitname", row.getUom() != null ? row.getUom().getNameEmptyforNA() : row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("uomname", row.getUom() != null ? row.getUom().getNameEmptyforNA() : row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("baseuomid", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getID());
                    obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("multiuom", row.getProduct().isMultiuom());
                    obj.put("memo", row.getRemark());
                    obj.put("includeprotax", row.getTax() != null ? true : false);
                    String productsBaseUomId = (row.getProduct().getUnitOfMeasure() == null) ? "" : row.getProduct().getUnitOfMeasure().getID();
                    String selectedUomId = (row.getUom() != null) ? row.getUom().getID() : "";
                    if (row.getProduct().isblockLooseSell() && !productsBaseUomId.equals(selectedUomId)) {
                        KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(row.getProduct().getID(), selectedUomId);
                        double availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                        obj.put("availableQtyInSelectedUOM", availableQuantity);
                        obj.put("isAnotherUOMSelected", true);
                        HashMap<String, Object> orderParams = new HashMap<String, Object>();
                        orderParams.put("companyid", companyid);
                        orderParams.put("gcurrencyid", currencyid);
                        orderParams.put("df", df);
                        orderParams.put("pendingapproval", false);
                        orderParams.put("startdate", stdate);
                        orderParams.put("enddate", enddate);
                        orderParams.put("currentuomid", selectedUomId);
                        orderParams.put("productId", row.getProduct().getID());

                        double pocountinselecteduom = accSalesOrderServiceDAOobj.getPOCount(orderParams);

                        double socountinselecteduom = accSalesOrderServiceDAOobj.getSOCount(orderParams);

                        obj.put("pocountinselecteduom", pocountinselecteduom);
                        obj.put("socountinselecteduom", socountinselecteduom);
                    }

                    Map<String, Object> variableMap = new HashMap<String, Object>();

                    variableMap = new HashMap<String, Object>();
                    replaceFieldMap = new HashMap<String, String>();
                    double unitprice = row.getRate();
                    obj.put("rate", unitprice);
                    obj.put("priceSource", row.getPriceSource() != null ? row.getPriceSource() : "");
                    obj.put("discountispercent", row.getDiscountispercent());
                    if (isExport) {
                        obj.put("prdiscount", row.getDiscountispercent() == 1 ? (row.getDiscount() != 0 ? (row.getDiscount() + "%") : row.getDiscount()) : currency.getSymbol() + " " + row.getDiscount());
                    } else {
                        obj.put("prdiscount", row.getDiscount());
                    }

                    if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
                        Product product = row.getProduct();// (Product) prodresult.getEntityList().get(0);
                        isLocationForProduct = product.isIslocationforproduct();
                        isWarehouseForProduct = product.isIswarehouseforproduct();
                        isBatchForProduct = product.isIsBatchForProduct();
                        isSerialForProduct = product.isIsSerialForProduct();
                        isRowForProduct = product.isIsrowforproduct();
                        isRackForProduct = product.isIsrackforproduct();
                        isBinForProduct = product.isIsbinforproduct();
                        maxqty = product.getMaxOrderingQuantity();
                        minqty = product.getMinOrderingQuantity();
                        productsDefaultLocation = (product.getLocation() != null && product.getLocation().getId() != null) ? product.getLocation().getId() : "";
                        productsDefaultWarehouse = (product.getWarehouse() != null && product.getWarehouse().getId() != null) ? product.getWarehouse().getId() : "";
                        if (extraCompanyPreferences != null && extraCompanyPreferences.getUomSchemaType() == Constants.PackagingUOM) {
                            obj.put("caseuom", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUoM().getID() : "");
                            obj.put("caseuomvalue", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUomValue() : 1);
                            obj.put("inneruom", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUoM().getID() : "");
                            obj.put("inneruomvalue", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUomValue() : 1);
                            obj.put("stockuom", (product.getUnitOfMeasure() != null) ? product.getUnitOfMeasure().getID() : "");
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
                        obj.put("maxorderingquantity", maxqty); 
                        obj.put("minorderingquantity", minqty);
                         
                        if ((preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory())) {  //check if company level option is on then only we will check productt level
                            if (isForDOGROLinking && ((isLocationForProduct && !StringUtil.isNullOrEmpty(productsDefaultLocation)) || (isWarehouseForProduct && !StringUtil.isNullOrEmpty(productsDefaultWarehouse))) && !isBatchForProduct && !isSerialForProduct) {
                                obj.put("batchdetails", getdefaultBatchJson(row.getProduct(), row.getID(), row.getQuantity()));
                            }
                        }
                    double rowTaxPercent = 0;
                    double rowTaxAmount = 0;
                    boolean isRowTaxApplicable = false;
                    if (row.getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), po.getSecurityDate(), row.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                        isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                        if (isRowTaxApplicable) {
                            rowTaxAmount = row.getRowTaxAmount();
                        }
                    }
                    obj.put("prtaxpercent", rowTaxPercent);
                    obj.put("rowTaxAmount", rowTaxAmount);
                    obj.put("taxamount", rowTaxAmount);
                    obj.put("recTermAmount", row.getRowTermAmount());
                    obj.put("OtherTermNonTaxableAmount", row.getOtherTermNonTaxableAmount());
                    obj.put("rowTaxPercent", rowTaxPercent);
                    obj.put("prtaxid", row.getTax() == null ? "None" : row.getTax().getID());

                    /*
                     * These keys are used while exporting details
                     */
                    double rowamountwithgst = 0;
                    double discountValueForExcel = 0, amountForExcelFile = 0;
                    if (row.getSecurityGateEntry().isGstIncluded()) {//if gstincluded is the case
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

                    obj.put(Constants.unitpriceForExcelFile, row.getRate());//obj.put(Constants.unitpriceForExcelFile, (Double) bAmt.getEntityList().get(0));
                    obj.put("marginExchangeRate", row.getSecurityGateEntry() != null ? row.getSecurityGateEntry().getExternalCurrencyRate() : 0);
                    double baseuomrate = row.getBaseuomrate();
                    if (row.getUom() != null) {
                        obj.put("uomid", row.getUom().getID());
                    } else {
                        obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
                    }
                    double quantity = 0;
                    double invoiceRowProductQty = row.getQuantity() * baseuomrate;
                    double remainedQty = invoiceRowProductQty;// which has not been linked yet
                    if (closeflag != null) {
                        addobj = row.getBalanceqty();
                        quantity = addobj;
                        obj.put("quantity", addobj);
                        obj.put("copyquantity", addobj);
                        obj.put("dquantity", addobj);
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

                    // Added vendor related information for Profit Margin Calculation
                    if (extraCompanyPreferences.isActivateProfitMargin() && isForLinking) {
                        obj.put("vendorid", (row.getSecurityGateEntry() != null && row.getSecurityGateEntry().getVendor() != null) ? row.getSecurityGateEntry().getVendor().getID() : "");
                        obj.put("vendorcurrexchangerate", row.getSecurityGateEntry() != null && row.getSecurityGateEntry().getExternalCurrencyRate() != 0 ? (1 / row.getSecurityGateEntry().getExternalCurrencyRate()) : 1);
                        obj.put("vendorcurrencyid", (row.getSecurityGateEntry() != null && row.getSecurityGateEntry().getCurrency() != null) ? row.getSecurityGateEntry().getCurrency().getCurrencyID() : "");
                        obj.put("vendorcurrencysymbol", (row.getSecurityGateEntry() != null && row.getSecurityGateEntry().getCurrency() != null) ? row.getSecurityGateEntry().getCurrency().getSymbol() : "");
                        obj.put("vendorunitcost", row.getRate());
                    }

                    if (isForLinking) {
                        if (row.getTax() != null && invoiceRowProductQty > 0) {
                            double taxAmt = (rowTaxAmount / invoiceRowProductQty) * remainedQty;
                            obj.put("rowTaxAmount", taxAmt);
                            obj.put("taxamount", taxAmt);
                        }
                    }
                    obj.put("rateIncludingGst", authHandler.roundUnitPrice(row.getRateincludegst(), companyid));
                    obj.put("israteIncludingGst", po.isGstIncluded());
                    obj.put("balanceQuantity", row.getBalanceqty());
                    if (row.getPodetail() != null) {
                        PurchaseOrderDetail poDetail = row.getPodetail();
                        if (poDetail != null) {
                            obj.put("linkto", poDetail.getPurchaseOrder().getPurchaseOrderNumber());
                            obj.put("linkid", poDetail.getPurchaseOrder().getID());
                            obj.put("rowid", poDetail.getID());
                            obj.put("savedrowid", row.getID());
                            obj.put("docrowid", row.getID());
                            obj.put("linktype", 0);
                        }
                    }
                    
                    String salesOrPurchase = requestParams.containsKey("termSalesOrPurchaseCheck") ? requestParams.get("termSalesOrPurchaseCheck").toString() : "false";
                    if (extraCompanyPreferences.getLineLevelTermFlag() == 1) {                        
                            Map<String, Object> mapData = new HashMap<String, Object>();
                            mapData.put("productid", row.getProduct().getID());
                            mapData.put("salesOrPurchase", salesOrPurchase);
                            mapData.put("sgedetails", row.getID());
                            KwlReturnObject result6 = accPurchaseOrderobj.getSGEDetailsTermMap(mapData);
                            if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                                ArrayList<SGEDetailsTermMap> productTermDetail = (ArrayList<SGEDetailsTermMap>) result6.getEntityList();
                                JSONArray productTermJsonArry = new JSONArray();
                                for (SGEDetailsTermMap productTermsMapObj : productTermDetail) {
                                    JSONObject productTermJsonObj = new JSONObject();
                                    productTermJsonObj.put("id", productTermsMapObj.getId());
                                    productTermJsonObj.put("productid", productTermsMapObj.getProduct().getID());
                                    productTermJsonObj.put("termid", productTermsMapObj.getTerm().getId());
                                    /**
                                     * ERP-32829
                                     */
                                    productTermJsonObj.put("productentitytermid", productTermsMapObj.getEntitybasedLineLevelTermRate() != null ? productTermsMapObj.getEntitybasedLineLevelTermRate().getId() : "");
                                    productTermJsonObj.put("isDefault", productTermsMapObj.isIsGSTApplied());
                                    productTermJsonObj.put("term", productTermsMapObj.getTerm().getTerm());
                                    productTermJsonObj.put("formula", productTermsMapObj.getTerm().getFormula());
                                    productTermJsonObj.put("formulaids", productTermsMapObj.getTerm().getFormula());
                                    productTermJsonObj.put("termpercentage", productTermsMapObj.getPercentage());
                                    productTermJsonObj.put("originalTermPercentage", productTermsMapObj.getTerm().getPercentage()); // For Service Tax Abatemnt calculation
                                    productTermJsonObj.put("termamount", productTermsMapObj.getTermamount());
                                    productTermJsonObj.put("glaccountname", productTermsMapObj.getTerm().getAccount().getAccountName());
                                    productTermJsonObj.put("glaccount", productTermsMapObj.getTerm().getAccount().getID());
                                    productTermJsonObj.put("IsOtherTermTaxable", productTermsMapObj.getTerm().isOtherTermTaxable());
                                    productTermJsonObj.put("sign", productTermsMapObj.getTerm().getSign());
                                    productTermJsonObj.put("purchasevalueorsalevalue", productTermsMapObj.getPurchaseValueOrSaleValue());
                                    productTermJsonObj.put("deductionorabatementpercent", productTermsMapObj.getDeductionOrAbatementPercent());
                                    productTermJsonObj.put("assessablevalue", productTermsMapObj.getAssessablevalue());
                                    productTermJsonObj.put("taxtype", productTermsMapObj.getTaxType());
                                    //                                productTermJsonObj.put("taxvalue", productTermsMapObj.getTaxType()==0 ? productTermsMapObj.getTermamount() : productTermsMapObj.getPercentage());
                                    productTermJsonObj.put("taxvalue", productTermsMapObj.getPercentage());
                                    productTermJsonObj.put("termtype", productTermsMapObj.getTerm().getTermType());
                                    productTermJsonObj.put("termsequence", productTermsMapObj.getTerm().getTermSequence());
                                    productTermJsonObj.put("formType", productTermsMapObj.getTerm().getFormType());
                                    productTermJsonObj.put("creditnotavailedaccount", productTermsMapObj.getTerm().getCreditNotAvailedAccount() != null ? productTermsMapObj.getTerm().getCreditNotAvailedAccount().getID() : "");
                                    productTermJsonObj.put("payableaccountid", productTermsMapObj.getTerm().getPayableAccount() != null ? productTermsMapObj.getTerm().getPayableAccount().getID() : "");
                                    productTermJsonArry.put(productTermJsonObj);
                                }
                                obj.put("LineTermdetails", productTermJsonArry.toString());
                            }
                        
                        
                        if (row.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                            /**
                             * Put GST Tax Class History.
                             */
                            obj.put("refdocid", row.getID());
                            fieldDataManagercntrl.getGSTTaxClassHistory(obj);
                        }
                    }
                    
                    /*
                    Retrieving Line Level custom fields for Security Gate Entry 
                    */
                     SecurityGateDetailsCustomData securityGateDetailsCustomData = (SecurityGateDetailsCustomData) row.getSgeDetailCustomData();
                    AccountingManager.setCustomColumnValues(securityGateDetailsCustomData, FieldMap, replaceFieldMap, variableMap);

                    if (securityGateDetailsCustomData != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, isExport);
                        params.put("isForReport", isForReport);
                        params.put("companyid",companyid);
                        params.put(Constants.isdefaultHeaderMap, requestParams.get(Constants.isdefaultHeaderMap));
                        if (isForDOGROLinking) {
                            params.put("isLink", true);
                            int moduleId = Constants.Acc_Goods_Receipt_ModuleId;
                            params.put("linkModuleId", moduleId);
                        }
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                    
                    boolean rejected = false;
                    if (doflag || isForInvoice) {//Loading only open purchase order rows in GR or Invoice
                        if (!rejected && (extraCompanyPreferences.isAllowZeroQuantityInPO() ? addobj >= 0 : addobj > 0) && !row.isIsLineItemClosed()) {
                            jArr.put(obj);
                        }
                    } else {
                        if (!rejected && (extraCompanyPreferences.isAllowZeroQuantityInPO() ? addobj >= 0 : addobj > 0)) {
                            jArr.put(obj);
                        }
                    }
                }
            }
        } catch (Exception je) {
            throw ServiceException.FAILURE("getPurchaseOrderRows : " + je.getMessage(), je);
        }
        return jArr;
    }
    
    
    /**
     * 
     * @param requestParams
     * @param paramJobj
     * @param jArr
     * @Desc : get Assembly Sub Product Rows
     * @throws ServiceException
     * @throws JSONException
     * @throws UnsupportedEncodingException
     * @throws ParseException
     * @throws SessionExpiredException 
     */
        public void getAssemblySubProductRows(Map<String, Object> requestParams, JSONObject paramJobj, JSONArray jArr) throws ServiceException, JSONException, UnsupportedEncodingException, ParseException, SessionExpiredException {
        PurchaseOrderDetail row = (PurchaseOrderDetail) requestParams.get("purchaseOrderDetailObject");
        String poid = row.getPurchaseOrder().getID();
        String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
        String bomid = "";
        KwlReturnObject result = null;
        JSONObject saDetailJson = new JSONObject();
        boolean isJobWorkStockOut = false;
        String companyid = paramJobj.optString("companyid");
        isJobWorkStockOut = paramJobj.optBoolean("isJobWorkStockOut", false);
        HashMap<String, Object> assemblyParams = new HashMap<String, Object>();
        HashMap<String, Object> SADetailParams = new HashMap<String, Object>();
        if (isJobWorkStockOut) { // On Job work in flag true, adding bomdetail id in params so that only ingredientys of that BOM can only load.
            bomid = row.getBomcode() != null ? row.getBomcode().getID() : "";
            if (!StringUtil.isNullOrEmpty(bomid)) {
                assemblyParams.put("bomdetailid", bomid);
            }
            SADetailParams.put("podid", row.getID());
            /*
             *  fetching SA details 
             */
            result = accProductObj.getISTDetailsForJobWorkOrder(SADetailParams);
            List<String> istDetails = result.getEntityList();
            /*
             *   Creating JSON For product and its quantity mapped
             */
            saDetailJson = getProductQuanityJSONForJWO(istDetails); 
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
                if (passembly.getSubproducts() != null && !passembly.getSubproducts().getProducttype().getID().equalsIgnoreCase(Producttype.SERVICE) && !passembly.getSubproducts().getProducttype().getID().equalsIgnoreCase(Producttype.CUSTOMER_INVENTORY) ) {
                    /**
                     * Exclude Service type and customer inventory type product
                     */
                    if (isJobWorkStockOut) {
                            requestParams.put("rowProductObject", passembly.getSubproducts());
                            JSONObject assemblyobj = getPurchaseOrderRowsDetails(requestParams, paramJobj);
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
                    } else {
                        requestParams.put("rowProductObject", passembly.getSubproducts());
                        JSONObject assemblyobj = getPurchaseOrderRowsDetails(requestParams, paramJobj);
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
         * To Get asssembly details of selected job work out from Aged order work report.
         */
        
       public void getAssemblySubProductRowsDetails(Map<String, Object> requestParams, JSONObject paramJobj, JSONArray jArr, String jobWorkOrderNo, String personname, Date date, String prodName) throws ServiceException, JSONException, UnsupportedEncodingException, ParseException, SessionExpiredException {
        if (requestParams.get("purchaseOrderDetailObject") != null) {
            KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(), (String) requestParams.get("purchaseOrderDetailObject"));
            PurchaseOrderDetail row = (PurchaseOrderDetail) curresult1.getEntityList().get(0);
            String currencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            String selectedJobStockOutid = (String) requestParams.get("selectedJobStockOutid");
            double orderedQty = (double) requestParams.get("orderedQty");
            String bomid = "";
            KwlReturnObject result = null;
            JSONObject saDetailJson = new JSONObject();
            boolean isJobWorkStockOut = false;
            String companyid = paramJobj.optString("companyid");
            String productId = (String) requestParams.get("rowProductId");
            isJobWorkStockOut = paramJobj.optBoolean("isJobWorkStockOut", false);
            HashMap<String, Object> assemblyParams = new HashMap<String, Object>();
            HashMap<String, Object> SADetailParams = new HashMap<String, Object>();
            if (isJobWorkStockOut) { // On Job work in flag true, adding bomdetail id in params so that only ingredientys of that BOM can only load.
                bomid = row.getBomcode() != null ? row.getBomcode().getID() : "";
                if (!StringUtil.isNullOrEmpty(bomid)) {
                    assemblyParams.put("bomdetailid", bomid);
                }
                SADetailParams.put("podid", row.getID());
                /*
                 * fetching SA details
                 */
                result = accProductObj.getISTDetailsForJobWorkOrder(SADetailParams);
                List<String> istDetails = result.getEntityList();
                /*
                 * Creating JSON For product and its quantity mapped
                 */
                saDetailJson = getProductQuanityJSONForJWO(istDetails);
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
                    if (productId.equals(passembly.getSubproducts().getID())) {
                        if (passembly.getSubproducts() != null && !passembly.getSubproducts().getProducttype().getID().equalsIgnoreCase(Producttype.SERVICE) && !passembly.getSubproducts().getProducttype().getID().equalsIgnoreCase(Producttype.CUSTOMER_INVENTORY)) {
                            /**
                             * Exclude Service type and customer inventory type
                             * product
                             */
                            if (isJobWorkStockOut) {
                                requestParams.put("rowProductObject", passembly.getSubproducts());
                                JSONObject assemblyobj = getPurchaseOrderRowsDetails(requestParams, paramJobj);
                                double quantity = assemblyobj.optDouble("quantity", 0);
                                /*
                                 * Fetching In quantity for sub product
                                 */
                                double inQuantity = saDetailJson.optDouble(passembly.getSubproducts().getID(), 0.0);
                                if (quantity > 0) {
                                    double subPrdQuantity = quantity * passembly.getActualQuantity();
                                    assemblyobj.put("copyquantity", subPrdQuantity);
                                    assemblyobj.put("dquantity", subPrdQuantity);
                                    assemblyobj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(subPrdQuantity, row.getBaseuomrate(), companyid));
                                    assemblyobj.put("baseuomrate", row.getBaseuomrate());
                                    assemblyobj.put("recQuantity", inQuantity);
                                    assemblyobj.put("jobWorkOrderNo", jobWorkOrderNo);
                                    assemblyobj.put("personname", personname);
                                    double usedqty = accAccountDAOobj.getSumofChallanUsedQuantity(selectedJobStockOutid);
                                    assemblyobj.put("date", date);
                                    KwlReturnObject price = accProductObj.getInitialPrice(productId, true);
                                    Object iniPrice = price.getEntityList().get(0);
                                    assemblyobj.put("rate", iniPrice);
                                    assemblyobj.put("selectedJobStockOutid", selectedJobStockOutid);
                                    assemblyobj.put("quantity", orderedQty - usedqty);
                                    jArr.put(assemblyobj);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
 
    /*
    * Creating JSON of a product and its stock in quantity for a job work order
    */
    public JSONObject getProductQuanityJSONForJWO(List<String> istDetails){
        JSONObject retJobj = new JSONObject();
        InterStoreTransferRequest iSTDetail = null;
        String productId = "";
        try {
            /*
            *   For Loop for Stock Adjustment Detail
            *   Creating a map for Stock adjustment detail product and Its Quantity Stocked IN
            */
            for (String saDetailId  : istDetails) {
                iSTDetail = (InterStoreTransferRequest) kwlCommonTablesDAOObj.getClassObject(InterStoreTransferRequest.class.getName(), saDetailId);
                productId = iSTDetail.getProduct().getID();
                retJobj.put(productId, iSTDetail.getOrderedQty()+ retJobj.optDouble(productId,0.0));
            }
        } catch (Exception ex) {
            
        }
        return  retJobj;
    }
    public JSONObject getPurchaseOrderRowsDetails(Map<String, Object> requestParams, JSONObject param) throws JSONException, ServiceException, UnsupportedEncodingException, SessionExpiredException {
        List masterFieldsResultList = Collections.EMPTY_LIST;
        if (requestParams.containsKey("masterFieldsResultList")) {
            masterFieldsResultList = (List) requestParams.get("masterFieldsResultList");;
        }
        /*
         * isJobWorkOutRemain is true if sales invoice is creating from Aged order work report.
         */
        boolean isJobWorkOutRemain = false;
        if (requestParams.containsKey("isJobWorkOutRemain") && requestParams.get("isJobWorkOutRemain") != null) {
            isJobWorkOutRemain = (boolean) requestParams.get("isJobWorkOutRemain");
        }
        PurchaseOrderDetail row = null;
        if (isJobWorkOutRemain) {
            KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(), (String) requestParams.get("purchaseOrderDetailObject"));
            row = (PurchaseOrderDetail) curresult1.getEntityList().get(0);
        } else {
            row = (PurchaseOrderDetail) requestParams.get("purchaseOrderDetailObject");
        }
        Product rowProduct = (Product) requestParams.get("rowProductObject");
        PurchaseOrder po = (PurchaseOrder) requestParams.get("purchaseOrderObject");
        KWLCurrency currency = null;
        DateFormat df = (DateFormat) requestParams.get("df");
        String currencyid = param.optString("gcurrencyid");
        String companyid = po.getCompany().getCompanyID();
        String description = "";
        String stdate = param.optString("stdate");
        String enddate = param.optString("enddate");
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) requestParams.get("preferences");
        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) requestParams.get("extraCompanyPreferences");
        KwlReturnObject curresult1 = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency kwlcurrency = (KWLCurrency) curresult1.getEntityList().get(0);
        if (po.getCurrency() != null) {
            currency = po.getCurrency();
        } else {
            currency = po.getVendor().getAccount().getCurrency() == null ? kwlcurrency : po.getVendor().getAccount().getCurrency();
        }
        JSONObject obj = new JSONObject();
        CommonFunctions.getterMethodForProductsData(rowProduct, masterFieldsResultList, obj);
        obj.put("billid", po.getID());
        obj.put("billno", po.getPurchaseOrderNumber());
        obj.put("currencysymbol", currency.getSymbol());
        obj.put("currencyCode", currency.getCurrencyCode() == null ? "" : currency.getCurrencyCode());
        obj.put("srno", row.getSrno());
        obj.put("rowid", row.getID());
        obj.put("podid", row.getID());
        obj.put("bomid", row.getBomcode() != null ? row.getBomcode().getID() : "");
        obj.put("bomcode", row.getBomcode() != null ? row.getBomcode().getBomCode() : "");
        obj.put("productid", rowProduct.getID());
        obj.put("hasAccess", rowProduct.isIsActive());
        obj.put("productname", rowProduct.getName());
        obj.put("itemdescription", rowProduct.getName());
        obj.put("leadtime", rowProduct.getLeadTimeInDays());
        obj.put("type", rowProduct.getProducttype() == null ? "" : rowProduct.getProducttype().getName());
        obj.put("typeid", rowProduct.getProducttype() == null ? "" : rowProduct.getProducttype().getID());
        obj.put("pid", rowProduct.getProductid());
        obj.put("supplierpartnumber", StringUtil.isNullOrEmpty(row.getSupplierpartnumber()) ? "" : row.getSupplierpartnumber());
        obj.put("status", row.isIsLineItemClosed() ? "Yes" : "N/A");
        if (rowProduct != null) {
            Packaging packaging = rowProduct.getPackaging();

            if (packaging != null) {
                JSONObject jObj = new JSONObject();
                obj.put("packagingid", packaging.getId());
                double orderToStockUOMFactor = 1;
                double transferToStockUOMFactor = 1;
                String packag = "";
                orderToStockUOMFactor = packaging.getStockUomQtyFactor(rowProduct.getOrderingUOM());
                transferToStockUOMFactor = packaging.getStockUomQtyFactor(rowProduct.getTransferUOM());
                packag = packaging.toString();
                obj.put("packaging", packag);
                obj.put("orderToStockUOMFactor", orderToStockUOMFactor);
                obj.put("transferToStockUOMFactor", transferToStockUOMFactor);
            }
        }
        if (!StringUtil.isNullOrEmpty(row.getDescription())) {
            description = row.getDescription();
        } else if (!StringUtil.isNullOrEmpty(rowProduct.getDescription())) {
            description = rowProduct.getDescription();
        } else {
            description = "";
        }
        UnitOfMeasure uom = rowProduct.getUnitOfMeasure();
        UnitOfMeasure purchaseuom = rowProduct.getPurchaseUOM();
        UnitOfMeasure salesuom = rowProduct.getSalesUOM();
        UnitOfMeasure orderingUoM = rowProduct.getOrderingUOM();
        UnitOfMeasure transferingUoM = rowProduct.getTransferUOM();
        obj.put("desc", URLDecoder.decode(description, StaticValues.ENCODING));
        obj.put("uomid", uom == null ? "" : uom.getID());
        obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
        obj.put("purchaseuom", purchaseuom == null ? "" : purchaseuom.getID());
        obj.put("salesuom", salesuom == null ? "" : salesuom.getID());
        obj.put("salesuomname", salesuom == null ? "" : salesuom.getNameEmptyforNA());
        obj.put("purchaseuomname", purchaseuom == null ? "" : purchaseuom.getNameEmptyforNA());
        obj.put("orderinguomname", orderingUoM == null ? "" : orderingUoM.getNameEmptyforNA());
        obj.put("orderinguomid", orderingUoM == null ? "" : orderingUoM.getID());
        obj.put("transferinguomname", transferingUoM == null ? "" : transferingUoM.getNameEmptyforNA());
        obj.put("transferinguomid", transferingUoM == null ? "" : transferingUoM.getID());
        obj.put("stockpurchaseuomvalue", (rowProduct.getPackaging() != null && purchaseuom != null) ? rowProduct.getPackaging().getStockUomQtyFactor(purchaseuom) : 1);
        obj.put("stocksalesuomvalue", (rowProduct.getPackaging() != null && salesuom != null) ? rowProduct.getPackaging().getStockUomQtyFactor(salesuom) : 1);
        obj.put("multiuom", rowProduct.isMultiuom());
        obj.put("description", URLDecoder.decode(description, StaticValues.ENCODING));
        obj.put("unitname", row.getUom() != null ? row.getUom().getNameEmptyforNA() : rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getNameEmptyforNA());
        obj.put("uomname", row.getUom() != null ? row.getUom().getNameEmptyforNA() : rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getNameEmptyforNA());
        obj.put("baseuomid", rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getID());
        obj.put("baseuomname", rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getNameEmptyforNA());
        obj.put("multiuom", rowProduct.isMultiuom());
        obj.put("memo", row.getRemark());
        obj.put("invstore", (StringUtil.isNullOrEmpty(row.getInvstoreid())) ? "" : row.getInvstoreid());
        obj.put("invlocation", (StringUtil.isNullOrEmpty(row.getInvlocid())) ? "" : row.getInvlocid());
        obj.put("shelfLocation", row.getShelfLocation());
        obj.put("isConsignment", po.isIsconsignment()); //for getting is consignment request
        obj.put("permit", row.getPermit() == null ? "" : row.getPermit());
        obj.put("includeprotax", row.getTax() != null ? true : false);
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
            orderParams.put("companyid", po.getCompany().getCompanyID());
            orderParams.put("gcurrencyid", currencyid);
            orderParams.put("df", df);
            orderParams.put("pendingapproval", false);
            orderParams.put("startdate", stdate);
            orderParams.put("enddate", enddate);
            orderParams.put("currentuomid", selectedUomId);
            orderParams.put("productId", rowProduct.getID());

            double pocountinselecteduom = accSalesOrderServiceDAOobj.getPOCount(orderParams);

            double socountinselecteduom = accSalesOrderServiceDAOobj.getSOCount(orderParams);

            obj.put("pocountinselecteduom", pocountinselecteduom);
            obj.put("socountinselecteduom", socountinselecteduom);
        }
        if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {  //This is sats specific code 	 
            obj.put("invoicetype", po.getInvoicetype());

            obj.put("dependentType", row.getDependentType() == null ? "" : row.getDependentType());
            try {
                obj.put("showquantity", URLDecoder.decode(row.getShowquantity() == null ? "" : row.getShowquantity(), "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(accInvoiceCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
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
        }

        obj.put("rate", row.getRate());
        obj.put("priceSource", row.getPriceSource() != null ? row.getPriceSource() : "");
        obj.put("discountispercent", row.getDiscountispercent());

        /* In exported file showing discount value with proper % or currency symbol 
         same as expander
                        
         */
        obj.put("prdiscount", row.getDiscount());
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        String productsDefaultLocation = "", productsDefaultWarehouse = "";
        if (!StringUtil.isNullOrEmpty(rowProduct.getID())) {
            Product product = rowProduct;
            isLocationForProduct = product.isIslocationforproduct();
            isWarehouseForProduct = product.isIswarehouseforproduct();
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
            isRowForProduct = product.isIsrowforproduct();
            isRackForProduct = product.isIsrackforproduct();
            isBinForProduct = product.isIsbinforproduct();
            productsDefaultLocation = (product.getLocation() != null && product.getLocation().getId() != null) ? product.getLocation().getId() : "";
            productsDefaultWarehouse = (product.getWarehouse() != null && product.getWarehouse().getId() != null) ? product.getWarehouse().getId() : "";
            if (extraCompanyPreferences != null && extraCompanyPreferences.getUomSchemaType() == Constants.PackagingUOM) {
                obj.put("caseuom", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUoM().getID() : "");
                obj.put("caseuomvalue", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUomValue() : 1);
                obj.put("inneruom", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUoM().getID() : "");
                obj.put("inneruomvalue", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUomValue() : 1);
                obj.put("stockuom", (product.getUnitOfMeasure() != null) ? product.getUnitOfMeasure().getID() : "");
            }

            /**
             * getDefaultLocationQty
             */
            if ((!(isBatchForProduct && isSerialForProduct)) && requestParams.containsKey("storeId") && requestParams.get("storeId") != null&&!StringUtil.isNullOrEmpty(requestParams.get("storeId").toString() )) {
                String storeId = (String) requestParams.get("storeId");
                Store store = null;
                Location location = null;
                if (!StringUtil.isNullOrEmpty(storeId)) {
                    store = storeService.getStoreById(storeId);
                    if ("true".equals(true)) {
                        location = store.getDefaultLocation();
                    }
                }
                List<NewProductBatch> productBatchList = location == null ? stockService.getERPActiveBatchList(product, store) : stockService.getERPActiveBatchList(product, store, location);
                for (NewProductBatch productBatch : productBatchList) {
                    if (!product.isIsBatchForProduct() && !product.isIsSerialForProduct()) {
                        obj.put("availableQty", productBatch.getQuantitydue() - (productBatch.getLockquantity()));
                        obj.put("defaultlocqty", productBatch.getQuantitydue() - (productBatch.getLockquantity()));
                        obj.put("deflocation", location != null ? location.getId() : "");
                    } else {
                        obj.put("defaultlocqty", 0);
                        obj.put("deflocation", location != null ? location.getId() : "");
                    }
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
        if ((preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory())) {  //check if company level option is on then only we will check productt level
            if (((isLocationForProduct && !StringUtil.isNullOrEmpty(productsDefaultLocation)) || (isWarehouseForProduct && !StringUtil.isNullOrEmpty(productsDefaultWarehouse))) && !isBatchForProduct && !isSerialForProduct) {
                obj.put("batchdetails", getdefaultBatchJson(rowProduct, row.getID(), row.getQuantity()));
            }
        }
        double rowTaxPercent = 0;
        double rowTaxAmount = 0;
        boolean isRowTaxApplicable = false;
        if (row.getTax() != null) {
            KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), po.getOrderDate(), row.getTax().getID());
            rowTaxPercent = (Double) perresult.getEntityList().get(0);
            isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
            if (isRowTaxApplicable) {
                rowTaxAmount = row.getRowTaxAmount();
            }
        }
        obj.put("prtaxpercent", rowTaxPercent);
        obj.put("rowTaxAmount", rowTaxAmount);
        obj.put("taxamount", rowTaxAmount);
        obj.put("recTermAmount", row.getRowTermAmount());
        obj.put("OtherTermNonTaxableAmount", row.getOtherTermNonTaxableAmount());
        obj.put("rowTaxPercent", rowTaxPercent);
        obj.put("prtaxid", row.getTax() == null ? "None" : row.getTax().getID());

        /*These keys are used while exporting details */
        double rowamountwithgst = 0;
        double discountValueForExcel = 0, amountForExcelFile = 0;
        if (row.getPurchaseOrder().isGstIncluded()) {//if gstincluded is the case
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

        obj.put(Constants.unitpriceForExcelFile, row.getRate());//obj.put(Constants.unitpriceForExcelFile, (Double) bAmt.getEntityList().get(0));
        obj.put("marginExchangeRate", row.getPurchaseOrder() != null ? row.getPurchaseOrder().getExternalCurrencyRate() : 0);
        double baseuomrate = row.getBaseuomrate();
        if (row.getUom() != null) {
            obj.put("uomid", row.getUom().getID());
        } else {
            obj.put("uomid", rowProduct.getUnitOfMeasure() != null ? rowProduct.getUnitOfMeasure().getID() : "");
        }

        double quantity = 0;
        double invoiceRowProductQty = row.getQuantity() * baseuomrate;
        quantity = row.getQuantity();
        obj.put("quantity", quantity);
        obj.put("copyquantity", quantity);
        obj.put("dquantity", quantity);
        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, baseuomrate, companyid));
        obj.put("baseuomrate", baseuomrate);
        obj.put("confactor", baseuomrate);

        obj.put("rateIncludingGst", authHandler.roundUnitPrice(row.getRateincludegst(), companyid));
        obj.put("israteIncludingGst", po.isGstIncluded());
        obj.put("balanceQuantity", row.getBalanceqty());
        if (!StringUtil.isNullOrEmpty(row.getSalesorderdetailid())) {
            KwlReturnObject sodetailresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), row.getSalesorderdetailid());
            SalesOrderDetail salesOrderDetail = (SalesOrderDetail) sodetailresult.getEntityList().get(0);
            if (salesOrderDetail != null) {
                obj.put("linkto", salesOrderDetail.getSalesOrder().getSalesOrderNumber());
                obj.put("linkid", salesOrderDetail.getSalesOrder().getID());
                obj.put("billblockstatus", salesOrderDetail.getSalesOrder().isDisabledSOforPO());       //Passing the Status of Sales order whether it is blocked Or not for edit view and copy case of purchase order ERP-35541 
                obj.put("rowid", salesOrderDetail.getID());
                obj.put("savedrowid", row.getID());
                obj.put("docrowid", row.getID());
                obj.put("linktype", 0);
            }
        } else if (row.getVqdetail() != null) {
            VendorQuotationDetail vqDetail = row.getVqdetail();
            obj.put("linkto", vqDetail.getVendorquotation().getQuotationNumber());
            obj.put("linkid", vqDetail.getVendorquotation().getID());
            obj.put("rowid", vqDetail.getID());
            obj.put("savedrowid", row.getID());
            obj.put("docrowid", row.getID());
            obj.put("linktype", 2);
        } else if (!StringUtil.isNullOrEmpty(row.getPurchaseRequisitionDetailId())) {
            KwlReturnObject sodetailresult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), row.getPurchaseRequisitionDetailId());
            PurchaseRequisitionDetail purchaseRequisitionDetail = (PurchaseRequisitionDetail) sodetailresult.getEntityList().get(0);
            obj.put("linkto", purchaseRequisitionDetail.getPurchaserequisition().getPrNumber());
            obj.put("linkid", purchaseRequisitionDetail.getPurchaserequisition().getID());
            obj.put("rowid", purchaseRequisitionDetail.getID());
            obj.put("savedrowid", row.getID());
            obj.put("docrowid", row.getID());
            obj.put("linktype", 5);
        } else {
            obj.put("linkto", "");
            obj.put("linkid", "");
            obj.put("linktype", -1);
        }

        return obj;
    }

    public JSONArray getExpenseDetailRows(JSONArray jArr, Map requestParams, PurchaseOrder purchaseOrder, List<ExpensePODetail> expensepodetails, HashMap<String, Integer> FieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap) throws JSONException, UnsupportedEncodingException, ServiceException {
        String companyid = (String) requestParams.get("companyid");
        boolean isExport = false;
        boolean isForReport = false;
        boolean isForLinking = false;
        boolean isCopy = (boolean) (requestParams.containsKey("iscopy") ? requestParams.get("iscopy") : false);
        if (requestParams.containsKey("isExport") && requestParams.get("isExport") != null) { // True in case of Export
            isExport = (Boolean) requestParams.get("isExport");
        }
        if (requestParams.containsKey("isForReport") && requestParams.get("isForReport") != null) { // True in case of Export
            isForReport = (Boolean) requestParams.get("isForReport");
        }
        if (requestParams.containsKey("isForLinking") && requestParams.get("isForLinking") != null) { // True in case of Export
            isForLinking = (Boolean) requestParams.get("isForLinking");
        }
        if (purchaseOrder != null && expensepodetails != null && !expensepodetails.isEmpty()) {
            for (ExpensePODetail row : expensepodetails) {
                /**
                 * for Account grid, load account only greater than zero amount.
                 */
                boolean partialPo=(row.getBalAmount()<= 0.0 && isForLinking) ? false : true;
                if (isForReport || partialPo)  {
                    JSONObject obj = new JSONObject();
                    obj.put("billid", purchaseOrder.getID());
                    obj.put("billno", purchaseOrder.getPurchaseOrderNumber());
                    obj.put("currencysymbol", (purchaseOrder.getCurrency() == null ? "" : purchaseOrder.getCurrency().getSymbol()));
                    obj.put("currencyCode", purchaseOrder.getCurrency() == null ? "" : purchaseOrder.getCurrency().getCurrencyCode());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("accountid", row.getAccount().getID());
                    obj.put("desc",StringUtil.DecodeText(row.getDescription()));
                    obj.put("debit", row.isIsdebit());
                    obj.put("isgstincluded", purchaseOrder.isGstIncluded());
                    obj.put("rateIncludingGstEx", row.getRateIncludingGst());
                    obj.put("accountname", row.getAccount().getName());
                    obj.put("isexpenseinv", purchaseOrder.isIsExpenseType());
                    /**
                     * Balance Amount shown in linking case of Expense PI.
                     */
                    if(row.getDiscount()!=null){
                        obj.put("rate", isForLinking ? row.getBalAmount()+row.getDiscount().getDiscountValue() : row.getRate());
                    } else {
                    obj.put("rate", isForLinking ? row.getBalAmount() : row.getRate());
                    }
                    obj.put(Constants.unitpriceForExcelFile, row.getRate());//for export excelfile
                    obj.put("orignalamount", row.getAmount());
                    obj.put(Constants.amountForExcelFile,row.getAmount());
                    obj.put("amount", row.getAmount());
                    obj.put("balanceAmount", row.getBalAmount());
                    obj.put("includeprotax", row.getTax()!=null?true:false);
                    /*
                     * disc.isInPercent()=1 (discountispercent is percentage)
                     * disc.isInPercent()=0 (discountispercent is flat)
                     */
                    Discount disc = row.getDiscount();
                    if (disc != null) {

                        /* In exported file showing discount value with proper % or currency symbol 
                         same as expander
                        
                         */
                    
                        if (isExport) {
                            obj.put("prdiscount", disc.isInPercent() ? (disc.getDiscount() + "%") : (purchaseOrder.getCurrency() == null ? "" : purchaseOrder.getCurrency().getSymbol())+ " " + disc.getDiscount());
                        } else {
                            obj.put("prdiscount", disc.getDiscount());
                        }

                        /*
                         * If discount in percent then calculate
                         */
                        obj.put("discountvalue", disc.getDiscountValue());
                        obj.put("discountispercent", disc.isInPercent() ? 1 : 0);
                    } else {
                        obj.put("prdiscount", 0);
                        obj.put("discountvalue", 0);
                        obj.put("discountispercent", 1);
                    }
                    double taxPercent = 0;
                    double rowTaxPercent = 0;
                    if (row.getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, purchaseOrder.getOrderDate(), row.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("prtaxpercent", rowTaxPercent);
                    if (purchaseOrder.isGstIncluded()) {
                        obj.put("isIncludingGst", true);
                    }
                    obj.put("rowTaxAmount", row.getRowTaxAmount());
                    obj.put("taxamount", row.getRowTaxAmount());
                    obj.put("prtaxid", row.getTax() == null ? "" : row.getTax().getID());
                    obj.put(Constants.isUserModifiedTaxAmount, row.isIsUserModifiedTaxAmount());
//                    obj.put("prtaxid", row.getTax() != null ? (isCopy ? (row.getTax().isActivated() ? row.getTax().getID() : "") : row.getTax().getID()) : "None");//ERP-38656
                    if (purchaseOrder.getTax() != null) {
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, purchaseOrder.getOrderDate(), purchaseOrder.getTax().getID());
                        taxPercent = (Double) perresult.getEntityList().get(0);
                    }
                    obj.put("taxpercent", taxPercent);

                    // ## Get Custom Field Data 
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                    ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                    Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                    Detailfilter_params.add(row.getID());
                    invDetailRequestParams.put("filter_names", Detailfilter_names);
                    invDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresult = accGoodsReceiptobj.getExpensePOCustomData(invDetailRequestParams);
                    if (idcustresult.getEntityList().size() > 0) {
                        ExpensePODetailCustomData jeDetailCustom = (ExpensePODetailCustomData) idcustresult.getEntityList().get(0);
                        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                        DateFormat userdf=requestParams.containsKey(Constants.userdf)?(DateFormat)requestParams.get(Constants.userdf):null;                    
                        if (jeDetailCustom != null) {
                            JSONObject params = new JSONObject();
                            params.put(Constants.isExport, isExport);
                            params.put("isForReport", isForReport);
                            params.put("userdf",userdf);
                            if (isForLinking) {
                                params.put("isLink", true);
                                int moduleId = Constants.Acc_Vendor_Invoice_ModuleId;
                                params.put("companyid", companyid);
                                params.put("linkModuleId", moduleId);
                            }
                            fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                        }
                    }
                    jArr.put(obj);
                }
            }
        }
        return jArr;
    }
    
    public String getdefaultBatchJson(Product product, String documentid, double quantity) throws JSONException {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        if (product.getLocation() != null && !StringUtil.isNullOrEmpty(product.getLocation().getId())) {
            jobj.put("location", product.getLocation().getId());
        }
        if (product.getWarehouse() != null && !StringUtil.isNullOrEmpty(product.getWarehouse().getId())) {
            jobj.put("warehouse", product.getWarehouse().getId());
        }
        jobj.put("documentid", "");
        if (!StringUtil.isNullOrEmpty(product.getID())) {
            jobj.put("productid", product.getID());
        }
        jobj.put("quantity", quantity);
        jobj.put("purchasebatchid", "");
        jarr.put(jobj);
        return jarr.toString();
    }
    @Override
    public String getTimeIntervalForProduct(String inouttime) throws ParseException, java.text.ParseException {
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
        DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
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

    private void setCustomColumnValuesForProduct(PurchaseOrderDetailProductCustomData poDetailsProductCustomData, HashMap<String, Integer> fieldMap, Map<String, String> replaceFieldMap,
            Map<String, Object> variableMap, JSONObject params) {
        for (Map.Entry<String, Integer> field : fieldMap.entrySet()) {
            boolean isForReport = params.optBoolean("isForReport", false);
            boolean isExport = params.optBoolean("isExport", false);            
            Integer colnumber = field.getValue();
            if (colnumber > 0) { // colnumber will be 0 if key is part of reference map
                Integer isref = fieldMap.get(field.getKey() + "#" + colnumber);// added '#' while creating map collection for custom fields.
                // Without this change, it creates problem if two custom columns having name like XYZ and XYZ1
                String coldata = null;
                if (isref != null) {
                    try {
                        if (poDetailsProductCustomData != null) {
                            coldata = poDetailsProductCustomData.getCol(colnumber);
                        }
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
                                    DateFormat userdf=params.has(Constants.userdf)?(DateFormat)params.get(Constants.userdf):null;
                                    DateFormat defaultDateFormat = new SimpleDateFormat(Constants.MMMMdyyyy);
                                    Date dateFromDB = null;
                                    try {
                                        dateFromDB = defaultDateFormat.parse(coldata);
                                        coldata = userdf.format(dateFromDB);
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
     
   @Override 
    public double getPurchaseOrderDetailStatusForGRO(PurchaseOrderDetail pod) throws ServiceException {
        double result = pod.getQuantity();
        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), pod.getCompany().getCompanyID());
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
        KwlReturnObject idresult = accGoodsReceiptobj.getGROIDFromPOD(pod.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0;
        while (ite1.hasNext()) {
            GoodsReceiptOrderDetails ge = (GoodsReceiptOrderDetails) ite1.next();
            qua += ge.getInventory().getQuantity();
        }
        result = pod.getQuantity() - qua;
        return result;
    }
@Override 
    public double getPurchaseOrderDetailStatus(PurchaseOrderDetail pod) throws ServiceException {
        double result = pod.getQuantity();
        try {
            double returnQty = 0;
            KwlReturnObject grresult = accGoodsReceiptobj.getReceiptDFromPOD(pod.getID());
            List<GoodsReceiptDetail> grdList = grresult.getEntityList();
            double qua = 0;
            grresult = accGoodsReceiptobj.getSGEtDFromPOD(pod.getID());
            List<SecurityGateDetails> sgdList = grresult.getEntityList();
            JSONObject requestParams = new JSONObject();
            requestParams.put("purchaseOrderDetailsID", pod.getID());
            requestParams.put(Constants.companyid, pod.getCompany().getCompanyID());
            /**
             * check if POD is available in PR and get Total Return Quantity.
             * this related to getPrchaseOrderRows.
             */
            returnQty = accPurchaseOrderobj.checkPurchaseReturnAndgetReturnQuantity(requestParams);

            for (GoodsReceiptDetail ge : grdList) {
                qua += ge.getInventory().getQuantity();
            }

            for (SecurityGateDetails ge : sgdList) {
                qua += ge.getQuantity();
            }
            qua -= returnQty; // if PO has Purchase Return then we need to minus return quantity. (ERP-33449)
            result = pod.getQuantity() - qua;
        } catch (ServiceException | JSONException ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public double getPurchaseOrderBalanceQuantity(PurchaseOrderDetail purchaseOrderDetail) {
        double result = 0;
        try {


            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), purchaseOrderDetail.getPurchaseOrder().getCompany().getCompanyID());
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

            boolean fullInv = false;
            double qua = 0;
            double quantPartTt = purchaseOrderDetail.getQuantity() * 100;
            double quantPartTtInv = 0.0;
            if (pref.isWithInvUpdate()) { //In Trading Flow                 
                KwlReturnObject grodresult = accGoodsReceiptobj.getGRODetails(purchaseOrderDetail.getID(), pref.getCompany().getCompanyID());
                List list = grodresult.getEntityList();
                if (list.size() > 0) {
                    Iterator ite1 = list.iterator();
                    while (ite1.hasNext()) {
                        String orderid = (String) ite1.next();
                        KwlReturnObject res = accountingHandlerDAOobj.getObject(GoodsReceiptOrderDetails.class.getName(), orderid);
                        GoodsReceiptOrderDetails goodsReceiptOrderDetails = (GoodsReceiptOrderDetails) res.getEntityList().get(0);
                        fullInv = true;
                        qua += goodsReceiptOrderDetails.getDeliveredQuantity();
                    }
                }
            } else { //In Non Trading Flow 

                KwlReturnObject grdresult = accGoodsReceiptobj.getGRDetails(purchaseOrderDetail.getID());
                List list = grdresult.getEntityList();
                Iterator ite1 = list.iterator();
                while (ite1.hasNext()) {
                    GoodsReceiptDetail grd = (GoodsReceiptDetail) ite1.next();
                    fullInv = true;
                    qua += grd.getInventory().isInvrecord() ? grd.getInventory().getQuantity() : grd.getInventory().getActquantity();
                }
            }
            if (fullInv) {
                result = qua;

            } else if (quantPartTt > quantPartTtInv) {
                result = quantPartTtInv;
            }
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    @Override
     public HashMap<String, Object> getPurchaseOrderMap (JSONObject paramObj) throws SessionExpiredException {
//        Map requestParams = AccountingManager.getGlobalParams(request);
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        
        requestParams.put(Constants.companyKey, paramObj.optString(Constants.companyKey));
        requestParams.put(Constants.globalCurrencyKey, paramObj.optString(Constants.globalCurrencyKey));
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        requestParams.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(paramObj.optString(Constants.userdateformat)));
        
        //checking filetype to print all records for csv,print & pdf
        if (StringUtil.isNullOrEmpty(paramObj.optString("filetype"))) {
            requestParams.put(Constants.start, paramObj.optString(Constants.start));
            requestParams.put(Constants.limit, paramObj.optString(Constants.limit));
        }
        requestParams.put(Constants.ss, paramObj.optString(Constants.ss));
        requestParams.put(Constants.REQ_costCenterId,paramObj.optString(Constants.REQ_costCenterId));
        requestParams.put(Constants.REQ_vendorId,paramObj.optString(Constants.REQ_vendorId));
        requestParams.put(Constants.REQ_startdate ,paramObj.optString(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate ,paramObj.optString(Constants.REQ_enddate));
        requestParams.put(InvoiceConstants.newvendorid, paramObj.optString(InvoiceConstants.newvendorid));
        requestParams.put(InvoiceConstants.productid, paramObj.optString(InvoiceConstants.productid));
        requestParams.put(InvoiceConstants.productCategoryid, paramObj.optString(InvoiceConstants.productCategoryid));
        requestParams.put("doflag", paramObj.optString("doflag")!=null?true:false);
        boolean closeflag = StringUtil.isNullOrEmpty(paramObj.optString("closeflag")) ? false : Boolean.parseBoolean(paramObj.optString("closeflag"));
        requestParams.put("closeflag", closeflag);
        requestParams.put("deleted", paramObj.optString("deleted"));
        requestParams.put("nondeleted", paramObj.optString("nondeleted"));
        requestParams.put(Constants.ValidFlag, paramObj.optString(Constants.ValidFlag));
        requestParams.put(Constants.BillDate ,paramObj.optString(Constants.BillDate));
        requestParams.put("pendingapproval" ,StringUtil.isNullOrEmpty(paramObj.optString("pendingapproval")) ? false : Boolean.parseBoolean(paramObj.optString("pendingapproval")));
        requestParams.put("istemplate" ,StringUtil.isNullOrEmpty(paramObj.optString("istemplate"))? 0 :Integer.parseInt(paramObj.optString("istemplate")));
        requestParams.put(Constants.MARKED_FAVOURITE, paramObj.optString(Constants.MARKED_FAVOURITE));
        requestParams.put("currencyid",paramObj.optString("currencyid"));
        requestParams.put("exceptFlagINV" ,paramObj.optString("exceptFlagINV"));
        requestParams.put("exceptFlagORD" ,paramObj.optString("exceptFlagORD"));
        requestParams.put("linkFlagInPO" ,paramObj.optString("linkFlagInPO"));
        requestParams.put("linkFlagInGR" ,paramObj.optString("linkFlagInGR"));
        requestParams.put("linkflag" ,paramObj.optString("linkflag"));
        requestParams.put("prpolinkflag" ,paramObj.optString("prpolinkflag"));
        requestParams.put(Constants.Acc_Search_Json ,paramObj.optString(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria ,paramObj.optString(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid ,paramObj.optString(Constants.moduleid));
        requestParams.put("currencyfilterfortrans", StringUtil.isNullOrEmpty(paramObj.optString("currencyfilterfortrans")) ? "" : paramObj.optString("currencyfilterfortrans"));
        requestParams.put("isOpeningBalanceOrder", StringUtil.isNullOrEmpty(paramObj.optString("isOpeningBalanceOrder")) ? false : Boolean.parseBoolean(paramObj.optString("isOpeningBalanceOrder")));
        requestParams.put(CCConstants.REQ_vendorId,paramObj.optString(CCConstants.REQ_vendorId));
        requestParams.put(Constants.customerCategoryid, paramObj.optString(Constants.customerCategoryid));
        requestParams.put("billId",paramObj.optString("billid"));
        requestParams.put("blockedDocuments",paramObj.optString("blockedDocuments"));
        requestParams.put("unblockedDocuments",paramObj.optString("unblockedDocuments"));
        if(StringUtil.isNullOrEmpty(paramObj.optString("includingGSTFilter"))){
            requestParams.put("includingGSTFilter",Boolean.parseBoolean(paramObj.optString("includingGSTFilter")));
        }
        requestParams.put("isConsignment", StringUtil.isNullOrEmpty(paramObj.optString("isConsignment")) ? false : Boolean.parseBoolean(paramObj.optString("isConsignment")));
        requestParams.put(Constants.isDraft, StringUtil.isNullOrEmpty(paramObj.optString("isDraft")) ? false : Boolean.parseBoolean(paramObj.optString("isDraft")));
        requestParams.put("isMRPJOBWORKIN", StringUtil.isNullOrEmpty(paramObj.optString("isMRPJOBWORKIN")) ? false : Boolean.parseBoolean(paramObj.optString("isMRPJOBWORKIN")));
        requestParams.put("isMRPJOBWORKOUT",StringUtil.isNullOrEmpty(paramObj.optString("isMRPJOBWORKOUT"))? false : Boolean.parseBoolean(paramObj.optString("isMRPJOBWORKOUT")));
        requestParams.put("isFixedAsset", StringUtil.isNullOrEmpty(paramObj.optString("isFixedAsset")) ? false : Boolean.parseBoolean(paramObj.optString("isFixedAsset")));
        requestParams.put("isShowAddress", StringUtil.isNullOrEmpty(paramObj.optString("isShowAddress")) ? false : Boolean.parseBoolean(paramObj.optString("isShowAddress")));
        if(!StringUtil.isNullOrEmpty(paramObj.optString("linknumber"))){
            requestParams.put("linknumber", paramObj.optString("linknumber"));
        }
        if (!StringUtil.isNullOrEmpty(paramObj.optString(Constants.browsertz))) {
            requestParams.put("browsertz", paramObj.optString(Constants.browsertz));
        }
        return requestParams;
    }
  
    @Override
    public JSONArray getQuotationsJson(JSONObject paramObj, List list, JSONArray jArr) throws ServiceException {
        try {
            HashMap<String, Object> requestParams = getPurchaseOrderMap(paramObj);
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get("companyid");
            
            boolean poflag = false;
            if (!StringUtil.isNullOrEmpty(paramObj.optString("sopolinkflag")) && paramObj.optString("sopolinkflag") != null) {
                poflag = Boolean.FALSE.parseBoolean(paramObj.optString("sopolinkflag"));
            }
            String ss = "";
            DateFormat userdf = (DateFormat) requestParams.get(Constants.userdf);
            boolean isFA_VQtoPI = (paramObj.optString("isFA_VQtoPI",null) != null) ? Boolean.parseBoolean(paramObj.optString("isFA_VQtoPI")) : false;
            boolean isVQLinkInCQ = Boolean.FALSE.parseBoolean(paramObj.optString("isVQLinkInCQ"));    //// Check wether VQ is link with CQ
            boolean isPOfromVQ = Boolean.FALSE.parseBoolean(paramObj.optString("isPOfromVQ"));    //// Check wether VQ is link with PO
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            boolean isFixedAsset = (paramObj.optString("isFixedAsset",null) != null) ? Boolean.parseBoolean(paramObj.optString("isFixedAsset")) : false;
            
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            
            // Get global-level CUSTOMFIELDS for Vendor Quotation
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, !isFixedAsset?Constants.Acc_Vendor_Quotation_ModuleId:Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), paramObj.optString(Constants.companyKey));
            ExtraCompanyPreferences extraCompanyPref = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            boolean isLineLevelTermFlag = false;//To Check Whether LinelevelTerms are applicable or not.
            if(extraCompanyPref != null && extraCompanyPref.getLineLevelTermFlag()==1){
                isLineLevelTermFlag = true;//If LineLevelTerms are applicable, then update the flag.
            }
            Iterator itr = list.iterator();
            String compids[] = Constants.Companyids_Chkl_And_Marubishi.split(",");
            boolean isFromChklorMarubishi = false;
            for (int cnt = 0; cnt < compids.length; cnt++) {
                String compid = compids[cnt];
                if (compid.equalsIgnoreCase(companyid)) {
                    isFromChklorMarubishi = true;
                }
            }
            while (itr.hasNext()) {
                String vid = (String) itr.next();
                if (!StringUtil.isNullOrEmpty(vid)) {
                    KwlReturnObject reqResult = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), vid);
                    VendorQuotation purchaseOrder = (VendorQuotation) reqResult.getEntityList().get(0);
                    Set<VendorQuotationDetail> rows = purchaseOrder.getRows();
                    String status = "";
                    boolean addFlag = true;
                    KWLCurrency currency = null;
                    Vendor vendor = purchaseOrder.getVendor();
                    if (purchaseOrder.getCurrency() != null) {
                        currency = purchaseOrder.getCurrency();
                    } else {
                        currency = vendor.getAccount().getCurrency() == null ? kwlcurrency : vendor.getAccount().getCurrency();
                    }

                    //Get Attachment details
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", purchaseOrder.getID());
                    hashMap.put("companyid", purchaseOrder.getCompany().getCompanyID());
                    KwlReturnObject object = accInvoiceDAOobj.getinvoiceDocuments(hashMap);
                    int attachemntcount = object.getRecordTotalCount();

                    JSONObject obj = new JSONObject();
                    obj.put("billid", purchaseOrder.getID());
                    obj.put("sequenceformatid", purchaseOrder.getSeqformat()!=null ? purchaseOrder.getSeqformat().getID():null);
                    obj.put("companyid", purchaseOrder.getCompany().getCompanyID());
                    obj.put("companyname", purchaseOrder.getCompany().getCompanyName());
                    obj.put("externalcurrencyrate", purchaseOrder.getExternalCurrencyRate());
                    obj.put("personid", vendor.getID());
                    obj.put("aliasname", vendor.getAliasname());
                    obj.put(Constants.PERSONCODE, vendor.getAcccode());
                    obj.put("personemail", vendor == null ? "" : vendor.getEmail());
                    obj.put("billno", purchaseOrder.getQuotationNumber());
                    obj.put("status", purchaseOrder.isIsOpen()?"Open":"Closed");
                    obj.put(Constants.HAS_ACCESS, vendor.isActivate());
                    obj.put("duedate", authHandler.getDateOnlyFormat().format(purchaseOrder.getDueDate()));
                    obj.put("billtoaddress", purchaseOrder.getBillingShippingAddresses()==null? "":CommonFunctions.getBillingShippingAddress(purchaseOrder.getBillingShippingAddresses(), true));
                    obj.put("shiptoaddress", purchaseOrder.getBillingShippingAddresses()==null? "":CommonFunctions.getBillingShippingAddress(purchaseOrder.getBillingShippingAddresses(), false));
                    obj.put("createdby", purchaseOrder.getCreatedby() == null ? "" : StringUtil.getFullName(purchaseOrder.getCreatedby()));
                    obj.put("dateinuserformat", authHandler.getUserDateFormatterWithoutTimeZone(paramObj.optString(Constants.userdateformat)).format(purchaseOrder.getQuotationDate()));
                    obj.put("shipdateinuserformat", purchaseOrder.getShipdate() == null ? "" : authHandler.getUserDateFormatterWithoutTimeZone(paramObj.optString(Constants.userdateformat)).format(purchaseOrder.getShipdate()));
                    obj.put("date", authHandler.getDateOnlyFormat().format(purchaseOrder.getQuotationDate()));
                    obj.put("shipdate", purchaseOrder.getShipdate() == null ? "" : authHandler.getDateOnlyFormat().format(purchaseOrder.getShipdate()));
                    obj.put("validdate", purchaseOrder.getValiddate() == null ? "" : authHandler.getDateOnlyFormat().format(purchaseOrder.getValiddate()));
                    obj.put("shipvia", purchaseOrder.getShipvia() == null ? "" : purchaseOrder.getShipvia());
                    obj.put("fob", purchaseOrder.getFob() == null ? "" : purchaseOrder.getFob());
                    obj.put("archieve", purchaseOrder.getArchieve());
                    obj.put("isfavourite", purchaseOrder.isFavourite());
                    obj.put("isprinted", purchaseOrder.isPrinted());
                    obj.put("formtypeid", purchaseOrder.getFormtype());
                    obj.put("gtaapplicable", purchaseOrder.isGtaapplicable());
                    obj.put("gstapplicable", purchaseOrder.isIsIndGSTApplied());
                    obj.put("isInterstateParty", purchaseOrder.getVendor().isInterstateparty());
                    obj.put("termdetails", getTermDetails(purchaseOrder.getID(), false));
                    if (purchaseOrder.getTermsincludegst() != null) {
                        obj.put(Constants.termsincludegst, purchaseOrder.getTermsincludegst());
                    }
//                    MasterItem gstRegistrationType = vendor != null ? vendor.getGSTRegistrationType() : null;
//                    if (gstRegistrationType != null && gstRegistrationType.getDefaultMasterItem() != null) {
//                        obj.put("GSTINRegTypeDefaultMstrID", gstRegistrationType.getDefaultMasterItem().getID());
//                    }
                    obj.put(Constants.IsRoundingAdjustmentApplied, purchaseOrder.isIsRoundingAdjustmentApplied());
                    
                    /*
                     * To check vendor quotation is linked with any other or not
                     */
                    JSONObject obj1 = new JSONObject();
                    KwlReturnObject linkRresult = accLinkDataDao.checkEntryForTransactionInLinkingTableForForwardReference(Constants.Acc_VendorQuotation_modulename, purchaseOrder.getID());
                    list = linkRresult.getEntityList();
                    if (list != null && !list.isEmpty()) {
                        obj.put(Constants.IS_LINKED_TRANSACTION, true);
                    } else {
                        obj.put(Constants.IS_LINKED_TRANSACTION, false);
                    }
                    /**
                     * Put GST document history.
                     */
                    if (purchaseOrder.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                        obj.put("refdocid", purchaseOrder.getID());
                        fieldDataManagercntrl.getGSTDocumentHistory(obj);
                        /**
                         * Put Merchant Exporter Check
                         */
                        obj.put(Constants.isMerchantExporter, purchaseOrder.isIsMerchantExporter());

                    }
//                    obj.put("termamount", CommonFunctions.getTotalTermsAmount(getTermDetails(purchaseOrder.getID(), false)));
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
                    obj=AccountingAddressManager.getTransactionAddressJSON(obj, purchaseOrder.getBillingShippingAddresses(), true);
                    obj.put("attachment", attachemntcount);
                    obj.put("createdby", purchaseOrder.getCreatedby() == null ? "" : StringUtil.getFullName(purchaseOrder.getCreatedby()));
                    String approvalStatus = "";
                    if (purchaseOrder.getApprovestatuslevel() < 0) {
                        approvalStatus = "Rejected";
                    } else if (purchaseOrder.getApprovestatuslevel() < 11) {
                        String ruleid = "", userRoleName = "";
                        HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                        qdDataMap.put("companyid", companyid);
                        qdDataMap.put("level", purchaseOrder.getApprovestatuslevel());
                        qdDataMap.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
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
                        if (!StringUtil.isNullOrEmpty(userRoleName)) {
                            userRoleName = userRoleName.substring(0, userRoleName.length() - 1);
                        }
                        approvalStatus = "Pending Approval by " + userRoleName + " at Level - " + purchaseOrder.getApprovestatuslevel();
                    } else {
                        approvalStatus = "Approved";
                    }
                    obj.put("approvalstatusinfo", approvalStatus);

                    boolean incProTax = false;
                    String taxname="";
                    Iterator itrRow = rows.iterator();
                    double amount = 0, totalDiscount = 0, discountPrice = 0;
                    double rowTaxAmt = 0d, rowDiscountAmt = 0d, rowOtherTermNonTaxableAmount = 0d;
                    double subtotal = 0d;
                    double productTotalAmount = 0d;
                    Set<String> uniqueProductTaxList = new HashSet<String>();
                    while (itrRow.hasNext()) {
                        VendorQuotationDetail sod = (VendorQuotationDetail) itrRow.next();
                        if (sod.getTax() != null) {
                            incProTax = true;
                            uniqueProductTaxList.add(sod.getTax().getID());
                            taxname += sod.getTax().getName() + ", ";
                        }
                       double rowsubtotal = 0d;
                        double vqrate = purchaseOrder.isGstIncluded() ? authHandler.roundUnitPrice(sod.getRateincludegst(), companyid) : authHandler.roundUnitPrice(sod.getRate(), companyid);
                        double quantity = authHandler.roundQuantity(sod.getQuantity(), companyid);
                        rowsubtotal = vqrate * quantity;
                        productTotalAmount += authHandler.round(rowsubtotal, companyid);

                        double quotationPrice = authHandler.round(quantity * vqrate, companyid);
                        double discountVQD = authHandler.round(sod.getDiscount(), companyid);
                        totalDiscount+=discountVQD;

                        if (sod.getDiscountispercent() == 1) {
                            discountPrice = (quotationPrice) - authHandler.round((quotationPrice * discountVQD / 100), companyid);
                            rowDiscountAmt += authHandler.round((quotationPrice * discountVQD/100), companyid);
                        } else {
                            discountPrice = quotationPrice - discountVQD;
                            rowDiscountAmt += discountVQD;
                        }
                        rowDiscountAmt = authHandler.round(rowDiscountAmt, companyid);
                        rowTaxAmt += sod.getRowTaxAmount();
                        amount += discountPrice + (purchaseOrder.isGstIncluded() ? 0 : authHandler.round(sod.getRowTaxAmount(), companyid));//amount += discountPrice + (discountPrice * rowTaxPercent/100);
                        if (isLineLevelTermFlag) {
                            rowTaxAmt += sod.getRowTermAmount();
                            // Append OtherTermNonTaxableAmount for rach row.
                            rowOtherTermNonTaxableAmount += sod.getOtherTermNonTaxableAmount();
                            amount += authHandler.round(sod.getOtherTermNonTaxableAmount(), companyid);
                                 /**
                                 * ERP-34717
                                 * If GST Include, no need to add Tax Amount. 
                                 * Amount is already with tax 
                                 */
                            amount +=  purchaseOrder.isGstIncluded() ? 0 : authHandler.round(sod.getRowTermAmount(), companyid);//amount += discountPrice + (discountPrice * rowTaxPercent/100);
                        }//For Live level Terms as Tax   
                      }
                     obj.put("productTotalAmount", productTotalAmount);
                    double discountVQ = authHandler.round(purchaseOrder.getDiscount(), companyid);
                    obj.put("includeprotax", incProTax);
                    if (purchaseOrder.getModifiedby() != null) {
                        obj.put("lasteditedby", StringUtil.getFullName(purchaseOrder.getModifiedby()));
                    }
                    if (discountVQ != 0) {
                        if (purchaseOrder.isPerDiscount()) {
                            totalDiscount = authHandler.round(amount * discountVQ / 100, companyid);
                            amount = amount - totalDiscount;
                        } else {
                            amount = amount - discountVQ;
                            totalDiscount = discountVQ;
                        }
                        obj.put("discounttotal", discountVQ);
                    } else {
                        obj.put("discounttotal", 0);
                    }
                    obj.put("discount", rowDiscountAmt);
                    obj.put("discountispertotal", purchaseOrder.isPerDiscount());
                    obj.put("ispercentdiscount", purchaseOrder.isPerDiscount());
                    obj.put("discountinbase", purchaseOrder.getDiscountinbase());
                    
                    boolean isApplyTaxToTerms=purchaseOrder.isApplyTaxToTerms();
                    obj.put("isapplytaxtoterms", isApplyTaxToTerms);
                    
                    double totalTermAmount = 0;
                    double totalTermTaxAmount=0;
                    double taxableTermamount = 0;
                    List vqTermMapList = kwlCommonTablesDAOObj.getSummationOfTermAmtAndTermTaxAmt(Constants.vendorquotationtermmap, purchaseOrder.getID());
                    if(vqTermMapList != null && !vqTermMapList.isEmpty()){
                        Iterator termItr = vqTermMapList.iterator();
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
                            if (purchaseOrder.isGstIncluded()) {
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
                    obj.put("amount", amount);
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("currencycode", currency.getCurrencyCode()==null?"":currency.getCurrencyCode());
                    obj.put("taxid", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getID());
                     if (incProTax) {
                        obj.put("taxname", taxname.substring(0, (taxname.length() > 1 ? taxname.length() - 2 : taxname.length())));
                    } else {
                        obj.put("taxname", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getName());
                    }
                    double taxPercent = 0;
                    if (purchaseOrder.getTax() != null) {
                        requestParams.put("transactiondate", purchaseOrder.getQuotationDate());
                        requestParams.put("taxid", purchaseOrder.getTax().getID());
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
                    double ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round((orderAmount + taxableTermamount) * taxPercent / 100, companyid));
                    double taxAmt = rowTaxAmt + ordertaxamount;// either row level tax will be avvailable or invoice level
                    obj.put("amountbeforegst", amount-rowTaxAmt); // Amount before both kind of tax row level or transaction level
                    
                    if (purchaseOrder.isIsRoundingAdjustmentApplied()) {
                        double totalAmountwithtax = 0;
                        totalAmountwithtax = ordertaxamount + authHandler.round(amount + totalTermAmount + totalTermTaxAmount, companyid) + purchaseOrder.getRoundingadjustmentamount();
                        if (!currencyid.equals(currency.getCurrencyID())) {//If not base curreny then need to convert
                            KwlReturnObject bAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalAmountwithtax, currency.getCurrencyID(), purchaseOrder.getQuotationDate(), purchaseOrder.getExternalCurrencyRate());
                            totalAmountwithtax = authHandler.round((Double) bAmtTax.getEntityList().get(0), companyid);
                        }
                        obj.put("amountinbase", totalAmountwithtax);
                    } else {
                        KwlReturnObject bAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ordertaxamount, currency.getCurrencyID(), purchaseOrder.getQuotationDate(), purchaseOrder.getExternalCurrencyRate());
                        double ordertaxamountBase = authHandler.round((Double) bAmtTax.getEntityList().get(0), companyid);
                        double totalQuotationAmtInBase =0d;
                        totalQuotationAmtInBase = amount + totalTermAmount + totalTermTaxAmount;
                        KwlReturnObject tempBaseAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, authHandler.round(totalQuotationAmtInBase, companyid), currency.getCurrencyID(), purchaseOrder.getQuotationDate(), purchaseOrder.getExternalCurrencyRate());
                        totalQuotationAmtInBase = authHandler.round((Double) tempBaseAmt.getEntityList().get(0), companyid);
                        obj.put("amountinbase", totalQuotationAmtInBase + ordertaxamountBase);
                    }                    
                    obj.put("taxpercent", taxPercent);
                    obj.put("taxamount",taxAmt + totalTermTaxAmount);// Tax Amount
                    
                    if(purchaseOrder.isGstIncluded()) {
                        subtotal = authHandler.round(productTotalAmount - rowDiscountAmt-(taxAmt), companyid);
                    } else {
                        subtotal = authHandler.round(productTotalAmount - rowDiscountAmt, companyid);
                    }
                    obj.put("amountBeforeTax", authHandler.formattingDecimalForAmount((subtotal+totalTermAmount),companyid));
                    obj.put("subtotal", subtotal);
                    if (isLineLevelTermFlag) {
                        // If LineLevelTerm is applicable then add the value in JSON Object.
                        obj.put("OtherTermNonTaxableAmount", rowOtherTermNonTaxableAmount);
                    }
                    amount+=totalTermAmount+totalTermTaxAmount;
                    orderAmount+=totalTermAmount + totalTermTaxAmount;
                    obj.put("orderamount", orderAmount);
                    double totalAmt=orderAmount + ordertaxamount;
                    if (purchaseOrder.isIsRoundingAdjustmentApplied()) {
                        totalAmt += purchaseOrder.getRoundingadjustmentamount();
                    }
                    obj.put("orderamountwithTax",totalAmt);
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("personname", vendor.getName());
                    obj.put("personcode", vendor.getAcccode()==null?"":vendor.getAcccode());
                    obj.put("memo", purchaseOrder.getMemo());
                    obj.put("posttext", purchaseOrder.getPostText());
                    obj.put(Constants.SUPPLIERINVOICENO, purchaseOrder.getSupplierInvoiceNo() != null ? purchaseOrder.getSupplierInvoiceNo() : "");
                    
                    boolean isExport = (paramObj.optString("isExport",null) == null) ? false : true;
                    boolean linkFlag = StringUtil.isNullOrEmpty(paramObj.optString("linkFlagInPO")) ? false : true;
                    boolean linkFlagInGR = StringUtil.isNullOrEmpty(paramObj.optString("linkFlagInGR")) ? false : true;
                    KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(VendorQuotationCustomData.class.getName(), purchaseOrder.getID());
                    if (custumObjresult.getEntityList().size() > 0) {
                        DateFormat df = (DateFormat) requestParams.get("df");
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        VendorQuotationCustomData vendorQuotationCustomData = (VendorQuotationCustomData) custumObjresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(vendorQuotationCustomData, FieldMap, replaceFieldMap, variableMap);
                        if (vendorQuotationCustomData != null) {
                            JSONObject params = new JSONObject();
                            params.put("isExport", isExport);
                            params.put("userdf", userdf);
                            if (poflag || linkFlagInGR || isVQLinkInCQ||isPOfromVQ || isFA_VQtoPI) {
                                int moduleId = poflag ? Constants.Acc_Purchase_Order_ModuleId : linkFlagInGR?Constants.Acc_Vendor_Invoice_ModuleId:isFixedAsset?Constants.Acc_FixedAssets_Purchase_Order_ModuleId:Constants.Acc_Customer_Quotation_ModuleId;
                                if(isFA_VQtoPI){
                                    moduleId = Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId;
                                }
                                params.put("linkModuleId", moduleId);
                                params.put("isLink", true);
                                params.put("companyid", companyid);
                                params.put("customcolumn", 0);
                            }
                            if (!StringUtil.isNullOrEmpty(paramObj.optString(Constants.browsertz))) {
                                params.put(Constants.browsertz, paramObj.optString(Constants.browsertz));
                            }
                            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                        }
                    }
                    KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                        Company company = (Company) cmp.getEntityList().get(0);
                        int countryid = company.getCountry() != null ? Integer.parseInt(company.getCountry().getID()) : 0;
                        if (Constants.indian_country_id == countryid && isFixedAsset) {
                            ExtraCompanyPreferences extraCompanyPreferences = null;
                            List<ExciseDetailsAssets> ed = null;
                            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                            extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
                            if (extraCompanyPreferences.isExciseApplicable()) {
                                KwlReturnObject exciseDetails = accGoodsReceiptobj.getExciseDetailsAssetQuotation(purchaseOrder.getID());
                                    ed = exciseDetails.getEntityList();
                            }
                            if (extraCompanyPreferences.isExciseApplicable() && ed.size() > 0) {
                                obj.put("assetExciseid", ed.get(0).getId());
                                obj.put("suppliers", ed.get(0).getSupplier());
                                obj.put("supplierTINSalesTAXNo", ed.get(0).getSupplierTINSalesTaxNo());
                                obj.put("supplierExciseRegnNo", ed.get(0).getSupplierExciseRegnNo());
                                obj.put("cstnumber", ed.get(0).getCstnumber());
                                obj.put("supplierRange", ed.get(0).getSupplierRange());
                                obj.put("supplierCommissionerate", ed.get(0).getSupplierCommissioneRate());
                                obj.put("supplierAddress", ed.get(0).getSupplierAddress());
                                obj.put("supplierImporterExporterCode", ed.get(0).getSupplierImporterExporterCode());
                                obj.put("supplierDivision", ed.get(0).getSupplierDivision());
                                obj.put("manufacturername", ed.get(0).getManufacturerName());
                                obj.put("manufacturerExciseRegnNo", ed.get(0).getManufacturerExciseregnNo());
                                obj.put("manufacturerRange", ed.get(0).getManufacturerRange());
                                obj.put("manufacturerCommissionerate", ed.get(0).getManufacturerCommissionerate());
                                obj.put("manufacturerDivision", ed.get(0).getManufacturerDivision());
                                obj.put("manufacturerAddress", ed.get(0).getManufacturerAddress());
                                obj.put("manufacturerImporterExporterCode", ed.get(0).getManufacturerImporterexporterCode());
                                obj.put("InvoicenoManuFacture", ed.get(0).getInvoicenoManufacture());
                                obj.put("InvoiceDateManuFacture", ed.get(0).getInvoiceDateManufacture());
                                obj.put("supplierState", ed.get(0).getSupplierstate());
                               
                            }
                        }                  
                    if (addFlag) {
                        jArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getQuotationsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

   @Override 
    public double getQuotationDetailStatusPO(VendorQuotationDetail quod) throws ServiceException {
        double result = quod.getQuantity();
        KwlReturnObject idresult = accPurchaseOrderobj.getPODFromVQD(quod.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0.0;
        while (ite1.hasNext()) {
            PurchaseOrderDetail pod = (PurchaseOrderDetail) ite1.next();
            qua += pod.getQuantity();

        }
        result = quod.getQuantity() - qua;
        return result;
    }
@Override 
    public double getQuotationDetailStatusGR(VendorQuotationDetail quod) throws ServiceException {
        double result = quod.getQuantity();
        KwlReturnObject idresult = accPurchaseOrderobj.getGRDFromVQD(quod.getID());
        List<GoodsReceiptDetail> list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0.0;
        for (GoodsReceiptDetail grd:list) {
            qua += grd.getInventory().getQuantity();
        }
        result = quod.getQuantity() - qua;
        return result;
    }
@Override 
    public JSONObject getQuotationRows(HttpServletRequest request) throws SessionExpiredException, ServiceException, ParseException {
        JSONObject jobj = new JSONObject();
        try {
            boolean linkingFlag = (StringUtil.isNullOrEmpty(request.getParameter("linkingFlag")))?false:Boolean.parseBoolean(request.getParameter("linkingFlag"));
            boolean isCopy = !StringUtil.isNullOrEmpty(request.getParameter("copyInvoice")) ? Boolean.parseBoolean(request.getParameter("copyInvoice")) : false;
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = (DateFormat) authHandler.getUserDateFormatterWithoutTimeZone(request);
            boolean isFixedAsset = Boolean.FALSE.parseBoolean(request.getParameter("isFixedAsset"));
            boolean FA_VQlinkToFA_PO = Boolean.FALSE.parseBoolean(request.getParameter("FA_VQlinkToFA_PO")); 
            boolean FA_VQlinkToPI = (request.getParameter("FA_VQlinkToPI") != null) ? Boolean.parseBoolean(request.getParameter("FA_VQlinkToPI")) : false;
            int moduleid = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("requestModuleid"))){
                moduleid = Integer.parseInt(request.getParameter("requestModuleid"));
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String gcurrencyid = sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            KwlReturnObject cap = accountingHandlerDAOobj.loadObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            
            KwlReturnObject extracapresult = accountingHandlerDAOobj.loadObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            Country country = extraCompanyPreferences.getCompany().getCountry();
             Map<String, Object> ProductFieldsRequestParams = new HashMap();
            ProductFieldsRequestParams.put("companyid", companyid);
            ProductFieldsRequestParams.put("moduleid", Constants.Acc_Vendor_Quotation_ModuleId);
            List masterFieldsResultList = CommonFunctions.getproductmastersFieldsToShowLineLevel(ProductFieldsRequestParams, accountingHandlerDAOobj);
           
            String closeflag = request.getParameter("closeflag");
            boolean poflag = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("sopolinkflag")) && request.getParameter("sopolinkflag") != null) {
                poflag = Boolean.FALSE.parseBoolean(request.getParameter("sopolinkflag"));
            }
            boolean vqtocqflag = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.VQCQFLAG)) && request.getParameter(Constants.VQCQFLAG) != null) {
                vqtocqflag = Boolean.FALSE.parseBoolean(request.getParameter(Constants.VQCQFLAG));
            }
            boolean isForLinking = Boolean.FALSE.parseBoolean(request.getParameter("isForLinking"));
            String[] sos=null;
            if(request.getAttribute("billid")!=null){
                String temp=request.getAttribute("billid").toString();
                sos = (String[]) temp.split(",");
            }else{
                sos = (String[]) request.getParameter("bills").split(",");
            }
            boolean customIsReport = false;
            boolean isExport = false;
            if (request.getAttribute("isExport") != null) {
                isExport = (boolean) request.getAttribute("isExport");
            }
            if (request.getParameter(Constants.dtype) != null) { 
                customIsReport = request.getParameter(Constants.dtype).equals(Constants.report);
            }
            int i = 0;
            JSONArray jArr = new JSONArray();
            double addobj = 1;
            String description="";

            // Get line-level CUSTOMFIELDS for Vendor Quotation
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), !isFixedAsset?Constants.Acc_Vendor_Quotation_ModuleId:Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId, 1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("vendorquotation.ID");
            order_by.add("srno");
            order_type.add("asc");
            soRequestParams.put("filter_names", filter_names);
            soRequestParams.put("filter_params", filter_params);
            soRequestParams.put("order_by", order_by);
            soRequestParams.put("order_type", order_type);

            while (sos != null && i < sos.length) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), sos[i]);
                VendorQuotation so = (VendorQuotation) result.getEntityList().get(0);
                KWLCurrency currency = null;

                if (so.getCurrency() != null) {
                    currency = so.getCurrency();
                } else {
                    currency = so.getVendor().getAccount().getCurrency() == null ? kwlcurrency : so.getVendor().getAccount().getCurrency();
                }
                filter_params.clear();
                filter_params.add(so.getID());
                KwlReturnObject podresult = accPurchaseOrderobj.getQuotationDetails(soRequestParams);
                List<VendorQuotationDetail> vqDetailsList=podresult.getEntityList();
                
                if (vqDetailsList.size() > 0) {
                    for (VendorQuotationDetail row:vqDetailsList) {
                        Product rowProduct = row.getProduct();
                        JSONObject obj = new JSONObject();
                        if (masterFieldsResultList != null) {
                            CommonFunctions.getterMethodForProductsData(row.getProduct(), masterFieldsResultList, obj);
                        }
                        obj.put("billid", so.getID());
                        obj.put("billno", so.getQuotationNumber());
                        obj.put("currencysymbol", currency.getSymbol());
                        obj.put("srno", row.getSrno());
                        obj.put("rowid", row.getID());
                        obj.put("productid", rowProduct.getID());
                        obj.put("purchasetaxId", rowProduct.getPurchasetaxid());
                        obj.put("salestaxId", rowProduct.getSalestaxid());
                        obj.put("hasAccess", rowProduct.isIsActive());
                        obj.put("productname", rowProduct.getName());
                        obj.put("unitname", row.getUom() != null ? row.getUom().getNameEmptyforNA() : rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getNameEmptyforNA());
                        obj.put("uomname", row.getUom() != null ? row.getUom().getNameEmptyforNA() : rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getNameEmptyforNA());
                        obj.put("baseuomid", rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getID());
                        obj.put("baseuomname", rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getNameEmptyforNA());
                        obj.put("multiuom", rowProduct.isMultiuom());
                        obj.put("maxorderingquantity", rowProduct.getMaxOrderingQuantity());
                        obj.put("minorderingquantity", rowProduct.getMinOrderingQuantity());

                        if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                            description = row.getDescription();
                        } else if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                            description = row.getProduct().getDescription();
                        } else {
                            description = "";
                        }

                        obj.put("desc", StringUtil.DecodeText(description));
                        obj.put("type", rowProduct.getProducttype() == null ? "" : rowProduct.getProducttype().getName());
                        obj.put("typeid", rowProduct.getProducttype() == null ? "" : rowProduct.getProducttype().getID());
                        obj.put("pid", rowProduct.getProductid());
                        obj.put("isAsset", rowProduct.isAsset());
                        obj.put("invstore", (StringUtil.isNullOrEmpty(row.getInvstoreid())) ? "" : row.getInvstoreid());
                        obj.put("invlocation", (StringUtil.isNullOrEmpty(row.getInvlocid())) ? "" : row.getInvlocid());
                        obj.put("memo", row.getRemark());

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

                            double pocountinselecteduom = accSalesOrderServiceDAOobj.getPOCount(orderParams);

                            double socountinselecteduom = accSalesOrderServiceDAOobj.getSOCount(orderParams);

                            obj.put("pocountinselecteduom", pocountinselecteduom);
                            obj.put("socountinselecteduom", socountinselecteduom);

                        }
                        if (extraCompanyPreferences != null && extraCompanyPreferences.getUomSchemaType() == Constants.PackagingUOM && rowProduct != null) {
                            Product product = rowProduct;
                            obj.put("caseuom", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUoM().getID() : "");
                            obj.put("caseuomvalue", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUomValue() : 1);
                            obj.put("inneruom", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUoM().getID() : "");
                            obj.put("inneruomvalue", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUomValue() : 1);
                            obj.put("stockuom", (product.getUnitOfMeasure() != null) ? product.getUnitOfMeasure().getID() : "");
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

                            obj.put("showquantity", StringUtil.DecodeText(row.getShowquantity() == null ? "" : row.getShowquantity()));

                        }
                        double rowTaxPercent = 0;
                        double rowTaxAmount = 0;
                        boolean isRowTaxApplicable = false;
                        if (row.getTax() != null) {
                            KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), so.getQuotationDate(), row.getTax().getID());
                            rowTaxPercent = (Double) perresult.getEntityList().get(0);
                            isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                            if (isRowTaxApplicable) {
                                rowTaxAmount = row.getRowTaxAmount();
                            }
                        }
                        /**
                         * If GST tax terms used then get tax amount Row term
                         * amount column
                         */
                        if (extraCompanyPreferences != null && extraCompanyPreferences.isIsNewGST()) {
                            rowTaxAmount = row.getRowTermAmount();
                        }
                        obj.put("prtaxpercent", rowTaxPercent);
                        obj.put("rowTaxAmount", rowTaxAmount);
                        obj.put("recTermAmount", row.getRowTermAmount());
                        obj.put("OtherTermNonTaxableAmount", row.getOtherTermNonTaxableAmount());
                        obj.put("prtaxid", row.getTax() == null ? "None" : row.getTax().getID());
                        obj.put(Constants.isUserModifiedTaxAmount, row.isIsUserModifiedTaxAmount());
//                        obj.put("prtaxid", row.getTax() != null ? (isCopy || linkingFlag ? (row.getTax().isActivated() ? row.getTax().getID() : "") : row.getTax().getID()) : "None");//ERP-38656
                        obj.put("priceSource", row.getPriceSource() != null ? row.getPriceSource() : "");
                        if (row.getPricingBandMasterid() != null) {
                            KwlReturnObject PricebandResult = accountingHandlerDAOobj.getObject(PricingBandMaster.class.getName(), row.getPricingBandMasterid());
                            PricingBandMaster pricingBandMaster = PricebandResult != null ? (PricingBandMaster) PricebandResult.getEntityList().get(0) : null;
                            obj.put("pricingbandmasterid", pricingBandMaster != null ? pricingBandMaster.getID() : "");
                            obj.put("pricingbandmastername", pricingBandMaster != null ? pricingBandMaster.getName() : "");
                        }
                        /**
                         * get the volume discount discount for the given
                         * product according its quantity.
                         */
                        HashMap<String, Object> pricingDiscountRequestParams = new HashMap<String, Object>();
                        pricingDiscountRequestParams.put("isPricePolicyUseDiscount", true);
                        pricingDiscountRequestParams.put("productID", row.getProduct().getID());
                        pricingDiscountRequestParams.put("isPurchase", true);
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
                        if (row.getVendorquotation().isGstIncluded()) {//if gstincluded is the case
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
                        obj.put("rate", row.getRate());
                        obj.put("discountispercent", row.getDiscountispercent());
                        obj.put("prdiscount", row.getDiscount());

                        if (so.isFixedAssetVQ()) {
                            getAssetDetailForVQRows(obj, companyid, linkingFlag, row, df, request);
                        }
                        boolean isForInvoice = (StringUtil.isNullOrEmpty(request.getParameter("isForInvoice"))) ? false : Boolean.parseBoolean(request.getParameter("isForInvoice"));
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        VendorQuotationDetailCustomData vendorQuotationDetailCustomData = (VendorQuotationDetailCustomData) row.getVendorQuotationDetailCustomData();
                        AccountingManager.setCustomColumnValues(vendorQuotationDetailCustomData, FieldMap, replaceFieldMap, variableMap);
                        if (vendorQuotationDetailCustomData != null) {
                            JSONObject params = new JSONObject();
                            params.put("isExport", isExport);
                            params.put("isForReport", customIsReport);
                            params.put("userdf", userdf);
                            if ((isForLinking || poflag || isForInvoice || FA_VQlinkToFA_PO || FA_VQlinkToPI)) {
                                int moduleId = poflag ? Constants.Acc_Purchase_Order_ModuleId : isForInvoice ? Constants.Acc_Vendor_Invoice_ModuleId : isFixedAsset ? Constants.Acc_FixedAssets_Purchase_Order_ModuleId : Constants.Acc_Customer_Quotation_ModuleId;
                                if (FA_VQlinkToPI) {
                                    moduleId = Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId;
                                }
                                if (FA_VQlinkToFA_PO) {
                                    moduleId = Constants.Acc_FixedAssets_Purchase_Order_ModuleId;
                                }
                                params.put("linkModuleId", moduleId);
                                params.put("isLink", true);
                                params.put("companyid", companyid);
                            }
                            fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                        }

                        // Get Product level Custom field data
                        HashMap<String, Object> fieldrequestParamsProduct = new HashMap();
                        HashMap<String, String> customProductFieldMap = new HashMap<String, String>();
                        HashMap<String, String> customProductDateFieldMap = new HashMap<String, String>();
                        Map<String, Object> variableMapProduct = new HashMap<String, Object>();
                        fieldrequestParamsProduct.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                        fieldrequestParamsProduct.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                        HashMap<String, String> replaceFieldMapProduct = new HashMap<String, String>();
                        HashMap<String, Integer> FieldMapProduct = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParamsProduct, replaceFieldMapProduct, customProductFieldMap, customProductDateFieldMap);
                        VendorQuotationDetailsProductCustomData VQDetailProductCustomData = (VendorQuotationDetailsProductCustomData) row.getVQDetailsProductCustomData();
                        AccountingManager.setCustomColumnValues(VQDetailProductCustomData, FieldMapProduct, replaceFieldMapProduct, variableMapProduct);

                        if (VQDetailProductCustomData != null) {
                            JSONObject params = new JSONObject();
                            params.put(Constants.userdf, userdf);
                            if (isForLinking) {
                                isExport = false;
                            }
                            params.put("isExport", isExport);
                            params.put("isForReport", customIsReport);
                            fieldDataManagercntrl.getLineLevelCustomData(variableMapProduct, customProductFieldMap, customProductDateFieldMap, obj, params);
                        }
                        obj.put(Constants.unitpriceForExcelFile, row.getRate());//obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
                        obj.put("marginExchangeRate", row.getVendorquotation() != null ? row.getVendorquotation().getExternalCurrencyRate() : 0);
                        obj.put("quantity", row.getQuantity());

                        obj.put("rateIncludingGst", authHandler.roundUnitPrice(row.getRateincludegst(), companyid));
                        obj.put("israteIncludingGst", so.isGstIncluded());
                        double baseuomrate = row.getBaseuomrate();
                        double quantity = 0;
                        double invoiceRowProductQty = authHandler.calculateBaseUOMQuatity(row.getQuantity(), baseuomrate, companyid);
                        double remainedQty = invoiceRowProductQty;// which has not been linked yet
                        if (closeflag != null && moduleid != Constants.Acc_Customer_Quotation_ModuleId) {
                            //While linking VQ in PO/VI, we have to check quantity of VQ, but in Cross-linking(i.e. in CQ) no need to check quantity of VQ.
                            addobj = poflag ? getQuotationDetailStatusPO(row) : getQuotationDetailStatusGR(row);
                            quantity = addobj;
                            obj.put("quantity", addobj);
                            obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(addobj, baseuomrate, companyid));
                            remainedQty = authHandler.calculateBaseUOMQuatity(addobj, baseuomrate, companyid);
                        } else {
                            quantity = row.getQuantity();
                            obj.put("quantity", quantity);
                            obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, baseuomrate, companyid));

                        }

                        // Added vendor related information for Profit Margin Calculation
                        if (extraCompanyPreferences.isActivateProfitMargin() && isForLinking) {
                            obj.put("vendorid", (row.getVendorquotation() != null && row.getVendorquotation().getVendor() != null) ? row.getVendorquotation().getVendor().getID() : "");
                            obj.put("vendorcurrexchangerate", row.getVendorquotation() != null && row.getVendorquotation().getExternalCurrencyRate() != 0 ? (1 / row.getVendorquotation().getExternalCurrencyRate()) : 1);
                            obj.put("vendorcurrencyid", (row.getVendorquotation() != null && row.getVendorquotation().getCurrency() != null) ? row.getVendorquotation().getCurrency().getCurrencyID() : "");
                            obj.put("vendorcurrencysymbol", (row.getVendorquotation() != null && row.getVendorquotation().getCurrency() != null) ? row.getVendorquotation().getCurrency().getSymbol() : "");
                            obj.put("vendorunitcost", row.getRate());
                        }

                        if (isForLinking) {// in case of linking in normal transactions not lease consignment etc.
                            if (row.getTax() != null && invoiceRowProductQty > 0) {
                                double taxAmt = (rowTaxAmount / invoiceRowProductQty) * remainedQty;
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
                            obj = accProductObj.getProductDisplayUOM(rowProduct, quantity, row.getBaseuomrate(), true, obj);
                        }    
                        if (!StringUtil.isNullOrEmpty(row.getPurchaseRequisitionDetailsId())) {   // If Purchase Requisition is linked
                            KwlReturnObject prdetailresult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), row.getPurchaseRequisitionDetailsId());
                            PurchaseRequisitionDetail purchaseRequisitionDetail = (PurchaseRequisitionDetail) prdetailresult.getEntityList().get(0);
                            if (!StringUtil.isNullObject(purchaseRequisitionDetail)) {
                                obj.put("linkto", !StringUtil.isNullObject(purchaseRequisitionDetail.getPurchaserequisition()) ? purchaseRequisitionDetail.getPurchaserequisition().getPrNumber() : "");
                                obj.put("linkid", !StringUtil.isNullObject(purchaseRequisitionDetail.getPurchaserequisition()) ? purchaseRequisitionDetail.getPurchaserequisition().getID() : "");
                                /*
                                     ERM-1037
                                     Field used for comparing dates to restrict linking of future doument date in Vendor Quotation document editing
                                 */
                                obj.put("linkDate", !StringUtil.isNullObject(purchaseRequisitionDetail.getPurchaserequisition()) ? purchaseRequisitionDetail.getPurchaserequisition().getRequisitionDate() : "");
                                obj.put("savedrowid", row.getID());//used same logic like invoice
                                if (FA_VQlinkToFA_PO) {
                                    obj.put("rowid", row.getID());
                                } else {
                                    obj.put("rowid", purchaseRequisitionDetail.getID());
                                }
                                obj.put("linktype", 5);
                            }
                        } else if (!StringUtil.isNullOrEmpty(row.getRfqDetailsId())) {
                            KwlReturnObject prdetailresult = accountingHandlerDAOobj.getObject(RequestForQuotationDetail.class.getName(), row.getRfqDetailsId());
                            RequestForQuotationDetail requestForQuotationDetail = (RequestForQuotationDetail) prdetailresult.getEntityList().get(0);
                            if (!StringUtil.isNullObject(requestForQuotationDetail)) {
                                obj.put("linkto", !StringUtil.isNullObject(requestForQuotationDetail.getRequestforquotation()) ? requestForQuotationDetail.getRequestforquotation().getRfqNumber() : "");
                                obj.put("linkid", !StringUtil.isNullObject(requestForQuotationDetail.getRequestforquotation()) ? requestForQuotationDetail.getRequestforquotation().getID() : "");
                                /*
                                     ERM-1037
                                     Field used for comparing dates to restrict linking of future doument date in Vendor Quotation document editing
                                 */
                                obj.put("linkDate", !StringUtil.isNullObject(requestForQuotationDetail.getRequestforquotation()) ? requestForQuotationDetail.getRequestforquotation().getRfqDate(): "");
                                obj.put("savedrowid", row.getID());//used same logic like invoice
                                obj.put("docrowid", row.getID());
                                obj.put("rowid", requestForQuotationDetail.getID());
                                obj.put("linktype", 6);
                            }
                        }
                        String salesOrPurchase = request.getParameter("termSalesOrPurchaseCheck") != null ? request.getParameter("termSalesOrPurchaseCheck").toString() : "false";
                        if (extraCompanyPreferences.getLineLevelTermFlag() == 1) { // Fetch Vat term details of Product
                            if (vqtocqflag && !poflag) {
                                boolean isDefault = true;
                                Map<String, Object> mapData = new HashMap<String, Object>();
                                mapData.put("productid", rowProduct.getID());
                                mapData.put("salesOrPurchase", true);
                                obj.put("recTermAmount", 0);
                                if (isDefault) {
                                    mapData.put("isDefault", isDefault);
                                }
                                KwlReturnObject result6 = accProductObj.getProductTermDetails(mapData);
                                if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                                    ArrayList<ProductTermsMap> productTermDetail = (ArrayList<ProductTermsMap>) result6.getEntityList();
                                    JSONArray productTermJsonArry = CommonFunctions.fetchProductTermMapDetails(productTermDetail);
                                    obj.put("LineTermdetails", productTermJsonArry.toString());
                                }
                                if (isDefault) {
                                    mapData.put("isDefault", false);
                                }
                                KwlReturnObject result7 = accProductObj.getProductTermDetails(mapData);
                                if (result7.getEntityList() != null && result7.getEntityList().size() > 0 && result7.getEntityList().get(0) != null) {
                                    ArrayList<ProductTermsMap> productTermDetail = (ArrayList<ProductTermsMap>) result7.getEntityList();
                                    JSONArray productTermJsonArry = CommonFunctions.fetchProductTermMapDetails(productTermDetail);
                                    obj.put("uncheckedTermdetails", productTermJsonArry.toString());
                                }

                            } else {
                                Map<String, Object> mapData = new HashMap<String, Object>();
                                mapData.put("productid", rowProduct.getID());
                                mapData.put("salesOrPurchase", salesOrPurchase);
                                mapData.put("vendorquotationdetails", row.getID());
                                KwlReturnObject result6 = accPurchaseOrderobj.getVendorQuotationProductTermDetails(mapData);
                                if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                                    ArrayList<VendorQuotationDetailsTermMap> productTermDetail = (ArrayList<VendorQuotationDetailsTermMap>) result6.getEntityList();
                                    JSONArray productTermJsonArry = new JSONArray();

                                    for (VendorQuotationDetailsTermMap productTermsMapObj : productTermDetail) {
                                        JSONObject productTermJsonObj = new JSONObject();
                                        productTermJsonObj.put("id", productTermsMapObj.getId());
                                        productTermJsonObj.put("productid", productTermsMapObj.getProduct().getID());
                                        /**
                                         * ERP-32829
                                         */
                                        productTermJsonObj.put("productentitytermid", productTermsMapObj.getEntitybasedLineLevelTermRate() != null ? productTermsMapObj.getEntitybasedLineLevelTermRate().getId() : "");
                                        productTermJsonObj.put("isDefault", productTermsMapObj.isIsGSTApplied());
                                        productTermJsonObj.put("termid", productTermsMapObj.getTerm().getId());
                                        productTermJsonObj.put("term", productTermsMapObj.getTerm().getTerm());
                                        productTermJsonObj.put("formula", productTermsMapObj.getTerm().getFormula());
                                        productTermJsonObj.put("formulaids", productTermsMapObj.getTerm().getFormula());
                                        productTermJsonObj.put("termpercentage", productTermsMapObj.getPercentage());
                                        productTermJsonObj.put("originalTermPercentage", productTermsMapObj.getTerm().getPercentage());
                                        productTermJsonObj.put("termamount", productTermsMapObj.getTermamount());
                                        productTermJsonObj.put("glaccountname", productTermsMapObj.getTerm().getAccount().getAccountName());
                                        productTermJsonObj.put("glaccount", productTermsMapObj.getTerm().getAccount().getID());
                                        productTermJsonObj.put("IsOtherTermTaxable", productTermsMapObj.getTerm().isOtherTermTaxable());
                                        productTermJsonObj.put("sign", productTermsMapObj.getTerm().getSign());
                                        productTermJsonObj.put("purchasevalueorsalevalue", productTermsMapObj.getPurchaseValueOrSaleValue());
                                        productTermJsonObj.put("deductionorabatementpercent", productTermsMapObj.getDeductionOrAbatementPercent());
                                        productTermJsonObj.put("assessablevalue", productTermsMapObj.getAssessablevalue());
                                        productTermJsonObj.put("taxtype", productTermsMapObj.getTaxType());
                                        //                                productTermJsonObj.put("taxvalue", productTermsMapObj.getTaxType()==0 ? productTermsMapObj.getTermamount() : productTermsMapObj.getPercentage());
                                        productTermJsonObj.put("taxvalue", productTermsMapObj.getPercentage());
                                        productTermJsonObj.put("termtype", productTermsMapObj.getTerm().getTermType());
                                        productTermJsonObj.put("termsequence", productTermsMapObj.getTerm().getTermSequence());
                                        productTermJsonObj.put("formType", productTermsMapObj.getTerm().getFormType());
                                        productTermJsonObj.put("creditnotavailedaccount", productTermsMapObj.getTerm().getCreditNotAvailedAccount() != null ? productTermsMapObj.getTerm().getCreditNotAvailedAccount().getID() : "");
                                        productTermJsonObj.put("payableaccountid", productTermsMapObj.getTerm().getPayableAccount() != null ? productTermsMapObj.getTerm().getPayableAccount().getID() : "");
                                        productTermJsonObj.put(IndiaComplianceConstants.GST_CESS_TYPE, productTermsMapObj.getEntitybasedLineLevelTermRate() != null && productTermsMapObj.getEntitybasedLineLevelTermRate().getCessType() != null ? productTermsMapObj.getEntitybasedLineLevelTermRate().getCessType().getId() : "");
                                        productTermJsonObj.put(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT, productTermsMapObj.getEntitybasedLineLevelTermRate() != null ? productTermsMapObj.getEntitybasedLineLevelTermRate().getValuationAmount() : 0.0);
                                        productTermJsonObj.put(IndiaComplianceConstants.DEFAULT_TERMID, productTermsMapObj.getTerm() != null && productTermsMapObj.getTerm().getDefaultTerms() != null ? productTermsMapObj.getTerm().getDefaultTerms().getId() : "");
                                        productTermJsonArry.put(productTermJsonObj);
                                    }
                                    obj.put("LineTermdetails", productTermJsonArry.toString());
                                }
                            }
                            if (row.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                                /**
                                 * Put GST Tax Class History.
                                 */
                                obj.put("refdocid", row.getID());
                                fieldDataManagercntrl.getGSTTaxClassHistory(obj);
                            }
                        }

                        if (country.getID().equals(String.valueOf(Constants.indian_country_id))) { // Fetch Vat term details of Product
                            // Excise AND VAT special Rate type TAX ------ START-------                        
                            boolean carryin = StringUtil.isNullOrEmpty(salesOrPurchase) ? true : Boolean.parseBoolean(salesOrPurchase);;
                            String uomid = (row == null) ? "" : (row.getUom() != null ? row.getUom().getID() : "");
                            if (extraCompanyPreferences.isExciseApplicable()) {
                                String reortingUOM = (row.getReportingUOMExcise() != null) ? row.getReportingUOMExcise().getID() : "";
                                String valuationType = !StringUtil.isNullOrEmpty(row.getExciseValuationType()) ? row.getExciseValuationType() : "";
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
                                    obj.put("productMRP", row.getMrpIndia());
                                }
                            }
                            if (extraCompanyPreferences.isEnableVatCst()) {
                                String reortingUOMVAT = (row.getReportingUOMVAT() != null) ? row.getReportingUOMVAT().getID() : "";
                                String valuationTypeVAT = !StringUtil.isNullOrEmpty(row.getVatValuationType()) ? row.getVatValuationType() : "";
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
                                    obj.put("productMRP", row.getMrpIndia());
                                }
                            }
                        }
                        jArr.put(obj);
                    }
                }//end of  if (vqDetailsList.size() > 0)
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }
  
    public void getAssetDetailForVQRows(JSONObject obj, String companyid, boolean linkingFlag, VendorQuotationDetail row, DateFormat df, HttpServletRequest request) throws JSONException, ServiceException, SessionExpiredException {
        HashMap<String, Object> assParams = new HashMap<String, Object>();
        assParams.put("companyId", companyid);
        assParams.put("invrecord", false);
        KwlReturnObject assResult = accProductObj.getAssetDetails(assParams);

        List assetList = assResult.getEntityList();

        List<String> assetNameList = new ArrayList<String>();

        Iterator it = assetList.iterator();
        while (it.hasNext()) {
            AssetDetails ad = (AssetDetails) it.next();
            assetNameList.add(ad.getAssetId());
        }

        JSONArray assetDetailsJArr = new JSONArray();
        HashMap<String, Object> assetDetailsParams = new HashMap<String, Object>();
        assetDetailsParams.put("companyid", companyid);
        assetDetailsParams.put("purchaseRequisitionDetailID", row.getID());
        assetDetailsParams.put("moduleId", Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);

        KwlReturnObject assetInvMapObj = accProductObj.getAssetPurchaseRequisitionDetailMapping(assetDetailsParams);
        List assetInvMapList = assetInvMapObj.getEntityList();
        Iterator assetInvMapListIt = assetInvMapList.iterator();
        while (assetInvMapListIt.hasNext()) {
            AssetPurchaseRequisitionDetailMapping invoiceDetailMapping = (AssetPurchaseRequisitionDetailMapping) assetInvMapListIt.next();
            PurchaseRequisitionAssetDetails assetDetails = invoiceDetailMapping.getPurchaseRequisitionAssetDetails();
            JSONObject assetDetailsJOBJ = new JSONObject();

            // <-- Following statement must be first statement in putting assetDetailsJOBJ
            if (linkingFlag) {

                if (assetNameList.contains(assetDetails.getAssetId())) { // don't put assets which are included in GR
                    continue;
                }

                assetDetailsJOBJ.put("assetId", assetDetails.getId());
            } else {
                if (!StringUtil.isNullOrEmpty(row.getPurchaseRequisitionDetailsId())) { // in case of linking while editing the Asset Details combo get load so i need to send id of asset.
                    assetDetailsJOBJ.put("assetId", assetDetails.getId());
                } else {
                    assetDetailsJOBJ.put("assetId", assetDetails.getAssetId());
                }
            }

            assetDetailsJOBJ.put("assetdetailId", assetDetails.getId());
            assetDetailsJOBJ.put("assetName", assetDetails.getAssetId());
            assetDetailsJOBJ.put("location", (assetDetails.getLocation() != null) ? assetDetails.getLocation().getId() : "");
            assetDetailsJOBJ.put("department", (assetDetails.getDepartment() != null) ? assetDetails.getDepartment().getId() : "");
            assetDetailsJOBJ.put("assetdescription", (assetDetails.getAssetDescription() != null) ? assetDetails.getAssetDescription() : "");
            assetDetailsJOBJ.put("assetUser", (assetDetails.getAssetUser() != null) ? assetDetails.getAssetUser().getUserID() : "");
            assetDetailsJOBJ.put("cost", assetDetails.getCost());
            assetDetailsJOBJ.put("costInForeignCurrency", assetDetails.getCostInForeignCurrency());
            assetDetailsJOBJ.put("salvageValueInForeignCurrency", assetDetails.getSalvageValueInForeignCurrency());
            assetDetailsJOBJ.put("salvageRate", assetDetails.getSalvageRate());
            assetDetailsJOBJ.put("salvageValue", assetDetails.getSalvageValue());
            assetDetailsJOBJ.put("accumulatedDepreciation", assetDetails.getAccumulatedDepreciation());
            assetDetailsJOBJ.put("wdv", assetDetails.getWdv());
            assetDetailsJOBJ.put("assetLife", assetDetails.getAssetLife());
            assetDetailsJOBJ.put("elapsedLife", assetDetails.getElapsedLife());
            assetDetailsJOBJ.put("nominalValue", assetDetails.getElapsedLife());
            assetDetailsJOBJ.put("installationDate", df.format(assetDetails.getInstallationDate()));
            assetDetailsJOBJ.put("purchaseDate", df.format(assetDetails.getPurchaseDate()));
            
            assetDetailsJArr.put(assetDetailsJOBJ);
        }
        obj.put("assetDetails", assetDetailsJArr.toString());
    }
    
    public void getAssetDetailForPORows(JSONObject obj, String companyid, boolean linkingFlag, PurchaseOrderDetail row, DateFormat df) throws JSONException, ServiceException, SessionExpiredException {
        HashMap<String, Object> assParams = new HashMap<String, Object>();
        assParams.put("companyId", companyid);
        assParams.put("invrecord", false);
        KwlReturnObject assResult = accProductObj.getAssetDetails(assParams);

        List assetList = assResult.getEntityList();

        List<String> assetNameList = new ArrayList<String>();

        Iterator it = assetList.iterator();
        while (it.hasNext()) {
            AssetDetails ad = (AssetDetails) it.next();
            assetNameList.add(ad.getAssetId());
        }

        JSONArray assetDetailsJArr = new JSONArray();
        HashMap<String, Object> assetDetailsParams = new HashMap<String, Object>();
        assetDetailsParams.put("companyid", companyid);
        assetDetailsParams.put("purchaseRequisitionDetailID", row.getID());
        assetDetailsParams.put("moduleId", Constants.Acc_FixedAssets_Purchase_Order_ModuleId);

        KwlReturnObject assetInvMapObj = accProductObj.getAssetPurchaseRequisitionDetailMapping(assetDetailsParams);
        List assetInvMapList = assetInvMapObj.getEntityList();
        Iterator assetInvMapListIt = assetInvMapList.iterator();
        while (assetInvMapListIt.hasNext()) {
            AssetPurchaseRequisitionDetailMapping invoiceDetailMapping = (AssetPurchaseRequisitionDetailMapping) assetInvMapListIt.next();
            PurchaseRequisitionAssetDetails assetDetails = invoiceDetailMapping.getPurchaseRequisitionAssetDetails();
            JSONObject assetDetailsJOBJ = new JSONObject();

            // <-- Following statement must be first statement in putting assetDetailsJOBJ
            if (linkingFlag) {

                if (assetNameList.contains(assetDetails.getAssetId())) { // don't put assets which are included in GR
                    continue;
                }

                assetDetailsJOBJ.put("assetId", assetDetails.getId());
            } else {
                if (row.getVqdetail() != null) { // in case of linking while editing the Asset Details combo get load so i need to send id of asset.
                    assetDetailsJOBJ.put("assetId", assetDetails.getId());
                } else {
                    assetDetailsJOBJ.put("assetId", assetDetails.getAssetId());
                }
            }

            assetDetailsJOBJ.put("assetdetailId", assetDetails.getId());
            assetDetailsJOBJ.put("assetName", assetDetails.getAssetId());
            assetDetailsJOBJ.put("location", (assetDetails.getLocation() != null) ? assetDetails.getLocation().getId() : "");
            assetDetailsJOBJ.put("department", (assetDetails.getDepartment() != null) ? assetDetails.getDepartment().getId() : "");
            assetDetailsJOBJ.put("assetdescription", (assetDetails.getAssetDescription() != null) ? assetDetails.getAssetDescription() : "");
            assetDetailsJOBJ.put("assetUser", (assetDetails.getAssetUser() != null) ? assetDetails.getAssetUser().getUserID() : "");
            assetDetailsJOBJ.put("cost", assetDetails.getCost());
            assetDetailsJOBJ.put("costInForeignCurrency", assetDetails.getCostInForeignCurrency());
            assetDetailsJOBJ.put("salvageValueInForeignCurrency", assetDetails.getSalvageValueInForeignCurrency());
            assetDetailsJOBJ.put("salvageRate", assetDetails.getSalvageRate());
            assetDetailsJOBJ.put("salvageValue", assetDetails.getSalvageValue());
            assetDetailsJOBJ.put("accumulatedDepreciation", assetDetails.getAccumulatedDepreciation());
            assetDetailsJOBJ.put("wdv", assetDetails.getWdv());
            assetDetailsJOBJ.put("assetLife", assetDetails.getAssetLife());
            assetDetailsJOBJ.put("elapsedLife", assetDetails.getElapsedLife());
            assetDetailsJOBJ.put("nominalValue", assetDetails.getElapsedLife());
            assetDetailsJOBJ.put("installationDate", df.format(assetDetails.getInstallationDate()));
            assetDetailsJOBJ.put("purchaseDate", df.format(assetDetails.getPurchaseDate()));

            assetDetailsJArr.put(assetDetailsJOBJ);
        }
        obj.put("assetDetails", assetDetailsJArr.toString());
    }
    
    @Override
    public JSONArray getVersionQuotationsJson(HttpServletRequest request, List list, JSONArray jArr) throws ServiceException {
        try {
            HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get("companyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Vendor_Quotation_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                String qid = (String) itr.next();
                if (!StringUtil.isNullOrEmpty(qid)) {
                    KwlReturnObject reqResult = accountingHandlerDAOobj.getObject(VendorQuotationVersion.class.getName(), qid);
                    VendorQuotationVersion salesOrder = (VendorQuotationVersion) reqResult.getEntityList().get(0);
                    KWLCurrency currency = null;
                    if (salesOrder.getCurrency() != null) {
                        currency = salesOrder.getCurrency();
                    } else {
                        currency = salesOrder.getVendor().getAccount().getCurrency() == null ? kwlcurrency : salesOrder.getVendor().getAccount().getCurrency();
                    }
                    Vendor customer = salesOrder.getVendor();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", salesOrder.getID());
                    obj.put("companyid", salesOrder.getCompany().getCompanyID());
                    obj.put("companyname", salesOrder.getCompany().getCompanyName());
                    obj.put("personid", customer.getID());
                    obj.put("personemail", salesOrder.getVendor() == null ? "" : salesOrder.getVendor().getEmail());
                    obj.put("billno", salesOrder.getQuotationNumber());
                    obj.put("duedate", authHandler.getDateOnlyFormat().format(salesOrder.getDueDate()));
                    obj.put("date", authHandler.getDateOnlyFormat().format(salesOrder.getQuotationDate()));
                    obj.put("shipdate", salesOrder.getShipdate() == null ? "" : authHandler.getDateOnlyFormat().format(salesOrder.getShipdate()));
                    obj.put("validdate", salesOrder.getValiddate() == null ? "" : authHandler.getDateOnlyFormat().format(salesOrder.getValiddate()));
                    obj.put("shipvia", salesOrder.getShipvia() == null ? "" : salesOrder.getShipvia());
                    obj.put("fob", salesOrder.getFob() == null ? "" : salesOrder.getFob());
                    obj.put("archieve", salesOrder.getArchieve());
                    obj.put("billto", salesOrder.getBillTo());
                    obj.put("shipto", salesOrder.getShipTo());
                    obj.put("deleted", salesOrder.isDeleted());
                    obj.put("salesPerson", salesOrder.getMasteragent() != null ? salesOrder.getMasteragent().getID() : "");
                    obj.put("salespersonname", salesOrder.getMasteragent() == null ? "" : salesOrder.getMasteragent().getValue());
                    obj.put("isfavourite", salesOrder.isFavourite());
                    obj.put("isprinted", salesOrder.isPrinted());
                    obj.put("termdetails", accSalesOrderServiceDAOobj.getTermDetails(salesOrder.getQuotation().getID(), false));
                    obj.put("termamount", CommonFunctions.getTotalTermsAmount(getTermDetails(salesOrder.getQuotation().getID(), false)));
                    obj.put("discountval", (salesOrder.getDiscount() == 0) ? 0 : salesOrder.getDiscount());
                    obj.put("gstIncluded", salesOrder.isGstIncluded());
                    obj.put("shiplengthval", salesOrder.getShiplength());
                    obj.put("invoicetype", salesOrder.getInvoicetype());
                    obj.put("approvalstatusinfo", salesOrder.getApprovestatuslevel() == -1 ? "Rejected" : salesOrder.getApprovestatuslevel() < 11 ? "Waiting for Approval at Level - " + salesOrder.getApprovestatuslevel() : "Approved");
                    obj.put("approvalstatus", salesOrder.getApprovestatuslevel());
                    obj.put("version", salesOrder.getVersion());
                    boolean incProTax = false;
                    Iterator itrRow = salesOrder.getRows().iterator();
                    double amount = 0, amountinbase = 0, totalDiscount = 0, discountPrice = 0;
                    double rowTaxAmt = 0d;
                    while (itrRow.hasNext()) {
                        VendorQuotationVersionDetail sod = (VendorQuotationVersionDetail) itrRow.next();
                        if (sod.getTax() != null) {
                            incProTax = true;
                        }
                        double vqrate = salesOrder.isGstIncluded() ? authHandler.roundUnitPrice(sod.getRateincludegst(), companyid) : authHandler.roundUnitPrice(sod.getRate(), companyid);
                        double quantity = authHandler.roundQuantity(sod.getQuantity(), companyid);

                        double quotationPrice = authHandler.round(quantity * vqrate, companyid);
                        double discountVQD = authHandler.round(sod.getDiscount(), companyid);
                        if (sod.getDiscountispercent() == 1) {
                            discountPrice = (quotationPrice) - authHandler.round((quotationPrice * discountVQD / 100), companyid);
                        } else {
                            discountPrice = quotationPrice - discountVQD;
                        }
                        rowTaxAmt += sod.getRowTaxAmount();
                        amount += discountPrice + (salesOrder.isGstIncluded() ? 0 : authHandler.round(sod.getRowTaxAmount(), companyid));//amount += discountPrice + (discountPrice * rowTaxPercent/100);
                    }
                    double discountVQ = authHandler.round(salesOrder.getDiscount(), companyid);
                    obj.put("includeprotax", incProTax);
                    if (salesOrder.getModifiedby() != null) {
                        obj.put("lasteditedby", StringUtil.getFullName(salesOrder.getModifiedby()));
                    }
                    if (discountVQ != 0) {
                        if (salesOrder.isPerDiscount()) {
                            totalDiscount = authHandler.round(amount * discountVQ / 100, companyid);;
                            amount = amount - totalDiscount;
                        } else {
                            amount = amount - discountVQ;
                            totalDiscount = discountVQ;
                        }
                        obj.put("discounttotal", discountVQ);
                    } else {
                        obj.put("discounttotal", 0);
                    }
                    obj.put("discount", totalDiscount);
                    obj.put("discountispertotal", salesOrder.isPerDiscount());
                    obj.put("ispercentdiscount", salesOrder.isPerDiscount());
                    double totalTermAmount = 0;
                    double taxableTermamount = 0;
                    HashMap<String, Object> requestParam = new HashMap();
                    HashMap<String, Object> filterrequestParams = new HashMap();
                    requestParam.put("vendorQuotation", salesOrder.getQuotation().getID());
                    KwlReturnObject vendorQuotationResult = null;
                    filterrequestParams.put("taxid", salesOrder.getTax() == null ? "" : salesOrder.getTax().getID());
                    vendorQuotationResult = accPurchaseOrderobj.getVendorQuotationTermMap(requestParam);
                    List<VendorQuotationTermMap> termMap = vendorQuotationResult.getEntityList();
                    for (VendorQuotationTermMap vendorQuotationTermMap : termMap) {
                        filterrequestParams.put("term", vendorQuotationTermMap.getTerm() == null ? "" : vendorQuotationTermMap.getTerm().getId());
                        InvoiceTermsSales mt = vendorQuotationTermMap.getTerm();
                        double termAmnt = vendorQuotationTermMap.getTermamount();
                        totalTermAmount += authHandler.round(termAmnt, companyid);

                        boolean isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);

                        if (isTermMappedwithTax) {
                            taxableTermamount += termAmnt;
                        }
                    }
                    totalTermAmount = authHandler.round(totalTermAmount, companyid);

                    KwlReturnObject termbAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, totalTermAmount, currency.getCurrencyID(), salesOrder.getQuotationDate(), salesOrder.getExternalCurrencyRate());
                    double termamountinBase = authHandler.round((Double) termbAmtTax.getEntityList().get(0), companyid);
                    obj.put("amount", amount);
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("currencycode", currency.getCurrencyCode() == null ? "" : currency.getCurrencyCode());
                    obj.put("taxid", salesOrder.getTax() == null ? "" : salesOrder.getTax().getID());
                    obj.put("taxname", salesOrder.getTax() == null ? "" : salesOrder.getTax().getName());
                    double taxPercent = 0;
                    if (salesOrder.getTax() != null) {
                        requestParams.put("transactiondate", salesOrder.getQuotationDate());
                        requestParams.put("taxid", salesOrder.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];

                    }
                    double orderAmount = amount;//(Double) bAmt.getEntityList().get(0);
                    double ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round((orderAmount + taxableTermamount) * taxPercent / 100, companyid));
                    double taxAmt = rowTaxAmt + ordertaxamount;// either row level tax will be avvailable or invoice level
                    obj.put("amountbeforegst", amount - rowTaxAmt); // Amount before both kind of tax row level or transaction level
                    KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, currency.getCurrencyID(), salesOrder.getQuotationDate(), salesOrder.getExternalCurrencyRate());
                    amountinbase = (Double) bAmt.getEntityList().get(0);
                    KwlReturnObject bAmtTax = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, authHandler.round(((orderAmount + taxableTermamount) * taxPercent / 100), companyid), currency.getCurrencyID(), salesOrder.getQuotationDate(), salesOrder.getExternalCurrencyRate());
                    double ordertaxamountBase = authHandler.round((Double) bAmtTax.getEntityList().get(0), companyid);
                    amountinbase += termamountinBase;
                    obj.put("amountinbase", amountinbase + ordertaxamountBase);
                    obj.put("taxpercent", taxPercent);
                    obj.put("taxamount", taxAmt);
                    amount += totalTermAmount;
                    orderAmount += totalTermAmount;
                    obj.put("orderamount", orderAmount);
                    obj.put("orderamountwithTax", orderAmount + ordertaxamount);
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("personname", customer.getName());
                    obj.put("personcode", customer.getAcccode() == null ? "" : customer.getAcccode());
                    obj.put("memo", salesOrder.getMemo());
                    obj.put("posttext", salesOrder.getPostText());

                    DateFormat df = (DateFormat) requestParams.get("df");
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    VendorQuotationVersionCustomData quotationDetailCustomData = (VendorQuotationVersionCustomData) salesOrder.getQuotationCustomData();
                    AccountingManager.setCustomColumnValues(quotationDetailCustomData, FieldMap, replaceFieldMap, variableMap);
                    if (quotationDetailCustomData != null) {
                        JSONObject params = new JSONObject();
                        params.put("isExport", true);
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                    boolean addFlag = true;
                    if (addFlag) {
                        jArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getVersionQuotationsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

     /**
     * Description : Method is used to Build Purchase Requisition record Json linked in Vendor Quotation 
     * @param <jsonarray> Used to build array of Linked documents Purchase Requisition in Vendor Quotation
     * @param <listcq> contains id of purchase Requisition Linked in Selected Vendor Quotation
     * @param <currency> Currency used in documents
     * @param <linkType> Contains Type when Purchase Requisition linked with Vendor Quotation
     * @param <userdf> Object Of user Date Format
     * @return :JSONArray
     */
    @Override
    public JSONArray getPurchaseRequisitionJsonForLinking(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, int linkType) throws ServiceException {

        try {

            PurchaseRequisition purchaseOrder = null;
            Iterator itr = listcq.iterator();
            while (itr.hasNext()) {
                String reqid = (String) itr.next();
                KwlReturnObject reqResult = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), reqid);
                purchaseOrder = (PurchaseRequisition) reqResult.getEntityList().get(0);

                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("invoiceID", purchaseOrder.getID());
                hashMap.put("companyid", purchaseOrder.getCompany().getCompanyID());
                JSONObject obj = new JSONObject();
                obj.put("billid", purchaseOrder.getID());
                obj.put("companyid", purchaseOrder.getCompany().getCompanyID());
                obj.put("companyname", purchaseOrder.getCompany().getCompanyName());
                obj.put("personname", purchaseOrder.getVendor() == null ? "" : purchaseOrder.getVendor().getName());
                obj.put("personemail", purchaseOrder.getVendor() == null ? "" : purchaseOrder.getVendor().getEmail());
                obj.put("aliasname", purchaseOrder.getVendor() == null ? "" : purchaseOrder.getVendor().getAliasname());
                obj.put("billno", purchaseOrder.getPrNumber());
                obj.put("transactionNo", purchaseOrder.getPrNumber());
                obj.put("isfavourite", purchaseOrder.isFavourite());
                obj.put("isprinted", purchaseOrder.isPrinted());
                obj.put("duedate", userdf.format(purchaseOrder.getDueDate()));
                obj.put("date", userdf.format(purchaseOrder.getRequisitionDate()));
                obj.put("archieve", purchaseOrder.getArchieve());
                obj.put("memo", purchaseOrder.getMemo());
                obj.put("status", purchaseOrder.getApprovestatuslevel() == Constants.DraftedPurchaseRequisitions ? "Drafted" : purchaseOrder.getApprovestatuslevel() < Constants.MaximumLimitOfLevelsInMultilevelApproval ? (purchaseOrder.getApprovestatuslevel() < 0 ? "Rejected" : "Waiting for Approval at Level - " + purchaseOrder.getApprovestatuslevel()) : "Approved"); // Level= -99 means Drafted , Level between 1 to 10 means pending for approval , Level<0 means Rejected and level=11 means approved
                obj.put("deleted", purchaseOrder.isDeleted());
                obj.put("createdby", purchaseOrder.getCreatedby() == null ? "" : StringUtil.getFullName(purchaseOrder.getCreatedby()));
                obj.put("lasteditedby", purchaseOrder.getModifiedby() == null ? "" : purchaseOrder.getModifiedby().getFirstName() + " " + purchaseOrder.getModifiedby().getLastName());
                obj.put("mergedCategoryData", "Purchase Requisition");  //type of data
                obj.put("type", linkType);
                obj.put("currencyid", purchaseOrder.getCurrency().getCurrencyID());
                obj.put("sequenceformatid", purchaseOrder.getSeqformat()==null?"":purchaseOrder.getSeqformat().getID());
                jsonArray.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getPurchaseRequisitionJsonForLinking : " + ex.getMessage(), ex);
        }
        return jsonArray;
    }
    /**
     * 
     * @param jsonArray = Used to put data object
     * @param listcq
     * @param currency
     * @param userdf
     * @param linkType = Used for maintain link type for Quotation
     * @return
     * @throws ServiceException 
     */
    @Override
    public JSONArray getRFQJsonForLinking(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, int linkType) throws ServiceException {
        try {

            RequestForQuotation purchaseOrder = null;
            Iterator itr = listcq.iterator();
            while (itr.hasNext()) {
                String reqid = (String) itr.next();
                KwlReturnObject reqResult = accountingHandlerDAOobj.getObject(RequestForQuotation.class.getName(), reqid);
                purchaseOrder = (RequestForQuotation) reqResult.getEntityList().get(0);
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("invoiceID", purchaseOrder.getID());
                hashMap.put("companyid", purchaseOrder.getCompany().getCompanyID());
                JSONObject obj = new JSONObject();
                obj.put("billid", purchaseOrder.getID());
                obj.put("companyid", purchaseOrder.getCompany().getCompanyID());
                obj.put("companyname", purchaseOrder.getCompany().getCompanyName());
                obj.put("billno", purchaseOrder.getRfqNumber());
                obj.put("transactionNo", purchaseOrder.getRfqNumber());
                obj.put("isfavourite", purchaseOrder.isFavourite());
                obj.put("duedate", userdf.format(purchaseOrder.getDueDate()));
                obj.put("date", userdf.format(purchaseOrder.getRfqDate()));
                obj.put("archieve", purchaseOrder.getArchieve());
                obj.put("memo", purchaseOrder.getMemo());
                obj.put("deleted", purchaseOrder.isDeleted());
                obj.put("mergedCategoryData", "RFQ");  //type of data
                obj.put("type", linkType);
                obj.put("sequenceformatid", purchaseOrder.getSeqformat() == null ? "" : purchaseOrder.getSeqformat().getID());
                if (!StringUtil.isNullOrEmpty(purchaseOrder.getVendors())) {
                    HashMap<String, Object> vReqParams = new HashMap();
                    String vendors = "";
                    ArrayList filter_names = new ArrayList();
                    ArrayList filter_params = new ArrayList();
                    String[] vendorIds = purchaseOrder.getVendors().split(",");
                    for (int i = 0; i < vendorIds.length; i++) {
                        vendors += "'" + vendorIds[i] + "',";
                    }
                    vendors = vendors.substring(0, Math.max(0, vendors.length() - 1));
                    filter_names.add("INID");
                    filter_params.add(vendors);
                    vReqParams.put("filter_names", filter_names);
                    vReqParams.put("filter_params", filter_params);
                    KwlReturnObject result = accVendorDAOobj.getVendorList(vReqParams);
                    List<Vendor> vendorObjs = result.getEntityList();
                    String vendorName = "";
                    String aliasname = "";
                    String vendorEmails = "";
                    if (vendorObjs.size() > 0) {
                        for (Vendor vobj : vendorObjs) {
                            vendorName += StringUtil.isNullOrEmpty(vobj.getName()) ? "" : " " + vobj.getName() + ",";
                            aliasname += StringUtil.isNullOrEmpty(vobj.getAliasname()) ? "" : " " + vobj.getAliasname() + ",";
                            if (!StringUtil.isNullOrEmpty(vobj.getEmail())) {
                                vendorEmails += vobj.getEmail() + ";";
                            }
                        }
                        aliasname = aliasname.substring(0, Math.max(0, aliasname.length() - 1));
                        vendorName = vendorName.substring(0, Math.max(0, vendorName.length() - 1));
                        obj.put("personemail", vendorEmails);
                        obj.put("personname", vendorName);
                        obj.put("aliasname", aliasname);
                        obj.put("personid", purchaseOrder.getVendors());
                    }
                }
                jsonArray.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getPurchaseRequisitionJsonForLinking : " + ex.getMessage(), ex);
        }
        return jsonArray;
    }
    public String getLinkedRFQs(String companyid,String prId) {
        List<String> list = new ArrayList();
        String rfqList = "";
        HashMap<String, Object> params = new HashMap<String, Object>();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        KwlReturnObject kwlq = null;
        try {
            requestParams.put("isFromRFQReport", true);
            requestParams.put("includeRFQlinkedPR", true);
            requestParams.put("companyid", companyid);
            requestParams.put("prId", prId);
            kwlq = accLinkDataDao.getRFQ(requestParams, params);
            list = kwlq.getEntityList();
            for(String rfq : list){
                rfqList += rfq + ",";
            }
            if(!StringUtil.isNullOrEmpty(rfqList)){
                rfqList = rfqList.substring(0, rfqList.length() -1);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rfqList;
    }

    @Override     /*Request Dependency removed*/    
    public JSONArray getPODetailsItemJSON(JSONObject paramJobj, String SOID, HashMap<String, Object> paramMap) {

        JSONArray jArr = new JSONArray();
        String SOref = "", PRref = "",customerName="",customerCode="";
        String VQref = "";
        String allTerms = "";
        PurchaseOrderDetail row = null;
        ExpensePODetail exprow = null;
        StringBuilder appendtermString = new StringBuilder();
        double globalLevelExchangedRateTotalAmount = 0, globalLevelExchangedRateSubTotal = 0, globalLevelExchangedRateTotalTax = 0, globalLevelExchangedRateSubTotalwithDiscount = 0, globalLevelExchangedRateTermAmount = 0;
        JSONObject summaryData = new JSONObject();
        try {
            HashMap<String, Integer> FieldMap = (HashMap<String, Integer>) paramMap.get(Constants.fieldMap);
            HashMap<String, String> replaceFieldMap = (HashMap<String, String>) paramMap.get(Constants.replaceFieldMap);
            HashMap<String, Integer> DimensionFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.dimensionFieldMap);
            HashMap<String, Integer> LineLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.lineLevelCustomFieldMap);
            HashMap<String, Integer> ProductLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.productLevelCustomFieldMap);
            String companyid = paramJobj.optString(Constants.companyKey);
            java.util.Date entryDate = null, transactionDate = null;
            PdfTemplateConfig config = null;
            String billAddr = "", shipAddr = "", custshipaddr = "",companyAddr = "", createdby = "", vendortransactionalAddr = "", updatedby = "",purchaseReqCreator="";
            double quantityConstant = 0;
            Tax mainTax = null;
            double totalDiscount = 0;
            double totaltax = 0, TotalLineLevelTaxAmount = 0;
            double totalAmount = 0;
            double taxPercent = 0, subtotal = 0, subTotalWithDiscount = 0, poBalnceAmount = 0;
            String mainTaxName = "", approvername = "", globallevelcustomfields = "", globalleveldimensions = "", vendorTitle = "";
            boolean isgstincluded = false;
            int quantitydigitafterdecimal = 2, amountdigitafterdecimal = 2, unitpricedigitafterdecimal = 2;
            boolean isexpenseinv = false;

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(paramJobj);
            HashMap<String, Object> poRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), SOID);
            PurchaseOrder purchaseOrder = (PurchaseOrder) objItr.getEntityList().get(0);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.optString(Constants.globalCurrencyKey, "6"));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = currencyid = (purchaseOrder.getCurrency() == null) ? currency.getCurrencyID() : purchaseOrder.getCurrency().getCurrencyID();
            isexpenseinv = purchaseOrder.isIsExpenseType();
            //Document Currency
            summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, currencyid);
            /**
             * get customer title (Mr./Mrs.)
             */
            vendorTitle = purchaseOrder.getVendor().getTitle();
            if(!StringUtil.isNullOrEmpty(vendorTitle)){
                KwlReturnObject masterItemResult = kwlCommonTablesDAOObj.getObject(MasterItem.class.getName(), vendorTitle);
                MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                vendorTitle = masterItem.getValue();
            }
            filter_names.add("purchaseOrder.ID");
            filter_params.add(purchaseOrder.getID());
            poRequestParams.put("filter_names", filter_names);
            poRequestParams.put("filter_params", filter_params);
            poRequestParams.put("order_by", order_by);
            poRequestParams.put("order_type", order_type);
            KwlReturnObject podresult = null;
            if (!isexpenseinv) {
                podresult = accPurchaseOrderobj.getPurchaseOrderDetails(poRequestParams);
            } else {
                podresult = accPurchaseOrderobj.getExpensePurchaseOrderDetails(poRequestParams);
            }
            Iterator itr = podresult.getEntityList().iterator();
            List<PurchaseOrderDetail> list = podresult.getEntityList();

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
            if (purchaseOrder.getApprover() != null) {
                approvername = StringUtil.getFullName(purchaseOrder.getApprover());
            }
            createdby = purchaseOrder.getCreatedby() != null ? purchaseOrder.getCreatedby().getFullName() : "";
            updatedby = purchaseOrder.getModifiedby() != null ? purchaseOrder.getModifiedby().getFullName() : "";
            //companyposttext
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, Integer.parseInt(paramJobj.optString(Constants.moduleid, "0")));
            if (templateConfig.getEntityList().size() > 0) {
                config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            KwlReturnObject pref = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) pref.getEntityList().get(0);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            /*
            * Declaration of Variables to fetch Line level taxes and its information like
            * Tax Code, Tax Amount etc.
             */
            String allLineLevelTax = "", allLineLevelTaxAmount = "", allLineLevelTaxBasic = "", lineLevelTaxSign = "";
            Set<String> lineLevelTaxesGST = new HashSet<String>();
            Map<String, Object> lineLevelTaxAmountGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxPercentGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxBasicGST = new HashMap<String, Object>();
            Map<String,Double> lineLevelTaxNames = new LinkedHashMap<String,Double>();
            int countryid = 0;
            if( extraCompanyPreferences.getCompany()!= null && extraCompanyPreferences.getCompany().getCountry() != null ){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }

            double externalCurrencyRate = purchaseOrder.getExternalCurrencyRate();
            double revExchangeRate = 0;
            if(externalCurrencyRate!=0){
                revExchangeRate = 1/externalCurrencyRate;
            }
            int rowcnt = 0;
            Set<String> uniqueProductTaxList = new HashSet<String>();
            JSONObject detailsTableData = new JSONObject();
            JSONArray gstTaxSummaryDetailsTableDataArr = new JSONArray();
            while (itr.hasNext()) {
                if (!isexpenseinv) {
                    row = (PurchaseOrderDetail) itr.next();
                    String proddesc = "";
                    rowcnt++;
                    JSONObject obj = new JSONObject();
                    JSONObject gstTaxSummaryDetailsTableData = new JSONObject();
                    Product prod = row.getProduct();
                    double rate = 0, rowTaxPercent = 0, rowTaxAmt = 0, rowamountwithtax = 0, rowdiscountvalue = 0;
                    double rowamountwithgst = 0;
                    String rowTaxName = "", discountname = "";

                    obj.put(CustomDesignerConstants.SrNO, rowcnt);// Sr no
                    obj.put(CustomDesignerConstants.ProductName, prod.getName().replaceAll("\n", "<br>"));// productname
                    obj.put("type", prod.getProducttype() == null ? "" : prod.getProducttype().getName());
                    proddesc = StringUtil.isNullOrEmpty(row.getDescription()) ? (StringUtil.isNullOrEmpty(row.getProduct().getDescription()) ? "" : row.getProduct().getDescription()) : row.getDescription();
                    proddesc = StringUtil.DecodeText(proddesc);
                    obj.put(CustomDesignerConstants.ProductDescription, proddesc.replaceAll("\n", "<br>"));//Product Description
                    obj.put(CustomDesignerConstants.AdditionalDescription, !StringUtil.isNullOrEmpty(prod.getAdditionalDesc()) ? prod.getAdditionalDesc().replaceAll(Constants.REGEX_LINE_BREAK, "<br>") :"");  //product Addtional description ERP-39061
                    obj.put(CustomDesignerConstants.PO_ProductCode, prod.getProductid() == null ? "" : prod.getProductid());
                    obj.put(CustomDesignerConstants.HSCode, (prod.getHSCode() != null) ? prod.getHSCode().replaceAll("\n", "<br>") : "");//get HSCode of ProductGrid
                    obj.put(CustomDesignerConstants.PartNumber, StringUtil.isNullOrEmpty(prod.getCoilcraft()) ? "" : prod.getCoilcraft()); //Part Number
                    obj.put(CustomDesignerConstants.SupplierPartNumber, StringUtil.isNullOrEmpty(prod.getSupplier()) ? "" : prod.getSupplier());
                    obj.put(CustomDesignerConstants.CustomerPartNumber, StringUtil.isNullOrEmpty(prod.getInterplant()) ? "" : prod.getInterplant());
                    if (row.getPurchaseOrder().isGstIncluded()) {//if gstincluded is the case.
                        isgstincluded = true;
                    }
                    rate = row.getRate();

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
                    if (StringUtil.isNullOrEmpty(cateogry)) {
                        cateogry = "None";
                    }
                    obj.put("productCategory", cateogry);
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
                    filter_params.add(Integer.parseInt(paramJobj.optString(Constants.moduleid, "0")));
                    params.put("filter_names", filter_names);
                    params.put("filter_values", filter_params);

                    KwlReturnObject fieldparams = accAccountDAOobj.getFieldParams(params);
                    List fieldParamsList = fieldparams.getEntityList();

                    JSONArray jsonarr = new JSONArray();
                    for (int cnt = 0; cnt < fieldParamsList.size(); cnt++) {
                        FieldParams fieldParamsObj = (FieldParams) fieldParamsList.get(cnt);
                        JSONObject tempObj = new JSONObject();
                        tempObj.put("comboName", fieldParamsObj.getFieldname());
                        jsonarr.put(tempObj);
                    }

                    obj.put("groupingComboList", jsonarr);

                    obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(rate, companyid));// Rate
                    obj.put(CustomDesignerConstants.RATEINCLUDINGGST, authHandler.formattingDecimalForUnitPrice(row.getRateincludegst(), companyid));// Rate including GST
                    String uom = row.getUom() == null ? (row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA()) : row.getUom().getNameEmptyforNA();
                    String baseuom = row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                    double quantity = row.getQuantity();
                    double baseuomrate = row.getBaseuomrate();
                    double baseQuantity = quantity * baseuomrate;
                    double rowamountwithouttax = authHandler.round((rate * quantity), companyid);//amount without tax
                    /*
                     * Balance Qty
                     */
                    double balanceQty = row.getBalanceqty();

                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, rate, currencyid, entryDate, 0);
                    if (row.getDiscountispercent() == 1) {
                        discountname = CustomDesignHandler.getAmountinCommaDecimal(row.getDiscount(), 0, countryid) + "%";//to return 0 no of zeros
                    } else {
                        discountname = purchaseOrder.getCurrency().getSymbol() + " " + authHandler.formattingDecimalForAmount(row.getDiscount(), companyid);//to show as it is in UI
                    }

//                rate=rateInCurr;
                    obj.put(CustomDesignerConstants.BaseQty, authHandler.formattingDecimalForQuantity(baseQuantity, companyid));
                    obj.put(CustomDesignerConstants.BaseQtyWithUOM, authHandler.formattingDecimalForQuantity(baseQuantity, companyid) + " " + baseuom);//base quantity wiyh uom
                    obj.put(CustomDesignerConstants.QuantitywithUOM, authHandler.formattingDecimalForQuantity(quantity, companyid) + " " + uom); // Quantity
                    /*
                     * Balance Qty
                     */
                    obj.put(CustomDesignerConstants.BalanceQty, authHandler.formattingDecimalForQuantity(balanceQty, companyid));
                    /*
                     * Balance Qty With UOM
                     */
                    obj.put(CustomDesignerConstants.BalanceQtyWithUOM, authHandler.formattingDecimalForQuantity(balanceQty, companyid) + " " + uom);
                    
                    quantityConstant += quantity;
                    obj.put(CustomDesignerConstants.Discountname, discountname);// Discount
                    obj.put(CustomDesignerConstants.IN_Currency, purchaseOrder.getCurrency().getCurrencyCode());
                    obj.put(CustomDesignerConstants.PO_Quantity, authHandler.formattingDecimalForQuantity(quantity, companyid));
                    obj.put(CustomDesignerConstants.PO_UOM, uom);
                    obj.put("currencysymbol", purchaseOrder.getCurrency().getSymbol()); //used for headercurrency and recordcurrency
                    obj.put("isGstIncluded", isgstincluded);
                    obj.put("currencycode", purchaseOrder.getCurrency().getCurrencyCode()); //used for headercurrencycode and recordcurrency
                    String stockUOM = "";
                    if (prod.getUnitOfMeasure() != null) {
                        stockUOM = prod.getUnitOfMeasure().getNameEmptyforNA();
                    }
                    obj.put(CustomDesignerConstants.ProductAvailableQuantity, authHandler.round(prod.getAvailableQuantity(), companyid) + " " + stockUOM); //Product Avaiable quantity in Stock UOM

                    /*
                     * In include GST case calculations are in reverse order In
                     * other cases calculations are in forward order
                     */
                    if (row.getPurchaseOrder().isGstIncluded()) {//if gstincluded is the case
                        rowamountwithgst = row.getRateincludegst() * quantity;
                        rowdiscountvalue = (row.getDiscountispercent() == 1) ? rowamountwithgst * row.getDiscount() / 100 : row.getDiscount();
                        obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(rowamountwithgst - row.getRowTaxAmount(), companyid));//Subtotal
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.formattingDecimalForAmount((rowamountwithgst - rowdiscountvalue - row.getRowTaxAmount()), companyid));//Subtotal
                        subtotal += authHandler.round(rowamountwithgst - row.getRowTaxAmount(), companyid);
                        subTotalWithDiscount = authHandler.round((rowamountwithgst - rowdiscountvalue - row.getRowTaxAmount()), companyid);
                    } else {
                        rowdiscountvalue = (row.getDiscountispercent() == 1) ? rowamountwithouttax * row.getDiscount() / 100 : row.getDiscount();
                        obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(rowamountwithouttax, companyid));//Subtotal
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.formattingDecimalForAmount((rowamountwithouttax - rowdiscountvalue), companyid));//Subtotal
                        subtotal += authHandler.round(rowamountwithouttax, companyid);//rounded becuase totaltax & amount are rounded and saved in db
                        subTotalWithDiscount = authHandler.round((rowamountwithouttax - rowdiscountvalue), companyid);
                    }
                    totalDiscount += authHandler.round(rowdiscountvalue, companyid);
                    obj.put(CustomDesignerConstants.IN_Discount, authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));// Discount
                    String baseUrl = com.krawler.common.util.URLUtil.getDomainURL(paramJobj.optString(Constants.RES_CDOMAIN), false);
                    /*
                     * Following code is to check whether the image is predent
                     * for product or not. If Image is not present sent s.gif
                     * instead of product id
                     */
                    String fileName = null;
                    fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages + prod.getID() + ".png";
                    File file = new File(fileName);
                    FileInputStream in = null;
                    String filePathString = "";
                    try {
                        in = new FileInputStream(file);
                    } catch (java.io.FileNotFoundException ex) {
                        //catched exception if file not found, and to continue for rest of the products
                        filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                    }
                    if (in != null) {
                        filePathString = baseUrl + "productimage?fname=" + prod.getID() + ".png&isDocumentDesignerPrint=true";
                    } else {
                        filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                    }
                    obj.put(CustomDesignerConstants.imageTag, filePathString);

                    entryDate = purchaseOrder.getOrderDate();
                    if (row != null && row.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", row.getTax().getID());
                        uniqueProductTaxList.add(row.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        rowTaxName = row.getTax().getName();
                        rowTaxAmt = row.getRowTaxAmount();
                    }
                    totaltax += authHandler.round(rowTaxAmt, companyid);//Calculate tax amount from line item
                    if (row.getPurchaseOrder().isGstIncluded()) {
                        rowamountwithtax = rowamountwithgst; //Amount will be equal to rowamountwithouttax because tax gets added in gst
                    } else {
                        if (rowdiscountvalue != 0) {
                            rowamountwithouttax -= authHandler.round(rowdiscountvalue, companyid);//deducting discount if any
                        }
                        rowamountwithtax = rowamountwithouttax + rowTaxAmt;
                    }

                    String lineLevelTax = "";
                    String lineLevelTaxPercent = "";
                    String lineLevelTaxAmount = "";
                    double lineLevelTaxAmountTotal = 0;
                    /*
                     * Check For new Gst flow 
                     */
                    if(extraCompanyPreferences.isIsNewGST()) { // For new Gst flow 
                        HashMap<String, Object> PODetailParams = new HashMap<String, Object>();
                        PODetailParams.put("podetails", row.getID());
                        PODetailParams.put("orderbyadditionaltax", true);
                        // GST
                        PODetailParams.put("termtype", 7);
                        KwlReturnObject grdTermMapresult = accPurchaseOrderobj.getPurchaseOrderDetailsTermMap(PODetailParams);
                        List<PurchaseOrderDetailsTermMap> gst = grdTermMapresult.getEntityList();
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
                        for (PurchaseOrderDetailsTermMap podetailTermMap : gst) {
                            LineLevelTerms mt = podetailTermMap.getTerm();
                            if (mt.getTerm().contains(CustomDesignerConstants.CGST)) {
                                obj.put(CustomDesignerConstants.CGSTPERCENT, podetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.CGSTAMOUNT, podetailTermMap.getTermamount());
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, podetailTermMap.getPercentage());
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, podetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.IGST)) {
                                obj.put(CustomDesignerConstants.IGSTPERCENT, podetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.IGSTAMOUNT, podetailTermMap.getTermamount());
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, podetailTermMap.getPercentage());
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, podetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.SGST)) {
                                obj.put(CustomDesignerConstants.SGSTPERCENT, podetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.SGSTAMOUNT, podetailTermMap.getTermamount());
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, podetailTermMap.getPercentage());
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, podetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.UTGST)) {
                                obj.put(CustomDesignerConstants.UTGSTPERCENT, podetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.UTGSTAMOUNT, podetailTermMap.getTermamount());
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, podetailTermMap.getPercentage());
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, podetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.CESS)) {
                                obj.put(CustomDesignerConstants.CESSPERCENT, podetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.CESSAMOUNT, podetailTermMap.getTermamount());
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, podetailTermMap.getPercentage());
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, podetailTermMap.getTermamount());
                            }
                            lineLevelTax += mt.getTerm();
                            lineLevelTax += "!## ";

                            lineLevelTaxPercent += authHandler.formattingDecimalForAmount(podetailTermMap.getPercentage(), companyid);
                            lineLevelTaxPercent += "!## ";
                            lineLevelTaxAmount += CustomDesignHandler.getAmountinCommaDecimal(podetailTermMap.getTermamount(), amountdigitafterdecimal, countryid);
                            lineLevelTaxAmount += "!## ";
                            /*
                             * calculating total of line level taxes
                             */
                            lineLevelTaxAmountTotal += podetailTermMap.getTermamount();
                            if (lineLevelTaxNames.containsKey(mt.getTerm()) && lineLevelTaxNames.get(mt.getTerm()) != null) {
                                double value = lineLevelTaxNames.get(mt.getTerm());
                                lineLevelTaxNames.put(mt.getTerm(), podetailTermMap.getTermamount() + value);

                            } else {
                                lineLevelTaxNames.put(mt.getTerm(), podetailTermMap.getTermamount());
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
                        obj.put("isFixedAsset", paramJobj.optBoolean("isFixedAsset", false));
                        ExportRecordHandler.setHsnSacProductDimensionField(prod, obj, companyid, accAccountDAOobj, kwlCommonTablesDAOObj);
                        gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.HSN_SAC_CODE, obj.optString(CustomDesignerConstants.HSN_SAC_CODE, ""));
                        gstTaxSummaryDetailsTableDataArr.put(gstTaxSummaryDetailsTableData);
                    } else {
                        /*
                         * Fetching distinct taxes used at line level, feetched
                         * in the set Also, fetched the information related to
                         * tax in different maps
                         */
                        double rowTaxPercentGST = 0.0;
                        if (row.getTax() != null) {
                            String taxCode = row.getTax().getTaxCode();
                            if (!lineLevelTaxesGST.contains(taxCode)) {
                                lineLevelTaxesGST.add(taxCode);
                                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, purchaseOrder.getOrderDate(), row.getTax().getID());
                                rowTaxPercentGST = (Double) perresult.getEntityList().get(0);
                                lineLevelTaxPercentGST.put(taxCode, rowTaxPercentGST);
                                lineLevelTaxAmountGST.put(taxCode, 0.0);
                                lineLevelTaxBasicGST.put(taxCode, 0.0);
                            }
                            lineLevelTaxAmountGST.put(taxCode, (Double) lineLevelTaxAmountGST.get(taxCode) + row.getRowTaxAmount());
                            lineLevelTaxBasicGST.put(taxCode, (Double) lineLevelTaxBasicGST.get(taxCode) + obj.optDouble(CustomDesignerConstants.SUBTOTAL, 0.0));
                            /*
                             * putting subtotal+tax
                             */
                            obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax), companyid) + authHandler.round(row.getRowTaxAmount(), companyid)); 
                        }
                    }
                    
                    obj.put(CustomDesignerConstants.SpecificCurrencyExchangeRate, 1);
                    obj.put("gstCurrencyRate", "");
                    summaryData.put("gstCurrencyRate", "");
                    transactionDate = purchaseOrder.getOrderDate();
                    obj.put("transactiondate", transactionDate); // Amount
                    summaryData.put("transactiondate", transactionDate); // Amount

                    double exchangerateunitprice = 0, exchangeratelineitemsubtotal = 0, exchangeratelineitemdiscount = 0, exchangeratelineitemamount = 0, exchangeratelineitemtax = 0, exchangeratelineitemsubtotalwithdiscount = 0;
                    /*
                     * <--------------------Base Currency Unit Price,Base
                     * currency Subtotal ----------->
                     */
                    /*
                     * Exchange Rate Section---- We have not given amount with
                     * tax because it is matched with UI
                     */
                    if (externalCurrencyRate != 0) {
                        //Unit Price
                        exchangerateunitprice = rate * revExchangeRate;  //exchanged rate unit rate
                        //SUbTotal (rate*quantity)
                        exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                        //Exhanged Rate Discount
                        exchangeratelineitemdiscount = authHandler.round((rowdiscountvalue * revExchangeRate), companyid);//exchange rate total discount
                        if (row.getPurchaseOrder().isGstIncluded()) {
                            double exchangeRateincludegst = row.getRateincludegst() * revExchangeRate;
                            double exchangerowamount= (exchangeRateincludegst*quantity)-(rowTaxAmt * revExchangeRate);
                            exchangeratelineitemsubtotal = exchangerowamount;
                            exchangeratelineitemsubtotalwithdiscount = exchangerowamount - exchangeratelineitemdiscount;
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                        } else {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                        }
                        
                        //Exhanged Rate Tax Amount
                        exchangeratelineitemtax = extraCompanyPreferences.isIsNewGST() ? authHandler.round((lineLevelTaxAmountTotal * revExchangeRate), companyid) : authHandler.round((rowTaxAmt * revExchangeRate), companyid);
                        
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, authHandler.formattingDecimalForUnitPrice(exchangerateunitprice,companyid));
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, authHandler.formattingDecimalForAmount(exchangeratelineitemtax,companyid));
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);
                        
                        obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, authHandler.formattingDecimalForUnitPrice(exchangerateunitprice,companyid));
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, authHandler.formattingDecimalForAmount(exchangeratelineitemtax,companyid));
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
                        if (row.getPurchaseOrder().isGstIncluded()) {
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

                    obj.put(CustomDesignerConstants.Amount, authHandler.formattingDecimalForAmount(rowamountwithtax, companyid)); // Amount
                    obj.put(CustomDesignerConstants.IN_Tax, rowTaxAmt);// Tax
                    obj.put(CustomDesignerConstants.IN_ProductTax, rowTaxName);
                    // Sub Total product level (Amount excluding Tax)

                    if (row.getSalesorderdetailid() != null) {
                        KwlReturnObject sodetailsitr = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), row.getSalesorderdetailid());
                        SalesOrderDetail sod = (SalesOrderDetail) sodetailsitr.getEntityList().get(0);
                        if (sod != null) {
                            KwlReturnObject soresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), sod.getSalesOrder().getID());
                            SalesOrder so = (SalesOrder) soresult.getEntityList().get(0);
                            if (SOref.indexOf(so.getSalesOrderNumber()) == -1) {
                                SOref += so.getSalesOrderNumber() + ", ";
                                /*
                                 * Multiple  SO of Same Customer Customer should be print one time
                                 */
                                customerName += so.getCustomer() != null ? (!customerName.contains(so.getCustomer().getName())? so.getCustomer().getName()+ ", ":""):"";
                                customerCode += so.getCustomer()!= null ? (!customerCode.contains(so.getCustomer().getAcccode())?so.getCustomer().getAcccode()+ ", ":""):"";
                            }
                        }
                    }
                    if (row.getVqdetail() != null) {
                        if (VQref.indexOf(row.getVqdetail().getVendorquotation().getQuotationNumber()) == -1) {
                            VQref += row.getVqdetail().getVendorquotation().getQuotationNumber() + ", ";
                        }
                    }
                    
                    //For Linked Purchase Requisition
                    String PurchaseReqId="";
                    if (row.getPurchaseRequisitionDetailId() != null) {
                        PurchaseReqId = row.getPurchaseRequisitionDetailId();
                    } else if (row.getVqdetail() != null) {
                        PurchaseReqId = row.getVqdetail().getPurchaseRequisitionDetailsId();
                    }
                    if (PurchaseReqId != null) {
                        KwlReturnObject prdetailsitr = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), PurchaseReqId);
                        PurchaseRequisitionDetail prd = (PurchaseRequisitionDetail) prdetailsitr.getEntityList().get(0);
                        if (prd != null) {
                            KwlReturnObject prresult = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), prd.getPurchaserequisition().getID());
                            PurchaseRequisition pr = (PurchaseRequisition) prresult.getEntityList().get(0);
                            if (PRref.indexOf(pr.getPrNumber()) == -1) {
                                PRref += pr.getPrNumber() + ", ";
                                purchaseReqCreator = pr.getCreatedby().getFullName();
                            }
                        }
                    }
                    
                    /*
                     * get custom line data
                     */
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    PurchaseOrderDetailsCustomData jeDetailCustom = (PurchaseOrderDetailsCustomData) row.getPoDetailCustomData();
                    replaceFieldMap = new HashMap<String, String>();
                    if (jeDetailCustom != null) {
                        ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(paramJobj, jeDetailCustom, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                    }

                    /*
                     * Product Level Custom Fields Evaluation
                     */
                    KwlReturnObject resultProduct = accountingHandlerDAOobj.getObject(PurchaseOrderDetailProductCustomData.class.getName(), row.getID());
                    PurchaseOrderDetailProductCustomData qProductDetailCustom = (PurchaseOrderDetailProductCustomData) resultProduct.getEntityList().get(0);
                    replaceFieldMap = new HashMap<String, String>();
                    if (qProductDetailCustom != null) {
                        ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(paramJobj, qProductDetailCustom, ProductLevelCustomFieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                    }

                    /*
                     * Set All Line level Dimension & All LIne level Custom
                     * Field Values
                     */
                    obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap, variableMap, obj, false);//for dimensions
                    obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap, variableMap, obj, true);//for customfields
                                   
                    JSONObject jObj = null;
                    if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                        jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());

                        if (jObj.has(Constants.isDisplayUOM) && jObj.get(Constants.isDisplayUOM) != null && (Boolean) jObj.get(Constants.isDisplayUOM) != false) {
                            obj = accProductObj.getProductDisplayUOM(row.getProduct(), row.getQuantity(), row.getBaseuomrate(), true, obj);
                        }
                    }
                    jArr.put(obj);
                } else { //EXPENSE
                    exprow = (ExpensePODetail) itr.next();
                    rowcnt++;
                    JSONObject obj = new JSONObject();
                    String accountdesc = "", discountname = "";
                    double rate = 0, rowTaxPercent = 0, rowTaxAmount = 0, rowdiscountvalue = 0;
                    double rateforAmount=0;
                    rate = exprow.getRate();
                    rateforAmount=rate;
                    Discount disc = exprow.getDiscount();
                    poBalnceAmount += exprow.getBalAmount();
                    if (disc != null) {   //For Discount Row
                        //If discount is selected, then line level total will be "Total-Discount"
                        rowdiscountvalue = exprow.getDiscount().getDiscountValue();
                        //rate = rate - rowdiscountvalue;
                         rateforAmount=rate - rowdiscountvalue;

                        if (exprow.getDiscount().isInPercent()) {
                            discountname = CustomDesignHandler.getAmountinCommaDecimal(exprow.getDiscount().getDiscount(), 0, countryid) + "%";//to return 0 no of zeros
                        } else {
                            discountname = purchaseOrder.getCurrency().getSymbol() + " " + authHandler.formattingDecimalForAmount(exprow.getDiscount().getDiscount(), companyid);//to show as it is in UI
                        }
                        totalDiscount += rowdiscountvalue;
                        obj.put(CustomDesignerConstants.Discountname, discountname);// Discount Name
                        obj.put(CustomDesignerConstants.IN_Discount, authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));// Discount
                    }
                    //Calculate Row Tax
                    String rowTaxName = "";
                    if (exprow != null && exprow.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", exprow.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        rowTaxName = exprow.getTax().getName();
                        rowTaxAmount = exprow.getRowTaxAmount();
                    }

                    // ## Get Custom Field Data 
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                    ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                    Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                    Detailfilter_params.add(exprow.getID());
                    invDetailRequestParams.put("filter_names", Detailfilter_names);
                    invDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresult = accGoodsReceiptobj.getExpensePOCustomData(invDetailRequestParams);
                    if (idcustresult.getEntityList().size() > 0) {
                        ExpensePODetailCustomData jeDetailCustom = (ExpensePODetailCustomData) idcustresult.getEntityList().get(0);
                        AccountingManager.setCustColValuesForExport(paramJobj, jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                            String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                            String valueForReport = "";
                            if (FieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                                FieldComboData fieldComboData = null;
                                try {
                                    String[] valueData = coldata.split(",");
                                    for (String value : valueData) {
                                        fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), value);
                                        if (fieldComboData != null) {
                                            valueForReport += fieldComboData.getValue() + "!##";
                                        }
                                    }
                                    if (valueForReport.length() > 1) {
                                        valueForReport = valueForReport.substring(0, valueForReport.length() - 3);
                                    }
                                    if (fieldComboData != null) {
                                        obj.put(varEntry.getKey(), valueForReport);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                                    } else {
                                        obj.put(varEntry.getKey(), varEntry.getValue() != null ? varEntry.getValue().toString() : "");
                                    }
                                } catch (Exception ex) {
                                    obj.put(varEntry.getKey(), coldata);
                                }
                            } else {
                                obj.put(varEntry.getKey(), coldata != null ? coldata : "");
                            }
                        }
                    }
                    obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap, variableMap, obj, false);//for dimensions
                    obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap, variableMap, obj, true);//for customfields

                    if (exprow.isIsdebit()) {
                        totaltax += rowTaxAmount;//Calculate tax amount from line item
                        if (exprow.getPurchaseOrder().isGstIncluded()) {
                            subtotal += (rate - rowTaxAmount);
                        } else {
                            subtotal += rate;//discounted subtotal
                        }
                    } else {
                        totaltax -= rowTaxAmount;//Calculate tax amount from line item
                        if (exprow.getPurchaseOrder().isGstIncluded()) {
                            subtotal -= (rate - rowTaxAmount);
                        } else {
                            subtotal -= rate;//discounted subtotal
                        }
                         totalDiscount = totalDiscount-2*rowdiscountvalue;//Need to substract discount value also in case of Credit Account
                    }
                    accountdesc = StringUtil.isNullOrEmpty(exprow.getDescription()) ? "" : exprow.getDescription();
                    accountdesc = StringUtil.DecodeText(accountdesc);
                    obj.put("currencysymbol", purchaseOrder.getCurrency().getSymbol());
                    obj.put("currencycode", purchaseOrder.getCurrency().getCurrencyCode());
                    obj.put("isGstIncluded", purchaseOrder.isGstIncluded());//used for headercurrency & record currency
                    obj.put(CustomDesignerConstants.SrNO, rowcnt);// Sr No
                    obj.put(CustomDesignerConstants.ProductName, exprow.getAccount().getName());
                    obj.put(CustomDesignerConstants.PO_ProductCode, exprow.getAccount().getAccountCode());
                    obj.put(CustomDesignerConstants.ProductDescription, accountdesc.replaceAll("\n", "<br>"));//Account Description
                    obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(exprow.getRate(), companyid));// Rate
                    obj.put(CustomDesignerConstants.RATEINCLUDINGGST, authHandler.formattingDecimalForUnitPrice(exprow.getRateIncludingGst(), companyid));// Rate including GST
                    obj.put(CustomDesignerConstants.IN_Currency, purchaseOrder.getCurrency().getCurrencyCode());
                    obj.put(CustomDesignerConstants.IN_Tax, rowTaxAmount);//Row TaxAmount
                    obj.put(CustomDesignerConstants.IN_ProductTax, rowTaxName);
                    obj.put(CustomDesignerConstants.Amount, authHandler.formattingDecimalForAmount(rateforAmount, companyid)); // Amount
                    jArr.put(obj);

                }
            }
            detailsTableData.put("gsttaxsummary", gstTaxSummaryDetailsTableDataArr);
            detailsTableData.put("isDetailsTableData", true);
            jArr.put(detailsTableData);
            
            if (!StringUtil.isNullOrEmpty(SOID)) {
                
                List poTermMapList = kwlCommonTablesDAOObj.getSummationOfTermAmtAndTermTaxAmt(Constants.purchaseordertermmap, SOID);
                if(poTermMapList != null && !poTermMapList.isEmpty()){
                    Iterator termItr = poTermMapList.iterator();
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
                        if(obj[4] != null){
                            totaltax += (Double) obj[2];
                        }
                    }
                }
            }
            mainTax = purchaseOrder.getTax();
            if(mainTax!=null){ //Get tax percent
                requestParams.put("transactiondate", entryDate);
                requestParams.put("taxid", mainTax.getID());
                KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                List taxList = result1.getEntityList();
                Object[] taxObj=(Object[]) taxList.get(0);
                taxPercent =taxObj[1]==null?0:(Double) taxObj[1];
                mainTaxName=mainTax.getName();
            
                totalAmount = subtotal - totalDiscount;
           
                totaltax += (taxPercent == 0 ? 0 : totalAmount * taxPercent / 100);//overall tax calculate
                if (externalCurrencyRate != 0) {
                    globalLevelExchangedRateTotalTax = authHandler.round((authHandler.round(totaltax,companyid) * revExchangeRate), companyid);//exchanged rate total tax amount 
                } else {
                    globalLevelExchangedRateTotalTax = authHandler.round((totaltax), companyid);//exchanged rate total tax amount 
                }

            }
            mainTaxName=purchaseOrder.getTax() != null ?purchaseOrder.getTax().getName():"";
          
            totalAmount = subtotal + totaltax - totalDiscount;
        
            KwlReturnObject basecurresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.optString(Constants.globalCurrencyKey, "6"));
            KWLCurrency baseCur = (KWLCurrency) basecurresult.getEntityList().get(0);
            String baseCurrency = baseCur.getSymbol();

            /*
             * Invoice Terms Calculation
             */
            
            Map<String, Object> taxListParams = new HashMap<String, Object>();
            taxListParams.put("companyid", companyid);
            boolean isApplyTaxToTerms=purchaseOrder.isApplyTaxToTerms();

            HashMap<String, Object> filterrequestParams = new HashMap();
            filterrequestParams.put("taxid", purchaseOrder.getTax()==null?"":purchaseOrder.getTax().getID());
            
//            boolean isTaxTermMapped = false;
//            double lineleveltermTaxAmount = 0;
//            double termAmountBeforeTax = 0;
//            double termAmountAfterTax = 0;

            double totalTermAmount = 0;
            String term = "", termsNameWithoutPercent= "",termsamount = "",termssign="",termsName= "";
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("purchaseOrder", purchaseOrder.getID());
            KwlReturnObject purchaseOrderResult = null;
            purchaseOrderResult = accPurchaseOrderobj.getPurchaseOrderTermMap(requestParam);
            List<PurchaseOrderTermMap> termMap = purchaseOrderResult.getEntityList();
            for (PurchaseOrderTermMap purchaseOrderTermMap : termMap) {
                InvoiceTermsSales mt = purchaseOrderTermMap.getTerm();
                double termAmnt = 0;
                if(purchaseOrder.isGstIncluded()){
                    termAmnt = purchaseOrderTermMap.getTermAmountExcludingTax();
                }else{
                    termAmnt = purchaseOrderTermMap.getTermamount();
                }
                
                filterrequestParams.put("term", purchaseOrderTermMap.getTerm() == null ? "" : purchaseOrderTermMap.getTerm().getId());
                filterrequestParams.put("companyid", companyid);

//                if (uniqueProductTaxList.size() > 0 && isApplyTaxToTerms) {
//                    for (String taxId : uniqueProductTaxList) {
//                        filterrequestParams.put("taxid", taxId);
//                        taxListParams.put("taxid", taxId);
//                        isTaxTermMapped = accTaxObj.isTermMappedwithTax(filterrequestParams);
//                        if (isTaxTermMapped) {
//                            KwlReturnObject taxListResult = accTaxObj.getTaxList(taxListParams);
//                            if (taxListResult != null && taxListResult.getEntityList() != null) {
//                                List<TaxList> taxListPercent = taxListResult.getEntityList();
//                                lineleveltermTaxAmount += (termAmnt * (taxListPercent.get(0).getPercent())) / 100;
//                            }
//                        }
//                    }
//                } else {
//                    isTaxTermMapped = accTaxObj.isTermMappedwithTax(filterrequestParams);
//                }

//                if (isTaxTermMapped) { // If term is mapped with tax
//                    termAmountBeforeTax += purchaseOrderTermMap.getTermamount(); // term amount for adding before tax calculation
//                } else {
//                    termAmountAfterTax += purchaseOrderTermMap.getTermamount(); // term amount for adding after tax calculation
//                }

                
                String termName = (mt.getTerm() + (termAmnt > 0 ? "(+)" : "(-)"));
                summaryData.put(mt.getTerm(), termAmnt);
                summaryData.put(CustomDesignerConstants.BaseCurrency + mt.getTerm(), (termAmnt * revExchangeRate));//Base currency exchange rate term value-ERP-13451
                totalTermAmount += termAmnt;
                double exchangeratetermamount = 0;
                if (externalCurrencyRate != 0) {
                    exchangeratetermamount = (termAmnt * revExchangeRate);
                } else {
                    exchangeratetermamount = termAmnt;
                }
                globalLevelExchangedRateTermAmount += exchangeratetermamount;
                double tempTermValue = (termAmnt > 0 ? termAmnt : (termAmnt * -1));
                term += "<div> <table><tr><td>" + termName + " : </td><td>" + Double.toString(tempTermValue) + "</td></tr></table></div><br>";
                termsNameWithoutPercent += mt.getTerm() +"!## ";
                if(termAmnt > 0){
                    termssign +="+!## ";
                } else{
                    termssign +="-&nbsp;!## ";
                }
                termsName += mt.getTerm() + " " + purchaseOrderTermMap.getPercentage() + "%, ";
                termsamount += CustomDesignHandler.getAmountinCommaDecimal(Math.abs(termAmnt), amountdigitafterdecimal,countryid) +"!## ";
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
            if (!StringUtil.isNullOrEmpty(termsNameWithoutPercent)) {
                termsNameWithoutPercent = termsNameWithoutPercent.substring(0, termsNameWithoutPercent.length() - 4);
            }
            if (!StringUtil.isNullOrEmpty(termssign)) {
                termssign = termssign.substring(0, termssign.length() - 4);
            }
            if (!StringUtil.isNullOrEmpty(termsamount)) {
                termsamount = termsamount.substring(0, termsamount.length() - 4);
            }
//            if(isTaxTermMapped){ // If tax mapped with any term
//                if (mainTax != null) {
//                    totaltax = (taxPercent == 0 ? 0 : ((subtotal - totalDiscount) + termAmountBeforeTax) * taxPercent /100); // first add term into subtotal then calculate tax
//                }
//                totaltax += lineleveltermTaxAmount;
//                totalAmount = subtotal - totalDiscount + termAmountBeforeTax + totaltax + termAmountAfterTax; // first add mapped terms amount in subtotal then add total tax then add unmapped terms amount for total amount
//            } else{
                totalAmount = totalAmount + totalTermAmount;
//            }
            globalLevelExchangedRateTermAmount = authHandler.round(globalLevelExchangedRateTermAmount, companyid);
            /*Base Currency Total Amount */
            globalLevelExchangedRateTotalAmount = authHandler.round(globalLevelExchangedRateSubTotalwithDiscount, companyid) + authHandler.round(globalLevelExchangedRateTotalTax, companyid) + authHandler.round(globalLevelExchangedRateTermAmount, companyid);
            if(!StringUtil.isNullOrEmpty(appendtermString.toString())){
                allTerms=appendtermString.toString();
            }

            /*
             * Check for Indian and USA tax calculation
             */
            if(extraCompanyPreferences.isIsNewGST()) { // For new Gst flow 
                if(!lineLevelTaxNames.isEmpty()){
                    Iterator lineTax = lineLevelTaxNames.entrySet().iterator();
                    while(lineTax.hasNext()){
                        Map.Entry tax = (Map.Entry)lineTax.next();
                        allLineLevelTax += tax.getKey();
                        allLineLevelTax += "!## ";
                        double taxamount = (double)tax.getValue();
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
            } else {
                /*
                 * Putting all line taxes and its information in summary data
                 * separated by !##
                 */
                for (String key : lineLevelTaxesGST) {
                    allLineLevelTax += key + "!##";
                    allLineLevelTaxAmount += lineLevelTaxAmountGST.get(key).toString() + "!##";
                    allLineLevelTaxBasic += lineLevelTaxBasicGST.get(key).toString() + "!##";
                }
            }
            if(extraCompanyPreferences.isIsNewGST()) { // For new Gst flow 
                totalAmount = totalAmount + TotalLineLevelTaxAmount;
                totaltax = TotalLineLevelTaxAmount;
            }
            /**
             * SDP-13772
             * Add Rounding Adjustment in Total Amount
             */
            if(purchaseOrder.isIsRoundingAdjustmentApplied()){
                totalAmount = totalAmount + purchaseOrder.getRoundingadjustmentamount();
            }
            DateFormat df = authHandler.getUserDateFormatterJson(paramJobj);//User Date Formatter
            Date date = new Date();
            String printedOn = df.format(date);

            String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAmount)), currency, countryLanguageId);
            /*
             * Get amount in indonesian words.
             */
            String indonesianAmountInWords = "";
            if(countryid == Constants.INDONESIAN_COUNTRY_ID){
                KWLCurrency indoCurrency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), Constants.CountryIndonesianCurrencyId);
                indonesianAmountInWords = IndonesianNumberToWordsOjb.indonesiaConvert(Double.parseDouble(String.valueOf(totalAmount)), indoCurrency);
            }
            String gstAmountInWords = EnglishNumberToWordsOjb.convert(totaltax, currency, countryLanguageId);
            billAddr = CommonFunctions.getTotalBillingShippingAddress(purchaseOrder.getBillingShippingAddresses(), true);
            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            addrRequestParams.put("vendorid", purchaseOrder.getVendor().getID());
            addrRequestParams.put("companyid", companyid);
            KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
            List<AddressDetails> addressResultList = addressResult.getEntityList();
            CommonFunctions.getAddressSummaryData(addressResultList, summaryData,preferences, extraCompanyPreferences);
            
            int moduleid = 0;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.moduleid,null))) {
                moduleid = paramJobj.optInt(Constants.moduleid,0);
            }
            if (moduleid == Constants.Acc_Purchase_Order_ModuleId) {
                DocumentEmailSettings documentEmailSettings = null;
                KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), companyid);
                documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
                if (documentEmailSettings != null && documentEmailSettings.isCustShippingAddressInPurDoc()) {
                    BillingShippingAddresses addresses = purchaseOrder.getBillingShippingAddresses();
                    custshipaddr = CommonFunctions.getTotalCustomerShippingAddressFromPurchaseDocument(purchaseOrder.getBillingShippingAddresses());
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_ADDRESS, !StringUtil.isNullOrEmpty(addresses.getCustomerShippingAddress()) ? addresses.getCustomerShippingAddress() : "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_STATE, !StringUtil.isNullOrEmpty(addresses.getCustomerShippingState()) ? addresses.getCustomerShippingState() : "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_COUNTRY, !StringUtil.isNullOrEmpty(addresses.getCustomerShippingCountry()) ? addresses.getCustomerShippingCountry() : "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_CITY, !StringUtil.isNullOrEmpty(addresses.getCustomerShippingCity()) ? addresses.getCustomerShippingCity() : "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_EMAIL, !StringUtil.isNullOrEmpty(addresses.getCustomerShippingEmail()) ? addresses.getCustomerShippingEmail() : "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_FAX, !StringUtil.isNullOrEmpty(addresses.getCustomerShippingFax()) ? addresses.getCustomerShippingFax() : "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_POSTAL, !StringUtil.isNullOrEmpty(addresses.getCustomerShippingPostal()) ? addresses.getCustomerShippingPostal() : "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_MOBILE, !StringUtil.isNullOrEmpty(addresses.getCustomerShippingMobile()) ? addresses.getCustomerShippingMobile() : "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_PHONE, !StringUtil.isNullOrEmpty(addresses.getCustomerShippingPhone()) ? addresses.getCustomerShippingPhone() : "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_CONTACT_PERSON_NO, !StringUtil.isNullOrEmpty(addresses.getCustomerShippingContactPersonNumber()) ? addresses.getCustomerShippingContactPersonNumber() : "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_CONTACT_PERSON, !StringUtil.isNullOrEmpty(addresses.getCustomerShippingContactPerson()) ? addresses.getCustomerShippingContactPerson() : "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIP_TO, !StringUtil.isNullOrEmpty(custshipaddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>")) ? custshipaddr : "");
                } else {
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_ADDRESS, "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_STATE, "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_COUNTRY, "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_CITY, "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_EMAIL, "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_FAX, "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_POSTAL, "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_MOBILE, "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_PHONE, "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_CONTACT_PERSON_NO, "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIPPING_CONTACT_PERSON, "");
                    summaryData.put(CustomDesignerConstants.CUSTOMER_SHIP_TO, "");
                }
            }
            billAddr=CommonFunctions.getTotalBillingShippingAddress(purchaseOrder.getBillingShippingAddresses(), true);
            shipAddr=CommonFunctions.getTotalBillingShippingAddress(purchaseOrder.getBillingShippingAddresses(), false);           
            vendortransactionalAddr=CommonFunctions.getTotalVendorTransactionalShippingAddress(purchaseOrder.getBillingShippingAddresses(), true); 

            companyAddr=CommonFunctions.getCompanyAddressForSenwanTec(purchaseOrder.getCompany());
            /*
             * All Global Section Custom Field and DImensions
             */
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, Constants.Acc_Purchase_Order_ModuleId);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put("billid", purchaseOrder.getID());
            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.browsertz, null))) {  //ERP-21781
                extraparams.put(Constants.browsertz, paramJobj.optString(Constants.browsertz));
            }
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
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
            extraparams.put("approvestatuslevel", purchaseOrder.getApprovestatuslevel());

            //Details like company details,base currency
            CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);
            String userId = paramJobj.optString(Constants.useridKey);
            KwlReturnObject userresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) userresult.getEntityList().get(0);
            //Details like company details,base currency
            CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);

            if(countryid == Constants.indian_country_id){
                //============================      Vendor Fields     =========================================

                String deducteeTypeValue = "",defaultNatureOfPayment = "",panStatus = "",deducteeCode = "",CSTDateStr = "",VATDateStr = "",dealerTypeStr = "";
                String tdsPaybleAccount = "",typeOfManufacturer = "",typeOfSales = "";

                if(purchaseOrder.getVendor().getDeducteeType()!= null && !StringUtil.isNullOrEmpty(purchaseOrder.getVendor().getDeducteeType())){
                    KwlReturnObject dedtyperesult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), purchaseOrder.getVendor().getDeducteeType());
                    MasterItem deducteeTypeObj = (MasterItem) dedtyperesult.getEntityList().get(0);
                    if(deducteeTypeObj != null){
                        deducteeTypeValue = deducteeTypeObj.getValue();
                    }
                }
                if(purchaseOrder.getVendor().getNatureOfPayment()!= null && !StringUtil.isNullOrEmpty(purchaseOrder.getVendor().getNatureOfPayment())){
                    KwlReturnObject naturetyperesult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), purchaseOrder.getVendor().getNatureOfPayment());
                    MasterItem natureTypeObj = (MasterItem) naturetyperesult.getEntityList().get(0);
                    if(natureTypeObj != null){
                        defaultNatureOfPayment = natureTypeObj.getValue();
                        String tdsPayableAccountId = natureTypeObj.getAccID();
                        if (!StringUtil.isNullOrEmpty(tdsPayableAccountId)) {
                        KwlReturnObject tdsPayableAccountresult = accountingHandlerDAOobj.getObject(Account.class.getName(), tdsPayableAccountId);
                        Account tdsPayableAccountObj = (Account) tdsPayableAccountresult.getEntityList().get(0);
                        if(tdsPayableAccountObj != null){
                            tdsPaybleAccount = tdsPayableAccountObj.getAccountName();
                        }
                    }
                }
                }

                if(purchaseOrder.getVendor().getDefaultnatureOfPurchase()!= null && !StringUtil.isNullOrEmpty(purchaseOrder.getVendor().getDefaultnatureOfPurchase())){
                    KwlReturnObject naturetyperesult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), purchaseOrder.getVendor().getDefaultnatureOfPurchase());
                    MasterItem natureTypeObj = (MasterItem) naturetyperesult.getEntityList().get(0);
                    if(natureTypeObj != null){
                        typeOfSales = natureTypeObj.getValue();
                    }
                }

                if(purchaseOrder.getVendor().getPanStatus()!= null && !StringUtil.isNullOrEmpty(purchaseOrder.getVendor().getPanStatus())){
                    panStatus = purchaseOrder.getVendor().getPanStatus().equals(IndiaComplianceConstants.PAN_STATUS_PANNOTAVBL) ?IndiaComplianceConstants.PAN_NOT_AVAILABLE:purchaseOrder.getVendor().getPanStatus().equals(IndiaComplianceConstants.PAN_STATUS_APPLIEDFOR)?IndiaComplianceConstants.PAN_APPLIED_FOR:"";
                }
                if(purchaseOrder.getVendor().getDeducteeCode()!= null && !StringUtil.isNullOrEmpty(purchaseOrder.getVendor().getDeducteeCode())){
                    deducteeCode = purchaseOrder.getVendor().getDeducteeCode().equals(IndiaComplianceConstants.DEDUCTEE_CODE_COMPANY) ?IndiaComplianceConstants.DEDUCTEE_CODE_COMPANY_STR:purchaseOrder.getVendor().getDeducteeCode().equals(IndiaComplianceConstants.DEDUCTEE_CODE_OTHER_THAN_COMPANY)?IndiaComplianceConstants.DEDUCTEE_CODE_OTHER_THAN_COMPANY_STR:"";
                }

                if(purchaseOrder.getVendor().getCSTRegDate()!= null){
                    Date CSTDate = purchaseOrder.getVendor().getCSTRegDate();
                    CSTDateStr = CSTDate != null ? authHandler.getUserDateFormatterJson(paramJobj).format(CSTDate) : "";
                }
                if (purchaseOrder.getVendor().getVatregdate() != null) {
                    Date VATDate = purchaseOrder.getVendor().getVatregdate();
                    VATDateStr = VATDate != null ? authHandler.getUserDateFormatterJson(paramJobj).format(VATDate) : "";
                }
                if(purchaseOrder.getVendor().getDealertype()!= null &&  !StringUtil.isNullOrEmpty(purchaseOrder.getVendor().getDealertype())){
                    String dealerType= purchaseOrder.getVendor().getDealertype();
                    if(dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_REGISTERED)){
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_REGISTERED_STR;
                    } else if(dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_UNREGISTERED)){
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_UNREGISTERED_STR;
                    } else if(dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_3_3A_4)){
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_3_3A_4_STR;
                    } else if(dealerType.equals(IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_1_2)){
                        dealerTypeStr = IndiaComplianceConstants.DEALER_TYPE_COMPOSITION_DEALER_42_1_2_STR;
                    }
                }
                if(purchaseOrder.getVendor().getManufacturerType()!= null &&  !StringUtil.isNullOrEmpty(purchaseOrder.getVendor().getManufacturerType())){
                    String ManufactureType= purchaseOrder.getVendor().getManufacturerType();
                    if(ManufactureType.equals(IndiaComplianceConstants.MANUFACTURER_TYPE_REGULAR)){
                        typeOfManufacturer = IndiaComplianceConstants.MANUFACTURER_TYPE_REGULAR_STR;
                    } else if(ManufactureType.equals(IndiaComplianceConstants.MANUFACTURER_TYPE_SMALLSCALE)){
                        typeOfManufacturer = IndiaComplianceConstants.MANUFACTURER_TYPE_SMALLSCALE_STR;
                    }
                }

                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, purchaseOrder.getVendor().getVATTINnumber()!= null ? purchaseOrder.getVendor().getVATTINnumber() : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, purchaseOrder.getVendor().getCSTTINnumber()!= null ? purchaseOrder.getVendor().getCSTTINnumber() : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_ECC_NO, purchaseOrder.getVendor().getECCnumber()!= null ? purchaseOrder.getVendor().getECCnumber() : "");
                summaryData.put(CustomDesignerConstants.CUSTOMER_PAN_NO, purchaseOrder.getVendor().getPANnumber()!= null ? purchaseOrder.getVendor().getPANnumber() : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_RESIDENTIAL_STATUS, purchaseOrder.getVendor().getResidentialstatus()==IndiaComplianceConstants.ResidentialStatus_Resident ? "Resident" : "Non-Resident");
                summaryData.put(CustomDesignerConstants.CustomerVendor_IMPORTER_ECC_NO, purchaseOrder.getVendor().getImporterECCNo()!= null ? purchaseOrder.getVendor().getImporterECCNo() : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_IEC_NO, purchaseOrder.getVendor().getIECNo()!= null ? purchaseOrder.getVendor().getIECNo() : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_RANGE_CODE, purchaseOrder.getVendor().getRangecode()!= null ? purchaseOrder.getVendor().getRangecode() : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_DIVISION_CODE, purchaseOrder.getVendor().getDivision()!= null ? purchaseOrder.getVendor().getDivision() : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_COMMISSIONERATE_CODE, purchaseOrder.getVendor().getCommissionerate()!= null ? purchaseOrder.getVendor().getCommissionerate() : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_DEALER_TYPE, dealerTypeStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_CST_REG_DATE, CSTDateStr );
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_REG_DATE, VATDateStr);
                summaryData.put(CustomDesignerConstants.CustomerVendor_DEDUCTEE_CODE, deducteeCode);
                summaryData.put(CustomDesignerConstants.CustomerVendor_PAN_STATAUS, panStatus);
                summaryData.put(CustomDesignerConstants.CustomerVendor_DEFAULT_NATURE_OF_PAYMENT, defaultNatureOfPayment);
                summaryData.put(CustomDesignerConstants.CustomerVendor_DEDUCTEE_TYPE,deducteeTypeValue);
                summaryData.put(CustomDesignerConstants.CustomerVendor_TDS_PAYABLE_ACCOUNT, tdsPaybleAccount);
                summaryData.put(CustomDesignerConstants.CustomerVendor_TDS_HIGHER_RATE, purchaseOrder.getVendor().getHigherTDSRate());
                summaryData.put(CustomDesignerConstants.CustomerVendor_INTERSTATEPARTY, purchaseOrder.getVendor().isInterstateparty()?"Yes":"No");
                summaryData.put(CustomDesignerConstants.CustomerVendor_C_FORM_APPLICABLE, purchaseOrder.getVendor().isCformapplicable()?"Yes":"No");
                summaryData.put(CustomDesignerConstants.CustomerVendor_TYPE_OF_MANUFATURER, typeOfManufacturer);
                summaryData.put(CustomDesignerConstants.CustomerVendor_TYPE_OF_SALES, typeOfSales);
                summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, purchaseOrder.getVendor().getGSTIN() != null ? purchaseOrder.getVendor().getGSTIN() : "");
                // ====================================================================
                summaryData.put(CustomDesignerConstants.Company_VAT_TIN_NO, extraCompanyPreferences.getVatNumber()!= null ? extraCompanyPreferences.getVatNumber() : "");
                summaryData.put(CustomDesignerConstants.Company_CST_TIN_NO, extraCompanyPreferences.getCstNumber()!= null ? extraCompanyPreferences.getCstNumber() : "");
            }
            if (!purchaseOrder.isIsExpenseType()) {
                String poStatus = "Closed";
                double count = accPurchaseOrderobj.getPOStatusOnBalanceQty(purchaseOrder.getID(), companyid);
                if (count > 0) {
                    poStatus = "Open";
                }
                summaryData.put(CustomDesignerConstants.PO_Status, poStatus.equalsIgnoreCase("Closed") ? "Closed" : (purchaseOrder.isIsPOClosed() ? "Closed" : "Open"));
            } else if (purchaseOrder.isIsExpenseType()) {
                /**
                 * to Update Status of 'Expese PO' according to 'Balance Amount
                 * '.
                 */
                summaryData.put(CustomDesignerConstants.PO_Status, (poBalnceAmount <= 0 || purchaseOrder.isIsPOClosed() || purchaseOrder.isDeleted()) ? "Closed" : "Open");
            }
            
            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, authHandler.formattedAmount(authHandler.round(totalAmount, companyid), companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, authHandler.formattedAmount(subtotal, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, authHandler.formattedAmount((subtotal-totalDiscount), companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, authHandler.formattedAmount(((subtotal - totalDiscount) + totaltax), companyid)); //ERP-25162
            summaryData.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, authHandler.formattedAmount(totalDiscount, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, authHandler.formattedAmount(totaltax, companyid));
            summaryData.put(CustomDesignerConstants.SummaryTaxPercent, taxPercent);
            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId,netinword+" Only.");
            summaryData.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, indonesianAmountInWords);
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, config == null ? "" : config.getPdfPostText());
            summaryData.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, SOref.equals("")?"":SOref.substring(0, SOref.length()-2));
            summaryData.put(CustomDesignerConstants.CustomDesignSOCustomerCode, customerCode.equals("") ? "" : customerCode.substring(0, customerCode.length()-2));
            summaryData.put(CustomDesignerConstants.CustomDesignSOCustomerName, customerName.equals("") ? "" : customerName.substring(0, customerName.length()-2));
            summaryData.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, VQref.equals("")?"":VQref.substring(0, VQref.length()-2));
            summaryData.put(CustomDesignerConstants.CustomerVendor_Term,purchaseOrder.getVendor().getDebitTerm()!=null?(purchaseOrder.getVendor().getDebitTerm().getTermname()!=null?purchaseOrder.getVendor().getDebitTerm().getTermname():""):"");
            summaryData.put(CustomDesignerConstants.CustomerVendor_AccountCode,purchaseOrder.getVendor().getAccount()!=null?(purchaseOrder.getVendor().getAccount().getAcccode()!=null?purchaseOrder.getVendor().getAccount().getAcccode():""):"");
            summaryData.put(CustomDesignerConstants.CustomerVendor_Code,purchaseOrder.getVendor().getAcccode()!=null?purchaseOrder.getVendor().getAcccode():"");
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorQuantity_Total,authHandler.formattingDecimalForQuantity(quantityConstant, companyid));
            summaryData.put(CustomDesignerConstants.BillTo, billAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.ShipTo, shipAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));            
            summaryData.put(CustomDesignerConstants.CustomDesignBaseCurrency_fieldTypeId, baseCurrency);
            summaryData.put(CustomDesignerConstants.CustomDesignVendorCurrency_fieldTypeId, purchaseOrder.getVendor().getCurrency() != null ? (purchaseOrder.getVendor().getCurrency().getCurrencyCode() != null ? purchaseOrder.getVendor().getCurrency().getCurrencyCode() : "") : "");
            summaryData.put(CustomDesignerConstants.Approvedby, approvername);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorCompanyEmail3_fieldTypeId, purchaseOrder.getCompany() != null ? (purchaseOrder.getCompany().getEmailID() != null ? purchaseOrder.getCompany().getEmailID() : "") : "");
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerCompanyAddress_fieldTypeId, companyAddr);
            summaryData.put(CustomDesignerConstants.Createdby, createdby);
            summaryData.put(CustomDesignerConstants.Updatedby, updatedby);

           /*Exchanged Rate Overall */
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
            summaryData.put(CustomDesignerConstants.CustomDesignTemplate_Print, printedOn);
            summaryData.put(CustomDesignerConstants.VendorTransactionalShipTo, vendortransactionalAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
            summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
            summaryData.put(CustomDesignerConstants.AllTerms, allTerms);
            summaryData.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, authHandler.formattedAmount(((subtotal-totalDiscount) + totalTermAmount), companyid));
            summaryData.put(CustomDesignerConstants.AllLineLevelTax, allLineLevelTax);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxAmount, allLineLevelTaxAmount);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxBasic, allLineLevelTaxBasic);
            summaryData.put(CustomDesignerConstants.AllLineLevelTermSigns, lineLevelTaxSign);
            summaryData.put(CustomDesignerConstants.CurrentUserFirstName, !StringUtil.isNullOrEmpty(user.getFirstName())?user.getFirstName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserLastName, !StringUtil.isNullOrEmpty(user.getLastName())?user.getLastName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserFullName, !StringUtil.isNullOrEmpty(user.getFullName())?user.getFullName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserEmail, !StringUtil.isNullOrEmpty(user.getEmailID())?user.getEmailID():"");
            summaryData.put(CustomDesignerConstants.CurrentUserAddress, !StringUtil.isNullOrEmpty(user.getAddress())?user.getAddress().replaceAll(Constants.REGEX_LINE_BREAK, "<br>"):"");
            summaryData.put(CustomDesignerConstants.CurrentUserContactNumber, !StringUtil.isNullOrEmpty(user.getContactNumber()) ? user.getContactNumber() : "");
            summaryData.put(CustomDesignerConstants.CustomDesignPRRefNo, PRref.equals("") ? "" : PRref.substring(0, PRref.length() - 2));
            summaryData.put(CustomDesignerConstants.VENDOR_TITLE, vendorTitle);
            summaryData.put(CustomDesignerConstants.AllTermNames, termsNameWithoutPercent);
            summaryData.put(CustomDesignerConstants.AllTermSigns, termssign);
            summaryData.put(CustomDesignerConstants.AllTermAmounts, termsamount);
            summaryData.put(CustomDesignerConstants.PURCHASEREQCREATOR, purchaseReqCreator);
            summaryData.put(CustomDesignerConstants.GSTAmountInWords, gstAmountInWords + Constants.ONLY);
            

            jArr.put(summaryData);
        } catch (Exception ex) {
            Logger.getLogger(AccPurchaseOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
 
    @Override
    public JSONArray getPurchaseOrdersVersionJson(HashMap<String, Object> requestParams, List list, JSONArray jArr) throws ServiceException {
        try {
            String ss = "";
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = requestParams.get("companyid").toString();
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid,Constants.Acc_Purchase_Order_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat userdf = (DateFormat) requestParams.get(Constants.userdf);

            Iterator itr = list.iterator();
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            /*
             * created DocumentEmailSetting Object for getting
             * customershippingaddress flag
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
            while (itr.hasNext()) {
                String orderid = itr.next().toString();
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseOrderVersion.class.getName(), orderid);
                PurchaseOrderVersion purchaseOrder = (PurchaseOrderVersion) objItr.getEntityList().get(0);
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("invoiceID", purchaseOrder.getID());
                hashMap.put("companyid", companyid);
                KwlReturnObject object = accInvoiceDAOobj.getinvoiceDocuments(hashMap);
                int attachemntcount = object.getRecordTotalCount();
                Vendor vendor = purchaseOrder.getVendor();
                KWLCurrency currency = null;
                if (purchaseOrder.getCurrency() != null) {
                    currency = purchaseOrder.getCurrency();
                } else {
                    currency = purchaseOrder.getVendor().getAccount().getCurrency() == null ? kwlcurrency : purchaseOrder.getVendor().getAccount().getCurrency();
                }
                JSONObject obj = new JSONObject();
                obj.put("billid", purchaseOrder.getID());
                obj.put("companyid", purchaseOrder.getCompany().getCompanyID());
                obj.put("companyname", purchaseOrder.getCompany().getCompanyName());
                obj.put("externalcurrencyrate", purchaseOrder.getExternalCurrencyRate());
                obj.put("isOpeningBalanceTransaction", purchaseOrder.isIsOpeningBalancePO());
                obj.put("isJobWorkOrderReciever", purchaseOrder.isIsJobWorkOutOrder());
                obj.put("currencyid", currency.getCurrencyID());
                obj.put("currencycode", currency.getCurrencyCode() == null ? "" : currency.getCurrencyCode());
                obj.put("personid", vendor.getID());
                obj.put("gstapplicable", purchaseOrder.isIsIndGSTApplied());
                obj.put("aliasname", vendor.getAliasname());
                obj.put("personcode", vendor.getAcccode() == null ? "" : vendor.getAcccode());
                obj.put("createdby", purchaseOrder.getCreatedby() == null ? "" : StringUtil.getFullName(purchaseOrder.getCreatedby()));
                obj.put("billtoaddress", purchaseOrder.getBillingShippingAddresses() == null ? "" : CommonFunctions.getBillingShippingAddress(purchaseOrder.getBillingShippingAddresses(), true));
                obj.put("shiptoaddress", purchaseOrder.getBillingShippingAddresses() == null ? "" : CommonFunctions.getBillingShippingAddress(purchaseOrder.getBillingShippingAddresses(), false));
                obj.put("personemail", vendor.getEmail());
                obj.put(Constants.HAS_ACCESS, vendor.isActivate());
                MasterItem gstRegistrationType = vendor != null ? vendor.getGSTRegistrationType() : null;
                if (gstRegistrationType != null && gstRegistrationType.getDefaultMasterItem() != null) {
                    obj.put("GSTINRegTypeDefaultMstrID", gstRegistrationType.getDefaultMasterItem().getID());
                }
                obj.put("billno", purchaseOrder.getPurchaseOrderNumber());
                obj.put("duedate", df.format(purchaseOrder.getDueDate()));
                obj.put("date", df.format(purchaseOrder.getOrderDate()));
                obj.put("dateinuserformat", userdf.format(purchaseOrder.getOrderDate()));
                obj.put("duedateinuserformat", userdf.format(purchaseOrder.getDueDate()));
                obj.put("shipdate", purchaseOrder.getShipdate() == null ? "" : df.format(purchaseOrder.getShipdate()));
                obj.put("shipdateinuserformat", purchaseOrder.getShipdate() == null ? "" : userdf.format(purchaseOrder.getShipdate()));
                obj.put("shipvia", purchaseOrder.getShipvia() == null ? "" : purchaseOrder.getShipvia());
                obj.put("fob", purchaseOrder.getFob() == null ? "" : purchaseOrder.getFob());
                obj.put("isfavourite", purchaseOrder.isFavourite());
                obj.put("isprinted", purchaseOrder.isPrinted());
                obj.put("isEmailSent", purchaseOrder.isIsEmailSent());
                obj.put("deleted", purchaseOrder.isDeleted());
                obj.put("billto", purchaseOrder.getBillTo() == null ? "" : purchaseOrder.getBillTo());
                obj.put("shipto", purchaseOrder.getShipTo() == null ? "" : purchaseOrder.getShipTo());
                obj.put("agent", purchaseOrder.getMasteragent() == null ? "" : purchaseOrder.getMasteragent().getID());
                obj.put("agentname", purchaseOrder.getMasteragent() == null ? "" : purchaseOrder.getMasteragent().getValue());
                obj.put("isSOPOBlock", purchaseOrder.isLinkedSOBlocked());
                obj.put("isdropshipchecked", purchaseOrder.isIsDropshipDocument());
                obj.put(Constants.IsRoundingAdjustmentApplied, purchaseOrder.isIsRoundingAdjustmentApplied());
                if (purchaseOrder.getApprover() != null) {
                    obj.put("approver", StringUtil.getFullName(purchaseOrder.getApprover()));
                }
                boolean gstIncluded = purchaseOrder.isGstIncluded();
                obj.put("gstIncluded", gstIncluded);
                obj.put("isConsignment", purchaseOrder.isIsconsignment());
                obj.put("fixedAssetInvoice", purchaseOrder.isFixedAssetPO());
                obj.put("termid", purchaseOrder.getTerm() == null ? "" : purchaseOrder.getTerm().getID());
                obj.put("supplierinvoiceno", purchaseOrder.getSupplierInvoiceNo());
                
                if (purchaseOrder.isIsDropshipDocument()) {
                    AccountingAddressManager.getTransactionAddressJSONForDropShipDoc(obj, purchaseOrder.getBillingShippingAddresses());
                } else if (documentEmailSettings != null && documentEmailSettings.isCustShippingAddressInPurDoc()) {
                    obj.put("isTransactionLevelAddress", true);
                    AccountingAddressManager.getTransactionAddressJSONForPOFromSO(obj, purchaseOrder.getBillingShippingAddresses(), true);
                } else {
                    AccountingAddressManager.getTransactionAddressJSON(obj, purchaseOrder.getBillingShippingAddresses(), true);
                }

                obj.put("termdays", purchaseOrder.getTerm() == null ? 0 : purchaseOrder.getTerm().getTermdays());
                obj.put("version", purchaseOrder.getVersion() == null ? "" : purchaseOrder.getVersion());
                obj.put("termname", purchaseOrder.getTerm() == null ? 0 : purchaseOrder.getTerm().getTermname());
                obj.put("termdetails", getPOVersiovTermDetails(purchaseOrder.getID(), true));
                obj.put("statusforcrosslinkage", purchaseOrder.isDisabledPOforSO() ? "Closed" : "Open");
                obj.put("closedmanually", purchaseOrder.isIsPOClosed() ? "Yes" : "N/A");
                obj.put("isexpenseinv", purchaseOrder.isIsExpenseType());
                obj.put("purchaseordertype", purchaseOrder.isIsExpenseType() ? "Expense" : "Product"); //SDP-11100
                obj.put("isInterstateParty", purchaseOrder.getVendor().isInterstateparty());
                obj.put("eccno", !StringUtil.isNullOrEmpty(vendor.getECCnumber()) ? vendor.getECCnumber() : "");
                obj.put("panno", !StringUtil.isNullOrEmpty(vendor.getPANnumber()) ? vendor.getPANnumber() : "");
                obj.put("tanno", !StringUtil.isNullOrEmpty(vendor.getTANnumber()) ? vendor.getTANnumber() : "");
                if (purchaseOrder.getTermsincludegst() != null) {
                    obj.put(Constants.termsincludegst, purchaseOrder.getTermsincludegst());
                }
                obj.put("termamount", CommonFunctions.getTotalTermsAmount(getPOVersiovTermDetails(purchaseOrder.getID(), true)));
                obj.put(Constants.SEQUENCEFORMATID, purchaseOrder.getSeqformat() == null ? "" : purchaseOrder.getSeqformat().getID());

                KwlReturnObject linkRresult = accLinkDataDao.checkEntryForTransactionInLinkingTableForForwardReference("PurchaseOrder", purchaseOrder.getID());
                list = linkRresult.getEntityList();
                if (list != null && !list.isEmpty()) {
                    obj.put(Constants.IS_LINKED_TRANSACTION, true);
                } else {
                    obj.put(Constants.IS_LINKED_TRANSACTION, false);
                }

                obj.put("approvalstatus", purchaseOrder.getApprovestatuslevel());
                String approvalStatus = "";
                if (purchaseOrder.getApprovestatuslevel() < 0) {
                    approvalStatus = "Rejected";
                } else if (purchaseOrder.getApprovestatuslevel() < 11) {
                    String ruleid = "", userRoleName = "";
                    HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                    qdDataMap.put("companyid", companyid);
                    qdDataMap.put("level", purchaseOrder.getApprovestatuslevel());
                    qdDataMap.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
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
                    if (!StringUtil.isNullOrEmpty(userRoleName)) {
                        userRoleName = userRoleName.substring(0, userRoleName.length() - 1);
                    }
                    approvalStatus = "Pending Approval" + (StringUtil.isNullOrEmpty(userRoleName) ? "" : " by " + userRoleName) + " at Level - " + purchaseOrder.getApprovestatuslevel();
                } else {
                    approvalStatus = "Approved";
                }
                obj.put("approvalstatusinfo", approvalStatus);
                Set<String> uniqueProductTaxList = new HashSet<String>();
                double amount = 0, totalRowTaxAmt = 0d, totalDiscount = 0, discountPrice = 0, rowDiscountAmt = 0d, rowOtherTermNonTaxableAmount = 0d;
                boolean includeprotax = false;
                String taxname = "";
                double pobalncequantity = 0;
                double subtotal = 0d;
                double productTotalAmount = 0d;
                double termAmount = CommonFunctions.getTotalTermsAmount(getPOVersiovTermDetails(purchaseOrder.getID(), true));
                if (purchaseOrder.isIsExpenseType()) {//** For Expense grid
                    for (ExpensePOVersionDetails epod : purchaseOrder.getExpenserows()) {
                        double rowAmount = epod.getRate();//this is user enter amount gainst column Amount on which discount can applied
                        double rowTax = 0;
                        double rowDiscount = 0;
                        if (!epod.getPurchaseOrderVersion().isGstIncluded()) {
                            productTotalAmount += epod.getRate();
                        } else {
                            productTotalAmount += epod.getRateIncludingGst();
                        }
                        //Discount
                        if (epod.getDiscount() != null) {
                            rowDiscount = epod.getDiscount().getDiscountValue();// adding discount to get total row level discount amount
                        }
                        //Tax
                        if (epod.getTax() != null) {//line level tax is given
                            includeprotax = true;
                            taxname += epod.getTax().getName() + ", ";
                            rowTax = epod.getRowTaxAmount();
                        }

                        if (gstIncluded) {//in including gst, tax already included so no need to add seperetaly
                            if (epod.isIsdebit()) {//if debit type then amount will be added
                                amount += (rowAmount - rowDiscount);//subtracting discount to calculate total amount from all line level    
                            } else {//id debit type then amount will be subtracted
                                amount -= (rowAmount - rowDiscount);//subtracting discount to calculate total amount from all line level    
                            }
                        } else {
                            if (epod.isIsdebit()) {//if debit type then amount will be added
                                amount += (rowAmount - rowDiscount + rowTax);//subtracting discount and then sum tax to find total amount from all line level    
                            } else {//if debit type then amount will be subtracted
                                amount -= (rowAmount - rowDiscount + rowTax);//subtracting discount and then sum tax to find total amount from all line level    
                            }
                        }
                        if (epod.isIsdebit()) {
                            totalRowTaxAmt += rowTax;// sum up total line level tax
                            rowDiscountAmt += rowDiscount;// sum up total discount given at line level
                        } else {
                            totalRowTaxAmt -= rowTax;// sum up total line level tax
                            rowDiscountAmt -= rowDiscount;// sum up total discount given at line level
                        }
                    }
                    subtotal = productTotalAmount - rowDiscountAmt;
                    obj.put("productTotalAmount", productTotalAmount);
                    obj.put("amountBeforeTax",authHandler.formattingDecimalForAmount((subtotal+termAmount),companyid));
                    obj.put("subtotal", subtotal);
                } else {//** For Product Grid

                    for (PurchaseOrderVersionDetails pod : purchaseOrder.getRows()) {
                        double rate = authHandler.roundUnitPrice(pod.getRate(), companyid);
                        if (gstIncluded) {
                                rate = pod.getRateincludegst();
                        }
                        double quantity = authHandler.roundQuantity(pod.getQuantity(), companyid);
                        productTotalAmount += rate * quantity;
                        double poPrice = authHandler.round(quantity * rate, companyid);
                        double discountPOD = authHandler.round(pod.getDiscount(), companyid);
                        if (pod.getDiscountispercent() == 1) {
                            discountPrice = (poPrice) - authHandler.round((poPrice * discountPOD / 100), companyid);
                            rowDiscountAmt += authHandler.round((poPrice * discountPOD / 100), companyid);
                        } else {
                            discountPrice = poPrice - discountPOD;
                            rowDiscountAmt += discountPOD;
                        }

                        amount += discountPrice;
                        if (!gstIncluded) {
                            amount += authHandler.round(pod.getRowTaxAmount(), companyid);
                        }
                        if (pod.getTax() != null) {//line level tax is given
                            includeprotax = true;
                            taxname += pod.getTax().getName() + ", ";
                            totalRowTaxAmt += pod.getRowTaxAmount();
                            uniqueProductTaxList.add(pod.getTax().getID());
                        }
                        if (isLineLevelTermFlag) {
                            /**
                             * ERP-34717 If GST Include, no need to add Tax
                             * Amount. Amount is already with tax
                             */
                            if (!gstIncluded) {
                                amount += authHandler.round(pod.getRowTermAmount(), companyid);
                                amount += authHandler.round(pod.getOtherTermNonTaxableAmount(), companyid);
                            }
                            // Append OtherTermNonTaxableAmount for rach row.
                            rowOtherTermNonTaxableAmount += pod.getOtherTermNonTaxableAmount();
                            totalRowTaxAmt += pod.getRowTermAmount();
                        }// For Line level terms as tax
                        pobalncequantity += pod.getBalanceqty();

                    }
                    obj.put("productTotalAmount", productTotalAmount);      //For purchase order
                    obj.put("amountBeforeTax", authHandler.formattingDecimalForAmount((subtotal+termAmount),companyid));      //For purchase order

                }

                double discountPO = authHandler.round(purchaseOrder.getDiscount(), companyid);
                if (purchaseOrder.getDiscount() != 0) {
                    if (purchaseOrder.isPerDiscount()) {
                        totalDiscount = authHandler.round((amount * discountPO / 100), companyid);
                        amount = amount - totalDiscount;
                    } else {
                        amount = amount - discountPO;
                        totalDiscount = discountPO;
                    }
                    obj.put("discounttotal", discountPO);
                } else {
                    obj.put("discounttotal", 0);
                }
                obj.put("discount", rowDiscountAmt);
                obj.put("discountinbase", purchaseOrder.getDiscountinbase());
                obj.put("discountispertotal", purchaseOrder.isPerDiscount());
                if (purchaseOrder.isPerDiscount()) {
                    obj.put("ispercentdiscount", purchaseOrder.isPerDiscount());
                    obj.put("discountval", discountPO);
                } else {
                    obj.put("discountval", totalDiscount);
                }

                double totalTermAmount = 0;
                double taxableTermamount = 0;
                HashMap<String, Object> requestParam = new HashMap();
                HashMap<String, Object> filterrequestParams = new HashMap();
                requestParam.put("purchaseOrderVersion", purchaseOrder.getID());
                KwlReturnObject purchaseOrderResult = null;
                filterrequestParams.put("taxid", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getID());
                double termTaxAmount = 0;
                Map<String, Object> taxListParams = new HashMap<String, Object>();
                KwlReturnObject taxListResult = null;
                List<TaxList> taxListPercent = new ArrayList<TaxList>();
                taxListParams.put("companyid", companyid);
                boolean isApplyTaxToTerms = purchaseOrder.isApplyTaxToTerms();
                obj.put("isapplytaxtoterms", isApplyTaxToTerms);
                purchaseOrderResult = accPurchaseOrderobj.getPurchaseOrderVersionTermMap(requestParam);
                List<PurchaseOrderVersionTermMap> termMap = purchaseOrderResult.getEntityList();
                for (PurchaseOrderVersionTermMap purchaseOrderTermMap : termMap) {
                    filterrequestParams.put("term", purchaseOrderTermMap.getTerm() == null ? "" : purchaseOrderTermMap.getTerm().getId());
                    InvoiceTermsSales mt = purchaseOrderTermMap.getTerm();
                    double termAmnt = purchaseOrderTermMap.getTermamount();
                    totalTermAmount += authHandler.round(termAmnt, companyid);

                    boolean isTermMappedwithTax = false;
                    filterrequestParams.put("companyid", companyid);
                    if (includeprotax && uniqueProductTaxList.size() > 0 && isApplyTaxToTerms) {
                        for (String taxId : uniqueProductTaxList) {
                            filterrequestParams.put("taxid", taxId);
                            taxListParams.put("taxid", taxId);
                            isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                            if (isTermMappedwithTax) {
                                taxListResult = accTaxObj.getTaxList(taxListParams);
                                if (taxListResult != null && taxListResult.getEntityList() != null) {
                                    taxListPercent = taxListResult.getEntityList();
                                    termTaxAmount += (termAmnt * (taxListPercent.get(0).getPercent())) / 100;
                                }
                            }
                        }
                    } else {
                        isTermMappedwithTax = accTaxObj.isTermMappedwithTax(filterrequestParams);
                    }

                    if (isTermMappedwithTax) {
                        taxableTermamount += termAmnt;
                    }
                }
                totalTermAmount = authHandler.round(totalTermAmount, companyid);

                obj.put("currencysymbol", currency.getSymbol());
                double taxPercent = 0;
                if (purchaseOrder.getTax() != null) {
                    requestParams.put("transactiondate", purchaseOrder.getOrderDate());
                    requestParams.put("taxid", purchaseOrder.getTax().getID());
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
                double orderAmount = amount;//double orderAmount=(Double) bAmt.getEntityList().get(0);
                double ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round(((orderAmount + taxableTermamount) * taxPercent / 100), companyid));
                obj.put("amountbeforegst", amount - totalRowTaxAmt); // Amount before both kind of tax row level or transaction level
                obj.put("taxpercent", taxPercent);
                amount = amount + totalTermAmount + ordertaxamount;
                amount += termTaxAmount;
                orderAmount += totalTermAmount;
                orderAmount += termTaxAmount;
                obj.put("orderamount", orderAmount);
                double totalAmt = orderAmount + ordertaxamount;
                if (purchaseOrder.isIsRoundingAdjustmentApplied()) {
                    totalAmt += purchaseOrder.getRoundingadjustmentamount();
                    amount += purchaseOrder.getRoundingadjustmentamount();
                }
                obj.put("orderamountwithTax", totalAmt);
                obj.put("amount", amount);
                obj.put("amountInWodrs", currency.getName() + " " + EnglishNumberToWordsOjb.convert(authHandler.round(amount, companyid), purchaseOrder.getCurrency(), countryLanguageId));

                KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, purchaseOrder.getCurrency().getCurrencyID(), purchaseOrder.getOrderDate(), purchaseOrder.getExternalCurrencyRate());
                obj.put("amountinbase", authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                obj.put("personname", vendor.getName());
                obj.put("memo", purchaseOrder.getMemo());
                obj.put("posttext", purchaseOrder.getPostText());
                obj.put("taxid", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getID());
                obj.put("costcenterid", purchaseOrder.getCostcenter() == null ? "" : purchaseOrder.getCostcenter().getID());
                obj.put("costcenterName", purchaseOrder.getCostcenter() == null ? "" : purchaseOrder.getCostcenter().getName());
                obj.put("shiplengthval", purchaseOrder.getShiplength());
                obj.put("invoicetype", purchaseOrder.getInvoicetype());
                obj.put("archieve", 0);
                obj.put("attachment", attachemntcount);

                List<String> taxList = new ArrayList(Arrays.asList(taxname.split(", ")));
                Collections.sort(taxList);
                String taxname1 = "";
                for (String str : taxList) {
                    taxname1 += str + ", ";
                }
                obj.put("includeprotax", includeprotax);
                if (includeprotax) {
                    obj.put("taxname", taxname1.substring(0, taxname1.length() > 1 ? taxname1.length() - 2 : taxname1.length()));
                } else {
                    obj.put("taxname", purchaseOrder.getTax() == null ? "" : purchaseOrder.getTax().getName());
                }

                double taxAmt = 0;
                //As either row level tax will be available or invoice level
                if (isLineLevelTermFlag) {
                    // If LineLevelTerm is applicable then add the value in JSON Object.
                    obj.put(Constants.OtherTermNonTaxableAmount, rowOtherTermNonTaxableAmount);
                    taxAmt = totalRowTaxAmt;
                } else {
                    if (purchaseOrder.getTax() == null) {// means global level tax is not given
                        taxAmt = totalRowTaxAmt;
                    } else {
                        taxAmt = ordertaxamount;
                    }
                }

                if (gstIncluded) {
                    subtotal = productTotalAmount - rowDiscountAmt - (taxAmt + termTaxAmount);
                } else {
                    subtotal = productTotalAmount - rowDiscountAmt;
                }
                obj.put("subtotal", subtotal);
                obj.put("taxamount", taxAmt + termTaxAmount);

                if (purchaseOrder.getModifiedby() != null) {
                    obj.put("lasteditedby", StringUtil.getFullName(purchaseOrder.getModifiedby()));
                }
                if (!purchaseOrder.isIsExpenseType()) {
                    obj.put("status", (pobalncequantity == 0 || purchaseOrder.isIsPOClosed() || purchaseOrder.isDeleted()) ? "Closed" : "Open");
                } else if (purchaseOrder.isIsExpenseType()) {
                    obj.put("status", (purchaseOrder.isIsPOClosed() || purchaseOrder.isDeleted()) ? "Closed" : "Open");
                }
                boolean isExport = (requestParams.get("isExport") == null) ? false : true;
                Map<String, Object> variableMap = new HashMap<String, Object>();
                PurchaseOrderVersionCustomData jeDetailCustom = (PurchaseOrderVersionCustomData) purchaseOrder.getPoVersionCustomData();
                replaceFieldMap = new HashMap<String, String>();
                if (jeDetailCustom != null) {
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                    JSONObject params = new JSONObject();
                    params.put("companyid", companyid);
                    params.put("isExport", isExport);
                    params.put(Constants.userdf, userdf);
                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                }
                jArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getPurchaseOrdersJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    @Override
    public JSONArray getPurchaseOrderVersionRows(HashMap<String, Object> requestParams) throws ServiceException {
        JSONArray jArr = new JSONArray();
        DateFormat userdf = null;
        try {
            boolean isConsignment = false;
            if (requestParams.containsKey(Constants.userdf) && requestParams.get(Constants.userdf) != null) {
                userdf = (DateFormat) requestParams.get(Constants.userdf);
            }
            if (requestParams.get("isConsignment") != null) {
                isConsignment = Boolean.FALSE.parseBoolean(requestParams.get("isConsignment").toString());
            }
            boolean isFixedAsset = false;
            if (requestParams.get("isFixedAsset") != null) {
                isFixedAsset = Boolean.FALSE.parseBoolean(requestParams.get("isFixedAsset").toString());
            }
            boolean isJobWorkStockOut = false;
            if (requestParams.containsKey("isJobWorkStockOut")) {
                isJobWorkStockOut = Boolean.FALSE.parseBoolean(requestParams.get("isJobWorkStockOut").toString());
            }
            String storeId = "";
            if (requestParams.containsKey("storeId")) {
                storeId = (String) requestParams.get("storeId");
            }
            boolean FA_POlinkToFA_PI = false;
            boolean isJobWorkOutRemain = false;
            if (requestParams.get("FA_POlinkToFA_PI") != null) {
                FA_POlinkToFA_PI = Boolean.FALSE.parseBoolean(requestParams.get("FA_POlinkToFA_PI").toString());
            }
            /*
             * ProdIds contains ids which are selected items from Aged order
             * work report.
             */

            String[] jobWorkOutId = (String[]) requestParams.get("prodIds");
            if (requestParams.containsKey("isJobWorkOutRemain") && requestParams.get("isJobWorkOutRemain") != null) {
                isJobWorkOutRemain = Boolean.parseBoolean(requestParams.get("isJobWorkOutRemain").toString());
            }
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get("companyid");
            DateFormat df = (DateFormat) requestParams.get("dateFormatValue");

            Map<String, Object> ProductFieldsRequestParams = new HashMap();
            ProductFieldsRequestParams.put("companyid", companyid);
            ProductFieldsRequestParams.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
            List masterFieldsResultList = CommonFunctions.getproductmastersFieldsToShowLineLevel(ProductFieldsRequestParams, accountingHandlerDAOobj);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            boolean doflag = requestParams.containsKey("doflag") ? (Boolean) requestParams.get("doflag") : false;
            String[] pos = (String[]) requestParams.get("bills");
            double addobj = 1;

            boolean isForDOGROLinking = false;
            boolean isForInvoice = false;
            if (requestParams.containsKey("isForInvoice") && requestParams.get("isForInvoice") != null) {
                isForInvoice = (Boolean) requestParams.get("isForInvoice");
            }
            /*
             * isForSGELink flag is used in case of linking PO in SGE
             */
            boolean isForSGELinking = false;
            if (requestParams.containsKey("isForSGELinking") && requestParams.get("isForSGELinking") != null) {
                isForSGELinking = (Boolean) requestParams.get("isForSGELinking");
            }
            /*
             * isForJobWorkOut , isJobWorkOutLinkedWithGRN ,
             * isJobWorkOutLinkedWithPI these flags are used when call is for
             * Job work out order.
             */
            boolean isForJobWorkOut = false;
            if (requestParams.containsKey("isForJobWorkOut") && requestParams.get("isForJobWorkOut") != null) { //isForJobWorkOut
                isForJobWorkOut = (Boolean) requestParams.get("isForJobWorkOut");
            }
            boolean isJobWorkOutLinkedWithGRN = false;
            if (requestParams.containsKey("isJobWorkOutLinkedWithGRN") && requestParams.get("isJobWorkOutLinkedWithGRN") != null) { //isJobWorkOutLinkedWithGRN
                isJobWorkOutLinkedWithGRN = (Boolean) requestParams.get("isJobWorkOutLinkedWithGRN");
            }
            boolean isJobWorkOutLinkedWithPI = false;
            if (requestParams.containsKey("isJobWorkOutLinkedWithPI") && requestParams.get("isJobWorkOutLinkedWithPI") != null) { //isJobWorkOutLinkedWithPI
                isJobWorkOutLinkedWithPI = (Boolean) requestParams.get("isJobWorkOutLinkedWithPI");
            }

            if (requestParams.containsKey("isForDOGROLinking") && requestParams.get("isForDOGROLinking") != null) {
                isForDOGROLinking = (Boolean) requestParams.get("isForDOGROLinking");
            }
            int moduleid = -1;
            if (requestParams.containsKey("requestModuleid")) {
                moduleid = Integer.parseInt(requestParams.get("requestModuleid").toString());
            }

            if (requestParams.containsKey("prodIds")) {
                moduleid = Integer.parseInt(requestParams.get("requestModuleid").toString());
            }

            boolean isForLinking = false;

            if (requestParams.containsKey("isForLinking") && requestParams.get("isForLinking") != null) {// True in case of linking with VI
                isForLinking = (Boolean) requestParams.get("isForLinking");
            }

            boolean linkingFlag = false;
            if (requestParams.containsKey("linkingFlag") && requestParams.get("linkingFlag") != null) { // True in case of Asset linking with VQ
                linkingFlag = (Boolean) requestParams.get("linkingFlag");
            }
            boolean isExport = false;
            if (requestParams.containsKey("isExport") && requestParams.get("isExport") != null) { // True in case of Export
                isExport = (Boolean) requestParams.get("isExport");
            }
            boolean sopolinkflag = false;
            if (requestParams.containsKey(Constants.POSOFLAG) && requestParams.get(Constants.POSOFLAG) != null) { // True in case of crosslink of PO and SO 
                sopolinkflag = (Boolean) requestParams.get(Constants.POSOFLAG);
            }

            boolean isForReport = false;
            boolean isBatchForProduct = false;
            boolean isSerialForProduct = false;
            boolean isLocationForProduct = false;
            boolean isWarehouseForProduct = false;
            boolean isRowForProduct = false;
            boolean isRackForProduct = false;
            boolean isBinForProduct = false;
            /*
             * if call is for "Job Work Out Order" then this check will be true
             */
            isForJobWorkOut = (isForJobWorkOut || isJobWorkOutLinkedWithGRN || isJobWorkOutLinkedWithPI);
            String description = "";
            double minqty = 0.0;
            double maxqty = 0.0;
            String productsDefaultLocation = "", productsDefaultWarehouse = "";
            if (requestParams.containsKey("isForReport")) {
                isForReport = (Boolean) requestParams.get("isForReport");
            }

            HashMap<String, Object> fieldrequestParams1 = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            int poCustomModuleid = isForJobWorkOut ? Constants.JOB_WORK_OUT_ORDER_MODULEID : isConsignment ? Constants.Acc_ConsignmentVendorRequest_ModuleId : (!isFixedAsset ? Constants.Acc_Purchase_Order_ModuleId : Constants.Acc_FixedAssets_Purchase_Order_ModuleId);

            fieldrequestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams1.put(Constants.filter_values, Arrays.asList(companyid, poCustomModuleid, 1));   // For line-level PO on dropdown
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMapRow = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams1, replaceFieldMap, customFieldMap, customDateFieldMap);
            HashMap<String, Object> poRequestParams = new HashMap<String, Object>();
            
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("purchaseOrderVersion.ID");
            order_by.add("srno");
            order_type.add("asc");
            poRequestParams.put("filter_names", filter_names);
            poRequestParams.put("filter_params", filter_params);
            poRequestParams.put("order_by", order_by);
            poRequestParams.put("order_type", order_type);
            
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            capresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) capresult.getEntityList().get(0);
            Country country = extraCompanyPreferences.getCompany().getCountry();
            String stdate = authHandler.getDates(preferences.getFinancialYearFrom(), true);
            String enddate = authHandler.getDates(preferences.getFinancialYearFrom(), false);
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
            /*
             * isJobWorkOutRemain is true if sales invoice is creating from Aged
             * order work report.
             */
            if (isJobWorkOutRemain) {
                KwlReturnObject idresult = null;
                if (pos != null) {
                    idresult = accPurchaseOrderobj.getJobWorkOutPurchaseOrderSinglePO(companyid, pos[0]);
                } else {
                    idresult = accPurchaseOrderobj.getJobWorkOutPurchaseOrder(companyid);
                }
                List list = idresult.getEntityList();
                Iterator ite1 = list.iterator();
                int i = 0;
                double qua = 0;
                filter_names.clear();
                filter_names.add("id");
                PurchaseOrder po = (PurchaseOrder) ite1.next();
                while (i < jobWorkOutId.length) {
                    filter_params.clear();
                    poRequestParams.clear();
                    filter_params.add(jobWorkOutId[i]);
                    poRequestParams.put("filter_names", filter_names);
                    poRequestParams.put("filter_params", filter_params);

                    KwlReturnObject podresult = accPurchaseOrderobj.getSelectedPurchaseOrderDetails(poRequestParams);
                    List<InterStoreTransferRequest> interStoreTransferRequest = podresult.getEntityList();
                    for (InterStoreTransferRequest row : interStoreTransferRequest) {
                        JSONObject obj = new JSONObject();
                        if (isJobWorkStockOut) {
                            Map<String, Object> reqMap = new HashMap();
                            reqMap.put("masterFieldsResultList", masterFieldsResultList);
                            reqMap.put("df", df);
                            reqMap.put("masterFieldsResultList", masterFieldsResultList);
                            reqMap.put("preferences", preferences);
                            reqMap.put("extraCompanyPreferences", extraCompanyPreferences);
                            reqMap.put("storeId", storeId);
                            JSONObject params = new JSONObject();
                            params.put("gcurrencyid", currencyid);
                            params.put("stdate", stdate);
                            params.put("enddate", enddate);
                            reqMap.put("selectedJobStockOutid", jobWorkOutId[i]);
                            reqMap.put("orderedQty", row.getOrderedQty());

                            reqMap.put("purchaseOrderDetailObject", row.getPurchaseOrderDetail());
                            reqMap.put("rowProductObject", row.getProduct());
                            reqMap.put("rowProductId", row.getProduct().getID());
                            reqMap.put("purchaseOrderObject", po);
                            params.put("isJobWorkStockOut", isJobWorkStockOut);
                            reqMap.put("isJobWorkOutRemain", isJobWorkOutRemain);
                            getAssemblySubProductRowsDetails(reqMap, params, jArr, po.getPurchaseOrderNumber(), po.getVendor().getName(), po.getDueDate(), row.getProduct().getName());
                        }
                    }
                    i++;
                }

            } else {
                for (int i = 0; pos != null && i < pos.length; i++) {
                    KwlReturnObject poresult = accountingHandlerDAOobj.getObject(PurchaseOrderVersion.class.getName(), pos[i]);
                    PurchaseOrderVersion po = (PurchaseOrderVersion) poresult.getEntityList().get(0);
                    KWLCurrency currency = null;
                    if (po.getCurrency() != null) {
                        currency = po.getCurrency();
                    } else {
                        currency = po.getVendor().getAccount().getCurrency() == null ? kwlcurrency : po.getVendor().getAccount().getCurrency();
                    }
                    filter_params.clear();
                    filter_params.add(po.getID());
                    if (po.isIsExpenseType()) {// when PO is expense type then we need to fetch data from ExpensePODetail
                        requestParams.put(Constants.userdf, userdf);
                        KwlReturnObject podresult = accPurchaseOrderobj.getExpensePurchaseOrderVersionDetails(poRequestParams);
                        List<ExpensePOVersionDetails> expensepodetails = podresult.getEntityList();
                        jArr=getExpenseDetailPOVersionRows(jArr,requestParams,po,expensepodetails,FieldMapRow,customFieldMap,customDateFieldMap);
                    } else {//For other case data will be fetched from PurchaseOrderDetail
                        KwlReturnObject podresult = accPurchaseOrderobj.getPurchaseOrderVersionDetails(poRequestParams);
                        List<PurchaseOrderVersionDetails> podetails = podresult.getEntityList();
                        for (PurchaseOrderVersionDetails row : podetails) {
                            JSONObject obj = new JSONObject();
                            if (isJobWorkStockOut) {
                                /**
                                 * if request from Job Work stock Transfer
                                 */
                                Map<String, Object> reqMap = new HashMap();
                                reqMap.put("masterFieldsResultList", masterFieldsResultList);
                                reqMap.put("df", df);
                                reqMap.put("masterFieldsResultList", masterFieldsResultList);
                                reqMap.put("preferences", preferences);
                                reqMap.put("extraCompanyPreferences", extraCompanyPreferences);
                                reqMap.put("storeId", storeId);
                                JSONObject params = new JSONObject();
                                params.put("gcurrencyid", currencyid);
                                params.put("stdate", stdate);
                                params.put("enddate", enddate);

                                reqMap.put("purchaseOrderDetailObject", row);
                                reqMap.put("rowProductObject", row.getProduct());
                                reqMap.put("purchaseOrderObject", row.getPurchaseOrderVersion());
                                params.put("isJobWorkStockOut", isJobWorkStockOut);
                                getAssemblySubProductRows(reqMap, params, jArr);
                            } else {
                                CommonFunctions.getterMethodForProductsData(row.getProduct(), masterFieldsResultList, obj);
                                obj.put("billid", po.getID());
                                obj.put("billno", po.getPurchaseOrderNumber());
                                obj.put("bomid", row.getBomcode() != null ? row.getBomcode().getID() : "");
                                obj.put("bomcode", row.getBomcode() != null ? row.getBomcode().getBomCode() : "");
                                obj.put("currencysymbol", currency.getSymbol());
                                obj.put("joborderdetail", row.getID());
                                obj.put("isJobWorkOutProd", row.getPurchaseOrderVersion().isIsJobWorkOutOrder());
                                obj.put("currencyCode", currency.getCurrencyCode() == null ? "" : currency.getCurrencyCode());
                                obj.put("srno", row.getSrno());
                                obj.put("rowid", row.getID());
                                obj.put("productid", row.getProduct().getID());
                                obj.put("isAsset", row.getProduct().isAsset());
                                obj.put("hasAccess", row.getProduct().isIsActive());
                                obj.put("productname", row.getProduct().getName());
                                obj.put("leadtime", row.getProduct().getLeadTimeInDays());
                                obj.put("type", row.getProduct().getProducttype() == null ? "" : row.getProduct().getProducttype().getName());
                                obj.put("typeid", row.getProduct().getProducttype() == null ? "" : row.getProduct().getProducttype().getID());
                                obj.put("pid", row.getProduct().getProductid());
                                obj.put("supplierpartnumber", StringUtil.isNullOrEmpty(row.getSupplierpartnumber()) ? "" : row.getSupplierpartnumber());
                                obj.put("status", row.isIsLineItemClosed() ? "Yes" : "N/A");
                                if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                                    description = row.getDescription();
                                } else if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                                    description = row.getProduct().getDescription();
                                } else {
                                    description = "";
                                }
                                obj.put("desc", StringUtil.DecodeText(description));
                                obj.put("description", StringUtil.DecodeText(description));
                                obj.put("unitname", row.getUom() != null ? row.getUom().getNameEmptyforNA() : row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                                obj.put("uomname", row.getUom() != null ? row.getUom().getNameEmptyforNA() : row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                                obj.put("baseuomid", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getID());
                                obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                                obj.put("multiuom", row.getProduct().isMultiuom());
                                obj.put("memo", row.getRemark());
                                obj.put("invstore", (StringUtil.isNullOrEmpty(row.getInvstoreid())) ? "" : row.getInvstoreid());
                                obj.put("invlocation", (StringUtil.isNullOrEmpty(row.getInvlocid())) ? "" : row.getInvlocid());
                                obj.put("shelfLocation", row.getShelfLocation());
                                obj.put("isConsignment", po.isIsconsignment()); //for getting is consignment request
                                obj.put("permit", row.getPermit() == null ? "" : row.getPermit());
                                obj.put("includeprotax", row.getTax() != null ? true : false);
                                String productsBaseUomId = (row.getProduct().getUnitOfMeasure() == null) ? "" : row.getProduct().getUnitOfMeasure().getID();
                                String selectedUomId = (row.getUom() != null) ? row.getUom().getID() : "";
                                if (row.getProduct().isblockLooseSell() && !productsBaseUomId.equals(selectedUomId)) {
                                    // Get Available Quantity of Product For Selected UOM
                                    KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(row.getProduct().getID(), selectedUomId);
                                    double availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                                    obj.put("availableQtyInSelectedUOM", availableQuantity);
                                    obj.put("isAnotherUOMSelected", true);
                                    // Getting Open PO/SO count
                                    HashMap<String, Object> orderParams = new HashMap<String, Object>();
                                    orderParams.put("companyid", companyid);
                                    orderParams.put("gcurrencyid", currencyid);
                                    orderParams.put("df", df);
                                    orderParams.put("pendingapproval", false);
                                    orderParams.put("startdate", stdate);
                                    orderParams.put("enddate", enddate);
                                    orderParams.put("currentuomid", selectedUomId);
                                    orderParams.put("productId", row.getProduct().getID());

                                    double pocountinselecteduom = accSalesOrderServiceDAOobj.getPOCount(orderParams);

                                    double socountinselecteduom = accSalesOrderServiceDAOobj.getSOCount(orderParams);

                                    obj.put("pocountinselecteduom", pocountinselecteduom);
                                    obj.put("socountinselecteduom", socountinselecteduom);
                                } else {
                                    /**
                                     * If isblockLooseSell check from company
                                     * preference is false then get the actual
                                     * quantity of product.
                                     */
                                    KwlReturnObject result = accProductObj.getQuantity(row.getProduct().getID());
                                    obj.put("availableQtyInSelectedUOM", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));
                                }
                                if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {  //This is sats specific code 	 
                                    obj.put("invoicetype", po.getInvoicetype());

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

                                    obj.put("parentid", ((row.getProduct().getParent() != null) ? row.getProduct().getParent().getID() : ""));
                                    obj.put("parentname", ((row.getProduct().getParent() != null) ? row.getProduct().getParent().getName() : ""));
                                    if (row.getProduct().getParent() != null) {
                                        obj.put("issubproduct", true);
                                    }
                                    if (row.getProduct().getChildren().size() > 0) {
                                        obj.put("isparentproduct", true);
                                    } else {
                                        obj.put("isparentproduct", false);
                                    }
                                }

                                Map<String, Object> variableMap = new HashMap<String, Object>();
                                PurchaseOrderVersionDetailsCustomData jeDetailCustom = (PurchaseOrderVersionDetailsCustomData) row.getPoVersionDetailCustomData();
                                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMapRow, replaceFieldMap, variableMap);
                                if (jeDetailCustom != null) {
                                    JSONObject params = new JSONObject();
                                    params.put("isExport", isExport);
                                    params.put("isForReport", isForReport);
                                    params.put(Constants.userdf, userdf);
                                    boolean generateInvoiceFromTransactionForms = false;
                                    boolean isSOfromPO = false;
                                    /*
                                     * --- generateInvoiceFromTransactionForms
                                     * ->Flag is true if invoice is generated
                                     * from PO form------
                                     */
                                    if (requestParams.containsKey("generateInvoiceFromTransactionForms") && requestParams.get("generateInvoiceFromTransactionForms") != null) {
                                        generateInvoiceFromTransactionForms = (Boolean) requestParams.get("generateInvoiceFromTransactionForms");
                                    }
                                    /*
                                     * 'isSOfromPO' flag is used to get custom
                                     * field or dimension while generating SO
                                     * from PO
                                     */
                                    if (requestParams.containsKey("isSOfromPO") && requestParams.get("isSOfromPO") != null) {
                                        isSOfromPO = (Boolean) requestParams.get("isSOfromPO");
                                    }

                                    if ((isForDOGROLinking || isForLinking || FA_POlinkToFA_PI) && !isForReport || generateInvoiceFromTransactionForms || isForSGELinking || isSOfromPO || isJobWorkOutLinkedWithGRN || isJobWorkOutLinkedWithPI) {
                                        params.put("isLink", true);
                                        int moduleId = isForDOGROLinking ? Constants.Acc_Goods_Receipt_ModuleId : isConsignment ? Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId : (isForInvoice || generateInvoiceFromTransactionForms) ? Constants.Acc_Vendor_Invoice_ModuleId : Constants.Acc_Sales_Order_ModuleId;
                                        params.put("companyid", companyid);
                                        if (FA_POlinkToFA_PI) {
                                            moduleId = Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId;
                                        }
                                        if (isConsignment) {
                                            moduleId = Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId;
                                        }
                                        if (isForSGELinking) {
                                            moduleId = Constants.Acc_SecurityGateEntry_ModuleId;
                                        }
                                        if (isSOfromPO) {
                                            moduleId = Constants.Acc_Sales_Order_ModuleId;
                                        }
                                        params.put("linkModuleId", moduleId);
                                    }
                                    fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                                }

                                variableMap = new HashMap<String, Object>();
                                replaceFieldMap = new HashMap<String, String>();
                                HashMap<String, Object> podrequestParams = new HashMap<String, Object>();
                                podrequestParams.put("podetailId", row.getID());
                                podrequestParams.put("companyId", (String) requestParams.get("companyid"));
                                KwlReturnObject idcustresultForProduct = accPurchaseOrderobj.getPODetailsCustomDataForProduct(podrequestParams);
                                PurchaseOrderDetailProductCustomData poDetailProductCustomData = null;
                                if (idcustresultForProduct.getEntityList().size() > 0) {
                                    poDetailProductCustomData = (PurchaseOrderDetailProductCustomData) idcustresultForProduct.getEntityList().get(0);
                                }
                                if (poDetailProductCustomData != null) {
                                    JSONObject params = new JSONObject();
                                    params.put("isExport", isExport);
                                    params.put("isForReport", isForReport);
                                    params.put(Constants.userdf, userdf);
                                    setCustomColumnValuesForProduct(poDetailProductCustomData, FieldMap, replaceFieldMap, variableMap, params);
                                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                        String coldata = varEntry.getValue().toString();
                                        if (!StringUtil.isNullOrEmpty(coldata)) {
                                            obj.put(varEntry.getKey(), coldata);
                                        }
                                    }
                                }
                                double unitprice = row.getRate();
                                if (row.getPurchaseOrderVersion().isIsJobWorkOutOrder() && row.getProduct().getProducttype().getID().equalsIgnoreCase(Producttype.ASSEMBLY) && linkingFlag) {
                                    /**
                                     * If GR is Job Work Out then return
                                     * purchase price = Sum of Purchase Price of
                                     * All components (Which are define while
                                     * Assembly )
                                     */
                                    unitprice = accProductObj.getSumOfPurchasePriceForSubassebmblyItems(row.getProduct().getID());
                                }
                                obj.put("rate", unitprice);
                                obj.put("priceSource", row.getPriceSource() != null ? row.getPriceSource() : "");
                                obj.put("discountispercent", row.getDiscountispercent());

                                /**
                                 * get the volume discount discount for the
                                 * given product according its quantity.
                                 */
                                HashMap<String, Object> pricingDiscountRequestParams = new HashMap<String, Object>();
                                pricingDiscountRequestParams.put("isPricePolicyUseDiscount", true);
                                pricingDiscountRequestParams.put("productID", row.getProduct() != null ? row.getProduct().getID() : "");
                                pricingDiscountRequestParams.put("isPurchase", true);
                                pricingDiscountRequestParams.put("companyID", companyid);
                                pricingDiscountRequestParams.put("currencyID", currencyid);
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
                                /*
                                 * In exported file showing discount value with
                                 * proper % or currency symbol same as expander
                                 *
                                 */

                                if (isExport) {
                                    obj.put("prdiscount", row.getDiscountispercent() == 1 ? (row.getDiscount() != 0 ? (row.getDiscount() + "%") : row.getDiscount()) : currency.getSymbol() + " " + row.getDiscount());
                                } else {
                                    obj.put("prdiscount", row.getDiscount());
                                }

                                if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
                                    Product product = row.getProduct();
                                    isLocationForProduct = product.isIslocationforproduct();
                                    isWarehouseForProduct = product.isIswarehouseforproduct();
                                    isBatchForProduct = product.isIsBatchForProduct();
                                    isSerialForProduct = product.isIsSerialForProduct();
                                    isRowForProduct = product.isIsrowforproduct();
                                    isRackForProduct = product.isIsrackforproduct();
                                    isBinForProduct = product.isIsbinforproduct();
                                    productsDefaultLocation = (product.getLocation() != null && product.getLocation().getId() != null) ? product.getLocation().getId() : "";
                                    maxqty = product.getMaxOrderingQuantity();
                                    minqty = product.getMinOrderingQuantity();
                                    productsDefaultWarehouse = (product.getWarehouse() != null && product.getWarehouse().getId() != null) ? product.getWarehouse().getId() : "";
                                    if (extraCompanyPreferences != null && extraCompanyPreferences.getUomSchemaType() == Constants.PackagingUOM) {
                                        obj.put("caseuom", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUoM().getID() : "");
                                        obj.put("caseuomvalue", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUomValue() : 1);
                                        obj.put("inneruom", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUoM().getID() : "");
                                        obj.put("inneruomvalue", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUomValue() : 1);
                                        obj.put("stockuom", (product.getUnitOfMeasure() != null) ? product.getUnitOfMeasure().getID() : "");
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
                                obj.put("maxorderingquantity", maxqty);
                                obj.put("minorderingquantity", minqty);

                                if ((preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory())) {  //check if company level option is on then only we will check productt level
                                    if (isForDOGROLinking && ((isLocationForProduct && !StringUtil.isNullOrEmpty(productsDefaultLocation)) || (isWarehouseForProduct && !StringUtil.isNullOrEmpty(productsDefaultWarehouse))) && !isBatchForProduct && !isSerialForProduct) {
                                        obj.put("batchdetails", getdefaultBatchJson(row.getProduct(), row.getID(), row.getQuantity()));
                                    }
                                }
                                double rowTaxPercent = 0;
                                double rowTaxAmount = 0;
                                boolean isRowTaxApplicable = false;
                                if (row.getTax() != null) {
                                    KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), po.getOrderDate(), row.getTax().getID());
                                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                                    isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                                    if (isRowTaxApplicable) {
                                        rowTaxAmount = row.getRowTaxAmount();
                                    }
                                }
                                obj.put("prtaxpercent", rowTaxPercent);
                                obj.put("rowTaxAmount", extraCompanyPreferences.getLineLevelTermFlag() == 1 ? row.getRowTermAmount() : rowTaxAmount);
                                obj.put("taxamount", rowTaxAmount);
                                obj.put("recTermAmount", row.getRowTermAmount());
                                obj.put("OtherTermNonTaxableAmount", row.getOtherTermNonTaxableAmount());
                                obj.put("rowTaxPercent", rowTaxPercent);
                                obj.put("prtaxid", row.getTax() == null ? "None" : row.getTax().getID());

                                /*
                                 * These keys are used while exporting details
                                 */
                                double rowamountwithgst = 0;
                                double discountValueForExcel = 0, amountForExcelFile = 0;
                                if (row.getPurchaseOrderVersion().isGstIncluded()) {//if gstincluded is the case
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

                                obj.put(Constants.unitpriceForExcelFile, row.getRate());//obj.put(Constants.unitpriceForExcelFile, (Double) bAmt.getEntityList().get(0));
                                obj.put("marginExchangeRate", row.getPurchaseOrderVersion() != null ? row.getPurchaseOrderVersion().getExternalCurrencyRate() : 0);
                                double baseuomrate = row.getBaseuomrate();
                                if (row.getUom() != null) {
                                    obj.put("uomid", row.getUom().getID());
                                } else {
                                    obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
                                }
                                double quantity = 0;
                                double invoiceRowProductQty = row.getQuantity() * baseuomrate;
                                double remainedQty = invoiceRowProductQty;// which has not been linked yet

                                quantity = row.getQuantity();
                                obj.put("quantity", quantity);
                                obj.put("copyquantity", quantity);
                                obj.put("dquantity", quantity);
                                obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, baseuomrate, companyid));
                                obj.put("baseuomrate", baseuomrate);


                                // Added vendor related information for Profit Margin Calculation
                                JSONObject jObj = new JSONObject();
                                if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                                    jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                                }
                                if (jObj.has("isDisplayUOM") && jObj.get("isDisplayUOM") != null && (Boolean) jObj.get("isDisplayUOM") != false) {
                                    obj = accProductObj.getProductDisplayUOM(row.getProduct(), quantity, baseuomrate, true, obj);
                                }
                                if (extraCompanyPreferences.isActivateProfitMargin() && isForLinking) {
                                    obj.put("vendorid", (row.getPurchaseOrderVersion() != null && row.getPurchaseOrderVersion().getVendor() != null) ? row.getPurchaseOrderVersion().getVendor().getID() : "");
                                    obj.put("vendorcurrexchangerate", row.getPurchaseOrderVersion() != null && row.getPurchaseOrderVersion().getExternalCurrencyRate() != 0 ? (1 / row.getPurchaseOrderVersion().getExternalCurrencyRate()) : 1);
                                    obj.put("vendorcurrencyid", (row.getPurchaseOrderVersion() != null && row.getPurchaseOrderVersion().getCurrency() != null) ? row.getPurchaseOrderVersion().getCurrency().getCurrencyID() : "");
                                    obj.put("vendorcurrencysymbol", (row.getPurchaseOrderVersion() != null && row.getPurchaseOrderVersion().getCurrency() != null) ? row.getPurchaseOrderVersion().getCurrency().getSymbol() : "");
                                    obj.put("vendorunitcost", row.getRate());
                                }

                                if (isForLinking || isForDOGROLinking) {// in case of linking in normal transactions not lease consignment etc.
                                    if (row.getTax() != null && invoiceRowProductQty > 0) {
                                        double taxAmt = (rowTaxAmount / invoiceRowProductQty) * remainedQty;
                                        obj.put("rowTaxAmount", taxAmt);
                                        obj.put("taxamount", taxAmt);
                                    }
                                }
                                obj.put("rateIncludingGst", authHandler.roundUnitPrice(row.getRateincludegst(), companyid));
                                obj.put("israteIncludingGst", po.isGstIncluded());
                                obj.put("balanceQuantity", row.getBalanceqty());
                                if (!StringUtil.isNullOrEmpty(row.getSalesorderdetailid())) {
                                    KwlReturnObject sodetailresult = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), row.getSalesorderdetailid());
                                    SalesOrderDetail salesOrderDetail = (SalesOrderDetail) sodetailresult.getEntityList().get(0);
                                    if (salesOrderDetail != null) {
                                        obj.put("linkto", salesOrderDetail.getSalesOrder().getSalesOrderNumber());
                                        obj.put("linkid", salesOrderDetail.getSalesOrder().getID());
                                        obj.put("rowid", salesOrderDetail.getID());
                                        obj.put("savedrowid", row.getID());
                                        obj.put("docrowid", row.getID());
                                        obj.put("linktype", 0);

                                        /*
                                         * In Edit case need Sales Order
                                         * customer id and isPOFromSO flag
                                         */
                                        if (salesOrderDetail.getSalesOrder().getCustomer() != null) {
                                            obj.put("customeridforshippingaddress", salesOrderDetail.getSalesOrder().getCustomer().getID());
                                            obj.put("isPOfromSO", true);
                                        }
                                    }
                                } else if (row.getVqdetail() != null) {
                                    VendorQuotationDetail vqDetail = row.getVqdetail();
                                    obj.put("linkto", vqDetail.getVendorquotation().getQuotationNumber());
                                    obj.put("linkid", vqDetail.getVendorquotation().getID());
                                    obj.put("rowid", vqDetail.getID());
                                    obj.put("savedrowid", row.getID());
                                    obj.put("docrowid", row.getID());
                                    obj.put("linktype", 2);
                                } else if (!StringUtil.isNullOrEmpty(row.getPurchaseRequisitionDetailId())) {
                                    KwlReturnObject sodetailresult = accountingHandlerDAOobj.getObject(PurchaseRequisitionDetail.class.getName(), row.getPurchaseRequisitionDetailId());
                                    PurchaseRequisitionDetail purchaseRequisitionDetail = (PurchaseRequisitionDetail) sodetailresult.getEntityList().get(0);
                                    if (purchaseRequisitionDetail != null) {
                                        obj.put("linkto", purchaseRequisitionDetail.getPurchaserequisition().getPrNumber());
                                        obj.put("linkid", purchaseRequisitionDetail.getPurchaserequisition().getID());
                                        obj.put("rowid", purchaseRequisitionDetail.getID());
                                    }
                                    obj.put("savedrowid", row.getID());
                                    obj.put("docrowid", row.getID());
                                    obj.put("linktype", 5);
                                } else {
                                    obj.put("linkto", "");
                                    obj.put("linkid", "");
                                    obj.put("linktype", -1);
                                }

                                //Check Integration and QA Approval flow then check status
                                boolean rejected = false;
                                if (row.getQastatus() == Constants.QA_Rejected) {
                                    rejected = true;
                                }
                                String salesOrPurchase = requestParams.containsKey("termSalesOrPurchaseCheck") ? requestParams.get("termSalesOrPurchaseCheck").toString() : "false";
                                if (extraCompanyPreferences.getLineLevelTermFlag() == 1) { // Fetch Vat term details of Product
                                    if (!sopolinkflag) {
                                        Map<String, Object> mapData = new HashMap<String, Object>();
                                        mapData.put("productid", row.getProduct().getID());
                                        mapData.put("salesOrPurchase", salesOrPurchase);
                                        mapData.put("povdetails", row.getID());
                                        KwlReturnObject result6 = accPurchaseOrderobj.getPurchaseOrderVersionDetailsTermMap(mapData);
                                        if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                                            ArrayList<PurchaseOrderVersionDetailsTermMap> productTermDetail = (ArrayList<PurchaseOrderVersionDetailsTermMap>) result6.getEntityList();
                                            JSONArray productTermJsonArry = new JSONArray();
                                            for (PurchaseOrderVersionDetailsTermMap productTermsMapObj : productTermDetail) {
                                                JSONObject productTermJsonObj = new JSONObject();
                                                productTermJsonObj.put("id", productTermsMapObj.getID());
                                                productTermJsonObj.put("productid", productTermsMapObj.getProduct().getID());
                                                productTermJsonObj.put("termid", productTermsMapObj.getTerm().getId());
                                                /**
                                                 * ERP-32829
                                                 */
                                                productTermJsonObj.put("productentitytermid", productTermsMapObj.getEntitybasedLineLevelTermRate() != null ? productTermsMapObj.getEntitybasedLineLevelTermRate().getId() : "");
                                                productTermJsonObj.put("isDefault", productTermsMapObj.isIsGSTApplied());
                                                productTermJsonObj.put("term", productTermsMapObj.getTerm().getTerm());
                                                productTermJsonObj.put("formula", productTermsMapObj.getTerm().getFormula());
                                                productTermJsonObj.put("formulaids", productTermsMapObj.getTerm().getFormula());
                                                productTermJsonObj.put("termpercentage", productTermsMapObj.getPercentage());
                                                productTermJsonObj.put("originalTermPercentage", productTermsMapObj.getTerm().getPercentage()); // For Service Tax Abatemnt calculation
                                                productTermJsonObj.put("termamount", productTermsMapObj.getTermamount());
                                                productTermJsonObj.put("glaccountname", productTermsMapObj.getTerm().getAccount().getAccountName());
                                                productTermJsonObj.put("glaccount", productTermsMapObj.getTerm().getAccount().getID());
                                                productTermJsonObj.put("IsOtherTermTaxable", productTermsMapObj.getTerm().isOtherTermTaxable());
                                                productTermJsonObj.put("sign", productTermsMapObj.getTerm().getSign());
                                                productTermJsonObj.put("purchasevalueorsalevalue", productTermsMapObj.getPurchaseValueOrSaleValue());
                                                productTermJsonObj.put("deductionorabatementpercent", productTermsMapObj.getDeductionOrAbatementPercent());
                                                productTermJsonObj.put("assessablevalue", productTermsMapObj.getAssessablevalue());
                                                productTermJsonObj.put("taxtype", productTermsMapObj.getTaxType());
                                                //                                productTermJsonObj.put("taxvalue", productTermsMapObj.getTaxType()==0 ? productTermsMapObj.getTermamount() : productTermsMapObj.getPercentage());
                                                productTermJsonObj.put("taxvalue", productTermsMapObj.getPercentage());
                                                productTermJsonObj.put("termtype", productTermsMapObj.getTerm().getTermType());
                                                productTermJsonObj.put("termsequence", productTermsMapObj.getTerm().getTermSequence());
                                                productTermJsonObj.put("formType", productTermsMapObj.getTerm().getFormType());
                                                productTermJsonObj.put("creditnotavailedaccount", productTermsMapObj.getTerm().getCreditNotAvailedAccount() != null ? productTermsMapObj.getTerm().getCreditNotAvailedAccount().getID() : "");
                                                productTermJsonObj.put("payableaccountid", productTermsMapObj.getTerm().getPayableAccount() != null ? productTermsMapObj.getTerm().getPayableAccount().getID() : "");
                                                productTermJsonArry.put(productTermJsonObj);
                                            }
                                            obj.put("LineTermdetails", productTermJsonArry.toString());
                                        }
                                    }
                                }
                                if (doflag || isForInvoice) {//Loading only open purchase order rows in GR or Invoice
                                    if (!rejected && (extraCompanyPreferences.isAllowZeroQuantityInPO() ? addobj >= 0 : addobj > 0) && !row.isIsLineItemClosed()) {
                                        jArr.put(obj);
                                    }
                                } else {
                                    if (!rejected && (extraCompanyPreferences.isAllowZeroQuantityInPO() ? addobj >= 0 : addobj > 0)) {
                                        jArr.put(obj);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception je) {
            throw ServiceException.FAILURE("getPurchaseOrderRows : " + je.getMessage(), je);
        }
        return jArr;
    }
    
    public JSONArray getExpenseDetailPOVersionRows(JSONArray jArr, Map requestParams, PurchaseOrderVersion purchaseOrder, List<ExpensePOVersionDetails> expensepodetails, HashMap<String, Integer> FieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap) throws JSONException, UnsupportedEncodingException, ServiceException {
        String companyid = (String) requestParams.get("companyid");
        boolean isExport = false;
        boolean isForReport = false;
        boolean isForLinking = false;
        if (requestParams.containsKey("isExport") && requestParams.get("isExport") != null) { // True in case of Export
            isExport = (Boolean) requestParams.get("isExport");
        }
        if (requestParams.containsKey("isForReport") && requestParams.get("isForReport") != null) { // True in case of Export
            isForReport = (Boolean) requestParams.get("isForReport");
        }
        if (requestParams.containsKey("isForLinking") && requestParams.get("isForLinking") != null) { // True in case of Export
            isForLinking = (Boolean) requestParams.get("isForLinking");
        }
        if (purchaseOrder != null && expensepodetails != null && !expensepodetails.isEmpty()) {
            for (ExpensePOVersionDetails row : expensepodetails) {
                JSONObject obj = new JSONObject();
                obj.put("billid", purchaseOrder.getID());
                obj.put("billno", purchaseOrder.getPurchaseOrderNumber());
                obj.put("currencysymbol", (purchaseOrder.getCurrency() == null ? "" : purchaseOrder.getCurrency().getSymbol()));
                obj.put("currencyCode", purchaseOrder.getCurrency() == null ? "" : purchaseOrder.getCurrency().getCurrencyCode());
                obj.put("srno", row.getSrno());
                obj.put("rowid", row.getID());
                obj.put("accountid", row.getAccount().getID());
                obj.put("desc", StringUtil.DecodeText(row.getDescription()));
                obj.put("debit", row.isIsdebit());
                obj.put("isgstincluded", purchaseOrder.isGstIncluded());
                obj.put("rateIncludingGstEx", row.getRateIncludingGst());
                obj.put("accountname", row.getAccount().getName());
                obj.put("isexpenseinv", purchaseOrder.isIsExpenseType());
                obj.put("rate", row.getRate());
                obj.put(Constants.unitpriceForExcelFile, row.getRate());//for export excelfile
                obj.put("orignalamount", row.getAmount());
                obj.put(Constants.amountForExcelFile, row.getAmount());
                obj.put("amount", row.getAmount());
                obj.put("includeprotax", row.getTax() != null ? true : false);
                /*
                 * disc.isInPercent()=1 (discountispercent is percentage)
                 * disc.isInPercent()=0 (discountispercent is flat)
                 */
                Discount disc = row.getDiscount();
                if (disc != null) {

                    /*
                     * In exported file showing discount value with proper % or
                     * currency symbol same as expander
                     *
                     */

                    if (isExport) {
                        obj.put("prdiscount", disc.isInPercent() ? (disc.getDiscount() + "%") : (purchaseOrder.getCurrency() == null ? "" : purchaseOrder.getCurrency().getSymbol()) + " " + disc.getDiscount());
                    } else {
                        obj.put("prdiscount", disc.getDiscount());
                    }

                    /*
                     * If discount in percent then calculate
                     */
                    obj.put("discountvalue", disc.getDiscountValue());
                    obj.put("discountispercent", disc.isInPercent() ? 1 : 0);
                } else {
                    obj.put("prdiscount", 0);
                    obj.put("discountvalue", 0);
                    obj.put("discountispercent", 1);
                }
                double taxPercent = 0;
                double rowTaxPercent = 0;
                if (row.getTax() != null) {
                    KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, purchaseOrder.getOrderDate(), row.getTax().getID());
                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                }
                obj.put("prtaxpercent", rowTaxPercent);
                if (purchaseOrder.isGstIncluded()) {
                    obj.put("isIncludingGst", true);
                }
                obj.put("rowTaxAmount", row.getRowTaxAmount());
                obj.put("taxamount", row.getRowTaxAmount());
                obj.put("prtaxid", row.getTax() == null ? "" : row.getTax().getID());
                if (purchaseOrder.getTax() != null) {
                    KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, purchaseOrder.getOrderDate(), purchaseOrder.getTax().getID());
                    taxPercent = (Double) perresult.getEntityList().get(0);
                }
                obj.put("taxpercent", taxPercent);

                // ## Get Custom Field Data 
                Map<String, Object> variableMap = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList();
                Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                ExpensePOVersionDetailCustomData jeDetailCustom = (ExpensePOVersionDetailCustomData)row.getExpensePOVersionDetailCustomData();
                if (jeDetailCustom != null) {
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                    DateFormat userdf = requestParams.containsKey(Constants.userdf) ? (DateFormat) requestParams.get(Constants.userdf) : null;
                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, isExport);
                        params.put("isForReport", isForReport);
                        params.put("userdf", userdf);
                        if (isForLinking) {
                            params.put("isLink", true);
                            int moduleId = Constants.Acc_Vendor_Invoice_ModuleId;
                            params.put("companyid", companyid);
                            params.put("linkModuleId", moduleId);
                        }
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                }
                jArr.put(obj);
            }
        }
        return jArr;
    }  
    
@Override 
    public JSONObject getPurchaseOrdersMerged(JSONObject paramJObj) {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = getPurchaseOrderMap(paramJObj);
            jobj = getPurchaseOrdersMerged(requestParams, paramJObj);
        } catch (Exception ex) {
            Logger.getLogger(accPurchaseOrderController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
 
  public JSONObject getPurchaseOrdersMerged(HashMap<String, Object> requestParams ,JSONObject request) throws ServiceException{
   
      JSONObject jobj = new JSONObject();
      JSONArray DataJArr = new JSONArray();
      boolean issuccess = true;
        /*
         *  includeAllRec = true if All is selected in View report filter 
         */
        boolean includeAllRec = true;
         String msg = "";
        try {
            /////HashMap<String, Object> requestParams = getPurchaseOrderMap(request);
            boolean consolidateFlag = request.optString("consolidateFlag") != null ? Boolean.parseBoolean(request.optString("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.optString("companyids") != null && !StringUtil.isNullOrEmpty(request.optString("companyids"))) ? request.optString("companyids").split(",") : request.optString(Constants.companyKey).split(",");
            String gcurrencyid = (consolidateFlag && request.optString("gcurrencyid") != null) ? request.optString("gcurrencyid") : request.optString(Constants.currencyKey);
            boolean isConsignment = request.optString("isConsignment")!=null?Boolean.parseBoolean(request.optString("isConsignment")):false;
            boolean isJobWorkOrderReciever = request.optString("isJobWorkOrderReciever")!=null?Boolean.parseBoolean(request.optString("isJobWorkOrderReciever")):false;
            boolean isForJobWorkOut = request.optString("isForJobWorkOut")!=null?Boolean.parseBoolean(request.optString("isForJobWorkOut")):false;
            boolean isJobWorkWithoutGRN = request.optString("isJobWorkWithoutGRN")!=null?Boolean.parseBoolean(request.optString("isJobWorkWithoutGRN")):false;
            boolean isFixedAsset = request.optString("isFixedAsset")!=null?Boolean.parseBoolean(request.optString("isFixedAsset")):false; // For line-level PO on dropdown            boolean eliminateflag = consolidateFlag;
            if (consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            // get TradingFlow indicator to determine which tables to be used for Outstanding Purchase Orders report
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), request.optString(Constants.companyKey));
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            Boolean isTradingFlow = false;
            if (pref != null && pref.isWithInvUpdate()) {
                isTradingFlow = true;
            }
            requestParams.put("isTradingFlow", isTradingFlow);
            if (isConsignment) {
                requestParams.put("isConsignment", isConsignment);
            }
            if(isFixedAsset){
                requestParams.put("isFixedAsset",isFixedAsset);
            }
            if(!StringUtil.isNullOrEmpty(request.optString("linknumber"))){
                requestParams.put("linknumber", request.optString("linknumber"));
            }
            if (!StringUtil.isNullOrEmpty(request.optString("bulkInv"))) {
                requestParams.put("bulkInv", request.optString("bulkInv"));
            }
            if (!StringUtil.isNullOrEmpty(request.optString("projectid"))) {
                requestParams.put("projectid", request.optString("projectid"));
            }
            if (!StringUtil.isNullOrEmpty(request.optString("searchString"))) {
                requestParams.put("searchString", request.optString("searchString"));
            }
            if (!StringUtil.isNullOrEmpty(request.optString("ss"))) {
                requestParams.put("ss", request.optString("ss"));
            }
            /*
            * Fetch parameter for generate list of unlinked PO
            */
            
            if (!StringUtil.isNullOrEmpty(request.optString("isfromsearchwin"))) {
                if(Boolean.parseBoolean(request.optString("isfromsearchwin"))){
                    requestParams.put("isfromsearchwin", Boolean.parseBoolean(request.optString("isfromsearchwin")));
                    if(!StringUtil.isNullOrEmpty(request.optString("linkedWithModuleId"))){
                        requestParams.put("linkedWithModuleId", Integer.parseInt(request.optString("linkedWithModuleId")));
                    }

                    if(!StringUtil.isNullOrEmpty(request.optString("closeflagForLink"))){
                        requestParams.put("closeflagForLink", Boolean.parseBoolean(request.optString("closeflagForLink")));
                    }
                }
            }
             /* Getting relevant type Document Parameter 
            1.orderLinkedWithDocType=12-If PO not Linked with any GR or Invoice
            
            2.orderLinkedWithDocType=13-If PO  Linked with  Invoice Only
            
            3.orderLinkedWithDocType=14-If PO  Linked with GR Only
            
            4.orderLinkedWithDocType=15-If PO  Linked with  GR & Invoice both
            */
            
            int orderLinkedWithDocType = request.optString("orderLinkedWithDocType")!=null && request.optString("orderLinkedWithDocType")!="" ? Integer.parseInt(request.optString("orderLinkedWithDocType")):0;
            requestParams.put("orderLinkedWithDocType", orderLinkedWithDocType);
           /* My PO flag true if Apllying filter MY PO from PO report*/
            boolean myPO = request.optString("myPO")!=null?Boolean.parseBoolean(request.optString("myPO")):false;
            requestParams.put("myPO", myPO);
            String userId = request.optString(Constants.useridKey);
            requestParams.put("userid", userId);
            
             ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", request.optString(Constants.companyKey));
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                int permCode = request.optInt("permCode");
                if (!((permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE is true then user has permission to view all vendors documents,so at that time there is need to filter record according to user&agent. 
                     */
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                }
                Map<String, Object> salesPersonParams = new HashMap<>();

                salesPersonParams.put("userid",  request.optString(Constants.useridKey));
                salesPersonParams.put("companyid", request.optString(Constants.companyKey));
                salesPersonParams.put("grID", "20");
                KwlReturnObject masterItemByUserList = accountingHandlerDAOobj.getMasterItemByUserID(salesPersonParams);
                List<MasterItem> masterItems = masterItemByUserList.getEntityList();
                String salesPersons = "";
                StringBuffer salesPersonids = new StringBuffer();
                for (Object obj : masterItems) {
                    if (obj != null) {
                        salesPersonids.append(obj.toString() + ",");
                    }
                }
                if (salesPersonids.length() > 0) {
                    salesPersons = salesPersonids.substring(0, (salesPersonids.length() - 1));
                    requestParams.put("salesPersonid", salesPersons);
               }
            }
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(),  request.optString(Constants.useridKey));
            User user = (User) userResult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(user.getDepartment())) {
                requestParams.put("userDepartment", user.getDepartment());
            }
            
            KwlReturnObject result = null;
            String companyid = "";
            String dir = "";
            String sort = "";
            if (!StringUtil.isNullOrEmpty(request.optString("dir")) && !StringUtil.isNullOrEmpty(request.optString("sort"))) {
                dir = request.optString("dir");
                sort = request.optString("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.put("companyid", companyid);
                request.put("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                boolean isForTemplate = false;
                String billId = "";
                if (!StringUtil.isNullOrEmpty(request.optString("isForTemplate"))) {
                    isForTemplate = Boolean.parseBoolean(request.optString("isForTemplate"));
                    requestParams.put("isForTemplate", isForTemplate);
                }
                if (!StringUtil.isNullOrEmpty(request.optString("billid"))) {
                    billId = request.optString("billid");
                    requestParams.put("billId", billId);
                }
                boolean isOutstanding = false;
                requestParams.put("isJobWorkOrderReciever", isJobWorkOrderReciever);
                requestParams.put("isJobWorkWithoutGRN", isJobWorkWithoutGRN);
                requestParams.put("isForJobWorkOut", isForJobWorkOut);
                isOutstanding = request.optString("isOutstanding") != null ? Boolean.parseBoolean(request.optString("isOutstanding")) : false;
                requestParams.put("isOutstanding", isOutstanding);
                if (!StringUtil.isNullOrEmpty(request.optString("includeAllRec"))) {
                    includeAllRec = Boolean.parseBoolean(request.optString("includeAllRec"));
                    requestParams.put("includeAllRec", includeAllRec);
                }
                /* Get PO with or without GR/invoice or with GR+Invoice both*/
                if (orderLinkedWithDocType!=0) {
                        result = accPurchaseOrderobj.getRelevantPurchaseOrderLinkingWise(requestParams);
                    } else if (!isOutstanding) {
                        result = accPurchaseOrderobj.getPurchaseOrdersMerged(requestParams);
                    } else {
                        result = accPurchaseOrderobj.getOutstandingPurchaseOrders(requestParams);
                    }

                DataJArr = getPurchaseOrdersJsonMerged(requestParams, result.getEntityList(), DataJArr);
            }
            int cnt = consolidateFlag ? DataJArr.length() : result.getRecordTotalCount();
            JSONArray pagedJson = DataJArr;
            if (consolidateFlag) {
                String start = request.optString(Constants.start);
                String limit = request.optString(Constants.limit);
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                }
            }
            jobj.put("data", pagedJson);
            jobj.put("count", cnt);
        }  catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(AccPurchaseOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accPurchaseOrderController.getPurchaseOrders : " + ex.getMessage();
            Logger.getLogger(AccPurchaseOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccPurchaseOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
        }
     return jobj;
  
  }
 
/*Description: It is called when fetching PO address in GRN Module*/  
    @Override
    public JSONObject fetchShippingAddressForGRN(JSONObject paramJObj) throws ServiceException, SessionExpiredException, JSONException {
        JSONObject shippingReturnObj = new JSONObject();
        boolean isSuccess = false;
        int linkedmodule = 0;
        int moduleid = 0;
        if (paramJObj.has(Constants.moduleid) && paramJObj.get(Constants.moduleid) != null) {
            moduleid = Integer.parseInt(paramJObj.optString(Constants.moduleid));
        }
        StringBuilder shippingAddressBuildString = new StringBuilder();
        try {
            String companyid = paramJObj.getString(Constants.companyKey);
            
            /*Case: When GRN is linked with PO then it is used to fetch address of PO.
             * Description: From lineitem, we are fetching purchase order transaction id of source company to fetch BillingAddress Object.
             * Required: sodetailid of parent company fetched from podetailid of parent company
             */
            if (paramJObj.has("linkedmodule") && paramJObj.get("linkedmodule") != null && !StringUtil.isNullOrEmpty(paramJObj.optString("linkedmodule", null))) {
                linkedmodule = Integer.parseInt(paramJObj.optString("linkedmodule"));
                if (linkedmodule == Constants.Acc_Purchase_Order_ModuleId && moduleid == Constants.Acc_Goods_Receipt_ModuleId) {
                    String podetailid = paramJObj.getString("podetailid");
                    StringBuilder detailidBuilderString = new StringBuilder();
                    detailidBuilderString.append(podetailid);
                    JSONObject returnJobj = accGroupCompanyDAO.getSOdetailid(paramJObj, companyid, detailidBuilderString.toString());
                    if (returnJobj.has(Constants.RES_TOTALCOUNT) && returnJobj.optInt(Constants.RES_TOTALCOUNT, 0) > 0) {
                        if (returnJobj.has("sodetailid") && !StringUtil.isNullOrEmpty(returnJobj.optString("sodetailid", null))) {
                            paramJObj.put("sodetailid", returnJobj.optString("sodetailid"));
                        }
                        shippingReturnObj = fetchShippingAddress(paramJObj);
                    }
                }
            } else {//end of linkmodule id
        
            /*Case: When GRN is saved.No linking is provided. In that case fetching the GRN address of Source Company
             * Description: From lineitem, we are fetching gro transaction id of source company to fetch BillingAddress Object.
             * Required: dodetailid of parent company fetched from grodetailid of parent company
             */    
                
                if (paramJObj.has("rowid") && paramJObj.get("rowid") != null) {
                    String grodetailsid = paramJObj.optString("rowid");

                    //Fetching gro transaction id of source company from grodetail id of parent company
                    JSONObject returnJobj = accGroupCompanyDAO.getSourceCompanyGROTranasctionId(paramJObj, companyid, grodetailsid);
                    if (returnJobj.has(Constants.RES_TOTALCOUNT) && returnJobj.optInt(Constants.RES_TOTALCOUNT, 0) > 0) {
                        
                       if (returnJobj.has("sourcegroid") && !StringUtil.isNullOrEmpty(returnJobj.optString("sourcegroid", null))) {
                            String groid = returnJobj.optString("sourcegroid");
                            KwlReturnObject poResultObj = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), groid);
                            GoodsReceiptOrder groObj = (GoodsReceiptOrder) poResultObj.getEntityList().get(0);
                            StringBuilder addressBuildString = new StringBuilder();
                            
                            //Building shipping address from gro of source transaction id
                            if (groObj != null) {
                                KwlReturnObject sourceCompanyResultObj = accountingHandlerDAOobj.getObject(Company.class.getName(), groObj.getCompany().getCompanyID());
                                Company sourceCompanyObj = (Company) sourceCompanyResultObj.getEntityList().get(0);
                                shippingAddressBuildString.append("Source Subdomain : " + sourceCompanyObj.getSubDomain() + "\n");
                                shippingAddressBuildString.append("Source Goods Receipt Order Transaction No : " + groObj.getGoodsReceiptOrderNumber() + "\n\n");
                                
                                //If is used to build the address and show in Window
                                if (groObj.getBillingShippingAddresses() != null) {
                                    BillingShippingAddresses bsa = groObj.getBillingShippingAddresses();

                                    String shipAddr = CommonFunctions.getTotalBillingShippingAddress(bsa, false);
                                    if (!StringUtil.isNullOrEmpty(shipAddr)) {
                                        addressBuildString.append(shipAddr + "\n\n");
                                    }
                                    if (!StringUtil.isNullOrEmpty(bsa.getShippingMobile())) {
                                        addressBuildString.append("Mobile : " + bsa.getShippingMobile() + "\n");

                                    }
                                    if (!StringUtil.isNullOrEmpty(bsa.getShippingFax())) {
                                        addressBuildString.append("Fax : " + bsa.getShippingFax() + "\n");

                                    }
                                    if (!StringUtil.isNullOrEmpty(bsa.getShippingEmail())) {
                                        addressBuildString.append("Email : " + bsa.getShippingEmail() + "\n");

                                    }
                                    if (!StringUtil.isNullOrEmpty(bsa.getShippingRecipientName())) {
                                        addressBuildString.append("Recipient Name : " + bsa.getShippingRecipientName() + "\n");

                                    }
                                    if (!StringUtil.isNullOrEmpty(bsa.getShippingContactPerson())) {
                                        addressBuildString.append("Contact Person : " + bsa.getShippingContactPerson() + "\n");
                                    }
                                    if (!StringUtil.isNullOrEmpty(bsa.getShippingContactPersonNumber())) {
                                        addressBuildString.append("Contact Person No : " + bsa.getShippingContactPersonNumber() + "\n");
                                    }

                                    if (!StringUtil.isNullOrEmpty(bsa.getShippingContactPersonDesignation())) {
                                        addressBuildString.append("Contact Person Designation : " + bsa.getShippingContactPersonDesignation() + "\n");
                                    }
                                }
                                if (addressBuildString.length() > 0) {
                                    shippingAddressBuildString.append("Shipping Adress Details : " + "\n\n");
                                    shippingAddressBuildString.append(addressBuildString.toString());
                                }
                            }
                            isSuccess = true;
                        }
                    }
                    shippingReturnObj.put("shippingAddress", shippingAddressBuildString.toString());
                    shippingReturnObj.put(Constants.RES_success, isSuccess);
                }
            }
        } catch (ServiceException ex) {
        } catch (Exception ex) {
            Logger.getLogger(AccPurchaseOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return shippingReturnObj;
    }
  
    /*Description: It is called when fetching PO address of source company in PO Module of parent company*/  
    @Override
    public JSONObject fetchShippingAddress(JSONObject paramJObj) throws ServiceException, SessionExpiredException, JSONException {
        JSONObject shippingReturnObj = new JSONObject();
        DateFormat df = authHandler.getDateOnlyFormat();
        boolean isSuccess = false;

        StringBuilder shippingAddressBuildString = new StringBuilder();
        try {
            String companyid = paramJObj.getString(Constants.companyKey);
            String sodetailid = paramJObj.getString("sodetailid");
            StringBuilder detailidBuilderString = new StringBuilder();
            detailidBuilderString.append(sodetailid);
            
            /*Case: When PO is generated in parent company from sales order of parent company
             * Description:From podetailid of parent company,fetching the purchase order transaction id of source company 
            */            
            JSONObject returnJobj = accGroupCompanyDAO.getSourceCompanyPOTransactionid(paramJObj, companyid, detailidBuilderString.toString());
            
            if (returnJobj.has(Constants.RES_TOTALCOUNT) && returnJobj.optInt(Constants.RES_TOTALCOUNT, 0) > 0) {
                if (returnJobj.has("sourcepodetailid") && !StringUtil.isNullOrEmpty(returnJobj.optString("sourcepodetailid", null))) {
                    String sourcepodetailid = returnJobj.optString("sourcepodetailid");

                    KwlReturnObject poreturnObj = accountingHandlerDAOobj.getObject(PurchaseOrderDetail.class.getName(), sourcepodetailid);
                    PurchaseOrderDetail poDetailObj = (PurchaseOrderDetail) poreturnObj.getEntityList().get(0);
                    KwlReturnObject sourceCompanyResultObj = accountingHandlerDAOobj.getObject(Company.class.getName(), poDetailObj.getCompany().getCompanyID());
                    Company sourceCompanyObj = (Company) sourceCompanyResultObj.getEntityList().get(0);
                    shippingAddressBuildString.append("Source Subdomain : " + sourceCompanyObj.getSubDomain()+"\n");
                    StringBuilder addressBuildString = new StringBuilder();
                    KwlReturnObject poResultObj = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), poDetailObj.getPurchaseOrder().getID());
                    PurchaseOrder poObj = (PurchaseOrder) poResultObj.getEntityList().get(0);
                    if (poObj != null) {
                        shippingAddressBuildString.append("Source Purchase Order Transaction No : " + poObj.getPurchaseOrderNumber() + "\n\n");
                        if (poObj.getBillingShippingAddresses() != null) {
                            BillingShippingAddresses bsa = poObj.getBillingShippingAddresses();

                            String shipAddr=CommonFunctions.getTotalBillingShippingAddress(bsa, false); 
                            if (!StringUtil.isNullOrEmpty(shipAddr)) {
                                addressBuildString.append(shipAddr + "\n\n");
                            }

                            
                            
//                            if (!StringUtil.isNullOrEmpty(bsa.getShippingAddress())) {
//                                addressBuildString.append("Shipping Adress : " + bsa.getShippingAddress() + "\n");
//                            }
//
//                            if (!StringUtil.isNullOrEmpty(bsa.getShippingCity())) {
//                                addressBuildString.append("City :" + bsa.getShippingCity() + "\n");
//                            }
//
//                            if (!StringUtil.isNullOrEmpty(bsa.getShippingState())) {
//                                addressBuildString.append("State :" + bsa.getShippingState() + "\n");
//                            }
//
//                            if (!StringUtil.isNullOrEmpty(bsa.getShippingCounty())) {
//                                addressBuildString.append("Country : " + bsa.getShippingCounty() + "\n");
//                            }
//
//                            if (!StringUtil.isNullOrEmpty(bsa.getShippingPostal())) {
//                                addressBuildString.append("Postal Code : " + bsa.getShippingPostal() + "\n");
//                            }

                            if (!StringUtil.isNullOrEmpty(bsa.getShippingMobile())) {
                                addressBuildString.append("Mobile : " + bsa.getShippingMobile() + "\n");
                            }
                            if (!StringUtil.isNullOrEmpty(bsa.getShippingFax())) {
                                addressBuildString.append("Fax : " + bsa.getShippingFax() + "\n");
                            }
                            if (!StringUtil.isNullOrEmpty(bsa.getShippingEmail())) {
                                addressBuildString.append("Email : " + bsa.getShippingEmail() + "\n");
                            }
                            if (!StringUtil.isNullOrEmpty(bsa.getShippingRecipientName())) {
                                addressBuildString.append("Recipient Name : " + bsa.getShippingRecipientName() + "\n");
                            }
                            if (!StringUtil.isNullOrEmpty(bsa.getShippingContactPerson())) {
                                addressBuildString.append("Contact Person : " + bsa.getShippingContactPerson() + "\n");
                            }
                            if (!StringUtil.isNullOrEmpty(bsa.getShippingContactPersonNumber())) {
                                addressBuildString.append("Contact Person No : " + bsa.getShippingContactPersonNumber() + "\n");
                            }

                            if (!StringUtil.isNullOrEmpty(bsa.getShippingContactPersonDesignation())) {
                                addressBuildString.append("Contact Person Designation : " + bsa.getShippingContactPersonDesignation() + "\n");
                            }
                        }
                        if (addressBuildString.length() > 0) {
                            shippingAddressBuildString.append("Shipping Adress Details : " + "\n\n");
                            shippingAddressBuildString.append(addressBuildString.toString());
                        }
                    }
                    isSuccess = true;
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(AccPurchaseOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccPurchaseOrderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            shippingReturnObj.put("shippingAddress", shippingAddressBuildString.toString());
            shippingReturnObj.put(Constants.RES_success, isSuccess);
        }
        return shippingReturnObj;
    }
  
  @Override
    public JSONObject getRequisitions(JSONObject paramJObj) {
        JSONObject jobj=new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;             
        try {
            HashMap<String, Object> requestParams = getPurchaseOrderMap(paramJObj);
            boolean pendingapproval = (paramJObj.optString("pendingapproval",null) != null)? Boolean.parseBoolean(paramJObj.optString("pendingapproval","false")): false;
            if(StringUtil.isNullOrEmpty(paramJObj.optString("archieve",null))){
                requestParams.put("archieve", 0);
            } else {
                requestParams.put("archieve", Integer.parseInt(paramJObj.optString("archieve","0")));
            }      
            if(!StringUtil.isNullOrEmpty(paramJObj.optString("billid"))){
                requestParams.put("ID",paramJObj.optString("billid"));
            }
            boolean consolidateFlag = paramJObj.optString("consolidateFlag",null)!=null?Boolean.parseBoolean(paramJObj.optString("consolidateFlag")):false; 
            boolean isPRLinktoVQ = paramJObj.optString("isPRLinktoVQ",null)!=null?Boolean.parseBoolean(paramJObj.optString("isPRLinktoVQ")):false;  
            boolean isDraft = paramJObj.optString("isDraft",null)!=null?Boolean.parseBoolean(paramJObj.optString("isDraft")):false;  
            requestParams.put("isPRLinktoVQ", isPRLinktoVQ);
            String[] companyids = (consolidateFlag && paramJObj.optString("companyids",null)!=null)?paramJObj.optString("companyids").split(","):paramJObj.optString(Constants.companyKey).split(",");
            String gcurrencyid = (consolidateFlag && paramJObj.optString("gcurrencyid",null)!=null)?paramJObj.optString("gcurrencyid"):paramJObj.optString(Constants.globalCurrencyKey); 
            boolean isRequisitionOutstandingFilterApplied = paramJObj.optString("isRequisitionOutstandingFilterApplied",null)!=null?Boolean.parseBoolean(paramJObj.optString("isRequisitionOutstandingFilterApplied","false")):false;  
            requestParams.put("isRequisitionOutstandingFilterApplied", isRequisitionOutstandingFilterApplied);
            requestParams.put("isDraft", isDraft);
            requestParams.put("ID",paramJObj.optString("ID",null));
            String selectedCustomerIds = paramJObj.optString("combovalue");
            List selectedcustomerList = new ArrayList();
            /*
             * in case of RFQ, putting currencyid in currencyfilterfortrans
             */
            String currencyid = paramJObj.optString("currencyid",null)!=null ? paramJObj.optString("currencyid") : "";
            boolean isRFQ = (paramJObj.optString("isRFQ",null) != null) ? Boolean.parseBoolean(paramJObj.optString("isRFQ")) : false;
            String currencyfilterfortrans = isRFQ ? currencyid : (paramJObj.optString("currencyfilterfortrans",null)!=null ? paramJObj.optString("currencyfilterfortrans") :"");
            boolean onlyApprovedRecords = paramJObj.optString("onlyApprovedRecords",null)!=null ? Boolean.parseBoolean(paramJObj.optString("onlyApprovedRecords")):false;  // For linking in Vendor Quotations
            if(consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            String dir = "";
            String sort = "";
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("dir",null)) && !StringUtil.isNullOrEmpty(paramJObj.optString("sort",null))) {
                dir = paramJObj.optString("dir");
                sort = paramJObj.optString("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            if (paramJObj.optString("query") != null && !StringUtil.isNullOrEmpty(paramJObj.optString("query"))) {
                requestParams.put("query", paramJObj.optString("query", null));
            }
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), paramJObj.optString(Constants.useridKey));
            User user = (User) userResult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(user.getDepartment())) {
                requestParams.put("userDepartment", user.getDepartment());
            }
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), paramJObj.optString(Constants.companyKey));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            if (paramJObj.optString("requestModuleid",null) != null && !StringUtil.isNullOrEmpty(paramJObj.optString("requestModuleid",null))) {
                int requestModuleID = Integer.parseInt(paramJObj.optString("requestModuleid"));
                if (extraCompanyPreferences.isEnableLinkToSelWin()) {
                    requestParams.put("requestModuleid", requestModuleID);
                }
                if (extraCompanyPreferences.isEnableLinkToSelWin() && !Boolean.parseBoolean(paramJObj.optString("isGrid")) && (requestModuleID == Constants.Acc_Vendor_Quotation_ModuleId || requestModuleID == Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId)) {
                    requestParams.put("start", "0");
                    requestParams.put("limit", "10");
                }
            }
            
            if (!StringUtil.isNullOrEmpty(selectedCustomerIds) && !selectedCustomerIds.equals("All")) {
                requestParams.put("multiselectcustomerids", selectedCustomerIds);
                requestParams.put("ismultiselectcustomeridsFlag", true);
                KwlReturnObject selectedcustomer =  accPurchaseOrderobj.getPurchaseRequisition(requestParams);
                requestParams.remove("ismultiselectcustomeridsFlag");
                selectedcustomerList = selectedcustomer.getEntityList();
            }
            
            KwlReturnObject result = null;
            String companyid = "";
            for(int cnt=0; cnt<companyids.length; cnt++) {
                companyid = companyids[cnt];
                paramJObj.put("companyid", companyid);
                paramJObj.put("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                requestParams.put("currencyfilterfortrans", currencyfilterfortrans);
                requestParams.put("onlyApprovedRecords", onlyApprovedRecords);
                boolean ispending=false;
                if(!pendingapproval) {
                    result = accPurchaseOrderobj.getPurchaseRequisition(requestParams);
                } else {
                    ispending=true;
                    requestParams.put("userid", paramJObj.optString(Constants.useridKey));
                    result = accPurchaseOrderobj.getPendingPurchaseRequisition(requestParams);
                }
                selectedcustomerList.addAll(result.getEntityList());
                DataJArr = getRequisitionJson(paramJObj, selectedcustomerList, DataJArr,ispending,requestParams);
            }
            int cnt = result.getRecordTotalCount();
            int totalCount = cnt;
//            if(extraCompanyPreferences.isEnableLinkToSelWin() && Boolean.parseBoolean(paramJObj.optString("isGrid","false"))){
//                cnt = DataJArr.length();
//            }
            
            JSONArray pagedJson = DataJArr;
            if(consolidateFlag) {
                String start = paramJObj.optString(Constants.start);
                String limit = paramJObj.optString(Constants.limit);
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                }
            }
            jobj.put("data", pagedJson);
            jobj.put("count", cnt);
            jobj.put("totalCount",totalCount);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }   
    
  @Override  
    public JSONArray getRequisitionJson(JSONObject paramJObj, List list, JSONArray jArr, boolean ispending, HashMap<String, Object> filterRequestParams) throws ServiceException {
        try {
            HashMap<String, Object> requestParams = getPurchaseOrderMap(paramJObj);
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            String companyid =paramJObj.optString(Constants.companyKey);
            DateFormat userDateFormat = null;
            if (requestParams.containsKey(Constants.userdf) && requestParams.get(Constants.userdf) != null) {
                userDateFormat = (DateFormat) requestParams.get(Constants.userdf);
            }
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;

            boolean isFixedAsset = (paramJObj.optString("isFixedAsset",null) != null) ? Boolean.parseBoolean(paramJObj.optString("isFixedAsset")) : false;
            boolean isRFQfromFA_PR = (paramJObj.optString("isFixedAssetRFQ",null) != null) ? Boolean.parseBoolean(paramJObj.optString("isFixedAssetRFQ")) : false;
            boolean isRFQfromPR = (paramJObj.optString("isRFQ",null) != null) ? Boolean.parseBoolean(paramJObj.optString("isRFQ")) : false;
            boolean prvqlinkflag = (!StringUtil.isNullOrEmpty(paramJObj.optString("prvqlinkflag",null))) ? Boolean.parseBoolean(paramJObj.optString("prvqlinkflag")) : false;
            boolean prpolinkflag = (!StringUtil.isNullOrEmpty(paramJObj.optString("prpolinkflag",null))) ? Boolean.parseBoolean(paramJObj.optString("prpolinkflag")) : false;
            boolean closeflag = (paramJObj.optString("closeflag",null) != null) ? true : false;
            
            double baseamount = 0;
            
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                PurchaseRequisition purchaseReq = null;
                String reqid = "";
                String multipleRuleids="";
                if (!ispending) {
                    reqid = (String) itr.next();
                } else {
                    purchaseReq = (PurchaseRequisition) itr.next();
                }
                if (!StringUtil.isNullOrEmpty(reqid) || purchaseReq != null) {
                    if (!ispending) {
                        KwlReturnObject reqResult = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), reqid);
                        purchaseReq = (PurchaseRequisition) reqResult.getEntityList().get(0);
                    }
                    KWLCurrency currency = null;

                    if (purchaseReq.getCurrency() != null) {
                        currency = purchaseReq.getCurrency();
                    } else {
                        currency = purchaseReq.getVendor().getAccount().getCurrency() == null ? kwlcurrency : purchaseReq.getVendor().getAccount().getCurrency();
                    }
                    baseamount = getAmountBasetoCurrency(purchaseReq.getRows(), companyid, purchaseReq.getCompany().getCurrency().getCurrencyID(), purchaseReq.getCurrency().getCurrencyID(), purchaseReq.getRequisitionDate());
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", purchaseReq.getID());
                    hashMap.put("companyid", purchaseReq.getCompany().getCompanyID());
                    KwlReturnObject object = accInvoiceDAOobj.getinvoiceDocuments(hashMap);
                    int attachemntcount = object.getRecordTotalCount();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", purchaseReq.getID());
                    obj.put("companyid", purchaseReq.getCompany().getCompanyID());
                    obj.put("companyname", purchaseReq.getCompany().getCompanyName());
                    obj.put("personname", purchaseReq.getVendor() == null ? "" : purchaseReq.getVendor().getName());
                    obj.put("personemail", purchaseReq.getVendor() == null ? "" : purchaseReq.getVendor().getEmail());
                    obj.put("aliasname", purchaseReq.getVendor() == null ? "" : purchaseReq.getVendor().getAliasname());
                    obj.put("billno", purchaseReq.getPrNumber());
                    obj.put("prno", purchaseReq.getPrNumber());
                    String rfq = getLinkedRFQs(purchaseReq.getCompany().getCompanyID(), purchaseReq.getID());
                    obj.put("rfqno", rfq);
                    obj.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                    obj.put("isfavourite", purchaseReq.isFavourite());
                    obj.put("isprinted", purchaseReq.isPrinted());
                    obj.put("duedate", authHandler.getDateOnlyFormat().format(purchaseReq.getDueDate()));
                    obj.put("date", authHandler.getDateOnlyFormat().format(purchaseReq.getRequisitionDate()));
                    obj.put("archieve", purchaseReq.getArchieve());
                    String approvalStatus="";
                    JSONArray productJArr = new JSONArray();
                    HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(paramJObj);

                    for (PurchaseRequisitionDetail prDetail : purchaseReq.getRows()) {
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
                        KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, purchaseReq.getRequisitionDate(), 0.0);
                        double discAmountinBase = (Double) dAmount.getEntityList().get(0);
                        discAmountinBase = authHandler.round(discAmountinBase, companyid);

                        JSONObject productObj = new JSONObject();
                        productObj.put("productId", productId);
                        productObj.put("discountAmount", discAmountinBase);
                        productJArr.put(productObj);
                    }
                    if(purchaseReq.getApprovestatuslevel() < 0){
                        approvalStatus="Rejected";
                    }else if(purchaseReq.getApprovestatuslevel() < 11){
                        String ruleid = "",userRoleName="";
                        HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                        qdDataMap.put("companyid", companyid);
                        qdDataMap.put("level",purchaseReq.getApprovestatuslevel() );
                        qdDataMap.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                        KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                        Iterator ruleitr = flowresult.getEntityList().iterator();
                        while (ruleitr.hasNext()) {
                            Object[] row = (Object[]) ruleitr.next();
                            String rule = "";
                            if (row[2] != null) {
                                rule = row[2].toString();
                            }
                            String discountRule = "";
                            if (row[7] != null) {
                                discountRule = row[7].toString();
                            }
                            ruleid = (String) row[0];
                            int appliedUpon = Integer.parseInt(row[5].toString());
                            boolean sendForApproval = false;
                            
                            if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount) {
                                /*
                                 * Check If Rule is apply on specefic product or specefic product discount
                                 *  specefic product discount from multiapproverule window
                                 */
                                if (productJArr != null) {
                                       sendForApproval = AccountingManager.checkForProductAndProductDiscountRule(productJArr, appliedUpon, rule, discountRule);
                                }
                            }else if (appliedUpon == Constants.Specific_Products_Category) {
                                /*
                                 * Check If Rule is apply on product
                                 * category from multiapproverule window
                                 */
                                sendForApproval = accountingHandlerDAOobj.checkForProductCategoryForProduct(productJArr, appliedUpon, rule);
                            } else {
                                rule = rule.replaceAll("[$$]+", String.valueOf(baseamount));
                            }

                            if (StringUtil.isNullOrEmpty(rule) || sendForApproval || (!StringUtil.isNullOrEmpty(rule) && appliedUpon == Constants.Total_Amount && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                                multipleRuleids += ruleid + ",";
                            }
                        }
                    String[] multipleRuleidsArray = multipleRuleids.split(",");
                    for (int multiRule = 0; multiRule < multipleRuleidsArray.length; multiRule++) {
                        ruleid = multipleRuleidsArray[multiRule];
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
                                userRoleName += roleName+" "+userName + ", ";
                            }
                        }
                    }
                        
                        if (!StringUtil.isNullOrEmpty(userRoleName)) {
                            userRoleName = userRoleName.substring(0, userRoleName.length() - 2);
                        }
                        approvalStatus="Pending Approval" + ( StringUtil.isNullOrEmpty(userRoleName) ? "" : " by "+userRoleName )+" at Level - "+purchaseReq.getApprovestatuslevel();
                    } else {
                        approvalStatus="Approved";
                    }
                    obj.put("status", approvalStatus); // Level= -99 means Drafted , Level between 1 to 10 means pending for approval , Level<0 means Rejected and level=11 means approved
                    obj.put("deleted", purchaseReq.isDeleted());
                    obj.put("isDraft", purchaseReq.isIsDraft());
                    obj.put("createdby", purchaseReq.getCreatedby() == null ? "" : StringUtil.getFullName(purchaseReq.getCreatedby()));
                    obj.put("lasteditedby", purchaseReq.getModifiedby() == null ? "" : purchaseReq.getModifiedby().getFirstName() + " " + purchaseReq.getModifiedby().getLastName());
                    MasterItem gstRegistrationType = purchaseReq.getVendor() != null ? purchaseReq.getVendor().getGSTRegistrationType() : null;
                    if (gstRegistrationType != null && gstRegistrationType.getDefaultMasterItem() != null) {
                        obj.put("GSTINRegTypeDefaultMstrID", gstRegistrationType.getDefaultMasterItem().getID());
                    }
                    /*
                     * To check vendor quotation is linked with any other or not
                     */
                    KwlReturnObject linkRresult = accLinkDataDao.checkEntryForTransactionInLinkingTableForForwardReference(Constants.Acc_Purchase_Requisition_modulename, purchaseReq.getID());
                    list = linkRresult.getEntityList();
                    if (list != null && !list.isEmpty()) {
                        obj.put(Constants.IS_LINKED_TRANSACTION, true);
                    } else {
                        obj.put(Constants.IS_LINKED_TRANSACTION, false);
                    }


                    boolean incProTax = false;
                    Iterator itrRow = purchaseReq.getRows().iterator();
                    double amount = 0, amountinbase = 0, totalDiscount = 0, discountPrice = 0;
                    while (itrRow.hasNext()) {
                        PurchaseRequisitionDetail sod = (PurchaseRequisitionDetail) itrRow.next();
                        //amount+=sod.getQuantity()*sod.getRate();
                        double rowTaxPercent = 0;
                        if (sod.getTax() != null) {
                            requestParams.put("transactiondate", purchaseReq.getRequisitionDate());
                            requestParams.put("taxid", sod.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                            incProTax = true;
                        }

                        double quotationPrice = authHandler.round(sod.getQuantity() * sod.getRate(), companyid);
                        if (sod.getDiscountispercent() == 1) {
                            discountPrice = (quotationPrice) - (quotationPrice * sod.getDiscount() / 100);
                        } else {
                            discountPrice = quotationPrice - sod.getDiscount();
                        }

                        amount += discountPrice + (discountPrice * rowTaxPercent / 100);
                    }
                    obj.put("includeprotax", incProTax);
                    if (purchaseReq.getDiscount() != 0) {
                        if (purchaseReq.isPerDiscount()) {
                            totalDiscount = amount * purchaseReq.getDiscount() / 100;
                            amount = amount - totalDiscount;
                        } else {
                            amount = amount - purchaseReq.getDiscount();
                            totalDiscount = purchaseReq.getDiscount();
                        }
                        obj.put("discounttotal", purchaseReq.getDiscount());
                    } else {
                        obj.put("discounttotal", 0);
                    }

                    obj.put("discountispertotal", purchaseReq.isPerDiscount());
                    if (purchaseReq.getTax() != null) {
                        requestParams.put("transactiondate", purchaseReq.getRequisitionDate());
                        requestParams.put("taxid", purchaseReq.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        double TaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        amountinbase = amount + amount * TaxPercent / 100;
                    }
                    if (purchaseReq.getTax() != null) {
                        obj.put("amountinbase", amountinbase);
                    } else {
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amount, purchaseReq.getCurrency().getCurrencyID(), purchaseReq.getRequisitionDate(), 0);
                        obj.put("amountinbase", authHandler.round((Double) bAmt.getEntityList().get(0), companyid));
                    }
                    obj.put("amount", amount);
                    KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, amount, currency.getCurrencyID(), purchaseReq.getRequisitionDate(), 0);
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("currencycode", currency.getCurrencyCode());
                    obj.put("taxid", purchaseReq.getTax() == null ? "" : purchaseReq.getTax().getID());
                    obj.put("taxname", purchaseReq.getTax() == null ? "" : purchaseReq.getTax().getName());
                    double taxPercent = 0;
                    if (purchaseReq.getTax() != null) {
                        requestParams.put("transactiondate", purchaseReq.getRequisitionDate());
                        requestParams.put("taxid", purchaseReq.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];

                    }
                    double orderAmount = (Double) bAmt.getEntityList().get(0);
                    double ordertaxamount = (taxPercent == 0 ? 0 : orderAmount * taxPercent / 100);
                    obj.put("taxpercent", taxPercent);
                    obj.put("taxamount", ordertaxamount);
                    obj.put("orderamount", orderAmount);
                    obj.put("orderamountwithTax", orderAmount + ordertaxamount);
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("approvestatuslevel", purchaseReq.getApprovestatuslevel());
                    obj.put("memo", purchaseReq.getMemo());
                    obj.put("attachment", attachemntcount);
                    obj.put(Constants.SEQUENCEFORMATID, purchaseReq.getSeqformat() == null ? "" : purchaseReq.getSeqformat().getID());

                    //while linking Purchse requisition we need exchange rate of it. Currently we are not saving its exchange rate in BD so I need to fetch it from below code
                    //Once exchange rate will be save in DB then need to change this code and fetching value direct from DB
                    double externalcurrencyrate = 0;
                    KwlReturnObject result = accCurrencyDAOobj.getExcDetailID(requestParams, currency.getCurrencyID(), purchaseReq.getRequisitionDate(), null);
                    if (!result.getEntityList().isEmpty()) {
                        Iterator iterator = result.getEntityList().iterator();
                        ExchangeRateDetails erd = (ExchangeRateDetails) iterator.next();
                        externalcurrencyrate = erd != null ? erd.getExchangeRate() : 0.0;
                    }
                    obj.put("externalcurrencyrate", externalcurrencyrate);
                    //end of exchange rate part

                    // Get Global-level CUSTOMFIELDS for Purchase Requisition
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();

                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(purchaseReq.getCompany().getCompanyID(), !isFixedAsset ? Constants.Acc_Purchase_Requisition_ModuleId : Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);


                    KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(PurchaseRequisitionCustomData.class.getName(), purchaseReq.getID());
                    if (custumObjresult.getEntityList().size() > 0) {
                        DateFormat df = (DateFormat) requestParams.get("df");
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        PurchaseRequisitionCustomData purchaseRequisitionCustomData = (PurchaseRequisitionCustomData) custumObjresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(purchaseRequisitionCustomData, FieldMap, replaceFieldMap, variableMap);
                        if (purchaseRequisitionCustomData != null) {
                            boolean isExport = (filterRequestParams.get("isExport") == null) ? false : true;
                            JSONObject params = new JSONObject();
                            params.put("isExport", isExport);
                            params.put("companyid", filterRequestParams.get("companyid"));
                            params.put(Constants.userdf, userDateFormat);
                            boolean isPRLinktoVQ = false;
                            if (filterRequestParams.containsKey("isPRLinktoVQ") && filterRequestParams.get("isPRLinktoVQ") != null) {
                                isPRLinktoVQ = Boolean.FALSE.parseBoolean(filterRequestParams.get("isPRLinktoVQ").toString());
                            }
                            if (isPRLinktoVQ || prvqlinkflag || prpolinkflag || isRFQfromPR || isRFQfromFA_PR) {
                                params.put("isLink", true);
                                int moduleId = prvqlinkflag ? Constants.Acc_Vendor_Quotation_ModuleId : isRFQfromPR ? Constants.Acc_RFQ_ModuleId : isRFQfromFA_PR ? Constants.Acc_FixedAssets_RFQ_ModuleId : Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId;
                                if (prpolinkflag) {
                                    moduleId = Constants.Acc_Purchase_Order_ModuleId;
                                }
                                params.put("linkModuleId", moduleId);
                                params.put("customcolumn", 0);
                            }
                            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                        }
                    }

                    boolean addflag = true;
                    String status = "";
                    if (isFixedAsset) {
                        status = getPurchaseRequisitionStatus(purchaseReq);
                    }
                    obj.put("closeStatus", status);

                    JSONObject columnprefObj = new JSONObject();
                    if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                        columnprefObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                    }
                    boolean statusOfRequisitionForPO = false;
                    if (columnprefObj.has("statusOfRequisitionForPO") && columnprefObj.get("statusOfRequisitionForPO") != null && (Boolean) columnprefObj.get("statusOfRequisitionForPO") != false) {
                        statusOfRequisitionForPO = true;
                    }

                    if (statusOfRequisitionForPO) {
                        obj.put("statusofpoforrequisition", purchaseReq.isIsOpenInPO() ? "Open" : "Closed");
                    }

                    if (closeflag && purchaseReq.isDeleted()) {
                        addflag = false;
                    } else if (closeflag && (status.equalsIgnoreCase("Closed"))) {
                        addflag = false;
                    }

                    /*
                     * ----------- Only Open Requisition will load in PO while
                     * linking ------------
                     */
                    if (statusOfRequisitionForPO && prpolinkflag && !purchaseReq.isIsOpenInPO()) {
                        addflag = false;
                    }

                    if (addflag) {
                        jArr.put(obj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getRequisitionJson : " + ex.getMessage(), ex);
        }
        return jArr;
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
    
    @Override
    public JSONObject getRFQs(JSONObject paramJObj) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getPurchaseOrderMap(paramJObj);
            boolean pendingapproval = (paramJObj.optString("pendingapproval",null) != null) ? Boolean.parseBoolean(paramJObj.optString("pendingapproval","false")) : false;
            if (StringUtil.isNullOrEmpty(paramJObj.optString("archieve",null))) {
                requestParams.put("archieve", 0);
            } else {
                requestParams.put("archieve", Integer.parseInt(paramJObj.optString("archieve","0")));
            }
            boolean consolidateFlag = paramJObj.optString("consolidateFlag") != null ? Boolean.parseBoolean(paramJObj.optString("consolidateFlag","false")) : false;
            String[] companyids = (consolidateFlag && paramJObj.optString("companyids",null) != null) ? paramJObj.optString("companyids").split(",") : paramJObj.optString(Constants.companyKey).split(",");
            String gcurrencyid = (consolidateFlag && paramJObj.optString("gcurrencyid",null) != null) ? paramJObj.optString("gcurrencyid") : paramJObj.optString(Constants.globalCurrencyKey);
            if (consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            KwlReturnObject result = null;
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                paramJObj.put("companyid", companyid);
                paramJObj.put("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                if (!pendingapproval) {
                    result = accPurchaseOrderobj.getRequestForQuotations(requestParams);
                } else {
                    requestParams.put("userid", paramJObj.optString(Constants.useridKey));
                    result = accPurchaseOrderobj.getPendingPurchaseRequisition(requestParams);
                }
                DataJArr = getRFQJson(paramJObj, result.getEntityList(), DataJArr, requestParams);
            }
            int cnt = consolidateFlag ? DataJArr.length() : result.getRecordTotalCount();
            JSONArray pagedJson = DataJArr;
            if (consolidateFlag) {
                String start = paramJObj.optString(Constants.start);
                String limit = paramJObj.optString(Constants.limit);
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                }
            }
            jobj.put("data", pagedJson);
            jobj.put("count", cnt);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    @Override
     public JSONArray getRFQJson(JSONObject paramJObj, List list, JSONArray jArr,HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String companyid = requestParams.get("companyid").toString();
            DateFormat userdf=authHandler.getUserDateFormatterWithoutTimeZone(paramJObj);
            boolean rfqlinkflag=(!StringUtil.isNullOrEmpty(paramJObj.optString("rfqlinkflag",null)))?Boolean.parseBoolean(paramJObj.optString("rfqlinkflag")):false;
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                boolean isFixedAsset = (paramJObj.optString("isFixedAsset",null) != null) ? Boolean.parseBoolean(paramJObj.optString("isFixedAsset","false")) : false;
                String orderid = itr.next().toString();
                RequestForQuotation  rfQuotationObj=(RequestForQuotation)kwlCommonTablesDAOObj.getClassObject(RequestForQuotation.class.getName(), orderid);
                
                //Get Attachment details
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("invoiceID", rfQuotationObj.getID());
                hashMap.put("companyid", rfQuotationObj.getCompany().getCompanyID());
                KwlReturnObject object = accInvoiceDAOobj.getinvoiceDocuments(hashMap);
                int attachemntcount = object.getRecordTotalCount();
                
                JSONObject obj = new JSONObject();
                obj.put("billid", rfQuotationObj.getID());
                obj.put("companyid", rfQuotationObj.getCompany().getCompanyID());
                obj.put("companyname", rfQuotationObj.getCompany().getCompanyName());
                obj.put("attachment",attachemntcount);
                if(!StringUtil.isNullOrEmpty(rfQuotationObj.getVendors())) {
                    HashMap<String, Object> vReqParams = new HashMap();
                    String vendors = "";
                    ArrayList filter_names = new ArrayList();
                    ArrayList filter_params = new ArrayList();
                    String[] vendorIds = rfQuotationObj.getVendors().split(",");
                    for (int i = 0; i < vendorIds.length; i++) {
                        vendors += "'"+vendorIds[i]+"',";
                    }
                    vendors = vendors.substring(0, Math.max(0, vendors.length() - 1));
                    filter_names.add("INID");
                    filter_params.add(vendors);
                    vReqParams.put("filter_names", filter_names);
                    vReqParams.put("filter_params", filter_params);
                    KwlReturnObject result = accVendorDAOobj.getVendorList(vReqParams);
                    List<Vendor> vendorObjs = result.getEntityList();
                    String vendorName = "";
                    String aliasname = "";
                    String vendorEmails = "";
                    String billingEmailId = "";
                    //params to send to get billing address
                    HashMap<String, Object> addressParams = new HashMap<String, Object>();
                    addressParams.put("companyid", companyid);
                    addressParams.put("isDefaultAddress", true);
                    addressParams.put("isBillingAddress", true);
                    if(vendorObjs.size()>0) {
                        for(Vendor vobj : vendorObjs) {
                            vendorName += StringUtil.isNullOrEmpty(vobj.getName()) ? "" : " "+vobj.getName()+",";
                            aliasname += StringUtil.isNullOrEmpty(vobj.getAliasname()) ? "" : " "+vobj.getAliasname()+",";
                            if(!StringUtil.isNullOrEmpty(vobj.getEmail()))
                                vendorEmails += vobj.getEmail()+";";
                            addressParams.put("vendorid", vobj.getID());
                            VendorAddressDetails vendorAddressDetail = accountingHandlerDAOobj.getVendorAddressObj(addressParams);
                            billingEmailId += vendorAddressDetail != null ? vendorAddressDetail.getEmailID()+"," : "";
                        }
                         
                        
                        aliasname = aliasname.substring(0, Math.max(0, aliasname.length() - 1));
                        vendorName = vendorName.substring(0, Math.max(0, vendorName.length() - 1));
                        vendorEmails = vendorEmails.substring(0, Math.max(0, vendorEmails.length() - 1));
                        billingEmailId = billingEmailId.substring(0, Math.max(0, billingEmailId.length() - 1));
                        obj.put("personemail", vendorEmails);
                        obj.put("billingEmail", billingEmailId);
                        obj.put("personname", vendorName);
                        obj.put("aliasname", aliasname);
                        obj.put("personid",rfQuotationObj.getVendors());
                    }
                }
                
                /*  To check RFQ is Linked in Vendor Quotation or not  */
                KwlReturnObject linkRresult = accLinkDataDao.checkEntryForTransactionInLinkingTableForForwardReference(Constants.Acc_RFQ_modulename, rfQuotationObj.getID());
                list = linkRresult.getEntityList();
                if (list != null && !list.isEmpty()) {
                    obj.put(Constants.IS_LINKED_TRANSACTION, true);
                } else {
                    obj.put(Constants.IS_LINKED_TRANSACTION, false);
                }
                obj.put("billno", rfQuotationObj.getRfqNumber());
                obj.put("rfqno", rfQuotationObj.getRfqNumber());
                String pr =  getLinkedPurchaseRequisitions(rfQuotationObj.getCompany().getCompanyID(),rfQuotationObj.getID());
                obj.put("prno", pr);
                obj.put("moduleid", Constants.Acc_RFQ_ModuleId);
                
                obj.put("isfavourite", rfQuotationObj.isFavourite());
                obj.put("duedate", authHandler.getDateOnlyFormat().format(rfQuotationObj.getDueDate()));
                obj.put("date", authHandler.getDateOnlyFormat().format(rfQuotationObj.getRfqDate()));
                obj.put("archieve", rfQuotationObj.getArchieve());
                obj.put("othervendoremails", rfQuotationObj.getOthervendoremails());
                obj.put("deleted", rfQuotationObj.isDeleted());
                boolean incProTax = false; 
                Iterator itrRow = rfQuotationObj.getRows().iterator();
                double amount = 0,discountPrice = 0;
                Set<String> pr_Ids = new HashSet<String>();
                while (itrRow.hasNext()) {
                    RequestForQuotationDetail sod= (RequestForQuotationDetail) itrRow.next();
                    double  rowTaxPercent=0;
                    /*
                     * adding the Purchase Requisition Id
                     */
                    if (sod.getPrid() != null) {
                        pr_Ids.add(sod.getPrid().getID());
                    }
                    amount += discountPrice + (discountPrice * rowTaxPercent/100);
                }
                obj.put("PR_IDS", pr_Ids);
                obj.put("includeprotax", incProTax);
                obj.put("discounttotal", 0);
                obj.put("discountispertotal", rfQuotationObj.isPerDiscount());
                obj.put("amount", amount);
                double  taxPercent=0;
                obj.put("taxpercent", taxPercent);
                obj.put("approvestatuslevel",rfQuotationObj.getApprovestatuslevel());
                obj.put("memo", rfQuotationObj.getMemo());
                obj.put(Constants.SEQUENCEFORMATID,rfQuotationObj.getSeqformat()==null?"":rfQuotationObj.getSeqformat().getID());
                HashMap<String, String> customFieldMap = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap = new HashMap<String, String>();

                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(rfQuotationObj.getCompany().getCompanyID(), isFixedAsset?Constants.Acc_FixedAssets_RFQ_ModuleId:Constants.Acc_RFQ_ModuleId));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);

                    KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(RFQCustomData.class.getName(), rfQuotationObj.getID());
                    if(custumObjresult.getEntityList().size()>0) {
                        boolean isExport=(requestParams.get("isExport")==null)?false:true;    
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        RFQCustomData rfqCustomData = (RFQCustomData) custumObjresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(rfqCustomData, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put("companyid", companyid);
                        params.put("userdf", userdf);
                        params.put("isExport", isExport);
                        if (rfqlinkflag) {
                            int moduleId = Constants.Acc_Vendor_Quotation_ModuleId;
                            params.put("linkModuleId", moduleId);
                            params.put("isLink", true);
                            params.put("companyid", companyid);
                            params.put("customcolumn", 0);
                        }
                        if (!StringUtil.isNullOrEmpty(paramJObj.optString(Constants.browsertz))) {
                            params.put("browsertz", paramJObj.optString(Constants.browsertz));
                        }
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                    jArr.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getRFQJson : "+ex.getMessage(), ex);
        }
        return jArr;
    } 
    
    public String getLinkedPurchaseRequisitions(String companyid, String rfqId) {
        List<String> list = new ArrayList();
        String prList = "";
        HashMap<String, Object> params = new HashMap<String, Object>();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        KwlReturnObject kwlq = null;
        try {
            requestParams.put("isFromRFQReport", true);
            requestParams.put("includeRFQlinkedPR", true);
            requestParams.put("companyid", companyid);
            requestParams.put("rfqId", rfqId);
            kwlq = accLinkDataDao.getPurchaseRequisition(requestParams, params);
            list = kwlq.getEntityList();
            for (String pr : list) {
                prList += pr + ",";
            }
            if (!StringUtil.isNullOrEmpty(prList)) {
                prList = prList.substring(0, prList.length() - 1);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accPurchaseOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return prList;
    }  
  
}
