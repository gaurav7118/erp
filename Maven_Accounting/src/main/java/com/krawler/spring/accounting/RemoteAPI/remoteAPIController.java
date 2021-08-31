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
package com.krawler.spring.accounting.RemoteAPI;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.BillingSalesOrder;
import com.krawler.hql.accounting.BillingSalesOrderDetail;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.InvoiceTermsSales;
import com.krawler.hql.accounting.QuotationTermMap;
import com.krawler.hql.accounting.RepeatedSalesOrder;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.SalesOrderDetail;
import com.krawler.hql.accounting.SalesOrderTermMap;
import com.krawler.hql.accounting.companypreferenceservice.AccCompanyPreferencesService;
import com.krawler.hql.accounting.currency.service.AccCurrencyService;
import com.krawler.hql.accounting.invoice.service.AccInvoiceModuleService;
import com.krawler.hql.accounting.journalentry.service.AccJournalEntryModuleService;
import com.krawler.hql.accounting.masteritems.service.AccMasterItemsService;
import com.krawler.hql.accounting.vendor.service.AccVendorService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.accountservice.AccAccountService;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.costCenter.service.AccCostCenterService;
import com.krawler.spring.accounting.currency.CurrencyContants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.service.AccCustomerService;
import com.krawler.spring.accounting.handler.*;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceHandler;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.iphone.iphoneAndAndroidController;
import com.krawler.spring.accounting.product.service.AccProductService;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.receipt.AccReceiptServiceDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderControllerCMN;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.tax.service.AccTaxService;
import com.krawler.spring.accounting.term.service.AccTermService;
import com.krawler.spring.accounting.uom.service.AccUomService;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
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
import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import com.krawler.esp.handlers.APICallHandlerService;
import java.text.ParseException;

public class remoteAPIController extends MultiActionController implements CurrencyContants {        
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
    private AccCustomerMainAccountingService accCustomerMainAccountingService;
    private AccUomService accUomService;
    private AccReceiptServiceDAO accReceiptServiceDAO;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accTaxDAO accTaxObj;
    private accCurrencyDAO accCurrencyobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;
    private AccJournalEntryModuleService accJournalEntryModuleService;
    private HibernateTransactionManager txnManager;
    private auditTrailDAO auditTrailObj;
    private accAccountDAO accAccountDAOobj;
    private APICallHandlerService apiCallHandlerService; 
    
    public void setAccJournalEntryModuleService(AccJournalEntryModuleService accJournalEntryModuleService) {
        this.accJournalEntryModuleService = accJournalEntryModuleService;
    }

    public void setAccReceiptServiceDAO(AccReceiptServiceDAO accReceiptServiceDAO) {
        this.accReceiptServiceDAO = accReceiptServiceDAO;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyobj = accCurrencyobj;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setaccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }
    public void setaccUomService(AccUomService accUomService) {
        this.accUomService = accUomService;
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
    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }
    
    public ModelAndView deskeraAccountingAPI(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SessionExpiredException, JSONException, ServiceException {
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
            if (!StringUtil.isNullOrEmpty(billdateinlong)) {
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
                    request.getParameter("cdomain");
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
                case 2:  //saving
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
                            model = saveJournalEntry(request,response);
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
                            result = URLEncoder.encode(result);
                            break;
                        case 9://send cost center's to eclaim
                            model = getCostCenter(request, response);
                            result = model.getModel().get("model").toString();
                            break;
//                        case 10://add/edit cost center's received from eclaim
//                            model = addOrEditCostCenter(request, response);
//                            result = model.getModel().get("model").toString();
//                            break;
//                        case 11://delete cost center's received from eclaim
//                            model = deleteCostCenter(request, response);
//                            result = model.getModel().get("model").toString();
//                            break;
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
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
            result = "{\"success\":\"false\",\"error\":\"Error occurred while retreiving data(" + ex.toString() + ")\",\"data\":[]}";
        }
        return new ModelAndView("jsonView_ex", "model", result);
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
                Logger.getLogger(remoteAPIController.class.getName()).log(Level.SEVERE, null, ex);
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
                if(!StringUtil.isNullOrEmpty(userID)){
                   userObj = (User) KwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.User", userID);  
                }
            }         
            if(userObj==null){
                String companyid=request.getParameter("companyid");
                Company userObj1 = (Company) KwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.Company", companyid);
                userID=userObj1.getCreator().getUserID();
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
        jobj = accJournalEntryModuleService.saveJournalEntry(request,response);
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
        } catch (ServiceException ex) {
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
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
            String ignorecustomers="true";
            String ignorevendors="true";
            if(StringUtil.isNullOrEmpty(request.getParameter("ignorecustomers"))){
                ignorecustomers=request.getParameter("ignorecustomers");
            }
            if(StringUtil.isNullOrEmpty(request.getParameter("ignorevendors"))){
                ignorevendors=request.getParameter("ignorevendors");
            }
            requestParams.put("ignorecustomers",ignorecustomers);
            requestParams.put("ignorevendors", ignorevendors);
            requestParams.put("nondeleted", "true");
            String currencyid=(String)sessionHandlerImpl.getCurrencyID(request);
            KWLCurrency currency = (KWLCurrency) KwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.KWLCurrency", currencyid);
            KwlReturnObject result = accAccountDAOobj.getAccountsForCombo(requestParams);
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object listObj = itr.next();
                Account account = (Account) listObj;
                JSONObject obj = new JSONObject();
                if(account.isActivate()){
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

            issuccess = true;
        } catch (Exception ex) {
            msg = "remoteAPIController.getAccounts:" + ex.getMessage();
            Logger.getLogger(remoteAPIController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", DataJArr);

            } catch (JSONException ex) {
                Logger.getLogger(remoteAPIController.class.getName()).log(Level.SEVERE, null, ex);
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
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            result = accSalesOrderDAOobj.getSalesOrdersMerged(requestParams);
            DataJArr = getSalesOrdersJsonMerged(request, result.getEntityList(), DataJArr);
            issuccess = true;
        } catch (ServiceException ex) {
            msg = "remoteAPIController.getSalesOrdersMerged:" + ex.getMessage();
            Logger.getLogger(remoteAPIController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "remoteAPIController.getSalesOrdersMerged:" + ex.getMessage();
            Logger.getLogger(remoteAPIController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", DataJArr);
                
            } catch (JSONException ex) {
                Logger.getLogger(remoteAPIController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString().replaceAll("%", "%25"));
    }

    public JSONArray getSalesOrdersJsonMerged(HttpServletRequest request, List list, JSONArray jArr) throws ServiceException {
//        JSONArray jArr = new JSONArray();
        try {
            String companyid=sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            boolean closeflag = request.getParameter("closeflag") != null ? true : false;
            boolean isLeaseSO = Boolean.FALSE.parseBoolean(request.getParameter("isLeaseFixedAsset"));
            boolean isConsignment = Boolean.FALSE.parseBoolean(request.getParameter("isConsignment"));
            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();

            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, isLeaseSO ? Constants.Acc_Lease_Order_ModuleId : Constants.Acc_Sales_Order_ModuleId, 0));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            //  HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap,customFieldMap,customDateFieldMap);
            boolean isOutstanding = request.getParameter("isOutstanding") != null ? Boolean.parseBoolean(request.getParameter("isOutstanding")) : false;
            boolean iscustomeridpresent=!StringUtil.isNullOrEmpty(request.getParameter("erpcustomerid"));
            String custid="";
            if(iscustomeridpresent)
            {
                custid=request.getParameter("erpcustomerid");
            }
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                //SalesOrder salesOrder=(SalesOrder)itr.next();
                Object[] oj = (Object[]) itr.next();
                String orderid = oj[0].toString();
                //Withoutinventory 0 for normal, 1 for billing
                boolean withoutinventory = Boolean.parseBoolean(oj[1].toString());
                if (withoutinventory) {
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(BillingSalesOrder.class.getName(), orderid);
                    BillingSalesOrder salesOrder = (BillingSalesOrder) objItr.getEntityList().get(0);

                    Customer customer = salesOrder.getCustomer();
                    KWLCurrency currency = null;
                    if (salesOrder.getCurrency() != null) {
                        currency = salesOrder.getCurrency();
                    } else {
                        currency = salesOrder.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : salesOrder.getCustomer().getAccount().getCurrency();
                    }
                    //currency = salesOrder.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : salesOrder.getCustomer().getAccount().getCurrency();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", salesOrder.getID());
                    obj.put("companyid", salesOrder.getCompany().getCompanyID());
                    obj.put("companyname", salesOrder.getCompany().getCompanyName());
                    obj.put("withoutinventory", withoutinventory);
                    obj.put("isOpeningBalanceTransaction", false);// no any opening balance PO creation takes place for without inv.
                    if (!customer.getID().equalsIgnoreCase(custid)) {
                        continue;
                    }
                    obj.put("personid", customer.getID());
                    obj.put("billno", salesOrder.getSalesOrderNumber());
                    obj.put("duedate", authHandler.getDateFormatter(request).format(salesOrder.getDueDate()));
                    obj.put("shipdate", salesOrder.getShipdate() == null ? "" : authHandler.getDateFormatter(request).format(salesOrder.getShipdate()));
                    obj.put("shipvia", salesOrder.getShipvia() == null ? "" : salesOrder.getShipvia());
                    obj.put("fob", salesOrder.getFob() == null ? "" : salesOrder.getFob());
                    obj.put("date", authHandler.getDateFormatter(request).format(salesOrder.getOrderDate()));
                    obj.put("isfavourite", salesOrder.isFavourite());
                    obj.put("billto", salesOrder.getBillTo());
                    obj.put("shipto", salesOrder.getShipTo());
                    obj.put("salesPerson", salesOrder.getSalesperson() == null ? null : salesOrder.getSalesperson().getID());
                    obj.put("salespersonname", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getValue());
                    obj.put("deleted", salesOrder.isDeleted());

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
//                    System.out.println(salesOrder.getSalesOrderNumber());
                    while (itrRow.hasNext()) {
                        BillingSalesOrderDetail sod = (BillingSalesOrderDetail) itrRow.next();
//                        amount += sod.getQuantity() * sod.getRate();
                        double rowTaxPercent = 0;
                        if (sod.getTax() != null) {
                            requestParams.put("transactiondate", salesOrder.getOrderDate());
                            requestParams.put("taxid", sod.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        }
//                        amount+=sod.getQuantity() *sod.getRate()*rowTaxPercent/100;

                        KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, sod.getRate(), currency.getCurrencyID(), salesOrder.getOrderDate(), 0);
                        double sorate = sod.getRate();//(Double) bAmt.getEntityList().get(0);

                        double quotationPrice = sod.getQuantity() * sorate;
                        if (sod.getDiscountispercent() == 1) {
                            discountPrice = (quotationPrice) - (quotationPrice * sod.getDiscount() / 100);
                        } else {
                            discountPrice = quotationPrice - sod.getDiscount();
                        }
                        
                        amount += discountPrice + sod.getRowTaxAmount();//amount += discountPrice + (discountPrice * rowTaxPercent/100);
                    }
                    if (salesOrder.getDiscount() != 0) {
                        if (salesOrder.isPerDiscount()) {
                            totalDiscount = amount * salesOrder.getDiscount() / 100;
                            amount = amount - totalDiscount;
                        } else {
                            amount = amount - salesOrder.getDiscount();
                            totalDiscount = salesOrder.getDiscount();
                        }
                        obj.put("discounttotal", salesOrder.getDiscount());
                    } else {
                        obj.put("discounttotal", 0);
                    }
                    obj.put("discount", totalDiscount);
                    obj.put("discountispertotal", salesOrder.isPerDiscount());
//                    obj.put("discountval", totalDiscount);
                    if (salesOrder.isPerDiscount()) {
                        obj.put("ispercentdiscount", salesOrder.isPerDiscount());
                        obj.put("discountval", salesOrder.getDiscount());
                    } else {
                        obj.put("discountval", totalDiscount);     //obj.put("discountval", salesOrder.getDiscount());  
                    }
                    double taxPercent = 0;
                    if (salesOrder.getTax() != null) {
                        requestParams.put("transactiondate", salesOrder.getOrderDate());
                        requestParams.put("taxid", salesOrder.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    }
                    double ordertaxamount = (taxPercent == 0 ? 0 : amount * taxPercent / 100);

    //                KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currency.getCurrencyID(), salesOrder.getOrderDate(), 0);
                    //                double orderAmount=(Double) bAmt.getEntityList().get(0);
                    obj.put("orderamountwithTax", amount + ordertaxamount);
                    obj.put("taxpercent", taxPercent);
                    obj.put("taxamount", ordertaxamount);
                    obj.put("amount", amount);
                    obj.put("orderamount", amount);
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("basecurrencysymbol", salesOrder.getCustomer().getAccount().getCurrency().getSymbol());
                    obj.put("taxid", salesOrder.getTax() == null ? "" : salesOrder.getTax().getID());
                    obj.put("taxname", salesOrder.getTax() == null ? "" : salesOrder.getTax().getName());
                    obj.put("currencyid", currency.getCurrencyID());
                    obj.put("personname", customer.getName());
                    HashMap<String, Object> addressParams = new HashMap<String, Object>();
                    addressParams.put("companyid", companyid);
                    addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                    addressParams.put("isBillingAddress", true);    //true to get billing address
                    addressParams.put("customerid", salesOrder.getCustomer().getID());
                    CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                    obj.put("personemail", customerAddressDetails!=null?customerAddressDetails.getEmailID():"");
                    //obj.put("creditoraccount", salesOrder.getCreditTo() == null ?"": salesOrder.getCreditTo().getID());
                    //obj.put("crdraccid", salesOrder.getCreditTo() == null ?"": salesOrder.getCreditTo().getID());
                    obj.put("memo", salesOrder.getMemo());
                    obj.put("posttext", salesOrder.getPostText());
                    obj.put("costcenterid", salesOrder.getCostcenter() == null ? "" : salesOrder.getCostcenter().getID());
                    obj.put("costcenterName", salesOrder.getCostcenter() == null ? "" : salesOrder.getCostcenter().getName());
                    obj.put("termamount", CommonFunctions.getTotalTermsAmount(getTermDetails(salesOrder.getID(), true)));
                    obj.put("archieve", 0);
                    boolean includeprotax = false;
                    Set<BillingSalesOrderDetail> billingSalesOrderDetails = salesOrder.getRows();
                    for (BillingSalesOrderDetail billingSalesOrderDetail : billingSalesOrderDetails) {
                        if (billingSalesOrderDetail.getTax() != null) {
                            includeprotax = true;
                            break;
                        }
                    }
                    obj.put("includeprotax", includeprotax);
                    String status = "open";//getBillingSalesOrderStatus(salesOrder);
//                    obj.put("status", status);

                    if (!closeflag || (closeflag && status.equalsIgnoreCase("open"))) {
                        jArr.put(obj);
                    }
                } else {
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), orderid);
                    SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
                    KWLCurrency currency = null;
                    if (salesOrder.getCurrency() != null) {
                        currency = salesOrder.getCurrency();
                    } else {
                        currency = salesOrder.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : salesOrder.getCustomer().getAccount().getCurrency();
                    }
                    //KWLCurrency currency=salesOrder.getCustomer().getAccount().getCurrency()==null?kwlcurrency:salesOrder.getCustomer().getAccount().getCurrency();
                    Customer customer = salesOrder.getCustomer();
                    JSONObject obj = new JSONObject();
                    if ( !customer.getID().equalsIgnoreCase(custid)) {
                        continue;
                    }
                    obj.put("billid", salesOrder.getID());
                    obj.put("companyid", salesOrder.getCompany().getCompanyID());
                    obj.put("companyname", salesOrder.getCompany().getCompanyName());
                    obj.put("externalcurrencyrate", salesOrder.getExternalCurrencyRate());
                    obj.put("withoutinventory", withoutinventory);
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
//                        amount+=sod.getQuantity()*sod.getRate();
                        double rowTaxPercent = 0;
                        if (sod.getTax() != null) {
                            requestParams.put("transactiondate", salesOrder.getOrderDate());
                            requestParams.put("taxid", sod.getTax().getID());
                            KwlReturnObject result = accTaxObj.getTax(requestParams);
                            List taxList = result.getEntityList();
                            Object[] taxObj = (Object[]) taxList.get(0);
                            rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        }
//                        amount+=sod.getQuantity() *sod.getRate()*rowTaxPercent/100;

//                         KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, sod.getRate(), currency.getCurrencyID(), salesOrder.getOrderDate(), 0);
                        double sorate = authHandler.roundUnitPrice(sod.getRate(), companyid);
                        double quantity = authHandler.roundQuantity(sod.getQuantity(), companyid);

                        double quotationPrice = authHandler.round(quantity * sorate, companyid);
                        double discountSOD = authHandler.round(sod.getDiscount(), companyid);

                        if (sod.getDiscountispercent() == 1) {
                            discountPrice = (quotationPrice) - authHandler.round((quotationPrice * discountSOD / 100), companyid);
                            rowDiscountAmt += authHandler.round((quotationPrice * discountSOD/100),companyid);
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
//                    obj.put("discount", totalDiscount);
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
                        InvoiceTermsSales mt = salesOrderTermMap.getTerm();
                        double termAmnt = salesOrderTermMap.getTermamount();
                        totalTermAmount += authHandler.round(termAmnt, companyid);
                    }
                    totalTermAmount = authHandler.round(totalTermAmount, companyid);

    //                    obj.put("orderamount", CompanyHandler.getBaseToCurrencyAmount(session,request,amount,currency.getCurrencyID(),salesOrder.getOrderDate()));
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
//                    double orderAmount=(Double) bAmt.getEntityList().get(0);
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
                    obj.put("personemail", customerAddressDetails!=null?customerAddressDetails.getEmailID():"");
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

                    DateFormat df = (DateFormat) requestParams.get("df");
                    Map<String, Object> variableMap = new HashMap<String, Object>();
//                    SalesOrderCustomData jeDetailCustom = (SalesOrderCustomData) salesOrder.getSoCustomData();
//                    replaceFieldMap = new HashMap<String, String>();
//                    if (jeDetailCustom != null) {
//                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
//                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
////                        String coldata = varEntry.getValue().toString();
////                        if (!StringUtil.isNullOrEmpty(coldata)) {
////                            obj.put(varEntry.getKey(), coldata);
////                        }
//                            String coldata = varEntry.getValue().toString();
//                            if (customFieldMap.containsKey(varEntry.getKey())) {
//                                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
//                                FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
//                                if (fieldComboData != null) {
//                                    obj.put(varEntry.getKey(), fieldComboData.getValue() != null ? fieldComboData.getValue() : "");
//                                }
//                            } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
//                                obj.put(varEntry.getKey(), df.format(Long.parseLong(coldata)));
//                            } else {
//                                if (!StringUtil.isNullOrEmpty(coldata)) {
//                                    obj.put(varEntry.getKey(), coldata);
//                                }
//                            }
//                        }
//                    }
                    if (isOutstanding && status.equalsIgnoreCase("open")) {
                        jArr.put(obj);
                    } else if (!isOutstanding) {
                        if (!closeflag || (closeflag && status.equalsIgnoreCase("open"))) {
                            jArr.put(obj);
                        }
                    }
                }

            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getSalesOrdersJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
//          public String getBillingSalesOrderStatus(BillingSalesOrder so) throws ServiceException {
//        Set<BillingSalesOrderDetail> orderDetail = so.getRows();
//        Iterator ite = orderDetail.iterator();
//        String result = "Closed";
//        while(ite.hasNext()){
//            BillingSalesOrderDetail sDetail = (BillingSalesOrderDetail)ite.next();
//            KwlReturnObject bidResult = accInvoiceDAOobj.getBIDFromBSOD(sDetail.getID());
//            Iterator ite1 = bidResult.getEntityList().iterator();
//            double qua = 0;
//            while(ite1.hasNext()){
//                BillingInvoiceDetail ge = (BillingInvoiceDetail)ite1.next();
//                qua += ge.getQuantity();
//            }
//            if(qua < sDetail.getQuantity()){
//                result = "Open";
//                break;
//            }
//        }
//        return result;
//    }

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
            Logger.getLogger(accSalesOrderControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }

    public HashMap<String, Object> getSalesOrdersMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        //checking filetype to print all records for csv,print & pdf
        if (StringUtil.isNullOrEmpty(request.getParameter("filetype"))) {
            requestParams.put(Constants.start, request.getParameter(Constants.start));
            requestParams.put(Constants.limit, request.getParameter(Constants.limit));
        }
        requestParams.put(CCConstants.REQ_costCenterId, request.getParameter(CCConstants.REQ_costCenterId));
        requestParams.put(Constants.REQ_customerId, request.getParameter(Constants.REQ_customerId));
        requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
        requestParams.put(Constants.MARKED_FAVOURITE, request.getParameter(Constants.MARKED_FAVOURITE));
        requestParams.put(InvoiceConstants.newcustomerid, request.getParameter(InvoiceConstants.newcustomerid));
        requestParams.put(InvoiceConstants.productid, request.getParameter(InvoiceConstants.productid));
        requestParams.put(InvoiceConstants.productCategoryid, request.getParameter(InvoiceConstants.productCategoryid));
        requestParams.put(Constants.isRepeatedFlag, request.getParameter(Constants.isRepeatedFlag));
        requestParams.put("deleted", request.getParameter("deleted"));
        requestParams.put("nondeleted", request.getParameter("nondeleted"));
        requestParams.put("orderforcontract", request.getParameter("orderForContract") != null ? Boolean.parseBoolean(request.getParameter("orderForContract")) : false);
        requestParams.put(Constants.ValidFlag, request.getParameter(Constants.ValidFlag));
        requestParams.put(Constants.BillDate, request.getParameter(Constants.BillDate));
        requestParams.put("pendingapproval", (request.getParameter("pendingapproval") != null) ? Boolean.parseBoolean(request.getParameter("pendingapproval")) : false);
        requestParams.put("istemplate", (request.getParameter("istemplate") != null) ? Integer.parseInt(request.getParameter("istemplate")) : 0);
        requestParams.put("currencyid", request.getParameter("currencyid"));
        requestParams.put("exceptFlagINV", request.getParameter("exceptFlagINV"));
        requestParams.put("exceptFlagORD", request.getParameter("exceptFlagORD"));
        requestParams.put("linkFlagInSO", request.getParameter("linkFlagInSO"));
        requestParams.put("linkFlagInInv", request.getParameter("linkFlagInInv"));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        requestParams.put("currencyfilterfortrans", (request.getParameter("currencyfilterfortrans") == null) ? "" : request.getParameter("currencyfilterfortrans"));
        requestParams.put("isOpeningBalanceOrder", request.getParameter("isOpeningBalanceOrder") != null ? Boolean.parseBoolean(request.getParameter("isOpeningBalanceOrder")) : false);
        requestParams.put("isLeaseFixedAsset", request.getParameter("isLeaseFixedAsset") != null ? Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")) : false);
        requestParams.put("isConsignment", request.getParameter("isConsignment") != null ? Boolean.parseBoolean(request.getParameter("isConsignment")) : false);
        requestParams.put("custWarehouse", (request.getParameter("custWarehouse") == null) ? "" : request.getParameter("custWarehouse"));
        requestParams.put(CCConstants.REQ_customerId, request.getParameter(CCConstants.REQ_customerId));
        requestParams.put(Constants.customerCategoryid, request.getParameter(Constants.customerCategoryid));
        requestParams.put("billId", request.getParameter("billid"));
        requestParams.put(Constants.checksoforcustomer, StringUtil.isNullOrEmpty(request.getParameter(Constants.checksoforcustomer)) ? false : Boolean.parseBoolean(request.getParameter(Constants.checksoforcustomer)));
        if (request.getParameter("includingGSTFilter") != null) {
            requestParams.put("includingGSTFilter", Boolean.parseBoolean(request.getParameter("includingGSTFilter")));
        }

        return requestParams;
    }

    public ModelAndView getInvoicesMerged(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        //    KwlReturnObject result = null;
        JSONArray DataJArr = new JSONArray();
        try {
            HashMap requestParams = AccInvoiceServiceHandler.getInvoiceRequestMap(request);
            requestParams.put("companyid", request.getParameter("companyid"));
            //  requestParams.put("gcurrencyid", gcurrencyid);
            KwlReturnObject result = accInvoiceDAOobj.getInvoicesMerged(requestParams);
            List list = result.getEntityList();
            DataJArr = accInvoiceServiceDAO.getInvoiceJsonMerged(request, list, DataJArr);
            DataJArr=filterJsonusingCustomer(DataJArr,request);
            issuccess = true;
        } catch (ServiceException ex) {
            msg = "remoteAPIController.getInvoicesMerged:" + ex.getMessage();
            Logger.getLogger(remoteAPIController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "remoteAPIController.getInvoicesMerged:" + ex.getMessage();
            Logger.getLogger(remoteAPIController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("data", DataJArr);
            } catch (JSONException ex) {
                Logger.getLogger(remoteAPIController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString().replaceAll("%", "%25"));
    }
    
    public JSONArray filterJsonusingCustomer(JSONArray array,HttpServletRequest request) throws JSONException{
        JSONArray DataJArr = new JSONArray();
        boolean iscustomeridpresent = !StringUtil.isNullOrEmpty(request.getParameter("erpcustomerid"));
        String custid = "";
        if (iscustomeridpresent) {
            custid = request.getParameter("erpcustomerid");
        }
        for(int i=0;i<array.length();i++){
            JSONObject jSONObjecta=array.getJSONObject(i);
            String personid=jSONObjecta.optString("personid","");
            if(custid.equalsIgnoreCase(personid)){
                DataJArr.put(jSONObjecta);
            }
            
        }
        return  DataJArr;
        
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
        Session session = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            String lmsURL = this.getServletContext().getInitParameter("lmsURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
            //session = HibernateUtil.getCurrentSession();
//            String action = "36";
            String lmsURL = URLUtil.buildRestURL("lmsURL");
            lmsURL = lmsURL + "financials/invoice";               
            resObj = apiCallHandlerService.restGetMethod(lmsURL, userData.toString());
//            try {
//                resObj = apiCallHandlerService.callApp(lmsURL, userData, companyid, action);
//            } catch (Exception ex) {
//            }
//            finally {
//               session.close();
//            }
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
                    } catch (Exception e) {
                        Logger.getLogger(remoteAPIController.class.getName()).log(Level.SEVERE, null, e);
                    }
                }

                // Call API To update invoice table at LMS side
                if (jarr.length() > 0) {
                    try {
                        //session = HibernateUtil.getCurrentSession();
//                        action = "37";
                        userData.put("invoicedata", array);
                        resObj = apiCallHandlerService.restPostMethod(lmsURL, userData.toString());
//                        JSONObject resObj1 = apiCallHandlerService.callApp(lmsURL, userData, companyid, action);
                    } catch (Exception ex) {
                    }
//                    finally {
//                        HibernateUtil.closeSession(session);
//                    }
                    if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                        issuccess = true;
                        status = txnManager.getTransaction(def);
                        auditTrailObj.insertAuditLog(AuditAction.INVOICE_CREATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has sync "+jarr.length()+" Invoices from LMS ", request,companyid);
                        txnManager.commit(status);
                    }
                }
            }
            msg = "Invoices Sync Successfully";
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(remoteAPIController.class.getName()).log(Level.SEVERE, null, ex);
            txnManager.rollback(status);
            isused = true;
            issuccess = false;
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("isused", isused);
            } catch (JSONException ex) {
                Logger.getLogger(remoteAPIController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getReceiptFromLMS(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException,ServiceException {
        HashMap<String, Object> requestParams = AccountingManager.getSyncAllRequestParams(request);
        requestParams.put("lmsURL", this.getServletContext().getInitParameter("lmsURL"));
        JSONObject jobj = new JSONObject();
        jobj = accReceiptServiceDAO.getReceiptFromLMS(requestParams);
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getCostCenter(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
//        jobj = accCostCenterService.getCostCenter(request, response);
        jobj = accMasterItemsService.getMasterItemsForEclaim(request);
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
//    public ModelAndView addOrEditCostCenter(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
//        JSONObject jobj = new JSONObject();
//        jobj = accMasterItemsService.addEditMasterItemsForEclaim(request);
//        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
//    }
//    
//    public ModelAndView deleteCostCenter(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException {
//        JSONObject jobj = new JSONObject();
//        jobj = accMasterItemsService.deleteMasterItemsForEclaim(request);
//        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
//    }
    
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
        } catch (Exception ex) {
            issuccess = false;
            txnManager.rollback(status);
            throw ServiceException.FAILURE("crmManager.insertAccProduct", ex);
        } finally {
            try {
                job.put("success", issuccess);
                job.put("companyexist", true);
            } catch (JSONException ex) {
                Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
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
            throw ServiceException.FAILURE("crmManager.insertAccProduct", ex);
        } finally {
            try {
                job.put("success", issuccess);
                job.put("companyexist", true);
            } catch (JSONException ex) {
                Logger.getLogger(iphoneAndAndroidController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", job.toString());
    }
}
