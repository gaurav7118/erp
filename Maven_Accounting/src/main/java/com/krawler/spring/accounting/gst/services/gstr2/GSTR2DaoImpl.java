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
package com.krawler.spring.accounting.gst.services.gstr2;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.LineLevelTerms;
import com.krawler.hql.accounting.Producttype;
import com.krawler.spring.accounting.entitygst.AccEntityGstImpl;
import com.krawler.spring.accounting.gst.dto.GstReturn;
import com.krawler.spring.accounting.gst.services.GSTRConstants;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.gst.AccGstDAOImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class GSTR2DaoImpl extends BaseDAO implements GSTR2Dao {

    @Override
    public List getInvoiceDataWithDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException {
        //initilization
        List params = new ArrayList();
        String joinSql = "";
        StringBuilder builder = new StringBuilder();
        DateFormat df = null;
        int hsncolnum = reqParams.optInt("hsncolnum");
        int taxclasscolnum = reqParams.optInt("taxclasscolnum");
        int assetHsncolnum = reqParams.optInt(GSTRConstants.ASSET_HSNCOLUMN);
        int assetTaxclasscolnum = reqParams.optInt(GSTRConstants.ASSET_TAXCLASSCOLUMN);

        //get parameters
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where gr.company=? and gr.deleteflag='F' ");
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is Unregistered i.e. GSTIN is not present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("isServiceProduct")) {
            /**
             * For service type of product
             */
            boolean isServiceProduct = reqParams.optBoolean("isServiceProduct");
            if (isServiceProduct) {
                builder.append(" and prod.producttype = ").append("'").append(Producttype.SERVICE).append("' ");
            } else {
                builder.append(" and prod.producttype <> ").append("'").append(Producttype.SERVICE).append("' ");
            }
        }
        if (reqParams.has("isRCMApplicable")) {
            boolean isRCMApplicable = reqParams.optBoolean("isRCMApplicable");
            builder.append(" and gr.gtaapplicable = ").append(isRCMApplicable ? "'T'" : "'F'");
        }
        if (reqParams.has("entitycolnum")) {
            /**
             * Column no of Entity dimension's value stored in Invoice
             */
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("entitycolnum");
            int assetColnum = reqParams.optInt(GSTRConstants.ASSET_PURCHASE_INVOICE_ENTITYCOLUMN);
            builder.append(" and (jecust.col" + colnum + "=? or jecust.col" + assetColnum + "=? )");
            //builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
            params.add(reqParams.optString(GSTRConstants.ASSET_PURCHASE_INVOICE_ENTIYVALUE));
        }
          if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type 
             */
            joinSql += " inner join masteritem mitm on mitm.id=gdh.gstrtype ";
            
            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();   
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for vendor Type
             */
            joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            String Type = reqParams.optString("CustomerType");

            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
        }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (!reqParams.optBoolean("excludetaxClassType", false)) {
            /**
             * excludetaxClassType is coming only from GSTR3B: Section 5_4
             */
           
            if (reqParams.has("taxClassType")) { 
              String Type = reqParams.optString("taxClassType");
                if (Type.contains(",")) {
                    StringBuilder conditionBuilder = new StringBuilder();
                    conditionBuilder.append(" and  ptaxfcd.valuetype in (");
                    String typeArr[] = Type.split(",");
                    for (String type : typeArr) {
                        if (conditionBuilder.indexOf("?") == -1) {
                            conditionBuilder.append("?");
                            params.add((int) FieldComboData.ValueTypeMap.get(type));
                        } else {
                            conditionBuilder.append(",?");
                            params.add((int) FieldComboData.ValueTypeMap.get(type));
                        }
                    }
                    conditionBuilder.append(") ");
                    builder.append(conditionBuilder.toString());
                } else {
                     builder.append(" and ptaxfcd.valuetype=?");
                     params.add((int) FieldComboData.ValueTypeMap.get(Type));
                }
            }
        }
         /*
          Flag for GST-calculation is based on Billing Address or Shipping Address
        */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping= true;
        }
        String billingShippingPOS = (isShipping==false)?"bst.billingstate":"bst.shippingstate" ;
       /**
         * For india country and vendor transactions and
         * isAddressFromVendorMaster is off then vendor billing address is store
         * in seperate key pair
         */
        boolean isAddressNotFromVendorMaster = reqParams.optBoolean(GSTRConstants.isAddressNotFromVendorMaster, false);
        if(isAddressNotFromVendorMaster){
            billingShippingPOS = (isShipping==false) ? " bst.vendorbillingstateforindia "  : " bst.vendcustshippingstate ";
        }
        String localState = (!StringUtil.isNullOrEmpty(reqParams.optString("localState")) ? reqParams.optString("localState") : "");
        if (reqParams.has("interstate")) {
            /**
             * Return only Inter state i.e. IGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("<>? ");
            params.add(localState);

//            builder.append(" and (lt.defaultterms=? or lt.defaultterms=? )");
//            params.add(LineLevelTerms.GSTName.get("InputIGST").toString());
//            params.add(LineLevelTerms.GSTName.get("InputCESS").toString());
//            params.add("00efb196-5f34-11e7-907b-a6006ad3dba0");
        } else if (reqParams.has("intrastate")) {
            builder.append(" and ").append(billingShippingPOS).append("=? ");
            params.add(localState);
//            builder.append(" and (lt.defaultterms=? or lt.defaultterms=? or lt.defaultterms=? or lt.defaultterms=?)");
//            params.add((String) LineLevelTerms.GSTName.get("InputCGST"));
//            params.add((String) LineLevelTerms.GSTName.get("InputSGST"));
//            params.add((String) LineLevelTerms.GSTName.get("InputUTGST"));
//            params.add(LineLevelTerms.GSTName.get("InputCESS").toString());
        }
         /*
         To perform Quick search by Inv.No or Vendor name
         */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            if (!StringUtil.isNullOrEmpty(searchString)) {
                try {
                    String[] searchcol = new String[]{"gr.grnumber","v.name"};

                    Map SearchStringMap = StringUtil.insertParamSearchStringMap((ArrayList) params, searchString, 2);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                    builder.append(searchQuery);                    
                } catch (SQLException ex) {
                    Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (reqParams.has("greaterlimit") && reqParams.has("limitamount")) {
            if (reqParams.optBoolean("greaterlimit")) {
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and gr.excludinggstamount>? ");
                params.add(limit);
            } else {
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and gr.excludinggstamount<=? ");
                params.add(limit);
            }
        }
        if(reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))){
            //builder.append(" and shl1.value=?");
            builder.append(" and ").append(billingShippingPOS).append("=?");
            params.add(reqParams.optString("statesearch"));
        }
        if (reqParams.has("itctype") && !StringUtil.isNullOrEmpty(reqParams.optString("itctype"))) {
            /**
             * Condition to filter records on the basis of ITC types.
             */
            builder.append(" and grd.itctype in (?) ");
            params.add(reqParams.optString("itctype"));
        }
        String groupby = "";
        String selectCol = "v.id as customerid,v.gstin,gr.id as invoiceid,gr.grnumber,je.id as jeid,je.entrydate,"
                + "grd.id as invoicedetailid,IF(je.externalcurrencyrate>0,(grd.rate/je.externalcurrencyrate),grd.rate ),inventory.quantity,prod.id as productid,llt.term,IF(je.externalcurrencyrate>0,(termmap.termamount/je.externalcurrencyrate),termmap.termamount),"
                + "gr.invoiceamountinbase,IFNULL(pfcd.value,pfcd1.value) as hsncode,"+ billingShippingPOS +" as pos,prod.description,shl1.id as posid,"
                + "termmap.percentage as taxrate,llt.defaultterms,d.inpercent as discPercentage,IF(d.inpercent='T', d.discount,IF(je.externalcurrencyrate>0,(d.discount/je.externalcurrencyrate),d.discount)) as discountValue,gr.supplierinvoiceno,v.name as vendorname";
        if (reqParams.has(GSTRConstants.ADD_LANDEDCOST_JOIN_FOR_IMPORT_INVOICES) && reqParams.optBoolean(GSTRConstants.ADD_LANDEDCOST_JOIN_FOR_IMPORT_INVOICES, false)) {
            selectCol = "v.id as customerid,v.gstin,gr.id as invoiceid,gr.grnumber,je.id as jeid,je.entrydate,"
                    + "grd.id as invoicedetailid,IF(manlc.expenseInvoiceid is null,IF(je.externalcurrencyrate>0,(grd.rate/je.externalcurrencyrate),grd.rate),IF(expje.externalcurrencyrate>0,(sum(manlc.taxablevalueforigst)/expje.externalcurrencyrate),sum(manlc.taxablevalueforigst))) as rate,IF(manlc.expenseInvoiceid is null,inventory.quantity,1.0),prod.id as productid,llt.term,IF(manlc.expenseInvoiceid is null,IF(je.externalcurrencyrate>0,(termmap.termamount/je.externalcurrencyrate),termmap.termamount),IF(expje.externalcurrencyrate>0,(sum(manlc.igstamount)/expje.externalcurrencyrate),sum(manlc.igstamount))) as termamount,"
                    + "gr.invoiceamountinbase,IFNULL(pfcd.value,pfcd1.value) as hsncode," + billingShippingPOS + " as pos,prod.description,shl1.id as posid,"
                    + "termmap.percentage as taxrate,IF(manlc.expenseInvoiceid is null,llt.defaultterms,'00efbd8a-5f34-11e7-907b-a6006ad3dba0'),d.inpercent as discPercentage,IF(manlc.expenseInvoiceid is null,IF(d.inpercent='T', d.discount,IF(je.externalcurrencyrate>0,(d.discount/je.externalcurrencyrate),d.discount)),0.0) as discountValue,gr.supplierinvoiceno,v.name as vendorname";
            groupby = " group by llt.id, grd.id, gr.id";
        }
        if (reqParams.has("GST3B") && reqParams.optBoolean("GST3B")) {
            /**
             * Used for GST3B report i.e. select sum of all Invoice taxable and
             * Tax amount (Separate for each GST type i.e. CGST,IGST etc)
             */
            selectCol = "sum(IF(je.externalcurrencyrate>0,(termmap.termamount/je.externalcurrencyrate),termmap.termamount)),llt.defaultterms,sum((IF(je.externalcurrencyrate>0,(grd.rate/je.externalcurrencyrate),grd.rate ) * inventory.quantity ) - IF(je.externalcurrencyrate>0,(ifNull(case when d.inpercent='T' then (d.discount*(grd.rate*inventory.quantity)/100) else d.discount end,0)/je.externalcurrencyrate), ifNull(case when d.inpercent='T' then (d.discount*(grd.rate*inventory.quantity)/100) else d.discount end,0))), sum(gr.invoiceamountinbase),count(distinct gr.id)";
            groupby = " group by llt.defaultterms ";

        }
        if (reqParams.has("zerorated")) {
            if (reqParams.optBoolean("zerorated")) {
                /**
                 * Condition for such Invoice having GST rates Zero
                 */
                builder.append(" and termmap.termamount=0");
            } else {
                /**
                 * Condition for such CN having GST rates Non Zero
                 */
                builder.append(" and termmap.termamount>0");
            }
        }
        String typeofjoin = " inner ";
        if (reqParams.has("typeofjoinisleft") && reqParams.optBoolean("typeofjoinisleft")) {
            typeofjoin = " left ";
        }
        joinSql += " left join discount d on d.id = grd.discount ";
        if (reqParams.has(GSTRConstants.ADD_LANDEDCOST_JOIN_FOR_IMPORT_INVOICES) && reqParams.optBoolean(GSTRConstants.ADD_LANDEDCOST_JOIN_FOR_IMPORT_INVOICES, false)) {
            joinSql += " left join lccmanualwiseproductamount manlc on manlc.grdetailid = grd.id "
                    + " left join goodsreceipt expgr on expgr.id = manlc.expenseInvoiceid "
                    + " left join journalentry expje on expje.id = expgr.journalentry ";
        }
        String query = "SELECT " + selectCol + " FROM goodsreceipt gr \n"
                + "inner join vendor v ON v.id = gr.vendor\n"
                + "INNER JOIN grdetails grd ON gr.id=grd.goodsreceipt and gr.company=? and gr.approvestatuslevel = 11 and gr.istemplate!=2 \n" //GSTR2A Transaction type B2B Invoices ,B2BUR Invoices - 4B, Comparison call also comes here
                + " inner join gstdocumenthistory gdh on gr.id=gdh.refdocid and gdh.moduleid in ('6','39') \n "
                + " inner join gsttaxclasshistory gtch on grd.id=gtch.refdocid and gtch.moduleid in ('6','39') \n"
                + "INNER JOIN journalentry je ON je.id = gr.journalentry\n"
                + "INNER JOIN inventory inventory ON inventory.id=grd.id\n"
//                + "INNER JOIN uom uom ON uom.id=inventory.uom\n"
                + "INNER JOIN product prod ON prod.id = inventory.product\n"
                + "INNER JOIN jedetail jed ON jed.id = grd.purchasejedid\n"
                + typeofjoin+" join receiptdetailtermsmap termmap ON termmap.goodsreceiptdetail = grd.id\n"
                + typeofjoin+" join entitybasedlineleveltermsrate eltr on eltr.id=termmap.entityterm\n"
                + typeofjoin+" join linelevelterms llt on llt.id=eltr.linelevelterms \n"
                + typeofjoin+" join fieldcombodata shl1 on shl1.id=eltr.shippedloc1  \n"
                + "INNER JOIN accproductcustomdata pcd on pcd.productId=prod.id  \n"
                + " LEFT JOIN fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + ""
                + " LEFT JOIN fieldcombodata ptaxfcd on ptaxfcd.id=gtch.producttaxclass "
                + " LEFT JOIN fieldcombodata pfcd1 on pfcd1.id=pcd.col" + assetHsncolnum + ""
                + " inner join billingshippingaddresses bst on bst.id=gr.billingshippingaddresses "
//                + " LEFT JOIN fieldcombodata ptaxfcd1 on ptaxfcd1.id=pcd.col" + assetTaxclasscolnum + ""
                + " " + joinSql + builder + groupby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    
    @Override
    public List getCNDNWithInvoiceDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        int hsncolnum = reqParams.optInt("hsncolnum");

        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where pr.company=? and pr.deleteflag='F' and  dn.approvestatuslevel=11 ");
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If vendor is registered i.e. GSTIN is present 
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If vendor is Unregistered i.e. GSTIN is present 
                 */
                
                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("entitycolnum")) {
            /**
             * Column no of Entity Value stored in CN
             */
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=cnje.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (cnje.entrydate>=? and cnje.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (reqParams.has("greaterlimit") && reqParams.has("limitamount")) {
            /**
             * CN taxable value limit
             */
            if (reqParams.optBoolean("greaterlimit")) {
                /**
                 * CN Amount greater than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and dn.dnamountinbase>? ");
                params.add(limit);
            } else {
                /**
                 * CN Amount less than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and dn.dnamountinbase<=? ");
                params.add(limit);
            }
        }
        /*
         Flag for GST-calculation is based on Billing Address or Shipping Address
         */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping = true;
        }
        String billingShippingPOS = (isShipping == false) ? "bst.billingstate" : "bst.shippingState";
        /**
         * For india country and vendor transactions and
         * isAddressFromVendorMaster is off then vendor billing address is store
         * in seperate key pair
         */
        boolean isAddressNotFromVendorMaster = reqParams.optBoolean(GSTRConstants.isAddressNotFromVendorMaster, false);
        if(isAddressNotFromVendorMaster){
            billingShippingPOS = (isShipping==false) ? " bst.vendorbillingstateforindia "  : " bst.vendcustshippingstate ";
        }
        String localState = (!StringUtil.isNullOrEmpty(reqParams.optString("localState")) ? reqParams.optString("localState") : "");
        if (reqParams.has("interstate")) {
            /**
             * Return only Inter state i.e. IGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("<>? ");
            params.add(localState);
        } else if (reqParams.has("intrastate")) {
            /**
             * Return only Intra state i.e. CGST and SGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("=? ");
            params.add(localState);
        }
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql += " inner join masteritem mitm on mitm.id=gdh.gstrtype ";

            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
               if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))) {
            builder.append(" and shl1.value=?");
            params.add(reqParams.optString("statesearch"));
        }
        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            String Type = reqParams.optString("taxClassType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and  ptaxfcd.valuetype in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    } else {
                        conditionBuilder.append(",?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and ptaxfcd.valuetype=?");
                params.add((int) FieldComboData.ValueTypeMap.get(Type));
            }

        }
        if (reqParams.has("isServiceProduct")) {
            /**
             * For service type of product
             */
            boolean isServiceProduct = reqParams.optBoolean("isServiceProduct");
            if (isServiceProduct) {
                builder.append(" and p.producttype = ").append("'").append(Producttype.SERVICE).append("' ");
            } else {
                builder.append(" and p.producttype <> ").append("'").append(Producttype.SERVICE).append("' ");
            }
        }
        /*
         To perform Quick search by Inv.No or customer name
         */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            if (!StringUtil.isNullOrEmpty(searchString)) {
                try {
                    String[] searchcol = new String[]{"dn.dnnumber","v.name"};

                    Map SearchStringMap = StringUtil.insertParamSearchStringMap((ArrayList) params, searchString, 2);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                    builder.append(searchQuery);                    
                } catch (SQLException ex) {
                    Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        /**
         * No Of table with its aliases 
         * Invoice = inv 
         * Invoice Detail = invd
         * Sales Return Detail=srd
         * Credit Note = cn
         * Credit Note details = cnd
         * Journal entry = je 
         * Inventory = it
         * Product = p 
         * Invoice Detail term Map = ivtd 
         * EntitybasedLineLevelTermRate= eltr
         * LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String selectCol = "";
        selectCol = "v.id as customerid,v.gstin,pr.id as invoiceid,gr.grnumber,cnje.id as jeid,cnje.entrydate,"
                    + "prd.id as invoicedetailid,IF(cnje.externalcurrencyrate>0,(prd.rate/cnje.externalcurrencyrate),prd.rate ),prd.returnquantity,p.id as productid,lt.term,IF(cnje.externalcurrencyrate>0,(ivtd.termamount/cnje.externalcurrencyrate),ivtd.termamount ),"
                    + "dn.dnamountinbase,pfcd.value as hsncode,"+billingShippingPOS+" as pos,p.description,shl1.id as posid,"
                    + "ivtd.percentage as taxrate,lt.defaultterms,dn.dnnumber,pr.orderdate,prd.discountispercent as discpercentage,"
                + "IF(prd.discountispercent=1,prd.discount,IF(cnje.externalcurrencyrate>0,(prd.discount/cnje.externalcurrencyrate),prd.discount)) as discountvalue,dn.id as cnid,dn.dnamountinbase as cnamountinbase,v.name as vendorname,'D' as doctype";
        String groupby = "";
        if (reqParams.has("GST3B") && reqParams.optBoolean("GST3B")) {
             /**
             * Used for GST3B report 
             * i.e. select sum of all CN taxable and Tax amount (Separate for each GST type i.e. CGST,IGST etc)
             * 
             */
//            selectCol = "sum(ivtd.termamount),lt.defaultterms,sum((prd.rate*prd.returnquantity)) ";
            
            
            selectCol = "sum(IF(je.externalcurrencyrate>0,(ivtd.termamount/je.externalcurrencyrate),ivtd.termamount)),lt.defaultterms,"
                    + "sum((IF(je.externalcurrencyrate>0,(prd.rate/je.externalcurrencyrate),prd.rate )*prd.returnquantity)- IF(je.externalcurrencyrate>0,(case when prd.discountispercent=1 then (prd.discount*(prd.rate*prd.returnquantity)/100) else prd.discount end/je.externalcurrencyrate),case when prd.discountispercent=1 then (prd.discount*(prd.rate*prd.returnquantity)/100) else prd.discount end )) "
                    + ",sum(pr.totalamountinbase)";

              
            groupby = " group by lt.defaultterms ";
            if (reqParams.optBoolean("zerorated")) {
                /**
                 * Condition for such CN having GST rates Zero
                 */
                builder.append(" and ivtd.termamount=0");
            } else {
                /**
                 * Condition for such CN having GST rates Zero
                 */
                builder.append(" and ivtd.termamount>0");
            }
        }
        String typeofjoin = " inner ";
        if (reqParams.has("typeofjoinisleft") && reqParams.optBoolean("typeofjoinisleft")) {
            typeofjoin = " left ";
        }

        String query = " select " + selectCol + " from purchasereturn pr "
                + " inner join prdetails prd on pr.id=prd.purchasereturn and pr.company=?"
                + " inner join gstdocumenthistory gdh on pr.id=gdh.refdocid "
                + " inner join gsttaxclasshistory gtch on prd.id=gtch.refdocid "
                + " inner join debitnote dn on dn.purchasereturn=pr.id "
                + " inner join journalentry cnje on cnje.id=dn.journalentry "
                + " inner join vendor v on v.id=dn.vendor "
                + " inner join product p on p.id=prd.product "
                + " left join grdetails grd on prd.videtails=grd.id "
                + " left join goodsreceipt gr on gr.id=grd.goodsreceipt "
                + typeofjoin + " join purchasereturndetailtermmap ivtd on ivtd.purchasereturndetail=prd.id "
                + typeofjoin + " join entitybasedlineleveltermsrate eltr on eltr.id=ivtd.entityterm"
                + typeofjoin + " join linelevelterms lt on lt.id=eltr.linelevelterms"
                + typeofjoin + " join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 "
                + typeofjoin + " join accproductcustomdata pcd on pcd.productId=p.id "
                + typeofjoin + " join fieldcombodata ptaxfcd on ptaxfcd.id=gtch.producttaxclass"
                + " inner join billingshippingaddresses bst on bst.id=dn.billingshippingaddresses "
                + " inner join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + "" + joinSql + builder + groupby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
        public List getHSNWiseInvoiceDataWithDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where gr.company=? and gr.deleteflag='F' and inv.approvestatuslevel = 11 and inv.istemplate!=2  ");
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));

        if (reqParams.has("entitycolnum")) {
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("entitycolnum");
            int assetColnum = reqParams.optInt(GSTRConstants.ASSET_PURCHASE_INVOICE_ENTITYCOLUMN);
            builder.append(" and (jecust.col" + colnum + "=? or jecust.col" + assetColnum + "=? )");
            params.add(reqParams.optString("entityValue"));
            params.add(reqParams.optString(GSTRConstants.ASSET_PURCHASE_INVOICE_ENTIYVALUE));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        int hsncolnum = reqParams.optInt("hsncolnum");
        int assetHsncolnum = reqParams.optInt(GSTRConstants.ASSET_HSNCOLUMN);
        /**
         * No Of table with its aliases 
         * Invoice = inv 
         * Invoice Detail = invd
         * Journal entry = je 
         * Inventory = it
         * Product = p 
         * Invoice Detail term Map = ivtd 
         * EntitybasedLineLevelTermRate= eltr
         * LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String selectCol = "gr.id as invoiceid,gr.grnumber,"
                + "grd.id as invoicedetailid,IF(je.externalcurrencyrate>0,(grd.rate/je.externalcurrencyrate),grd.rate ),it.quantity,lt.term,IF(je.externalcurrencyrate>0,(ivtd.termamount/je.externalcurrencyrate),ivtd.termamount),"
                + "gr.invoiceamountinbase,IFNULL(pfcd.value,pfcd1.value) as hsncode,ivtd.percentage as taxrate,lt.defaultterms,IFNULL(pfcd.id,pfcd1.id) as hsnid"
                + ",d.inpercent as discPercentage,IF(d.inpercent='T', d.discount,IF(je.externalcurrencyrate>0,(d.discount/je.externalcurrencyrate),d.discount)) as discountValue";
        joinSql += " left join discount d on d.id = grd.discount ";
        String query = " select " + selectCol + " from goodsreceipt gr inner join grdetails grd on gr.id=grd.goodsreceipt and gr.company=? "
                + " inner join journalentry je on je.id=gr.journalentry "
                + " inner join vendor c on c.id=gr.vendor "
                + " inner join inventory it on it.id=grd.id "
                + " inner join product p on p.id=it.product "
                + " left join receiptdetailtermsmap ivtd on ivtd.goodsreceiptdetail=grd.id "
                + " left join entitybasedlineleveltermsrate eltr on eltr.id=ivtd.entityterm"
                + " left join linelevelterms lt on lt.id=eltr.linelevelterms"
                + " left join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 "
                + " inner join accproductcustomdata pcd on pcd.productId=p.id "
                + " left join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + "" 
                + " left  join fieldcombodata pfcd1 on pfcd1.id=pcd.col" + assetHsncolnum + ""
                + joinSql + builder;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * Get data for GSTR JE data for GSTR2 i.e. TDS/TCS
     *
     * @param reqParams
     * @return
     * @throws ServiceException
     */
    public List getTDSTCSITCDetails(JSONObject reqParams) throws ServiceException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where je.company=?");
        params.add(reqParams.optString("companyid"));

        if (reqParams.has("entitycolnum")) {
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (reqParams.has("gstrjetype")) {
            /**
             * GSTR JE Type
             */
            builder.append(" and je.gstrtype=?");
            String Type = reqParams.optString("gstrjetype");
            params.add(Constants.GSTRJETYPE.get(Type));
        }
        String groupby = "group by je.id";
        builder.append(" and jed.debit='T' ");
        String selectCol = "je.id,je.entryno,sum(jed.amount),count(distinct je.id)";
        String query = " select " + selectCol + " from journalentry je inner join jedetail jed on jed.journalentry=je.id "
                + joinSql + builder + groupby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * Function to fetch ITC JE
     *
     * @param reqParams
     * @return
     * @throws ServiceException
     */
    public List getITCJournalEntryDetails(JSONObject reqParams) throws ServiceException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where je.company=?");
        params.add(reqParams.optString("companyid"));

        if (reqParams.has("jeentitycolnum")) {
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("jeentitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("jeentityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (reqParams.has("gstrjetype")) {
            /**
             * GSTR JE Type
             */
            builder.append(" and je.gstrtype=?");
            String Type = reqParams.optString("gstrjetype");
            params.add(Constants.GSTRJETYPE.get(Type));
        }
        if (reqParams.has("defaultAccountIds")) {
            builder.append(" and a.defaultaccountid in (?)");
            params.add(reqParams.optString("defaultAccountIds"));
        }
        String groupby = "group by jed.id";
        builder.append(" and jed.debit='F' ");
        String selectCol = " je.id,je.entryno,je.entrydate,a.defaultaccountid,jed.amount ";
        String query = " select " + selectCol + " from journalentry je inner join jedetail jed on jed.journalentry=je.id "
                + " inner join account a on a.id=jed.account "
                + joinSql + builder + groupby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    public List getAdvanceDetailsInSql(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where p.company=? and p.deleteflag='F' and p.approvestatuslevel=11 ");
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If vendor is registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If vendor is Unregistered i.e. GSTIN is not present
                 */
                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("entitycolnum")) {
            /**
             * Column no of Entity Value stored in Receipt
             */
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    if (reqParams.optBoolean("atadj")) {
                        builder.append(" and (ldr.paymentlinkdate>=? and ldr.paymentlinkdate<=?) and je.entrydate<=? ");
                        params.add(df.parse(reqParams.optString("startdate")));
                        params.add(df.parse(reqParams.optString("enddate")));
                        params.add(df.parse(reqParams.optString("startdate")));
                    } else {
                        builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                        params.add(df.parse(reqParams.optString("startdate")));
                        params.add(df.parse(reqParams.optString("enddate")));
                    }

                }
                
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
         if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            String Type = reqParams.optString("taxClassType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and  ptaxfcd.valuetype in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    } else {
                        conditionBuilder.append(",?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and ptaxfcd.valuetype=?");
                params.add((int) FieldComboData.ValueTypeMap.get(Type));
            }

        }
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql += " inner join masteritem mitm on mitm.id=gdh.gstrtype ";

            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        
        if (reqParams.has("CustomerType")) {
            joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            /**
             * Append condition for customer Type
             */
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.optBoolean("at")) {
            joinSql += " left join linkdetailpayment ldr on ldr.payment=p.id ";
//            try {
//                /**
//                 * Includes such Advance which is not linked to any invoice
//                 */
//                builder.append(" and ((ldr.paymentlinkdate>=? and ldr.paymentlinkdate<=?))");
//                params.add(df.parse(reqParams.optString("startdate")));
//                params.add(df.parse(reqParams.optString("enddate")));
//                
//            } catch (ParseException ex) {
//                Logger.getLogger(GSTR2DaoImpl.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
        if (reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))) {
            builder.append(" and shl1.value=?");
            params.add(reqParams.optString("statesearch"));
        }

        /**
         * No Of table with its aliases *
         *
         */
        String selectCol = " shl1.value as pos ,shl1.id as posid,pad.amountdue,pad.taxamount,padtm.percentage,padtm.termamount,lt.defaultterms,pad.id,"
                + "(IF(je.externalcurrencyrate>0,(ldr.amount/je.externalcurrencyrate),ldr.amount)) as adjustamt,(IF(je.externalcurrencyrate>0,(pad.amount/je.externalcurrencyrate),pad.amount)) as amount,ldr.paymentlinkdate,v.name as vendorname,v.gstin as gstin,p.paymentnumber as anum";

        if (reqParams.optBoolean("atadj")) {
            /**
             * Includes such Advance which is linked to any invoice
             */
            joinSql += " inner join linkdetailpayment ldr on ldr.payment=p.id ";
//            selectCol += " ,ldr.amount as adjustamt";
        }
        String groupby = "";
        if (reqParams.has("GST3B") && reqParams.optBoolean("GST3B")) {
            /**
             * Used for GST3B report i.e. select sum of all Advance Receipt
             * taxable and Tax amount (Separate for each GST type i.e. CGST,IGST
             * etc) Includes such Advance which is not linked to any invoice
             */
            selectCol = "sum(padtm.termamount),lt.defaultterms,sum(pad.amountdue) ";
            groupby = " group by lt.defaultterms ";
            if (reqParams.optBoolean("atadj")) {
                /**
                 * Includes such Advance which is linked to any invoice
                 */
                selectCol = "sum((IF(je.externalcurrencyrate>0,(padtm.termamount/je.externalcurrencyrate),padtm.termamount))),lt.defaultterms,sum((IF(je.externalcurrencyrate>0,(ldr.amount/je.externalcurrencyrate),ldr.amount))) ";
            }
            if (reqParams.optBoolean("zerorated")) {
                /**
                 * Condition for such receipt having GST rates Zero
                 */
                builder.append(" and padtm.termamount=0");
            } else {
                /**
                 * Condition for such receipt having GST rates non Zero
                 */
                builder.append(" and padtm.termamount>0");
            }
        }
        if (reqParams.has("isDocumentDetails") && reqParams.optBoolean("isDocumentDetails")) {
            /**
             * Used for Document Details report
             *
             */
            selectCol = "sq.name,Max(inv.seqnumber),Min(inv.seqnumber),sq.prefix,sq.suffix,sq.numberofdigit,count(distinct inv.id)";
            groupby = " group by sq.id ";
            joinSql += " inner join sequenceformat sq on inv.seqformat=sq.id ";

        }
        String query = " select " + selectCol + " from payment p inner join advancedetail pad on pad.payment=p.id "
                + " inner join gstdocumenthistory gdh on p.id=gdh.refdocid and gdh.moduleid = 14 "
                + " inner join gsttaxclasshistory gtch on pad.id=gtch.refdocid and gtch.moduleid = 14 "
                + " inner join journalentry je on je.id=p.journalentry "
                + " inner join vendor v on v.id=p.vendor "
                + " inner join advancedetailtermmap padtm on padtm.advancedetail=pad.id "
                + " inner join entitybasedlineleveltermsrate eltr on eltr.id=padtm.entityterm"
                + " inner join linelevelterms lt on lt.id=eltr.linelevelterms"
                + " left join product pr on pr.id=pad.productid "
                + " left join accproductcustomdata pcd on pcd.productId=pr.id "
                + " left join fieldcombodata ptaxfcd on ptaxfcd.id=gtch.producttaxclass "
                + " inner join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 " + joinSql + builder + groupby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    public List getDNAgainstVendor(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where dn.company=? and dn.deleteflag='F' and dn.approvestatuslevel = 11 ");
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Unregistered i.e. GSTIN is present
                 */

                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("entitycolnum")) {
            /**
             * Column no of Entity Value stored in CN
             */
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=dnje.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (dnje.entrydate>=? and dnje.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /*
         To perform Quick search by Inv.No or customer name
         */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            if (!StringUtil.isNullOrEmpty(searchString)) {
                try {
                    String[] searchcol = new String[]{"dn.dnnumber","v.name"};

                    Map SearchStringMap = StringUtil.insertParamSearchStringMap((ArrayList) params, searchString, 2);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                    builder.append(searchQuery);                    
                } catch (SQLException ex) {
                    Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql += " inner join masteritem mitm on mitm.id=gdh.gstrtype ";

            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            String Type = reqParams.optString("taxClassType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and  ptaxfcd.valuetype in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    } else {
                        conditionBuilder.append(",?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and ptaxfcd.valuetype=?");
                params.add((int) FieldComboData.ValueTypeMap.get(Type));
            }

        }
        /*
          Flag for GST-calculation is based on Billing Address or Shipping Address
        */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping = true;
        }
        String billingShippingPOS = (isShipping == false) ? "bst.billingstate" : "bst.shippingState";
        /**
         * For india country and vendor transactions and
         * isAddressFromVendorMaster is off then vendor billing address is store
         * in seperate key pair
         */
        boolean isAddressNotFromVendorMaster = reqParams.optBoolean(GSTRConstants.isAddressNotFromVendorMaster, false);
        if(isAddressNotFromVendorMaster){
            billingShippingPOS = (isShipping==false) ? " bst.vendorbillingstateforindia "  : " bst.vendcustshippingstate ";
        }
        String localState = (!StringUtil.isNullOrEmpty(reqParams.optString("localState")) ? reqParams.optString("localState") : "");

        if (reqParams.has("interstate")) {
            /**
             * Return only Inter state i.e. IGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("<>? ");
            params.add(localState);
        } else if (reqParams.has("intrastate")) {
            /**
             * Return only Intra state i.e. CGST and SGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("=? ");
            params.add(localState);
        }
        if (reqParams.has("greaterlimit") && reqParams.has("limitamount")) {
            /**
             * CN taxable value limit
             */
            if (reqParams.optBoolean("greaterlimit")) {
                /**
                 * CN Amount greater than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and dn.dnamountinbase>? ");
                params.add(limit);
            } else {
                /**
                 * CN Amount less than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and dn.dnamountinbase<=? ");
                params.add(limit);
            }
        }
         if (reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))) {
            builder.append(" and shl1.value=?");
            params.add(reqParams.optString("statesearch"));
        }
        int hsncolnum = reqParams.optInt("hsncolnum");
        if (reqParams.has("isServiceProduct")) {
            /**
             * For service type of product
             */
            boolean isServiceProduct = reqParams.optBoolean("isServiceProduct");
            if (isServiceProduct) {
                builder.append(" and p.producttype = ").append("'").append(Producttype.SERVICE).append("' ");
            } else {
                builder.append(" and p.producttype <> ").append("'").append(Producttype.SERVICE).append("' ");
            }
        }
        /**
         * No Of table with its aliases Invoice = inv Invoice Detail = invd
         * Sales Return Detail=srd Credit Note = cn Credit Note details = cnd
         * Journal entry = je Inventory = it Product = p Invoice Detail term Map
         * = ivtd EntitybasedLineLevelTermRate= eltr LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String selectCol = "";
        
        selectCol = " v.id as vendorid,v.gstin,dn.id as invoiceid,GROUP_CONCAT(gr.grnumber separator ','),"
                + "je.id as jeid,je.entrydate,dnt.id as grdetailid,(IF(dnje.externalcurrencyrate>0,(dnt.amount/dnje.externalcurrencyrate),dnt.amount)) as rate,1.0 as quantity,"
                + "'' as productid,lt.term,(IF(dnje.externalcurrencyrate>0,(dndtm.termamount/dnje.externalcurrencyrate),dndtm.termamount)), dn.dnamountinbase, "
                + "'' as hsncode,"+billingShippingPOS+" as pos,'' as productdescription,"
                + "shl1.id as posid,dndtm.percentage as taxrate,lt.defaultterms,"
                + "dn.dnnumber,dnje.entrydate as cndndate,0 as discpercentage,"
                + "0.0 as discountvalue,dn.id as cnid,dn.dnamountinbase as cnamountinbase,"
                + "v.name as vendorname,"
                +"'D' as doctype";
        String groupby = "", orderby = " ORDER by dn.dnnumber";
        String dnInvoiceMappingInfo = " left join debitnoteinvoicemappinginfo dnmi on dnmi.debitnote = dn.id \n"
                + "left join goodsreceipt gr on gr.id = dnmi.goodsreceipt\n"
                + "left join journalentry je on je.id = gr.journalentry ";
        if (reqParams.has("isDocumentDetails") && reqParams.optBoolean("isDocumentDetails")) {
            /**
             * Used for Document Details report
             *
             */
            selectCol = " dn.dnnumber,dn.seqformat,dn.seqnumber";
            groupby += " group by dn.id ";
            joinSql += " inner join sequenceformat sq on dn.seqformat=sq.id ";
            orderby += " ORDER BY dn.seqformat,dn.seqnumber ";

        } else if (reqParams.has("GST3B") && reqParams.optBoolean("GST3B")) {
             /**
             * Used for GST3B report 
             * i.e. select sum of all CN taxable and Tax amount (Separate for each GST type i.e. CGST,IGST etc)
             * 
             */
//            selectCol = "sum(ivtd.termamount),lt.defaultterms,sum((prd.rate*prd.returnquantity)) ";
            
            
            selectCol = "sum((IF(dnje.externalcurrencyrate>0,(dndtm.termamount/dnje.externalcurrencyrate),dndtm.termamount))),lt.defaultterms,"
                    + "sum((IF(dnje.externalcurrencyrate>0,(dnt.amount/dnje.externalcurrencyrate),dnt.amount))) "
                    + ",sum(dn.dnamountinbase)";

              
            groupby = " group by lt.defaultterms ";
            
            dnInvoiceMappingInfo = "";
        } else {
            groupby += " group by lt.id,dnt.id,dn.id ";
        }
        if (reqParams.has("zerorated")) {
            if (reqParams.optBoolean("zerorated")) {
                /**
                 * Condition for such CN having GST rates Zero
                 */
                builder.append(" and dndtm.termamount=0");
            } else {
                /**
                 * Condition for such CN having GST rates Zero
                 */
                builder.append(" and dndtm.termamount>0");
            }
        }
        builder.append(" and dn.purchasereturn is null ");
        String query = " select " + selectCol + " from dntaxentry dnt \n"
                + "inner join debitnote dn on dn.id = dnt.debitnote  \n"
                + " inner join gsttaxclasshistory gtch on dnt.id=gtch.refdocid and gtch.moduleid in ('10','12') \n"
                + " inner join gstdocumenthistory gdh on dn.id=gdh.refdocid and gdh.moduleid in ('10','12') \n"
                + "inner join journalentry dnje on dnje.id = dn.journalentry\n"
                + "left join debitnotedetailtermmap dndtm on dndtm.debitnotetaxentry= dnt.id \n"
                + "left join entitybasedlineleveltermsrate eltr on eltr.id=dndtm.entityterm\n"
                + "left join linelevelterms lt on lt.id=eltr.linelevelterms \n"
                + "left join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 \n"
                + " left join product p on p.id=dnt.productid "
                + " left join accproductcustomdata pcd on pcd.productId=p.id "
                + " left join fieldcombodata ptaxfcd on ptaxfcd.id=gtch.producttaxclass "
                + " inner join billingshippingaddresses bst on bst.id=dn.billingshippingaddresses "
                + "inner join vendor v on v.id = dn.vendor \n"
                + dnInvoiceMappingInfo + joinSql + builder + groupby + orderby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
        
    @Override
    public KwlReturnObject saveGSTR2JSON(JSONObject params) {
        List list = new ArrayList();
        try {
            GstReturn gstReturn = new GstReturn();
            if (params.has(Constants.companyKey)) {
                gstReturn.setCompanyid((Company) get(Company.class, params.optString(Constants.companyKey)));
            }
            if (params.has(GstReturn.ENTITYID_KEY)) {
                gstReturn.setEntityID(params.optString(GstReturn.ENTITYID_KEY));
            }            
            if (params.has(GstReturn.GSTR2AJSON_KEY)) {
                gstReturn.setGstr2a(params.optString(GstReturn.GSTR2AJSON_KEY));
            }
            gstReturn.setUploadedon(new Date());
            if (params.has(GstReturn.UPLOADFILE_KEY)) {
                gstReturn.setUploadFileName(params.optString(GstReturn.UPLOADFILE_KEY));
            }
            if (params.has(GstReturn.MONTH_KEY)) {
                gstReturn.setMonth(params.optInt(GstReturn.MONTH_KEY));
            }
            if (params.has(GstReturn.YEAR_KEY)) {
                gstReturn.setYear(params.optInt(GstReturn.YEAR_KEY));
            }
            save(gstReturn);
            list.add(gstReturn);
        } catch (Exception ex) {
            org.apache.log4j.Logger.getLogger(AccGstDAOImpl.class.getName()).log(org.apache.log4j.Level.INFO, ex.getMessage());
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public KwlReturnObject getImportedGSTR2AData(JSONObject reqParams) {
        List list = new ArrayList();
        try {
            StringBuilder query = new StringBuilder();
            List params = new ArrayList();
            query.append("FROM GstReturn");
            if (reqParams.has(Constants.companyKey)) {
                query.append(" WHERE companyid.companyID = ?");
                params.add(reqParams.optString(Constants.companyKey));
            }
            if (reqParams.has(GstReturn.ENTITYID_KEY)) {
                if (query.indexOf("WHERE") == -1) {
                    query.append(" WHERE");
                } else {
                    query.append(" AND");
                }
                query.append(" entityID = ?");
                params.add(reqParams.optString(GstReturn.ENTITYID_KEY));
            }
            if (reqParams.has(GstReturn.MONTH_KEY)) {
                if (query.indexOf("WHERE") == -1) {
                    query.append(" WHERE");
                } else {
                    query.append(" AND");
                }
                query.append(" month = ?");
                if (reqParams.get(GstReturn.MONTH_KEY) instanceof Integer) {
                    params.add(reqParams.optInt(GstReturn.MONTH_KEY));
                } else {
                    params.add(Integer.parseInt(reqParams.optString(GstReturn.MONTH_KEY)));
                }
            }
            if (reqParams.has(GstReturn.YEAR_KEY)) {
                if (query.indexOf("WHERE") == -1) {
                    query.append(" WHERE");
                } else {
                    query.append(" AND");
                }
                query.append(" year = ?");
                if (reqParams.get(GstReturn.YEAR_KEY) instanceof Integer) {
                    params.add(reqParams.optInt(GstReturn.YEAR_KEY));
                } else {
                    params.add(Integer.parseInt(reqParams.optString(GstReturn.YEAR_KEY)));
                }
            }
            query.append(" ORDER BY uploadedon DESC");

            list = executeQuery(query.toString(), params.toArray());
        } catch (Exception ex) {
            org.apache.log4j.Logger.getLogger(AccGstDAOImpl.class.getName()).log(org.apache.log4j.Level.INFO, ex.getMessage());
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public List getCNAgainstVendor(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        builder.append(" where cn.company=? and cn.deleteflag='F' and cn.approvestatuslevel=11 ");
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Unregistered i.e. GSTIN is present
                 */

                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("entitycolnum")) {
            /**
             * Column no of Entity Value stored in CN
             */
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=cnje.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (cnje.entrydate>=? and cnje.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql += " inner join masteritem mitm on mitm.id=gdh.gstrtype ";

            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            String Type = reqParams.optString("taxClassType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and  ptaxfcd.valuetype in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    } else {
                        conditionBuilder.append(",?");
                        params.add((int) FieldComboData.ValueTypeMap.get(type));
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and ptaxfcd.valuetype=?");
                params.add((int) FieldComboData.ValueTypeMap.get(Type));
            }
        }
        /*
          Flag for GST-calculation is based on Billing Address or Shipping Address
        */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping = true;
        }
        String billingShippingPOS = (isShipping == false) ? "bst.billingstate" : "bst.shippingState";
        /**
         * For india country and vendor transactions and
         * isAddressFromVendorMaster is off then vendor billing address is store
         * in seperate key pair
         */
        boolean isAddressNotFromVendorMaster = reqParams.optBoolean(GSTRConstants.isAddressNotFromVendorMaster, false);
        if(isAddressNotFromVendorMaster){
            billingShippingPOS = (isShipping==false) ? " bst.vendorbillingstateforindia "  : " bst.vendcustshippingstate ";
        }
        
        String localState = (!StringUtil.isNullOrEmpty(reqParams.optString("localState")) ? reqParams.optString("localState") : "");

        if (reqParams.has("interstate")) {
            /**
             * Return only Inter state i.e. IGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("<>? ");
            params.add(localState);
        } else if (reqParams.has("intrastate")) {
            /**
             * Return only Intra state i.e. CGST and SGST records
             */
            builder.append(" and ").append(billingShippingPOS).append("=? ");
            params.add(localState);
        }
        if (reqParams.has("greaterlimit") && reqParams.has("limitamount")) {
            /**
             * CN taxable value limit
             */
            if (reqParams.optBoolean("greaterlimit")) {
                /**
                 * CN Amount greater than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and cn.cnamountinbase > ? ");
                params.add(limit);
            } else {
                /**
                 * CN Amount less than limit i.e. 2.5 Lac
                 */
                double limit = reqParams.optDouble("limitamount");
                builder.append(" and cn.cnamountinbase <=? ");
                params.add(limit);
            }
        }
        if (reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))) {
            builder.append(" and shl1.value=?");
            params.add(reqParams.optString("statesearch"));
        }
        if (reqParams.has("isServiceProduct")) {
            /**
             * For service type of product
             */
            boolean isServiceProduct = reqParams.optBoolean("isServiceProduct");
            if (isServiceProduct) {
                builder.append(" and p.producttype = ").append("'").append(Producttype.SERVICE).append("' ");
            } else {
                builder.append(" and p.producttype <> ").append("'").append(Producttype.SERVICE).append("' ");
            }
        }
        /*
         To perform Quick search by Inv.No or vendor name
         */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            if (!StringUtil.isNullOrEmpty(searchString)) {
                try {
                    String[] searchcol = new String[]{"cn.cnnumber","v.name"};

                    Map SearchStringMap = StringUtil.insertParamSearchStringMap((ArrayList) params, searchString, 2);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                    builder.append(searchQuery);                    
                } catch (SQLException ex) {
                    Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        /**
         * No Of table with its aliases Invoice = inv Invoice Detail = invd
         * Sales Return Detail=srd Credit Note = cn Credit Note details = cnd
         * Journal entry = je Inventory = it Product = p Invoice Detail term Map
         * = ivtd EntitybasedLineLevelTermRate= eltr LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String selectCol = " v.id as vendorid,v.gstin,cn.id as invoiceid,GROUP_CONCAT(gr.grnumber separator ','),"
                + "je.id as jeid,je.entrydate,cnt.id as grdetailid,(IF(cnje.externalcurrencyrate>0,(cnt.amount/cnje.externalcurrencyrate),cnt.amount)) as rate,1.0 as quantity,"
                + "'' as productid,lt.term,(IF(cnje.externalcurrencyrate>0,(cndtm.termamount/cnje.externalcurrencyrate),cndtm.termamount)), cn.cnamountinbase, '' as hsncode,"
                + billingShippingPOS+ " as pos,'' as productdescription,shl1.id as posid,cndtm.percentage as taxrate,"
                + "lt.defaultterms,cn.cnnumber,cnje.entrydate as cndndate,0 as discpercentage,0.0 as discountvalue,"
                + "cn.id as cnid,cn.cnamountinbase as cnamountinbase,v.name as vendorname";
        String groupby = "", orderby = " ORDER by cn.cnnumber ";
        String cnInvoiceMappingInfo = " left join creditnoteinvoicemappinginfo cnmi on cnmi.creditnote = cn.id \n"
                + "left join goodsreceipt gr on gr.id = cnmi.goodsreceipt\n"
                + "left join journalentry je on je.id = gr.journalentry ";
        if (reqParams.has("isDocumentDetails") && reqParams.optBoolean("isDocumentDetails")) {
            /**
             * Used for Document Details report
             *
             */
            selectCol = " cn.cnnumber,cn.seqformat,cn.seqnumber";
            groupby += " group by cn.id ";
            joinSql += " inner join sequenceformat sq on cn.seqformat=sq.id ";
            orderby += " ORDER BY cn.seqformat,cn.seqnumber ";

        } else if (reqParams.has("GST3B") && reqParams.optBoolean("GST3B")) {
            /**
             * Used for GST3B report i.e. select sum of all CN taxable and Tax
             * amount (Separate for each GST type i.e. CGST,IGST etc)
             *
             */
            selectCol = "sum((IF(cnje.externalcurrencyrate>0,(cndtm.termamount/cnje.externalcurrencyrate),cndtm.termamount))),lt.defaultterms,"
                    + "sum((IF(cnje.externalcurrencyrate>0,(cnt.amount/cnje.externalcurrencyrate),cnt.amount))) "
                    + ",sum(cn.cnamountinbase)";
            groupby = " group by lt.defaultterms ";
            cnInvoiceMappingInfo = "";
        } else {
            groupby += " group by lt.id,cnt.id,cn.id ";
        }
        if (reqParams.has("zerorated")) {
            if (reqParams.optBoolean("zerorated")) {
                /**
                 * Condition for such CN having GST rates Zero
                 */
                builder.append(" and cndtm.termamount=0");
            } else {
                /**
                 * Condition for such CN having GST rates Zero
                 */
                builder.append(" and cndtm.termamount>0");
            }
        }
        String query = " select " + selectCol + " from cntaxentry cnt \n"
                + "inner join creditnote cn on cn.id = cnt.creditnote and cn.company=? \n"
                + " inner join gstdocumenthistory gdh on cn.id=gdh.refdocid and gdh.moduleid=12 \n"
                + " inner join gsttaxclasshistory gtch on cnt.id=gtch.refdocid and gtch.moduleid=12 \n"
                + "inner join journalentry cnje on cnje.id = cn.journalentry\n"
                + "left join creditnotedetailtermmap cndtm on cndtm.creditnotetaxentry= cnt.id \n"
                + "left join entitybasedlineleveltermsrate eltr on eltr.id=cndtm.entityterm\n"
                + "left join linelevelterms lt on lt.id=eltr.linelevelterms \n"
                + "left join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 \n"
                + " left join product p on p.id=cnt.productid "
                + " left join fieldcombodata ptaxfcd on ptaxfcd.id=gtch.producttaxclass "
                + " inner join billingshippingaddresses bst on bst.id=cn.billingshippingaddresses "
                + "inner join vendor v on v.id = cn.vendor \n"
                + cnInvoiceMappingInfo + joinSql + builder + groupby + orderby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * Get Debit note in mismatch report
     *
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getGSTMissingDN(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        String having = "";
        String orderby = "";
        String additionalSelectCol = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        String col = "p.description as description";
        builder.append(" where dn.company=? and dn.deleteflag='F' ");
        params.add(reqParams.optString("companyid"));
        int hsncolnum = reqParams.optInt("hsncolnum");
        int uqccolnum = reqParams.optInt("uqccolnum");
        int taxclasscolnum = reqParams.optInt("taxclasscolnum");
        String joinForHSNandUQCCode = " left ";
        if (reqParams.has("ishsnblank") && reqParams.optBoolean("ishsnblank")) {
            builder.append(" and (pcd.col" + hsncolnum + " is NULL or pcd.col" + hsncolnum + "='' or (LENGTH(pfcd.value) > 8))");
            col = "pcd.col" + hsncolnum;
            joinForHSNandUQCCode = " inner ";
        }
        if (reqParams.has("isuqcblank") && reqParams.optBoolean("isuqcblank")) {
            builder.append(" and (pcd.col" + uqccolnum + " is NULL or pcd.col" + uqccolnum + "='' )");
            col = "pcd.col" + uqccolnum;
            joinForHSNandUQCCode = " inner ";
        }
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is Unregistered i.e. GSTIN is not present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin <>'' ");
            }
        }
//        if (reqParams.has("iscndnwithoutinvoice") && reqParams.optBoolean("iscndnwithoutinvoice")) {
//            builder.append(" and prd.videtails is NULL ");
//        }

        if (reqParams.has("entitycolnum")) {
            /**
             * Column no of Entity dimension's value stored in Invoice
             */
            joinSql += " inner join accjecustomdata jecust on jecust.journalentryId=je.id ";
            int colnum = reqParams.optInt("entitycolnum");
            builder.append(" and jecust.col" + colnum + "=?");
            params.add(reqParams.optString("entityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (je.entrydate>=? and je.entrydate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        /**
         * Below query for GST Registration blank and Wrong GST registration
         * tagged. Example - Other than Regular, Composition and Unregistered is
         * wrong GST Registration type ,Please ref.ERP-35464 for more details
         */
        String GSTRegTypeSelectColumn = " ,'' as gstrtype";
        String GSTTypeName = " ,'' as gsttypename";
        if (reqParams.has("isgstregtypeblank") && reqParams.optBoolean("isgstregtypeblank")) {
            if(!reqParams.has("registrationType")){
                joinSql+= " left join masteritem mitm on mitm.id=gdh.gstrtype ";
            }
            String GSTRegTypeQuery = "'"+Constants.GSTRegType.get(Constants.GSTRegType_Composition)+"',";
            GSTRegTypeQuery += "'"+Constants.GSTRegType.get(Constants.GSTRegType_Regular)+"',";
            GSTRegTypeQuery += "'"+Constants.GSTRegType.get(Constants.GSTRegType_Unregistered)+"'";
            builder.append(" and ((gdh.gstrtype IS NULL or gdh.gstrtype='') or (mitm.defaultmasteritem not in(" + GSTRegTypeQuery + ")))");
            GSTRegTypeSelectColumn = " ,gdh.gstrtype as gstrtype ";
            GSTTypeName = " ,mitm.value as gsttypename";
        }
        /**
         * Below query for GST Customer blank and Wrong GST registration
         * Please ref.ERP-35464 for more details
         */
        String GSTCustVendTypeSelectColumn = " ,'' as custventypeid";
        if (reqParams.has("iscusttypeblank") && reqParams.optBoolean("iscusttypeblank")) {
            if (!reqParams.has("CustomerType") && !reqParams.has("registrationType")) {
                joinSql += " left join masteritem mitm on mitm.id=gdh.gstrtype ";
                joinSql += " left join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            }
            String GSTCustomerTypeQuery = " (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Composition) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Regular) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZ) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZWOPAY) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Unregistered) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_Export) + "','" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_ExportWOPAY) + "')) ";
            builder.append(" and ((gdh.custventypeid IS NULL or gdh.custventypeid='') or (" + GSTCustomerTypeQuery+"))");
            GSTCustVendTypeSelectColumn = " ,gdh.custventypeid as custventypeid ";
            GSTTypeName = " ,mstrItem.value as gsttypename";
        }
         if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql+= " inner join masteritem mitm on mitm.id=gdh.gstrtype";
            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            joinSql+= " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.has("ishistoryblank") && reqParams.optBoolean("ishistoryblank")) {
            builder.append(" and dn.id not in (select refdocid from gstdocumenthistory where moduleid in ('10'))");
        }
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping= true;
        }
        String billingShippingPOS = (isShipping==false)?"bst.billingstate":"bst.shippingstate" ;
        /**
         * For india country and vendor transactions and
         * isAddressFromVendorMaster is off then vendor billing address is store
         * in seperate key pair
         */
        boolean isAddressNotFromVendorMaster = reqParams.optBoolean(GSTRConstants.isAddressNotFromVendorMaster, false);
        if(isAddressNotFromVendorMaster){
            billingShippingPOS = (isShipping==false) ? " bst.vendorbillingstateforindia "  : " bst.vendcustshippingstate ";
        }
        if (reqParams.has("statemismatch") && reqParams.optBoolean("statemismatch")) {
            int DNstatecolnum = reqParams.optInt(GSTRConstants.DN_STATE_COLUMN);
            builder.append(" and ((jecust.col" + DNstatecolnum + " is NULL or jecust.col" + DNstatecolnum + "='')");
            builder.append(" or (").append(billingShippingPOS).append(" IS NULL or ").append(billingShippingPOS).append(" ='' ))");

        }
        if (reqParams.has("withoutseqformat") && reqParams.optBoolean("withoutseqformat")) {
            builder.append(" and dn.seqformat is NULL");

        }
         if(reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))){
            builder.append(" and ").append(billingShippingPOS).append("=?");
            params.add(reqParams.optString("statesearch"));
        }
        /*
         To perform Quick search by Inv.No or customer name
         */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            if (!StringUtil.isNullOrEmpty(searchString)) {
                try {
                    String[] searchcol = new String[]{"dn.dnnumber","c.name","v.name"};

                    Map SearchStringMap = StringUtil.insertParamSearchStringMap((ArrayList) params, searchString, 3);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString,"and", searchcol);
                    builder.append(searchQuery);                 
                } catch (SQLException ex) {
                    Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        /**
         * No Of table with its aliases Invoice = inv Invoice Detail = invd
         * Journal entry = je Inventory = it Product = p Invoice Detail term Map
         * = ivtd EntitybasedLineLevelTermRate= eltr LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String groupby = " group by ivtd.id ";
        String selectCol = " IFNULL(c.name,v.name) as vendorid, gdh.gstin ,dn.id as dnid,dn.dnnumber,je.id as jeid,je.entrydate,"
                + "dnd.id as dntid,dnt.amount as rate, 1.0 as quantity,p.productid as productid,lt.term,ivtd.termamount,"
                + "dn.dnamountinbase, pfcd.value as hsncode,'' as pos,'' as col,'' as posid,"
                + "ivtd.percentage as taxrate,lt.defaultterms,'' as discPercentage,'' as discountValue, '4' as code,"
                + "ivtd.percentage as termpercentage,eltr.percentage as rulepercentage "
                + ",ivtd.taxtype " + GSTRegTypeSelectColumn + GSTCustVendTypeSelectColumn + GSTTypeName;

        String typeofjoin = " left ";
        String query = " select " + selectCol + " from dntaxentry dnt "
                + " inner join debitnote dn on dn.id=dnt.debitnote and (dn.purchasereturn is null) "
                + " inner join dndetails dnd on dn.id=dnd.debitnote "
//                + typeofjoin + " join purchasereturn pr on pr.id=dn.purchasereturn "
//                + typeofjoin + " join prdetails prd on prd.purchasereturn=pr.id "
                + joinForHSNandUQCCode + " join product p on p.id=dnt.productid "
                + joinForHSNandUQCCode + " join accproductcustomdata pcd on pcd.productId=p.id "
                + " left join gstdocumenthistory gdh on dn.id=gdh.refdocid and gdh.moduleid in ('10') "
                + " left join gsttaxclasshistory gtch on dnd.id=gtch.refdocid and gtch.moduleid in ('10') "             
                + " inner join journalentry je on dn.journalentry=je.id "
                + " inner join jedetail jed on jed.journalentry=je.id "
                + typeofjoin + " join vendor v on v.id=dn.vendor "
                + typeofjoin + " join customer c on c.id=dn.customer "
                + " inner join billingshippingaddresses bst on bst.id=dn.billingshippingaddresses"
                + joinSql
                + typeofjoin + " join debitnotedetailtermmap ivtd on ivtd.debitnotetaxentry=dnt.id "
                + typeofjoin + " join entitybasedlineleveltermsrate eltr on eltr.id=ivtd.entityterm"
                + typeofjoin + " join linelevelterms lt on lt.id=eltr.linelevelterms"
                + typeofjoin + " join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 " 
                + typeofjoin + " join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + ""
                + typeofjoin + " join fieldcombodata ptaxfcd on ptaxfcd.id=pcd.col" + taxclasscolnum + ""
                + builder + groupby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * Function to get purchase order for mismatch report.
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public List getGSTMissingPurchaseOrder(JSONObject reqParams) throws ServiceException, JSONException {
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        String having = "";
        String orderby = "";
        String additionalSelectCol = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        String col = "p.description as description";
        int hsncolnum = reqParams.optInt("hsncolnum");
        int uqccolnum = reqParams.optInt("uqccolnum");
        int taxclasscolnum = reqParams.optInt("taxclasscolnum");
        builder.append(" where po.company=? and po.deleteflag='F'  and po.approvestatuslevel = 11 and po.istemplate!=2 ");
        params.add(reqParams.optString("companyid"));
        params.add(reqParams.optString("companyid"));
        if (reqParams.has("ishsnblank") && reqParams.optBoolean("ishsnblank")) {
            builder.append(" and (pcd.col" + hsncolnum + " is NULL or pcd.col" + hsncolnum + "='' or (LENGTH(pfcd.value) > 8))");
            col = "pcd.col" + hsncolnum;
        }
        if (reqParams.has("isuqcblank") && reqParams.optBoolean("isuqcblank")) {
            builder.append(" and (pcd.col" + uqccolnum + " is NULL or pcd.col" + uqccolnum + "='' )");
            col = "pcd.col" + uqccolnum;
        }
        /**
         * Below query for GST Registration blank and Wrong GST registration
         * tagged. Example - Other than Regular, Composition and Unregistered is
         * wrong GST Registration type ,Please ref.ERP-35464 for more details
         */
        String GSTRegTypeSelectColumn = " ,'' as gstrtype";
        String GSTTypeName = " ,'' as gsttypename";
        if (reqParams.has("isgstregtypeblank") && reqParams.optBoolean("isgstregtypeblank")) {
            if (!reqParams.has("registrationType")) {
                joinSql += " left join masteritem mitm on mitm.id=gdh.gstrtype ";
            }
            String GSTRegTypeQuery = "'" + Constants.GSTRegType.get(Constants.GSTRegType_Composition) + "',";
            GSTRegTypeQuery += "'" + Constants.GSTRegType.get(Constants.GSTRegType_Regular) + "',";
            GSTRegTypeQuery += "'" + Constants.GSTRegType.get(Constants.GSTRegType_Unregistered) + "'";
            builder.append(" and ((gdh.gstrtype IS NULL or gdh.gstrtype='') or (mitm.defaultmasteritem not in(" + GSTRegTypeQuery + ")))");
            GSTRegTypeSelectColumn = " ,gdh.gstrtype as gstrtype ";
            GSTTypeName = " ,mitm.value as gsttypename";
        }
        /**
         * Below query for GST Customer blank and Wrong GST registration Please
         * ref.ERP-35464 for more details
         */
        String GSTCustVendTypeSelectColumn = " ,'' as custventypeid";
        if (reqParams.has("iscusttypeblank") && reqParams.optBoolean("iscusttypeblank")) {
            if (!reqParams.has("CustomerType") && !reqParams.has("registrationType")) {
                joinSql += " left join masteritem mitm on mitm.id=gdh.gstrtype ";
                joinSql += " left join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            }
            String GSTCustomerTypeQuery = " (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Composition) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Regular) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZ) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZWOPAY) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Unregistered) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_Import) + "')) ";
            builder.append(" and ((gdh.custventypeid IS NULL or gdh.custventypeid='') or (" + GSTCustomerTypeQuery + "))");
            GSTCustVendTypeSelectColumn = " ,gdh.custventypeid as custventypeid ";
            GSTTypeName = " ,mstrItem.value as gsttypename";
        }
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is Unregistered i.e. GSTIN is not present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("ishistoryblank") && reqParams.optBoolean("ishistoryblank")) {
            builder.append(" and po.id not in (select refdocid from gstdocumenthistory where moduleid in ('18'))");
        }
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql += " inner join masteritem mitm on mitm.id=gdh.gstrtype";
            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            joinSql += " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }
        if (reqParams.has("poentitycolnum")) {
            /**
             * Column no of Entity dimension's value stored in Invoice
             */
            joinSql += " inner join purchaseordercustomdata pocust on pocust.poID=po.id ";
            int colnum = reqParams.optInt("poentitycolnum");
            builder.append(" and pocust.col" + colnum + "=?");
            params.add(reqParams.optString("poentityValue"));
        }
        if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (po.orderdate>=? and po.orderdate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            builder.append(" and ptaxfcd.valuetype=?");
            String Type = reqParams.optString("taxClassType");
            params.add((int) FieldComboData.ValueTypeMap.get(Type));
        }

        /*
         Flag for GST-calculation is based on Billing Address or Shipping Address
         */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping = true;
        }
        String billingShippingPOS = (isShipping == false) ? "bst.billingstate" : "bst.shippingstate";
       /**
         * For India country and vendor transactions and
         * isAddressFromVendorMaster is off then vendor billing address is store
         * in separate key pair
         */
        boolean isAddressNotFromVendorMaster = reqParams.optBoolean(GSTRConstants.isAddressNotFromVendorMaster, false);
        if(isAddressNotFromVendorMaster){
            billingShippingPOS = (isShipping==false) ? " bst.vendorbillingstateforindia "  : " bst.vendcustshippingstate ";
        }
        if (reqParams.has("statemismatch") && reqParams.optBoolean("statemismatch")) {
            int sostatecolnum = reqParams.optInt("postatecolnum");
            builder.append(" and ((pocust.col" + sostatecolnum + " is NULL or pocust.col" + sostatecolnum + "='')");
            builder.append(" or (").append(billingShippingPOS).append(" IS NULL or ").append(billingShippingPOS).append(" ='' ))");

        }
        if (reqParams.has("withoutseqformat") && reqParams.optBoolean("withoutseqformat")) {
            builder.append(" and po.seqformat is NULL");

        }
        if (reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))) {
            builder.append(" and ").append(billingShippingPOS).append("=?");
            params.add(reqParams.optString("statesearch"));
        }
        /*
         To perform Quick search by Inv.No or customer name
         */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            if (!StringUtil.isNullOrEmpty(searchString)) {
                try {
                    String[] searchcol = new String[]{"po.ponumber", "c.name"};

                    Map SearchStringMap = StringUtil.insertParamSearchStringMap((ArrayList) params, searchString, 2);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                    builder.append(searchQuery);
                } catch (SQLException ex) {
                    Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        /**
         * No Of table with its aliases Invoice = inv Invoice Detail = invd
         * Journal entry = je Inventory = it Product = p Invoice Detail term Map
         * = ivtd EntitybasedLineLevelTermRate= eltr LineLevelTerms= lt Field
         * Combo Data for location 1=shl1
         *
         */
        String groupby = "";
        String selectCol = " c.name as customerid,gdh.gstin,po.id as invoiceid,po.ponumber,'' as jeid,po.orderdate,"
                + "pod.id as invoicedetailid,pod.rate,pod.quantity,p.productid as productid,lt.term,ivtd.termamount,"
                + "po.totalamountinbase,pfcd.value as hsncode,ptaxfcd.value as pos," + col + ",'' as posid,"
                + "ivtd.percentage as taxrate,lt.defaultterms,'' as discPercentage,pod.discount as discountValue, '18' as code,"
                + "ivtd.percentage as termpercentage,eltr.percentage as rulepercentage "
                + ",ivtd.taxtype " + GSTRegTypeSelectColumn + GSTCustVendTypeSelectColumn + GSTTypeName;

        String query = " select " + selectCol + " from purchaseorder po inner join podetails pod on po.id=pod.purchaseorder and po.company=?"
                + " left join gstdocumenthistory gdh on po.id=gdh.refdocid and gdh.moduleid in ('18') "
                + " inner join gsttaxclasshistory gtch on pod.id=gtch.refdocid and gtch.moduleid in ('18') "
                + " inner join vendor c on c.id=po.vendor "
                + " inner join product p on p.id=pod.product "
                + " inner join billingshippingaddresses bst on bst.id=po.billingshippingaddresses"
                + " left join purchaseorderdetailstermmap ivtd on ivtd.podetails=pod.id "
                + " left join entitybasedlineleveltermsrate eltr on eltr.id=ivtd.entityterm"
                + " left join linelevelterms lt on lt.id=eltr.linelevelterms"
                + " left join fieldcombodata shl1 on shl1.id=eltr.shippedloc1 "
                + " left join accproductcustomdata pcd on pcd.productId=p.id "
                + " left join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + ""
                + " left join fieldcombodata ptaxfcd on ptaxfcd.id=pcd.col" + taxclasscolnum + ""
                + " " + joinSql + builder + groupby + having + orderby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    /**
     * Function to get Goods Receipt Order in mismatch report.
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public List getGSTMissingGRN(JSONObject reqParams) throws ServiceException, JSONException{
        List params = new ArrayList();
        StringBuilder builder = new StringBuilder();
        String joinSql = "";
        DateFormat df = null;
        if (reqParams.opt(Constants.df) != null) {
            df = (DateFormat) reqParams.opt(Constants.df);
        }
        String col = "p.description as description";
        int hsncolnum = reqParams.optInt("hsncolnum");
        int uqccolnum = reqParams.optInt("uqccolnum");
        int taxclasscolnum = reqParams.optInt("taxclasscolnum");
        builder.append(" where grn.company=? and grn.deleteflag='F'"); 
        params.add(reqParams.optString("companyid"));
                if (reqParams.has("ishsnblank") && reqParams.optBoolean("ishsnblank")) {
            builder.append(" and (pcd.col" + hsncolnum + " is NULL or pcd.col" + hsncolnum + "='' or (LENGTH(pfcd.value) > 8))");
            col = "pcd.col" + hsncolnum;
        }      
        if (reqParams.has("isuqcblank") && reqParams.optBoolean("isuqcblank")) {
            builder.append(" and (pcd.col" + uqccolnum + " is NULL or pcd.col" + uqccolnum + "='' )");
            col = "pcd.col" + uqccolnum;
        }
        /**
         * Below query for GST Registration blank and Wrong GST registration
         * tagged. Example - Other than Regular, Composition and Unregistered is
         * wrong GST Registration type ,Please ref.ERP-35464 for more details
         */
        String GSTRegTypeSelectColumn = " ,'' as gstrtype";
        String GSTTypeName = " ,'' as gsttypename";
        if (reqParams.has("isgstregtypeblank") && reqParams.optBoolean("isgstregtypeblank")) {
            if(!reqParams.has("registrationType")){
                joinSql+= " left join masteritem mitm on mitm.id=gdh.gstrtype ";
            }
            String GSTRegTypeQuery = "'"+Constants.GSTRegType.get(Constants.GSTRegType_Composition)+"',";
            GSTRegTypeQuery += "'"+Constants.GSTRegType.get(Constants.GSTRegType_Regular)+"',";
            GSTRegTypeQuery += "'"+Constants.GSTRegType.get(Constants.GSTRegType_Unregistered)+"'";
            builder.append(" and ((gdh.gstrtype IS NULL or gdh.gstrtype='') or (mitm.defaultmasteritem not in(" + GSTRegTypeQuery + ")))");
            GSTRegTypeSelectColumn = " ,gdh.gstrtype as gstrtype ";
            GSTTypeName = " ,mitm.value as gsttypename";
        }
        /**
         * Below query for GST Customer blank and Wrong GST registration
         * Please ref.ERP-35464 for more details
         */
        String GSTCustVendTypeSelectColumn = " ,'' as custventypeid";
        if (reqParams.has("iscusttypeblank") && reqParams.optBoolean("iscusttypeblank")) {
            if (!reqParams.has("CustomerType") && !reqParams.has("registrationType")) {
                joinSql += " left join masteritem mitm on mitm.id=gdh.gstrtype ";
                joinSql += " left join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            }
            String GSTCustomerTypeQuery = " (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Composition) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Regular) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZ) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_SEZWOPAY) + "')) "
                    + " or (mitm.defaultmasteritem='" + Constants.GSTRegType.get(Constants.GSTRegType_Unregistered) + "' and mstrItem.defaultmasteritem not in('" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_NA) + "', '" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_Export) + "','" + Constants.CUSTVENTYPE.get(Constants.CUSTVENTYPE_ExportWOPAY) + "')) ";
            builder.append(" and ((gdh.custventypeid IS NULL or gdh.custventypeid='') or (" + GSTCustomerTypeQuery+"))");
            GSTCustVendTypeSelectColumn = " ,gdh.custventypeid as custventypeid ";
            GSTTypeName = " ,mstrItem.value as gsttypename";
        }
        if (reqParams.has("isGSTINnull")) {
            boolean isGSTINnull = reqParams.optBoolean("isGSTINnull");
            if (isGSTINnull) {
                /**
                 * If customer is Unregistered i.e. GSTIN is not present
                 */
                builder.append(" and gdh.gstin ='' ");
            } else {
                /**
                 * If customer is Registered i.e. GSTIN is present
                 */
                builder.append(" and gdh.gstin <>'' ");
            }
        }
        if (reqParams.has("ishistoryblank") && reqParams.optBoolean("ishistoryblank")) {
            builder.append(" and grn.id not in (select refdocid from gstdocumenthistory where moduleid in ('28'))");
        }
        if (reqParams.has("registrationType")) {
            /**
             * Append condition for GST Req Type
             */
            joinSql+= " inner join masteritem mitm on mitm.id=gdh.gstrtype";
            String Type = reqParams.optString("registrationType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mitm.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.GSTRegType.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mitm.defaultmasteritem=?");
                params.add(Constants.GSTRegType.get(Type).toString());
            }
        }
        if (reqParams.has("CustomerType")) {
            /**
             * Append condition for customer Type
             */
            joinSql+= " inner join masteritem mstrItem on gdh.custventypeid= mstrItem.id ";
            String Type = reqParams.optString("CustomerType");
            if (Type.contains(",")) {
                StringBuilder conditionBuilder = new StringBuilder();
                conditionBuilder.append(" and mstrItem.defaultmasteritem in (");
                String typeArr[] = Type.split(",");
                for (String type : typeArr) {
                    if (conditionBuilder.indexOf("?") == -1) {
                        conditionBuilder.append("?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    } else {
                        conditionBuilder.append(",?");
                        params.add(Constants.CUSTVENTYPE.get(type).toString());
                    }
                }
                conditionBuilder.append(") ");
                builder.append(conditionBuilder.toString());
            } else {
                builder.append(" and mstrItem.defaultmasteritem=?");
                params.add(Constants.CUSTVENTYPE.get(Type).toString());
            }
        }       

        if (reqParams.has("taxClassType")) {
            /**
             * Append condition for Product Tax Class Type Ex : Exempt
             */
            builder.append(" and ptaxfcd.valuetype=?");
            String Type = reqParams.optString("taxClassType");
            params.add((int) FieldComboData.ValueTypeMap.get(Type));
        }
        if (reqParams.has("isRCMApplicable")) {
            /**
             * Append condition for RCM Applicable or not
             */
            builder.append(" and grn.rcmapplicable=?");
            boolean InvoiceType = reqParams.optBoolean("isRCMApplicable");
            if (InvoiceType) {
                params.add('T');
            } else {
                params.add('F');
            }
        }
         /*
          Flag for GST-calculation is based on Billing Address or Shipping Address
        */
        boolean isShipping = false;
        if (reqParams.has("isShipping") && reqParams.optBoolean("isShipping") == true) {
            isShipping= true;
        }
        String billingShippingPOS = (isShipping==false)?"bst.billingstate":"bst.shippingstate" ;
       /**
         * For India country and vendor transactions and
         * isAddressFromVendorMaster is off then vendor billing address is store
         * in separate key pair
         */
        boolean isAddressNotFromVendorMaster = reqParams.optBoolean(GSTRConstants.isAddressNotFromVendorMaster, false);
        if(isAddressNotFromVendorMaster){
            billingShippingPOS = (isShipping==false) ? " bst.vendorbillingstateforindia "  : " bst.vendcustshippingstate ";
        }
        if (reqParams.has("statemismatch") && reqParams.optBoolean("statemismatch")) {
            int grnstatecolnum = reqParams.optInt("grnstatecolnum");
            builder.append(" and ((grncust.col" + grnstatecolnum + " is NULL or grncust.col" + grnstatecolnum + "='')");
            builder.append(" or (").append(billingShippingPOS).append(" IS NULL or ").append(billingShippingPOS).append(" ='' ))");

        }
        if (reqParams.has("withoutseqformat") && reqParams.optBoolean("withoutseqformat")) {
            builder.append(" and grn.seqformat is NULL");

        }
        if(reqParams.has("statesearch") && !StringUtil.isNullOrEmpty(reqParams.optString("statesearch"))){
            builder.append(" and ").append(billingShippingPOS).append("=?");
            params.add(reqParams.optString("statesearch"));
        }
        /*
         To perform Quick search by Inv.No or customer name
         */
        if (reqParams.has("ss") && !StringUtil.isNullOrEmpty(reqParams.optString("ss"))) {
            String searchString = reqParams.optString("ss");
            if (!StringUtil.isNullOrEmpty(searchString)) {
                try {
                    String[] searchcol = new String[]{"grn.gronumber","c.name"};

                    Map SearchStringMap = StringUtil.insertParamSearchStringMap((ArrayList) params, searchString, 2);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                    builder.append(searchQuery);                    
                } catch (SQLException ex) {
                    Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
         if (reqParams.has("grnentitycolnum")) {
    /**
             * Column no of Entity Value stored in Receipt
             */
            joinSql += " inner join grordercustomdata grncust on grncust.goodsreceiptorderid=grn.id ";
            int colnum = reqParams.optInt("grnentitycolnum");
            builder.append(" and grncust.col" + colnum + "=?");
            params.add(reqParams.optString("grnentityValue"));
        }
         if (reqParams.has("startdate") && df != null) {
            try {
                if (builder.length() > 1) {
                    builder.append(" and (grn.grorderdate>=? and grn.grorderdate<=?) ");
                }
                params.add(df.parse(reqParams.optString("startdate")));
                params.add(df.parse(reqParams.optString("enddate")));
            } catch (ParseException ex) {
                Logger.getLogger(AccEntityGstImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         String typeofjoin = " inner ";
        if (reqParams.has("typeofjoinisleft") && reqParams.optBoolean("typeofjoinisleft")) {
            typeofjoin = " left ";
        }
    
        String groupby="",orderby="";
        String selectCol = " c.name as customerid,gdh.gstin,grn.id as invoiceid,grn.gronumber,'' as jeid,grn.grorderdate,grndtl.id as invoicedetailid,grndtl.rate,grndtl.deliveredquantity,"
                +"p.productid as productid,lt.term,ivtd.termamount,grn.totalamountinbase,pfcd.value as hsncode,ptaxfcd.value as pos,pcd.col4,"
                +"'' as posid,ivtd.percentage as taxrate,lt.defaultterms,'' as discPercentage,grndtl.discount as discountValue, '28' as code,"
                +"ivtd.percentage as termpercentage,eltr.percentage as rulepercentage ,ivtd.taxtype ,'' as gstrtype ,'' as custventypeid ,'' as gsttypename  " 
                + GSTRegTypeSelectColumn + GSTCustVendTypeSelectColumn + GSTTypeName;

          joinSql += " left join sequenceformat sq on grn.seqformat=sq.id ";
        String query = " select " + selectCol + " from grodetails grndtl inner join grorder grn on grndtl.grorder=grn.id "  
                + " left join gstdocumenthistory gdh on grn.id=gdh.refdocid and gdh.moduleid in ('28') "
                + " inner join gsttaxclasshistory gtch on grndtl.id=gtch.refdocid and gtch.moduleid in ('28') " 
                + " inner join vendor c on c.id=grn.vendor "
                + " inner join product p on p.id=grndtl.product "
                + " left  join receiptorderdetailtermsmap ivtd on ivtd.grodetail=grndtl.id "
                + " left  join entitybasedlineleveltermsrate eltr on eltr.id=ivtd.entityterm"
                + " left  join linelevelterms lt on lt.id=eltr.linelevelterms"
                + " left  join billingshippingaddresses bst on bst.id=grn.billingshippingaddresses"
                + " left  join accproductcustomdata pcd on pcd.productId=p.id "
                + " left  join fieldcombodata pfcd on pfcd.id=pcd.col" + hsncolnum + ""
                + " left  join fieldcombodata ptaxfcd on ptaxfcd.id=pcd.col" + taxclasscolnum + ""
                +joinSql+builder+groupby+orderby;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
}
