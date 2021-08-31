/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.User;
import com.krawler.common.admin.UserPreferences;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.permissionHandler.permissionHandler;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.spring.accounting.creditnote.accCreditNoteService;
import com.krawler.spring.accounting.debitnote.accDebitNoteService;
import com.krawler.spring.authHandler.authHandler;

import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccDashboardServiceImpl implements AccDashboardService{

    private accCustomerDAO accCustomerDAOobj;
    private accVendorDAO accVendorDAOobj;
    private accProductDAO accProductObj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private sessionHandlerImpl sessionHandlerImplObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private MessageSource messageSource;
    private accGoodsReceiptDAO accGoodsReceiptDAOObj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private companyDetailsDAO companyDetailsDAOObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private APICallHandlerService apiCallHandlerService;
    private AccountingDashboardDAO accountingDashboardDAOObj; 
    private accDebitNoteService accDebitNoteService;
    private accCreditNoteService accCreditNoteService;
    
    public void setaccCreditNoteService(accCreditNoteService accCreditNoteService) {
        this.accCreditNoteService = accCreditNoteService;
    }
    public void setaccDebitNoteService(accDebitNoteService accDebitNoteService) {
        this.accDebitNoteService = accDebitNoteService;
    }
    public void setAccountingDashboardDAOObj(AccountingDashboardDAO accountingDashboardDAOObj) {
        this.accountingDashboardDAOObj = accountingDashboardDAOObj;
    }

    @Override
    public void setMessageSource(MessageSource msg) {
		this.messageSource = msg;
    }
    
    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj1) {
        this.companyDetailsDAOObj = companyDetailsDAOObj1;
    }
    
    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }
    public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }
    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptDAOObj) {
        this.accGoodsReceiptDAOObj = accGoodsReceiptDAOObj;
    }
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }
    

    public String getDashboardData(HttpServletRequest request) throws ServiceException, SessionExpiredException {
		StringBuilder data=new StringBuilder();
        try {
            String userid = sessionHandlerImplObj.getUserid(request);
            String companyid = sessionHandlerImplObj.getCompanyid(request);

//            data.append("<div id=\"DashboardContent\" class=\"dashboardcontent\">");
            data.append("<div id=\"DashboardContent\" class=\"mrpdashboardcontent\">");
//            String userid = AuthHandler.getUserid(request);
//            User user = (User) session.get(User.class, AuthHandler.getUserid(request));
            KwlReturnObject uresult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
            User user = (User) uresult.getEntityList().get(0);

//            JSONObject perms = PermissionHandler.getPermissions(session, user.getUserID());
            JSONObject perms = new JSONObject();

            if (!permissionHandlerDAOObj.isSuperAdmin(userid, companyid)) {
                KwlReturnObject kmsg = permissionHandlerDAOObj.getActivityFeature();
                perms = permissionHandler.getAllPermissionJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("userid", userid);

                kmsg = permissionHandlerDAOObj.getUserPermission(requestParams);
                perms = permissionHandler.getRolePermissionJson(kmsg.getEntityList(), perms);
            } else {
                perms.put("deskeraadmin", true);
            }


            if (AccountingManager.isCompanyAdmin(user)) {
//                getCompanyAdminDashboardData(session, request, data, perms);
                getCompanyAdminDashboardData(request, data, perms);
            } else {
//                getUserDashboardData(session, request, data, perms);
                getUserDashboardData(request, data, perms);
            }
            data.append("</div>");
        } catch (Exception ex) {
            throw ServiceException.FAILURE(""+ex.getMessage(), ex);
        }
        return data.toString();
    }
     
    @Override
    public String getDashboardUpdates(HttpServletRequest request,HttpServletResponse response) throws ServiceException{
        StringBuilder data=new StringBuilder();
        try {
            String userid = sessionHandlerImplObj.getUserid(request);
            String companyid = sessionHandlerImplObj.getCompanyid(request);
            KwlReturnObject uresult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
            User user = (User) uresult.getEntityList().get(0);
            JSONObject perms = new JSONObject();

            if (!permissionHandlerDAOObj.isSuperAdmin(userid, companyid)) {
                KwlReturnObject kmsg = permissionHandlerDAOObj.getActivityFeature();
                perms = permissionHandler.getAllPermissionJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("userid", userid);

                kmsg = permissionHandlerDAOObj.getUserPermission(requestParams);
                perms = permissionHandler.getRolePermissionJson(kmsg.getEntityList(), perms);
            } else {
                perms.put("deskeraadmin", true);
            }


            if (AccountingManager.isCompanyAdmin(user)) {  
                 StringBuilder temp=(getUserDashboardUpdateList(request, perms));
                 data.append(createLeftPaneSepratly(messageSource.getMessage("acc.dashboard.updates", null, RequestContextUtils.getLocale(request)), temp, userid));
                
            } else {  
                 StringBuilder temp=(getUserDashboardUpdateList(request, perms));
                 data.append(createLeftPaneSepratly(messageSource.getMessage("acc.dashboard.updates", null, RequestContextUtils.getLocale(request)), temp, userid));
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(""+ex.getMessage(), ex);
        }
        return data.toString();
    }
    
    public String getDashboardUpdateDataIphone(HttpServletRequest request) throws ServiceException, SessionExpiredException {
		StringBuilder data=new StringBuilder();
                JSONArray jSONArray=new JSONArray();
        try {
            String userid = sessionHandlerImplObj.getUserid(request);
            String companyid = sessionHandlerImplObj.getCompanyid(request);
            KwlReturnObject uresult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
            User user = (User) uresult.getEntityList().get(0);

            JSONObject perms = new JSONObject();

            if (!permissionHandlerDAOObj.isSuperAdmin(userid, companyid)) {
                KwlReturnObject kmsg = permissionHandlerDAOObj.getActivityFeature();
                perms = permissionHandler.getAllPermissionJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("userid", userid);

                kmsg = permissionHandlerDAOObj.getUserPermission(requestParams);
                perms = permissionHandler.getRolePermissionJson(kmsg.getEntityList(), perms);
            } else {
                perms.put("deskeraadmin", true);
            }
            request.setAttribute("iphoneUpdate",true);
            if (AccountingManager.isCompanyAdmin(user)) {
                jSONArray=getCompanyAdminDashboardUpdateDataIphone(request, data, perms);
            } else {
                jSONArray=getUserDashboardUpdateDataIphone(request, data, perms);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(""+ex.getMessage(), ex);
        }
        return jSONArray.toString();
    }

    private void getUserDashboardData(HttpServletRequest request, StringBuilder data, JSONObject perms) throws ServiceException, SessionExpiredException, ParseException {
//        StringBuilder temp = getUserDashboardUpdateList(session, request, perms);
        StringBuilder temp = getCompanyAdminApprovals(request, perms);
        temp.append(getUserDashboardUpdateList(request, perms));
        String userid = sessionHandlerImplObj.getUserid(request);
//        CompanyAccountPreferences pref = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

        KwlReturnObject compaccresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) compaccresult.getEntityList().get(0);

        StringBuilder temp2;
        if (pref != null) {
            if (!pref.isWithoutInventory()) {
                if (extraCompanyPreferences.isActivateMRPModule()) {
                    temp2 = getDashBoardDataFlowWithMRP(request, perms);
                } else {
                    temp2 = getDashBoardDataFlow(request, perms);
                }
            } else {
//                temp2 = getDashBoardDataFlowWithoutInv(session, request, perms);
                temp2 = getDashBoardDataFlowWithoutInv(request, perms);
            }
        } else {
//            temp2 = getDashBoardDataFlow(session, request, perms);
            temp2 = getDashBoardDataFlow(request, perms);
        }
        if (temp.length() > 0) {
            data.append(createLeftPane(messageSource.getMessage("acc.dashboard.updates", null, RequestContextUtils.getLocale(request)), temp, temp2, userid));
        } else {
            data.append(createLeftPane(messageSource.getMessage("acc.rem.93", null, RequestContextUtils.getLocale(request)), getSetupWizard(request, sessionHandlerImpl.getCompanyid(request)), temp2, userid));
        }
    }
    private JSONArray getUserDashboardUpdateDataIphone(HttpServletRequest request, StringBuilder data, JSONObject perms) throws ServiceException, SessionExpiredException, ParseException {
//        StringBuilder temp = getCompanyAdminApprovals(request, perms);
//        temp.append(getUserDashboardUpdateList(request, perms));
//        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
//        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
//        StringBuilder temp2;
//        if (pref != null) {
//            if (!pref.isWithoutInventory()) {
//                temp2 = getDashBoardDataFlow(request, perms);
//            } else {
//                temp2 = getDashBoardDataFlowWithoutInv(request, perms);
//            }
//        } else {
//            temp2 = getDashBoardDataFlow(request, perms);
//        }
//        if (temp.length() > 0) {
//            data.append(createLeftPane(messageSource.getMessage("acc.dashboard.updates", null, RequestContextUtils.getLocale(request)), temp, temp2));
//        } else {
//            data.append(createLeftPane(messageSource.getMessage("acc.rem.93", null, RequestContextUtils.getLocale(request)), getSetupWizard(request, sessionHandlerImpl.getCompanyid(request)), temp2));
//        }
        
        
        JSONArray jSONArray=new JSONArray();
         getCompanyAdminApprovalsIphone(request, perms,jSONArray);
         getCompanyAdminDashboardUpdateIphoneList(request, perms,jSONArray);
         return jSONArray;
    }

     private void getCompanyAdminDashboardData(HttpServletRequest request, StringBuilder data, JSONObject perms) throws ServiceException, SessionExpiredException {
//        StringBuilder temp = getCompanyAdminDashboardUpdateList(session, request, perms);
//        StringBuilder temp = getCompanyAdminDashboardUpdateList(request, perms);
        StringBuilder temp = getCompanyAdminApprovals(request, perms);
//        temp.append(getCompanyAdminDashboardUpdateList(request, perms));  //we are loading Update list after loading the Dashboard    
//        CompanyAccountPreferences pref = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
        String companyID = sessionHandlerImplObj.getCompanyid(request);
        String userid = sessionHandlerImplObj.getUserid(request);

        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
        
        KwlReturnObject compaccresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) compaccresult.getEntityList().get(0);

        StringBuilder temp2;
         if (pref != null) {
             if (!pref.isWithoutInventory()) {
                 if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateMRPModule()) {
                     temp2 = getDashBoardDataFlowWithMRP(request, perms);
                 } else {
                     temp2 = getDashBoardDataFlow(request, perms);
                 }
             } else {
//                temp2 = getDashBoardDataFlowWithoutInv(session, request, perms);
                 temp2 = getDashBoardDataFlowWithoutInv(request, perms);
             }
         } else {
//            temp2 = getDashBoardDataFlow(session, request, perms);
             temp2 = getDashBoardDataFlow(request, perms);
         }
        if (temp.length() > 0) {
            data.append(createLeftPane(messageSource.getMessage("acc.dashboard.updates", null, RequestContextUtils.getLocale(request)), temp, temp2, userid));
        } else {
            data.append(createLeftPane(messageSource.getMessage("acc.rem.93", null, RequestContextUtils.getLocale(request)), getSetupWizard(request, companyID), temp2, userid));
        }
    }
     private JSONArray getCompanyAdminDashboardUpdateDataIphone(HttpServletRequest request, StringBuilder data, JSONObject perms) throws ServiceException, SessionExpiredException, ParseException {
         JSONArray jSONArray=new JSONArray();
         getCompanyAdminApprovalsIphone(request, perms,jSONArray);
         getCompanyAdminDashboardUpdateIphoneList(request, perms,jSONArray);
         return jSONArray;

    }

    private StringBuilder getCompanyAdminDashboardUpdateList(HttpServletRequest request, JSONObject perms) throws ServiceException, ParseException {
        StringBuilder finalStr=new StringBuilder();
        try {
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            String companyID = sessionHandlerImplObj.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

//            finalStr.append(joinArrayList(getVendorsUpdationInfo(session, companyID, perms,true), ""));
            finalStr.append(joinArrayList(getVendorsUpdationInfo(companyID, perms,true), ""));
//            finalStr.append(joinArrayList(getCustomersUpdationInfo(session, companyID, perms,true), ""));
            finalStr.append(joinArrayList(getCustomersUpdationInfo(companyID, perms,true), ""));
            if(pref!=null){
              if(!pref.isWithoutInventory())
//                finalStr.append(joinArrayList(getProductsBelowROLInfo(session, companyID, perms,true), ""));
                finalStr.append(joinArrayList(getProductsBelowROLInfo(request, companyID, perms,true), ""));
            }else
//                finalStr.append(joinArrayList(getProductsBelowROLInfo(session, companyID, perms,true), ""));
                finalStr.append(joinArrayList(getProductsBelowROLInfo(request, companyID, perms,true), ""));
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }
    private void getCompanyAdminDashboardUpdateIphoneList(HttpServletRequest request, JSONObject perms,JSONArray jSONArray) throws ServiceException, ParseException {
        try {
            String companyID = sessionHandlerImplObj.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            getVendorsUpdationInfoIphone(companyID, perms,true,jSONArray);
            getCustomersUpdationInfoIphone(companyID, perms,true,jSONArray);
            if((pref!=null&&!pref.isWithoutInventory())||pref==null){
                getProductsBelowROLInfoIphone(request, companyID, perms,true,jSONArray);
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    private StringBuilder getCompanyAdminApprovals(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalStr=new StringBuilder();
        try {
            String companyID = sessionHandlerImplObj.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

            finalStr.append(joinArrayList(getApprovalInfo(companyID, perms,true), ""));
            
            
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }
    private StringBuilder getCompanyAdminApprovalsIphone(HttpServletRequest request, JSONObject perms,JSONArray jSONArray) throws ServiceException {
        StringBuilder finalStr=new StringBuilder();
        try {
            String companyID = sessionHandlerImplObj.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

            getApprovalInfoIphone(companyID, perms,true,jSONArray);
            
            
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }
        
    private StringBuilder getUserDashboardUpdateList(HttpServletRequest request, JSONObject perms) throws ServiceException, ParseException {
        StringBuilder finalStr = new StringBuilder();
        try {
//            CompanyAccountPreferences pref = (CompanyAccountPreferences) session.get(CompanyAccountPreferences.class, AuthHandler.getCompanyid(request));
            String companyID = sessionHandlerImplObj.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
//            finalStr.append(joinArrayList(getVendorsUpdationInfo(session, companyID, perms, true), ""));
            cpresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) cpresult.getEntityList().get(0);
            if(extraCompanyPreferences!=null && extraCompanyPreferences.isShowVendorUpdateFlag()){
                finalStr.append(joinArrayList(getVendorsUpdationInfo(companyID, perms, true), ""));
            }
//            finalStr.append(joinArrayList(getCustomersUpdationInfo(session, companyID, perms, true), ""));
            if(extraCompanyPreferences!=null && extraCompanyPreferences.isShowCustomerUpdateFlag()){
                finalStr.append(joinArrayList(getCustomersUpdationInfo(companyID, perms, true), ""));
            }
            if(extraCompanyPreferences!=null && extraCompanyPreferences.isShowProductUpdateFlag()){
                if (pref != null) {
                    if (!pref.isWithoutInventory()) {
    //                    finalStr.append(joinArrayList(getProductsBelowROLInfo(session, companyID, perms, true), ""));
                        finalStr.append(joinArrayList(getProductsBelowROLInfo(request, companyID, perms, true), ""));
                    }
                } else {
    //                finalStr.append(joinArrayList(getProductsBelowROLInfo(session, companyID, perms, true), ""));
                    finalStr.append(joinArrayList(getProductsBelowROLInfo(request, companyID, perms, true), ""));
                }
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }

    public ArrayList getProductsBelowROLInfo(HttpServletRequest request, String companyID, JSONObject perms,Boolean isDashboard) throws ServiceException, ParseException {
        ArrayList jArray=new ArrayList();
        try {
//            JSONArray jArr=CompanyHandler.getProducts(session,null,companyID).getJSONArray("data");
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);            
            KwlReturnObject result = accProductObj.getSuggestedReorderProducts(requestParams);
            JSONArray jArr =  null;
            if (requestParams.get(Constants.isFromDashBoard) != null) {
                if ((boolean) requestParams.get(Constants.isFromDashBoard)) {
                    jArr = productHandler.getProductsJsonForDashBoradUpdates(requestParams, result.getEntityList(), accProductObj, null, accountingHandlerDAOobj, accCurrencyDAOobj, false);
                } else {
                    jArr = productHandler.getProductsJson(requestParams, result.getEntityList(), accProductObj, null, accountingHandlerDAOobj, accCurrencyDAOobj, false);
                }
            }
            String link;
            String productID;
            if(jArr!=null) {
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject obj = jArr.getJSONObject(i);
                    link = obj.getString("productname");
                    productID = obj.getString("productid");
                    if (permissionHandler.isPermitted(perms, "product", "view")) {
                        link = getLink(link, "callProductDetails(\"" + productID + "\")");
                    }
                    if (obj.getInt("quantity") == obj.getInt("reorderlevel")) {
                        jArray.add(getFormatedAlert("The Product " + link + "'s stock is equal to reorder level (Available quantity:" + obj.getInt("quantity") + " " + obj.getString("uomname") + ")", "accountingbase updatemsg-product", isDashboard));
                    } else {
                    jArray.add(getFormatedAlert("The Product "+link+" is below reorder level (Available quantity:"+obj.getInt("quantity")+" "+obj.getString("uomname")+")","accountingbase updatemsg-product",isDashboard));
                    }
                }
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jArray;
    }
    public void getProductsBelowROLInfoIphone(HttpServletRequest request, String companyID, JSONObject perms,Boolean isDashboard ,JSONArray jSONArray) throws ServiceException, ParseException {
        JSONObject jSONObject=new JSONObject();
        JSONArray jSONArrayTemp=new JSONArray();
        try {
//            JSONArray jArr=CompanyHandler.getProducts(session,null,companyID).getJSONArray("data");
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            KwlReturnObject result = accProductObj.getSuggestedReorderProducts(requestParams);
            JSONArray jArr = productHandler.getProductsJson(requestParams, result.getEntityList(), accProductObj , null , accountingHandlerDAOobj,accCurrencyDAOobj,false);
            
            String link;
            String productID;
            for(int i=0;i<jArr.length();i++){
                JSONObject obj = jArr.getJSONObject(i);
                link=obj.getString("productname");
                productID=obj.getString("productid");
//                if(permissionHandler.isPermitted(perms, "product", "view"))
//                    link=getLink(link, "callProductDetails(\""+productID+"\")");
                if(obj.getInt("quantity")==obj.getInt("reorderlevel")){
                    jSONArrayTemp.put("The Product "+link+"'s stock is equal to reorder level (Available quantity:"+obj.getInt("quantity")+" "+obj.getString("uomname")+")");
                } else {
                    jSONArrayTemp.put("The Product "+link+" is below reorder level (Available quantity:"+obj.getInt("quantity")+" "+obj.getString("uomname")+")");
                }
            }
            jSONObject.put("Product",jSONArrayTemp);
            jSONArray.put(jSONObject);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public ArrayList getVendorsUpdationInfo(String companyID, JSONObject perms, Boolean isDashboard) throws ServiceException {
        ArrayList jArray=new ArrayList();
        try {
//            String query="from Vendor where company.companyID=? and modifiedOn is null order by createdOn";
//            List list = HibernateUtil.executeQueryPaging(session, query, new Object[]{companyID}, new Integer[]{0,2});
            KwlReturnObject result = accVendorDAOobj.getVendor_Dashboard(companyID, true, "createdOn", 0, 2);
            List list = result.getEntityList();
            Iterator itr=list.iterator();
            String link;
            String vendorID="";
            while(itr.hasNext()){
                Vendor vendor=(Vendor)itr.next();
                link=vendor.getName();
                vendorID=vendor.getID();
                if(permissionHandler.isPermitted(perms, "vendor", "view"))
                    link=getLink(link, "callVendorDetails(\""+vendorID+"\")");
                jArray.add(getFormatedAlert("New vendor "+link+" created","accountingbase updatemsg-vendor",isDashboard));
            }
//            query="from Vendor where company.companyID=? and modifiedOn is not null order by modifiedOn";
//            list = HibernateUtil.executeQueryPaging(session, query, new Object[]{companyID}, new Integer[]{0,2});
            result = accVendorDAOobj.getVendor_Dashboard(companyID, false, "modifiedOn", 0, 2);
            list = result.getEntityList();
            itr=list.iterator();
            while(itr.hasNext()){
                Vendor vendor=(Vendor)itr.next();
                link=vendor.getName();
                vendorID=vendor.getID();
                if(permissionHandler.isPermitted(perms, "vendor", "view"))
                    link=getLink(link, "callVendorDetails(\""+vendorID+"\")");
                jArray.add(getFormatedAlert("Vendor "+link+" modified","accountingbase updatemsg-vendor",isDashboard));
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jArray;
    }
    public void getVendorsUpdationInfoIphone(String companyID, JSONObject perms, Boolean isDashboard,JSONArray jSONArray) throws ServiceException {
        JSONObject jSONObject=new JSONObject();
        JSONArray jSONArrayTemp=new JSONArray();
        try {
            KwlReturnObject result = accVendorDAOobj.getVendor_Dashboard(companyID, true, "createdOn", 0, 2);
            List list = result.getEntityList();
            Iterator itr=list.iterator();
            String link;
            String vendorID="";
            while(itr.hasNext()){
                Vendor vendor=(Vendor)itr.next();
                link=vendor.getName();
                vendorID=vendor.getID();
//                if(permissionHandler.isPermitted(perms, "vendor", "view"))
//                    link=getLink(link, "callVendorDetails(\""+vendorID+"\")");
                jSONArrayTemp.put("New vendor "+link+" created");
            }
            result = accVendorDAOobj.getVendor_Dashboard(companyID, false, "modifiedOn", 0, 2);
            list = result.getEntityList();
            itr=list.iterator();
            while(itr.hasNext()){
                Vendor vendor=(Vendor)itr.next();
                link=vendor.getName();
                vendorID=vendor.getID();
//                if(permissionHandler.isPermitted(perms, "vendor", "view"))
//                    link=getLink(link, "callVendorDetails(\""+vendorID+"\")");
                jSONArrayTemp.put("Vendor "+link+" modified");
            }
            jSONObject.put("vendor",jSONArrayTemp);
            jSONArray.put(jSONObject);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }
    
    public ArrayList getApprovalInfo(String companyID, JSONObject perms, Boolean isDashboard) throws ServiceException {
        try {
            ArrayList jArray=new ArrayList();
                
            /*Orders*/
            if((permissionHandler.isPermitted(perms, "approvals", "purchaseorderapprovelevelone")) || (permissionHandler.isPermitted(perms, "approvals", "purchaseorderapproveleveltwo"))){
                    int pendingCustInv = accPurchaseOrderobj.pendingApprovalOrdersCount(companyID);
                    if(pendingCustInv > 0){
                        String functionCall = "pendingOrdersTab(false)";
                        String link=getLink(pendingCustInv+" Purchase Order(s)", functionCall);
                        jArray.add(getFormatedAlert("You have "+link+" pending for approval.","accountingbase updatemsg-vendor",isDashboard));
                    }
                    
            }    
                
            if((permissionHandler.isPermitted(perms, "approvals", "salesorderapprovelevelone")) || (permissionHandler.isPermitted(perms, "approvals", "salesorderapproveleveltwo"))){
                    int pendingCustInv = accSalesOrderDAOobj.pendingApprovalOrdersCount(companyID);
                    if(pendingCustInv > 0){
                        String functionCall = "pendingOrdersTab(true)";
                        String link=getLink(pendingCustInv+" Sales Order(s)", functionCall);
                        jArray.add(getFormatedAlert("You have "+link+" pending for approval.","accountingbase updatemsg-vendor",isDashboard));
                    }
                    
            }
            
            /*Invoices*/
            if((permissionHandler.isPermitted(perms, "approvals", "vendorinvoiceapprovelevelone")) || (permissionHandler.isPermitted(perms, "approvals", "vendorinvoiceapproveleveltwo"))){
                    int pendingCustInv = accGoodsReceiptDAOObj.pendingApprovalInvoicesCount(companyID);
                    if(pendingCustInv > 0){
                        String functionCall = "pendingCustomerInvpicesTab(false)";
                        String link=getLink(pendingCustInv+" Purchase Invoice(s)", functionCall);
                        jArray.add(getFormatedAlert("You have "+link+" pending for approval.","accountingbase updatemsg-vendor",isDashboard));
                    }
                    
            }    
                
            if((permissionHandler.isPermitted(perms, "approvals", "customerinvoiceapprovelevelone")) || (permissionHandler.isPermitted(perms, "approvals", "customerinvoiceapproveleveltwo"))){
                    int pendingCustInv = accInvoiceDAOobj.pendingApprovalInvoicesCount(companyID);
                    if(pendingCustInv > 0){
                        String functionCall = "pendingCustomerInvpicesTab(true)";
                        String link=getLink(pendingCustInv+" Sales Invoice(s)", functionCall);
                        jArray.add(getFormatedAlert("You have "+link+" pending for approval.","accountingbase updatemsg-vendor",isDashboard));
                    }
                    
            }
            
            return jArray;
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    public void getApprovalInfoIphone(String companyID, JSONObject perms, Boolean isDashboard,JSONArray jSONArray) throws ServiceException {
        JSONObject jSONObject=new JSONObject();
        JSONArray jSONArrayTemp=new JSONArray();
        try {
           
                
            /*Orders*/
            if((permissionHandler.isPermitted(perms, "approvals", "purchaseorderapprovelevelone")) || (permissionHandler.isPermitted(perms, "approvals", "purchaseorderapproveleveltwo"))){
                    int pendingCustInv = accPurchaseOrderobj.pendingApprovalOrdersCount(companyID);
                    if(pendingCustInv > 0){
                        String functionCall = "pendingOrdersTab(false)";
//                        String link=getLink(pendingCustInv+" Purchase Order(s)", functionCall);
                        jSONArrayTemp.put("You have "+pendingCustInv+" Purchase Order(s) pending for approval.");
                    }
                    
            }    
                
            if((permissionHandler.isPermitted(perms, "approvals", "salesorderapprovelevelone")) || (permissionHandler.isPermitted(perms, "approvals", "salesorderapproveleveltwo"))){
                    int pendingCustInv = accSalesOrderDAOobj.pendingApprovalOrdersCount(companyID);
                    if(pendingCustInv > 0){
                        String functionCall = "pendingOrdersTab(true)";
//                        String link=getLink(pendingCustInv+" Sales Order(s)", functionCall);
                        jSONArrayTemp.put("You have "+pendingCustInv+" Sales Order(s) pending for approval.");
                    }
                    
            }
            
            /*Invoices*/
            if((permissionHandler.isPermitted(perms, "approvals", "vendorinvoiceapprovelevelone")) || (permissionHandler.isPermitted(perms, "approvals", "vendorinvoiceapproveleveltwo"))){
                    int pendingCustInv = accGoodsReceiptDAOObj.pendingApprovalInvoicesCount(companyID);
                    if(pendingCustInv > 0){
                        String functionCall = "pendingCustomerInvpicesTab(false)";
//                        String link=getLink(pendingCustInv+" Vendor Invoice(s)", functionCall);
                        jSONArrayTemp.put("You have "+pendingCustInv+" Purchase Invoice(s) pending for approval.");
                    }
                    
            }    
                
            if((permissionHandler.isPermitted(perms, "approvals", "customerinvoiceapprovelevelone")) || (permissionHandler.isPermitted(perms, "approvals", "customerinvoiceapproveleveltwo"))){
                    int pendingCustInv = accInvoiceDAOobj.pendingApprovalInvoicesCount(companyID);
                    if(pendingCustInv > 0){
                        String functionCall = "pendingCustomerInvpicesTab(true)";
//                        String link=getLink(pendingCustInv+" Customer Invoice(s)", functionCall);
                        jSONArrayTemp.put("You have "+pendingCustInv+" Sales Invoice(s) pending for approval.");
                    }
                    
            }
            jSONObject.put("approvalInfo",jSONArrayTemp);
            jSONArray.put(jSONObject);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public ArrayList getCustomersUpdationInfo(String companyID, JSONObject perms, Boolean isDashboard) throws ServiceException {
        ArrayList jArray=new ArrayList();
        try {
//            String query="from Customer where company.companyID=? and modifiedOn is null order by createdOn";
//            List list = HibernateUtil.executeQueryPaging(session, query, new Object[]{companyID}, new Integer[]{0,2});
            KwlReturnObject result = accCustomerDAOobj.getCustomer_Dashboard(companyID, true, "createdOn", 0, 2);
            List list = result.getEntityList();
            Iterator itr=list.iterator();
            String link;
            String customerID;
            while(itr.hasNext()){
                Customer customer=(Customer)itr.next();
                customerID=customer.getID();
                link=customer.getName();
                if(permissionHandler.isPermitted(perms, "customer", "view"))
                    link=getLink(link, "callCustomerDetails(\""+customerID+"\")");

                jArray.add(getFormatedAlert("New customer "+link+" created","accountingbase updatemsg-customer",isDashboard));
            }
//            query="from Customer where company.companyID=? and modifiedOn is not null order by modifiedOn";
//            list = HibernateUtil.executeQueryPaging(session, query, new Object[]{companyID}, new Integer[]{0,2});
            result = accCustomerDAOobj.getCustomer_Dashboard(companyID, false, "modifiedOn", 0, 2);
            itr=list.iterator();
            while(itr.hasNext()){
                Customer customer=(Customer)itr.next();
                customerID=customer.getID();
                link=customer.getName();
                if(permissionHandler.isPermitted(perms, "customer", "view"))
                    link=getLink(link, "callCustomerDetails(\""+customerID+"\")");
                jArray.add(getFormatedAlert("Customer "+link+" modified","accountingbase updatemsg-customer",isDashboard));
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jArray;
    }
    
    public void getCustomersUpdationInfoIphone(String companyID, JSONObject perms, Boolean isDashboard,JSONArray jSONArray) throws ServiceException {
       JSONObject jSONObject=new JSONObject();
        JSONArray jSONArrayTemp=new JSONArray();
        try {
//            String query="from Customer where company.companyID=? and modifiedOn is null order by createdOn";
//            List list = HibernateUtil.executeQueryPaging(session, query, new Object[]{companyID}, new Integer[]{0,2});
            KwlReturnObject result = accCustomerDAOobj.getCustomer_Dashboard(companyID, true, "createdOn", 0, 2);
            List list = result.getEntityList();
            Iterator itr=list.iterator();
            String link;
            String customerID;
            while(itr.hasNext()){
                Customer customer=(Customer)itr.next();
                customerID=customer.getID();
                link=customer.getName();
//                if(permissionHandler.isPermitted(perms, "customer", "view"))
//                    link=getLink(link, "callCustomerDetails(\""+customerID+"\")");

                jSONArrayTemp.put("New customer "+link+" created");
            }
//            query="from Customer where company.companyID=? and modifiedOn is not null order by modifiedOn";
//            list = HibernateUtil.executeQueryPaging(session, query, new Object[]{companyID}, new Integer[]{0,2});
            result = accCustomerDAOobj.getCustomer_Dashboard(companyID, false, "modifiedOn", 0, 2);
            itr=list.iterator();
            while(itr.hasNext()){
                Customer customer=(Customer)itr.next();
                customerID=customer.getID();
                link=customer.getName();
//                if(permissionHandler.isPermitted(perms, "customer", "view"))
//                    link=getLink(link, "callCustomerDetails(\""+customerID+"\")");
                jSONArrayTemp.put("Customer "+link+" modified");
            }
            jSONObject.put("customer",jSONArrayTemp);
            jSONArray.put(jSONObject);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
    }

    public String getContentDiv(String typeStr) {
        String div = "<div  class=\""+typeStr +" statusitemimg\"></div>";
        return div;
    }

    public String getContentSpan(String textStr,Boolean isDashboard){
        String upperspacing="";
        if(isDashboard)
            upperspacing="dashboardupdate";
        String span = "<span class=\"statusitemcontent "+upperspacing+"\">" +textStr + "</span><div class=\"statusclr\"></div>";
        return span;
    }

    public String getLink(String message, String functionName) {
        return "<a href=# onclick='"+functionName+"'>"+message+"</a>";
    }

    public String getLink(String message, String functionName, String toolTip) {
        return "<a href=# onclick='"+functionName+"' wtf:qtip='"+toolTip+"'>"+message+"</a>";
    }

    public String getFormatedAlert(String message, String cssClass,Boolean isDashboard) {
        String fmtMsg="";
        if(isDashboard)
            fmtMsg=getContentDiv(cssClass);
        fmtMsg+=message;
        return getContentSpan(fmtMsg,isDashboard);
    }

    public StringBuilder getSectionHeader(String headerText) {
        StringBuilder sb=new StringBuilder();
        sb.append("<div class=\"statuspanelheader\"><span class=\"statuspanelheadertext\">");
        sb.append(headerText);
        sb.append("</span></div>");
        return sb;
    }

    private StringBuilder createNewLink(String text, String functionName, String cssClass) {
        StringBuilder newLink=new StringBuilder();
        newLink.append("<li>");
        newLink.append(getLink(text, functionName));
        newLink.append("<ul class='").append(cssClass).append("'>");
        newLink.append("</ul>");
        newLink.append("</li>");
        return newLink;
    }

    private StringBuilder createNewLink(String text, String functionName, String cssClass, String toolTip) {
        StringBuilder newLink=new StringBuilder();
        newLink.append("<li>");
        newLink.append(getLink(text, functionName, toolTip));
        newLink.append("<ul class='").append(cssClass).append("'>");
        newLink.append("</ul>");
        newLink.append("</li>");
        return newLink;
    }

    private StringBuilder createNewLink(String text, String functionName) {
        return createNewLink(text, functionName, "leadlist");
    }

    private StringBuilder createSection(String title, String sectionid, StringBuilder innerData) {
        StringBuilder data=new StringBuilder();
        data.append(getSectionHeader(title));
        data.append("<ul id='").append(sectionid).append("'>");
        data.append(innerData);
        data.append("</ul>");
        data.append("<div>&nbsp;</div>");
        return data;
    }

    private StringBuilder createLeftPane(String title, StringBuilder innerData,StringBuilder outerData,String userid) {
        StringBuilder buffer=new StringBuilder();
        buffer.append(outerData);
        //buffer.append("<div  id='statuspanelouterid' class=\"statuspanelouter\"><div class=\"statuspanelinner\">");
        buffer.append("<div id='statuspanelouterid");
        buffer.append(userid);
        buffer.append("' class=\"statuspanelouter\"><div class=\"statuspanelinner\">");
        buffer.append(getSectionHeader("<span style='float: left;'>"+title+"</span><span style='float: right;font-weight:normal'></span>"));
        buffer.append(innerData);
        buffer.append("</div></div>");
        return buffer;
    }
    private StringBuilder createLeftPaneSepratly(String title, StringBuilder innerData, String userid) {
        StringBuilder buffer=new StringBuilder();
        if(!StringUtil.isNullOrEmpty(innerData.toString())){
            buffer.append("<div class=\"statuspanelouter\"><div class=\"statuspanelinner\">");
            buffer.append(getSectionHeader("<span style='float: left;'>"+title+"</span><span style='float: right;font-weight:normal'></span>"));
            buffer.append(innerData);
            buffer.append("</div></div>");
        }
        return buffer;
    }

    public StringBuilder getSetupWizard(HttpServletRequest request, String companyid){
        String imgPath="../../images/welcome/";
        StringBuilder buffer = new StringBuilder();
        buffer.append(createHelpSection(getLink(messageSource.getMessage("acc.nee.26", null, RequestContextUtils.getLocale(request)),"callCOA()"),
        		messageSource.getMessage("acc.nee.27", null, RequestContextUtils.getLocale(request)),
                imgPath+getImageName("coa", companyid)+".gif",""));
        buffer.append(createHelpSection(messageSource.getMessage("acc.nee.28", null, RequestContextUtils.getLocale(request))+" "+getLink(messageSource.getMessage("acc.nee.42", null, RequestContextUtils.getLocale(request)),"callCustomerDetails()")+" "+messageSource.getMessage("acc.nee.29", null, RequestContextUtils.getLocale(request))+" "+getLink(messageSource.getMessage("acc.nee.41", null, RequestContextUtils.getLocale(request)),"callVendorDetails()"),
        		messageSource.getMessage("acc.nee.30", null, RequestContextUtils.getLocale(request)),
                imgPath+getImageName("customer", companyid)+".png",""));
        buffer.append(createHelpSection(getLink(messageSource.getMessage("acc.accPref.autoInvoice", null, RequestContextUtils.getLocale(request)),"callInvoice(false,null)"),
        		messageSource.getMessage("acc.nee.31", null, RequestContextUtils.getLocale(request)),
                imgPath+getImageName("invoice", companyid)+".png",messageSource.getMessage("acc.nee.36", null, RequestContextUtils.getLocale(request))));
        buffer.append(createHelpSection(getLink(messageSource.getMessage("acc.nee.32", null, RequestContextUtils.getLocale(request)),"callJournalEntry()"),
        		messageSource.getMessage("acc.nee.33", null, RequestContextUtils.getLocale(request)),
                imgPath+getImageName("journalentry", companyid)+".png",messageSource.getMessage("acc.nee.37", null, RequestContextUtils.getLocale(request))));
        buffer.append(createHelpSection(getLink(messageSource.getMessage("acc.nee.34", null, RequestContextUtils.getLocale(request)),"callProductDetails()"),
        		messageSource.getMessage("acc.nee.35", null, RequestContextUtils.getLocale(request)),
                imgPath+getImageName("product", companyid)+".png",messageSource.getMessage("acc.nee.38", null, RequestContextUtils.getLocale(request))));
        buffer.append(createHelpSection(getLink(messageSource.getMessage("acc.nee.40", null, RequestContextUtils.getLocale(request)),"callReceiptNew()"),
        		messageSource.getMessage("acc.nee.39", null, RequestContextUtils.getLocale(request)),
                imgPath+getImageName("receipt", companyid)+".gif",""));
        return buffer;
    }

    private StringBuilder createHelpSection(String title, String message, String imgPath, String tipMsg) {
        StringBuilder data=new StringBuilder();
       data.append("<div>&nbsp;</div>");
         data.append("<h2 class='bullet'>"+title+"</h2>");
        data.append("<div style='padding:10px 20px'>"+message+"</div>");
        data.append("<div class='centered'><img src='"+imgPath+"' width='300px' wtf:qtip='"+tipMsg+"' wtf:qtitle='Tip'></div>");
        return data;
    }

    public String joinArrayList(ArrayList arr, String sep) {
        StringBuilder sb=new StringBuilder();
        if(!arr.isEmpty())sb.append(arr.get(0));
        for(int i=1;i<arr.size();i++){
            sb.append(sep+arr.get(i));
        }
        return sb.toString();
    }

    public StringBuilder getSysAdminLinks(HttpServletRequest request, JSONObject perms) {
        StringBuilder newLink = new StringBuilder();

        newLink.append(createNewLink("List of the companies","callSystemAdmin()"));

        return newLink;
    }

    public StringBuilder getCompanyLinks(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();

        try {
            if(permissionHandler.isPermitted(perms, "accpref", "view"))
                newLink.append(createNewLink("Account Preferences","callAccountPref()","leadlist","Maintain general settings for your organization such as financial year settings, account settings, automatic number generation and email settings."));
            if(permissionHandler.isPermitted(perms, "coa", "view"))
                newLink.append(createNewLink("Chart of Accounts","callCOA()","leadlist","Maintain all your accounts including income, expense, bank accounts and more. You can also export the account list in convenient formats as well as add sub-accounts to existing accounts."));
            if(permissionHandler.isPermitted(perms, "customer", "view"))
                newLink.append(createNewLink("Accounts Receivable/Customer(s)","callCustomerDetails()","leadlist","Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts."));
            if(permissionHandler.isPermitted(perms, "vendor", "view"))
                newLink.append(createNewLink("Accounts Payable/Vendor(s)","callVendorDetails()","leadlist","Maintain all information about your vendors including contact information, account details, preferred delivery mode and debit term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts."));
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }

        return finalString;
    }

    public StringBuilder getMasterSettingLinks(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();
        try {
           newLink.append(createNewLink("Master Configuration","callMasterConfiguration()","leadlist","Define settings for payment methods, payment terms, unit of measure, bank names, preferred delivery mode and more."));
        } finally {
            finalString = newLink;
        }
        return finalString;
    }

    public StringBuilder getProductLinks(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();

        try {
            if(permissionHandler.isPermitted(perms, "product", "view"))
                newLink.append(createNewLink("Product List","callProductDetails()","leadlist","Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product."));
//            if(PermissionHandler.isPermitted(perms, "uom", "view"))
//                newLink.append(createNewLink("Unit of measure","callUOM()"));
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }

        return finalString;
    }

     public StringBuilder getAdministrationLinks(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();
        try {
            if(permissionHandler.isPermitted(perms, "useradmin", "view"))
                newLink.append(createNewLink("User Administration","loadAdminPage(1)","leadlist","Easily manage all users in the system. Assign roles and permission to individual users in accordance to their work functions."));
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }
        return finalString;
    }

    public StringBuilder getPurchaseManagementLinks(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();
        try {
                newLink.append(createNewLink("Create Cash Purchase","callPurchaseReceipt(false,null)","leadlist","Create a cash purchase receipt to give to your vendors as a payment record, on paying full amount at the time of purchase."));
                newLink.append(createNewLink("Create Purchase Order","callPurchaseOrder(false, null)","leadlist","Easily create purchase order for your vendors. Include debit term and complete purchase information."));
                newLink.append(createNewLink("Create Vendor Invoice","callPurchaseInvoiceType()","leadlist","Provide your vendors with receipt on delivery of purchased goods. Record product and payment details."));
                newLink.append(createNewLink("Create Debit Note","callCreditNote(false)","leadlist","Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc."));
         } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }
        return finalString;
    }

    public StringBuilder getSalesManagementLinks(HttpServletRequest request, JSONObject perms) throws ServiceException, SessionExpiredException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();
        try {
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            String companyID = sessionHandlerImplObj.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

            if(pref!=null){
                if(pref.isWithoutInventory()){
                    newLink.append(createNewLink("Create Cash Sales"," callBillingSalesReceipt(false,null)","leadlist","Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale."));
                        newLink.append(createNewLink("Create Invoice","callBillingInvoice(false,null)","leadlist","Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount."));
                }
                else{
                    newLink.append(createNewLink("Create Cash Sales","callSalesReceipt(false,null)","leadlist","Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale."));
                    newLink.append(createNewLink("Create Sales Order","callSalesOrder(false, null)","leadlist","Record all details related to a customer purchase order by generating an associated sales order."));
                    if(permissionHandler.isPermitted(perms, "invoice", "create"))
                        newLink.append(createNewLink("Create Invoice","callInvoice(false,null)","leadlist","Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount."));
                    if(permissionHandler.isPermitted(perms, "invoice", "view"))
                        newLink.append(createNewLink("Create Credit Note","callCreditNote(true)","leadlist","If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases."));

                }
            }else{
                newLink.append(createNewLink("Create Cash Sales","callSalesReceipt(false,null)","leadlist","Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale."));
                newLink.append(createNewLink("Create Sales Order","callSalesOrder(false, null)","leadlist","Record all details related to a customer purchase order by generating an associated sales order."));
                if(permissionHandler.isPermitted(perms, "invoice", "create"))
                    newLink.append(createNewLink("Create Invoice","callInvoice(false,null)","leadlist","Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount."));
                if(permissionHandler.isPermitted(perms, "invoice", "view"))
                    newLink.append(createNewLink("Create Credit Note","callCreditNote(true)","leadlist","If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases."));
            }
            //   TODO         if(PermissionHandler.isPermitted(perms, "invoice", "view"))
//            if(PermissionHandler.isPermitted(perms, "invoice", "view"))
//                newLink.append(createNewLink("Credit Note","callCreditMemo()"));
//            if(PermissionHandler.isPermitted(perms, "invoice", "view"))
//                newLink.append(createNewLink("Credit Note/Receipt","callReceipt()"));
//               newLink.append(createNewLink("Debit Note/Payment","callDebitNote()"));
//                newLink.append(createNewLink("Payment","callPayment()"));
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }
        return finalString;
    }

    public StringBuilder getJournalEntryLinks(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();
        try {
                if(permissionHandler.isPermitted(perms, "journalentry", "create"))
                    newLink.append(createNewLink("Make a Journal Entry","callJournalEntry()","leadlist","Record miscellaneous transactions which have not been recorded in the application through customer/vendor transactions."));
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }
        return finalString;
    }

    public StringBuilder getPaymentLinks(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();
        try {
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            String companyID = sessionHandlerImplObj.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

            if(pref!=null){
                if(pref.isWithoutInventory())
                    newLink.append(createNewLink("Receive Payment(s)","callBillingReceipt()","leadlist","Record all payments through multiple payment methods including cash, cheque and debit/credit card."));
                else{
                    if (Constants.isNewPaymentStructure) {
                        newLink.append(createNewLink("Receive Payment(s)", "callReceiptNew()", "leadlist", "Record all payments through multiple payment methods including cash, cheque and debit/credit card."));
                    } else {
                        newLink.append(createNewLink("Receive Payment(s)", "callReceipt()", "leadlist", "Record all payments through multiple payment methods including cash, cheque and debit/credit card."));
                    }
                }
            }
            else{
                if (Constants.isNewPaymentStructure) {
                    newLink.append(createNewLink("Receive Payment(s)", "callReceiptNew()", "leadlist", "Record all payments through multiple payment methods including cash, cheque and debit/credit card."));
                } else {
                    newLink.append(createNewLink("Receive Payment(s)", "callReceipt()", "leadlist", "Record all payments through multiple payment methods including cash, cheque and debit/credit card."));
                }
            }
                if(Constants.isNewPaymentStructure){ // Used for redirecting the request either to Old Payment structure  or Newly designed structure
                newLink.append(createNewLink("Make Payment(s)","callPaymentNew()","leadlist","Record all payments through multiple payment methods including cash, cheque and debit/credit card."));
            } else {
                newLink.append(createNewLink("Make Payment(s)","callPayment()","leadlist","Record all payments through multiple payment methods including cash, cheque and debit/credit card."));
            }
            } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }
        return finalString;
    }

    public StringBuilder getReportLinks(HttpServletRequest request, JSONObject perms) throws ServiceException, SessionExpiredException {
        StringBuilder finalString = new StringBuilder();
        StringBuilder newLink = new StringBuilder();

        try {
//            CompanyAccountPreferences pref=(CompanyAccountPreferences)session.get(CompanyAccountPreferences.class,AuthHandler.getCompanyid(request));
            String companyID = sessionHandlerImplObj.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

                //newLink.append(createNewLink("Products List","callProductList()"));
                //newLink.append(createNewLink("Customer List","callCustomerReport()"));
                //newLink.append(createNewLink("Vendor List","callVendorReport()"));
//            if(PermissionHandler.isPermitted(perms, "invoice", "view"))
//                newLink.append(createNewLink("Sales Register","callInvoiceDetails()"));
//            if(PermissionHandler.isPermitted(perms, "creditnote", "view"))
//                newLink.append(createNewLink("Credit Note","callCreditMemoDetails()"));
              newLink.append(createNewLink("<b>Financial Statements</b>","callFinalStatement()","leadlist","Track all major financial statements such as trial balance, ledger, trading and profit/loss statement and balance sheet."));
               if(pref!=null){
              if(pref.isWithoutInventory()){
                        newLink.append(createNewLink("Invoice and Cash Sales Report","callBillingInvoiceList()","leadlist","Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list."));
                        newLink.append(createNewLink("Received Payment","BillingReceiptReport()","leadlist","View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list."));
                    }
                    else{
                        if(permissionHandler.isPermitted(perms, "invoice", "view"))
                            newLink.append(createNewLink("Invoice and Cash Sales Report","callInvoiceList()","leadlist","Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list."));
                        newLink.append(createNewLink("Purchase Order", "callPurchaseOrderList()","leadlist","View complete list of purchase orders issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a purchase order from the given list."));
                        newLink.append(createNewLink("Sales Order", "callSalesOrderList()","leadlist","View complete list of sales order associated with your customers. Export the list in convenient formats or get a quick view by easily expanding a sales order from the given list."));
                        if(permissionHandler.isPermitted(perms, "creditnote", "view"))
                            newLink.append(createNewLink("Credit Note","callCreditNoteDetails()","leadlist","View complete list of credit notes issued to your customers. Export the list in convenient formats or get a quick view by easily expanding a credit note from the given list."));
                        if(permissionHandler.isPermitted(perms, "receipt", "view")){
                            if (Constants.isNewPaymentStructure) {
                                newLink.append(createNewLink("Received Payment(s)", "callReceiptReportNew()", "leadlist", "View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list."));
                            } else {
                                newLink.append(createNewLink("Received Payment(s)", "ReceiptReport()", "leadlist", "View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list."));
                            }
                        }
                        newLink.append(createNewLink("Cash and Credit Purchase Report","callGoodsReceiptList()","leadlist","View complete details of vendor invoice and cash purchase receipt(s) to your vendors. Export the list in convenient formats or get a quick view by easily expanding a vendor invoice from the given list."));
                        newLink.append(createNewLink("Debit Note Report","callDebitNoteDetails()","leadlist","View complete list of debit notes issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a debit note from the given list."));
                   }
               }
               else{
                  if(permissionHandler.isPermitted(perms, "invoice", "view"))
                            newLink.append(createNewLink("Invoice and Cash Sales Report","callInvoiceList()","leadlist","Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list."));
                        newLink.append(createNewLink("Purchase Order", "callPurchaseOrderList()","leadlist","View complete list of purchase orders issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a purchase order from the given list."));
                        newLink.append(createNewLink("Sales Order", "callSalesOrderList()","leadlist","View complete list of sales order associated with your customers. Export the list in convenient formats or get a quick view by easily expanding a sales order from the given list."));
                        if(permissionHandler.isPermitted(perms, "creditnote", "view"))
                            newLink.append(createNewLink("Credit Note","callCreditNoteDetails()","leadlist","View complete list of credit notes issued to your customers. Export the list in convenient formats or get a quick view by easily expanding a credit note from the given list."));
                        if(permissionHandler.isPermitted(perms, "receipt", "view")){
                            if (Constants.isNewPaymentStructure) {
                                newLink.append(createNewLink("Receive Payment", "callReceiptReportNew()", "leadlist", "View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list."));
                            } else {
                                newLink.append(createNewLink("Receive Payment", "ReceiptReport()", "leadlist", "View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list."));
                            }
                        }
                        newLink.append(createNewLink("Vendor Invoice and Cash Purchase Report","callGoodsReceiptList()","leadlist","View complete details of vendor invoice and cash purchase receipt(s) to your vendors. Export the list in convenient formats or get a quick view by easily expanding a vendor invoice from the given list."));
                        newLink.append(createNewLink("Debit Note Report","callDebitNoteDetails()","leadlist","View complete list of debit notes issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a debit note from the given list."));
              }
//             newLink.append(createNewLink("Book Reports","callBookReport()"));
               if (Constants.isNewPaymentStructure) {  // Used for redirecting the request either to Old Payment structure  or Newly designed structure
                    newLink.append(createNewLink("Payment Made", "callPaymentReportNew()", "leadlist", "View complete details of payments made to your vendors. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list."));
                } else {
                    newLink.append(createNewLink("Payment Made","callPaymentReport()","leadlist","View complete details of payments made to your vendors. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list."));
                } 
             if(permissionHandler.isPermitted(perms, "journalentry", "view"))
                newLink.append(createNewLink("Journal Entry","callJournalEntryDetails()","leadlist","Track all journal entries transactions entered into the system."));
//            if(PermissionHandler.isPermitted(perms, "ledger", "view"))
//                newLink.append(createNewLink("Ledger","callLedger()"));
//            if(PermissionHandler.isPermitted(perms, "trialbalance", "view"))
//                newLink.append(createNewLink("Trial Balance","TrialBalance()"));
//            if(PermissionHandler.isPermitted(perms, "trading", "view"))
//                newLink.append(createNewLink("Trading","Trading()"));
//            if(PermissionHandler.isPermitted(perms, "pl", "view"))
//                newLink.append(createNewLink("Profit and Loss","ProfitandLoss()"));
//            if(PermissionHandler.isPermitted(perms, "trading", "view")&&PermissionHandler.isPermitted(perms, "pl", "view"))
//                newLink.append(createNewLink("Trading, Profit and Loss","TradingProfitLoss()"));
//            if(PermissionHandler.isPermitted(perms, "bsheet", "view"))
//                newLink.append(createNewLink("Balance Sheet","BalanceSheet()"));

            if(permissionHandler.isPermitted(perms, "cashbook", "view"))
                newLink.append(createNewLink("Cash Book","callFrequentLedger(true,\"23\",\"Cash Book\",\"accountingbase cashbook\")","leadlist","Monitor all cash transactions entered into the system for any time duration."));
            if(permissionHandler.isPermitted(perms, "bankbook", "view"))
                newLink.append(createNewLink("Bank Book","callFrequentLedger(false,\"9\",\"Bank Book\",\"accountingbase bankbook\")","leadlist","Monitor all transactions for a bank account for any time duration."));
////            if(PermissionHandler.isPermitted(perms, "bankbook", "view"))
                newLink.append(createNewLink("Aged Receivable", "callAgedRecievable()","leadlist","Keep a track record of all amount receivables."));
////            if(PermissionHandler.isPermitted(perms, "bankbook", "view"))
                newLink.append(createNewLink("Aged Payable", "callAgedPayable()","leadlist",messageSource.getMessage("acc.WoutI.36", null, RequestContextUtils.getLocale(request))));

            if(permissionHandler.isPermitted(perms, "audittrail", "view"))
                newLink.append(createNewLink("Audit Trail","callAuditTrail()","leadlist",messageSource.getMessage("acc.field.TrackalluseractivitiesthroughcomprehensiveAccountingsystemrecords", null, RequestContextUtils.getLocale(request))));
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            finalString = newLink;
        }

        return finalString;
    }

    private StringBuilder getDashBoardDataFlow(HttpServletRequest request, JSONObject perms) throws ServiceException, SessionExpiredException {
        StringBuilder finalStr = new StringBuilder();
        try {
            String companyID = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            KwlReturnObject cpresult1 = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            ExtraCompanyPreferences extrapref = null;
            if(cpresult1 != null){
               extrapref = (ExtraCompanyPreferences) cpresult1.getEntityList().get(0);
            }
            boolean isInventory =false;
            if(extrapref!=null){
             isInventory=extrapref.isActivateInventoryTab();
            }
            String inventorydiv = "";
            int count =0;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyID);
            KwlReturnObject result = accountingDashboardDAOObj.getPendingApprovalDetails(requestParams);
            count = result.getRecordTotalCount();
            /*
                getDashboardWelcomeMsg(HttpServletRequest, int approvalPendingCount)
            */
            finalStr.append(getDashboardWelcomeMsg(request, count));
            
            if (isInventory) {
               inventorydiv = "<IMG class='thickBlackBorderInside1' id='MRPInventory' SRC='../../images/" + getImageName("Inventory", companyID) + ".jpg' usemap='#AlienAreas8'>";
            }
            String imageName = pref.isWithInvUpdate() ? getImageName("purchasemanagement",companyID) : getImageName("purchasemanagement_withoutDO",companyID);
            finalStr.append("<div class='firstflowlink' ><IMG class='thickBlackBorder' id = 'purchasemanagement1' SRC='../../images/"+imageName+".jpg' usemap='#AlienAreas1'>"
                    + "<IMG class='thickBlackBorderInside1' id='customerVendorAndProductManagement1' SRC='../../images/"+getImageName("customervendorinventory",companyID)+".jpg' usemap='#AlienAreas4'>"
                    +inventorydiv+"</div>");
            
            imageName = pref.isWithInvUpdate() ? getImageName("salesmanagement_quotation",companyID) : getImageName("salesmanagement_quotation_withoutDO",companyID);
            /*
                Original code for Ratio Analysis Report on dashboard has commented below. Next consecutive line is a temporary code. It does not contain Ratio Analysis Report
                link for Dashboard. Once re-designing of Ratio Analysis report gets completed then un-comment the below line & remove the next consecutive line.
            */
            //finalStr.append("<div class='secondflowlink'><IMG class='thickBlackBorder' id='salesAndBillingManagement1' SRC='../../images/"+imageName+".jpg' usemap='#AlienAreas2'><IMG class='thickBlackBorderInside3' id='accountManagement1' SRC='../../images/"+getImageName("accountmanagement",companyID)+".jpg' usemap='#AlienAreas5'><IMG class='thickBlackBorderInside6' id='ratioAnalysis' SRC='../../images/"+getImageName("ratio-analysis-report",companyID)+".jpg' usemap='#AlienAreas6'></div>");    
            finalStr.append("<div class='secondflowlink'><IMG class='thickBlackBorder' id='salesAndBillingManagement1' SRC='../../images/"+imageName+".jpg' usemap='#AlienAreas2'><IMG class='thickBlackBorderInside3' id='accountManagement1' SRC='../../images/"+getImageName("accountmanagement",companyID)+".jpg' usemap='#AlienAreas5'></div>");   //SDP-10113
            
            if(pref.isWithoutTax1099()) {
                if (extrapref !=null && extrapref.isDeliveryPlanner()) {
                    imageName = getImageName("financialreport133", companyID);
                } else {
                    imageName = pref.isWithInvUpdate() ? getImageName("financialreport13", companyID) : getImageName("financialreport13_withoutDO", companyID);
                }
               finalStr.append("<div class='thirdflowlink'><IMG class='thickBlackBorder' id='financialReports1' SRC='../../images/"+imageName+".jpg' usemap='#AlienAreas3'></div>");
            
            } else {
                imageName = pref.isWithInvUpdate() ? getImageName("financialreport14",companyID) : getImageName("financialreport14_withoutDO",companyID);
                finalStr.append("<div class='thirdflowlink'><IMG class='thickBlackBorder' id='financialReports1' SRC='../../images/"+imageName+".jpg' usemap='#AlienAreas3'></div>");
            }
                
            finalStr.append("<map name='AlienAreas1'>" );
            finalStr.append("<area shape='rect' coords='50,60,150,160' href=# onclick='callBusinessContactWindow(false, null, null, false)'  wtf:qtip='"+messageSource.getMessage("acc.WI.44", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your vendors including contact information, account details, preferred delivery mode and debit term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts.'>");
            finalStr.append("<area shape='rect' coords='140,60,240,149' href=# onclick='callProductDetails(null,true)' wtf:qtip='"+messageSource.getMessage("acc.WI.43", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product.'> ");
            finalStr.append("<area shape='rect' coords='10,209,145,303' href=# onclick='callPurchaseReceipt(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WI.42", null, RequestContextUtils.getLocale(request))+"'>");   //Create a cash purchase receipt to give to your vendors as a payment record, on paying full amount at the time of purchase.'> ");
            finalStr.append("<area shape='rect' coords='160,213,280,294' href=# onclick='callPurchaseOrder(false, null)' wtf:qtip='"+messageSource.getMessage("acc.WI.41", null, RequestContextUtils.getLocale(request))+"'>");   //Easily create purchase order for your vendors. Include debit term and complete purchase information.'> ");
            finalStr.append("<area shape='rect' coords='300, 213, 405, 320' href=# onclick='callVendorQuotation()' wtf:qtip='"+messageSource.getMessage("acc.WI.51", null, RequestContextUtils.getLocale(request))+"'>");   
            finalStr.append("<area shape='rect' coords='300, 83, 405, 200' href=# onclick='callPurchaseReq()' wtf:qtip='"+messageSource.getMessage("acc.WI.52", null, RequestContextUtils.getLocale(request))+"'>");   
            
            finalStr.append("<area shape='rect' coords='19,369,83,473' href=# onclick='callAgedPayable({\"withinventory\":true})' wtf:qtip='"+messageSource.getMessage("acc.WI.40", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount payables.'> ");
            finalStr.append("<area shape='rect' coords='160,317,310,400' href=# onclick='callPurchaseInvoiceType()' wtf:qtip='"+messageSource.getMessage("acc.WI.39", null, RequestContextUtils.getLocale(request))+"'>");   //Provide your vendors with receipt on delivery of purchased goods. Record product and payment details.'> ");
            if(pref.isWithInvUpdate()){
                finalStr.append("<area shape='rect' coords='321,318,394,419' href=# onclick='callGoodsReceiptDelivery(false,null,null)' wtf:qtip='"+messageSource.getMessage("acc.WI.47", null, RequestContextUtils.getLocale(request))+"'>");   //Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc.'>");
                finalStr.append("<area shape='rect' coords='321,425,394,525' href=# onclick='callSalesReturnWindow(false)' wtf:qtip='"+messageSource.getMessage("acc.WI.50", null, RequestContextUtils.getLocale(request))+"'>");   //Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc.'>");
                finalStr.append("<area shape='rect' coords='321,528,394,628' href=# onclick='callCreditNote(false)' wtf:qtip='"+messageSource.getMessage("acc.WI.38", null, RequestContextUtils.getLocale(request))+"'>");   //Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc.'>");
            } else {
                finalStr.append("<area shape='rect' coords='321,318,394,419' href=# onclick='callCreditNote(false)' wtf:qtip='"+messageSource.getMessage("acc.WI.38", null, RequestContextUtils.getLocale(request))+"'>");   //Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc.'>");
            }
            finalStr.append("<area shape='rect' coords='19,369,71,430' href=# onclick='callAgedPayable({\"withinventory\":true})' wtf:qtip='Keep a track record of all amount payables.'> ");
            if(Constants.isNewPaymentStructure){  // Used for redirecting the request either to Old Payment structure  or Newly designed structure
                finalStr.append("<area shape='rect' coords='172,422,272,524' href=# onclick='callPaymentNew()' wtf:qtip='"+messageSource.getMessage("acc.WI.37", null, RequestContextUtils.getLocale(request))+"'>");//Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
            } else {
                finalStr.append("<area shape='rect' coords='172,422,272,524' href=# onclick='callPayment()' wtf:qtip='"+messageSource.getMessage("acc.WI.37", null, RequestContextUtils.getLocale(request))+"'>");
            }   
            finalStr.append("</map>");

            finalStr.append("<map name='AlienAreas2'>" );
            finalStr.append("<area shape='rect' coords='34,67,138,161' href=# onclick='callBusinessContactWindow(false, null, null, true)' wtf:qtip='"+messageSource.getMessage("acc.WI.36", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.'>");
            finalStr.append("<area shape='rect' coords='140,73,240,149' href=# onclick='callProductDetails(null,true)' wtf:qtip='"+messageSource.getMessage("acc.WI.35", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product.'>");
            finalStr.append("<area shape='rect' coords='10,215,130,301' href=# onclick='callSalesReceipt(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WI.34", null, RequestContextUtils.getLocale(request))+"'>");   //Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale.'>");
            finalStr.append("<area shape='rect' coords='160,213,280,294' href=# onclick='callSalesOrder(false, null)' wtf:qtip='"+messageSource.getMessage("acc.WI.33", null, RequestContextUtils.getLocale(request))+"'>");   //Record all details related to a customer purchase order by generating an associated sales order.'>");
            finalStr.append("<area shape='rect' coords='19,369,83,473' href=# onclick='callAgedRecievable({\"withinventory\":true})' wtf:qtip='"+messageSource.getMessage("acc.WI.32", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount receivables.'>");
            finalStr.append("<area shape='rect' coords='160,317,280,400' href=# onclick='callInvoice(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WI.31", null, RequestContextUtils.getLocale(request))+"'>");   //Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount.'>");
            if(pref.isWithInvUpdate()){
                finalStr.append("<area shape='rect' coords='321,545,394,650' href=# onclick='callCreditNote(true)' wtf:qtip='"+messageSource.getMessage("acc.WI.30", null, RequestContextUtils.getLocale(request))+"'>");   //If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases.'>");
                finalStr.append("<area shape='rect' coords='321,435,394,540' href=# onclick='callSalesReturnWindow(true)' wtf:qtip='"+messageSource.getMessage("acc.WI.53", null, RequestContextUtils.getLocale(request))+"'>");   //If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases.'>");
                finalStr.append("<area shape='rect' coords='321,318,394,419' href=# onclick='callDeliveryOrder(false, null)' wtf:qtip='"+messageSource.getMessage("acc.WI.45", null, RequestContextUtils.getLocale(request))+"'>");   //If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases.'>");
            } else {
                finalStr.append("<area shape='rect' coords='321,318,394,419' href=# onclick='callCreditNote(true)' wtf:qtip='"+messageSource.getMessage("acc.WI.30", null, RequestContextUtils.getLocale(request))+"'>");   //If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases.'>");
            }
            if (Constants.isNewPaymentStructure) {
                finalStr.append("<area shape='rect' coords='154,425,302,511' href=# onclick='callReceiptNew()' wtf:qtip='" + messageSource.getMessage("acc.WI.29", null, RequestContextUtils.getLocale(request)) + "'>");   //Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
            } else {
                finalStr.append("<area shape='rect' coords='154,425,302,511' href=# onclick='callReceipt()' wtf:qtip='" + messageSource.getMessage("acc.WI.29", null, RequestContextUtils.getLocale(request)) + "'>");   //Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");                
            }
            finalStr.append("<area shape='rect' coords='320,213,385,290' href=# onclick='callQuotation()' wtf:qtip="+messageSource.getMessage("acc.WI.28", null, RequestContextUtils.getLocale(request))+"'>");   //\"Generate Quotation's related to your Customer's requirements. You can use this to give your Customer's a rough idea of financial costings for their requirements.\">");
            finalStr.append("</map>");
            
            finalStr.append("<map name='AlienAreas3'>" );

            finalStr.append("<area shape='rect' coords='15,34,129,139' href=# onclick='callFinalStatement()' wtf:qtip='"+messageSource.getMessage("acc.WI.27", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as trial balance, ledger, trading and profit/loss statement and balance sheet.'> ");
            finalStr.append("<area shape='rect' coords='145,35,390,140' href=# onclick='javascript:void(0)' wtf:qtip='"+messageSource.getMessage("acc.WI.26", null, RequestContextUtils.getLocale(request))+"'>");   //Track all the Transaction Records such as Journal Entry, Cash Sales, Cash Purchases, Customer and Vendor Invoices, Credit Note and Debit Note reports among others.'> ");
            finalStr.append("<area shape='rect' coords='36,157,116,243' href=# onclick='TrialBalance()' wtf:qtip='"+messageSource.getMessage("acc.WI.25", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as trial balance.'> ");
            finalStr.append("<area shape='rect' coords='21,254,128,322' href=# onclick='callGeneralLedger()' wtf:qtip='"+messageSource.getMessage("acc.WI.24", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as ledger.'> ");
            finalStr.append("<area shape='rect' coords='22,336,146,426' href=# onclick='NewTradingProfitLoss()' wtf:qtip='"+messageSource.getMessage("acc.WI.23", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as  trading and profit/loss statement '> ");
            finalStr.append("<area shape='rect' coords='23,429,141,501' href=# onclick='periodViewBalanceSheet()' wtf:qtip='"+messageSource.getMessage("acc.WI.22", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as balance sheet.'> ");


            finalStr.append("<area shape='rect' coords='18,517,146,583' href=# onclick='callAgedPayable({\"withinventory\":true})' wtf:qtip='"+messageSource.getMessage("acc.WI.21", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount payables'> ");
            finalStr.append("<area shape='rect' coords='22,598,142,683' href=# onclick='callAgedRecievable({\"withinventory\":true})' wtf:qtip='"+messageSource.getMessage("acc.WI.20", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount receivables'>");

            finalStr.append("<area shape='rect' coords='169,146,275,257' href=# onclick='callJournalEntryDetails()' wtf:qtip='"+messageSource.getMessage("acc.WI.19", null, RequestContextUtils.getLocale(request))+"'>");   //Track all journal entries transactions entered into the system.'>");
            finalStr.append("<area shape='rect' coords='162,263,271,352' href=# onclick='callInvoiceList()'  wtf:qtip='"+messageSource.getMessage("acc.WI.18", null, RequestContextUtils.getLocale(request))+"'>");   //Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list.'>");
            finalStr.append("<area shape='rect' coords='158,367,273,451' href=# onclick='callPurchaseOrderList()' wtf:qtip='"+messageSource.getMessage("acc.WI.17", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of purchase orders issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a purchase order from the given list.'>");
            finalStr.append("<area shape='rect' coords='161,462,278,539' href=# onclick='callSalesOrderList()' wtf:qtip='"+messageSource.getMessage("acc.WI.16", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of sales order associated with your customers. Export the list in convenient formats or get a quick view by easily expanding a sales order from the given list.'>");
            finalStr.append("<area shape='rect' coords='162,541,267,632' href=# onclick='callCreditNoteDetails()' wtf:qtip='"+messageSource.getMessage("acc.WI.15", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of credit notes issued to your customers. Export the list in convenient formats or get a quick view by easily expanding a credit note from the given list.'>");
             if (Constants.isNewPaymentStructure) {
                finalStr.append("<area shape='rect' coords='162,643,287,723' href=# onclick='callReceiptReportNew()' wtf:qtip='" + messageSource.getMessage("acc.WI.14", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
            } else {
                finalStr.append("<area shape='rect' coords='162,643,287,723' href=# onclick='ReceiptReport()' wtf:qtip='" + messageSource.getMessage("acc.WI.14", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");                 
            }
            if(pref.isWithInvUpdate()){
                finalStr.append("<area shape='rect' coords='162,720,260,820' href=# onclick='callDeliveryOrderList()' wtf:qtip='"+messageSource.getMessage("acc.WI.46", null, RequestContextUtils.getLocale(request))+"'>");   //View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
                finalStr.append("<area shape='rect' coords='162,825,260,920' href=# onclick='callPurchaseReturnList()'wtf:qtip='"+messageSource.getMessage("acc.field.ViewcompletedetailsofPurchaseReturnfromyourVendors", null, RequestContextUtils.getLocale(request))+"'>");   //View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
                finalStr.append("<area shape='rect' coords='162,930,260,1020' href=# onclick='callSalesReturnList()'wtf:qtip='"+messageSource.getMessage("acc.field.ViewcompletedetailsofSalesReturnfromyourcustomers", null, RequestContextUtils.getLocale(request))+"'>");   //View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
            }
            
            finalStr.append("<area shape='rect' coords='294,147,394,254' href=# onclick='callGoodsReceiptList()' wtf:qtip='"+messageSource.getMessage("acc.WI.13", null, RequestContextUtils.getLocale(request))+"'>");   //View complete details of vendor invoice and cash purchase receipt(s) to your vendors. Export the list in convenient formats or get a quick view by easily expanding a vendor invoice from the given list.'>");
            finalStr.append("<area shape='rect' coords='291,260,383,354' href=# onclick='callDebitNoteDetails()' wtf:qtip='"+messageSource.getMessage("acc.WI.12", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of debit notes issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a debit note from the given list.'>");
            if (Constants.isNewPaymentStructure) {  // Used for redirecting the request either to Old Payment structure  or Newly designed structure
                finalStr.append("<area shape='rect' coords='290,366,387,453' href=# onclick='callPaymentReportNew()' wtf:qtip='" + messageSource.getMessage("acc.WI.11", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete details of payments made to your vendors. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
            } else {
                finalStr.append("<area shape='rect' coords='290,366,387,453' href=# onclick='callPaymentReport()' wtf:qtip='" + messageSource.getMessage("acc.WI.11", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete details of payments made to your vendors. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
            } 
            if(pref.isWithInvUpdate()){
                finalStr.append("<area shape='rect' coords='293,461,389,534' href=# onclick='callGoodsReceiptOrderList()' wtf:qtip='"+messageSource.getMessage("acc.WI.48", null, RequestContextUtils.getLocale(request))+"'>");   //Monitor all cash transactions entered into the system for any time duration.'>");
                finalStr.append("<area shape='rect' coords='291,549,389,617' href=# onclick='callFrequentLedger(true,\"23\",\""+messageSource.getMessage("acc.WoutI.7", null, RequestContextUtils.getLocale(request))+"\",\"accountingbase cashbook\")' wtf:qtip='"+messageSource.getMessage("acc.WI.10", null, RequestContextUtils.getLocale(request))+"'>");   //Monitor all cash transactions entered into the system for any time duration.'>");
                finalStr.append("<area shape='rect' coords='291,638,389,720' href=# onclick='callFrequentLedger(false,\"9\",\""+messageSource.getMessage("acc.WoutI.8", null, RequestContextUtils.getLocale(request))+"\",\"accountingbase bankbook\")' wtf:qtip='"+messageSource.getMessage("acc.WI.9", null, RequestContextUtils.getLocale(request))+"'>");   //Monitor all transactions for a bank account for any time duration.'>");
                if (extrapref !=null && extrapref.isDeliveryPlanner()) {
                    finalStr.append("<area shape='rect' coords='291,728,389,810' href=# onclick='getDeliveryPlannerTabView()' wtf:qtip='" + messageSource.getMessage("acc.filed.viewdeliveryplantp", null, RequestContextUtils.getLocale(request)) + "'>");   //"Delivery Planner
                }
                if(!pref.isWithoutTax1099()) {
                    finalStr.append("<area shape='rect' coords='291,728,389,810' href=# onclick='call1099Report()' wtf:qtip='"+messageSource.getMessage("acc.WI.7", null, RequestContextUtils.getLocale(request))+"'>");   //Tax 1099 Accounts.'>");
                }
            } else {
                finalStr.append("<area shape='rect' coords='293,461,389,534' href=# onclick='callFrequentLedger(true,\"23\",\""+messageSource.getMessage("acc.WoutI.7", null, RequestContextUtils.getLocale(request))+"\",\"accountingbase cashbook\")' wtf:qtip='"+messageSource.getMessage("acc.WI.10", null, RequestContextUtils.getLocale(request))+"'>");   //Monitor all cash transactions entered into the system for any time duration.'>");
                finalStr.append("<area shape='rect' coords='291,549,389,617' href=# onclick='callFrequentLedger(false,\"9\",\""+messageSource.getMessage("acc.WoutI.8", null, RequestContextUtils.getLocale(request))+"\",\"accountingbase bankbook\")' wtf:qtip='"+messageSource.getMessage("acc.WI.9", null, RequestContextUtils.getLocale(request))+"'>");   //Monitor all transactions for a bank account for any time duration.'>");
                if (extrapref !=null && extrapref.isDeliveryPlanner()) {
                    finalStr.append("<area shape='rect' coords='291,638,389,720' href=# onclick='getDeliveryPlannerTabView()' wtf:qtip='"+messageSource.getMessage("acc.filed.viewdeliveryplantp", null, RequestContextUtils.getLocale(request))+"'>");   //"Delivery Planner
                }
                if(!pref.isWithoutTax1099())
                {
                    finalStr.append("<area shape='rect' coords='291,638,389,720' href=# onclick='call1099Report()' wtf:qtip='"+messageSource.getMessage("acc.WI.7", null, RequestContextUtils.getLocale(request))+"'>");   //Tax 1099 Accounts.'>");
                }
            }
            finalStr.append("<area shape='rect' coords='35,675,110,780' href=# onclick='callVendorQuotationList()'wtf:qtip='"+messageSource.getMessage("acc.field.ViewcompletelistofQuotationsassociatedwithyourvendors", null, RequestContextUtils.getLocale(request))+"'>");
            finalStr.append("<area shape='rect' coords='30,775,115,870' href=# onclick='callQuotationList()' wtf:qtip='"+messageSource.getMessage("acc.WI.8", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of Quotations associated with your customers.'>");
            finalStr.append("<area shape='rect' coords='30,875,115,980' href=# onclick='callPurchaseReqList()'wtf:qtip='"+messageSource.getMessage("acc.field.ViewcompletelistofPurchaseRequisitionsassociatedwithyourvendors", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of Quotations associated with your customers.'>");
            finalStr.append("</map>");

            finalStr.append("<map name='AlienAreas4'>" );
            finalStr.append("<area shape='rect' coords='9,27,100,137' href=# onclick='callCustomerDetails(true)' wtf:qtip='"+messageSource.getMessage("acc.WI.6", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.'>");
            finalStr.append("<area shape='rect' coords='103,35,181,136' href=# onclick='callVendorDetails(true)'  wtf:qtip='"+messageSource.getMessage("acc.WI.5", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your vendors including contact information, account details, preferred delivery mode and debit term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts.'>");
            finalStr.append("<area shape='rect' coords='5,31,289,132' href=# onclick='callProductDetails()' wtf:qtip='"+messageSource.getMessage("acc.WI.4", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product.'> ");
            finalStr.append("</map>");


            finalStr.append("<map name='AlienAreas5'>" );
            finalStr.append("<area shape='rect' coords='10,32,97,125' href=# onclick='callJournalEntry()' wtf:qtip='"+messageSource.getMessage("acc.WI.3", null, RequestContextUtils.getLocale(request))+"'>");   //Record miscellaneous transactions which have not been recorded in the application through customer/vendor transactions.'>");
            finalStr.append("<area shape='rect' coords='109,41,190,131' href=# onclick='callCOA()'  wtf:qtip='"+messageSource.getMessage("acc.WI.2", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all your accounts including income, expense, bank accounts and more. You can also export the account list in convenient formats as well as add sub-accounts to existing accounts.'>");
            finalStr.append("</map>");

            finalStr.append("<map name='AlienAreas6'>" );
            finalStr.append("<area shape='rect' coords='5,36,160,131' href=# onclick='callRatioAnalysis()' wtf:qtip='"+messageSource.getMessage("acc.WI.1", null, RequestContextUtils.getLocale(request))+"'>");   //Gives the summary view of the effect of account on each other.'>");
            finalStr.append("</map>");
            //Mapping Inventory
            finalStr.append("<map name='AlienAreas8'>");
            finalStr.append("<area shape='rect' coords='4,19,72,110' href=# onclick='markoutallTab()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.17", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='88,18,165,109' href=# onclick='goodsOrderTab()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.18", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='182,18,242,106' href=# onclick='goodsIssueTab()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.19", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='5,117,75,207' href=# onclick='interStoreTransfers()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.20", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='96,117,167,207' href=# onclick='interLocationTransfers()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.21", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='186,116,244,207' href=# onclick='callCycleCountCalendar()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.25", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='4,215,68,302' href=# onclick='callCycleCountForm()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.24", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='95,212,157,301' href=# onclick='qaApprovalTab()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.22", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='187,213,242,302' href=# onclick='stockRepaire()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.23", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("</map>");
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }
    
    private StringBuilder getDashBoardDataFlowWithMRP(HttpServletRequest request, JSONObject perms) throws ServiceException, SessionExpiredException {
        StringBuilder finalStr = new StringBuilder();
        try {
            String companyID = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            KwlReturnObject cpresult1 = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            ExtraCompanyPreferences extrapref = null;
            if (cpresult1 != null) {
                extrapref = (ExtraCompanyPreferences) cpresult1.getEntityList().get(0);
            }
            int count = 0;
            boolean isInventory =false;
            isInventory=extrapref.isActivateInventoryTab();
            String inventorydiv ="";
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyID);
            KwlReturnObject result = accountingDashboardDAOObj.getPendingApprovalDetails(requestParams);
            count = result.getRecordTotalCount();
            /*
                getDashboardWelcomeMsg(HttpServletRequest, int approvalPendingCount)
            */
            finalStr.append(getDashboardWelcomeMsg(request, count));
            /*
             * Purchase Management
             */
            String imageName =  getImageName("MRP_purchasemanagement", companyID);
            finalStr.append("<div class='mrpfirstflowlink' >"
                    + "<IMG class='mrpthickBlackBorder' id = 'purchasemanagement1' SRC='../../images/" + imageName + ".jpg' usemap='#AlienAreas1'>"
                    /*
                     * Customer, Vendor and Product Management
                     */
                    + "<IMG class='mrpthickBlackBorderInside1' id='accountManagement1' SRC='../../images/" + getImageName("MRP_accountmanagement", companyID) + ".jpg' usemap='#AlienAreas5'>"
                    + "<IMG class='mrpthickBlackBorderInside7' id='customerVendorAndProductManagement1' SRC='../../images/" + getImageName("MRP_customervendorproduct", companyID) + ".jpg' usemap='#AlienAreas4'>"
                    + "<IMG class='mrpthickBlackBorderInside7' id='MRPMasters' SRC='../../images/" + getImageName("MRP_manufacturingcontractandmaster", companyID) + ".jpg' usemap='#AlienAreas6'>"
                    + "</div>");
            /*
             * Sales and Billing Management
             */
            imageName =  getImageName("MRP_salesmanagement", companyID);
            if (isInventory) {
               inventorydiv = "<IMG class='mrpthickBlackBorderInside3' id='MRPInventory' SRC='../../images/" + getImageName("MRP_inventory", companyID) + ".jpg' usemap='#AlienAreas8'>";
            }
            finalStr.append("<div class='mrpsecondflowlink'>"
                    + "<IMG class='mrpthickBlackBorder' id='salesAndBillingManagement1' SRC='../../images/" + imageName + ".jpg' usemap='#AlienAreas2'>"
                    /*
                     * Inventory Management
                     */
                    +inventorydiv
            
            /*
                     * Ratio Analysis Report
                     */
//                    + "<IMG class='mrpthickBlackBorderInside6' id='ratioAnalysis' SRC='../../images/" + getImageName("ratio-analysis-report", companyID) + ".jpg' usemap='#AlienAreas6'>"
                    + "</div>");
            /*
             * Financials Reports
             */
            if (pref.isWithoutTax1099()) {
                if (extrapref != null && extrapref.isDeliveryPlanner()) {
                    imageName = getImageName("MRP_financialreport_with_deliveryplanner", companyID);
                } else {
                    imageName =  getImageName("MRP_financialreport_without_tax1099", companyID);
                }
                finalStr.append("<div class='mrpthirdflowlink'><IMG class='thickBlackBorder' id='financialReports1' SRC='../../images/" + imageName + ".jpg' usemap='#AlienAreas3'></div>");

            } else {
                imageName =  getImageName("MRP_financialreport_with_tax1099", companyID);
                finalStr.append("<div class='mrpthirdflowlink'><IMG class='thickBlackBorder' id='financialReports1' SRC='../../images/" + imageName + ".jpg' usemap='#AlienAreas3'></div>");
            }
            /*
             * MRP Reports
             */
            imageName = "MRP_reports";
            finalStr.append("<div class='mrpfourthflowlink'><IMG class='thickBlackBorder' id='MRPReports' SRC='../../images/" + imageName + ".jpg' usemap='#AlienAreas7'></div>");

            
            //Map for Purchase Management
            finalStr.append("<map name='AlienAreas1'>");
            finalStr.append("<area shape='rect' coords='49,56,128,137' href=# onclick='callBusinessContactWindow(false, null, null, false)'  wtf:qtip='" + messageSource.getMessage("acc.WI.44", null, RequestContextUtils.getLocale(request)) + "'>");   //Maintain all information about your vendors including contact information, account details, preferred delivery mode and debit term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts.'>");
            finalStr.append("<area shape='rect' coords='140,54,216,137' href=# onclick='callProductDetails(null,true)' wtf:qtip='" + messageSource.getMessage("acc.WI.43", null, RequestContextUtils.getLocale(request)) + "'>");   //Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product.'> ");
            finalStr.append("<area shape='rect' coords='9,183,114,268' href=# onclick='callPurchaseReceipt(false,null)' wtf:qtip='" + messageSource.getMessage("acc.WI.42", null, RequestContextUtils.getLocale(request)) + "'>");   //Create a cash purchase receipt to give to your vendors as a payment record, on paying full amount at the time of purchase.'> ");
            finalStr.append("<area shape='rect' coords='140,183,255,270' href=# onclick='callPurchaseOrder(false, null)' wtf:qtip='" + messageSource.getMessage("acc.WI.41", null, RequestContextUtils.getLocale(request)) + "'>");   //Easily create purchase order for your vendors. Include debit term and complete purchase information.'> ");
            finalStr.append("<area shape='rect' coords='279,184,365,279' href=# onclick='callVendorQuotation()' wtf:qtip='" + messageSource.getMessage("acc.WI.51", null, RequestContextUtils.getLocale(request)) + "'>");
            finalStr.append("<area shape='rect' coords='279,81,366,170' href=# onclick='callPurchaseReq()' wtf:qtip='" + messageSource.getMessage("acc.WI.52", null, RequestContextUtils.getLocale(request)) + "'>");
            finalStr.append("<area shape='rect' coords='14,328,88,424' href=# onclick='callAgedPayable({\"withinventory\":true})' wtf:qtip='" + messageSource.getMessage("acc.WI.40", null, RequestContextUtils.getLocale(request)) + "'>");   //Keep a track record of all amount payables.'> ");
            finalStr.append("<area shape='rect' coords='143,279,255,360' href=# onclick='callPurchaseInvoiceType()' wtf:qtip='" + messageSource.getMessage("acc.WI.39", null, RequestContextUtils.getLocale(request)) + "'>");   //Provide your vendors with receipt on delivery of purchased goods. Record product and payment details.'> ");
            finalStr.append("<area shape='rect' coords='281,285,364,372' href=# onclick='callGoodsReceiptDelivery(false,null,null)' wtf:qtip='" + messageSource.getMessage("acc.WI.47", null, RequestContextUtils.getLocale(request)) + "'>");   //Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc.'>");
            finalStr.append("<area shape='rect' coords='283,396,364,486' href=# onclick='callSalesReturnWindow(false)' wtf:qtip='" + messageSource.getMessage("acc.WI.50", null, RequestContextUtils.getLocale(request)) + "'>");   //Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc.'>");
            finalStr.append("<area shape='rect' coords='283,488,364,578' href=# onclick='callCreditNote(false)' wtf:qtip='" + messageSource.getMessage("acc.WI.38", null, RequestContextUtils.getLocale(request)) + "'>");   //Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc.'>");
            finalStr.append("<area shape='rect' coords='14,328,88,424' href=# onclick='callAgedPayable({\"withinventory\":true})' wtf:qtip='Keep a track record of all amount payables.'> ");
            if (Constants.isNewPaymentStructure) {  // Used for redirecting the request either to Old Payment structure  or Newly designed structure
                finalStr.append("<area shape='rect' coords='159,376,243,462' href=# onclick='callPaymentNew()' wtf:qtip='" + messageSource.getMessage("acc.WI.37", null, RequestContextUtils.getLocale(request)) + "'>");//Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
            } else {
                finalStr.append("<area shape='rect' coords='159,376,243,462' href=# onclick='callPayment()' wtf:qtip='" + messageSource.getMessage("acc.WI.37", null, RequestContextUtils.getLocale(request)) + "'>");
            }
            finalStr.append("</map>");

            
            //Map for Sales Management
            finalStr.append("<map name='AlienAreas2'>");
            finalStr.append("<area shape='rect' coords='26,59,117,136' href=# onclick='callBusinessContactWindow(false, null, null, true)' wtf:qtip='" + messageSource.getMessage("acc.WI.36", null, RequestContextUtils.getLocale(request)) + "'>");   //Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.'>");
            finalStr.append("<area shape='rect' coords='206,137,123,60' href=# onclick='callProductDetails(null,true)' wtf:qtip='" + messageSource.getMessage("acc.WI.35", null, RequestContextUtils.getLocale(request)) + "'>");   //Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product.'>");
            finalStr.append("<area shape='rect' coords='3,189,96,267' href=# onclick='callSalesReceipt(false,null)' wtf:qtip='" + messageSource.getMessage("acc.WI.34", null, RequestContextUtils.getLocale(request)) + "'>");   //Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale.'>");
            finalStr.append("<area shape='rect' coords='137,190,236,264' href=# onclick='callSalesOrder(false, null)' wtf:qtip='" + messageSource.getMessage("acc.WI.33", null, RequestContextUtils.getLocale(request)) + "'>");   //Record all details related to a customer purchase order by generating an associated sales order.'>");
            finalStr.append("<area shape='rect' coords='267,181,353,279' href=# onclick='callQuotation()' wtf:qtip=" + messageSource.getMessage("acc.WI.28", null, RequestContextUtils.getLocale(request)) + "'>");   //\"Generate Quotation's related to your Customer's requirements. You can use this to give your Customer's a rough idea of financial costings for their requirements.\">");
            finalStr.append("<area shape='rect' coords='2,433,80,519' href=# onclick='callAgedRecievable({\"withinventory\":true})' wtf:qtip='" + messageSource.getMessage("acc.WI.32", null, RequestContextUtils.getLocale(request)) + "'>");   //Keep a track record of all amount receivables.'>");
            finalStr.append("<area shape='rect' coords='134,282,240,367' href=# onclick='callContractOrderReport(undefined,true,true,undefined)' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.5", null, RequestContextUtils.getLocale(request)) + "'>");   //\"Generate Quotation's related to your Customer's requirements. You can use this to give your Customer's a rough idea of financial costings for their requirements.\">");
            finalStr.append("<area shape='rect' coords='135,379,246,463' href=# onclick='callInvoice(false,null)' wtf:qtip='" + messageSource.getMessage("acc.WI.31", null, RequestContextUtils.getLocale(request)) + "'>");   //Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount.'>");
            finalStr.append("<area shape='rect' coords='274,710,351,800' href=# onclick='callCreditNote(true)' wtf:qtip='" + messageSource.getMessage("acc.WI.30", null, RequestContextUtils.getLocale(request)) + "'>");   //If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases.'>");
            finalStr.append("<area shape='rect' coords='273,603,351,696' href=# onclick='callSalesReturnWindow(true)' wtf:qtip='" + messageSource.getMessage("acc.WI.53", null, RequestContextUtils.getLocale(request)) + "'>");   //If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases.'>");
            finalStr.append("<area shape='rect' coords='272,382,352,475' href=# onclick='callDeliveryOrder(false, null)' wtf:qtip='" + messageSource.getMessage("acc.WI.45", null, RequestContextUtils.getLocale(request)) + "'>");   //If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases.'>");
            if (Constants.isNewPaymentStructure) {
                finalStr.append("<area shape='rect' coords='141,478,239,560' href=# onclick='callReceiptNew()' wtf:qtip='" + messageSource.getMessage("acc.WI.29", null, RequestContextUtils.getLocale(request)) + "'>");   //Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
            } else {
                finalStr.append("<area shape='rect' coords='141,478,239,560' href=# onclick='callReceipt()' wtf:qtip='" + messageSource.getMessage("acc.WI.29", null, RequestContextUtils.getLocale(request)) + "'>");   //Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");                
            }
            finalStr.append("<area shape='rect' coords='362,69,437,164' href=# onclick='callMRPWorkOrderReport(\"MRPWorkOrderReportEntry\",false)' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.1", null, RequestContextUtils.getLocale(request)) + "'>");   //Record all detail starting from inventory component availability , production and quality to be carried out against products.
            finalStr.append("<area shape='rect' coords='360,177,436,271' href=# onclick='callRoutingMasterList()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.2", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='361,282,437,368' href=# onclick='callTaskProgressList()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.3", null, RequestContextUtils.getLocale(request)) + "'>");
            finalStr.append("<area shape='rect' coords='364,382,440,472' href=# onclick='MRPQualityCOntrolReport()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.4", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='271,494,350,587' href=# onclick='getDeliveryPlannerTabView(2)' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.6", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("</map>");
            
            
            //Map for Financials Reports.
            finalStr.append("<map name='AlienAreas3'>");
            finalStr.append("<area shape='rect' coords='23,33,118,125' href=# onclick='callFinalStatement()' wtf:qtip='" + messageSource.getMessage("acc.WI.27", null, RequestContextUtils.getLocale(request)) + "'>");   //Track all major financial statements such as trial balance, ledger, trading and profit/loss statement and balance sheet.'> ");
            finalStr.append("<area shape='rect' coords='140,33,370,125' href=# onclick='javascript:void(0)' wtf:qtip='" + messageSource.getMessage("acc.WI.26", null, RequestContextUtils.getLocale(request)) + "'>");   //Track all the Transaction Records such as Journal Entry, Cash Sales, Cash Purchases, Customer and Vendor Invoices, Credit Note and Debit Note reports among others.'> ");
            finalStr.append("<area shape='rect' coords='140,136,248,227' href=# onclick='javascript:void(0)' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.10", null, RequestContextUtils.getLocale(request)) + "'>");   //Track all the Transaction Records such as Journal Entry, Cash Sales, Cash Purchases, Customer and Vendor Invoices, Credit Note and Debit Note reports among others.'> ");
            finalStr.append("<area shape='rect' coords='262,134,371,228' href=# onclick='javascript:void(0)' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.11", null, RequestContextUtils.getLocale(request)) + "'>");   //Track all the Transaction Records such as Journal Entry, Cash Sales, Cash Purchases, Customer and Vendor Invoices, Credit Note and Debit Note reports among others.'> ");
            finalStr.append("<area shape='rect' coords='39,142,119,232' href=# onclick='callJournalEntryDetails()' wtf:qtip='" + messageSource.getMessage("acc.WI.19", null, RequestContextUtils.getLocale(request)) + "'>");   //Track all journal entries transactions entered into the system.'>");
            finalStr.append("<area shape='rect' coords='39,235,116,313' href=# onclick='callGeneralLedger()' wtf:qtip='" + messageSource.getMessage("acc.WI.24", null, RequestContextUtils.getLocale(request)) + "'>");   //Track all major financial statements such as ledger.'> ");
            finalStr.append("<area shape='rect' coords='43,323,115,404' href=# onclick='TrialBalance()' wtf:qtip='" + messageSource.getMessage("acc.WI.25", null, RequestContextUtils.getLocale(request)) + "'>");   //Track all major financial statements such as trial balance.'> ");
            finalStr.append("<area shape='rect' coords='35,409,123,497' href=# onclick='NewTradingProfitLoss()' wtf:qtip='" + messageSource.getMessage("acc.WI.23", null, RequestContextUtils.getLocale(request)) + "'>");   //Track all major financial statements such as  trading and profit/loss statement '> ");
            finalStr.append("<area shape='rect' coords='42,502,115,571' href=# onclick='periodViewBalanceSheet()' wtf:qtip='" + messageSource.getMessage("acc.WI.22", null, RequestContextUtils.getLocale(request)) + "'>");   //Track all major financial statements such as balance sheet.'> ");
            finalStr.append("<area shape='rect' coords='42,579,117,646' href=# onclick='callAgedPayable({\"withinventory\":true})' wtf:qtip='" + messageSource.getMessage("acc.WI.21", null, RequestContextUtils.getLocale(request)) + "'>");   //Keep a track record of all amount payables'> ");
            finalStr.append("<area shape='rect' coords='39,655,120,729' href=# onclick='callAgedRecievable({\"withinventory\":true})' wtf:qtip='" + messageSource.getMessage("acc.WI.20", null, RequestContextUtils.getLocale(request)) + "'>");   //Keep a track record of all amount receivables'>");
            finalStr.append("<area shape='rect' coords='41,738,119,845' href=# onclick='callRatioAnalysis()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.1", null, RequestContextUtils.getLocale(request))+"'>");  // Gives the summary view of the effect of account on each other.
            finalStr.append("<area shape='rect' coords='43,853,116,926' href=# onclick='callFrequentLedger(true,\"23\",\"" + messageSource.getMessage("acc.WoutI.7", null, RequestContextUtils.getLocale(request)) + "\",\"accountingbase cashbook\")' wtf:qtip='" + messageSource.getMessage("acc.WI.10", null, RequestContextUtils.getLocale(request)) + "'>");   //Monitor all cash transactions entered into the system for any time duration.'>");
            finalStr.append("<area shape='rect' coords='43,932,114,1003' href=# onclick='callFrequentLedger(false,\"9\",\"" + messageSource.getMessage("acc.WoutI.8", null, RequestContextUtils.getLocale(request)) + "\",\"accountingbase bankbook\")' wtf:qtip='" + messageSource.getMessage("acc.WI.9", null, RequestContextUtils.getLocale(request)) + "'>");   //Monitor all transactions for a bank account for any time duration.'>");
            if (extrapref != null && extrapref.isDeliveryPlanner()) {
                finalStr.append("<area shape='rect' coords='35,1010,121,1082' href=# onclick='getDeliveryPlannerTabView()' wtf:qtip='" + messageSource.getMessage("acc.filed.viewdeliveryplantp", null, RequestContextUtils.getLocale(request)) + "'>");   //"Delivery Planner
            }
            if (!pref.isWithoutTax1099()) {
                finalStr.append("<area shape='rect' coords='35,1010,121,1082' href=# onclick='call1099Report()' wtf:qtip='" + messageSource.getMessage("acc.WI.7", null, RequestContextUtils.getLocale(request)) + "'>");   //Tax 1099 Accounts.'>");
            }
            
            //Mapping Purchase Reports
            finalStr.append("<area shape='rect' coords='158,262,239,350' href=# onclick='callPurchaseReqList()'wtf:qtip='" + messageSource.getMessage("acc.field.ViewcompletelistofPurchaseRequisitionsassociatedwithyourvendors", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete list of Quotations associated with your customers.'>");
            finalStr.append("<area shape='rect' coords='160,354,236,438' href=# onclick='callVendorQuotationList()'wtf:qtip='" + messageSource.getMessage("acc.field.ViewcompletelistofQuotationsassociatedwithyourvendors", null, RequestContextUtils.getLocale(request)) + "'>");
            finalStr.append("<area shape='rect' coords='159,442,234,526' href=# onclick='callPurchaseOrderList()' wtf:qtip='" + messageSource.getMessage("acc.WI.17", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete list of purchase orders issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a purchase order from the given list.'>");
            finalStr.append("<area shape='rect' coords='154,530,233,628' href=# onclick='callGoodsReceiptList()' wtf:qtip='" + messageSource.getMessage("acc.WI.13", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete details of vendor invoice and cash purchase receipt(s) to your vendors. Export the list in convenient formats or get a quick view by easily expanding a vendor invoice from the given list.'>");
            if (Constants.isNewPaymentStructure) {  // Used for redirecting the request either to Old Payment structure  or Newly designed structure
                finalStr.append("<area shape='rect' coords='158,635,233,717' href=# onclick='callPaymentReportNew()' wtf:qtip='" + messageSource.getMessage("acc.WI.11", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete details of payments made to your vendors. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
            } else {
                finalStr.append("<area shape='rect' coords='158,635,233,717' href=# onclick='callPaymentReport()' wtf:qtip='" + messageSource.getMessage("acc.WI.11", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete details of payments made to your vendors. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
            }
            finalStr.append("<area shape='rect' coords='159,728,236,811' href=# onclick='callDebitNoteDetails()' wtf:qtip='" + messageSource.getMessage("acc.WI.12", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete list of debit notes issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a debit note from the given list.'>");
            finalStr.append("<area shape='rect' coords='159,819,236,901' href=# onclick='callGoodsReceiptOrderList()' wtf:qtip='" + messageSource.getMessage("acc.WI.48", null, RequestContextUtils.getLocale(request)) + "'>");   //Monitor all cash transactions entered into the system for any time duration.'>");
            finalStr.append("<area shape='rect' coords='161,909,238,996' href=# onclick='callPurchaseReturnList()'wtf:qtip='" + messageSource.getMessage("acc.field.ViewcompletedetailsofPurchaseReturnfromyourVendors", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
            
            //Mapping Sales Reports
            finalStr.append("<area shape='rect' coords='278,259,359,349' href=# onclick='callQuotationList()' wtf:qtip='" + messageSource.getMessage("acc.WI.8", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete list of Quotations associated with your customers.'>");
            finalStr.append("<area shape='rect' coords='280,353,357,434' href=# onclick='callSalesOrderList()' wtf:qtip='" + messageSource.getMessage("acc.WI.16", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete list of sales order associated with your customers. Export the list in convenient formats or get a quick view by easily expanding a sales order from the given list.'>");
            finalStr.append("<area shape='rect' coords='281,443,367,523' href=# onclick='callInvoiceList()'  wtf:qtip='" + messageSource.getMessage("acc.WI.18", null, RequestContextUtils.getLocale(request)) + "'>");   //Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list.'>");
            if (Constants.isNewPaymentStructure) {
                finalStr.append("<area shape='rect' coords='282,533,362,616' href=# onclick='callReceiptReportNew()' wtf:qtip='" + messageSource.getMessage("acc.WI.14", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
            } else {
                finalStr.append("<area shape='rect' coords='282,533,362,616' href=# onclick='ReceiptReport()' wtf:qtip='" + messageSource.getMessage("acc.WI.14", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");                 
            }
            finalStr.append("<area shape='rect' coords='281,625,359,707' href=# onclick='callCreditNoteDetails()' wtf:qtip='" + messageSource.getMessage("acc.WI.15", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete list of credit notes issued to your customers. Export the list in convenient formats or get a quick view by easily expanding a credit note from the given list.'>");
            finalStr.append("<area shape='rect' coords='282,719,360,803' href=# onclick='callDeliveryOrderList()' wtf:qtip='" + messageSource.getMessage("acc.WI.46", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
            finalStr.append("<area shape='rect' coords='282,809,360,899' href=# onclick='callSalesReturnList()'wtf:qtip='" + messageSource.getMessage("acc.field.ViewcompletedetailsofSalesReturnfromyourcustomers", null, RequestContextUtils.getLocale(request)) + "'>");   //View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");
            finalStr.append("</map>");

            //Mapping Customer, Vendor and Customer Master
            finalStr.append("<map name='AlienAreas4'>");
            finalStr.append("<area shape='rect' coords='74,115,2,25' href=# onclick='callCustomerDetails(true)' wtf:qtip='" + messageSource.getMessage("acc.WI.6", null, RequestContextUtils.getLocale(request)) + "'>");   //Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.'>");
            finalStr.append("<area shape='rect' coords='89,25,171,118' href=# onclick='callVendorDetails(true)'  wtf:qtip='" + messageSource.getMessage("acc.WI.5", null, RequestContextUtils.getLocale(request)) + "'>");   //Maintain all information about your vendors including contact information, account details, preferred delivery mode and debit term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts.'>");
            finalStr.append("<area shape='rect' coords='182,25,257,118' href=# onclick='callProductDetails()' wtf:qtip='" + messageSource.getMessage("acc.WI.4", null, RequestContextUtils.getLocale(request)) + "'>");   //Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product.'> ");
            finalStr.append("</map>");

            //Mapping Account Management
            finalStr.append("<map name='AlienAreas5'>");
            finalStr.append("<area shape='rect' coords='78,113,2,21' href=# onclick='callJournalEntry()' wtf:qtip='" + messageSource.getMessage("acc.WI.3", null, RequestContextUtils.getLocale(request)) + "'>");   //Record miscellaneous transactions which have not been recorded in the application through customer/vendor transactions.'>");
            finalStr.append("<area shape='rect' coords='95,21,172,111' href=# onclick='callCOA()'  wtf:qtip='" + messageSource.getMessage("acc.WI.2", null, RequestContextUtils.getLocale(request)) + "'>");   //Maintain all your accounts including income, expense, bank accounts and more. You can also export the account list in convenient formats as well as add sub-accounts to existing accounts.'>");
            finalStr.append("</map>");
            
            //Mapping Contract and Master Management
            finalStr.append("<map name='AlienAreas6'>");
            finalStr.append("<area shape='rect' coords='7,23,94,108' href=# onclick='callContractMasterDetails(true)' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.12", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='104,24,182,109' href=# onclick='workCentreMaster(false)' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.13", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='195,22,267,109' href=# onclick='callMachineMasterList()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.14", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='7,118,84,197' href=# onclick='callLabourDetails()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.15", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='88,116,184,198' href=# onclick='callRoutingMasterList()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.16", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("</map>");
            
            //Mapping MRP Reports
            finalStr.append("<map name='AlienAreas7'>");
            finalStr.append("<area shape='rect' coords='7,36,117,127' href=# onclick='callMRPWorkOrderReport(\"MRPWorkOrderReportList\",true)'  wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.7", null, RequestContextUtils.getLocale(request)) + "'>");  
            finalStr.append("<area shape='rect' coords='8,136,117,226' href=# onclick='MRPQualityCOntrolReport()'  wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.8", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='8,236,116,325' href=# onclick='getDeliveryPlannerTabView(2)'  wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.9", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("</map>");
            
             //Mapping Inventory
            finalStr.append("<map name='AlienAreas8'>");
            finalStr.append("<area shape='rect' coords='4,19,72,110' href=# onclick='markoutallTab()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.17", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='88,18,165,109' href=# onclick='goodsOrderTab()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.18", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='182,18,242,106' href=# onclick='goodsIssueTab()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.19", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='5,117,75,207' href=# onclick='interStoreTransfers()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.20", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='96,117,167,207' href=# onclick='interLocationTransfers()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.21", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='186,116,244,207' href=# onclick='callCycleCountCalendar()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.25", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='4,215,68,302' href=# onclick='callCycleCountForm()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.24", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='95,212,157,301' href=# onclick='qaApprovalTab()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.22", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("<area shape='rect' coords='187,213,242,302' href=# onclick='stockRepaire()' wtf:qtip='" + messageSource.getMessage("acc.MRP.dashboard.23", null, RequestContextUtils.getLocale(request)) + "'>");   
            finalStr.append("</map>");

        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }

    private StringBuilder getDashBoardDataFlowWithoutInv(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalStr = new StringBuilder();
        try {
            String companyID = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            /*
                getDashboardWelcomeMsg(HttpServletRequest, int approvalPendingCount)
            */
            finalStr.append(getDashboardWelcomeMsg(request, 0));
            
            finalStr.append("<div class='firstflowlink' ><IMG class='thickBlackBorder' id = 'purchasemanagement2' SRC='../../images/"+getImageName("purchasemanagement2",companyID)+".jpg' usemap='#AlienAreas1'><IMG class='thickBlackBorderInside1' id='customerVendorManagement2' SRC='../../images/"+getImageName("customervendorwithoutinventory",companyID)+".jpg' usemap='#AlienAreas4'></div>");
            /*
                Original code for Ratio Analysis Report on dashboard has commented below. Next consecutive line is a temporary code. It does not contain Ratio Analysis Report
                link for Dashboard. Once re-designing of Ratio Analysis report gets completed then un-comment the below line & remove the next consecutive line.
            */
            finalStr.append("<div class='secondflowlink'><IMG class='thickBlackBorder' id='salesAndBillingManagement2' SRC='../../images/"+getImageName("salesmanagement2",companyID)+".jpg' usemap='#AlienAreas2'><IMG class='thickBlackBorderInside3' id='accountManagement2' SRC='../../images/"+getImageName("accountmanagement",companyID)+".jpg' usemap='#AlienAreas5'></div>");  //SDP-10113
            //finalStr.append("<div class='secondflowlink'><IMG class='thickBlackBorder' id='salesAndBillingManagement2' SRC='../../images/"+getImageName("salesmanagement2",companyID)+".jpg' usemap='#AlienAreas2'><IMG class='thickBlackBorderInside3' id='accountManagement2' SRC='../../images/"+getImageName("accountmanagement",companyID)+".jpg' usemap='#AlienAreas5'><IMG class='thickBlackBorderInside6' id='ratioAnalysis' SRC='../../images/"+getImageName("ratio-analysis-report",companyID)+".jpg' usemap='#AlienAreas6'></div>");
            
            if(pref.isWithoutTax1099())
                finalStr.append("<div class='thirdflowlink'><IMG class='thickBlackBorder' id='financialReports2' SRC='../../images/"+getImageName("financialreport11",companyID)+".jpg' usemap='#AlienAreas3'></div>");
            else
                finalStr.append("<div class='thirdflowlink'><IMG class='thickBlackBorder' id='financialReports2' SRC='../../images/"+getImageName("financialreport12",companyID)+".jpg' usemap='#AlienAreas3'></div>");
            finalStr.append("<map name='AlienAreas1'>" );
            finalStr.append("<area shape='rect' coords='111,74,171,154' href=# onclick='callBusinessContactWindow(false, null, null, false);'  wtf:qtip='"+messageSource.getMessage("acc.WoutI.39", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your vendors including contact information, account details, preferred delivery mode and debit term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts.'>");
            finalStr.append("<area shape='rect' coords='0,208,130,301' href=# onclick='callBillingPurchaseReceipt(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.38", null, RequestContextUtils.getLocale(request))+"'>");   //Create a cash purchase receipt to give to your vendors as a payment record, on paying full amount at the time of purchase.'> ");
            finalStr.append("<area shape='rect' coords='156,208,296,301' href=# onclick='callBillingPurchaseOrder(false, null)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.37", null, RequestContextUtils.getLocale(request))+"'>");   //Easily create purchase order for your vendors. Include debit term and complete purchase information.'> ");
            finalStr.append("<area shape='rect' coords='14,375,84,473' href=# onclick='callAgedPayable({\"withinventory\":false})' wtf:qtip='"+messageSource.getMessage("acc.WoutI.36", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount payables.'> ");
            finalStr.append("<area shape='rect' coords='150,319,287,408' href=# onclick='callBillingGoodsReceipt(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.35", null, RequestContextUtils.getLocale(request))+"'>");   //Provide your vendors with receipt on delivery of purchased goods. Record product and payment details.'> ");
            finalStr.append("<area shape='rect' coords='326,323,386,420' href=# onclick='callBillingDebitNote()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.34", null, RequestContextUtils.getLocale(request))+"'>");   //Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc.'>");
            finalStr.append("<area shape='rect' coords='170,429,270,509' href=# onclick='callBillingPayment()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.33", null, RequestContextUtils.getLocale(request))+"'>");   //Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
            finalStr.append("</map>");

            finalStr.append("<map name='AlienAreas2'>" );
            finalStr.append("<area shape='rect' coords='84,67,201,159' href=# onclick='callBusinessContactWindow(false, null, null, true)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.32", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.'>");
            finalStr.append("<area shape='rect' coords='10,218,110,298' href=# onclick='callBillingSalesReceipt(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.31", null, RequestContextUtils.getLocale(request))+"'>");   //Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale.'>");
            finalStr.append("<area shape='rect' coords='170,213,270,294' href=# onclick='callBillingSalesOrder(false, null)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.30", null, RequestContextUtils.getLocale(request))+"'>");   //Record all details related to a customer purchase order by generating an associated sales order.'>");
            finalStr.append("<area shape='rect' coords='14,375,84,465' href=# onclick='callAgedRecievable({\"withinventory\":false})' wtf:qtip='"+messageSource.getMessage("acc.WoutI.29", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount receivables'>");
            finalStr.append("<area shape='rect' coords='170,320,253,395' href=# onclick='callBillingInvoice(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.28", null, RequestContextUtils.getLocale(request))+"'>");   //Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount.'>");
            finalStr.append("<area shape='rect' coords='326,323,386,413' href=# onclick='callBillingCreditNote()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.27", null, RequestContextUtils.getLocale(request))+"'>");   //If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases.'>");
            finalStr.append("<area shape='rect' coords='170,429,270,509' href=# onclick='callBillingReceipt()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.26", null, RequestContextUtils.getLocale(request))+"'>");   //Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
            finalStr.append("</map>");

            finalStr.append("<map name='AlienAreas3'>" );

            finalStr.append("<area shape='rect' coords='15,34,129,139' href=# onclick='callFinalStatement()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.25", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as trial balance, ledger, trading and profit/loss statement and balance sheet.'> ");
            finalStr.append("<area shape='rect' coords='145,35,390,140' href=# onclick='javascript:void(0)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.24", null, RequestContextUtils.getLocale(request))+"'>");   //Track all the Transaction Records such as Journal Entry, Cash Sales, Cash Purchases, Customer and Vendor Invoices, Credit Note and Debit Note reports among others.'> ");
            finalStr.append("<area shape='rect' coords='36,157,116,243' href=# onclick='TrialBalance()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.23", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as trial balance.'> ");
            finalStr.append("<area shape='rect' coords='21,254,128,322' href=# onclick='callGeneralLedger()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.22", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as ledger.'> ");
            finalStr.append("<area shape='rect' coords='22,336,146,426' href=# onclick='NewTradingProfitLoss()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.21", null, RequestContextUtils.getLocale(request))+"'>");   //rack all major financial statements such as  trading and profit/loss statement.'> ");
            finalStr.append("<area shape='rect' coords='23,429,141,501' href=# onclick='periodViewBalanceSheet()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.20", null, RequestContextUtils.getLocale(request))+"'>");   //Track all major financial statements such as balance sheet.'> ");


            finalStr.append("<area shape='rect' coords='18,517,146,583' href=# onclick='callAgedPayable({\"withinventory\":false})' wtf:qtip='"+messageSource.getMessage("acc.WoutI.19", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount payables'> ");
            finalStr.append("<area shape='rect' coords='22,598,142,683' href=# onclick='callAgedRecievable({\"withinventory\":false})' wtf:qtip='"+messageSource.getMessage("acc.WoutI.18", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount receivables'>");

            finalStr.append("<area shape='rect' coords='169,146,275,257' href=# onclick='callJournalEntryDetails()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.17", null, RequestContextUtils.getLocale(request))+"'>");   //Track all journal entries transactions entered into the system.'>");
            finalStr.append("<area shape='rect' coords='162,263,271,352' href=# onclick='callBillingInvoiceList()'  wtf:qtip='"+messageSource.getMessage("acc.WoutI.16", null, RequestContextUtils.getLocale(request))+"'>");   //Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list.'>");
            finalStr.append("<area shape='rect' coords='158,367,273,451' href=# onclick='callBillingPurchaseOrderList()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.15", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of purchase orders issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a purchase order from the given list.'>");
            finalStr.append("<area shape='rect' coords='161,462,278,539' href=# onclick='callBillingSalesOrderList()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.14", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of sales order associated with your customers. Export the list in convenient formats or get a quick view by easily expanding a sales order from the given list.'>");
            finalStr.append("<area shape='rect' coords='162,541,267,632' href=# onclick='callBillingCreditNoteDetails()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.13", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of credit notes issued to your customers. Export the list in convenient formats or get a quick view by easily expanding a credit note from the given list.'>");
            finalStr.append("<area shape='rect' coords='162,643,287,723' href=# onclick='BillingReceiptReport()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.12", null, RequestContextUtils.getLocale(request))+"'>");   //View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.'>");

            finalStr.append("<area shape='rect' coords='294,147,394,254' href=# onclick='callBillingGoodsReceiptList()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.11", null, RequestContextUtils.getLocale(request))+"'>");   //View complete details of vendor invoice and cash purchase receipt(s) to your vendors. Export the list in convenient formats or get a quick view by easily expanding a vendor invoice from the given list.
            finalStr.append("<area shape='rect' coords='291,260,383,354' href=# onclick='callBillingDebitNoteDetails()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.10", null, RequestContextUtils.getLocale(request))+"'>");   //View complete list of debit notes issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a debit note from the given list.
            finalStr.append("<area shape='rect' coords='290,366,387,453' href=# onclick='callBillingPaymentReport()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.9", null, RequestContextUtils.getLocale(request))+"'>"); //View complete details of payments made to your vendors. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.
            finalStr.append("<area shape='rect' coords='293,461,389,534' href=# onclick='callFrequentLedger(true,\"23\",\""+messageSource.getMessage("acc.WoutI.8", null, RequestContextUtils.getLocale(request))+"\",\"accountingbase cashbook\")' wtf:qtip='"+messageSource.getMessage("acc.WoutI.8", null, RequestContextUtils.getLocale(request))+"'>");   //Monitor all cash transactions entered into the system for any time duration.
            finalStr.append("<area shape='rect' coords='291,549,389,617' href=# onclick='callFrequentLedger(false,\"9\",\""+messageSource.getMessage("acc.WoutI.7", null, RequestContextUtils.getLocale(request))+"\",\"accountingbase bankbook\")' wtf:qtip='"+messageSource.getMessage("acc.WoutI.7", null, RequestContextUtils.getLocale(request))+"'>");   //Monitor all transactions for a bank account for any time duration.
            if(!pref.isWithoutTax1099())
            {
                finalStr.append("<area shape='rect' coords='291,638,389,720' href=# onclick='call1099Report()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.6", null, RequestContextUtils.getLocale(request))+"'>");   //Tax 1099 Accounts.
            }
            finalStr.append("</map>");

            finalStr.append("<map name='AlienAreas4'>" );
            finalStr.append("<area shape='rect' coords='14,27,105,137' href=# onclick='callCustomerDetails(true)' wtf:qtip='"+messageSource.getMessage("acc.WoutI.5", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.
            finalStr.append("<area shape='rect' coords='120,35,205,136' href=# onclick='callVendorDetails(true)'  wtf:qtip='"+messageSource.getMessage("acc.WoutI.4", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your vendors including contact information, account details, preferred delivery mode and debit term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts.
            finalStr.append("</map>");


            finalStr.append("<map name='AlienAreas5'>" );
            finalStr.append("<area shape='rect' coords='10,32,97,125' href=# onclick='callJournalEntry()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.3", null, RequestContextUtils.getLocale(request))+"'>");   //Record miscellaneous transactions which have not been recorded in the application through customer/vendor transactions.
            finalStr.append("<area shape='rect' coords='109,41,190,131' href=# onclick='callCOA()'  wtf:qtip='"+messageSource.getMessage("acc.WoutI.2", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all your accounts including income, expense, bank accounts and more. You can also export the account list in convenient formats as well as add sub-accounts to existing accounts.
            finalStr.append("</map>");

            finalStr.append("<map name='AlienAreas6'>" );
            finalStr.append("<area shape='rect' coords='0,0,117,133' href=# onclick='callRatioAnalysis()' wtf:qtip='"+messageSource.getMessage("acc.WoutI.1", null, RequestContextUtils.getLocale(request))+"'>");  // Gives the summary view of the effect of account on each other.
            finalStr.append("</map>");
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }
    
    @Override
    public String getDashboardWelcomeMsg(HttpServletRequest request, int PendingApprovalCount) {
        StringBuilder msg = new StringBuilder();
        try {
            String userid = sessionHandlerImpl.getUserid(request);
            KwlReturnObject userPreferencesResult = accountingHandlerDAOobj.getObject(UserPreferences.class.getName(), userid);
            UserPreferences userPreferences = (UserPreferences) userPreferencesResult.getEntityList().get(0);
            boolean showNewToDeskeraWelcomeMsg = true;
            boolean showPendingApprovalWelcomeMsg = true;
            
            if(!StringUtil.isNullObject(userPreferences) && !StringUtil.isNullOrEmpty(userPreferences.getPreferencesJSON())) {
                JSONObject preferencesJson = new JSONObject(userPreferences.getPreferencesJSON());
                if(preferencesJson.has(Constants.showNewToDeskeraWelcomeMsg)) {
                    showNewToDeskeraWelcomeMsg = preferencesJson.getBoolean(Constants.showNewToDeskeraWelcomeMsg);
                }
                if(preferencesJson.has(Constants.showPendingApprovalWelcomeMsg)) {
                    showPendingApprovalWelcomeMsg = preferencesJson.optBoolean(Constants.showPendingApprovalWelcomeMsg);
                }
            }
            
            String alertIcon = "<div class='helpAlert'>"
                    + "<img src='../../images/alerticon.gif'>"
                    + "</div>";
            
            String newToDeskeraMsg = "<div class='helpHeader'>" 
                    + messageSource.getMessage("acc.WoutI.42", null, RequestContextUtils.getLocale(request)) 
                    + "</div>"
                    + "<div class='helpContent'>"
                    + "<a href='#' class='helplinks' style='color:#445566;' onclick='takeTour()'>" 
                    + messageSource.getMessage("acc.WoutI.40", null, RequestContextUtils.getLocale(request))
                    + "</a>"
                    + "&nbsp;&nbsp;"
                    + "<a class='helplinks' style='color:#445566;' href='#' onclick='closeWelcomeMsg()'>" 
                    + messageSource.getMessage("acc.WoutI.41", null, RequestContextUtils.getLocale(request)) 
                    + "</a>"
                    + "</div>";
            
            String pendingApprovalMsg = "<div id='approverlinkondashboard' class='pendingapproverlink'>"
                    + "<a href='#' class='helplinks' style=color:#445566;' onclick='callPendingApprovalsReport()'>"
                    + messageSource.getMessage("acc.WoutI.46", null, RequestContextUtils.getLocale(request))+""
                    + "</a>"
                    + "&nbsp;&nbsp;&nbsp;"
                    + "<a class='helplinks' style='color:#445566;' href='#' onclick='closePAMsg()'>"
                    + messageSource.getMessage("acc.WoutI.47", null, RequestContextUtils.getLocale(request))
                    + "</a>"
                    + "</div>";
            
            String doNotShowAgainCheckBox1 = "<div class='doNotShowCheckbox'>"
                    + "<input type='checkbox' id='doNotShowCheckBoxId'>"
                    + "</div>"
                    + "<div class='doNotShowText'>"
                    + "Do not show this messages again"
                    + "</div>"
                    + "<div class='cancelImg' onclick='closeWelcomeMsg()'></div>";
            
            String doNotShowAgainCheckBox2 = "<div class='doNotShowCheckbox'>"
                    + "<input type='checkbox' id='doNotShowCheckBoxPAId'>"
                    + "</div>"
                    + "<div class='doNotShowText'>"
                    + "Do not show this messages again"
                    + "</div>"
                    + "<div class='cancelImg' onclick='closePAMsg()'></div>";
            
            if(showNewToDeskeraWelcomeMsg) {
                if(PendingApprovalCount > 0 && showPendingApprovalWelcomeMsg) {
                    msg.append("<div id='dashhelp' class='outerHelp' >" 
                            + alertIcon
                            + "<div id='NewToDeskeraHelp'>"
                            + newToDeskeraMsg
                            + doNotShowAgainCheckBox1
                            + "</div>"
                            + "<div id='ViewPendingApprovalHelp'>"
                            + pendingApprovalMsg
                            + doNotShowAgainCheckBox2
                            + "</div>"
                            + "</div>"
                    );
                } else {
                    msg.append("<div id='dashhelp' class='outerHelp' >" 
                            + alertIcon
                            + "<div id='NewToDeskeraHelp'>"
                            + newToDeskeraMsg
                            + doNotShowAgainCheckBox1
                            + "</div>"
                            + "</div>"
                    );
                }
            } else if(showPendingApprovalWelcomeMsg){
                if(PendingApprovalCount > 0 && showPendingApprovalWelcomeMsg) {
                    msg.append("<div id='dashhelpPA' class='outerHelpPA' >" 
                            + alertIcon
                            + "<div id='ViewPendingApprovalHelp'>"
                            + pendingApprovalMsg
                            + doNotShowAgainCheckBox2
                            + "</div>"
                            + "</div>"
                    );
                }
            }
        
        } catch (Exception ex) {
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return msg.toString();
    }
    
    public ModelAndView getDashboardLinks(HttpServletRequest request, HttpServletResponse response) {
        String msg = "";
        //Session session=null;
        try {
            //session = HibernateUtil.getCurrentSession();
            msg = getPartnerLinks(request);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
//        finally {
//            HibernateUtil.closeSession(session);
//        }
        return new ModelAndView("jsonView_ex", "model", msg);
    }
    
    
    public String getPartnerLinks(HttpServletRequest request) throws ServiceException, SessionExpiredException{
        JSONObject jResult = new JSONObject();
        JSONObject appdata = new JSONObject();
        try {                       
//            String platformURL =  request.getAttribute("platformURL").toString();
            String platformURL = URLUtil.buildRestURL("platformURL");
            platformURL = platformURL + "company/partnerlink";                
            JSONObject jobj = new JSONObject();
            String companyid = request.getParameter("companyid");
            jobj.put("companyid",companyid);
            jobj.put("userid",sessionHandlerImplObj.getUserid(request));
            jobj.put("subdomain",URLUtil.getDomainName(request));
            jobj.put("appid", "3");
            KwlReturnObject companyresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyresult.getEntityList().get(0);
            appdata = apiCallHandlerService.restGetMethod(platformURL, jobj.toString());
//            if(company!=null && company.getSwitchpref()==1){
//                appdata = apiCallHandlerService.callApp(platformURL, jobj, companyid,"15");
//            }else{
//                appdata = apiCallHandlerService.callApp(platformURL, jobj, companyid,"14");
//            }
            getChildCompanyURLs(companyid, appdata);
            jResult.put("valid", true);
            jResult.put("success", true);
            jResult.put("data", appdata.toString());
        } catch (JSONException ex) {
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jResult.toString();
    }
    
    public void getChildCompanyURLs(String companyid, JSONObject jobj) {
        String childCompany_subdomain = "";
        String base_urlformat = ConfigReader.getinstance().get("base_urlformat");
        try {
            if(!StringUtil.isNullOrEmpty(base_urlformat)){
                JSONArray JArr = new JSONArray();
                List ll = companyDetailsDAOObj.getChildCompanies(companyid);
                Iterator itr = ll.iterator();
                while(itr.hasNext()) {                    
                    Object[] arr = (Object[]) itr.next();
                    childCompany_subdomain = arr[1].toString();
                    String appURL =  String.format(base_urlformat, childCompany_subdomain);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("appurlformat", appURL);
                    tempObj.put("appname", "Deskera Accounting");
                    tempObj.put("appname_subdomain", "Deskera Accounting - "+childCompany_subdomain);
                    tempObj.put("subdomain", childCompany_subdomain);
                    JArr.put(tempObj);
                }
                jobj.put("childapplist", JArr);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public ModelAndView getMaintainanceDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        //Session session=null;
        try{
            JSONArray jarr=null;
            //String platformURL=this.getServletContext().getInitParameter("platformURL");
            //session = HibernateUtil.getCurrentSession();
//            String platformURL = request.getAttribute("platformURL").toString();
            String accURL = StorageHandler.getAccURL();
            //String crmURL=this.getServletContext().getInitParameter("crmURL");
            String action = "9";
            String companyID = sessionHandlerImpl.getCompanyid(request);
            JSONObject userData = new JSONObject();
            userData.put("remoteapikey",StorageHandler.GetRemoteAPIKey());
            userData.put("companyid",sessionHandlerImpl.getCompanyid(request));
            userData.put("requesturl",accURL);
            
            String platformURL = URLUtil.buildRestURL(Constants.PLATFORM_URL);
            platformURL = platformURL + "company/maintenance";                    
            JSONObject resObj = apiCallHandlerService.restGetMethod(platformURL, userData.toString());
//            JSONObject resObj = apiCallHandlerService.callApp(platformURL, userData, companyID, action);
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                jarr=resObj.getJSONArray("data");
            }
            if (jarr!=null&&jarr.length()>0) {
                jobj.put("data", jarr);
                msg="Data fetched successfully";
                issuccess = true;
            } else {
                msg="Error occurred while fetching data ";
            }
        } catch (Exception ex){
            msg += " " + ex.getMessage();
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
               msg += " " + ex.getMessage();
            Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
            }
            //HibernateUtil.closeSession(session);
        }
        return new ModelAndView("jsonView","model", jobj.toString());
    }
    
    public String getImageName(String imageName, String companyid) {
        String reportFileName = imageName;
    	try {
    		KwlReturnObject companyresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyresult.getEntityList().get(0);
            
            if (company.getLanguage() != null && company.getLanguage().getId() != 1) {
	            if(company.getLanguage().getId() == 3)
	            	reportFileName = company.getLanguage().getId() + "/" + imageName + "_" + company.getLanguage().getId();
	            else
	            	reportFileName = imageName;
            } else 
            	reportFileName = imageName;
    	} catch (Exception ex) {
    		Logger.getLogger(accDashboardController.class.getName()).log(Level.SEVERE, null, ex);
    	}
		return reportFileName;
    }
    /**
     *
     * @param request
     * @param response
     * @return = Return True if User Time zone different than company
     * @throws SessionExpiredException
     * @throws ServiceException
     */
    public boolean checkCompanyAndUserTimezone(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        String userid = sessionHandlerImplObj.getUserid(request);
        String companyid = sessionHandlerImplObj.getCompanyid(request);
        KwlReturnObject uresult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
        User user = (User) uresult.getEntityList().get(0);
        /*
         Compare User Timezone and Company Timezone if same then return false other wise return true
         */
        if (user.getTimeZone() != null && user.getCompany().getTimeZone() != null && user.getTimeZone().getTimeZoneID() == user.getCompany().getTimeZone().getTimeZoneID()) {
            return false;
        } else {
            return true;
        }
    }
    /**
     *
     * @param requestParams
     * @return = Return JSONObject with data
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public JSONObject getPendingApprovalsForAllModulesJson(HashMap<String, Object> requestParams) throws ServiceException, JSONException {
        JSONArray jArr = new JSONArray();
        JSONObject jobj = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONObject commData = new JSONObject();
        int count = 0;
        Locale locale = null;
        if(requestParams.containsKey("locale")){
        locale = (Locale)requestParams.get("locale");
        }
        try {
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat userdf = (DateFormat) requestParams.get("userdf");
            String companyId = (String) requestParams.get("companyid");
            String userid = (String) requestParams.get("userid");
            String userFullName = (String) requestParams.get("userFullName");
            /*
            Sending Departmentid for department wise approval
            */
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
            User user = (User) userResult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(user.getDepartment())) {
                requestParams.put("userDepartment", user.getDepartment());
            }
            KwlReturnObject result = accountingDashboardDAOObj.getPendingApprovalDetails(requestParams);   // Get PendingApprovalDetails for all respective modules
            List<Object[]> list = result.getEntityList();
            count = result.getRecordTotalCount();

            // Column Model
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "date");
            jobjTemp.put("type", "date");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "billid");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "billno");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "jeno");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "module");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "moduleid");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "deleted");
            jarrRecords.put(jobjTemp);
            
            /**
             * As JS is dynamically Created adding customer key in record. ERP-38444
             */
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "customer");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("view.pendingapproval.DocumentNo", null, locale));
            jobjTemp.put("dataIndex", "billno");
            jobjTemp.put("width", 750);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header",messageSource.getMessage("view.pendingapproval.JournalEntryNo", null, locale));
            jobjTemp.put("dataIndex", "jeno");
            jobjTemp.put("width", 150);
            jobjTemp.put("hidden", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("view.pendingapproval.DocumentDate", null, locale));
            jobjTemp.put("dataIndex", "date");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 750);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("view.pendingapproval.Module", null, locale));
            jobjTemp.put("dataIndex", "module");
            jobjTemp.put("hidden", true);
            jarrColumns.put(jobjTemp);

            for (Object[] oj : list) {
                String billid = oj[0].toString();
                String deleteflag = oj[1].toString();
                if (deleteflag.endsWith("F")) {
                    deleteflag = "false";
                } else {
                    deleteflag = "true";
                }
                String billno = oj[2]!=null?oj[2].toString():"";
                String jeno = oj[3]!=null?oj[3].toString():"";
                String date = oj[4]!=null?oj[4].toString():"";
                String module = oj[5].toString();
                String moduleid = oj[6].toString();
                String socustomer = oj[7].toString();       //person ERP-38444
                JSONObject newJobj = new JSONObject();
                newJobj.put("billid", billid);
                newJobj.put("billno", billno);
                newJobj.put("jeno", jeno);
                newJobj.put("customer", socustomer);
                newJobj.put("deleted", Boolean.parseBoolean(deleteflag));
                if (!moduleid.equalsIgnoreCase("32") &&!moduleid.equalsIgnoreCase("90")&& !moduleid.equalsIgnoreCase("22") && !moduleid.equalsIgnoreCase("23") && !moduleid.equalsIgnoreCase("28") && !moduleid.equalsIgnoreCase("27") && !moduleid.equalsIgnoreCase("87") && !moduleid.equalsIgnoreCase("89")) {
                    newJobj.put("date", userdf.format(Long.parseLong(date)));
                } else {
                    newJobj.put("date", date);
                }
                newJobj.put("module", module);
                newJobj.put("moduleid", moduleid);
                if(moduleid.equalsIgnoreCase("10")){//when DN module 
                    newJobj=accDebitNoteService.getDebitNoteApprovalPendingJsonData(newJobj, billid, companyId, userid, userFullName);
                } else if( moduleid.equalsIgnoreCase("12")){//when CN module 
                    newJobj=accCreditNoteService.getCreditNoteApprovalPendingJsonData(newJobj, billid, companyId, userid, userFullName);
                }
                
                jArr.put(newJobj);
            }
            commData.put("success", true);
            commData.put("coldata", jArr);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", count);
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

            jobj.put("valid", true);
            jobj.put("data", commData);
        } catch (Exception ex) {
            Logger.getLogger(AccDashboardServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return jobj;
    }
    
    @Override
    public boolean saveUserPreferencesOptions(JSONObject paramObj) throws ServiceException,JSONException {
        boolean success = false;
        try {
            String userid = paramObj.optString("userid");
            UserPreferences userPreferences = null;

            if (!StringUtil.isNullOrEmpty(userid)) {
                KwlReturnObject userPreferencesResult = accountingHandlerDAOobj.getObject(UserPreferences.class.getName(), userid);
                userPreferences = (UserPreferences) userPreferencesResult.getEntityList().get(0);

                if (StringUtil.isNullObject(userPreferences)) {
                    userPreferences = new UserPreferences();
                    KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
                    userPreferences.setUser((User)userResult.getEntityList().get(0));
                }

                JSONObject preferencesJSON = null;
                if (!StringUtil.isNullOrEmpty(userPreferences.getPreferencesJSON())) {
                    preferencesJSON = new JSONObject(userPreferences.getPreferencesJSON());
                } else {
                    preferencesJSON = new JSONObject();
                }

                if (paramObj.has(Constants.showNewToDeskeraWelcomeMsg)) {
                    preferencesJSON.put(Constants.showNewToDeskeraWelcomeMsg, paramObj.getBoolean(Constants.showNewToDeskeraWelcomeMsg));
                }

                if (paramObj.has(Constants.showPendingApprovalWelcomeMsg)) {
                    preferencesJSON.put(Constants.showPendingApprovalWelcomeMsg, paramObj.getBoolean(Constants.showPendingApprovalWelcomeMsg));
                }
                
                if (paramObj.has(Constants.agedReceivableDateFilter)) {
                    preferencesJSON.put(Constants.agedReceivableDateFilter, paramObj.getInt(Constants.agedReceivableDateFilter));
                }

                if (paramObj.has(Constants.agedPayableDateFilter)) {
                    preferencesJSON.put(Constants.agedPayableDateFilter, paramObj.getInt(Constants.agedPayableDateFilter));
                }
                
                if (paramObj.has(Constants.agedReceivableInterval)) {
                    preferencesJSON.put(Constants.agedReceivableInterval, paramObj.getInt(Constants.agedReceivableInterval));
                }
                
                if (paramObj.has(Constants.agedReceivableNoOfInterval)) {
                    preferencesJSON.put(Constants.agedReceivableNoOfInterval, paramObj.getInt(Constants.agedReceivableNoOfInterval));
                }
                
                if (paramObj.has(Constants.agedPayableInterval)) {
                    preferencesJSON.put(Constants.agedPayableInterval, paramObj.getInt(Constants.agedPayableInterval));
                }
                
                if (paramObj.has(Constants.agedPayableNoOfInterval)) {
                    preferencesJSON.put(Constants.agedPayableNoOfInterval, paramObj.getInt(Constants.agedPayableNoOfInterval));
                }

                if (!StringUtil.isNullObject(preferencesJSON)) {
                    userPreferences.setPreferencesJSON(preferencesJSON.toString());
                }
                
                success = accountingDashboardDAOObj.saveUserPreferencesOptions(userPreferences);
            }
            
        } catch (Exception ex) {
            Logger.getLogger(AccDashboardServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }        
        return success;
    }
}
