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
package com.krawler.spring.accounting.product;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.*;
//import com.krawler.customFieldMaster.fieldDataManager;
import java.io.UnsupportedEncodingException;

import com.krawler.hql.accounting.*;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.AccountingException;
//import com.krawler.hql.accounting.Cyclecount;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.JournalEntry;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.hql.accounting.PriceList;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.ProductAssembly;
//import com.krawler.hql.accounting.ProductCyclecount;
//import com.krawler.hql.accounting.ProductStockPriceList;
import com.krawler.hql.accounting.Producttype;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.ObjectNotFoundException;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
import java.util.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.ServerEventManager;
import com.krawler.esp.handlers.StorageHandler;
import static com.krawler.spring.accounting.handler.AccountingManager.getGlobalCurrencyidFromRequest;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * 
 * @author krawler
 */
public class accProductController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accProductDAO accProductObj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private exportMPXDAOImpl exportDaoObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private MessageSource messageSource;
    private fieldDataManager fieldDataManagercntrl;
    private accAccountDAO accAccountDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    private accJournalEntryDAO accJournalEntryobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private AccProductModuleService accProductModuleService;
    private accCurrencyDAO accCurrencyDAOobj;
    private authHandlerDAO authHandlerDAOObj;
    private companyDetailsDAO companyDetailsDAOObj;
    private fieldManagerDAO fieldManagerDAOobj;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }

    public void setAccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
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

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj1) {
        this.companyDetailsDAOObj = companyDetailsDAOObj1;
    }
    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
//Product Type
    public ModelAndView saveProductTypes(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveProductTypes(request);
            issuccess = true;
            msg = "Product Types has been updated successfully";
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public Producttype saveProductTypes(HttpServletRequest request) throws ServiceException, AccountingException {
        Producttype ptype = null;
        try {
            String delQuery = "";
            int delCount = 0;
            JSONArray jArr = new JSONArray(request.getParameter(Constants.RES_data));
            JSONArray jDelArr = new JSONArray(request.getParameter("deleteddata"));
            KwlReturnObject typeresult = null;
            for (int i = 0; i < jDelArr.length(); i++) {
                JSONObject jobj = jDelArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString(Constants.Acc_id)) == false) {
                    try {
                        typeresult = accProductObj.deleteProductType(delQuery);
                        delCount += typeresult.getRecordTotalCount();
                    } catch (ServiceException ex) {
                        throw new AccountingException("Selected record(s) is currently used by the product(s).");
                    }
                }
            }

            String auditMsg = "", auditID = "";
            String fullName = sessionHandlerImpl.getUserFullName(request);
            if (delCount > 0) {
                auditTrailObj.insertAuditLog(AuditAction.PRODUCT_TYPE_DELETED, "User " + fullName + " deleted " + delCount + " product type", request, "0");
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (jobj.getBoolean("modified") == false) {
                    continue;
                }

                HashMap<String, Object> typeMap = new HashMap<String, Object>();
                typeMap.put("name", jobj.getString("name"));

                if (StringUtil.isNullOrEmpty(jobj.getString(Constants.Acc_id))) {
                    auditMsg = "added";
                    auditID = AuditAction.PRODUCT_TYPE_CREATED;
                } else {
                    typeMap.put(Constants.Acc_id, jobj.getString(Constants.Acc_id));
                    auditMsg = "updated";
                    auditID = AuditAction.PRODUCT_TYPE_UPDATED;
                }
                typeresult = accProductObj.saveProductTypes(typeMap);
                ptype = (Producttype) typeresult.getEntityList().get(0);
                auditTrailObj.insertAuditLog(auditID, "User " + fullName + " " + auditMsg + " product type to " + ptype.getName(), request, ptype.getID());
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return ptype;
    }

    public ModelAndView getProductTypes(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject result = accProductObj.getProductTypes(requestParams);
            jobj = getProductTypesJson(request, result.getEntityList());
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getProductTypesJson(HttpServletRequest request, List<Producttype> list) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(Constants.Acc_id, sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject extraresult = accCompanyPreferencesObj.getExtraCompanyPreferences(filterParams);
            ExtraCompanyPreferences extra = null;
            if (extraresult.getEntityList().size() > 0) {
                extra = (ExtraCompanyPreferences) extraresult.getEntityList().get(0);
            }
            JSONArray jArr = new JSONArray();
            for (Producttype ptype:list) {
                JSONObject obj = new JSONObject();
                obj.put(Constants.Acc_id, ptype.getID());
                obj.put("name", ptype.getName());
                if (((ptype.getID().equalsIgnoreCase(ptype.CUSTOMER_ASSEMBLY)) || (ptype.getID().equalsIgnoreCase(ptype.CUSTOMER_INVENTORY)))&& !extra.isJobworkrecieverflow()) {
                    continue;
                }
                jArr.put(obj);
            }
            jobj.put(Constants.RES_data, jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getProductTypesJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

//Product
    public ModelAndView getProducts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("companyid", sessionHandlerImpl.getCompanyid(request));
            paramJobj.put("userid", sessionHandlerImpl.getUserid(request));
            jobj = accProductModuleService.getProducts(paramJobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getProductsOptimized(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("includeParent"))) {
                requestParams.put("includeParent", request.getParameter("includeParent"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("showallproduct"))) {
                requestParams.put("showallproduct", Integer.parseInt(request.getParameter("showallproduct")));
            }
            KwlReturnObject result = accProductObj.getProducts(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();
            JSONArray DataJArr = getProductsOptimizedJson(request, list);
            jobj.put(Constants.RES_data, DataJArr);
            jobj.put(Constants.RES_TOTALCOUNT, count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception  ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray callProductTermMapJSONArray(HashMap<String, Object> data) {
        JSONArray productData = new JSONArray();
        try {
            KwlReturnObject result = accProductObj.getProductTermDetails(data);
            ArrayList<ProductTermsMap> ProductTermPurchaselist = (ArrayList<ProductTermsMap>) result.getEntityList();
            for (ProductTermsMap mt : ProductTermPurchaselist) {
                JSONObject jsonobj = new JSONObject();
                jsonobj.put(Constants.Acc_id, mt.getId());
                jsonobj.put("term", mt.getTerm().getTerm());
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("accountid", mt.getAccount().getID());
                jsonobj.put("glaccountname", !StringUtil.isNullOrEmpty(mt.getAccount().getName()) ? mt.getAccount().getName() : "");
                jsonobj.put("accode", !StringUtil.isNullOrEmpty(mt.getAccount().getAcccode()) ? mt.getAccount().getAcccode() : "");
                jsonobj.put("sign", mt.getTerm().getSign());
                jsonobj.put("formula", mt.getTerm().getFormula());
                jsonobj.put("isDefault", mt.isIsDefault());
                jsonobj.put("IsOtherTermTaxable", mt.getTerm().isOtherTermTaxable());
                jsonobj.put("termid", mt.getTerm().getId());
                jsonobj.put("formulaids", mt.getTerm().getFormula());
                jsonobj.put("termpercentage", mt.getPercentage());
                jsonobj.put("termtype", mt.getTerm().getTermType());
                jsonobj.put("termsequence", mt.getTerm().getTermSequence()+"");
                jsonobj.put(Constants.productid, mt.getProduct().getID());
                jsonobj.put("producttermmapid", mt.getId());
                jsonobj.put("purchasevalueorsalevalue", mt.getPurchaseValueOrSaleValue());
                jsonobj.put("deductionorabatementpercent", mt.getDeductionOrAbatementPercent());
                jsonobj.put("formType",   !StringUtil.isNullOrEmpty(mt.getFormType())?mt.getFormType():"1"); // 1 for without form
                jsonobj.put("taxtype", mt.getTaxType());
                jsonobj.put("taxvalue", mt.getPercentage());
                productData.put(jsonobj);
            }
        } catch (Exception ex) {
           Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return productData;
    }

    
    public ModelAndView getProductTermsList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        try {
            String productId = request.getParameter(Constants.productid);
            String termType = request.getParameter("termType");
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(Product.class.getName(), productId);
            Product product = (Product) cap.getEntityList().get(0);
            JSONArray ProductTermPurchaseArr = new JSONArray();
            JSONArray ProductTermSalesArr = new JSONArray();
            JSONArray ProductTermAdditionPurchaseArr = new JSONArray();
            JSONArray ProductTermAdditionSalesArr = new JSONArray();
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put(Constants.productid, product.getID());
            hashMap.put("specificTerm",3);
            if ("ProductTermPurchase".equalsIgnoreCase(termType)) {
                hashMap.put("salesOrPurchase", false);//For Purchase
                ProductTermPurchaseArr = accProductModuleService.callProductTermMapJSONArray(hashMap);
                hashMap.put("isAdditional", true);
                ProductTermAdditionPurchaseArr = accProductModuleService.callProductTermMapJSONArray(hashMap);
                jobj.put("ProductTermPurchaseMapp", ProductTermPurchaseArr);
                jobj.put("ProductTermAdditionalPurchaseMapp", ProductTermAdditionPurchaseArr);
            } else if ("ProductTermSales".equalsIgnoreCase(termType)) {
                hashMap.put("salesOrPurchase", true);//For Sales
                ProductTermSalesArr =accProductModuleService.callProductTermMapJSONArray(hashMap);
                hashMap.put("isAdditional", true);
                ProductTermAdditionSalesArr = accProductModuleService.callProductTermMapJSONArray(hashMap);
                jobj.put("ProductTermSalesMapp", ProductTermSalesArr);
                jobj.put("ProductTermAdditionalSalesMapp", ProductTermAdditionSalesArr);
            }
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public JSONArray getProductsOptimizedJson(HttpServletRequest request, List list) throws JSONException, ServiceException, SessionExpiredException, AccountingException, ParseException {

        //If you are changing anything in this function then make same changes in the getProductsJson function which is available in productHandler file

        Iterator itr = list.iterator();
        JSONArray jArr = new JSONArray();
        Producttype producttype = new Producttype();
        String productid = request.getParameter(Constants.productid);
        Boolean isSearch = false;
        if (request.getParameter("ss") != null && !StringUtil.isNullOrEmpty(request.getParameter("ss"))) {
            isSearch = true;
        }
        Boolean nonSaleInventory = Boolean.parseBoolean((String) request.getParameter("loadInventory"));
        while (itr.hasNext()) {
            try {
                Object[] row = (Object[]) itr.next();
                Product product = (Product) row[0];
                Product parentProduct = product.getParent();
                if (product.getID().equals(productid)) {
                    continue;
                }
                JSONObject obj = new JSONObject();
                obj.put(Constants.productid, product.getID());
                obj.put("productname", product.getName());
                obj.put("pid", product.getProductid());
                obj.put("parentid", parentProduct == null ? "" : parentProduct.getProductid());
                obj.put("parentname", parentProduct == null ? "" : parentProduct.getName());
                if (isSearch) {
                    obj.put("level", 0);
                    obj.put("leaf", true);
                } else {
                    obj.put("level", row[1]);
                    obj.put("leaf", row[2]);
                }

                if (!(nonSaleInventory && obj.get("producttype").equals(producttype.Inventory_Non_Sales))) {
                    jArr.put(obj);
                }
            } catch (Exception ex) {
                throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
            }
        }
        return jArr;
    }
//Product
    public ModelAndView getAssemblyProducts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            DateFormat sdf = new SimpleDateFormat("MMMM,yyyy");
            if(request.getParameter("stdate")!=null){
            Date startdt=sdf.parse(request.getParameter("stdate"));
            requestParams.put(Constants.REQ_startdate,startdt);
           Date enddt=sdf.parse(request.getParameter("enddate"));
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(enddt);
                // passing month-1 because 0-->jan, 1-->feb... 11-->dec
                calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
                Date enddate = calendar.getTime();
            String edate=df.format(enddate);
            try{
                enddate=df.parse(edate);
            }catch(ParseException ex){
                    enddate = calendar.getTime();
                }
            requestParams.put(Constants.REQ_enddate,enddate);
            }
            //Quick Search on Fetch button
            if(!StringUtil.isNullOrEmpty(request.getParameter("search"))){
                requestParams.put("search",request.getParameter("search"));
            }
            //Fetch Build / Unbuild Assembly Report
            boolean isUnbuildAssembly = !StringUtil.isNullOrEmpty(request.getParameter("isUnbuildAssembly")) ? Boolean.parseBoolean(request.getParameter("isUnbuildAssembly")): false;
            if(isUnbuildAssembly){
                requestParams.put("isBuild","false");
            }else{
                requestParams.put("isBuild","true");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("billid"))) {
                requestParams.put("buildid", request.getParameter("billid"));
            }
            KwlReturnObject result = accProductObj.getAssemblyProducts(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();
            JSONArray DataJArr = productHandler.getAssemblyProductsJSON(request, list);
            jobj.put(Constants.RES_data, DataJArr);
            jobj.put(Constants.RES_TOTALCOUNT, count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /*
     * Method to get assembly products with batch serial details.
     * Also we can perform quick search on serial no of main and sub products.
     */
    public ModelAndView getAssemblyProductsWithSerials(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            //Quick Search on Fetch button
            if (!StringUtil.isNullOrEmpty(request.getParameter("search"))) {
                requestJobj.put("search", request.getParameter("search"));
            }
            //Fetch Build / Unbuild Assembly Report
            boolean isUnbuildAssembly = !StringUtil.isNullOrEmpty(request.getParameter("isUnbuildAssembly")) ? Boolean.parseBoolean(request.getParameter("isUnbuildAssembly")) : false;
            if (isUnbuildAssembly) {
                requestJobj.put("isBuild", "false");
            } else {
                requestJobj.put("isBuild", "true");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("billid"))) {
                requestJobj.put("buildid", request.getParameter("billid"));
            }

            jobj = accProductModuleService.getAssembyProductDetails(requestJobj);

            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /*
     * Method to export assembly products with batch serial details.
     * Also we can perform quick search on serial no of main and sub products.
     */
    public ModelAndView exportAssemblyProductsWithSerials(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            //Quick Search on Fetch button
            if (!StringUtil.isNullOrEmpty(request.getParameter("search"))) {
                requestJobj.put("search", request.getParameter("search"));
            }
            //Fetch Build / Unbuild Assembly Report
            boolean isUnbuildAssembly = !StringUtil.isNullOrEmpty(request.getParameter("isUnbuildAssembly")) ? Boolean.parseBoolean(request.getParameter("isUnbuildAssembly")) : false;
            if (isUnbuildAssembly) {
                requestJobj.put("isBuild", "false");
            } else {
                requestJobj.put("isBuild", "true");
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("billid"))) {
                requestJobj.put("buildid", request.getParameter("billid"));
            }

            requestJobj.put("exportfalg",true);
            jobj = accProductModuleService.getAssembyProductDetails(requestJobj);

            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView getProductAvialbaleQuantity(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
              String productid=request.getParameter(Constants.productid);
              if(!StringUtil.isNullOrEmpty(productid)){
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(Product.class.getName(), productid);
                Product product = (Product) cap.getEntityList().get(0);
                KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(productid, product.getUnitOfMeasure().getID());
                double availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                    jobj.put("quantity",availableQuantity);
            }
            issuccess = true;
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView exportAssemblyProducts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
             requestParams.put("exportfalg",true);
            KwlReturnObject result = accProductObj.getAssemblyProducts(requestParams);
            List list = result.getEntityList();
            JSONArray DataJArr = productHandler.getAssemblyProductsJSON(request, list);
            jobj.put(Constants.RES_data, DataJArr);

            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView getProducsPriceRule(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getProducsPriceRule(request);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accProductController.getProducsPriceRule : " + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getProducsPriceRule(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            boolean productPriceinMultipleCurrency = Boolean.FALSE.parseBoolean(request.getParameter("productPriceinMultipleCurrency"));
            filter_names.add("company.companyID");
            filter_params.add(sessionHandlerImpl.getCompanyid(request));
            if (!productPriceinMultipleCurrency) {
                filter_names.add("currency.currencyID");
                filter_params.add(sessionHandlerImpl.getCurrencyID(request));
            }
            order_by.add("lowerlimit");
            order_by.add("upperlimit");
            order_type.add("asc");
            order_type.add("asc");
            filterRequestParams.put(Constants.filterNamesKey, filter_names);
            filterRequestParams.put(Constants.filterParamsKey, filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accProductObj.getProducsPriceRule(filterRequestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                ProductPriceRule productPriceRule = (ProductPriceRule) itr.next();
                JSONObject obj = new JSONObject();
                obj.put(Constants.Acc_id, productPriceRule.getID());
                if (productPriceRule.getLowerlimit() == 0 && productPriceRule.getUpperlimit() == 0) {
                    obj.put("upperlimit", "-");
                    obj.put("lowerlimit", "-");
                } else {
                    obj.put("upperlimit", productPriceRule.getUpperlimit());
                    obj.put("lowerlimit", productPriceRule.getLowerlimit());
                }
                obj.put("percentagetype", productPriceRule.getPercentageType());
                if (productPriceRule.getPercentageType() == 1) {
                    obj.put("percentagevalue", "Percentage");
                } else {
                    obj.put("percentagevalue", "Flat");
                }
                obj.put("amount", productPriceRule.getAmount());
                if (productPriceRule.getCategory() != null) {
                    obj.put("category", productPriceRule.getCategory().getValue());
                    obj.put("categoryid", productPriceRule.getCategory().getID());
                } else if (productPriceRule.getLowerlimit() == 0 && productPriceRule.getUpperlimit() == 0 && productPriceRule.getCategory() == null) {
                    obj.put("category", "All");
                    obj.put("categoryid", "All");
                } else {
                    obj.put("category", "-");
                    obj.put("categoryid", "");
                }
                obj.put("increamentordecreamentType", productPriceRule.getIncreamentordecreamentType());
                obj.put("priceType", productPriceRule.getPriceType());
                obj.put("ruleType", productPriceRule.getRuleType());
                if (productPriceRule.getIncreamentordecreamentType() == 1) {
                    obj.put("increamentordecreamentTypeName", "Increment");
                } else {
                    obj.put("increamentordecreamentTypeName", "Decrement");
                }
                if (productPriceRule.getPriceType() == 1) {
                    obj.put("priceTypeName", "Sales");
                } else {
                    obj.put("priceTypeName", "Purchase");
                }
                if (productPriceRule.getRuleType() == 1) {
                    obj.put("ruleTypeName", "Category");
                } else {
                    obj.put("ruleTypeName", "Product Price");
                }
                if (productPriceRule.getCurrency() != null) {
                    obj.put("currency", productPriceRule.getCurrency().getName());
                    obj.put(Constants.currencyKey, productPriceRule.getCurrency().getCurrencyID());
                } else {
                    obj.put("currency", "-");
                    obj.put(Constants.currencyKey, "");
                }
                obj.put("basedOnId", productPriceRule.getBasedOn());
                if (productPriceRule.getBasedOn() == 0) {
                    obj.put("basedOnName", "-");
                } else if (productPriceRule.getBasedOn() == 1) {
                    obj.put("basedOnName", "Existing Price");
                } else if (productPriceRule.getBasedOn() == 2) {
                    obj.put("basedOnName", "Average Cost");
                } else if (productPriceRule.getBasedOn() == 3) {
                    obj.put("basedOnName", "Most Recent Cost");
                } else if (productPriceRule.getBasedOn() == 4) {
                    obj.put("basedOnName", "Initial Purchase Price");
                }
                jArr.put(obj);
            }
            jobj.put(Constants.RES_data, jArr);
            jobj.put("count", jArr.length());
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jobj;
    }

    public ModelAndView saveProducsPriceRule(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveProducsPriceRule(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.prod.priceapp", null, RequestContextUtils.getLocale(request));   //"New Price has been applied successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public KwlReturnObject saveProducsPriceRule(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        KwlReturnObject productPriceRuleResult = null;
        String msg = "";
        String PriceType = "Sales";
        try {
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            HashMap<String, Object> ProductPriceRuleMap = AccountingManager.getGlobalParams(request);
            ProductPriceRuleMap.put("ruleType", request.getParameter("ruletype"));
            ProductPriceRuleMap.put("priceType", request.getParameter("pricetype"));
            ProductPriceRuleMap.put("category", request.getParameter("category"));
            ProductPriceRuleMap.put("increamentordecreamentType", request.getParameter("incrementrule"));
            ProductPriceRuleMap.put("lowerlimit", StringUtil.isNullOrEmpty(request.getParameter("lowerlimit")) ? "0" : request.getParameter("lowerlimit"));
            ProductPriceRuleMap.put("upperlimit", StringUtil.isNullOrEmpty(request.getParameter("upperlimit")) ? "0" : request.getParameter("upperlimit"));
            ProductPriceRuleMap.put("percentage", request.getParameter("percentagetype"));
            ProductPriceRuleMap.put("amount", request.getParameter("amount"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("currency"))) {
                ProductPriceRuleMap.put(Constants.currencyKey, request.getParameter("currency"));
            } else {
                ProductPriceRuleMap.put(Constants.currencyKey, currencyid);
            }
            ProductPriceRuleMap.put("basedOn", request.getParameter("basedOn"));
            productPriceRuleResult = accProductObj.saveProducsPriceRule(ProductPriceRuleMap);
            ProductPriceRule pl = (ProductPriceRule) productPriceRuleResult.getEntityList().get(0);
            if ("2".equalsIgnoreCase(request.getParameter("pricetype"))) {
                PriceType = "Purchase";
            }
            if ("1".equalsIgnoreCase(request.getParameter("ruletype"))) {
                auditTrailObj.insertAuditLog(AuditAction.PRODUCT_PRICE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " set rule on " + PriceType + " price of Product of Category " + request.getParameter("categoryName"), request, pl.getID());
            } else {
                auditTrailObj.insertAuditLog(AuditAction.PRODUCT_PRICE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " set rule on " + PriceType + " price of Product having price between " + request.getParameter("lowerlimit") + " and " + request.getParameter("upperlimit"), request, pl.getID());
            }

        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, msg, null, productPriceRuleResult.getEntityList(), 0);
    }

    public ModelAndView deleteProductPriceRule(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false, isCommitEx = false;
        String msg = "";
        KwlReturnObject result = null;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String id = request.getParameter("itempriceid");
            result = accProductObj.deleteProductPriceRule(id);
            issuccess = true;
            msg = messageSource.getMessage("acc.master.del", null, RequestContextUtils.getLocale(request));
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
                msg = messageSource.getMessage("acc.master.excp1", null, RequestContextUtils.getLocale(request));
            }
        } catch (ServiceException ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAssetDepreciation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONArray dataArray = new JSONArray();
            int depreciationMethod = Integer.parseInt(request.getParameter("depreciationmethod"));
            if (depreciationMethod == 1) {
                dataArray = getAssetStraightLineDepreciation(request);
            } else {
                dataArray = getDoubleDeclineDepreciation(request);
            }

            jobj.put(Constants.RES_data, dataArray);
            jobj.put(Constants.RES_TOTALCOUNT, dataArray.length());
            issuccess = true;

        } catch (JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private JSONArray getAssetStraightLineDepreciation(HttpServletRequest request) throws SessionExpiredException {
        JSONArray finalJArr = new JSONArray();
        try {
            String assetdetailId = request.getParameter("assetdetailId");
            AssetDetails ad = (AssetDetails) kwlCommonTablesDAOObj.getClassObject(AssetDetails.class.getName(), assetdetailId);
            Date creationDate = ad.getInstallationDate();

            DateFormat sdf = authHandler.getDateOnlyFormat(request);

            Calendar startcal = Calendar.getInstance();
            Calendar endcal = Calendar.getInstance();
            Calendar cal = Calendar.getInstance();

            double openingbalance = ad.getCost();
            double balance = openingbalance;
            double life = ad.getAssetLife();
            double salvage = ad.getSalvageValue();

            if (balance == 0) {
                return new JSONArray();
            }

            double periodDepreciation = calMonthwiseDepreciation(openingbalance, salvage, life * 12);
            double accDepreciation = 0;


            cal.setTime(creationDate);

            double firstPeriodAmt = periodDepreciation;
            double postedAccAmt = 0;

            for (int j = 0; j < life; j++) {
                for (int i = 0; i < 12; i++) {
                    int period = (12 * j) + i + 1;
                    accDepreciation += periodDepreciation;
                    balance -= periodDepreciation;

                    if (balance - salvage < -0.01 || (openingbalance == salvage && period > life * 12)) {
                        break;
                    }

                    if (balance - periodDepreciation - salvage < -0.01) {
                        periodDepreciation += balance - salvage;
                        accDepreciation += balance - salvage;
                        balance = salvage;
                    }

                    JSONObject finalObj = new JSONObject();
                    startcal.setTime(creationDate);
                    endcal.setTime(creationDate);
                    startcal.add(Calendar.YEAR, j);
                    endcal.add(Calendar.YEAR, j);
                    startcal.set(Calendar.MONTH, i + cal.get(Calendar.MONTH));
                    endcal.set(Calendar.MONTH, i + 1 + cal.get(Calendar.MONTH));
                    startcal.set(Calendar.HOUR, 0);
                    startcal.set(Calendar.MINUTE, 0);
                    startcal.set(Calendar.SECOND, 0);
                    endcal.set(Calendar.HOUR, 0);
                    endcal.set(Calendar.MINUTE, 0);
                    endcal.set(Calendar.SECOND, 0);

                    HashMap<String, Object> filters = new HashMap<String, Object>();
                    filters.put("period", period);
                    filters.put("assetDetailsId", ad.getId());
                    filters.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));

                    KwlReturnObject dresult = accProductObj.getAssetDepreciationDetail(filters);
                    Iterator itrcust = dresult.getEntityList().iterator();

                    if (itrcust.hasNext()) {
                        AssetDepreciationDetail dd = (AssetDepreciationDetail) itrcust.next();
                        if (period - 1 == cal.get(Calendar.MONTH)) {
                            firstPeriodAmt = dd.getPeriodAmount();
                        }
                        finalObj.put("perioddepreciation", dd.getPeriodAmount());
                        finalObj.put("accdepreciation", dd.getAccumulatedAmount());
                        finalObj.put("netbookvalue", dd.getNetBookValue());
                        finalObj.put("isje", true);
                        finalObj.put("depdetailid", dd.getID());
                        postedAccAmt = accDepreciation - dd.getAccumulatedAmount();
                    } else {
                        if (postedAccAmt > 0) {
                            finalObj.put("perioddepreciation", periodDepreciation + postedAccAmt);
                            postedAccAmt = 0;
                        } else {
                            finalObj.put("perioddepreciation", periodDepreciation);
                        }
                        finalObj.put("accdepreciation", accDepreciation);
                        finalObj.put("netbookvalue", balance);
                        finalObj.put("isje", false);
                        finalObj.put("depdetailid", "");
                    }

                    finalObj.put("period", period);
                    finalObj.put("firstperiodamt", firstPeriodAmt);
                    finalObj.put("frommonth", sdf.format(startcal.getTime()));
                    finalObj.put("tomonth", sdf.format(endcal.getTime()));
                    finalJArr.put(finalObj);
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return finalJArr;
    }

    public double calMonthwiseDepreciation(double openingbalance, double salvage, double month) throws ServiceException {
        double amount;
        try {
            amount = (openingbalance - salvage) / month;
        } catch (NumberFormatException ne) {
            throw ServiceException.FAILURE("calMonthwiseDepreciation : " + ne.getMessage(), ne);
        }
        return amount;
    }

    private JSONArray getDoubleDeclineDepreciation(HttpServletRequest request) throws SessionExpiredException {
        JSONArray finalJArr = new JSONArray();
        try {
            String assetdetailId = request.getParameter("assetdetailId");
            AssetDetails ad = (AssetDetails) kwlCommonTablesDAOObj.getClassObject(AssetDetails.class.getName(), assetdetailId);
            Date creationDate = ad.getInstallationDate();

            Calendar startcal = Calendar.getInstance();
            Calendar endcal = Calendar.getInstance();
            Calendar cal = Calendar.getInstance();

            DateFormat sdf = authHandler.getDateOnlyFormat(request);

            double openingbalance = ad.getCost();
            double balance = openingbalance;
            double life = ad.getAssetLife();
            double salvage = ad.getSalvageValue();
            if (balance == 0) {
                return new JSONArray();
            }

            double depreciationPercent = calDoubleDepreciationPercent(openingbalance, life * 12);
            double depreciationPercentValue = depreciationPercent;
            double accDepreciation = 0;

            cal.setTime(creationDate);
            double firstPeriodAmt = 0;
            double postedAccAmt = 0;
            for (int j = 0; j < life; j++) {
                for (int i = 0; i < 12; i++) {
                    int period = (12 * j) + i + 1;
                    double periodDepreciation = getFormatedNumber(balance * depreciationPercent / 100);
                    accDepreciation += periodDepreciation;
                    balance -= periodDepreciation;
                    if (period > life * 12) {
                        break;
                    }

                    if (balance < salvage) {
                        periodDepreciation += balance - salvage;
                        accDepreciation += balance - salvage;
                        balance = salvage;
                        depreciationPercentValue = (periodDepreciation / (balance + periodDepreciation)) * 100;
                    }

                    firstPeriodAmt = periodDepreciation;
                    JSONObject finalObj = new JSONObject();

                    startcal.setTime(creationDate);
                    endcal.setTime(creationDate);
                    startcal.add(Calendar.YEAR, j);
                    endcal.add(Calendar.YEAR, j);
                    startcal.set(Calendar.MONTH, i + cal.get(Calendar.MONTH));
                    endcal.set(Calendar.MONTH, i + 1 + cal.get(Calendar.MONTH));
                    startcal.set(Calendar.HOUR, 0);
                    startcal.set(Calendar.MINUTE, 0);
                    startcal.set(Calendar.SECOND, 0);
                    endcal.set(Calendar.HOUR, 0);
                    endcal.set(Calendar.MINUTE, 0);
                    endcal.set(Calendar.SECOND, 0);

                    HashMap<String, Object> filters = new HashMap<String, Object>();
                    filters.put("period", period);
                    filters.put("assetDetailsId", ad.getId());
                    filters.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));

                    KwlReturnObject dresult = accProductObj.getAssetDepreciationDetail(filters);
                    Iterator itrcust = dresult.getEntityList().iterator();

                    if (itrcust.hasNext()) {
                        AssetDepreciationDetail dd = (AssetDepreciationDetail) itrcust.next();
                        if (period - 1 == cal.get(Calendar.MONTH)) {
                            firstPeriodAmt = dd.getPeriodAmount();
                        }
                        finalObj.put("perioddepreciation", dd.getPeriodAmount());
                        finalObj.put("accdepreciation", dd.getAccumulatedAmount());
                        finalObj.put("netbookvalue", dd.getNetBookValue());
                        finalObj.put("isje", true);
                        finalObj.put("depdetailid", dd.getID());
                        postedAccAmt = accDepreciation - dd.getAccumulatedAmount();
                    } else {
                        if (postedAccAmt > 0) {
                            finalObj.put("perioddepreciation", periodDepreciation + postedAccAmt);
                            postedAccAmt = 0;
                        } else {
                            finalObj.put("perioddepreciation", periodDepreciation);
                        }
                        finalObj.put("accdepreciation", accDepreciation);
                        finalObj.put("netbookvalue", balance);
                        finalObj.put("isje", false);
                        finalObj.put("depdetailid", "");
                    }

                    finalObj.put("period", period);
                    finalObj.put("firstperiodamt", firstPeriodAmt);
                    finalObj.put("frommonth", sdf.format(startcal.getTime()));
                    finalObj.put("tomonth", sdf.format(endcal.getTime()));
                    String depreciationPercentString = getFormatedNumber(depreciationPercentValue) + "%";
                    finalObj.put("depreciatedPercent", depreciationPercentString);
                    finalJArr.put(finalObj);
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return finalJArr;
    }

    public double getFormatedNumber(double number) {
        NumberFormat nf = new DecimalFormat("0.00");
        String formatedStringValue = nf.format(number);
        double formatedValue = Double.parseDouble(formatedStringValue);
        return formatedValue;
    }

    /*
     * this method returns double depeciation percent for calculating
     * deprication of fixed asset. as given in WIKIPEDIA named as Declining
     * Balance Method
     */
    public double calDoubleDepreciationPercent(double openingbalance, double month) throws ServiceException {
        double doubleDepreciationPercent = 0d;
        try {
            double oneMonthDepriciationPercent = ((openingbalance / month) / openingbalance) * 100;
            doubleDepreciationPercent = oneMonthDepriciationPercent * 2;
            doubleDepreciationPercent = getFormatedNumber(doubleDepreciationPercent);

        } catch (NumberFormatException ne) {
            throw ServiceException.FAILURE("calMonthwiseDepreciation : " + ne.getMessage(), ne);
        }
        return doubleDepreciationPercent;
    }

    public ModelAndView getAssetOpenings(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            jobj = getAssetOpenings(request);
            issuccess = true;

        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private JSONObject getAssetOpenings(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        JSONObject finalJSONObject=new JSONObject();
        String companyId = sessionHandlerImpl.getCompanyid(request);
        String productId = request.getParameter("productId");

        DateFormat df = authHandler.getDateOnlyFormat();

        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyId", companyId);
        requestParams.put("productId", productId);
        if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.start))) {
            requestParams.put(Constants.start, request.getParameter(Constants.start));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.limit))) {
            requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        }
        int count=0;
        KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_FixedAssets_Details_ModuleId));
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
        HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
        try {
            KwlReturnObject result = accProductObj.getAssetOpenings(requestParams);

            List<FixedAssetOpening> list = result.getEntityList();
            count=result.getRecordTotalCount();
            for ( FixedAssetOpening assetOpening:list) {
                JSONObject jobj = new JSONObject();

                jobj.put("documentId", assetOpening.getId());
                jobj.put("documentNo", assetOpening.getDocumentNumber());
                jobj.put("documentDate", df.format(assetOpening.getCreationDate()));
                jobj.put("quantity", assetOpening.getQuantity());
                jobj.put("rate", assetOpening.getRate());
                requestParams.put("documentid",assetOpening.getId());
                KwlReturnObject result1 = accProductObj.getAssetDetail(requestParams);
                List list1 = result1.getEntityList();
                Iterator it1 = list1.iterator();
                JSONArray jArr1 = new JSONArray();
                while (it1.hasNext()) {
                    String assetDetailId = (String) it1.next();
                    AssetDetails ad = (AssetDetails) kwlCommonTablesDAOObj.getClassObject(AssetDetails.class.getName(), assetDetailId);
                Product product=null;
                    JSONObject obj = new JSONObject();
                    obj.put("documentId", assetOpening.getId());
                    obj.put("documentNo", assetOpening.getDocumentNumber());
                    obj.put("assetdetailId", ad.getId());
                    obj.put("assetGroup", ad.getProduct().getName());
                    obj.put("assetDepreciationMethod", ad.getProduct().getDepreciationMethod());
                    obj.put("assetGroupId", ad.getProduct().getID());
                    obj.put("assetId", ad.getAssetId());
                    obj.put("assetName", ad.getAssetId());
                    obj.put("sellAmount", ad.getSellAmount());
                    obj.put("installationDate", df.format(ad.getInstallationDate()));
                    obj.put("purchaseDate", df.format(ad.getPurchaseDate()));
                    obj.put("salvageRate", ad.getSalvageRate());
                    obj.put("assetLife", ad.getAssetLife());
                    obj.put("location", (ad.getLocation() != null) ? ad.getLocation().getId() : "");
                    obj.put("department", (ad.getDepartment() != null) ? ad.getDepartment().getId() : "");
                    obj.put("assetdescription", (ad.getAssetDescription() != null) ? ad.getAssetDescription() : "");
                    obj.put("assetUser", (ad.getAssetUser() != null) ? ad.getAssetUser().getUserID() : "");
                    obj.put("isBatchForProduct", isBatchForProduct);
                    obj.put("isSerialForProduct", isSerialForProduct);
                    addMachine(obj, companyId);


                    if (!StringUtil.isNullOrEmpty(productId)) {
                        KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), productId);
                        product = (Product) prodresult.getEntityList().get(0);
                        isBatchForProduct = product.isIsBatchForProduct();
                        isSerialForProduct = product.isIsSerialForProduct();
                        isLocationForProduct =product.isIslocationforproduct();
                        isWarehouseForProduct=product.isIswarehouseforproduct();
                        isRowForProduct=product.isIsrowforproduct();
                        isRackForProduct=product.isIsrackforproduct();
                        isBinForProduct=product.isIsbinforproduct();
                    }
                    if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {  //check if company level option is on then only we will check productt level
                        if (isBatchForProduct || isSerialForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct) {  //product level batch and serial no on or not
                            obj.put("batchdetails", getNewBatchJson(product, request, ad.getId(),new ArrayList()));
                        }
                    }
                    // calculate asset depreciation cost
                    double assetDepreciatedCost = 0d;

                    HashMap<String, Object> assetParams = new HashMap<String, Object>();
                    assetParams.put("assetDetailsId", ad.getId());
                    assetParams.put(Constants.companyKey, companyId);

                    KwlReturnObject assResult = accProductObj.getAssetDepreciationDetail(assetParams);
                    List<AssetDepreciationDetail> assList = assResult.getEntityList();

                    for (AssetDepreciationDetail depreciationDetail:assList) {
                        assetDepreciatedCost += depreciationDetail.getPeriodAmount();
                    }

                    double assetNetBookValue = ad.getCost() - assetDepreciatedCost;


                    obj.put("assetNetBookValue", assetNetBookValue);
                    obj.put("location", (ad.getLocation() != null ? ad.getLocation().getName() : ""));
                    obj.put("cost", ad.getCost());
                    obj.put("salvageRate", ad.getSalvageRate());
                    obj.put("salvageValue", ad.getSalvageValue());
                    obj.put("salvageValueInForeignCurrency", ad.getSalvageValue());
                    obj.put("costInForeignCurrency", ad.getCost());
                    obj.put("accumulatedDepreciation", ad.getAccumulatedDepreciation());
                    obj.put("assetLife", ad.getAssetLife());
                    obj.put("elapsedLife", ad.getElapsedLife());
                    obj.put("nominalValue", ad.getNominalValue());
                    obj.put("isAssetSold", (ad.getAssetSoldFlag() != 0) ? true : false);
                    obj.put("isDepreciable", assList.size() != 0 ? true : false);
                    obj.put("isLeased", ad.isLinkedToLeaseSO());
                    boolean isExport = (requestParams.get("isExport") == null) ? false : true;
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    AssetDetailsCustomData jeDetailCustom = (AssetDetailsCustomData) ad.getAssetDetailsCustomData();
                    replaceFieldMap1 = new HashMap<String, String>();
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, fieldMap, replaceFieldMap1, variableMap);
                        JSONObject params = new JSONObject();
                        params.put(Constants.companyKey, companyId);
                        params.put("isExport", isExport);
                        params.put("getCustomFieldArray", true);
                        params.put("isForReport", false);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                    jArr1.put(obj);
                }
                jobj.put("assetdetails",jArr1.toString());   //Asset details pass as form of assetdetailsarray
                jArr.put(jobj);
            }
            finalJSONObject.put(Constants.RES_data, jArr);
            finalJSONObject.put("count", count);
        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return finalJSONObject;
    }
    private void addMachine(JSONObject jSONObject, String companyId) throws ServiceException {
        try {
            Map<String, Object> requestParams = new HashMap();
            requestParams.put("companyId", companyId);
            requestParams.put("assetDetails", jSONObject.optString("assetdetailId"));
            KwlReturnObject result = accProductObj.getMachineId(requestParams);
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                try {
                    jSONObject.put("machine", (String) itr.next());
                } catch (com.krawler.utils.json.base.JSONException ex) {
                    throw ServiceException.FAILURE(ex.getMessage(), ex);
                }
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
   
    public Map<String, List<Object[]>> getBatchDetailsMap(Map<String, Object> requestParams) {
        Map<String, List<Object[]>> baMap = new HashMap<>();
        try {
            boolean linkingFlag = false;
            if (requestParams.containsKey("linkingFlag")) {
                linkingFlag = Boolean.parseBoolean(requestParams.get("linkingFlag").toString());
            }
            boolean isEdit = false;
            if (requestParams.containsKey("isEdit")) {
                isEdit = Boolean.parseBoolean(requestParams.get("isEdit").toString());
            }
            String moduleID = "";
            if (requestParams.containsKey("moduleID")) {
                moduleID = requestParams.get("moduleID").toString();
            }
            String documentIds = "";
            if (requestParams.containsKey("documentIds")) {
                documentIds = requestParams.get("documentIds").toString();
            }
            KwlReturnObject kmsg = accCommonTablesDAO.getBatchSerialDetails("", true, linkingFlag, moduleID, false, isEdit, documentIds);
            List<Object[]> batchserialdetails = kmsg.getEntityList();
            for (Object[] objects : batchserialdetails) {
                if (objects.length >= 20 && objects[20] != null) {  // chek wheather result having the documentid or not
                    if (baMap.containsKey(objects[20].toString())) {
                        List<Object[]> details = baMap.get(objects[20].toString());
                        details.add(objects);
                        baMap.put(objects[20].toString(), details);
                    } else {
                        List<Object[]> details = new ArrayList<>();
                        details.add(objects);
                        baMap.put(objects[20].toString(), details);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.INFO, ex.getMessage());
        }
        return baMap;
    }

    public ModelAndView getAssetDetailsForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONArray dataArray = getAssetDetailsForCombo(request);
            jobj.put(Constants.RES_data, dataArray);
            jobj.put(Constants.RES_TOTALCOUNT, dataArray.length());
            issuccess = true;
        } catch (JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private JSONArray getAssetDetailsForCombo(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        String companyId = sessionHandlerImpl.getCompanyid(request);
        String productId = request.getParameter("productId");
        String invdetailId = request.getParameter("invdetailId");
        String parentInvdetailId = request.getParameter("parentInvdetailId");
        boolean isFromSalesOrder = false;
        boolean usedFlag = false;
        boolean isSalesReturn = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        boolean isCustomer = false;
        boolean fromInvoice = false;
        boolean isEdit = false;

        if(!StringUtil.isNullOrEmpty(request.getParameter("isEdit"))){
            isEdit = Boolean.parseBoolean(request.getParameter("isEdit"));
        }

        if(!StringUtil.isNullOrEmpty(request.getParameter("isCustomer"))){
            isCustomer = Boolean.parseBoolean(request.getParameter("isCustomer"));
        }

        if(!StringUtil.isNullOrEmpty(request.getParameter("fromInvoice"))){
            fromInvoice = Boolean.parseBoolean(request.getParameter("fromInvoice"));
        }

        if (!StringUtil.isNullOrEmpty(request.getParameter("usedFlag"))) {
            usedFlag = Boolean.parseBoolean(request.getParameter("usedFlag"));
        }

        if (!StringUtil.isNullOrEmpty(request.getParameter("isSalesReturn"))) {
            isSalesReturn = Boolean.parseBoolean(request.getParameter("isSalesReturn"));
        }

        boolean isLeaseFixedAsset = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isLeaseFixedAsset"))) {
            isLeaseFixedAsset = Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset"));
        }

        boolean invrecord = true;
        if (!StringUtil.isNullOrEmpty(request.getParameter("invrecord"))) {
            invrecord = Boolean.parseBoolean(request.getParameter("invrecord"));
        }

        boolean isForGRO = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isForGRO"))) {
            isForGRO = Boolean.parseBoolean(request.getParameter("isForGRO"));
        }
        if (!StringUtil.isNullOrEmpty("isFromSalesOrder")) {
            isFromSalesOrder = Boolean.parseBoolean(request.getParameter("isFromSalesOrder"));
        }

        boolean excludeSoldAsset = (!StringUtil.isNullOrEmpty(request.getParameter("excludeSoldAsset"))) ? Boolean.parseBoolean(request.getParameter("excludeSoldAsset")) : false;

        boolean isQuotationFromPR = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isQuotationFromPR"))) {
            isQuotationFromPR = Boolean.parseBoolean(request.getParameter("isQuotationFromPR"));
        }

        boolean isPOfromVQ = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isPOfromVQ"))) {
            isPOfromVQ = Boolean.parseBoolean(request.getParameter("isPOfromVQ"));
        }

        boolean isPIFromVQ = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isPIFromVQ"))) {
            isPIFromVQ = Boolean.parseBoolean(request.getParameter("isPIFromVQ"));
        }

        boolean isPIFromPO = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isPIFromPO"))) {
            isPIFromPO = Boolean.parseBoolean(request.getParameter("isPIFromPO"));
        }

        boolean isFixedAssetPR = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAssetPR"))) {
            isFixedAssetPR = Boolean.parseBoolean(request.getParameter("isFixedAssetPR"));
        }

        boolean isFixedAssetSR = false;
        if (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAssetSR"))) {
            isFixedAssetSR = Boolean.parseBoolean(request.getParameter("isFixedAssetSR"));
        }

        DateFormat df = authHandler.getDateOnlyFormat(request);

        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("companyId", companyId);
        requestParams.put("invrecord", invrecord);
        requestParams.put("isForGRO", isForGRO);
        requestParams.put("usedFlag", usedFlag);
        requestParams.put("isSalesReturn", isSalesReturn);
        requestParams.put("isLeaseFixedAsset", isLeaseFixedAsset);
        requestParams.put("isCustomer", isCustomer);
        requestParams.put("fromInvoice", fromInvoice);
        requestParams.put("isEdit", isEdit);
        requestParams.put("isQuotationFromPR", isQuotationFromPR);
        requestParams.put("isPOfromVQ", isPOfromVQ);
        requestParams.put("isPIFromVQ", isPIFromVQ);
        requestParams.put("isPIFromPO", isPIFromPO);
        requestParams.put("isFixedAssetPR", isFixedAssetPR);
        requestParams.put("isFixedAssetSR", isFixedAssetSR);

        if (!StringUtil.isNullOrEmpty(invdetailId)) {
            requestParams.put("invdetailId", invdetailId);
        }

        if (!StringUtil.isNullOrEmpty(parentInvdetailId)) {
            requestParams.put("parentInvdetailId", parentInvdetailId);
        }

        KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyId);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

        if (!StringUtil.isNullOrEmpty(productId)) {
            requestParams.put("productId", productId);
        }
//        if (excludeSoldAsset) {
        requestParams.put("excludeSoldAsset", excludeSoldAsset);
        requestParams.put("isFromSalesOrder", isFromSalesOrder);
//        }

        if (!StringUtil.isNullOrEmpty(request.getParameter("isLeasedDoCreated"))) {
            requestParams.put("isLeasedDoCreated", Boolean.parseBoolean(request.getParameter("isLeasedDoCreated")));
        }

        if (!StringUtil.isNullOrEmpty(request.getParameter("isLeasedCICreated"))) {
            requestParams.put("isLeasedCICreated", Boolean.parseBoolean(request.getParameter("isLeasedCICreated")));
        }

        if (!StringUtil.isNullOrEmpty(request.getParameter("isLeasedSRCreated"))) {
            requestParams.put("isLeasedSRCreated", Boolean.parseBoolean(request.getParameter("isLeasedSRCreated")));
        }

        try {
            if (isPOfromVQ || isPIFromPO) {
                requestParams.put("linkrowid", request.getParameter("linkrowid"));
                requestParams.put(Constants.billid, request.getParameter(Constants.billid));
            }
            KwlReturnObject result = accProductObj.getAssetDetailsForCombo(requestParams);
            List list = result.getEntityList();
            Iterator it = list.iterator();
            while (it.hasNext()) {
                String assetDetailId = (String) it.next();
                if (isQuotationFromPR || isPOfromVQ || isPIFromVQ || (isPIFromPO && !isEdit)) {
                    PurchaseRequisitionAssetDetails ad = (PurchaseRequisitionAssetDetails) kwlCommonTablesDAOObj.getClassObject(PurchaseRequisitionAssetDetails.class.getName(), assetDetailId);
                    Product product = null;
                    JSONObject jobj = new JSONObject();

                    jobj.put("assetdetailId", ad.getId());
                    jobj.put("assetGroup", ad.getProduct().getName());
                    jobj.put("assetDepreciationMethod", ad.getProduct().getDepreciationMethod());
                    jobj.put("assetGroupId", ad.getProduct().getID());
                    jobj.put("assetId", ad.getAssetId());
                    jobj.put("assetName", ad.getAssetId());
                    jobj.put("assetdescription", ad.getAssetDescription());
                    jobj.put("sellAmount", ad.getSellAmount());
                    jobj.put("installationDate", df.format(ad.getInstallationDate()));
                    jobj.put("purchaseDate", df.format(ad.getPurchaseDate()));
                    jobj.put("salvageRate", ad.getSalvageRate());
                    jobj.put("assetLife", ad.getAssetLife());
                    jobj.put("location", (ad.getLocation() != null) ? ad.getLocation().getId() : "");
                    jobj.put("department", (ad.getDepartment() != null) ? ad.getDepartment().getId() : "");
                    jobj.put("assetUser", (ad.getAssetUser() != null) ? ad.getAssetUser().getUserID() : "");
                    jobj.put("assetUser", (ad.getAssetUser() != null) ? ad.getAssetUser().getUserID() : "");
                    jobj.put("assetUser", (ad.getAssetUser() != null) ? ad.getAssetUser().getUserID() : "");

                    if (!StringUtil.isNullOrEmpty(productId)) {
                        KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), productId);
                        product = (Product) prodresult.getEntityList().get(0);
                        isBatchForProduct = product.isIsBatchForProduct();
                        isSerialForProduct = product.isIsSerialForProduct();
                        isLocationForProduct = product.isIslocationforproduct();
                        isWarehouseForProduct = product.isIswarehouseforproduct();
                        isRowForProduct = product.isIsrowforproduct();
                        isRackForProduct = product.isIsrackforproduct();
                        isBinForProduct = product.isIsbinforproduct();
                    }
                    jobj.put("isBatchForProduct", isBatchForProduct);
                    jobj.put("isSerialForProduct", isSerialForProduct);

                    if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {  //check if company level option is on then only we will check productt level
                        if (isBatchForProduct || isSerialForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct) {  //product level batch and serial no on or not
                            jobj.put("batchdetails", getNewBatchJson(product, request, ad.getId(),new ArrayList()));
                        }
                    }

                    // calculate asset depreciation cost
                    double assetDepreciatedCost = 0d;

                    HashMap<String, Object> assetParams = new HashMap<String, Object>();
                    assetParams.put("assetDetailsId", ad.getId());
                    assetParams.put(Constants.companyKey, companyId);
                    
                    KwlReturnObject assResult = accProductObj.getAssetDepreciationDetail(assetParams);
                    List<AssetDepreciationDetail> assList = assResult.getEntityList();

                    for (AssetDepreciationDetail depreciationDetail :assList) {
                        assetDepreciatedCost += depreciationDetail.getPeriodAmount();
                    }

                    jobj.put("cost", (ad.getCost() - assetDepreciatedCost));
                    jobj.put("salvageValue", ad.getSalvageValue());
                    jobj.put("accumulatedDepreciation", ad.getAccumulatedDepreciation());
                    jobj.put("wdv", ad.getWdv());
                    jobj.put("assetLife", ad.getAssetLife());
                    jobj.put("elapsedLife", ad.getElapsedLife());
                    jobj.put("nominalValue", ad.getNominalValue());
                    jArr.put(jobj);
                } else {
                    HashMap<String, Object> fieldrequestParams1 = new HashMap();
                    fieldrequestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams1.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_FixedAssets_Details_ModuleId));
                    HashMap<String, String> customFieldMap1 = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap1 = new HashMap<String, String>();
                    HashMap<String, String> replaceFieldMap11 = new HashMap<String, String>();
                    HashMap<String, Integer> fieldMap1 = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams1, replaceFieldMap11, customFieldMap1, customDateFieldMap1);
                    AssetDetails ad = (AssetDetails) kwlCommonTablesDAOObj.getClassObject(AssetDetails.class.getName(), assetDetailId);
                    Product product=null;
                    JSONObject jobj = new JSONObject();

                    jobj.put("assetdetailId", ad.getId());
                    jobj.put("assetGroup", ad.getProduct().getName());
                    jobj.put("assetDepreciationMethod", ad.getProduct().getDepreciationMethod());
                    jobj.put("assetGroupId", ad.getProduct().getID());
                    jobj.put("assetId", ad.getAssetId());
                    jobj.put("assetName", ad.getAssetId());
                    jobj.put("assetdescription", ad.getAssetDescription());
                    jobj.put("sellAmount", ad.getSellAmount());
                    jobj.put("installationDate", df.format(ad.getInstallationDate()));
                    jobj.put("purchaseDate", df.format(ad.getPurchaseDate()));
                    jobj.put("salvageRate", ad.getSalvageRate());
                    jobj.put("assetLife", ad.getAssetLife());
                    jobj.put("location", (ad.getLocation() != null) ? ad.getLocation().getId() : "");
                    jobj.put("department", (ad.getDepartment() != null) ? ad.getDepartment().getId() : "");
                    jobj.put("assetUser", (ad.getAssetUser() != null) ? ad.getAssetUser().getUserID() : "");
                    jobj.put("assetUser", (ad.getAssetUser() != null) ? ad.getAssetUser().getUserID() : "");
                    jobj.put("assetUser", (ad.getAssetUser() != null) ? ad.getAssetUser().getUserID() : "");
                    jobj.put("isBatchForProduct", isBatchForProduct);
                    jobj.put("isSerialForProduct", isSerialForProduct);
                    addMachine(jobj,companyId);
                    if (!StringUtil.isNullOrEmpty(productId)) {
                        KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), productId);
                        product = (Product) prodresult.getEntityList().get(0);
                        isBatchForProduct = product.isIsBatchForProduct();
                        isSerialForProduct = product.isIsSerialForProduct();
                        isLocationForProduct =product.isIslocationforproduct();
                        isWarehouseForProduct=product.isIswarehouseforproduct();
                        isRowForProduct=product.isIsrowforproduct();
                        isRackForProduct=product.isIsrackforproduct();
                        isBinForProduct=product.isIsbinforproduct();
                    }
                    if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {  //check if company level option is on then only we will check productt level
                        if (isBatchForProduct || isSerialForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct) {  //product level batch and serial no on or not
                            jobj.put("batchdetails", getNewBatchJson(product, request, ad.getId(),new ArrayList()));
                        }
                    }

                    // calculate asset depreciation cost
                    boolean isDepreciationPosted = false;
                    double assetDepreciatedCost = 0d;

                    HashMap<String, Object> assetParams = new HashMap<String, Object>();
                    assetParams.put("assetDetailsId", ad.getId());
                    assetParams.put(Constants.companyKey, companyId);
                    assetParams.put("assetDetails", true);

                    KwlReturnObject assResult = accProductObj.getAssetDepreciationDetail(assetParams);
                    List<AssetDepreciationDetail> assList = assResult.getEntityList();

                    for (AssetDepreciationDetail depreciationDetail :assList) {
                        assetDepreciatedCost += depreciationDetail.getPeriodAmount();
                    }
                    jobj.put("isDepreciationPosted", isDepreciationPosted);
                    jobj.put("cost", (ad.getCost() - assetDepreciatedCost));
                    jobj.put("salvageValue", ad.getSalvageValue());
                    jobj.put("accumulatedDepreciation", ad.getAccumulatedDepreciation());
                    jobj.put("assetLife", ad.getAssetLife());
                    jobj.put("elapsedLife", ad.getElapsedLife());
                    jobj.put("nominalValue", ad.getNominalValue());
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    AssetDetailsCustomData jeDetailCustom = (AssetDetailsCustomData) ad.getAssetDetailsCustomData();
                    replaceFieldMap11 = new HashMap<String, String>();
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, fieldMap1, replaceFieldMap11, variableMap);
                        JSONObject params = new JSONObject();
                        params.put(Constants.companyKey, companyId);
                        params.put("getCustomFieldArray", true);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap1, customDateFieldMap1, jobj, params);
                    }
                    jArr.put(jobj);
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jArr;
    }

    public String getNewBatchJson(Product product, HttpServletRequest request, String documentid, List batchserialdetails) throws ServiceException, SessionExpiredException, JSONException {
        JSONArray jSONArray = new JSONArray();
        KwlReturnObject kmsg = null;
        boolean linkingFlag = (StringUtil.isNullOrEmpty(request.getParameter("linkingFlag"))) ? false : Boolean.parseBoolean(request.getParameter("linkingFlag"));
        boolean isEdit=(StringUtil.isNullOrEmpty(request.getParameter("isEdit")))?false:Boolean.parseBoolean(request.getParameter("isEdit"));
        String moduleID = request.getParameter("moduleid");
        boolean isBatch = false;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        if (batchserialdetails.isEmpty()) {
            if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && !product.isIsrowforproduct() && !product.isIsrackforproduct() && !product.isIsbinforproduct() && product.isIsSerialForProduct()) {
                kmsg = accCommonTablesDAO.getOnlySerialDetails(documentid, linkingFlag, moduleID, false, isEdit);
                batchserialdetails=kmsg.getEntityList();
            } else {
                isBatch = true;
                kmsg = accCommonTablesDAO.getBatchSerialDetails(documentid, !product.isIsSerialForProduct(), linkingFlag, moduleID, false, isEdit, "");
                batchserialdetails=kmsg.getEntityList();
            }
        }
        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.SerialWindow_ModuleId, 1));
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
        HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
        double ActbatchQty = 1;
        double batchQty = 0;
        Iterator iter = batchserialdetails.iterator();
        while (iter.hasNext()) {
            Object[] objArr = (Object[]) iter.next();
            JSONObject obj = new JSONObject();
            obj.put(Constants.Acc_id, objArr[0] != null ? (String) objArr[0] : "");
            obj.put("batch", objArr[1] != null ? (String) objArr[1] : "");
            obj.put("batchname", objArr[1] != null ? (String) objArr[1] : "");
            obj.put("location", objArr[2] != null ? (String) objArr[2] : "");
            obj.put("warehouse", objArr[3] != null ? (String) objArr[3] : "");
        if (isBatch){
                obj.put("row", objArr[15] != null ? (String) objArr[15] : "");
                obj.put("rack", objArr[16] != null ? (String) objArr[16] : "");
                obj.put("bin", objArr[17] != null ? (String) objArr[17] : "");
            }

            if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct()) && product.isIsSerialForProduct()) {

                ActbatchQty=accCommonTablesDAO.getBatchQuantity(documentid,(String)objArr[0]);

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
            if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct()) && !product.isIsSerialForProduct()) {
                obj.put("quantity", objArr[11] != null ? objArr[11] : "");
            }

            obj.put("balance", 0);
            obj.put("asset", "");
            obj.put("serialnoid", objArr[7] != null ? (String) objArr[7] : "");
            obj.put("serialno", objArr[8] != null ? (String) objArr[8] : "");
            obj.put("purchasebatchid", objArr[0] != null ? (String) objArr[0] : "");
            obj.put("purchaseserialid", objArr[7] != null ? (String) objArr[7] : "");
            obj.put("expstart", (objArr[9] != null && !objArr[9].toString().equalsIgnoreCase("")) ? df.format(objArr[9]) : "");
            obj.put("expend", (objArr[10] != null && !objArr[10].toString().equalsIgnoreCase("")) ? df.format(objArr[10]) : "");
            obj.put("documentid", documentid != null ? documentid : "");
            obj.put("skufield", objArr[13] != null ? (String) objArr[13] : "");
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("invoiceID", objArr[0]);
            hashMap.put(Constants.companyKey, product.getCompany().getCompanyID());
            /**
             * Get document count attached to batch
             */
            obj.put("attachment", 0);
            obj.put("attachmentids", "");
            KwlReturnObject object = accMasterItemsDAOobj.getBatchDocuments(hashMap);
            if (object.getEntityList() != null && object.getEntityList().size() > 0) {
                obj.put("attachment", object.getEntityList().size());
                List<Object[]> attachmentDetails = object.getEntityList();
                String docids = "";
                for (Object[] attachmentArray : attachmentDetails) {
                    docids = docids + attachmentArray[3] + ",";
                }
                if (!StringUtil.isNullOrEmpty(docids)) {
                    docids = docids.substring(0, docids.length() - 1);
                }
                obj.put("attachmentids", docids);
            }
            if (objArr[14] != null && !objArr[14].toString().equalsIgnoreCase("")) {           //Get SerialDocumentMappingId
                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(SerialDocumentMapping.class.getName(), objArr[14].toString());
                SerialDocumentMapping sdm = (SerialDocumentMapping) result1.getEntityList().get(0);
                Map<String, Object> variableMap = new HashMap<String, Object>();
                SerialCustomData serialCustomData = (SerialCustomData) sdm.getSerialCustomData();
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                AccountingManager.setCustomColumnValues(serialCustomData, fieldMap, replaceFieldMap, variableMap);
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
                        DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateFromDB=null;
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
            jSONArray.put(obj);
            batchQty--;
        }
        return jSONArray.toString();
    }

    public String getBatchJson(ProductBatch productBatch, HttpServletRequest request, boolean isbatch, boolean isBatchForProduct, boolean isserial, boolean isSerialForProduct) throws ServiceException, SessionExpiredException, JSONException {
        JSONArray jSONArray = new JSONArray();
        String purchasebatchid = "";
        DateFormat df = authHandler.getDateOnlyFormat(request);
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
        filter_names.add("batch.id");
        filter_params.add(productBatch.getId());

        filter_names.add("company.companyID");
        filter_params.add(sessionHandlerImpl.getCompanyid(request));

        filterRequestParams.put(Constants.filterNamesKey, filter_names);
        filterRequestParams.put(Constants.filterParamsKey, filter_params);

        KwlReturnObject kmsg = accCommonTablesDAO.getSerialForBatch(filterRequestParams);

        List list = kmsg.getEntityList();
        Iterator iter = list.iterator();
        int i = 1;
        while (iter.hasNext()) {
            BatchSerial batchSerial = (BatchSerial) iter.next();
            JSONObject obj = new JSONObject();
            if (i == 1) {
                obj.put(Constants.Acc_id, productBatch.getId());
                obj.put("batch", productBatch.getName());
                obj.put("batchname", productBatch.getName());
                obj.put("location", productBatch.getLocation().getId());
                obj.put("warehouse", productBatch.getWarehouse().getId());
                obj.put("mfgdate", productBatch.getMfgdate() != null ? authHandler.getDateFormatter(request).format(productBatch.getMfgdate()) : "");
                obj.put("expdate", productBatch.getExpdate() != null ? authHandler.getDateFormatter(request).format(productBatch.getExpdate()) : "");
                obj.put("quantity", productBatch.getQuantity());
                obj.put("balance", productBatch.getBalance());
                obj.put("asset", productBatch.getAsset());
                obj.put("purchasebatchid", productBatch.getId()); //in do we are allocating the record directly in this case purchase batch id is batch id
                purchasebatchid = productBatch.getId();
            } else {
                obj.put(Constants.Acc_id, "");
                obj.put("batch", "");
                obj.put("batchname", "");
                obj.put("location", "");
                obj.put("warehouse", "");
                obj.put("mfgdate", "");
                obj.put("expdate", "");
                obj.put("quantity", "");
                obj.put("balance", "");
            }
            i++;
            obj.put("serialnoid", batchSerial.getId());
            obj.put("serialno", batchSerial.getName());
            obj.put("expstart", batchSerial.getExpfromdate() != null ? authHandler.getDateFormatter(request).format(batchSerial.getExpfromdate()) : "");
            obj.put("expend", batchSerial.getExptodate() != null ? authHandler.getDateFormatter(request).format(batchSerial.getExptodate()) : "");
            obj.put("purchaseserialid", batchSerial.getId());   //in do we are allocating the record directly in this case purchase serial id is seial if
            jSONArray.put(obj);
        }
        if (isBatchForProduct && !isSerialForProduct) //only in batch case
        {
            JSONObject Jobj = new JSONObject();
            Jobj = getOnlyBatchDetail(productBatch, request);
            if (!StringUtil.isNullOrEmpty(purchasebatchid)) {
                Jobj.put("purchasebatchid", purchasebatchid);
            }
            jSONArray.put(Jobj);
        }
        return jSONArray.toString();
    }

    public JSONObject getOnlyBatchDetail(ProductBatch productBatch, HttpServletRequest request) throws JSONException, SessionExpiredException {

        JSONObject obj = new JSONObject();
        obj.put(Constants.Acc_id, productBatch.getId());
        obj.put("batch", productBatch.getName());
        obj.put("batchname", productBatch.getName());
        obj.put("location", productBatch.getLocation().getId());
        obj.put("warehouse", productBatch.getWarehouse().getId());
        obj.put("mfgdate", productBatch.getMfgdate() != null ? authHandler.getDateFormatter(request).format(productBatch.getMfgdate()) : "");
        obj.put("expdate", productBatch.getExpdate() != null ? authHandler.getDateFormatter(request).format(productBatch.getExpdate()) : "");
        obj.put("quantity", productBatch.getQuantity());
        obj.put("balance", productBatch.getBalance());
        obj.put("asset", productBatch.getAsset());
        obj.put("expstart", "");
        obj.put("expend", "");

        return obj;
    }

    public ModelAndView hasAssetDepreciationPostedUnderAssetGroup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject jobject = hasAssetDepreciationPostedUnderAssetGroup(request);
            jobj.put(Constants.RES_data, jobject);
            issuccess = true;
        } catch (JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private JSONObject hasAssetDepreciationPostedUnderAssetGroup(HttpServletRequest request) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String companyId = sessionHandlerImpl.getCompanyid(request);
        String productId = request.getParameter("productId");

        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.companyKey, companyId);

        if (!StringUtil.isNullOrEmpty(productId)) {
            requestParams.put("productId", productId);
        }
        try {
            KwlReturnObject result = accProductObj.getAssetDepreciationDetail(requestParams);

            boolean hasAssetDepreciationPostedUnderAssetGroup = false;
            List list = result.getEntityList();
            if ((list != null) && !list.isEmpty()) {
                hasAssetDepreciationPostedUnderAssetGroup = true;
            }
            jobj.put("hasAssetDepreciationPostedUnderAssetGroup", hasAssetDepreciationPostedUnderAssetGroup);
        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView getProductsByCategory(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put("userid", sessionHandlerImpl.getUserid(request));
        jobj = accProductModuleService.getProductsByCategory(paramJobj);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getProductsForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            /**
             * This Function will use when Users Visibility Feature is Enable
             * Append user condition while querying data
             */
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", sessionHandlerImpl.getCompanyid(request));
            if (extraPref != null && extraPref.isUsersVisibilityFlow()) {
                KwlReturnObject object = accountingHandlerDAOobj.getObject(User.class.getName(), sessionHandlerImpl.getUserid(request));
                User user = object.getEntityList().size() > 0 ? (User) object.getEntityList().get(0) : null;
                if (!AccountingManager.isCompanyAdmin(user)) {
                    /**
                     * if Users visibility enable and current user is not admin
                     */
                    Map<String, Object> reqMap = new HashMap();
                    requestParams.put("isUserVisibilityFlow", true);
                    reqMap.put("companyid", sessionHandlerImpl.getCompanyid(request));
                    reqMap.put("userid", sessionHandlerImpl.getUserid(request));
                    reqMap.put("jointable", "pcd");
                    reqMap.put("moduleid", Constants.Acc_Product_Master_ModuleId);                    
                    String custcondition = fieldManagerDAOobj.appendUsersCondition(reqMap);
                    if (!StringUtil.isNullOrEmpty(custcondition)) {
                        /**
                         * If mapping found with dimension
                         */

                        String usercondition = " and (" + custcondition + ")";
                        requestParams.put("appendusercondtion", usercondition);
                    } else {
                        /**
                         * If no Mapping found for current ser then return
                         * function call
                         */
                        jobj.put(Constants.RES_success, true);
                        jobj.put(Constants.RES_msg, msg);
                        jobj.put(Constants.RES_data, new com.krawler.utils.json.JSONArray());
                        jobj.put(Constants.RES_TOTALCOUNT, 0);
                        return new ModelAndView("jsonView", "model", jobj.toString());
                    }
                }
            }
            requestParams.put(Constants.PRODUCT_SEARCH_FLAG, (extraPref != null) ? extraPref.getProductSearchingFlag() : Constants.PRODUCT_SEARCH_ANYWHERE);
            KwlReturnObject result = accProductObj.getProductsForCombo(requestParams);
            List list = result.getEntityList();

            JSONArray DataJArr = getProductsJsonForCombo(request, list);
            jobj.put(Constants.RES_data, DataJArr);
            jobj.put(Constants.RES_TOTALCOUNT, result.getRecordTotalCount());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    // while changing in this method do same changes in 'getConfiguredPriceOfProduct' method of 'accInvoiceCMN.java' file.
    public ModelAndView getIndividualProductPrice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);            
            jobj = accProductModuleService.getIndividualProductPrice(paramJobj);
            if (jobj.has(Constants.RES_success)) {
                issuccess = jobj.optBoolean(Constants.RES_success, false);
            }
            if (jobj.has(Constants.RES_msg)) {
                msg = jobj.optString(Constants.RES_msg, "");
                            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getRichTextArea(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();                
        boolean issuccess = false;
        String msg = "",fieldValue="";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String fieldName="",productId="";     
            if (params.has("fieldName") && params.get("fieldName") != null) {
                fieldName = String.valueOf(params.get("fieldName")).replace("Custom_", "");
            }
            if (params.has("productid") && params.get("productid") != null) {
                productId = String.valueOf(params.get("productid"));
            }
            HashMap<String, Object> richTextAreaParams = new HashMap<String, Object>();
            richTextAreaParams.put("fieldname", fieldName);
            richTextAreaParams.put("productId", productId);
            richTextAreaParams.put("companyid", companyid);
            fieldValue=fieldDataManagercntrl.getRichTextAreaForProduct(richTextAreaParams);   
            jobj.put(Constants.RES_data, fieldValue);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getProductAvailableQuantiyInSelectedUOM(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject obj = new JSONObject();
            String productId = (String) request.getParameter("productId");
            String currentuomid = (String) request.getParameter("currentuomid");
            // Get Available Quantity of Product For Selected UOM
            KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(productId, currentuomid);
            double availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);

            qtyResult = accProductObj.getLockQuantityInSelectedUOM(productId, currentuomid);
            double lockQuantityInSelectedUOM = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);

            obj.put("availableQtyInSelectedUOM", availableQuantity);
            obj.put("lockQuantityInSelectedUOM", lockQuantityInSelectedUOM);
            jobj.append(Constants.RES_data, obj);

            issuccess = true;
        }catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getCustomFieldHistoryForProduct(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            JSONArray dataJson = getCustomFieldHistoryForProduct(request);
            issuccess = true;
            jobj.put(Constants.RES_data, dataJson);
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getCustomFieldHistoryForProduct(HttpServletRequest request) {
        JSONArray jarr = new JSONArray();
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String fieldId = request.getParameter("fieldId");
            String productId = request.getParameter("productId");
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("fieldId", fieldId);
            requestParams.put("productId", productId);
            KwlReturnObject returnObject = accProductObj.getCustomFieldHistoryForProduct(requestParams);
            List list = returnObject.getEntityList();
            for (int i = 0; i < list.size(); i++) {
                String productCustomFieldHistoryId = (String) list.get(i);
                ProductCustomFieldHistory customFieldHistory = (ProductCustomFieldHistory) kwlCommonTablesDAOObj.getClassObject(ProductCustomFieldHistory.class.getName(), productCustomFieldHistoryId);
                if (customFieldHistory != null) {
                    JSONObject jobj = new JSONObject();
                    jobj.put("fieldLabel", (customFieldHistory.getFieldParams() != null) ? customFieldHistory.getFieldParams().getFieldlabel() : "");
                    jobj.put("fieldValue", customFieldHistory.getValue());
                    jobj.put("applyDate", df.format(customFieldHistory.getApplyDate()));
                    Date creationDate = customFieldHistory.getCreationDate();
                    if (creationDate != null) {
                        jobj.put("creationDate", df.format(creationDate));
                    }
                    String userName = customFieldHistory.getUser() != null ? customFieldHistory.getUser().getFirstName() : "";
                    jobj.put("creator", userName);
                    jarr.put(jobj);
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jarr;
    }

    public ModelAndView maintainCustomFieldHistoryForProduct(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            maintainCustomFieldHistoryForProduct(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.custom.field.saved.success", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public Product maintainCustomFieldHistoryForProduct(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        String productId = request.getParameter("productId");
        String fieldId = request.getParameter("fieldId");
        String value = request.getParameter("value");
        String applyDateString = request.getParameter("applyDate");
        String creationDateString = request.getParameter("creationDate");
        String loginId = sessionHandlerImpl.getUserid(request);
        String moduleIdString = request.getParameter("moduleId");
        int moduleId = 0;
        if (!StringUtil.isNullOrEmpty(moduleIdString)) {
            moduleId = Integer.parseInt(moduleIdString);
        }
        Date applyDate = null;
        Date creationDate = null;
        if (!StringUtil.isNullOrEmpty(applyDateString)) {
            try {
                applyDate = authHandler.getDateOnlyFormat(request).parse(applyDateString);
            } catch (ParseException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (!StringUtil.isNullOrEmpty(creationDateString)) {
            try {
                creationDate = authHandler.getDateOnlyFormat(request).parse(creationDateString);
            } catch (ParseException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("productId", productId);
        requestParams.put("fieldId", fieldId);
        requestParams.put("value", value);
        requestParams.put("applyDate", applyDate);
        requestParams.put("creationDate", creationDate);
        requestParams.put("loginId", loginId);
        requestParams.put("moduleId", moduleId);
        KwlReturnObject fieldReturnObject = accProductObj.getCustomFieldHistoryForProduct(requestParams);
        List list = fieldReturnObject.getEntityList();
        if (list.size() > 0) {
            accProductObj.deleteCustomFieldHistoryForProduct(requestParams);
        }
        KwlReturnObject returnObject = accProductObj.maintainCustomFieldHistoryForProduct(requestParams);
        ProductCustomFieldHistory customFieldHistory = (ProductCustomFieldHistory) returnObject.getEntityList().get(0);
        Product product = null;
        if (customFieldHistory != null) {
            product = customFieldHistory.getProduct();
        }

        SimpleDateFormat sdf = new SimpleDateFormat(Constants.MMMMdyyyy);
        Date currDate = new Date();
        try {
            applyDate = sdf.parse(sdf.format(applyDate));
            currDate = sdf.parse(sdf.format(currDate));
        } catch (ParseException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (applyDate.equals(currDate)) {// we will update here AccProductCustomData values only in this condition
            saveCustomFieldData(request);
        }
        return product;
    }

    public void maintainCustomFieldHistoryForProduct(HttpServletRequest request, HashMap<String, Object> customrequestParams) {
        accProductModuleService.maintainCustomFieldHistoryForProduct(request, customrequestParams);
    }

    public void saveCustomFieldData(HttpServletRequest request) {
        String customfield = request.getParameter(Constants.customfield);
        if (!StringUtil.isNullOrEmpty(customfield)) {
            try {
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                String productId = request.getParameter("productId");
                customrequestParams = AccountingManager.getGlobalParams(request);
                JSONArray jcustomarray = new JSONArray(customfield);
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_Product_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_Productid);
                customrequestParams.put("modulerecid", productId);
                customrequestParams.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                customrequestParams.put(Constants.Acc_id, productId);
                customrequestParams.put("customdataclasspath", Constants.Acc_Product_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            } catch (ServiceException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SessionExpiredException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setUpdatedValueOfCustomField(Map<String, String> replaceFieldMap, HashMap<String, Object> basicParams, String latestValue) {
        try {
            SimpleDateFormat df = new SimpleDateFormat(Constants.MMMMdyyyy);
            Date currDate = df.parse(df.format(new Date()));
            if (basicParams.get("transactionDate") != null) {
                Date transactionDate = (Date) basicParams.get("transactionDate");
                transactionDate = df.parse(df.format(transactionDate));
                if (transactionDate.equals(currDate)) {// custom field value for product will be update only if transaction date and current dates are same. because i am updating custom field value when user is viewing data so that adavance search can work properly regarding to updated values.
                    String fieldKey = (String) basicParams.get("fieldKey");
                    String fieldIdData = replaceFieldMap.get(fieldKey);
                    String[] fieldIdDataArray = fieldIdData.split("_");
                    String fieldId = fieldIdDataArray[1];
                    String productId = (String) basicParams.get("productId");
                    String companyId = (String) basicParams.get("companyId");
                    FieldParams fieldParams = (FieldParams) kwlCommonTablesDAOObj.getClassObject(FieldParams.class.getName(), fieldId);
                    JSONObject jobj = new JSONObject();
                    jobj.put("refcolumn_name", "Col" + fieldParams.getRefcolnum());
                    jobj.put("fieldname", fieldParams.getFieldname());
                    jobj.put("xtype", fieldParams.getFieldtype());
                    jobj.put("Col" + fieldParams.getColnum(), latestValue);
                    jobj.put(fieldParams.getFieldname(), "Col" + fieldParams.getColnum());
                    JSONArray jarr = new JSONArray();
                    jarr.put(jobj);

                    // saving values

                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jarr);
                    customrequestParams.put("modulename", Constants.Acc_Product_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_Productid);
                    customrequestParams.put("modulerecid", productId);
                    customrequestParams.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                    customrequestParams.put(Constants.Acc_id, productId);
                    customrequestParams.put(Constants.companyKey, companyId);
                    customrequestParams.put("customdataclasspath", Constants.Acc_Product_custom_data_classpath);
                    if (fieldParams.getFieldtype() == 1 || fieldParams.getFieldtype() == 2) {
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    }
                }
            }

        } catch (ParseException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Object getProductCustomFieldValue(String fieldId, String productId, String companyId, Date transactionDate) {
        Object returnObject = null;
        try {
            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
            customrequestParams.put("fieldId", fieldId);
            customrequestParams.put("productId", productId);
            customrequestParams.put("companyId", companyId);
            customrequestParams.put("transactionDate", transactionDate);
            FieldParams fieldParams = (FieldParams) kwlCommonTablesDAOObj.getClassObject(FieldParams.class.getName(), fieldId);
            if (fieldParams != null && fieldParams.getFieldtype() == 1 || fieldParams.getFieldtype() == 2) {
                KwlReturnObject result = accProductObj.getProductCustomFieldValue(customrequestParams);

                List list = result.getEntityList();
                Iterator itr = list.iterator();
                if (itr.hasNext()) {
                    returnObject = itr.next();
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnObject;
    }

    public ModelAndView getIndividualProductSalesPurchasePrices(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobj1 = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String productId = request.getParameter("productId");
            KwlReturnObject purchase = accProductObj.getProductPrice(productId, true, null, "", "");
            jobj1.put("purchaseprice", purchase.getEntityList().get(0) == null ? 0 : purchase.getEntityList().get(0));
            KwlReturnObject sales = accProductObj.getProductPrice(productId, false, null, "", "");
            jobj1.put("saleprice", sales.getEntityList().get(0) == null ? 0 : sales.getEntityList().get(0));
            jobj.append(Constants.RES_data, jobj1);

            issuccess = true;
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /*
        Function to get products mapped in work order
    */
    public Map<String, Object> getWOProductsCombo(Map<String, Object> requestParams) throws JSONException, ServiceException, SessionExpiredException {

        /* These method written for MRP .
         * The map is used to store the product ids which are mapped in work order
         */
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> mapStore = new HashMap<>();
       
        KwlReturnObject kwlObj = accProductObj.getWorkOrderProductsCombo(requestParams);
        List productList = kwlObj.getEntityList();
        Iterator ittr = productList.iterator();
        while (ittr.hasNext()) {
            String pid = (String) ittr.next();
            mapStore.put(pid, pid);
        }

        return mapStore;
    }
    
    public JSONArray getProductsJsonForCombo(HttpServletRequest request, List list) throws JSONException, ServiceException, SessionExpiredException {
         Iterator itr = list.iterator();
        JSONArray jArr = new JSONArray();
        Producttype producttype = new Producttype();
        String productid = request.getParameter(Constants.productid);
        Boolean onlyProduct = Boolean.parseBoolean((String) request.getParameter("onlyProduct"));
        Boolean nonSaleInventory = Boolean.parseBoolean((String) request.getParameter("loadInventory"));
	boolean isAssemblyType = request.getParameter("isAssemblyType")!=null ? Boolean.parseBoolean(request.getParameter("isAssemblyType")) : false;  //Check from Product Assembly Form. Do not allow service type of Product in BOM.
        
        Boolean isSettingNewPrice = StringUtil.isNullOrEmpty(request.getParameter("isSettingNewPrice"))? false:Boolean.parseBoolean((String) request.getParameter("isSettingNewPrice"));
        String companyid = sessionHandlerImpl.getCompanyid(request);
        boolean isMRPActivated = accCompanyPreferencesObj.isMRPModuleActivated(companyid);
        /* If isMRPActivated and work centre id found then put values in map */
        Map<String, Object> mapStoreWorkOrderProducts = new HashMap<>();
        
        /*
         COndition to check if MRP is activated for company and if there is request to check products mapped in work order
        */
        
        if (isMRPActivated) {
            boolean isProductUsedinWorkOrder = StringUtil.isNullOrEmpty(request.getParameter("isProductUsedInWorkOrder")) ? false : Boolean.parseBoolean(request.getParameter("isProductUsedInWorkOrder").toString());

            if (isProductUsedinWorkOrder && !StringUtil.isNullOrEmpty(request.getParameter("workcenterid"))) {
                Map<String, Object> reqParams = new HashMap<>();
                reqParams.put("companyid", companyid);
                reqParams.put("workcenterid", request.getParameter("workcenterid"));
                mapStoreWorkOrderProducts = getWOProductsCombo(reqParams);
            }
        }
         
        if (isSettingNewPrice) {
            while (itr.hasNext()) {
                Product product = (Product) itr.next();
                
                JSONObject obj = new JSONObject();
                obj.put(Constants.productid, product.getID());
                obj.put("productname", product.getName());
                obj.put("pid", product.getProductid());
                obj.put("barcodetype", product.getBarcodefield());  //ERM-304
                String type = "";
                if (product.getProducttype() != null) {
                    if (storageHandlerImpl.GetVRnetCompanyId().contains(companyid)) {
                        if (StringUtil.equal(product.getProducttype().getName(), "Inventory Assembly")) {
                            type = "Inventory Bundle";
                        } else {
                            type = product.getProducttype().getName();
                        }
                    } else {
                        type = product.getProducttype().getName();
                    }
                    if (isAssemblyType && product.getProducttype().getName().equals("Service")) {   //ERP-21517
                        continue;   //Do not load Service Type of Product in BOM when create new Assembly Product.
                    }
                }
                if (product.isAsset()) {   //For Fixed Asset Group, type will be "Asset"
                    obj.put("type", "Asset");
                } else {
                    obj.put("type", type);
                }
                jArr.put(obj);
            }
        } else {
            while (itr.hasNext()) {
                Product product = (Product) itr.next();
                if (product.getID().equals(productid)) {
                    continue;
                }
                JSONObject obj = new JSONObject();
                String productType = "";
                productType = (product.getProducttype() != null ? product.getProducttype().getName() : "");
                if (nonSaleInventory && productType.equals(producttype.Inventory_Non_Sales)) {
                    continue;
                }
                if (isAssemblyType && productType.equals("Service")) {   //ERP-21517
                    continue;   //Do not load Service Type of Product in BOM when create new Assembly Product.
                }
                obj.put(Constants.productid, product.getID());
                
                /*If map contains product then hasAccess is false otherwise true*/
                if(mapStoreWorkOrderProducts.containsKey(product.getID())){
                    obj.put("hasAccess", false);
                }
                else
                {
                    obj.put("hasAccess", product.isIsActive());    
                }

                obj.put("productname", product.getName());
                obj.put("barcodetype", product.getBarcodefield());  //ERM-304
                obj.put("desc", product.getDescription());
                obj.put("isAsset", product.isAsset());
                obj.put("supplierpartnumber", StringUtil.isNullOrEmpty(product.getSupplier()) ? "" : product.getSupplier());
                obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
                obj.put("minorderingquantity", product.getMinOrderingQuantity());
                obj.put("maxorderingquantity", product.getMaxOrderingQuantity());
                obj.put("reorderQuantity", product.getReorderQuantity());
                obj.put("uomschematypeid", product.getUomSchemaType() != null ? product.getUomSchemaType().getID() : "");
                if (!onlyProduct) {
                    UnitOfMeasure uom = product.getUnitOfMeasure();
                    UnitOfMeasure displayUoM = product.getDisplayUoM();
                    UnitOfMeasure purchaseuom = product.getPurchaseUOM();
                    UnitOfMeasure salesuom = product.getSalesUOM();
                    UnitOfMeasure orderingUoM = product.getOrderingUOM();
                    UnitOfMeasure transferingUoM = product.getTransferUOM();
                    obj.put("uomid", uom == null ? "" : uom.getID());
                    obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
                    obj.put("displayUoMid", displayUoM == null ? "" : displayUoM.getID());
                    obj.put("displayUoMName", displayUoM == null ? "" : displayUoM.getNameEmptyforNA());
                    obj.put("purchaseuom", purchaseuom == null ? "" : purchaseuom.getID());
                    obj.put("salesuom", salesuom == null ? "" : salesuom.getID());
                    obj.put("salesuomname", salesuom == null ? "" : salesuom.getNameEmptyforNA());
                    obj.put("purchaseuomname", purchaseuom == null ? "" : purchaseuom.getNameEmptyforNA());
                    obj.put("orderinguomname", orderingUoM == null ? "" : orderingUoM.getNameEmptyforNA());
                    obj.put("orderinguomid", orderingUoM == null ? "" : orderingUoM.getID());
                    obj.put("transferinguomname", transferingUoM == null ? "" : transferingUoM.getNameEmptyforNA());
                    obj.put("transferinguomid", transferingUoM == null ? "" : transferingUoM.getID());
                    obj.put("stockpurchaseuomvalue", (product.getPackaging() != null && purchaseuom != null) ? product.getPackaging().getStockUomQtyFactor(purchaseuom) : 1);
                    obj.put("stocksalesuomvalue", (product.getPackaging() != null && salesuom != null) ? product.getPackaging().getStockUomQtyFactor(salesuom) : 1);
                    obj.put("multiuom", product.isMultiuom());
                    obj.put("isLocationForProduct", product.isIslocationforproduct());
                    obj.put("isWarehouseForProduct", product.isIswarehouseforproduct());
                    obj.put("isRowForProduct", product.isIsrowforproduct());
                    obj.put("isRackForProduct", product.isIsrackforproduct());
                    obj.put("isBinForProduct", product.isIsbinforproduct());
                    obj.put("isBatchForProduct", product.isIsBatchForProduct());
                    obj.put("isSerialForProduct", product.isIsSerialForProduct());
                    obj.put("isSKUForProduct", product.isIsSKUForProduct());
                    obj.put("isRecyclable", product.isRecyclable());
                    obj.put("recycleQuantity", product.getRecycleQuantity());
                    obj.put("isWastageApplicable", product.isWastageApplicable());
                    obj.put("purchaseacctaxcode", ((product.getPurchaseAccount() != null && (!StringUtil.isNullOrEmpty(product.getPurchaseAccount().getTaxid()))) ? product.getPurchaseAccount().getTaxid() : ""));
                    obj.put("salesacctaxcode", ((product.getSalesAccount() != null && (!StringUtil.isNullOrEmpty(product.getSalesAccount().getTaxid()))) ? product.getSalesAccount().getTaxid() : ""));
                    obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : ""));
                    obj.put("salesretaccountid", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getID() : ""));
                    obj.put("location", (product.getLocation() != null ? product.getLocation().getId() : ""));
                    obj.put("warehouse", (product.getWarehouse() != null ? product.getWarehouse().getId() : ""));
                    KwlReturnObject purchase = accProductObj.getProductPrice(product.getID(), true, null, "", "");
                    obj.put("purchaseprice", purchase.getEntityList().get(0));
                    KwlReturnObject sales = accProductObj.getProductPrice(product.getID(), false, null, "", "");
                    obj.put("saleprice", sales.getEntityList().get(0));

                    KwlReturnObject result = accProductObj.getQuantity(product.getID());
                    obj.put("quantity", (result.getEntityList().get(0) == null ? 0 : result.getEntityList().get(0)));

                    KwlReturnObject result2 = accProductObj.getAssemblyLockQuantity(product.getID());//get the lock quantity of assembly type of product locked in SO
                    Double assmblyLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));

                    KwlReturnObject result1 = accProductObj.getLockQuantity(product.getID());
                    Double SoLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));

                    obj.put("lockquantity", assmblyLockQuantity + SoLockQuantity);

                    KwlReturnObject result4 = accProductObj.getVendorConsignedQuantity(product.getID());
                    obj.put("venconsignuomquantity", (result4.getEntityList().get(0) == null ? 0 : result4.getEntityList().get(0)));

                    KwlReturnObject result5 = accProductObj.getConsignedQuantity(product.getID());
                    obj.put("consignquantity", (result5.getEntityList().get(0) == null ? 0 : result5.getEntityList().get(0)));

                }
                obj.put("rcmapplicable", product.isRcmApplicable());
                obj.put("shelfLocation", (product.getShelfLocation() != null ? product.getShelfLocation().getShelfLocationValue() : ""));
                obj.put("location", (product.getLocation() != null ? product.getLocation().getId() : ""));
                obj.put("warehouse", (product.getWarehouse() != null ? product.getWarehouse().getId() : ""));
                String type = "";
                if (product.getProducttype() != null) {
                    if (storageHandlerImpl.GetVRnetCompanyId().contains(companyid)) {
                        if (StringUtil.equal(product.getProducttype().getName(), "Inventory Assembly")) {
                            type = "Inventory Bundle";
                        } else {
                            type = product.getProducttype().getName();
                        }
                    } else {
                        type = product.getProducttype().getName();
                    }
                }
                if (product.isAsset()) {   //For Fixed Asset Group, type will be "Asset"
                    obj.put("type", "Asset");
                } else {
                    obj.put("type", type);
                }
                obj.put("pid", product.getProductid());

                double orderToStockUOMFactor = 1;
                double transferToStockUOMFactor = 1;
                String packaging = "";
                Packaging prodPackaging = product.getPackaging();
                if (prodPackaging != null) {
                    orderToStockUOMFactor = prodPackaging.getStockUomQtyFactor(product.getOrderingUOM());
                    transferToStockUOMFactor = prodPackaging.getStockUomQtyFactor(product.getTransferUOM());
                    packaging = prodPackaging.toString();
                }
                obj.put("orderToStockUOMFactor", orderToStockUOMFactor);
                obj.put("transferToStockUOMFactor", transferToStockUOMFactor);
                obj.put("packaging", packaging);
                obj.put("packagingid", prodPackaging != null ? prodPackaging.getId() : "");
                obj.put("uomschematype", product.getUomSchemaType()!=null?product.getUomSchemaType().getID():"");
                obj.put("ismultipleuom", product.isMultiuom());
                obj.put("productweightperstockuom",  product.getProductWeightPerStockUom());
                obj.put("productweightincludingpakagingperstockuom", product.getProductWeightIncludingPakagingPerStockUom());
                obj.put("productvolumeperstockuom",product.getProductVolumePerStockUom());
                obj.put("productvolumeincludingpakagingperstockuom",product.getProductVolumeIncludingPakagingPerStockUom());
                obj.put("hsncode",!StringUtil.isNullOrEmpty(product.getHSNCode())?product.getHSNCode():"");

                jArr.put(obj);
            }
        }
        return jArr;
    }
    public ModelAndView getProductsByType(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            List purchasePrice = new ArrayList();
            List salesPrice = new ArrayList();
            List<Object[]> selectedProductList = new ArrayList();
            String selectedProductIds = request.getParameter("combovalue");
            boolean isBuild = false;
            if (request.getParameter("isBuild") != null) {
                isBuild = Boolean.parseBoolean(request.getParameter("isBuild"));
            }
                
            requestParams.put("type", request.getParameter("type"));
            requestParams.put("isBuild", isBuild);
            requestParams.put("ss", request.getParameter("query"));
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put("limit", request.getParameter("limit"));
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            
            /**
             * Block used to get selected products using their ids.
             */
            if (!StringUtil.isNullOrEmpty(selectedProductIds) && !selectedProductIds.equals("All")) {
                requestParams.put("selectedProductIds", selectedProductIds);
                requestParams.put("isMultiSelectProductidsFlag", true);
                selectedProductList = accProductObj.getProductsByType(requestParams).getEntityList();
                requestParams.remove("isMultiSelectProductidsFlag");
            }
            
            KwlReturnObject result = accProductObj.getProductsByType(requestParams);
            List <Object[]> list= result.getEntityList();
            
            /**
             * adding list of selected products.
             */
            selectedProductList.addAll(list);
            
            for (Object[] row : selectedProductList) {
                Product product = (Product) row[0];
                KwlReturnObject purchase = accProductObj.getProductPrice(product.getID(), true, null, "", "");
                purchasePrice.add(purchase.getEntityList().get(0));
                KwlReturnObject sales = accProductObj.getProductPrice(product.getID(), false, null, "", "");
                salesPrice.add(sales.getEntityList().get(0));
            }

            jobj = getProductsByTypeJson(request, selectedProductList, purchasePrice, salesPrice);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getProductsByTypeJson(HttpServletRequest request, List list, List purchaseprice, List salesPrice) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArr = new JSONArray();
            Iterator itr = list.iterator();
            Iterator iteratorPurchase = purchaseprice.iterator();
            Iterator iteratorSales = salesPrice.iterator();
            int index = 0;

            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject kwlReturnObject = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) kwlReturnObject.getEntityList().get(0);


            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Product product = (Product) row[0];
                JSONObject obj = new JSONObject();
                obj.put(Constants.productid, product.getID());
                obj.put("pid", product.getProductid());
                obj.put("productname", product.getName());
                obj.put("desc", product.getDescription());
                obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
                obj.put("type", (product.getProducttype() != null ? product.getProducttype().getName() : ""));
                obj.put("quantity", (row[1] == null ? 0 : row[1]));
                obj.put("purchaseprice", purchaseprice.get(index));
                obj.put("salesprice", salesPrice.get(index++));
                obj.put("isLocationForProduct", product.isIslocationforproduct());
                obj.put("isWarehouseForProduct", product.isIswarehouseforproduct());
                obj.put("isBatchForProduct", product.isIsBatchForProduct());
                obj.put("isSerialForProduct", product.isIsSerialForProduct());
                obj.put("isSKUForProduct", product.isIsSKUForProduct());
                obj.put("isRowForProduct", product.isIsrowforproduct());
                obj.put("isRackForProduct", product.isIsrackforproduct());
                obj.put("isBinForProduct", product.isIsbinforproduct());

                if (preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || product.isIslocationforproduct() || product.isIswarehouseforproduct()) {
                    obj.put("location", product.getLocation() != null ? product.getLocation().getId() : "");
                        obj.put("warehouse",  product.getWarehouse() != null ?  product.getWarehouse().getId() : "");
                }
                jArr.put(obj);
            }

            jobj.put(Constants.RES_data, jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getProductsByTypeJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView getSuggestedReorderProducts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            requestParams.remove(Constants.start);
            requestParams.remove(Constants.limit);
            KwlReturnObject result = accProductObj.getSuggestedReorderProducts(requestParams);
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            JSONArray DataJArr = accProductModuleService.getProductsJson(paramJobj, result.getEntityList());
            JSONArray pagedJson = DataJArr;
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.start)) && !StringUtil.isNullOrEmpty(request.getParameter(Constants.limit))) {
                pagedJson = StringUtil.getPagedJSON(DataJArr, Integer.parseInt(request.getParameter(Constants.start)), Integer.parseInt(request.getParameter(Constants.limit)));
            }
            jobj.put(Constants.RES_data, pagedJson);
            jobj.put(Constants.RES_TOTALCOUNT,DataJArr.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

//Product Price
    public ModelAndView getProductPrice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(Constants.start, (StringUtil.isNullOrEmpty(request.getParameter(Constants.start))) ? "0" : request.getParameter(Constants.start));
            requestParams.put(Constants.limit, (StringUtil.isNullOrEmpty(request.getParameter(Constants.limit))) ? "30" : request.getParameter(Constants.limit));
            requestParams.put(Constants.ss, (StringUtil.isNullOrEmpty(request.getParameter(Constants.ss))) ? "" : request.getParameter(Constants.ss));
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                requestParams.put("dir", request.getParameter("dir"));
                requestParams.put("sort", request.getParameter("sort"));
            }
            requestParams.put(Constants.productid, (StringUtil.isNullOrEmpty(request.getParameter(Constants.productid))) ? "" : request.getParameter(Constants.productid));
            requestParams.put("productPriceinMultipleCurrency", Boolean.FALSE.parseBoolean(request.getParameter("productPriceinMultipleCurrency")));

            KwlReturnObject result = accProductObj.getPrice(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getPriceListJson(request, list);
            jobj.put(Constants.RES_data, DataJArr);
            jobj.put("count", count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accProductController.getProductPrice : " + ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getPriceListJson(HttpServletRequest request, List list) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
        String companyId = sessionHandlerImpl.getCompanyid(request);
        try {
            Iterator itr = list.iterator();
            DateFormat df = authHandler.getDateOnlyFormat(request);
              boolean isExport=request.getAttribute("isExport")!=null? (Boolean)request.getAttribute("isExport"): false; 
            while (itr.hasNext()) {
                PriceList price = (PriceList) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("priceid", price.getID());
                obj.put("applydate", df.format(price.getApplyDate()));
                if (isExport) {
                    obj.put("carryin", price.isCarryIn() ? "Purchase Price" : "Sales Price");
                } else {
                    obj.put("carryin", price.isCarryIn());
                }
                obj.put("price", authHandler.formattingDecimalForUnitPrice(price.getPrice(),companyId));
                obj.put("currency", price.getCurrency() != null ? price.getCurrency().getName() : "-");
                obj.put(Constants.productid, (price.getProduct() != null) ? price.getProduct().getProductid() : "");
                obj.put("productuuid", (price.getProduct() != null) ? price.getProduct().getID() : "");
                //added uom column in price list report
                obj.put("uomname", (price.getUomid() != null) ? price.getUomid().getName():(price.getProduct().getUnitOfMeasure()!=null)?price.getProduct().getUnitOfMeasure().getName(): "");
                obj.put("uomid", (price.getUomid() != null) ? price.getUomid().getID():(price.getProduct().getUnitOfMeasure()!=null)?price.getProduct().getUnitOfMeasure().getID(): "");
                obj.put(Constants.currencyKey, (price.getCurrency() != null) ? price.getCurrency().getCurrencyID() : "");
                obj.put("productName", (price.getProduct() != null) ? price.getProduct().getProductName() : "");
                obj.put("productDesc", (price.getProduct() != null) ? price.getProduct().getDescription() : "");
                obj.put("initialPrice", price.isInitialPrice());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getPriceListJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView setNewPrice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        String channelName="/ProductsDetails/gridAutoRefresh";//used for refreshing product and services grid after setting new price
        boolean dateexist = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            dateexist = setNewPrice(request);
            jobj.put("dateexist", dateexist);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.prod.priceapp", null, RequestContextUtils.getLocale(request));   //"New Price has been applied successfully";

            //*****************************************Propagate Product In child companies**************************
            boolean propagateTOChildCompaniesFalg = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("ispropagatetochildcompanyflag"))) {
                propagateTOChildCompaniesFalg = Boolean.parseBoolean(request.getParameter("ispropagatetochildcompanyflag"));
            }
            if (propagateTOChildCompaniesFalg) {
                try {
                    String parentcompanyid = sessionHandlerImpl.getCompanyid(request);
                    List childCompaniesList = companyDetailsDAOObj.getChildCompanies(parentcompanyid);
                    for (Object childObj : childCompaniesList) {
                        try {
                            status = txnManager.getTransaction(def);
                            Object[] childdataOBj = (Object[]) childObj;
                            String childCompanyID = (String) childdataOBj[0];
                            String childCompanyName = (String) childdataOBj[1];
                            propagatePriceListInChildCompanies(request,childCompanyID,parentcompanyid,childCompanyName);
                            txnManager.commit(status);
                        } catch (Exception ex) {
                            txnManager.rollback(status);
                        }
                    }
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                    }
                }
            }
            //*****************************************Propagate Product price list In child companies**************************
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                if (issuccess && !StringUtil.isNullOrEmpty(channelName) && !dateexist) {
                    ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
                }
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public boolean setNewPrice(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        KwlReturnObject priceresult;
        try {
            boolean updatePrice = Boolean.parseBoolean(request.getParameter("changeprice"));
            String productids = request.getParameter(Constants.productid);
            String currencyid = request.getParameter(Constants.currencyKey);
//            String uomids = request.getParameter("uomid");
            String uomid = request.getParameter("uomid");
            String priceid = (request.getParameter("priceid") != null) ? request.getParameter("priceid") : "";
            boolean isEdit = (StringUtil.isNullOrEmpty(request.getParameter("isEdit"))) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            boolean isFromAsset = (StringUtil.isNullOrEmpty(request.getParameter("isFromAsset"))) ? false : Boolean.parseBoolean(request.getParameter("isFromAsset"));
            if (StringUtil.isNullOrEmpty(currencyid)) {
                currencyid = sessionHandlerImpl.getCurrencyID(request);
            }
            String productidsarr[] = productids.split(",");
            for (int i = 0; i < productidsarr.length; i++) {
                String productid = productidsarr[i];
                String companyid = sessionHandlerImpl.getCompanyid(request);
                boolean carryIn = Boolean.parseBoolean(request.getParameter("carryin"));
                boolean initialPrice = Boolean.parseBoolean(request.getParameter("initialPrice"));
                String stockUOMId = "";
                if (StringUtil.isNullOrEmpty(uomid)) {
                    Map<String, Object> prefparams = new HashMap();
                    prefparams.put("id", productid);
                    Object stockUOM = kwlCommonTablesDAOObj.getRequestedObjectFields(Product.class, new String[]{"unitOfMeasure"}, prefparams);
                    if (stockUOM != null && stockUOM instanceof UnitOfMeasure) {
                        UnitOfMeasure unitOfMeasure = (UnitOfMeasure) stockUOM;
                        stockUOMId = unitOfMeasure.getID();
                    } else if (isFromAsset) {
                        Map<String, Object> prefparamsForNA = new HashMap();
                        prefparamsForNA.put("name", "N/A");
                        prefparamsForNA.put("company.companyID", companyid);
                        Object NAUOM = kwlCommonTablesDAOObj.getRequestedObjectFields(UnitOfMeasure.class, new String[]{"id"}, prefparamsForNA);
                        if (NAUOM != null && NAUOM instanceof UnitOfMeasure) {
                            UnitOfMeasure unitOfMeasure = (UnitOfMeasure) NAUOM;
                            stockUOMId = unitOfMeasure.getID();
                        }
                    }
                }
                        
                String affecteduser = carryIn ? request.getParameter("vendor") : request.getParameter("customer");
                Date appDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("applydate"));
                double newprice = Double.parseDouble(request.getParameter("price"));
                if (StringUtil.isNullOrEmpty(affecteduser)) {
                    affecteduser = "-1";
                }

                HashMap<String, Object> priceMap = new HashMap<>();
                List list = null;

                HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                requestParams.put(Constants.productid, productid);
                requestParams.put("carryin", carryIn);
                if (!(carryIn && initialPrice)) {
                    /**
                     * At a time, only one pricelist entry can be set as initial
                     * price. Set all other entries on pricelist as non-initial
                     * price for product. For initial purchase price don't send
                     * applydate for fetching initial purchase price.
                     */
                    requestParams.put("applydate", appDate);
                }
                requestParams.put("price", newprice);
                requestParams.put("initialPrice", initialPrice);
                requestParams.put("affecteduser", affecteduser);
                requestParams.put(Constants.currencyKey, currencyid);
                /**
                 * update price in price list.
                 */
                if (isEdit && !StringUtil.isNullOrEmpty(priceid) && updatePrice) {
                    requestParams.put("priceid", priceid);
                    /**
                     * Checking entry present for applicable date and stock uom.
                     */
//                    KwlReturnObject priceresultwithnullUOM = accProductObj.getPriceListEntry(requestParams);
//                    list = priceresultwithnullUOM.getEntityList();
//                    if (list.isEmpty()) {
                    if (!StringUtil.isNullOrEmpty(uomid)) {
                        requestParams.put("uomid", uomid);
                    }else{
                        requestParams.put("uomid", stockUOMId);
                    }
                        /**
                         * Cheking entry for applicable date and given uom id.
                         */
                        KwlReturnObject result = accProductObj.getPriceListEntry(requestParams);
                        list = result.getEntityList();
//                    }
                    priceMap.put("applydate", appDate);
                    priceMap.put(Constants.currencyKey, currencyid);
                } else {
                    /**
                     * Add price in price list.
                     */
                    if (!StringUtil.isNullOrEmpty(uomid)) {
                        requestParams.put("uomid", uomid);
                    } else {
                        requestParams.put("uomid", stockUOMId);
                    }

                    KwlReturnObject result = accProductObj.getPriceListEntry(requestParams);
                    list = result.getEntityList();
                }

                priceMap.put("price", newprice);

                if (list != null && list.size() > 0 && !updatePrice) {
                    return true;
                } else {
                    if (list.size() <= 0) {
                        priceMap.put(Constants.productid, productid);
                        priceMap.put(Constants.companyKey, companyid);
                        priceMap.put("carryin", carryIn);
                        priceMap.put("applydate", appDate);
                        priceMap.put("affecteduser", affecteduser);
                        priceMap.put(Constants.currencyKey, currencyid);
                        priceMap.put("initialPrice", initialPrice);
                        /**
                         * Passing uomid to save it in db.
                         * ERM-389 / ERP-35140
                         */
                        if (!StringUtil.isNullOrEmpty(uomid)) {
                            priceMap.put("uomid", uomid);
                        } else {
                            priceMap.put("uomid", stockUOMId);
                        }

                        priceresult = accProductObj.addPriceList(priceMap);
                    } else {
                        PriceList price = (PriceList) list.get(0);
                        priceMap.put("priceid", price.getID());
                        priceMap.put("initialPrice", initialPrice);
                        priceresult = accProductObj.updatePriceList(priceMap);
                    }
                    if (!isFromAsset) {
                        PriceList pl = (PriceList) priceresult.getEntityList().get(0);
                        String pDescription = StringUtil.isNullOrEmpty(pl.getProduct().getDescription()) ? "" : " (" + pl.getProduct().getDescription() + ")";
                        auditTrailObj.insertAuditLog(AuditAction.PRICE_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " changed the" + (carryIn ? " purchase" : " sales") + " price for product "
                                + pl.getProduct().getName() + " [" + pl.getProduct().getProductid() + "] " + pDescription
                                + " to " + pl.getPrice() + " ", request, pl.getID());  //+"currencyChange"
                    }
                }
            }
        } catch (ParseException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("setNewPrice : " + ex.getMessage(), ex);
        }
        return false;
    }

    public boolean propagatePriceListInChildCompanies(HttpServletRequest request, String ChildCompanyid, String parentcompanyid,String childCompanyName) throws ServiceException, SessionExpiredException, DataInvalidateException {
        KwlReturnObject priceresult;
        try {
            boolean updatePrice = Boolean.parseBoolean(request.getParameter("changeprice"));
            String productids = request.getParameter(Constants.productid);
            String companyid = ChildCompanyid;

            boolean carryIn = Boolean.parseBoolean(request.getParameter("carryin"));
            String affecteduser = carryIn ? request.getParameter("vendor") : request.getParameter("customer");
            Date appDate = authHandler.getDateOnlyFormat(request).parse(request.getParameter("applydate"));
            double newprice = Double.parseDouble(request.getParameter("price"));
            String currencyid = request.getParameter(Constants.currencyKey);
            String uomid = request.getParameter("uomid");


            String table = "", dataColumn = "", fetchColumn = "";
            if (carryIn) {
                table = "Customer";
                fetchColumn = "name";
                dataColumn = "ID";

            } else {
                table = "Vendor";
                fetchColumn = "name";
                dataColumn = "ID";
            }
            List dataList = null;
            String data = "";
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
            requestParams1.put(Constants.companyKey, parentcompanyid);
            try {
                dataList = importHandler.getRefData(requestParams1, table, dataColumn, fetchColumn, "", affecteduser);
                data = (String) dataList.get(0);
                //get id from name .example - select id from account where name=?
                requestParams1.put(Constants.companyKey, companyid);
                dataList = importHandler.getRefData(requestParams1, table, fetchColumn, dataColumn, "", data);
                data = (String) dataList.get(0);
                if (!StringUtil.isNullOrEmpty(data)) {
                    affecteduser = data;
                }

            } catch (Exception ex) {
                affecteduser = "-1";
            }

            String productidsarr[] = productids.split(",");
            for (int i = 0; i < productidsarr.length; i++) {
                String productid = productidsarr[i];

                try {
                    table = "Product";
                    dataColumn = "ID";
                    fetchColumn = "name";
                    requestParams1.put(Constants.companyKey, parentcompanyid);
                    dataList = importHandler.getRefData(requestParams1, table, dataColumn, fetchColumn, "", productid);
                    data = (String) dataList.get(0);
                    //get id from name .example - select id from account where name=?
                    requestParams1.put(Constants.companyKey, companyid);
                    dataList = importHandler.getRefData(requestParams1, table, fetchColumn, dataColumn, "", data);
                    data = (String) dataList.get(0);
                    if (!StringUtil.isNullOrEmpty(data)) {
                        productid = data;
                    }

                } catch (Exception ex) {
//                    throw new DataInvalidateException("Product does not found in child company.");
                }

                HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                requestParams.put(Constants.companyKey, companyid);
                requestParams.put(Constants.productid, productid);
                requestParams.put("carryin", carryIn);
                requestParams.put("applydate", appDate);
                requestParams.put("price", newprice);
                requestParams.put("affecteduser", affecteduser);
                requestParams.put(Constants.currencyKey, currencyid);
                requestParams.put("uomid", uomid);

                KwlReturnObject result = accProductObj.getPriceListEntry(requestParams);
                List list = result.getEntityList();

                HashMap<String, Object> priceMap = new HashMap<String, Object>();
                priceMap.put("price", newprice);

                if (list.size() > 0 && !updatePrice) {
                    return true;
                } else {
                    if (list.size() <= 0) {
                        priceMap.put(Constants.productid, productid);
                        priceMap.put(Constants.companyKey, companyid);
                        priceMap.put("carryin", carryIn);
                        priceMap.put("applydate", appDate);
                        priceMap.put("affecteduser", affecteduser);
                        priceMap.put(Constants.currencyKey, currencyid);
                        priceMap.put("uomid", uomid);

                        priceresult = accProductObj.addPriceList(priceMap);
                    } else {
                        PriceList price = (PriceList) list.get(0);
                        priceMap.put("priceid", price.getID());
                        priceresult = accProductObj.updatePriceList(priceMap);
                    }
                    PriceList pl = (PriceList) priceresult.getEntityList().get(0);
                    String pDescription = StringUtil.isNullOrEmpty(pl.getProduct().getDescription()) ? "" : " (" + pl.getProduct().getDescription() + ")";
                    auditTrailObj.insertAuditLog(AuditAction.PRICE_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " changed the"+(carryIn?" purchase":" sales")+" price in child company "+childCompanyName+" for product  "
                            + pl.getProduct().getName() +" ["+pl.getProduct().getProductid()+"] "+ pDescription
                            + " to " + pl.getPrice() + " ", request, pl.getID());  //+"currencyChange"
                }
            }
        } catch (ParseException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accProductController.propagatePriceListInChildCompanies : " + ex.getMessage(), ex);
        }
        return false;
    }

    public ModelAndView deleteFixedAssetOpeningDocuments(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String result = deleteFixedAssetOpeningDocuments(request);
            issuccess = true;
            if(StringUtil.isNullOrEmpty(result)){
                msg = messageSource.getMessage("acc.asset.deleteAssetOpeningdocument", null, RequestContextUtils.getLocale(request));
            }else{
                msg = result;
            }
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private String deleteFixedAssetOpeningDocuments(HttpServletRequest request) throws SessionExpiredException, AccountingException {
        String result="";
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            boolean isException = false;
            String docNos = "";
            String documentIds = request.getParameter("documentId");
            String[] documentIdsArray = documentIds.split(",");
            for (int i = 0; i < documentIdsArray.length; i++) {
                String documentId = documentIdsArray[i];
                HashMap<String, Object> requestMap = new HashMap<String, Object>();
                requestMap.put("companyId", companyId);
                requestMap.put("documentId", documentId);

                request.setAttribute("documentId", documentId);
                boolean isOpeningDocumentHasSoldORDepreciatedAsset = isOpeningDocumentHasSoldORDepreciatedAsset(request);
                request.removeAttribute("documentId");
                KwlReturnObject res = accountingHandlerDAOobj.getObject(FixedAssetOpening.class.getName(), documentId);
                FixedAssetOpening openingDoc = (FixedAssetOpening) res.getEntityList().get(0);
                    
                if (isOpeningDocumentHasSoldORDepreciatedAsset) {
                    docNos += " "+openingDoc.getDocumentNumber()+",";
                    isException = true;
                    continue;
                }
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("companyId", companyId);
                requestParams.put("documentid", documentId);
                KwlReturnObject result1 = accProductObj.getAssetDetail(requestParams);
                List<String> list1 = result1.getEntityList();
                String assetIds = "";
                for (String assetDetailId : list1) {
                    assetIds += assetDetailId + ",";
                }
                if (!StringUtil.isNullOrEmpty(assetIds)) {
                    assetIds = assetIds.substring(0, assetIds.length() - 1);
                    updateOpeningAmountForAssetControlAccountBeforeDelete(companyId, assetIds, documentId);
                }
                accProductObj.deleteAssetDetailsLinkedWithOpeningDocuments(requestMap);
                auditTrailObj.insertAuditLog(AuditAction.PRODUCT_TYPE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated Opening Balance of Account by Deleting Asset Opening Document "+openingDoc.getDocumentNumber(), request, "0");            
            }
            
            if (isException) {
                result = "Assets from the document(s) "+ docNos.substring(0, docNos.length()-1) +" has been depreciated or sold, So document(s) cannot be deleted.";
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error while deleting Data");
        }
        return result;
    }
    
    public ModelAndView deleteAssetDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String linkedTransaction;
        String msg = "";

        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            linkedTransaction=deleteAssetDetails(request);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                msg = "Asset details has been deleted successfully.";
            } else {
                msg = messageSource.getMessage("acc.field.fixedAssetExcept", null, RequestContextUtils.getLocale(request)) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            }
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private String deleteAssetDetails(HttpServletRequest request) throws SessionExpiredException, AccountingException {
            String linktransaction="";
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String AssetDetailsid = "";

            String assetId[] = request.getParameterValues("assetId");
            HashMap<String, Object> requestMap = new HashMap<String, Object>();
            requestMap.put("companyId", companyId);
            for (int i = 0; i < assetId.length; i++) {

                if (!StringUtil.isNullOrEmpty(assetId[i])) {
                    AssetDetailsid = assetId[i];
                    KwlReturnObject res = accountingHandlerDAOobj.getObject(AssetDetails.class.getName(), AssetDetailsid);
                    AssetDetails asseetid = (AssetDetails) res.getEntityList().get(0);
                    if (checkAssetTransactions(request, asseetid, companyId)) {
                        linktransaction+=asseetid.getAssetId()+ ", ";
                        continue;
                    }
                }
                requestMap.put("assetDetailId", AssetDetailsid);
                accProductObj.deleteAssetDetails(requestMap);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error while deleting Data");
        }
        return linktransaction;
    }

    public boolean isOpeningDocumentHasSoldORDepreciatedAsset(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        String companyId = sessionHandlerImpl.getCompanyid(request);
        String documentId =  "";
        if(request.getAttribute("documentId") != null){
            documentId = (String)request.getAttribute("documentId");
        }else{
            documentId = request.getParameter("documentId");
        }

        boolean isOpeningDocumentHasSoldORDepreciatedAsset = false;
        boolean isDocumentHasDepreciatedAsset = false;
        boolean isDocumentHasSoldAsset = false;

        HashMap<String, Object> requestMap = new HashMap<String, Object>();

        requestMap.put("companyId", companyId);
        requestMap.put("documentId", documentId);

        KwlReturnObject depreciatedAssetRetObject = accProductObj.getDepreciatedAssetsOfOpeningDocuments(requestMap);
        if (depreciatedAssetRetObject.getRecordTotalCount() > 0) {
            isDocumentHasDepreciatedAsset = true;
        }

        KwlReturnObject soldAssetRetObject = accProductObj.getSoldAssetsOfOpeningDocuments(requestMap);
        if (soldAssetRetObject.getRecordTotalCount() > 0) {
            isDocumentHasSoldAsset = true;
        }

        if (isDocumentHasDepreciatedAsset || isDocumentHasSoldAsset) {
            isOpeningDocumentHasSoldORDepreciatedAsset = true;
        }
        return isOpeningDocumentHasSoldORDepreciatedAsset;
    }

    public ModelAndView saveAssetDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";

        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(Constants.Acc_id, sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject extraresult = accCompanyPreferencesObj.getExtraCompanyPreferences(filterParams);
            ExtraCompanyPreferences extra = null;
            if (extraresult.getEntityList().size() > 0) {
                extra = (ExtraCompanyPreferences) extraresult.getEntityList().get(0);
            }
            Map<String,String> map=new HashMap<String,String>();
            Set<AssetDetails> assetDetailsSet = saveAssetDetails(request,extra,map);
            issuccess = true;
            msg = "Asset Details has been saved successfully.";
            txnManager.commit(status);
            if (extra.isActivateMRPModule()) {
                savemachineAsset(map, sessionHandlerImpl.getCompanyid(request));
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    private void savemachineAsset(Map<String, String> map,String company) throws AccountingException {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                Map<String,String> map1=new HashMap<String,String>();
                map1.put("assetdetailId", entry.getValue());
                map1.put("machineid", entry.getKey());
                map1.put(Constants.companyKey, company);
                accProductObj.saveAssetMachineMapping(map1);
            } catch (ServiceException ex) {
                throw new AccountingException("Error While saving Machine Data");
            }
        }
    }
    private Set<AssetDetails> saveAssetDetails(HttpServletRequest request,ExtraCompanyPreferences companyPreferences,Map<String,String> map) throws SessionExpiredException, AccountingException, UnsupportedEncodingException {
        Set<AssetDetails> assetDetailsSet = new HashSet<AssetDetails>();
        try {
            String product = request.getParameter("productId");
            String fixedAssetDetail = request.getParameter("assetDetails");
            JSONArray jArr = new JSONArray(fixedAssetDetail);
            String companyId = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);

            HashMap<String, Object> assetParams = new HashMap<String, Object>();
            assetParams.put("companyId", companyId);
            KwlReturnObject assetResult = accProductObj.getAssetDetails(assetParams);
            List<AssetDetails> assetList = assetResult.getEntityList();
            List<String> assetNameList = new ArrayList<String>();
            for (AssetDetails ad:assetList) {
                assetNameList.add(ad.getAssetId().toLowerCase());
            }
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                String assetId = StringUtil.DecodeText(jobj.optString("assetId"));
                String assetdescription = StringUtil.DecodeText(jobj.optString("assetdescription"));
                String location = jobj.getString("location");
                String department = jobj.getString("department");
                String machine = (jobj.has("machine") && !StringUtil.isNullOrEmpty(jobj.getString("machine"))) ? jobj.getString("machine") : "";
                String assetUser = jobj.getString("assetUser");
                String batchDetails = jobj.optString("batchdetails");

                double cost = jobj.optDouble("cost",0);
                double salvageRate = jobj.optDouble("salvageRate",0);
                double salvageValue = jobj.optDouble("salvageValue",0);
                double accumulatedDepreciation = jobj.optDouble("accumulatedDepreciation",0);
                /* // ERP-16629: WDV field should be optional during asset creation
                double wdv = jobj.getDouble("wdv"); */
                double assetLife = jobj.optDouble("assetLife",0);
                double elapsedLife = jobj.optDouble("elapsedLife",0);
                double nominalValue = jobj.optDouble("nominalValue",0);
                double sellAmount = jobj.optDouble("sellAmount",0);

                String installationDateStr = jobj.getString("installationDate");

                Date installationDate = df.parse(installationDateStr);

                String purchaseDateStr = jobj.getString("purchaseDate");
                Date purchaseDate = df.parse(purchaseDateStr);

                // Check Whether asset of this name exist or not in case of GRO -

                if (assetNameList.contains(assetId.toLowerCase())) {
                    throw new AccountingException(messageSource.getMessage("acc.fixed.asset.id", null, RequestContextUtils.getLocale(request))+" " +"<b>" + assetId + "</b>"+messageSource.getMessage("acc.po.assetalreadygenerated", null, RequestContextUtils.getLocale(request)));
                }

                HashMap<String, Object> dataMap = new HashMap<String, Object>();

                dataMap.put("assetId", assetId);
                dataMap.put("assetdescription", assetdescription);
                dataMap.put("location", location);
                dataMap.put("department", department);
                dataMap.put("assetUser", assetUser);
                dataMap.put("cost", cost);
                dataMap.put("salvageRate", salvageRate);
                dataMap.put("salvageValue", salvageValue);
                dataMap.put("accumulatedDepreciation", accumulatedDepreciation);
                dataMap.put("assetLife", assetLife);
                dataMap.put("elapsedLife", elapsedLife);
                dataMap.put("nominalValue", nominalValue);
                dataMap.put("sellAmount", sellAmount);
                dataMap.put("productId", product);
                dataMap.put("isCreatedFromOpeningForm", true);
                dataMap.put("installationDate", installationDate);
                dataMap.put("purchaseDate", purchaseDate);
                dataMap.put("companyId", companyId);
                dataMap.put("invrecord", true);

                KwlReturnObject result = accProductObj.saveAssetDetails(dataMap);
                AssetDetails row = (AssetDetails) result.getEntityList().get(0);
                if (jobj.has(Constants.customfield)) {
                    String customfield = jobj.getString(Constants.customfield);
                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        HashMap<String, Object> DOMap = new HashMap<String, Object>();
                        JSONArray jcustomarray = new JSONArray(customfield);
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "AssetDetails");
                        customrequestParams.put("moduleprimarykey", "AssetDetailsId");
                        customrequestParams.put("modulerecid", row.getId());
                        customrequestParams.put("moduleid", Constants.Acc_FixedAssets_Details_ModuleId);
                        customrequestParams.put(Constants.companyKey, companyId);
                        DOMap.put(Constants.Acc_id, row.getId());
                        customrequestParams.put("customdataclasspath", Constants.Acc_FixedAsset_Details_Custom_Data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            DOMap.put("accassetdetailscustomdata", row.getId());
                            accProductObj.updateAssetDetails(DOMap);
                        }
                    }
                }
                if(!StringUtil.isNullOrEmpty(machine)){
                    map.put(machine, row.getId());
                }
                if (!StringUtil.isNullOrEmpty(batchDetails) && !batchDetails.equalsIgnoreCase("null") ) {
                    String assetMainId = row.getId();
                    dataMap.put("assetDetailId", assetMainId);
                    saveAssetNewBatch(batchDetails, product, request, assetMainId);
                }
                AssetDetails assetDetails = (AssetDetails) result.getEntityList().get(0);

                assetDetailsSet.add(assetDetails);
            }
            /*
             Save split Opening balance
             */
            if (companyPreferences !=null && companyPreferences.isSplitOpeningBalanceAmount()) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                String productId = request.getParameter("productId");
                requestParams.put(Constants.companyKey, companyId);
                requestParams.put("productId", productId);
                saveSplitOpeningBalanceOfAsset(requestParams, jArr);
                auditTrailObj.insertAuditLog(AuditAction.PRODUCT_TYPE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has added Split Opening Balance of Account by adding Asset Opening Document ", request, "0");
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException(messageSource.getMessage("acc.commom.ErrorWhileProcessingData", null, RequestContextUtils.getLocale(request)));
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException(messageSource.getMessage("acc.commom.ErrorWhileProcessingData", null, RequestContextUtils.getLocale(request)));
        } catch (ParseException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException(messageSource.getMessage("acc.commom.ErrorWhileProcessingData", null, RequestContextUtils.getLocale(request)));
        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException(messageSource.getMessage("acc.commom.ErrorWhileProcessingData", null, RequestContextUtils.getLocale(request)));
        }
        return assetDetailsSet;
    }
    
    public void saveSplitOpeningBalanceOfAsset(HashMap<String, Object> requestParam, JSONArray array) throws JSONException, ServiceException {
        String companyid="";
        String productId="";
        if(requestParam.containsKey(Constants.companyKey)){
            companyid=requestParam.get(Constants.companyKey).toString();
        }
        if(requestParam.containsKey("productId")){
            productId=requestParam.get("productId").toString();
        }
        Account account = null;
        if (!StringUtil.isNullOrEmpty(productId)) {
            Product product = (Product) kwlCommonTablesDAOObj.getClassObject(Product.class.getName(), productId);
            if (product != null) {
                account = product.getSalesAccount();
            } else {
                return;
            }
        }
        HashMap hashMap = new HashMap();
        int count = 0;
        boolean addSplitOpeningBalance = false;
        JSONArray distributedopeningbalancearray = new JSONArray();
        for (int detail = 0; detail < array.length(); detail++) {
            JSONObject jsono = array.getJSONObject(detail);
            double cost = jsono.optDouble("cost", 0);
            if (jsono.has(Constants.customfield)) {
                String customfield = jsono.optString(Constants.customfield);
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    JSONArray array1 = new JSONArray(customfield);
                    for (int index = 0; index < array1.length(); index++) {
                        JSONObject jSONObject = array1.getJSONObject(index);
                        if (jSONObject.optString("xtype", "0").equalsIgnoreCase("4")) {
                            String fieldName = jSONObject.optString("fieldname");
                            String column = jSONObject.optString(fieldName);
                            String val = jSONObject.optString(column);
                            if (!StringUtil.isNullOrEmpty(val)) {
                                FieldComboData comboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), val);
                                if (comboData != null) {
                                    val = comboData.getValue();
                                    /*
                                    get FieldCombodata Id for Account custom field value
                                     */
                                    val = fieldDataManagercntrl.getValuesForLinkRecords(Constants.Account_Statement_ModuleId, companyid, fieldName, val, 1);
                                    JSONObject object = new JSONObject();
                                    /*
                                    check if same comboId entry present in distribute balance array or not.
                                    if present then take balance for it and exclude its entry from array
                                     */
                                    double balance = 0;
                                    if (hashMap.containsKey(val)) {
                                        JSONArray filterArray = new JSONArray();
                                        filterArray = distributedopeningbalancearray;
                                        distributedopeningbalancearray = new JSONArray();
                                        for (int j = 0; j < filterArray.length(); j++) {
                                            JSONObject jSONObject1 = filterArray.getJSONObject(j);
                                            if (jSONObject1.optString("comboid").equalsIgnoreCase(val)) {
                                                balance = jSONObject1.optDouble("distributedopeningbalanace");
                                            } else {
                                                distributedopeningbalancearray.put(jSONObject1);
                                            }

                                        }
                                    } else {
                                        /*
                                        get split balance for comboId for Perticular account
                                         */
                                        balance = accAccountDAOobj.getSplitBalanceForComboId(val, comboData.getFieldid(), account.getID());
                                    }
                                    hashMap.put(val, "Val" + count++);
                                    object.put("distributedopeningbalanace", balance + cost);
                                    object.put("comboid", val);
                                    distributedopeningbalancearray.put(object);
                                    addSplitOpeningBalance = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (addSplitOpeningBalance) {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.companyKey, companyid);
            requestParams.put("account", account);
            requestParams.put("debitType", true);
            requestParams.put("isAssetOpeningBalance", false);
            if (!StringUtil.isNullOrEmpty(distributedopeningbalancearray.toString())) {
                requestParams.put("distributedopeningbalancearray", distributedopeningbalancearray);
            }
            accAccountDAOobj.distributeOpeningBalance(requestParams);
        }
    }

    public void updateAssetControlAccountOpeningBalance(HashMap<String, Object> accountParams) throws ServiceException {
        String productId = "";
        String accountId = "";
        try {
            if (accountParams.containsKey("productId")) {
                productId = accountParams.get("productId").toString();
            }
            if (!StringUtil.isNullOrEmpty(productId)) {
                Product product = (Product) kwlCommonTablesDAOObj.getClassObject(Product.class.getName(), productId);
                if (product != null) {
                    accountId = product.getSalesAccount().getID().toString();
                }
            }
            double balance = 0;
            if (accountParams.containsKey("assetTotalCost")) {
                balance = Double.parseDouble(accountParams.get("assetTotalCost").toString());
            }
            if (!StringUtil.isNullOrEmpty(accountId)) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("accountid", accountId);
                jSONObject.put("balance", balance);
                accAccountDAOobj.updateAccountOpeningAmount(accountId, balance);
            }
        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateOpeningAmountForAssetControlAccountBeforeDelete(String companyId, String assetIds, String docId) {
        try {
            String assetDetailId = "";
            if (!StringUtil.isNullOrEmpty(assetIds)) {
                String assetDetailArr[] = assetIds.split(",");
                assetDetailId = assetDetailArr[0];
            }
            /*
             update Opening Amount for control account
             */
            AssetDetails ad = null;
            ad = (AssetDetails) kwlCommonTablesDAOObj.getClassObject(AssetDetails.class.getName(), assetDetailId);
            double rate = accProductObj.getBalanceFromAssetOpening(companyId, docId);
            String accountId = ad.getProduct().getSalesAccount().getID();
            accAccountDAOobj.updateAccountOpeningAmount(accountId, (-rate));

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.customcolumn, "moduleid"));
            requestParams.put(Constants.filter_values, Arrays.asList(companyId, 1, Constants.Acc_FixedAssets_Details_ModuleId));
            KwlReturnObject result = accAccountDAOobj.getFieldParams(requestParams);
            List<FieldParams> list = result.getEntityList();
            for (FieldParams fieldParams : list) {
                if (fieldParams.getFieldtype() == 4) {
                    String fieldName = fieldParams.getFieldname();
                    String column = "col" + fieldParams.getColnum();
                    String asset[] = assetIds.split(",");
                    for (int i = 0; i < asset.length; i++) {
                        String AssetId = asset[i].toString();
                        AssetDetails assetDetails = (AssetDetails) kwlCommonTablesDAOObj.getClassObject(AssetDetails.class.getName(), AssetId);
                        double cost = assetDetails.getCost();
                        /*
                         get ComboId from asset Details custom data
                         */
                        String comboId = accProductObj.getfieldComboIdFromAssetDetail(AssetId, column);
                        if (!StringUtil.isNullOrEmpty(comboId)) {
                            FieldComboData comboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), comboId);
                            if (comboData != null) {
                                String value = comboData.getValue();
                                /*
                                 get ComboId for value of Account Custom field
                                 */
                                comboId = fieldDataManagercntrl.getValuesForLinkRecords(Constants.Account_Statement_ModuleId, companyId, fieldName, value, 1);
                                if(!StringUtil.isNullOrEmpty(comboId)){
                                    accAccountDAOobj.updateSplitOpeningAmount(comboId, (-cost), accountId);
                                }
                            }
                        }
                    }
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ModelAndView saveAssetOpenings(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        boolean isEdit=(StringUtil.isNullOrEmpty(request.getParameter("isEdit")))?false:Boolean.parseBoolean(request.getParameter("isEdit"));
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            Map<String, String> map1 = new HashMap<String, String>();
            String companyId=sessionHandlerImpl.getCompanyid(request);
            FixedAssetOpening assetOpening = saveAssetOpenings(request,map1);
            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(Constants.Acc_id, companyId);
            KwlReturnObject extraresult = accCompanyPreferencesObj.getExtraCompanyPreferences(filterParams);
            ExtraCompanyPreferences extra = null;
            if (extraresult.getEntityList().size() > 0) {
                extra = (ExtraCompanyPreferences) extraresult.getEntityList().get(0);
            }
            String documentNumber = "";
            if (assetOpening != null) {
                documentNumber = assetOpening.getDocumentNumber();
                jobj.put("documentNumber", documentNumber);
            }
            issuccess = true;
            if (isEdit) {
                msg = messageSource.getMessage("acc.commom.document", null, RequestContextUtils.getLocale(request))+" "+ documentNumber + " "+messageSource.getMessage("acc.package.updateremain", null, RequestContextUtils.getLocale(request));
            } else {
                msg =messageSource.getMessage("acc.commom.document", null, RequestContextUtils.getLocale(request))+" "+ documentNumber + " "+messageSource.getMessage("acc.field.hasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            }
            txnManager.commit(status);
            if (extra.isActivateMRPModule()) {
                savemachineAsset(map1,companyId);
            }

        } catch (JSONException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private FixedAssetOpening saveAssetOpenings(HttpServletRequest request,Map<String, String> map1) throws SessionExpiredException, AccountingException, UnsupportedEncodingException, JSONException {
        FixedAssetOpening assetOpening = null;
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String productId = request.getParameter("productId");
            String assetOpeningId = request.getParameter("assetOpeningId");
            String creationDateStr = request.getParameter("creationDateStr");
            String documentNumber = request.getParameter("documentNumber");
            String documentId = request.getParameter("documentId");
            boolean isEdit=(StringUtil.isNullOrEmpty(request.getParameter("isEdit")))?false:Boolean.parseBoolean(request.getParameter("isEdit"));
            double quantity = Double.parseDouble(request.getParameter("quantity"));
            double rate = Double.parseDouble(request.getParameter("rate"));
            double assetTotalCost=rate;
           /* // ERP-16629: WDV field should be optional during asset creation
            double wdv = Double.parseDouble(request.getParameter("wdv")); */

            DateFormat df = authHandler.getDateOnlyFormat(request);
            Date creationDate = df.parse(creationDateStr);

            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(Constants.Acc_id, companyId);
            KwlReturnObject extraresult = accCompanyPreferencesObj.getExtraCompanyPreferences(filterParams);
            ExtraCompanyPreferences extra = null;
            if (extraresult.getEntityList().size() > 0) {
                extra = (ExtraCompanyPreferences) extraresult.getEntityList().get(0);
            }

            if (StringUtil.isNullOrEmpty(assetOpeningId)&& !isEdit) {
                HashMap<String, Object> reqMap = new HashMap<String, Object>();
                reqMap.put("companyId", companyId);
                reqMap.put("documentNumber", documentNumber);
                KwlReturnObject result = accProductObj.getAssetOpenings(reqMap);
                int nocount = result.getRecordTotalCount();
                if (nocount > 0) {
                    throw new AccountingException("Document number '" + documentNumber + "' already exists.");
                }
            }

            if (isEdit) {
                KwlReturnObject aocnt = accProductObj.getAssetOpeningsEditCount(documentNumber, companyId, documentId);
                if (aocnt.getRecordTotalCount() > 0 ) {
                    throw new AccountingException("Document number '" + documentNumber + "' already exists.");
                }
                String EditDocumentNumber = request.getParameter("editdocumentNumber");
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("companyId", companyId);
                requestParams.put("documentNumber", EditDocumentNumber);
                KwlReturnObject result2 = accProductObj.getAssetOpenings(requestParams);
                List <FixedAssetOpening>list = result2.getEntityList();

                FixedAssetOpening fao = (FixedAssetOpening)list.get(0);
                documentId = fao.getId();
                accProductObj.deleteInventory(fao.getInventory().getID(), companyId);

                requestParams.put("documentid", documentId);
                KwlReturnObject result1 = accProductObj.getAssetDetail(requestParams);
                List list1 = result1.getEntityList();
                Iterator it1 = list1.iterator();
                String assetIds="";
                while (it1.hasNext()) {
                    String assetDetailId = (String) it1.next();
                    assetIds+=assetDetailId+",";
                    AssetDetails ad = (AssetDetails) kwlCommonTablesDAOObj.getClassObject(AssetDetails.class.getName(), assetDetailId);
                    if (checkAssetTransactions(request, ad, companyId)) {
                        throw new AccountingException(" Asset Id  '" + ad.getAssetId() + "' already used.");
                    }
                    requestParams.put(Constants.productid, ad.getId());
                    accProductObj.deleteProductBatchSerialDetails(requestParams);
                }

                requestParams.put("companyId", companyId);
                requestParams.put("documentId", documentId);
                boolean isOpeningDocumentHasSoldORDepreciatedAsset = false;
                boolean isDocumentHasDepreciatedAsset = false;
                boolean isDocumentHasSoldAsset = false;

                KwlReturnObject depreciatedAssetRetObject = accProductObj.getDepreciatedAssetsOfOpeningDocuments(requestParams);
                if (depreciatedAssetRetObject.getRecordTotalCount() > 0) {
                    isDocumentHasDepreciatedAsset = true;
                }

                KwlReturnObject soldAssetRetObject = accProductObj.getSoldAssetsOfOpeningDocuments(requestParams);
                if (soldAssetRetObject.getRecordTotalCount() > 0) {
                    isDocumentHasSoldAsset = true;
                }

                if (isDocumentHasDepreciatedAsset || isDocumentHasSoldAsset) {
                    isOpeningDocumentHasSoldORDepreciatedAsset = true;
                }
                if (isOpeningDocumentHasSoldORDepreciatedAsset) {
                    throw new AccountingException("Selected Asset has been depreciated or sold, So cannot be edited.");
                }
                if (!StringUtil.isNullOrEmpty(assetIds)) {
                    assetIds = assetIds.substring(0, assetIds.length() - 1);
                    updateOpeningAmountForAssetControlAccountBeforeDelete(companyId, assetIds, documentId);
                }

                accProductObj.deleteAssetDetailsLinkedWithOpeningDocuments(requestParams);
            }

            // update Inventory First then save it in fixedassetopening table.
            Inventory inventory = updateOpeningInventory(request);

            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("documentId",documentId);
            dataMap.put("companyId", companyId);
            dataMap.put("productId", productId);
            dataMap.put("creationDate", creationDate);
            dataMap.put("documentNumber", documentNumber);
            dataMap.put("quantity", quantity);
            dataMap.put("rate", rate);
            /* // ERP-16629: WDV field should be optional during asset creation
            dataMap.put("wdv", wdv); */
            dataMap.put("inventory", inventory.getID());

            KwlReturnObject invresult = accProductObj.addAssetOpening(dataMap);
            assetOpening = (FixedAssetOpening) invresult.getEntityList().get(0);

            // saving Asset Detail For this assetOpening

            Set<AssetDetails> assetDetailsSet = saveAssetDetails(request, extra, map1);

            saveAssetOpeningMapping(assetOpening, assetDetailsSet, companyId);
            HashMap <String ,Object> accountParams=new HashMap();
            accountParams.put("companyId", companyId);
            accountParams.put("productId", productId);
            accountParams.put("assetTotalCost", assetTotalCost);
            updateAssetControlAccountOpeningBalance(accountParams);
            
            String assetGroupId = assetOpening.getProduct() == null ? "" : assetOpening.getProduct().getProductid();
            String assetGroupName = assetOpening.getProduct() == null ? "" : assetOpening.getProduct().getName();
            String action="adding";
            if(isEdit){
                action="updating";
            }
            auditTrailObj.insertAuditLog(AuditAction.PRODUCT_TYPE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated Opening Balance of Account by "+ action+" Asset Opening Document " + (assetOpening == null ? "" : assetOpening.getDocumentNumber()) +" for asset group "+ assetGroupId +" : " + assetGroupName, request, "0");
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return assetOpening;
    }

    public boolean checkAssetTransactions(HttpServletRequest request, AssetDetails assetid, String companyid) throws SessionExpiredException, ServiceException, AccountingException {
        boolean Assetusedflag = false;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyId", companyid);
            
            if (!StringUtil.isNullOrEmpty(assetid.getId())) {
                boolean isAssetSold = (assetid.getAssetSoldFlag() != 0) ? true : false;
                boolean isLeased = assetid.isLinkedToLeaseSO();
                if (isAssetSold || isLeased) {
                    Assetusedflag = true; //"Selected product is currently used in the Transaction(s). So it cannot be edited.");
                }
                requestParams.put("assetDetailsId", assetid.getId());
                /*Check Asset Detail Id used in Maintenance Scedule */
                if (accProductObj.isAssetUsedInMaintenanceSchedule(requestParams)) {
                    Assetusedflag = true; 
                }
            }

        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.acc.editprodexcp", null, RequestContextUtils.getLocale(request)), ex);
        }
        return Assetusedflag;
    }
    private Inventory updateOpeningInventory(HttpServletRequest request) throws SessionExpiredException, AccountingException {
        Inventory inventory = null;
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String productId = request.getParameter("productId");
            double quantity = Integer.parseInt(request.getParameter("quantity"));
            Product product = (Product) kwlCommonTablesDAOObj.getClassObject(Product.class.getName(), productId);
            Date newUserDate = new Date();
            KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
            Company company = (Company) custumObjresult.getEntityList().get(0);
            if (company.getCreator() != null) {
               newUserDate = authHandler.getUserNewDate(null, company.getCreator().getTimeZone()!=null?company.getCreator().getTimeZone().getDifference() : company.getTimeZone().getDifference());
            }
            JSONObject inventoryjson = new JSONObject();
            inventoryjson.put(Constants.productid, product.getID());
            inventoryjson.put("quantity", quantity);
            inventoryjson.put("baseuomquantity", quantity);
            inventoryjson.put("baseuomrate", 1);
            if (product.getUnitOfMeasure() != null) {
                inventoryjson.put("uomid", product.getUnitOfMeasure().getID());
            }
            inventoryjson.put("description", "Inventory Opened");
            inventoryjson.put("carryin", true);
            inventoryjson.put("defective", false);
            inventoryjson.put("newinventory", true);
            inventoryjson.put(Constants.companyKey, companyId);
            inventoryjson.put("updatedate", newUserDate);
            inventoryjson.put("invrecord", true);
            inventoryjson.put("isOpeningInventory", true);
            KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
            inventory = (Inventory) invresult.getEntityList().get(0);
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error while saving opening document.");
        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error while saving opening document.");
        }
        return inventory;
    }

    public void saveAssetOpeningMapping(FixedAssetOpening assetOpening, Set<AssetDetails> assetDetailsSet, String companyId) throws AccountingException {
        try {
            for (AssetDetails assetDetails : assetDetailsSet) {
                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                dataMap.put("assetOpening", assetOpening.getId());
                dataMap.put("assetDetails", assetDetails.getId());
                dataMap.put("company", companyId);
                accProductObj.saveAssetOpeningMapping(dataMap);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            throw new AccountingException("Error while processing data.");
        }
    }

    public ModelAndView saveAssetDepreciation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ADD_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveAssetDepreciation(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.dep.done", null, RequestContextUtils.getLocale(request));   //"Depreciation has been done successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveAssetDepreciation(HttpServletRequest request) throws SessionExpiredException, AccountingException {
        try {
            Calendar Cal = Calendar.getInstance();
            Cal.setTime(new Date());

            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            JSONArray jArr = new JSONArray(request.getParameter(Constants.detail));
            DateFormat df=authHandler.getDateOnlyFormat();
            String assetId = request.getParameter("assetdetailId");
            String productId = request.getParameter("productId");
            Product product = (Product) kwlCommonTablesDAOObj.getClassObject(Product.class.getName(), productId);

            String accountid = "";

            if (product.getDepreciationProvisionGLAccount() != null) {
                accountid = product.getDepreciationProvisionGLAccount().getID();
            } else {
                accountid = product.getPurchaseAccount().getID();// this is containing value of Asset Controlling Account.
            }

            if (jArr.length() > 0) {
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    double perioddepreciation = Double.parseDouble(StringUtil.DecodeText(jobj.optString("perioddepreciation")));
                    String jeentryNumber = "";
                    String jeIntegerPart = "";
                    String jeDatePrefix = "";
                    String jeDateAfterPrefix = "";
                    String jeDateSuffix = "";
                    String jeSeqFormatId = "";
                    boolean jeautogenflag = false;
                    String date=df.format(Cal.getTime());
                    Date entryDate = null;
                    try{
                        entryDate=df.parse(date);
                    }catch(ParseException ex){
                        entryDate = Cal.getTime();
                    }
                    synchronized (this) {
                        HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                        JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                        JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                        JEFormatParams.put(Constants.companyKey, companyid);
                        JEFormatParams.put("isdefaultFormat", true);

                        KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                        SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, entryDate);
                        jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                        jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                        jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        jeSeqFormatId = format.getID();
                        jeautogenflag = true;
                    }

                    Map<String, Object> jeDataMap = AccountingManager.getGlobalParams(request);
                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                    jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                    jeDataMap.put("entrydate", entryDate);
                    jeDataMap.put(Constants.companyKey, companyid);
                    jeDataMap.put(Constants.memo, request.getParameter(Constants.memo));
                    jeDataMap.put(Constants.currencyKey, currencyid);
                    jeDataMap.put("costcenterid", request.getParameter(Constants.costcenter));

                    HashSet jeDetails = new HashSet();
                    KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails

                    JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                    String jeid = journalEntry.getID();
                    jeDataMap.put("jeid", jeid);

                    if (perioddepreciation >= 0) {
                        JSONObject jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put(Constants.companyKey, companyid);
                        jedjson.put("amount", perioddepreciation);
                        jedjson.put("accountid", accountid);
                        jedjson.put("debit", false);
                        jedjson.put("jeid", jeid);
                        KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jeDetails.add(jed);

                        jedjson = new JSONObject();
                        jedjson.put("srno", jeDetails.size() + 1);
                        jedjson.put(Constants.companyKey, companyid);
                        jedjson.put("amount", perioddepreciation);
                        jedjson.put("accountid", product.getDepreciationGLAccount().getID());
                        jedjson.put("debit", true);
                        jedjson.put("jeid", jeid);
                        jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                        jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                        jeDetails.add(jed);
                    }

                    jeDataMap.put("jedetails", jeDetails);
                    jeDataMap.put("externalCurrencyRate", 0.0);

                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details

                    HashMap<String, Object> ddMap = new HashMap<String, Object>();
                    ddMap.put("depreciationCreditToAccountId", accountid);
                    ddMap.put("depreciationGLAccountId", product.getDepreciationGLAccount().getID());
                    ddMap.put("productId", productId);
                    ddMap.put("assetId", assetId);
                    ddMap.put("period", Integer.parseInt(StringUtil.DecodeText(jobj.optString("period"))));
                    ddMap.put(Constants.companyKey, companyid);
                    ddMap.put("jeid", jeid);
                    ddMap.put("periodamount", perioddepreciation);
                    ddMap.put("accamount", jobj.optDouble("accdepreciation", 0));
                    ddMap.put("netbookvalue", jobj.optDouble("netbookvalue", 0));
                    // add depreciation detail
                    accProductObj.addDepreciationDetail(ddMap);
                }
            }
        } /*catch (UnsupportedEncodingException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }*/ catch (ServiceException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    // for product Batch & for product category
    public void saveProductBatch(String batchJSON, Product product, HttpServletRequest request) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {
        JSONArray jArr = new JSONArray(batchJSON);
        //Please do same changes to ProductControllerCMN for Same Method
        KwlReturnObject kmsg = null;
        double ActbatchQty = 1;
        double batchQty = 0;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isEdit = false;
        int serialsequence = 1 , batchsequence = 1; // for user selected sequence of batch and serial while creating Product.
        isEdit=!StringUtil.isNullObject(request.getAttribute("EditFlag"))?(Boolean)request.getAttribute("EditFlag"):false;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);

        if (!StringUtil.isNullOrEmpty(product.getID())) {
            isLocationForProduct = (isEdit && !StringUtil.isNullOrEmpty(request.getParameter("isLocationForProduct")))?Boolean.parseBoolean(request.getParameter("isLocationForProduct")):product.isIslocationforproduct();
            isWarehouseForProduct = (isEdit && !StringUtil.isNullOrEmpty(request.getParameter("isWarehouseForProduct")))?Boolean.parseBoolean(request.getParameter("isWarehouseForProduct")):product.isIswarehouseforproduct();
            isBatchForProduct = (isEdit && !StringUtil.isNullOrEmpty(request.getParameter("isBatchForProduct")))?Boolean.parseBoolean(request.getParameter("isBatchForProduct")):product.isIsBatchForProduct();
            isSerialForProduct = (isEdit && !StringUtil.isNullOrEmpty(request.getParameter("isSerialForProduct")))?Boolean.parseBoolean(request.getParameter("isSerialForProduct")):product.isIsSerialForProduct();
            isRowForProduct = (isEdit && !StringUtil.isNullOrEmpty(request.getParameter("isRowForProduct")))?Boolean.parseBoolean(request.getParameter("isRowForProduct")):product.isIsrowforproduct();
            isRackForProduct = (isEdit && !StringUtil.isNullOrEmpty(request.getParameter("isRackForProduct")))?Boolean.parseBoolean(request.getParameter("isRackForProduct")):product.isIsrackforproduct();
            isBinForProduct = (isEdit && !StringUtil.isNullOrEmpty(request.getParameter("isBinForProduct")))?Boolean.parseBoolean(request.getParameter("isBinForProduct")):product.isIsbinforproduct();
        }
        NewProductBatch productBatch = null;
        String productBatchId = "";
        //Save Batch detail for both  option for serial no and batch also as if batch option is off then also we are generating batch in backend
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
              if (jSONObject.has("quantity") && !jSONObject.getString("quantity").equals("undefined")  && !jSONObject.getString("quantity").isEmpty()) {
                ActbatchQty = jSONObject.getDouble("quantity");
            }
            if (batchQty == 0) {
                batchQty = jSONObject.getDouble("quantity");
            }
            if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct  || isRowForProduct || isRackForProduct  || isBinForProduct) && (batchQty == ActbatchQty)) {

                HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                pdfTemplateMap.put(Constants.companyKey, companyid);
                pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("batch")));
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                    pdfTemplateMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                    pdfTemplateMap.put("expdate", df.parse(jSONObject.getString("expdate")));
                }
                pdfTemplateMap.put("quantity", jSONObject.getString("quantity"));
                pdfTemplateMap.put("balance", jSONObject.getString("balance"));
                pdfTemplateMap.put("location", jSONObject.getString("location"));
                pdfTemplateMap.put("row", jSONObject.getString("row"));
                pdfTemplateMap.put("rack", jSONObject.getString("rack"));
                pdfTemplateMap.put("bin", jSONObject.getString("bin"));
                pdfTemplateMap.put("product", product.getID());
                pdfTemplateMap.put("warehouse", jSONObject.getString("warehouse"));
                pdfTemplateMap.put("isopening", true);
                pdfTemplateMap.put("transactiontype", "28");//This is GRN Type Tranction  
                pdfTemplateMap.put("ispurchase", true);
                kmsg = accCommonTablesDAO.saveNewBatchForProduct(pdfTemplateMap);

                if (kmsg != null && kmsg.getEntityList().size() != 0) {
                    productBatch = (NewProductBatch) kmsg.getEntityList().get(0);
                    productBatchId = productBatch.getId();
                }

                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", jSONObject.getString("quantity"));
                documentMap.put("batchmapid", productBatchId);
                documentMap.put("documentid", product.getID());
                documentMap.put("transactiontype", "28");//This is GRN Type Tranction
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                    documentMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                    documentMap.put("expdate", df.parse(jSONObject.getString("expdate")));
                }
                
                /**
                 * added selected sequence for batch selected by user while
                 * Edit Product.
                 */
                documentMap.put("batchsequence", batchsequence++);
                accCommonTablesDAO.saveBatchDocumentMapping(documentMap);
            }
            batchQty--;

            if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 

                HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                pdfTemplateMap.put(Constants.Acc_id,isEdit? "": jSONObject.getString("serialnoid"));
                pdfTemplateMap.put(Constants.companyKey, companyid);
                pdfTemplateMap.put("product", product.getID());
                pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("serialno")));
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    pdfTemplateMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    pdfTemplateMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }
                pdfTemplateMap.put("batch", productBatchId);
                pdfTemplateMap.put("transactiontype", "28");//This is GRN Type Tranction  
                pdfTemplateMap.put("quantity", "1");//This is GRN Type Tranction  
                pdfTemplateMap.put("ispurchase", true);
                pdfTemplateMap.put("isopening", true);
                pdfTemplateMap.put("skuvalue", jSONObject.optString("skufield",""));
                
                kmsg = accCommonTablesDAO.saveNewSerialForBatch(pdfTemplateMap);
                String serialDetailsId = "";
                if (kmsg != null && kmsg.getEntityList().size() != 0) {
                    NewBatchSerial serialDetails = (NewBatchSerial) kmsg.getEntityList().get(0);
                    serialDetailsId = serialDetails.getId();
                }

                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", 1);
                documentMap.put("serialmapid", serialDetailsId);
                documentMap.put("documentid", product.getID());
                documentMap.put("transactiontype", "28");//This is GRN Type Tranction  
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    documentMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    documentMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }

                // accCommonTablesDAO.saveSerialDocumentMapping(documentMap);
                /**
                 * added selected sequence for serial selected by user while
                 * Edit Product.
                 */
                documentMap.put("serialsequence", serialsequence++);
                KwlReturnObject krObj = accCommonTablesDAO.saveSerialDocumentMapping(documentMap);

                SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) krObj.getEntityList().get(0);
                if (jSONObject.has(Constants.customfield)) {
                    String customfield = jSONObject.getString(Constants.customfield);
                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        HashMap<String, Object> DOMap = new HashMap<String, Object>();
                        JSONArray jcustomarray = new JSONArray(customfield);

                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "SerialDocumentMapping");
                        customrequestParams.put("moduleprimarykey", "SerialDocumentMappingId");
                        customrequestParams.put("modulerecid", serialDocumentMapping.getId());
                        customrequestParams.put("moduleid", Constants.SerialWindow_ModuleId);
                        customrequestParams.put(Constants.companyKey, companyid);
                        DOMap.put(Constants.Acc_id, serialDocumentMapping.getId());
                        customrequestParams.put("customdataclasspath", Constants.Acc_Serial_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            DOMap.put("serialcustomdataref", serialDocumentMapping.getId());
                            accCommonTablesDAO.updateserialcustomdata(DOMap);
                        }
                    }
                }
            }else{
                batchQty=0;
            }
            if (jSONObject.has("attachmentids") && !StringUtil.isNullOrEmpty(jSONObject.getString("attachmentids"))) {
                accCommonTablesDAO.UpdateDocuments(productBatchId,jSONObject.getString("attachmentids"));
            }
        }

    }

    public ProductBatch saveAssetBatch(String batchJSON, String assetId, String productId, HttpServletRequest request) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException {
        JSONArray jArr = new JSONArray(batchJSON);
        KwlReturnObject kmsg = null;
        boolean isSerialForProduct = false;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String companyId = sessionHandlerImpl.getCompanyid(request);
        if (!StringUtil.isNullOrEmpty(productId)) {
            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), productId);
            Product product = (Product) prodresult.getEntityList().get(0);
            isSerialForProduct = product.isIsSerialForProduct();
        }
        for (int i = 0; i < 1; i++) {
            HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
            pdfTemplateMap.put(Constants.companyKey, companyId);
            pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("batch")));//for saving the batch name its showing %20 in space
            if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                pdfTemplateMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
            }
            if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                pdfTemplateMap.put("expdate", df.parse(jSONObject.getString("expdate")));
            }
            pdfTemplateMap.put("quantity", jSONObject.getString("quantity"));
            pdfTemplateMap.put("balance", jSONObject.getString("balance"));
            pdfTemplateMap.put("location", jSONObject.getString("location"));
            pdfTemplateMap.put("product", jSONObject.getString(Constants.productid));
            pdfTemplateMap.put("asset", assetId);   //stored the assetid of the 
            pdfTemplateMap.put("warehouse", jSONObject.getString("warehouse"));
            pdfTemplateMap.put("isopening", true);
            pdfTemplateMap.put("transactiontype", "1");//This is product Type Tranction
            pdfTemplateMap.put("ispurchase", true);
            kmsg = accCommonTablesDAO.saveBatchForProduct(pdfTemplateMap);
        }
        ProductBatch productBatch = null;
        String productBatchId = "";
        if (kmsg.getEntityList().size() != 0) {
            productBatch = (ProductBatch) kmsg.getEntityList().get(0);
            productBatchId = productBatch.getId();
        }
        if (isSerialForProduct) {
            for (int i = 0; i < jArr.length(); i++) {
                HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
                pdfTemplateMap.put(Constants.Acc_id, jSONObject.getString("serialnoid"));
                pdfTemplateMap.put(Constants.companyKey, companyId);
                pdfTemplateMap.put("product", jSONObject.getString(Constants.productid));
                pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("serialno")));  //for saving the serial no its showing %20 in space
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    pdfTemplateMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    pdfTemplateMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }
                pdfTemplateMap.put("batch", productBatchId);
                pdfTemplateMap.put("transactiontype", "1");//This is product Type Tranction
                pdfTemplateMap.put("ispurchase", true);
                kmsg = accCommonTablesDAO.saveSerialForBatch(pdfTemplateMap);
            }
        }
        return productBatch;
    }

    public void saveAssetNewBatch(String batchJSON, String productId, HttpServletRequest request, String documentId) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {
        JSONArray jArr = new JSONArray(batchJSON);
        KwlReturnObject kmsg = null;
        double ActbatchQty = 1;
        double batchQty = 0;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        int serialsequence = 1 , batchsequence = 1; // for user selected sequence of batch and serial while creating Asset.
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
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
        NewProductBatch productBatch = null;
        String productBatchId = "";
        //Save Batch detail for both  option for serial no and batch also as if batch option is off then also we are generating batch in backend
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
            if (jSONObject.has("quantity") && !jSONObject.getString("quantity").equals("undefined") && !jSONObject.getString("quantity").isEmpty()) {
                ActbatchQty = jSONObject.getDouble("quantity");
            }
            if (batchQty == 0) {
                batchQty = jSONObject.getDouble("quantity");
            }
            if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct || isRowForProduct || isRackForProduct  || isBinForProduct ) && (batchQty == ActbatchQty)) {

                HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                pdfTemplateMap.put(Constants.companyKey, companyid);
                pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("batch")));
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                    pdfTemplateMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                    pdfTemplateMap.put("expdate", df.parse(jSONObject.getString("expdate")));
                }
                pdfTemplateMap.put("quantity", jSONObject.getString("quantity"));
                pdfTemplateMap.put("balance", jSONObject.getString("balance"));
                pdfTemplateMap.put("location", jSONObject.getString("location"));
                pdfTemplateMap.put("row", jSONObject.getString("row"));
                pdfTemplateMap.put("rack", jSONObject.getString("rack"));
                pdfTemplateMap.put("bin", jSONObject.getString("bin"));
                pdfTemplateMap.put("product", productId);
                pdfTemplateMap.put("warehouse", jSONObject.getString("warehouse"));
                pdfTemplateMap.put("isopening", true);
                pdfTemplateMap.put("transactiontype", "1");//This is product Type Tranction  
                pdfTemplateMap.put("ispurchase", true);
                kmsg = accCommonTablesDAO.saveNewBatchForProduct(pdfTemplateMap);

                if (kmsg != null && kmsg.getEntityList().size() != 0) {
                    productBatch = (NewProductBatch) kmsg.getEntityList().get(0);
                    productBatchId = productBatch.getId();
                }
//               
                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", jSONObject.getString("quantity"));
                documentMap.put("batchmapid", productBatchId);
                documentMap.put("documentid", documentId);
                documentMap.put("transactiontype", "1");//This is product Type Tranction  
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                    documentMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                    documentMap.put("expdate", df.parse(jSONObject.getString("expdate")));
                }
                
                /**
                 * added selected sequence for batch selected by user while
                 * creating Asset.
                 */
                documentMap.put("batchsequence", batchsequence++);
                accCommonTablesDAO.saveBatchDocumentMapping(documentMap);
            }
            batchQty--;


            if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 

                HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                 if(isEdit){
                    pdfTemplateMap.put(Constants.Acc_id, "");
                } else {
                    pdfTemplateMap.put(Constants.Acc_id, jSONObject.getString("serialnoid"));
                }
                pdfTemplateMap.put(Constants.companyKey,companyid);
                pdfTemplateMap.put("product", productId);
                pdfTemplateMap.put("name", StringUtil.DecodeText(jSONObject.optString("serialno")));
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    pdfTemplateMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    pdfTemplateMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }
                pdfTemplateMap.put("batch", productBatchId);
                pdfTemplateMap.put("transactiontype", "1");//This is product Type Tranction  
                pdfTemplateMap.put("quantity", "1");
                pdfTemplateMap.put("ispurchase", true);
                pdfTemplateMap.put("isopening", true);
                pdfTemplateMap.put("skuvalue",jSONObject.getString("skufield") );
                /**
                 * added selected sequence for serial selected by user while
                 * creating Asset.
                 */
                pdfTemplateMap.put("serialsequence", serialsequence++);
                kmsg = accCommonTablesDAO.saveNewSerialForBatch(pdfTemplateMap);
                String serialDetailsId = "";
                if (kmsg != null && kmsg.getEntityList().size() != 0) {
                    NewBatchSerial serialDetails = (NewBatchSerial) kmsg.getEntityList().get(0);
                    serialDetailsId = serialDetails.getId();
                }

                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", 1);
                documentMap.put("serialmapid", serialDetailsId);
                documentMap.put("documentid", documentId);
                documentMap.put("transactiontype", "1");//This is product Type Tranction  
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    documentMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    documentMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }

                accCommonTablesDAO.saveSerialDocumentMapping(documentMap);

            }else{
               batchQty=0; 
            }
        }
    }

    public ModelAndView saveProductCategoryMapping(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        // Moved save product category code to AccProductModuleServiceImpl.java
        if(false){
        boolean isAccountingExe = false;
        boolean isSameIndCode = true;
        String auditMsg = "", auditID = "",productName="",productId="",codeVal="",msg="";

        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
//            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] productList = request.getParameter("productList").split(",");
            String[] productCategory = request.getParameter("productCategory").split(",");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            String industryCode="";
            if (!StringUtil.isNullOrEmpty(request.getParameter("industryCode"))&& !request.getParameter("industryCode").equals("-1")) {
                industryCode = request.getParameter("industryCode");
                KwlReturnObject indCode = accProductObj.getObject(MasterItem.class.getName(), industryCode);
                MasterItem code = (MasterItem) indCode.getEntityList().get(0);
                if (code!=null) {
                    codeVal = code.getValue();
                    for (int i = 0; i < productCategory.length; i++) {
                        KwlReturnObject category = accProductObj.getObject(MasterItem.class.getName(), productCategory[i]);
                        MasterItem categoryIndCode = (MasterItem) category.getEntityList().get(0);
                        if (!productCategory[i].equalsIgnoreCase("None")) {
                            if (categoryIndCode.getIndustryCodeId()!=null&&!categoryIndCode.getIndustryCodeId().equals(industryCode)&&!categoryIndCode.getIndustryCodeId().equals("-1")) {
                                isSameIndCode = false;
                            }
                        }
                    }
                }
                if (!isSameIndCode) {
                    isAccountingExe = true;
                    throw new AccountingException(messageSource.getMessage("acc.productcate.code", null, RequestContextUtils.getLocale(request)));
                }
            }
             
            if (productList.length > 0) {
                for (int i = 0; i < productList.length; i++) {
                    if (!StringUtil.isNullOrEmpty(productList[i])) {
                        accProductObj.deleteProductCategoryMappingDtails(productList[i]);
                    }
                }
            }

            if (productList.length > 0 && productCategory.length > 0) {
                for (int i = 0; i < productList.length; i++) {
                    for (int j = 0; j < productCategory.length; j++) {
                        if (!StringUtil.isNullOrEmpty(productList[i]) && !StringUtil.isNullOrEmpty(productCategory[j])) {
                            String productCategoryId=StringUtil.equal(productCategory[j], "None") ? null :productCategory[j];
                            accProductObj.saveProductCategoryMapping(productList[i], productCategoryId);
                           
                            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), productList[i]);
                            Product product = (Product) prodresult.getEntityList().get(0);

                            KwlReturnObject categoryresult = accProductObj.getObject(MasterItem.class.getName(), productCategory[j]);
                            MasterItem ccategory = (MasterItem) categoryresult.getEntityList().get(0);
                            String categoryName=StringUtil.equal(productCategory[j], "None") ? "None" :ccategory.getValue();
                            productName=product.getName();
                            productId=product.getID();
                            auditMsg = " added new product category " + categoryName + " to ";
                            auditID = AuditAction.PRODUCT_CATEGORY_CHANGED;

                            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + auditMsg + product.getName(), request, product.getID());
                        }
                    }
                     auditMsg = " Updated new Industry Code " + codeVal + " to ";
                     auditID = AuditAction.PRODUCT_INDUSRTYCODE_CHANGED;
                    if (!StringUtil.isNullOrEmpty(request.getParameter("industryCode"))&&Integer.parseInt(company.getCountry().getID()) == Constants.malaysian_country_id) {
                        accProductObj.updateProductCategoryIndustryCode(productList[i], industryCode);
                        auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + auditMsg + productName, request, productId);
                    }
                    
                }
            }
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception e) {
            msg = "" + e.getMessage();
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put("accException", isAccountingExe);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        } else {
            String msg = "";
            try {
                JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);

                jobj = accProductModuleService.saveProductCategoryMapping(paramJobj);

            } catch (Exception ex) {
                issuccess = false;
                msg = "" + ex.getMessage();
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveQualityControlData(HttpServletRequest request, Map<String, Object> reqParams) throws ServiceException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String qualitycontroldetailjson = "";
            JSONArray qualitycontroljsonArr = null;
            if(!StringUtil.isNullOrEmpty(request.getParameter("qualitycontroldetailjson"))){
                qualitycontroldetailjson = request.getParameter("qualitycontroldetailjson");
                qualitycontroljsonArr = new JSONArray("["+qualitycontroldetailjson+"]");
            }

            Product product = null;
            if(reqParams.containsKey("product") && reqParams.get("product")!=null){
                product = (Product) reqParams.get("product");
            }

            if(product!=null && qualitycontroljsonArr!=null){
                //Delete quality control data
                accProductObj.deleteQualityControlData(product.getID());

                for (int i = 0; i < qualitycontroljsonArr.length(); i++) {
                    JSONObject jobj = qualitycontroljsonArr.getJSONObject(i);
                    Map<String, Object> qcMap = new HashMap();
                    qcMap.put("qcid", jobj.get("qcid"));

                    String qcbomcode = "";
                    String qcbomcodeid ="";
                    if(jobj.has("qcbomcodeid") && jobj.get("qcbomcodeid")!=null){
                        qcbomcodeid = jobj.get("qcbomcodeid").toString();
                    }
                    if(StringUtil.isNullOrEmpty(qcbomcodeid)){
                        if(jobj.has("qcbomcode") && jobj.get("qcbomcode")!=null && jobj.get("qcbomcode")!=""){
                            qcbomcode = jobj.get("qcbomcode").toString();
                            Map<String, Object> requestParams = new HashMap();
                            requestParams.put(Constants.productid, product.getID());
                            KwlReturnObject returnObject = accProductObj.getBOMDetail(requestParams);
                            List<BOMDetail> list = returnObject.getEntityList();
                            for (BOMDetail bomDetail : list) {
                                if(qcbomcode.equals(bomDetail.getBomCode())){
                                    qcbomcodeid = bomDetail.getID();
                                    break;
                                }
                            }
                        }
                    }
                    qcMap.put("qcbomcodeid", qcbomcodeid);
                    qcMap.put("qcgroupid", jobj.get("qcgroupid"));
                    qcMap.put("qcuom", jobj.get("qcuom"));
                    qcMap.put("qcparameterid", jobj.get("qcparameterid"));
                    qcMap.put("qcvalue", jobj.get("qcvalue"));
                    qcMap.put("qcdescription", jobj.get("qcdescription"));
                    qcMap.put(Constants.productid, product.getID());
                    qcMap.put(Constants.companyKey, companyid);
                    KwlReturnObject qcResult = accProductObj.saveQualityControlData(qcMap);
                }
            }
        } catch (SessionExpiredException | JSONException | ServiceException ex) {
            throw ServiceException.FAILURE("saveQualityControlData : " + ex.getMessage(), ex);
        }
    }

    public ModelAndView getQualityControlData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String productid = "";
            if(!StringUtil.isNullOrEmpty(request.getParameter(Constants.productid))){
                productid = request.getParameter(Constants.productid);
            }
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.productid, productid);
            KwlReturnObject result = accProductObj.getQualityControlData(requestParams);
            List<QualityControl> list = result.getEntityList();
            jobj = accProductModuleService.getQualityControlJSON(list);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (ServiceException | SessionExpiredException | JSONException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     public ModelAndView isDefaultSeuenceFormatSetForBuildAssembly(HttpServletRequest request, HttpServletResponse response) {
         JSONObject jobj = new JSONObject();
         boolean issuccess = false;
         boolean isDefaultSeuenceFormatSetForBuildAssembly = false;
         String msg = "";
         try {
             JSONObject requestParams = new JSONObject();
             requestParams = StringUtil.convertRequestToJsonObject(request);
             isDefaultSeuenceFormatSetForBuildAssembly = accProductObj.isDefaultSeuenceFormatSetForBuildAssembly(requestParams);
             jobj.put("isDefaultSeuenceFormatSetForBuildAssembly", isDefaultSeuenceFormatSetForBuildAssembly);
             issuccess = true;
         } catch (JSONException ex) {
             msg = "" + ex.getMessage();
             Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (AccountingException ex) {
             msg = "" + ex.getMessage();
             Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SessionExpiredException ex) {
             msg = "" + ex.getMessage();
             Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 jobj.put(Constants.RES_success, issuccess);
                 jobj.put(Constants.RES_msg, msg);
                 jobj.put("isDefaultSeuenceFormatSetForBuildAssembly", isDefaultSeuenceFormatSetForBuildAssembly);
             } catch (JSONException ex) {
                 Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return new ModelAndView("jsonView", "model", jobj.toString());
     }

//Product Assembly
    public void saveAssemblyProduct(HttpServletRequest request, Product assemblyProduct, boolean isRebuild,boolean isMRPModuleActivated) throws ServiceException {
        String jsondata = request.getParameter("assembly");
        String bomdetailjson = request.getParameter("bomdetailjson");
        try {
            // If assembly data is not present in case of saving product from product view. then continue execution.
            boolean isFromProductView = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isFromProductView"))) {
                isFromProductView = Boolean.parseBoolean(request.getParameter("isFromProductView"));
            }
            if (isFromProductView && (jsondata == null || bomdetailjson == null)) {
                return;
            }
            JSONArray jarr = new JSONArray("[" + jsondata + "]");
            JSONArray bomdetailjsonArr = new JSONArray("[" + bomdetailjson + "]");
//            if (isRebuild) {
//                updateBillofMaterialsInventory(request, assemblyProduct);
//            }
            accProductObj.deleteProductAssembly(assemblyProduct.getID());
            if (!isMRPModuleActivated) { 
                accProductObj.deleteBOMDetail(assemblyProduct.getID());
            }
            if (isMRPModuleActivated) {
                for (int i = 0; i < bomdetailjsonArr.length(); i++) {
                    JSONObject jobj = bomdetailjsonArr.getJSONObject(i);
                    Map<String, Object> bomMap = new HashMap<String, Object>();
                    boolean isClone=(StringUtil.isNullOrEmpty(request.getParameter("isClone"))?false:Boolean.parseBoolean(request.getParameter("isClone")));
                    String id = jobj.optString("bomid","");
                    if (!StringUtil.isNullOrEmpty(id) && !isClone) {
                        bomMap.put(Constants.Acc_id, id);
                    }
                    bomMap.put("bomCode", jobj.get("bomCode"));
                    bomMap.put("bomName", jobj.get("bomName"));
                    bomMap.put("isdefaultbom", jobj.get("isdefaultbom"));
                    bomMap.put(Constants.productid, assemblyProduct.getID());
                    KwlReturnObject bomdetailResult = accProductObj.saveBOMDetail(bomMap);
                    BOMDetail bomdetail = bomdetailResult.getEntityList() != null ? (BOMDetail) bomdetailResult.getEntityList().get(0) : null;
                    JSONArray assemblyArray = jobj.getJSONArray("bomAssemblyDetails");
                    for(int j=0;j<assemblyArray.length();j++){
                        JSONObject assemblyjobj = assemblyArray.getJSONObject(j);
                        if(!StringUtil.isNullOrEmpty(assemblyjobj.optString(Constants.productid,""))){
                            HashMap<String, Object> assemblyMap = new HashMap<String, Object>();
                            assemblyMap.put(Constants.productid, assemblyProduct.getID());
                            assemblyMap.put("subproductid", assemblyjobj.getString(Constants.productid));
                            assemblyMap.put("quantity", Double.parseDouble(assemblyjobj.getString("quantity")));
                            assemblyMap.put("percentage", Double.parseDouble(assemblyjobj.optString("percentage", "0")));
                            assemblyMap.put("actualquantity", Double.parseDouble(assemblyjobj.optString("actualquantity", "0")));
                            assemblyMap.put("inventoryquantiy", Double.parseDouble(assemblyjobj.optString("inventoryquantiy", "0")));
                            assemblyMap.put("recylequantity", Double.parseDouble(assemblyjobj.optString("recylequantity", "").equals("") ? "0" : assemblyjobj.optString("recylequantity", "0")));
                            assemblyMap.put("remainingquantity", Double.parseDouble(assemblyjobj.optString("remainingquantity", "").equals("") ? "0" : assemblyjobj.optString("remainingquantity", "")));
                            assemblyMap.put("wastageInventoryQuantity", Double.parseDouble(assemblyjobj.optString("wastageInventoryQuantity", "0")));
                            assemblyMap.put("wastageQuantityType", Integer.parseInt(assemblyjobj.optString("wastageQuantityType", "0")));
                            assemblyMap.put("wastageQuantity", Double.parseDouble(assemblyjobj.optString("wastageQuantity", "0")));
                            assemblyMap.put("bomdetailid", bomdetail != null ? bomdetail.getID() : "");
                            if (!assemblyjobj.optString("subbomid", "").equals("")) {
                                assemblyMap.put("subbomid", assemblyjobj.optString("subbomid", ""));
                            }
                            
                            if (!StringUtil.isNullOrEmpty(assemblyjobj.optString("crate", "0")))
                            {
                                assemblyMap.put("crate", Double.parseDouble(assemblyjobj.optString("crate","0")));
                            }
                            if (!StringUtil.isNullOrEmpty(assemblyjobj.optString("componentType", "0")))
                            {
                            assemblyMap.put("componentType", Integer.parseInt(assemblyjobj.optString("componentType","0")));
                            
                            }
                           
                            accProductObj.saveProductAssembly(assemblyMap);
                        }
                    }
                }
            } else {
                boolean isDefaultBOMAdded=false;
                BOMDetail bomdetail = null;
                /*
                This will return default Bom Id only when it is used in transaction 
                further  we are updating default bom id of product so there is no duplicate default Bom for product  
                */
                String DefaultBOMId;
                DefaultBOMId = accProductObj.getDefaultBomId(assemblyProduct.getID());
                for (int i = 0; i < jarr.length(); i++) {
                    if (!isDefaultBOMAdded) {
                        Map<String, Object> bomMap = new HashMap<String, Object>();
                        bomMap.put(Constants.productid, assemblyProduct.getID());
                        if (!DefaultBOMId.isEmpty()){
                            bomMap.put("id", DefaultBOMId);
                        }
                        bomMap.put("assigndefaultbomcode", true);
                        KwlReturnObject bomdetailResult = accProductObj.saveBOMDetail(bomMap);
                        bomdetail = bomdetailResult.getEntityList() != null ? (BOMDetail) bomdetailResult.getEntityList().get(0) : null;
                        isDefaultBOMAdded = true;
                    }
                    JSONObject jobj = jarr.getJSONObject(i);
                    HashMap<String, Object> assemblyMap = new HashMap<String, Object>();
                    try {
                        KwlReturnObject productList = accProductObj.getProductByID(jobj.getString("product"), sessionHandlerImpl.getCompanyid(request));
                        Product subproduct = productList.getEntityList() != null ? (Product) productList.getEntityList().get(0) : null;
                        if (subproduct != null) {
                            if (subproduct.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                                Map<String, Object> subBOMMap = new HashMap<String, Object>();
                                subBOMMap.put(Constants.productid, subproduct.getID());
                                subBOMMap.put("isdefaultbom", true);
                                KwlReturnObject subReturnObject = accProductObj.getBOMDetail(subBOMMap);
                                if (subReturnObject != null && subReturnObject.getEntityList() != null && !subReturnObject.getEntityList().isEmpty()) {
                                    BOMDetail subbom = (BOMDetail) subReturnObject.getEntityList().get(0);
                                    assemblyMap.put("subbomid", subbom.getID());
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(accProductController.class.getName()).log(Level.INFO, ex.getMessage());
                    }
                    assemblyMap.put(Constants.productid, assemblyProduct.getID());
                    assemblyMap.put("subproductid", jobj.getString("product"));
                    assemblyMap.put("quantity", Double.parseDouble(jobj.getString("quantity")));
                    assemblyMap.put("percentage", Double.parseDouble(jobj.optString("percentage", "0")));
                    assemblyMap.put("actualquantity", Double.parseDouble(jobj.optString("actualquantity", "0")));
                    assemblyMap.put("inventoryquantiy", Double.parseDouble(jobj.optString("inventoryquantiy", "0")));
                    assemblyMap.put("recylequantity", Double.parseDouble(jobj.optString("recylequantity", "0")));
                    assemblyMap.put("remainingquantity", Double.parseDouble(jobj.optString("remainingquantity", "0")));
                    assemblyMap.put("wastageInventoryQuantity", Double.parseDouble(jobj.optString("wastageInventoryQuantity", "0")));
                    assemblyMap.put("wastageQuantityType", Integer.parseInt(jobj.optString("wastageQuantityType", "0")));
                    assemblyMap.put("wastageQuantity", Double.parseDouble(jobj.optString("wastageQuantity", "0")));
                    assemblyMap.put("bomdetailid", bomdetail != null ? bomdetail.getID() : "");
                    accProductObj.saveProductAssembly(assemblyMap);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveAssemblyProduct : " + ex.getMessage(), ex);
        }
    }
    public ModelAndView deleteBOMByID(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("DELBOM_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        try {
            status = txnManager.getTransaction(def);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String bomid = "";
            if (request.getParameter("bomid") != null) {
                bomid = request.getParameter("bomid");
            }
            try {
                accProductObj.deleteProductAssemblyByBOMDetailId(bomid);
                accProductObj.deleteBOMDetailById(bomid);
                issuccess = true;
                msg = "BOM deleted successfully.";
                txnManager.commit(status);
            } catch (DataIntegrityViolationException divex) {
                throw new AccountingException("Cannot delete BOM as it may be used in some transaction or in Quality Check Parameters.");
            } catch (Exception ex) {
                throw ex;
            }
        } catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveProductComposition(HttpServletRequest request, Product product, String companyid) throws ServiceException {
        String jsondata = request.getParameter("productcompositionjson");
        try {
            JSONArray jarr = new JSONArray("[" + jsondata + "]");
            accProductObj.deleteProductComposition(product.getID());
            for (int i = 0; i < jarr.length(); i++) {
                JSONObject jobj = jarr.getJSONObject(i);
                HashMap<String, Object> productCompositionMap = new HashMap<String, Object>();
                productCompositionMap.put(Constants.productid, product.getID());
                productCompositionMap.put("srno", jobj.optInt("srno"));
                productCompositionMap.put("ingredients", jobj.getString("ingredients"));
                productCompositionMap.put("strength",jobj.getString("strength"));
                productCompositionMap.put(Constants.companyKey,companyid);
                accProductObj.saveProductComposition(productCompositionMap);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveProductComposition : " + ex.getMessage(), ex);
        }
    }

    public HashMap<String, Object> getAssemblyRequestParams(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("assembly", request.getParameter("assembly"));
        requestParams.put("applydate", request.getParameter("applydate"));
        requestParams.put("quantity", Double.parseDouble(request.getParameter("quantity") == null ? "0" : request.getParameter("quantity").toString()));
        return requestParams;
    }

    public ModelAndView getAssemblyItems(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            /*
            Initial price should be displayed while creating the assembly product so in edit and close case also initial price will be displayed.
            */
            boolean displayInitialPrice=false;//For creation of assembly ptoducts it will be changed to true where we will display initial purchase price otherwise current price will be displayed
            requestParams.put(Constants.productid, request.getParameter(Constants.productid));
            requestParams.put(Constants.currencyKey, sessionHandlerImpl.getCurrencyID(request));
            if(!StringUtil.isNullOrEmpty(request.getParameter("isdefaultbom"))){
                boolean isdefaultbom=Boolean.parseBoolean(request.getParameter("isdefaultbom"));
                requestParams.put("isdefaultbom", isdefaultbom);
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("bomdetailid"))){ //This is for Multiple MRP BOM Formula
                requestParams.put("bomdetailid", request.getParameter("bomdetailid"));
            } else if(!StringUtil.isNullOrEmpty(request.getParameter("bomid"))){    //This is for selected BOM formula in Build Assembly on Build Quantity selection
                requestParams.put("bomdetailid", request.getParameter("bomid"));
            }
            boolean isForCompAvailablity=false;
             if(!StringUtil.isNullOrEmpty(request.getParameter("isForCompAvailablity"))){
                isForCompAvailablity=Boolean.parseBoolean(request.getParameter("isForCompAvailablity"));  
            }
             if(!StringUtil.isNullOrEmpty(request.getParameter("rendermode"))){//render mode check comes from ProductAssemblyGrid.js
                if("productform".equals((String)request.getParameter("rendermode"))){
                    displayInitialPrice=true;
                }
            }
             
            requestParams.put("isForCompAvailablity", isForCompAvailablity);
            requestParams.put("displayInitialPrice", displayInitialPrice);
            KwlReturnObject result = accProductObj.getAssemblyItems(requestParams);
            /*Get request parameters */
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put(Constants.displayInitialPrice, displayInitialPrice);
            jobj = accProductModuleService.getAssemblyItemsJson(paramJobj, result.getEntityList(),0);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getBOMDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.productid, request.getParameter(Constants.productid));
            requestParams.put(Constants.currencyKey, sessionHandlerImpl.getCurrencyID(request));
            if(!StringUtil.isNullOrEmpty(request.getParameter("isdefaultbom"))){
                boolean isdefaultbom=Boolean.parseBoolean(request.getParameter("isdefaultbom"));
                requestParams.put("isdefaultbom", isdefaultbom);
            }
            KwlReturnObject result = accProductObj.getBOMDetail(requestParams);
            List<BOMDetail> list = result.getEntityList();
            jobj = accProductModuleService.getBOMDetailJSON(request, list);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getPriceOnSerialNo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONArray jarr=getPriceOnSerialNo(request);
            jobj.put(Constants.RES_data,jarr);
            jobj.put("count",jarr.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getPriceOnSerialNo(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONObject tmpobj= new JSONObject();
        JSONArray jarr=new JSONArray();
        try {
            if(!StringUtil.isNullOrEmpty(request.getParameter("serialdetails"))){
                JSONArray jArr = new JSONArray(request.getParameter("serialdetails"));
            if(jArr!=null){
                    HashMap<String, String> hm = new HashMap<String, String>();
                    for (int i = 0; i < jArr.length(); i++) {
                        tmpobj = jArr.getJSONObject(i);
                        String pid = tmpobj.getString(Constants.productid);
                        if (hm.containsKey(pid)) {
                    String serialid = hm.get(pid) +','+tmpobj.getString("serailno");
                            hm.put(pid, serialid);
                        } else {
                            hm.put(pid, tmpobj.getString("serailno"));
                        }
                    }
            Set keyset=hm.keySet();
            Iterator<String> ite= keyset.iterator();
            while(ite.hasNext()){
                double price=0;
                JSONObject tmpObj=new JSONObject();
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                String productid=ite.next();
                        String serial = hm.get(productid);
                        requestParams.put(Constants.productid, productid);
                        String[] serialId = serial.split(",");
                        for (int j = 0; j < serialId.length; j++) {
                            requestParams.put("serialId", serialId[j]);
                            KwlReturnObject result = accProductObj.getDocumentBySerialId(requestParams);
                            List docList = result.getEntityList();
                            if (docList.size() > 0) {
                                String documentId = (String) docList.get(0);
                                KwlReturnObject accresultSerial = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), serialId[j]);
                                NewBatchSerial newBatchSerial = (NewBatchSerial) accresultSerial.getEntityList().get(0);
                                if (!newBatchSerial.isIsopening()) {
                                    price += accProductObj.getPriceByDocumentId(documentId);
                                } else {
                                    KwlReturnObject priceResult = accProductObj.getProductPrice(productid, true, null, "", "");
                                    List<Object> priceList = priceResult.getEntityList();
                                    double proPrice = 0;
                                    if (priceList != null) {
                                        for (Object cogsval : priceList) {
                                            proPrice = (cogsval == null ? 0.0 : (Double) cogsval);
                                        }
                                    }
                                    price += proPrice;
                                }
                            }
                        }
                tmpObj.put("price",price/serialId.length);
                tmpObj.put(Constants.productid,productid);
                        jarr.put(tmpObj);
                    }
            }}
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getAssemblyItemsJson : " + ex.getMessage(), ex);
        }
        return jarr;
    }

    public ModelAndView getProductBatchDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String Productid= request.getParameter(Constants.productid);
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            KwlReturnObject result = accProductObj.getAssemblyBuidDetails(Productid);
            if(isEdit){
                jobj = getProductBuildEditDetailsJson(request, result.getEntityList());
            }
            else{
                jobj = getProductBuildDetailsJson(request, result.getEntityList()); //Assembly Report Call
            }
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getProductBuildDetailsJson(HttpServletRequest request, List list) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jArr = new JSONArray();
            // get batch serial details for sub product
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            Map <String,JSONObject> serialDetails = accProductModuleService.getBatchSerialDetailsOfAssembyProduct(requestJobj);
            
            Iterator itr = list.iterator();
            JSONObject detailsJobj = null;
            while (itr.hasNext()) {
                ProductBuildDetails pbd = (ProductBuildDetails) itr.next();
                JSONObject obj = new JSONObject();
                obj.put(Constants.Acc_id, pbd.getID());
                obj.put(Constants.productid, pbd.getAproduct().getID());
                obj.put("productname", pbd.getAproduct().getName());
                obj.put("desc", pbd.getAproduct().getDescription());
                obj.put("producttype", pbd.getAproduct().getProducttype().getID());
                obj.put("type", pbd.getAproduct().getProducttype().getName());
                obj.put("purchaseprice", pbd.getRate());
//                obj.put("quantity", (pbd.getInventoryQuantity()+pbd.getRecycleQuantity()));
                double usedQty = (pbd.getBuild().getQuantity() * (pbd.getInventoryQuantity()+pbd.getRecycleQuantity()));
                obj.put("quantity", usedQty);   //SDP-11958
                obj.put("actualquantity",(pbd.getInventoryQuantity()+pbd.getRecycleQuantity()));
                obj.put("inventoryquantiy", pbd.getInventoryQuantity());
                obj.put("recyclequantity", pbd.getRecycleQuantity());
                obj.put("wastageInventoryQuantity", pbd.getWastageInventoryQuantity());
                obj.put("wastageQuantityType", pbd.getWastageQuantityType());
                obj.put("wastageQuantity", pbd.getWastageQuantity());    

                if (serialDetails.containsKey(pbd.getID())) {
                    detailsJobj = serialDetails.get(pbd.getID());
                    obj.put("warehouse", detailsJobj.optString("warehouse"));
                    obj.put("location", detailsJobj.optString("location"));
                    obj.put("batch", detailsJobj.optString("batch"));
                    obj.put("serial", detailsJobj.optString("serial"));
                }
//                obj.put("percentage", passembly.getPercentage());
//                obj.put("recylequantity", passembly.getRecycleQuantity());
//                obj.put("remainingquantity", passembly.getRemainingQuantity());
                // obj.put("onhand", row[3]==null?0:row[3]);
//                Double availableQty = (Double) (row[3] == null ? 0 : row[3]);  //iis the actual available wuantity for product
//                KwlReturnObject result2 = accProductObj.getAssemblyLockQuantityForBuild(mainProductid, subProductid);
//                Double assmblyLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));  //it is the lock quantity in assembly product locked in SO
//
//                KwlReturnObject result1 = accProductObj.getLockQuantity(subProductid); //for geting a locked quantity of inventory product used in salesorder
//                Double SoLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));   //it is the lock quantity of product locked in SO
//                Double lockquantity = assmblyLockQuantity + SoLockQuantity;   //total lock quantity
//                obj.put("lockquantity", lockquantity);
//                obj.put("onhand", availableQty - lockquantity);   //its actual quantity available for user

                jArr.put(obj);
            }
            jobj.put(Constants.RES_data, jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getAssemblyItemsJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public JSONObject getProductBuildEditDetailsJson(HttpServletRequest request, List list) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            String mainProductid = request.getParameter("mainproduct");
            JSONArray jArr = new JSONArray();
            Iterator itr = list.iterator();


            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.productid, mainProductid);
            requestParams.put(Constants.currencyKey, sessionHandlerImpl.getCurrencyID(request));
            KwlReturnObject result = accProductObj.getAssemblyItems(requestParams);
            Iterator assemblyItr = result.getEntityList().iterator();

            while (itr.hasNext()) {
                Object[] row = (Object[]) assemblyItr.next();
                ProductBuildDetails pbd = (ProductBuildDetails) itr.next();
                JSONObject obj = new JSONObject();
                String subProductid = pbd.getAproduct().getID();
                obj.put(Constants.Acc_id, pbd.getID());
                obj.put(Constants.productid, pbd.getAproduct().getID());
                obj.put("productname", pbd.getAproduct().getName());
                obj.put("desc", pbd.getAproduct().getDescription());
                obj.put("producttype", pbd.getAproduct().getProducttype().getID());
                obj.put("type", pbd.getAproduct().getProducttype().getName());
                obj.put("purchaseprice", pbd.getRate());
                obj.put("quantity", (pbd.getInventoryQuantity() + pbd.getRecycleQuantity()));
                obj.put("actualquantity", (pbd.getInventoryQuantity() + pbd.getRecycleQuantity()));
                obj.put("inventoryquantiy", pbd.getInventoryQuantity());
                obj.put("recyclequantity", pbd.getRecycleQuantity());
                obj.put("percentage", pbd.getPercentage());
                Double availableQty = (Double) (row[3] == null ? 0 : row[3]);  //iis the actual available wuantity for product
                KwlReturnObject result2 = accProductObj.getAssemblyLockQuantityForBuild(mainProductid, subProductid);
                Double assmblyLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));  //it is the lock quantity in assembly product locked in SO

                KwlReturnObject result1 = accProductObj.getLockQuantity(subProductid); //for geting a locked quantity of inventory product used in salesorder
                Double SoLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));   //it is the lock quantity of product locked in SO
                Double lockquantity = assmblyLockQuantity + SoLockQuantity;   //total lock quantity
                obj.put("onhand", availableQty - lockquantity);   //its actual quantity available for user

                obj.put("wastageInventoryQuantity", pbd.getWastageInventoryQuantity());
                obj.put("wastageQuantityType", pbd.getWastageQuantityType());
                obj.put("wastageQuantity", pbd.getWastageQuantity());
                jArr.put(obj);
            }
            jobj.put(Constants.RES_data, jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getAssemblyItemsJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView buildProductAssembly(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        TransactionStatus status=null;
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
        try{
            status = txnManager.getTransaction(def);
        }catch(Exception e){
            e.printStackTrace();
        }
        try {
            if(isEdit){
                deleteExistingBuildAssemblyEntries(request);
            }
            buildProductAssembly(request);
            String action = "added";
             String auditaction=AuditAction.PRODUCT_BUILD_ASSEMBLY_ADDED;
             if(isEdit){
                action = "updated";
                 auditaction=AuditAction.PRODUCT_BUILD_ASSEMBLY_UPDATED;
            }
            auditTrailObj.insertAuditLog(auditaction, "User "+ sessionHandlerImpl.getUserFullName(request) +" has "+action +" product build Assembly "+request.getParameter("refno"), request, request.getParameter("product"));
            issuccess = true;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            msg = storageHandlerImpl.GetVRnetCompanyId().contains(companyid) ? "Product's Bundles have been "+action+" successfully" : "Product's build Assembly have been "+action+" successfully";   //"Product's Assemblies have been updated successfully";
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void buildProductAssembly(HttpServletRequest request) throws ServiceException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String productid = request.getParameter("product");
            double quantity = Double.parseDouble(request.getParameter("quantity") == null ? "0" : request.getParameter("quantity").toString());
            HashMap<String, Object> assemblyParams = getAssemblyRequestParams(request);
            assemblyParams.put(Constants.memo, request.getParameter(Constants.memo));
            assemblyParams.put("refno", request.getParameter("refno"));
            assemblyParams.put("refno", request.getParameter("refno"));
            assemblyParams.put("cost", Double.parseDouble(request.getParameter("cost")));
            assemblyParams.put("buildproductid", request.getParameter("product"));
            assemblyParams.put("bomdetailid", request.getParameter("bomdetailid"));

            Product product = (Product) kwlCommonTablesDAOObj.getClassObject(Product.class.getName(), request.getParameter("product"));
            String assemblyserialJson = request.getParameter("ProductSerialJson");
            assemblyParams.put("batchjson", assemblyserialJson);
            String jsonSrtingArray[] = {};
            if (!StringUtil.isNullOrEmpty(assemblyserialJson)) {
                jsonSrtingArray = assemblyserialJson.replace(",[", "#,[").split("#,");          // split product into array
            }
            if (!StringUtil.isNullOrEmpty(assemblyserialJson)) {
                for (int i = 0; i < jsonSrtingArray.length; i++) {
                    String jsonStr = jsonSrtingArray[i];
//                    if (i != jsonSrtingArray.length - 1) {            // No need because of split without removing delimeter 
//                        jsonStr += "]";
//            }
                    saveProductBatch(jsonStr, product, request);
                }
            }

            String assemblyproductjson = (String) request.getParameter("assembly");
            JSONArray jarr = new JSONArray("[" + assemblyproductjson + "]");
            if (jarr.length() > 0) { //Bug Fixed #16851
                ProductBuild productBuild = accProductObj.updateAssemblyInventory(assemblyParams);
                for (int i = 0; i < jarr.length(); i++) {
                    JSONObject jobj = jarr.getJSONObject(i);
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("quantity", Double.parseDouble(request.getParameter("quantity") == null ? "0" : request.getParameter("quantity").toString()));
                    requestParams.put("assemblyjson", jobj.toString());
                    requestParams.put("buildproductid", productBuild.getID());
                    requestParams.put(Constants.companyKey, companyid);
                    List returnList = new ArrayList();
                    ProductBuildDetails productBuildDetails = accProductObj.updateAssemblyBuildDetails(requestParams,null,null, returnList);

                    for (int k = 0; k < jsonSrtingArray.length; k++) {
                        String jsonStr = jsonSrtingArray[k];
                        if (k != jsonSrtingArray.length - 1) {
                            jsonStr += "]";
                        }

                        JSONArray batchProductjArr = new JSONArray(jsonStr);
                        JSONObject jSONObject = new JSONObject(batchProductjArr.get(0).toString());

                        String subassemblyserialJson = jSONObject.optString("subproduct");
                        String subassemblyjsonStringArray[] = {};
                        String subassemblyjsonStr = "";

                        if (!StringUtil.isNullOrEmpty(subassemblyserialJson)) {
                            subassemblyjsonStringArray = subassemblyserialJson.split("]\",\"");
                        }
                        for (int j = 0; j < subassemblyjsonStringArray.length; j++) {
                            subassemblyjsonStr = subassemblyjsonStringArray[j];
                            if (j != subassemblyjsonStringArray.length - 1) {
                                subassemblyjsonStr += "]";
                            }
                            JSONArray jArr = new JSONArray(subassemblyjsonStr);
                            String Serialproductid = "";
                            for (int p = 0; p < jArr.length(); p++) {
                                JSONObject jSONSerialObject = new JSONObject(jArr.get(p).toString());
                                Serialproductid = jSONSerialObject.getString(Constants.productid);

                            }
                            if (Serialproductid.equals(productBuildDetails.getAproduct().getID())) {
                                saveNewAssemblyBatch(jArr.toString(), Serialproductid, request, productBuildDetails.getID());
                            }
                        }
                    }
                }
            }

            JSONObject inventoryjson = new JSONObject();
            inventoryjson.put(Constants.productid, productid);
            inventoryjson.put("quantity", quantity);
            inventoryjson.put("baseuomquantity", quantity);
            inventoryjson.put("baseuomrate", 1);
            if (product.getUnitOfMeasure() != null) {
                inventoryjson.put("uomid", product.getUnitOfMeasure().getID());
            }
            inventoryjson.put("description", "Build Assembly");
            inventoryjson.put("carryin", true);
            inventoryjson.put("defective", false);
            inventoryjson.put("newinventory", false);
            inventoryjson.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
        } catch (Exception e) {
            throw ServiceException.FAILURE("buildProductAssembly : " + e.getMessage(), e);
        }
    }

    public void saveNewAssemblyBatch(String batchJSON, String productIdStr, HttpServletRequest request, String documentId) throws JSONException, ParseException, SessionExpiredException, ServiceException, UnsupportedEncodingException, AccountingException {
        JSONArray jArr = new JSONArray(batchJSON);
        String purchasebatchid = "";
        KwlReturnObject kmsg = null;
        double ActbatchQty = 1;
        double batchQty = 0;
        boolean isBatch = false;
        boolean isserial = false;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
        isBatch = preferences.isIsBatchCompulsory();
        isserial = preferences.isIsSerialCompulsory();

        if (!StringUtil.isNullOrEmpty(productIdStr)) {
            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), productIdStr);
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
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
            if (jSONObject.has("quantity") && !jSONObject.getString("quantity").equals("undefined") && !jSONObject.getString("quantity").isEmpty()) {
                ActbatchQty = jSONObject.getDouble("quantity");
            }
            if (batchQty == 0) {
                batchQty = jSONObject.getDouble("quantity");
            }
            if ((isLocationForProduct || isWarehouseForProduct || isBatchForProduct  || isRowForProduct || isRackForProduct  || isBinForProduct) && (batchQty == ActbatchQty)) {
                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", jSONObject.getString("quantity"));
                documentMap.put("documentid", documentId);
                documentMap.put("transactiontype", "27");//This is GRN Type Tranction  
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                    documentMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                    documentMap.put("expdate", df.parse(jSONObject.getString("expdate")));
                }
                documentMap.put("batchmapid", jSONObject.getString("purchasebatchid"));

                if (!isBatchForProduct && !isSerialForProduct) {
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                    filter_names.add("company.companyID");
                    filter_params.add(sessionHandlerImpl.getCompanyid(request));

                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("warehouse"))) {
                        String warehouse = jSONObject.getString("warehouse");
                        filter_names.add("warehouse.id");
                        filter_params.add(warehouse);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("location"))) {
                        String location = jSONObject.getString("location");
                        filter_names.add("location.id");
                        filter_params.add(location);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("row"))) {
                        String row = jSONObject.getString("row");
                        filter_names.add("row.id");
                        filter_params.add(row);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("rack"))) {
                        String rack = jSONObject.getString("rack");
                        filter_names.add("rack.id");
                        filter_params.add(rack);
                    }
                    if (!StringUtil.isNullOrEmpty(jSONObject.getString("bin"))) {
                        String bin = jSONObject.getString("bin");
                        filter_names.add("bin.id");
                        filter_params.add(bin);
                    }

                    filter_names.add("product");
                    filter_params.add(productIdStr);

                    filterRequestParams.put(Constants.filterNamesKey, filter_names);
                    filterRequestParams.put(Constants.filterParamsKey, filter_params);
                    filterRequestParams.put("order_by", order_by);
                    filterRequestParams.put("order_type", order_type);
                    KwlReturnObject result = accMasterItemsDAOobj.getNewBatches(filterRequestParams,false,false);
                    List listResult = result.getEntityList();
                    Iterator itrResult = listResult.iterator();
                    Double quantityToDue = ActbatchQty;
                    while (itrResult.hasNext()) {
                        if (quantityToDue > 0) {
                            NewProductBatch newProductBatch = (NewProductBatch) itrResult.next();
                            double dueQty = newProductBatch.getQuantitydue();
                            HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                            batchUpdateQtyMap.put(Constants.Acc_id, newProductBatch.getId());
                            if (dueQty > 0) {
                                if (quantityToDue > dueQty) {
                                    batchUpdateQtyMap.put("qty", String.valueOf(-(dueQty)));
                                    quantityToDue = quantityToDue - dueQty;

                                } else {
                                    batchUpdateQtyMap.put("qty", String.valueOf(-(quantityToDue)));
                                    quantityToDue = quantityToDue - quantityToDue;

                                }
                                documentMap.put("batchmapid", newProductBatch.getId());
                                accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
                            }
                        }
                    }
                } else {

                    HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                    batchUpdateQtyMap.put("qty", String.valueOf(-(Double.parseDouble(jSONObject.getString("quantity")))));
                    batchUpdateQtyMap.put(Constants.Acc_id, jSONObject.getString("purchasebatchid"));
                    accCommonTablesDAO.saveBatchAmountDue(batchUpdateQtyMap);
                }
                accCommonTablesDAO.saveBatchDocumentMapping(documentMap);
            }
            batchQty--;

            if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 

                HashMap<String, Object> documentMap = new HashMap<String, Object>();
                documentMap.put("quantity", 1);
                documentMap.put("serialmapid", jSONObject.getString("purchaseserialid"));
                documentMap.put("documentid", documentId);
                documentMap.put("transactiontype", "27");//This is GRN Type Tranction  
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    documentMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    documentMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }

                //   accCommonTablesDAO.saveSerialDocumentMapping(documentMap);
                KwlReturnObject krObj = accCommonTablesDAO.saveSerialDocumentMapping(documentMap);
                SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) krObj.getEntityList().get(0);
                if (jSONObject.has(Constants.customfield)) {
                    String customfield = jSONObject.getString(Constants.customfield);
                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        HashMap<String, Object> DOMap = new HashMap<String, Object>();
                        JSONArray jcustomarray = new JSONArray(customfield);

                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "SerialDocumentMapping");
                        customrequestParams.put("moduleprimarykey", "SerialDocumentMappingId");
                        customrequestParams.put("modulerecid", serialDocumentMapping.getId());
                        customrequestParams.put("moduleid", Constants.SerialWindow_ModuleId);
                        customrequestParams.put(Constants.companyKey, companyid);
                        DOMap.put(Constants.Acc_id, serialDocumentMapping.getId());
                        customrequestParams.put("customdataclasspath", Constants.Acc_Serial_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            DOMap.put("serialcustomdataref", serialDocumentMapping.getId());
                            accCommonTablesDAO.updateserialcustomdata(DOMap);
                        }
                    }
                }
                HashMap<String, Object> serialUpdateQtyMap = new HashMap<String, Object>();
                serialUpdateQtyMap.put("qty", "-1");
                serialUpdateQtyMap.put(Constants.Acc_id, jSONObject.getString("purchaseserialid"));
                accCommonTablesDAO.saveSerialAmountDue(serialUpdateQtyMap);

            } else {
                batchQty = 0;
            }
        }

    }

    public ProductBatch saveAssemblyProductBatch(String batchJSON, Product product, HttpServletRequest request) throws JSONException, ParseException, SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray(batchJSON);

        KwlReturnObject kmsg = null;
        ProductBatch productBatch = null;
        String productBatchId = "";
        Boolean isBatchForProduct=false;
        Boolean isSerialForProduct=false;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        if (product != null) {
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
        }

        String jsonSrtingArray[] = {};
        if (!StringUtil.isNullOrEmpty(batchJSON)) {
            jsonSrtingArray = batchJSON.split("],");
        }

        for (int i = 0; i < jsonSrtingArray.length; i++) {
            String jsonStr = jsonSrtingArray[i];
            if (i != jsonSrtingArray.length - 1) {
                jsonStr += "]";
            }
            jArr = new JSONArray(jsonStr);
            if (i == 0) {
                HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                JSONObject jSONObject = new JSONObject(jArr.get(0).toString());
                pdfTemplateMap.put(Constants.companyKey, product.getCompany().getCompanyID());
                pdfTemplateMap.put("name", jSONObject.getString("batch"));
                if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                    pdfTemplateMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
                }
                if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                    pdfTemplateMap.put("expdate", df.parse(jSONObject.getString("expdate")));
                }
                pdfTemplateMap.put("quantity", jSONObject.getString("quantity"));
                pdfTemplateMap.put("balance", jSONObject.getString("balance"));
                pdfTemplateMap.put("location", jSONObject.getString("location"));
                pdfTemplateMap.put("product", product.getID());
                pdfTemplateMap.put("warehouse", jSONObject.getString("warehouse"));
                pdfTemplateMap.put("isopening", true);
                pdfTemplateMap.put("transactiontype", "1");//This is product Type Tranction
                pdfTemplateMap.put("ispurchase", true);
                kmsg = accCommonTablesDAO.saveBatchForProduct(pdfTemplateMap);

                if (kmsg.getEntityList().size() != 0) {
                    productBatch = (ProductBatch) kmsg.getEntityList().get(0);
                    productBatchId = productBatch.getId();
                }
            }

            HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
            JSONObject jSONObject = new JSONObject(jArr.get(0).toString());
            if (isSerialForProduct) {
                pdfTemplateMap.put(Constants.Acc_id, jSONObject.getString("serialnoid"));
                pdfTemplateMap.put("name", jSONObject.getString("serialno"));
                pdfTemplateMap.put(Constants.companyKey, product.getCompany().getCompanyID());
                pdfTemplateMap.put("product", product.getID());
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    pdfTemplateMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    pdfTemplateMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }
                pdfTemplateMap.put("batch", productBatchId);
                pdfTemplateMap.put("transactiontype", "1");//This is product Type Tranction
                pdfTemplateMap.put("ispurchase", true);
                kmsg = accCommonTablesDAO.saveSerialForBatch(pdfTemplateMap);
            }
            String subassemblyserialJson = jSONObject.getString("subproduct");
            String subassemblyjsonStringArray[] = {};
            String subassemblyjsonStr = "";
            String batchSerialId = "";

            if (!StringUtil.isNullOrEmpty(subassemblyserialJson)) {
                subassemblyjsonStringArray = subassemblyserialJson.split("]\",\"");
            }
            for (int j = 0; j < subassemblyjsonStringArray.length; j++) {
                subassemblyjsonStr = subassemblyjsonStringArray[j];
                if (j != subassemblyjsonStringArray.length - 1) {
                    subassemblyjsonStr += "]";
                }
                ProductBatch subProductBatch = saveAssemblySubProductBatch(subassemblyjsonStr, request);

                if (kmsg.getEntityList().size() != 0) {
                    BatchSerial batchSerial = (BatchSerial) kmsg.getEntityList().get(0);
                    batchSerialId = batchSerial.getId();
                }
                HashMap<String, Object> batchSerialMap = new HashMap<String, Object>();
                if (subProductBatch != null) {
                    batchSerialMap.put("subproductbatch", subProductBatch.getId());
                    batchSerialMap.put("mainproductserial", batchSerialId);
                    KwlReturnObject kmsgmap = accCommonTablesDAO.saveAssembySerialBatchMapping(batchSerialMap);
                }
            }

        }
        return productBatch;
    }

    public ProductBatch saveAssemblySubProductBatch(String batchJSON, HttpServletRequest request) throws JSONException, ParseException, SessionExpiredException, ServiceException {

        JSONArray jArr = new JSONArray(batchJSON);

        KwlReturnObject kmsg = null;
        String purchasebatchid = "";
        String purchaseserialid = "";
        String productid = "";
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        double quantity = 0;
        DateFormat df = authHandler.getDateOnlyFormat(request);
        for (int i = 0; i < 1; i++) {
            HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
            JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
            pdfTemplateMap.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            pdfTemplateMap.put("name", jSONObject.getString("batch"));
            if (jSONObject.has("mfgdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("mfgdate"))) {
                pdfTemplateMap.put("mfgdate", df.parse(jSONObject.getString("mfgdate")));
            }
            if (jSONObject.has("expdate") && !StringUtil.isNullOrEmpty(jSONObject.getString("expdate"))) {
                pdfTemplateMap.put("expdate", df.parse(jSONObject.getString("expdate")));
            }
            pdfTemplateMap.put("quantity", jSONObject.getString("quantity"));
            quantity = Double.parseDouble(jSONObject.getString("quantity"));
            pdfTemplateMap.put("balance", jSONObject.getString("balance"));
            pdfTemplateMap.put("location", jSONObject.getString("location"));
            pdfTemplateMap.put("product", jSONObject.getString(Constants.productid));
            if (jSONObject.has(Constants.productid) && !StringUtil.isNullOrEmpty(jSONObject.getString(Constants.productid))) {
                productid = jSONObject.getString(Constants.productid);
            }
            pdfTemplateMap.put("warehouse", jSONObject.getString("warehouse"));
            purchasebatchid = jSONObject.getString("purchasebatchid");
            pdfTemplateMap.put("isopening", false);
            pdfTemplateMap.put("transactiontype", "1");//This is product Type Tranction
            pdfTemplateMap.put("ispurchase", false);
            kmsg = accCommonTablesDAO.saveBatchForProduct(pdfTemplateMap);
        }
        if (!StringUtil.isNullOrEmpty(productid)) {

            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), productid);
            Product product = (Product) prodresult.getEntityList().get(0);
            isBatchForProduct = product.isIsBatchForProduct();
            isSerialForProduct = product.isIsSerialForProduct();
        }
        ProductBatch productBatch = null;
        String productBatchId = "";
        if (kmsg.getEntityList().size() != 0) {
            productBatch = (ProductBatch) kmsg.getEntityList().get(0);
            productBatchId = productBatch.getId();
            HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
            pdfTemplateMap.put("purchasebatchid", purchasebatchid);
            pdfTemplateMap.put("salesbatchid", productBatch.getId());
            pdfTemplateMap.put("quantity", quantity);
            kmsg = accCommonTablesDAO.saveBatchMapping(pdfTemplateMap);
        }
        if (isSerialForProduct) {  //if serial no option is on then only save the serial no details 
            for (int i = 0; i < jArr.length(); i++) {
                HashMap<String, Object> pdfTemplateMap = new HashMap<String, Object>();
                JSONObject jSONObject = new JSONObject(jArr.get(i).toString());
                pdfTemplateMap.put(Constants.Acc_id, jSONObject.getString("serialnoid"));
                pdfTemplateMap.put("name", jSONObject.getString("serialno"));
                pdfTemplateMap.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
                pdfTemplateMap.put("product", jSONObject.getString(Constants.productid));
                if (jSONObject.has("expstart") && !StringUtil.isNullOrEmpty(jSONObject.getString("expstart"))) {
                    pdfTemplateMap.put("expfromdate", df.parse(jSONObject.getString("expstart")));
                }
                if (jSONObject.has("expend") && !StringUtil.isNullOrEmpty(jSONObject.getString("expend"))) {
                    pdfTemplateMap.put("exptodate", df.parse(jSONObject.getString("expend")));
                }
                pdfTemplateMap.put("batch", productBatchId);
                pdfTemplateMap.put("transactiontype", "1");//This is product Type Tranction
                pdfTemplateMap.put("ispurchase", false);
                kmsg = accCommonTablesDAO.saveSerialForBatch(pdfTemplateMap);

                if (kmsg.getEntityList().size() != 0) {
                    BatchSerial batchSerial = (BatchSerial) kmsg.getEntityList().get(0);
                    String salesSerial = batchSerial.getId();
                    pdfTemplateMap = new HashMap<String, Object>();
                    pdfTemplateMap.put("purchaseserialid", jSONObject.getString("purchaseserialid"));
                    pdfTemplateMap.put("salesserialid", salesSerial);
                    kmsg = accCommonTablesDAO.saveSalesPurchaseSerialMapping(pdfTemplateMap);
                }

            }
        }
        return productBatch;
    }

    //Inventory
    public ModelAndView getInventory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(Constants.productid, request.getParameter(Constants.productid));
            requestParams.put(Constants.start, request.getParameter(Constants.start));
            requestParams.put(Constants.limit, request.getParameter(Constants.limit));
            requestParams.put("ss", request.getParameter("ss"));

            int count1 = 0, count2 = 0;
            List list1, list2;
            JSONArray DataJArr;

            KwlReturnObject result = accProductObj.getInventoryWOdetails(requestParams);
            list1 = result.getEntityList();
            count1 = result.getRecordTotalCount();
            Object[] res = getInventoryWOdetailsJson(request, list1);
            DataJArr = (JSONArray) res[0];
            double remQuantity = (Double) res[1];

            result = accProductObj.getInventoryWithDetails(requestParams);
            list2 = result.getEntityList();
            count2 = result.getRecordTotalCount();
            DataJArr = getInventoryWithDetailsJson(request, list2, DataJArr, remQuantity);

            JSONArray pagedJSON = DataJArr;
            String start = request.getParameter(Constants.start);
            String limit = request.getParameter(Constants.limit);
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJSON = StringUtil.getPagedJSON(pagedJSON, Integer.parseInt(start), Integer.parseInt(limit));
            }

            jobj.put(Constants.RES_data, pagedJSON);
            jobj.put("count", count1 + count2);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private Object[] getInventoryWOdetailsJson(HttpServletRequest request, List list) throws ServiceException, SessionExpiredException {
        double remQuantity = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa");
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Inventory inv = (Inventory) itr.next();
                if (inv.isCarryIn()) {
                    remQuantity += inv.getBaseuomquantity();
                } else {
                    remQuantity -= inv.getBaseuomquantity();
                }
                JSONObject obj = new JSONObject();
                obj.put("inventoryid", "");
                obj.put(Constants.productid, "");
                obj.put("productname", "");
                obj.put("quantity", inv.getBaseuomquantity());
                obj.put("remquantity", remQuantity);
                obj.put("date", authHandler.getDateFormatter(request).format(inv.getUpdateDate()));
                obj.put("carryin", inv.isNewInv() ? "Stock" : inv.isCarryIn());                // To detect what transaction has made - Amol D.
                obj.put("desc", "Stock exists.");
                obj.put("uom", inv.getUom() == null ? (inv.getProduct().getUnitOfMeasure() == null ? "" : inv.getProduct().getUnitOfMeasure().getNameEmptyforNA()) : inv.getUom().getNameEmptyforNA());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getInventoryWOdetailsJson : " + ex.getMessage(), ex);
        }
        return new Object[]{jArr, remQuantity};
    }

    private JSONArray getInventoryWithDetailsJson(HttpServletRequest request, List list, JSONArray jArr, double remQuantity) throws ServiceException, SessionExpiredException {
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                String carryIn = "";
                Object[] row = (Object[]) itr.next();
                Inventory inventory = (Inventory) row[0];
                JournalEntry je = (JournalEntry) row[1];
                JSONObject obj = new JSONObject();
                obj.put("inventoryid", inventory.getID());
                obj.put(Constants.productid, inventory.getProduct().getID());
                obj.put("productname", inventory.getProduct().getName());
                obj.put("quantity", inventory.getBaseuomquantity());
                if (inventory.isCarryIn()) {
                    remQuantity += inventory.getBaseuomquantity();
                } else {
                    remQuantity -= inventory.getBaseuomquantity();
                }
                obj.put("remquantity", remQuantity);
                obj.put("date", authHandler.getDateFormatter(request).format(je.getEntryDate()));
                if (!inventory.isCarryIn() && inventory.isDefective()) {
                    carryIn = "Debit";
                } else if (inventory.isCarryIn() && inventory.isDefective()) {
                    carryIn = "Credit";
                }
                obj.put("carryin", carryIn.equals("") ? inventory.isCarryIn() : carryIn);
                obj.put("desc", inventory.getDescription());
                obj.put("uom", inventory.getUom() == null ? (inventory.getProduct().getUnitOfMeasure() == null ? "" : inventory.getProduct().getUnitOfMeasure().getNameEmptyforNA()) : inventory.getUom().getNameEmptyforNA());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getInventoryWithDetailsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public boolean isChildorGrandChild(String productid, String parentid) throws ServiceException {
        try {
            List Result = accProductObj.isChildorGrandChild(parentid);
            Iterator iterator = Result.iterator();
            if (iterator.hasNext()) {
                Object ResultObj = iterator.next();
                Product ResultParentProduct = (Product) ResultObj;
                ResultParentProduct = ResultParentProduct.getParent();
                if (ResultParentProduct == null) {
                    return false;
                } else {
                    String Resultparent = ResultParentProduct.getID();
                    if (Resultparent.equals(productid)) {
                        return true;
                    } else {
                        return isChildorGrandChild(productid, Resultparent);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("isChildorGrandChild : " + ex.getMessage(), ex);
        }
        return false;
    }

    public void updateBillofMaterialsInventory(HttpServletRequest request, Product assemblyProduct) throws ServiceException {
        try {
            String subproductid1;
            double quantity1;
            boolean flag = false;
            KwlReturnObject originalAssembly;
            KwlReturnObject result = accProductObj.getQuantity(assemblyProduct.getID());
            if (result.getEntityList() != null) {
                if (result.getEntityList().get(0) != null) {
                    double qty = Double.parseDouble(result.getEntityList().get(0).toString());
                    if (qty > 0) {
                        String jsondata = request.getParameter("assembly");
                        JSONArray jarr = new JSONArray("[" + jsondata + "]");
                        String[] subproductid = new String[jarr.length()];
                        double[] quantity = new double[jarr.length()];
                        for (int i = 0; i < jarr.length(); i++) {
                            JSONObject jobj = jarr.getJSONObject(i);
                            subproductid[i] = jobj.getString("product");
                            quantity[i] = Double.parseDouble(jobj.getString("inventoryquantiy"));
                        }

                        originalAssembly = accProductObj.getAssemblyProductBillofMaterials(assemblyProduct.getID());

                        if (originalAssembly.getEntityList() != null) {
                            Iterator iterator = originalAssembly.getEntityList().iterator();
                            while (iterator.hasNext()) {
                                Object ResultObj = iterator.next();
                                ProductAssembly productAssembly = (ProductAssembly) ResultObj;
                                subproductid1 = productAssembly.getSubproducts().getID();
                                quantity1 = productAssembly.getQuantity();
                                flag = false;
                                for (int i = 0; i < subproductid.length; i++) {
                                    if (subproductid1.equalsIgnoreCase(subproductid[i])) {
                                        flag = true;
                                        if (quantity1 == quantity[i]) {
                                            break;
                                        } else {
                                            if (quantity1 > quantity[i]) {
                                                addInventoryJson(request, subproductid1, (quantity1 - quantity[i]) * qty, true, "Rebuild quantity added after being removed from editing");
                                            } else {
                                                addInventoryJson(request, subproductid1, (quantity[i] - quantity1) * qty, false, "Rebuild quantity subtracted after being removed from editing");
                                            }
                                        }
                                    }
                                }
                                if (!flag) {
                                    addInventoryJson(request, subproductid1, quantity1 * qty, true, "Rebuild quantity added after being removed from editing");
                                }
                            }

                            for (int i = 0; i < subproductid.length; i++) {
                                iterator = originalAssembly.getEntityList().iterator();
                                flag = false;
                                while (iterator.hasNext()) {
                                    Object ResultObj = iterator.next();
                                    ProductAssembly productAssembly = (ProductAssembly) ResultObj;
                                    subproductid1 = productAssembly.getSubproducts().getID();
                                    if (subproductid[i].equalsIgnoreCase(subproductid1)) {
                                        flag = true;
                                    }
                                }
                                if (!flag) {
                                    addInventoryJson(request, subproductid[i], quantity[i] * qty, false, "Rebuild quantity subtracted after being removed from editing");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateBillofMaterialsInventory : " + ex.getMessage(), ex);
        }
    }

    public void addInventoryJson(HttpServletRequest request, String productid, double quantity, boolean carryin, String description) throws ServiceException {
        try {
            JSONObject inventoryjson = new JSONObject();
            inventoryjson.put(Constants.productid, productid);
            inventoryjson.put("quantity", quantity);
            inventoryjson.put("baseuomquantity", quantity);
            inventoryjson.put("baseuomrate", 1);
            Product obj = (Product) kwlCommonTablesDAOObj.getClassObject(Product.class.getName(), productid);
            if (obj.getUnitOfMeasure() != null) {
                inventoryjson.put("uomid", obj.getUnitOfMeasure().getID());
            }
            inventoryjson.put("description", description);
            inventoryjson.put("carryin", carryin);
            inventoryjson.put("defective", false);
            inventoryjson.put("newinventory", false);
            inventoryjson.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            accProductObj.addInventory(inventoryjson);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("addInventoryJson: " + ex.getMessage(), ex);
        }
    }

    public ModelAndView getProductIDAutoNumber(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String autoNumber = accProductObj.getNextAutoProductIdNumber(sessionHandlerImpl.getCompanyid(request));
            issuccess = true;
            jobj.put("autoNumberID", autoNumber);
            msg = "Product ID generated successfully";
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView exportProduct(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        try {
            boolean isCustomColumnExport=true;
            request.setAttribute("exportPDFCSV", true);
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);

            if(requestParams.containsKey(Constants.start)){
                requestParams.remove(Constants.start);
            }

            if(requestParams.containsKey(Constants.limit)){
                requestParams.remove(Constants.limit);
            }

            if(!StringUtil.isNullOrEmpty(request.getParameter("selproductIds"))){
                String[] ids=request.getParameter("selproductIds").split(",");
                requestParams.put("ids", ids);
            } else if (!StringUtil.isNullOrEmpty(request.getParameter("isForProductQuantityDetailsReport")) && Boolean.parseBoolean(request.getParameter("isForProductQuantityDetailsReport")) && !StringUtil.isNullOrEmpty(request.getParameter("ids"))) {
                String[] ids = request.getParameter("ids").split(",");
                requestParams.put("ids", ids);
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("isFromQuantityDetailsReport"))){
                requestParams.put("isFromQuantityDetailsReport", Boolean.parseBoolean(request.getParameter("isFromQuantityDetailsReport")));
            }
            String fileType = request.getParameter("filetype");

            String companyId = sessionHandlerImpl.getCompanyid(request);
            int totalProducts = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("totalProducts"))) {
                totalProducts = Integer.parseInt(request.getParameter("totalProducts"));
            }
            
            boolean otherThanAboveCheckForProductMaster = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("otherThanAboveCheckForProductMaster"))) {
                otherThanAboveCheckForProductMaster = Boolean.parseBoolean(request.getParameter("otherThanAboveCheckForProductMaster"));
            }
            
            String productTypeFilter ="";
            if (!StringUtil.isNullOrEmpty(request.getParameter("productTypeFilter"))) {
                productTypeFilter = request.getParameter("productTypeFilter");
                requestParams.put("productTypeFilter", productTypeFilter);
            }
            requestParams.put("isExportStr", "true");
            if (otherThanAboveCheckForProductMaster) {//If Exporting data with "other than above" check true

                Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyId);
                String countryid = company != null ? company.getCountry().getID() : String.valueOf(0);

                /*------------ Fetching default Column Config-------- */
                Map<String, Object> importParams = new HashMap<>();
                importParams.put("moduleId", "30");
                importParams.put("companyid", companyId);
                importParams.put("isdocumentimport", "F");
                importParams.put("countryid", countryid);
                importParams.put("subModuleFlag", 0);
                List list = importDao.getModuleColumnConfig(importParams);       //getting entries from default header
                Iterator defaultHeaderConfig = list.iterator();
                String title = "";
                String header = "";
                String align = "";
                boolean isPartNumberActivated = importDao.isPartNumberActivated(companyId);
                boolean isPerpetualInventory = importDao.isPerpetualInventory(companyId);
                while (defaultHeaderConfig.hasNext()) {
                    Object[] row = (Object[]) defaultHeaderConfig.next();

                    if (!isPartNumberActivated && (row[1].toString().equalsIgnoreCase("Supplier Part Number") || row[1].toString().equalsIgnoreCase("Part Number") || row[1].toString().equalsIgnoreCase("Customer Part Number"))) {
                        continue;//Default Header
                    }

                    if (!isPerpetualInventory && (row[4].toString().equalsIgnoreCase("stockadjustmentaccountid") || row[4].toString().equalsIgnoreCase("inventoryaccountid") || row[4].toString().equalsIgnoreCase("cogsaccountid"))) {
                        continue;//Data index
                    }

                    title += row[1].toString() + ",";
                    header += row[4].toString() + ",";
                    align += (row[5] == null ? "none" : row[2].toString()) + ",";

                }

                boolean fetchCustomFields = false;

                if (!StringUtil.isNullOrEmpty(request.getParameter("fetchCustomFields"))) {
                    fetchCustomFields = Boolean.parseBoolean(request.getParameter("fetchCustomFields"));
                }

                /*------------- Code for Fetching Custom/Dimension Fields column config------------ */
                if (fetchCustomFields) {
                    list = importDao.getCustomModuleColumnConfig("30", companyId, true);
                    Iterator customFields = list.iterator();
                    while (customFields.hasNext()) {
                        Object[] customFieldsRow = (Object[]) customFields.next();

                        title += customFieldsRow[4].toString() + ",";
                        header += customFieldsRow[6].toString() + ",";
                        align += "none" + ",";
                    }
                }

                request.setAttribute("header", header.substring(0, header.length() - 1));
                request.setAttribute("title", title.substring(0, title.length() - 1));
                request.setAttribute("align", align.substring(0, align.length() - 1));
		jobj.put("header", header.substring(0, header.length() - 1));  //SDP-14930
                jobj.put("title", title.substring(0, title.length() - 1));
                jobj.put("align", align.substring(0, align.length() - 1));
            }
            
            if (totalProducts > 1000 && !StringUtil.equal(fileType, "print")) {
                String loginUserId = sessionHandlerImpl.getUserid(request);
                String userDateFormatId = sessionHandlerImpl.getDateFormatID(request);
                String timeZoneDifferenceId = sessionHandlerImpl.getTimeZoneDifference(request);
                String currencyIDForProduct = sessionHandlerImpl.getCurrencyID(request);
                DateFormat dateFormatForProduct = authHandler.getDateFormatter(request);
                jobj.put("isLargeNumberofProductsExporting", true);
                jobj.put("dateFormatForProduct", dateFormatForProduct);
                jobj.put("userDateFormatId", userDateFormatId);
                jobj.put("timeZoneDifferenceId", timeZoneDifferenceId);
                jobj.put("currencyIDForProduct", currencyIDForProduct);


                Date requestTime = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
                int exportStatus = 1;
                SimpleDateFormat sdfTemp = new SimpleDateFormat("ddMMyyyy_hhmmssaa");
                DateFormat dateFormat = authHandler.getDateFormatter(request);
                String fileName = "Products_" + (sdfTemp.format(requestTime)).toString();
                HashMap<String, Object> exportDetails = new HashMap<String, Object>();
                exportDetails.put("fileName", fileName+"."+fileType);
                exportDetails.put("requestTime", requestTime);
                exportDetails.put("status", exportStatus);
                exportDetails.put("companyId", companyId);
                exportDetails.put("fileType", fileType);

                ProductExportDetail obj = null;
                try{
                    status = txnManager.getTransaction(def);
                    KwlReturnObject result1=accProductObj.saveProductExportDetails(exportDetails);
                    obj=(ProductExportDetail) result1.getEntityList().get(0);
                    txnManager.commit(status);
                }catch(Exception ex){
                    txnManager.rollback(status);
                    Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                Logger.getLogger(accProductController.class.getName()).log(Level.INFO, "Export Product - Filename - "+ fileName +" - Starts - "+ new Date());
                Logger.getLogger(accProductController.class.getName()).log(Level.INFO, "Export Product - Filename - "+ fileName +" - Product Count - "+ totalProducts);
                Calendar c1 = Calendar.getInstance();
                KwlReturnObject result = accProductObj.getProducts(requestParams);
                Calendar c2 = Calendar.getInstance();
                Logger.getLogger(accProductController.class.getName()).log(Level.INFO, "Export Product - Filename - "+ fileName +" - GetProducts Time - "+ (c2.getTimeInMillis()-c1.getTimeInMillis()) + " - Ends - "+ new Date());
                
                JSONArray DataJArr = productHandler.getProductsJson(requestParams, result.getEntityList(), accProductObj , accAccountDAOobj , accountingHandlerDAOobj,accCurrencyDAOobj,isCustomColumnExport);
                Calendar c3 = Calendar.getInstance();
                Logger.getLogger(accProductController.class.getName()).log(Level.INFO, "Export Product - Filename - "+ fileName +" - GetProductsJson Time - "+ (c3.getTimeInMillis()-c2.getTimeInMillis()) + " - Ends - "+ new Date());

                totalProducts = result.getEntityList().size();
                jobj.put(Constants.RES_data, DataJArr);
                jobj.put("productExportFileName", fileName);
                jobj.put("filename", fileName);
                jobj.put("totalProducts", totalProducts);

//                JSONArray DataJArr = productHandler.getProductsPDFJson(request, result.getEntityList(), requestParams, accProductObj);

                exportDaoObj.processRequest(requestJobj, jobj);

                HashMap<String, Object> requestParamsForExport = new HashMap<String, Object>();
                requestParamsForExport.put("loginUserId", loginUserId);
                requestParamsForExport.put("productExportFileName", fileName);
                requestParamsForExport.put("filetype", fileType);

                SendMail(requestParamsForExport);

                if(obj!=null){
                    exportStatus=2;
                    exportDetails.put(Constants.Acc_id,obj.getId());
                    exportDetails.put("status", exportStatus);
                    try{
                        status = txnManager.getTransaction(def);
                        accProductObj.saveProductExportDetails(exportDetails);
                        txnManager.commit(status);
                    }catch(Exception ex){
                        txnManager.rollback(status);
                        Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                c3 = Calendar.getInstance();
                Logger.getLogger(accProductController.class.getName()).log(Level.INFO, "Export Product - Filename - "+ fileName +" - Ends - "+ new Date());
                Logger.getLogger(accProductController.class.getName()).log(Level.INFO, "Export Product - Filename - "+ fileName +" - Total Time - "+ (c3.getTimeInMillis()-c1.getTimeInMillis()));
            }else{
                KwlReturnObject result = accProductObj.getProducts(requestParams);
                JSONArray DataJArr = productHandler.getProductsJson(requestParams, result.getEntityList(), accProductObj , accAccountDAOobj , accountingHandlerDAOobj,accCurrencyDAOobj,isCustomColumnExport);
                jobj.put(Constants.RES_data, DataJArr);


                if (StringUtil.equal(fileType, "print")) {
                        String  GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                    jobj.put("GenerateDate", GenerateDate);
                    view = "jsonView-empty";
                }
                exportDaoObj.processRequest(request, response, jobj);
            }
            jobj.put(Constants.RES_success, true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView exportProductsByCategory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            requestParams.put("categoryid", !StringUtil.isNullOrEmpty(request.getParameter("categoryid")) ? request.getParameter("categoryid") : "");

            KwlReturnObject result = accProductObj.getNewProductList(requestParams);
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
//            JSONArray DataJArr = getProductsByCategoryJson(request, result.getEntityList());
            JSONArray DataJArr = accProductModuleService.getProductsByCategoryJsonForExport(paramJobj, result.getEntityList());
            jobj.put(Constants.RES_data, DataJArr);

            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put(Constants.RES_success, true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
     
    /*-----Function to Export Product Price------- */
    public ModelAndView exportProductsPrice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(Constants.start, (StringUtil.isNullOrEmpty(request.getParameter(Constants.start))) ? "0" : request.getParameter(Constants.start));
            requestParams.put(Constants.limit, (StringUtil.isNullOrEmpty(request.getParameter(Constants.limit))) ? "30" : request.getParameter(Constants.limit));
            requestParams.put(Constants.productid, (StringUtil.isNullOrEmpty(request.getParameter(Constants.productid))) ? "" : request.getParameter(Constants.productid));
            requestParams.put("productPriceinMultipleCurrency", Boolean.FALSE.parseBoolean(request.getParameter("productPriceinMultipleCurrency")));
            requestParams.put(Constants.globalCurrencyKey, getGlobalCurrencyidFromRequest(request));

            KwlReturnObject result = accProductObj.getPrice(requestParams);
            List list = result.getEntityList();
            request.setAttribute("isExport", true);
            JSONArray DataJArr = getPriceListJson(request, list);

            jobj.put(Constants.RES_data, DataJArr);

            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put(Constants.RES_success, true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
      
      
            
    public static String getActualFileName(String storageName) {
        String ext = storageName.substring(storageName.lastIndexOf("."));
        String actualName = storageName.substring(0, storageName.lastIndexOf("_"));
        actualName = actualName + ext;
        return actualName;
    }

    public String createCSVrecord(Object[] listArray) {
        return accProductModuleService.createCSVrecord(listArray);
    }

    public void createFailureFiles(String filename, StringBuilder failedRecords, String ext) {
        accProductModuleService.createFailureFiles(filename, failedRecords, ext);
    }

    private Producttype getProductTypeByName(String productTypeName) throws AccountingException {
        return accProductModuleService.getProductTypeByName(productTypeName);
    }

    private UnitOfMeasure getUOMByName(String productUOMName, String companyID) throws AccountingException {
        return accProductModuleService.getUOMByName(productUOMName, companyID);
    }

    private Product getProductByProductID(String productID, String companyID) throws AccountingException {
        return accProductModuleService.getProductByProductID(productID, companyID);
    }

    private Account getAccountByName(String accountName, String companyID) throws AccountingException {
        return accProductModuleService.getAccountByName(accountName, companyID);
    }

    private Vendor getVendorByName(String vendorName, String companyID) throws AccountingException {
        return accProductModuleService.getVendorByName(vendorName, companyID);
    }

    private InventoryLocation getInventoryLocationByName(String inventoryLocation, String companyID) throws AccountingException {
        return accProductModuleService.getInventoryLocationByName(inventoryLocation, companyID);
    }

    private InventoryWarehouse getInventoryWarehouseByName(String inventoryWarehouse, String companyID) throws AccountingException {
        return accProductModuleService.getInventoryWarehouseByName(inventoryWarehouse, companyID);
    }

    private String getCurrencyId(String currencyName, HashMap currencyMap) {
        return accProductModuleService.getCurrencyId(currencyName, currencyMap);
    }

    public HashMap getCurrencyMap(boolean  isCurrencyCode) throws ServiceException {
        return accProductModuleService.getCurrencyMap(isCurrencyCode);
    }

    private String getCustomerIDByCode(String customerCode, String companyID) throws AccountingException {
        return accProductModuleService.getCustomerIDByCode(customerCode,companyID);
    }

    private String getVendorIDByCode(String vendorCode, String companyID) throws AccountingException {
        return accProductModuleService.getVendorIDByCode(vendorCode, companyID);
    }

    public ModelAndView addImage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject myjobj = new JSONObject();
        List fileItems = null;
        String details = "";
        KwlReturnObject kmsg = null;
        String auditAction = "";
        String id = java.util.UUID.randomUUID().toString();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            myjobj.put(Constants.RES_success, true);
            myjobj.put(Constants.RES_msg, "Image uploaded successfully.");
            response.setContentType("text/html;charset=UTF-8");
            String userid = sessionHandlerImpl.getUserid(request);
            String companyID = sessionHandlerImpl.getCompanyid(request);
            String map = request.getParameter("mapid");
            String key = request.getParameter("keyid");
            HashMap<String, String> arrParam = new HashMap<String, String>();
            boolean fileUpload = false;
            ArrayList<FileItem> fi = new ArrayList<FileItem>();
            if (request.getParameter("imageAdd") != null) {
                DiskFileUpload fu = new DiskFileUpload();
                fileItems = fu.parseRequest(request);
                parseRequest(fileItems, arrParam, fi, fileUpload);
            }

            for (int cnt = 0; cnt < fi.size(); cnt++) {
                kmsg = uploadImage(key, fi.get(cnt), userid, companyID, getServletContext());
                String refid = arrParam.get("refid");
                String randomid = java.util.UUID.randomUUID().toString();
                Product product = (Product) kwlCommonTablesDAOObj.getClassObject(Product.class.getName(), refid);
                if (map.equals("3")) {
                    details = " Product - ";
                    details += StringUtil.isNullOrEmpty(product.getName()) ? "" : product.getName();
                }
                auditTrailObj.insertAuditLog(AuditAction.PRODUCT_IMAGEUPLOAD, "User " + sessionHandlerImpl.getUserFullName(request) + " has uploded image for Product <b>"  + product.getProductName() + "</b> ( " + product.getProductid() + " ) ", request, product.getID());
            }
            txnManager.commit(status);
        } catch (Exception e) {
            try {
                myjobj.put(Constants.RES_success, false);
                if (e.getMessage().equals("system failure: invalidformat")) {
                    myjobj.put(Constants.RES_msg, "invalidformat");
                } else {
                    myjobj.put(Constants.RES_msg, "Sorry! Image could not be uploaded successfully. Please try again.");
                }
            } catch (JSONException ex) {
                System.out.println(ex);
            }
            logger.warn(e.getMessage(), e);
            txnManager.rollback(status);
        } finally {
            // out.close();
        }
        return new ModelAndView("jsonView", "model", myjobj.toString());
    }

    public void parseRequest(List fileItems, HashMap<String, String> arrParam, ArrayList<FileItem> fi, boolean fileUpload) throws ServiceException {

        FileItem fi1 = null;
        for (Iterator k = fileItems.iterator(); k.hasNext();) {
            fi1 = (FileItem) k.next();
            if (fi1.isFormField()) {
                try {
                    arrParam.put(fi1.getFieldName(), fi1.getString("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    logger.error(e.getMessage());
                }
            } else {
                if (fi1.getSize() != 0) {
                    fi.add(fi1);
                    fileUpload = true;
                }
            }
        }
    }

    public KwlReturnObject uploadImage(String fname, FileItem fi, String userid, String companyId, ServletContext servletContext) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        try {
            if (!fi.getContentType().startsWith("image")) {
                throw new Exception("invalidformat");   //invalid image format
            }
            String fileName = new String(fname.getBytes(), "UTF8");
            String Ext = ".png";
            String fileid = fileName + Ext;
            String temp = storageHandlerImpl.GetProfileImgStorePath() +Constants.ProductImages;
            uploadFile(fi, temp, fileid);
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Success", "", ll, dl);
    }

    public void uploadFile(FileItem fi, String destinationDirectory, String fileName) throws ServiceException {
        try {
            File destDir = new File(destinationDirectory);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            File uploadFile = new File(destinationDirectory + "/" + fileName);
            fi.write(uploadFile);
            BufferedImage img = null;
            img = ImageIO.read(new File(uploadFile.getPath().toString()));
            if (img == null) {
                throw new Exception("invalidformat");
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), "", false);
        }
    }

    public ModelAndView deleteImage(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        
        try {
            String fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages+ request.getParameter("fileid")+".png";         
            File file=new File(fileName);
            Product product = (Product) kwlCommonTablesDAOObj.getClassObject(Product.class.getName(), request.getParameter("fileid"));    
            
            if(file.delete()){
                jobj.put(Constants.RES_success, true);
                jobj.put(Constants.RES_msg, messageSource.getMessage("acc.ProductDetails.deleteImageMessage", null, RequestContextUtils.getLocale(request)));
                //jobj.put(Constants.RES_msg, "Image deleted successfully.");
                //messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, RequestContextUtils.getLocale(request))
                auditTrailObj.insertAuditLog(AuditAction.PRODUCT_IMAGE_DELETE, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted image for Product <b>"  + product.getProductName() + "</b> ( " + product.getProductid() + " ) ", request, product.getID());
                
            }else{
                //messageSource.getMessage("acc.ProductDetails.deleteImageMessage", null, RequestContextUtils.getLocale(request)))
                jobj.put(Constants.RES_success, false);
                jobj.put(Constants.RES_msg, messageSource.getMessage("acc.ProductDetails.deletewarningMessage", null, RequestContextUtils.getLocale(request)));
                //jobj.put(Constants.RES_msg, "Please select the product that has image uploaded.");
            }
           
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getImagePath(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            String fileName = "productimage?loadtime="+new Date().getTime()+"&fname=" + request.getParameter("fileid") + ".png";
            jobj.put("fileName", fileName);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public boolean deleteExistingBuildAssemblyEntries(HttpServletRequest request) throws ServiceException,SessionExpiredException {
        try{
            String productids[] = request.getParameterValues("productids");
            String productrefno[] = request.getParameterValues("productrefno");
            String mainproductids[] = request.getParameterValues("product");
            String assmbledProdQty[] = request.getParameterValues("assmbledProdQty");
            boolean isEdit = StringUtil.isNullOrEmpty(request.getParameter("isEdit")) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            for (int i = 0; i < productids.length; i++) {
//                addAsblyProdsNegativeEntry(productids[i], companyid);
                KwlReturnObject prodresult1 = accProductObj.getObject(ProductBuild.class.getName(), productids[i]);
                ProductBuild productbuild = (ProductBuild) prodresult1.getEntityList().get(0);
                //ERP-11730
                JSONObject assmblejson = new JSONObject();
                assmblejson.put(Constants.productid,mainproductids[i]);
                assmblejson.put("productBuildID",productbuild.getID());
                assmblejson.put("productBuildQuantity",productbuild.getQuantity());
                assmblejson.put(Constants.companyKey, companyid);
                accProductObj.updateQuantityDueOfSerailnumbers(assmblejson);

                // delete ProductBuild inventory
                if (productbuild.getInventory() != null) {
                    accProductObj.deleteInventory(productbuild.getInventory().getID(), companyid);
                }

                // delete ProductBuildDetail inventory
                KwlReturnObject result = accProductObj.getProductBuildDetailInventory(productbuild.getID());
                List<ProductBuildDetails> list = result.getEntityList();
                for (ProductBuildDetails buildDetails : list) {
                    if(buildDetails.getAproduct()!=null){
                        if(buildDetails.getRecycleQuantity()==0 && buildDetails.getRemainingQuantity() > 0 ){
                            buildDetails.getAproduct().setRecycleQuantity(buildDetails.getAproduct().getRecycleQuantity()-buildDetails.getRemainingQuantity());
                        }
                        buildDetails.getAproduct().setRecycleQuantity(buildDetails.getAproduct().getRecycleQuantity()+buildDetails.getRecycleQuantity());
                    }
                    accProductObj.deleteInventory(buildDetails.getInventory().getID(), companyid);
                }

                accProductObj.deleteProductBuildDetailsByID(productids[i], companyid);
                accProductObj.deleteProductbBuildByID(productids[i], companyid);
                accJournalEntryobj.deleteJournalEntryPermanent(productbuild.getJournalentry().getID(), sessionHandlerImpl.getCompanyid(request));

                if (!isEdit) {
                    auditTrailObj.insertAuditLog(AuditAction.PRODUCT_BUILD_ASSEMBLY_DELETION, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted product build Assembly " + productrefno[i], request, productids[i]);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public ModelAndView deleteProductBuildAssembly(HttpServletRequest request, HttpServletResponse response) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BuildAssembly_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            deleteExistingBuildAssemblyEntries(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.buildassembly.deletebuildaseembly", null, RequestContextUtils.getLocale(request));
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private void addAsblyProdsNegativeEntry(String buildProductid, String companyid) {
        try {
            List<ProductBuildDetails> list = accProductObj.getAssembyProducts(buildProductid, companyid);
            for (ProductBuildDetails pbd : list) {
                JSONObject inventoryjson = new JSONObject();
                inventoryjson.put(Constants.productid, pbd.getAproduct().getID());
                inventoryjson.put("quantity", "-" + (pbd.getAquantity() * pbd.getBuild().getQuantity()));
                inventoryjson.put("baseuomquantity", "-" + (pbd.getAquantity() * pbd.getBuild().getQuantity()));
                inventoryjson.put("baseuomrate", 1);

                inventoryjson.put("description", "Build Product Assembly for " + pbd.getAproduct().getName());
                inventoryjson.put("carryin", false);
                inventoryjson.put("defective", false);
                inventoryjson.put("newinventory", false);
                inventoryjson.put(Constants.companyKey, companyid);
                KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
            }
        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException se) {
        }
    }

    public ModelAndView importPriceListBandPrice(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));

            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());

            if (doAction.compareToIgnoreCase("import") == 0) {
                System.out.println("A(( Import start : " + new Date());
                JSONObject datajobj = new JSONObject();
                JSONObject resjson = new JSONObject(request.getParameter("resjson").toString());
                JSONArray resjsonJArray = resjson.getJSONArray("root");

                String filename = request.getParameter("filename");
                datajobj.put("filename", filename);

                String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
                File filepath = new File(destinationDirectory + "/" + filename);
                datajobj.put("FilePath", filepath);

                datajobj.put("resjson", resjsonJArray);

                jobj = accProductModuleService.importPriceListBandPriceRecords(request, datajobj);
                System.out.println("A(( Import end : " + new Date());
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                System.out.println("A(( Validation start : " + new Date());
                jobj = importHandler.validateFileData(requestParams);
                System.out.println("A(( Validation end : " + new Date());
            }
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException e) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject importPriceListBandPriceRecords(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
        BufferedReader br = null;
        int total = 0, failed = 0;
        String currencyId = sessionHandlerImpl.getCurrencyID(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("filename");
        String masterPreference = request.getParameter("masterPreference");

        JSONObject returnObj = new JSONObject();

        try {
            String dateFormat = null, dateFormatId = request.getParameter("dateFormat");
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", companyid);
            boolean isCurrencyCode=extraPref.isCurrencyCode();
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }

            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
//            sdf.setTimeZone(TimeZone.getTimeZone("GMT" + sessionHandlerImpl.getTimeZoneDifference(request)));
            DateFormat df = sdf;
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cnt = 0;

            StringBuilder failedRecords = new StringBuilder();

            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);

                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }

            HashMap currencyMap = getCurrencyMap(isCurrencyCode);

            while ((record = br.readLine()) != null) {
                String[] recarr = record.split(",");
                if (cnt == 0) {
                    failedRecords.append(createCSVrecord(recarr) + "\"Error Message\"");
                }
                if (cnt != 0) {
                    try {
                        currencyId = sessionHandlerImpl.getCurrencyID(request);

                        Product product = null;
                        if (columnConfig.containsKey(Constants.productid)) {
                            String productID = recarr[(Integer) columnConfig.get(Constants.productid)].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productID)) {
                                product = getProductByProductID(productID, companyid);

                                if (product == null) {
                                    throw new AccountingException("Product ID is not found for " + productID);
                                }
                            } else {
                                throw new AccountingException("Product ID is not available.");
                            }
                        } else {
                            throw new AccountingException("Product ID column is not found.");
                        }

                        String priceListBandID = "";
                        if (columnConfig.containsKey("priceListBand")) {
                            String priceListBandName = recarr[(Integer) columnConfig.get("priceListBand")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(priceListBandName)) {
                                priceListBandID = getPriceListBandIDByName(priceListBandName, companyid);

                                if (StringUtil.isNullOrEmpty(priceListBandID)) {
                                    throw new AccountingException("Price List - Band is not found for " + priceListBandName);
                                }
                            } else {
                                throw new AccountingException("Price List - Band is not available.");
                            }
                        } else {
                            throw new AccountingException("Price List - Band column is not found.");
                        }

                        String productPurchasePrice = "";
                        if (columnConfig.containsKey("purchasePrice")) {
                            productPurchasePrice = recarr[(Integer) columnConfig.get("purchasePrice")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(productPurchasePrice)) {
                                throw new AccountingException("Product Purchase Price is not available");
                            }
                        } else {
                            throw new AccountingException("Purchase Price column is not found.");
                        }

                        String productSalesPrice = "";
                        if (columnConfig.containsKey("salesPrice")) {
                            productSalesPrice = recarr[(Integer) columnConfig.get("salesPrice")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(productSalesPrice)) {
                                throw new AccountingException("Product Sales Price is not available");
                            }
                        } else {
                            throw new AccountingException("Sales Price column is not found.");
                        }

                        if (isCurrencyCode?columnConfig.containsKey("currencyCode"):columnConfig.containsKey("currencyName")) {
                            String productPriceCurrencyStr = isCurrencyCode? recarr[(Integer) columnConfig.get("currencyCode")].replaceAll("\"", "").trim():recarr[(Integer) columnConfig.get("currencyName")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(productPriceCurrencyStr)) {
                                currencyId = getCurrencyId(productPriceCurrencyStr, currencyMap);

                                if (StringUtil.isNullOrEmpty(currencyId)) {
                                    throw new AccountingException(messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, RequestContextUtils.getLocale(request)));
                                }
                            } else {
                                throw new AccountingException("Currency is not available.");
                            }
                        }

                        Date applicableDate = null;
                        if (columnConfig.containsKey("applicableDate")) {
                            String applicableDateStr = recarr[(Integer) columnConfig.get("applicableDate")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(applicableDateStr)) {
                                throw new AccountingException("Applicable Date is not available");
                            } else {
                                applicableDate = df.parse(applicableDateStr);
                            }
                        } else {
                            throw new AccountingException("Applicable Date column is not found.");
                        }

                        // For save Purchase and Sales Price
                        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                        requestParams.put("pricingBandMasterID", priceListBandID);
                        requestParams.put("purchasePrice", productPurchasePrice);
                        requestParams.put("salesPrice", productSalesPrice);
                        requestParams.put("currencyID", currencyId);
                        requestParams.put("productID", product.getID());
                        requestParams.put("companyID", companyid);
                        requestParams.put("applicableDate", applicableDate);
                        requestParams.put("isSavePricingBandMasterDetails", true);

                        KwlReturnObject result = accMasterItemsDAOobj.getPriceOfProductForPricingBandAndCurrency(requestParams);

                        if (result.getEntityList() != null && !result.getEntityList().isEmpty()) { // for edit case
                            Object[] priceObj = (Object[]) result.getEntityList().get(0);
                            requestParams.put("rowid", priceObj[2]);
                        }

                        accMasterItemsDAOobj.saveOrUpdatePricingBandMasterDetails(requestParams);
                    } catch (Exception ex) {
                        failed++;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                        }
                        failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }
                cnt++;
            }

            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = messageSource.getMessage("acc.field.Emptyfile", null, RequestContextUtils.getLocale(request));
            } else if (success == 0) {
//                issuccess = false;
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
            } else if (success == total) {
                msg = messageSource.getMessage("acc.rem.168", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request))+ " " + success + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
                msg += (failed == 0 ? "." : messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request))+ " " + failed + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { // if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            br.close();

            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                // Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed>0?"csv":"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_Price_List_Band_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);

                String tableName = importDao.getTableName(fileName);
                importDao.removeFileTable(tableName); // Remove table after importing all records

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put(Constants.RES_success, issuccess);
                returnObj.put(Constants.RES_msg, msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }

    private String getPriceListBandIDByName(String priceListBandName, String companyID) throws AccountingException {
        String priceListBandID = "";
        try {
            if (!StringUtil.isNullOrEmpty(priceListBandName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getPriceListBandIDByName(priceListBandName, companyID);
                List list = retObj.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    priceListBandID = (String) itr.next();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Price List - Band");
        }
        return priceListBandID;
    }

    public ModelAndView importPriceListVolumeDiscount(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String doAction = request.getParameter("do");
            System.out.println("A(( " + doAction + " start : " + new Date());

            JSONObject datajobj = new JSONObject();
            if (doAction.compareToIgnoreCase("getMapCSV") == 0) {
                datajobj = importHandler.getMappingCSVHeader(request);
                JSONArray jSONArray = datajobj.getJSONArray("Header");
                validateHeadersForPriceListVolumeDiscount(jSONArray);

                jobj = importPriceListVolumeDiscountRecords(request, datajobj);
                issuccess = true;
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException e) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }

    public void validateHeadersForPriceListVolumeDiscount(JSONArray validateJArray) throws AccountingException, ServiceException {
        try {
            List<String> list = new ArrayList<String>();
            list.add("Product ID");
            list.add("Price List - Volume Discount");
            list.add("Purchase Price");
            list.add("Sales Price");
            list.add("Currency");
            list.add("Minimum Qty");
            list.add("Maximum Qty");
            list.add("Applicable Date");

            List<String> fileHeaderList = new ArrayList<String>();

            for (int i = 0; i < validateJArray.length(); i++) {
                String header = validateJArray.getJSONObject(i).getString("header").trim();
                fileHeaderList.add(header);
            }

            // iterating for manadatory columns
            for (String manadatoryField : list) {
                if (!fileHeaderList.contains(manadatoryField)) {
                    throw new AccountingException(manadatoryField + " column is not availabe in file");
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public JSONObject importPriceListVolumeDiscountRecords(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
        BufferedReader br = null;
        int total = 0, failed = 0;
        String currencyId = sessionHandlerImpl.getCurrencyID(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("name");

        JSONObject returnObj = new JSONObject();

        try {
            String dateFormat = null, dateFormatId = request.getParameter("dateFormat");

            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }

            DateFormat df = new SimpleDateFormat(dateFormat);
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", companyid);
            boolean isCurrencyCode=extraPref.isCurrencyCode();
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cnt = 0;

            StringBuilder failedRecords = new StringBuilder();

            JSONArray jSONArray = jobj.getJSONArray("Header");
            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                headArrayList.add(jSONObject.get("header"));
            }

            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\"");
            HashMap currencyMap = getCurrencyMap(isCurrencyCode);

            while ((record = br.readLine()) != null) {
                if (cnt != 0) {
                    String[] recarr = record.split(",");
                    try {
                        String productID = recarr[0].trim();
                        if (!StringUtil.isNullOrEmpty(productID)) {
                            productID = productID.replaceAll("\"", "");
                        } else {
                            throw new AccountingException("Product ID is not available");
                        }

                        String priceListVolumeDiscountID = "";
                        String priceListBandName = recarr[1].trim();
                        if (!StringUtil.isNullOrEmpty(priceListBandName)) {
                            priceListBandName = priceListBandName.replaceAll("\"", "");

                            priceListVolumeDiscountID = getPriceListBandIDByName(priceListBandName, companyid);
                            if (StringUtil.isNullOrEmpty(priceListVolumeDiscountID)) {
                                throw new AccountingException("Price List - Volume Discount is not found for name " + priceListBandName);
                            }
                        }

                        String productPurchasePrice = recarr[2].trim();
                        if (!StringUtil.isNullOrEmpty(productPurchasePrice)) {
                            productPurchasePrice = productPurchasePrice.replaceAll("\"", "");
                        } else {
                            throw new AccountingException("Product Purchase Price is not available");
                        }

                        String productSalesPrice = recarr[3].trim();
                        if (!StringUtil.isNullOrEmpty(productSalesPrice)) {
                            productSalesPrice = productSalesPrice.replaceAll("\"", "");
                        } else {
                            throw new AccountingException("Product Sales Price is not available");
                        }

                        String productPriceCurrencyStr = recarr[4].trim();
                        if (!StringUtil.isNullOrEmpty(productPriceCurrencyStr)) {
                            productPriceCurrencyStr = productPriceCurrencyStr.replaceAll("\"", "");
                        }
                        currencyId = getCurrencyId(productPriceCurrencyStr, currencyMap);
                        if (StringUtil.isNullOrEmpty(currencyId)) {
                            String MsgExep = messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, RequestContextUtils.getLocale(request));
                            throw new AccountingException(MsgExep);
                        }

                        String minimumQty = recarr[5].trim();
                        if (!StringUtil.isNullOrEmpty(minimumQty)) {
                            minimumQty = minimumQty.replaceAll("\"", "");
                        } else {
                            throw new AccountingException("Minimum Qty is not available");
                        }

                        String maximumQty = recarr[6].trim();
                        if (!StringUtil.isNullOrEmpty(maximumQty)) {
                            maximumQty = maximumQty.replaceAll("\"", "");
                        } else {
                            throw new AccountingException("Maximum Qty is not available");
                        }

                        Date applicableDate = null;
                        String applicableDateStr = recarr[7].trim();
                        if (!StringUtil.isNullOrEmpty(applicableDateStr)) {
                            applicableDateStr = applicableDateStr.replaceAll("\"", "");
                            applicableDate = df.parse(applicableDateStr);
                        } else {
                            throw new AccountingException("Applicable Date is not available");
                        }

                        // getting product object
                        KwlReturnObject result = accProductObj.getProductIDCount(productID, companyid,false);
                        int nocount = result.getRecordTotalCount();
                        if (nocount == 0) {
                            throw new AccountingException("productID '" + productID + "' not exists.");
                        }
                        Product product = (Product) result.getEntityList().get(0);

                        // For save volume discount
                        if (productPurchasePrice.length() > 0) {
                            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                            requestParams.put("pricingBandMasterID", priceListVolumeDiscountID);
                            requestParams.put("purchasePrice", productPurchasePrice);
                            requestParams.put("salesPrice", productSalesPrice);
                            requestParams.put("currencyID", currencyId);
                            requestParams.put("productID", product.getID());
                            requestParams.put("companyID", companyid);
                            requestParams.put("minimumQty", Integer.parseInt(minimumQty));
                            requestParams.put("maximumQty", Integer.parseInt(maximumQty));
                            requestParams.put("applicableDate", applicableDate);
                            requestParams.put("isVolumeDiscount", true);
                            requestParams.put("isSavePricingBandMasterDetails", true);

                            result = accMasterItemsDAOobj.getPriceOfProductForPricingBandAndCurrency(requestParams);

                            if (result.getEntityList() != null && !result.getEntityList().isEmpty()) { // for edit case
                                Object[] priceObj = (Object[]) result.getEntityList().get(0);
                                requestParams.put("rowid", priceObj[2]);
                            }

                            accMasterItemsDAOobj.saveOrUpdatePricingBandMasterDetails(requestParams);
                        }
                    } catch (Exception ex) {
                        failed++;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                        }
                        failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }
                cnt++;
            }

            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = messageSource.getMessage("acc.field.Emptyfile", null, RequestContextUtils.getLocale(request));
            } else if (success == 0) {
//                issuccess = false;
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
            } else if (success == total) {
                msg = messageSource.getMessage("acc.rem.168", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request))+ " " + success + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
                msg += (failed == 0 ? "." : " "+messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request))+ " " + failed + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { // if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            br.close();

            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                // Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed>0?"csv":"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_Product_Master_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put(Constants.RES_success, issuccess);
                returnObj.put(Constants.RES_msg, msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }

    public void SendMail(HashMap requestParams) throws ServiceException{

        String loginUserId = (String) requestParams.get("loginUserId");
        User user = (User) kwlCommonTablesDAOObj.getClassObject(User.class.getName(), loginUserId);
        KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), user.getCompany().getCompanyID());
        Company company = (Company) returnObject.getEntityList().get(0);
        String sendorInfo = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
        String fileName = (String) requestParams.get("productExportFileName");
        String exportFileType = (String) requestParams.get("filetype");
        fileName = fileName+"."+exportFileType;

        String cEmail=user.getEmailID()!=null?user.getEmailID():"";
        if(!StringUtil.isNullOrEmpty(cEmail)){
            try {
            String subject="Product Report Export Status";
                //String sendorInfo="admin@deskera.com";
            String htmlTextC="";
            htmlTextC+="<br/>Hello "+user.getFirstName()+"<br/>"; 
            htmlTextC+="<br/>Product Report <b>\""+fileName+"\"</b> has been generated successfully.<br/><br/> You can download it from <b> Export Details Report</b>.<br/>";

            htmlTextC+="<br/>Regards,<br/>";
            htmlTextC+="<br/>ERP System<br/>";

            String plainMsgC="";
            plainMsgC+="\nHello "+user.getFirstName()+"\n"; 
            plainMsgC+="\nProduct Report <b>\""+fileName+"\"</b> has been generated successfully.<br/><br/> You can download it from <b> Export Details Report</b>.\n";
                        plainMsgC+="\nRegards,\n";
            plainMsgC+="\nERP System\n";

                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
            SendMailHandler.postMail(new String[]{cEmail},subject,htmlTextC,plainMsgC,sendorInfo, smtpConfigMap);
            } catch (Exception ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    public ModelAndView getBuildAssemblyProducts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            KwlReturnObject result = accProductObj.getBuildAssemblyProducts(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();
            JSONArray DataJArr = productHandler.getAssemblyProductsJSON(request, list);
            jobj.put(Constants.RES_data, DataJArr);
            jobj.put(Constants.RES_TOTALCOUNT, count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

//Import & Build assembly Product from XLS File
//    public JSONObject importAssemblyProductXLS(HttpServletRequest request, HttpServletResponse response) throws AccountingException, IOException, SessionExpiredException, JSONException {
//        
//    }
    
   

    //For BOM We need Product Type.
    public Producttype getProductTypeByProductID(String productTypeID) throws AccountingException {
        Producttype producttype = null;
        try {
            if (!StringUtil.isNullOrEmpty(productTypeID)) {
                KwlReturnObject retObj = accProductObj.getProductTypeByProductID(productTypeID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    producttype = (Producttype) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product Type");
        }
        return producttype;
    }

    //For BOM We need Product Type.
    public Product getProductByProductName(String companyid, String productTypeID) throws AccountingException {
        return accProductModuleService.getProductByProductName(companyid, productTypeID);
    }

    private String getProductCategoryIDByName(String productCategoryName, String companyID) throws AccountingException {
        return accProductModuleService.getProductCategoryIDByName(productCategoryName, companyID);
    }

    public ModelAndView getReportSchema(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(request.getParameter("reportId"))) {
                requestParams.put("reportId", request.getParameter("reportId"));
            }
            KwlReturnObject result = accProductObj.getReportSchema(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getReportSchemaJson(request, list);
            jobj.put(Constants.RES_data, DataJArr);
            jobj.put(Constants.RES_TOTALCOUNT, count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getReportSchemaJson(HttpServletRequest request, List list) throws JSONException, ServiceException, SessionExpiredException, AccountingException, ParseException {
        JSONArray jArr = new JSONArray();
        String msg = "";
        try{
            for (Object object : list) {
                JSONObject obj = new JSONObject();
                Object[] arr = (Object[]) object;
                obj.put(Constants.Acc_id, arr[0]);
                obj.put("name", arr[1]);
                obj.put("reportschema", arr[2]);
                jArr.put(obj);
            }
        } catch(Exception e) {
            msg = "" + e.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, e);
        }
        return jArr;
    }

    public ModelAndView getNewBatchDetailsForProduct(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String productid = !StringUtil.isNullOrEmpty(request.getParameter(Constants.productid)) ? request.getParameter(Constants.productid) : "" ;

            KwlReturnObject prod = accountingHandlerDAOobj.getObject(Product.class.getName(), productid);
            Product product = (Product) prod.getEntityList().get(0);

            JSONObject obj = new JSONObject();
            try {
                //product level batch and serial no on or not
                obj.put("batchdetails", getNewBatchJson(product, request, product.getID(),new ArrayList()));
            } catch (SessionExpiredException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }

            jobj.put(Constants.RES_data, obj);
            issuccess = true;
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deletePriceList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Product_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deletePriceList(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.priceList.del", null, RequestContextUtils.getLocale(request)); // "Price List entry has been deleted successfully.";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deletePriceList(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransaction = "";
        try {
            JSONArray jArr = new JSONArray(request.getParameter(Constants.RES_data));
            String companyID = sessionHandlerImpl.getCompanyid(request);

            String priceListID = "";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                priceListID = jobj.getString("priceid");
                KwlReturnObject priceListResult = accountingHandlerDAOobj.getObject(PriceList.class.getName(), priceListID);
                PriceList priceList = (PriceList) priceListResult.getEntityList().get(0);

                HashMap<String, Object> requestParams = new HashMap<>();
                requestParams.put("priceListID", priceListID);
                requestParams.put("companyID", companyID);
                accProductObj.deletePriceList(requestParams);
                auditTrailObj.insertAuditLog(AuditAction.PRICE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a Price List entry for product " + priceList.getProduct().getProductid(), request, priceListID);
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return linkedTransaction;
    }
    
    public ModelAndView getAssetTransferDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj1 = new JSONObject();
        JSONArray tranasferDetail = new JSONArray(); // Variable to put current Data which we are populating to transfer
        JSONArray transferHistory = new JSONArray(); // Vaiable to put history of Asset Transfer
        boolean issuccess = false;
        String msg = "";
        Product product = null;
        String assetId = null;
        try {
            boolean isBatchForProduct = false;
            boolean isSerialForProduct = false;
            boolean isLocationForProduct = false;
            boolean isWarehouseForProduct = false;
            boolean isRowForProduct = false;
            boolean isRackForProduct = false;
            boolean isBinForProduct = false;

            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyId = sessionHandlerImpl.getCompanyid(request);

            boolean excludeSoldAssets = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("excludeSoldAssets"))) {
                excludeSoldAssets = Boolean.parseBoolean(request.getParameter("excludeSoldAssets"));
            }

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyId", companyId);
            requestParams.put("invrecord", true);

            if (excludeSoldAssets) {
                requestParams.put("excludeSoldAsset", excludeSoldAssets);
            }
            String assetDetailsIds = "";
            if (request.getParameter("assetDetailIds") != null) {
                assetDetailsIds = request.getParameter("assetDetailIds");
                requestParams.put("assetDetailsIds", assetDetailsIds);
            }

            KwlReturnObject result = accProductObj.getAssetDetails(requestParams);
            List<AssetDetails> list = result.getEntityList();

            String documentIds = "";
            for (AssetDetails ad : list) {
                documentIds += "'" + ad.getId() + "',";
            }
            Map<String, List<Object[]>> baMap = new HashMap<>();
            boolean linkingFlag = (StringUtil.isNullOrEmpty(request.getParameter("linkingFlag"))) ? false : Boolean.parseBoolean(request.getParameter("linkingFlag"));
            boolean isEdit = (StringUtil.isNullOrEmpty(request.getParameter("isEdit"))) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            Map<String, Object> batchSerialReqMap = new HashMap<>();
            batchSerialReqMap.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            batchSerialReqMap.put(Constants.df, df);
            batchSerialReqMap.put("linkingFlag", linkingFlag);
            batchSerialReqMap.put("isEdit", isEdit);
            if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory()) {  //check if company level option is on then only we will check productt level
                batchSerialReqMap.put("documentIds", StringUtil.isNullOrEmpty(documentIds) ? "" : documentIds.substring(0, documentIds.length() - 1));
                baMap = getBatchDetailsMap(batchSerialReqMap);
            }

            for (AssetDetails assetDetails : list) {
                // Put Asset Transfer Details
                JSONObject transferData = new JSONObject();
                assetId = assetDetails.getAssetId();
                transferData.put("assetId", assetDetails.getAssetId());
                transferData.put("department", assetDetails.getDepartment() != null ? assetDetails.getDepartment().getId() : "");
                transferData.put("assetUser", (assetDetails.getAssetUser() != null) ? assetDetails.getAssetUser().getUserID() : "N/A");
                transferData.put("assetdetailId", assetDetails.getId());
                transferData.put("assetGroup", assetDetails.getProduct().getName());
                transferData.put("assetDepreciationMethod", assetDetails.getProduct().getDepreciationMethod());
                transferData.put("assetGroupId", assetDetails.getProduct().getID());
                transferData.put("assetId", assetDetails.getAssetId());
                transferData.put("assetdescription", assetDetails.getAssetDescription());
                transferData.put("installationDate", df.format(assetDetails.getInstallationDate()));
                transferData.put("purchaseDate", df.format(assetDetails.getPurchaseDate()));
                transferData.put("assetValue", assetDetails.getCost());
                transferData.put("salvageRate", assetDetails.getSalvageRate());
                transferData.put("salvageValue", assetDetails.getSalvageValue());
                transferData.put("openingDepreciation", assetDetails.getOpeningDepreciation());
                transferData.put("cost", (assetDetails.getCost()));
                transferData.put("salvageRate", assetDetails.getSalvageRate());
                transferData.put("salvageValue", assetDetails.getSalvageValue());
                transferData.put("salvageValueInForeignCurrency", assetDetails.getSalvageValue());
                transferData.put("costInForeignCurrency", assetDetails.getCost());
                transferData.put("accumulatedDepreciation", assetDetails.getAccumulatedDepreciation());
                transferData.put("assetLife", assetDetails.getAssetLife());
                transferData.put("elapsedLife", assetDetails.getElapsedLife());
                transferData.put("nominalValue", assetDetails.getNominalValue());
                transferData.put("isAssetSold", (assetDetails.getAssetSoldFlag() != 0) ? true : false);
                transferData.put("isLeased", assetDetails.isLinkedToLeaseSO());

                // Put Custom Field And Dimensions

                HashMap<String, Object> fieldrequestParams1 = new HashMap();
                fieldrequestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams1.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_FixedAssets_Details_ModuleId, 1));

                HashMap<String, String> customFieldMap1 = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap1 = new HashMap<String, String>();
                HashMap<String, String> replaceFieldMap11 = new HashMap<String, String>();
                HashMap<String, Integer> fieldMap1 = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams1, replaceFieldMap11, customFieldMap1, customDateFieldMap1);

                Map<String, Object> variableMap = new HashMap<String, Object>();
                AssetDetailsCustomData jeDetailCustom = (AssetDetailsCustomData) assetDetails.getAssetDetailsCustomData();
                replaceFieldMap11 = new HashMap<String, String>();
                if (jeDetailCustom != null) {
                    AccountingManager.setCustomColumnValues(jeDetailCustom, fieldMap1, replaceFieldMap11, variableMap);
                    JSONObject params = new JSONObject();
                    params.put(Constants.companyKey, companyId);
                    params.put("getCustomFieldArray", true);
                    params.put("isExport", false);
                    fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap1, customDateFieldMap1, transferData, params);
                }

                // Put Warehouse and location
                if (!StringUtil.isNullOrEmpty(assetDetails.getProduct().getID())) {
                    KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), assetDetails.getProduct().getID());
                    product = (Product) prodresult.getEntityList().get(0);
                    isBatchForProduct = product.isIsBatchForProduct();
                    isSerialForProduct = product.isIsSerialForProduct();
                    isLocationForProduct = product.isIslocationforproduct();
                    isWarehouseForProduct = product.isIswarehouseforproduct();
                    isRowForProduct = product.isIsrowforproduct();
                    isRackForProduct = product.isIsrackforproduct();
                    isBinForProduct = product.isIsbinforproduct();
                }

                if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory()) {  //check if company level option is on then only we will check productt level
                    if (isBatchForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct) {
                        KwlReturnObject kmsg = null;
                        List batchserialdetails = new ArrayList();
                        if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && product.isIsSerialForProduct()) {
                            kmsg = accCommonTablesDAO.getOnlySerialDetails(assetDetails.getId(), false, "121", false, true);
                            batchserialdetails = kmsg.getEntityList();
                        } else {
                            if (!product.isIsSerialForProduct() && baMap.containsKey(assetDetails.getId())) {
                                batchserialdetails = baMap.get(assetDetails.getId());
                            } else {
                                kmsg = accCommonTablesDAO.getBatchSerialDetails(assetDetails.getId(), !product.isIsSerialForProduct(), false, "121", false, true, "");
                                batchserialdetails = kmsg.getEntityList();
                            }
                        }
                        Iterator iter = batchserialdetails.iterator();
                        while (iter.hasNext()) {
                            Object[] objArr = (Object[]) iter.next();
                            if (objArr[2] != null) {
                                String locationId = objArr[2].toString();
                                if (!StringUtil.isNullOrEmpty(locationId)) {
                                    KwlReturnObject loct = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), locationId);
                                    InventoryLocation location = (InventoryLocation) loct.getEntityList().get(0);
                                    transferData.put("location", !StringUtil.isNullOrEmpty(location.getId()) ? location.getId() : "N/A");
                                } else {
                                    transferData.put("location", "N/A");
                                }
                            }
                            if (objArr[3] != null) {
                                String warehouseId = objArr[3].toString();
                                if (!StringUtil.isNullOrEmpty(warehouseId)) {
                                    KwlReturnObject war = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), warehouseId);
                                    InventoryWarehouse warehouse = (InventoryWarehouse) war.getEntityList().get(0);
                                    transferData.put("warehouse", !StringUtil.isNullOrEmpty(warehouse.getId()) ? warehouse.getId() : "N/A");
                                } else {
                                    transferData.put("warehouse", "N/A");
                                }
                            }
                        }
                    }
                }

                if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {  //check if company level option is on then only we will check productt level
                    if (isBatchForProduct || isSerialForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct) {  //product level batch and serial no on or not
                        transferData.put("batchdetails", getNewBatchJson(product, request, assetDetails.getId(), new ArrayList()));
                    }
                }

                tranasferDetail.put(transferData);
            }
            jobj1.put(Constants.RES_data, tranasferDetail.toString());

            // Put Asset Transfer History                

            HashMap<String, Object> historyRequestParams = new HashMap<String, Object>();
            historyRequestParams.put("companyId", companyId);
            historyRequestParams.put("invrecord", true);

            if (excludeSoldAssets) {
                historyRequestParams.put("excludeSoldAsset", excludeSoldAssets);
            }

            historyRequestParams.put("assetId", assetId);
            historyRequestParams.put("isTransferHistory",true);

            KwlReturnObject historyResult = accProductObj.getAssetDetails(historyRequestParams);
            List<AssetDetails> historyList = historyResult.getEntityList();

            String historyDocumentIds = "";
            for (AssetDetails ad : historyList) {
                historyDocumentIds += "'" + ad.getId() + "',";
            }
            Map<String, List<Object[]>> historyBaMap = new HashMap<>();
            Map<String, Object> batchSerialReqMap1 = new HashMap<>();
            batchSerialReqMap1.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            batchSerialReqMap1.put(Constants.df, df);
            batchSerialReqMap1.put("linkingFlag", linkingFlag);
            batchSerialReqMap1.put("isEdit", isEdit);
            if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory()) {  //check if company level option is on then only we will check productt level
                batchSerialReqMap1.put("documentIds", StringUtil.isNullOrEmpty(historyDocumentIds) ? "" : historyDocumentIds.substring(0, historyDocumentIds.length() - 1));
                baMap = getBatchDetailsMap(batchSerialReqMap1);
            }

            for (AssetDetails assetDetailsHistory : historyList) {
                // Put Asset Transfer History Details
                JSONObject transferHistoryData = new JSONObject();
                transferHistoryData.put("assetId", assetDetailsHistory.getAssetId());
                transferHistoryData.put("assetUser", (assetDetailsHistory.getAssetUser() != null) ? assetDetailsHistory.getAssetUser().getUserID() : "N/A");
                transferHistoryData.put("assetdetailId", assetDetailsHistory.getId());
                transferHistoryData.put("assetGroup", assetDetailsHistory.getProduct().getName());
                transferHistoryData.put("assetDepreciationMethod", assetDetailsHistory.getProduct().getDepreciationMethod());
                transferHistoryData.put("assetGroupId", assetDetailsHistory.getProduct().getID());
                transferHistoryData.put("assetId", assetDetailsHistory.getAssetId());
                transferHistoryData.put("assetdescription", assetDetailsHistory.getAssetDescription());
                transferHistoryData.put("installationDate", df.format(assetDetailsHistory.getInstallationDate()));
                transferHistoryData.put("purchaseDate", df.format(assetDetailsHistory.getPurchaseDate()));
                transferHistoryData.put("assetValue", assetDetailsHistory.getCost());
                transferHistoryData.put("salvageRate", assetDetailsHistory.getSalvageRate());
                transferHistoryData.put("salvageValue", assetDetailsHistory.getSalvageValue());
                transferHistoryData.put("openingDepreciation", assetDetailsHistory.getOpeningDepreciation());
                transferHistoryData.put("cost", (assetDetailsHistory.getCost()));
                transferHistoryData.put("salvageRate", assetDetailsHistory.getSalvageRate());
                transferHistoryData.put("salvageValue", assetDetailsHistory.getSalvageValue());
                transferHistoryData.put("salvageValueInForeignCurrency", assetDetailsHistory.getSalvageValue());
                transferHistoryData.put("costInForeignCurrency", assetDetailsHistory.getCost());
                transferHistoryData.put("accumulatedDepreciation", assetDetailsHistory.getAccumulatedDepreciation());
                transferHistoryData.put("assetLife", assetDetailsHistory.getAssetLife());
                transferHistoryData.put("elapsedLife", assetDetailsHistory.getElapsedLife());
                transferHistoryData.put("nominalValue", assetDetailsHistory.getNominalValue());
                transferHistoryData.put("isAssetSold", (assetDetailsHistory.getAssetSoldFlag() != 0) ? true : false);
                transferHistoryData.put("isLeased", assetDetailsHistory.isLinkedToLeaseSO());
                transferHistoryData.put("transferDate", assetDetailsHistory.getTransferDate() != null ? df.format(assetDetailsHistory.getTransferDate()) :"");
                
                // Put Custom Field And Dimensions

                HashMap<String, Object> fieldrequestParams2 = new HashMap();
                fieldrequestParams2.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams2.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_FixedAssets_Details_ModuleId, 1));

                HashMap<String, String> customFieldMap2 = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap2 = new HashMap<String, String>();
                HashMap<String, String> replaceFieldMap12 = new HashMap<String, String>();
                HashMap<String, Integer> fieldMap2 = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams2, replaceFieldMap12, customFieldMap2, customDateFieldMap2);

                Map<String, Object> variableMap1 = new HashMap<String, Object>();
                AssetDetailsCustomData jeDetailCustom1 = (AssetDetailsCustomData) assetDetailsHistory.getAssetDetailsCustomData();
                replaceFieldMap12 = new HashMap<String, String>();
                if (jeDetailCustom1 != null) {
                    AccountingManager.setCustomColumnValues(jeDetailCustom1, fieldMap2, replaceFieldMap12, variableMap1);
                    JSONObject params = new JSONObject();
                    params.put(Constants.companyKey, companyId);
                    params.put("getCustomFieldArray", true);
                    params.put("isExport", false);
                    fieldDataManagercntrl.getLineLevelCustomData(variableMap1, customFieldMap2, customDateFieldMap2, transferHistoryData, params);
                }

                // Put Warehouse and location
                if (!StringUtil.isNullOrEmpty(assetDetailsHistory.getProduct().getID())) {
                    KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), assetDetailsHistory.getProduct().getID());
                    product = (Product) prodresult.getEntityList().get(0);
                    isBatchForProduct = product.isIsBatchForProduct();
                    isSerialForProduct = product.isIsSerialForProduct();
                    isLocationForProduct = product.isIslocationforproduct();
                    isWarehouseForProduct = product.isIswarehouseforproduct();
                    isRowForProduct = product.isIsrowforproduct();
                    isRackForProduct = product.isIsrackforproduct();
                    isBinForProduct = product.isIsbinforproduct();
                }

                if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory()) {  //check if company level option is on then only we will check productt level
                    if (isBatchForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct) {
                        KwlReturnObject kmsg = null;
                        List batchserialdetails = new ArrayList();
                        if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && product.isIsSerialForProduct()) {
                            kmsg = accCommonTablesDAO.getOnlySerialDetails(assetDetailsHistory.getId(), false, "121", false, true);
                            batchserialdetails = kmsg.getEntityList();
                        } else {
                            if (!product.isIsSerialForProduct() && historyBaMap.containsKey(assetDetailsHistory.getId())) {
                                batchserialdetails = historyBaMap.get(assetDetailsHistory.getId());
                            } else {
                                kmsg = accCommonTablesDAO.getBatchSerialDetails(assetDetailsHistory.getId(), !product.isIsSerialForProduct(), false, "121", false, true, "");
                                batchserialdetails = kmsg.getEntityList();
                            }
                        }
                        Iterator iter = batchserialdetails.iterator();
                        while (iter.hasNext()) {
                            Object[] objArr = (Object[]) iter.next();
                            if (objArr[2] != null) {
                                String locationId = objArr[2].toString();
                                if (!StringUtil.isNullOrEmpty(locationId)) {
                                    KwlReturnObject loct = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), locationId);
                                    InventoryLocation location = (InventoryLocation) loct.getEntityList().get(0);
                                    transferHistoryData.put("location", !StringUtil.isNullOrEmpty(location.getId()) ? location.getId() : "N/A");
                                } else {
                                    transferHistoryData.put("location", "N/A");
                                }
                            }
                            if (objArr[3] != null) {
                                String warehouseId = objArr[3].toString();
                                if (!StringUtil.isNullOrEmpty(warehouseId)) {
                                    KwlReturnObject war = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), warehouseId);
                                    InventoryWarehouse warehouse = (InventoryWarehouse) war.getEntityList().get(0);
                                    transferHistoryData.put("warehouse", !StringUtil.isNullOrEmpty(warehouse.getId()) ? warehouse.getId() : "N/A");
                                } else {
                                    transferHistoryData.put("warehouse", "N/A");
                                }
                            }
                        }
                    }
                }

                if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {  //check if company level option is on then only we will check productt level
                    if (isBatchForProduct || isSerialForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct) {  //product level batch and serial no on or not
                        transferHistoryData.put("batchdetails", getNewBatchJson(product, request, assetDetailsHistory.getId(), new ArrayList()));
                    }
                }
                transferHistory.put(transferHistoryData);
            }
            jobj1.put("historydata", transferHistory.toString());
            issuccess = true;
        } catch (JSONException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj1.put(Constants.RES_success, issuccess);
                jobj1.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj1.toString());
    }

    public ModelAndView saveAssetTransferDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj1 = new JSONObject();
        boolean issuccess = false;
        boolean validTransfer = true;
        String msg = "";
        try {
            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(Constants.Acc_id, sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject extraresult = accCompanyPreferencesObj.getExtraCompanyPreferences(filterParams);
            ExtraCompanyPreferences extra = null;
            if (extraresult.getEntityList().size() > 0) {
                extra = (ExtraCompanyPreferences) extraresult.getEntityList().get(0);
            }
            int depreciationCalculationType = extra.getAssetDepreciationCalculationType();
            
            String newFixedAssetDetail = request.getParameter("newAssetDetails");
            JSONArray newJArr = new JSONArray(newFixedAssetDetail);
            String companyId = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Date transferDate = new Date(); // Variable to consider selected transfer Date 
            
            for (int i = 0; i < newJArr.length(); i++) {
                
                JSONObject jobj = newJArr.getJSONObject(i);
                String assetDetailId = jobj.optString("assetdetailId");
                String transferDateStr = jobj.getString("transferDate");
                transferDate = df.parse(transferDateStr);

                KwlReturnObject accresult = accountingHandlerDAOobj.getObject(AssetDetails.class.getName(), assetDetailId);
                AssetDetails assetDetails = (AssetDetails) accresult.getEntityList().get(0);
                
                // Check if selected Asset is already disposed
                if (assetDetails.isIsDisposed()) {
                    msg = messageSource.getMessage("acc.msg.Assetisalreadydisposed", null, RequestContextUtils.getLocale(request)); // Selected Asset is already disposed
                    issuccess = false;
                    validTransfer = false;
                    break;
                }
                //Check if disposal date is before the installation date the simply show the message and break the request                
                if (transferDate.before(assetDetails.getInstallationDate())) {
                    msg = messageSource.getMessage("acc.msg.transferdateislessthaninstallation", null, RequestContextUtils.getLocale(request));// "Entered Transfer Date for the selected Asset is less than the Installation Date, So Asset can not be Transfered.";
                    issuccess = false;
                    validTransfer = false;
                    break;
                }
                if (assetDetails.getAssetSoldFlag() == AssetDetails.AssetsSoldFromDO) {
                    msg = messageSource.getMessage("acc.msg.Assetisalreadydelivered", null, RequestContextUtils.getLocale(request));// "Selected Asset is already delivered.";
                    issuccess = false;
                    validTransfer = false;
                    break;
                }
                if (assetDetails.getAssetSoldFlag() == AssetDetails.AssetsSoldFromCI) {
                    msg = messageSource.getMessage("acc.msg.Assetisalreadyinvoiced", null, RequestContextUtils.getLocale(request));// "Selected Asset is already invoiced.";
                    issuccess = false;
                    validTransfer = false;
                    break;
                }
                Calendar cal1 = Calendar.getInstance();
                cal1.setTime(transferDate);
                cal1.add(Calendar.DATE, -1);
                Calendar cal2 = Calendar.getInstance();
                cal2.setTime(assetDetails.getInstallationDate());
                int period = -1;
                int creationyear1 = cal2.get(Calendar.YEAR);
                int year = cal1.get(Calendar.YEAR);
                int yeardiff = year - creationyear1;
                if (depreciationCalculationType == 0) {
                    if (yeardiff < 0) {  //if selected year is less than the cretion year then there will be no depreciation to show
                        continue;
                    }
                    period = yeardiff + 1;
                } else {
                    period = (12 * yeardiff) + transferDate.getMonth() + 1;
                    period = cal2.get(Calendar.MONTH) != 0 ? period - cal2.get(Calendar.MONTH) : period;
                }

                HashMap<String, Object> futureFilters = new HashMap<String, Object>();
                futureFilters.put("period", period + 1);
                futureFilters.put("assetDetailsId", assetDetails.getId());
                futureFilters.put(Constants.companyKey, companyId);
                KwlReturnObject dresultFuture = accProductObj.getAssetDepreciationDetail(futureFilters);
                if (dresultFuture.getEntityList().size()>0) {
                    msg = messageSource.getMessage("acc.msg.depreciationalreadypostedaftertransdate", null, RequestContextUtils.getLocale(request)); //"Depreciation is already posted after the transfer Date. The transfer date can only be later than the final depreciation posted for the selected Asset. ";
                    issuccess = false;
                    validTransfer = false;
                    break;
                }
                
                boolean isAllDepreciated = true;
                int periodDep = -1;
                Calendar calInstallation = Calendar.getInstance();
                calInstallation.setTime(assetDetails.getInstallationDate());
                int creationyearDep = calInstallation.get(Calendar.YEAR);
                int startMonth = calInstallation.get(Calendar.MONTH);
                long diffinlong = cal1.getTimeInMillis() - calInstallation.getTimeInMillis();
                long enddateinlong = calInstallation.getTimeInMillis() + diffinlong;
                Date endDate = new Date(enddateinlong);
                Calendar calEndMonth = Calendar.getInstance();
                calEndMonth.setTime(endDate);
                int endMonth = calEndMonth.get(Calendar.MONTH) + 1;
                if (depreciationCalculationType == 0) {
                    startMonth = calInstallation.get(Calendar.YEAR);
                    endMonth = calEndMonth.get(Calendar.YEAR) + 1;
                }
                for (int j = startMonth; j < endMonth; j++) {
                    int yeardiffDep = year - creationyearDep;
                    if (depreciationCalculationType == 0) {
                        if (yeardiffDep < 0) {  //if selected year is less than the cretion year then there will be no depreciation to show
                            continue;
                        }
                        periodDep = yeardiffDep + 1;
                    } else {
                        periodDep = (12 * yeardiffDep) + j + 1;
                        periodDep = calInstallation.get(Calendar.MONTH) != 0 ? periodDep - calInstallation.get(Calendar.MONTH) : periodDep;
                    }
                    
                    HashMap<String, Object> filters = new HashMap<String, Object>();
                    filters.put("period", periodDep);
                    filters.put("assetDetailsId", assetDetails.getId());
                    filters.put(Constants.companyKey, companyId);
                    KwlReturnObject dresult = accProductObj.getAssetDepreciationDetail(filters);
                    if (dresult.getEntityList().isEmpty()) {
                        isAllDepreciated = false;
                        break;
                    }
                }
                if(!isAllDepreciated){
                    msg = messageSource.getMessage("acc.msg.depreciationnotposteduptotransdate", null, RequestContextUtils.getLocale(request)); // "Depreciation is not posted upto the transfer Date. Please post remaining depreciation from the depreciation screen.";
                    issuccess = false;
                    validTransfer = false;
                    break;
                }
                
                String assetId = StringUtil.DecodeText(jobj.optString("assetId"));
                String assetdescription = StringUtil.DecodeText(jobj.optString("assetdescription"));
                String location = jobj.getString("location");
                String warehouse = jobj.getString("warehouse");
                String department = jobj.getString("department");
                String assetUser = jobj.getString("assetUser");
                String batchDetails = jobj.optString("batchdetails");

                double cost = jobj.optDouble("cost");
                double salvageRate = jobj.optDouble("salvageRate", 0);
                double salvageValue = jobj.optDouble("salvageValue", 0);
                double accumulatedDepreciation = jobj.optDouble("accumulatedDepreciation", 0);
                double assetLife = jobj.optDouble("assetLife", 0);
                double elapsedLife = jobj.optDouble("elapsedLife", 0);
                double nominalValue = jobj.optDouble("nominalValue", 0);
                double sellAmount = jobj.optDouble("sellAmount", 0);

                HashMap<String, Object> dataMap = new HashMap<String, Object>();

                dataMap.put("assetDetailId", assetDetailId);
                dataMap.put("assetId", assetId);
                dataMap.put("assetdescription", assetdescription);
                dataMap.put("department", department);
                dataMap.put("assetUser", assetUser);
                dataMap.put("cost", cost);
                dataMap.put("salvageRate", salvageRate);
                dataMap.put("salvageValue", salvageValue);
                dataMap.put("accumulatedDepreciation", accumulatedDepreciation);
                dataMap.put("assetLife", assetLife);
                dataMap.put("elapsedLife", elapsedLife);
                dataMap.put("nominalValue", nominalValue);
                dataMap.put("sellAmount", sellAmount);
                dataMap.put("productId", jobj.getString("assetGroupId"));
                dataMap.put("companyId", companyId);
                dataMap.put("invrecord", true);
                
                KwlReturnObject result = accProductObj.updateAssetDetails(dataMap);
                AssetDetails row = (AssetDetails) result.getEntityList().get(0);
                if (jobj.has(Constants.customfield)) {
                    String customfield = jobj.getString(Constants.customfield);
                    if (!StringUtil.isNullOrEmpty(customfield)) {
                        HashMap<String, Object> DOMap = new HashMap<String, Object>();
                        JSONArray jcustomarray = new JSONArray(customfield);
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "AssetDetails");
                        customrequestParams.put("moduleprimarykey", "AssetDetailsId");
                        customrequestParams.put("modulerecid", row.getId());
                        customrequestParams.put("moduleid", Constants.Acc_FixedAssets_Details_ModuleId);
                        customrequestParams.put(Constants.companyKey, companyId);
                        DOMap.put(Constants.Acc_id, row.getId());
                        customrequestParams.put("customdataclasspath", Constants.Acc_FixedAsset_Details_Custom_Data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            DOMap.put("accassetdetailscustomdata", row.getId());
                            accProductObj.updateAssetDetails(DOMap);
                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(batchDetails) && !batchDetails.equalsIgnoreCase("null")) {
                    String assetMainId = row.getId();
                    dataMap.put("assetDetailId", assetMainId);
                    JSONArray jArr = new JSONArray(batchDetails);
                    for (int k = 0; k < jArr.length(); k++) {
                        JSONObject jSONObject = new JSONObject(jArr.get(k).toString());
                        if (jSONObject.has("location")) {
                            jSONObject.put("location",location);
                        }
                        if (jSONObject.has("warehouse")) {
                            jSONObject.put("warehouse",warehouse);
                        }
                        jArr.put(k,jSONObject);
                    }
                    saveAssetNewBatch(jArr.toString(), jobj.getString("assetGroupId"), request, assetMainId);
                }
            }
            
            // If Transfer is Valid then maintain History
            if (validTransfer) {
                String fixedAssetDetail = request.getParameter("oldAssetDetails");
                JSONArray jArr = new JSONArray(fixedAssetDetail);

                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    String assetId = StringUtil.DecodeText(jobj.optString("assetId"));
                    String assetdescription = StringUtil.DecodeText(jobj.optString("assetdescription"));
                    String location = jobj.optString("location","");
                    String department = jobj.optString("department","");
                    String assetUser = jobj.optString("assetUser","");
                    String batchDetails = jobj.optString("batchdetails","");

                    double cost = jobj.optDouble("cost");
                    double salvageRate = jobj.optDouble("salvageRate", 0);
                    double salvageValue = jobj.optDouble("salvageValue", 0);
                    double accumulatedDepreciation = jobj.optDouble("accumulatedDepreciation", 0);
                    double assetLife = jobj.optDouble("assetLife", 0);
                    double elapsedLife = jobj.optDouble("elapsedLife", 0);
                    double nominalValue = jobj.optDouble("nominalValue", 0);
                    double sellAmount = jobj.optDouble("sellAmount", 0);

                    String installationDateStr = jobj.getString("installationDate");

                    Date installationDate = df.parse(installationDateStr);

                    String purchaseDateStr = jobj.getString("purchaseDate");
                    Date purchaseDate = df.parse(purchaseDateStr);

                    HashMap<String, Object> dataMap = new HashMap<String, Object>();

                    dataMap.put("assetId", assetId);
                    dataMap.put("assetdescription", assetdescription);
                    if(!StringUtil.isNullOrEmpty(location)){
                        dataMap.put("location", location);
                    }
                    dataMap.put("department", department);
                    dataMap.put("assetUser", assetUser);
                    dataMap.put("cost", cost);
                    dataMap.put("salvageRate", salvageRate);
                    dataMap.put("salvageValue", salvageValue);
                    dataMap.put("accumulatedDepreciation", accumulatedDepreciation);
                    dataMap.put("assetLife", assetLife);
                    dataMap.put("elapsedLife", elapsedLife);
                    dataMap.put("nominalValue", nominalValue);
                    dataMap.put("sellAmount", sellAmount);
                    dataMap.put("productId", jobj.getString("assetGroupId"));
                    dataMap.put("installationDate", installationDate);
                    dataMap.put("purchaseDate", purchaseDate);
                    dataMap.put("companyId", companyId);
                    dataMap.put("invrecord", true);
                    dataMap.put("isTransferHistory", true);
                    dataMap.put("transferDate", transferDate);

                    KwlReturnObject result = accProductObj.saveAssetDetails(dataMap);
                    AssetDetails row = (AssetDetails) result.getEntityList().get(0);
                    if (jobj.has("customfield")) {
                        String customfield = jobj.getString("customfield");
                        if (!StringUtil.isNullOrEmpty(customfield)) {
                            HashMap<String, Object> DOMap = new HashMap<String, Object>();
                            JSONArray jcustomarray = new JSONArray(customfield);
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", "AssetDetails");
                            customrequestParams.put("moduleprimarykey", "AssetDetailsId");
                            customrequestParams.put("modulerecid", row.getId());
                            customrequestParams.put("moduleid", Constants.Acc_FixedAssets_Details_ModuleId);
                            customrequestParams.put("companyid", companyId);
                            DOMap.put("id", row.getId());
                            customrequestParams.put("customdataclasspath", Constants.Acc_FixedAsset_Details_Custom_Data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                DOMap.put("accassetdetailscustomdata", row.getId());
                                accProductObj.updateAssetDetails(DOMap);
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(batchDetails) && !batchDetails.equalsIgnoreCase("null")) {
                        String assetMainId = row.getId();
                        dataMap.put("assetDetailId", assetMainId);
                        JSONArray jArray = new JSONArray(batchDetails);
                        for (int k = 0; k < jArray.length(); k++) {
                            JSONObject jSONObject = new JSONObject(jArray.get(k).toString());
                            if (jSONObject.has("serialnoid")) {
                                //For asset's history - Generate new entry in NewBatchSerial if serial is enabled for the asset
                                jSONObject.put("serialnoid","");
                            }                            
                            jArray.put(k,jSONObject);
                        }
                        saveAssetNewBatch(jArray.toString(), jobj.getString("assetGroupId"), request, assetMainId);
                    }
                    issuccess = true;
                }
            }
            if(issuccess){ 
                msg = messageSource.getMessage("acc.transfersucc", null, RequestContextUtils.getLocale(request));
                // Maintain the aduit Trial
                auditTrailObj.insertAuditLog(AuditAction.PRODUCT_TYPE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has added Split Opening Balance of Account by adding Asset Opening Document ", request, "0");        
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj1.put(Constants.RES_success, issuccess);
                jobj1.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj1.toString());
    }
    
    
    public ModelAndView getAssemblyProductsWithBOM(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            //Quick Search on Fetch button
            if (!StringUtil.isNullOrEmpty(request.getParameter("search"))) {
                requestJobj.put("search", request.getParameter("search"));
            }
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("billid"))) {
                requestJobj.put("buildid", request.getParameter("billid"));
            }

            jobj = accProductModuleService.getAssembyProductBOMDetails(requestJobj);

            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /*
     * Method to export assembly products with bom details.
     */
    public ModelAndView exportAssemblyProductsWithBOM(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            //Quick Search on Fetch button
            if (!StringUtil.isNullOrEmpty(request.getParameter("search"))) {
                requestJobj.put("search", request.getParameter("search"));
            }
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("billid"))) {
                requestJobj.put("buildid", request.getParameter("billid"));
            }

            requestJobj.put("exportfalg",true);
            jobj = accProductModuleService.getAssembyProductBOMDetails(requestJobj);

            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
   
}
