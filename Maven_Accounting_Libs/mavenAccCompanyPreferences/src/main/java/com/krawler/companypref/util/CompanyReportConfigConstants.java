/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.companypref.util;

import com.krawler.common.util.Constants;

/**
 *
 * @author krawler
 */
public class CompanyReportConfigConstants {

    public static final String GL_SELECT_NON_CONFIG_FIELDS = "acc.id as accid, case je.transactionModuleid when "+Constants.Inventory_Stock_Adjustment_ModuleId+" then '' else curr.currencyid end as currid, jed.debit, je.id as jeid, je.isreval, jed.account, je.ismulticurrencypaymentje, je.typevalue, jed.id as jedid, acc.currency as accountcurrency, acccurr.currencyid as acccurrencyid, je.transactionModuleid,je.entrydate,je.entryno";
    public static final String GL_ACCOUNT_ID = "acc.id as accid";
    public static final String GL_ACCOUNT_INFO = "fetchaccounts_diffdebit(acc.id,je.id,jed.id) as accinfo";
    public static final String GL_CURR_CODE = "curr.currencycode";
    public static final String GL_CURR_ID = "curr.currencyid";
    public static final String GL_CURR_SYMBOL = "curr.symbol";
    public static final String GL_COSTCENTER_NAME = "cc.name as costcentername";
    public static final String GL_INV_PERSON_NAME = "cust.name as personname";
    public static final String GL_GR_PERSON_NAME = "vend.name as personname";
    public static final String GL_CN_PERSON_NAME = "if(cn.cntype='4',vend.name, cust.name) as personname";
    public static final String GL_DN_PERSON_NAME = "if(dn.dntype='4',cust.name, vend.name) as personname";
    public static final String GL_RECEIPT_PERSON_NAME = "if(cust.id is not null,cust.name,vend.name) as personname";
    public static final String GL_PAYMENT_PERSON_NAME = "if(cust.id is not null,cust.name,vend.name) as personname";
    public static final String GL_OTHERS_PERSON_NAME = "if(vend.id=jed.customervendorid,vend.name, cust.name) as personname";
    public static final String GL_INV_PERSON_CODE = "cust.acccode as personcode";
    public static final String GL_GR_PERSON_CODE = "vend.acccode as personcode";
    public static final String GL_CN_PERSON_CODE = "if(cn.cntype='4',vend.acccode, cust.acccode) as personcode";
    public static final String GL_DN_PERSON_CODE = "if(dn.dntype='4',cust.acccode, vend.acccode) as personcode";
    public static final String GL_RECEIPT_PERSON_CODE = "if(cust.id is not null,cust.acccode,vend.acccode) as personcode";
    public static final String GL_PAYMENT_PERSON_CODE = "if(cust.id is not null,cust.acccode,vend.acccode) as personcode";
    public static final String GL_OTHERS_PERSON_CODE = "if(vend.id=jed.customervendorid,vend.acccode, cust.acccode) as personcode";
    public static final String GL_EXCHANGE_RATE = "if(curr.symbol=compcurr.symbol,'1',1/je.externalcurrencyrate)";
    public static final String GL_IS_DEBIT = "jed.debit";
    public static final String GL_C_AMOUNT = "if(jed.debit = 'F',jed.amount,0)";
    public static final String GL_C_AMOUNT_IN_BASE = "if(jed.debit = 'F',jed.amountinbase,0)";
    public static final String GL_D_AMOUNT = "if(jed.debit = 'T',jed.amount,0)";
    public static final String GL_D_AMOUNT_IN_BASE = "if(jed.debit = 'T',jed.amountinbase,0)";
    public static final String GL_ENTRY_DATE = "je.entrydate as jedate";
    public static final String GL_JE_ID = "je.id as jeid";
    public static final String GL_INV_REF_NO = "inv.invoicenumber as refno";
    public static final String GL_GR_REF_NO = "gr.grnumber as refno";
    public static final String GL_CN_REF_NO = "cn.cnnumber as refno";
    public static final String GL_DN_REF_NO = "dn.dnnumber as refno";
    public static final String GL_RECEIPT_REF_NO = "rt.receiptnumber as refno";
    public static final String GL_PAYMENT_REF_NO = "pt.paymentnumber as refno";
    public static final String GL_OTHERS_REF_NO = "if((pb.id is null and pbd.id is null),je.entryno,pb.refno) as jeno";
    public static final String GL_CURR_RATE = "je.externalcurrencyrate";
    public static final String GL_INV_REF_ID = "inv.id as refid";
    public static final String GL_GR_REF_ID = "gr.id as refid";
    public static final String GL_CN_REF_ID = "cn.id as refid";
    public static final String GL_DN_REF_ID = "dn.id as refid";
    public static final String GL_RECEIPT_REF_ID = "rt.id as refid";
    public static final String GL_PAYMENT_REF_ID = "pt.id as refid";
    public static final String GL_OTHERS_REF_ID = "je.id as refid";
    public static final String GL_INV_CASH_TRANSACTION = "inv.cashtransaction";
    public static final String GL_GR_CASH_TRANSACTION = "gr.cashtransaction";
    public static final String GL_CN_CASH_TRANSACTION = "null";
    public static final String GL_DN_CASH_TRANSACTION = "null";
    public static final String GL_RECEIPT_CASH_TRANSACTION = "null";
    public static final String GL_PAYMENT_CASH_TRANSACTION = "null";
    public static final String GL_OTHERS_CASH_TRANSACTION = "null";
    public static final String GL_INV_MEMO = "inv.memo as memo";
    public static final String GL_GR_MEMO = "gr.memo as memo";
    public static final String GL_CN_MEMO = "cn.memo as memo";
    public static final String GL_DN_MEMO = "dn.memo as memo";
    public static final String GL_RECEIPT_MEMO = "rt.memo as memo";
    public static final String GL_PAYMENT_MEMO = "pt.memo as memo";
    public static final String GL_OTHERS_MEMO = "je.memo  as memo";
    public static final String GL_ENTRY_NO = "je.entryno as jeentryno";    
    public static final String GL_IS_REVAL = "je.isReval";
    public static final String GL_ACCOUNT = "jed.account";
    public static final String GL_PAYMENT_CURR_RATE = "je.paymentcurrencytopaymentmethodcurrencyrate";
    public static final String GL_MULTI_CURR_PAYMENT = "je.ismulticurrencypaymentje";
    public static final String GL_TYPE_VALUE = "je.typevalue";
    public static final String GL_jed_ID = "jed.id";
    public static final String GL_ACC_CURR = "acc.currency";
    public static final String GL_ACC_CURR_ID = "acccurr.currencyid as acccurrencyid";
    public static final String GL_ACC_CURR_CODE = "acccurr.currencycode as acccurrencycode";
    public static final String GL_ACC_CURR_SYMBOL = "acccurr.symbol as acccurrencysymbol";
    public static final String GL_MODULE_ID = "je.transactionModuleid";
    public static final String GL_CN_DESCRIPTION = " GROUP_CONCAT(distinct CONCAT_WS('', 'Credit Note - ',cn.cnnumber,'<br>',if(cust.id is not null,cust.acccode,vend.acccode),' - ',if(cust.id is not null,cust.name, vend.name),'<br>', cn.memo,'<br>', acc1.acccode, ' - ', acc1.name,'<br>', cnt.description)) ";
    public static final String GL_DN_DESCRIPTION = " GROUP_CONCAT(distinct CONCAT_WS('', 'Debit Note - ',dn.dnnumber,'<br>',if(vend.id is not null,vend.acccode,cust.acccode),' - ',if(vend.id is not null, vend.name,cust.name),'<br>', dn.memo,'<br>', acc1.acccode, ' - ', acc1.name,'<br>', dnt.description)) ";

    public static final String GL_RECEIPTS_DESCRIPTION = "case "
            + "when (rd.totaljedid =jed.id  or (rt.deposittojedetail =jed.id  and rd.id is not null)) then "
            + "group_concat(distinct concat_ws('','Receive Payment','<br>',rt.memo,'<br>', "
            + "if(inv.id is not null,concat('Invoice - ',inv.invoicenumber),concat('Purchase Invoice - ',gr.grnumber)),', ', "
            + "if(cust.id is not null,cust.name,vend.name),'<br>', "
            + "rd.description,'<br>', "
            + "if(cq.id is not null, concat('Cheque no. ',cq.chequeno,' dated ',cq.duedate,', ',pm.methodname,', ',cq.description),''),if(rt.receivedfrom is not null,concat('Received From : ',mi.value),''))) "
            + "when rad.totaljedid = jed.id then "
            + "group_concat(distinct concat_ws('','Receive Payment','<br>',rt.memo,'<br>Advance Payment, ', if(cust.id is not null,cust.name,vend.name),'<br>',rad.description,'<br>',if(cq.id is not null, concat('Cheque no. ',cq.chequeno,' dated ',cq.duedate,', ',pm.methodname,', ',cq.description),''),if(rt.receivedfrom is not null,concat('<br>Received From : ',mi.value),''))) "
            + "when dnp.totaljedid = jed.id  then "
            + "group_concat(distinct concat_ws('','Receive Payment','<br>',rt.memo,'<br>','Debit Note - ',dn.dnnumber,', ', if(cust.id is not null,cust.name,vend.name),'<br>',dnp.description,'<br>',if(cq.id is not null, concat('Cheque no. ',cq.chequeno,' dated ',cq.duedate,', ',pm.methodname,', ',cq.description),''),if(rt.receivedfrom is not null,concat('<br>Received From : ',mi.value),''))) "
            + "when rdo.totaljedid = jed.id then "
            + "group_concat(distinct concat_ws('','Receipt Payment Against GL',if(pm.detailtype='0',', Cash Payment',''),'<br>',rt.memo,'<br>',acc.acccode,' - ',acc.name , case when cust.id is not null "
            + "    then concat(', ',cust.name) "
            + "    when vend.id is not null "
            + "    then concat(', ',vend.name) "
            + "    else '' "
            + "end, '<br>',rdo.description,'<br>',if(cq.id is not null, concat('Cheque no. ',cq.chequeno,' dated ',cq.duedate,', ',pm.methodname,', ',cq.description),''), if(rt.receivedfrom is not null,concat('<br>Received From : ',mi.value),''))) "
            + "else "
            + "group_concat(distinct concat_ws('','Receive Payment','<br>',rt.memo,'<br>',acc.acccode,' - ',acc.name , '<br>',rd.description,rad.description,rdo.description,dnp.description,if(cq.id is not null, concat('Cheque no. ',cq.chequeno,' dated ',cq.duedate,', ',pm.methodname,', ',cq.description),''),if(rt.receivedfrom is not null,concat('<br>Received From : ',mi.value),''))) "
            + "end as gldescription ";
    public static final String GL_PAYMENT_DESCRIPTION = " case "
            + " when (pmd.totaljedid = jed.id or (pt.deposittojedetail = jed.id and pmd.id is not null)) then "
            + " group_concat(distinct concat_ws('','Make Payment','<br>',pt.memo,'<br>', "
            + " if(inv.id is not null,concat('Invoice - ',inv.invoicenumber),concat('Purchase Invoice - ',gr.grnumber)),', ', "
            + " if(cust.id is not null,cust.name,vend.name),'<br>', "
            + " pmd.description,'<br>', "
            + " if(cq.id is not null, concat('Cheque no. ',cq.chequeno,' dated ',cq.duedate,', ',pm.methodname,', ',cq.description),''),if(pt.paidto is not null,concat('<br>Paid to : ',mi.value),''))) "
            + " when pad.totaljedid =jed.id then "
            + " group_concat(distinct concat_ws('','Make Payment','<br>',pt.memo,'<br>Advance Payment, ', if(cust.id is not null,cust.name,vend.name),'<br>',pad.description,'<br>',if(cq.id is not null, concat('Cheque no. ',cq.chequeno,' dated ',cq.duedate,', ',pm.methodname,', ',cq.description),''),if(pt.paidto is not null,concat('<br>Paid to : ',mi.value),''))) "
            + " when cnp.totaljedid =jed.id then "
            + " group_concat(distinct concat_ws('','Make Payment','<br>',pt.memo,'<br>','Debit Note - ',cn.cnnumber,', ', if(cust.id is not null,cust.name,vend.name),'<br>',cnp.description,'<br>',if(cq.id is not null, concat('Cheque no. ',cq.chequeno,' dated ',cq.duedate,', ',pm.methodname,', ',cq.description),''),if(pt.paidto is not null,concat('<br>Paid to : ',mi.value),''))) "
            + " when pdo.totaljedid =jed.id then "
            + " group_concat(distinct concat_ws('','Make Payment Against GL',if(pm.detailtype='0',', Cash Payment',''),'<br>',pt.memo,'<br>',acc.acccode,' - ',acc.name , case when cust.id is not null "
            + "    then concat(', ',cust.name) "
            + "    when vend.id is not null "
            + "    then concat(', ',vend.name) "
            + "    else '' "
            + " end, '<br>', pdo.description,'<br>',if(cq.id is not null, concat('Cheque no. ',cq.chequeno,' dated ',cq.duedate,', ',pm.methodname,', ',cq.description),''), if(pt.paidto is not null,concat('<br>Paid to : ',mi.value),''))) "
            + " else "
            + " group_concat(distinct concat_ws('','Make Payment','<br>',pt.memo,'<br>',acc.acccode,' - ',acc.name , '<br>',pmd.description,pad.description,pdo.description,cnp.description,if(cq.id is not null, concat('Cheque no. ',cq.chequeno,' dated ',cq.duedate,', ',pm.methodname,', ',cq.description),''),if(pt.paidto is not null,concat('<br>Paid to : ',mi.value),''))) "
            + " end as gldescription ";

    public static final String GL_OTHERS_DESCRIPTION = "GROUP_CONCAT(distinct CONCAT_WS('', if(je.typevalue='2','Party Journal Entry','Journal Entry'),'<br>',je.memo,'<br>',acc.acccode, ' - ', acc.name,'<br>',jed.description,'<br>'))";
    public static final String GL_INV_DETAILS_DESCRIPTION = "GROUP_CONCAT(distinct CONCAT_WS('',if(inv.cashtransaction='1','Cash Sales - ',if(inv.isfixedassetleaseinvoice='1','Lease Invoice - ',if(inv.isfixedassetinvoice='1','Fixed Asset Disposal Invoice - ','Sales Invoice - '))),inv.invoicenumber,'<br>',cust.acccode,' - ',cust.name,'<br>',inv.memo,'<br>',p.productid, ' - ', p.name,'<br>',invd.description,'<br>Amount : ',curr.currencycode,' ', format(invd.rowexcludinggstamount,2)))";
    public static final String GL_INV_ROUNDING_DESCRIPTION = "GROUP_CONCAT(distinct CONCAT_WS('', case inv.isfixedassetinvoice when '0' then (if(inv.cashtransaction='1','Cash Sales - ','Sales Invoice - ')) else 'Fixed Asset' end,inv.invoicenumber,'<br>',cust.acccode,' - ',cust.name,'<br>', inv.memo,'<br>', acc.acccode, ' - ', acc.name,'<br>'))";
    public static final String GL_INV_TERMS_DESCRIPTION = "GROUP_CONCAT(distinct CONCAT_WS('',if(inv.cashtransaction='1','Cash Sales - ','Sales Invoice - '),inv.invoicenumber,'<br>',cust.acccode,' - ',cust.name,'<br>',inv.memo))";
    public static final String GL_INV_CAP_DESCRIPTION = "GROUP_CONCAT(distinct CONCAT_WS('',if(inv.cashtransaction='1','Cash Sales - ','Sales Invoice - '),inv.invoicenumber,'<br>',cust.acccode,' - ',cust.name,'<br>',inv.memo))";
    public static final String GL_GR_DETAILS_DESCRIPTION = "substring(GROUP_CONCAT(distinct CONCAT_WS('', case gr.isfixedassetinvoice when '0' then (if(gr.cashtransaction='1','Cash Purchase - ','Purchase Invoice - ')) else 'Fixed Asset' end,gr.grnumber,'<br>',vend.acccode,' - ',vend.name,'<br>',gr.memo,'<br>',p.productid,' - ', p.name,'<br>',grd.description,'<br>Amount : ',curr.currencycode,' ', format(grd.rowexcludinggstamount,2))),1,20000)";
    public static final String GL_GR_EXP_DETAILS_DESCRIPTION = "GROUP_CONCAT(distinct CONCAT_WS('', case gr.isfixedassetinvoice when '0' then (if(gr.cashtransaction='1','Cash Purchase - ','Purchase Invoice - ')) else 'Fixed Asset Acquired Invoice - ' end,gr.grnumber,'<br>',vend.acccode,' - ',vend.name,'<br>', gr.memo,'<br>', acc1.acccode, ' - ', acc1.name,'<br>', grd.description))";
    public static final String GL_GR_ROUNDING_DESCRIPTION = "GROUP_CONCAT(distinct CONCAT_WS('', case gr.isfixedassetinvoice when '0' then (if(gr.cashtransaction='1','Cash Purchase - ','Purchase Invoice - ')) else 'Fixed Asset' end,gr.grnumber,'<br>',vend.acccode,' - ',vend.name,'<br>', gr.memo,'<br>', acc.acccode, ' - ', acc.name,'<br>'))";
    public static final String GL_GR_TERMS_DESCRIPTION = "GROUP_CONCAT(distinct CONCAT_WS('',case gr.isfixedassetinvoice when '0' then (if(gr.cashtransaction='1','Cash Purchase - ','Purchase Invoice - ')) else 'Fixed Asset' end,gr.grnumber,'<br>',vend.acccode,' - ',vend.name,'<br>',gr.memo))";
    public static final String GL_GR_CAP_DESCRIPTION = "GROUP_CONCAT(distinct CONCAT_WS('',case gr.isfixedassetinvoice when '0' then (if(gr.cashtransaction='1','Cash Purchase - ','Purchase Invoice - ')) else 'Fixed Asset' end,gr.grnumber,'<br>',vend.acccode,' - ',vend.name,'<br>',gr.memo))";
//    public static final String GL_CUSTOM_DATA = "GROUP_CONCAT(distinct CONCAT_WS(',',tc.value, tc_dd.value))";
    public static final String GL_INV_DETAILS_line_description = "GROUP_CONCAT(distinct invd.description) as linedescription";
    public static final String GL_INV_CAP_line_description = "'' as linedescription";
    public static final String GL_INV_ROUNDING_line_description = "'' as linedescription";
    public static final String GL_INV_TERMS_line_description = "'' as linedescription";
    public static final String GL_GR_CAP_line_description = "'' as linedescription";
    public static final String GL_GR_DETAILS_line_description = "GROUP_CONCAT(distinct grd.description) as linedescription";
    public static final String GL_GR_EXP_DETAILS_line_description = "GROUP_CONCAT(distinct grd.description) as linedescription";
    public static final String GL_GR_ROUNDING_line_description = "'' as linedescription";
    public static final String GL_GR_TERMS_line_description = "'' as linedescription";
    public static final String GL_CN_line_description = "GROUP_CONCAT(distinct cnt.description) as linedescription";
    public static final String GL_DN_line_description = "GROUP_CONCAT(distinct dnt.description) as linedescription";
    public static final String GL_RECEIPT_line_description = "case jed.id \n"
            + "when rd.totaljedid then\n"
            + "GROUP_CONCAT(distinct rd.description) \n"
            + "when rad.totaljedid then\n"
            + "GROUP_CONCAT(distinct rad.description) \n"
            + "when rdo.totaljedid then\n"
            + "GROUP_CONCAT(distinct rdo.description) \n"
            + "when dnp.totaljedid then\n"
            + "GROUP_CONCAT(distinct dnp.description) \n"
            + "else\n"
            + "GROUP_CONCAT(distinct CONCAT_WS(',',rd.description,rad.description,rdo.description,dnp.description)) end as linedescription";
    public static final String GL_PAYMENT_line_description = "case jed.id \n"
            + "when pmd.totaljedid then\n"
            + "GROUP_CONCAT(distinct pmd.description) \n"
            + "when pad.totaljedid then\n"
            + "GROUP_CONCAT(distinct pad.description) \n"
            + "when pdo.totaljedid then\n"
            + "GROUP_CONCAT(distinct pdo.description) \n"
            + "when cnp.totaljedid then\n"
            + "GROUP_CONCAT(distinct cnp.description) \n"
            + "else\n"
            + "GROUP_CONCAT(distinct CONCAT_WS(',',pmd.description,pad.description,pdo.description,cnp.description)) end as linedescription";
    public static final String GL_OTHERS_line_description = "GROUP_CONCAT(distinct jed.description) as linedescription";
    public static final String GL_INV_DETAILS_GST_CODE = "COALESCE(gtax.taxcode,GROUP_CONCAT(distinct tax.taxcode)) as taxvalue";
    public static final String GL_INV_CAP_GST_CODE = "'' as taxvalue";
    public static final String GL_INV_ROUNDING_GST_CODE = "'' as taxvalue";
    public static final String GL_INV_TERMS_GST_CODE = "'' as taxvalue";
    public static final String GL_GR_CAP_GST_CODE = "'' as taxvalue";
    public static final String GL_GR_DETAILS_GST_CODE = "COALESCE(gtax.taxcode,GROUP_CONCAT(distinct tax.taxcode)) as taxvalue";
    public static final String GL_GR_EXP_DETAILS_GST_CODE = "COALESCE(gtax.taxcode,GROUP_CONCAT(distinct tax.taxcode)) as taxvalue";
    public static final String GL_GR_ROUNDING_GST_CODE = "'' as taxvalue";
    public static final String GL_GR_TERMS_GST_CODE = "'' as taxvalue";
    public static final String GL_CN_GST_CODE = "COALESCE(gtax.taxcode,GROUP_CONCAT(distinct tax.taxcode),GROUP_CONCAT(distinct cndtax.taxcode)) as taxvalue";
    public static final String GL_DN_GST_CODE = "COALESCE(gtax.taxcode,GROUP_CONCAT(distinct tax.taxcode),GROUP_CONCAT(distinct dndtax.taxcode)) as taxvalue";
    public static final String GL_RECEIPT_GST_CODE = "GROUP_CONCAT(distinct tax.taxcode) as taxvalue";
    public static final String GL_PAYMENT_GST_CODE = "GROUP_CONCAT(distinct tax.taxcode) as taxvalue";
    public static final String GL_OTHERS_GST_CODE = "'' as taxvalue";
    public static final String GL_INV_TYPE = "case inv.cashtransaction when '0' then if(inv.isconsignment='T','Consignment Sales Invoice',(if(inv.isfixedassetinvoice='1','Asset Disposal Invoice',(if(inv.isfixedassetleaseinvoice='1','Lease Invoice','Sales Invoice'))))) else 'Cash Sale' end as doctype";
    public static final String GL_GR_TYPE = "case gr.cashtransaction when '0' then if(gr.isconsignment='T','Consignment Purchase Invoice',(if(gr.isfixedassetinvoice='1','Asset Acquired Invoice','Purchase Invoice'))) else 'Cash Purchase' end as doctype";
    public static final String GL_CN_TYPE = "'Credit Note' as doctype";
    public static final String GL_DN_TYPE = "'Debit Note' as doctype";
    public static final String GL_RECEIPT_TYPE = "'Payment Received' as doctype";
    public static final String GL_RECEIPT_WO_TYPE = "if(je.transactionModuleid = " + Constants.Acc_Receipt_WriteOff_ModuleId + ", 'Receipt Write off', 'Reverse Receipt Write off') as doctype";
    public static final String GL_PAYMENT_TYPE = "'Payment Made' as doctype";
    public static final String GL_OTHERS_TYPE = "if((pb.id is null and pbd.id is null),(case when je.typevalue = 1 then 'Normal Journal Entry' when je.typevalue = 2 then 'Party Journal Entry' when je.typevalue = 3 then 'Funds Transfer' else '' END),'Build Assembly')  as doctype";
    public static final String GL_INV_PAY_MI = "'' as paymi";
    public static final String GL_GR_PAY_MI = "'' as paymi";
    public static final String GL_CN_PAY_MI = "'' as paymi";
    public static final String GL_DN_PAY_MI = "'' as paymi";
    public static final String GL_RECEIPT_PAY_MI = "mi.value as paymi";
    public static final String GL_PAYMENT_PAY_MI = "mi.value as paymi";
    public static final String GL_OTHERS_PAY_MI = "'' as paymi";
    public static final String GL_INV_SALES_PERSON_NAME = "mi.value as salespersonname";
    public static final String GL_GR_SALES_PERSON_NAME = "mi.value as salespersonname";
    public static final String GL_CN_SALES_PERSON_NAME = "mi.value as salespersonname";
    public static final String GL_DN_SALES_PERSON_NAME = "'' as salespersonname";
    public static final String GL_RECEIPT_SALES_PERSON_NAME = "'' as salespersonname";
    public static final String GL_PAYMENT_SALES_PERSON_NAME = "'' as salespersonname";
    public static final String GL_OTHERS_SALES_PERSON_NAME = "'' as salespersonname";
    public static final String GL_BALANCE = "'$$CALCULATED_OPENING$$'";
    public static final String GL_TXN_CURRENCY = "curr.currencycode as txncurrency";
    public static final String GL_SA_PERSON_NAME = "'' as personname";
    public static final String GL_SA_PERSON_CODE = "'' as personcode";
    public static final String GL_SA_REF_NO = "sa.seqno as jeno";
    public static final String GL_SA_REF_ID = "sa.id as refid";
    public static final String GL_SA_CASH_TRANSACTION = "null";
    public static final String GL_SA_MEMO = "sa.memo as memo";
    public static final String GL_SA_DESCRIPTION = "'Stock Adjustment'";
    public static final String GL_SA_line_description = "'' as linedescription";
    public static final String GL_SA_GST_CODE = "'' as taxvalue";
    public static final String GL_SA_TYPE = "'Stock Adjustment'  as doctype";
    public static final String GL_SA_PAY_MI = "'' as paymi";
    public static final String GL_SA_SALES_PERSON_NAME = "'' as salespersonname";
    public static final String GL_SR_PERSON_NAME = "cust.name as personname";
    public static final String GL_SR_PERSON_CODE = "cust.acccode as personcode";
    public static final String GL_SR_REF_NO = "sr.srnumber as jeno";
    public static final String GL_SR_REF_ID = "sr.id as refid";
    public static final String GL_SR_CASH_TRANSACTION = "null";
    public static final String GL_SR_MEMO = "sr.memo as memo";
    public static final String GL_SR_DESCRIPTION = "CONCAT('Sales Return, ',cust.name) ";
    public static final String GL_SR_line_description = "'' as linedescription";
    public static final String GL_SR_GST_CODE = "'' as taxvalue";
    public static final String GL_SR_TYPE = "'Sales Return' as doctype";
    public static final String GL_SR_PAY_MI = "'' as paymi";
    public static final String GL_SR_SALES_PERSON_NAME = "'' as salespersonname";
    public static final String GL_PR_PERSON_NAME = "vend.name as personname";
    public static final String GL_PR_PERSON_CODE = "vend.acccode as personcode";
    public static final String GL_PR_REF_NO = "pr.prnumber as jeno";
    public static final String GL_PR_REF_ID = "pr.id as refid";
    public static final String GL_PR_CASH_TRANSACTION = "null";
    public static final String GL_PR_MEMO = "pr.memo as memo";
    public static final String GL_PR_DESCRIPTION = "CONCAT('Purchase Return',vend.name) ";
    public static final String GL_PR_line_description = "'' as linedescription";
    public static final String GL_PR_GST_CODE = "'' as taxvalue";
    public static final String GL_PR_TYPE = "'Purchase Return'  as doctype";
    public static final String GL_PR_PAY_MI = "'' as paymi";
    public static final String GL_PR_SALES_PERSON_NAME = "'' as salespersonname";
    public static final String GL_DO_PERSON_NAME = "cust.name as personname";
    public static final String GL_DO_PERSON_CODE = "cust.acccode as personcode";
    public static final String GL_DO_REF_NO = "do.donumber as jeno";
    public static final String GL_DO_REF_ID = "do.id as refid";
    public static final String GL_DO_CASH_TRANSACTION = "null";
    public static final String GL_DO_MEMO = "do.memo as memo";
    public static final String GL_DO_DESCRIPTION = "CONCAT('Delivery Order, ',cust.name) ";
    public static final String GL_DO_line_description = "'' as linedescription";
    public static final String GL_DO_GST_CODE = "'' as taxvalue";
    public static final String GL_DO_TYPE = "'Delivery Order'  as doctype";
    public static final String GL_DO_PAY_MI = "'' as paymi";
    public static final String GL_DO_SALES_PERSON_NAME = "'' as salespersonname";
    public static final String GL_GRO_PERSON_NAME = "vend.name as personname";
    public static final String GL_GRO_PERSON_CODE = "vend.acccode as personcode";
    public static final String GL_GRO_REF_NO = "gro.gronumber as jeno";
    public static final String GL_GRO_REF_ID = "gro.id as refid";
    public static final String GL_GRO_CASH_TRANSACTION = "null";
    public static final String GL_GRO_MEMO = "gro.memo as memo";
    public static final String GL_GRO_DESCRIPTION = "CONCAT('Goods Receipt, ',vend.name) ";
    public static final String GL_GRO_line_description = "'' as linedescription";
    public static final String GL_GRO_GST_CODE = "'' as taxvalue";
    public static final String GL_GRO_TYPE = "'Goods Receipt Order'  as doctype";
    public static final String GL_GRO_PAY_MI = "'' as paymi";
    public static final String GL_GRO_SALES_PERSON_NAME = "'' as salespersonname";
    

    
    public static final String CUST_ID = "customer.id as custid";
    public static final String CUST_NAME = "customer.name as custname";
    public static final String CUST_accCode = "customer.acccode as acccode";
    public static final String CUST_EMAIL = "customeraddressdetails.emailid as custemail";    
    public static final String Cust_Curr = "customer.currency as custcurrency";
    public static final String Cust_Alise = "customer.aliasname as CustAlise";
    
    
    //dn
    public static final String DN_REF_ID = "dn.id as docid";
    public static final String DN_DOC_NUMBER = "dn.dnnumber as docno";
    public static final String DN_JEID = "dn.journalentry as jeid";
    public static final String DN_MEMO = "dn.memo as memo";
    public static final String DN_CURR_ID = "COALESCE(dn.currency,company.currency) as currid";
    public static final String DN_CURR_CODE = "COALESCE(dncurr.currencycode,compcurr.currencycode) as currcode";
    public static final String DN_CURR_SYMBOL = "COALESCE(dncurr.symbol,compcurr.symbol) as currsymbol";
    public static final String DN_JE_ENTRYNO = "je.entryno as jeentryno";
    public static final String DN_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String DN_JE_ENTRYDATE = "je.entrydate as entrydate";
    public static final String DN_OPN_JE_ENTRYDATE = "dn.creationdate as entrydate";
    public static final String DN_JE_EXT_CURR_RATE = "je.externalcurrencyrate as extcurrrate";
    public static final String DN_OPN_JE_EXT_CURR_RATE = "dn.exchangerateforopeningtransaction as extcurrrate";
    public static final String DN_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(dn.currency=company.currency,1.0,je.externalcurrencyrate),' ',dncurr.currencycode) as exchangerate";
    public static final String DN_OPN_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(dn.currency=company.currency,1.0,dn.exchangerateforopeningtransaction),' ',dncurr.currencycode) as exchangerate";
    
    
    public static final String DN_AMOUNT_DUE = "dn.dnamount as amtdue";    
    public static final String DN_OPN_DEBIT_AMT_BASE = "dn.dnamount/COALESCE(if((1/dn.exchangerateforopeningtransaction)=0,exchangerate_calc(dn.company,dn.creationdate,dn.currency,company.currency),(1/dn.exchangerateforopeningtransaction)),1) as debitamtbase";    
    public static final String DN_DEBIT_AMT_BASE = "dn.dnamount/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(dn.company,dn.creationdate,dn.currency,company.currency),je.externalcurrencyrate),1) as debitamtbase";    
    public static final String DN_OPN_TRANSACTION_AMT_BASE = "dn.dnamount/COALESCE(if((1/dn.exchangerateforopeningtransaction)=0,exchangerate_calc(dn.company,dn.creationdate,dn.currency,company.currency),(1/dn.exchangerateforopeningtransaction)),1) as transactionamtbase";    
    public static final String DN_TRANSACTION_AMT_BASE = "dn.dnamount/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(dn.company,dn.creationdate,dn.currency,company.currency),je.externalcurrencyrate),1) as transactionamtbase";    
    public static final String DN_DEBIT_AMT = "dn.dnamount as debitamt";    
    public static final String DN_OPN_DEBIT_AMT = "dn.dnamount as debitamt";    
    public static final String DN_OPN_CREDIT_AMT_BASE = "0 as creditamtbase";        
    public static final String DN_CREDIT_AMT_BASE = "0 as creditamtbase";        
    public static final String DN_CREDIT_AMT = "0 as creditamt";        
    public static final String DN_OPN_CREDIT_AMT = "0 as creditamt";        
    public static final String DN_OPN_KNOCK_OFF_AMT_BASE = "0 as koamtbase";            
    public static final String DN_KNOCK_OFF_AMT_BASE = "0 as koamtbase";        
    public static final String DN_OPN_KNOCK_OFF_AMT = "0 as koamt";            
    public static final String DN_KNOCK_OFF_AMT = "0 as koamt";        
   
    //cnAll
    public static final String CN_ALL_REF_ID = "cn.id as docid";
    public static final String CN_ALL_DOC_NUMBER = "cn.cnnumber as docno";
    public static final String CN_ALL_JEID = "cn.journalentry as jeid";
    public static final String CN_ALL_MEMO = "cn.memo as memo";
    public static final String CN_ALL_CURR_ID = "COALESCE(cn.currency,company.currency) as currid";
    public static final String CN_ALL_CURR_CODE = "COALESCE(cncurr.currencycode,compcurr.currencycode) as currcode";
    public static final String CN_ALL_CURR_SYMBOL = "COALESCE(cncurr.symbol,compcurr.symbol) as currsymbol";
    public static final String CN_ALL_JE_ENTRYNO = "je.entryno as jeentryno";
    public static final String CN_ALL_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String CN_ALL_JE_ENTRYDATE = "je.entrydate as entrydate";
    public static final String CN_ALL_OPN_JE_ENTRYDATE = "cn.creationdate as entrydate";
    public static final String CN_ALL_JE_EXT_CURR_RATE = "je.externalcurrencyrate as extcurrrate";
    public static final String CN_ALL_OPN_JE_EXT_CURR_RATE = "cn.exchangerateforopeningtransaction as extcurrrate";
    public static final String CN_ALL_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(cn.currency=company.currency,1.0,je.externalcurrencyrate),' ',cncurr.currencycode) as exchangerate";
    public static final String CN_ALL_OPN_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(cn.currency=company.currency,1.0,cn.exchangerateforopeningtransaction),' ',cncurr.currencycode) as exchangerate";
    public static final String CN_ALL_AMOUNT_DUE = "cn.cnamount as amtdue";    
    public static final String CN_ALL_OPN_CREDIT_AMT_BASE = "0 as creditamtbase";    
    public static final String CN_ALL_CREDIT_AMT_BASE = "0 as creditamtbase";    
    public static final String CN_ALL_OPN_TRANSACTION_AMT_BASE = "0 as transactionamtbase";    
    public static final String CN_ALL_TRANSACTION_AMT_BASE = "0 as transactionamtbase";    
    public static final String CN_ALL_CREDIT_AMT = "0 as creditamt";    
    public static final String CN_ALL_OPN_DEBIT_AMT_BASE = "0 as debitamtbase";        
    public static final String CN_ALL_DEBIT_AMT_BASE = "0 as debitamtbase";        
    public static final String CN_ALL_DEBIT_AMT = "0 as debitamt";        
    public static final String CN_ALL_OPN_KNOCK_OFF_AMT_BASE = "0 as koamtbase";            
    public static final String CN_ALL_KNOCK_OFF_AMT_BASE = "0 as koamtbase";        
    public static final String CN_ALL_OPN_KNOCK_OFF_AMT = "0 as koamt";            
    public static final String CN_ALL_KNOCK_OFF_AMT = "0 as koamt";    
    
    //cn
    public static final String CN_REF_ID = "cn.id as docid";
    public static final String CN_DOC_NUMBER = "cn.cnnumber as docno";
    public static final String CN_JEID = "cn.journalentry as jeid";
    public static final String CN_MEMO = "cn.memo as memo";
    public static final String CN_CURR_ID = "COALESCE(cn.currency,company.currency) as currid";
    public static final String CN_CURR_CODE = "COALESCE(cncurr.currencycode,compcurr.currencycode) as currcode";
    public static final String CN_CURR_SYMBOL = "COALESCE(cncurr.symbol,compcurr.symbol) as currsymbol";
    public static final String CN_JE_ENTRYNO = "je.entryno as jeentryno";
    public static final String CN_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String CN_JE_ENTRYDATE = "je.entrydate as entrydate";
    public static final String CN_OPN_JE_ENTRYDATE = "cn.creationdate as entrydate";
    public static final String CN_JE_EXT_CURR_RATE = "je.externalcurrencyrate as extcurrrate";
    public static final String CN_OPN_JE_EXT_CURR_RATE = "cn.exchangerateforopeningtransaction as extcurrrate";
    public static final String CN_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(cn.currency=company.currency,1.0,je.externalcurrencyrate),' ',cncurr.currencycode) as exchangerate";
    public static final String CN_OPN_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(cn.currency=company.currency,1.0,cn.exchangerateforopeningtransaction),' ',cncurr.currencycode) as exchangerate";
    public static final String CN_AMOUNT_DUE = "cn.cnamount as amtdue";    
    public static final String CN_OPN_CREDIT_AMT_BASE = "cn.cnamount/COALESCE(if((1/cn.exchangerateforopeningtransaction)=0,exchangerate_calc(cn.company,cn.creationdate,cn.currency,company.currency),(1/cn.exchangerateforopeningtransaction)),1) as creditamtbase";    
    public static final String CN_CREDIT_AMT_BASE = "jed.amount/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(cn.company,cn.creationdate,cn.currency,company.currency),je.externalcurrencyrate),1) as creditamtbase";    
    public static final String CN_OPN_TRANSACTION_AMT_BASE = "cn.cnamount/COALESCE(if((1/cn.exchangerateforopeningtransaction)=0,exchangerate_calc(cn.company,cn.creationdate,cn.currency,company.currency),(1/cn.exchangerateforopeningtransaction)),1) as transactionamtbase";    
    public static final String CN_TRANSACTION_AMT_BASE = "cn.cnamount/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(cn.company,cn.creationdate,cn.currency,company.currency),je.externalcurrencyrate),1) as transactionamtbase";    
    public static final String CN_CREDIT_AMT = "cn.cnamount as creditamt";    
    public static final String CN_OPN_CREDIT_AMT = "cn.cnamount as creditamt";    
    public static final String CN_OPN_DEBIT_AMT_BASE = "0 as debitamtbase";        
    public static final String CN_DEBIT_AMT_BASE = "0 as debitamtbase";        
    public static final String CN_DEBIT_AMT = "0 as debitamt";        
    public static final String CN_OPN_DEBIT_AMT = "0 as debitamt";        
    public static final String CN_OPN_KNOCK_OFF_AMT_BASE = "0 as koamtbase";            
    public static final String CN_KNOCK_OFF_AMT_BASE = "0 as koamtbase";        
    public static final String CN_OPN_KNOCK_OFF_AMT = "0 as koamt";            
    public static final String CN_KNOCK_OFF_AMT = "0 as koamt";    
    
    //payment
    public static final String PAYMENT_REF_ID = "p.id as docid";
    public static final String PAYMENT_TYPE = "'Payment Made' as type";
    public static final String PAYMENT_DOC_NUMBER = "p.paymentnumber as docno";
    public static final String PAYMENT_JEID = "p.journalentry as jeid";
    public static final String PAYMENT_MEMO = "p.memo as memo";
    public static final String PAYMENT_CURR_ID = "COALESCE(p.currency,company.currency) as currid";
    public static final String PAYMENT_CURR_CODE = "COALESCE(pcurr.currencycode,compcurr.currencycode) as currcode";
    public static final String PAYMENT_CURR_SYMBOL = "COALESCE(pcurr.symbol,compcurr.symbol) as currsymbol";
    public static final String PAYMENT_JE_ENTRYNO = "je.entryno as jeentryno";
//    public static final String PAYMENT_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String PAYMENT_JE_ENTRYDATE = "je.entrydate as entrydate";
    public static final String PAYMENT_OPN_JE_ENTRYDATE = "p.creationdate as entrydate";
    public static final String PAYMENT_JE_EXT_CURR_RATE = "je.externalcurrencyrate as extcurrrate";
    public static final String PAYMENT_OPN_JE_EXT_CURR_RATE = "p.exchangerateforopeningtransaction as extcurrrate";
    public static final String PAYMENT_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(p.currency=company.currency,1.0,je.externalcurrencyrate),' ',pcurr.currencycode) as exchangerate";
    public static final String PAYMENT_AMOUNT_DUE = "jed.amount as amtdue";    
    public static final String PAYMENT_CREDIT_AMT_BASE = "0 as creditamtbase";    
    public static final String PAYMENT_TRANSACTION_AMT_BASE = "p.depositamountinbase as transactionamtbase";    
    public static final String PAYMENT_CREDIT_AMT = "0 as creditamt";    
    public static final String PAYMENT_DEBIT_AMT_BASE = "p.depositamountinbase as debitamtbase";        
    public static final String PAYMENT_DEBIT_AMT = "p.depositamount as debitamt";        
    public static final String PAYMENT_KNOCK_OFF_AMT_BASE = "0 as koamtbase";        
    public static final String PAYMENT_KNOCK_OFF_AMT = "0 as koamt";        
    
    //ERM-744
    public static final String PAYMENT_DISHONOURED_TYPE = "'Dishonoured Make Payment' as type";
    public static final String PAYMENT_DISHONOURED_JEID = "p.dishonouredchequeje as jeid";
    public static final String PAYMENT_DISHONOURED_CREDIT_AMT_BASE = "p.depositamountinbase as creditamtbase";    
    public static final String PAYMENT_DISHONOURED_TRANSACTION_AMT_BASE = "p.depositamountinbase as transactionamtbase";    
    public static final String PAYMENT_DISHONOURED_CREDIT_AMT = "p.depositamount as creditamt";    
    public static final String PAYMENT_DISHONOURED_DEBIT_AMT_BASE = "0 as debitamtbase";        
    public static final String PAYMENT_DISHONOURED_DEBIT_AMT = "0 as debitamt";   

    //receipt
    public static final String RECEIPT_REF_ID = "r.id as docid";
    public static final String RECEIPT_DOC_NUMBER = "r.receiptnumber as docno";
    public static final String RECEIPT_JEID = "r.journalentry as jeid";
    public static final String RECEIPT_MEMO = "r.memo as memo";
    public static final String RECEIPT_TYPE = "'Payment Received' as type";
    public static final String RECEIPT_CURR_ID = "COALESCE(r.currency,company.currency) as currid";
    public static final String RECEIPT_CURR_CODE = "COALESCE(rcurr.currencycode,compcurr.currencycode) as currcode";
    public static final String RECEIPT_CURR_SYMBOL = "COALESCE(rcurr.symbol,compcurr.symbol) as currsymbol";
    public static final String RECEIPT_JE_ENTRYNO = "je.entryno as jeentryno";
    public static final String RECEIPT_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String RECEIPT_JE_ENTRYDATE = "je.entrydate as entrydate";
    public static final String RECEIPT_OPN_JE_ENTRYDATE = "r.creationdate as entrydate";
    public static final String RECEIPT_JE_EXT_CURR_RATE = "je.externalcurrencyrate as extcurrrate";
    public static final String RECEIPT_OPN_JE_EXT_CURR_RATE = "r.exchangerateforopeningtransaction as extcurrrate";
    public static final String RECEIPT_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(r.currency=company.currency,1.0,je.externalcurrencyrate),' ',rcurr.currencycode) as exchangerate";
    public static final String RECEIPT_OPN_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(r.currency=company.currency,1.0,r.exchangerateforopeningtransaction),' ',rcurr.currencycode) as exchangerate";
    public static final String RECEIPT_AMOUNT_DUE = "r.depositamount as amtdue";    
    public static final String RECEIPT_OPN_CREDIT_AMT_BASE = "r.depositamount/COALESCE(if((1/r.exchangerateforopeningtransaction)=0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),(1/r.exchangerateforopeningtransaction)),1) as creditamtbase";    
    public static final String RECEIPT_AMOUNT_CONV_FACTOR = "/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)";        
    public static final String RECEIPT_OPN_AMOUNT_CONV_FACTOR = "/COALESCE(if(r.exchangerateforopeningtransaction =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),r.exchangerateforopeningtransaction ),1)";        
    public static final String RECEIPT_CREDIT_AMT_BASE = "SUM(jed.amount)"+RECEIPT_AMOUNT_CONV_FACTOR+" as creditamtbase";    
    public static final String RECEIPT_OPN_TRANSACTION_AMT_BASE = "r.originalOpeningBalanceBaseAmount as transactionamtbase";    
    public static final String RECEIPT_TRANSACTION_AMT_BASE = "SUM(jed.amount)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1) as transactionamtbase";    
    public static final String RECEIPT_CREDIT_AMT = "SUM(jed.amount) as creditamt";    
    public static final String RECEIPT_OPN_CREDIT_AMT = "r.depositamount as creditamt";    
    public static final String RECEIPT_OPN_DEBIT_AMT_BASE = "0 as debitamtbase";        
    public static final String RECEIPT_DEBIT_AMT_BASE = "0 as debitamtbase";        
    public static final String RECEIPT_DEBIT_AMT = "0 as debitamt";        
    public static final String RECEIPT_OPN_DEBIT_AMT = "0 as debitamt";        
    public static final String RECEIPT_KNOCK_OFF_AMT = "0 as koamt";
    public static final String RECEIPT_KNOCK_OFF_AMT_BASE = "0 as koamtbase";
    public static final String RECEIPT_OPN_KNOCK_OFF_AMT = "0 as koamt";
    public static final String RECEIPT_OPN_KNOCK_OFF_AMT_BASE = "0 as koamtbase";
    
    //ERM-744
    public static final String RECEIPT_DISHONOURED_TYPE = "'Dishonoured Receive Payment' as type";
//    public static final String RECEIPT_DISHONOURED_REF_ID = "r.id as docid";
//    public static final String RECEIPT_DISHONOURED_DOC_NUMBER = "r.receiptnumber as docno";
    public static final String RECEIPT_DISHONOURED_JEID = "r.dishonouredchequeje as jeid";
//    public static final String RECEIPT_DISHONOURED_MEMO = "r.memo as memo";
//    public static final String RECEIPT_DISHONOURED_CURR_ID = "COALESCE(r.currency,company.currency) as currid";
//    public static final String RECEIPT_DISHONOURED_CURR_CODE = "COALESCE(rcurr.currencycode,compcurr.currencycode) as currcode";
//    public static final String RECEIPT_DISHONOURED_CURR_SYMBOL = "COALESCE(rcurr.symbol,compcurr.symbol) as currsymbol";
//    public static final String RECEIPT_DISHONOURED_JE_ENTRYNO = "je.entryno as jeentryno";
//    public static final String RECEIPT_DISHONOURED_OPN_JE_ENTRYNO = "' ' as jeentryno";
//    public static final String RECEIPT_DISHONOURED_JE_ENTRYDATE = "je.entrydate as entrydate";
//    public static final String RECEIPT_DISHONOURED_OPN_JE_ENTRYDATE = "r.creationdate as entrydate";
//    public static final String RECEIPT_DISHONOURED_JE_EXT_CURR_RATE = "je.externalcurrencyrate as extcurrrate";
//    public static final String RECEIPT_DISHONOURED_OPN_JE_EXT_CURR_RATE = "r.exchangerateforopeningtransaction as extcurrrate";
//    public static final String RECEIPT_DISHONOURED_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(r.currency=company.currency,1.0,je.externalcurrencyrate),' ',rcurr.currencycode) as exchangerate";
//    public static final String RECEIPT_DISHONOURED_OPN_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(r.currency=company.currency,1.0,r.exchangerateforopeningtransaction),' ',rcurr.currencycode) as exchangerate";
//    public static final String RECEIPT_DISHONOURED_AMOUNT_DUE = "r.depositamount as amtdue";    
//    public static final String RECEIPT_DISHONOURED_OPN_CREDIT_AMT_BASE = "r.depositamount/COALESCE(if((1/r.exchangerateforopeningtransaction)=0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),(1/r.exchangerateforopeningtransaction)),1) as creditamtbase";    
    public static final String RECEIPT_DISHONOURED_AMOUNT_CONV_FACTOR = "/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)";        
//    public static final String RECEIPT_DISHONOURED_OPN_AMOUNT_CONV_FACTOR = "/COALESCE(if(r.exchangerateforopeningtransaction =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),r.exchangerateforopeningtransaction ),1)";        
    public static final String RECEIPT_DISHONOURED_CREDIT_AMT_BASE = "0 as creditamtbase";    
    public static final String RECEIPT_DISHONOURED_OPN_TRANSACTION_AMT_BASE = "r.originalOpeningBalanceBaseAmount as transactionamtbase";    
    public static final String RECEIPT_DISHONOURED_TRANSACTION_AMT_BASE = "SUM(jed.amount)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1) as transactionamtbase";    
    public static final String RECEIPT_DISHONOURED_CREDIT_AMT = "0 as creditamt";    
    public static final String RECEIPT_DISHONOURED_OPN_CREDIT_AMT = "r.depositamount as creditamt";    
    public static final String RECEIPT_DISHONOURED_OPN_DEBIT_AMT_BASE = " as debitamtbase";        
    public static final String RECEIPT_DISHONOURED_DEBIT_AMT_BASE = "SUM(jed.amount)"+RECEIPT_DISHONOURED_AMOUNT_CONV_FACTOR+" as debitamtbase";        
    public static final String RECEIPT_DISHONOURED_DEBIT_AMT = "SUM(jed.amount) as debitamt";        
    public static final String RECEIPT_DISHONOURED_OPN_DEBIT_AMT = "0 as debitamt";        
    public static final String RECEIPT_DISHONOURED_KNOCK_OFF_AMT = "0 as koamt";
    public static final String RECEIPT_DISHONOURED_KNOCK_OFF_AMT_BASE = "0 as koamtbase";
    public static final String RECEIPT_DISHONOURED_OPN_KNOCK_OFF_AMT = "0 as koamt";
    public static final String RECEIPT_DISHONOURED_OPN_KNOCK_OFF_AMT_BASE = "0 as koamtbase";
    
    // Receipt For Fetch All
    
    public static final String RECEIPT_ALL_REF_ID = "r.id as docid";
    public static final String RECEIPT_ALL_DOC_NUMBER = "r.receiptnumber as docno";
    public static final String RECEIPT_ALL_JEID = "r.journalentry as jeid";
    public static final String RECEIPT_ALL_MEMO = "r.memo as memo";
    public static final String RECEIPT_ALL_TYPE = "'Payment Received' as type";
    public static final String RECEIPT_ALL_CURR_ID = "COALESCE(r.currency,company.currency) as currid";
    public static final String RECEIPT_ALL_CURR_CODE = "COALESCE(rcurr.currencycode,compcurr.currencycode) as currcode";
    public static final String RECEIPT_ALL_CURR_SYMBOL = "COALESCE(rcurr.symbol,compcurr.symbol) as currsymbol";
    public static final String RECEIPT_ALL_JE_ENTRYNO = "je.entryno as jeentryno";
    public static final String RECEIPT_ALL_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String RECEIPT_ALL_JE_ENTRYDATE = "je.entrydate as entrydate";
    public static final String RECEIPT_ALL_OPN_JE_ENTRYDATE = "r.creationdate as entrydate";
    public static final String RECEIPT_ALL_JE_EXT_CURR_RATE = "je.externalcurrencyrate as extcurrrate";
    public static final String RECEIPT_ALL_OPN_JE_EXT_CURR_RATE = "r.exchangerateforopeningtransaction as extcurrrate";
    public static final String RECEIPT_ALL_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(r.currency=company.currency,1.0,je.externalcurrencyrate),' ',rcurr.currencycode) as exchangerate";
    public static final String RECEIPT_ALL_OPN_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(r.currency=company.currency,1.0,r.exchangerateforopeningtransaction),' ',rcurr.currencycode) as exchangerate";
    public static final String RECEIPT_ALL_AMOUNT_DUE = "r.depositamount as amtdue";    
    public static final String RECEIPT_ALL_OPN_CREDIT_AMT_BASE = "r.depositamount/COALESCE(if((1/r.exchangerateforopeningtransaction)=0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),(1/r.exchangerateforopeningtransaction)),1) as creditamtbase";    
    public static final String RECEIPT_ALL_AMOUNT_CONV_FACTOR = "/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)";        
    public static final String RECEIPT_ALL_OPN_AMOUNT_CONV_FACTOR = "/COALESCE(if(r.exchangerateforopeningtransaction =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),r.exchangerateforopeningtransaction ),1)";        
    public static final String RECEIPT_ALL_CREDIT_AMT_BASE = "SUM(jed.amount)"+RECEIPT_ALL_AMOUNT_CONV_FACTOR+" as creditamtbase";    
    public static final String RECEIPT_ALL_OPN_TRANSACTION_AMT_BASE = "r.originalOpeningBalanceBaseAmount as transactionamtbase";    
    public static final String RECEIPT_ALL_TRANSACTION_AMT_BASE = "SUM(jed.amount)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1) as transactionamtbase";    
    public static final String RECEIPT_ALL_CREDIT_AMT = "SUM(jed.amount) as creditamt";    
    public static final String RECEIPT_ALL_OPN_CREDIT_AMT = "r.depositamount as creditamt";    
    public static final String RECEIPT_ALL_OPN_DEBIT_AMT_BASE = "0 as debitamtbase";        
    public static final String RECEIPT_ALL_DEBIT_AMT_BASE = "0 as debitamtbase";        
    public static final String RECEIPT_ALL_DEBIT_AMT = "0 as debitamt";        
    public static final String RECEIPT_ALL_OPN_DEBIT_AMT = "0 as debitamt";        
    public static final String RECEIPT_ALL_KNOCK_OFF_AMT = "0 as koamt";
    public static final String RECEIPT_ALL_KNOCK_OFF_AMT_BASE = "0 as koamtbase";
    public static final String RECEIPT_ALL_OPN_KNOCK_OFF_AMT = "0 as koamt";
    public static final String RECEIPT_ALL_OPN_KNOCK_OFF_AMT_BASE = "0 as koamtbase";
    
    
    // Receipt For Outstanding
    
    public static final String RECEIPT_OUT_REF_ID = "r.id as docid";
    public static final String RECEIPT_OUT_DOC_NUMBER = "r.receiptnumber as docno";
    public static final String RECEIPT_OUT_JEID = "r.journalentry as jeid";
    public static final String RECEIPT_OUT_MEMO = "r.memo as memo";
    public static final String RECEIPT_OUT_TYPE = "'Payment Received' as type";
    public static final String RECEIPT_OUT_CURR_ID = "COALESCE(r.currency,company.currency) as currid";
    public static final String RECEIPT_OUT_CURR_CODE = "COALESCE(rcurr.currencycode,compcurr.currencycode) as currcode";
    public static final String RECEIPT_OUT_CURR_SYMBOL = "COALESCE(rcurr.symbol,compcurr.symbol) as currsymbol";
    public static final String RECEIPT_OUT_JE_ENTRYNO = "je.entryno as jeentryno";
    public static final String RECEIPT_OUT_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String RECEIPT_OUT_JE_ENTRYDATE = "je.entrydate as entrydate";
    public static final String RECEIPT_OUT_OPN_JE_ENTRYDATE = "r.creationdate as entrydate";
    public static final String RECEIPT_OUT_JE_EXT_CURR_RATE = "je.externalcurrencyrate as extcurrrate";
    public static final String RECEIPT_OUT_OPN_JE_EXT_CURR_RATE = "r.exchangerateforopeningtransaction as extcurrrate";
    public static final String RECEIPT_OUT_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(r.currency=company.currency,1.0,je.externalcurrencyrate),' ',rcurr.currencycode) as exchangerate";
    public static final String RECEIPT_OUT_OPN_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(r.currency=company.currency,1.0,r.exchangerateforopeningtransaction),' ',rcurr.currencycode) as exchangerate";
    public static final String RECEIPT_OUT_AMOUNT_DUE = "r.depositamount as amtdue";    
    public static final String RECEIPT_OUT_OPN_CREDIT_AMT_BASE = "r.depositamount/COALESCE(if((1/r.exchangerateforopeningtransaction)=0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),(1/r.exchangerateforopeningtransaction)),1) as creditamtbase";    
    public static final String RECEIPT_OUT_AMOUNT_CONV_FACTOR = "/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)";        
    public static final String RECEIPT_OUT_OPN_AMOUNT_CONV_FACTOR = "/COALESCE(if(r.exchangerateforopeningtransaction =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),r.exchangerateforopeningtransaction ),1)";        
    public static final String RECEIPT_OUT_CREDIT_AMT_BASE = "rad.amount"+RECEIPT_OUT_AMOUNT_CONV_FACTOR+" as creditamtbase";    
    public static final String RECEIPT_OUT_OPN_TRANSACTION_AMT_BASE = "r.originalOpeningBalanceBaseAmount as transactionamtbase";    
    public static final String RECEIPT_OUT_TRANSACTION_AMT_BASE = "rad.amount/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1) as transactionamtbase";    
    public static final String RECEIPT_OUT_CREDIT_AMT = "rad.amount as creditamt";    
    public static final String RECEIPT_OUT_OPN_CREDIT_AMT = "r.depositamount as creditamt";    
    public static final String RECEIPT_OUT_OPN_DEBIT_AMT_BASE = "0 as debitamtbase";        
    public static final String RECEIPT_OUT_DEBIT_AMT_BASE = "0 as debitamtbase";        
    public static final String RECEIPT_OUT_DEBIT_AMT = "0 as debitamt";        
    public static final String RECEIPT_OUT_OPN_DEBIT_AMT = "0 as debitamt";        
    public static final String RECEIPT_OUT_KNOCK_OFF_AMT = "0 as koamt";
    public static final String RECEIPT_OUT_KNOCK_OFF_AMT_BASE = "0 as koamtbase";
    public static final String RECEIPT_OUT_OPN_KNOCK_OFF_AMT = "0 as koamt";
    public static final String RECEIPT_OUT_OPN_KNOCK_OFF_AMT_BASE = "0 as koamtbase";
    
        
    public static final String RECEIPT_Detail_REF_ID = "r.id as docid";
    public static final String RECEIPT_Detail_DOC_NUMBER = "r.receiptnumber as docno";
    public static final String RECEIPT_Detail_JEID = "r.journalentry as jeid";
    public static final String RECEIPT_Detail_MEMO = "r.memo as memo";
    public static final String RECEIPT_Detail_CURR_ID = "COALESCE(r.currency,company.currency) as currid";
    public static final String RECEIPT_Detail_CURR_CODE = "COALESCE(rcurr.currencycode,compcurr.currencycode) as currcode";
    public static final String RECEIPT_Detail_CURR_SYMBOL = "COALESCE(rcurr.symbol,compcurr.symbol) as currsymbol";
    public static final String RECEIPT_Detail_JE_ENTRYNO = "je.entryno as jeentryno";
    public static final String RECEIPT_Detail_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String RECEIPT_Detail_JE_ENTRYDATE = "je.entrydate as entrydate";
    public static final String RECEIPT_Detail_OPN_JE_ENTRYDATE = "r.creationdate as entrydate";
    public static final String RECEIPT_Detail_JE_EXT_CURR_RATE = "je.externalcurrencyrate as extcurrrate";
    public static final String RECEIPT_Detail_OPN_JE_EXT_CURR_RATE = "r.exchangerateforopeningtransaction as extcurrrate";
    public static final String RECEIPT_Detail_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(r.currency=company.currency,1.0,je.externalcurrencyrate),' ',rcurr.currencycode) as exchangerate";
    public static final String RECEIPT_Detail_OPN_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(r.currency=company.currency,1.0,r.exchangerateforopeningtransaction),' ',rcurr.currencycode) as exchangerate";
    public static final String RECEIPT_Detail_AMOUNT_DUE = "r.depositamount as amtdue";    
    public static final String RECEIPT_Detail_AMOUNT_Dif = "r.depositamount/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)";    
    public static final String RECEIPT_Detail_OPN_CREDIT_AMT_BASE = "r.depositamount/COALESCE(if((1/r.exchangerateforopeningtransaction)=0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),(1/r.exchangerateforopeningtransaction)),1) as creditamtbase";    
    public static final String RECEIPT_Detail_AMOUNT_CONV_FACTOR = "/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)";        
    public static final String RECEIPT_Detail_OPN_AMOUNT_CONV_FACTOR = "/COALESCE(if(r.exchangerateforopeningtransaction =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),r.exchangerateforopeningtransaction ),1)";        
    public static final String RECEIPT_Detail_OPN_TRANSACTION_AMT_BASE = "r.originalOpeningBalanceBaseAmount as transactionamtbase";    
    public static final String RECEIPT_Detail_TRANSACTION_AMT_BASE = "0 as transactionamtbase";    
    public static final String RECEIPT_Detail_CREDIT_AMT_BASE = "0 as creditamtbase";    
    public static final String RECEIPT_Detail_CREDIT_AMT = "0 as creditamt";    
    public static final String RECEIPT_Detail_OPN_CREDIT_AMT = "r.depositamount as creditamt";    
    public static final String RECEIPT_Detail_OPN_DEBIT_AMT_BASE = "0 as debitamtbase";        
    public static final String RECEIPT_Detail_DEBIT_AMT_BASE = "0 as debitamtbase";        
    public static final String RECEIPT_Detail_DEBIT_AMT = "0 as debitamt";        
    
     
    
    public static final String RECEIPT_Detail_Knoff_Cal1 = "(SUM(jed.amountinbase) - (r.depositamount/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)))";        
    public static final String RECEIPT_Detail_Knoff_Cal2 = "((r.depositamount/ COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)) - (rad.amount / COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)))";        
    public static final String RECEIPT_Detail_OPN_KNOCK_OFF_AMT = "SUM(ifnull(rd.amount,0)) as koamt";
    public static final String RECEIPT_Detail_OPN_KNOCK_OFF_AMT_BASE = "SUM(ifnull(rd.amount,0)/COALESCE(if(r.exchangerateforopeningtransaction =0, exchangerate_calc(r.company,r.creationdate,r.currency,company.currency), r.exchangerateforopeningtransaction ),1)) as koamtbase";
    
    
    public static final String INV_REF_ID = "invoice.id as docid";
    public static final String INV_DOC_NUMBER = "invoice.invoicenumber as docno";
    public static final String INV_JEID = "invoice.journalentry as jeid";
    public static final String INV_MEMO = "invoice.memo as memo";
    public static final String INV_CURR_ID = "COALESCE(invoice.currency,company.currency) as currid";
    public static final String INV_CURR_CODE = "COALESCE(invcurr.currencycode,compcurr.currencycode) as currcode";
    public static final String INV_CURR_SYMBOL = "COALESCE(invcurr.symbol,compcurr.symbol) as currsymbol";
    public static final String INV_JE_ENTRYNO = "je.entryno as jeentryno";
    public static final String INV_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String INV_JE_ENTRYDATE = "je.entrydate as entrydate";
    public static final String INV_OPN_JE_ENTRYDATE = "invoice.creationdate as entrydate";
    public static final String INV_JE_EXT_CURR_RATE = "je.externalcurrencyrate as extcurrrate";
    public static final String INV_OPN_JE_EXT_CURR_RATE = "invoice.exchangerateforopeningtransaction as extcurrrate";
    public static final String INV_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(invoice.currency=company.currency,1.0,je.externalcurrencyrate),' ',invcurr.currencycode) as exchangerate";
    public static final String INV_OPN_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(invoice.currency=company.currency,1.0,invoice.exchangerateforopeningtransaction),' ',invcurr.currencycode) as exchangerate";
    public static final String INV_AMOUNT_DUE = "invoice.invoiceamount as amtdue";    
    public static final String INV_OPN_DEBIT_AMT_BASE = "invoice.originalopeningbalanceamount/COALESCE(if((1/invoice.exchangerateforopeningtransaction)=0,exchangerate_calc(invoice.company,invoice.creationdate,invoice.currency, company.currency),(1/invoice.exchangerateforopeningtransaction)),1) as debitamtbase";    
    public static final String INV_DEBIT_AMT_BASE = "invoice.invoiceamountinbase as debitamtbase";    
    public static final String INV_OPN_TRANSACTION_AMT_BASE = "invoice.originalopeningbalancebaseamount as transactionamtbase";    
    public static final String INV_TRANSACTION_AMT_BASE = "invoice.invoiceamountinbase as transactionamtbase";    
    public static final String INV_DEBIT_AMT = "invoice.invoiceamount as debitamt";    
    public static final String INV_OPN_DEBIT_AMT = "invoice.originalopeningbalanceamount as debitamt";    
    public static final String INV_OPN_CREDIT_AMT_BASE = "0 as creditamtbase";        
    public static final String INV_CREDIT_AMT_BASE = "0 as creditamtbase";        
    public static final String INV_CREDIT_AMT = "0 as creditamt";   
    public static final String INV_OPN_CREDIT_AMT = "0 as creditamt";   
    public static final String INV_OPN_KNOCK_OFF_AMT_BASE = "0 as koamtbase";            
    public static final String INV_OPN_KNOCK_OFF_AMT = "0 as koamt";            
    public static final String INV_KNOCK_OFF_AMT_BASE = "0 as koamtbase";        
    public static final String INV_KNOCK_OFF_AMT = "0 as koamt";        
    public static final String INV_CND_KNOCK_OFF_AMT_BASE = "SUM(discount.amountinInvCurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(invoice.company,invoice.creationdate,invoice.currency, company.currency),je.externalcurrencyrate),1)) as koamtbase";            
    public static final String INV_CND_KNOCK_OFF_AMT = "SUM(discount.amountinInvCurrency) as koamt";        
    public static final String INV_LDR_KNOCK_OFF_AMT_BASE = "SUM(ldr.amountininvoicecurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(invoice.company,invoice.creationdate,invoice.currency, company.currency),je.externalcurrencyrate),1)) as koamtbase";            
    public static final String INV_LDR_KNOCK_OFF_AMT = "SUM(ldr.amountininvoicecurrency) as koamt";        
    public static final String INV_RD_KNOCK_OFF_AMT_BASE = "SUM(rd.amountininvoicecurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(invoice.company,invoice.creationdate,invoice.currency, company.currency),je.externalcurrencyrate),1)) as koamtbase";            
    public static final String INV_RD_KNOCK_OFF_AMT = "SUM(rd.amountininvoicecurrency) as koamt";        
    public static final String INV_WRITEOFF_KNOCK_OFF_AMT_BASE = "SUM(iwo.writtenoffamountininvoicecurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(invoice.company,invoice.creationdate,invoice.currency, company.currency),je.externalcurrencyrate),1)) as koamtbase";            
    public static final String INV_WRITEOFF_KNOCK_OFF_AMT = "SUM(iwo.writtenoffamountininvoicecurrency) as koamt";            
   
    public static final String INV_MAIN_KNOCK_OFF_AMT_BASE = "SUM(i.koamtbase) as koamtbase";            
    public static final String INV_MAIN_KNOCK_OFF_AMT = "SUM(i.koamt) as koamt";            
    public static final String INV_MAIN_REF_ID = "i.docid as docid";
    public static final String INV_MAIN_DOC_NUMBER = "i.docno as docno";
    public static final String INV_MAIN_JEID = "i.jeid as jeid";
    public static final String INV_MAIN_MEMO = "i.memo as memo";
    public static final String INV_MAIN_CURR_ID = "i.currid as currid";
    public static final String INV_MAIN_CURR_CODE = "i.currcode as currcode";
    public static final String INV_MAIN_CURR_SYMBOL = "i.currsymbol as currsymbol";
    public static final String INV_MAIN_JE_ENTRYNO = "i.jeentryno as jeentryno";
    public static final String INV_MAIN_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String INV_MAIN_JE_ENTRYDATE = "i.entrydate as entrydate";
    public static final String INV_MAIN_OPN_JE_ENTRYDATE = "i.entrydate as entrydate";
    public static final String INV_MAIN_JE_EXT_CURR_RATE = "i.extcurrrate as extcurrrate";
    public static final String INV_MAIN_OPN_JE_EXT_CURR_RATE = "i.extcurrrate as extcurrrate";
    public static final String INV_MAIN_EXT_CURR_RATE = "i.exchangerate as exchangerate";
    public static final String INV_MAIN_OPN_EXT_CURR_RATE = "i.exchangerate as exchangerate";
    public static final String INV_MAIN_AMOUNT_DUE = "i.amtdue as amtdue";    
    public static final String INV_MAIN_OPN_CREDIT_AMT_BASE = "i.creditamtbase as creditamtbase";    
    public static final String INV_MAIN_CREDIT_AMT_BASE = "i.creditamtbase as creditamtbase";    
    public static final String INV_MAIN_OPN_TRANSACTION_AMT_BASE = "i.transactionamtbase as transactionamtbase";    
    public static final String INV_MAIN_TRANSACTION_AMT_BASE = "i.transactionamtbase as transactionamtbase";    
    public static final String INV_MAIN_CREDIT_AMT = "i.creditamt as creditamt";    
    public static final String INV_MAIN_OPN_CREDIT_AMT = "i.creditamt as creditamt";    
    public static final String INV_MAIN_OPN_DEBIT_AMT_BASE = "i.debitamtbase as debitamtbase";        
    public static final String INV_MAIN_DEBIT_AMT_BASE = "i.debitamtbase as debitamtbase";        
    public static final String INV_MAIN_DEBIT_AMT = "i.debitamt as debitamt";   
    public static final String INV_MAIN_OPN_DEBIT_AMT = "i.debitamt as debitamt";   
    public static final String INV_MAIN_OPN_KNOCK_OFF_AMT_BASE = "SUM(i.koamtbase)  as koamtbase";            
    public static final String INV_MAIN_OPN_KNOCK_OFF_AMT = "SUM(i.koamt)  as koamt";    
    public static final String INV_MAIN_CUST_ID = "i.custid as custid";
    public static final String INV_MAIN_CUST_NAME = "i.custname as custname";
    public static final String INV_MAIN_CUST_accCode = "i.acccode as acccode";
   
    public static final String INV_CND_OPN_KNOCK_OFF_AMT_BASE = "SUM(discount.amountinInvCurrency/COALESCE(exchangerate_calc(invoice.company,invoice.creationdate,invoice.currency, company.currency),1)) as koamtbase";
    public static final String INV_CND_OPN_KNOCK_OFF_AMT = "SUM(discount.amountinInvCurrency) as koamt";
    public static final String INV_LDR_OPN_KNOCK_OFF_AMT_BASE = "SUM(ldr.amountininvoicecurrency/COALESCE(exchangerate_calc(invoice.company,invoice.creationdate,invoice.currency, company.currency),1)) as koamtbase";
    public static final String INV_LDR_OPN_KNOCK_OFF_AMT = "SUM(ldr.amountininvoicecurrency) as koamt";
    public static final String INV_RD_OPN_KNOCK_OFF_AMT_BASE = "SUM(rd.amountininvoicecurrency/COALESCE(exchangerate_calc(invoice.company,invoice.creationdate,invoice.currency, company.currency),1)) as koamtbase";
    public static final String INV_RD_OPN_KNOCK_OFF_AMT ="SUM(rd.amountininvoicecurrency) as koamt";
    public static final String INV_WRITEOFF_OPN_KNOCK_OFF_AMT_BASE = "SUM(iwo.writtenoffamountininvoicecurrency/COALESCE(exchangerate_calc(invoice.company,invoice.creationdate,invoice.currency, company.currency),1)) as koamtbase";
    public static final String INV_WRITEOFF_OPN_KNOCK_OFF_AMT = "SUM(iwo.writtenoffamountininvoicecurrency) as koamt";
    
    
    
    
    //Credit Note
    
    public static final String CN_MAIN_KNOCK_OFF_AMT_BASE = "SUM(cn.koamtbase) as koamtbase";            
    public static final String CN_MAIN_KNOCK_OFF_AMT = "SUM(cn.koamt) as koamt";            
    public static final String CN_MAIN_REF_ID = "cn.docid as docid";
    public static final String CN_MAIN_DOC_NUMBER = "cn.docno as docno";
    public static final String CN_MAIN_JEID = "cn.jeid as jeid";
    public static final String CN_MAIN_MEMO = "cn.memo as memo";
    public static final String CN_MAIN_CURR_ID = "cn.currid as currid";
    public static final String CN_MAIN_CURR_CODE = "cn.currcode as currcode";
    public static final String CN_MAIN_CURR_SYMBOL = "cn.currsymbol as currsymbol";
    public static final String CN_MAIN_JE_ENTRYNO = "cn.jeentryno as jeentryno";
    public static final String CN_MAIN_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String CN_MAIN_JE_ENTRYDATE = "cn.entrydate as entrydate";
    public static final String CN_MAIN_OPN_JE_ENTRYDATE = "cn.entrydate as entrydate";
    public static final String CN_MAIN_JE_EXT_CURR_RATE = "cn.extcurrrate as extcurrrate";
    public static final String CN_MAIN_OPN_JE_EXT_CURR_RATE = "cn.extcurrrate as extcurrrate";
    public static final String CN_MAIN_EXT_CURR_RATE = "cn.exchangerate as exchangerate";
    public static final String CN_MAIN_OPN_EXT_CURR_RATE = "cn.exchangerate as exchangerate";
    public static final String CN_MAIN_AMOUNT_DUE = "cn.amtdue as amtdue";    
    public static final String CN_MAIN_OPN_CREDIT_AMT_BASE = "cn.creditamtbase as creditamtbase";    
    public static final String CN_MAIN_CREDIT_AMT_BASE = "if(cn.creditamtbase=0,0,cn.creditamtbase) as creditamtbase";    
    public static final String CN_MAIN_OPN_TRANSACTION_AMT_BASE = "cn.transactionamtbase as transactionamtbase";    
    public static final String CN_MAIN_TRANSACTION_AMT_BASE = "cn.transactionamtbase as transactionamtbase";    
    public static final String CN_MAIN_CREDIT_AMT = "cn.creditamt as creditamt";    
    public static final String CN_MAIN_OPN_CREDIT_AMT = "cn.creditamt as creditamt";    
    public static final String CN_MAIN_OPN_DEBIT_AMT_BASE = "if(cn.debitamtbase = 0,0,cn.debitamtbase) as debitamtbase";        
    public static final String CN_MAIN_DEBIT_AMT_BASE = "if(cn.debitamtbase = 0,0,cn.debitamtbase) as debitamtbase";        
    public static final String CN_MAIN_DEBIT_AMT = "cn.debitamt as debitamt";   
    public static final String CN_MAIN_OPN_DEBIT_AMT = "cn.debitamt as debitamt";   
    public static final String CN_MAIN_OPN_KNOCK_OFF_AMT_BASE = "SUM(cn.koamtbase)  as koamtbase";            
    public static final String CN_MAIN_OPN_KNOCK_OFF_AMT = "SUM(cn.koamt)  as koamt";    
    public static final String CN_MAIN_CUST_ID = "cn.custid as custid";
    public static final String CN_MAIN_CUST_NAME = "cn.custname as custname";
    public static final String CN_MAIN_CUST_accCode = "cn.acccode as acccode";
    
    //FRX

    public static final String CN_FRX_KNOCK_OFF_AMT_BASE = "jed.amountinbase as koamtbase";            
    public static final String CN_FRX_KNOCK_OFF_AMT = "jed.amount as koamt";            
    public static final String CN_FRX_REF_ID = "cn.id as docid";
    public static final String CN_FRX_DOC_NUMBER = "cn.cnnumber as docno";
    public static final String CN_FRX_JEID = "jed.id as jeid";
    public static final String CN_FRX_MEMO = "acc.name as memo";
    public static final String CN_FRX_CURR_ID = "COALESCE(cn.currency,company.currency) as currid";
    public static final String CN_FRX_CURR_CODE = "COALESCE(cncurr.currencycode,compcurr.currencycode) as currcode";
    public static final String CN_FRX_CURR_SYMBOL = "COALESCE(cncurr.symbol,compcurr.symbol) as currsymbol";
    public static final String CN_FRX_JE_ENTRYNO = "je.entryno as jeentryno";
    public static final String CN_FRX_JE_ENTRYDATE = "je.entrydate as entrydate";
    public static final String CN_FRX_JE_EXT_CURR_RATE = "je.externalcurrencyrate as extcurrrate";
    public static final String CN_FRX_EXT_CURR_RATE = "CONCAT(1,' ',compcurr.currencycode,' = ',if(cn.currency=company.currency,1.0,je.externalcurrencyrate),' ',cncurr.currencycode) as exchangerate";
    public static final String CN_FRX_AMOUNT_DUE = "cn.amtdue as amtdue";    
    public static final String CN_FRX_CREDIT_AMT_BASE = "if(jed.debit='T',jed.amountinbase,0) as creditamtbase";    
    public static final String CN_FRX_TRANSACTION_AMT_BASE = "jed.amountinbase as transactionamtbase";    
    public static final String CN_FRX_CREDIT_AMT = "if(jed.debit='T',jed.amount,0) as creditamt";    
    public static final String CN_FRX_DEBIT_AMT_BASE = "if(jed.debit='T',0,jed.amountinbase) as debitamtbase";        
    public static final String CN_FRX_DEBIT_AMT = "if(jed.debit='T',0,jed.amount) as debitamt";   
    
    //KNOCK-OFF AMT
    public static final String CN_LDR_KNOCK_OFF_AMT = "SUM(ldr.amountincncurrency) as koamt";        
    public static final String CN_LDR_KNOCK_OFF_AMT_BASE = "SUM(ldr.amountincncurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(cn.company,cn.creationdate,cn.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";            
    public static final String CN_CNP_KNOCK_OFF_AMT = "SUM(cnp.paidamountinpaymentcurrency) as koamt";        
    public static final String CN_CNP_KNOCK_OFF_AMT_BASE = "SUM((cnp.paidamountinpaymentcurrency / cnp.exchangeratefortransaction)) as koamtbase";            
    public static final String CN_DIS_KNOCK_OFF_AMT = "SUM(COALESCE(if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount),0)) as koamt";        
    public static final String CN_DIS_KNOCK_OFF_AMT_BASE = "SUM((if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount))/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(cn.company,cn.creationdate,cn.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";            
    
    //Opening 
    public static final String CN_LDR_OPN_KNOCK_OFF_AMT = "SUM(ldr.amountincncurrency) as koamt";        
    public static final String CN_LDR_OPN_KNOCK_OFF_AMT_BASE = "SUM(ldr.amountincncurrency/COALESCE(exchangerate_calc(cn.company,cn.creationdate,cn.currency,company.currency),1)) as koamtbase";            
    public static final String CN_CNP_OPN_KNOCK_OFF_AMT = "SUM(cnp.paidamountdueinbasecurrency) as koamt";        
    public static final String CN_CNP_OPN_KNOCK_OFF_AMT_BASE = "SUM(cnp.paidamountdueinbasecurrency) as koamtbase";
    public static final String CN_DIS_OPN_KNOCK_OFF_AMT = "SUM(COALESCE(if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount),0)) as koamt";
    public static final String CN_DIS_OPN_KNOCK_OFF_AMT_BASE = "SUM((if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount))/COALESCE(exchangerate_calc(cn.company,cn.creationdate,cn.currency,company.currency),1)) as koamtbase";            
    
    
    //Debit Note
    
    public static final String DN_MAIN_KNOCK_OFF_AMT_BASE = "SUM(dn.koamtbase) as koamtbase";            
    public static final String DN_MAIN_KNOCK_OFF_AMT = "SUM(dn.koamt) as koamt";            
    public static final String DN_MAIN_REF_ID = "dn.docid as docid";
    public static final String DN_MAIN_DOC_NUMBER = "dn.docno as docno";
    public static final String DN_MAIN_JEID = "dn.jeid as jeid";
    public static final String DN_MAIN_MEMO = "dn.memo as memo";
    public static final String DN_MAIN_CURR_ID = "dn.currid as currid";
    public static final String DN_MAIN_CURR_CODE = "dn.currcode as currcode";
    public static final String DN_MAIN_CURR_SYMBOL = "dn.currsymbol as currsymbol";
    public static final String DN_MAIN_JE_ENTRYNO = "dn.jeentryno as jeentryno";
    public static final String DN_MAIN_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String DN_MAIN_JE_ENTRYDATE = "dn.entrydate as entrydate";
    public static final String DN_MAIN_OPN_JE_ENTRYDATE = "dn.entrydate as entrydate";
    public static final String DN_MAIN_JE_EXT_CURR_RATE = "dn.extcurrrate as extcurrrate";
    public static final String DN_MAIN_OPN_JE_EXT_CURR_RATE = "dn.extcurrrate as extcurrrate";
    public static final String DN_MAIN_EXT_CURR_RATE = "dn.exchangerate as exchangerate";
    public static final String DN_MAIN_OPN_EXT_CURR_RATE = "dn.exchangerate as exchangerate";
    public static final String DN_MAIN_AMOUNT_DUE = "dn.amtdue as amtdue";    
    public static final String DN_MAIN_OPN_CREDIT_AMT_BASE = "dn.creditamtbase as creditamtbase";    
    public static final String DN_MAIN_CREDIT_AMT_BASE = "dn.creditamtbase as creditamtbase";    
    public static final String DN_MAIN_OPN_TRANSACTION_AMT_BASE = "dn.transactionamtbase as transactionamtbase";    
    public static final String DN_MAIN_TRANSACTION_AMT_BASE = "dn.transactionamtbase as transactionamtbase";    
    public static final String DN_MAIN_CREDIT_AMT = "dn.creditamt as creditamt";    
    public static final String DN_MAIN_OPN_CREDIT_AMT = "dn.creditamt as creditamt";    
    public static final String DN_MAIN_OPN_DEBIT_AMT_BASE = "dn.debitamtbase as debitamtbase";        
    public static final String DN_MAIN_DEBIT_AMT_BASE = "dn.debitamtbase as debitamtbase";        
    public static final String DN_MAIN_DEBIT_AMT = "dn.debitamt as debitamt";   
    public static final String DN_MAIN_OPN_DEBIT_AMT = "dn.debitamt as debitamt";   
    public static final String DN_MAIN_OPN_KNOCK_OFF_AMT_BASE = "SUM(dn.koamtbase)  as koamtbase";            
    public static final String DN_MAIN_OPN_KNOCK_OFF_AMT = "SUM(dn.koamt)  as koamt";    
    public static final String DN_MAIN_CUST_ID = "dn.custid as custid";
    public static final String DN_MAIN_CUST_NAME = "dn.custname as custname";
    public static final String DN_MAIN_CUST_accCode = "dn.acccode as acccode";
    
    //KNOCK-OFF AMT
    public static final String DN_LDR_KNOCK_OFF_AMT = "SUM(ldr.amountindncurrency) as koamt";        
    public static final String DN_LDR_KNOCK_OFF_AMT_BASE = "SUM(ldr.amountindncurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(dn.company,dn.creationdate,dn.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";            
    public static final String DN_DNP_KNOCK_OFF_AMT = "SUM(dnp.paidamountinreceiptcurrency) as koamt";        
    public static final String DN_DNP_KNOCK_OFF_AMT_BASE = "SUM((dnp.paidamountinreceiptcurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(dn.company,dn.creationdate,dn.currency,company.currency),je.externalcurrencyrate),1))) as koamtbase";
    public static final String DN_DIS_KNOCK_OFF_AMT = "SUM(COALESCE(if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount),0)) as koamt";
    public static final String DN_DIS_KNOCK_OFF_AMT_BASE = "SUM((if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount))/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(dn.company,dn.creationdate,dn.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";

    //Opening
    public static final String DN_LDR_OPN_KNOCK_OFF_AMT = "SUM(ldr.amountindncurrency) as koamt";
    public static final String DN_LDR_OPN_KNOCK_OFF_AMT_BASE = "SUM(ldr.amountindncurrency/COALESCE(exchangerate_calc(dn.company,dn.creationdate,dn.currency,company.currency),1)) as koamtbase";
    public static final String DN_DNP_OPN_KNOCK_OFF_AMT = "SUM(dnp.paidamountinreceiptcurrency) as koamt";
    public static final String DN_DNP_OPN_KNOCK_OFF_AMT_BASE = "SUM((dnp.paidamountinreceiptcurrency/dnp.exchangeratefortransaction)) as koamtbase";
    public static final String DN_DIS_OPN_KNOCK_OFF_AMT = "SUM(COALESCE(if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount),0)) as koamt";
    public static final String DN_DIS_OPN_KNOCK_OFF_AMT_BASE = "SUM((if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount))/COALESCE(exchangerate_calc(dn.company,dn.creationdate,dn.currency,company.currency),1)) as koamtbase";

    
    //Payment
    
    public static final String PAYMENT_MAIN_TYPE = "py.type as type";
    public static final String PAYMENT_MAIN_KNOCK_OFF_AMT_BASE = "SUM(py.koamtbase) as koamtbase";            
    public static final String PAYMENT_MAIN_KNOCK_OFF_AMT = "SUM(py.koamt) as koamt";            
    public static final String PAYMENT_MAIN_REF_ID = "py.docid as docid";
    public static final String PAYMENT_MAIN_DOC_NUMBER = "py.docno as docno";
    public static final String PAYMENT_MAIN_JEID = "py.jeid as jeid";
    public static final String PAYMENT_MAIN_MEMO = "py.memo as memo";
    public static final String PAYMENT_MAIN_CURR_ID = "py.currid as currid";
    public static final String PAYMENT_MAIN_CURR_CODE = "py.currcode as currcode";
    public static final String PAYMENT_MAIN_CURR_SYMBOL = "py.currsymbol as currsymbol";
    public static final String PAYMENT_MAIN_JE_ENTRYNO = "py.jeentryno as jeentryno";
    public static final String PAYMENT_MAIN_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String PAYMENT_MAIN_JE_ENTRYDATE = "py.entrydate as entrydate";
    public static final String PAYMENT_MAIN_OPN_JE_ENTRYDATE = "py.entrydate as entrydate";
    public static final String PAYMENT_MAIN_JE_EXT_CURR_RATE = "py.extcurrrate as extcurrrate";
    public static final String PAYMENT_MAIN_OPN_JE_EXT_CURR_RATE = "py.extcurrrate as extcurrrate";
    public static final String PAYMENT_MAIN_EXT_CURR_RATE = "py.exchangerate as exchangerate";
    public static final String PAYMENT_MAIN_OPN_EXT_CURR_RATE = "py.exchangerate as exchangerate";
    public static final String PAYMENT_MAIN_AMOUNT_DUE = "py.amtdue as amtdue";    
    public static final String PAYMENT_MAIN_OPN_CREDIT_AMT_BASE = "py.creditamtbase as creditamtbase";    
    public static final String PAYMENT_MAIN_CREDIT_AMT_BASE = "py.creditamtbase as creditamtbase";    
    public static final String PAYMENT_MAIN_OPN_TRANSACTION_AMT_BASE = "py.transactionamtbase as transactionamtbase";    
    public static final String PAYMENT_MAIN_TRANSACTION_AMT_BASE = "py.transactionamtbase as transactionamtbase";    
    public static final String PAYMENT_MAIN_CREDIT_AMT = "py.creditamt as creditamt";    
    public static final String PAYMENT_MAIN_OPN_DEBIT_AMT_BASE = "py.debitamtbase as debitamtbase";        
    public static final String PAYMENT_MAIN_DEBIT_AMT_BASE = "py.debitamtbase as debitamtbase";        
    public static final String PAYMENT_MAIN_DEBIT_AMT = "py.debitamt as debitamt";   
    public static final String PAYMENT_MAIN_OPN_KNOCK_OFF_AMT_BASE = "SUM(py.koamtbase)  as koamtbase";            
    public static final String PAYMENT_MAIN_OPN_KNOCK_OFF_AMT = "SUM(py.koamt)  as koamt";    
    public static final String PAYMENT_MAIN_CUST_ID = "py.custid as custid";
    public static final String PAYMENT_MAIN_CUST_NAME = "py.custname as custname";
    public static final String PAYMENT_MAIN_CUST_accCode = "py.acccode as acccode";

    //Knock-Off Amount
    public static final String PAYMENT_CNP_KNOCK_OFF_AMT = "SUM(cnp.amountinpaymentcurrency) as koamt";
    public static final String PAYMENT_CNP_KNOCK_OFF_AMT_BASE = "SUM(cnp.amountinpaymentcurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(p.company,p.creationdate,p.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";
    public static final String PAYMENT_LDP_KNOCK_OFF_AMT = "SUM(ldp.amount) as koamt";
    public static final String PAYMENT_LDP_KNOCK_OFF_AMT_BASE = "SUM(ldp.amount/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(p.company,p.creationdate,p.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";
    public static final String PAYMENT_LDAP_KNOCK_OFF_AMT = "SUM(ldap.amountinpaymentcurrency) as koamt";
    public static final String PAYMENT_LDAP_KNOCK_OFF_AMT_BASE = "SUM(ldap.amountinpaymentcurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(p.company,p.creationdate,p.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";
    public static final String PAYMENT_LDPCN_KNOCK_OFF_AMT = "SUM(ldpcn.amount) as koamt";
    public static final String PAYMENT_LDPCN_KNOCK_OFF_AMT_BASE = "SUM(ldpcn.amount/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(p.company,p.creationdate,p.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";
    public static final String PAYMENT_PO_KNOCK_OFF_AMT = "SUM(jed.amount) as koamt";
    public static final String PAYMENT_PO_KNOCK_OFF_AMT_BASE = "SUM(jed.amount/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(p.company,p.creationdate,p.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";
    public static final String PAYMENT_SR_KNOCK_OFF_AMT = "SUM(sr.totalamount) as koamt";
    public static final String PAYMENT_SR_KNOCK_OFF_AMT_BASE = "SUM(sr.totalamountinbase) as koamtbase";
    

    
    //receipt
 

    public static final String RECEIPT_MAIN_KNOCK_OFF_AMT_BASE = "SUM(rc.koamtbase) as koamtbase";            
    public static final String RECEIPT_MAIN_KNOCK_OFF_AMT = "SUM(rc.koamt) as koamt";            
    public static final String RECEIPT_MAIN_REF_ID = "rc.docid as docid";
    public static final String RECEIPT_MAIN_DOC_NUMBER = "rc.docno as docno";
    public static final String RECEIPT_MAIN_JEID = "rc.jeid as jeid";
    public static final String RECEIPT_MAIN_MEMO = "rc.memo as memo";
    public static final String RECEIPT_MAIN_TYPE = "rc.type as type";
    public static final String RECEIPT_MAIN_CURR_ID = "rc.currid as currid";
    public static final String RECEIPT_MAIN_CURR_CODE = "rc.currcode as currcode";
    public static final String RECEIPT_MAIN_CURR_SYMBOL = "rc.currsymbol as currsymbol";
    public static final String RECEIPT_MAIN_JE_ENTRYNO = "rc.jeentryno as jeentryno";
    public static final String RECEIPT_MAIN_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String RECEIPT_MAIN_JE_ENTRYDATE = "rc.entrydate as entrydate";
    public static final String RECEIPT_MAIN_OPN_JE_ENTRYDATE = "rc.entrydate as entrydate";
    public static final String RECEIPT_MAIN_JE_EXT_CURR_RATE = "rc.extcurrrate as extcurrrate";
    public static final String RECEIPT_MAIN_OPN_JE_EXT_CURR_RATE = "rc.extcurrrate as extcurrrate";
    public static final String RECEIPT_MAIN_EXT_CURR_RATE = "rc.exchangerate as exchangerate";
    public static final String RECEIPT_MAIN_OPN_EXT_CURR_RATE = "rc.exchangerate as exchangerate";
    public static final String RECEIPT_MAIN_AMOUNT_DUE = "rc.amtdue as amtdue";    
    public static final String RECEIPT_MAIN_OPN_CREDIT_AMT_BASE = "rc.creditamtbase as creditamtbase";    
    public static final String RECEIPT_MAIN_CREDIT_AMT_BASE = "rc.creditamtbase as creditamtbase";    
    public static final String RECEIPT_MAIN_OPN_TRANSACTION_AMT_BASE = "rc.transactionamtbase as transactionamtbase";    
    public static final String RECEIPT_MAIN_TRANSACTION_AMT_BASE = "rc.transactionamtbase as transactionamtbase";    
    public static final String RECEIPT_MAIN_CREDIT_AMT = "rc.creditamt as creditamt";    
    public static final String RECEIPT_MAIN_OPN_CREDIT_AMT = "rc.creditamt as creditamt";    
    public static final String RECEIPT_MAIN_OPN_DEBIT_AMT_BASE = "rc.debitamtbase as debitamtbase";        
    public static final String RECEIPT_MAIN_DEBIT_AMT_BASE = "rc.debitamtbase as debitamtbase";        
    public static final String RECEIPT_MAIN_DEBIT_AMT = "rc.debitamt as debitamt";   
    public static final String RECEIPT_MAIN_OPN_DEBIT_AMT = "rc.debitamt as debitamt";   
    public static final String RECEIPT_MAIN_OPN_KNOCK_OFF_AMT_BASE = "SUM(rc.koamtbase)  as koamtbase";            
    public static final String RECEIPT_MAIN_OPN_KNOCK_OFF_AMT = "SUM(rc.koamt)  as koamt";    
    public static final String RECEIPT_MAIN_CUST_ID = "rc.custid as custid";
    public static final String RECEIPT_MAIN_CUST_NAME = "rc.custname as custname";
    public static final String RECEIPT_MAIN_CUST_accCode = "rc.acccode as acccode";
    
    
    //Knock-Off Amount
    public static final String RECEIPT_RDO_KNOCK_OFF_AMT_BASE = "SUM(ifnull(abs(rdo.amount),0)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency), je.externalcurrencyrate ),1)) as koamtbase";            
    public static final String RECEIPT_RDO_KNOCK_OFF_AMT = "SUM(ifnull(abs(rdo.amount),0)) as koamt";            
    public static final String RECEIPT_Detail_KNOCK_OFF_AMT = "if(ldr.id is null,rad.amount,0) as koamt";
    public static final String RECEIPT_Detail_KNOCK_OFF_AMT_BASE = "if(ldr.id is null,rad.amount,0)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1) as koamtbase";
    
    public static final String RECEIPT_LP_KNOCK_OFF_AMT = "SUM(ifnull(lp.amountinpaymentcurrency,0)) as koamt";
    public static final String RECEIPT_LP_KNOCK_OFF_AMT_BASE = "SUM(ifnull(lp.amountinpaymentcurrency,0)/COALESCE(if(je.externalcurrencyrate =0, exchangerate_calc(r.company,r.creationdate,r.currency,company.currency), je.externalcurrencyrate ),1)) as koamtbase";
    public static final String RECEIPT_LDR_KNOCK_OFF_AMT = "SUM(ifnull(ldr.amount,0)) as koamt";
    public static final String RECEIPT_LDR_KNOCK_OFF_AMT_BASE = "SUM(ifnull(ldr.amount,0)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)) as koamtbase";
    public static final String RECEIPT_RD_KNOCK_OFF_AMT = "SUM(ifnull(rd.amount,0)) as koamt";
    public static final String RECEIPT_RD_KNOCK_OFF_AMT_BASE = "SUM(ifnull(rd.amount,0)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)) as koamtbase";
    public static final String RECEIPT_RDP_KNOCK_OFF_AMT = "SUM(ifnull(rdp.amount,0)) as koamt";
    public static final String RECEIPT_RDP_KNOCK_OFF_AMT_BASE = "SUM(ifnull(rdp.amount,0)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)) as koamtbase";
    public static final String RECEIPT_ADV_KNOCK_OFF_AMT = "SUM(if(je.id is null,0,ifnull(adv.amount,0))) as koamt";
    public static final String RECEIPT_ADV_KNOCK_OFF_AMT_BASE = "SUM((ifnull(adv.amount,0))/COALESCE(if(je.externalcurrencyrate=0, exchangerate_calc(r.company,r.creationdate,r.currency,company.currency), je.externalcurrencyrate ),1)) as koamtbase";
    public static final String RECEIPT_RWO_KNOCK_OFF_AMT = "SUM(ifnull(rwo.writtenoffamountinreceiptcurrency,0)) as koamt";
    public static final String RECEIPT_RWO_KNOCK_OFF_AMT_BASE = "SUM(ifnull(rwo.writtenoffamountinbasecurrency,0)) as koamtbase";
    public static final String RECEIPT_SI_KNOCK_OFF_AMT = "SUM(ifnull(rd.amount,0)) as koamt";
    public static final String RECEIPT_SI_KNOCK_OFF_AMT_BASE = "SUM(ifnull(rd.amountinbasecurrency,0)) as koamtbase";
    
    public static final String RECEIPT_LP_OPN_KNOCK_OFF_AMT = "SUM(ifnull(lp.amountinpaymentcurrency,0)) as koamt";
    public static final String RECEIPT_LP_OPN_KNOCK_OFF_AMT_BASE = "SUM(ifnull(lp.amountinpaymentcurrency,0)/COALESCE(if(r.exchangerateforopeningtransaction =0, exchangerate_calc(r.company,r.creationdate,r.currency,company.currency), r.exchangerateforopeningtransaction ),1)) as koamtbase";
    public static final String RECEIPT_LDR_OPN_KNOCK_OFF_AMT = "SUM(ifnull(ldr.amount,0)) as koamt";
    public static final String RECEIPT_LDR_OPN_KNOCK_OFF_AMT_BASE = "SUM(ifnull(ldr.amount,0)/COALESCE(if(r.exchangerateforopeningtransaction =0, exchangerate_calc(r.company,r.creationdate,r.currency,company.currency), r.exchangerateforopeningtransaction ),1))  as koamtbase";
    public static final String RECEIPT_RD_OPN_KNOCK_OFF_AMT = "SUM(ifnull(rd.amount,0)) as koamt";
    public static final String RECEIPT_RD_OPN_KNOCK_OFF_AMT_BASE = "SUM(ifnull(rd.amount,0)/COALESCE(if(r.exchangerateforopeningtransaction =0, exchangerate_calc(r.company,r.creationdate,r.currency,company.currency), r.exchangerateforopeningtransaction ),1)) as koamtbase";
    public static final String RECEIPT_RDP_OPN_KNOCK_OFF_AMT = "SUM(ifnull(rdp.amount,0)) as koamt";
    public static final String RECEIPT_RDP_OPN_KNOCK_OFF_AMT_BASE = "SUM(ifnull(rdp.amount,0)/COALESCE(if(r.exchangerateforopeningtransaction =0 ,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency) ,r.exchangerateforopeningtransaction ),1)) as koamtbase";
    public static final String RECEIPT_ADV_OPN_KNOCK_OFF_AMT = "SUM(ifnull(adv.amount,0)) as koamt";
    public static final String RECEIPT_ADV_OPN_KNOCK_OFF_AMT_BASE = "SUM((ifnull(adv.amount,0))/COALESCE(exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),1)) as koamtbase";
    public static final String RECEIPT_RWO_OPN_KNOCK_OFF_AMT = "SUM(ifnull(rwo.writtenoffamountinreceiptcurrency,0)) as koamt";
    public static final String RECEIPT_RWO_OPN_KNOCK_OFF_AMT_BASE = "SUM(ifnull(rwo.writtenoffamountinbasecurrency,0)) as koamtbase";
    
    
    
    
     public static final String CASH_SALE = "Cash Sale";
     public static final String INV_Type = "if(invoice.cashtransaction = 1, '"+CASH_SALE+"','Sales Invoice') as type";
     public static final String INV_MAIN_Type = "i.type as type";
    
    
    // AR Query
    
    //INV Main 
    
    
    public static final String INV_MAIN_AR_Type = "i.type as type";
//    public static final String INV_MAIN_AR_Cust_Id = "i.custid as custid";
//    public static final String INV_MAIN_AR_Cust_Name = "i.custname as custname";
//    public static final String INV_MAIN_AR_Cust_Code = "i.acccode as acccode";
    public static final String INV_MAIN_AR_Cust_Curr = "i.custcurrency as custcurrency";
    public static final String INV_MAIN_AR_Cust_Alise = "i.CustAlise as CustAlise";
    public static final String INV_MAIN_AR_Term_Name = "i.TermName as TermName";
//    public static final String INV_MAIN_AR_REF_ID = "i.docid as docid";
//    public static final String INV_MAIN_AR_JEID = "i.jeid as jeid";
    public static final String INV_MAIN_AR_OPN_JEID = "i.jeid as jeid";
    public static final String INV_MAIN_AR_Amt = "i.Amount as Amount";
    public static final String INV_MAIN_AR_Amt_Base = "i.BaseAmount as BaseAmount";
    public static final String INV_MAIN_AR_OPN_Amt = "i.Amount as Amount";
    public static final String INV_MAIN_AR_OPN_Amt_Base = "i.BaseAmount as BaseAmount";
    public static final String INV_MAIN_AR_Without_Inventry = "i.withoutinventory as withoutinventory";
//    public static final String INV_MAIN_AR_JE_Createdon = "i.entrydate as entrydate";
    public static final String INV_MAIN_AR_OPN_JE_Createdon = "i.entrydate as entrydate";
    public static final String INV_MAIN_AR_Doc_Createdon = "i.INVcreatedon as INVcreatedon";
    public static final String INV_MAIN_AR_Opening_Trans = "i.openinginv as openinginv";
    public static final String INV_MAIN_AR_Company_Id = "i.CompanyId as CompanyId";
    public static final String INV_MAIN_AR_Company_Name = "i.CompanyName as CompanyName";
//    public static final String INV_MAIN_AR_DOC_NUMBER = "i.docno as docno";
//    public static final String INV_MAIN_AR_Trans_Curr = "i.invcurr as invcurr";
//    public static final String INV_MAIN_AR_Trans_CurrSymbol = "i.invcurrsymbol as invcurrsymbol";
    public static final String INV_MAIN_AR_Trans_CurrName = "i.invcurrname as invcurrname";
    public static final String INV_MAIN_AR_Ext_Curr_Rate = "i.externalcurrrrate as externalcurrrrate";
    public static final String INV_MAIN_AR_ExcahgeRate = "i.exchangerate as exchangerate";
    public static final String INV_MAIN_AR_OPN_Ext_Curr_Rate = "i.externalcurrrrate as externalcurrrrate";
    public static final String INV_MAIN_AR_OPN_ExcahgeRate = "i.exchangerate as exchangerate";
    public static final String INV_MAIN_AR_Ship_Date = "i.shipdate as shipdate";
    public static final String INV_MAIN_AR_Due_Date = "i.duedate as duedate";
//    public static final String INV_MAIN_AR_JE_ENTRYNO = "i.jeentryno as jeentryno";
//    public static final String INV_MAIN_AR_OPN_JE_ENTRYNO = "i.jeentryno as jeentryno";
    public static final String INV_MAIN_AR_MEMO = "i.memo as memo";
    public static final String INV_MAIN_AR_Sale_Per_Name = "i.salespersonname as salespersonname";
    public static final String INV_MAIN_AR_Sale_Per_Code = "i.salespersoncode as salespersoncode";
    public static final String INV_MAIN_AR_Sale_Per_Id = "i.salespersonid as salespersonid";
//    public static final String INV_MAIN_AR_KNOCK_OFF_AMT = "SUM(i.koamt) as koamt";        
//    public static final String INV_MAIN_AR_KNOCK_OFF_AMT_BASE = "SUM(i.koamtbase) as koamtbase";        
//    public static final String INV_MAIN_AR_OPN_KNOCK_OFF_AMT = "SUM(i.koamt) as koamt";        
//    public static final String INV_MAIN_AR_OPN_KNOCK_OFF_AMT_BASE = "SUM(i.koamtbase) as koamtbase";        
    
    
    
    
    //Inv Query
    
   
    public static final String INV_AR_Term_Name = "term.termname as TermName";
//    public static final String INV_AR_REF_ID = "invoice.id as docid";
//    public static final String INV_AR_JEID = "invoice.journalentry as jeid";
    public static final String INV_AR_OPN_JEID = "' ' as jeid";
    public static final String INV_AR_Amt = "invoice.invoiceamount as Amount";
    public static final String INV_AR_Amt_Base = "invoice.invoiceamountinbase as BaseAmount";
//    public static final String INV_AR_OPN_Amt = "invoice.originalopeningbalanceamount as Amount";
//    public static final String INV_AR_OPN_Amt_Base = "invoice.originalopeningbalancebaseamount as BaseAmount";
    public static final String INV_AR_OPN_Amt = "invoice.originalopeningbalanceamount as Amount";
    public static final String INV_AR_OPN_Amt_Base = "invoice.originalopeningbalancebaseamount as BaseAmount";
    public static final String INV_AR_Without_Inventry = "'false' as withoutinventory";
//    public static final String INV_AR_JE_Createdon = "je.createdon as entrydate";
    public static final String INV_AR_OPN_JE_Createdon = "' ' as entrydate";
    public static final String INV_AR_Doc_Createdon = "invoice.creationdate as INVcreatedon";
    public static final String INV_AR_Opening_Trans = "invoice.isopeningbalenceinvoice as openinginv";
    public static final String INV_AR_Company_Id = "invoice.company as CompanyId";
    public static final String INV_AR_Company_Name = "company.companyname as CompanyName";
//    public static final String INV_AR_DOC_NUMBER = "invoice.invoicenumber as docno";
//    public static final String INV_AR_Trans_Curr = "invoice.currency as invcurr";
//    public static final String INV_AR_Trans_CurrSymbol = "invcurr.symbol as invcurrsymbol";
    public static final String INV_AR_Trans_CurrName = "invcurr.name as invcurrname";
    public static final String INV_AR_Ext_Curr_Rate = "if(invoice.currency=company.currency,1,je.externalcurrencyrate) as externalcurrrrate";
    public static final String INV_AR_ExcahgeRate = "CONCAT(1,' ',compcurr.currencycode,' = ',if(invoice.currency=company.currency,1.0,je.externalcurrencyrate),' ',invcurr.currencycode) as exchangerate";
    public static final String INV_AR_OPN_Ext_Curr_Rate = "if(invoice.currency=company.currency,1,invoice.exchangerateforopeningtransaction) as externalcurrrrate";
    public static final String INV_AR_OPN_ExcahgeRate = "CONCAT(1,' ',compcurr.currencycode,' = ',if(invoice.currency=company.currency,1.0,invoice.exchangerateforopeningtransaction),' ',invcurr.currencycode) as exchangerate";
    public static final String INV_AR_Ship_Date = "invoice.shipdate as shipdate";
    public static final String INV_AR_Due_Date = "invoice.duedate as duedate";
//    public static final String INV_AR_JE_ENTRYNO = "je.entryno as jeentryno";
//    public static final String INV_AR_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String INV_AR_MEMO = "invoice.memo as memo";
    public static final String INV_AR_Sale_Per_Name = "COALESCE(masteritem.value,' ') as salespersonname";
    public static final String INV_AR_Sale_Per_Code = "COALESCE(masteritem.code,' ') as salespersoncode";
    public static final String INV_AR_Sale_Per_Id = "COALESCE(masteritem.id,' ') as salespersonid";
//    public static final String INV_AR_KNOCK_OFF_AMT = "0 as koamt";        
//    public static final String INV_AR_KNOCK_OFF_AMT_BASE = "0 as koamtbase";        
//    public static final String INV_AR_OPN_KNOCK_OFF_AMT = "0 as koamt";        
//    public static final String INV_AR_OPN_KNOCK_OFF_AMT_BASE = "0 as koamtbase";        
    
    
    // Inv KnockOff
    
//    public static final String INV_AR_CND_KNOCK_OFF_AMT_BASE = "ROUND(discount.amountinInvCurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(invoice.company,invoice.creationdate,invoice.currency, company.currency),je.externalcurrencyrate),1),compaccpreferences.amountdigitafterdecimal) as koamtbase";
//    public static final String INV_AR_CND_KNOCK_OFF_AMT = "discount.amountinInvCurrency as koamt";        
//    public static final String INV_AR_LDR_KNOCK_OFF_AMT_BASE = "ROUND(ldr.amountininvoicecurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(invoice.company,invoice.creationdate,invoice.currency, company.currency),je.externalcurrencyrate),1),compaccpreferences.amountdigitafterdecimal) as koamtbase";
//    public static final String INV_AR_LDR_KNOCK_OFF_AMT = "ldr.amountininvoicecurrency as koamt";        
//    public static final String INV_AR_RD_KNOCK_OFF_AMT_BASE = "ROUND(rd.amountininvoicecurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(invoice.company,invoice.creationdate,invoice.currency, company.currency),je.externalcurrencyrate),1),compaccpreferences.amountdigitafterdecimal) as koamtbase";            
//    public static final String INV_AR_RD_KNOCK_OFF_AMT = "rd.amountininvoicecurrency as koamt";        
//    public static final String INV_AR_WRITEOFF_KNOCK_OFF_AMT_BASE = "ROUND(iwo.writtenoffamountininvoicecurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(invoice.company,invoice.creationdate,account.currency, company.currency),je.externalcurrencyrate),1),compaccpreferences.amountdigitafterdecimal) as koamtbase";
//    public static final String INV_AR_WRITEOFF_KNOCK_OFF_AMT = "iwo.writtenoffamountininvoicecurrency as koamt";
//   
//    // Inv KnockOff Opening
//    
//    public static final String INV_AR_CND_OPN_KNOCK_OFF_AMT_BASE = "SUM(discount.amountinInvCurrency/COALESCE(exchangerate_calc(invoice.company,invoice.creationdate,invoice.currency, company.currency),1)) as koamtbase";
//    public static final String INV_AR_CND_OPN_KNOCK_OFF_AMT = "SUM(discount.amountinInvCurrency) as koamt";
//    public static final String INV_AR_LDR_OPN_KNOCK_OFF_AMT_BASE = "SUM(ldr.amountininvoicecurrency/COALESCE(exchangerate_calc(invoice.company,invoice.creationdate,invoice.currency, company.currency),1)) as koamtbase";
//    public static final String INV_AR_LDR_OPN_KNOCK_OFF_AMT = "SUM(ldr.amountininvoicecurrency) as koamt";
//    public static final String INV_AR_RD_OPN_KNOCK_OFF_AMT_BASE = "SUM(rd.amountdueinbasecurrency) as koamtbase";
//    public static final String INV_AR_RD_OPN_KNOCK_OFF_AMT ="SUM(rd.amountdueinbasecurrency) as koamt";
//    public static final String INV_AR_WRITEOFF_OPN_KNOCK_OFF_AMT_BASE = "SUM(iwo.writtenoffamountininvoicecurrency/COALESCE(exchangerate_calc(invoice.company,invoice.creationdate,invoice.currency, company.currency),1)) as koamtbase";
//    public static final String INV_AR_WRITEOFF_OPN_KNOCK_OFF_AMT = "SUM(iwo.writtenoffamountininvoicecurrency) as koamt";

    
    
    
    
    //CN Main
    
    public static final String CN_MAIN_AR_Type = "cn.type as type";
//    public static final String CN_MAIN_AR_Cust_Id = "cn.custid as custid";
//    public static final String CN_MAIN_AR_Cust_Name = "cn.custname as custname";
//    public static final String CN_MAIN_AR_Cust_Code = "cn.acccode as acccode";
    public static final String CN_MAIN_AR_Cust_Curr = "cn.custcurrency as custcurrency";
    public static final String CN_MAIN_AR_Cust_Alise = "cn.CustAlise as CustAlise";
    public static final String CN_MAIN_AR_Term_Name = "cn.TermName as TermName";
//    public static final String CN_MAIN_AR_REF_ID = "cn.docid as docid";
//    public static final String CN_MAIN_AR_JEID = "cn.jeid as jeid";
    public static final String CN_MAIN_AR_OPN_JEID = "cn.jeid as jeid";
    public static final String CN_MAIN_AR_Amt = "cn.Amount as Amount";
    public static final String CN_MAIN_AR_Amt_Base = "cn.BaseAmount as BaseAmount";
    public static final String CN_MAIN_AR_OPN_Amt = "cn.Amount as Amount";
    public static final String CN_MAIN_AR_OPN_Amt_Base = "cn.BaseAmount as BaseAmount";
    public static final String CN_MAIN_AR_Without_Inventry = "cn.withoutinventory as withoutinventory";
//    public static final String CN_MAIN_AR_JE_Createdon = "cn.entrydate as entrydate";
    public static final String CN_MAIN_AR_OPN_JE_Createdon = "cn.entrydate as entrydate";
    public static final String CN_MAIN_AR_Doc_Createdon = "cn.CNcreatedon as CNcreatedon";
    public static final String CN_MAIN_AR_Opening_Trans = "cn.openingcn as openingcn";
    public static final String CN_MAIN_AR_Company_Id = "cn.CompanyId as CompanyId";
    public static final String CN_MAIN_AR_Company_Name = "cn.CompanyName as CompanyName";
//    public static final String CN_MAIN_AR_DOC_NUMBER = "cn.docno as docno";
//    public static final String CN_MAIN_AR_Trans_Curr = "cn.cncurr as cncurr";
//    public static final String CN_MAIN_AR_Trans_CurrSymbol = "cn.cncurrsymbol as cncurrsymbol";
    public static final String CN_MAIN_AR_Trans_CurrName = "cn.cncurrname as cncurrname";
    public static final String CN_MAIN_AR_Ext_Curr_Rate = "cn.externalcurrrrate as externalcurrrrate";
    public static final String CN_MAIN_AR_ExcahgeRate = "cn.exchangerate as exchangerate";
    public static final String CN_MAIN_AR_OPN_Ext_Curr_Rate = "cn.externalcurrrrate as externalcurrrrate";
    public static final String CN_MAIN_AR_OPN_ExcahgeRate = "cn.exchangerate as exchangerate";
    public static final String CN_MAIN_AR_Ship_Date = "cn.shipdate as shipdate";
    public static final String CN_MAIN_AR_Due_Date = "cn.duedate as duedate";
//    public static final String CN_MAIN_AR_JE_ENTRYNO = "cn.jeentryno as jeentryno";
//    public static final String CN_MAIN_AR_OPN_JE_ENTRYNO = "cn.jeentryno as jeentryno";
    public static final String CN_MAIN_AR_MEMO = "cn.memo as memo";
    public static final String CN_MAIN_AR_Sale_Per_Name = "cn.salespersonname as salespersonname";
    public static final String CN_MAIN_AR_Sale_Per_Code = "cn.salespersoncode as salespersoncode";
    public static final String CN_MAIN_AR_Sale_Per_Id = "cn.salespersonid as salespersonid";
//    public static final String CN_MAIN_AR_KNOCK_OFF_AMT = "SUM(cn.koamt) as koamt";        
//    public static final String CN_MAIN_AR_KNOCK_OFF_AMT_BASE = "SUM(cn.koamtbase) as koamtbase";        
//    public static final String CN_MAIN_AR_OPN_KNOCK_OFF_AMT = "SUM(cn.koamt) as koamt";        
//    public static final String CN_MAIN_AR_OPN_KNOCK_OFF_AMT_BASE = "SUM(cn.koamtbase) as koamtbase";        
    public static final String CN_MAIN_AR_CNTYPE = "cn.cntype as cntype";
    
    
    
    
    //CN Query
    
    public static final String CN_AR_Type = "'Credit Note' as type";
    public static final String CN_AR_Term_Name = "' ' as TermName";
//    public static final String CN_AR_REF_ID = "cn.id as docid";
//    public static final String CN_AR_JEID = "cn.journalentry as jeid";
    public static final String CN_AR_OPN_JEID = "' ' as jeid";
    public static final String CN_AR_Amt = "cn.cnamount as Amount";
    public static final String CN_AR_Amt_Base = "cn.cnamountinbase as BaseAmount";
    public static final String CN_AR_OPN_Amt = "cn.cnamount as Amount";
    public static final String CN_AR_OPN_Amt_Base = "cn.originalopeningbalancebaseamount as BaseAmount";
    public static final String CN_AR_Without_Inventry = "'false' as withoutinventory";
//    public static final String CN_AR_JE_Createdon = "je.createdon as entrydate";
    public static final String CN_AR_OPN_JE_Createdon = "' ' as entrydate";
    public static final String CN_AR_Doc_Createdon = "cn.creationdate as CNcreatedon";
    public static final String CN_AR_Opening_Trans = "cn.isopeningbalencecn as openingcn";
    public static final String CN_AR_Company_Id = "cn.company as CompanyId";
    public static final String CN_AR_Company_Name = "company.companyname as CompanyName";
//    public static final String CN_AR_DOC_NUMBER = "cn.cnnumber as docno";
//    public static final String CN_AR_Trans_Curr = "cn.currency as cncurr";
//    public static final String CN_AR_Trans_CurrSymbol = "cncurr.symbol as cncurrsymbol";
    public static final String CN_AR_Trans_CurrName = "cncurr.name as cncurrname";
    public static final String CN_AR_Ext_Curr_Rate = "if(cn.currency=company.currency,1,je.externalcurrencyrate) as externalcurrrrate";
    public static final String CN_AR_ExcahgeRate = "CONCAT(1,' ',compcurr.currencycode,' = ',if(cn.currency=company.currency,1.0,je.externalcurrencyrate),' ',cncurr.currencycode) as exchangerate";
    public static final String CN_AR_OPN_Ext_Curr_Rate = "if(cn.currency=company.currency,1,cn.exchangerateforopeningtransaction) as externalcurrrrate";
    public static final String CN_AR_OPN_ExcahgeRate = "CONCAT(1,' ',compcurr.currencycode,' = ',if(cn.currency=company.currency,1.0,cn.exchangerateforopeningtransaction),' ',cncurr.currencycode) as exchangerate";
    public static final String CN_AR_Ship_Date = "' ' as shipdate";
    public static final String CN_AR_Due_Date = "cn.creationdate as duedate";
//    public static final String CN_AR_JE_ENTRYNO = "je.entryno as jeentryno";
//    public static final String CN_AR_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String CN_AR_MEMO = "cn.memo as memo";
    public static final String CN_AR_Sale_Per_Name = "COALESCE(masteritem.value,' ') as salespersonname";
    public static final String CN_AR_Sale_Per_Code = "COALESCE(masteritem.code,' ') as salespersoncode";
    public static final String CN_AR_Sale_Per_Id = "COALESCE(masteritem.id,' ') as salespersonid";
//    public static final String CN_AR_KNOCK_OFF_AMT = "0 as koamt";        
//    public static final String CN_AR_KNOCK_OFF_AMT_BASE = "0 as koamtbase";        
//    public static final String CN_AR_OPN_KNOCK_OFF_AMT = "0 as koamt";        
//    public static final String CN_AR_OPN_KNOCK_OFF_AMT_BASE = "0 as koamtbase";        
    public static final String CN_AR_CNTYPE = "cn.cntype as cntype";
    
    //KnockOff
//    public static final String CN_AR_LDR_KNOCK_OFF_AMT = "SUM(ldr.amountincncurrency) as koamt";        
//    public static final String CN_AR_LDR_KNOCK_OFF_AMT_BASE = "SUM(ldr.amountincncurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(cn.company,cn.creationdate,cn.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";            
//    public static final String CN_AR_CNP_KNOCK_OFF_AMT = "SUM(cnp.paidamountinpaymentcurrency) as koamt";        
//    public static final String CN_AR_CNP_KNOCK_OFF_AMT_BASE = "SUM((cnp.paidamountinpaymentcurrency / cnp.exchangeratefortransaction)) as koamtbase";            
//    public static final String CN_AR_DIS_KNOCK_OFF_AMT = "SUM(COALESCE(if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount),0)) as koamt";        
//    public static final String CN_AR_DIS_KNOCK_OFF_AMT_BASE = "SUM((if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount))/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(cn.company,cn.creationdate,cn.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";            
//    
//    // CN KnockOff Opening
//    
//    public static final String CN_AR_LDR_OPN_KNOCK_OFF_AMT = "SUM(ldr.amountincncurrency) as koamt";        
//    public static final String CN_AR_LDR_OPN_KNOCK_OFF_AMT_BASE = "SUM(ldr.amountincncurrency/COALESCE(exchangerate_calc(cn.company,cn.creationdate,cn.currency,company.currency),1)) as koamtbase";            
//    public static final String CN_AR_CNP_OPN_KNOCK_OFF_AMT = "SUM(cnp.paidamountdueinbasecurrency) as koamt";        
//    public static final String CN_AR_CNP_OPN_KNOCK_OFF_AMT_BASE = "SUM(cnp.paidamountdueinbasecurrency) as koamtbase";
//    public static final String CN_AR_DIS_OPN_KNOCK_OFF_AMT = "SUM(COALESCE(if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount),0)) as koamt";
//    public static final String CN_AR_DIS_OPN_KNOCK_OFF_AMT_BASE = "SUM((if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount))/COALESCE(exchangerate_calc(cn.company,cn.creationdate,cn.currency,company.currency),1)) as koamtbase";            
    
    
    
    
    
    //DN Main
    
//    public static final String DN_MAIN_AR_Cust_Id = "dn.custid as custid";
//    public static final String DN_MAIN_AR_Cust_Name = "dn.custname as custname";
//    public static final String DN_MAIN_AR_Cust_Code = "dn.acccode as acccode";
    public static final String DN_MAIN_AR_Cust_Curr = "dn.custcurrency as custcurrency";
    public static final String DN_MAIN_AR_Cust_Alise = "dn.CustAlise as CustAlise";
    public static final String DN_MAIN_AR_Term_Name = "dn.TermName as TermName";
//    public static final String DN_MAIN_AR_REF_ID = "dn.docid as docid";
//    public static final String DN_MAIN_AR_JEID = "dn.jeid as jeid";
    public static final String DN_MAIN_AR_OPN_JEID = "dn.jeid as jeid";
    public static final String DN_MAIN_AR_Amt = "dn.Amount as Amount";
    public static final String DN_MAIN_AR_Amt_Base = "dn.BaseAmount as BaseAmount";
    public static final String DN_MAIN_AR_OPN_Amt = "dn.Amount as Amount";
    public static final String DN_MAIN_AR_OPN_Amt_Base = "dn.BaseAmount as BaseAmount";
    public static final String DN_MAIN_AR_Without_Inventry = "dn.withoutinventory as withoutinventory";
//    public static final String DN_MAIN_AR_JE_Createdon = "dn.entrydate as entrydate";
    public static final String DN_MAIN_AR_OPN_JE_Createdon = "dn.entrydate as entrydate";
    public static final String DN_MAIN_AR_Doc_Createdon = "dn.DNcreatedon as DNcreatedon";
    public static final String DN_MAIN_AR_Opening_Trans = "dn.openingdn as openingdn";
    public static final String DN_MAIN_AR_Company_Id = "dn.CompanyId as CompanyId";
    public static final String DN_MAIN_AR_Company_Name = "dn.CompanyName as CompanyName";
    public static final String DN_MAIN_AR_Type = "dn.type as type";
//    public static final String DN_MAIN_AR_DOC_NUMBER = "dn.docno as docno";
//    public static final String DN_MAIN_AR_Trans_Curr = "dn.dncurr as dncurr";
//    public static final String DN_MAIN_AR_Trans_CurrSymbol = "dn.dncurrsymbol as dncurrsymbol";
    public static final String DN_MAIN_AR_Trans_CurrName = "dn.dncurrname as dncurrname";
    public static final String DN_MAIN_AR_Ext_Curr_Rate = "dn.externalcurrrrate as externalcurrrrate";
    public static final String DN_MAIN_AR_ExcahgeRate = "dn.exchangerate as exchangerate";
    public static final String DN_MAIN_AR_OPN_Ext_Curr_Rate = "dn.externalcurrrrate as externalcurrrrate";
    public static final String DN_MAIN_AR_OPN_ExcahgeRate = "dn.exchangerate as exchangerate";
    public static final String DN_MAIN_AR_Ship_Date = "dn.shipdate as shipdate";
    public static final String DN_MAIN_AR_Due_Date = "dn.duedate as duedate";
//    public static final String DN_MAIN_AR_JE_ENTRYNO = "dn.jeentryno as jeentryno";
//    public static final String DN_MAIN_AR_OPN_JE_ENTRYNO = "dn.jeentryno as jeentryno";
    public static final String DN_MAIN_AR_MEMO = "dn.memo as memo";
    public static final String DN_MAIN_AR_Sale_Per_Name = "dn.salespersonname as salespersonname";
    public static final String DN_MAIN_AR_Sale_Per_Code = "dn.salespersoncode as salespersoncode";
    public static final String DN_MAIN_AR_Sale_Per_Id = "dn.salespersonid as salespersonid";
//    public static final String DN_MAIN_AR_KNOCK_OFF_AMT = "SUM(dn.koamt) as koamt";        
//    public static final String DN_MAIN_AR_KNOCK_OFF_AMT_BASE = "SUM(dn.koamtbase) as koamtbase";        
//    public static final String DN_MAIN_AR_OPN_KNOCK_OFF_AMT = "SUM(dn.koamt) as koamt";        
//    public static final String DN_MAIN_AR_OPN_KNOCK_OFF_AMT_BASE = "SUM(dn.koamtbase) as koamtbase";        
    public static final String DN_MAIN_AR_CNTYPE = "dn.cntype as cntype";
    
    
    
    
    //DN Query
    
    public static final String DN_AR_Type = "'Debit Note' as type";
    public static final String DN_AR_Term_Name = "' ' as TermName";
//    public static final String DN_AR_REF_ID = "dn.id as docid";
//    public static final String DN_AR_JEID = "dn.journalentry as jeid";
    public static final String DN_AR_OPN_JEID = "' ' as jeid";
    public static final String DN_AR_Amt = "dn.dnamount as Amount";
    public static final String DN_AR_Amt_Base = "dn.dnamountinbase as BaseAmount";
    public static final String DN_AR_OPN_Amt = "dn.dnamount as Amount";
    public static final String DN_AR_OPN_Amt_Base = "dn.originalopeningbalancebaseamount as BaseAmount";
    public static final String DN_AR_Without_Inventry = "'false' as withoutinventory";
//    public static final String DN_AR_JE_Createdon = "je.createdon as entrydate";
    public static final String DN_AR_OPN_JE_Createdon = "' ' as entrydate";
    public static final String DN_AR_Doc_Createdon = "dn.creationdate as DNcreatedon";
    public static final String DN_AR_Opening_Trans = "dn.isopeningbalencedn as openingdn";
    public static final String DN_AR_Company_Id = "dn.company as CompanyId";
    public static final String DN_AR_Company_Name = "company.companyname as CompanyName";
//    public static final String DN_AR_DOC_NUMBER = "dn.dnnumber as docno";
//    public static final String DN_AR_Trans_Curr = "dn.currency as dncurr";
//    public static final String DN_AR_Trans_CurrSymbol = "dncurr.symbol as dncurrsymbol";
    public static final String DN_AR_Trans_CurrName = "dncurr.name as dncurrname";
    public static final String DN_AR_Ext_Curr_Rate = "if(dn.currency=company.currency,1,je.externalcurrencyrate) as externalcurrrrate";
    public static final String DN_AR_ExcahgeRate = "CONCAT(1,' ',compcurr.currencycode,' = ',if(dn.currency=company.currency,1.0,je.externalcurrencyrate),' ',dncurr.currencycode) as exchangerate";
    public static final String DN_AR_OPN_Ext_Curr_Rate = "if(dn.currency=company.currency,1,dn.exchangerateforopeningtransaction) as externalcurrrrate";
    public static final String DN_AR_OPN_ExcahgeRate = "CONCAT(1,' ',compcurr.currencycode,' = ',if(dn.currency=company.currency,1.0,dn.exchangerateforopeningtransaction),' ',dncurr.currencycode) as exchangerate";
    public static final String DN_AR_Ship_Date = "' ' as shipdate";
    public static final String DN_AR_Due_Date = "dn.creationdate as duedate";
//    public static final String DN_AR_JE_ENTRYNO = "je.entryno as jeentryno";
//    public static final String DN_AR_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String DN_AR_MEMO = "dn.memo as memo";
    public static final String DN_AR_Sale_Per_Name = "COALESCE(masteritem.value,' ') as salespersonname";
    public static final String DN_AR_Sale_Per_Code = "COALESCE(masteritem.code,' ') as salespersoncode";
    public static final String DN_AR_Sale_Per_Id = "COALESCE(masteritem.id,' ') as salespersonid";
//    public static final String DN_AR_KNOCK_OFF_AMT = "0 as koamt";        
//    public static final String DN_AR_KNOCK_OFF_AMT_BASE = "0 as koamtbase";        
//    public static final String DN_AR_OPN_KNOCK_OFF_AMT = "0 as koamt";        
//    public static final String DN_AR_OPN_KNOCK_OFF_AMT_BASE = "0 as koamtbase";        
    public static final String DN_AR_CNTYPE = "dn.dntype as cntype";
    
    //KnockOff
//    public static final String DN_AR_LDR_KNOCK_OFF_AMT = "SUM(ldr.amountindncurrency) as koamt";        
//    public static final String DN_AR_LDR_KNOCK_OFF_AMT_BASE = "SUM(ldr.amountindncurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(dn.company,dn.creationdate,dn.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";            
//    public static final String DN_AR_DNP_KNOCK_OFF_AMT = "SUM(dnp.paidamountinreceiptcurrency) as koamt";        
//    public static final String DN_AR_DNP_KNOCK_OFF_AMT_BASE = "SUM((dnp.paidamountinreceiptcurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(dn.company,dn.creationdate,dn.currency,company.currency),je.externalcurrencyrate),1))) as koamtbase";
//    public static final String DN_AR_DIS_KNOCK_OFF_AMT = "SUM(COALESCE(if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount),0)) as koamt";
//    public static final String DN_AR_DIS_KNOCK_OFF_AMT_BASE = "SUM((if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount))/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(dn.company,dn.creationdate,dn.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";
//
//    //Knock Opening
//    
//    public static final String DN_AR_LDR_OPN_KNOCK_OFF_AMT = "SUM(ldr.amountindncurrency) as koamt";
//    public static final String DN_AR_LDR_OPN_KNOCK_OFF_AMT_BASE = "SUM(ldr.amountindncurrency/COALESCE(exchangerate_calc(dn.company,dn.creationdate,dn.currency,company.currency),1)) as koamtbase";
//    public static final String DN_AR_DNP_OPN_KNOCK_OFF_AMT = "SUM(dnp.paidamountinreceiptcurrency) as koamt";
//    public static final String DN_AR_DNP_OPN_KNOCK_OFF_AMT_BASE = "SUM((dnp.paidamountinreceiptcurrency/dnp.exchangeratefortransaction)) as koamtbase";
//    public static final String DN_AR_DIS_OPN_KNOCK_OFF_AMT = "SUM(COALESCE(if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount),0)) as koamt";
//    public static final String DN_AR_DIS_OPN_KNOCK_OFF_AMT_BASE = "SUM((if(discount.inpercent='T',((discount.origamount * discount.discount) / 100),discount.discount))/COALESCE(exchangerate_calc(dn.company,dn.creationdate,dn.currency,company.currency),1)) as koamtbase";

    
    
    //Payment Main
    
//    public static final String PAYMENT_MAIN_AR_Cust_Id = "py.custid as custid";
//    public static final String PAYMENT_MAIN_AR_Cust_Name = "py.custname as custname";
//    public static final String PAYMENT_MAIN_AR_Cust_Code = "py.acccode as acccode";
    public static final String PAYMENT_MAIN_AR_Cust_Curr = "py.custcurrency as custcurrency";
    public static final String PAYMENT_MAIN_AR_Cust_Alise = "py.CustAlise as CustAlise";
    public static final String PAYMENT_MAIN_AR_Term_Name = "py.TermName as TermName";
//    public static final String PAYMENT_MAIN_AR_REF_ID = "py.docid as docid";
//    public static final String PAYMENT_MAIN_AR_JEID = "py.jeid as jeid";
    public static final String PAYMENT_MAIN_AR_Amt = "py.Amount as Amount";
    public static final String PAYMENT_MAIN_AR_Amt_Base = "py.BaseAmount as BaseAmount";
    public static final String PAYMENT_MAIN_AR_Without_Inventry = "py.withoutinventory as withoutinventory";
//    public static final String PAYMENT_MAIN_AR_JE_Createdon = "py.entrydate as entrydate";
    public static final String PAYMENT_MAIN_AR_Doc_Createdon = "py.PYcreatedon as PYcreatedon";
    public static final String PAYMENT_MAIN_AR_Opening_Trans = "py.openingpy as openingpy";
    public static final String PAYMENT_MAIN_AR_Company_Id = "py.CompanyId as CompanyId";
    public static final String PAYMENT_MAIN_AR_Company_Name = "py.CompanyName as CompanyName";
    public static final String PAYMENT_MAIN_AR_Type = "py.type as type";
//    public static final String PAYMENT_MAIN_AR_DOC_NUMBER = "py.docno as docno";
//    public static final String PAYMENT_MAIN_AR_Trans_Curr = "py.pcurr as pcurr";
//    public static final String PAYMENT_MAIN_AR_Trans_CurrSymbol = "py.pcurrsymbol as pcurrsymbol";
    public static final String PAYMENT_MAIN_AR_Trans_CurrName = "py.pcurrname as pcurrname";
    public static final String PAYMENT_MAIN_AR_Ext_Curr_Rate = "py.externalcurrrrate as externalcurrrrate";
    public static final String PAYMENT_MAIN_AR_ExcahgeRate = "py.exchangerate as exchangerate";
    public static final String PAYMENT_MAIN_AR_Ship_Date = "py.shipdate as shipdate";
    public static final String PAYMENT_MAIN_AR_Due_Date = "py.duedate as duedate";
//    public static final String PAYMENT_MAIN_AR_JE_ENTRYNO = "py.jeentryno as jeentryno";
    public static final String PAYMENT_MAIN_AR_MEMO = "py.memo as memo";
    public static final String PAYMENT_MAIN_AR_Sale_Per_Name = "py.salespersonname as salespersonname";
    public static final String PAYMENT_MAIN_AR_Sale_Per_Code = "py.salespersoncode as salespersoncode";
    public static final String PAYMENT_MAIN_AR_Sale_Per_Id = "py.salespersonid as salespersonid";
//    public static final String PAYMENT_MAIN_AR_KNOCK_OFF_AMT = "SUM(py.koamt) as koamt";        
//    public static final String PAYMENT_MAIN_AR_KNOCK_OFF_AMT_BASE = "SUM(py.koamtbase) as koamtbase";        
    
    
    
    
    //PAYMENT Query
    
    public static final String PAYMENT_AR_Type = "'Payment Made' as type";
    public static final String PAYMENT_AR_Term_Name = "' ' as TermName";
//    public static final String PAYMENT_AR_REF_ID = "p.id as docid";
//    public static final String PAYMENT_AR_JEID = "p.journalentry as jeid";
    public static final String PAYMENT_AR_Amt = "p.depositamount as Amount";
    public static final String PAYMENT_AR_Amt_Base = "p.depositamountinbase as BaseAmount";
    public static final String PAYMENT_AR_Without_Inventry = "'false' as withoutinventory";
//    public static final String PAYMENT_AR_JE_Createdon = "je.createdon as entrydate";
    public static final String PAYMENT_AR_Doc_Createdon = "p.creationdate as PYcreatedon";
    public static final String PAYMENT_AR_Opening_Trans = "p.isopeningbalencepayment as openingpy";
    public static final String PAYMENT_AR_Company_Id = "p.company as CompanyId";
    public static final String PAYMENT_AR_Company_Name = "company.companyname as CompanyName";
//    public static final String PAYMENT_AR_DOC_NUMBER = "p.paymentnumber as docno";
//    public static final String PAYMENT_AR_Trans_Curr = "p.currency as pcurr";
//    public static final String PAYMENT_AR_Trans_CurrSymbol = "pcurr.symbol as pcurrsymbol";
    public static final String PAYMENT_AR_Trans_CurrName = "pcurr.name as pcurrname";
    public static final String PAYMENT_AR_Ext_Curr_Rate = "if(p.currency=company.currency,1,je.externalcurrencyrate) as externalcurrrrate";
    public static final String PAYMENT_AR_ExcahgeRate = "CONCAT(1,' ',compcurr.currencycode,' = ',if(p.currency=company.currency,1.0,je.externalcurrencyrate),' ',pcurr.currencycode) as exchangerate";
    public static final String PAYMENT_AR_Ship_Date = "' ' as shipdate";
    public static final String PAYMENT_AR_Due_Date = "p.creationdate as duedate";
//    public static final String PAYMENT_AR_JE_ENTRYNO = "je.entryno as jeentryno";
    public static final String PAYMENT_AR_MEMO = "p.memo as memo";
    public static final String PAYMENT_AR_Sale_Per_Name = "' ' as salespersonname";
    public static final String PAYMENT_AR_Sale_Per_Code = "' ' as salespersoncode";
    public static final String PAYMENT_AR_Sale_Per_Id = "' ' as salespersonid";
//    public static final String PAYMENT_AR_KNOCK_OFF_AMT = "0 as koamt";        
//    public static final String PAYMENT_AR_KNOCK_OFF_AMT_BASE = "0 as koamtbase";        
    
    //Knock-Off Amount
//    public static final String PAYMENT_AR_CNP_KNOCK_OFF_AMT = "SUM(cnp.amountinpaymentcurrency) as koamt";
//    public static final String PAYMENT_AR_CNP_KNOCK_OFF_AMT_BASE = "SUM(cnp.amountinpaymentcurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(p.company,p.creationdate,p.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";
//    public static final String PAYMENT_AR_LDP_KNOCK_OFF_AMT = "SUM(ldp.amount) as koamt";
//    public static final String PAYMENT_AR_LDP_KNOCK_OFF_AMT_BASE = "SUM(ldp.amount/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(p.company,p.creationdate,p.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";
//    public static final String PAYMENT_AR_LDAP_KNOCK_OFF_AMT = "SUM(ldap.amountinpaymentcurrency) as koamt";
//    public static final String PAYMENT_AR_LDAP_KNOCK_OFF_AMT_BASE = "SUM(ldap.amountinpaymentcurrency/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(p.company,p.creationdate,p.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";
//    public static final String PAYMENT_AR_LDPCN_KNOCK_OFF_AMT = "SUM(ldpcn.amount) as koamt";
//    public static final String PAYMENT_AR_LDPCN_KNOCK_OFF_AMT_BASE = "SUM(ldpcn.amount/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(p.company,p.creationdate,p.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";
//    public static final String PAYMENT_AR_PO_KNOCK_OFF_AMT = "SUM(jed.amount) as koamt";
//    public static final String PAYMENT_AR_PO_KNOCK_OFF_AMT_BASE = "SUM(jed.amount/COALESCE(if(je.externalcurrencyrate=0,exchangerate_calc(p.company,p.creationdate,p.currency,company.currency),je.externalcurrencyrate),1)) as koamtbase";
    
    
    
    //Receive Payment Main
    
    public static final String RECEIPT_MAIN_AR_Type = "rc.type as type";
//    public static final String RECEIPT_MAIN_AR_Cust_Id = "rc.custid as custid";
//    public static final String RECEIPT_MAIN_AR_Cust_Name = "rc.custname as custname";
//    public static final String RECEIPT_MAIN_AR_Cust_Code = "rc.acccode as acccode";
    public static final String RECEIPT_MAIN_AR_Cust_Curr = "rc.custcurrency as custcurrency";
    public static final String RECEIPT_MAIN_AR_Cust_Alise = "rc.CustAlise as CustAlise";
    public static final String RECEIPT_MAIN_AR_Term_Name = "rc.TermName as TermName";
//    public static final String RECEIPT_MAIN_AR_REF_ID = "rc.docid as docid";
//    public static final String RECEIPT_MAIN_AR_JEID = "rc.jeid as jeid";
    public static final String RECEIPT_MAIN_AR_OPN_JEID = "rc.jeid as jeid";
    public static final String RECEIPT_MAIN_AR_Amt = "rc.Amount as Amount";
    public static final String RECEIPT_MAIN_AR_Amt_Base = "rc.BaseAmount as BaseAmount";
    public static final String RECEIPT_MAIN_AR_OPN_Amt = "rc.Amount as Amount";
    public static final String RECEIPT_MAIN_AR_OPN_Amt_Base = "rc.BaseAmount as BaseAmount";
    public static final String RECEIPT_MAIN_AR_Without_Inventry = "rc.withoutinventory as withoutinventory";
//    public static final String RECEIPT_MAIN_AR_JE_Createdon = "rc.entrydate as entrydate";
    public static final String RECEIPT_MAIN_AR_OPN_JE_Createdon = "rc.entrydate as entrydate";
    public static final String RECEIPT_MAIN_AR_RECEIPT_Createdon = "rc.RCcreatedon as RCcreatedon";
    public static final String RECEIPT_MAIN_AR_Opening_rc = "rc.openingrc as openingrc";
    public static final String RECEIPT_MAIN_AR_Company_Id = "rc.CompanyId as CompanyId";
    public static final String RECEIPT_MAIN_AR_Company_Name = "rc.CompanyName as CompanyName";
//    public static final String RECEIPT_MAIN_AR_DOC_NUMBER = "rc.docno as docno";
//    public static final String RECEIPT_MAIN_AR_RECEIPT_Curr = "rc.rcurr as rcurr";
//    public static final String RECEIPT_MAIN_AR_RECEIPT_CurrSymbol = "rc.rcurrsymbol as rcurrsymbol";
    public static final String RECEIPT_MAIN_AR_RECEIPT_CurrName = "rc.rcurrname as rcurrname";
    public static final String RECEIPT_MAIN_AR_Ext_Curr_Rate = "rc.externalcurrrrate as externalcurrrrate";
    public static final String RECEIPT_MAIN_AR_ExcahgeRate = "rc.exchangerate as exchangerate";
    public static final String RECEIPT_MAIN_AR_OPN_Ext_Curr_Rate = "rc.externalcurrrrate as externalcurrrrate";
    public static final String RECEIPT_MAIN_AR_OPN_ExcahgeRate = "rc.exchangerate as exchangerate";
    public static final String RECEIPT_MAIN_AR_Ship_Date = "rc.shipdate as shipdate";
    public static final String RECEIPT_MAIN_AR_Due_Date = "rc.duedate as duedate";
//    public static final String RECEIPT_MAIN_AR_JE_ENTRYNO = "rc.jeentryno as jeentryno";
//    public static final String RECEIPT_MAIN_AR_OPN_JE_ENTRYNO = "rc.jeentryno as jeentryno";
    public static final String RECEIPT_MAIN_AR_MEMO = "rc.memo as memo";
    public static final String RECEIPT_MAIN_AR_Sale_Per_Name = "rc.salespersonname as salespersonname";
    public static final String RECEIPT_MAIN_AR_Sale_Per_Code = "rc.salespersoncode as salespersoncode";
    public static final String RECEIPT_MAIN_AR_Sale_Per_Id = "rc.salespersonid as salespersonid";
//    public static final String RECEIPT_MAIN_AR_KNOCK_OFF_AMT = "SUM(rc.koamt) as koamt";        
//    public static final String RECEIPT_MAIN_AR_KNOCK_OFF_AMT_BASE = "SUM(rc.koamtbase) as koamtbase";        
//    public static final String RECEIPT_MAIN_AR_OPN_KNOCK_OFF_AMT = "SUM(rc.koamt) as koamt";        
//    public static final String RECEIPT_MAIN_AR_OPN_KNOCK_OFF_AMT_BASE = "SUM(rc.koamtbase) as koamtbase";        
    
    
    
    
    //RECEIPT Query
    
    public static final String RECEIPT_AR_Type = "'Payment Received' as type";
    public static final String RECEIPT_AR_Term_Name = "' ' as TermName";
//    public static final String RECEIPT_AR_REF_ID = "r.id as docid";
//    public static final String RECEIPT_AR_JEID = "r.journalentry as jeid";
    public static final String RECEIPT_AR_OPN_JEID = "' ' as jeid";
    public static final String RECEIPT_AR_Amt = "rad.amount as Amount";
    public static final String RECEIPT_AR_Amt_Base = "rad.amount/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1) as BaseAmount";
    public static final String RECEIPT_AR_OPN_Amt = "r.depositamount as Amount";
    public static final String RECEIPT_AR_OPN_Amt_Base = "r.originalOpeningBalanceBaseAmount as BaseAmount";
    public static final String RECEIPT_AR_Without_Inventry = "'false' as withoutinventory";
//    public static final String RECEIPT_AR_JE_Createdon = "' ' as entrydate";
    public static final String RECEIPT_AR_OPN_JE_Createdon = "' ' as entrydate";
    public static final String RECEIPT_AR_RECEIPT_Createdon = "r.creationdate as RCcreatedon";
    public static final String RECEIPT_AR_Opening_rc = "r.isopeningbalencereceipt as openingrc";
    public static final String RECEIPT_AR_Company_Id = "r.company as CompanyId";
    public static final String RECEIPT_AR_Company_Name = "company.companyname as CompanyName";
//    public static final String RECEIPT_AR_DOC_NUMBER = "r.receiptnumber as docno";
//    public static final String RECEIPT_AR_RECEIPT_Curr = "r.currency as rcurr";
//    public static final String RECEIPT_AR_RECEIPT_CurrSymbol = "rcurr.symbol as rcurrsymbol";
    public static final String RECEIPT_AR_CurrName = "rcurr.name as rcurrname";
    public static final String RECEIPT_AR_Ext_Curr_Rate = "if(r.currency=company.currency,1,je.externalcurrencyrate) as externalcurrrrate";
    public static final String RECEIPT_AR_ExcahgeRate = "CONCAT(1,' ',compcurr.currencycode,' = ',if(r.currency=company.currency,1.0,je.externalcurrencyrate),' ',rcurr.currencycode) as exchangerate";
    public static final String RECEIPT_AR_OPN_Ext_Curr_Rate = "if(r.currency=company.currency,1,r.exchangerateforopeningtransaction) as externalcurrrrate";
    public static final String RECEIPT_AR_OPN_ExcahgeRate = "CONCAT(1,' ',compcurr.currencycode,' = ',if(r.currency=company.currency,1.0,r.exchangerateforopeningtransaction),' ',rcurr.currencycode) as exchangerate";
    public static final String RECEIPT_AR_Ship_Date = "' ' as shipdate";
    public static final String RECEIPT_AR_Due_Date = "r.creationdate as duedate";
//    public static final String RECEIPT_AR_JE_ENTRYNO = "je.entryno as jeentryno";
//    public static final String RECEIPT_AR_OPN_JE_ENTRYNO = "' ' as jeentryno";
    public static final String RECEIPT_AR_MEMO = "r.memo as memo";
    public static final String RECEIPT_AR_Sale_Per_Name = "' ' as salespersonname";
    public static final String RECEIPT_AR_Sale_Per_Code = "' ' as salespersoncode";
    public static final String RECEIPT_AR_Sale_Per_Id = "' ' as salespersonid";
//    public static final String RECEIPT_AR_KNOCK_OFF_AMT = "0 as koamt";        
//    public static final String RECEIPT_AR_KNOCK_OFF_AMT_BASE = "0 as koamtbase";        
//    public static final String RECEIPT_AR_OPN_KNOCK_OFF_AMT = "0 as koamt";        
//    public static final String RECEIPT_AR_OPN_KNOCK_OFF_AMT_BASE = "0 as koamtbase";        
    
    
    //LDR
    
//    public static final String RECEIPT_LDR_AR_Type = "'Payment Received' as type";
//    public static final String RECEIPT_LDR_AR_Term_Name = "' ' as TermName";
//    public static final String RECEIPT_LDR_AR_REF_ID = "r.id as docid";
//    public static final String RECEIPT_LDR_AR_JEID = "r.journalentry as jeid";
//    public static final String RECEIPT_LDR_OPN_AR_JEID = "' ' as jeid";
    public static final String RECEIPT_LDR_AR_Amt = "0 as Amount";
    public static final String RECEIPT_LDR_AR_Amt_Base = "0 as BaseAmount";
//    public static final String RECEIPT_LDR_AR_OPN_Amt = "r.depositamount as Amount";
//    public static final String RECEIPT_LDR_AR_OPN_Amt_Base = "r.originalOpeningBalanceBaseAmount as BaseAmount";
//    public static final String RECEIPT_LDR_AR_Without_Inventry = "'false' as withoutinventory";
//    public static final String RECEIPT_LDR_AR_JE_Createdon = "' ' as entrydate";
//    public static final String RECEIPT_LDR_AR_OPN_JE_Createdon = "' ' as entrydate";
//    public static final String RECEIPT_LDR_AR_RECEIPT_Createdon = "r.creationdate as RCcreatedon";
//    public static final String RECEIPT_LDR_AR_Opening_rc = "r.isopeningbalencereceipt as openingrc";
//    public static final String RECEIPT_LDR_AR_Company_Id = "r.company as CompanyId";
//    public static final String RECEIPT_LDR_AR_Company_Name = "company.companyname as CompanyName";
//    public static final String RECEIPT_LDR_AR_DOC_NUMBER = "r.receiptnumber as docno";
//    public static final String RECEIPT_LDR_AR_RECEIPT_Curr = "r.currency as rcurr";
//    public static final String RECEIPT_LDR_AR_RECEIPT_CurrSymbol = "rcurr.symbol as rcurrsymbol";
//    public static final String RECEIPT_LDR_AR_RECEIPT_CurrName = "rcurr.name as rcurrname";
//    public static final String RECEIPT_LDR_AR_Ext_Curr_Rate = "1 as externalcurrrrate";
//    public static final String RECEIPT_LDR_AR_ExcahgeRate = "1 as exchangerate";
//    public static final String RECEIPT_LDR_AR_OPN_Ext_Curr_Rate = "1 as externalcurrrrate";
//    public static final String RECEIPT_LDR_AR_OPN_ExcahgeRate = "1 as exchangerate";
//    public static final String RECEIPT_LDR_AR_Ship_Date = "' ' as shipdate";
//    public static final String RECEIPT_LDR_AR_Due_Date = "r.creationdate as duedate";
//    public static final String RECEIPT_LDR_AR_JE_ENTRYNO = "' ' as jeentryno";
//    public static final String RECEIPT_LDR_AR_OPN_JE_ENTRYNO = "' ' as jeentryno";
//    public static final String RECEIPT_LDR_AR_MEMO = "r.memo as memo";
//    public static final String RECEIPT_LDR_AR_Sale_Per_Name = "' ' as salespersonname";
//    public static final String RECEIPT_LDR_AR_Sale_Per_Code = "' ' as salespersoncode";
//    public static final String RECEIPT_LDR_AR_Sale_Per_Id = "' ' as salespersonid";
    public static final String RECEIPT_LDR_AR_KNOCK_OFF_AMT = "SUM(ifnull(ldr.amount,0)) as koamt";
    public static final String RECEIPT_LDR_AR_KNOCK_OFF_AMT_BASE = "SUM(ifnull(ldr.amount,0)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)) as koamtbase";
//    public static final String RECEIPT_LDR_AR_OPN_KNOCK_OFF_AMT = "SUM(ifnull(ldr.amount,0)) as koamt";
//    public static final String RECEIPT_LDR_AR_OPN_KNOCK_OFF_AMT_BASE = "SUM(ifnull(ldr.amount,0)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)) as koamountinbase";
    
    
    
    //Knock-Off Amount
//    public static final String RECEIPT_AR_LP_KNOCK_OFF_AMT = "SUM(ifnull(lp.amountinpaymentcurrency,0)) as koamt";
//    public static final String RECEIPT_AR_LP_KNOCK_OFF_AMT_BASE = "SUM(ifnull(lp.amountinpaymentcurrency,0)/COALESCE(if(je.externalcurrencyrate =0, exchangerate_calc(r.company,r.creationdate,r.currency,company.currency), je.externalcurrencyrate ),1)) as koamtbase";
////    public static final String RECEIPT_AR_LDR_KNOCK_OFF_AMT = "SUM(ifnull(ldr.amount,0)) as koamt";
////    public static final String RECEIPT_AR_LDR_KNOCK_OFF_AMT_BASE = "SUM(ifnull(ldr.amount,0)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)) as koamtbase";
//    public static final String RECEIPT_AR_RD_KNOCK_OFF_AMT = "SUM(ifnull(rd.amount,0)) as koamt";
//    public static final String RECEIPT_AR_RD_KNOCK_OFF_AMT_BASE = "SUM(ifnull(rd.amount,0)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)) as koamtbase";
//    public static final String RECEIPT_AR_RDP_KNOCK_OFF_AMT = "SUM(ifnull(rdp.amount,0)) as koamt";
//    public static final String RECEIPT_AR_RDP_KNOCK_OFF_AMT_BASE = "SUM(ifnull(rdp.amount,0)/COALESCE(if(je.externalcurrencyrate =0,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),je.externalcurrencyrate ),1)) as koamtbase";
//    public static final String RECEIPT_AR_ADV_KNOCK_OFF_AMT = "SUM(if(je.id is null,0,ifnull(adv.amount,0))) as koamt";
//    public static final String RECEIPT_AR_ADV_KNOCK_OFF_AMT_BASE = "SUM((ifnull(adv.amount,0))/COALESCE(if(je.externalcurrencyrate=0, exchangerate_calc(r.company,r.creationdate,r.currency,company.currency), je.externalcurrencyrate ),1)) as koamtbase";
//    public static final String RECEIPT_AR_RWO_KNOCK_OFF_AMT = "SUM(ifnull(rwo.writtenoffamountinreceiptcurrency,0)) as koamt";
//    public static final String RECEIPT_AR_RWO_KNOCK_OFF_AMT_BASE = "SUM(ifnull(rwo.writtenoffamountinbasecurrency,0)) as koamtbase";
//    public static final String RECEIPT_AR_SI_KNOCK_OFF_AMT = "SUM(ifnull(rd.amount,0)) as koamt";
//    public static final String RECEIPT_AR_SI_KNOCK_OFF_AMT_BASE = "SUM(ifnull(rd.amountinbasecurrency,0)) as koamtbase";
    
    //Knock-Off Opening Amount
    
    public static final String RECEIPT_AR_LP_OPN_KNOCK_OFF_AMT = "SUM(ifnull(lp.amountinpaymentcurrency,0)) as koamt";
    public static final String RECEIPT_AR_LP_OPN_KNOCK_OFF_AMT_BASE = "SUM(ifnull(lp.amountinpaymentcurrency,0)/COALESCE(if(r.exchangerateforopeningtransaction =0, exchangerate_calc(r.company,r.creationdate,r.currency,company.currency), r.exchangerateforopeningtransaction ),1)) as koamtbase";
    public static final String RECEIPT_AR_LDR_OPN_KNOCK_OFF_AMT = "SUM(ifnull(ldr.amount,0)) as koamt";
    public static final String RECEIPT_AR_LDR_OPN_KNOCK_OFF_AMT_BASE = "SUM(ifnull(ldr.amount,0)/COALESCE(if(r.exchangerateforopeningtransaction =0, exchangerate_calc(r.company,r.creationdate,r.currency,company.currency), r.exchangerateforopeningtransaction ),1))  as koamtbase";
    public static final String RECEIPT_AR_RD_OPN_KNOCK_OFF_AMT = "SUM(ifnull(rd.amount,0)) as koamt";
    public static final String RECEIPT_AR_RD_OPN_KNOCK_OFF_AMT_BASE = "SUM(ifnull(rd.amount,0)/COALESCE(if(r.exchangerateforopeningtransaction =0, exchangerate_calc(r.company,r.creationdate,r.currency,company.currency), r.exchangerateforopeningtransaction ),1)) as koamtbase";
    public static final String RECEIPT_AR_RDP_OPN_KNOCK_OFF_AMT = "SUM(ifnull(rdp.amount,0)) as koamt";
    public static final String RECEIPT_AR_RDP_OPN_KNOCK_OFF_AMT_BASE = "SUM(ifnull(rdp.amount,0)/COALESCE(if(r.exchangerateforopeningtransaction =0 ,exchangerate_calc(r.company,r.creationdate,r.currency,company.currency) ,r.exchangerateforopeningtransaction ),1)) as koamtbase";
    public static final String RECEIPT_AR_ADV_OPN_KNOCK_OFF_AMT = "SUM(ifnull(adv.amount,0)) as koamt";
    public static final String RECEIPT_AR_ADV_OPN_KNOCK_OFF_AMT_BASE = "SUM((ifnull(adv.amount,0))/COALESCE(exchangerate_calc(r.company,r.creationdate,r.currency,company.currency),1)) as koamtbase";
    public static final String RECEIPT_AR_RWO_OPN_KNOCK_OFF_AMT = "SUM(ifnull(rwo.writtenoffamountinreceiptcurrency,0)) as koamt";
    public static final String RECEIPT_AR_RWO_OPN_KNOCK_OFF_AMT_BASE = "SUM(ifnull(rwo.writtenoffamountinbasecurrency,0)) as koamtbase";

    
}