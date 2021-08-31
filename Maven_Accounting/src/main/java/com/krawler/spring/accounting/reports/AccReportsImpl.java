/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.DefaultHeader;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import java.util.List;
import java.util.Map;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.journalentry.accJournalEntryImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceImpl;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.Calendar;

/**
 *
 * @author krawler
 */
public class AccReportsImpl extends BaseDAO implements AccReportsDAO {

    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    
    public KwlReturnObject getLedgerInfo(Map<String, Object> requestParams) throws ServiceException {
        String filterConjuctionCriteria = (String)requestParams.get("filterConjuctionCriteria");
        String searchjson = (String)requestParams.get("searchjson");
        ArrayList advSearchParams = new ArrayList();        
        String accountid = (String)requestParams.get("accountid");
        String companyid = (String)requestParams.get("companyid");
        String startdate = (String)requestParams.get("startdate");
        String enddate = (String)requestParams.get("enddate");
        String othersselect = (String)requestParams.get("othersselect");
        String jeinvrevalselect = (String)requestParams.get("jeinvrevalselect");
        String jegrrevalselect = (String)requestParams.get("jegrrevalselect");
        String cnselect = (String)requestParams.get("cnselect");
        String invoicedetailsselect = (String)requestParams.get("invoicedetailsselect");
        String invoicetermselect = (String)requestParams.get("invoicetermselect");
        String invoiceroundingselect = (String)requestParams.get("invoiceroundingselect");
        String invoicecapselect = (String)requestParams.get("invoicecapselect");
        String grexpdetailsselect = (String)requestParams.get("grexpdetailsselect");
        String grexpdetailslandedselect = (String)requestParams.get("grexpdetailsselect");
        String grdetailsselect = (String)requestParams.get("grdetailsselect");
        String grtermselect = (String)requestParams.get("grtermselect");
        String grroundingselect = (String)requestParams.get("grroundingselect");
        String grcapselect = (String)requestParams.get("grcapselect");
        String dnselect = (String)requestParams.get("dnselect");
        String receiptselect = (String)requestParams.get("receiptselect");
        String receiptwoselect = (String)requestParams.get("receiptwoselect");
        String paymentselect = (String)requestParams.get("paymentselect");
        String saselect = (String)requestParams.get("saselect");
        String srselect = (String)requestParams.get("srselect");
        String prselect = (String)requestParams.get("prselect");
        String doselect = (String)requestParams.get("doselect");
        String groselect = (String)requestParams.get("groselect");
        String selectedCurrencyIds = (String)requestParams.get("selectedCurrencyIds");
        String advSearchCustomtablejoin = "";
        String advSearchFilterString = "";
        String advSearchSqljoin = "";
        if(!StringUtil.isNullOrEmpty(searchjson)){
            Map<String, Object> advSearchAttributes = null;
            boolean isKnockOffAdvancedSearch = false;
            boolean isAdvanceSearchOnGlobalDimension = false;
            if (requestParams.containsKey("advSearchAttributes")) {
                advSearchAttributes = (Map<String, Object>) requestParams.get("advSearchAttributes");
            } else {
                CompanyAccountPreferences preferences = null;
                advSearchAttributes = new HashMap<String, Object>();
                if (!StringUtil.isNullOrEmpty(companyid)) {
                    KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                    preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
                }
                advSearchAttributes = accJournalEntryobj.getAdvanceSearchAttributes(searchjson, preferences, companyid, null, filterConjuctionCriteria);
            }
            if(advSearchAttributes.containsKey("isKnockOffAdvancedSearch") && advSearchAttributes.get("isKnockOffAdvancedSearch")!=null){
                isKnockOffAdvancedSearch=(Boolean)advSearchAttributes.get("isKnockOffAdvancedSearch");
            }
            if(advSearchAttributes.containsKey("isAdvanceSearchOnGlobalDimension") && advSearchAttributes.get("isAdvanceSearchOnGlobalDimension")!= null){
                isAdvanceSearchOnGlobalDimension=(Boolean)advSearchAttributes.get("isAdvanceSearchOnGlobalDimension");
            }

            advSearchParams = (ArrayList) advSearchAttributes.get("params");
            advSearchFilterString = advSearchAttributes.containsKey("mySearchFilterString") && !StringUtil.isNullOrEmpty((String) advSearchAttributes.get("mySearchFilterString")) ? (String) advSearchAttributes.get("mySearchFilterString") : "";
            advSearchCustomtablejoin = advSearchAttributes.containsKey("customtablejoin") && !StringUtil.isNullOrEmpty((String) advSearchAttributes.get("customtablejoin")) ? (String) advSearchAttributes.get("customtablejoin") : "";
            advSearchSqljoin += advSearchAttributes.containsKey("sqljoin") && !StringUtil.isNullOrEmpty((String) advSearchAttributes.get("sqljoin")) ? (String) advSearchAttributes.get("sqljoin") : "";

            if (isKnockOffAdvancedSearch) {
                String knockOffCondition = "";
                if (advSearchAttributes.containsKey("knockOffCondition") && advSearchAttributes.get("knockOffCondition") != null) {
                    knockOffCondition = (String) advSearchAttributes.get("knockOffCondition");
                }
                advSearchFilterString += knockOffCondition;
            } else {
                if (isAdvanceSearchOnGlobalDimension) {
                    /*
                     * If advance search is performed on only Global custom
                     * field/dimension then inner join is not get applied on
                     * accjedetailcustom data. So additional(separated jed) as well as default entry
                     * for payment method get fetched so payment method's amount get
                     * doubled.So added check to restrict additional jedetails.
                     */
                    advSearchFilterString += " and jed.isseparated = 'F' ";
                }
            }
        } else { //As discussed with Shrinath S. added below code
            advSearchFilterString += " and jed.isseparated = 'F' ";
        }
        
        if (!StringUtil.isNullOrEmpty(selectedCurrencyIds)) {
            selectedCurrencyIds = AccountingManager.getFilterInString(selectedCurrencyIds);
            advSearchFilterString += " and je.currency in " + selectedCurrencyIds + " ";
        }


        String query = "select a.* from ( "
                + invoicedetailsselect + " \n"
                + "from account acc \n"
                + "inner join jedetail jed on jed.account=acc.id  \n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.isreval=0\n"
                + "inner join invoice inv on inv.id=je.transactionid\n"
                + "inner join invoicedetails invd on invd.invoice =inv.id\n"
                + "left join invoicetermssales term on term.id in (select itm.term from invoicetermsmap itm where itm.invoice =inv.id) and term.company=je.company\n"
                + "left join invoicetermsmap invtm on invtm.invoice=inv.id\n"
                + "inner join inventory i on i.id=invd.id\n"
                + "inner join customer cust on cust.id=inv.customer\n"
                + "inner join currency curr on inv.currency =curr.currencyid\n"
                + "inner join product p on p.id = i.product\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join compaccpreferences cap on cap.id=inv.company \n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "left join masteritem mi on mi.company=inv.company and inv.mastersalesperson=mi.id\n"
                + "left join tax tax on tax.id=invd.tax \n"
                + "left join tax gtax on gtax.id=inv.tax \n"
                + "left join tax ttax on ttax.id=invtm.termtax \n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=inv.company and jedcd.recdetailid=invd.id \n" 
                + "left join accjecustomdata jecd on jecd.company=inv.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
//                + "where inv.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ?  and (tax.account=acc.id  OR p.salesaccount=jed.account OR invd.salesjedid=jed.id OR inv.centry =jed.id or inv.taxentry = jed.id or invd.gstjedid = jed.id) group by if(inv.centry =jed.id or inv.taxentry = jed.id,je.id,jed.id)"
//                + "where inv.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ? and ((tax.account=acc.id OR  invd.gstjedid = jed.id) OR (p.salesaccount=jed.account AND invd.salesjedid=jed.id) OR (inv.centry =jed.id and cust.account) or (inv.taxentry = jed.id)) group by if(inv.centry =jed.id or inv.taxentry = jed.id,je.id,jed.id)"
//                + "where inv.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ? and ((tax.account=acc.id ) OR (p.salesaccount=jed.account) OR (cust.account and jed.id = COALESCE(inv.centry)) or (inv.taxentry = jed.id)) group by if(inv.centry =jed.id or inv.taxentry = jed.id,je.id,jed.id)"
                + "where inv.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ? and jed.roundingdifferencedetail ='F' and (acc.id<>cap.roundingDifferenceAccount and je.typevalue<>4 and acc.id<>cap.discountGiven and acc.id<>cap.discountReceived)  and ((if((invd.salesjedid is null or invd.salesjedid=''),p.salesAccount = jed.account, invd.salesjedid = jed.id)) or term.account=acc.id or (ttax.account=acc.id) OR cust.account =acc.id or inv.account=acc.id or inv.centry =jed.id  OR p.depreciationprovisionglaccount = acc.id or p.depreciationglaccount=acc.id or p.sellassetglaccount=acc.id or p.writeoffassetaccount=acc.id or inv.taxentry = jed.id or if((comp.country='"+Constants.indian_country_id+"' or comp.country='"+Constants.USA_country_id+"'),false, if(invd.gstjedid is null or invd.gstjedid ='',tax.account=acc.id,invd.gstjedid=jed.id))) " +advSearchFilterString +" group by if(inv.centry =jed.id or inv.taxentry = jed.id,je.id,jed.id)"
                + " UNION "
                + invoiceroundingselect + " \n"
                + "from account acc                  \n"
                + "inner join jedetail jed on jed.account=acc.id\n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.isreval=0\n"
                + "inner join invoice inv on inv.id=je.transactionid\n"
                + "inner join customer cust on cust.id=inv.customer\n"
                + "inner join currency curr on inv.currency =curr.currencyid\n"
                + "inner join compaccpreferences cap on cap.id=inv.company\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "left join masteritem mi on mi.company=inv.company and inv.mastersalesperson=mi.id\n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=inv.company and jedcd.jedetailid=jed.id \n" 
                + "left join accjecustomdata jecd on jecd.company=inv.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + "where inv.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ?  and (cap.roundingDifferenceAccount = acc.id or je.typevalue=4)" +advSearchFilterString +" group by jed.id"
                + " UNION "
                + invoicetermselect + "\n"
                + "from account acc \n"
                + "inner join jedetail jed on jed.account=acc.id\n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.isreval=0\n"
                + "inner join invoice inv on inv.id=je.transactionid\n"
                + "inner join invoicetermsmap invd on invd.invoice =inv.id\n"
                + "inner join invoicetermssales term on term.id =invd.term and term.account=acc.id\n"
                + "inner join customer cust on cust.id=inv.customer\n"
                + "inner join currency curr on inv.currency =curr.currencyid\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "left join compaccpreferences cap on cap.id=inv.company \n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "left join masteritem mi on mi.company=inv.company and inv.mastersalesperson=mi.id\n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=inv.company and jedcd.jedetailid=jed.id \n" 
                + "left join accjecustomdata jecd on jecd.company=inv.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + "where inv.company= ?  and acc.id= ? and je.entrydate >= ? and (acc.id<>cap.roundingDifferenceAccount and je.typevalue<>4 and acc.id<>cap.discountGiven and acc.id<>cap.discountReceived )  and je.entrydate <= ? and acc.id=term.account and jed.amount=ABS(invd.termamount)" +advSearchFilterString +" group by jed.id"
                + " UNION "
                + invoicetermselect + "\n"
                + "from account acc \n"
                + "inner join jedetail jed on jed.account=acc.id\n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.isreval=0\n"
                + "inner join invoice inv on inv.id=je.transactionid\n"
                + "inner join invoicedetails invd on (invd.invoice = inv.id and invd.salesjedid <> jed.id) \n"
                + "inner join customer cust on cust.id=inv.customer\n"
                + "inner join currency curr on inv.currency =curr.currencyid\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "inner join invoicedetailtermsmap idtm on idtm.invoicedetail=invd.id\n" 
                + "inner join linelevelterms llt on idtm.term=llt.id \n"
                + "left join compaccpreferences cap on cap.id=inv.company \n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "left join masteritem mi on mi.company=inv.company and inv.mastersalesperson=mi.id\n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=inv.company and jedcd.jedetailid=jed.id \n" 
                + "left join accjecustomdata jecd on jecd.company=inv.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + "where inv.company= ?  and acc.id= ? and je.entrydate >= ? and (acc.id<>cap.roundingDifferenceAccount and je.typevalue<>4 and acc.id<>cap.discountGiven and acc.id<>cap.discountReceived)  and jed.roundingdifferencedetail ='F' and je.entrydate <= ? and (acc.id=llt.account or acc.id=llt.payableaccount or acc.id=creditnotavailedaccount) and jed.amount=ABS(idtm.termamount)" +advSearchFilterString +" group by jed.id"                
                + " UNION "
                + invoicecapselect + " \n"
                + "from account acc \n"
                + "inner join jedetail jed on jed.account=acc.id\n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.isreval=0\n"
                + "inner join invoice inv on inv.id=je.transactionid\n"
                + "inner join customer cust on cust.id=inv.customer\n"
                + "inner join currency curr on inv.currency =curr.currencyid\n"
                + "inner join compaccpreferences cap on cap.id=inv.company \n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "left join masteritem mi on mi.company=inv.company and inv.mastersalesperson=mi.id\n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=inv.company and jedcd.jedetailid=jed.id \n" 
                + "left join accjecustomdata jecd on jecd.company=inv.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + "where inv.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ?  and (acc.id=cap.discountGiven OR acc.id=cap.discountReceived OR acc.id=cap.shippingCharges)  " +advSearchFilterString +" group by jed.id"
                + " UNION "
                + grdetailsselect + " \n"
                + "from account acc \n"
                + "inner join jedetail jed on jed.account=acc.id\n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.isreval=0\n"
                + "inner join goodsreceipt gr on gr.id=je.transactionid\n"
                + "inner join grdetails grd on grd.goodsreceipt =gr.id \n"
                + "left join invoicetermssales term on term.id in (select rtm.term from receipttermsmap rtm where rtm.goodsreceipt =gr.id) and term.company=je.company \n"
                + "left join receipttermsmap rtmtm on rtmtm.goodsreceipt=gr.id\n"
//                + "inner join grdetails grd on grd.goodsreceipt =gr.id \n"
                + "inner join inventory i on i.id=grd.id\n"
                + "inner join vendor vend on vend.id=gr.vendor\n"
                + "inner join currency curr on gr.currency =curr.currencyid\n"
                + "inner join product p on p.id = i.product\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join compaccpreferences cap on cap.id=gr.company\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "left join masteritem mi on mi.company=gr.company and gr.masteragent=mi.id\n"
//                + "left join masteritem mitds on mitds.company=gr.company and mitds.mastergroup='"+Constants.NatureofPaymentGroup+"'\n"
                + "left join tax tax on tax.id=grd.tax \n"
                + "left join tax gtax on gtax.id=gr.tax \n"
                + "left join tax ttax on ttax.id=rtmtm.termtax \n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=gr.company and jedcd.recdetailid=grd.id \n" 
                + "left join accjecustomdata jecd on jecd.company=gr.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
//                + "where gr.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ?  and  (tax.account=acc.id  OR p.purchaseAccount = jed.account OR grd.purchasejedid=jed.id OR gr.centry =jed.id or gr.taxentry = jed.id or grd.gstjedid = jed.id) group by if(gr.centry =jed.id or gr.taxentry = jed.id,je.id,jed.id)"
//                + "where gr.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ?  and ((tax.account=acc.id OR  grd.gstjedid = jed.id) OR (p.purchaseAccount = jed.account and grd.purchasejedid=jed.id) OR (vend.account=acc.id and gr.centry =jed.id) or (gr.taxentry = jed.id)) group by if(gr.centry =jed.id or gr.taxentry = jed.id,je.id,jed.id)"
                + "where gr.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ? and (acc.id<>cap.roundingDifferenceAccount and je.typevalue<>4 and acc.id<>cap.discountGiven and acc.id<>cap.discountReceived) and jed.roundingdifferencedetail ='F' and ((if((grd.purchasejedid is null or grd.purchasejedid = ''),p.purchaseAccount = jed.account, grd.purchasejedid = jed.id)) or term.account=acc.id or (ttax.account=acc.id)OR vend.account=acc.id or gr.account=acc.id  OR p.depreciationprovisionglaccount = acc.id or p.depreciationglaccount=acc.id or p.sellassetglaccount=acc.id or p.writeoffassetaccount=acc.id or gr.centry = jed.id or gr.taxentry = jed.id or grd.tdspayableaccount=jed.account or if((comp.country='"+Constants.indian_country_id+"' or comp.country='"+Constants.USA_country_id+"'),false, if(grd.gstjedid is null or grd.gstjedid ='',tax.account=acc.id,grd.gstjedid=jed.id)))  " +advSearchFilterString +" group by if(gr.centry =jed.id or gr.taxentry = jed.id,je.id,jed.id)"
                + " UNION "
                + grexpdetailsselect + " \n"
                + "from account acc\n"
                + "inner join jedetail jed on jed.account=acc.id\n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.isreval=0\n"
                + "inner join goodsreceipt gr on gr.id=je.transactionid\n"
                + "inner join expenseggrdetails grd on grd.goodsreceipt =gr.id\n"
                + "inner join vendor vend on vend.id=gr.vendor\n"
                + "inner join currency curr on gr.currency =curr.currencyid\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join compaccpreferences cap on cap.id=gr.company\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "left join masteritem mi on mi.company=gr.company and gr.masteragent=mi.id\n"
//                + "left join masteritem mitds on mitds.company=gr.company and mitds.mastergroup='"+Constants.NatureofPaymentGroup+"'\n"
                + "left join tax tax on tax.id=grd.tax \n"
                + "left join tax gtax on gtax.id=gr.tax \n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "inner join account acc1 on grd.account=acc1.id\n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=gr.company and jedcd.jedetailid=jed.id \n" 
                + "left join accjecustomdata jecd on jecd.company=gr.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + "where gr.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ?  and jed.roundingdifferencedetail ='F' and (acc.id<>cap.roundingDifferenceAccount and je.typevalue<>4 and acc.id<>cap.discountGiven and acc.id<>cap.discountReceived) and (if(grd.purchasejedid is null or grd.purchasejedid='',true,grd.purchasejedid=jed.id) or gr.centry =jed.id or gr.taxentry = jed.id or grd.tdspayableaccount=jed.account or if((comp.country='"+Constants.indian_country_id+"' or comp.country='"+Constants.USA_country_id+"'),false, if(grd.gstjedid is null or grd.gstjedid ='',tax.account=acc.id,grd.gstjedid = jed.id)))  " +advSearchFilterString +" group by if(gr.centry =jed.id or gr.taxentry = jed.id,je.id,jed.id)"
                + " UNION "
                //Landed Invoices JE 
                + grexpdetailslandedselect + " \n"
                + "from account acc\n"
                + "inner join jedetail jed on jed.account=acc.id\n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.isreval=0\n"
                + "inner join goodsreceipt gr on gr.landedinvoiceje=je.id\n"
                + "inner join expenseggrdetails grd on grd.goodsreceipt =gr.id\n"
                + "inner join vendor vend on vend.id=gr.vendor\n"
                + "inner join currency curr on gr.currency =curr.currencyid\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join compaccpreferences cap on cap.id=gr.company\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "left join masteritem mi on mi.company=gr.company and gr.masteragent=mi.id\n"
                + "left join tax tax on tax.id=grd.tax \n"
                + "left join tax gtax on gtax.id=gr.tax \n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "inner join account acc1 on grd.account=acc1.id\n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=gr.company and jedcd.jedetailid=jed.id \n" 
                + "left join accjecustomdata jecd on jecd.company=gr.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + "where gr.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ?  and (acc.id<>cap.roundingDifferenceAccount and je.typevalue<>4 and acc.id<>cap.discountGiven and acc.id<>cap.discountReceived) and jed.roundingdifferencedetail ='F'" +advSearchFilterString +" group by if(gr.centry =jed.id or gr.taxentry = jed.id,je.id,jed.id)"
                + " UNION "
                + grroundingselect + " \n"
                + "from account acc                  \n"
                + "inner join jedetail jed on jed.account=acc.id\n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.isreval=0\n"
                + "inner join goodsreceipt gr on gr.id=je.transactionid\n"
                + "inner join vendor vend on vend.id=gr.vendor\n"
                + "inner join currency curr on gr.currency =curr.currencyid\n"
                + "inner join compaccpreferences cap on cap.id=gr.company\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "left join masteritem mi on mi.company=gr.company and gr.masteragent=mi.id\n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=gr.company and jedcd.jedetailid=jed.id \n" 
                + "left join accjecustomdata jecd on jecd.company=gr.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + "where gr.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ? and (cap.roundingDifferenceAccount = acc.id or je.typevalue=4) " +advSearchFilterString +" group by jed.id"
                + " UNION "
                + grtermselect + " \n"
                + "from account acc \n"
                + "inner join jedetail jed on jed.account=acc.id\n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.isreval=0\n"
                + "inner join goodsreceipt gr on gr.id=je.transactionid\n"
                + "inner join receipttermsmap rtm on rtm.goodsreceipt =gr.id\n"
                + "inner join invoicetermssales term on rtm.term=term.id\n"
                + "inner join vendor vend on vend.id=gr.vendor\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "left join masteritem mi on mi.company=gr.company and gr.masteragent=mi.id\n"
                + "left join compaccpreferences cap on cap.id=gr.company \n"
                + "inner join currency curr on gr.currency =curr.currencyid\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=gr.company and jedcd.jedetailid=jed.id \n" 
                + "left join accjecustomdata jecd on jecd.company=gr.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + "where gr.company= ?  and acc.id= ? and je.entrydate >= ? and (acc.id<>cap.roundingDifferenceAccount and je.typevalue<>4 and acc.id<>cap.discountGiven and acc.id<>cap.discountReceived) and je.entrydate <= ? and acc.id=term.account and jed.amount=ABS(rtm.termamount)" +advSearchFilterString +" group by jed.id"
                + " UNION "
                + grtermselect + " \n"
                + "from account acc \n"
                + "inner join jedetail jed on jed.account=acc.id\n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.isreval=0\n"
                + "inner join goodsreceipt gr on gr.id=je.transactionid\n"
                + "inner join grdetails grd on (grd.goodsreceipt = gr.id and grd.purchasejedid <> jed.id) \n"
                + "inner join vendor vend on vend.id=gr.vendor\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "inner join receiptdetailtermsmap rdtm on rdtm.goodsreceiptdetail=grd.id\n" 
                + "inner join linelevelterms llt on rdtm.term=llt.id \n"
                + "left join masteritem mi on mi.company=gr.company and gr.masteragent=mi.id\n"
                + "left join compaccpreferences cap on cap.id=gr.company \n"
                + "inner join currency curr on gr.currency =curr.currencyid\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=gr.company and jedcd.jedetailid=jed.id \n" 
                + "left join accjecustomdata jecd on jecd.company=gr.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + "where gr.company= ?  and acc.id= ? and je.entrydate >= ? and je.entrydate <= ?  and (acc.id<>cap.roundingDifferenceAccount and je.typevalue<>4 and acc.id<>cap.discountGiven and acc.id<>cap.discountReceived) and jed.roundingdifferencedetail ='F' and (acc.id=llt.account or acc.id=llt.payableaccount or acc.id=creditnotavailedaccount) and jed.amount=ABS(rdtm.termamount)" +advSearchFilterString +" group by jed.id"
                + " UNION "
                + grcapselect + " \n"
                + "from account acc \n"
                + "inner join jedetail jed on jed.account=acc.id\n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.isreval=0\n"
                + "inner join goodsreceipt gr on gr.id=je.transactionid\n"
                + "inner join compaccpreferences cap on cap.id=gr.company \n"
                + "inner join vendor vend on vend.id=gr.vendor\n"
                + "inner join currency curr on gr.currency =curr.currencyid\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "left join masteritem mi on mi.company=gr.company and gr.masteragent=mi.id\n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=gr.company and jedcd.jedetailid=jed.id \n" 
                + "left join accjecustomdata jecd on jecd.company=gr.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + "where gr.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ?  and (acc.id=cap.discountGiven OR acc.id=cap.discountReceived OR acc.id=cap.shippingCharges)  " +advSearchFilterString +" group by jed.id"
                + " UNION "
                + cnselect + " \n"
                + "from account acc \n"
                + "inner join jedetail jed on jed.account=acc.id\n"
                + "inner join journalentry je on je.id=jed.journalentry and je.entrydate >= ?  and je.entrydate <= ?  and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0 and je.pendingapproval=0  and je.partlyjeentrywithcndn='0'\n"
                + "inner join creditnote cn on cn.id=je.transactionid\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "left join customer cust on cust.id=cn.customer\n"
                + "left join vendor vend on vend.id=cn.vendor\n"
                + "inner join compaccpreferences cap on cap.id=cn.company\n"
                + "left join cntaxentry cnt on cnt.creditnote =cn.id and ( if(cnt.totaljedid is null or cnt.totaljedid = '' ,true, cnt.totaljedid = jed.id) or cnt.gstjedid = jed.id or cnt.taxjedid = jed.id or COALESCE(cust.account,vend.account)=acc.id or cap.foreignexchange=acc.id)\n"
                + "left join cndetailsgst cndgst on cndgst.creditNote = cn.id and ( if(cndgst.jedid is null or cndgst.jedid ='' , true, cndgst.jedid = jed.id) or cndgst.gstjedid = jed.id or COALESCE(cust.account,vend.account)=acc.id ) \n"
                + "inner join currency curr on cn.currency =curr.currencyid\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "left join masteritem mi on cn.company=mi.company and COALESCE(cn.salesperson,cn.masteragent)=mi.id\n"
                + "left join tax tax on tax.id=cnt.tax \n"
                + "left join tax gtax on gtax.id=cn.tax \n"
                + "left join tax cndtax on cndtax.id=cndgst.tax \n"
                + "left join account acc1 on cnt.account=acc1.id\n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=cn.company and jedcd.recdetailid=cnt.id \n" 
                + "left join accjecustomdata jecd on jecd.company=cn.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + "where cn.company= ?  and acc.id= ?  " +advSearchFilterString +" group by if(cn.centry=jed.id,je.id,jed.id)"
                + " UNION "
                + dnselect + " \n"
                + "from account acc \n"
                + "inner join jedetail jed on jed.account=acc.id\n"
                + "inner join journalentry je on je.id=jed.journalentry and je.entrydate >= ?  and je.entrydate <= ?  and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0 and je.pendingapproval=0 and je.partlyjeentrywithcndn='0' \n"
                + "inner join debitnote dn on dn.id=je.transactionid\n"
                + "left join customer cust on cust.id=dn.customer\n"
                + "left join vendor vend on vend.id=dn.vendor\n"
                + "inner join compaccpreferences cap on cap.id=dn.company\n"
                + "left join dntaxentry dnt on dnt.debitnote =dn.id and ( if(dnt.totaljedid is null or dnt.totaljedid ='' , true, dnt.totaljedid = jed.id) or dnt.gstjedid = jed.id or dnt.taxjedid = jed.id or COALESCE(cust.account,vend.account)=acc.id or cap.foreignexchange=acc.id)\n"
                + "left join dndetailsgst dndgst on dndgst.debitnote = dn.id and ( if(dndgst.jedid is null or dndgst.jedid ='' , true, dndgst.jedid = jed.id) or dndgst.gstjedid = jed.id or COALESCE(cust.account,vend.account)=acc.id ) \n"
                + "inner join currency curr on dn.currency =curr.currencyid\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "left join tax tax on tax.id=dnt.tax \n"
                + "left join tax gtax on gtax.id=dn.tax \n"
                + "left join tax dndtax on dndtax.id=dndgst.tax \n"
                + "left join account acc1 on dnt.account=acc1.id\n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=dn.company and jedcd.recdetailid=dnt.id \n" 
                + "left join accjecustomdata jecd on jecd.company=dn.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + "where dn.company= ?  and acc.id= ?  " +advSearchFilterString +" group by if(dn.centry=jed.id,je.id,jed.id)"
                + " UNION "
                + receiptselect + " "
                + " from account acc inner join jedetail jed on acc.id = jed.account and jed.isseparated = 'F' "
                + "inner join journalentry je on jed.journalentry = je.id and je.entrydate >= ?  and je.entrydate <= ?  and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 \n"
                + "inner join receipt rt on je.transactionid =rt.id \n"
                + "left join receipt rt1 on rt1.id=rt.id and (rt1.journalEntryForBankCharges=je.id or rt1.journalentryforbankinterest=je.id) \n"
                + "left join receiptdetails rd on rd.receipt=rt.id and if(rd.totaljedid is null or rd.totaljedid ='', true, (rd.totaljedid  = jed.id or rt.deposittojedetail = jed.id )) \n"
                + "left join receiptadvancedetail rad on rad.receipt=rt.id and if(rad.totaljedid is null or rad.totaljedid ='', true, rad.totaljedid = jed.id) \n"
                + "left join debitnotepayment dnp on dnp.receiptid=rt.id and if(dnp.totaljedid is null or dnp.totaljedid ='', true, dnp.totaljedid = jed.id) \n"
                + "left join debitnote dn on dn.id=dnp.dnid \n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "left join paydetail pd on rt.paydetail=pd.id \n"
                + "left join cheque cq on pd.cheque=cq.id \n"
                + "left join paymentmethod pm on pm.id=pd.paymentmethod \n"
                + "left join customer cust on cust.id=rt.customer \n"
                + "inner join compaccpreferences cap on cap.id=rt.company\n"
                + "left join vendor vend on vend.id=rt.vendor \n" 
                + "left join receiptdetailotherwise rdo on rdo.receipt=rt.id and ( if(rdo.totaljedid is null or rdo.totaljedid ='', true, rdo.totaljedid = jed.id) or rdo.gstjedid = jed.id or pm.account=acc.id or COALESCE(cust.account,vend.account) = acc.id or cap.foreignexchange=acc.id)\n"
                + "left join tax tax on tax.id=rdo.tax \n"                
                + "left join invoice inv on inv.id=rd.invoice \n"
                + "left join goodsreceipt gr on gr.id=rd.goodsreceipt \n"
                + "left join currency curr on rt.currency =curr.currencyid \n"
                + "left join currency acccurr on acc.currency =acccurr.currencyid \n"
                + "left join costcenter cc on cc.id=je.costcenter \n"
                + "left join masteritem mi on rt.receivedfrom=mi.id \n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=rt.company and jedcd.recdetailid= COALESCE(rd.id,rad.id,rdo.id,dnp.id) \n" 
                + "left join accjecustomdata jecd on jecd.company=rt.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
//                + "where rt.company= ?  and acc.id= ?  " +advSearchFilterString +" group by if(cust.account=acc.id or vend.account=acc.id,je.id,jed.id)\n"
                + "where rt.company= ?  and acc.id= ?  " +advSearchFilterString +" group by if(rt.deposittojedetail=jed.id,je.id,jed.id)\n"
//                + "where rt.company= ?  and acc.id= ?  " +advSearchFilterString +" group by if(rdo.receipt=rt.id or dnp.receiptid=rt.id or rad.receipt=rt.id or rd.totaljedid =jed.id or rad.totaljedid =jed.id or dnp.totaljedid =jed.id or rdo.totaljedid =jed.id or rd.receipt=rt.id or cap.foreignexchange = acc.id or cap.cashAccount = acc.id  or je.isdishonouredcheque='T',jed.id,je.id)\n"
                + " UNION "
                + receiptwoselect + " "
                + " from account acc inner join jedetail jed on acc.id = jed.account and jed.isseparated = 'F' "
                + "inner join journalentry je on jed.journalentry = je.id and je.entrydate >= ?  and je.entrydate <= ?  and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 \n"
                + "inner join receiptwriteoff rwo on je.transactionid =rwo.id \n"
                + "inner join receipt rt on rwo.receipt =rt.id \n"
                + "left join receipt rt1 on rt1.id=rt.id and (rt1.journalEntryForBankCharges=je.id or rt1.journalentryforbankinterest=je.id) \n"
                + "left join receiptdetails rd on rd.receipt=rt.id and if(rd.totaljedid is null or rd.totaljedid ='', true, rd.totaljedid  = jed.id )\n"
                + "left join receiptadvancedetail rad on rad.receipt=rt.id and if(rad.totaljedid is null or rad.totaljedid ='', true, rad.totaljedid = jed.id) \n"
                + "left join debitnotepayment dnp on dnp.receiptid=rt.id and if(dnp.totaljedid is null or dnp.totaljedid ='', true, dnp.totaljedid = jed.id) \n"
                + "left join debitnote dn on dn.id=dnp.dnid \n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "left join paydetail pd on rt.paydetail=pd.id \n"
                + "left join cheque cq on pd.cheque=cq.id \n"
                + "left join paymentmethod pm on pm.id=pd.paymentmethod \n"
                + "left join customer cust on cust.id=rt.customer \n"
                + "inner join compaccpreferences cap on cap.id=rt.company\n"
                + "left join vendor vend on vend.id=rt.vendor \n" 
                + "left join receiptdetailotherwise rdo on rdo.receipt=rt.id and ( if(rdo.totaljedid is null or rdo.totaljedid ='', true, rdo.totaljedid = jed.id) or rdo.gstjedid = jed.id or pm.account=acc.id or COALESCE(cust.account,vend.account) = acc.id or cap.foreignexchange=acc.id)\n"
                + "left join tax tax on tax.id=rdo.tax \n"                
                + "left join invoice inv on inv.id=rd.invoice \n"
                + "left join goodsreceipt gr on gr.id=rd.goodsreceipt \n"
                + "left join currency curr on rt.currency =curr.currencyid \n"
                + "left join currency acccurr on acc.currency =acccurr.currencyid \n"
                + "left join costcenter cc on cc.id=je.costcenter \n"
                + "left join masteritem mi on rt.receivedfrom=mi.id \n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=rt.company and jedcd.recdetailid= COALESCE(rd.id,rad.id,rdo.id,dnp.id) \n" 
                + "left join accjecustomdata jecd on jecd.company=rt.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + "where rt.company= ?  and acc.id= ?  " +advSearchFilterString +" group by if(rdo.receipt=rt.id or dnp.receiptid=rt.id or rad.receipt=rt.id or rd.totaljedid =jed.id or rad.totaljedid =jed.id or dnp.totaljedid =jed.id or rdo.totaljedid =jed.id or rd.receipt=rt.id or cap.foreignexchange = acc.id or cap.cashAccount = acc.id  or je.isdishonouredcheque='T',jed.id,je.id)\n"
                + " UNION "
                + othersselect + " \n"
                + "from account acc \n"
                + "inner join jedetail jed on jed.account=acc.id\n"
                + "inner join journalentry je on je.id=jed.journalentry and ( je.transactionid is null or je.transactionid='' ) and je.entrydate >= ?  and je.entrydate <= ?  and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0 and je.pendingapproval=0\n"
                + "inner join currency curr on je.currency =curr.currencyid\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join compaccpreferences cap on cap.id=je.company\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "left join pbdetails pbd on jed.id=pbd.jedetail\n"
                + "left join productbuild pb on (jed.id=pb.totaljed or pbd.build=pb.id)\n"
                + "left join customer cust on cust.id=jed.customervendorid\n"
                + "left join vendor vend on vend.id=jed.customervendorid\n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=je.company and jedcd.jedetailid=jed.id \n" 
                + "left join accjecustomdata jecd on jecd.company=je.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + "where je.company= ?  and acc.id= ?  " +advSearchFilterString +" group by jed.id "
                + " UNION "
                + paymentselect + " \n"
                + "from account acc\n"
                + "inner join jedetail jed on acc.id = jed.account and jed.isseparated = 'F'\n"
                + "inner join journalentry je on jed.journalentry = je.id and je.entrydate >= ?  and je.entrydate <= ?  and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0\n"
                + "inner join payment pt on je.transactionid =pt.id \n"
                + "left join payment pt1 on pt1.id=pt.id and (pt1.journalentryforbankcharges=je.id or pt1.journalentryforbankinterest=je.id)\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"                
                + "inner join compaccpreferences cap on cap.id=pt.company\n"
                + "left join paymentdetail pmd on pmd.payment=pt.id and if(pmd.totaljedid is null or pmd.totaljedid ='', true, (pmd.totaljedid = jed.id or pt.deposittojedetail = jed.id))  \n"
                + "left join advancedetail pad on pad.payment=pt.id and if(pad.totaljedid is null or pad.totaljedid ='', true, pad.totaljedid = jed.id ) \n"
                + "left join creditnotpayment cnp on cnp.paymentid=pt.id and if(cnp.totaljedid is null or cnp.totaljedid ='', true, cnp.totaljedid = jed.id) \n"
                + "left join creditnote cn on cn.id=cnp.cnid \n"
                + "left join vendor vend on vend.id=pt.vendor\n"
                + "left join paydetail pd on pt.paydetail=pd.id\n"
                + "left join cheque cq on pd.cheque=cq.id\n"
                + "left join paymentmethod pm on pm.id=pd.paymentmethod\n"
                + "left join customer cust on cust.id=pt.customer\n"
                + "left join paymentdetailotherwise pdo on pdo.payment=pt.id and ( if(pdo.totaljedid is null or pdo.totaljedid ='', true, pdo.totaljedid = jed.id) or pdo.gstjedid = jed.id or pm.account=acc.id or COALESCE(cust.account,vend.account) = acc.id or cap.foreignexchange = acc.id)\n"
                + "left join tax tax on tax.id=pdo.tax \n"
                + "left join invoice inv on inv.id=pmd.invoice\n"
                + "left join goodsreceipt gr on gr.id=pmd.goodsreceipt\n"
                + "left join currency curr on pt.currency =curr.currencyid\n"
                + "left join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "left join costcenter cc on cc.id=je.costcenter\n"
                + "left join masteritem mi on pt.paidto=mi.id \n"
                + "left join accjedetailcustomdata jedcd on jedcd.company=pt.company and jedcd.recdetailid=COALESCE(pmd.id,pad.id,pdo.id,cnp.id) \n" 
                + "left join accjecustomdata jecd on jecd.company=pt.company and jecd.journalentryId=je.id \n" 
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"
                + "where pt.company= ?  and acc.id= ?  " +advSearchFilterString +" \n"
//                + "group by if(cust.account=acc.id or vend.account=acc.id,je.id,jed.id)"                
                + "group by if(pt.deposittojedetail=jed.id,je.id,jed.id)"                
//                + "group by if(cnp.paymentid=pt.id or pad.payment=pt.id or pdo.payment=pt.id or pmd.payment=pt.id or pmd.totaljedid =jed.id or pad.totaljedid =jed.id or cnp.totaljedid =jed.id or pdo.totaljedid =jed.id or pmd.payment=pt.id or cap.foreignexchange = acc.id or cap.cashAccount = acc.id  or je.isdishonouredcheque='T',jed.id,je.id)"                
                + " UNION "
                + saselect + " \n"
                + "from account acc \n"
                + " inner join jedetail jed on jed.account=acc.id  \n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.transactionModuleid="+Constants.Inventory_Stock_Adjustment_ModuleId+"\n"
                + "inner join in_stockadjustment sa on sa.id=je.transactionid\n"
                + "inner join compaccpreferences cap on cap.id=je.company\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "inner join currency curr on comp.currency=curr.currencyid \n"
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"                
                + "where sa.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ? "+advSearchFilterString +" group by jed.id"
                + " UNION "
                + srselect + " \n"
                + "from account acc \n"
                + " inner join jedetail jed on jed.account=acc.id  \n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.transactionModuleid="+Constants.Acc_Sales_Return_ModuleId+"\n"
                + "inner join salesreturn sr on sr.id=je.transactionid\n"
                + "inner join compaccpreferences cap on cap.id=je.company\n"
                + "inner join customer cust on cust.id=sr.customer \n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "inner join currency curr on sr.currency=curr.currencyid \n"
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"                
                + "where sr.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ?  "+advSearchFilterString +" group by jed.id"
                + " UNION "
                + prselect + " \n"
                + "from account acc \n"
                + " inner join jedetail jed on jed.account=acc.id  \n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.transactionModuleid="+Constants.Acc_Purchase_Return_ModuleId+"\n"
                + "inner join purchasereturn pr on pr.id=je.transactionid\n"
                + "inner join vendor vend on vend.id=pr.vendor \n"
                + "inner join compaccpreferences cap on cap.id=je.company\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "inner join currency curr on pr.currency=curr.currencyid \n"
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"                
                + "where pr.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ?  "+advSearchFilterString +" group by jed.id"
                + " UNION "
                + doselect + " \n"
                + "from account acc \n"
                + " inner join jedetail jed on jed.account=acc.id  \n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.transactionModuleid="+Constants.Acc_Delivery_Order_ModuleId+"\n"
                + "inner join deliveryorder do on do.id=je.transactionid\n"
                + "inner join customer cust on cust.id=do.customer \n"
                + "inner join compaccpreferences cap on cap.id=je.company\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "inner join currency curr on do.currency=curr.currencyid \n"
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"                
                + "where do.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ?  "+advSearchFilterString +" group by jed.id"
                + " UNION "
                + groselect + " \n"
                + "from account acc \n"
                + " inner join jedetail jed on jed.account=acc.id  \n"
                + "inner join journalentry je on je.id=jed.journalentry and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0 and je.transactionModuleid="+Constants.Acc_Goods_Receipt_ModuleId+"\n"
                + "inner join grorder gro on gro.id=je.transactionid\n"
                + "inner join vendor vend on vend.id=gro.vendor \n"
                + "inner join compaccpreferences cap on cap.id=je.company\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join company comp on je.company = comp.companyid\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid\n"
                + "inner join currency curr on gro.currency=curr.currencyid \n"
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"                
                + "where gro.company= ?  and acc.id= ? and je.entrydate >= ?  and je.entrydate <= ?  "+advSearchFilterString +" group by jed.id"
                + " UNION "
                + jeinvrevalselect + " \n"
                + "from account acc\n"
                + "inner join jedetail jed on acc.id = jed.account and jed.isseparated = 'F'\n"
                + "inner join journalentry je on jed.journalentry = je.id and je.entrydate >= ?  and je.entrydate <= ? and je.isreval=1 and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0\n"
                + "inner join invoice inv on inv.id=je.transactionid \n"
                + "inner join customer cust on cust.id=inv.customer \n"
                + "inner join compaccpreferences cap on cap.id=je.company\n"
                + "inner join currency curr on curr.currencyid=inv.currency\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join company comp on je.company = comp.companyid\n" 
                + "left join masteritem mi on mi.company=inv.company and inv.mastersalesperson=mi.id\n"
                + "inner join currency compcurr on comp.currency=compcurr.currencyid \n"
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"                
                + "where je.company= ?  and acc.id= ?  "+advSearchFilterString +" \n"
                +" group by je.id"
                + " UNION "
                + jegrrevalselect + " \n"
                + "from account acc\n"
                + "inner join jedetail jed on acc.id = jed.account and jed.isseparated = 'F'\n"
                + "inner join journalentry je on jed.journalentry = je.id and je.entrydate >= ?  and je.entrydate <= ? and je.isreval=1 and je.approvestatuslevel = 11 and je.istemplate != 2 and je.deleteflag='F' and je.isdraft=0   and je.pendingapproval=0\n"
                + "inner join goodsreceipt gr on gr.id=je.transactionid \n"
                + "inner join vendor vend on vend.id=gr.vendor\n"
                + "inner join currency curr on curr.currencyid=gr.currency\n"
                + "inner join compaccpreferences cap on cap.id=je.company\n"
                + "inner join currency acccurr on acc.currency =acccurr.currencyid\n"
                + "inner join company comp on je.company = comp.companyid\n" 
                + "inner join currency compcurr on comp.currency=compcurr.currencyid \n"
                + "left join masteritem mi on mi.company=gr.company and gr.mastersalesperson=mi.id\n"
                + advSearchSqljoin + advSearchCustomtablejoin + "\n"                
                + "where je.company= ?  and acc.id= ?  "+advSearchFilterString +" \n"
                +" group by je.id"

                               + ") a order by a.entrydate, a.entryno";
        List params = new ArrayList();
        
        
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        //Invoice receipt details term map
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        //Invoice receipt details term map
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        params.add(startdate);
        params.add(enddate);
        params.add(companyid);
        params.add(accountid);
        params.addAll(advSearchParams);
        
        params.add(startdate);
        params.add(enddate);
        params.add(companyid);
        params.add(accountid);
        params.addAll(advSearchParams);
        
        params.add(startdate);
        params.add(enddate);
        params.add(companyid);
        params.add(accountid);
        params.addAll(advSearchParams);
        
        params.add(startdate);
        params.add(enddate);
        params.add(companyid);
        params.add(accountid);
        params.addAll(advSearchParams);
        
        //Receipt write off
        params.add(startdate);
        params.add(enddate);
        params.add(companyid);
        params.add(accountid);
        params.addAll(advSearchParams);
        
        params.add(startdate);
        params.add(enddate);
        params.add(companyid);
        params.add(accountid);
        params.addAll(advSearchParams);
        
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        params.add(companyid);
        params.add(accountid);
        params.add(startdate);
        params.add(enddate);
        params.addAll(advSearchParams);
        
        params.add(startdate);
        params.add(enddate);
        params.add(companyid);
        params.add(accountid);
        params.addAll(advSearchParams);
        
        params.add(startdate);
        params.add(enddate);
        params.add(companyid);
        params.add(accountid);
        params.addAll(advSearchParams);
//        System.out.println(query);
        List list = executeSQLQuery(query, params.toArray());
        int totalCount = list.size();
        return new KwlReturnObject(true, "", "", list, totalCount);
    }

    private Map<String, String> getAdvanceSearchForCustomQuery(String filterConjuctionCriteria, String searchjson, ArrayList params){
        Map<String, String> advSearchAttributes = new HashMap<String, String>();
        String condition = " ";
        String mySearchFilterString = "",customDataJoin = "";
        if (!StringUtil.isNullOrEmpty(searchjson)) {
            boolean isLineDimPresent = false;
            boolean isGlobalDimPresent = false;
            try {
                JSONObject jobj = new JSONObject(searchjson);
                JSONArray rootArr = jobj.getJSONArray("root");
                JSONObject searchjobj = null;
                for (int i = 0; i < rootArr.length(); i++) {
                    searchjobj = rootArr.getJSONObject(i);
                    if (searchjobj.optBoolean("iscustomcolumndata")) {
                        isLineDimPresent = true;
                    } else if (!searchjobj.optBoolean("iscustomcolumndata")) {
                        isGlobalDimPresent = true;
                    }
                    if (isLineDimPresent && isGlobalDimPresent) {
                        break;
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            if ((isGlobalDimPresent && !isLineDimPresent) || (isLineDimPresent && isGlobalDimPresent && filterConjuctionCriteria.trim().equalsIgnoreCase(Constants.or.trim()))) {
                condition += " and jedetail.isseparated = 'F' ";
            }
            HashMap<String, Object> request = new HashMap<String, Object>();
            request.put(Constants.Searchjson, searchjson);
            request.put(Constants.appendCase, "and");
            request.put(Constants.moduleid, "100");
            request.put("filterConjuctionCriteria", filterConjuctionCriteria);
            try {
                mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                if (mySearchFilterString.contains(Constants.AccJECustomData)) {
                    customDataJoin += " left join accjecustomdata jecd1 on je.id = jecd1.journalentryid";
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "jecd1");
                }
                if (mySearchFilterString.contains(Constants.AccJEDetailCustomData)) {
                    customDataJoin += " left join accjedetailcustomdata jedcd1 on jedetail.id = jedcd1.jedetailid";
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jedcd1");
                }
                if (mySearchFilterString.contains(Constants.AccJEDetailsProductCustomData)) {
                    customDataJoin += " left join accjedetailproductcustomdata jepcd1 on jedetail.id = jepcd1.jedetailid";
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jepcd1");
                }
                StringUtil.insertParamAdvanceSearchString1(params, searchjson);
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(accJournalEntryImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        }else{
            condition += " and jedetail.isseparated = 'F' ";
        }
        advSearchAttributes.put("condition", condition);
        advSearchAttributes.put("customDataJoin", customDataJoin);
        advSearchAttributes.put("mySearchFilterString", mySearchFilterString);
        return advSearchAttributes;
    }
    
    public List getSOAInfo(JSONObject requestObj) throws ServiceException, JSONException {
        boolean isOutstanding = !requestObj.optBoolean("invoiceAmountDueFilter");
        Boolean showDishonouredPayments = requestObj.optBoolean("isFromAR", false);  //if the call is not from aged receivable the we have to display dishonoured payment and reverse JE of dishonoured payments.
    
        
//        boolean isOutstanding = true; // Always True in case of age receinvable. 
        
        
        String invSelectStatement="" ;
        String invMainSelectStatement="";
        String invCNDSelectStatement="";
        String invRDSelectStatement="";
        String invLDRSelectStatement="";
        String invWOSelectStatement="";
        
        String invOpnSelectStatement="";
        String invOpnMainSelectStatement=""; 
        String invOpnCNDSelectStatement="";
        String invOpnRDSelectStatement="";
        String invOpnLDRSelectStatement="";
        String invOpnWOSelectStatement=""; 
        
        
        //dn Select Statement
        String dnSelectStatement="";
        String dnMainSelectStatement="";
        String dnLDRSelectStatement="";
        String dnDNPSelectStatement="";
        String dnDISSelectStatement="";
        
        //dn opn Select Statement
        String dnOpnSelectStatement="";
        String dnOpnMainSelectStatement="";
        String dnOpnLDRSelectStatement=""; 
        String dnOpnDNPSelectStatement="";
        String dnOpnDISSelectStatement="";

        //cn Select Statement
        String cnSelectStatement="";
        String cnMainSelectStatement="";
        String cnLDRSelectStatement=""; 
        String cnCNPSelectStatement=""; 
        String cnDISSelectStatement=""; 
        
        //cn FRX select Statement
        
        String cnFRXSelectStatement="";
    
        //cn opn select Statement
        String cnOpnSelectStatement=""; 
        String cnOpnMainSelectStatement="";
        String cnOpnLDRSelectStatement=""; 
        String cnOpnCNPSelectStatement=""; 
        String cnOpnDISSelectStatement=""; 
        
        
        //receipt Select Statement
        String receiptAllSelectStatement="";
        String receiptOutstandingSelectStatement="";
        String receiptSelectStatement="";
        String receiptMainSelectStatement="";
        String receiptLPSelectStatement="";
        String receiptLDRSelectStatement="";
        String receiptRDSelectStatement="";
        String receiptRDPSelectStatement="";
        String receiptADVSelectStatement="";
        String receiptRWOSelectStatement="";
        String receiptdishonouredselect="";
        
        
        //receipt opn Select Statement
        String receiptOpnSelectStatement="";
        String receiptOpnMainSelectStatement="";
        String receiptOpnLPSelectStatement=""; 
        String receiptOpnLDRSelectStatement="";
        String receiptOpnRDSelectStatement=""; 
        String receiptOpnRDPSelectStatement="";
        String receiptOpnADVSelectStatement="";
        String receiptOpnRWOSelectStatement="";
        

        String paymentSelectStatement=""; 
        String paymentMainSelectStatement="";
        String paymentCNPSelectStatement="";
        String paymentLDPSelectStatement=""; 
        String paymentLDAPSelectStatement=""; 
        String paymentLDPCNSelectStatement="";
        String paymentPOSelectStatement="";
        String paymentSRSelectStatement="";
        String paymentdishonouredselect="";
        
         String customerquery = "";
        
         
         //Invoice Select Statement
            invSelectStatement = requestObj.getString("invselect");
            invMainSelectStatement = requestObj.getString("invmainselect");
            invCNDSelectStatement = requestObj.getString("invcndselect");
            invRDSelectStatement = requestObj.getString("invrdselect");
            invLDRSelectStatement = requestObj.getString("invldrselect");
            invWOSelectStatement = requestObj.getString("invwoselect");

            invOpnSelectStatement = requestObj.getString("invopnselect");
            invOpnMainSelectStatement = requestObj.getString("invopnmainselect");
            invOpnCNDSelectStatement = requestObj.getString("invopnCNDselect");
            invOpnRDSelectStatement = requestObj.getString("invopnRDselect");
            invOpnLDRSelectStatement = requestObj.getString("invopnLDRselect");
            invOpnWOSelectStatement = requestObj.getString("invopnWOselect");

            //dn Select Statement
            dnSelectStatement = requestObj.getString("dnselect");
            dnMainSelectStatement = requestObj.getString("dnmainselect");
            dnLDRSelectStatement = requestObj.getString("dnLDRselect");
            dnDNPSelectStatement = requestObj.getString("dnDNPselect");
            dnDISSelectStatement = requestObj.getString("dnDISselect");

            //dn opn Select Statement
            dnOpnSelectStatement = requestObj.getString("dnopnselect");
            dnOpnMainSelectStatement = requestObj.getString("dnopnmainselect");
            dnOpnLDRSelectStatement = requestObj.getString("dnopnLDRselect");
            dnOpnDNPSelectStatement = requestObj.getString("dnopnDNPselect");
            dnOpnDISSelectStatement = requestObj.getString("dnopnDISselect");

            //cn Select Statement
            cnSelectStatement = requestObj.getString("cnselect");
            cnMainSelectStatement = requestObj.getString("cnmainselect");
            cnLDRSelectStatement = requestObj.getString("cnLDRselect");
            cnCNPSelectStatement = requestObj.getString("cnCNPselect");
            cnDISSelectStatement = requestObj.getString("cnDISselect");
//            CN FRX 
//            cnFRXSelectStatement = requestObj.getString("cnFRXselect");
            
             //cn opn select Statement
            cnOpnSelectStatement = requestObj.getString("cnopnselect");
            cnOpnMainSelectStatement = requestObj.getString("cnopnmainselect");
            cnOpnLDRSelectStatement = requestObj.getString("cnopnLDRselect");
            cnOpnCNPSelectStatement = requestObj.getString("cnopnCNPselect");
            cnOpnDISSelectStatement = requestObj.getString("cnopnDISselect");

         
            paymentSelectStatement = requestObj.getString("paymentselect");
            paymentMainSelectStatement = requestObj.getString("paymentmainselect");
            paymentCNPSelectStatement = requestObj.getString("paymentCNPselect");
            paymentLDPSelectStatement = requestObj.getString("paymentLDPselect");
            paymentLDAPSelectStatement = requestObj.getString("paymentLDAPselect");
            paymentLDPCNSelectStatement = requestObj.getString("paymentLDPCNselect");
            paymentPOSelectStatement = requestObj.getString("paymentPOselect");
            paymentSRSelectStatement = requestObj.getString("paymentSRselect");
            
         
             //receipt Select Statement
            receiptSelectStatement = requestObj.optString("receiptselect");
            receiptMainSelectStatement = requestObj.getString("receiptmainselect");
            receiptLPSelectStatement = requestObj.getString("receiptLPselect");
            receiptLDRSelectStatement = requestObj.getString("receiptLDRselect");
            receiptRDSelectStatement = requestObj.getString("receiptRDselect");
            receiptRDPSelectStatement = requestObj.getString("receiptRDPselect");
            receiptADVSelectStatement = requestObj.getString("receiptADVselect");
            receiptRWOSelectStatement = requestObj.getString("receiptRWOselect");
            if (!showDishonouredPayments) {                //if the call is not from aged receivable the we have to display dishonoured payment and reverse JE of dishonoured payments.ERM-744
                receiptdishonouredselect = requestObj.optString("receiptdishonouredselect","");
                paymentdishonouredselect = requestObj.optString("paymentdishonouredselect","");
            }

            //receipt opn Select Statement
            receiptOpnSelectStatement = requestObj.getString("receiptopnselect");
            receiptOpnMainSelectStatement = requestObj.getString("receiptopnmainselect");
            receiptOpnLPSelectStatement = requestObj.getString("receiptopnLPselect");
            receiptOpnLDRSelectStatement = requestObj.getString("receiptopnLDRselect");
            receiptOpnRDSelectStatement = requestObj.getString("receiptopnRDselect");
            receiptOpnRDPSelectStatement = requestObj.getString("receiptopnRDPselect");
            receiptOpnADVSelectStatement = requestObj.getString("receiptopnADVselect");
            receiptOpnRWOSelectStatement = requestObj.getString("receiptopnRWOselect");

         
         
        String conditionINVCurr = "";
        String conditionCNCurr = "";
        String conditionDNCurr = "";
        String conditionPYCurr = "";
        String conditionRCCurr = "";
        String endDate ="";
        String asofDate ="";
        String companyid ="";
        String customerids ="";
        
        String INVCondition="";
        String InvJoin ="";
        String CNJoin ="";
        String DNJoin ="";
        
        String deuDateCondition="";
        
        boolean isAgedDetailsReport = false; 
        boolean isSalesPersonAgedReport = false;
            
        boolean includeExcludeChildCmb = false;
        
        Boolean isFromAR = requestObj.optBoolean("isFromAR", false);
        
        int datefilter =1;
        
        if (isFromAR) {
        // Ar Select Statement

            isOutstanding = true;

            
            cnFRXSelectStatement= cnSelectStatement;
            
            endDate = requestObj.getString("enddate");
            asofDate = requestObj.getString("asofdate");
            companyid = requestObj.getString("companyid");
             customerids = requestObj.getString("custVendorID");
            
             
            isSalesPersonAgedReport = requestObj.optBoolean("isSalesPersonAgedReport",false);
            String salesPerosnJoinType = " left join";
            if (isSalesPersonAgedReport) {
                salesPerosnJoinType = " inner join";
            }
            
            //Extra Join And Condition For AR
            InvJoin = " inner join compaccpreferences on compaccpreferences.id=company.companyid\n"
                    +salesPerosnJoinType + " masteritem on masteritem.id = invoice.mastersalesperson  \n";
            CNJoin = salesPerosnJoinType + " masteritem on masteritem.id = cn.salesperson \n";
            DNJoin = salesPerosnJoinType + " masteritem on masteritem.id = dn.salesperson \n";
            
            
            
            
             isAgedDetailsReport = requestObj.optBoolean("isAgedDetailsReport",false); 
            
             includeExcludeChildCmb= requestObj.optBoolean("includeExcludeChildCmb",false);
            
            
             if (!StringUtil.isNullOrEmpty(customerids) && !customerids.equals("All")) {
                String[] customers = customerids.split(",");
                StringBuilder custValues = new StringBuilder();
                for (String customer : customers) {
                    custValues.append("'").append(customer).append("',");
                }
                String custStr = custValues.substring(0, custValues.lastIndexOf(","));
                if (isSalesPersonAgedReport) {
                    customerquery += " and masteritem.id IN (" + custStr + ")";
                } else if (includeExcludeChildCmb) {
                    customerquery += " and (customer.id IN (" + custStr + ") or customer.parent IN (" + custStr + "))";
                } else {
                    customerquery += " and customer.id IN (" + custStr + ")";
                }
            }else if(!includeExcludeChildCmb){
                customerquery += " and customer.parent is  null";
            }
            
            // Group Combo Filter
//            if (requestObj.has("groupcombo") && requestObj.get("groupcombo") != null && requestObj.has(Constants.globalCurrencyKey) && requestObj.get(Constants.globalCurrencyKey) != null) {
            if ((!StringUtil.isNullOrEmpty(requestObj.optString("groupcombo",""))) && (!StringUtil.isNullOrEmpty(requestObj.optString(Constants.globalCurrencyKey,"")))) {
                int groupcombo = requestObj.getInt("groupcombo");
                if (groupcombo == Constants.AgedPayableBaseCurrency) {
                    conditionINVCurr += " and invoice.currency = " + Integer.parseInt((String) requestObj.get(Constants.globalCurrencyKey)) +" ";
                    conditionCNCurr += " and cn.currency = " + Integer.parseInt((String) requestObj.get(Constants.globalCurrencyKey)) +" ";
                    conditionDNCurr += " and dn.currency = " + Integer.parseInt((String) requestObj.get(Constants.globalCurrencyKey)) +" ";
                    conditionPYCurr += " and p.currency = " + Integer.parseInt((String) requestObj.get(Constants.globalCurrencyKey)) +" ";
                    conditionRCCurr += " and r.currency = " + Integer.parseInt((String) requestObj.get(Constants.globalCurrencyKey)) +" ";
                } else if (groupcombo == Constants.AgedPayableOtherthanBaseCurrency) {
                    conditionINVCurr += " and invoice.currency != " + Integer.parseInt((String) requestObj.get(Constants.globalCurrencyKey)) +" ";
                    conditionCNCurr += " and cn.currency != " + Integer.parseInt((String) requestObj.get(Constants.globalCurrencyKey)) +" ";
                    conditionDNCurr += " and dn.currency != " + Integer.parseInt((String) requestObj.get(Constants.globalCurrencyKey)) +" ";
                    conditionPYCurr += " and p.currency != " + Integer.parseInt((String) requestObj.get(Constants.globalCurrencyKey)) +" ";
                    conditionRCCurr += " and r.currency != " + Integer.parseInt((String) requestObj.get(Constants.globalCurrencyKey)) +" ";
                }
            } 
             
            
            datefilter = requestObj.optInt("datefilter",0);
            
            if(datefilter == 0){
                deuDateCondition = " where a.duedate<=? \n";
            } else {
                deuDateCondition = " ";
            }
            
            
            INVCondition = " and (invoice.amountduedate > ? or invoice.amountduedate is null) \n";
            
        }else{

            //SOA select Statement
            
            // Select statement for CN FRX 
            cnFRXSelectStatement = requestObj.getString("cnFRXselect");
            
            //receipt Select Statement
            receiptAllSelectStatement = requestObj.getString("receiptAllSelectStatement");
//            receiptOutstandingSelectStatement = requestObj.getString("receiptOutstandingSelectStatement");
            receiptSelectStatement = "";

            endDate = requestObj.getString("enddateStr");
            asofDate = requestObj.getString("asofdateStr");
            companyid = requestObj.getString("companyid");
            customerids = requestObj.optString("customerIds");

            receiptSelectStatement = receiptAllSelectStatement; // amount is fetch from jed table 

            
            //Query For Selected Customer
            if (!StringUtil.isNullOrEmpty(customerids) && !customerids.equals("All")) {
                String customer[] = customerids.split(",");
                StringBuilder customerQueryBuilder = new StringBuilder();
                for (int i = 0; i < customer.length; i++) {
                    if (!StringUtil.isNullOrEmpty(customer[i])) {
                        customerQueryBuilder.append("'" + customer[i] + "'").append(",");
                    }
                }
                if (!StringUtil.isNullOrEmpty(customerQueryBuilder.toString())) {
                    customerquery = "and customer.id IN (" + customerQueryBuilder.substring(0, customerQueryBuilder.lastIndexOf(",")).toString() + ")";
                }
            }

        }
        
        
          
       
        
        
         String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestObj.has("filterConjuctionCriteria") && requestObj.get("filterConjuctionCriteria") != null) {
                if (requestObj.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
        
        boolean advanceSearch = requestObj.optBoolean("isAdvanceSearch",false);
        String moduleid = "";
        
        String searchJoin = "";
        String mySISearchJoin = "";
        String myOpnSISearchJoin = "";
        String myCreditNoteSearchJoin = "";
        String myOpnCreditNoteSearchJoin = "";
        String myDebitNoteSearchJoin = "";
        String myOpnDebitNoteSearchJoin = "";
        String myPaymentMadeSearchJoin = "";
        String myReceiptPaymentSearchJoin = "";
        String myOpnReceiptPaymentSearchJoin = "";
        
       
        String mySearchFilterString = "";
        String mySISearchString = "";
        String myOpnSISearchString = "";
        String myCreditNoteSearchString = "";
        String myOpnCreditNoteSearchString = "";
        String myDebitNoteSearchString = "";
        String myOpnDebitNoteSearchString = "";
        String myPaymentMadeSearchString = "";
        String myReceiptPaymentSearchString = "";
        String myOpnReceiptPaymentSearchString = "";
        
        String searchDefaultFieldSQL = "";
        
        String invoiceSearchJson = "";
        String makePaymentSearchJson = "";
        String receiptSearchJson = "";
        String cnSearchJson = "";
        String dnSearchJson = "";
        
        if (requestObj.has(Constants.moduleid) && requestObj.get(Constants.moduleid) != null) {
                moduleid = requestObj.get(Constants.moduleid).toString();
            }
        
        if(advanceSearch){
            if (requestObj.has("searchJson") && requestObj.get("searchJson") != null) { //Advance Search Case
               // Advance  Search Code

                if (requestObj.has("invoiceSearchJson") && requestObj.get("invoiceSearchJson") != null) {
                    invoiceSearchJson = (String) requestObj.get("invoiceSearchJson");
                }

                if (requestObj.has("makePaymentSearchJson") && requestObj.get("makePaymentSearchJson") != null) {
                    makePaymentSearchJson = (String) requestObj.get("makePaymentSearchJson");
                }

                if (requestObj.has("receiptSearchJson") && requestObj.get("receiptSearchJson") != null) {
                    receiptSearchJson = (String) requestObj.get("receiptSearchJson");
                }

                if (requestObj.has("cnSearchJson") && requestObj.get("cnSearchJson") != null) {
                    cnSearchJson = (String) requestObj.get("cnSearchJson");
                }

                if (requestObj.has("dnSearchJson") && requestObj.get("dnSearchJson") != null) {
                    dnSearchJson = (String) requestObj.get("dnSearchJson");
                }

                JSONObject mySearchJSON = getCustomSearchString(requestObj, Constants.Acc_Invoice_ModuleId, false);
                mySISearchString = mySearchJSON.getString("mySearchFilterString");
                mySISearchJoin = mySearchJSON.getString("customjoin");

                mySearchJSON = getCustomSearchString(requestObj, Constants.Acc_Make_Payment_ModuleId, false);
                myPaymentMadeSearchString = mySearchJSON.getString("mySearchFilterString");
                myPaymentMadeSearchJoin = mySearchJSON.getString("customjoin");

                mySearchJSON = getCustomSearchString(requestObj, Constants.Acc_Receive_Payment_ModuleId, false);
                myReceiptPaymentSearchString = mySearchJSON.getString("mySearchFilterString");
                myReceiptPaymentSearchJoin = mySearchJSON.getString("customjoin");

                mySearchJSON = getCustomSearchString(requestObj, Constants.Acc_Debit_Note_ModuleId, false);
                myDebitNoteSearchString = mySearchJSON.getString("mySearchFilterString");
                myDebitNoteSearchJoin = mySearchJSON.getString("customjoin");

                mySearchJSON = getCustomSearchString(requestObj, Constants.Acc_Credit_Note_ModuleId, false);
                myCreditNoteSearchString = mySearchJSON.getString("mySearchFilterString");
                myCreditNoteSearchJoin = mySearchJSON.getString("customjoin");

                mySearchJSON = getCustomSearchString(requestObj, Constants.Acc_Invoice_ModuleId, true);
                myOpnSISearchString = mySearchJSON.getString("mySearchFilterString");
                myOpnSISearchJoin = mySearchJSON.getString("customjoin");

                mySearchJSON = getCustomSearchString(requestObj, Constants.Acc_Receive_Payment_ModuleId, true);
                myOpnReceiptPaymentSearchString = mySearchJSON.getString("mySearchFilterString");
                myOpnReceiptPaymentSearchJoin = mySearchJSON.getString("customjoin");

                mySearchJSON = getCustomSearchString(requestObj, Constants.Acc_Debit_Note_ModuleId, true);
                myOpnDebitNoteSearchString = mySearchJSON.getString("mySearchFilterString");
                myOpnDebitNoteSearchJoin = mySearchJSON.getString("customjoin");

                mySearchJSON = getCustomSearchString(requestObj, Constants.Acc_Credit_Note_ModuleId, true);
                myOpnCreditNoteSearchString = mySearchJSON.getString("mySearchFilterString");
                myOpnCreditNoteSearchJoin = mySearchJSON.getString("customjoin");

                mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
            }
        }
        /*========================Advance Search Related Code End==========================*/
        
        String receiptCheque = "";
        boolean isPostDatedCheque = requestObj.optBoolean("isPostDatedCheque", false);
        if (isPostDatedCheque) {
            receiptCheque += "INNER JOIN paydetail pd1 ON r.paydetail = pd1.id INNER JOIN cheque ch1 ON pd1.cheque = ch1.id AND DATE(ch1.duedate) <= DATE(now()) \n";
        }
        String receiptADVQuery =  "left join receiptadvancedetail rad on rad.receipt=r.id\n";
       
        
        String JEJoinForLDR = " inner join jedetail jed on jed.journalentry = je.id and jed.debit='F' ";
        String LDRSearchJoin = myReceiptPaymentSearchJoin;
        String LDRSearchString = myReceiptPaymentSearchString;
        
        if(isOutstanding){
            receiptADVQuery =  "inner join receiptadvancedetail rad on rad.receipt=r.id\n";
        
//            if(!isFromAR){
//            receiptSelectStatement = receiptOutstandingSelectStatement;  //Select Statement for outstanding case where amount is fetch from receiptadvancedetail table
//            }
            JEJoinForLDR ="";
            LDRSearchJoin = "";
            LDRSearchString = "";
            
            
        }
        
        boolean isSortedOnCreationDate = false;
        if (requestObj.has("isSortedOnCreationDate") && requestObj.optString("isSortedOnCreationDate") != "") {
            isSortedOnCreationDate = requestObj.optBoolean("isSortedOnCreationDate");
        }
//        String orderBy = " ";
        String orderBy = " ORDER BY a.type, a.entrydate";
        
        if (isSortedOnCreationDate) {
            if (isFromAR) {
                orderBy = " ORDER BY a.custname,a.type, a.duedate desc";//customerName,duedate
            } else {
                orderBy = " ORDER BY a.custname, a.type, a.entrydate desc";//customerName,entryno 
            }
        }
        /**
         * if the call is not from aged receivable the we have to display dishonoured payment and reverse JE of dishonoured payments ERM-744.
         */
        String selectForDishonoured = "";
        String selectForDishonouredMPToCustomer = "";
        String whereCaluseForDishonoured = "where r.company = ? and r.isopeningbalencereceipt=0 and r.deleteflag='F' and r.contraentry='F' and r.isdishonouredcheque='F' \n";
        String whereCaluseForDishonouredMPToCustomer = "where p.company = ? and p.deleteflag='F' and p.contraentry='F' and p.isDishonouredCheque='F' and p.paymentWindowType = '2' and p.approvestatuslevel = '11'";
        if (!showDishonouredPayments) {
            selectForDishonoured = "\n UNION ALL \n"
                    + receiptdishonouredselect + "\n" 
                    + " FROM receipt r \n"
                    + "inner join journalentry je on r.dishonouredchequeje = je.id and entryDate <=?\n"
                    + "INNER JOIN customer on customer.id = r.customer \n"
//                    + "inner join jedetail jed on jed.journalentry = je.id and jed.debit='T' and jed.account = customer.account\n"
                    + "inner join jedetail jed on jed.journalentry = je.id and jed.debit='T' \n"
                    + "inner join account acc on jed.account = acc.id\n"
                    + receiptADVQuery
                    + "INNER JOIN company on r.company = company.companyid \n"
                    + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                    + "INNER JOIN currency rcurr on r.currency = rcurr.currencyid \n"
                    + receiptCheque
                    + myReceiptPaymentSearchJoin
                    + "where r.company = ? and r.isopeningbalencereceipt=0 and r.deleteflag='F' and r.contraentry='F' and r.isdishonouredcheque='T' \n"
                    + "and r.paymentwindowtype != '3' and r.approvestatuslevel = '11' and acc.usedin like '%"+ Constants.Customer_default_account +"%' \n"
                    + conditionRCCurr
                    + myReceiptPaymentSearchString + " \n"
                    + customerquery
                    + " group by r.receiptnumber \n";  //Select Statement for Dishonoured Payment
            
            whereCaluseForDishonoured = "where r.company = ? and r.isopeningbalencereceipt=0 and r.deleteflag='F' and r.contraentry='F'  \n";
            
            selectForDishonouredMPToCustomer="\n UNION ALL \n"
                    + paymentdishonouredselect + "\n"
                    + " FROM payment p\n"
                    + "inner join journalentry je on p.dishonouredchequeje=je.id and je.entryDate <=?\n"
                    + "inner join jedetail jed on jed.journalentry=je.id and jed.debit='F'\n"
                    + "inner join account acc on jed.account=acc.id \n"
                    + "INNER JOIN company on p.company = company.companyid \n"
                    + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                    + "INNER JOIN customer on customer.id = p.customer \n"
                    + "INNER JOIN currency pcurr on p.currency = pcurr.currencyid \n"
                    + myPaymentMadeSearchJoin
                    + "where p.company = ? and p.deleteflag='F' and p.contraentry='F' and p.isDishonouredCheque='T' and p.paymentWindowType = '2' and p.approvestatuslevel = '11'"
                    + conditionPYCurr
                    + myPaymentMadeSearchString  +" \n"
                    + customerquery
                    + " group by p.paymentnumber \n";

            whereCaluseForDishonouredMPToCustomer = "where p.company = ? and p.deleteflag='F' and p.contraentry='F' and p.paymentWindowType = '2' and p.approvestatuslevel = '11'";
        }

        String query = "SELECT a.* from ("
                + invMainSelectStatement + "\n"
                + "from (\n"
                + invSelectStatement + "\n" //Inv Query
                + "from invoice   \n"
                + "inner join journalentry je on invoice.journalentry = je.id  \n"
                + "inner join jedetail on  jedetail.id = invoice.centry \n"
                + "inner join customer on customer.id = invoice.customer  \n"
                + "inner join company on invoice.company=company.companyid\n"
                + "inner join creditterm term on customer.creditterm = term.termid\n"
                + "inner join currency invcurr on invoice.currency=invcurr.currencyid\n"
                + "inner join currency compcurr on company.currency=compcurr.currencyid\n"
                + InvJoin
                + "left join customercategorymapping on customercategorymapping.customerid = customer.id \n"
                + "left join costcenter on costcenter.id = je.costcenter   \n"
                + mySISearchJoin
                + "where  invoice.company = ? and je.entrydate <=?  and invoice.deleteflag='F'  and invoice.approvestatuslevel= '11'  and invoice.istemplate != '2'  and invoice.isdraft = '0' "
                + conditionINVCurr
                + INVCondition
                + mySISearchString  +"\n"
                + customerquery
                + " group by invoice.invoicenumber \n"
                + "\n UNION ALL \n"
                + invCNDSelectStatement + "\n" //To Calculate KnockOff From CNdetail.
                + "from invoice   \n"
                + "inner join journalentry je on invoice.journalentry = je.id  \n"
                + "inner join jedetail on  jedetail.id = invoice.centry \n"
                + "inner join customer on customer.id = invoice.customer  \n"
                + "left join customercategorymapping on customercategorymapping.customerid = customer.id  \n"
                + "left join costcenter on costcenter.id = je.costcenter   \n"
                + "inner join cndetails cnd on cnd.invoice=invoice.id and cnd.company=invoice.company and cnd.invoicelinkdate<=?\n"
                + "inner join discount on discount.id=cnd.discount \n"
                + "inner join creditnote cn on cn.id=cnd.creditnote and cn.deleteflag='F' \n"
                + "inner join company on invoice.company=company.companyid\n"
                + "inner join creditterm term on customer.creditterm = term.termid\n"
                + InvJoin
                + "inner join currency invcurr on invoice.currency=invcurr.currencyid\n"
                + "inner join currency compcurr on company.currency=compcurr.currencyid\n"
                + mySISearchJoin
                + "where  invoice.company = ? and je.entrydate <=?  and invoice.deleteflag='F'  and invoice.approvestatuslevel= '11'  and invoice.istemplate != '2'  and invoice.isdraft = '0' "
                + conditionINVCurr
                + INVCondition
                + mySISearchString  +"  \n"
                + customerquery
                + " group by invoice.invoicenumber \n"
                + "\n UNION ALL \n"
                + invLDRSelectStatement + "\n"
                + " from invoice   \n"
                + "inner join journalentry je on invoice.journalentry = je.id  \n"
                + "inner join jedetail on  jedetail.id = invoice.centry \n"
                + "inner join customer on customer.id = invoice.customer  \n"
                + "left join customercategorymapping on customercategorymapping.customerid = customer.id  \n"
                + "left join costcenter on costcenter.id = je.costcenter   \n"
                + "inner join linkdetailreceipt ldr on ldr.invoice=invoice.id and ldr.company=invoice.company and ldr.receiptLinkDate<=?\n"
                + "inner join company on invoice.company=company.companyid\n"
                + "inner join creditterm term on customer.creditterm = term.termid\n"
                + InvJoin
                + "inner join currency invcurr on invoice.currency=invcurr.currencyid\n"
                + "inner join currency compcurr on company.currency=compcurr.currencyid\n"
                + mySISearchJoin
                + "where  invoice.company = ? and je.entrydate <=?  and invoice.deleteflag='F'  and invoice.approvestatuslevel= '11'  and invoice.istemplate != '2'  and invoice.isdraft = '0' "
                + conditionINVCurr
                + INVCondition
                + mySISearchString  +"  \n"
                + customerquery
                + " group by invoice.invoicenumber \n"
                + "\n UNION ALL \n"
                + invRDSelectStatement + "\n"
                + " from invoice   \n"
                + "inner join journalentry je on invoice.journalentry = je.id  \n"
                + "inner join jedetail on  jedetail.id = invoice.centry \n"
                + "inner join customer on customer.id = invoice.customer  \n"
                + "left join customercategorymapping on customercategorymapping.customerid = customer.id  \n"
                + "left join costcenter on costcenter.id = je.costcenter   \n"
                + "inner join receiptdetails rd on rd.invoice=invoice.id and rd.company=invoice.company  \n"
                + "inner join receipt on rd.receipt=receipt.id and receipt.isdishonouredcheque='F' and receipt.approvestatuslevel=11 \n"
                + "inner join journalentry rje on receipt.journalentry=rje.id and (receipt.creationdate<=? or rje.entryDate<=?) \n"
                + "inner join company on invoice.company=company.companyid\n"
                + "inner join creditterm term on customer.creditterm = term.termid\n"
                + InvJoin
                + "inner join currency invcurr on invoice.currency=invcurr.currencyid\n"
                + "inner join currency compcurr on company.currency=compcurr.currencyid\n"
                + mySISearchJoin
                + "where  invoice.company = ? and je.entrydate <=?  and invoice.deleteflag='F'  and invoice.approvestatuslevel= '11'  and invoice.istemplate != '2'  and invoice.isdraft = '0' "
                + conditionINVCurr
                + INVCondition
                + mySISearchString  +"  \n"
                + customerquery
                + " group by invoice.invoicenumber \n"
                + "\n UNION ALL \n"
                + invWOSelectStatement + "\n"
                + " from invoice   \n"
                + "inner join journalentry je on invoice.journalentry = je.id  \n"
                + "inner join jedetail on  jedetail.id = invoice.centry \n"
                + "inner join customer  on customer.id = invoice.customer  \n"
                + "left join customercategorymapping on customercategorymapping.customerid = customer.id  \n"
                + "left join costcenter on costcenter.id = je.costcenter   \n"
                + "inner join company on invoice.company=company.companyid\n"
                + "inner join creditterm term on customer.creditterm = term.termid\n"
                + InvJoin
                + "inner join currency invcurr on invoice.currency=invcurr.currencyid\n"
                + "inner join currency compcurr on company.currency=compcurr.currencyid\n"
                + "inner join invoicewriteoff iwo on iwo.invoice=invoice.id and iwo.company=invoice.company and iwo.isrecovered='F' and iwo.writeoffdate<=?  \n"
                + mySISearchJoin
                + "where  invoice.company = ? and je.entrydate <=?  and invoice.deleteflag='F'  and invoice.approvestatuslevel= '11'  and invoice.istemplate != '2'  and invoice.isdraft = '0' "
                + conditionINVCurr
                + INVCondition
                + mySISearchString  +"  \n"
                + customerquery
                + " group by invoice.invoicenumber \n"
                + ") i  group by i.jeentryno "  //inv
//                + ") i  group by i.entryno "  //inv
                +"\n UNION ALL \n"
                 + invOpnMainSelectStatement + "\n"
                + "from (\n"
                + invOpnSelectStatement + "\n"
                + "from invoice   \n"
                + "inner join customer  on customer.id = invoice.customer  \n"
                + "inner join company on invoice.company=company.companyid\n"
                + "inner join creditterm term on customer.creditterm = term.termid\n"
                + "inner join currency invcurr on invoice.currency=invcurr.currencyid\n"
                + "inner join currency compcurr on company.currency=compcurr.currencyid\n"
                + "left join customercategorymapping on customercategorymapping.customerid = customer.id \n"
                + InvJoin
                + myOpnSISearchJoin
                + "where  invoice.company = ? and invoice.deleteflag='F' and invoice.istemplate != '2'  and invoice.isdraft = '0' and invoice.isopeningbalenceinvoice=1  "
                + conditionINVCurr
                + INVCondition
                + myOpnSISearchString  +"\n"
                + customerquery
                + " group by invoice.invoicenumber \n"
                + "\n UNION ALL \n"
                + invOpnCNDSelectStatement + "\n"
                + "from invoice   \n"
                + "inner join customer on customer.id = invoice.customer  \n"
                + "left join customercategorymapping on customercategorymapping.customerid = customer.id  \n"
                + "inner join cndetails cnd on cnd.invoice=invoice.id and cnd.company=invoice.company and cnd.invoicelinkdate<=?\n"
                + "inner join discount on discount.id=cnd.discount \n"
                + "inner join creditnote cn on cn.id=cnd.creditnote and cn.deleteflag='F' \n"
                + "inner join company on invoice.company=company.companyid\n"
                + "inner join creditterm term on customer.creditterm = term.termid\n"
                + "inner join currency invcurr on invoice.currency=invcurr.currencyid\n"
                + "inner join currency compcurr on company.currency=compcurr.currencyid\n"
                + InvJoin
                + myOpnSISearchJoin
                + "where  invoice.company = ? and invoice.deleteflag='F' and invoice.istemplate != '2'  and invoice.isdraft = '0' and invoice.isopeningbalenceinvoice=1 "
                + conditionINVCurr
                + INVCondition
                + myOpnSISearchString  +" \n"
                + customerquery
                + " group by invoice.invoicenumber \n"
                + "\n UNION ALL \n"
                + invOpnLDRSelectStatement + "\n"
                + " from invoice   \n"
                + "inner join customer  on customer.id = invoice.customer  \n"
                + "left join customercategorymapping on customercategorymapping.customerid = customer.id  \n"
                + "left join linkdetailreceipt ldr on ldr.invoice=invoice.id and ldr.company=invoice.company and ldr.receiptLinkDate<=?\n"
                + "inner join company on invoice.company=company.companyid\n"
                + "inner join creditterm term on customer.creditterm = term.termid\n"
                + "inner join currency invcurr on invoice.currency=invcurr.currencyid\n"
                + "inner join currency compcurr on company.currency=compcurr.currencyid\n"
                + InvJoin
                + myOpnSISearchJoin
                + "where  invoice.company = ? and invoice.deleteflag='F' and invoice.istemplate != '2'  and invoice.isdraft = '0'  and invoice.isopeningbalenceinvoice=1 "
                + conditionINVCurr 
                + INVCondition
                + myOpnSISearchString  +"\n"
                + customerquery
                + " group by invoice.invoicenumber \n"
                + "\n UNION ALL \n"
                + invOpnRDSelectStatement + "\n"
                + " from invoice   \n"
                + "inner join customer  on customer.id = invoice.customer  \n"
                + "left join customercategorymapping on customercategorymapping.customerid = customer.id  \n"
                + "inner join receiptdetails rd on rd.invoice=invoice.id and rd.company=invoice.company  \n"
                + "inner join receipt on rd.receipt=receipt.id and receipt.isdishonouredcheque='F' and receipt.approvestatuslevel=11 \n"
                + "inner join company on invoice.company=company.companyid\n"
                + "inner join creditterm term on customer.creditterm = term.termid\n"
                + "inner join currency invcurr on invoice.currency=invcurr.currencyid\n"
                + "inner join currency compcurr on company.currency=compcurr.currencyid\n"
                + InvJoin
                + myOpnSISearchJoin
                + "where  invoice.company = ? and invoice.deleteflag='F' and invoice.istemplate != '2'  and invoice.isdraft = '0' and invoice.isopeningbalenceinvoice=1 "
                + conditionINVCurr
                + INVCondition
                + myOpnSISearchString  +" \n"
                + customerquery
                + " group by invoice.invoicenumber \n"
                + "\n UNION ALL \n"
                + invOpnWOSelectStatement + "\n"
                + " from invoice   \n"
                + "inner join customer on customer.id = invoice.customer  \n"
                + "left join customercategorymapping on customercategorymapping.customerid = customer.id  \n"
                + "inner join company on invoice.company=company.companyid\n"
                + "inner join creditterm term on customer.creditterm = term.termid\n"
                + "inner join currency invcurr on invoice.currency=invcurr.currencyid\n"
                + "inner join currency compcurr on company.currency=compcurr.currencyid\n"
                + "inner join invoicewriteoff iwo on iwo.invoice=invoice.id and iwo.company=invoice.company and iwo.isrecovered='F' and iwo.writeoffdate<=?  \n"
                + InvJoin
                + myOpnSISearchJoin
                + "where  invoice.company = ? and invoice.deleteflag='F' and invoice.istemplate != '2'  and invoice.isdraft = '0' and invoice.isopeningbalenceinvoice=1 "
                + conditionINVCurr
                + INVCondition
                + myOpnSISearchString  +" \n"
                + customerquery
                + " group by invoice.invoicenumber \n"
                + ") i  group by i.docno "  //inv opn
                +"\n UNION ALL \n"
                + cnMainSelectStatement +"\n"
                + "from ("
                + cnSelectStatement + "\n" // CN Select Query
                + "from creditnote cn \n"
                + "INNER JOIN journalentry je ON cn.journalentry=je.id and je.entrydate <=?\n"
                + "INNER JOIN jedetail jed  on jed.id = cn.centry \n"
                + "INNER JOIN customer  ON customer.id=cn.customer \n"
                + "INNER JOIN company on cn.company = company.companyid \n"
                + CNJoin
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency cncurr on cn.currency = cncurr.currencyid \n"
                + myCreditNoteSearchJoin
                + "where cn.company = ? and cn.approvestatuslevel = '11' and cn.deleteflag='F'  and cn.cntype!=4 "
                + conditionCNCurr
                + myCreditNoteSearchString  +"\n"
                + customerquery
                + " group by cn.cnnumber \n"
                + "\n UNION ALL \n"
                + cnLDRSelectStatement + "\n"  // To Calculate Knockoff for creditnote.
                + "from creditnote cn \n"
                + "INNER JOIN journalentry je ON cn.journalentry=je.id and je.entrydate <=?\n"
                + "INNER JOIN jedetail jed  on jed.id = cn.centry \n"
                + "INNER JOIN customer  ON customer.id=cn.customer \n"
                + "INNER JOIN company on cn.company = company.companyid \n"
                + CNJoin
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency cncurr on cn.currency = cncurr.currencyid \n"
                + "INNER JOIN linkdetailpaymenttocreditnote ldr on ldr.creditnote=cn.id and ldr.paymentlinkdate<=? and ldr.company=cn.company \n" 
                + myCreditNoteSearchJoin
                + "where cn.company = ? and cn.approvestatuslevel = '11' and cn.deleteflag='F'  and cn.cntype!=4 "
                + conditionCNCurr
                + myCreditNoteSearchString  +"\n"
                + customerquery
                + " group by cn.cnnumber \n"
                + "\n UNION ALL \n"
                + cnCNPSelectStatement + "\n"
                + "from creditnote cn \n"
                + "INNER JOIN journalentry je ON cn.journalentry=je.id and je.entrydate <=?\n"
                + "INNER JOIN jedetail jed  on jed.id = cn.centry \n"
                + "INNER JOIN customer ON customer.id=cn.customer \n"
                + "INNER JOIN company on cn.company = company.companyid \n"
                + CNJoin
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency cncurr on cn.currency = cncurr.currencyid \n"
                + "INNER JOIN creditnotpayment cnp on cnp.cnid = cn.id \n"
                + myCreditNoteSearchJoin
                + "where cn.company = ? and cn.approvestatuslevel = '11' and cn.deleteflag='F'  and cn.cntype!=4 "
                + conditionCNCurr
                + myCreditNoteSearchString  +"\n"
                + customerquery
                + " group by cn.cnnumber \n"
                + "\n UNION ALL \n"
                + cnDISSelectStatement + "\n"
                + "from creditnote cn \n"
                + "INNER JOIN journalentry je ON cn.journalentry=je.id and je.entrydate <=?\n"
                + "INNER JOIN jedetail jed  on jed.id = cn.centry \n"
                + "INNER JOIN customer  ON customer.id=cn.customer \n"
                + "INNER JOIN company on cn.company = company.companyid \n"
                + CNJoin
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency cncurr on cn.currency = cncurr.currencyid \n"
                + "INNER JOIN cndetails cnd on cnd.creditnote=cn.id and cnd.company=cn.company and (cnd.invoice is not null or cnd.debitnoteid is not null) and cnd.invoicelinkdate<=?\n"
                + "INNER JOIN discount on cnd.discount=discount.id \n"
                + myCreditNoteSearchJoin
                + "where cn.company = ? and cn.approvestatuslevel = '11' and cn.deleteflag='F'  and cn.cntype!=4 "
                + conditionCNCurr
                + myCreditNoteSearchString  +"\n"
                + customerquery
                + " group by cn.cnnumber \n"
                //CN FRX
                + "\n UNION ALL \n"
                + cnFRXSelectStatement + "\n" //Calculate FRX of CN
                + "from jedetail jed \n"
                + "inner join journalentry je on je.id=jed.journalentry and entryDate <=?\n"
                + "inner join cndetails cnd on cnd.linkedgainlossje=je.id\n" 
                + "inner join creditnote cn on cnd.creditNote=cn.id\n"
                + "INNER JOIN account acc ON jed.account=acc.id \n"
                + "INNER JOIN customer  ON customer.id=cn.customer \n"
                + "INNER JOIN company on cn.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency cncurr on cn.currency = cncurr.currencyid \n"
                +  CNJoin
                + myCreditNoteSearchJoin
                + "where cn.company = ? and cn.approvestatuslevel = '11' and cn.deleteflag='F'  and cn.cntype!=4 \n"
                + conditionCNCurr
                + myCreditNoteSearchString  +" \n"
                + customerquery
                + " group by je.entryno \n" //cn
                + ") cn group by cn.jeentryno"  //cn
                + "\nUNION ALL\n"
                + cnOpnMainSelectStatement +"\n"
                + "from ("
                + cnOpnSelectStatement + "\n"
                + "from creditnote cn\n"
                + "INNER JOIN company on cn.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN customer  on customer.id = cn.customer \n"
                + "INNER JOIN currency cncurr on cn.currency = cncurr.currencyid \n"
                + CNJoin
                + myOpnCreditNoteSearchJoin
                + "where cn.isopeningbalencecn=1 and cn.iscnforcustomer=1 and cn.deleteflag='F' and cn.company = ?  \n"
                + conditionCNCurr
                + myOpnCreditNoteSearchString  +" \n"
                + customerquery
                + " group by cn.cnnumber \n"
                + "\n UNION ALL \n"
                + cnOpnCNPSelectStatement + "\n"
                + "from creditnote cn\n"
                + "INNER JOIN company on cn.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN customer  on customer.id = cn.customer \n"
                + "INNER JOIN currency cncurr on cn.currency = cncurr.currencyid \n"
                + "INNER JOIN creditnotpayment cnp on cnp.cnid = cn.id \n"
                + CNJoin
                + myOpnCreditNoteSearchJoin
                + "where cn.isopeningbalencecn=1 and cn.iscnforcustomer=1 and cn.deleteflag='F' and cn.company = ?  \n"
                + conditionCNCurr
                + myOpnCreditNoteSearchString  +" \n"
                + customerquery
                + " group by cn.cnnumber \n"
                + "\n UNION ALL \n"
                + cnOpnDISSelectStatement+ "\n"
                + "from creditnote cn\n"
                + "INNER JOIN company on cn.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN customer  on customer.id = cn.customer \n"
                + "INNER JOIN cndetails cnd on cnd.creditnote=cn.id and cnd.company=cn.company and (cnd.invoice is not null or cnd.debitnoteid is not null) and cnd.invoicelinkdate<=?\n"
                + "INNER JOIN currency cncurr on cn.currency = cncurr.currencyid \n"
                + "INNER JOIN discount on cnd.discount=discount.id \n"
                + CNJoin
                + myOpnCreditNoteSearchJoin
                + "where cn.isopeningbalencecn=1 and cn.iscnforcustomer=1 and cn.deleteflag='F' and cn.company = ? \n"
                + conditionCNCurr
                + myOpnCreditNoteSearchString  +" \n"
                + customerquery
                + " group by cn.cnnumber \n"
                + "\n UNION ALL \n"
                + cnOpnLDRSelectStatement+ "\n"
                + "from creditnote cn\n"
                + "INNER JOIN company on cn.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN customer on customer.id = cn.customer \n"
                + "INNER JOIN currency cncurr on cn.currency = cncurr.currencyid \n"
                + "INNER JOIN linkdetailpaymenttocreditnote ldr on ldr.creditnote=cn.id and ldr.paymentlinkdate<=? and ldr.company=cn.company\n"
                + CNJoin
                + myOpnCreditNoteSearchJoin
                + "where cn.isopeningbalencecn=1 and cn.iscnforcustomer=1 and cn.deleteflag='F' and cn.company = ? \n"
                + conditionCNCurr
                + myOpnCreditNoteSearchString  +" \n"
                + customerquery
                + " group by cn.cnnumber \n"
                + ") cn group by cn.docno"  //cn opn
                + "\n UNION ALL \n"
                + dnMainSelectStatement +"\n"
                + "from ("
                + dnSelectStatement + "\n"
                + " FROM debitnote dn \n"
                + "INNER JOIN journalentry je ON dn.journalentry=je.id and je.entrydate <=?\n"
                + "INNER JOIN jedetail jed ON dn.centry=jed.id\n"
                + "INNER JOIN customer  on customer.id = dn.customer \n"
                + "INNER JOIN company on dn.company = company.companyid \n"
                + DNJoin
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency dncurr on dn.currency = dncurr.currencyid \n"
                + myDebitNoteSearchJoin
                + "where dn.company = ? and dn.approvestatuslevel = '11' and dn.deleteflag='F' and (dn.dntype=4 or dn.dntype=5)  and jed.id=dn.centry \n"
                + conditionDNCurr
                + myDebitNoteSearchString  +" \n"
                + customerquery
                + " group by dn.dnnumber \n"               
                + "\n UNION ALL \n"
                + dnLDRSelectStatement + "\n"
                + " FROM debitnote dn \n"
                + "INNER JOIN journalentry je ON dn.journalentry=je.id and je.entrydate <=?\n"
                + "INNER JOIN jedetail jed ON dn.centry=jed.id\n"
                + "INNER JOIN customer  on customer.id = dn.customer \n"
                + "INNER JOIN company on dn.company = company.companyid \n"
                + DNJoin
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency dncurr on dn.currency = dncurr.currencyid \n"
                + "INNER JOIN linkdetailreceipttodebitnote ldr on ldr.debitnote=dn.id and ldr.receiptlinkdate<=? and ldr.company=dn.company\n"
                + myDebitNoteSearchJoin
                + "where dn.company = ? and dn.approvestatuslevel = '11' and dn.deleteflag='F' and (dn.dntype=4 or dn.dntype=5)  and jed.id=dn.centry \n"
                + conditionDNCurr
                + myDebitNoteSearchString  +" \n"
                + customerquery
                + " group by dn.dnnumber \n"               
                + "\n UNION ALL \n"
                + dnDNPSelectStatement + "\n"
                + " FROM debitnote dn \n"
                + "INNER JOIN journalentry je ON dn.journalentry=je.id and je.entrydate <=?\n"
                + "INNER JOIN jedetail jed ON dn.centry=jed.id\n"
                + "INNER JOIN customer  on customer.id = dn.customer \n"
                + "INNER JOIN company on dn.company = company.companyid \n"
                + DNJoin
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency dncurr on dn.currency = dncurr.currencyid \n"
                + "INNER JOIN debitnotepayment dnp on dnp.dnid = dn.id \n"
                + myDebitNoteSearchJoin
                + "where dn.company = ? and dn.approvestatuslevel = '11' and dn.deleteflag='F' and (dn.dntype=4 or dn.dntype=5)  and jed.id=dn.centry \n"
                + conditionDNCurr
                + myDebitNoteSearchString  +" \n"
                + customerquery
                + " group by dn.dnnumber \n"               
                + "\n UNION ALL \n"
               + dnDISSelectStatement + "\n"
                + " FROM debitnote dn \n"
                + "INNER JOIN journalentry je ON dn.journalentry=je.id and je.entrydate <=?\n"
                + "INNER JOIN jedetail jed ON dn.centry=jed.id\n"
                + "INNER JOIN customer  on customer.id = dn.customer \n"
                + "INNER JOIN company on dn.company = company.companyid \n"
                + DNJoin
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency dncurr on dn.currency = dncurr.currencyid \n"
                + "INNER JOIN cndetails cnd on cnd.debitnoteid=dn.id and cnd.company=dn.company and cnd.invoicelinkdate<=?\n"
                + "INNER JOIN discount on cnd.discount=discount.id  and dn.deleteflag='F'\n"
                + myDebitNoteSearchJoin
                + "where dn.company = ? and dn.approvestatuslevel = '11' and dn.deleteflag='F' and (dn.dntype=4 or dn.dntype=5)  and jed.id=dn.centry  \n"
                + conditionDNCurr
                + myDebitNoteSearchString  +" \n"
                + customerquery
                + " group by dn.dnnumber \n"               
                + ") dn group by dn.jeentryno"  //dn
                + " \n UNION ALL \n"
                + dnOpnMainSelectStatement +"\n"
                + "from ("
                + dnOpnSelectStatement + "\n"
                + " from debitnote dn\n"
                + "INNER JOIN company on dn.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN customer  on customer.id = dn.customer \n"
                + "INNER JOIN currency dncurr on dn.currency = dncurr.currencyid \n"
                + DNJoin
                + myOpnDebitNoteSearchJoin
                + "where dn.isopeningbalencedn=1 and dn.isdnforvendor=0 and dn.deleteflag='F' and dn.company = ?  \n"
                + conditionDNCurr
                + myOpnDebitNoteSearchString  +"\n"
                + customerquery
                + " group by dn.dnnumber \n"               
                + "\n UNION ALL \n"
                + dnOpnLDRSelectStatement + "\n"
                + " from debitnote dn\n"
                + "INNER JOIN company on dn.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN customer  on customer.id = dn.customer \n"
                + "INNER JOIN currency dncurr on dn.currency = dncurr.currencyid \n"
                + "INNER join linkdetailreceipttodebitnote ldr on ldr.debitnote=dn.id and ldr.company=dn.company\n"
                + DNJoin
                + myOpnDebitNoteSearchJoin
                + "where dn.isopeningbalencedn=1 and dn.isdnforvendor=0 and dn.deleteflag='F' and dn.company = ? \n"
                + conditionDNCurr
                + myOpnDebitNoteSearchString  +"\n"
                + customerquery
                + " group by dn.dnnumber \n"               
                + "\n UNION ALL \n"
                + dnOpnDNPSelectStatement + "\n"
                + " from debitnote dn\n"
                + "INNER JOIN company on dn.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN customer  on customer.id = dn.customer \n"
                + "INNER JOIN currency dncurr on dn.currency = dncurr.currencyid \n"
                + "INNER join debitnotepayment dnp on dnp.dnid=dn.id \n"
                + DNJoin
                + myOpnDebitNoteSearchJoin
                + "where dn.isopeningbalencedn=1 and dn.isdnforvendor=0 and dn.deleteflag='F' and dn.company = ? \n"
                + conditionDNCurr
                + myOpnDebitNoteSearchString  +" \n"
                + customerquery
                + " group by dn.dnnumber \n"               
                + "\n UNION ALL \n"
                + dnOpnDISSelectStatement + "\n"
                + " from debitnote dn\n"
                + "INNER JOIN company on dn.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN customer  on customer.id = dn.customer \n"
                + "INNER JOIN currency dncurr on dn.currency = dncurr.currencyid \n"
                + "INNER JOIN cndetails cnd on cnd.debitnoteid=dn.id and cnd.company=dn.company and cnd.invoicelinkdate<=?\n"
                + "INNER JOIN discount on cnd.discount=discount.id and dn.deleteflag='F' \n"
                + DNJoin
                + myOpnDebitNoteSearchJoin
                + "where dn.isopeningbalencedn=1 and dn.isdnforvendor=0 and dn.deleteflag='F' and dn.company = ?  \n"
                + conditionDNCurr
                + myOpnDebitNoteSearchString  +" \n"
                + customerquery
                + " group by dn.dnnumber \n"               
                + ") dn group by dn.docno"  //dn opn
                + "\n UNION ALL \n"
                + paymentMainSelectStatement +"\n"
                + "from ("
                + paymentSelectStatement + "\n"
                + " FROM payment p\n"
                + "inner join journalentry je on p.journalEntry=je.id and je.entryDate <=?\n"
                + "inner join jedetail jed on jed.journalentry=je.id and jed.debit='T'\n"
                + "INNER JOIN company on p.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN customer  on customer.id = p.customer \n"
                + "INNER JOIN currency pcurr on p.currency = pcurr.currencyid \n"
                + myPaymentMadeSearchJoin
//                + "where p.company = ? and p.deleteflag='F' and p.contraentry='F' and p.isDishonouredCheque='F' and p.paymentWindowType = '2' and p.approvestatuslevel = '11'"
                + whereCaluseForDishonouredMPToCustomer
                + conditionPYCurr
                + myPaymentMadeSearchString  +" \n"
                + customerquery
                + " group by p.paymentnumber \n"               
                + "\n UNION ALL \n"
                + paymentCNPSelectStatement + "\n"
                 + " FROM payment p\n"
                + "inner join journalentry je on p.journalEntry=je.id and je.entryDate <=?\n"
                + "inner join jedetail jed on jed.journalentry=je.id and jed.debit='T'\n"
                + "INNER JOIN company on p.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN customer  on customer.id = p.customer \n"
                + "INNER JOIN currency pcurr on p.currency = pcurr.currencyid \n"
                + "INNER join creditnotpayment cnp on cnp.paymentid=p.id and je.entrydate <=? \n"
                + myPaymentMadeSearchJoin
//                + "where p.company = ? and p.deleteflag='F' and p.contraentry='F' and p.isDishonouredCheque='F' and p.paymentWindowType = '2' and p.approvestatuslevel = '11'"
                + whereCaluseForDishonouredMPToCustomer
                + conditionPYCurr
                + myPaymentMadeSearchString  +" \n"
                + customerquery
                + " group by p.paymentnumber \n"               
                + "\n UNION ALL \n"
                + paymentLDPSelectStatement + "\n"
                + " FROM payment p\n"
                + "inner join journalentry je on p.journalEntry=je.id and je.entryDate <=?\n"
                + "inner join jedetail jed on jed.journalentry=je.id and jed.debit='T'\n"
                + "INNER JOIN company on p.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN customer  on customer.id = p.customer \n"
                + "INNER JOIN currency pcurr on p.currency = pcurr.currencyid \n"
                + "inner join linkdetailpayment ldp on ldp.payment=p.id and ldp.company=p.company and ldp.paymentlinkdate <=?\n"
                + myPaymentMadeSearchJoin
//                + "where p.company = ? and p.deleteflag='F' and p.contraentry='F' and p.isDishonouredCheque='F' and p.paymentWindowType = '2' and p.approvestatuslevel = '11'"
                + whereCaluseForDishonouredMPToCustomer
                + conditionPYCurr
                + myPaymentMadeSearchString  +" \n"
                + customerquery
                + " group by p.paymentnumber \n"               
                + "\n UNION ALL \n"
                + paymentLDAPSelectStatement + "\n"
                + " FROM payment p\n"
                + "inner join journalentry je on p.journalEntry=je.id and je.entryDate <=?\n"
                + "inner join jedetail jed on jed.journalentry=je.id and jed.debit='T'\n"
                + "INNER JOIN company on p.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN customer  on customer.id = p.customer \n"
                + "INNER JOIN currency pcurr on p.currency = pcurr.currencyid \n"
                + "inner join linkdetailpaymenttoadvancepayment ldap on ldap.payment=p.id and ldap.company=p.company and ldap.paymentlinkdate <=?\n"
                + myPaymentMadeSearchJoin
//                + "where p.company = ? and p.deleteflag='F' and p.contraentry='F' and p.isDishonouredCheque='F' and p.paymentWindowType = '2' and p.approvestatuslevel = '11'"
                + whereCaluseForDishonouredMPToCustomer
                + conditionPYCurr
                + myPaymentMadeSearchString  +"\n"
                + customerquery
                + " group by p.paymentnumber \n"               
                + "\n UNION ALL \n"
                + paymentLDPCNSelectStatement + "\n"
                + " FROM payment p\n"
                + "inner join journalentry je on p.journalEntry=je.id and je.entryDate <=?\n"
                + "inner join jedetail jed on jed.journalentry=je.id and jed.debit='T'\n"
                + "INNER JOIN company on p.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN customer  on customer.id = p.customer \n"
                + "INNER JOIN currency pcurr on p.currency = pcurr.currencyid \n"
                + "inner join linkdetailpaymenttocreditnote ldpcn on ldpcn.payment=p.id and ldpcn.company=p.company and ldpcn.paymentlinkdate <=?\n"
                + myPaymentMadeSearchJoin
//                + "where p.company = ? and p.deleteflag='F' and p.contraentry='F' and p.isDishonouredCheque='F' and p.paymentWindowType = '2' and p.approvestatuslevel = '11'"
                + whereCaluseForDishonouredMPToCustomer
                + conditionPYCurr
                + myPaymentMadeSearchString  +"\n"
                + customerquery
                + "\n UNION ALL \n"
                + paymentPOSelectStatement + "\n"
                + " FROM payment p\n"
                + "inner join journalentry je on p.journalEntry=je.id and je.entryDate <=?\n"
                + "INNER JOIN customer  on customer.id = p.customer \n"
//                + "inner join jedetail jed on jed.journalentry=je.id and jed.debit='T' and customer.account = jed.account\n"
                + "inner join jedetail jed on jed.journalentry=je.id and jed.debit='T'\n"
                + "INNER JOIN company on p.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency pcurr on p.currency = pcurr.currencyid \n"
                + "inner join paymentdetailotherwise po on po.payment=p.id \n"
                + myPaymentMadeSearchJoin
//                + "where p.company = ? and p.deleteflag='F' and p.contraentry='F' and p.isDishonouredCheque='F' and p.paymentWindowType = '2' and p.approvestatuslevel = '11'"
                + whereCaluseForDishonouredMPToCustomer
                + conditionPYCurr
                + myPaymentMadeSearchString  +"\n"
                + customerquery
                + " group by p.paymentnumber \n"
                + "\n UNION ALL \n"
                + paymentSRSelectStatement + "\n"
                + " FROM payment p\n"
                + "inner join journalentry je on p.journalEntry=je.id and je.entryDate <=?\n"
                + "INNER JOIN customer  on customer.id = p.customer \n"
//                + "inner join jedetail jed on jed.journalentry=je.id and jed.debit='T' and customer.account = jed.account\n"
                + "inner join jedetail jed on jed.journalentry=je.id and jed.debit='T'\n"
                + "INNER JOIN company on p.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency pcurr on p.currency = pcurr.currencyid \n"
                + "inner join salesreturn sr on sr.id=p.salesreturn \n"
                + myPaymentMadeSearchJoin
//                + "where p.company = ? and p.deleteflag='F' and p.contraentry='F' and p.isDishonouredCheque='F' and p.paymentWindowType = '2' and p.approvestatuslevel = '11'"
                + whereCaluseForDishonouredMPToCustomer +" and p.creationdate <= ?"
                + conditionPYCurr
                + myPaymentMadeSearchString  +"\n"
                + customerquery
                + " group by p.paymentnumber \n"
                + selectForDishonouredMPToCustomer
                + ") py group by py.jeentryno"  //payment
                +"\n UNION ALL \n"
                + receiptMainSelectStatement +"\n"
                + "from ("
                + receiptSelectStatement + "\n"  //Main Query for Receipt
                + " FROM receipt r \n"
                + "inner join journalentry je on r.journalentry = je.id and entryDate <=?\n"
                + "INNER JOIN customer  on customer.id = r.customer \n"
//                + "inner join jedetail jed on jed.journalentry = je.id and jed.debit='F' and jed.account = customer.account\n"
                + "inner join jedetail jed on jed.journalentry = je.id and jed.debit='F' \n"
                + "inner join account acc on jed.account = acc.id\n"
                + receiptADVQuery
                + "INNER JOIN company on r.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency rcurr on r.currency = rcurr.currencyid \n"
                + receiptCheque
                + myReceiptPaymentSearchJoin
//                + "where r.company = ? and r.isopeningbalencereceipt=0 and r.deleteflag='F' and r.contraentry='F' and r.isdishonouredcheque='F' \n"
                + whereCaluseForDishonoured
                + "and r.paymentwindowtype != '3' and r.approvestatuslevel = '11' and acc.usedin like '%"+ Constants.Customer_default_account +"%' \n"
                + conditionRCCurr
                + myReceiptPaymentSearchString  +" \n"
                + customerquery
                + " group by r.receiptnumber \n"               
                +"\n UNION ALL \n"
                + receiptLPSelectStatement + "\n"
                + " FROM receipt r \n"
                + "inner join journalentry je on r.journalentry = je.id and entryDate <=?\n"
                + "inner join jedetail jed on jed.journalentry = je.id and jed.debit='F'\n"
                + receiptADVQuery
                + "INNER JOIN company on r.company = company.companyid \n"
                + "INNER JOIN customer  on customer.id = r.customer \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency rcurr on r.currency = rcurr.currencyid \n"
                + "INNER join linkdetailpaymenttoadvancepayment lp on lp.receipt=r.id and lp.paymentlinkdate <=? and lp.company=r.company\n"
                + receiptCheque
                + myReceiptPaymentSearchJoin
//                + "where r.company = ? and r.isopeningbalencereceipt=0 and r.deleteflag='F' and r.contraentry='F' and r.isdishonouredcheque='F' \n"
                + whereCaluseForDishonoured
                + "and r.paymentwindowtype != '3' and r.approvestatuslevel = '11' "
                + conditionRCCurr
                + myReceiptPaymentSearchString  +" \n"
                + customerquery
                + " group by r.receiptnumber \n"               
                +"\n UNION ALL \n"
                + receiptLDRSelectStatement + "\n" //Below query are to Calculate Knock-off for receipt  
                + " FROM receipt r \n"
                + "INNER JOIN customer  on customer.id = r.customer \n"
                + "inner join journalentry je on r.journalentry = je.id and entryDate <=?\n"
//                + "inner join jedetail jed on jed.journalentry = je.id and jed.debit='F'\n"
                + JEJoinForLDR
                + receiptADVQuery
                + "INNER JOIN company on r.company = company.companyid \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency rcurr on r.currency = rcurr.currencyid \n"
                + "INNER join linkdetailreceipt ldr on ldr.receipt=r.id and ldr.receiptlinkdate<=? and ldr.company=r.company\n"
                + receiptCheque
                + LDRSearchJoin
//                + "where r.company = ? and r.isopeningbalencereceipt=0 and r.deleteflag='F' and r.contraentry='F' and r.isdishonouredcheque='F'  \n"
                + whereCaluseForDishonoured
                + "and r.paymentwindowtype != '3' and r.approvestatuslevel = '11' "
                + conditionRCCurr
                + LDRSearchString  +" \n"
                + customerquery
                + " group by r.receiptnumber \n"               
                +"\n UNION ALL \n"
                + receiptRDSelectStatement + "\n"
                + " FROM receipt r \n"
                + "inner join journalentry je on r.journalentry = je.id and entryDate <=?\n"
                + "inner join jedetail jed on jed.journalentry = je.id and jed.debit='F'\n"
                + receiptADVQuery
                + "INNER JOIN company on r.company = company.companyid \n"
                + "INNER JOIN customer  on customer.id = r.customer \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency rcurr on r.currency = rcurr.currencyid \n"
                + "INNER join linkdetailreceipttodebitnote rd on rd.receipt=r.id and rd.receiptlinkdate<=? and rd.company=r.company\n"
                + receiptCheque
                + myReceiptPaymentSearchJoin
//                + "where r.company = ? and r.isopeningbalencereceipt=0 and r.deleteflag='F' and r.contraentry='F' and r.isdishonouredcheque='F' \n"
                + whereCaluseForDishonoured
                + "and r.paymentwindowtype != '3' and r.approvestatuslevel = '11' "
                + conditionRCCurr
                + myReceiptPaymentSearchString  +" \n"
                + customerquery
                + " group by r.receiptnumber \n"               
                +"\n UNION ALL \n"
                + receiptRDPSelectStatement + "\n"
                + " FROM receipt r \n"
                + "inner join journalentry je on r.journalentry = je.id and entryDate <=?\n"
                + "inner join jedetail jed on jed.journalentry = je.id and jed.debit='F'\n"
                + receiptADVQuery
                + "INNER JOIN company on r.company = company.companyid \n"
                + "INNER JOIN customer  on customer.id = r.customer \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency rcurr on r.currency = rcurr.currencyid \n"
                + "INNER join linkdetailreceipttoadvancepayment rdp on rdp.paymentid=r.id and rdp.company=r.company and rdp.receiptlinkdate <=?\n"
                + receiptCheque
                + myReceiptPaymentSearchJoin
//                + "where r.company = ? and r.isopeningbalencereceipt=0 and r.deleteflag='F' and r.contraentry='F' and r.isdishonouredcheque='F' \n"
                + whereCaluseForDishonoured
                + "and r.paymentwindowtype != '3' and r.approvestatuslevel = '11' "
                + conditionRCCurr
                + myReceiptPaymentSearchString  +" \n"
                + customerquery
                + " group by r.receiptnumber \n"               
                +"\n UNION ALL \n"
                + receiptADVSelectStatement + "\n"
                + " FROM receipt r \n"
                + "inner join journalentry je on r.journalentry = je.id and entryDate <=?\n"
                + "inner join jedetail jed on jed.journalentry = je.id and jed.debit='F'\n"
                + receiptADVQuery
                + "INNER JOIN company on r.company = company.companyid \n"
                + "INNER JOIN customer  on customer.id = r.customer \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency rcurr on r.currency = rcurr.currencyid \n"
                + "INNER join advancedetail adv on receiptadvancedetail=rad.id\n"
                + receiptCheque
                + myReceiptPaymentSearchJoin
//                + "where r.company = ? and r.isopeningbalencereceipt=0 and r.deleteflag='F' and r.contraentry='F' and r.isdishonouredcheque='F' \n"
                + whereCaluseForDishonoured
                + "and r.paymentwindowtype != '3' and r.approvestatuslevel = '11' "
                + conditionRCCurr
                + myReceiptPaymentSearchString  +" \n"
                + customerquery
                + " group by r.receiptnumber \n"               
                +"\n UNION ALL \n"
                + receiptRWOSelectStatement + "\n"
                + " FROM receipt r \n"
                + "inner join journalentry je on r.journalentry = je.id and entryDate <=?\n"
                + "inner join jedetail jed on jed.journalentry = je.id and jed.debit='F'\n"
                + receiptADVQuery
                + "INNER JOIN company on r.company = company.companyid \n"
                + "INNER JOIN customer  on customer.id = r.customer \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency rcurr on r.currency = rcurr.currencyid \n"
                + "INNER join receiptwriteoff rwo on rwo.receipt=r.id and rwo.writeoffdate<=? and rwo.company=r.company and rwo.isrecovered='F'\n"
                + receiptCheque
                + myReceiptPaymentSearchJoin
//                + "where r.company = ? and r.isopeningbalencereceipt=0 and r.deleteflag='F' and r.contraentry='F' and r.isdishonouredcheque='F' \n"
                + whereCaluseForDishonoured
                + "and r.paymentwindowtype != '3' and r.approvestatuslevel = '11' "
                + conditionRCCurr
                + myReceiptPaymentSearchString  +" \n"
                + customerquery
                + " group by r.receiptnumber \n"
                + selectForDishonoured
                + ") rc group by rc.jeentryno"  //Receipt
                +"\n UNION ALL \n"
                + receiptOpnMainSelectStatement +"\n"
                + "from ("
                + receiptOpnSelectStatement + "\n"
                + " FROM receipt r \n"
                + "INNER JOIN company on r.company = company.companyid \n"
                + "INNER JOIN customer  on customer.id = r.customer \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency rcurr on r.currency = rcurr.currencyid \n"
                + receiptCheque + "\n"
                + myOpnReceiptPaymentSearchJoin + "\n"
                + "where r.isopeningbalencereceipt='1' AND r.deleteflag='F' AND r.company = ? AND r.isdishonouredcheque='F' "
                + conditionRCCurr
                + myOpnReceiptPaymentSearchString  +" \n"
                + customerquery
                + " group by r.receiptnumber \n"               
                +"\n UNION ALL \n"
                + receiptOpnLPSelectStatement + "\n"
                + " FROM receipt r \n"
                + "INNER JOIN company on r.company = company.companyid \n"
                + "INNER JOIN customer  on customer.id = r.customer \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency rcurr on r.currency = rcurr.currencyid \n"
                + "INNER join linkdetailpaymenttoadvancepayment lp on lp.receipt=r.id and lp.paymentlinkdate <=? and lp.company=r.company\n"            
                + receiptCheque
                + "inner join receiptadvancedetail rad on rad.receipt=r.id\n"
                + myOpnReceiptPaymentSearchJoin
                + "where r.isopeningbalencereceipt='1' AND r.deleteflag='F' AND r.company = ? AND r.isdishonouredcheque='F' "
                + conditionRCCurr
                + myOpnReceiptPaymentSearchString  +" \n"
                + customerquery
                + " group by r.receiptnumber \n"               
                +"\n UNION ALL \n"
                + receiptOpnLDRSelectStatement + "\n"
                + " FROM receipt r \n"
                + "INNER JOIN company on r.company = company.companyid \n"
                + "INNER JOIN customer  on customer.id = r.customer \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency rcurr on r.currency = rcurr.currencyid \n"
                + "Inner join linkdetailreceipt ldr on ldr.receipt=r.id and ldr.company=r.company and ldr.receiptLinkDate<=? \n"
                + receiptCheque
                + "inner join receiptadvancedetail rad on rad.receipt=r.id\n"
                + myOpnReceiptPaymentSearchJoin
                + "where r.isopeningbalencereceipt='1' AND r.deleteflag='F' AND r.company = ? AND r.isdishonouredcheque='F' "
                + conditionRCCurr
                + myOpnReceiptPaymentSearchString  +" \n"
                + customerquery
                + " group by r.receiptnumber \n"               
                +"\n UNION ALL \n"
                + receiptOpnRDSelectStatement + "\n"
                + " FROM receipt r \n"
                + "INNER JOIN company on r.company = company.companyid \n"
                + "INNER JOIN customer  on customer.id = r.customer \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency rcurr on r.currency = rcurr.currencyid \n"
                + "INNER join linkdetailreceipttodebitnote rd on rd.receipt=r.id and rd.company=r.company and rd.receiptlinkdate<=? \n"
                + receiptCheque
                + "inner join receiptadvancedetail rad on rad.receipt=r.id\n"
                + myOpnReceiptPaymentSearchJoin
                + "where r.isopeningbalencereceipt='1' AND r.deleteflag='F' AND r.company = ? AND r.isdishonouredcheque='F' "
                + conditionRCCurr
                + myOpnReceiptPaymentSearchString  +" \n"
                + customerquery
                + " group by r.receiptnumber \n"               
                +"\n UNION ALL \n"
                + receiptOpnRDPSelectStatement + "\n"
                + " FROM receipt r \n"
                + "INNER JOIN company on r.company = company.companyid \n"
                + "INNER JOIN customer  on customer.id = r.customer \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency rcurr on r.currency = rcurr.currencyid \n"
                + "INNER join linkdetailreceipttoadvancepayment rdp on rdp.paymentid=r.id and rdp.company=r.company and rdp.receiptlinkdate <=?\n"
                + receiptCheque
                + "inner join receiptadvancedetail rad on rad.receipt=r.id\n"
                + myOpnReceiptPaymentSearchJoin
                + "where r.isopeningbalencereceipt='1' AND r.deleteflag='F' AND r.company = ? AND r.isdishonouredcheque='F' "
                + conditionRCCurr
                + myOpnReceiptPaymentSearchString  +" \n"
                + customerquery
                + " group by r.receiptnumber \n"               
                +"\n UNION ALL \n"
                + receiptOpnADVSelectStatement + "\n"
                + " FROM receipt r \n"
                + "INNER JOIN company on r.company = company.companyid \n"
                + "INNER JOIN customer  on customer.id = r.customer \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency rcurr on r.currency = rcurr.currencyid \n"
                + "inner join receiptadvancedetail rad on rad.receipt=r.id\n"
                + "INNER join advancedetail adv on receiptadvancedetail=rad.id\n"
                + receiptCheque + "\n"
                + myOpnReceiptPaymentSearchJoin
                + " where r.isopeningbalencereceipt='1' AND r.deleteflag='F' AND r.company = ? AND r.isdishonouredcheque='F' "
                + conditionRCCurr
                + myOpnReceiptPaymentSearchString  +" \n"
                + customerquery
                + " group by r.receiptnumber \n"               
                +"\n UNION ALL \n"
                + receiptOpnRWOSelectStatement + "\n"
                + " FROM receipt r \n"
                + "INNER JOIN company on r.company = company.companyid \n"
                + "INNER JOIN customer  on customer.id = r.customer \n"
                + "INNER JOIN currency compcurr on company.currency = compcurr.currencyid \n"
                + "INNER JOIN currency rcurr on r.currency = rcurr.currencyid \n"
                + "INNER join receiptwriteoff rwo on rwo.receipt=r.id and rwo.isrecovered=0 and rwo.company=r.company and rwo.writeoffdate<=? \n"
                + receiptCheque
                + "inner join receiptadvancedetail rad on rad.receipt=r.id\n"
                + myOpnReceiptPaymentSearchJoin
                + "where r.isopeningbalencereceipt='1' AND r.deleteflag='F' AND r.company = ? AND r.isdishonouredcheque='F' "
                + conditionRCCurr
                + myOpnReceiptPaymentSearchString  +" \n"
                + customerquery
                + " group by r.receiptnumber \n"
                + ") rc group by rc.docno"  //Receipt Opn
                + ") a " + deuDateCondition+ orderBy;
                
        List params = new ArrayList();
        //inv
        params.add(companyid);
        params.add(endDate);
        if(isFromAR){
            params.add(asofDate);
        }
        List invoiceAdvSearchParams = new ArrayList();
        if (advanceSearch) {
            try {
                StringUtil.insertParamAdvanceSearchString1((ArrayList)invoiceAdvSearchParams, invoiceSearchJson);
            } catch (ParseException ex) {
                Logger.getLogger(AccReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            params.addAll(invoiceAdvSearchParams);
        }

        params.add(asofDate);
        params.add(companyid);
        params.add(endDate);
        if(isFromAR){
        params.add(asofDate);
        }
        
        params.addAll(invoiceAdvSearchParams);
        

        params.add(asofDate);
        params.add(companyid);
        params.add(endDate);
        if(isFromAR){
        params.add(asofDate);
        }
        params.addAll(invoiceAdvSearchParams);

        params.add(asofDate);
        params.add(asofDate);
        params.add(companyid);
        params.add(endDate);
        if(isFromAR){
        params.add(asofDate);
        }
        params.addAll(invoiceAdvSearchParams);

        params.add(asofDate);
        params.add(companyid);
        params.add(endDate);
        if(isFromAR){
        params.add(asofDate);
        }
        params.addAll(invoiceAdvSearchParams);

        //inv opn
        params.add(companyid);
        if(isFromAR){
        params.add(asofDate);
        }
        params.addAll(invoiceAdvSearchParams);

        params.add(asofDate);
        params.add(companyid);
        if(isFromAR){
        params.add(asofDate);
        }
        params.addAll(invoiceAdvSearchParams);

        params.add(asofDate);
        params.add(companyid);
        if(isFromAR){
        params.add(asofDate);
        }
        params.addAll(invoiceAdvSearchParams);

        params.add(companyid);
        if(isFromAR){
        params.add(asofDate);
        }
        params.addAll(invoiceAdvSearchParams);

        params.add(asofDate);
        params.add(companyid);
        if(isFromAR){
        params.add(asofDate);
        }
        params.addAll(invoiceAdvSearchParams);
        //cn
        params.add(endDate);
        params.add(companyid);
        List cnAdvSearchParams = new ArrayList();
        if (advanceSearch) {
            try {
                StringUtil.insertParamAdvanceSearchString1((ArrayList) cnAdvSearchParams, cnSearchJson);
            } catch (ParseException ex) {
                Logger.getLogger(AccReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        params.addAll(cnAdvSearchParams);

        params.add(endDate);
        params.add(asofDate);
        params.add(companyid);
        params.addAll(cnAdvSearchParams);

        params.add(endDate);
        params.add(companyid);
        params.addAll(cnAdvSearchParams);

        params.add(endDate);
        params.add(asofDate);
        params.add(companyid);
        params.addAll(cnAdvSearchParams);
        
        // CN FRX
        
        
        
        params.add(endDate);
        params.add(companyid);
        params.addAll(cnAdvSearchParams);
               
        //cn opn
        params.add(companyid);
        params.addAll(cnAdvSearchParams);

        params.add(companyid);
        params.addAll(cnAdvSearchParams);

        params.add(asofDate);
        params.add(companyid);
        params.addAll(cnAdvSearchParams);

        params.add(asofDate);
        params.add(companyid);
        params.addAll(cnAdvSearchParams);
                
        //dn
        params.add(endDate);
        params.add(companyid);
        List dnAdvSearchParams = new ArrayList();
        if (advanceSearch) {
            try {
                StringUtil.insertParamAdvanceSearchString1((ArrayList) dnAdvSearchParams, dnSearchJson);
            } catch (ParseException ex) {
                Logger.getLogger(AccReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        params.addAll(dnAdvSearchParams);

        params.add(endDate);
        params.add(asofDate);
        params.add(companyid);
        params.addAll(dnAdvSearchParams);

        params.add(endDate);
        params.add(companyid);
        params.addAll(dnAdvSearchParams);

        params.add(endDate);
        params.add(asofDate);
        params.add(companyid);
        params.addAll(dnAdvSearchParams);

        //dn opn
        params.add(companyid);
        params.addAll(dnAdvSearchParams);

        params.add(companyid);
        params.addAll(dnAdvSearchParams);

        params.add(companyid);
        params.addAll(dnAdvSearchParams);

        params.add(asofDate);
        params.add(companyid);
        params.addAll(dnAdvSearchParams);

        //py
        params.add(endDate);
        params.add(companyid);
        List pyAdvSearchParams = new ArrayList();
        if (advanceSearch) {
            try {
                StringUtil.insertParamAdvanceSearchString1((ArrayList) pyAdvSearchParams, makePaymentSearchJson);
            } catch (ParseException ex) {
                Logger.getLogger(AccReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        params.addAll(pyAdvSearchParams);

        params.add(endDate);
        params.add(endDate);
        params.add(companyid);
        params.addAll(pyAdvSearchParams);

        params.add(endDate);
        params.add(asofDate);
        params.add(companyid);
        params.addAll(pyAdvSearchParams);

        params.add(endDate);
        params.add(asofDate);
        params.add(companyid);
        params.addAll(pyAdvSearchParams);
        
        params.add(endDate);
        params.add(asofDate);
        params.add(companyid);
        params.addAll(pyAdvSearchParams);
        
        params.add(endDate);        
        params.add(companyid);
        params.add(asofDate);
        params.addAll(pyAdvSearchParams);

        params.add(endDate);
        params.add(companyid);
        params.addAll(pyAdvSearchParams);
        /**
         * if the call is not from aged receivable the we have to display dishonoured payment and reverse JE of dishonoured payments.ERM-744
     *   */
        if (!showDishonouredPayments) {  
            params.add(endDate);
            params.add(companyid);
            params.addAll(pyAdvSearchParams);
        }
        //rc
        params.add(endDate);
        params.add(companyid);
        List rtAdvSearchParams = new ArrayList();
        if (advanceSearch) {
            try {
                StringUtil.insertParamAdvanceSearchString1((ArrayList) rtAdvSearchParams, receiptSearchJson);
            } catch (ParseException ex) {
                Logger.getLogger(AccReportsImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        params.addAll(rtAdvSearchParams);

        params.add(endDate);
        params.add(asofDate);
        params.add(companyid);
        params.addAll(rtAdvSearchParams);

        params.add(endDate);
        params.add(asofDate);
        params.add(companyid);
        if (advanceSearch && !isOutstanding) {
            params.addAll(rtAdvSearchParams);
        }

        params.add(endDate);
        params.add(asofDate);
        params.add(companyid);
        params.addAll(rtAdvSearchParams);

        params.add(endDate);
        params.add(asofDate);
        params.add(companyid);
        params.addAll(rtAdvSearchParams);

        params.add(endDate);
        params.add(companyid);
        params.addAll(rtAdvSearchParams);
        
        params.add(endDate);
        params.add(asofDate);
        params.add(companyid);
        params.addAll(rtAdvSearchParams);
        /**
         * if the call is not from aged receivable the we have to display dishonoured payment and reverse JE of dishonoured payments.ERM-744
         */
        if (!showDishonouredPayments) {            //ERM-744
            params.add(asofDate);
            params.add(companyid);
            params.addAll(rtAdvSearchParams);
        }

        
        
        //rc opn
        params.add(companyid);
        params.addAll(rtAdvSearchParams);

        params.add(asofDate);
        params.add(companyid);
        params.addAll(rtAdvSearchParams);

        params.add(asofDate);
        params.add(companyid);
        params.addAll(rtAdvSearchParams);

        params.add(asofDate);
        params.add(companyid);
        params.addAll(rtAdvSearchParams);

        params.add(asofDate);
        params.add(companyid);
        params.addAll(rtAdvSearchParams);

        params.add(companyid);
        params.addAll(rtAdvSearchParams);

        params.add(asofDate);
        params.add(companyid);
        params.addAll(rtAdvSearchParams);
             
        
        
//        Add Only in case of AR.
        if(isFromAR && datefilter == 0){
            params.add(asofDate);
        }
                
        List list = executeSQLQuery(query, params.toArray());

        return list;
    }
    
    private JSONObject getCustomSearchString(JSONObject request, int moduleid, boolean isOpeningTransaction) throws JSONException {
        String searchDefaultFieldSQL = "";
        String mySearchFilterString = "";
        String searchString = getSearchStringByModuleID(request, moduleid);

        String filterConjuctionCriteria = request.get(Constants.Filter_Criteria).toString();
        JSONObject serachJobj = new JSONObject(searchString);

        JSONArray customSearchFieldArray = new JSONArray();
        JSONArray defaultSearchFieldArray = new JSONArray();
        StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);

        String joinString = "";
        
        if (customSearchFieldArray.length() > 0) {

            HashMap<String, Object> requestParam = new HashMap<>();
            requestParam.put("Searchjson", searchString);
            requestParam.put("appendCase", "and");
            requestParam.put(Constants.moduleid, moduleid);
            requestParam.put("filterConjuctionCriteria", filterConjuctionCriteria);

            if (isOpeningTransaction) {
                requestParam.put("isOpeningBalance", true);
            }

            mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(requestParam, true).get(Constants.myResult));
            if (isOpeningTransaction) {
                if (mySearchFilterString.contains("OpeningBalanceVendorInvoiceCustomData") || mySearchFilterString.contains("openingbalancevendorinvoicecustomdata")
                        || mySearchFilterString.contains("OpeningBalanceInvoiceCustomData") || mySearchFilterString.contains("openingbalanceinvoicecustomdata")
                        || mySearchFilterString.contains("OpeningBalanceMakePaymentCustomData") || mySearchFilterString.contains("openingbalancemakepaymentcustomdata")
                        || mySearchFilterString.contains("OpeningBalanceReceiptCustomData") || mySearchFilterString.contains("openingbalancereceiptcustomdata")
                        || mySearchFilterString.contains("OpeningBalanceDebitNoteCustomData") || mySearchFilterString.contains("openingbalancedebitnotecustomdata")
                        || mySearchFilterString.contains("OpeningBalanceCreditNoteCustomData") || mySearchFilterString.contains("openingbalancecreditnotecustomdata")
                        || mySearchFilterString.contains("AccJEDetailCustomData") || mySearchFilterString.contains("AccJEDetailsProductCustomData") || mySearchFilterString.contains("accjecustomdata")) {
                    joinString += getOBCustomDataJoinStringByModuleID(moduleid);
                    mySearchFilterString = getOBReplacedMySearchStringByModuleID(mySearchFilterString, moduleid);
                }
            } else {
                if (mySearchFilterString.contains("accjecustomdata") || mySearchFilterString.contains("AccJECustomData")) {
                    joinString += getAccJECustomDataJoinStringByModuleID(moduleid);
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "accjecustomdata");//    
                }
                if (mySearchFilterString.contains("AccJEDetailCustomData")) {
                    joinString += getAccJAccJEDetailCustomDataJoinStringByModuleID(moduleid);
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "accjedetailcustomdata");//    
                }
            }
            if (mySearchFilterString.contains("VendorCustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("VendorCustomData", "vendorcustomdata");
                joinString += " left join vendorcustomdata  on vendorcustomdata.vendorId=v.id ";
            }
            if (mySearchFilterString.contains("CustomerCustomData")) {
                mySearchFilterString = mySearchFilterString.replaceAll("CustomerCustomData", "customercustomdata");
                joinString += " left join customercustomdata  on customercustomdata.customerId=cust.id ";
            }
        }
        if (defaultSearchFieldArray.length() > 0) {
            request.put("filterConjuctionCriteria", filterConjuctionCriteria);
            for (int arrayItr = 0; arrayItr < defaultSearchFieldArray.length(); arrayItr++) {
                JSONObject jObj = defaultSearchFieldArray.getJSONObject(arrayItr);
                if (jObj.optBoolean("isRangeSearchField", false)) {
                    String headerTableName = "";
                    String headercolumnName = "";
                    String xtype = "";
                    try {
                        String fieldId = jObj.getString("column");
                        String query = "from DefaultHeader where id=?";
                        List<DefaultHeader> headerlist = executeQuery(query, fieldId);
                        if (headerlist.size() > 0) {
                            DefaultHeader header = headerlist.get(0);
                            xtype = header.getXtype();
                            headerTableName = header.getDbTableName();
                            headercolumnName = header.getDbcolumnname();
                        }
                        if (StringUtil.isNullOrEmptyWithTrim(searchDefaultFieldSQL)) {
                            searchDefaultFieldSQL += " AND (" + headerTableName + "." + headercolumnName + " ";
                        } else {
                            searchDefaultFieldSQL += filterConjuctionCriteria + " " + headerTableName + "." + headercolumnName;
                        }
                        searchDefaultFieldSQL += StringUtil.getStringRangeFilterForAdvanceSearch(jObj, filterConjuctionCriteria) + " ";

                    } catch (Exception ex) {
                        Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if (!StringUtil.isNullOrEmptyWithTrim(searchDefaultFieldSQL)) {
                searchDefaultFieldSQL += ") ";
            }
        }
        
        mySearchFilterString = " "+StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
        JSONObject returnJOBJ = new JSONObject();
        returnJOBJ.put("customjoin", joinString);
        returnJOBJ.put("mySearchFilterString", mySearchFilterString);
        return returnJOBJ;
    }
    
    
    private String getSearchStringByModuleID(JSONObject paramObj, int moduleID) {
        String searchString = "";
            switch (moduleID) {
                case Constants.Acc_Vendor_Invoice_ModuleId:
                    searchString = paramObj.optString("invoiceSearchJson");
                    break;
                case Constants.Acc_Invoice_ModuleId:
                    searchString = paramObj.optString("invoiceSearchJson");
                    break;
                case Constants.Acc_Make_Payment_ModuleId:
                    searchString = paramObj.optString("makePaymentSearchJson");
                    break;
                case Constants.Acc_Receive_Payment_ModuleId:
                    searchString = paramObj.optString("receiptSearchJson");
                    break;
                case Constants.Acc_Debit_Note_ModuleId:
                    searchString = paramObj.optString("dnSearchJson");
                    break;
                case Constants.Acc_Credit_Note_ModuleId:
                    searchString = paramObj.optString("cnSearchJson");
                    break;
            }
        
        return searchString;
    }
    
    private String getOBCustomDataJoinStringByModuleID(int moduleID) {
        String joinString = "";
        switch (moduleID){
            case Constants.Acc_Invoice_ModuleId :
                joinString = " INNER JOIN openingbalanceinvoicecustomdata on openingbalanceinvoicecustomdata.openingbalanceinvoiceid=invoice.id ";          
                break;
            case Constants.Acc_Make_Payment_ModuleId :
                joinString = " INNER JOIN openingbalancemakepaymentcustomdata on openingbalancemakepaymentcustomdata.openingbalancemakepaymentid=p.id ";    
                break;
            case Constants.Acc_Receive_Payment_ModuleId :
                joinString = " INNER JOIN openingbalancereceiptcustomdata on openingbalancereceiptcustomdata.openingbalancereceiptid=r.id ";    
                break;
            case Constants.Acc_Debit_Note_ModuleId :
                joinString = " INNER JOIN openingbalancedebitnotecustomdata on openingbalancedebitnotecustomdata.openingbalancedebitnoteid=dn.id ";
                break;
            case Constants.Acc_Credit_Note_ModuleId :
                joinString = " INNER JOIN openingbalancecreditnotecustomdata on openingbalancecreditnotecustomdata.openingbalancecreditnoteid=cn.id ";
                break;
        }
        
        return joinString;
    }
    
    private String getOBReplacedMySearchStringByModuleID(String mySearchFilterString, int moduleID) {
        switch (moduleID){
            case Constants.Acc_Vendor_Invoice_ModuleId :
                
                mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceVendorInvoiceCustomData", "openingbalancevendorinvoicecustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "openingbalancevendorinvoicecustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "openingbalancevendorinvoicecustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "openingbalancevendorinvoicecustomdata");
                
                break;
            case Constants.Acc_Invoice_ModuleId :
         
                mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceInvoiceCustomData", "openingbalanceinvoicecustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "openingbalanceinvoicecustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "openingbalanceinvoicecustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "openingbalanceinvoicecustomdata");
                break;
            case Constants.Acc_Make_Payment_ModuleId :
                
                mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceMakePaymentCustomData", "openingbalancemakepaymentcustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "openingbalancemakepaymentcustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "openingbalancemakepaymentcustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "openingbalancemakepaymentcustomdata");
                break;
            case Constants.Acc_Receive_Payment_ModuleId :
        
                mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceReceiptCustomData", "openingbalancereceiptcustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "openingbalancereceiptcustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "openingbalancereceiptcustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "openingbalancereceiptcustomdata");
                break;
            case Constants.Acc_Debit_Note_ModuleId :
                
                mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceDebitNoteCustomData", "openingbalancedebitnotecustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "openingbalancedebitnotecustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "openingbalancedebitnotecustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "openingbalancedebitnotecustomdata");    
                break;
            case Constants.Acc_Credit_Note_ModuleId :
                
                mySearchFilterString = mySearchFilterString.replaceAll("OpeningBalanceCreditNoteCustomData", "openingbalancecreditnotecustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "openingbalancecreditnotecustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "openingbalancecreditnotecustomdata");
                mySearchFilterString = mySearchFilterString.replaceAll("accjecustomdata", "openingbalancecreditnotecustomdata");
                break;
        }
        
        
        
        
        return mySearchFilterString;
    }
    
    private String getAccJECustomDataJoinStringByModuleID(int moduleID) {
        String joinString = "";
        switch (moduleID){
            case Constants.Acc_Invoice_ModuleId :
                joinString = " INNER JOIN accjecustomdata on accjecustomdata.journalentryId=invoice.journalentry ";
                break;
            case Constants.Acc_Make_Payment_ModuleId :
                joinString = " INNER JOIN accjecustomdata on accjecustomdata.journalentryId=p.journalentry ";
                break;
            case Constants.Acc_Receive_Payment_ModuleId :
                joinString = " INNER JOIN accjecustomdata on accjecustomdata.journalentryId=r.journalentry ";
                break;
            case Constants.Acc_Debit_Note_ModuleId :
                joinString = " INNER JOIN accjecustomdata on accjecustomdata.journalentryId=dn.journalentry ";
                break;
            case Constants.Acc_Credit_Note_ModuleId :
                joinString = " INNER JOIN accjecustomdata on accjecustomdata.journalentryId=cn.journalentry ";    
                break;
        }
        
        return joinString;
    }

    private String getAccJAccJEDetailCustomDataJoinStringByModuleID(int moduleID) {
        String joinString = "";
        if (moduleID == Constants.Acc_Invoice_ModuleId) {
            joinString = " INNER JOIN invoicedetails invd on invd.invoice =invoice.id"
                    + " LEFT JOIN accjedetailcustomdata  on accjedetailcustomdata.recdetailId=invd.id ";
        } else {
            joinString = " LEFT JOIN accjedetailcustomdata  on accjedetailcustomdata.jedetailId=jed.id ";
        }
        return joinString;
    }
    
}
