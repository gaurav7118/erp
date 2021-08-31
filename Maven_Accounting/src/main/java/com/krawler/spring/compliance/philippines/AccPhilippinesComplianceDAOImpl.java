/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.compliance.philippines;

import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.PhilippinesConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.entitygst.AccEntityGstImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class AccPhilippinesComplianceDAOImpl extends BaseDAO implements AccPhilippinesComplianceDAO {

    /**
     * 
     * @param requestParams
     * @param sectionExtraParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public List getSalesInvoiceListDataInSQL(JSONObject requestParams, JSONObject sectionExtraParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder condition = new StringBuilder();
        String query = "";
        String selectColumn = "";
        String joinSql = "";
        String orderbyQuery = "";
        DateFormat df = null;
        if (requestParams.opt(Constants.df) != null) {
            df = (DateFormat) requestParams.opt(Constants.df);
        }
        String companyid = requestParams.optString(Constants.companyKey);
        params.add(companyid);
        params.add(companyid);
        condition.append(" where inv.company = ? ");
        params.add(companyid);
        if (requestParams.has("startdate") && df != null) {
            try {
                condition.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                params.add(df.parse(requestParams.optString("startdate")));
                params.add(df.parse(requestParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (sectionExtraParams.has("CustomerType")) {
            joinSql += " inner join masteritem mstrItem on c.gstcustomertype= mstrItem.id ";
            /**
             * Append condition for customer Type
             */
            String Type = sectionExtraParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(PhilippinesConstants.CUSTOMER_VENDOR_TYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(PhilippinesConstants.CUSTOMER_VENDOR_TYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                condition.append(conditionBuilder.toString());
            } else {
                condition.append(" and mstrItem.defaultmasteritem=?");
                params.add(PhilippinesConstants.CUSTOMER_VENDOR_TYPE.get(Type).toString());
            }
        }
        if (sectionExtraParams.has("taxType")) {
            /** 
             * Append condition for Tax Type
             */
            String Type = sectionExtraParams.optString("taxType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and t.defaulttax in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(PhilippinesConstants.TAX_LIST.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(PhilippinesConstants.TAX_LIST.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                condition.append(conditionBuilder.toString());
            } else {
                condition.append(" and t.defaulttax=?");
                params.add(PhilippinesConstants.TAX_LIST.get(Type).toString());
            }
        }
        boolean isSalesReliefReport = (requestParams.has("reportType") && !StringUtil.isNullOrEmpty(requestParams.optString("reportType")) && requestParams.optString("reportType").equalsIgnoreCase(PhilippinesConstants.SALES_RELIEF_REPORT)) ? true : false;
        if (isSalesReliefReport || sectionExtraParams.has("taxType")) {
            joinSql += " inner join tax t on (t.id=inv.tax or t.id=invd.tax) ";
        }
        /**
         * Sales Relief Report Condition  ERP-41756
         */
        String salesReliefSelectcol="";
        if (isSalesReliefReport) {
            salesReliefSelectcol = ",cdetails.address as customeraddress,c.gstin as clientTIN ,if(inv.tax is NOT NULL,inv.tax,invd.tax) as taxid,cmp.companyname as companyname,t.defaulttax as deafulttaxid,tlist.percent as taxpercentage ";
            joinSql += " inner join company cmp on cmp.companyid=inv.company " +
                       " inner join taxlist tlist on tlist.tax=t.id " +
                       " left join customeraddressdetails cdetails on cdetails.customerid=c.id and cdetails.isdefaultaddress='T' and cdetails.isbillingaddress='T' " ;
        }
        /**
         * Select Column Indexing 
         * 0 - inv.id as invoiceid 
         * 1 - inv.invoicenumber
         * 2 - rate (IF(je.externalcurrencyrate>0,(invd.rate/je.externalcurrencyrate),invd.rate)) 
         * 3 - quantity 
         * 4 - externalcurrencyrate 
         * 5 - discPercentageType 
         * 6 - discountValueInBase 
         * 7 - rowtaxamountinbase 
         * 8 - customerid 
         * 9 - customername 
         * 10 - invoicedetailid 
         * 11 - isGlobalTax 
         * 12 - taxamountinbase
         * 13 - isPayment - false
         * 14 - inv.invoiceamountinbase
         */
        orderbyQuery = " order by invoiceid ASC ";
        selectColumn = " inv.id as invoiceid, inv.invoicenumber, IF(je.externalcurrencyrate>0,(invd.rate/je.externalcurrencyrate),invd.rate ) as rate,it.quantity, je.externalcurrencyrate, "
                + " d.inpercent as discPercentageType,IF(d.inpercent='T', d.discount, IF(je.externalcurrencyrate>0,(d.discount/je.externalcurrencyrate),d.discount)) as discountValueInBase, "
                + " invd.rowtaxamountinbase, c.id as customerid,c.name as customername, invd.id as invoicedetailid, "
                + " inv.tax is NOT NULL as isGlobalTax, inv.taxamountinbase, 'F' as isPayment, inv.invoiceamountinbase " + salesReliefSelectcol;
        query = " select " + selectColumn + " from invoice inv "
                + " inner join customer c on c.id=inv.customer and c.company = ? "
                + " inner join invoicedetails invd on inv.id=invd.invoice  and inv.company=? and inv.deleteflag='F' and inv.isdraft = false and inv.approvestatuslevel = 11 and inv.istemplate!=2 "
                + " inner join journalentry je on je.id=inv.journalentry "
                + " inner join inventory it on it.id=invd.id "
                + joinSql
                + " left join discount d on d.id = invd.discount "
                + condition
                + orderbyQuery;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * 
     * @param requestParams
     * @param sectionExtraParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public List getPurchaseInvoiceListDataInSQL(JSONObject requestParams, JSONObject sectionExtraParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder condition = new StringBuilder();
        String query = "";
        String selectColumn = "";
        String joinSql = "";
        String orderbyQuery = "";
        DateFormat df = null;
        if (requestParams.opt(Constants.df) != null) {
            df = (DateFormat) requestParams.opt(Constants.df);
        }
        String companyid = requestParams.optString(Constants.companyKey);
        params.add(companyid);
        params.add(companyid);
        condition.append(" where inv.company = ? ");
        params.add(companyid);
        if (requestParams.has("startdate") && df != null) {
            try {
                condition.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                params.add(df.parse(requestParams.optString("startdate")));
                params.add(df.parse(requestParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (sectionExtraParams.has("VendorType")) {
            joinSql += " inner join masteritem mstrItem on v.gstvendortype= mstrItem.id ";
            /**
             * Append condition for customer Type
             */
            String Type = sectionExtraParams.optString("VendorType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(PhilippinesConstants.CUSTOMER_VENDOR_TYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(PhilippinesConstants.CUSTOMER_VENDOR_TYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                condition.append(conditionBuilder.toString());
            } else {
                condition.append(" and mstrItem.defaultmasteritem=?");
                params.add(PhilippinesConstants.CUSTOMER_VENDOR_TYPE.get(Type).toString());
            }
        }
        /**
         * Add Tax Type condition 
         */
        if (sectionExtraParams.has("taxType")) {
            /**
             * Append condition for Tax Type
             */
            String Type = sectionExtraParams.optString("taxType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and t.defaulttax in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(PhilippinesConstants.TAX_LIST.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(PhilippinesConstants.TAX_LIST.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                condition.append(conditionBuilder.toString());
            } else {
                condition.append(" and t.defaulttax=?");
                params.add(PhilippinesConstants.TAX_LIST.get(Type).toString());
            }
        }
        boolean isPurchaseReliefReport = (requestParams.has("reportType") && !StringUtil.isNullOrEmpty(requestParams.optString("reportType")) && requestParams.optString("reportType").equalsIgnoreCase(PhilippinesConstants.PURCHASES_RELIEF_REPORT)) ? true : false;
        if (sectionExtraParams.has("taxType") || isPurchaseReliefReport) {
            joinSql += " inner join tax t on (t.id=inv.tax or t.id=invd.tax) ";
        }
        /**
         * Purchase Relief Report Condition
         */
        String purchaseReliefSelectcol = " ,'' as vendoraddress , '' as taxid,'' as vendorTIN,'' as isAssetPurchaseInvoice";
        if (isPurchaseReliefReport) {
            purchaseReliefSelectcol = ",vdetails.address as vendoraddress,v.gstin as vendorTIN ,if(inv.tax is NOT NULL,inv.tax,invd.tax) as taxid,inv.isfixedassetinvoice as isAssetPurchaseInvoice";
            joinSql += " left join vendoraddressdetails vdetails on vdetails.vendorid=v.id and vdetails.isdefaultaddress='T' and vdetails.isbillingaddress='T'";
        }
        if (sectionExtraParams.has("productType")) {
            joinSql += " inner join product p on p.id =  it.product ";
            /**
             * Append condition for product Type
             */
            String productType = sectionExtraParams.optString("productType");
            if (productType.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and p.producttype in (");
                String typeArr[] = productType.split(",");
                for (String pType : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(pType);
                    } else {
                        conditionBuilder.append(",?");
                        params.add(pType);
                    }
                }
                conditionBuilder.append(") ");
                condition.append(conditionBuilder.toString());
            } else {
                condition.append(" and p.producttype =?");
                params.add(productType);
            }
        }
        /**
         * Amount Limit Condition 
         * Section Names  - 
         *   1) 18 A/B. Purchase of Capital Goods not exceeding P1Million
         *   2) 18 C/D. Purchase of Capital Goods exceeding P1Million
         */
        if(sectionExtraParams.optBoolean(PhilippinesConstants.isLowerLimitAmount, false)){
            condition.append(" and inv.invoiceamountinbase <= ? ");
            params.add(PhilippinesConstants.VATReport_LimitAmount);
        }else if(sectionExtraParams.optBoolean(PhilippinesConstants.isGreaterLimitAmount, false)){
            condition.append(" and inv.invoiceamountinbase >= ? ");
            params.add(PhilippinesConstants.VATReport_LimitAmount);
        }
        /**
         * Get Only Asset Invoices
         */
        if (sectionExtraParams.optBoolean(PhilippinesConstants.isAssetDocumentType, false)) {
            condition.append(" and inv.isfixedassetinvoice = ? ");
            params.add(true);
        }
        /**
         * Select Column Indexing 
         * 0 - inv.id as invoiceid 
         * 1 - inv.invoicenumber
         * 2 - rate (IF(je.externalcurrencyrate>0,(invd.rate/je.externalcurrencyrate),invd.rate)) 
         * 3 - quantity 
         * 4 - externalcurrencyrate 
         * 5 - discPercentageType 
         * 6 - discountValueInBase 
         * 7 - rowtaxamountinbase 
         * 8 - vendorid 
         * 9 - vendorname 
         * 10 - invoicedetailid 
         * 11 - isGlobalTax 
         * 12 - taxamountinbase
         * 13 - isPayment - false
         * 14 - inv.invoiceamountinbase
         * if Purchase Relief Report
         * 15 - vendoraddress
         * 16 - vendorTIN
         * 17 - taxid
         * 18 - isfixedassetinvoice
         */
        orderbyQuery = " order by invoiceid ASC ";
        selectColumn = " inv.id as invoiceid, inv.grnumber, IF(je.externalcurrencyrate>0,(invd.rate/je.externalcurrencyrate),invd.rate ) as rate,it.quantity, je.externalcurrencyrate, "
                + " d.inpercent as discPercentageType,IF(d.inpercent='T', d.discount, IF(je.externalcurrencyrate>0,(d.discount/je.externalcurrencyrate),d.discount)) as discountValueInBase, "
                + " invd.rowtaxamountinbase, v.id as vendorid,v.name as vendorname, invd.id as invoicedetailid, "
                + " inv.tax is NOT NULL as isGlobalTax, inv.taxamountinbase, 'F' as isPayment, inv.invoiceamountinbase "+purchaseReliefSelectcol;
        query = " select " + selectColumn + " from goodsreceipt inv "
                + " inner join vendor v on v.id=inv.vendor and v.company = ? "
                + " inner join grdetails invd on inv.id=invd.goodsreceipt  and inv.company=? and inv.deleteflag='F' and inv.approvestatuslevel = 11 and inv.istemplate!=2 "
                + " inner join journalentry je on je.id=inv.journalentry "
                + " inner join inventory it on it.id=invd.id "
                + joinSql
                + " left join discount d on d.id = invd.discount "
                + condition
                + orderbyQuery;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * 
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public List getReceivePaymentAdvanceListDataInSql(JSONObject reqParams, JSONObject sectionExtraParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        String orderbyQuery = "";
        String selectColumn = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where r.company=? and r.deleteflag='F' and r.approvestatuslevel=11 ");
        params.add(reqParams.optString("companyid"));
         if (sectionExtraParams.has("CustomerType")) {
            joinSql += " inner join masteritem mstrItem on c.gstcustomertype= mstrItem.id ";
            /**
             * Append condition for customer Type
             */
            String Type = sectionExtraParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(PhilippinesConstants.CUSTOMER_VENDOR_TYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(PhilippinesConstants.CUSTOMER_VENDOR_TYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(PhilippinesConstants.CUSTOMER_VENDOR_TYPE.get(Type).toString());
            }
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                    params.add(df.parse(reqParams.optString("startdate")));
                    params.add(df.parse(reqParams.optString("enddate")));
                }
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         /**
         * Select Column Indexing 
         * 0 - r.id as receiptid
         * 1 - r.receiptnumber
         * 2 - rate - '0'
         * 3 - quantity - '0'
         * 4 - externalcurrencyrate 
         * 5 - discPercentageType -  'F'
         * 6 - discountValueInBase  - '0'
         * 7 - taxamount 
         * 8 - customerid 
         * 9 - customername 
         * 10 - invoicedetailid 
         * 11 - isGlobalTax - always False for Payment
         * 12 - taxamount - 
         * 13 - isPayment - true
         * 14 - rad.amount
         */
        orderbyQuery = " order by receiptid ASC ";
        selectColumn = " r.id as receiptid, r.receiptnumber, '0' as rate,'0' as quantity, je.externalcurrencyrate, "
                + " 'F' as discPercentageType,'0' as discountValueInBase, "
                + " rad.taxamount, c.id as customerid,c.name as customername, rad.id as receiptdetailid, "
                + " rad.tax is NOT NULL as isGlobalTax, r.taxamount , 'T' as isPayment , rad.amount  ";
        
        String query = " select " + selectColumn + " from receipt r "
                + " inner join receiptadvancedetail rad on rad.receipt=r.id "
                + " inner join journalentry je on je.id=r.journalentry "
                + " inner join customer c on c.id=r.customer "
                + joinSql 
                + builder 
                + orderbyQuery;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * 
     * @param reqParams
     * @param sectionExtraParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public List getReceivePaymentInvoiceListDataInSql(JSONObject reqParams, JSONObject sectionExtraParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        String orderbyQuery = "";
        String selectColumn = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where r.company=? and r.deleteflag='F' and r.approvestatuslevel=11 ");
        params.add(reqParams.optString("companyid"));
         if (sectionExtraParams.has("CustomerType")) {
            joinSql += " inner join masteritem mstrItem on c.gstcustomertype= mstrItem.id ";
            /**
             * Append condition for customer Type
             */
            String Type = sectionExtraParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(PhilippinesConstants.CUSTOMER_VENDOR_TYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(PhilippinesConstants.CUSTOMER_VENDOR_TYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(PhilippinesConstants.CUSTOMER_VENDOR_TYPE.get(Type).toString());
            }
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                    params.add(df.parse(reqParams.optString("startdate")));
                    params.add(df.parse(reqParams.optString("enddate")));
                }
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         /**
         * Select Column Indexing 
         * 0 - r.id as receiptid
         * 1 - r.receiptnumber
         * 2 - rate - '0'
         * 3 - quantity - '0'
         * 4 - externalcurrencyrate 
         * 5 - discPercentageType -  'F'
         * 6 - discountValueInBase  - '0'
         * 7 - taxamount 
         * 8 - customerid 
         * 9 - customername 
         * 10 - invoicedetailid 
         * 11 - isGlobalTax - always False for Payment
         * 12 - taxamount - 
         * 13 - isPayment - true
         * 14 - rad.amount
         */
        orderbyQuery = " order by receiptid ASC ";
        selectColumn = " r.id as receiptid, r.receiptnumber, '0' as rate,'0' as quantity, je.externalcurrencyrate, "
                + " 'F' as discPercentageType, rad.discountamountinbase as discountValueInBase, "
                + " '0' as taxamount, c.id as customerid,c.name as customername, rad.id as receiptdetailid, "
                + " false as isGlobalTax, r.taxamount , 'T' as isPayment , rad.amount  ";
        
        String query = " select " + selectColumn + " from receipt r "
                + " inner join receiptdetails rad on rad.receipt=r.id "
                + " inner join journalentry je on je.id=r.journalentry "
                + " inner join customer c on c.id=r.customer "
                + joinSql 
                + builder 
                + orderbyQuery;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * 
     * @param reqParams
     * @param sectionExtraParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public List getReceivePaymentOtherWiseListDataInSql(JSONObject reqParams, JSONObject sectionExtraParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        String orderbyQuery = "";
        String selectColumn = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where r.company=? and r.deleteflag='F' and r.approvestatuslevel=11 ");
        params.add(reqParams.optString("companyid"));
         if (sectionExtraParams.has("CustomerType")) {
            joinSql += " inner join masteritem mstrItem on c.gstcustomertype= mstrItem.id ";
            /**
             * Append condition for customer Type
             */
            String Type = sectionExtraParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(PhilippinesConstants.CUSTOMER_VENDOR_TYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(PhilippinesConstants.CUSTOMER_VENDOR_TYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(PhilippinesConstants.CUSTOMER_VENDOR_TYPE.get(Type).toString());
            }
        }
         /**
          * Added tax Type condition in SQL query
          */
        if (sectionExtraParams.has("taxType")) {
            joinSql += " inner join tax t on t.id = rad.tax ";
            /**
             * Append condition for Tax Type
             */
            String Type = sectionExtraParams.optString("taxType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and t.defaulttax in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(PhilippinesConstants.TAX_LIST.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(PhilippinesConstants.TAX_LIST.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and t.defaulttax=?");
                params.add(PhilippinesConstants.TAX_LIST.get(Type).toString());
            }
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                    params.add(df.parse(reqParams.optString("startdate")));
                    params.add(df.parse(reqParams.optString("enddate")));
                }
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         /**
         * Select Column Indexing 
         * 0 - r.id as receiptid
         * 1 - r.receiptnumber
         * 2 - rate - '0'
         * 3 - quantity - '0'
         * 4 - externalcurrencyrate 
         * 5 - discPercentageType -  'F'
         * 6 - discountValueInBase  - '0'
         * 7 - taxamount 
         * 8 - customerid 
         * 9 - customername 
         * 10 - invoicedetailid 
         * 11 - isGlobalTax - always False for Payment
         * 12 - taxamount - 
         * 13 - isPayment - true
         * 14 - rad.amount
         */
        orderbyQuery = " order by receiptid ASC ";
        selectColumn = " r.id as receiptid, r.receiptnumber, '0' as rate,'0' as quantity, je.externalcurrencyrate, "
                + " 'F' as discPercentageType, '0' as discountValueInBase, "
                + " rad.taxamount, c.id as customerid,c.name as customername, rad.id as receiptdetailid, "
                + " false as isGlobalTax, r.taxamount , 'T' as isPayment , rad.amount  ";
        
        String query = " select " + selectColumn + " from receipt r "
                + " inner join receiptdetailotherwise rad on rad.receipt=r.id "
                + " inner join journalentry je on je.id=r.journalentry "
                + " inner join customer c on c.id=r.customer "
                + joinSql 
                + builder 
                + orderbyQuery;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }

}
