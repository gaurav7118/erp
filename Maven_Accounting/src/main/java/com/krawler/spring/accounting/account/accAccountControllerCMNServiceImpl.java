/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.account;

import com.krawler.common.admin.AddressFieldDimensionMapping;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.CustomerAddressDetails;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.common.util.ValuationMethod;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.depreciation.accDepreciationDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import static com.krawler.spring.accounting.tax.TaxConstants.ACCOUNTID;
import static com.krawler.spring.accounting.tax.TaxConstants.ACCOUNTNAME;
import static com.krawler.spring.accounting.tax.TaxConstants.APPLYDATE;
import static com.krawler.spring.accounting.tax.TaxConstants.COMPANYID;
import static com.krawler.spring.accounting.tax.TaxConstants.PERCENT;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXCODE;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXDESCRIPTION;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXID;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXNAME;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXTYPEID;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
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
 * Renamed class accAccountControllerCMNService as
 * accAccountControllerCMNServiceImpl and added
 * deleteAccountsFromChildCompanies,deleteAccount,deleteAssetPurchase,scanChildAccounts,deleteAccountFromChildCompany.
 * accAccountControllerCMNService created as interface.
 *
 * @author krawler
 */
public class accAccountControllerCMNServiceImpl implements MessageSourceAware, accAccountControllerCMNService {

    private accTaxDAO accTaxObj;
    private accCusVenMapDAO accCusVenMapDAOObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private String successView;
    private MessageSource messageSource;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private HibernateTransactionManager txnManager;
    private accProductDAO accProductObj;
    private auditTrailDAO auditTrailObj;
    private accCustomerDAO accCustomerDAOobj;
    private APICallHandlerService apiCallHandlerService;
    private accAccountDAO accAccountDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accPaymentDAO accPaymentDAOobj;
    private accDepreciationDAO accDepreciationDAOobj;

    public void setaccDepreciationDAO(accDepreciationDAO accDepreciationDAOobj) {
        this.accDepreciationDAOobj = accDepreciationDAOobj;
    }

    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    @Override
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setAccCusVenMapDAOObj(accCusVenMapDAO accCusVenMapDAOObj) {
        this.accCusVenMapDAOObj = accCusVenMapDAOObj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     //VP
        this.apiCallHandlerService = apiCallHandlerService;
    }

    @Override
    public JSONArray getTaxJson(HttpServletRequest request) throws SessionExpiredException, ServiceException {

        DateFormat df = authHandler.getDateOnlyFormat(request);
//        DateFormat df = authHandler.getDateFormatter(request);
        Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        String transDate = request.getParameter("transactiondate");
        String taxtypeid = request.getParameter("taxtypeid");
        if (transDate != null) {
            try {
                requestParams.put("transactiondate", df.parse(transDate));
            } catch (ParseException ex) {
                Logger.getLogger(accAccountControllerCMNService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (!StringUtil.isNullOrEmpty(taxtypeid)) {
            requestParams.put("taxtypeid", taxtypeid);
        }
        String module = request.getParameter(Constants.moduleid);
        if (!StringUtil.isNullOrEmpty(module)) {
            int moduleid = Integer.parseInt(module);
            if (moduleid == Constants.Acc_Purchase_Order_ModuleId || moduleid == Constants.Acc_BillingPurchase_Order_ModuleId
                    || moduleid == Constants.Acc_Vendor_Invoice_ModuleId || moduleid == Constants.Acc_Cash_Purchase_ModuleId || moduleid == Constants.Acc_Vendor_BillingInvoice_ModuleId
                    || moduleid == Constants.Acc_Vendor_Quotation_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId
                    || moduleid == Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId
                    || moduleid == Constants.Acc_ConsignmentVendorRequest_ModuleId
                    || moduleid == Constants.Acc_Consignment_GoodsReceipt_ModuleId
                    || moduleid == Constants.Acc_Make_Payment_ModuleId
                    || moduleid == Constants.Acc_FixedAssets_Purchase_Order_ModuleId
                    || moduleid == Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId
                    || moduleid == Constants.Acc_SecurityGateEntry_ModuleId
                    || moduleid == Constants.Acc_Purchase_Requisition_ModuleId) {
                requestParams.put("taxtype", 1);
            } else {
                requestParams.put("taxtype", 2);
            }
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("query"))) {   //SDP-12753
            requestParams.put("ss", request.getParameter("query"));
        }
        /*
         *ERP-40242 :  Show only activated taxes in create and copy case and all taxes in edit cases
         */
        if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.includeDeactivatedTax))) {
            requestParams.put(Constants.includeDeactivatedTax, Boolean.parseBoolean(request.getParameter(Constants.includeDeactivatedTax)));
        }

        KwlReturnObject result = accTaxObj.getTax(requestParams);
        List<Object[]> list = result.getEntityList();

        JSONArray jArr = new JSONArray();
        try {
            if (list != null && !list.isEmpty()) {
                for (Object[] row : list) {
                    if (row[2] == null) {
                        continue;
                    }
                    Tax tax = (Tax) row[0];
                    JSONObject obj = new JSONObject();
                    List l = accCusVenMapDAOObj.getTerms(tax.getID());
                    String termid = "";
                    String termname = "";
                    Iterator itr = l.iterator();
                    while (itr.hasNext()) {
                        InvoiceTermsSales invoiceTermsSales = (InvoiceTermsSales) kwlCommonTablesDAOObj.getClassObject(InvoiceTermsSales.class.getName(), itr.next().toString());
                        //   InvoiceTermsSales invoiceTermsSales=(InvoiceTermsSales)itr.next();
                        if (invoiceTermsSales != null) {
                            termid += invoiceTermsSales.getId() + ",";
                            termname += invoiceTermsSales.getTerm() + ",";;
                        }

                    }
                    if (!StringUtil.isNullOrEmpty(termid)) {
                        termid = termid.substring(0, termid.length() - 1);
                    }
                    if (!StringUtil.isNullOrEmpty(termname)) {
                        termname = termname.substring(0, termname.length() - 1);
                    }
                    obj.put(TAXID, tax.getID());
                    obj.put(TAXNAME, tax.getName());
                    obj.put(TAXDESCRIPTION, tax.getDescription());
                    obj.put(PERCENT, row[1]);
                    obj.put(TAXCODE, tax.getTaxCode());
                    obj.put(ACCOUNTID, tax.getAccount().getID());
                    obj.put(ACCOUNTNAME, tax.getAccount().getName());
                    obj.put(TAXTYPEID, tax.getTaxtype());
                    obj.put(COMPANYID, tax.getCompany().getCompanyID());
                    obj.put("taxTypeName", tax.getTaxtype() == 2 ? "Sales" : "Purchase");
                    obj.put(APPLYDATE, authHandler.getDateOnlyFormat(request).format(row[2]));
                    obj.put("termid", termid);
                    obj.put("termname", termname);
                    obj.put("extrataxtypeid", tax.getExtrataxtype() == 0 ? "ED" : tax.getExtrataxtype() == 1 ? "VAT" : tax.getExtrataxtype() == 2 ? "CST" : "Service Tax");
                    obj.put("activated", tax.isActivated());
                    obj.put(Constants.HAS_ACCESS, tax.isActivated());
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTaxJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    @Override
    public void syncCustomFieldDataFromOtherProjects(HashMap<String, Object> requestParams) throws SessionExpiredException, ServiceException {

        //Session session =null;
        try {
            KwlReturnObject result = null;
            String companyid = (String) requestParams.get("companyid");
            KwlReturnObject extracompanyprefObjresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extracompanyobj = (ExtraCompanyPreferences) extracompanyprefObjresult.getEntityList().get(0);

            //Fetched data from Deskera LMS
//            String action = "32";
//            String lmsURL = (String)requestParams.get("lmsURL");
            String lmsURL = URLUtil.buildRestURL("lmsURL");
            lmsURL = lmsURL + "academic/dimention";
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("companyid", companyid);
            userData.put("mapWithFieldType", (String) requestParams.get("mapWithFieldType"));

            //session = HibernateUtil.getCurrentSession();
            JSONObject jobj = apiCallHandlerService.restGetMethod(lmsURL, userData.toString());
//            JSONObject jobj = apiCallHandlerService.callApp(lmsURL, userData, companyid, action);

            JSONArray jArray = jobj.getJSONArray("data");

            //Fetched Project field params     
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
            requestParams1.put("filter_names", Arrays.asList("companyid", "mapwithtype"));
            requestParams1.put("filter_values", Arrays.asList(companyid, (Integer.parseInt(requestParams.get("mapWithFieldType").toString()))));

            result = accMasterItemsDAOobj.getFieldParams(requestParams1);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            //Loop on fields params having isProject true
            while (itr.hasNext()) {
                FieldParams fieldParams = (FieldParams) itr.next();
                //Loop to set combo values 
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jobjData = jArray.getJSONObject(i);

                    String mapwithfieldid = (String) jobjData.get("id");
                    String lmsfieldname = (String) jobjData.get("name");
                    if (StringUtil.isNullOrEmpty(lmsfieldname)) {
                        continue;
                    }
                    //Check for duplicate. If not present then insert.
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();

                    filter_names.add("id");
                    filter_names.add("field.id");

                    filter_params.add(mapwithfieldid + "-" + fieldParams.getModuleid());
                    filter_params.add(fieldParams.getId());
                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);

                    List listItems = cntResult.getEntityList();
                    Iterator itrItems = listItems.iterator();
                    HashMap requestParam = new HashMap();

                    requestParam.put("name", lmsfieldname);
                    requestParam.put("groupid", fieldParams.getId());
                    requestParam.put("mapwithfieldid", mapwithfieldid + "-" + fieldParams.getModuleid());
                    if (itrItems.hasNext()) {
                        FieldComboData item = (FieldComboData) itrItems.next();
                        requestParam.put("id", item.getId());
                    }
                    result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, false);
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
//        finally {
//            HibernateUtil.closeSession(session);
//        }
    }

    @Override
    public void getLMSCourcesAsProducts(HashMap<String, Object> requestParams) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONObject resObj = new JSONObject();
        //Session session =null;
        String msg = "";
        boolean issuccess = false;
        boolean isShelfLocationSaved = false;//This flag is used for save shelf locations only one time.
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {

            boolean iterate = false;
            int o = 0;
            int l = 100;
            do {
                String companyid = (String) requestParams.get("companyid");
                String currencyid = (String) requestParams.get("gcurrencyid");
//            String lmsURL = (String)requestParams.get("lmsURL");
                String userfullName = (String) requestParams.get("userfullName");
                JSONObject userData = new JSONObject();
                userData.put("iscommit", true);
                userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
                userData.put("userid", (String) requestParams.get("userid"));
                userData.put("companyid", companyid);
                userData.put("start", o);
                userData.put("limit", l);
                //session = HibernateUtil.getCurrentSession();
//            String action = "29";
                String lmsURL = URLUtil.buildRestURL("lmsURL");
                lmsURL = lmsURL + "academic/modules";
                //userData.put("data", pjobj);                                    
                int totalRows = 0;
                resObj = apiCallHandlerService.restGetMethod(lmsURL, userData.toString());
//                resObj = apiCallHandlerService.callApp(lmsURL, userData, companyid, action);

//            finally{
//                session.close();
//            }    
                if (!resObj.isNull("success") && resObj.getBoolean("success")) {

                    JSONArray productsArr = resObj.getJSONArray("data");
                    if (productsArr.length() > 0) {
                        totalRows = productsArr.length();
                    }
                    if (totalRows >= l) {
                        o += l;
                        iterate = true;
                    } else {
                        iterate = false;
                    }
                    if (productsArr.length() > 0) {

                        //Create transaction
                        try {

                            HashMap<String, Object> defaultAccountMap = new HashMap<String, Object>();
                            defaultAccountMap.put("companyid", companyid);
                            defaultAccountMap.put("productType", "Service");
                            DefaultsForProduct defaultsForProduct = accProductObj.getDefaultAccountsForProduct(defaultAccountMap);
                            String revenueaccount = "";
                            String liabilityAccount = "";
                            Account liabilityAcc = null;
                            Account revenueacc = null;
                            KwlReturnObject cap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                            ExtraCompanyPreferences preferences = (ExtraCompanyPreferences) cap.getEntityList().get(0);
                            if (preferences.isRecurringDeferredRevenueRecognition()) {
                                KwlReturnObject cap1 = accountingHandlerDAOobj.getObject(Account.class.getName(), preferences.getSalesAccount());
                                Account salesaccount = (Account) cap1.getEntityList().get(0);
                                defaultsForProduct.setSaccount(salesaccount);
                                KwlReturnObject cap2 = accountingHandlerDAOobj.getObject(Account.class.getName(), preferences.getSalesRevenueRecognitionAccount());
                                revenueacc = (Account) cap2.getEntityList().get(0);
                                revenueaccount = revenueacc.getID();
                                KwlReturnObject cap3 = accountingHandlerDAOobj.getObject(Account.class.getName(), preferences.getLiabilityAccountForLMS());
                                liabilityAcc = (Account) cap3.getEntityList().get(0);
                                liabilityAccount = liabilityAcc.getID();
                            } else {
                                throw new AccountingException("Please set Advance sales and Revenue Recognition Account for LMS");
                            }
                            for (int i = 0; i < productsArr.length(); i++) {
                                JSONObject productObject = productsArr.getJSONObject(i);

                                KwlReturnObject productresult;
                                String auditMsg = "added";
                                String auditID = AuditAction.PRODUCT_CREATION;

                                String productCode = productObject.optString("productCode", "");
                                String productName = productObject.optString("productName", "");
                                String description = productObject.optString("description", "");
                                String DefaultLocation = (productObject.isNull("orderLocationId")) ? "" : productObject.optString("orderLocationId", "");
                                String DefaultWarehouse = (productObject.isNull("orderStoreId")) ? "" : productObject.optString("orderStoreId", "");
                                boolean isQAenable = (!productObject.isNull("isQAenable") && productObject.optBoolean("isQAenable", false) == true) ? true : false;
                                if (productCode.equalsIgnoreCase("3") && liabilityAcc != null) {
                                    defaultsForProduct.setSaccount(liabilityAcc);
                                } else if (productCode.equalsIgnoreCase("2") && revenueacc != null) {
                                    defaultsForProduct.setSaccount(revenueacc);
                                }
                                Date appDate = new Date();
                                HashMap<String, Object> productMap = new HashMap<String, Object>();
                                productMap.put("parentid", null);
                                if (defaultsForProduct.getPaccount() == null || defaultsForProduct.getSaccount() == null) {
                                    throw new AccountingException(" Purchases and Sales account not present.");
                                }
                                productMap.put("purchaseaccountid", defaultsForProduct.getPaccount().getID());
                                productMap.put("salesaccountid", defaultsForProduct.getSaccount().getID());
                                productMap.put("purchaseretaccountid", defaultsForProduct.getPaccount().getID());
                                productMap.put("salesretaccountid", defaultsForProduct.getSaccount().getID());
                                if (!StringUtil.isNullOrEmpty(revenueaccount)) {
                                    productMap.put("salesRevenueRecognitionAccountid", revenueaccount);
                                }
                                productMap.put("leadtime", 0);
                                productMap.put("currencyid", currencyid);
                                productMap.put("warrantyperiod", 0);
                                productMap.put("warrantyperiodsal", 0);
                                productMap.put("reorderlevel", 0.0);
                                productMap.put("reorderquantity", 0.0);
                                productMap.put("companyid", companyid);
                                productMap.put("syncable", false);
//                        productMap.put("multiuom", true);
                                productMap.put("name", productName);
                                productMap.put("location", DefaultLocation);
                                productMap.put("warehouse", DefaultWarehouse);
                                ValuationMethod valMethod = ValuationMethod.FIFO;
                                if ((productMap.get("valuationmethod")) != null) {
                                    int valuationMethod = Integer.parseInt(productMap.get("valuationmethod").toString());
                                    for (ValuationMethod st : ValuationMethod.values()) {
                                        if (st.ordinal() == valuationMethod) {
                                            valMethod = st;
                                            break;
                                        }
                                    }
                                }
                                productMap.put("valuationmethod", valMethod);

                                String productid = "";
                                KwlReturnObject proObject = accProductObj.getProduct(productCode, companyid);
                                if (!proObject.getEntityList().isEmpty()) {
                                    Product productObj = (Product) proObject.getEntityList().get(0);
                                    productid = productObj.getID();
                                }

                                Product product = null;
                                if (StringUtil.isNullOrEmpty(productid)) {
                                    productMap.put("name", productName);
                                    productMap.put("desc", description);

                                    productMap.put("producttype", defaultsForProduct.getProducttype().getID());
                                    productMap.put("productid", productCode);

                                    productresult = accProductObj.addProduct(productMap);
                                    product = (Product) productresult.getEntityList().get(0);
                                    HashMap<String, Object> purchasePriceMap = new HashMap<String, Object>();
                                    purchasePriceMap.put("price", productObject.optDouble("productPrice", 0.0));
                                    purchasePriceMap.put("productid", product.getID());
                                    purchasePriceMap.put("companyid", companyid);
                                    purchasePriceMap.put("carryin", true);
                                    purchasePriceMap.put("applydate", appDate);
                                    purchasePriceMap.put("currencyid", currencyid);
                                    purchasePriceMap.put("uomid", product.getUnitOfMeasure().getID());
                                    accProductObj.addPriceList(purchasePriceMap);

                                    HashMap<String, Object> sellingPriceMap = new HashMap<String, Object>();
                                    sellingPriceMap.put("price", productObject.optDouble("sellingPrice", 0.0));
                                    sellingPriceMap.put("productid", product.getID());
                                    sellingPriceMap.put("companyid", companyid);
                                    sellingPriceMap.put("carryin", false);
                                    sellingPriceMap.put("applydate", appDate);
                                    purchasePriceMap.put("currencyid", currencyid);
                                    sellingPriceMap.put("uomid", product.getUnitOfMeasure().getID());
                                    accProductObj.addPriceList(sellingPriceMap);

                                } else {

                                    auditMsg = "updated";
                                    auditID = AuditAction.PRODUCT_UPDATION;
                                    productMap.put("id", productid);
                                    productresult = accProductObj.updateProduct(productMap);
                                    product = (Product) productresult.getEntityList().get(0);

                                    HashMap<String, Object> purchasePriceMap = new HashMap<String, Object>();
                                    purchasePriceMap.put("price", productObject.optDouble("productPrice", 0.0));
                                    purchasePriceMap.put("productid", product.getID());
                                    purchasePriceMap.put("companyid", companyid);
                                    purchasePriceMap.put("carryin", true);
                                    purchasePriceMap.put("applydate", appDate);
                                    purchasePriceMap.put("currencyid", currencyid);
                                    purchasePriceMap.put("uomid", product.getUnitOfMeasure().getID());
                                    accProductObj.addPriceList(purchasePriceMap);

                                    HashMap<String, Object> sellingPriceMap = new HashMap<String, Object>();
                                    sellingPriceMap.put("price", productObject.optDouble("sellingPrice", 0.0));
                                    sellingPriceMap.put("productid", product.getID());
                                    sellingPriceMap.put("companyid", companyid);
                                    sellingPriceMap.put("carryin", false);
                                    sellingPriceMap.put("applydate", appDate);
                                    sellingPriceMap.put("currencyid", currencyid);
                                    sellingPriceMap.put("uomid", product.getUnitOfMeasure().getID());
                                    accProductObj.addPriceList(sellingPriceMap);

                                }

                            }
                        } catch (NumberFormatException ex) {
                            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                            txnManager.rollback(status);
                        } catch (JSONException ex) {
                            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                            txnManager.rollback(status);
                        } catch (Exception ex) {
                            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                            txnManager.rollback(status);
                            msg = "" + ex.getMessage();
                            throw ServiceException.FAILURE(ex.getMessage(), ex);
                        }
                        issuccess = resObj.getBoolean("success");
                        jobj.put("success", true);
                        jobj.put("msg", msg);

                    }
                }
            } while (iterate);
            txnManager.commit(status);

        } catch (Exception ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            txnManager.rollback(status);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    @Override
    public void getCustomerFromLMS(HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONObject resObj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        TransactionStatus status = txnManager.getTransaction(def);
        //Session session=null;
        try {
            String companyid = (String) requestParams.get("companyid");
            String currencyid1 = (String) requestParams.get("gcurrencyid");
//            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            //HttpServletRequest request=(HttpServletRequest)requestParams.get("requestObj");
//             boolean autogen=false;
//             int nextAutoNoInt=0;

//            String auditMsg = "added";
//            String auditID = AuditAction.CUSTOMER_ADDED;
//            String lmsURL = (String)requestParams.get("lmsURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", (String) requestParams.get("userid"));
            userData.put("companyid", companyid);
            //session = HibernateUtil.getCurrentSession();
//            String action = "30";
            String lmsURL = URLUtil.buildRestURL("lmsURL");
            lmsURL = lmsURL + "company/students";
            resObj = apiCallHandlerService.restGetMethod(lmsURL, userData.toString());
//                resObj = apiCallHandlerService.callApp(lmsURL, userData, companyid, action);

            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("companyid", companyid);
                params.put("gcurrencyid", currencyid1);
                saveCustomerDataFromLMS(params, resObj);
            }
            issuccess = true;

            txnManager.commit(status);
        } catch (NumberFormatException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);

            txnManager.rollback(status);

        } catch (Exception ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);

            txnManager.rollback(status);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }

    @Override
    public JSONArray saveCustomerDataFromLMS(HashMap<String, Object> params, JSONObject resObj) throws ServiceException {
        JSONObject job;
        JSONArray jArr = new JSONArray();
        boolean autogen = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String companyid = (String) params.get("companyid");
            String currencyid1 = (String) params.get("gcurrencyid");
            JSONArray customerArr = resObj.getJSONArray("data");
            if (customerArr.length() > 0) {
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

                for (int i = 0; i < customerArr.length(); i++) {
                    JSONObject custObj = customerArr.getJSONObject(i);

                    Customer customer = null;
                    HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
                    boolean isCustomerPreExist = false;
                    String custCode = custObj.isNull("studentID") ? "" : custObj.getString("studentID");
                    String firstName = custObj.isNull("fname") ? "" : custObj.getString("fname");
                    String lastName = custObj.isNull("lname") ? "" : custObj.getString("lname");
                    String currencyid = custObj.isNull("currency") ? currencyid1 : custObj.getString("currency");
                    Date creationDate = custObj.isNull("creationDate") ? new Date() : sdf.parse(custObj.getString("creationDate"));
                    if (creationDate == null) {
                        creationDate = new Date();
                    }

                    String customerName = "";
                    if (!StringUtil.isNullOrEmpty(firstName) && !StringUtil.isNullOrEmpty(lastName)) {
                        customerName = firstName + " " + lastName;
                    } else if (!StringUtil.isNullOrEmpty(firstName) && StringUtil.isNullOrEmpty(lastName)) {
                        customerName = firstName;
                    }

                    KwlReturnObject resultcheck = accCustomerDAOobj.checkCustomerExistbyCode(custCode, companyid);
                    if (!resultcheck.getEntityList().isEmpty()) {
                        customer = (Customer) resultcheck.getEntityList().get(0);
                    }

                    if (customer != null) {
                        requestParams1.put("accname", customerName);
                        HashMap<String, Object> params1 = new HashMap<String, Object>();
                        params.put("accid", customer.getID());
                        KwlReturnObject result = accCustomerDAOobj.updateCustomer(params1);
                        isCustomerPreExist = true;
                    } else {

                        if (preferences != null) {
                            requestParams1.put("accountid", preferences.getCustomerdefaultaccount().getID());
                        }
                        requestParams1.put("acccode", custCode);
                        requestParams1.put("currencyid", currencyid);
                        requestParams1.put("autogenerated", autogen);
                        requestParams1.put("accname", customerName);
                        requestParams1.put("openbalance", 0);
                        requestParams1.put("companyid", companyid);
                        requestParams1.put("overseas", false);
                        requestParams1.put("mapcustomervendor", false);
                        requestParams1.put("creationDate", creationDate);

                        resultcheck = accCustomerDAOobj.getDefaultCreditTermForCustomer(companyid);
                        Term term = null;
                        if (!resultcheck.getEntityList().isEmpty()) {
                            term = (Term) resultcheck.getEntityList().get(0);
                            if (term != null) {
                                requestParams1.put("termid", term.getID());
                            }
                        }
                        String title = custObj.optString("title");
                        if (!StringUtil.isNullOrEmpty(title)) {
                            KwlReturnObject kro = accCustomerDAOobj.getTitleForCustomer(companyid, title);
                            MasterItem masterItem = null;
                            if (!kro.getEntityList().isEmpty()) {
                                masterItem = (MasterItem) kro.getEntityList().get(0);
                                if (masterItem != null) {
                                    requestParams1.put("title", masterItem.getID());
                                }
                            }
                        }
                        KwlReturnObject result = accCustomerDAOobj.addCustomer(requestParams1);
                        customer = (Customer) result.getEntityList().get(0);
                    }
                    if (customer != null) {
                        // For Existing Customer If Default Address Present then updating it  
                        // For New Customer inserting a new default address if address coming from LMS 

                        if (custObj.has("addressData") && custObj.optJSONObject("addressData") != null) {
                            JSONObject addrDataObject = custObj.optJSONObject("addressData");
                            HashMap<String, Object> custAddrMap = new HashMap<String, Object>();
                            custAddrMap.put("customerid", customer.getID());
                            custAddrMap.put("address", addrDataObject.optString("address", ""));
                            custAddrMap.put("city", addrDataObject.optString("city", ""));
                            custAddrMap.put("state", addrDataObject.optString("state", ""));
                            custAddrMap.put("postalCode", addrDataObject.optString("postalCode", ""));
                            custAddrMap.put("phone", addrDataObject.optString("phone", ""));
                            custAddrMap.put("mobileNumber", addrDataObject.optString("mobileNumber", ""));
                            custAddrMap.put("emailID", addrDataObject.optString("emailID", ""));

                            //Saving billing Address
                            boolean isBillingDefault = true;
                            String billingAliasName = "Billing Address1"; //Since No alias name coming from LMS so giving default alias name
                            String biilingAddressID = "";
                            if (isCustomerPreExist) {
                                List addrDetail = getCustomerAddressInfoByAliasName(billingAliasName, customer.getID(), companyid, true);
                                biilingAddressID = (String) addrDetail.get(0);
                                isBillingDefault = (Boolean) addrDetail.get(1);
                            }
                            custAddrMap.put("aliasName", billingAliasName);
                            custAddrMap.put("addressid", biilingAddressID);
                            custAddrMap.put("isBillingAddress", true);
                            custAddrMap.put("isDefaultAddress", isBillingDefault);
                            accountingHandlerDAOobj.saveCustomerAddressesDetails(custAddrMap, companyid);

                            //Saving shipping Address
                            boolean isShippingDefault = true;
                            String shippingAliasName = "Shipping Address1";//Since No alias name coming from LMS so giving default alias name
                            String shippingAddressID = "";
                            if (isCustomerPreExist) {
                                List addrDetail = getCustomerAddressInfoByAliasName(shippingAliasName, customer.getID(), companyid, false);
                                shippingAddressID = (String) addrDetail.get(0);
                                isShippingDefault = (Boolean) addrDetail.get(1);
                            }
                            custAddrMap.put("aliasName", shippingAliasName);
                            custAddrMap.put("addressid", shippingAddressID);
                            custAddrMap.put("isBillingAddress", false);
                            custAddrMap.put("isDefaultAddress", isShippingDefault);
                            accountingHandlerDAOobj.saveCustomerAddressesDetails(custAddrMap, companyid);
                        }

                    }
                    job = new JSONObject();
                    job.put("erpCustomerid", customer.getID());
                    job.put("accountid", customer.getAcccode());
                    jArr.put(job);
                }

            }
        } catch (JSONException | ServiceException | ParseException ex) {
            Logger.getLogger(accAccountControllerCMNService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }

    public List getCustomerAddressInfoByAliasName(String AliasName, String customerid, String companyid, boolean isbillingAddress) throws ServiceException {
        List addressDetails = new ArrayList();
        //Logic for Pre Exist Customer
        //here we find address of same alias name. If it is present then we return it ID and default type. 
        HashMap<String, Object> addressParams = new HashMap<String, Object>();
        addressParams.put("companyid", companyid);
        addressParams.put("customerid", customerid);
        addressParams.put("isBillingAddress", isbillingAddress); //for billing address this falg will be true
        KwlReturnObject returnObject = accountingHandlerDAOobj.getCustomerAddressDetails(addressParams);
        List list = returnObject.getEntityList();
        boolean isDefault = true;
        String addressID = "";
        if (!list.isEmpty()) {
            // Here if this condition is true it mens that Existing customer have 1 or more Billing address. so making default value false
            isDefault = false;
            Iterator addrItr = list.iterator();
            while (addrItr.hasNext()) {
                CustomerAddressDetails details = (CustomerAddressDetails) addrItr.next();
                if (details != null && details.getAliasName() != null && details.getAliasName().equalsIgnoreCase(AliasName)) {
                    addressID = details.getID();
                    isDefault = details.isIsDefaultAddress();
                    break;
                }
            }
        }
        addressDetails.add(addressID);
        addressDetails.add(isDefault);
        return addressDetails;
    }

    /**
     * This method saves and updates Address fields mapped against Dimensions
     * which are created "isForGST"
     */
    @Override
    public void saveOrUpdateAddressFieldForGSTDimension(HashMap<String, Object> paramMap) throws ServiceException, JSONException {
        if (paramMap.containsKey("isEdit") && (Boolean) paramMap.get("isEdit")) {
            KwlReturnObject result = accAccountDAOobj.getDimensionMappedAddressFieldID(paramMap);
            if (result.getEntityList().size() > 0) {
                AddressFieldDimensionMapping addressFieldDimensionMapping = (AddressFieldDimensionMapping) result.getEntityList().get(0);
                paramMap.put("id", addressFieldDimensionMapping.getId());
            }
        }
        /*
         *  If case is of not Edit then check for entry with same address mapping in AddressFieldDimensionMapping, if present then overrite that entry with new one.    
         */
        if (paramMap.containsKey("isEdit") && !((Boolean) paramMap.get("isEdit"))) {
            KwlReturnObject result = accAccountDAOobj.getDimensionMappedWithSameAddressField(paramMap);
            if (result.getEntityList().size() > 0) {
                AddressFieldDimensionMapping addressFieldDimensionMapping = (AddressFieldDimensionMapping) result.getEntityList().get(0);
                accMasterItemsDAOobj.deleteAddressFieldAgainstDimension(addressFieldDimensionMapping.getId());
            }
        }
        accAccountDAOobj.saveOrUpdateAddressFieldForGSTDimension(paramMap);
    }

    /**
     * Code is moved from accAccountControllerCMN.
     * @param paramJobj
     * @return
     * @throws ServiceException
     * @throws AccountingException 
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {JSONException.class, AccountingException.class, ServiceException.class})
    public JSONObject deleteAccount(JSONObject requestJobj) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        boolean propagateToChildCompaniesFlag = false;
        try {
            String companyid = requestJobj.optString(Constants.companyKey);
            boolean isFixedAsset = requestJobj.optBoolean("isFixedAsset", false);
            if (!isFixedAsset) {
                deleteAccount(requestJobj, companyid);
            } else {
                deleteAssetPurchase(requestJobj, companyid);
            }
            msg = (isFixedAsset ? messageSource.getMessage("acc.acc.delasset", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))) : messageSource.getMessage("acc.acc.del", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
            issuccess = true;
            returnJobj.put(Constants.RES_success, issuccess);
            returnJobj.put(Constants.RES_msg, msg);
            propagateToChildCompaniesFlag = requestJobj.optBoolean("ispropagatetochildcompanyflag", false);
            if (propagateToChildCompaniesFlag) {
                deleteAccountsFromChildCompanies(requestJobj, propagateToChildCompaniesFlag);
            }
        } catch (JSONException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountControllerCMNServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accAccountControllerCMNServiceImpl.deleteAccount : " + ex.getMessage(), ex);
        } catch (AccountingException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountControllerCMNServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (ServiceException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountControllerCMNServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountControllerCMNServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accAccountControllerCMNServiceImpl.deleteAccount : " + ex.getMessage(), ex);
        } finally {
            try {
                returnJobj.put(Constants.RES_success, issuccess);
                returnJobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMNServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE("accAccountControllerCMNServiceImpl.deleteAccount : " + ex.getMessage(), ex);
            }
        }
        return returnJobj;

    }

    public void deleteAccountsFromChildCompanies(JSONObject requestJobj, boolean propagateToChildCompaniesFlag) throws JSONException, ServiceException {
        try {
            String auditID = AuditAction.ACCOUNT_DELETED;
            JSONArray jArr = new JSONArray(requestJobj.optString("data", null));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject accountJobj = jArr.getJSONObject(i);
                String parentcompanyAccountid = accountJobj.getString("accid");
                String childCompanyName = "";
                HashMap<String, Object> propagaedRequestParams = new HashMap<String, Object>();
                propagaedRequestParams.put("propagatedAccountID", parentcompanyAccountid);
                KwlReturnObject result = accAccountDAOobj.getPropagatedAccounts(propagaedRequestParams);
                List childCompaniesCustomerList = result.getEntityList();

                JSONObject deleteObj = null;
                for (Object childObj : childCompaniesCustomerList) {
                    String childcompanysAccountId = "";
                    String childAccountName = "";
                    String childCompanyID = "";
                    try {
                        Account acc = (Account) childObj;
                        if (acc != null) {
                            childAccountName = acc.getName();
                            childCompanyName = acc.getCompany().getSubDomain();
                            childcompanysAccountId = acc.getID();
                            childCompanyID = acc.getCompany().getCompanyID();
                            JSONArray propagatedAccoutJarr = new JSONArray();
                            deleteObj.put("accid", acc.getID());
                            deleteObj.put("openbalance", acc.getOpeningBalance());
                            propagatedAccoutJarr.put(deleteObj);
                            requestJobj.put("propagateToChildCompaniesFlag", propagateToChildCompaniesFlag);
                            requestJobj.put("propagatedAccountIdsJarr", propagatedAccoutJarr);
                            deleteAccount(requestJobj, childCompanyID);

                            Map<String, Object> auditParamsMap = new HashMap();
                            auditParamsMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
                            auditParamsMap.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
                            auditParamsMap.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
                            auditParamsMap.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
                            auditTrailObj.insertAuditLog(auditID, "User " + requestJobj.optString(Constants.userfullname) + " has deleted account " + childAccountName + " from child company " + childCompanyName, auditParamsMap, childcompanysAccountId);
                        }
                    } catch (Exception ex) {
                        Map<String, Object> auditParamsMap = new HashMap();
                        auditParamsMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
                        auditParamsMap.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
                        auditParamsMap.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
                        auditParamsMap.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
                        auditTrailObj.insertAuditLog(auditID, "Account " + childAccountName + " could not be deleted  from child company " + childCompanyName, auditParamsMap, childcompanysAccountId);
                        throw ServiceException.FAILURE("accAccountControllerCMNServiceImpl.deleteAccountsFromChildCompanies : " + ex.getMessage(), ex);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accAccountControllerCMNServiceImpl.deleteAccountsFromChildCompanies : " + ex.getMessage(), ex);
        }
    }

    public void deleteAccount(JSONObject requestJobj, String companyid) throws AccountingException, ServiceException {
        KwlReturnObject result = null;
        try {
            JSONArray jArr = null;
            boolean propagateToChildCompaniesFlag = false;//when this flag is true then at the time of deleting propagated record do not check opening balance of account from jobj .record is not deleted when there is opening balance for that account.
            if (requestJobj.optBoolean("propagateToChildCompaniesFlag", false)) {
                jArr = requestJobj.getJSONArray("propagatedAccountIdsJarr");
                propagateToChildCompaniesFlag = true;
            } else {
                jArr = new JSONArray(requestJobj.optString("data", null));
            }
            boolean isFixedAsset = requestJobj.optBoolean("isFixedAsset", false);
            boolean coaaccdel = requestJobj.optBoolean("coaaccdel", false);
            String accountid = "";
            int count = 0;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                accountid = StringUtil.DecodeText(jobj.optString("accid"));
                if (!StringUtil.isNullOrEmpty(jobj.getString("accid"))) {
                    try {
                        // Check is Account previouly or currently used in control accounts.
                        if (!StringUtil.isNullOrEmpty(accountid)) {
                            KwlReturnObject object = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
                            Account account = object.getEntityList().size() > 0 ? (Account) object.getEntityList().get(0) : null;
                            if (account != null && account.isControlAccounts()) {
                                throw new AccountingException(messageSource.getMessage("acc.field.AccountUsedInControlAccounts", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                            }
                        }
                        //Delete Account and Subaccounts          Neeraj
                        List<String> ChildsArray = new ArrayList<String>();
                        ChildsArray = scanChildAccounts(accountid);
                        Iterator<String> ChildsArrayiterator = ChildsArray.iterator();
                        while (ChildsArrayiterator.hasNext()) {
                            Object obj = ChildsArrayiterator.next();
                            Logger.getLogger(accAccountController.class.getName()).info("\n" + obj.toString());

                            accountid = obj.toString();
                            if (!isFixedAsset) { //Dont check Opening Balance for Fix Asset
                                if (jobj.getDouble("openbalance") != 0) {
                                    throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsishavingtheOpeningBalanceSoitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                                }
                            }
                            // Check in Journal Entry
                            result = accJournalEntryobj.getJEDfromAccount(accountid, companyid);
                            count = result.getRecordTotalCount();
                            if (count > 0) {
                                throw new AccountingException(messageSource.getMessage("acc.term.excp1", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                            }

                            // Check Product Entry
                            result = accProductObj.getProductfromAccount(accountid, companyid);
                            count = result.getRecordTotalCount();
                            if (count > 0) {
                                throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsiscurrentlyusedintheProducts.Soitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                            }

                            // Check Account used is Asset Group
                            result = accProductObj.getAssetGroupfromAccount(accountid, companyid);
                            count = result.getRecordTotalCount();
                            if (count > 0) {
                                throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsiscurrentlyusedintheAssetGroup.Soitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                            }
                            // Check InvoiceTerm Entry
                            result = accProductObj.getInvoiceTermfromAccount(accountid, companyid);
                            count = result.getRecordTotalCount();
                            if (count > 0) {
                                throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsiscurrentlyusedinTerms.Soitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                            }
                            // Check InvoiceTerm Entry
                            result = accAccountDAOobj.getAccountUsedInExpenesePo(accountid, companyid);
                            count = result.getRecordTotalCount();
                            if (count > 0) {
                                throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsiscurrentlyusedinExpensePo.Soitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                            }

                            // Check for Preferances Entry
                            result = accCompanyPreferencesObj.getPreferencesFromAccount(accountid, companyid);
                            count = result.getRecordTotalCount();
                            if (count > 0) {
                                throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsiscurrentlyusedintheAccountPreferences.Soitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                            }

                            result = accCompanyPreferencesObj.getExtraPreferencesFromAccount(accountid, companyid);
                            count = result.getRecordTotalCount();
                            if (count > 0) {
                                throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsiscurrentlyusedintheAccountPreferences.Soitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                            }

                            result = accCompanyPreferencesObj.getAccountUsedForFreeGift(accountid, companyid);
                            count = result.getRecordTotalCount();
                            if (count > 0) {
                                throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsiscurrentlyusedintheAccountPreferences.Soitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                            }

                            // Check fot Payment Entry
                            result = accPaymentDAOobj.getPaymentMethodFromAccount(accountid, companyid);
                            count = result.getRecordTotalCount();
                            if (count > 0) {
                                throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsiscurrentlyusedinthePaymentMethodSoitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                            }
                            result = accPaymentDAOobj.getChequeSequenceFormatFromAccount(accountid, companyid);
                            count = result.getRecordTotalCount();
                            if (count > 0) {
                                throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsiscurrentlyusedintheChequeSequenceFormatSoitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                            }

                            // Check for Tax Entry
                            result = accTaxObj.getTaxFromAccount(accountid, companyid);
                            count = result.getRecordTotalCount();
                            if (count > 0) {
                                throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsiscurrentlyusedintheTaxsSoitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                            }
                            result = accAccountDAOobj.getChildAccountfromAccount(accountid, companyid); //parent account
                            count = result.getRecordTotalCount();
                            if (count > 0) {
                                throw new AccountingException(messageSource.getMessage("acc.field.Selectedaccountiscurrentlyused", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                            }
                            // Check is Account previouly or currently used in control accounts.
                            if (!StringUtil.isNullOrEmpty(accountid)) {
                                KwlReturnObject object = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
                                Account account = object.getEntityList().size() > 0 ? (Account) object.getEntityList().get(0) : null;
                                if (account != null && account.isControlAccounts()) {
                                    throw new AccountingException(messageSource.getMessage("acc.field.AccountUsedInControlAccounts", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                                }
                            }
                        }
                        // Check for Custom Layout Entry
                        result = accAccountDAOobj.getCustomLayoutfromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsiscurrentlyusedintheCustomLayoutSoitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                        }

                        // Check for Custom Layout Entry
                        result = accAccountDAOobj.getMasterItemfromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsiscurrentlyusedintheMasterItemSoitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                        }

                        //Check Whether Account used in Customer
                        HashMap<String, Object> reqParams = new HashMap<String, Object>();
                        reqParams.put("companyid", companyid);
                        reqParams.put("accontid", accountid);
                        reqParams.put("accountid", accountid);
                        reqParams.put("isAccActivateDeactivate", true); 
                        result = accAccountDAOobj.getCustomerForCombo(reqParams);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsiscurrentlyusedintheCustomerMasterSoitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                        }

                        /*
                        *ERP-41788
                         *Check Whether Account used in Vendor
                        */
                        result = accAccountDAOobj.getVendorForCombo(reqParams);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsiscurrentlyusedintheVendorMasterSoitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                        }
                        
                        // Check for Line Level Terms
                        result = accAccountDAOobj.getLineLevelTermsfromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsiscurrentlyusedintheLineLeveTermsSoitcannotbedeleted", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                        }

                        //   Now when the parent and children arent used in any of the transactions above we can now delete the whole hierrarchy
                        Iterator<String> ChildsDeleteiterator = ChildsArray.iterator();
                        while (ChildsDeleteiterator.hasNext()) {
                            Object obj = ChildsDeleteiterator.next();
                            if (coaaccdel) {
                                accAccountDAOobj.deleteIBGBankDetail(obj.toString(), companyid);
                                accAccountDAOobj.deleteAccount(obj.toString(), companyid);
                                if (!propagateToChildCompaniesFlag) {
                                    Map<String, Object> auditParamsMap = new HashMap();
                                    auditParamsMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
                                    auditParamsMap.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
                                    auditParamsMap.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
                                    auditParamsMap.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
                                    auditTrailObj.insertAuditLog(AuditAction.ACCOUNT_DELETED, "User " + requestJobj.optString(Constants.userfullname) + " has deleted account Permanently " + jobj.getString("acccode"), auditParamsMap, obj.toString());
                                }
                            } else {
                                accAccountDAOobj.deleteAccount(obj.toString(), true);
                                accAccountDAOobj.deleteIBGBankDetail(obj.toString(), companyid);
                                if (!propagateToChildCompaniesFlag) {
                                    Map<String, Object> auditParamsMap = new HashMap();
                                    auditParamsMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
                                    auditParamsMap.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
                                    auditParamsMap.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
                                    auditParamsMap.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
                                    auditTrailObj.insertAuditLog(AuditAction.ACCOUNT_DELETED, "User " + requestJobj.optString(Constants.userfullname) + " has deleted account " + jobj.getString("acccode"), auditParamsMap, obj.toString());
                                }
                            }
                        }

                    } catch (ServiceException ex) {
                        throw new AccountingException(messageSource.getMessage("acc.term.excp1", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                    }
                }
            }
        } catch (AccountingException ex) {
            throw ex;
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("accAccountControllerCMNServiceImpl.deleteAccount : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accAccountControllerCMNServiceImpl.deleteAccount : " + ex.getMessage(), ex);
        }
    }

    public void deleteAssetPurchase(JSONObject requestJobj, String companyid) throws AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(requestJobj.optString("data", null));
            JSONObject jobj = jArr.getJSONObject(0);
            if (!StringUtil.isNullOrEmpty(jobj.getString("accid"))) {
                String assetid = StringUtil.DecodeText(jobj.optString("accid"));
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("accountid", assetid);
                requestParams.put("companyid", companyid);
                KwlReturnObject result = accDepreciationDAOobj.getDepreciation(requestParams);
                if (result != null && result.getEntityList().size() != 0) {
                    throw new AccountingException(messageSource.getMessage("acc.rem.156", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
                } else {
                    requestParams.clear();
                    requestParams.put("id", assetid);
                    result = accDepreciationDAOobj.getAsset(requestParams);
                    if (result != null && result.getEntityList().size() > 0) {
                        Asset asset = (Asset) result.getEntityList().get(0);
                        if (asset.getPurchaseJe() != null) {
                            String purchaseJe = asset.getPurchaseJe().getID();
                            accJournalEntryobj.deleteJEEntry(purchaseJe, companyid);
                            //Delete entry from optimized table
                            accJournalEntryobj.deleteAccountJEs_optimized(purchaseJe);
                        }
                    }
                    accAccountDAOobj.deleteAccount(assetid, true);
                }

            }

        } catch (ServiceException ex) {
            throw new AccountingException(messageSource.getMessage("acc.prod.excp1", null, Locale.forLanguageTag(requestJobj.optString(Constants.language))));
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("accAccountControllerCMNServiceImpl.deleteAssetPurchase : " + ex.getMessage(), ex);
        }
    }

    public List<String> scanChildAccounts(String accountID) throws ServiceException {
        try {
            List<String> ReturnAccountChilds = new ArrayList<String>();
            ReturnAccountChilds.add(accountID);
            boolean flag = true;
            List<String> AccountChilds = new ArrayList<String>();
            AccountChilds.add(accountID);
            while (flag == true) {
                String str = (String) AccountChilds.get(0);
                List<?> Result = accAccountDAOobj.isChildforDelete(str);
                if (Result != null) {
                    Iterator<?> resultIterator = Result.iterator();
                    while (resultIterator.hasNext()) {
                        Object ResultObj = resultIterator.next();
                        Account account = (Account) ResultObj;
                        String Child = account.getID();
                        AccountChilds.add(Child);
                        ReturnAccountChilds.add(Child);
                    }
                }
                AccountChilds.remove(0);
                if (AccountChilds.isEmpty() == true) {
                    flag = false;
                }
            }
            return ReturnAccountChilds;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("scanChildAccounts : " + ex.getMessage(), ex);
        }
    }
}
