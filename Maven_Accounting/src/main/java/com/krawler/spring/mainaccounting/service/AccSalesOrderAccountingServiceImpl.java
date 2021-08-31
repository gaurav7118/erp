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
package com.krawler.spring.mainaccounting.service;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.CCConstants;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccSalesOrderAccountingServiceImpl implements AccSalesOrderAccountingService,MessageSourceAware{
     private AccountingHandlerDAO accountingHandlerDAOobj;
     private accSalesOrderDAO accSalesOrderDAOobj;
     private accAccountDAO accAccountDAOobj;
     private accTaxDAO accTaxObj;
     private accCurrencyDAO accCurrencyobj;
     private accInvoiceDAO accInvoiceDAOobj;
     private accProductDAO accProductObj;
     private AccCommonTablesDAO accCommonTablesDAO;
     private HibernateTransactionManager txnManager;
     private accCompanyPreferencesDAO accCompanyPreferencesObj;
     private fieldDataManager fieldDataManagercntrl;
     private kwlCommonTablesDAO kwlCommonTablesDAOObj;
     private MessageSource messageSource;
     private auditTrailDAO auditTrailObj;
     
     public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
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
    public void setAccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    public void setAuditTrailObj(auditTrailDAO auditTrailObj) {
        this.auditTrailObj = auditTrailObj;
    }
    public JSONObject getSalesOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            KwlReturnObject result = accSalesOrderDAOobj.getSalesOrders(requestParams);
            JSONArray jarr = getSalesOrdersJson(request, result.getEntityList());
            jobj.put("data", jarr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccSalesOrderAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccSalesOrderAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public JSONArray getSalesOrdersJson(HttpServletRequest request, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            boolean closeflag = Boolean.parseBoolean(request.getParameter("closeflag"));
            boolean exceptFlagINV = Boolean.parseBoolean(request.getParameter("exceptFlagINV"));
            boolean doflag = request.getParameter("doflag") != null ? true : false;
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Sales_Order_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                SalesOrder salesOrder = (SalesOrder) itr.next();
                KWLCurrency currency = null;

                if (salesOrder.getCurrency() != null) {
                    currency = salesOrder.getCurrency();
                } else {
                    currency = salesOrder.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : salesOrder.getCustomer().getAccount().getCurrency();
                }

                Customer customer = salesOrder.getCustomer();
                JSONObject obj = new JSONObject();
                obj.put("billid", salesOrder.getID());
                obj.put("personid", customer.getID());
                obj.put("billno", salesOrder.getSalesOrderNumber());
                obj.put("duedate", authHandler.getDateFormatter(request).format(salesOrder.getDueDate()));
                obj.put("date", authHandler.getDateFormatter(request).format(salesOrder.getOrderDate()));
                obj.put("shipdate", salesOrder.getShipdate() == null ? "" : authHandler.getDateFormatter(request).format(salesOrder.getShipdate()));
                obj.put("shipvia", salesOrder.getShipvia() == null ? "" : salesOrder.getShipvia());
                obj.put("fob", salesOrder.getFob() == null ? "" : salesOrder.getFob());
                Iterator itrRow = salesOrder.getRows().iterator();
                double amount = 0, totalDiscount = 0, discountPrice = 0;
                while (itrRow.hasNext()) {
                    SalesOrderDetail sod = (SalesOrderDetail) itrRow.next();
                    amount += sod.getQuantity() * sod.getRate();
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
                    double sorate = (Double) bAmt.getEntityList().get(0);

                    double quotationPrice = authHandler.round(sod.getQuantity() * sorate, companyid);
                    if (sod.getDiscountispercent() == 1) {
                        discountPrice = (quotationPrice) - (quotationPrice * sod.getDiscount() / 100);
                    } else {
                        discountPrice = quotationPrice - sod.getDiscount();
                    }

                    amount += discountPrice + (discountPrice * rowTaxPercent / 100);
                }
                obj.put("amount", amount);
                obj.put("amountinbase", amount);
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
                //                    obj.put("orderamount", CompanyHandler.getBaseToCurrencyAmount(session,request,amount,currency.getCurrencyID(),salesOrder.getOrderDate()));
                KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currency.getCurrencyID(), salesOrder.getOrderDate(), 0);
                obj.put("currencysymbol", currency.getSymbol());
                obj.put("taxid", salesOrder.getTax() == null ? "" : salesOrder.getTax().getID());
                obj.put("taxname", salesOrder.getTax() == null ? "" : salesOrder.getTax().getName());
                double taxPercent = 0;
                if (salesOrder.getTax() != null) {
                    requestParams.put("transactiondate", salesOrder.getOrderDate());
                    requestParams.put("taxid", salesOrder.getTax().getID());
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
                obj.put("personname", customer.getName());
                obj.put("memo", salesOrder.getMemo());
                obj.put("costcenterid", salesOrder.getCostcenter() == null ? "" : salesOrder.getCostcenter().getID());
                obj.put("costcenterName", salesOrder.getCostcenter() == null ? "" : salesOrder.getCostcenter().getName());
                String status = "";
                if (exceptFlagINV) {
                    Iterator itr1 = salesOrder.getRows().iterator();
                    status = "Closed";
                    while (itr1.hasNext()) {
                        SalesOrderDetail row = (SalesOrderDetail) itr1.next();
                        double addobj = doflag ? getSalesOrderDetailStatusForDO(row) : getSalesOrderDetailStatus(row);
                        if (addobj > 0) {
                            status = "Open";
                            break;
                        }
                    }
                } else {
                    status = (doflag) ? getSalesOrderStatusForDO(salesOrder) : getSalesOrderStatus(salesOrder);
                }
                obj.put("status", status);
                boolean includeprotax = false;
                Set<SalesOrderDetail> salesOrderDetails = salesOrder.getRows();
                for (SalesOrderDetail salesOrderDetail : salesOrderDetails) {
                    if (salesOrderDetail.getTax() != null) {
                        includeprotax = true;
                        break;
                    }
                }
                obj.put("includeprotax", includeprotax);
                obj.put("salesPerson", salesOrder.getSalesperson() == null ? "" : salesOrder.getSalesperson().getID());

                DateFormat df = (DateFormat) requestParams.get("df");
                Map<String, Object> variableMap = new HashMap<String, Object>();
                SalesOrderCustomData jeDetailCustom = (SalesOrderCustomData) salesOrder.getSoCustomData();
                replaceFieldMap = new HashMap<String, String>();
                if (jeDetailCustom != null) {
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                    DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                    Date dateFromDB=null;
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        String coldata = varEntry.getValue().toString();
                        if (customFieldMap.containsKey(varEntry.getKey())) {
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                            FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                            if (fieldComboData != null) {
                                obj.put(varEntry.getKey(), fieldComboData.getValue() != null ? fieldComboData.getValue() : "");
                            }
                        } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                dateFromDB = defaultDateFormat.parse(coldata);
                                coldata = sdf.format(dateFromDB);
                            } catch (Exception e) {
                            }
                            obj.put(varEntry.getKey(),coldata);
                        } else {
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                obj.put(varEntry.getKey(), coldata);
                            }
                        }
                    }
                }

                boolean addFlag = true;
                if (closeflag && salesOrder.isDeleted()) {
                    addFlag = false;
                } else if (closeflag && status.equalsIgnoreCase("Closed")) {
                    addFlag = false;
                }
                if (addFlag) {
                    jArr.put(obj);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getSalesOrdersJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public HashMap<String, Object> getSalesOrdersMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put(Constants.ss, request.getParameter(Constants.ss));
        requestParams.put(Constants.start, request.getParameter(Constants.start));
        requestParams.put(Constants.limit, request.getParameter(Constants.limit));
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
        requestParams.put("orderforcontract", request.getParameter("orderForContract")!=null?Boolean.parseBoolean(request.getParameter("orderForContract")):false);
        requestParams.put(Constants.ValidFlag, request.getParameter(Constants.ValidFlag));
        requestParams.put(Constants.BillDate, request.getParameter(Constants.BillDate));
        requestParams.put("pendingapproval", (request.getParameter("pendingapproval") != null) ? Boolean.parseBoolean(request.getParameter("pendingapproval")) : false);
        requestParams.put("istemplate", (request.getParameter("istemplate") != null) ? Integer.parseInt(request.getParameter("istemplate")) : 0);
        requestParams.put("currencyid", request.getParameter("currencyid"));
        requestParams.put("exceptFlagINV", request.getParameter("exceptFlagINV"));
        requestParams.put("exceptFlagORD", request.getParameter("exceptFlagORD"));
        requestParams.put(Constants.Acc_Search_Json, request.getParameter(Constants.Acc_Search_Json));
        requestParams.put(Constants.moduleid, request.getParameter(Constants.moduleid));
        requestParams.put("currencyfilterfortrans", (request.getParameter("currencyfilterfortrans") == null) ? "" : request.getParameter("currencyfilterfortrans"));
        requestParams.put("isOpeningBalanceOrder", request.getParameter("isOpeningBalanceOrder") != null ? Boolean.parseBoolean(request.getParameter("isOpeningBalanceOrder")) : false);
        requestParams.put("isLeaseFixedAsset", request.getParameter("isLeaseFixedAsset")!=null?Boolean.parseBoolean(request.getParameter("isLeaseFixedAsset")):false);
        requestParams.put(CCConstants.REQ_customerId, request.getParameter(CCConstants.REQ_customerId));
        return requestParams;
    }

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

    public double getSalesOrderDetailStatus(SalesOrderDetail sod) throws ServiceException {
        double result = sod.getQuantity();
        KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), sod.getCompany().getCompanyID());
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
        KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSOD(sod.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0.0;
        boolean fullInv = false;
        double quantPartTtInv = 0.0;
        while (ite1.hasNext()) {
            InvoiceDetail ge = (InvoiceDetail) ite1.next();
            if (ge.getInvoice().isPartialinv()) {
//                Need to test properly.
//                double quantity = ge.getInventory().isInvrecord() ? ge.getInventory().getQuantity() : ge.getInventory().getActquantity();
                double quantity = ge.getInventory().getQuantity();
                quantPartTtInv += quantity * ge.getPartamount();
            } else {
                fullInv = true;
//                qua += ge.getInventory().isInvrecord() ? ge.getInventory().getQuantity() : ge.getInventory().getActquantity();
                qua += ge.getInventory().getQuantity();
            }
        }

        if (fullInv) {
            result = sod.getQuantity() - qua;
        } else {
            if (sod.getQuantity() * 100 > quantPartTtInv) {
                result = sod.getQuantity() - qua;
            } else {
                result = 0;
            }
        }

        return result;
    }

    /*
     * Function to fetch sales order status for Delivery order. Checked if
     * delivery order of all sales order quantities are prepared.
     */
    public String getSalesOrderStatusForDO(SalesOrder so) throws ServiceException {
        Set<SalesOrderDetail> orderDetail = so.getRows();
        Iterator ite = orderDetail.iterator();
        String result = "Closed";
        while (ite.hasNext()) {
            SalesOrderDetail soDetail = (SalesOrderDetail) ite.next();

            KwlReturnObject idresult = accInvoiceDAOobj.getDOIDFromSOD(soDetail.getID(),"");
            List list = idresult.getEntityList();
            Iterator ite1 = list.iterator();
            double qua = 0;
            while (ite1.hasNext()) {
                DeliveryOrderDetail ge = (DeliveryOrderDetail) ite1.next();
                qua += ge.getInventory().getQuantity();
            }
            if (qua < soDetail.getQuantity()) {
                result = "Open";
                break;
            }
        }
        return result;
    }

    public String getSalesOrderStatus(SalesOrder so) throws ServiceException {
        String result = "Closed";
        try {
            Set<SalesOrderDetail> orderDetail = so.getRows();
            Iterator ite = orderDetail.iterator();

            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), so.getCompany().getCompanyID());
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

            boolean fullInv = false;
            while (ite.hasNext()) {
                SalesOrderDetail soDetail = (SalesOrderDetail) ite.next();
//            String query = "from InvoiceDetail ge where ge.salesorderdetail.ID = ?";
//            List list =  executeQuery(session, query,pDetail.getID());
                double qua = 0;
                double quantPartTt = soDetail.getQuantity() * 100;
                double quantPartTtInv = 0.0;
                if (pref.isWithInvUpdate()) { //In Trading Flow                 
                    KwlReturnObject doresult = accInvoiceDAOobj.getDeliveryOrderDFromSOD(soDetail.getID(), pref.getCompany().getCompanyID());
                    List list = doresult.getEntityList();
                    if (list.size() > 0) {
                        Iterator ite1 = list.iterator();
                        while (ite1.hasNext()) {
                            String orderid = (String) ite1.next();
                            KwlReturnObject res = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), orderid);
                            DeliveryOrderDetail deliveryOrderDetail = (DeliveryOrderDetail) res.getEntityList().get(0);
                            fullInv = true;
                            qua += deliveryOrderDetail.getDeliveredQuantity();
                        }
                    }
                } else { //In Non Trading Flow 

                    KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSOD(soDetail.getID());
                    List list = idresult.getEntityList();
                    Iterator ite1 = list.iterator();
                    while (ite1.hasNext()) {
                        InvoiceDetail ge = (InvoiceDetail) ite1.next();
                        if (ge.getInvoice().isPartialinv()) {
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

    public JSONObject getSalesOrderRows(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateFormatter(request);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            boolean doflag = request.getParameter("doflag") != null ? true : false;
            String[] sos = (String[]) request.getParameter("bills").split(",");
            int i = 0;
            JSONArray jArr = new JSONArray();
            double addobj = 1;

            String dtype = request.getParameter("dtype");

            boolean isForReport = false;

            if (!StringUtil.isNullOrEmpty(dtype) && dtype.equals("report")) {
                isForReport = true;
            }

            HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("salesOrder.ID");
            order_by.add("srno");
            order_type.add("asc");
            soRequestParams.put("filter_names", filter_names);
            soRequestParams.put("filter_params", filter_params);
            soRequestParams.put("order_by", order_by);
            soRequestParams.put("order_type", order_type);

            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Sales_Order_ModuleId, 1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);


            String closeflag = request.getParameter("closeflag");
            while (sos != null && i < sos.length) {
//                SalesOrder so=(SalesOrder)session.get(SalesOrder.class, sos[i]);
                KwlReturnObject result = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), sos[i]);
                SalesOrder so = (SalesOrder) result.getEntityList().get(0);

                KWLCurrency currency = null;
                if (so.getCurrency() != null) {
                    currency = so.getCurrency();
                } else {
                    currency = so.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : so.getCustomer().getAccount().getCurrency();
                }

//                KWLCurrency currency=so.getCustomer().getAccount().getCurrency()==null?kwlcurrency:so.getCustomer().getAccount().getCurrency();
//                Iterator itr=so.getRows().iterator();
                filter_params.clear();
                filter_params.add(so.getID());
                KwlReturnObject podresult = accSalesOrderDAOobj.getSalesOrderDetails(soRequestParams);
                Iterator itr = podresult.getEntityList().iterator();

                while (itr.hasNext()) {
                    SalesOrderDetail row = (SalesOrderDetail) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("billid", so.getID());
                    obj.put("billno", so.getSalesOrderNumber());
                    obj.put("salesPerson", so.getSalesperson() != null ? so.getSalesperson().getID() : "");
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", row.getProduct().getID());
                    obj.put("productname", row.getProduct().getName());
                    String uom = row.getUom() != null ? row.getUom().getNameEmptyforNA() : row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                    obj.put("unitname", uom);
                    obj.put("uomname", uom);
                    obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("desc", StringUtil.isNullOrEmpty(row.getDescription()) ? row.getProduct().getDescription() : row.getDescription());
                    obj.put("multiuom", row.getProduct().isMultiuom());
                    obj.put("description", StringUtil.isNullOrEmpty(row.getDescription()) ? row.getProduct().getDescription() : row.getDescription());
                    obj.put("type", row.getProduct().getProducttype() == null ? "" : row.getProduct().getProducttype().getName());
                    obj.put("pid", row.getProduct().getProductid());
                    obj.put("memo", row.getRemark());
//                    obj.put("discountispercent", 1);
//                    obj.put("prdiscount", 0);
                    obj.put("discountispercent", row.getDiscountispercent());
                    obj.put("prdiscount", row.getDiscount());
                    obj.put("lockquantity", row.getLockquantity()); //for getting locked  quantity of indivisual so
                    obj.put("islockQuantityflag",so.isLockquantityflag()); //for getting locked flag of indivisual so
                    
                    if (row.getSalesOrder().getLeaseOrMaintenanceSO() == 1) {// if it is a lease SO
                        getAssetDetailJsonObject(request, row, obj);
                    }
                    
                    if (row.getQuotationDetail() != null) {
                        obj.put("linkto", row.getQuotationDetail().getQuotation().getquotationNumber());
                        obj.put("linkid", row.getQuotationDetail().getQuotation().getID());
                        obj.put("rowid", row.getQuotationDetail().getID());
                        obj.put("savedrowid", row.getID());
                        obj.put("docrowid", row.getID());
                        obj.put("linktype", 2);
                    } else if(row.getProductReplacementDetail() != null){
                            obj.put("linkto", row.getProductReplacementDetail().getProductReplacement().getReplacementRequestNumber());
                            obj.put("linkid", row.getProductReplacementDetail().getProductReplacement().getId());
                            obj.put("rowid", row.getID());
                            obj.put("savedrowid", row.getID());
                            obj.put("docrowid", row.getID());
                            obj.put("linktype", 2);
                    } else {
                        obj.put("linkto", "");
                        obj.put("linkid", "");
                        obj.put("savedrowid", "");
                        obj.put("linktype", -1);
                    }
                    double rowTaxPercent = 0;
                    double rowTaxAmount = 0;
                    boolean isRowTaxApplicable = false;
                    if (row.getTax() != null) {
//                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), so.getOrderDate(), row.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                        isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                        if (isRowTaxApplicable) {
                            rowTaxAmount = row.getRowTaxAmount();
                        }
                    }
                    obj.put("prtaxpercent", rowTaxPercent);
                    obj.put("rowTaxAmount", rowTaxAmount);
                    obj.put("prtaxid", row.getTax() == null ? "" : row.getTax().getID());
                    obj.put("rate", row.getRate());
                    obj.put("isAsset", row.getProduct().isAsset());
                    
                    HashMap<String, Object> fieldrequestParamsProduct = new HashMap();
                    Map<String, Object> variableMapProduct = new HashMap<String, Object>();
                    fieldrequestParamsProduct.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParamsProduct.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Product_Master_ModuleId, 0));
                    HashMap<String, String> replaceFieldMapProduct = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMapProduct = accAccountDAOobj.getFieldParamsMap(fieldrequestParamsProduct, replaceFieldMapProduct);
                    KwlReturnObject resultProduct = accountingHandlerDAOobj.getObject(AccProductCustomData.class.getName(), row.getProduct().getID());
                    AccProductCustomData objProduct = (AccProductCustomData) resultProduct.getEntityList().get(0);
                    if (objProduct != null) {
                        productHandler.setCustomColumnValuesForProduct(objProduct, FieldMapProduct, replaceFieldMapProduct, variableMapProduct);
                        for (Map.Entry<String, Object> varEntry : variableMapProduct.entrySet()) {
                            String coldata = varEntry.getValue().toString();
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                obj.put(varEntry.getKey(), coldata);
                                obj.put("key", varEntry.getKey());
//                                jobj.append("data", jsonObj);
                            }
                        }
                    }

//                        obj.put("orderrate", CompanyHandler.getBaseToCurrencyAmount(session,request,row.getRate(),currency.getCurrencyID(),so.getOrderDate()));
                    KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), so.getOrderDate(), 0);
                    obj.put("orderrate", row.getRate());//obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
//                        obj.put("quantity", row.getQuantity());                    
                    double baseuomrate = row.getBaseuomrate();
                    if (row.getUom() != null) {
                        obj.put("uomid", row.getUom().getID());
                    } else {
                        obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
                    }
                    double quantity = 0;
                    if (closeflag != null) {
                        addobj = doflag ? getSalesOrderDetailStatusForDO(row) : getSalesOrderDetailStatus(row);
                        quantity = addobj;
                        obj.put("quantity", addobj);
                        obj.put("copyquantity", addobj);
                        obj.put("dquantity", addobj);
                        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(addobj,baseuomrate, companyid));
                        obj.put("baseuomrate", baseuomrate);
                    } else {
                        quantity = row.getQuantity();
                        obj.put("quantity", quantity);
                        obj.put("copyquantity", quantity);
                        obj.put("dquantity", quantity);
                        obj.put("baseuomquantity",authHandler.calculateBaseUOMQuatity(quantity,baseuomrate, companyid));
                        obj.put("baseuomrate", baseuomrate);
                    }
                    if (row.getUom() != null) {
                        obj.put("uomid", row.getUom().getID());
                        obj.put("baseuomquantity",authHandler.calculateBaseUOMQuatity(quantity,row.getBaseuomrate(), companyid));
                        obj.put("baseuomrate", row.getBaseuomrate());
                    } else {
                        obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
                        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity,row.getBaseuomrate(), companyid));
                        obj.put("baseuomrate", row.getBaseuomrate());
                    }
                    obj.put("balanceQuantity", row.getQuantity() - getSalesOrderBalanceQuantity(row));
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    SalesOrderDetailsCustomData jeDetailCustom = (SalesOrderDetailsCustomData) row.getSoDetailCustomData();
                    replaceFieldMap = new HashMap<String, String>();
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                            FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                            if (fieldComboData != null) {
                                if (isForReport) {
                                    obj.put(varEntry.getKey(), fieldComboData.getValue() != null ? fieldComboData.getValue() : "");//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                                } else {
                                    obj.put(varEntry.getKey(), coldata != null ? coldata : "");
                                }
                            } else {
                                obj.put(varEntry.getKey(), coldata != null ? coldata : "");
                            }
                        }
                    }

                    if (addobj > 0) {
                        jArr.put(obj);
                    }
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }
    
    public void getAssetDetailJsonObject(HttpServletRequest request, SalesOrderDetail row, JSONObject obj) throws ServiceException, JSONException, SessionExpiredException {
        String companyid = sessionHandlerImpl.getCompanyid(request);
        DateFormat df = authHandler.getDateFormatter(request);

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

//              if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
//                KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), row.getProduct().getID());
//                Product product = (Product) prodresult.getEntityList().get(0);
//                isBatchForProduct = product.isIsBatchForProduct();
//                isSerialForProduct = product.isIsSerialForProduct();
//            }
//            if (pref.isIsBatchCompulsory() || pref.isIsSerialCompulsory()) {  //check if company level option is on then only we will check productt level
//                if (isBatchForProduct || isSerialForProduct) {
//                    assetDetailsJOBJ.put("batchdetails", (assetDetails.getBatch() == null) ? "" : getBatchJson(assetDetails.getBatch(), isFixedAssetDO, pref.isIsBatchCompulsory(), isBatchForProduct, pref.isIsSerialCompulsory(), isSerialForProduct, request));
//                }
//            }
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
            if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {  //check if company level option is on then only we will check productt level
                if (isBatchForProduct || isSerialForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct) {  //product level batch and serial no on or not
                    assetDetailsJOBJ.put("batchdetails", getNewBatchJson(row.getProduct(), request, assetDetails.getId()));
                }
            }
            assetDetailsJArr.put(assetDetailsJOBJ);
        }
        obj.put("assetDetails", assetDetailsJArr.toString());
    }
        public String getNewBatchJson(Product product, HttpServletRequest request, String documentid) throws ServiceException, SessionExpiredException, JSONException {
        JSONArray jSONArray = new JSONArray();
        DateFormat df = authHandler.getDateFormatter(request);
        KwlReturnObject kmsg = null;
        boolean linkingFlag = (StringUtil.isNullOrEmpty(request.getParameter("linkingFlag"))) ? false : Boolean.parseBoolean(request.getParameter("linkingFlag"));
        boolean isEdit=(StringUtil.isNullOrEmpty(request.getParameter("isEdit")))?false:Boolean.parseBoolean(request.getParameter("isEdit"));
        String moduleID = request.getParameter("moduleid");
         boolean isBatch=false;
        if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && !product.isIsrowforproduct() && !product.isIsrackforproduct() && !product.isIsbinforproduct() && product.isIsSerialForProduct()) {
            kmsg = accCommonTablesDAO.getOnlySerialDetails(documentid, linkingFlag, moduleID,false,isEdit);
        } else {
             isBatch=true;
            kmsg = accCommonTablesDAO.getBatchSerialDetails(documentid, !product.isIsSerialForProduct(), linkingFlag, moduleID,false,isEdit,"");
        }
//        product.getName()
        double ActbatchQty = 1;
        double batchQty = 0;
        List batchserialdetails = kmsg.getEntityList();
        Iterator iter = batchserialdetails.iterator();
        while (iter.hasNext()) {
            Object[] objArr = (Object[]) iter.next();
            JSONObject obj = new JSONObject();
            obj.put("id", objArr[0] != null ? (String) objArr[0] : "");
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

//            obj.put("quantity", ActbatchQty);
            obj.put("balance", 0);
            obj.put("asset", "");
            obj.put("serialnoid", objArr[7] != null ? (String) objArr[7] : "");
            obj.put("serialno", objArr[8] != null ? (String) objArr[8] : "");
            obj.put("purchasebatchid", objArr[0] != null ? (String) objArr[0] : "");
            obj.put("purchaseserialid", objArr[7] != null ? (String) objArr[7] : "");
            obj.put("expstart", (objArr[9] != null && !objArr[9].toString().equalsIgnoreCase("")) ? df.format(objArr[9]) : "");
            obj.put("expend", (objArr[10] != null && !objArr[10].toString().equalsIgnoreCase(""))  ? df.format(objArr[10]) : "");
            obj.put("documentid", documentid != null ? documentid : "");
            jSONArray.put(obj);
            batchQty--;

        }


        return jSONArray.toString();
    }
    public String getBatchJson(ProductBatch productBatch, boolean isFixedAssetDO,boolean isbatch,boolean isBatchForProduct,boolean isserial,boolean isSerialForProduct,HttpServletRequest request) throws ServiceException, SessionExpiredException, JSONException {
        JSONArray jSONArray = new JSONArray();
        DateFormat df = authHandler.getDateFormatter(request);
        String purchasebatchid = "";
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
        filter_names.add("batch.id");
        filter_params.add(productBatch.getId());
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        KwlReturnObject kmsg = accSalesOrderDAOobj.getSerialForBatch(filterRequestParams);

        List list = kmsg.getEntityList();
        Iterator iter = list.iterator();
        int i = 1;
        while (iter.hasNext()) {
            BatchSerial batchSerial = (BatchSerial) iter.next();
            JSONObject obj = new JSONObject();
            if (i == 1) {
                obj.put("id", productBatch.getId());
                obj.put("batch", productBatch.getName());
                obj.put("batch", productBatch.getName());
                obj.put("batchname", productBatch.getName());
                obj.put("location", productBatch.getLocation().getId());
                obj.put("warehouse", productBatch.getWarehouse().getId());
                obj.put("mfgdate", productBatch.getMfgdate()!=null?authHandler.getDateFormatter(request).format(productBatch.getMfgdate()):"");
                obj.put("expdate",productBatch.getExpdate()!=null?authHandler.getDateFormatter(request).format(productBatch.getExpdate()):"");obj.put("quantity", productBatch.getQuantity());
                obj.put("balance", productBatch.getBalance());
                obj.put("balance", productBatch.getBalance());
                obj.put("asset", productBatch.getAsset());
                if (isFixedAssetDO) {
                    obj.put("purchasebatchid", productBatch.getId());
                }
            } else {
                obj.put("id", "");
                obj.put("batch", "");
                obj.put("batchname", "");
                obj.put("location", "");
                obj.put("warehouse", "");
                obj.put("mfgdate", "");
                obj.put("expdate", "");
                obj.put("quantity", "");
                obj.put("balance", "");
                obj.put("purchasebatchid", "");
            }
            i++;
            obj.put("serialnoid", batchSerial.getId());
            obj.put("serialno", batchSerial.getName());
            obj.put("expstart", batchSerial.getExpfromdate()!=null?authHandler.getDateFormatter(request).format(batchSerial.getExpfromdate()):"");
            obj.put("expend",batchSerial.getExptodate()!=null? authHandler.getDateFormatter(request).format(batchSerial.getExptodate()):"");
            if (isFixedAssetDO) {
                obj.put("purchaseserialid", batchSerial.getId());
            } 
            jSONArray.put(obj);

        }
        if (isBatchForProduct && !isSerialForProduct) //only in batch case
          {
              JSONObject Jobj = new JSONObject();
              Jobj = getOnlyBatchDetail(productBatch, request);
              if (isFixedAssetDO) {
                  purchasebatchid = productBatch.getId();
              }
              if (!StringUtil.isNullOrEmpty(purchasebatchid)) {
                  Jobj.put("purchasebatchid", purchasebatchid);
              }
              jSONArray.put(Jobj);
          }

          return jSONArray.toString();
    }
         public JSONObject getOnlyBatchDetail(ProductBatch productBatch, HttpServletRequest request) throws JSONException, SessionExpiredException {

        JSONObject obj = new JSONObject();
        obj.put("id", productBatch.getId());
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
        obj.put("expend","");
        return obj;
    }
    
    public double getSalesOrderBalanceQuantity(SalesOrderDetail salesOrderDetail) {
        double result = 0;
        try {


            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), salesOrderDetail.getSalesOrder().getCompany().getCompanyID());
            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

            boolean fullInv = false;
            double qua = 0;
            double quantPartTt = salesOrderDetail.getQuantity() * 100;
            double quantPartTtInv = 0.0;
            if (pref.isWithInvUpdate()) { //In Trading Flow                 
                KwlReturnObject doresult = accInvoiceDAOobj.getDeliveryOrderDFromSOD(salesOrderDetail.getID(), pref.getCompany().getCompanyID());
                List list = doresult.getEntityList();
                if (list.size() > 0) {
                    Iterator ite1 = list.iterator();
                    while (ite1.hasNext()) {
                        String orderid = (String) ite1.next();
                        KwlReturnObject res = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), orderid);
                        DeliveryOrderDetail deliveryOrderDetail = (DeliveryOrderDetail) res.getEntityList().get(0);
                        fullInv = true;
                        qua += deliveryOrderDetail.getDeliveredQuantity();
                    }
                }
            } else { //In Non Trading Flow 

                KwlReturnObject idresult = accInvoiceDAOobj.getIDFromSOD(salesOrderDetail.getID());
                List list = idresult.getEntityList();
                Iterator ite1 = list.iterator();
                while (ite1.hasNext()) {
                    InvoiceDetail ge = (InvoiceDetail) ite1.next();
                    if (ge.getInvoice().isPartialinv()) {
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
                result = qua;

            } else if (quantPartTt > quantPartTtInv) {
                result = quantPartTtInv;

            }

        } catch (Exception ex) {
        }
        return result;
    }
    //getting the quotations  details when selecting the quotation in invoice  

    public JSONObject getQuotations(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);

            if (StringUtil.isNullOrEmpty(request.getParameter("archieve"))) {
                requestParams.put("archieve", 0);
            } else {
                requestParams.put("archieve", Integer.parseInt(request.getParameter("archieve")));
            }
            boolean consolidateFlag = request.getParameter("consolidateFlag") != null ? Boolean.parseBoolean(request.getParameter("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && request.getParameter("companyids") != null) ? request.getParameter("companyids").split(",") : sessionHandlerImpl.getCompanyid(request).split(",");
            String gcurrencyid = (consolidateFlag && request.getParameter("gcurrencyid") != null) ? request.getParameter("gcurrencyid") : sessionHandlerImpl.getCurrencyID(request);
            boolean eliminateflag = consolidateFlag;
            if (consolidateFlag) {
                requestParams.put(Constants.start, "");
                requestParams.put(Constants.limit, "");
            }
            String dir = "";
            String sort = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                dir = request.getParameter("dir");
                sort = request.getParameter("sort");
                requestParams.put("sort", sort);
                requestParams.put("dir", dir);
            }
            KwlReturnObject result = null;
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                request.setAttribute("companyid", companyid);
                request.setAttribute("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);

                result = accSalesOrderDAOobj.getQuotations(requestParams);
                DataJArr = getQuotationsJson(request, result.getEntityList(), DataJArr);
            }
            int cnt = consolidateFlag ? DataJArr.length() : result.getRecordTotalCount();
            JSONArray pagedJson = DataJArr;
            if (consolidateFlag) {
                String start = request.getParameter(Constants.start);
                String limit = request.getParameter(Constants.limit);
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
                }
            }
            jobj.put("data", pagedJson);
            jobj.put("count", cnt);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccSalesOrderAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccSalesOrderAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public JSONArray getQuotationsJson(HttpServletRequest request, List list, JSONArray jArr) throws ServiceException {
//        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = getSalesOrdersMap(request);
            String currencyid = (String) requestParams.get("gcurrencyid");
            String companyid = (String) requestParams.get("companyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();

            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Customer_Quotation_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);


            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Quotation salesOrder = (Quotation) itr.next();
                KWLCurrency currency = null;

                if (salesOrder.getCurrency() != null) {
                    currency = salesOrder.getCurrency();
                } else {
                    currency = salesOrder.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : salesOrder.getCustomer().getAccount().getCurrency();
                }
                //KWLCurrency currency=salesOrder.getCustomer().getAccount().getCurrency()==null?kwlcurrency:salesOrder.getCustomer().getAccount().getCurrency();
                Customer customer = salesOrder.getCustomer();
                JSONObject obj = new JSONObject();
                obj.put("billid", salesOrder.getID());
                obj.put("companyid", salesOrder.getCompany().getCompanyID());
                obj.put("companyname", salesOrder.getCompany().getCompanyName());
                obj.put("personid", customer.getID());
                obj.put("personemail", salesOrder.getCustomer() == null ? "" : salesOrder.getCustomer().getEmail());
                obj.put("billno", salesOrder.getquotationNumber());
                obj.put("duedate", authHandler.getDateFormatter(request).format(salesOrder.getDueDate()));
                obj.put("date", authHandler.getDateFormatter(request).format(salesOrder.getQuotationDate()));
                obj.put("shipdate", salesOrder.getShipdate() == null ? "" : authHandler.getDateFormatter(request).format(salesOrder.getShipdate()));
                obj.put("validdate", salesOrder.getValiddate() == null ? "" : authHandler.getDateFormatter(request).format(salesOrder.getValiddate()));
                obj.put("shipvia", salesOrder.getShipvia() == null ? "" : salesOrder.getShipvia());
                obj.put("fob", salesOrder.getFob() == null ? "" : salesOrder.getFob());
                obj.put("archieve", salesOrder.getArchieve());
                obj.put("billto", salesOrder.getBillTo());
                obj.put("shipto", salesOrder.getShipTo());
                obj.put("deleted", salesOrder.isDeleted());
                obj.put("salesPerson", salesOrder.getSalesperson() != null ? salesOrder.getSalesperson().getID() : "");
                obj.put("isfavourite", salesOrder.isFavourite());
                obj.put("isprinted", salesOrder.isPrinted());
                obj.put("termdetails", getTermDetails(salesOrder.getID(), false));
                obj.put("discountval", (salesOrder.getDiscount() == 0) ? 0 : salesOrder.getDiscount());

                BillingShippingAddresses addresses = salesOrder.getBillingShippingAddresses();
                obj.put(Constants.BILLING_ADDRESS, addresses == null ? (salesOrder.getBillTo() == null ? "" : salesOrder.getBillTo()) : addresses.getBillingAddress());
                obj.put(Constants.BILLING_CITY, addresses == null ? "" : addresses.getBillingCity());
                obj.put(Constants.BILLING_CONTACT_PERSON, addresses == null ? "" : addresses.getBillingContactPerson());
                obj.put(Constants.BILLING_CONTACT_PERSON_NUMBER, addresses == null ? "" : addresses.getBillingContactPersonNumber());
                obj.put(Constants.BILLING_CONTACT_PERSON_DESIGNATION, addresses == null ? "" : addresses.getBillingContactPersonDesignation());
                obj.put(Constants.BILLING_COUNTRY, addresses == null ? "" : addresses.getBillingCountry());
                obj.put(Constants.BILLING_EMAIL, addresses == null ? "" : addresses.getBillingEmail());
                obj.put(Constants.BILLING_FAX, addresses == null ? "" : addresses.getBillingFax());
                obj.put(Constants.BILLING_MOBILE, addresses == null ? "" : addresses.getBillingMobile());
                obj.put(Constants.BILLING_PHONE, addresses == null ? "" : addresses.getBillingPhone());
                obj.put(Constants.BILLING_POSTAL, addresses == null ? "" : addresses.getBillingPostal());
                obj.put(Constants.BILLING_STATE, addresses == null ? "" : addresses.getBillingState());
                obj.put(Constants.BILLING_ADDRESS_TYPE, addresses == null ? "" : addresses.getBillingAddressType());
                obj.put(Constants.SHIPPING_ADDRESS, addresses == null ? (salesOrder.getShipTo() == null ? "" : salesOrder.getShipTo()) : addresses.getShippingAddress());
                obj.put(Constants.SHIPPING_CITY, addresses == null ? "" : addresses.getShippingCity());
                obj.put(Constants.SHIPPING_CONTACT_PERSON, addresses == null ? "" : addresses.getShippingContactPerson());
                obj.put(Constants.SHIPPING_CONTACT_PERSON_NUMBER, addresses == null ? "" : addresses.getShippingContactPersonNumber());
                obj.put(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION, addresses == null ? "" : addresses.getShippingContactPersonDesignation());
                obj.put(Constants.SHIPPING_COUNTRY, addresses == null ? "" : addresses.getShippingCountry());
                obj.put(Constants.SHIPPING_EMAIL, addresses == null ? "" : addresses.getShippingEmail());
                obj.put(Constants.SHIPPING_FAX, addresses == null ? "" : addresses.getShippingFax());
                obj.put(Constants.SHIPPING_MOBILE, addresses == null ? "" : addresses.getShippingMobile());
                obj.put(Constants.SHIPPING_PHONE, addresses == null ? "" : addresses.getShippingPhone());
                obj.put(Constants.SHIPPING_POSTAL, addresses == null ? "" : addresses.getShippingPostal());
                obj.put(Constants.SHIPPING_STATE, addresses == null ? "" : addresses.getShippingState());
                obj.put(Constants.SHIPPING_ADDRESS_TYPE, addresses == null ? "" : addresses.getShippingAddressType());
                boolean incProTax = false;
                Iterator itrRow = salesOrder.getRows().iterator();
                double amount = 0, amountinbase = 0, totalDiscount = 0, discountPrice = 0;
                while (itrRow.hasNext()) {
                    QuotationDetail sod = (QuotationDetail) itrRow.next();
                    //amount+=sod.getQuantity()*sod.getRate();
                    double rowTaxPercent = 0;
                    if (sod.getTax() != null) {
                        requestParams.put("transactiondate", salesOrder.getQuotationDate());
                        requestParams.put("taxid", sod.getTax().getID());
                        KwlReturnObject result = accTaxObj.getTax(requestParams);
                        List taxList = result.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        incProTax = true;
                    }

                    //discountPrice = (sod.getQuantity() * sod.getRate()) - (sod.getQuantity() * sod.getRate() * sod.getDiscount()/100);
                    KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, sod.getRate(), currency.getCurrencyID(), salesOrder.getQuotationDate(), 0);
                    double sorate = sod.getRate();//(Double) bAmt.getEntityList().get(0);

                    double quotationPrice = authHandler.round(sod.getQuantity() * sorate, companyid);
                    if (sod.getDiscountispercent() == 1) {
                        discountPrice = (quotationPrice) - (quotationPrice * sod.getDiscount() / 100);
                    } else {
                        discountPrice = quotationPrice - sod.getDiscount();
                    }


                    //amount = amount - (sod.getQuantity() * sod.getRate() * sod.getDiscount()/100);
                    amount += discountPrice + sod.getRowTaxAmount();//amount += discountPrice + (discountPrice * rowTaxPercent/100);
                }
                obj.put("includeprotax", incProTax);
                if(salesOrder.getModifiedby()!=null){
                        obj.put("lasteditedby",StringUtil.getFullName(salesOrder.getModifiedby()));
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
                obj.put("ispercentdiscount", salesOrder.isPerDiscount());
                if (salesOrder.getTax() != null) {
                    requestParams.put("transactiondate", salesOrder.getQuotationDate());
                    requestParams.put("taxid", salesOrder.getTax().getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    double TaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    amountinbase = amount + amount * TaxPercent / 100;
                }
//                if(salesOrder.getTax() != null)
//                	obj.put("amountinbase", amountinbase);
//                else{
//                	obj.put("amountinbase", amount);
//                }	
                obj.put("amount", amount);
//                KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currency.getCurrencyID(), salesOrder.getQuotationDate(), 0);
                obj.put("currencysymbol", currency.getSymbol());
                obj.put("taxid", salesOrder.getTax() == null ? "" : salesOrder.getTax().getID());
                obj.put("taxname", salesOrder.getTax() == null ? "" : salesOrder.getTax().getName());
                double taxPercent = 0;
                double totalTermAmount = 0;
                HashMap<String, Object> requestParam = new HashMap();
                requestParam.put("quotation", salesOrder.getID());
                KwlReturnObject quotationResult = null;
                quotationResult = accSalesOrderDAOobj.getQuotationTermMap(requestParam);
                List<QuotationTermMap> termMap = quotationResult.getEntityList();
                for (QuotationTermMap quotationTermMap : termMap) {
                    InvoiceTermsSales mt = quotationTermMap.getTerm();
                    double termAmnt = quotationTermMap.getTermamount();
                    totalTermAmount += termAmnt;
                }

                if (salesOrder.getTax() != null) {
                    requestParams.put("transactiondate", salesOrder.getQuotationDate());
                    requestParams.put("taxid", salesOrder.getTax().getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];

                }
                double orderAmount = amount;//(Double) bAmt.getEntityList().get(0);
                KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amount, currency.getCurrencyID(), salesOrder.getQuotationDate(), 0);
                amountinbase = (Double) bAmt.getEntityList().get(0);

                double ordertaxamount = (taxPercent == 0 ? 0 : orderAmount * taxPercent / 100);
                double ordertaxamountBase = (taxPercent == 0 ? 0 : amountinbase * taxPercent / 100);
                amountinbase += totalTermAmount;
                obj.put("amountinbase", amountinbase + ordertaxamountBase);
                obj.put("taxpercent", taxPercent);
                obj.put("taxamount", ordertaxamount);
                amount += totalTermAmount;
                orderAmount += totalTermAmount;
                obj.put("orderamount", orderAmount);
                obj.put("orderamountwithTax", orderAmount + ordertaxamount);
                obj.put("currencyid", currency.getCurrencyID());
                obj.put("personname", customer.getName());
                obj.put("memo", salesOrder.getMemo());
                obj.put("posttext", salesOrder.getPostText());

                KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(QuotationCustomData.class.getName(), salesOrder.getID());
                if (custumObjresult.getEntityList().size() > 0) {
                    DateFormat df = (DateFormat) requestParams.get("df");
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    QuotationCustomData quotationCustomData = (QuotationCustomData) custumObjresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(quotationCustomData, FieldMap, replaceFieldMap, variableMap);
                    DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                    Date dateFromDB=null;
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {

                        String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                        if (customFieldMap.containsKey(varEntry.getKey())) {
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                            FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                            if (fieldComboData != null) {
                                obj.put(varEntry.getKey(), fieldComboData.getValue() != null ? fieldComboData.getValue() : "");
                            }
                        } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                dateFromDB = defaultDateFormat.parse(coldata);
                                coldata = sdf.format(dateFromDB);
                            } catch (Exception e) {
                            }
                            obj.put(varEntry.getKey(),coldata);
                        } else {
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                obj.put(varEntry.getKey(), coldata);
                            }
                        }
                    }
                }
                jArr.put(obj);
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
            Logger.getLogger(AccSalesOrderAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
//on selecting quotation number
    public JSONObject getQuotationRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Customer_Quotation_ModuleId, 1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);

            jobj = getQuotationRows(request, fieldMap);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccSalesOrderAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = "AccSalesOrderAccountingServiceImpl.getQuotationRows:" + ex.getMessage();
            Logger.getLogger(AccSalesOrderAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "AccSalesOrderAccountingServiceImpl.getQuotationRows:" + ex.getMessage();
            Logger.getLogger(AccSalesOrderAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccSalesOrderAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }//getQuotationRows

    public JSONObject getQuotationRows(HttpServletRequest request, HashMap<String, Integer> fieldMap) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), sessionHandlerImpl.getCurrencyID(request));
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

            String[] sos = (String[]) request.getParameter("bills").split(",");
            String dType = request.getParameter("dtype");
            boolean isOrder = false;
            String isorder = request.getParameter("isOrder");
            if (!StringUtil.isNullOrEmpty(isorder) && StringUtil.equal(isorder, "true")) {
                isOrder = true;
            }
            boolean isReport = false;
            if (!StringUtil.isNullOrEmpty(dType) && StringUtil.equal(dType, "report")) {
                isReport = true;
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("copyInvoice")) && Boolean.parseBoolean(request.getParameter("copyInvoice"))) {
                isReport = true;
            }
            int i = 0;
            JSONArray jArr = new JSONArray();
            int addobj = 1;

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
                    obj.put("billid", so.getID());
                    obj.put("billno", so.getquotationNumber());
                    obj.put("currencysymbol", currency.getSymbol());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("productid", row.getProduct().getID());
                    obj.put("productname", row.getProduct().getName());
                    String uom = row.getUom() != null ? row.getUom().getNameEmptyforNA() : row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                    obj.put("unitname", uom);
                    obj.put("uomname", uom);
                    obj.put("baseuomname", row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("multiuom", row.getProduct().isMultiuom());
                    obj.put("desc", StringUtil.isNullOrEmpty(row.getDescription()) ? row.getProduct().getDescription() : row.getDescription());
                    obj.put("type", row.getProduct().getProducttype() == null ? "" : row.getProduct().getProducttype().getName());
                    obj.put("pid", row.getProduct().getProductid());
                    obj.put("memo", row.getRemark());
                    //obj.put("discountispercent", row.getDiscountispercent());
                    //obj.put("prdiscount", row.getDiscount());
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
                    obj.put("prtaxpercent", rowTaxPercent);
                    obj.put("rowTaxAmount", rowTaxAmount);
                    obj.put("prtaxid", row.getTax() == null ? "" : row.getTax().getID());

                    if (!isReport && row.getDiscount() > 0 && isOrder) {//In Sales order creation, we need to display Unit Price including row discount
                        double discount = (row.getDiscountispercent() == 1) ? (row.getRate() * (row.getDiscount() / 100)) : row.getDiscount();
                        obj.put("rate", (row.getRate() - discount));
                        obj.put("discountispercent", 1);
                        obj.put("prdiscount", 0);
                    } else {
                        obj.put("rate", row.getRate());
                        obj.put("discountispercent", row.getDiscountispercent());
                        obj.put("prdiscount", row.getDiscount());
                    }

                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    QuotationDetailCustomData quotationDetailCustomData = (QuotationDetailCustomData) row.getQuotationDetailCustomData();
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    AccountingManager.setCustomColumnValues(quotationDetailCustomData, fieldMap, replaceFieldMap, variableMap);
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                            FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                            if (fieldComboData != null) {
                                if (isReport) {
                                    obj.put(varEntry.getKey(), fieldComboData.getValue() != null ? fieldComboData.getValue() : "");//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                                } else {
                                    obj.put(varEntry.getKey(), coldata != null ? coldata : "");
                                }
                            } else {
                                obj.put(varEntry.getKey(), coldata != null ? coldata : "");
                            }
                        }
                    }

                    KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), so.getQuotationDate(), 0);
                    obj.put("orderrate", row.getRate());//obj.put("orderrate", (Double) bAmt.getEntityList().get(0));

                    obj.put("quantity", row.getQuantity());
                    if (row.getUom() != null) {
                        obj.put("uomid", row.getUom().getID());
                        obj.put("baseuomquantity", row.getBaseuomquantity());
                        obj.put("baseuomrate", row.getBaseuomrate());
                    } else {
                        obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
                        obj.put("baseuomquantity", row.getBaseuomquantity());
                        obj.put("baseuomrate", row.getBaseuomrate());
                    }
                    if (addobj > 0) {
                        jArr.put(obj);
                    }
                }
                i++;
                jobj.put("data", jArr);
            }
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }
    
    // This method used only for syncing quotation from CRM to ERP
    public JSONObject saveQuotation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String billno = "";
        String billid = "";
        String amount = "";
        boolean issuccess = false;
        boolean isAttachNewDocsForExistingSyncedQuotation = false; //ERP-39422 : This varible will be true when CRM customer upload attachment to already synced quotation.
        String butPendingForApproval = "";
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("Quotation_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = txnManager.getTransaction(def);
        JSONArray array=new JSONArray();
        JSONArray rejectedArray=new JSONArray();
        try {
            String currentUser = sessionHandlerImpl.getUserid(request);
            if(!StringUtil.isNullOrEmpty(request.getParameter("isAttachNewDocsForExistingSyncedQuotation"))){
                isAttachNewDocsForExistingSyncedQuotation = Boolean.parseBoolean(request.getParameter("isAttachNewDocsForExistingSyncedQuotation"));
            }
            JSONArray jarr = new JSONArray(request.getParameter("quotationdata"));
            for (int i = 0; i < jarr.length(); i++) {
                JSONObject dataMap = jarr.getJSONObject(i);
                JSONObject rejObj = new JSONObject();
                    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                    def1.setName("Quotation_Tx");
                    def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                    TransactionStatus status = txnManager.getTransaction(def1);
                try {
                    JSONObject jSONObject = new JSONObject();
                    //Before calling save method we need to check wheather produts and customer exist in ERP or NOT
                    boolean isCustomerandProductExist = checkProductCustomerExistInERP(dataMap);
                    if (isCustomerandProductExist) {
                        Quotation quotation = saveQuotation(request, dataMap);
                        quotation.setApprovestatuslevel(11);
                        jSONObject.put("entryNumber", dataMap.optString("quotationnumber"));
                        jSONObject.put("erpQuotationId", quotation.getID());
                        jSONObject.put("crmQuotationId", dataMap.optString("quotationid", null));
                        array.put(jSONObject);
                    } else if (isAttachNewDocsForExistingSyncedQuotation) {
                        String crmdocid = "";
                        String crmdocids = "";
                        /*ERP-39422
                         *Below piece of code has written to sync attached docs of already synced quotation.
                         *Suppose, one of the CRM quotation is already synced with ERP. Later CRM user uploaded some docs to that quotation.
                         *In such scenario, recently uploaded doc in CRM should be auto-sync to ERP without clicking on Quotation Sync button in CRM.
                         *Below code has written by considering that From CRM To ERP, only one document can be uploaded at a time. This code will get execute at the
                         *time of document uploading in CRM.
                        */
                        /*---------  Save Documnets related Data--------*/
                        JSONArray fileNameList = dataMap.getJSONArray("shareddocs");
                        for (int id = 0; id < fileNameList.length(); id++) {
                            crmdocid += "'" + fileNameList.getJSONObject(id).getString("docid") + "',"; /*--Provide docid from CRM side to ERP side in crmdocid column--*/

                        }
                        if (crmdocid.length() > 0) {
                            crmdocids = crmdocid.substring(0, crmdocid.length() - 1);        /*--Save all crm document id(s)*/

                        }

                        /*----Save New Documents information----*/
                        for (int doccnt = 0; doccnt < fileNameList.length(); doccnt++) {
                            String companyid = fileNameList.getJSONObject(doccnt).getString("companyid");
                            String crmquotationid = fileNameList.getJSONObject(doccnt).getString("quotationid");    //CRM Quotation ID
                            HashMap<String, Object> requestParamsNew = new HashMap<String, Object>();
                            requestParamsNew.put("crmquoatationid", crmquotationid);
                            requestParamsNew.put("nondeleted", "true");
                            requestParamsNew.put("companyid", companyid);
                            KwlReturnObject quoteResult = accSalesOrderDAOobj.getQuotations(requestParamsNew);
                            String quotationId = (quoteResult.getEntityList() != null && quoteResult.getEntityList().size() > 0) ? (String) quoteResult.getEntityList().get(0) : null;  //ERP Quotation ID
                            if (quotationId != null) {
                                InvoiceDocuments document = new InvoiceDocuments();         //new document invoice
                                String docid = UUID.randomUUID().toString();
                                document.setDocID(docid);//uuid
                                document.setDocName(fileNameList.getJSONObject(doccnt).getString("documentname"));//File Name
                                document.setDocType("");//document type 
                                document.setCrmDocumentID(fileNameList.getJSONObject(doccnt).getString("docid"));//Document Id

                                InvoiceDocumentCompMap invoiceDocumentMap = new InvoiceDocumentCompMap();//Mapping of New Document
                                invoiceDocumentMap.setDocument(document);

                                KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                                Company company = (Company) cmp.getEntityList().get(0);
                                invoiceDocumentMap.setCompany(company);
                                invoiceDocumentMap.setInvoiceID(quotationId);         //Set ERP Quotation Id to document

                                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                                hashMap.put("InvoceDocument", document);
                                hashMap.put("InvoiceDocumentMapping", invoiceDocumentMap);
                                accInvoiceDAOobj.saveinvoiceDocuments(hashMap);    //Save Documents
                            }

                        }
                    } else {
                        /**
                         * array of rejected quotations due to product not synced
                         */
                        rejObj.put("rejquotationnumber", dataMap.optString("quotationnumber"));
                        rejObj.put("rejquotationid", dataMap.optString("quotationid"));
                        rejObj.put("auditmsg", "Cannot be sync to ERP because products not synced from ERP");
                        rejectedArray.put(rejObj);
                    }
                   txnManager.commit(status);
                } catch (Exception e) {
                    txnManager.rollback(status);
                    /**
                     * array of rejected quotations which are belong to auto sequence
                     * format sent to CRM side.
                     */
                    boolean cannotsyncquotation = dataMap.optBoolean("cannotsyncquotation",false);
                    if (cannotsyncquotation) {
                        rejObj.put("rejquotationnumber", dataMap.optString("quotationnumber"));
                        rejObj.put("rejquotationid", dataMap.optString("rejquotationid"));
                        rejObj.put("auditmsg", dataMap.optString("auditmsg"));
                        rejectedArray.put(rejObj);
                    }
                    Logger.getLogger(AccSalesOrderAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, e);
                }

            }
//            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.so.save1", null, RequestContextUtils.getLocale(request));
        } catch (Exception ex) {
//            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(AccSalesOrderAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put(Constants.RES_MESSAGE, msg);
                jobj.put("billid", billid);
                jobj.put("billno", billno);
                jobj.put("amount", "");
                jobj.put("quotationdata", array);
                jobj.put("rejectedquotationdata", rejectedArray);
            } catch (JSONException ex) {
                Logger.getLogger(AccSalesOrderAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    public boolean checkProductCustomerExistInERP(JSONObject dataMap) throws JSONException, ServiceException {
        //Checking customer existence
        Customer customer = null;
        String customerid = dataMap.optString("erpcustomerid", "");
        if (!StringUtil.isNullOrEmpty(customerid)) {
            KwlReturnObject rst = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerid);
            customer = (Customer) rst.getEntityList().get(0);
        }
        if (customer == null) { //If customer does not exist in ERP then we can not preceed with such quotation hence returning false.
            return false;
        }

        //Checking product existence
        Product product = null;
        String details = dataMap.optString("quotationproducts", "");
        if (!StringUtil.isNullOrEmpty(details)) {
            JSONArray jArr = new JSONArray(details);
            for (int recordCnt = 0; recordCnt < jArr.length(); recordCnt++) {
                JSONObject prdObj = jArr.getJSONObject(recordCnt);
                product = null;//initializing with null after each loop
                String productid = prdObj.optString("productid", "");
                if (!StringUtil.isNullOrEmpty(productid)) {
                    KwlReturnObject rst = accountingHandlerDAOobj.getObject(Product.class.getName(), productid);
                    product = (Product) rst.getEntityList().get(0);
                }
                if (product == null) {
                    break;//if any product not found then no need to check for other details product
                }
            }
        }
        if (product == null) { //If customer does not exist in ERP then we can not preceed with such quotation hence returning false.
            return false;
        }
        return true;
    }

    //Below saveQuotation method used only for syncing quotation from CRM to ERP
    public Quotation saveQuotation(HttpServletRequest request, JSONObject dataMap) throws SessionExpiredException, ServiceException, AccountingException {
        Quotation quotation = null;
        String auditMsg = "", auditID = "";
        try {
            int istemplate = dataMap.has("istemplate") ? Integer.parseInt(dataMap.optString("istemplate", null)) : 0;
            String taxid = null;
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            taxid = dataMap.optString("taxid", null);
            String sequenceformat = dataMap.optString("sequenceformat", "NA");
            double taxamount = StringUtil.getDouble(dataMap.optString("taxamount", null));
            double externalCurrencyRate = StringUtil.getDouble(dataMap.optString("externalcurrencyrate", null));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean isLinkedFromReplacementNumber = (!StringUtil.isNullOrEmpty(dataMap.optString("isLinkedFromReplacementNumber", null))) ? Boolean.parseBoolean(dataMap.optString("isLinkedFromReplacementNumber", null)) : false;
            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);
            boolean isLeaseFixedAsset = (!StringUtil.isNullOrEmpty(dataMap.optString("isLeaseFixedAsset", null))) ? Boolean.parseBoolean(dataMap.optString("isLeaseFixedAsset", null)) : false;
            String quotationType = ((!StringUtil.isNullOrEmpty(dataMap.optString("quotationType"))) ? dataMap.optString("quotationType") : "0");
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            String entryNumber = dataMap.optString("quotationnumber", null);
            String crmquoatationid = dataMap.optString("quotationid", null);
            String costCenterId = dataMap.optString("costcenter", null);
            String quotationId = dataMap.optString("invoiceid", null);
            String shipLength = dataMap.optString("shipLength", null);
            String invoicetype = dataMap.optString("invoicetype", null);
            String date = dataMap.optString("quotationdateStr");
            Date quotationDate = df.parse(date);
            String nextAutoNumber = "";
            Customer customer = null;
            String customerid = dataMap.getString("erpcustomerid");
            KwlReturnObject rst = accountingHandlerDAOobj.getObject(Customer.class.getName(), customerid);
            customer = (Customer) rst.getEntityList().get(0);          
            String paymentterm = StringUtil.isNullOrEmpty(dataMap.optString("paymenttermid", null)) ? "" : dataMap.optString("paymenttermid", null);
            HashMap<String, Object> requestParamsForTerms = new HashMap<String, Object>();
            requestParamsForTerms.put("companyid", companyid);
            requestParamsForTerms.put("crmtermid", paymentterm);
            Term term = null;
            Company companyObj = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getCanonicalName(), companyid);
            KwlReturnObject podresult1 = accSalesOrderDAOobj.getTermMappedToCrmQuotation(requestParamsForTerms);
            Iterator itr1 = podresult1.getEntityList().iterator();
            while (itr1.hasNext()) {
                term = (Term) itr1.next();
            }
            if(term==null){
                term=customer.getCreditTerm();
            }
//            boolean isEdit = Boolean.parseBoolean(dataMap.optString("sendToErpFlag"));
//            if(isEdit) {
                HashMap<String, Object> requestParamsNew = new HashMap<String, Object>();
                requestParamsNew.put("crmquoatationid", crmquoatationid);
//                requestParamsNew.put("entrynumber", entryNumber);
//                requestParamsNew.put(Constants.REQ_customerId, customerid);
//                requestParamsNew.put("archieve", 0);
                requestParamsNew.put("nondeleted", "true");
                requestParamsNew.put("companyid", companyid);
                if (quotationType.equalsIgnoreCase("1")) {
                    requestParamsNew.put("isLeaseFixedAsset", true);
                }
                KwlReturnObject quoteResult = accSalesOrderDAOobj.getQuotations(requestParamsNew);
                quotationId = (quoteResult.getEntityList()!=null && quoteResult.getEntityList().size()>0) ? (String) quoteResult.getEntityList().get(0) : null;
                if (quotationId != null) {
                    HashMap<String, Object> requestParams1 = checkQuotationUsages(quotationId);
                    if (requestParams1.size() > 0 && ((Boolean) requestParams1.get("isquotationused"))) {
                        String moduleName = (String) requestParams1.get("module");
                        String audit_msg = "Cannot be sync to ERP because Quotation "+entryNumber+" currently used.";//messageSource.getMessage("acc.field.alreadyusedintransactions.crmquotations", null, entryNumber, RequestContextUtils.getLocale(request));
                        dataMap.put("cannotsyncquotation", true);
                        dataMap.put("rejquotationnumber", entryNumber);
                        dataMap.put("rejquotationid", crmquoatationid);
                        dataMap.put("auditmsg", audit_msg);
                        throw new AccountingException(messageSource.getMessage("acc.field.CustomerQuotation(s)", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyusedintransactions.", new Object[]{moduleName}, RequestContextUtils.getLocale(request)));
                    }
                }
//            }
            HashMap<String, Object> qDataMap = new HashMap<String, Object>();
            String currencyid = (dataMap.optString("quotationcurrency", null) == null ? sessionHandlerImpl.getCurrencyID(request) : dataMap.optString("quotationcurrency", null));
            synchronized (this) {
                SequenceFormat prevSeqFormat = null;
                if (!StringUtil.isNullOrEmpty(quotationId)) { //For edit case check duplicate
                    KwlReturnObject socnt = accSalesOrderDAOobj.getEditQuotationCount(entryNumber, companyid, quotationId);
                    int count = socnt.getRecordTotalCount();
                    if (count > 0 && istemplate != 2 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Quotationnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    } else {
                        rst = accountingHandlerDAOobj.getObject(Quotation.class.getName(), quotationId);
                        Quotation q = (Quotation) rst.getEntityList().get(0);
                        prevSeqFormat = q.getSeqformat();
                        if (!sequenceformat.equals("NA")) {
                            nextAutoNumber = entryNumber;
                        }
                        qDataMap.put("id", quotationId);
                        accSalesOrderDAOobj.deleteQuotationDetails(quotationId, companyid);
                    }
                } else {   //for ceate new chech duplicate
                    KwlReturnObject socnt = accSalesOrderDAOobj.getQuotationCount(entryNumber, companyid);
                    int count = socnt.getRecordTotalCount();
                    if (count > 0 && istemplate != 2 && sequenceformat.equals("NA")) {
                        String audit_msg = "Cannot be sync to ERP because Quotation "+entryNumber+" already exists.";//messageSource.getMessage("acc.field.alreadyusedintransactions.crmquotations", null, entryNumber, RequestContextUtils.getLocale(request));
                        dataMap.put("cannotsyncquotation", true);
                        dataMap.put("rejquotationnumber", entryNumber);
                        dataMap.put("rejquotationid", crmquoatationid);
                        dataMap.put("auditmsg", audit_msg);
                        throw new AccountingException(messageSource.getMessage("acc.field.Quotationnumber", null, RequestContextUtils.getLocale(request)) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                }

                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this entry number can also be generated by a sequence format or not
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Customer_Quotation_ModuleId, entryNumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        if (!isvalidEntryNumber) {
                            String audit_msg = "Quotation "+entryNumber+" "+"Cannot be sync to ERP because "+messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " " + entryNumber + " " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " " + formatName + " Please change the Sequence Format or Quotation Number from CRM side.";
                            dataMap.put("cannotsyncquotation", true);
                            dataMap.put("rejquotationnumber", entryNumber);
                            dataMap.put("rejquotationid", crmquoatationid);
                            dataMap.put("auditmsg", audit_msg);
//                            auditTrailObj.insertAuditLog(auditID, msg, request, entryNumber,entryNumber);
                            /**
                             * Added audit entry while syncing the quotation from CRM to ERP and exception thrown that 
                             * Quotation Cannot be sync to ERP because document number belongs to auto sequence format. 
                             */
                            auditTrailObj.insertAuditLog(auditID,audit_msg, request, "" + Constants.CUSTOMER_QUOTATION);
                            throw new AccountingException(audit_msg);
                        }
                    }
                }

                if (!sequenceformat.equals("NA") && prevSeqFormat == null) { //to generate new sequence number
                    boolean seqformat_oldflag = StringUtil.getBoolean(dataMap.optString("seqformat_oldflag", null));
                    String nextAutoNoInt = "";
                    String datePrefix = "";
                    String dateSuffix = "";
                    if (seqformat_oldflag) {
                        nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_QUOTATION, sequenceformat);
                    } else {
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_QUOTATION, sequenceformat, seqformat_oldflag, quotationDate);
                        nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                        datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part

                        qDataMap.put(Constants.SEQFORMAT, sequenceformat);
                        qDataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                        qDataMap.put(Constants.DATEPREFIX, datePrefix);
                        qDataMap.put(Constants.DATESUFFIX, dateSuffix);
                    }
                    entryNumber = nextAutoNumber;
                }
            }

//            DateFormat df = authHandler.getDateFormatter(request);
            if (!StringUtil.isNullOrEmpty(quotationId)) {//Edit PO Case for updating address detail
                auditMsg = "updated";
                auditID = AuditAction.CUSTOMER_QUOTATION_UPDATED;
                Map<String, Object> addressParams = new HashMap<String, Object>();
                if (dataMap.has("billingAddress") && dataMap.has("shippingAddress")) {  
                    addressParams = getAddressParams(dataMap);
                } else {
                    addressParams = AccountingAddressManager.getDefaultCustomerAddressParams(customer.getID(), companyid, accountingHandlerDAOobj);
                }
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Quotation.class.getName(), quotationId);
                Quotation quotation1 = (Quotation) returnObject.getEntityList().get(0);
                    addressParams.put("id", quotation1.getBillingShippingAddresses() == null ? "" : quotation1.getBillingShippingAddresses().getID());
                    KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                    BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                    qDataMap.put("billshipAddressid", bsa.getID());
            } else { //Other Cases for saving address detail
                auditMsg = "added";
                auditID = AuditAction.CUSTOMER_QUOTATION_ADDED;                
                Map<String, Object> addressParams = new HashMap<String, Object>();
                if (dataMap.has("billingAddress") && dataMap.has("shippingAddress") ) {
                     addressParams = getAddressParams(dataMap);                   
                } else {
                     addressParams =AccountingAddressManager.getDefaultCustomerAddressParams(customer.getID(), companyid, accountingHandlerDAOobj); //getCustomerDefaultAddressParams(customer,companyid);
                }
                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams, companyid);
                BillingShippingAddresses bsa = (BillingShippingAddresses) addressresult.getEntityList().get(0);
                qDataMap.put("billshipAddressid", bsa.getID());
            }
            if(dataMap.has("quotationOwnerId") && !StringUtil.isNullOrEmpty(dataMap.getString("quotationOwnerId"))) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("companyid", companyid);
                requestParams.put("user", dataMap.getString("quotationOwnerId"));
                KwlReturnObject retObj = accSalesOrderDAOobj.getSalesPersons(requestParams);
                MasterItem salesPerson = (MasterItem) (retObj.getEntityList()!=null && retObj.getEntityList().size()>0 ? retObj.getEntityList().get(0) : null);
                qDataMap.put("salesPerson", salesPerson!=null ? salesPerson.getID() : null);
            }
            qDataMap.put("externalCurrencyRate", externalCurrencyRate);
            qDataMap.put("entrynumber", entryNumber);
            qDataMap.put("crmquoatationid", crmquoatationid);
            qDataMap.put("autogenerated", nextAutoNumber.equals(entryNumber));
            qDataMap.put("memo", dataMap.optString("memo", null));
            qDataMap.put("posttext", dataMap.optString("posttext", null));
            qDataMap.put("customerid", customer.getID());
            qDataMap.put("termid", term!=null?term.getID():"");
//            Date date = new Date(dataMap.optLong("quotationdate"));
//            qDataMap.put("orderdate", df.parse(df.format(date)));            
            qDataMap.put("orderdate", df.parse(date));            
            if(dataMap.has("quotationduedate") && dataMap.optLong("quotationduedate")!=0){ //If quotation date coming from CRM then save that value    
//                 date = new Date(dataMap.optLong("quotationduedate"));
//                 qDataMap.put("duedate", df.parse(df.format(date)));
                date=dataMap.optString("quotationduedateStr");
                qDataMap.put("duedate", df.parse(date));
            } else if(term!=null){ //If duedate not coming from CRM and term present then calculating due date as per term 
                Calendar calendarDueDate = Calendar.getInstance();
//                long quotationDatelong = dataMap.getLong("quotationdate");
//                calendarDueDate.setTimeInMillis(quotationDatelong);
                Date quodate = df.parse(dataMap.optString("quotationdateStr"));
                calendarDueDate.setTime(quodate);
                calendarDueDate.add(Calendar.DATE, term.getTermdays());                            
                qDataMap.put("duedate", df.parse(df.format(calendarDueDate.getTime())));
            } else { // If duedate not coming and term not present then saving creation date as due date
//                 qDataMap.put("duedate", df.parse(df.format(date)));
                qDataMap.put("duedate", df.parse(date));
            }
            
            qDataMap.put("perDiscount", StringUtil.getBoolean(dataMap.optString("discounttype", null)));
            qDataMap.put("discount", StringUtil.getDouble(dataMap.optString("discount", null)));
            qDataMap.put("gstIncluded", dataMap.optString("includingGST", null) == null ? false : Boolean.parseBoolean(dataMap.optString("includingGST", null)));
            if (dataMap.optString("shipdate", null) != null && !StringUtil.isNullOrEmpty(dataMap.optString("shipdate", null))) {
//                date = new Date(dataMap.optLong("shipdate"));
//                qDataMap.put("shipdate", df.parse(df.format(date)));
                date=dataMap.optString("shipdateStr");
                qDataMap.put("shipdate", df.parse(date));
            }
            if (!StringUtil.isNullOrEmpty(dataMap.optString("validtilldateStr", null))) {
                qDataMap.put("validdate", df.parse(dataMap.optString("validtilldateStr", null)));
            } else {
//                Date qDate = new Date(dataMap.optLong("quotationdate"));
                Date qDate = df.parse(dataMap.optString("quotationdateStr"));
                KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
                int b = 0;
                if (extraCompanyPreferences != null) {
                    b = extraCompanyPreferences.getNoOfDaysforValidTillField();
                }
                Calendar cal = Calendar.getInstance();
                cal.setTime(qDate);
                cal.add(Calendar.DAY_OF_MONTH, b);
                Date validDate = cal.getTime();
                qDataMap.put("validdate", df.parse(df.format(validDate)));
            }
            qDataMap.put("shipvia", dataMap.optString("shipvia", null));
            qDataMap.put("shippingterm", dataMap.optString("shippingterm", null));
            qDataMap.put("fob", dataMap.optString("fob", null));
            qDataMap.put("currencyid", currencyid);
            qDataMap.put("isfavourite", dataMap.optString("isfavourite", null));
            qDataMap.put("shipaddress", dataMap.optString("shipaddress", null));
            qDataMap.put("billto", dataMap.optString("billto", null));
            qDataMap.put("istemplate", istemplate);
            qDataMap.put("isLeaseFixedAsset", isLeaseFixedAsset);
            if(quotationType.equalsIgnoreCase("1")){
                qDataMap.put("isLeaseFixedAsset", true);
            }
//            qDataMap.put("salesPerson", dataMap.optString("salesPerson", null));
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                qDataMap.put("costCenterId", costCenterId);
            }
            qDataMap.put("companyid", companyid);
            qDataMap.put("createdby", createdby);
            qDataMap.put("modifiedby", modifiedby);
            qDataMap.put("createdon", createdon);
            qDataMap.put("updatedon", updatedon);
            qDataMap.put("customerporefno", dataMap.optString("customerporefno", null));
            qDataMap.put("profitMargin", dataMap.optString("profitMargin", null));
            qDataMap.put("profitMarginPercent", dataMap.optString("profitMarginPercent", null));
            qDataMap.put("formtypeid", dataMap.optString("formtypeid", null));
            qDataMap.put("isApplyTaxToTerms", dataMap.optBoolean("isApplyTaxToTerms", false));
            
            /**
             * SDP-15752 While sync from CRM here following keys are added to
             * save quotation amount and quotationamountinbase is saved in
             * quotation table. Before saving Quotation validation is applied on
             * keys mentioned above.
             */
            qDataMap.put("gcurrencyid", companyObj.getCurrency().getCurrencyID());
            KwlReturnObject result1 = accCurrencyobj.getExcDetailID(qDataMap, currencyid, quotationDate, null);
            Object obj = result1.getEntityList().get(0);
            if (StringUtil.isNullObject(obj)) {
                String audit_msg = "Cannot be sync to ERP because exchange rate for selected currency in quotation is not available in ERP application.";
                dataMap.put("cannotsyncquotation", true);
                dataMap.put("rejquotationnumber", entryNumber);
                dataMap.put("rejquotationid", crmquoatationid);
                dataMap.put("auditmsg", audit_msg);
                throw new AccountingException(audit_msg);
            } else {
                if (dataMap.has("quotationamount")) {
                    double amount = (Double) dataMap.optDouble("quotationamount");
                    KwlReturnObject result = accCurrencyobj.getCurrencyToBaseAmount(qDataMap, amount, currencyid, quotationDate, externalCurrencyRate);
                    double quotationamount = (Double) result.getEntityList().get(0);
                    qDataMap.put("quotationamount", amount);
                    qDataMap.put("quotationamountinbase", quotationamount);
                }
                if (dataMap.has("totallineleveldiscount")) {
                    double amount = Double.parseDouble(dataMap.optString("totallineleveldiscount"));
                    KwlReturnObject result = accCurrencyobj.getCurrencyToBaseAmount(qDataMap, amount, currencyid, quotationDate, externalCurrencyRate);
                    double discountinbase = (Double) result.getEntityList().get(0);
                    qDataMap.put("totallineleveldiscount", amount);
                    qDataMap.put("discountinbase", discountinbase);
                }
            }
            
            if (!StringUtil.isNullOrEmpty(shipLength)) {
                qDataMap.put("shipLength", shipLength);
            }
            if (!StringUtil.isNullOrEmpty(invoicetype)) {
                qDataMap.put("invoicetype", invoicetype);
            }
            String replacementId = "";
            if (isLinkedFromReplacementNumber || !StringUtil.isNullOrEmpty(dataMap.optString("replacementid", null))) {
                String[] replacementIdArray = dataMap.optString("replacementid", null).split(",");
                replacementId = replacementIdArray[0];// only single select option will be true in linking combo
            }
            if (!StringUtil.isNullOrEmpty(dataMap.optString("contractid", null))) {
                qDataMap.put("contractid", dataMap.optString("contractid", null));
            }
            if (!StringUtil.isNullOrEmpty(dataMap.optString("replacementid", null))) {
                qDataMap.put("isLinkedFromReplacementNumber", true);
                ProductReplacement productReplacement = (ProductReplacement) kwlCommonTablesDAOObj.getClassObject(ProductReplacement.class.getName(), replacementId);
                if (productReplacement != null) {
                    Contract contract = productReplacement.getContract();

                    if (contract != null) {
                        qDataMap.put("contractid", contract.getID());
                    }
                }
                qDataMap.put("isLeaseFixedAsset", true);
                qDataMap.put("quotationtype", 1);
            }
            if (!StringUtil.isNullOrEmpty(dataMap.optString("maintenanceid", null))) {
                qDataMap.put("maintenanceid", dataMap.optString("maintenanceid", null));
                qDataMap.put("quotationtype", 2);

            }
            if (taxid != null && !taxid.isEmpty()) {
                Tax tax = (Tax) kwlCommonTablesDAOObj.getClassObject(Tax.class.getName(), taxid);
                if (tax == null) {
                    String audit_msg = "Tax is not synced or created in CRM only.";
                    dataMap.put("cannotsyncquotation", true);
                    dataMap.put("rejquotationnumber", entryNumber);
                    dataMap.put("rejquotationid", crmquoatationid);
                    dataMap.put("auditmsg", audit_msg);
                    throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                }
                qDataMap.put("taxid", taxid);
            } else {
                qDataMap.put("taxid", taxid);     // Put taxid as null if the CQ doesnt have any total tax included. (To avoid problem while editing CQ)
            }
            String quotationTerms = dataMap.optString("invoicetermsmap", null);
            if (StringUtil.isAsciiString(quotationTerms)) {
                if (new JSONArray(quotationTerms).length() > 0) {
                    qDataMap.put(Constants.termsincludegst, Boolean.parseBoolean(dataMap.optString(Constants.termsincludegst, null)));
                }
            }
            KwlReturnObject soresult = accSalesOrderDAOobj.saveQuotation(qDataMap);
            quotation = (Quotation) soresult.getEntityList().get(0);
            qDataMap.put("id", quotation.getID());
            HashSet sodetails = saveQuotationRows(request, quotation, companyid, dataMap);
            quotation.setRows(sodetails);
            //Save record as template
            if (!StringUtil.isNullOrEmpty(dataMap.optString("templatename", null)) && (istemplate == 1 || istemplate == 2)) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("templatename", dataMap.optString("templatename", null));
                hashMap.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                hashMap.put("modulerecordid", quotation.getID());
                hashMap.put("companyid", companyid);
                if(!StringUtil.isNullOrEmpty(dataMap.optString("companyunitid",null))){
                    hashMap.put("companyunitid", dataMap.getString("companyunitid")); // Added Unit ID if it is present in request
                }
                accountingHandlerDAOobj.saveModuleTemplate(hashMap);
            }
            
            if (StringUtil.isAsciiString(quotationTerms)) {
                // Delete Quotation InvoiceTerms Map
                HashMap<String, Object> termReqMap = new HashMap<String, Object>();
                termReqMap.put("quotationid", quotation.getID());
                accSalesOrderDAOobj.deleteQuotationTermMap(termReqMap);
                //Again mapped new terms in case of edit
                mapInvoiceTerms(quotationTerms, quotation.getID(), createdby, true, qDataMap, currencyid, quotationDate, externalCurrencyRate);
            }
            
            String entry = dataMap.getString("quotationnumber");
                /*---------  Save Documnets related Data--------*/
                JSONArray fileNameList = dataMap.getJSONArray("shareddocs");
                
                    String crmdocid = "";
                    String crmdocids = "";
                    for (int i = 0; i < fileNameList.length(); i++) {
                        crmdocid += "'" + fileNameList.getJSONObject(i).getString("docid") + "',"; /*--Provide docid from CRM side to ERP side in crmdocid column--*/
                    }
                   if(crmdocid.length()>0){
                      crmdocids=crmdocid.substring(0, crmdocid.length()-1);        /*--Save all crm document id(s)*/
                   }
                   Map<String, Object> paramsMap = new HashMap<String,Object>();
                   paramsMap.put("crmdocids", crmdocids);
                   
                   accInvoiceDAOobj.deleteAttachDocuments(paramsMap);           /*--Delete the previous documents from CRM to avoid duplicate data--*/ 
                   
                   /*----Save New Documents information----*/
                   for (int i = 0; i < fileNameList.length(); i++) {
                    InvoiceDocuments document = new InvoiceDocuments();         //new document invoice
                    String docid = UUID.randomUUID().toString();
                    document.setDocID(docid);//uuid
                    document.setDocName(fileNameList.getJSONObject(i).getString("documentname"));//File Name
                    document.setDocType("");//document type 
                    document.setCrmDocumentID(fileNameList.getJSONObject(i).getString("docid"));//Document Id

                    InvoiceDocumentCompMap invoiceDocumentMap = new InvoiceDocumentCompMap();//Mapping of New Document
                    invoiceDocumentMap.setDocument(document);
                    KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                    Company company = (Company) cmp.getEntityList().get(0);
                    invoiceDocumentMap.setCompany(company);

                    invoiceDocumentMap.setInvoiceID(quotation.getID());         //Set Quotation Id as Invoice id for document

                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("InvoceDocument", document);
                    hashMap.put("InvoiceDocumentMapping", invoiceDocumentMap);
                    KwlReturnObject docresult = accInvoiceDAOobj.saveinvoiceDocuments(hashMap);    //Save Documents
                }
            
            String customfield = dataMap.optString("customfield", null);
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                if (jcustomarray.length() > 0) {
                  
                    /* It is not called from CRM So last parameter is being sent "false" */
                    jcustomarray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_Customer_Quotation_ModuleId, companyid, 0,false);            // 1= for line item
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_Quotation_modulename);
                    customrequestParams.put("moduleprimarykey", "QuotationId");
                    customrequestParams.put("modulerecid", quotation.getID());
                    customrequestParams.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                    customrequestParams.put("companyid", companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_Quotation_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        qDataMap.put("accquotationcustomdataref", quotation.getID());
                        KwlReturnObject accresult = accSalesOrderDAOobj.updateQuotationCustomData(qDataMap);
                    }
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(AccSalesOrderAccountingServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveQuotation : " + ex.getMessage(), ex);
        }
        return quotation;
    }
    /*
     *This method is used while integration with CRM for customer quotation.
     */
    public List mapInvoiceTerms(String InvoiceTerms, String id, String userid, boolean isQuotation, Map request, String currencyid, Date transactiondate, double rate) throws ServiceException {
        List ll = new ArrayList();
        try {
            JSONArray termsArr = new JSONArray(InvoiceTerms);
            String companyid = "";
            if (request.containsKey("companyid")) {
                companyid = (String) request.get("companyid");
            }
            for (int cnt = 0; cnt < termsArr.length(); cnt++) {
                JSONObject temp = termsArr.getJSONObject(cnt);
                HashMap<String, Object> termMap = new HashMap<String, Object>();
                double termAmountInbase= 0,termTaxAmountInbase= 0,termAmountExcludingTaxInBase= 0;
                
                double termAmount= Double.parseDouble(temp.getString("termamount"));
                double termTaxAmount= temp.optDouble("termtaxamount",0);
                double termAmountExcludingTax= temp.optDouble("termAmountExcludingTax",0);
                
                termMap.put("term", temp.getString("id"));
                termMap.put("termamount", termAmount);
                termMap.put("termtaxamount", termTaxAmount);
                termMap.put("termAmountExcludingTax", termAmountExcludingTax);
                
                KwlReturnObject result = accCurrencyobj.getCurrencyToBaseAmount(request, termAmount, currencyid, transactiondate, rate);
                termAmountInbase = (Double) result.getEntityList().get(0);
                termMap.put("termamountinbase", authHandler.round(termAmountInbase,companyid));
                
                result = accCurrencyobj.getCurrencyToBaseAmount(request, termTaxAmount, currencyid, transactiondate, rate);
                termTaxAmountInbase = (Double) result.getEntityList().get(0);
                termMap.put("termtaxamountinbase", authHandler.round(termTaxAmountInbase,companyid));
                
                result = accCurrencyobj.getCurrencyToBaseAmount(request, termAmountExcludingTax, currencyid, transactiondate, rate);
                termAmountExcludingTaxInBase = (Double) result.getEntityList().get(0);
                termMap.put("termAmountExcludingTaxInBase", authHandler.round(termAmountExcludingTaxInBase, companyid));
                
                termMap.put("termtax", temp.optString("termtax",null));
                
                double percentage = 0;
                if (!StringUtil.isNullOrEmpty(temp.getString("termpercentage"))) {
                    percentage = Double.parseDouble(temp.getString("termpercentage"));
                }
                termMap.put("termpercentage", percentage);
                termMap.put("creationdate", new Date());
                termMap.put("userid", userid);
                if (isQuotation) {
                    termMap.put("quotationID", id);
                    accSalesOrderDAOobj.saveQuotationTermMap(termMap);
                } else {
                    termMap.put("salesOrderID", id);
                    accSalesOrderDAOobj.saveSalesOrderTermMap(termMap);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return ll;
    }
    
    public HashMap<String, Object> checkQuotationUsages(String qtnId) throws ServiceException {
        HashMap<String, Object> returnObj = new HashMap<String, Object>();
        if(qtnId!=null) {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            KwlReturnObject res = accountingHandlerDAOobj.getObject(Quotation.class.getName(), qtnId);
            Quotation qtnObj = (Quotation) res.getEntityList().get(0);
            String qno = qtnObj.getQuotationNumber();

            requestParams.put("qid", qtnObj.getID());
            requestParams.put("companyid", qtnObj.getCompany().getCompanyID());
            requestParams.put("qno", qno);
            requestParams.put("versionid", qtnObj.getID());
            
            KwlReturnObject result = accSalesOrderDAOobj.getQTforinvoice(qtnObj.getID(), qtnObj.getCompany().getCompanyID());  //for cheching Customer Quotation used in invoice or not
            int count2 = result.getRecordTotalCount();
            if (count2 > 0) {
                returnObj.put("isquotationused", true);
                returnObj.put("module", "Sales Invoice");
                return returnObj;
            }
            KwlReturnObject results = accSalesOrderDAOobj.getSOforQT(qtnObj.getID(), qtnObj.getCompany().getCompanyID());  //for cheching Customer Quotation used in sales order or not
            int count3 = results.getRecordTotalCount();
            if (count3 > 0) {
                returnObj.put("isquotationused", true);
                returnObj.put("module", "Sales Order");
                return returnObj;
            }   
            KwlReturnObject result2 = accSalesOrderDAOobj.getVersionQuotations(requestParams);  //for checking Customer Quotation has any Version or not
            int count4 = result2.getRecordTotalCount();
            if (count4 > 0) {
                returnObj.put("isquotationused", true);
                returnObj.put("module", "Sales Order");
                return returnObj;
            }
        }
        return returnObj;
    }

    public HashSet saveQuotationRows(HttpServletRequest request, Quotation quotation, String companyid, JSONObject dataMap) throws ServiceException, AccountingException {
        HashSet rows = new HashSet();
        try {
            JSONArray jArr = new JSONArray(dataMap.optString("quotationproducts", null));
            boolean isLinkedFromReplacementNumber = (!StringUtil.isNullOrEmpty(dataMap.optString("isLinkedFromReplacementNumber", null))) ? Boolean.parseBoolean(dataMap.optString("isLinkedFromReplacementNumber", null)) : false;
            boolean includeProductTax = StringUtil.isNullOrEmpty(dataMap.optString("includeprotax", null)) ? false : Boolean.parseBoolean(dataMap.optString("includeprotax"));
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put("srno", i + 1);
                qdDataMap.put("companyid", companyid);
                qdDataMap.put("soid", quotation.getID());
                qdDataMap.put("productid", jobj.optString("productid", null));
                qdDataMap.put("rate", jobj.getDouble("unitprice"));//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
                if (jobj.has("priceSource")) {
                    qdDataMap.put("priceSource", !StringUtil.isNullOrEmpty(jobj.optString("priceSource", null)) ? StringUtil.DecodeText(jobj.optString("priceSource", null)) : "");
                }
                if (jobj.has("rateIncludingGst")) {
                    qdDataMap.put("rateIncludingGst", jobj.optDouble("rateIncludingGst", 0));
                }
                qdDataMap.put("quantity", jobj.getDouble("quantity"));
                if (jobj.has("uomid")) {
                    qdDataMap.put("uomid", jobj.optString("uomid", null));
                }
                qdDataMap.put("baseuomquantity", 1.0);
                qdDataMap.put("baseuomrate", 1.0);
                qdDataMap.put("remark", jobj.optString("remark", null));
                if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {  //This is sats specific code  
                    if (jobj.has("dependentType")) {
                        qdDataMap.put("dependentType", StringUtil.isNullOrEmpty(jobj.optString("dependentType", null)) ? jobj.optString("dependentTypeNo", null) : jobj.optString("dependentType", null));
                    }
                    if (jobj.has("inouttime")) {
                        qdDataMap.put("inouttime", !StringUtil.isNullOrEmpty(jobj.optString("inouttime", null)) ? jobj.optString("inouttime", null) : "");
                    }
                    if (jobj.has("showquantity")) {
                        qdDataMap.put("showquantity", !StringUtil.isNullOrEmpty(jobj.getString("showquantity")) ? jobj.getString("showquantity") : "");
                    }
                }
              
                    qdDataMap.put("desc", StringUtil.DecodeText(jobj.optString("description", null)));
               
                if (!StringUtil.isNullOrEmpty(jobj.optString("invstore", null))) {
                    qdDataMap.put("invstoreid", jobj.optString("invstore", null));
                } else {
                    qdDataMap.put("invstoreid", "");
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("invlocation", null))) {
                    qdDataMap.put("invlocationid", jobj.optString("invlocation", null));
                } else {
                    qdDataMap.put("invlocationid", "");
                }

                String linkMode = dataMap.optString("fromLinkCombo", null);

                if (!StringUtil.isNullOrEmpty(dataMap.optString("replacementid", null))) {
                    String replacementid = (String) dataMap.optString("replacementid", null);
                    ProductReplacementDetail pr = null;
                    KwlReturnObject podresult1 = accSalesOrderDAOobj.getProductReplacement(replacementid, jobj.getString("productid"));
                    Iterator itr1 = podresult1.getEntityList().iterator();
                    while (itr1.hasNext()) {
                        pr = (ProductReplacementDetail) itr1.next();
                    }
                    if (pr != null) {
                        qdDataMap.put("productreplacementDetailId", pr.getId());
                    }
                }
                qdDataMap.put("discount", jobj.optDouble("discount", 0));
                qdDataMap.put("discountispercent", jobj.optInt("discountispercent", 0));
                String rowtaxid = jobj.optString("rowtaxid", null);
                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    double rowtaxamount = StringUtil.getDouble(jobj.optString("rowtaxamount", null));
                    if (rowtax == null) {
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    } else {
                        qdDataMap.put("rowtaxid", rowtaxid);
                        qdDataMap.put("rowTaxAmount", rowtaxamount);
                    }
                }

                String linkmode = dataMap.optString("linkNumber", null);
                if ((!StringUtil.isNullOrEmpty(linkmode)) && (!StringUtil.isNullOrEmpty(jobj.optString("rowid", null)))) {
                    qdDataMap.put("vendorquotationdetails", jobj.optString("rowid", null));
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("recTermAmount"))) {
                    qdDataMap.put("recTermAmount", jobj.optString("recTermAmount"));
                }
                if (!StringUtil.isNullOrEmpty(jobj.optString("OtherTermNonTaxableAmount"))) {
                    qdDataMap.put("OtherTermNonTaxableAmount", jobj.optString("OtherTermNonTaxableAmount"));
                }
                //  row.setTax(rowtax);

                KwlReturnObject result = accSalesOrderDAOobj.saveQuotationDetails(qdDataMap);
                QuotationDetail row = (QuotationDetail) result.getEntityList().get(0);
                String customfield = jobj.optString("customfield", null);
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    HashMap<String, Object> DOMap = new HashMap<String, Object>();
                    JSONArray jcustomarray = new JSONArray(customfield);
                    if (jcustomarray.length() > 0) {
                      
                        /* It is called from CRM for Normal Line & Global Custom Fields,
                        
                         So last parameter is being sent "false" */
                        
                        jcustomarray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_Customer_Quotation_ModuleId, companyid, 1,false);            // 1= for line item
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "QuotationDetail");
                        customrequestParams.put("moduleprimarykey", "QuotationDetailId");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        DOMap.put("id", row.getID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_QuotationDetails_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            DOMap.put("qdetailscustomdataref", row.getID());
                            accSalesOrderDAOobj.updateQuotationDetailsCustomData(DOMap);
                        }
                       
                        /* Adding Custom fields details for Product 
                         when Custom Field of Quotation is Editing from CRM 
                         */
                       jcustomarray = new JSONArray(customfield);
                       
                        /* It is called from CRM for Product Custom Field which is available for Quoatation at Line level,
                       
                         So last parameter is being sent "true" */
                       jcustomarray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(jcustomarray, Constants.Acc_Customer_Quotation_ModuleId, companyid, 1,true);            // 1= for line item
                        HashMap<String, Object> quotationMap = new HashMap<>();
                        customrequestParams = new HashMap<>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", "CqProductCustomData");
                        customrequestParams.put("moduleprimarykey", "CqDetailID");
                        customrequestParams.put("modulerecid", row.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        quotationMap.put("id", row.getID());
                        customrequestParams.put("customdataclasspath", Constants.Acc_CQDetail_Productcustom_data_classpath);
                        customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            quotationMap.put("qdetailscustomdataref", row.getID());
                            accSalesOrderDAOobj.updateQuotationDetailsProductCustomData(quotationMap);
                    }
                        
                        
                }
                }
                HashMap<String, Object> qdVendDataMap = new HashMap<String, Object>();
                String vendorid = jobj.optString("vendorid", "");
                double vendorunitcost = jobj.optDouble("vendorunitcost", 0);
                double vendorcurrexchangerate = jobj.optDouble("vendorcurrexchangerate", 0);
                double totalcost = jobj.optDouble("totalcost", 0);
                KwlReturnObject resquoDetailsVendorMap = null;
                if (!StringUtil.isNullOrEmpty(vendorid) && extraCompanyPreferences.isActivateProfitMargin()) {
                    qdVendDataMap.put("id", row.getID());
                    qdVendDataMap.put("vendorid", vendorid);
                    qdVendDataMap.put("vendorunitcost", vendorunitcost);
                    qdVendDataMap.put("vendorcurrexchangerate", vendorcurrexchangerate);
                    qdVendDataMap.put("totalcost", totalcost);
                    resquoDetailsVendorMap = accSalesOrderDAOobj.saveQuotationDetailsVendorMapping(qdVendDataMap);
                }
                /**
                 * Save Customer Quotation details terms mapping
                 * if Subdomain have line level terms as taxes(LiveleveltermFlag==1) from CRM
                 * While Create/ Edit/ Sunc  Customer Quotation from CRM save Product terms of that Quotation.
                 * 
                 */
                if (extraCompanyPreferences.getLineLevelTermFlag()==1 && jobj.has("LineTermdetails") && !StringUtil.isNullOrEmpty((String) jobj.optString("LineTermdetails"))) {
                    JSONArray termsArray = new JSONArray((String) jobj.optString("LineTermdetails"));
                    for (int j = 0; j < termsArray.length(); j++) {
                        HashMap<String, Object> quotationDetailsTermsMap = new HashMap<>();
                        JSONObject termObject = termsArray.getJSONObject(j);

                        if (termObject.has("termid")) {
                            quotationDetailsTermsMap.put("term", termObject.get("termid"));
                        }
                        if (termObject.has("termamount")) {
                            quotationDetailsTermsMap.put("termamount", termObject.get("termamount"));
                        }
                        if (termObject.has("termpercentage")) {
                            quotationDetailsTermsMap.put("termpercentage", termObject.get("termpercentage"));
                        }
                        if (termObject.has("assessablevalue")) {
                            quotationDetailsTermsMap.put("assessablevalue", termObject.get("assessablevalue"));
                        }
                        if (termObject.has("purchasevalueorsalevalue")) {
                            quotationDetailsTermsMap.put("purchasevalueorsalevalue", termObject.get("purchasevalueorsalevalue"));
                        }
                        if (termObject.has("deductionorabatementpercent")) {
                            quotationDetailsTermsMap.put("deductionorabatementpercent", termObject.get("deductionorabatementpercent"));
                        }
                        if (termObject.has("taxtype") && !StringUtil.isNullOrEmpty(termObject.getString("taxtype"))) {
                            quotationDetailsTermsMap.put("taxtype", termObject.getInt("taxtype"));
                            if (termObject.has("taxvalue") && !StringUtil.isNullOrEmpty(termObject.getString("taxvalue"))) {
                                if (termObject.getInt("taxtype") == 0) { // If Flat
                                    quotationDetailsTermsMap.put("termamount", termObject.getDouble("taxvalue"));
                                } else { // Else Percentage
                                    quotationDetailsTermsMap.put("termpercentage", termObject.getDouble("taxvalue"));
                                }
                            }
                        }
                        quotationDetailsTermsMap.put("quotationDetailID", row.getID());
                        quotationDetailsTermsMap.put("product", jobj.get("productid"));
                        quotationDetailsTermsMap.put("userid", dataMap.has(Constants.useridKey)? dataMap.getString(Constants.useridKey) : (quotation.getCreatedby() != null ? quotation.getCreatedby().getUserID(): "" ));
                        accSalesOrderDAOobj.saveQuotationDetailsTermMap(quotationDetailsTermsMap);
                    }
                }
                rows.add(row);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveQuotationRows : " + ex.getMessage(), ex);
        }
        return rows;
    }

    public static Map<String, Object> getAddressParams(JSONObject dataMap) {
        HashMap<String, Object> addressMap = new HashMap<String, Object>();
        try {
            if (dataMap.has("billingAddress") && dataMap.optJSONObject("billingAddress")!=null) {
                JSONObject addrObj=dataMap.optJSONObject("billingAddress");
                addressMap.put(Constants.BILLING_ADDRESS, addrObj.optString("address", ""));
                addressMap.put(Constants.BILLING_COUNTRY, addrObj.optString("country",""));        
                addressMap.put(Constants.BILLING_STATE, addrObj.optString("state",""));
                addressMap.put(Constants.BILLING_COUNTY, addrObj.optString("county",""));
                addressMap.put(Constants.BILLING_CITY, addrObj.optString("city",""));
                addressMap.put(Constants.BILLING_POSTAL, addrObj.optString("postalCode",""));
                addressMap.put(Constants.BILLING_EMAIL, addrObj.optString("emailID",""));
                addressMap.put(Constants.BILLING_FAX, addrObj.optString("fax",""));
                addressMap.put(Constants.BILLING_MOBILE, addrObj.optString("mobileNumber",""));
                addressMap.put(Constants.BILLING_PHONE, addrObj.optString("phone",""));
                addressMap.put(Constants.BILLING_CONTACT_PERSON, addrObj.optString("contactPerson",""));
                addressMap.put(Constants.BILLING_CONTACT_PERSON_NUMBER, addrObj.optString("contactPersonNumber",""));
                addressMap.put(Constants.BILLING_CONTACT_PERSON_DESIGNATION, addrObj.optString("contactPersonDesignation",""));
                addressMap.put(Constants.BILLING_ADDRESS_TYPE, addrObj.optString("aliasName", "Billing Address1"));
            }
            if (dataMap.has("shippingAddress") && dataMap.optJSONObject("shippingAddress")!=null) {
                JSONObject addrObj=dataMap.optJSONObject("shippingAddress");
                addressMap.put(Constants.SHIPPING_ADDRESS, addrObj.optString("address",""));
                addressMap.put(Constants.SHIPPING_COUNTRY, addrObj.optString("country",""));
                addressMap.put(Constants.SHIPPING_STATE, addrObj.optString("state",""));
                addressMap.put(Constants.SHIPPING_COUNTY, addrObj.optString("county",""));
                addressMap.put(Constants.SHIPPING_CITY, addrObj.optString("city",""));
                addressMap.put(Constants.SHIPPING_EMAIL, addrObj.optString("emailID",""));
                addressMap.put(Constants.SHIPPING_FAX, addrObj.optString("fax",""));
                addressMap.put(Constants.SHIPPING_MOBILE, addrObj.optString("mobileNumber",""));
                addressMap.put(Constants.SHIPPING_PHONE, addrObj.optString("phone",""));
                addressMap.put(Constants.SHIPPING_POSTAL, addrObj.optString("postalCode",""));
                addressMap.put(Constants.SHIPPING_CONTACT_PERSON_NUMBER, addrObj.optString("contactPersonNumber",""));
                addressMap.put(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION, addrObj.optString("contactPersonDesignation",""));
                addressMap.put(Constants.SHIPPING_CONTACT_PERSON, addrObj.optString("contactPerson",""));
                addressMap.put(Constants.SHIPPING_ROUTE, addrObj.optString(Constants.SHIPPING_ROUTE,""));
                addressMap.put(Constants.SHIPPING_ADDRESS_TYPE, addrObj.optString("aliasName", "Shipping Address1"));
            }                                 
        } catch (Exception ex) {
        }
        return addressMap;
    }
}
