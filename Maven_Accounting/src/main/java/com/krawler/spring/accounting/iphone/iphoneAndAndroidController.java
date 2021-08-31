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
package com.krawler.spring.accounting.iphone;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.FieldConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.Producttype;
import com.krawler.hql.accounting.companypreferenceservice.AccCompanyPreferencesService;
import com.krawler.hql.accounting.currency.service.AccCurrencyService;
import com.krawler.hql.accounting.invoice.service.AccInvoiceModuleService;
import com.krawler.hql.accounting.masteritems.service.AccMasterItemsService;
import com.krawler.hql.accounting.vendor.service.AccVendorService;
import com.krawler.spring.accounting.accountservice.AccAccountService;
import com.krawler.spring.accounting.costCenter.service.AccCostCenterService;
import com.krawler.spring.accounting.currency.CurrencyContants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.service.AccCustomerService;
import com.krawler.spring.accounting.handler.AccDashboardService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.accDashboardController;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.accounting.product.service.AccProductService;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.tax.service.AccTaxService;
import com.krawler.spring.accounting.term.service.AccTermService;
import com.krawler.spring.accounting.uom.service.AccUomService;
import com.krawler.spring.authHandler.authHandlerController;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.commonlibs.service.AccExportpdfService;
import com.krawler.spring.mainaccounting.service.AccCustomerMainAccountingService;
import com.krawler.spring.mainaccounting.service.AccInvoiceService;
import com.krawler.spring.mainaccounting.service.AccMainAccountingService;
import com.krawler.spring.mainaccounting.service.AccSalesOrderAccountingService;
import com.krawler.spring.permissionHandler.permissionHandler;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.lowagie.text.DocumentException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import com.krawler.esp.handlers.APICallHandlerService;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import static com.krawler.spring.accounting.currency.CurrencyContants.TRANSACTIONDATE;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.goodsreceipt.service.accGoodsReceiptModuleService;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.authHandler.authHandler;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContextUtils;

public class iphoneAndAndroidController extends MultiActionController implements CurrencyContants {

    private authHandlerController authHandlerControllerObj;
    private sessionHandlerImpl sessionHandlerImplObj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private kwlCommonTablesDAO KwlCommonTablesDAOObj;
    private AccAccountService accAccountService;
    private AccDashboardService accDashboardService;
    private AccCompanyPreferencesService accCompanyPreferencesService;
    private AccVendorService accVendorService;
    private AccCurrencyService accCurrencyService;
    private AccMasterItemsService accMasterItemsService;
    private AccExportpdfService accExportpdfService;
    private AccMainAccountingService accMainAccountingService;
    private AccTaxService accTaxService;
    private AccCustomerService accCustomerService;
    private AccProductService accProductService;
    private AccInvoiceService accInvoiceService;
    private AccSalesOrderAccountingService accSalesOrderAccountingService;
    private AccProductModuleService accProductModuleService;
    private AccTermService accTermService;
    private AccCostCenterService accCostCenterService;
    private AccInvoiceModuleService accInvoiceModuleService;
    private accGoodsReceiptModuleService accGoodsReceiptModuleService;
    private AccCustomerMainAccountingService accCustomerMainAccountingService;
    private AccUomService accUomService;
    private accProductDAO accProductObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private APICallHandlerService apiCallHandlerService;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private AccJournalEntryModuleService accJournalEntryModuleService;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private HibernateTransactionManager txnManager;
    private accGoodsReceiptDAO accGoodsReceiptobj;
//    private auditTrailDAO auditTrailObj;
    
//    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
//        this.auditTrailObj = auditTrailDAOObj;
//    }
    
    public void setaccUomService(AccUomService accUomService) {
        this.accUomService = accUomService;
    }
        public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setAccMasterItemsService(AccMasterItemsService accMasterItemsService) {
        this.accMasterItemsService = accMasterItemsService;
    }

    public void setAccCompanyPreferencesService(AccCompanyPreferencesService accCompanyPreferencesService) {
        this.accCompanyPreferencesService = accCompanyPreferencesService;
    }

    public void setAccVendorService(AccVendorService accVendorService) {
        this.accVendorService = accVendorService;
    }

    public void setAccCurrencyService(AccCurrencyService accCurrencyService) {
        this.accCurrencyService = accCurrencyService;
    }

    public void setAccExportpdfService(AccExportpdfService accExportpdfService) {
        this.accExportpdfService = accExportpdfService;
    }
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setAccMainAccountingService(AccMainAccountingService accMainAccountingService) {
        this.accMainAccountingService = accMainAccountingService;
    }

    public void setAccTaxService(AccTaxService accTaxService) {
        this.accTaxService = accTaxService;
    }

    public void setAccCustomerService(AccCustomerService accCustomerService) {
        this.accCustomerService = accCustomerService;
    }

    public void setAccProductService(AccProductService accProductService) {
        this.accProductService = accProductService;
    }

    public void setAccInvoiceService(AccInvoiceService accInvoiceService) {
        this.accInvoiceService = accInvoiceService;
    }

    public void setAccSalesOrderAccountingService(AccSalesOrderAccountingService accSalesOrderAccountingService) {
        this.accSalesOrderAccountingService = accSalesOrderAccountingService;
    }

    public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }

    public void setAccTermService(AccTermService accTermService) {
        this.accTermService = accTermService;
    }

    public void setAccCostCenterService(AccCostCenterService accCostCenterService) {
        this.accCostCenterService = accCostCenterService;
    }

    public void setAccInvoiceModuleService(AccInvoiceModuleService accInvoiceModuleService) {
        this.accInvoiceModuleService = accInvoiceModuleService;
    }
    public void setAccGoodsReceiptModuleService(accGoodsReceiptModuleService accGoodsReceiptModuleService) {
        this.accGoodsReceiptModuleService = accGoodsReceiptModuleService;
    }

    public void setAccCustomerMainAccountingService(AccCustomerMainAccountingService accCustomerMainAccountingService) {
        this.accCustomerMainAccountingService = accCustomerMainAccountingService;
    }
                    
    public void setauthHandlerController(authHandlerController authHandlerControllerObj) {
        this.authHandlerControllerObj = authHandlerControllerObj;
    }

    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }

    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }

    public void setKwlCommonTablesDAO(kwlCommonTablesDAO KwlCommonTablesDAOObj1) {
        this.KwlCommonTablesDAOObj = KwlCommonTablesDAOObj1;
    }

    public void setAccAccountService(AccAccountService accAccountService) {
        this.accAccountService = accAccountService;
    }
    
    public AccDashboardService getAccDashboardService() {
        return accDashboardService;
    }

    public void setAccDashboardService(AccDashboardService accDashboardService) {
        this.accDashboardService = accDashboardService;
    }

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setAccJournalEntryModuleService(AccJournalEntryModuleService accJournalEntryModuleService) {
        this.accJournalEntryModuleService = accJournalEntryModuleService;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    public ModelAndView deskeraAccounting(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SessionExpiredException, JSONException, ServiceException {
        ModelAndView model;
        String result = "";
        try {

            JSONObject jSONObject = new JSONObject();
            int action = Integer.parseInt(request.getParameter("act"));
            int mode = Integer.parseInt(request.getParameter("actionmode"));
            String transactionDateinLong = request.getParameter("transactiondate");
            String billdateinlong = request.getParameter("billdate");
            String duedateinlong = request.getParameter("duedate");
            String shipdateinlong = request.getParameter("shipdate");
            DateFormat datef=authHandler.getDateOnlyFormat();
            //Check whether the call is from Iphone or Android. We are doing this to handle billdate case.
            boolean isIPhone = Boolean.parseBoolean(StringUtil.isNullOrEmpty(request.getParameter("isIPhone"))?"false":request.getParameter("isIPhone"));
            
            //transactiondate
            if (!StringUtil.isNullOrEmpty(transactionDateinLong)) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.valueOf(transactionDateinLong).longValue());
                String Trdate=datef.format(cal.getTime());
                try {
                    Date TransactionDate = datef.parse(Trdate);
                    request.setAttribute(TRANSACTIONDATE, TransactionDate);
                } catch (ParseException ex) {
                    request.setAttribute(TRANSACTIONDATE, cal.getTime());
                }
            }
            //Billdate
            if (isIPhone && !StringUtil.isNullOrEmpty(request.getParameter("billdate"))) {
                String billdate = request.getParameter("billdate");
                request.setAttribute("billdate", billdate);      
                request.setAttribute("isIPhone", isIPhone);
            } else if (!StringUtil.isNullOrEmpty(billdateinlong)) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.valueOf(billdateinlong).longValue());
                String bdate=datef.format(cal.getTime());
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
                String ddate=datef.format(cal.getTime());
                try {
                    Date billDate = datef.parse(ddate);
                    request.setAttribute("duedate", billDate);
                } catch (ParseException ex) {
                    request.setAttribute("duedate", cal.getTime());
                }
            }
            //shipdate
            if (!StringUtil.isNullOrEmpty(shipdateinlong)) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.valueOf(shipdateinlong).longValue());
                String sdate=datef.format(cal.getTime());
                try {
                    Date shipDate = datef.parse(sdate);
                    request.setAttribute("shipdate", shipDate);
                } catch (ParseException ex) {
                    request.setAttribute("shipdate", cal.getTime());
                }
            }

            if (action != 0) {
                try {
                    if (StringUtil.isNullOrEmpty(sessionHandlerImpl.getUserid(request))) {
                        setUserSession(request, response);
                    } else if (!StringUtil.equal(sessionHandlerImpl.getUserid(request), request.getParameter("userid"))) {
                        sessionHandlerImplObj.destroyUserSession(request, response);
                        setUserSession(request, response);
                    }
                } catch (SessionExpiredException ex) {
                    logger.warn("Exception in iphoneController:deskeraAccounting() - Session has not set. Need to create new session.");
                    setUserSession(request, response);
                }
            }

            switch (action) {
                case 0:
                    switch (mode) {
                        case 0://verify Login
                            JSONObject jobj = new JSONObject();
                            jobj = verifyLogin(request, response);
                            if (jobj.has("success") && (jobj.get("success").equals(true))) {
                                String userId = jobj.getString("lid");
                                request.setAttribute("userId", userId);
                                String permValues = getUserPermissions(request, response);
                                JSONObject permJson = new JSONObject(permValues);
                                jobj.remove("perms");
                                jobj.put("perms", permJson);
                                result = jobj.toString();
                            }
                            result = jobj.toString();
                            break;
                    }
                    break;
                case 1:  //dashboard request
                    switch (mode) {

                        case 0://getUserPermissions
                            String permValues = getUserPermissions(request, response);
                            JSONObject permJson = new JSONObject(permValues);
                            result = (new JSONObject().put("perms", permJson)).toString();
                            break;
                        case 1: // get Dashboard Updates
                            model = getDashboardUpdateDataIphone(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 2: // get Form Fields
                            model = accAccountService.getTransactionFormFields(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 3: // get Currency Exchange
                            model = getCurrencyExchange(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 4: // get Sequence Format
                            model = getSequenceFormatStore(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 5: // get getAccountsForCombo
                            model = getAccountsForCombo(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 6: // get getProductsForCombo
                            model = getProductsForCombo(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 7: // get getFieldParams
                            model = getFieldParams(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 8: // get getTax
                            model = getTax(request, response);
                            result = model.getModel().get("model").toString();
                            break;
//                        case 9: // get getModuleTemplate
//                            model = getModuleTemplate(request, response);
//                            result = model.getModel().get("model").toString();
//                            break;
                        case 10: // get getAllReportTemplate
                            model = getAllReportTemplate(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 11: // get getCustomCombodata
                            model = getCustomCombodata(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 12: // get getMasterItems
                            model = getMasterItems(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 13: // get Customer Information
                            model = getCustomersForCombo(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 14: // get Vendor Information
                            model = getVendorsForCombo(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 15: // get InvoiceCreated json
                            model = getInvoiceCreationJson(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 16://getNextAutoNumber
                            model = getNextAutoNumber(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 17://getSalesOrder
                            model = getSalesOrders(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 18://getNextAutoNumber
                            model = getIndividualProductPrice(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 19:
                            model = getSalesOrderRows(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 20://Term -NET
                            model = getTerm(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 21://DeliveryOrder Merged
                            model = getDeliveryOrdersMerged(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 22://DeliveryOrder Rows
                            model = getDeliveryOrderRows(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 23://Get Quotations
                            model = getQuotations(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 24://Get Quotations Rows
                            model = getQuotationRows(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 25://Get CostCenter
                            model = getCostCenter(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 26://Get Customer Exceeding Credit Limit
                            model = getCustomerExceedingCreditLimit(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 27://Get Unit Of Measure 
                            model = getUnitOfMeasure(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 28:// Get the Get Company Account Preferences Data
                            model = getCompanyAccountPreferences(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 29: //printing html for records   
                            model = getDocumentDesignerHtml(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 30://getting templatelist for print
                             model =getDesignTemplateList(request, response);
                            result = model.getModel().get("model").toString();
                            break;

                        case 32://getting templatelist for print
                            model = getProducts(request, response);
                            result = model.getModel().get("model").toString();
                            break;

                    }
                    break;
                case 2:  //Request Received From CRM 
                    switch (mode) {

                        case 1://SaveInvoice
                            model = saveCustomerInvoice(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 2://Save Quatation
                            model = saveCustomerQuotation(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                        case 3:// checking product used or not in accounting
                            result = checkProductUsedInTransactions(request, response);
                            break;
                        case 4:// get company currencyid
                            result = getCompanyCurrancy(request, response);
                            break;
                        case 5://Save Purchase Invoice
                            model = saveVendorInvoice(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                    }
                    break;
                case 3:    // Request received from POS - to get retail store list
                    switch (mode) {
                        case 1:
                            result = getRetailStorelistForPOS(request, response);
                            break;
                        case 2:
                            result = getStockForPOS(request, response);
                            break;
                        case 3:
                            result = getProductPriceForPOS(request, response);
                            break;
                        case 4:
                            model =saveCashoutTransation(request, response);
                            result = model.getModel().get("model").toString();
                            break;
                    }

                    break;
            }
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
            result = "{\"success\":\"false\",\"error\":\"Error occurred while retreiving data(" + ex.toString() + ")\",\"data\":[]}";
        }
        return new ModelAndView("jsonView_ex", "model", result);
    }

    public String checkProductUsedInTransactions(HttpServletRequest request, HttpServletResponse response) {
        JSONObject object = new JSONObject();
        boolean isProductUsedInTransaction = false;
        String modulesProductUsedIn = "";
        try {
            Product prd=null;
            String productID = request.getParameter("productid");
            String companyid = request.getParameter("companyid");
            if (!StringUtil.isNullOrEmpty(productID)) {
                KwlReturnObject rtObj = accProductObj.getProductByID(productID, companyid);
                if (!rtObj.getEntityList().isEmpty()) {
                    prd = ((Product) rtObj.getEntityList().get(0));
                    if (prd != null) {
                        List listObj = accProductModuleService.isProductUsedintransction(productID, companyid, request,false);// false: Product & Services Report
                        isProductUsedInTransaction =(Boolean) listObj.get(0);    //always boolean value
                        modulesProductUsedIn =(String) listObj.get(1);           //always String value
                    }
                }
            }
  
            if (!isProductUsedInTransaction && prd != null) {//If product present in erp and not used in transaction then delete this product from ERP side with CRM side
                MasterItem prodMasterItemObj = accProductObj.getProductsMasterItem(companyid, productID);
                KwlReturnObject kwlReturnObject_I = accProductObj.selectInventoryByProduct(productID, companyid);
                accProductObj.deleteProductCustomData(productID);
                accProductObj.deleteProPricePermanently(productID, companyid);
                accProductObj.deleteProductCategoryMappingDtails(productID);
                if (prd.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                    accProductObj.deleteProductAssembly(productID);
                    accProductObj.deleteProductBuildDetails(productID, companyid);
                    accProductObj.deleteProductbBuild(productID, companyid);
                }
                if (!StringUtil.isNullOrEmpty(productID) && prodMasterItemObj != null) {
                    accProductObj.deleteProductCategoryMappingDtails(productID);
                }
                if (!StringUtil.isNullOrEmpty(productID)) {
                    accProductObj.deleteNewProductBatch(productID, companyid);
                }
                if (!StringUtil.isNullOrEmpty(productID) && kwlReturnObject_I.getRecordTotalCount() > 0 && !prd.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                    accProductObj.deleteProductInitialInventoryDtails(productID, companyid);
                } else {
                    accProductObj.deleteAssemblyProductInventory(productID, companyid);
                }
                HashMap<String, Object> deleteSerialMap = new HashMap<String, Object>();
                deleteSerialMap.put("productid", productID);
                deleteSerialMap.put("companyid", companyid);
                accProductObj.deleteProductBatchSerialDetails(deleteSerialMap);
                accProductObj.deleteProductPermanently(productID, companyid);
//                auditTrailObj.insertAuditLog(AuditAction.PRODUCT_DELETION, "User "+ sessionHandlerImpl.getUserFullName(request) +" has deleted product "+prd.getName()+" from ERP and CRM permanently.", request, prd.getID());
            }
            
            object.put("productisusedinerp", isProductUsedInTransaction);
            object.put("moduleName", "ERP : "+modulesProductUsedIn.substring(0, Math.max(0, modulesProductUsedIn.length()-2)));
            object.put("success", true);
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
            object.put("productisusedinerp", true);
            object.put("success", false);
        } finally {
            return object.toString();
        }
    }
    
    private JSONObject verifyLogin(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
       try{
           ModelAndView model=authHandlerControllerObj.verifyLogin(request, response);
           jobj =  new JSONObject(model.getModel().get("model").toString());
           jobj =jobj.getJSONObject("data");
            if (jobj.has("success") && (jobj.get("success").equals(true))) {
                String userid = jobj.getString("lid");
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("userid", StringUtil.checkForNull(userid));
           }else{
                jobj.put("success", false);
                jobj.put("error", "Authentication failed");
            }
       } catch(Exception e) {
            logger.warn(e.getMessage(), e);
            jobj.put("success", false);
            jobj.put("error", "Error occurred while authentication " + e.toString());
            logger.warn(e.getMessage(),e);
        }
        return jobj;
    }

    public String getUserPermissions(HttpServletRequest request, HttpServletResponse response) {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        try {
            String userid = (request.getParameter("userid") != null ? request.getParameter("userid") : (request.getAttribute("userId") != null ? request.getAttribute("userId").toString() : ""));
            User user = (User) KwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.User", userid);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("userid", userid);
            JSONObject permJobj = new JSONObject();
            kmsg = permissionHandlerDAOObj.getActivityFeature();
            permJobj = permissionHandler.getAllPermissionJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());

            requestParams = new HashMap<String, Object>();
            requestParams.put("userid", userid);
            kmsg = permissionHandlerDAOObj.getUserPermission(requestParams);
            permJobj = permissionHandler.getRolePermissionJson(kmsg.getEntityList(), permJobj);

            kmsg = permissionHandlerDAOObj.getUserPermission(requestParams);
            List<Object[]> rows = kmsg.getEntityList();
            ArrayList jo = new ArrayList();
            JSONArray jarr = new JSONArray();
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
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
//        String[] keysNames=keys.getNames(keys);
        List<String> strings = new ArrayList<String>();
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
                Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return newValues;
    }
   

    private void setUserSession(HttpServletRequest request, HttpServletResponse response) {
       try {
            String userID = request.getParameter("u");
            User userObj=null;
           if (userID == null) {
               userID = request.getParameter("userid");
               if (!StringUtil.isNullOrEmpty(userID)) {
                   userObj = (User) KwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.User", userID);
               }
           }
           if (userObj == null) {
               String companyid = request.getParameter("companyid");

               if (StringUtil.isNullOrEmpty(companyid)) {
                   companyid = KwlCommonTablesDAOObj.getCompanyId(request.getParameter("cdomain"));
               }
               Company userObj1 = (Company) KwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.Company", companyid);
               userID = userObj1.getCreator().getUserID();
               userObj = (User) KwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.User", userID);
           }
            String user = userObj.getUserLogin().getUserName();
            String pwd = userObj.getUserLogin().getPassword();
            String domain = userObj.getCompany().getSubDomain();
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
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
            return;
        }

    }

    public ModelAndView getCustomerInvoiceForAndroidTemplateData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        JSONArray jArr = new JSONArray();
        JSONObject jobmodule = new JSONObject();
        JSONObject jobjmasteritems = new JSONObject();
        JSONObject jsoncurrency = new JSONObject();
        JSONObject companyprefermodel = new JSONObject();
        JSONObject vendorscomboviewmodel = new JSONObject();
        JSONObject purchaseorderandroidobj = new JSONObject();
        try {
            companyprefermodel = accCompanyPreferencesService.getSequenceFormatStore(request, response);
            purchaseorderandroidobj.put("sequenceformat", companyprefermodel);
//           jobmodule=accCommonService.getModuleTemplate(request); 
            jsoncurrency = accCurrencyService.getCurrencyExchange(request, response);
            vendorscomboviewmodel = accVendorService.getVendorsForCombo(request, response);
            jobjmasteritems = accMasterItemsService.getMasterItems(request);
            accExportpdfService.getAllReportTemplate(request, response);
            accMainAccountingService.getAccountsForCombo(request, response);

        } catch (SessionExpiredException ex) {
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //getting currencyexchange
    public ModelAndView getCurrencyExchange(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        try {
            jobj = accCurrencyService.getCurrencyExchange(request, response);
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(JSONVIEW, MODEL, jobj.toString());
    }

    //getting sequence format
    public ModelAndView getSequenceFormatStore(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = accCompanyPreferencesService.getSequenceFormatStore(request, response);
        }catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //getAccountsForCombo
    public ModelAndView getAccountsForCombo(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        jobj = accMainAccountingService.getAccountsForCombo(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //products details
    public ModelAndView getProductsForCombo(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        try {
            JSONObject paramjobj=StringUtil.convertRequestToJsonObject(request);
            jobj = accProductService.getProductsJsonForCombo(paramjobj);
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
//getting fieldparams

    public ModelAndView getFieldParams(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        KwlReturnObject result = null;
        JSONObject jresult = new JSONObject();
        try {
            jresult = accMainAccountingService.getFieldParams(request, response);
        } catch (Exception ex) {
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jresult.toString());
    }

    //include product tax
    public ModelAndView getTax(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        jobj = accTaxService.getTax(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

//      public ModelAndView getModuleTemplate(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
//        JSONObject jobj = accCommonService.getModuleTemplate(request);
//        return new ModelAndView("jsonView", "model", jobj.toString());
//    }
    //getReportsTemplates
    public ModelAndView getAllReportTemplate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        jobj = accExportpdfService.getAllReportTemplate(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getMasterItems(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = accMasterItemsService.getMasterItems(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accMasterItemsController.getMasterItems : " + ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    //getCustomComboData

    public ModelAndView getCustomCombodata(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        KwlReturnObject result = null;
        JSONObject jresult = new JSONObject();
        String flag = request.getParameter(FieldConstants.Crm_flag);
        String jsonview = flag != null ? "jsonView" : "jsonView-ex";
        try {
            jresult = accMainAccountingService.getCustomCombodata(request, response);
        } catch (Exception ex) {
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(jsonview, Constants.model, jresult.toString());
    }

    //getCustomersDetails
    public ModelAndView getCustomersForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accCustomerService.getCustomersForCombo(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //gettings vendors
    public ModelAndView getVendorsForCombo(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        JSONArray jArr = new JSONArray();
        jobj = accVendorService.getVendorsForCombo(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    //getInvoiceCreation

    public ModelAndView getInvoiceCreationJson(HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException, DocumentException, ServiceException, JSONException, com.itextpdf.text.DocumentException {
        JSONObject jobj = new JSONObject();
        jobj = accMainAccountingService.getInvoiceCreationJson(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //getNextAuto Number  
    public ModelAndView getNextAutoNumber(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = accCompanyPreferencesService.getNextAutoNumber(request, response);
        } catch (Exception ex) {
            issuccess = false;
            msg = ex.getMessage();
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getInvoicesMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = accInvoiceService.getInvoicesMerged(request, response);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getSalesOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = accSalesOrderAccountingService.getSalesOrders(request, response);

        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    //getPrice of Product

    public ModelAndView getIndividualProductPrice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accProductModuleService.getIndividualProductPrice(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    //loading the product grid on selection of salesorderno in invoice

    public ModelAndView getSalesOrderRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = accSalesOrderAccountingService.getSalesOrderRows(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "iphoneAndAndroidController.getSalesOrderRows:" + ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "iphoneAndAndroidController.getSalesOrderRows:" + ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    //getting Term NET  

    public ModelAndView getTerm(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accTermService.getTerm(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    //getDeliveryOrder on selecting deliveryorder

    public ModelAndView getDeliveryOrdersMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accInvoiceService.getDeliveryOrdersMerged(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //GetDeliveryOrderRows
    public ModelAndView getDeliveryOrderRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {

            jobj = accInvoiceService.getDeliveryOrderRows(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "iphoneAndAndroidController.getSalesOrderRows:" + ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "iphoneAndAndroidController.getSalesOrderRows:" + ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    //get quotation number in invoice

    public ModelAndView getQuotations(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accSalesOrderAccountingService.getQuotations(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //on selecting quotation number in link to
    public ModelAndView getQuotationRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accSalesOrderAccountingService.getQuotationRows(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getCostCenter(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accCostCenterService.getCostCenter(request, response);
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
     public ModelAndView getDashboardUpdateDataIphone(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        try {
            JSONObject jbj = new JSONObject();
            msg = accDashboardService.getDashboardUpdateDataIphone(request);
            boolean refresh = true;
            //  msg += "<link rel='alternate' type='application/rss+xml' title='RSS - Global RSS Feed' href=\""+com.krawler.common.util.URLUtil.getPageURL(request,"")+"feed.rss?m=global&u="+AuthHandler.getUserName(request)+"\">";
            /*
             * Request param must be sent from atleast one case
             */
            if (StringUtil.isNullOrEmpty(request.getParameter("refresh"))) {
                refresh = true;
            } else {
                refresh = Boolean.parseBoolean(request.getParameter("refresh"));
            }
            if (refresh) {
                jbj.put("valid", true);
                jbj.put("data", msg);
                msg = jbj.toString();
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }
    //before saving checking customer credit exceptions

    public ModelAndView getCustomerExceedingCreditLimit(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();
        jobj = accCustomerMainAccountingService.getCustomerExceedingCreditLimit(request);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    //saving the invoice     

    public ModelAndView saveCustomerInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accInvoiceModuleService.saveInvoice(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView saveVendorInvoice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            boolean isExpenseInv = false;
            boolean isFromPOS = StringUtil.isNullOrEmpty(request.getParameter("fromPOS")) ? false : true;
            String grid = request.getParameter("grid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            paramJobj.put("isFromPOS", isFromPOS);
            paramJobj.put("externalcurrencyrate", "1");
            paramJobj.put("defaultAdress", "true");
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            paramJobj.put("createdby", createdby);
            paramJobj.put("modifiedby", modifiedby);
            paramJobj.put("updatedon", updatedon);
            paramJobj.put("createdon", createdon);
            paramJobj.put("userid", createdby);
            paramJobj.put("incash", "true");
            paramJobj.put("df", df);
            String sequenceformat = "";
            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put("companyid", companyid);
            filterParams.put("modulename", "autogoodsreceipt");
            filterParams.put("ischecked", true);                       //is for active sequence format
            KwlReturnObject result1 = accCompanyPreferencesObj.getSequenceFormat(filterParams);
            Iterator itr = result1.getEntityList().iterator();
            while (itr.hasNext()) {
                SequenceFormat seqFormat = (SequenceFormat) itr.next();
                if (seqFormat.isIsdefaultformat()) {
                    sequenceformat = seqFormat.getID();
                }
            }
            if ((sequenceformat.equals("NA") || StringUtil.isNullOrEmpty(sequenceformat)) && result1.getEntityList().size() > 0) {
                SequenceFormat seqFormat = (SequenceFormat) result1.getEntityList().get(0);
                sequenceformat = seqFormat.getID();
            }
            if (sequenceformat.equals("NA") || StringUtil.isNullOrEmpty(sequenceformat)) {
                try {
                    throw new AccountingException("Sequence format is not set for customer Invoice");
                } catch (AccountingException ex) {
                    Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            paramJobj.put("methodid", request.getParameter("pmtmethod") != null ? request.getParameter("pmtmethod") : "");

            isExpenseInv = Boolean.parseBoolean(request.getParameter("isExpenseInv"));
            paramJobj.put("isExpenseInv", isExpenseInv);
            String currencyid = (request.getParameter("currencyid") == null ? request.getParameter("currencyid") : request.getParameter("currencyid"));
            paramJobj.put("currencyid", currencyid);
            KwlReturnObject kwlObj, result = null;
            kwlObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences ecp = (ExtraCompanyPreferences) kwlObj.getEntityList().get(0);
            paramJobj.put("vendor", ecp.getVendorForPOS());
            paramJobj.put("vendorid", ecp.getVendorForPOS());

            MasterItem masterItem = null;
            String creditAcc = null;
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONArray expjArr = new JSONArray();
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject detailData = jArr.getJSONObject(i);
                if (isFromPOS) {
                    isFromPOS = true;
                    kwlObj = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                    CompanyAccountPreferences cp = (CompanyAccountPreferences) kwlObj.getEntityList().get(0);
//                    creditAcc = cp.getCashoutACCForPOS() != null ? cp.getCashoutACCForPOS().getID() : "";
                    paramJobj.put("pmtmethod", cp.getPaymentMethod());
                    kwlObj = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), detailData.optString("reasonid"));
                    masterItem = (MasterItem) kwlObj.getEntityList().get(0);

                }
                JSONObject expJobj = new JSONObject();
                expJobj.put("accountid", masterItem.getAccID());
                expJobj.put("rate", detailData.opt("amount"));
                expJobj.put("debit", isFromPOS ? true : detailData.optBoolean("debit"));
                expJobj.put("amount", detailData.optString("amount"));
                expJobj.put("calamount", detailData.optString("amount"));
                expJobj.put("desc", "PI From POS");
                expJobj.put("discountispercent", 1);
                expJobj.put("discountamount", 0);
                expJobj.put("prdiscount", 0);
                expJobj.put("prtaxid", "");
                expJobj.put("rateIncludingGstEx", 0);
                expjArr.put(expJobj);

                paramJobj.put("expensedetail", expjArr.toString());
            }
            
            Map<String, String> map = new HashMap<>();
            paramJobj.put(Constants.PAGE_URL, URLUtil.getPageURL(request, loginpageFull));
            List li = accGoodsReceiptModuleService.saveGoodsReceipt(paramJobj, map);
            txnManager.commit(status);
            String JENumBer = "";
            if (li.get(5) != null) {
                JENumBer = li.get(5).toString();
            }
            String[] id = (String[]) li.get(0);
            status = null;
            TransactionStatus AutoNoStatus = null;
            int istemplate = 0;

            synchronized (this) {
                DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                def1.setName("AutoNum_Tx");
                def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                AutoNoStatus = txnManager.getTransaction(def1);
                if (!sequenceformat.equals("NA") && StringUtil.isNullOrEmpty(grid) && !StringUtil.isNullOrEmpty(sequenceformat)) {

                    GoodsReceipt gr = (GoodsReceipt) li.get(8);
                    String nextAutoNo = "";
                    int nextAutoNoInt = 0;
                    int from = StaticValues.AUTONUM_GOODSRECEIPT;
                    boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    if (seqformat_oldflag) {
                        nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, from, sequenceformat);
                        seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextAutoNo);
                    } else {
//                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, from, sequenceformat, seqformat_oldflag, gr.getJournalEntry().getEntryDate());
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, from, sequenceformat, seqformat_oldflag, gr.getCreationDate());
                    }
                    seqNumberMap.put(Constants.DOCUMENTID, id[0]);
                    seqNumberMap.put(Constants.companyKey, companyid);
                    seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                    String billno = accGoodsReceiptobj.updatePIEntryNumberForNewPI(seqNumberMap);
                    String invoiceNumBer = billno;
                }


                txnManager.commit(AutoNoStatus);

                if (StringUtil.isNullOrEmpty(id[1]) && istemplate != 2) { // only when new invoice is created
                    try {
                        status = txnManager.getTransaction(def);
                        HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                        JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                        JEFormatParams.put("modulename", "autojournalentry");
                        JEFormatParams.put("companyid", companyid);
                        JEFormatParams.put("isdefaultFormat", true);

                        kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                        GoodsReceipt gr = (GoodsReceipt) li.get(8);
                        Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
//                        JENumBer = accJournalEntryModuleService.updateJEEntryNumberForNewJE(jeDataMap, gr.getJournalEntry(), companyid, format.getID(), gr.getJournalEntry().getPendingapproval());
                        KwlReturnObject returnObj = accJournalEntryModuleService.updateJEEntryNumberForNewJE(jeDataMap, gr.getJournalEntry(), companyid, format.getID(), gr.getJournalEntry().getPendingapproval());
                        if(returnObj.isSuccessFlag() && returnObj.getRecordTotalCount()>0){
                            JENumBer = (String) returnObj.getEntityList().get(0);
                        }
                        txnManager.commit(status);
                    } catch (Exception ex) {
                        txnManager.rollback(status);
                        Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            issuccess = true;
            jobj.put("success", issuccess);
            jobj.put("msg", msg);

        } catch (AccountingException ex) {
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            txnManager.rollback(status);
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            txnManager.rollback(status);
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveCustomerQuotation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accSalesOrderAccountingService.saveQuotation(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    // Get the available Unit Of Measures
    public ModelAndView getUnitOfMeasure(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg="";
        try{
        jobj = accUomService.getUnitOfMeasure(request, response);
        }catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        }catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        }catch (JSONException ex) {
                Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
            }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    // Get the Get Company Account Preferences Data
    public ModelAndView getCompanyAccountPreferences(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accCompanyPreferencesService.getCompanyAccountPreferences(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //function to get print html for document designer
     public ModelAndView getDocumentDesignerHtml(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        int modueleid = Integer.parseInt(request.getParameter("moduleid"));
         switch (modueleid) {
             case 2://invoice template
                 jobj = accInvoiceService.exportSingleInvoice(request, response);
                 break;
         }
        jobj = accCompanyPreferencesService.getCompanyAccountPreferences(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     //to get the templatelist of document designer
    public ModelAndView getDesignTemplateList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accMainAccountingService.getDesignTemplateList(request, response);
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }   
        public ModelAndView getProducts(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams = productHandler.getProductRequestMap(request);
            KwlReturnObject result = accProductObj.getProducts(requestParams);
            List list = result.getEntityList();
            JSONArray DataJArr = productHandler.getProductsJson(requestParams, list,accProductObj,null,accountingHandlerDAOobj,accCurrencyDAOobj,false);
            jobj.put("productdata", DataJArr);
            
            
          //  jobj = accProductService.getProducts(request, response);
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     public String getRetailStorelistForPOS(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject result = new JSONObject();
//        ArrayList params = new ArrayList();
        try {

            JSONArray jArr = new JSONArray();
            String companyID = KwlCommonTablesDAOObj.getCompanyId(request.getParameter("cdomain"));
//            params.add(companyID);
//            String query = "select id,abbrev,description,address from in_storemaster where company=? and type=1 and isActive=1";
//            List list = HibernateUtil.executeSQLQuery(HibernateUtil.getCurrentSession(), query, params.toArray());
            List list = accProductObj.getRetailStorelistForPOS(companyID);
            int totalCount = list.size();
            Iterator itr = list.iterator();

            while (itr.hasNext()) {
                Object[] oj = (Object[]) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("storeid", oj[0].toString());
                obj.put("storeabberev", oj[1].toString());
                obj.put("storedescription", oj[2] != null ? oj[2].toString() : "");
                obj.put("storeaddress", oj[3] != null ? oj[3].toString() : "");
                jArr.put(obj);
            }
            result.put("success", true);
            result.put("data", jArr);
            result.put("totalCount", totalCount);

        } catch (ServiceException ex) {
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, "Service Exception while creating hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, "JSON Exception while creating hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        }
        return result.toString();
    }

    public String getStockForPOS(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject result = new JSONObject();
//        ArrayList params = new ArrayList();
//        ArrayList params1 = new ArrayList();
        try {

            JSONArray jArr = new JSONArray();
            String companyID = KwlCommonTablesDAOObj.getCompanyId(request.getParameter("cdomain"));
//            params.add(companyID);
//            String productQuery = "select id from product where company=?";
//            List productList = HibernateUtil.executeSQLQuery(HibernateUtil.getCurrentSession(), productQuery, params.toArray());
            List productList = accProductObj.getProductIDsOfCompanyForPOS(companyID);
            Iterator itr1 = productList.iterator();

            while (itr1.hasNext()) {
                String pid = itr1.next().toString();
//                params1.add(pid);
//                String query = "select sum((case when carryIn=true then baseuomquantity else -baseuomquantity end)) AS quantity from Inventory where deleted=false and product.ID=? group by product.ID";
//                List list = HibernateUtil.executeQuery(HibernateUtil.getCurrentSession(), query, params1.toArray());
                List list = accProductObj.getProductQuantityForPOS(pid);
                Iterator itr = list.iterator();

                while (itr.hasNext()) {
                    JSONObject obj = new JSONObject();
                    obj.put("product", pid);
                    obj.put("quantity", itr.next());
                    jArr.put(obj);
                }
//                params1.clear();
            }
            result.put("success", true);
            result.put("data", jArr);
            result.put("totalCount", jArr.length());

        } catch (ServiceException ex) {
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, "Service Exception while creating hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, "JSON Exception while creating hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        }
        return result.toString();
    }

    public String getCompanyCurrancy(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException, SessionExpiredException {
        boolean issuccess = false;
        JSONObject jobjResult = new JSONObject();
        String currencyid = "";
        try {
            String companyid = request.getParameter("companyid");
            Company companyObj = (Company) KwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.Company", companyid);
            if (companyObj != null && companyObj.getCurrency() != null) {
                currencyid = companyObj.getCurrency().getCurrencyID();
                issuccess = true;
                /*
                 If Integration settings changed request from CRM
                 */
                String deskeraerpflowcheckfield = request.getParameter("deskeraerpflowcheckfield");
                if (!StringUtil.isNullOrEmpty(deskeraerpflowcheckfield)) {
                    boolean isCRMActivate = false;
                    isCRMActivate = Boolean.parseBoolean(deskeraerpflowcheckfield);
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("id", companyid);
                    requestParams.put("activateCRMIntegration", isCRMActivate);
                    ExtraCompanyPreferences companyPreferences = accCompanyPreferencesObj.updateExtraCompanyPreferences(requestParams);
                }
            }
        } catch (Exception e) {
            issuccess = false;
            logger.warn("iphoneAndAndroidController.getCompanyCurrancy:" + e.getMessage(), e);
        } finally {
            jobjResult.put("currencyid", currencyid);
            jobjResult.put("success", issuccess);
        }
        return jobjResult.toString();
    }
    
    public String getProductPriceForPOS(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject result = new JSONObject();
        ArrayList params = new ArrayList();
        ArrayList params1 = new ArrayList();
        try {

            JSONArray jArr = new JSONArray();
          
            String companyID = KwlCommonTablesDAOObj.getCompanyId(request.getParameter("cdomain"));
            int offset=Integer.parseInt(request.getParameter("offset"));
            int limit=Integer.parseInt(request.getParameter("limit"));
            String currencyID=request.getParameter("currencyid");
            boolean firstRequest=Boolean.parseBoolean(request.getParameter("firstRequest"));
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date applicableDate = cal.getTime();

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("isSyncToPOS", true);
            requestParams.put("companyID", companyID);
            requestParams.put("applicableDate", applicableDate);
            requestParams.put("offset", offset);
            requestParams.put("limit",limit);
            requestParams.put("fromPOS",true);
            requestParams.put("currencyID",currencyID);
            requestParams.put("firstRequest",firstRequest);
            KwlReturnObject result1 = accMasterItemsDAOobj.getPOSProductsPrice(requestParams);

            List<Object[]> list = result1.getEntityList();
            Iterator itr = list.iterator();

            for (Object[] objs : list) {
                 JSONObject obj = new JSONObject();
                 obj.put("productId", (String) objs[0]);
                 obj.put("costPrice", (double) objs[1]);
                 obj.put("salesPrice", (double) objs[2]);
                 obj.put("currencyId", (KWLCurrency) objs[3]);
                 jArr.put(obj);
        }

            result.put("success", true);
            result.put("data", jArr);
            int totalCount = jArr.length();
            result.put("totalCount", result1.getRecordTotalCount());


        } catch (ServiceException ex) {
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, "Service Exception while creating hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, "JSON Exception while creating hrms Salary JE", ex);
            throw ServiceException.FAILURE(result.toString(), ex);
        }
        return result.toString();
    }
    
     public ModelAndView saveCashoutTransation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        jobj = accJournalEntryModuleService.saveJournalEntryRemoteApplication(request, response);
        return new ModelAndView("jsonView", "model", jobj.toString());
}
}
