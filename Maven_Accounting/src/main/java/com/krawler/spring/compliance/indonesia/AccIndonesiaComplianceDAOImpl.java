/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 */
package com.krawler.spring.compliance.indonesia;

import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndonesiaConstants;
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
 * @author Rahul A. Bhawar - Indonesia Compliance
 */
public class AccIndonesiaComplianceDAOImpl extends BaseDAO implements AccIndonesiaComplianceDAO {

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
        DateFormat df = null;
        if (requestParams.opt(Constants.df) != null) {
            df = (DateFormat) requestParams.opt(Constants.df);
        }
        String companyid = requestParams.optString(Constants.companyKey);
        params.add(companyid);
        params.add(companyid);
        params.add(companyid);
        params.add(companyid);
        params.add(companyid);
        params.add(companyid);
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
        /**
         * FG_PENGGANTI
         * Replacement FG  0 or 1 Regarding to IS_FP_PENGGANTI in Sales Invoice? if yes then 1 else 0 Default : No
         */
        String FG_PENGGANTIColumnString = " ,'0' as replacement";
        if(sectionExtraParams.has(IndonesiaConstants.Custom_FG_PENGGANTI) && sectionExtraParams.optInt(IndonesiaConstants.Custom_FG_PENGGANTI, 0) != 0){
             joinSql += " inner join accjecustomdata jecust on jecust.journalentryId = je.id "
                     + "   left join fieldcombodata fcd on fcd.id = jecust.col" + sectionExtraParams.optInt(IndonesiaConstants.Custom_FG_PENGGANTI);
             FG_PENGGANTIColumnString  = " ,if(fcd.value='Yes', '1','0') as replacement ";
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
         * 13 - inv.invoiceamountinbase
         * 14 - code - Tax Type (Customer)
         * 15 - isSalesInvoice - 1 
         * 16 - entrydate (Invoice creation date) 
         * 17 - NPWP - from Customer - pannumber
         * 18 - billingaddress - Customer Billing Address
         * 19 - ProductID 
         * 20 - Name - Product Name
         * 21 - percent - Tax Percentage
         * 22 - Postal Code - billingPostalCode
         * 23 - billingphone 
         * 24 -  istaxApplied  - if(inv.tax is null, if(invd.tax is null,'F' , 'T') , 'T')
         * 25 - additionalmemo ((CASE mstrItem.code WHEN '07' THEN am.code  WHEN '08' THEN am.code  ELSE '0' END) as additionalmemo)
         * 26 - FG_PENGGANTI - Replacement FG  0 or 1 Regarding to IS_FP_PENGGANTI in Sales Invoice? if yes then 1 else 0 Default : No
         */
        selectColumn = " inv.id as invoiceid, inv.invoicenumber, IF(je.externalcurrencyrate>0,(invd.rate/je.externalcurrencyrate),invd.rate ) as rate,it.quantity, je.externalcurrencyrate, "
                + " d.inpercent as discPercentageType,IF(d.inpercent='T', d.discount, IF(je.externalcurrencyrate>0,(d.discount/je.externalcurrencyrate),d.discount)) as discountValueInBase, "
                + " invd.rowtaxamountinbase, c.id as customerid,c.name as customername, invd.id as invoicedetailid, "
                + " inv.tax is NOT NULL as isGlobalTax, inv.taxamountinbase, inv.invoiceamountinbase , mstrItem.code as taxType,"
                + " '1' as isSalesInvoice ,je.entrydate, c.pannumber, ad.billingaddress, p.productid , p.name,"
                + " tl.percent, ad.billingpostal, ad.billingphone, if(inv.tax is null, if(invd.tax is null, 'F' , 'T') , 'T') as isTaxApplied,"
                + " (CASE mstrItem.code WHEN '07' THEN am.code  WHEN '08' THEN am.code  ELSE '0' END) as additionalmemo "
                + FG_PENGGANTIColumnString;
        query = " select " + selectColumn + " from invoice inv  "
                + " inner join billingshippingaddresses ad on ad.id=inv.billingshippingaddresses and (inv.company = ? and inv.deleteflag='F' and inv.isdraft = false and inv.approvestatuslevel = 11 and inv.istemplate!=2) "
                + " inner join customer c on c.id=inv.customer and c.company = ? "
                //+ " inner join customeraddressdetails cad on cad.customer=c.id and cad.isdefaultaddress='T' and cad.isbillingaddress='T' "
                + " inner join invoicedetails invd on inv.id=invd.invoice "
                + " inner join journalentry je on je.id=inv.journalentry and je.company = ? "
                + " inner join inventory it on it.id=invd.id and it.company = ? "
                + " inner join product p on p.id=it.product and p.company = ? "
                + " left join tax t on (inv.tax = t.id or invd.tax = t.id) and t.company = ? "
                + " left join taxlist tl on t.id = tl.tax "
                + " left join additionalmemo am on am.id = inv.additionalmemo "
                + " left join masteritem mstrItem on c.gstcustomertype= mstrItem.id and mstrItem.company = ? "
                + " left join discount d on d.id = invd.discount and d.company = ? "
                + joinSql
                + condition;
        List returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
}
