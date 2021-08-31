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
package com.krawler.spring.accounting.handler;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.ProjectActivity;
import com.krawler.common.admin.ProjectFeature;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.permissionHandler.permissionHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author krawler
 */
public class accWidgetDashboardController extends MultiActionController implements MessageSourceAware {
    
    private MessageSource messageSource;
    private AccountingDashboardDAO accountingDashboardDAO;
    private AccountingHandlerDAO accountHandlerDao;
    private accPurchaseOrderDAO accPurchaseOrderDao;
    private accSalesOrderDAO accSalesOrderDao;
    private accGoodsReceiptDAO accGoodsReceiptDao;
    private accInvoiceDAO accInvoiceDao;
    private accVendorDAO accVendorDao;
    private accCustomerDAO accCustomerDao;
    private accProductDAO accProductDao;
    private permissionHandlerDAO permissionHandlerdao;
    private accCurrencyDAO accCurrencyDAOobj;
    private AccDashboardService accDashboardService;
    
    public void setAccountingDashboardDAO(AccountingDashboardDAO accountingDashboardDAO) {
        this.accountingDashboardDAO = accountingDashboardDAO;
    }

    @Override
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }
      public void setaccountingHandlerDAO(AccountingHandlerDAO accountHandlerDao) {
        this.accountHandlerDao = accountHandlerDao;
    }
    
   public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderDao) {
        this.accPurchaseOrderDao = accPurchaseOrderDao;
    }
     
   public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDao) {
        this.accSalesOrderDao = accSalesOrderDao;
    }
   
   public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptDao) {
        this.accGoodsReceiptDao = accGoodsReceiptDao;
    }
   
   public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDao) {
        this.accInvoiceDao = accInvoiceDao;
    }
   
   public void setaccVendorDAO(accVendorDAO accVendorDao) {
        this.accVendorDao = accVendorDao;
    }
   
   public void setaccCustomerDAO(accCustomerDAO accCustomerDao) {
        this.accCustomerDao = accCustomerDao;
    }
   
   public void setaccProductDAO(accProductDAO accProductDao) {
        this.accProductDao= accProductDao;
    }
   
   public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
   
   public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerdao) {
        this.permissionHandlerdao = permissionHandlerdao;
    }
    public void setAccDashboardService(AccDashboardService accDashboardService) {
        this.accDashboardService = accDashboardService;
    }
    public ModelAndView getWidgetFrame(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject resultJson = new JSONObject();
        try {
            String userId = sessionHandlerImpl.getUserid(request);
            
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("userid", userId);
            String roleid = sessionHandlerImpl.getRole(request);
            requestParams.put("roleid", roleid);
            kmsg = accountingDashboardDAO.getWidgetStatus(requestParams);

            List<widgetManagement> widgets = kmsg.getEntityList();
            if (widgets != null && !widgets.isEmpty()) {
                widgetManagement wmObj = widgets.get(0);
                JSONObject empty = new JSONObject(wmObj.getWidgetstate());
                JSONArray jarr = new JSONArray("[" + empty + "]");
                List<String> widgetNames = new ArrayList<String>();
                StringBuffer widgetdata = new StringBuffer();

                if (jarr.length() > 0) {
                    JSONObject jobj1 = jarr.getJSONObject(0);
                    String colA = jobj1.getString("col1");
                    String colB = jobj1.getString("col2");
                    String colC = jobj1.getString("col3");
                    JSONArray jarrColA = new JSONArray(colA);
                    JSONArray jarrColB = new JSONArray(colB);
                    JSONArray jarrColC = new JSONArray(colC);

                    for (int a = 0; a < jarrColA.length(); a++) {
                        JSONObject jobjA = jarrColA.getJSONObject(a);

                        boolean nonCrmModuleFlag = NonCrmModuleFlag(jobjA); // Check Whether current portlet is Crm module or other portlet like Links,Report etc

                        if (nonCrmModuleFlag) {
                            widgetNames.add(jobjA.getString("id"));
                        } else {
                            widgetdata.append(jobjA.getString("id"));
                            widgetdata.append(",");
                        }

                    }

                    for (int b = 0; b < jarrColB.length(); b++) {
                        JSONObject jobjB = jarrColB.getJSONObject(b);

                        boolean nonCrmModuleFlag = NonCrmModuleFlag(jobjB); // Check Whether current portlet is Crm module or other portlet like Links,Report etc

                        if (nonCrmModuleFlag) {
                            widgetNames.add(jobjB.getString("id"));
                        } else {
                            widgetdata.append(jobjB.getString("id"));
                            widgetdata.append(",");
                        }
                    }

                    for (int c = 0; c < jarrColC.length(); c++) {
                        JSONObject jobjC = jarrColC.getJSONObject(c);

                        boolean nonCrmModuleFlag = NonCrmModuleFlag(jobjC); // Check Whether current portlet is Crm module or other portlet like Links,Report etc

                        if (nonCrmModuleFlag) {
                            widgetNames.add(jobjC.getString("id"));
                        } else {
                            widgetdata.append(jobjC.getString("id"));
                            widgetdata.append(",");
                        }
                    }
                }
                // get report widgets for logged in user
                ArrayList<String> filter_names = new ArrayList<String>();
                ArrayList<Object> filter_values = new ArrayList<Object>();
                filter_names.add("dc.dashboard");
                filter_values.add(1);
                filter_names.add("dc.userid");
                filter_values.add(userId);
                String widgetStr = widgetdata.toString();
                if (!StringUtil.isNullOrEmpty(widgetStr)) {
                    widgetStr = widgetStr.substring(0, (widgetStr.length() - 1));
                }
                resultJson.put("colLength", empty.toString());
                resultJson.put("widgetFrame", widgetStr);

                String widgetDataStr = getWidgetDataString(request, widgetNames);

                resultJson.put("widgetData", widgetDataStr);
            
            }
                resultJson.put("Timezone", accDashboardService.checkCompanyAndUserTimezone(request, response));
        } catch (JSONException ex) {
            logger.warn(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            logger.warn(ex.getMessage(), ex);
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", resultJson.toString());
    }

    public String getWidgetDataString(HttpServletRequest request, List<String> widgetNames) {
        JSONObject jobj = new JSONObject();
        try {
            int start = 0;
            int limit = 5;
            try {
                start = Integer.parseInt(request.getParameter("start"));
                limit = Integer.parseInt(request.getParameter("limit"));
            } catch (NumberFormatException e) {
            }

            String companyId = sessionHandlerImpl.getCompanyid(request);
            getColumnWiseWidget(request, widgetNames, null, jobj, start, limit, companyId);

        } catch (JSONException ex) {
            logger.warn(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            logger.warn(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            logger.warn(ex.getMessage(), ex);
        }

        return jobj.toString();
    }

    public boolean NonCrmModuleFlag(JSONObject jobj) {
        boolean flag = false;
        try {

            if (StringUtil.equal(jobj.getString("id"), "crmmodule_drag")) {
                flag = true;
            } else if (StringUtil.equal(jobj.getString("id"), "crmfacilityManagement")) {
                flag = true;
            } else if (StringUtil.equal(jobj.getString("id"), "DSBMyWorkspaces")) {
                flag = true;
            } else if (StringUtil.equal(jobj.getString("id"), "reports_drag")) {
                flag = true;
            } else if (StringUtil.equal(jobj.getString("id"), "marketing_drag")) {
                flag = true;
            } else if (StringUtil.equal(jobj.getString("id"), "crm_admin_widget")) {
                flag = true;
            } else if (StringUtil.equal(jobj.getString("id"), "DSBAdvanceSearch")) {
                flag = true;
            } else if (StringUtil.equal(jobj.getString("id"), "DSBAssignedCase")) {
                flag = true;
            } else if (StringUtil.equal(jobj.getString("id"), "DSBPendingCase")) {
                flag = true;
            } else if (StringUtil.equal(jobj.getString("id"), "campaign_reports_drag")) {
                flag = true;
            }

        } catch (JSONException ex) {
            logger.warn(ex.getMessage(), ex);
        }
        return flag;
    }

    private void getColumnWiseWidget(HttpServletRequest request, List<String> widgets, StringBuffer usersList, JSONObject jobj, int start, int limit, String companyId) throws ServiceException, JSONException, SessionExpiredException {
        if (widgets != null) {
            List<Integer> types = new ArrayList<Integer>();
            List<String> typeDef = new ArrayList<String>();

            // ERP dashboard update widgets
            for (String widget : widgets) {
                if (StringUtil.equal(widget, "purchasemgntwidget_drag")) {
                    types.add(5);
                    typeDef.add("Purchase Management");
                } else if (StringUtil.equal(widget, "salesbillingwidget_drag")) {
                    types.add(5);
                    typeDef.add("Sales and Billing Management");
                } else if (StringUtil.equal(widget, "financialstmtwidget_drag")) {
                    types.add(5);
                    typeDef.add("Financial Statement");
                } else if (StringUtil.equal(widget, "purchasetransactionreportwidget_drag")) {
                    types.add(5);
                    typeDef.add("Purchase Transaction Report");
                } else if (StringUtil.equal(widget, "salestransactionreportwidget_drag")) {
                    types.add(5);
                    typeDef.add("Sales Transaction Report");
                } else if (StringUtil.equal(widget, "masterswidget_drag")) {
                    types.add(5);
                    typeDef.add("Masters");
                }else if (StringUtil.equal(widget, "accountmgntwidget_drag")) {
                    types.add(5);
                    typeDef.add("Account Management");
                }else if (StringUtil.equal(widget, "adminwidget_drag")) {
                    types.add(5);
                    typeDef.add("Administration");
                } else if (StringUtil.equal(widget, "updateswidget_drag")) {
                    types.add(5);
                    typeDef.add("Updates");
                }
            }

            List<Map<String, Object>> widgetDataList = getDetailForWidget(request, usersList, types, start, limit, companyId);

            for (String widget : widgets) {
                JSONObject jobjColumn = null;
                if (StringUtil.equal(widget, "purchasemgntwidget_drag")) {
                    jobjColumn = getAccountingModuleWidget(request, jobj, "purchasemgntwidget_drag");

                } else if (StringUtil.equal(widget, "salesbillingwidget_drag")) {
                    jobjColumn = getAccountingModuleWidget(request, jobj, "salesbillingwidget_drag");

                } else if (StringUtil.equal(widget, "financialstmtwidget_drag")) {
                    jobjColumn = getAccountingModuleWidget(request, jobj, "financialstmtwidget_drag");

                } else if (StringUtil.equal(widget, "purchasetransactionreportwidget_drag")) {
                    jobjColumn = getAccountingModuleWidget(request, jobj, "purchasetransactionreportwidget_drag");

                }else if (StringUtil.equal(widget, "salestransactionreportwidget_drag")) {
                    jobjColumn = getAccountingModuleWidget(request, jobj, "salestransactionreportwidget_drag");

                } else if (StringUtil.equal(widget, "masterswidget_drag")) {
                    jobjColumn = getAccountingModuleWidget(request, jobj, "masterswidget_drag");

                } else if (StringUtil.equal(widget, "accountmgntwidget_drag")) {
                    jobjColumn = getAccountingModuleWidget(request, jobj, "accountmgntwidget_drag");

                } else if (StringUtil.equal(widget, "updateswidget_drag")) {
                    jobjColumn = getAccountingModuleWidget(request, jobj, "updateswidget_drag");
                }else if (StringUtil.equal(widget, "adminwidget_drag")) {
                    jobjColumn = getAccountingModuleWidget(request, jobj, "adminwidget_drag");

                }
            }
        }
    }

    public ModelAndView getWidgetData(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject resultJson = new JSONObject();
        try {
            String lst = request.getParameter("widgetFrame");
            List<String> widgetNames = Arrays.asList(lst.split(","));

            String widgetDataStr = getWidgetDataString(request, widgetNames);
            resultJson.put("widgetData", widgetDataStr);


        } catch (JSONException ex) {
            logger.warn(ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", resultJson.toString());
    }

    private JSONObject getAccountingModuleWidget(HttpServletRequest request, JSONObject jobj, String id) throws ServiceException, JSONException, SessionExpiredException {
        JSONObject tempobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        String userid = sessionHandlerImpl.getUserid(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        ExtraCompanyPreferences extracompanypreferences = null;
        try {
            KwlReturnObject cpresult = accountHandlerDao.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            extracompanypreferences = (ExtraCompanyPreferences) cpresult.getEntityList().get(0);
        } catch (Exception ex) {
            System.out.println("Exception while getting ExtraCompanyPreferences : " + ex.getMessage());
        }
        if (StringUtil.equal(id, "purchasemgntwidget_drag")) {
            JSONObject jobj1 = new JSONObject();
             tempobj = new JSONObject();
             StringBuilder temp= getPurchaseManagementModuleData(request);
             tempobj.put("update", temp.toString());
             jArr.put(tempobj);
             jobj1.put("data", jArr);
             jobj.put("purchasemgntwidget_drag", jobj1);

        } else if (StringUtil.equal(id, "salesbillingwidget_drag")) {

            JSONObject jobj2 = new JSONObject();
            tempobj = new JSONObject();
            tempobj = new JSONObject();
            StringBuilder temp=  getSalesBillingModuleData(request);
            tempobj.put("update", temp.toString());
            jArr.put(tempobj);           
            jobj2.put("data", jArr);
            jobj.put("salesbillingwidget_drag", jobj2);

        } else if (StringUtil.equal(id, "financialstmtwidget_drag")) {

            JSONObject jobj3 = new JSONObject();
            tempobj = new JSONObject();
            long permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Financial_Statements);
            if (((permCode & ProjectActivity.View_Trial_Balance) == ProjectActivity.View_Trial_Balance)||((permCode & ProjectActivity.View_Ledger) == ProjectActivity.View_Ledger)||((permCode & ProjectActivity.View_Trading_profitLoss) == ProjectActivity.View_Trading_profitLoss)||((permCode & ProjectActivity.View_Balance_Sheet) == ProjectActivity.View_Balance_Sheet)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.dashboardlinks.financialstmt.title1", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callFinalStatement()");
                tempobj.put("img", "../../images/FianancialStatement/Fianacial-Statement.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WoutI.25", null, RequestContextUtils.getLocale(request)));// Track all major financial statements such as trial balance, ledger, trading and profit/loss statement and balance sheet.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();

            if (((permCode & ProjectActivity.View_Trial_Balance) == ProjectActivity.View_Trial_Balance)) {
                tempobj.put("name", messageSource.getMessage("acc.trial.tabtitle", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "TrialBalance()");
                tempobj.put("img", "../../images/FianancialStatement/Trial-Balance.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WoutI.23", null, RequestContextUtils.getLocale(request)));// Track all major financial statements such as trial balance.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            if (((permCode & ProjectActivity.View_Ledger) == ProjectActivity.View_Ledger)) {
                tempobj.put("name", messageSource.getMessage("acc.het.147", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callGeneralLedger()");
                tempobj.put("img", "../../images/FianancialStatement/Ledger.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WoutI.22", null, RequestContextUtils.getLocale(request)));//Track all major financial statements such as ledger.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            if (((permCode & ProjectActivity.View_Trading_profitLoss) == ProjectActivity.View_Trading_profitLoss)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.TradingProfitLossStatement", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "NewTradingProfitLoss()");
                tempobj.put("img", "../../images/FianancialStatement/Accounts-Old-Management.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WoutI.21", null, RequestContextUtils.getLocale(request)));//Track all major financial statements such as  trading and profit/loss statement.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            if (((permCode & ProjectActivity.View_Balance_Sheet) == ProjectActivity.View_Balance_Sheet)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.consolidateBalanceSheetLink", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "periodViewBalanceSheet()");
                tempobj.put("img", "../../images/FianancialStatement/Balance-Sheet.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WoutI.20", null, RequestContextUtils.getLocale(request)));//Track all major financial statements such as balance sheet.
                jArr.put(tempobj);
            }
            if (extracompanypreferences != null && extracompanypreferences.isAccountpayableManagementFlag()) {
                tempobj = new JSONObject();
                permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Aged_Payable);
                if (((permCode & ProjectActivity.View_Aged_Payable) == ProjectActivity.View_Aged_Payable)) {
                    tempobj.put("name", messageSource.getMessage("acc.wtfTrans.agedp", null, RequestContextUtils.getLocale(request)));
                    tempobj.put("onclick", "callAgedPayable({\"withinventory\":true})");
                    tempobj.put("img", "../../images/FianancialStatement/Aged-Payble.png");
                    tempobj.put("qtip", messageSource.getMessage("acc.WoutI.36", null, RequestContextUtils.getLocale(request)));//Keep a track record of all amount payables.
                    jArr.put(tempobj);
                }
            }
            if (extracompanypreferences != null && extracompanypreferences.isAccountsreceivablesalesFlag()) {
                tempobj = new JSONObject();
                permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Aged_Receivable);
                if (((permCode & ProjectActivity.View_Aged_Receivable) == ProjectActivity.View_Aged_Receivable)) {
                    tempobj.put("name", messageSource.getMessage("acc.dashboard.AgedReceivable", null, RequestContextUtils.getLocale(request)));
                    tempobj.put("onclick", "callAgedRecievable({\"withinventory\":true})");
                    tempobj.put("img", "../../images/FianancialStatement/Aged-Receivables.png");
                    tempobj.put("qtip", messageSource.getMessage("acc.WoutI.29", null, RequestContextUtils.getLocale(request)));// Keep a track record of all amount receivables
                    jArr.put(tempobj);
                }
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Financial_Statements);
            if (((permCode & ProjectActivity.View_Cash_Book) == ProjectActivity.View_Cash_Book)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.consolidateCashBook", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callFrequentLedger(true,'23','Monitor all transactions for a bank account for any time duration.','accountingbase cashbook')");
                tempobj.put("img", "../../images/TransactionReport/Cash-Book.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WoutI.8", null, RequestContextUtils.getLocale(request)));//Monitor all cash transactions entered into the system for any time duration.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            if (((permCode & ProjectActivity.View_Bank_Book) == ProjectActivity.View_Bank_Book)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.consolidateBankBook", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callFrequentLedger(false,'9','Monitor all cash transactions entered into the system for any time duration.','accountingbase bankbook')");
                tempobj.put("img", "../../images/TransactionReport/Bank-Book.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WI.9", null, RequestContextUtils.getLocale(request)));//Monitor all transactions for a bank account for any time duration.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Quantative_Analysis);
            if (((permCode & ProjectActivity.View_Quantitative_Analysis) == ProjectActivity.View_Quantitative_Analysis)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.QuantitativeAnalysisofFinancialStatements", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callRatioAnalysis()");
                tempobj.put("img", "../../images/Master/Quantitative-Analysis-of-financial-Statements.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WoutI.1", null, RequestContextUtils.getLocale(request)));//Gives the summary view of the effect of account on each other.
                jArr.put(tempobj);
            }
//            tempobj = new JSONObject();
//            tempobj.put("name", messageSource.getMessage("acc.up.26", null, RequestContextUtils.getLocale(request)));   //This is applicable only for US specific countries therefore this part is commented
//            tempobj.put("onclick", "call1099Report()");
//            tempobj.put("img", "../../images/TransactionReport/Tax1099-Report.png");
//            tempobj.put("qtip", messageSource.getMessage("acc.field.ViewDetailsofTax1099Accounts", null, RequestContextUtils.getLocale(request)));//View Details of Tax 1099 Accounts.
//            jArr.put(tempobj);
            jobj3.put("data", jArr);
            jobj.put("financialstmtwidget_drag", jobj3);

        } else if (StringUtil.equal(id, "purchasetransactionreportwidget_drag")) {     // For Transaction Reports

            JSONObject jobj4 = new JSONObject();
            long permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Account_Management_and_Journal_Entry);
            if (((permCode & ProjectActivity.View_Journal_Entry) == ProjectActivity.View_Journal_Entry)) {
                tempobj = new JSONObject();
                tempobj.put("name", messageSource.getMessage("acc.dashboard.JournalEntryRecords", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callJournalEntryDetails()");
                tempobj.put("img", "../../images/TransactionReport/Journal-Entry.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WoutI.17", null, RequestContextUtils.getLocale(request)));// Track all journal entries transactions entered into the system.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Purchase_Management);
            if (((permCode & ProjectActivity.View_PurchaseInvoice_CashPurchase) == ProjectActivity.View_PurchaseInvoice_CashPurchase)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.VendorInvoicesCashPurchaseReport", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callGoodsReceiptList()");
                tempobj.put("img", "../../images/TransactionReport/Vendor-Invoice-&-CAsh-Purchase-Report.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WI.13", null, RequestContextUtils.getLocale(request)));//View complete details of vendor invoice and cash purchase receipt(s) to your vendors. Export the list in convenient formats or get a quick view by easily expanding a vendor invoice from the given list.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Debit_Note);
            if (((permCode & ProjectActivity.View_Debit_Note) == ProjectActivity.View_Debit_Note)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.consolidateDebitNoteReport", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callDebitNoteDetails()");
                tempobj.put("img", "../../images/TransactionReport/Debit-Note-Report.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WoutI.10", null, RequestContextUtils.getLocale(request)));//View complete list of debit notes issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a debit note from the given list.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Receive_Payment);
            if (((permCode & ProjectActivity.View_Payment_Made) == ProjectActivity.View_Payment_Made)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.consolidatePaymentsMadeReport", null, RequestContextUtils.getLocale(request)));
                if (Constants.isNewPaymentStructure) {
                    tempobj.put("onclick", "callPaymentReportNew()");
                } else {
                    tempobj.put("onclick", "callPaymentReport()");
                }
                tempobj.put("img", "../../images/TransactionReport/Payment-Made.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WI.11", null, RequestContextUtils.getLocale(request)));//View complete details of payments made to your vendors. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Purchase_Management);
            if (((permCode & ProjectActivity.View_Purchase_Order) == ProjectActivity.View_Purchase_Order)) {
                tempobj.put("name", messageSource.getMessage("acc.poList.tabTitle", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callPurchaseOrderList()");
                tempobj.put("img", "../../images/TransactionReport/Purchase-Order-Report.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WI.17", null, RequestContextUtils.getLocale(request)));// View complete list of purchase orders issued to your vendors. Export the list in convenient formats or get a quick view by easily expanding a purchase order from the given list.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Goods_Receipt_Reports);
            if (((permCode & ProjectActivity.View_Goods_Receipts) == ProjectActivity.View_Goods_Receipts)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.GoodReceiptReport", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callGoodsReceiptOrderList()");
                tempobj.put("img", "../../images/TransactionReport/Good-Receipt-reports.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WI.48", null, RequestContextUtils.getLocale(request)));//View complete details of goods receipt from your vendors. Export the list in convenient formats or get a quick view by easily expanding a goods receipt from the given list.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Purchase_Return_Reports);
            if (((permCode & ProjectActivity.View_Purchase_Return) == ProjectActivity.View_Purchase_Return)) {
                tempobj.put("name", messageSource.getMessage("acc.field.PurchaseReturnReport", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callPurchaseReturnList()");
                tempobj.put("img", "../../images/TransactionReport/Purchase-Return-Report.png");
                tempobj.put("qtip", messageSource.getMessage("acc.field.ViewcompletedetailsofPurchaseReturnfromyourVendors", null, RequestContextUtils.getLocale(request)));//View complete details of Purchase Return from your Vendors.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Purchase_Management);
            if (((permCode & ProjectActivity.View_Vendor_Quotation) == ProjectActivity.View_Vendor_Quotation)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.ViewVendorQuotations", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callVendorQuotationList()");
                tempobj.put("img", "../../images/FianancialStatement/View-Vendor-Quotes.png");
                tempobj.put("qtip", messageSource.getMessage("acc.field.ViewcompletelistofQuotationsassociatedwithyourvendors", null, RequestContextUtils.getLocale(request)));//View complete list of Quotations associated with your vendors.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Purchase_Requision);
            if (((permCode & ProjectActivity.View_Purchase_Requisition) == ProjectActivity.View_Purchase_Requisition)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.ViewPurchaseRequisitions", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callPurchaseReqList()");
                tempobj.put("img", "../../images/FianancialStatement/View-Purchase-Requisition.png");
                tempobj.put("qtip", messageSource.getMessage("acc.field.ViewcompletelistofPurchaseRequisitionsassociatedwithyourvendors", null, RequestContextUtils.getLocale(request)));//View complete list of Purchase Requisitions associated with your vendors.
                jArr.put(tempobj);
            }
            jobj4.put("data", jArr);
            jobj.put("purchasetransactionreportwidget_drag", jobj4);

        } else if (StringUtil.equal(id, "salestransactionreportwidget_drag")) {     // For Transaction Reports

            JSONObject jobj4 = new JSONObject();
            tempobj = new JSONObject();
            long permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Account_Management_and_Journal_Entry);
            if (((permCode & ProjectActivity.View_Journal_Entry) == ProjectActivity.View_Journal_Entry)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.JournalEntryRecords", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callJournalEntryDetails()");
                tempobj.put("img", "../../images/TransactionReport/Journal-Entry.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WoutI.17", null, RequestContextUtils.getLocale(request)));// Track all journal entries transactions entered into the system.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Sales_Management);
            if (((permCode & ProjectActivity.View_SalesInvoice_CashSales) == ProjectActivity.View_SalesInvoice_CashSales)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.CustomerInvoicesCashSalesReport", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callInvoiceList()");
                tempobj.put("img", "../../images/TransactionReport/Customer-Invoice-&-Cash-SAles-Report.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WI.18", null, RequestContextUtils.getLocale(request)));//Customers can view complete list of invoices and cash sales receipts issued. Export the list in convenient formats or get a quick view by easily expanding an invoice from the given list.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Credit_Note);
            if (((permCode & ProjectActivity.View_Credit_Note) == ProjectActivity.View_Credit_Note)) {
                tempobj.put("name", messageSource.getMessage("acc.cnList.tabTitle", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callCreditNoteDetails()");
                tempobj.put("img", "../../images/TransactionReport/Creadit-Note-Report.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WoutI.13", null, RequestContextUtils.getLocale(request)));//View complete list of credit notes issued to your customers. Export the list in convenient formats or get a quick view by easily expanding a credit note from the given list.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Make_Payment);
            if (((permCode & ProjectActivity.View_Payment_Received) == ProjectActivity.View_Payment_Received)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.consolidatePaymentsReceivedReport", null, RequestContextUtils.getLocale(request)));
                if (Constants.isNewPaymentStructure) {
                    tempobj.put("onclick", "callReceiptReportNew()");
                }
                tempobj.put("img", "../../images/TransactionReport/Payment-Received.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WI.14", null, RequestContextUtils.getLocale(request)));//View complete details of payments received from your customers. Export the list in convenient formats or get a quick view by easily expanding a payment detail from the given list.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Sales_Management);
            if (((permCode & ProjectActivity.View_Sales_Order) == ProjectActivity.View_Sales_Order)) {
                tempobj.put("name", messageSource.getMessage("acc.soList.tabTitle", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callSalesOrderList()");
                tempobj.put("img", "../../images/TransactionReport/Sales-Order-Report.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WoutI.14", null, RequestContextUtils.getLocale(request)));//View complete list of sales order associated with your customers. Export the list in convenient formats or get a quick view by easily expanding a sales order from the given list.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Delivery_Order_Reports);
            if (((permCode & ProjectActivity.View_Delivery_order) == ProjectActivity.View_Delivery_order)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.consolidateDeliveryOrderReport", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callDeliveryOrderList()");
                tempobj.put("img", "../../images/TransactionReport/Delivery-Order-Report.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WI.46", null, RequestContextUtils.getLocale(request)));//View complete details of delivery orders for your customers. Export the list in convenient formats or get a quick view by easily expanding a delivery order from the given list.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Sales_Return_Reports);
            if (((permCode & ProjectActivity.View_Sales_Return) == ProjectActivity.View_Sales_Return)) {
                tempobj.put("name", messageSource.getMessage("acc.field.SalesReturnReport", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callSalesReturnList()");
                tempobj.put("img", "../../images/TransactionReport/Sales-Return-Report.png");
                tempobj.put("qtip", messageSource.getMessage("acc.field.ViewcompletedetailsofSalesReturnfromyourcustomers", null, RequestContextUtils.getLocale(request)));//View complete details of Sales Return from your customers.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Sales_Management);
            if (((permCode & ProjectActivity.View_Customer_Quotation) == ProjectActivity.View_Customer_Quotation)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.ViewCustomerQuotations", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callQuotationList()");
                tempobj.put("img", "../../images/FianancialStatement/View-Customer-Quotes.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WI.8", null, RequestContextUtils.getLocale(request)));//View complete list of Quotations associated with your customers.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            if (extracompanypreferences != null && extracompanypreferences.isDeliveryPlanner()) {
                permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Delivery_Planner);
                if (((permCode & ProjectActivity.View_Delivery_Planner) == ProjectActivity.View_Delivery_Planner)) {
                    tempobj.put("name", messageSource.getMessage("acc.up.60", null, RequestContextUtils.getLocale(request)));
                    tempobj.put("onclick", "getDeliveryPlannerTabView()");
                    tempobj.put("img", "../../images/FianancialStatement/View-Delivey-Planner.png");
                    tempobj.put("qtip", messageSource.getMessage("acc.filed.viewdeliveryplantp", null, RequestContextUtils.getLocale(request)));//View complete list of Purchase Requisitions associated with your vendors.
                    jArr.put(tempobj);
                }
            }
            jobj4.put("data", jArr);
            jobj.put("salestransactionreportwidget_drag", jobj4);

        }else if (StringUtil.equal(id, "masterswidget_drag")) { 

            JSONObject jobj5 = new JSONObject();
            tempobj = new JSONObject();
            long permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Customer);
            if (((permCode & ProjectActivity.View_Customer) == ProjectActivity.View_Customer)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.CustomerManagement", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callCustomerDetails(true)");
                tempobj.put("img", "../../images/Master/Customer-Management.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WoutI.5", null, RequestContextUtils.getLocale(request)));// Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Vendor);
            if (((permCode & ProjectActivity.View_Vendor) == ProjectActivity.View_Vendor)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.VendorManagement", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callVendorDetails(true)");
                tempobj.put("img", "../../images/Master/Vendor-Management.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WoutI.4", null, RequestContextUtils.getLocale(request)));//Maintain all information about your vendors including contact information, account details, preferred delivery mode and debit term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Product);
            if (((permCode & ProjectActivity.View_Product) == ProjectActivity.View_Product)) {
                tempobj.put("name", messageSource.getMessage("acc.up.5", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callProductDetails()");
                tempobj.put("img", "../../images/Master/Product-Management.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WI.4", null, RequestContextUtils.getLocale(request)));//Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Master_Configuration);
            if (((permCode & ProjectActivity.View_Customdesign) == ProjectActivity.View_Customdesign)) {
                tempobj.put("name", messageSource.getMessage("acc.customdesignTT", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callCustomDesigner()");
                tempobj.put("img", "../../images/AccountManagement/Document-Designer.png");
                tempobj.put("qtip", messageSource.getMessage("acc.field.DesignCustomPDFdesignlayout", null, RequestContextUtils.getLocale(request)));//custome Designer
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            if (((permCode & ProjectActivity.View_Masterconfig) == ProjectActivity.View_Masterconfig)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.masterConfiguration", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callMasterConfiguration()");
                tempobj.put("img", "../../images/AccountManagement/Master-Configration.png");
                tempobj.put("qtip", messageSource.getMessage("acc.dashboard.TT.masterConfiguration", null, RequestContextUtils.getLocale(request)));//Master Configuration
                jArr.put(tempobj);
            }
            jobj5.put("data", jArr);
            jobj.put("masterswidget_drag", jobj5);

        } else if (StringUtil.equal(id, "accountmgntwidget_drag")) { 

            JSONObject jobj6 = new JSONObject();
            tempobj = new JSONObject();
            long permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Account_Management_and_Journal_Entry);
            if (((permCode & ProjectActivity.Create_Journal_Entry) == ProjectActivity.Create_Journal_Entry)) {
                tempobj.put("name", messageSource.getMessage("acc.nee.32", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callJournalEntry()");
                tempobj.put("img", "../../images/Master/Make-A-Journal-Entry.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WoutI.3", null, RequestContextUtils.getLocale(request)));// Record miscellaneous transactions which have not been recorded in the application through customer/vendor transactions.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            if (((permCode & ProjectActivity.View_Coa) == ProjectActivity.View_Coa)) {
                tempobj.put("name", messageSource.getMessage("acc.coa.tabTitle", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callCOA()");
                tempobj.put("img", "../../images/Master/Chart-Of-Accounts.png");
                tempobj.put("qtip", messageSource.getMessage("acc.WI.2", null, RequestContextUtils.getLocale(request)));//Maintain all your accounts including income, expense, bank accounts and more. You can also export the account list in convenient formats as well as add sub-accounts to existing accounts.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Bank_Reconciliation);
            if (((permCode & ProjectActivity.View_Reconciliation) == ProjectActivity.View_Reconciliation)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.bankReconciliation", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callReconciliationWindow()");
                tempobj.put("img", "../../images/AccountManagement/Bank-Reconciliation.png");
                tempobj.put("qtip", messageSource.getMessage("acc.dashboard.TT.bankReconciliation", null, RequestContextUtils.getLocale(request)));//Bank Reconsilition
                jArr.put(tempobj);
            }
            jobj6.put("data", jArr);
            jobj.put("accountmgntwidget_drag", jobj6);

        }else if (StringUtil.equal(id, "adminwidget_drag")) { 

            JSONObject jobj6 = new JSONObject();
            tempobj = new JSONObject();
            long permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.User_Administration);
            if (((permCode & ProjectActivity.View_User_Administration) == ProjectActivity.View_User_Administration)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.userAdministration", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "loadAdminPage(1)");
                tempobj.put("img", "../../images/Administration/Administrator.png");
                tempobj.put("qtip", messageSource.getMessage("acc.dashboard.TT.userAdministration", null, RequestContextUtils.getLocale(request)));// user Administration
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Audit_Trail);
            if (((permCode & ProjectActivity.View_Audit_Trail) == ProjectActivity.View_Audit_Trail)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.auditTrail", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callAuditTrail()");
                tempobj.put("img", "../../images/Administration/Audit-Trail.png");
                tempobj.put("qtip", messageSource.getMessage("acc.dashboard.auditTrail", null, RequestContextUtils.getLocale(request)));//Maintain all your accounts including income, expense, bank accounts and more. You can also export the account list in convenient formats as well as add sub-accounts to existing accounts.
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Import_Log);
            if (((permCode & ProjectActivity.View_Import_Log) == ProjectActivity.View_Import_Log)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.importLog", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callImportFilesLog()");
                tempobj.put("img", "../../images/Administration/Import-Log.png");
                tempobj.put("qtip", messageSource.getMessage("acc.dashboard.TT.importLog", null, RequestContextUtils.getLocale(request)));//Bank Reconsilition
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Master_Configuration);
            if (((permCode & ProjectActivity.View_CustomLayout) == ProjectActivity.View_CustomLayout)) {
                tempobj.put("name", messageSource.getMessage("acc.field.CustomLayouts", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callCustomLayoutGrid()");
                tempobj.put("img", "../../images/Administration/Custome-Layout.png");
                tempobj.put("qtip", messageSource.getMessage("acc.field.Createandviewcustomlayouts", null, RequestContextUtils.getLocale(request)));//Bank Reconsilition
                jArr.put(tempobj);
            }
            tempobj = new JSONObject();
            permCode = sessionHandlerImpl.getPerms(request, ProjectFeature.Account_Preferences);
            if (((permCode & ProjectActivity.View_AccountPref) == ProjectActivity.View_AccountPref)) {
                tempobj.put("name", messageSource.getMessage("acc.dashboard.accountPreferences", null, RequestContextUtils.getLocale(request)));
                tempobj.put("onclick", "callAccountPref()");
                tempobj.put("img", "../../images/Administration/Account-Preferences.png");
                tempobj.put("qtip", messageSource.getMessage("acc.dashboard.TT.accountPreferences", null, RequestContextUtils.getLocale(request)));//Bank Reconsilition
                jArr.put(tempobj);
            }
            jobj6.put("data", jArr);
            jobj.put("adminwidget_drag", jobj6);

        }else if (StringUtil.equal(id, "updateswidget_drag")) {
            JSONArray jArr1 = new JSONArray();
            JSONObject jobj7 = new JSONObject();
            JSONObject obj =null;
            tempobj = new JSONObject();
            JSONObject perms = new JSONObject();     
            String updateDiv="";
            KwlReturnObject kmsg = permissionHandlerdao.getActivityFeature();
            perms = permissionHandler.getAllPermissionJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("userid", userid);
            kmsg = permissionHandlerdao.getUserPermission(requestParams);
            perms = permissionHandler.getRolePermissionJson(kmsg.getEntityList(), perms);
            int i=0;
            StringBuilder temp3 = getCompanyAdminApprovals(request, perms);    
            String temp=temp3.toString();
           while(temp.indexOf("<div>")!=-1 &&temp.indexOf("<div style='clear:both;visibility:hidden;height:0;line-height:0;'>")!=-1) {           
		String title   = StringUtils.substringBetween(temp.toString(), "<div>", "<div style='clear:both;visibility:hidden;height:0;line-height:0;'>");
                obj=new JSONObject();
                obj.put("update",title);
                jArr1.put(obj);
                i+=temp.indexOf("<div style='clear:both;visibility:hidden;height:0;line-height:0;'>");
               temp=StringUtils.remove(temp, "<div>"+title+"<div style='clear:both;visibility:hidden;height:0;line-height:0;'>");
            }
            StringBuilder temp4=getCompanyAdminDashboardUpdateList(request, perms);
            String temp1=temp4.toString();
            int j=0;
            try{
            while( temp1.indexOf("<div>")!=-1 &&temp1.indexOf("<div style='clear:both;visibility:hidden;height:0;line-height:0;'>")!=-1) {           
		String title1 = StringUtils.substringBetween(temp1.toString(), "<div>", "<div style='clear:both;visibility:hidden;height:0;line-height:0;'>");
                obj=new JSONObject();
                obj.put("update", title1);
                jArr1.put(obj);
                j+=temp1.indexOf("<div style='clear:both;visibility:hidden;height:0;line-height:0;'>");
                temp1=StringUtils.remove(temp1, "<div>"+title1+"<div style='clear:both;visibility:hidden;height:0;line-height:0;'>");
            }   
            }catch(Exception e)
            {
                System.out.println(e);
            }
            int start=Integer.parseInt(request.getParameter("start1"));
            int limit=Integer.parseInt(request.getParameter("limit"));
            for(int k=start;k<(start+limit) &&k<(jArr1.length());k++){
                jArr.put(jArr1.get(k));
            }
            jobj7.put("data",jArr);
            jobj7.put("count",jArr1.length());
            jobj.put("updateswidget_drag", jobj7);
        }
        return jobj;
    }

      private String getContentSpan(String textStr) {
        String span = "<div>" + textStr + "<div style='clear:both;visibility:hidden;height:0;line-height:0;'></div></div>";
        return span;
    }
    
    private List<Map<String, Object>> getDetailForWidget(HttpServletRequest request, StringBuffer usersList, List<Integer> types, int start, int limit, String companyId) throws ServiceException, JSONException, SessionExpiredException {
        List<Map<String, Object>> requestParamsList = new ArrayList<Map<String, Object>>();
        int interval = 7;
        for (Integer type : types) {
            KwlReturnObject kmsg = null;
            boolean heirarchyPerm = false;
            Map<String, Object> requestParams = new HashMap<String, Object>();
            String grp = "";
            switch (type) {
                case 0:
                    grp = ProjectFeature.campaignFName; 
                    break;
                case 1:
                    grp = ProjectFeature.leadFName;
                    break;
                case 2:
                    grp = ProjectFeature.accountFName;
                    break;
                case 3:
                    grp = ProjectFeature.contactFName;
                    break;
                case 4:
                    grp = ProjectFeature.opportunityFName;
                    break;
                case 5:
                    grp = ProjectFeature.caseFName;
                    break;
                case 6:
                    grp = ProjectFeature.activityFName;
                    break;
                case 7:
                    grp = ProjectFeature.productFName;
                    break;
                case 8:
                    grp = ProjectFeature.activityFName;
                    break;
            }
            if (!heirarchyPerm) {
                requestParams.put("userslist", usersList);
            }
            requestParams.put("groups", grp);
            requestParams.put("start", start);
            requestParams.put("limit", limit);
            requestParams.put("type", type);
            requestParamsList.add(requestParams);
        }
        return requestParamsList;
    }

    public ModelAndView getAccountingModuleWidget(HttpServletRequest request, HttpServletResponse response) {
        JSONObject resultStr = new JSONObject();
        String id = request.getParameter("id");
        try {
            resultStr = getAccountingModuleWidget(request, resultStr, id);
        } catch (JSONException ex) {
            logger.warn(ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            logger.warn(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            logger.warn(ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", resultStr.toString());
        }
    
    public ModelAndView removeWidgetFromState(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject resultStr = new JSONObject();
        String wid = request.getParameter("wid");
        String userid = sessionHandlerImpl.getUserid(request);
        HashMap<String, Object> requestParams=new  HashMap();
        try {
            requestParams.put("wid",wid);
            requestParams.put("userid",userid);
            KwlReturnObject res=accountingDashboardDAO.removeWidgetFromState(requestParams);
            resultStr.put("result",res);
        } catch (JSONException ex) {
            logger.warn(ex.getMessage(), ex);
        }  catch (ServiceException ex) {
            logger.warn(ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", resultStr.toString());
   }
    
      public ModelAndView insertWidgetIntoState(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject resultStr = new JSONObject();
        String wid = request.getParameter("wid");
        String colno = request.getParameter("colno");
        String userid = sessionHandlerImpl.getUserid(request);
        HashMap<String, Object> requestParams=new  HashMap();
        try {
            requestParams.put("wid",wid);
            requestParams.put("colno",colno);
            requestParams.put("userid",userid);
            KwlReturnObject res=accountingDashboardDAO.insertWidgetIntoState(requestParams);
            resultStr.put("result",res);
        } catch (JSONException ex) {
            logger.warn(ex.getMessage(), ex);
        }  catch (ServiceException ex) {
            logger.warn(ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", resultStr.toString());
        }
      
       public ModelAndView changeWidgetState(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException {
        JSONObject resultStr = new JSONObject();
        String wid = request.getParameter("wid");
        String colno = request.getParameter("colno");
        String position=request.getParameter("position");
        String userid = sessionHandlerImpl.getUserid(request);
        HashMap<String, Object> requestParams=new  HashMap();
        try {
            requestParams.put("wid",wid);
            requestParams.put("colno",colno);
            requestParams.put("userid",userid);
            requestParams.put("position",position);
            KwlReturnObject res=accountingDashboardDAO.changeWidgetState(requestParams);
            resultStr.put("result",res);
        } catch (JSONException ex) {
            logger.warn(ex.getMessage(), ex);
        }  catch (ServiceException ex) {
            logger.warn(ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView", "model", resultStr.toString());
        }
         
        private StringBuilder getCompanyAdminApprovals(HttpServletRequest request, JSONObject perms) throws ServiceException {
            StringBuilder finalStr=new StringBuilder();
            try {
                String companyID = sessionHandlerImpl.getCompanyid(request);
                KwlReturnObject cpresult = accountHandlerDao.getObject(CompanyAccountPreferences.class.getName(), companyID);
                CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
                finalStr.append(joinArrayList(getApprovalInfo(companyID, perms,true,request), ""));            
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }
       
    private StringBuilder getUserDashboardUpdateList(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalStr = new StringBuilder();
        try {
            String companyID = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cpresult = accountHandlerDao.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            finalStr.append(joinArrayList(getVendorsUpdationInfo(companyID, perms, true,request), ""));
            finalStr.append(joinArrayList(getCustomersUpdationInfo(companyID, perms, true,request), ""));
            if (pref != null) {
                if (!pref.isWithoutInventory()) {
                    finalStr.append(joinArrayList(getProductsBelowROLInfo(request, companyID, perms, true), ""));
                }
            } else {
                finalStr.append(joinArrayList(getProductsBelowROLInfo(request, companyID, perms, true), ""));
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }
     
      public ArrayList getApprovalInfo(String companyID, JSONObject perms, Boolean isDashboard, HttpServletRequest request) throws ServiceException {
        try {
            ArrayList jArray=new ArrayList();
                
            /*Orders*/
            if((permissionHandler.isPermitted(perms, "approvals", "purchaseorderapprovelevelone")) || (permissionHandler.isPermitted(perms, "approvals", "purchaseorderapproveleveltwo"))){
                    int pendingCustInv = accPurchaseOrderDao.pendingApprovalOrdersCount(companyID);
                    if(pendingCustInv > 0){
                        String functionCall = "pendingOrdersTab(false)";
                        String link=getLink(pendingCustInv+" Purchase Order(s) ", functionCall);
                        jArray.add(getFormatedAlert(messageSource.getMessage("acc.field.Youhave", null, RequestContextUtils.getLocale(request))+" "+link+" "+messageSource.getMessage("acc.field.pendingforapproval", null, RequestContextUtils.getLocale(request)),"accountingbase updatemsg-vendor",isDashboard));
                    }
                    
            }    
                
            if((permissionHandler.isPermitted(perms, "approvals", "salesorderapprovelevelone")) || (permissionHandler.isPermitted(perms, "approvals", "salesorderapproveleveltwo"))){
                    int pendingCustInv = accSalesOrderDao.pendingApprovalOrdersCount(companyID);
                    if(pendingCustInv > 0){
                        String functionCall = "pendingOrdersTab(true)";
                        String link=getLink(pendingCustInv+" Sales Order(s) ", functionCall);
                        jArray.add(getFormatedAlert(messageSource.getMessage("acc.field.Youhave", null, RequestContextUtils.getLocale(request))+" "+link+" "+messageSource.getMessage("acc.field.pendingforapproval", null, RequestContextUtils.getLocale(request)),"accountingbase updatemsg-vendor",isDashboard));
                    }
                    
            }
            
            /*Invoices*/
            if((permissionHandler.isPermitted(perms, "approvals", "vendorinvoiceapprovelevelone")) || (permissionHandler.isPermitted(perms, "approvals", "vendorinvoiceapproveleveltwo"))){
                    int pendingCustInv = accGoodsReceiptDao.pendingApprovalInvoicesCount(companyID);
                    if(pendingCustInv > 0){
                        String functionCall = "pendingCustomerInvpicesTab(false)";
                        String link=getLink(pendingCustInv+" Purchase Invoice(s) ", functionCall);
                        jArray.add(getFormatedAlert(messageSource.getMessage("acc.field.Youhave", null, RequestContextUtils.getLocale(request))+" "+link+" "+messageSource.getMessage("acc.field.pendingforapproval", null, RequestContextUtils.getLocale(request)),"accountingbase updatemsg-vendor",isDashboard));
                    }
                    
            }    
                
            if((permissionHandler.isPermitted(perms, "approvals", "customerinvoiceapprovelevelone")) || (permissionHandler.isPermitted(perms, "approvals", "customerinvoiceapproveleveltwo"))){
                    int pendingCustInv = accInvoiceDao.pendingApprovalInvoicesCount(companyID);
                    if(pendingCustInv > 0){
                        String functionCall = "pendingCustomerInvpicesTab(true)";
                        String link=getLink(pendingCustInv+" Sales Invoice(s) ", functionCall);
                        jArray.add(getFormatedAlert(messageSource.getMessage("acc.field.Youhave", null, RequestContextUtils.getLocale(request))+" "+link+" "+messageSource.getMessage("acc.field.pendingforapproval", null, RequestContextUtils.getLocale(request)),"accountingbase updatemsg-vendor",isDashboard));
                    }
                    
            }
            
            return jArray;
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

     public String joinArrayList(ArrayList arr, String sep) {
        StringBuilder sb=new StringBuilder();
        if(!arr.isEmpty())sb.append(arr.get(0));
        for(int i=1;i<arr.size();i++){
            sb.append(sep+arr.get(i));
        }
        return sb.toString();
    }
   
       public ArrayList getVendorsUpdationInfo(String companyID, JSONObject perms, Boolean isDashboard, HttpServletRequest request) throws ServiceException {
        ArrayList jArray=new ArrayList();
        try {
            KwlReturnObject result = accVendorDao.getVendor_Dashboard(companyID, true, "createdOn", 0, 2);
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
                jArray.add(getFormatedAlert(messageSource.getMessage("acc.field.Newvendor", null, RequestContextUtils.getLocale(request))+" "+link+" "+messageSource.getMessage("acc.field.created", null, RequestContextUtils.getLocale(request)),"accountingbase updatemsg-vendor",isDashboard));
            }
            result = accVendorDao.getVendor_Dashboard(companyID, false, "modifiedOn", 0, 2);
            list = result.getEntityList();
            itr=list.iterator();
            while(itr.hasNext()){
                Vendor vendor=(Vendor)itr.next();
                link=vendor.getName();
                vendorID=vendor.getID();
                if(permissionHandler.isPermitted(perms, "vendor", "view"))
                    link=getLink(link, "callVendorDetails(\""+vendorID+"\")");
                jArray.add(getFormatedAlert(messageSource.getMessage("acc.field.Vendor", null, RequestContextUtils.getLocale(request))+" "+link+" "+messageSource.getMessage("acc.field.modified", null, RequestContextUtils.getLocale(request)),"accountingbase updatemsg-vendor",isDashboard));
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jArray;
    }
    
    public ArrayList getCustomersUpdationInfo(String companyID, JSONObject perms, Boolean isDashboard, HttpServletRequest request) throws ServiceException {
        ArrayList jArray=new ArrayList();
        try {
            KwlReturnObject result = accCustomerDao.getCustomer_Dashboard(companyID, true, "createdOn", 0, 2);
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

                jArray.add(getFormatedAlert(messageSource.getMessage("acc.field.Newcustomer", null, RequestContextUtils.getLocale(request))+" "+link+" "+messageSource.getMessage("acc.field.created", null, RequestContextUtils.getLocale(request)),"accountingbase updatemsg-customer",isDashboard));
            }
            result = accCustomerDao.getCustomer_Dashboard(companyID, false, "modifiedOn", 0, 2);
            itr=list.iterator();
            while(itr.hasNext()){
                Customer customer=(Customer)itr.next();
                customerID=customer.getID();
                link=customer.getName();
                if(permissionHandler.isPermitted(perms, "customer", "view"))
                    link=getLink(link, "callCustomerDetails(\""+customerID+"\")");
                jArray.add(getFormatedAlert(messageSource.getMessage("acc.het.13", null, RequestContextUtils.getLocale(request))+" "+link+" "+messageSource.getMessage("acc.field.modified", null, RequestContextUtils.getLocale(request)),"accountingbase updatemsg-customer",isDashboard));
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return jArray;
    }
        
    
     public ArrayList getProductsBelowROLInfo(HttpServletRequest request, String companyID, JSONObject perms,Boolean isDashboard) throws ServiceException {
        ArrayList jArray=new ArrayList();
        try {
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            KwlReturnObject result =accProductDao.getSuggestedReorderProducts(requestParams);
            JSONArray jArr = productHandler.getProductsJson(requestParams, result.getEntityList(),accProductDao,null,accountHandlerDao,accCurrencyDAOobj,false);
            
            String link;
            String productID;
            for(int i=0;i<jArr.length();i++){
                JSONObject obj = jArr.getJSONObject(i);
                link=obj.getString("productname");
                productID=obj.getString("productid");
                if(permissionHandler.isPermitted(perms, "product", "view"))
                    link=getLink(link, "callProductDetails(\""+productID+"\")");
                if(obj.getInt("quantity")==obj.getInt("reorderlevel")){
                    jArray.add(getFormatedAlert(messageSource.getMessage("acc.field.TheProduct", null, RequestContextUtils.getLocale(request))+" "+link+" "+messageSource.getMessage("acc.field.sstockisequaltoreorderlevelAvailablequantity", null, RequestContextUtils.getLocale(request))+obj.getInt("quantity")+" "+obj.getString("uomname")+")","accountingbase updatemsg-product",isDashboard));
                } else {
                    jArray.add(getFormatedAlert(messageSource.getMessage("acc.field.TheProduct", null, RequestContextUtils.getLocale(request))+" "+link+" "+messageSource.getMessage("acc.field.isbelowreorderlevelAvailablequantity", null, RequestContextUtils.getLocale(request))+obj.getInt("quantity")+" "+obj.getString("uomname")+")","accountingbase updatemsg-product",isDashboard));
                }
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }catch(Exception e){
            System.out.println(e.getStackTrace());
        }
        return jArray;
    }
     
         public String getLink(String message, String functionName) {
        return "<a href=# onclick='"+functionName+"'>"+message+"</a>";
    }
         
    public String getFormatedAlert(String message, String cssClass,Boolean isDashboard) {
        String fmtMsg="";
        if(isDashboard)
            fmtMsg=getContentDiv(cssClass);
        fmtMsg+=message;
        return getContentSpan(fmtMsg);
    }
           
              public String getContentDiv(String typeStr) {
        String div = "<div  class=\""+typeStr +" statusitemimg\"></div>";
        return div;
    }
              
    private StringBuilder getCompanyAdminDashboardUpdateList(HttpServletRequest request, JSONObject perms) throws ServiceException {
        StringBuilder finalStr=new StringBuilder();
        try {
            String companyID = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cpresult = accountHandlerDao.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            finalStr.append(joinArrayList(getVendorsUpdationInfo(companyID, perms,true,request), ""));
            finalStr.append(joinArrayList(getCustomersUpdationInfo(companyID, perms,true,request), ""));
            if(pref!=null){
              if(!pref.isWithoutInventory())
                finalStr.append(joinArrayList(getProductsBelowROLInfo(request, companyID, perms,true), ""));
            }else
                finalStr.append(joinArrayList(getProductsBelowROLInfo(request, companyID, perms,true), ""));
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }
    
   private StringBuilder getPurchaseManagementModuleData(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        StringBuilder finalStr = new StringBuilder();
        try {
            finalStr.append("<div class='firstflowlink1'><IMG class='thickBlackBorderWidget'  SRC='../../images/purchasemanagementwidget.png' usemap='#AlienAreas1'></div>");                          
            finalStr.append("<map name='AlienAreas1'>" );
            finalStr.append("<area shape='rect' coords='45,47,103,105' href=# onclick='callBusinessContactWindow(false, null, null, false)'  wtf:qtip='"+messageSource.getMessage("acc.WI.44", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your vendors including contact information, account details, preferred delivery mode and debit term. You can also export the vendor list in convenient formats as well as add sub-accounts to existing vendor accounts.'>");
            finalStr.append("<area shape='rect' coords='147,47,203,105' href=# onclick='callProductDetails(null,true)' wtf:qtip='"+messageSource.getMessage("acc.WI.43", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product.'> ");
            finalStr.append("<area shape='rect' coords='25,192,80,250' href=# onclick='callPurchaseReceipt(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WI.42", null, RequestContextUtils.getLocale(request))+"'>");   //Create a cash purchase receipt to give to your vendors as a payment record, on paying full amount at the time of purchase.'> ");
            finalStr.append("<area shape='rect' coords='151,191,210,250' href=# onclick='callPurchaseOrder(false, null)' wtf:qtip='"+messageSource.getMessage("acc.WI.41", null, RequestContextUtils.getLocale(request))+"'>");   //Easily create purchase order for your vendors. Include debit term and complete purchase information.'> ");
            finalStr.append("<area shape='rect' coords='282,192,343,251' href=# onclick='callVendorQuotation()' wtf:qtip='"+messageSource.getMessage("acc.WI.51", null, RequestContextUtils.getLocale(request))+"'>");   
            finalStr.append("<area shape='rect' coords='284,48,339,104' href=# onclick='callPurchaseReq()' wtf:qtip='"+messageSource.getMessage("acc.WI.52", null, RequestContextUtils.getLocale(request))+"'>");               
            finalStr.append("<area shape='rect' coords='22,312,80,370' href=# onclick='callAgedPayable({\"withinventory\":true})' wtf:qtip='"+messageSource.getMessage("acc.WI.40", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount payables.'> ");
            finalStr.append("<area shape='rect' coords='152,310,210,370' href=# onclick='callPurchaseInvoiceType()' wtf:qtip='"+messageSource.getMessage("acc.WI.39", null, RequestContextUtils.getLocale(request))+"'>");   //Provide your vendors with receipt on delivery of purchased goods. Record product and payment details.'> ");
            finalStr.append("<area shape='rect' coords='282,315,342,370' href=# onclick='callGoodsReceiptDelivery(false,null,null)' wtf:qtip='"+messageSource.getMessage("acc.WI.47", null, RequestContextUtils.getLocale(request))+"'>");   //Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc.'>");
            finalStr.append("<area shape='rect' coords='284,429,342,490' href=# onclick='callSalesReturnWindow(false)' wtf:qtip='"+messageSource.getMessage("acc.WI.50", null, RequestContextUtils.getLocale(request))+"'>");   //Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc.'>");
            finalStr.append("<area shape='rect' coords='155,430,215,490' href=# onclick='callCreditNote(false)' wtf:qtip='"+messageSource.getMessage("acc.WI.38", null, RequestContextUtils.getLocale(request))+"'>");   //Generate a debit note for your vendors for reducing your account payables in cases, such as return of damaged goods, error in billing etc.'>");
            if(Constants.isNewPaymentStructure){
                finalStr.append("<area shape='rect' coords='21,430,79,490' href=# onclick='callPaymentNew()' wtf:qtip='"+messageSource.getMessage("acc.WI.37", null, RequestContextUtils.getLocale(request))+"'>");   //Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
            } else {
                finalStr.append("<area shape='rect' coords='21,430,79,490' href=# onclick='callPayment()' wtf:qtip='"+messageSource.getMessage("acc.WI.37", null, RequestContextUtils.getLocale(request))+"'>");   //Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
            }
            finalStr.append("</map>");
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }        
   private StringBuilder getSalesBillingModuleData(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        StringBuilder finalStr = new StringBuilder();
        try {
            finalStr.append("<div class='firstflowlink1'><IMG class='thickBlackBorderWidget'  SRC='../../images/salesbillingwidget.png' usemap='#AlienAreas2'></div>");                          
            finalStr.append("<map name='AlienAreas2'>" );
            finalStr.append("<area shape='rect' coords='50,35,107,95' href=# onclick='callBusinessContactWindow(false, null, null, true)' wtf:qtip='"+messageSource.getMessage("acc.WI.36", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain all information about your customers including contact information, account details, preferred delivery mode and credit term. You can also export the customer list in convenient formats as well as add sub-accounts to existing customer accounts.'>");
            finalStr.append("<area shape='rect' coords='145,35,202,95' href=# onclick='callProductDetails(null,true)' wtf:qtip='"+messageSource.getMessage("acc.WI.35", null, RequestContextUtils.getLocale(request))+"'>");   //Maintain details for all products sold by your organization including product details, price, as well as inventory details. You can also add a sub-product to an existing product.'>");
            finalStr.append("<area shape='rect' coords='30,167,87,227' href=# onclick='callSalesReceipt(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WI.34", null, RequestContextUtils.getLocale(request))+"'>");   //Create a cash sales receipt to give to your customers as a payment record, on receiving full amount at the time of sale.'>");
            finalStr.append("<area shape='rect' coords='153,167,210,223' href=# onclick='callSalesOrder(false, null)' wtf:qtip='"+messageSource.getMessage("acc.WI.33", null, RequestContextUtils.getLocale(request))+"'>");   //Record all details related to a customer purchase order by generating an associated sales order.'>");
            finalStr.append("<area shape='rect' coords='28,307,85,362' href=# onclick='callAgedRecievable({\"withinventory\":true})' wtf:qtip='"+messageSource.getMessage("acc.WI.32", null, RequestContextUtils.getLocale(request))+"'>");   //Keep a track record of all amount receivables.'>");
            finalStr.append("<area shape='rect' coords='157,300,215,360' href=# onclick='callInvoice(false,null)' wtf:qtip='"+messageSource.getMessage("acc.WI.31", null, RequestContextUtils.getLocale(request))+"'>");   //Generate Invoices for your customers. Include credit term and discounts offered on individual products as well as on the total bill amount.'>");
            finalStr.append("<area shape='rect' coords='155,440,215,500' href=# onclick='callCreditNote(true)' wtf:qtip='"+messageSource.getMessage("acc.WI.30", null, RequestContextUtils.getLocale(request))+"'>");   //If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases.'>");
            finalStr.append("<area shape='rect' coords='290,440,350,500' href=# onclick='callSalesReturnWindow(true)' wtf:qtip='"+messageSource.getMessage("acc.WI.53", null, RequestContextUtils.getLocale(request))+"'>");   //If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases.'>");
            finalStr.append("<area shape='rect' coords='290,300,350,360' href=# onclick='callDeliveryOrder(false, null)' wtf:qtip='"+messageSource.getMessage("acc.WI.45", null, RequestContextUtils.getLocale(request))+"'>");   //If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases.'>");
            finalStr.append("<area shape='rect' coords='170,310,220,360' href=# onclick='callCreditNote(true)' wtf:qtip='"+messageSource.getMessage("acc.WI.30", null, RequestContextUtils.getLocale(request))+"'>");   //If you need to refund your customers on a credit basis i.e. in the near future, generate a credit note for the transaction. Customers can use this credit memo to get a refund in future purchases.'>");
            if(Constants.isNewPaymentStructure){
                finalStr.append("<area shape='rect' coords='28,440,85,500' href=# onclick='callReceiptNew()' wtf:qtip='"+messageSource.getMessage("acc.WI.29", null, RequestContextUtils.getLocale(request))+"'>");   //Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
            } else {
                finalStr.append("<area shape='rect' coords='28,440,85,500' href=# onclick='callReceipt()' wtf:qtip='"+messageSource.getMessage("acc.WI.29", null, RequestContextUtils.getLocale(request))+"'>");   //Record all payments through multiple payment methods including cash, cheque and debit/credit card.'>");
            }
            finalStr.append("<area shape='rect' coords='287,168,342,222' href=# onclick='callQuotation()' wtf:qtip="+messageSource.getMessage("acc.WI.28", null, RequestContextUtils.getLocale(request))+"'>");   //\"Generate Quotation's related to your Customer's requirements. You can use this to give your Customer's a rough idea of financial costings for their requirements.\">");
            finalStr.append("</map>");
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return finalStr;
    }           
}
