/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.handler;

import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author k3
 */
public class AccLinkDataDaoImpl extends BaseDAO implements AccLinkDataDao {

    @Override
    public KwlReturnObject getPurchaseRequisition(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        boolean includeVqlinkedPR = false;
        boolean includeRFQlinkedPR = false;
        boolean includePolinkedPR = false;
        
        if(requestParams.get("includeVqlinkedPR")!= null && !StringUtil.isNullOrEmpty(requestParams.get("includeVqlinkedPR").toString())){
            includeVqlinkedPR = Boolean.parseBoolean(requestParams.get("includeVqlinkedPR").toString());
        }
        if(requestParams.get("includeRFQlinkedPR")!= null && !StringUtil.isNullOrEmpty(requestParams.get("includeRFQlinkedPR").toString())){
            includeRFQlinkedPR = Boolean.parseBoolean(requestParams.get("includeRFQlinkedPR").toString());
        }
        if (requestParams.get("includePolinkedPR") != null && !StringUtil.isNullOrEmpty(requestParams.get("includePolinkedPR").toString())) {
            includePolinkedPR = Boolean.parseBoolean(requestParams.get("includePolinkedPR").toString());
        }

        String query = " select distinct prnumber from purchaserequisition prq inner join "
                + " ( ";
        if(includeVqlinkedPR){
           query += " select  prd.purchaserequisition as purchaserequisition from purchaserequisitiondetail prd inner join vendorquotationdetails vqd on prd.id =  vqd.purchaserequisitiondetailsid ";
        }
        if(includeRFQlinkedPR){
            if(includeVqlinkedPR){
                query += " union ";
            }
            query += " select prid as purchaserequisition from requestforquotationdetail ";
            if(requestParams.containsKey("rfqId") && requestParams.get("rfqId") != null && !StringUtil.isNullOrEmpty(requestParams.get("rfqId").toString())){
                query += " inner join requestforquotation on requestforquotation.id = requestforquotationdetail.requestforquotation where requestforquotation.id = ? ";
                params.add(requestParams.get("rfqId").toString());
            } else{
                query+= " where prid is NOT NULL and prid !='' ";
            }
        }
        
                
        /*--- Appending Query for fetching Requisition linked like PR->PO ----  */
        if (includePolinkedPR) {
            if (includeRFQlinkedPR || includeVqlinkedPR) {
                query += " union ";
            }

            query += " select  prd.purchaserequisition as purchaserequisition from purchaserequisitiondetail prd inner join podetails pod on prd.id =  pod.purchaserequisitiondetailid ";
        }
        
        query += " )  as prqv on prq.id = prqv.purchaserequisition "
        + " and deleteflag=? ";
        params.add(Constants.SQL_FALSE);
        
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( requisitiondate >=? and requisitiondate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        if (requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            query += " and prq.company=? ";
            params.add(requestParams.get("companyid").toString());
        }
        if (requestParams.get("isAdvanceSearch") != null && Boolean.parseBoolean(requestParams.get("isAdvanceSearch").toString())) {
            params = new ArrayList();
            query = " select distinct prq.prnumber,prq.id from purchaserequisition prq inner join purchaserequisitionlinking vql on vql.docid = prq.id where ";
            if (requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                query += "  prq.company=? ";
                params.add(requestParams.get("companyid").toString());
            }
            if (requestParams.containsKey("sourceflag") && requestParams.get("sourceflag") != null && !StringUtil.isNullOrEmpty(requestParams.get("sourceflag").toString())) {
                query += " and vql.sourceflag =? ";
                params.add(requestParams.get("sourceflag").toString());
            }
            if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null && !StringUtil.isNullOrEmpty(requestParams.get("moduleid").toString())) {
                query += " and vql.moduleid =? ";
                params.add(requestParams.get("moduleid").toString());
            }
        }
        query += " and prq.isfixedassetpurchaserequisition=? and prq.deleteflag =? and prq.approvestatuslevel=? ";
        params.add(Constants.SQL_FALSE);
        params.add(Constants.SQL_FALSE);
        params.add(Constants.INVOICEAPPROVED);
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getRFQ(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        String query = " select distinct rfqnumber from requestforquotation rfq inner join "
                + " ( select rfqd.requestforquotation from requestforquotationdetail rfqd inner join purchaserequisition prq on  prq.id = rfqd.prid "
                + " where prq.deleteflag =?  ";
        params.add(Constants.SQL_FALSE);
        if (requestParams.containsKey("number") && requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
            query += " and prq.prnumber = ? ";
            params.add(requestParams.get("number").toString());
        }
        if (requestParams.containsKey("prId") && requestParams.get("prId") != null && !StringUtil.isNullOrEmpty(requestParams.get("prId").toString())) {
            query += " and prq.id = ? ";
            params.add(requestParams.get("prId").toString());
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
             query += " and prq.company = ? ";
            params.add(requestParams.get("companyid").toString());
        }
        query += " ) as innertable on rfq.id = innertable.requestforquotation  ";
        query += " where rfq.deleteflag =? ";
        params.add(Constants.SQL_FALSE);
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( rfqdate >=? and rfqdate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        if (requestParams.get("isAdvanceSearch") != null && Boolean.parseBoolean(requestParams.get("isAdvanceSearch").toString())) {
            params = new ArrayList();
            query = " select distinct rfq.rfqnumber,rfq.id from requestforquotation rfq inner join requestforquotationlinking vql on vql.docid = rfq.id where ";
            if (requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                query += "  rfq.company=? ";
                params.add(requestParams.get("companyid").toString());
            }
            if (requestParams.containsKey("sourceflag") && requestParams.get("sourceflag") != null && !StringUtil.isNullOrEmpty(requestParams.get("sourceflag").toString())) {
                query += " and vql.sourceflag =? ";
                params.add(requestParams.get("sourceflag").toString());
            }
            if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null && !StringUtil.isNullOrEmpty(requestParams.get("moduleid").toString())) {
                query += " and vql.moduleid =? ";
                params.add(requestParams.get("moduleid").toString());
            }
        }
        query += " and rfq.isfixedassetrfq=? and rfq.deleteflag =? ";
        params.add(Constants.SQL_FALSE);
        params.add(Constants.SQL_FALSE);
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getVendorQuotation(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        boolean isVqWithinRange = false;
        if(requestParams.containsKey("isVqWithinRange") && requestParams.get("isVqWithinRange") != null){
            isVqWithinRange = (boolean) requestParams.get("isVqWithinRange");
        }
        String query = " select distinct vq.quotationnumber, vq.id from vendorquotation vq ";
        if (requestParams.containsKey("isFromRFQ") && requestParams.get("isFromRFQ") != null) {
            boolean isFromRFQ = (boolean) requestParams.get("isFromRFQ");
            if (isFromRFQ) {
                query += " inner join vendorquotationdetails vqd on vq.id = vqd.vendorquotation inner join "
                        + " ( select prd.id from purchaserequisitiondetail prd inner join purchaserequisition pr on prd.purchaserequisition = pr.id where pr.prnumber = ? and pr.company=? ) as innertable on innertable.id = vqd.purchaserequisitiondetailsid";
            }
            if (requestParams.containsKey("number") && requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
                params.add(requestParams.get("number").toString());
            }
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                params.add(requestParams.get("companyid").toString());
            }
        } else {
            if( requestParams.containsKey("companyid") && dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) ){
                query += " inner join ( ";
                if(isVqWithinRange){
                   query += " select vendorquotation from vendorquotationdetails vqd inner join ( "
                         + " select prd.id from purchaserequisitiondetail prd inner join purchaserequisition pr on  prd.purchaserequisition = pr.id ";
                   if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                      query += " where (pr.requisitiondate < ? or pr.requisitiondate > ?) ";
                       params.add(dateparams.get(Constants.REQ_startdate));
                       params.add(dateparams.get(Constants.REQ_enddate));
                   }
                   query += " )  as PrOutOfDate on PrOutOfDate.id = vqd.purchaserequisitiondetailsid ";
                } else{
                   query += " select vendorquotation from vendorquotationdetails where purchaserequisitiondetailsid =''  or purchaserequisitiondetailsid is NULL "; 
                }
                query += " ) as innertable on innertable.vendorquotation = vq.id  where vq.company= ? ";
                params.add(requestParams.get("companyid").toString());
            }else if (requestParams.get("isAdvanceSearch") != null && Boolean.parseBoolean(requestParams.get("isAdvanceSearch").toString())){
                query += " inner join vqlinking vql on vql.docid = vq.id ";
                if (requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                    query += " and vq.company=? ";
                    params.add(requestParams.get("companyid").toString());
                }
                if(requestParams.containsKey("sourceflag") && requestParams.get("sourceflag") != null && !StringUtil.isNullOrEmpty(requestParams.get("sourceflag").toString())){
                    query += " and vql.sourceflag =? ";
                    params.add(requestParams.get("sourceflag").toString());
                }
                if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null && !StringUtil.isNullOrEmpty(requestParams.get("moduleid").toString())) {
                    query += " and vql.moduleid =? ";
                    params.add(requestParams.get("moduleid").toString());
                }
            }
        }
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( quotationdate >=? and quotationdate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        if (requestParams.containsKey("linkedflag") && requestParams.get("linkedflag") != null) {
            query += " and vq.linkflag != ? ";
            params.add(requestParams.get("linkedflag"));
        }
        query += " and vq.isfixedassetvq=? and vq.deleteflag =? and vq.approvestatuslevel = ? ";
        params.add(Constants.SQL_FALSE);
        params.add(Constants.SQL_FALSE);
        params.add(Constants.INVOICEAPPROVED);
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getPurchaseOrder(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        boolean isStartFromPo = false;
        boolean POWithinDateRange = false;
        boolean isCallFromSO = false;
        boolean solinkedpo = false;
        boolean poWithinDateRange = false;
        boolean isLinkedInSO = false;
        boolean isFromPR =false;
        if (requestParams.containsKey("isStartFromPO") && requestParams.get("isStartFromPO") != null) {
            isStartFromPo = (boolean) requestParams.get("isStartFromPO");
        }
        if (requestParams.containsKey("POWithinDateRange") && requestParams.get("POWithinDateRange") != null) {
            POWithinDateRange = (boolean) requestParams.get("POWithinDateRange");
        }
        if(requestParams.containsKey("isCallFromSO") && requestParams.get("isCallFromSO") != null){
            isCallFromSO = (boolean) requestParams.get("isCallFromSO");
        }
        if(requestParams.containsKey("solinkedpo") && requestParams.get("solinkedpo") != null){
            solinkedpo = (boolean) requestParams.get("solinkedpo");
        }
        if(requestParams.containsKey("poWithinDateRange") && requestParams.get("poWithinDateRange") != null){
            poWithinDateRange = (boolean) requestParams.get("poWithinDateRange");
        }
        if(requestParams.containsKey("isLinkedInSO") && requestParams.get("isLinkedInSO") != null){
            isLinkedInSO = (boolean) requestParams.get("isLinkedInSO");
        }
        
        if (requestParams.containsKey("isFromPR") && requestParams.get("isFromPR") != null) {
            isFromPR = (boolean) requestParams.get("isFromPR");
        }
        
        ArrayList params = new ArrayList();
        String query = " select distinct ponumber, po.id from purchaseorder po ";
       
        /* ---Fetching PO linked with PR--------*/
        String condition = "";
        if (requestParams.containsKey("RequisitionNumber") && requestParams.get("RequisitionNumber") != null && !StringUtil.isNullOrEmpty(requestParams.get("RequisitionNumber").toString())) {
            condition += " pr.prnumber = ?  and ";
            params.add(requestParams.get("RequisitionNumber").toString());

        }
        
        if (isFromPR) {
            
            query += " inner join podetails pod on pod.purchaseorder = po.id "
                    + " inner join purchaserequisitiondetail prd on prd.id = pod.purchaserequisitiondetailid"
                    + " inner join purchaserequisition pr on pr.id = prd.purchaserequisition"
                    + " where "+ condition +" po.deleteflag = ? ";
            params.add(Constants.SQL_FALSE); //deleteflag   
        }else if (isStartFromPo) {
            if(POWithinDateRange){
                if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                    query += " inner join "
                          + " ( "
                          + "    select pod.purchaseorder from podetails pod inner join "
                          + "           ( "
                          + "               select vqd.id from vendorquotationdetails vqd inner join vendorquotation vq "
                          + "               on vq.id = vqd.vendorquotation "
                          + "               where vq.quotationdate < ? or vq.quotationdate > ? "
                          + "           ) as innertable on innertable.id = pod.vqdetail "
                          + " ) as polinked on polinked.purchaseorder = po.id  ";
                    params.add(dateparams.get(Constants.REQ_startdate));
                    params.add(dateparams.get(Constants.REQ_enddate));
                }
            } else if(isLinkedInSO){
                query += " inner join podetails pod on pod.purchaseorder = po.id where (salesorderdetailid!='' and salesorderdetailid is NOT NULL) ";
                if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                    query += " and pod.company=?";
                    params.add(requestParams.get("companyid").toString());
                }
            } else{
                if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                    query += " where id not in ( "
                            + " select purchaseorder from podetails where (vqdetail!='' and vqdetail is NOT NULL) or ( purchaserequisitiondetailid !='' and purchaserequisitiondetailid is NOT NULL) and company=? "
                            + " ) ";
                    params.add(requestParams.get("companyid").toString());
                }
            }
        } else if( isCallFromSO ){
                if( solinkedpo ){
                    query = " select distinct po.ponumber, po.id from salesorder so "
                          + " inner join sodetails sod on sod.salesorder = so.id"
                          + " inner join podetails pod on pod.id = sod.purchaseorderdetailid "
                          + " inner join purchaseorder po on po.id = pod.purchaseorder";
                    query += " where so.deleteflag = ? "; 
                    params.add(Constants.SQL_FALSE); //deleteflag

                } else{

                    query += " inner join podetails pod on pod.purchaseorder = po.id "
                          +  " inner join sodetails sod on sod.id = pod.salesorderdetailid"
                          +  " inner join salesorder so on so.id = sod.salesorder"
                          +  " where po.deleteflag = ? "; 
                        params.add(Constants.SQL_FALSE); //deleteflag
                }
                if(requestParams.containsKey("soNumber") && requestParams.get("soNumber") != null){
                    query += " and so.sonumber = ? ";
                    params.add(requestParams.get("soNumber")); 
                }
                if( poWithinDateRange ){
                    if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                        query += " and ( so.orderdate < ? or so.orderdate > ? )";
                        params.add(dateparams.get(Constants.REQ_startdate));
                        params.add(dateparams.get(Constants.REQ_enddate));
                    }
                }
            } else {
            if(requestParams.containsKey("number") && requestParams.containsKey("companyid")){
                query += " inner join ( select purchaseorder from podetails pod inner join "
                        + " (select vqd.id from vendorquotationdetails vqd inner join vendorquotation vq on vqd.vendorquotation = vq.id where vq.quotationnumber= ? and vq.company=? ) as innertable1 on innertable1.id = pod.vqdetail )"
                        + " as innertable2 on innertable2.purchaseorder = po.id ";
                if (requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
                    params.add(requestParams.get("number").toString());
                }
                if (requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                    params.add(requestParams.get("companyid").toString());
                }
            }else if (requestParams.get("isAdvanceSearch") != null && Boolean.parseBoolean(requestParams.get("isAdvanceSearch").toString())){
                query += " inner join polinking pl on pl.docid = po.id ";
                if(requestParams.containsKey("sourceflag") && requestParams.get("sourceflag") != null && !StringUtil.isNullOrEmpty(requestParams.get("sourceflag").toString())){
                    query += " and pl.sourceflag =? ";
                    params.add(requestParams.get("sourceflag").toString());
                }
                if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null && !StringUtil.isNullOrEmpty(requestParams.get("moduleid").toString())) {
                    query += " and pl.moduleid =? ";
                    params.add(requestParams.get("moduleid").toString());
                }
            }
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            query += " and po.company = ? ";
            params.add(requestParams.get("companyid").toString());
        }

        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( po.orderdate >=? and po.orderdate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        if (requestParams.containsKey("linkedflag") && requestParams.get("linkedflag") != null && !StringUtil.isNullOrEmpty(requestParams.get("linkedflag").toString())) {
            query += " and po.linkflag != ? ";
            params.add(requestParams.get("linkedflag"));
        }
        query += " and po.isfixedassetpo=? and po.isconsignment=? and po.deleteflag =? and po.approvestatuslevel =? ";
        params.add(Constants.SQL_FALSE);
        params.add(Constants.SQL_FALSE);
        params.add(Constants.SQL_FALSE);
        params.add(Constants.INVOICEAPPROVED);
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getPurchaseInvoice(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        List detailList = new ArrayList();
        boolean isStartFromPI = false;
        boolean InvoiceWithinDateRange = false;
        boolean excludeChecks = false;
        if (requestParams.containsKey("InvoiceWithinDateRange") && requestParams.get("InvoiceWithinDateRange") != null) {
            InvoiceWithinDateRange = (boolean) requestParams.get("InvoiceWithinDateRange");
        }
        if (requestParams.containsKey("isStartFromPI") && requestParams.get("isStartFromPI") != null) {
            isStartFromPI = (boolean) requestParams.get("isStartFromPI");
        }
        if (requestParams.containsKey("excludeChecks") && requestParams.get("excludeChecks") != null) {
            excludeChecks = (boolean) requestParams.get("excludeChecks");
        }
        try {
            String query = "select distinct grnumber, gr.id, gr.cashtransaction  from goodsreceipt gr";
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                query += " inner join journalentry je on gr.journalentry = je.id ";
            }
            if (requestParams.containsKey("isFromPO") && requestParams.get("isFromPO") != null) {
                boolean isFromPO = (boolean) requestParams.get("isFromPO");
                if (isFromPO && requestParams.containsKey("number") && requestParams.containsKey("companyid")) {
                    query += " inner join (select goodsreceipt from grdetails grd inner join "
                            + " (select pd.id from podetails pd inner join purchaseorder po on pd.purchaseorder = po.id where po.ponumber=? and po.company=? ) "
                            + " as innertable1 on innertable1.id = grd.purchaseorderdetail ) as innertable2 on innertable2.goodsreceipt = gr.id ";
                    if (requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
                        params.add(requestParams.get("number").toString());
                    }
                    if (requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                        params.add(requestParams.get("companyid").toString());
                    }
                }
            }
            if (requestParams.containsKey("isFromGR") && requestParams.get("isFromGR") != null) {
                boolean isFromGR = (boolean) requestParams.get("isFromGR");
                if (isFromGR && requestParams.containsKey("number") && requestParams.containsKey("companyid")) {
                    query += " inner join ( select goodsreceipt from grdetails where grorderdetails in "
                            + " (select id from grodetails where grorder in "
                            + " (select id from grorder where gronumber=? and company=?)) "
                            + " union select grd.goodsreceipt from grdetails grd inner join (select grod.videtails from grodetails grod inner join grorder gro on grod.grorder = gro.id where gro.gronumber =?  and grod.videtails!='' and grod.videtails IS NOT NULL and grod.company= ? ) "
                            + " as innertable1 on innertable1.videtails = grd.id "
                            + " ) as innertable3 on innertable3.goodsreceipt = gr.id ";
                    if (requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
                        params.add(requestParams.get("number").toString());
                    }
                    if (requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                        params.add(requestParams.get("companyid").toString());
                    }
                    if (requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
                        params.add(requestParams.get("number").toString());
                    }
                    if (requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                        params.add(requestParams.get("companyid").toString());
                    }
                }
            }
            if (requestParams.containsKey("isFromVQ") && requestParams.get("isFromVQ") != null) {
                boolean isFromVQ = (boolean) requestParams.get("isFromVQ");
                if (isFromVQ && requestParams.containsKey("number") && requestParams.containsKey("companyid") ) {
                    query += " inner join (select goodsreceipt from grdetails grd inner join "
                            + " (select vqd.id from vendorquotationdetails vqd inner join vendorquotation vq on vqd.vendorquotation = vq.id  where vq.quotationnumber=? and vq.company=? )as innertable1 on innertable1.id = grd.vendorquotationdetail )"
                            + " as innertable2 on innertable2.goodsreceipt = gr.id ";
                    if (requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
                        params.add(requestParams.get("number").toString());
                    }
                    if (requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                        params.add(requestParams.get("companyid").toString());
                    }
                }
            }
            if (requestParams.containsKey("isStartFromPI") && requestParams.get("isStartFromPI") != null) {
                if (isStartFromPI) {
                    String conditionQuery = "" ;
                    
                    if(InvoiceWithinDateRange){
                        conditionQuery += " inner join ( ";
                        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                            conditionQuery += " select goodsreceipt from grdetails grd inner join vendorquotationdetails vqd on grd.vendorquotationdetail = vqd.id "
                                           + " inner join vendorquotation vq on vq.id = vqd.vendorquotation where ( quotationdate < ? or quotationdate > ? ) ";
                            params.add(dateparams.get(Constants.REQ_startdate));
                            params.add(dateparams.get(Constants.REQ_enddate));

                            conditionQuery += " union select goodsreceipt from grdetails grd inner join podetails pod on grd.purchaseorderdetail = pod.id "
                                           + " inner join purchaseorder po on po.id = pod.purchaseorder where ( orderdate < ? or orderdate > ? ) ";
                            params.add(dateparams.get(Constants.REQ_startdate));
                            params.add(dateparams.get(Constants.REQ_enddate));

                            conditionQuery += " union select goodsreceipt from grdetails grd inner join grodetails grod on grd.grorderdetails = grod.id "
                                           + " inner join grorder gro on gro.id = grod.grorder where ( grorderdate < ? or grorderdate > ? ) ";
                            

                            params.add(dateparams.get(Constants.REQ_startdate));
                            params.add(dateparams.get(Constants.REQ_enddate));
                        }
                        conditionQuery += " ) as invoiceIds on invoiceIds.goodsreceipt = gr.id  where gr.company = ? ";
                        conditionQuery += " and invoiceIds.goodsreceipt not in( select grd.goodsreceipt from grdetails grd inner join grodetails grod on grod.videtails = grd.id where grod.videtails is NOT NULL and grod.company=? )";
                        params.add(requestParams.get("companyid").toString());
                        params.add(requestParams.get("companyid").toString());
                    } else {
                       
                        /* Fetching Invoice linked with child documents but invoice has not any parent i.e transaction is start from PI*/
                        /*1.Getting invoice linked with Payment, DN and this invoice has not any parent */
                        /*2.UNION:-Getting invoice linked with PR */
                        /* 3.UNION:-Getting Opening Invoice linked with Payment or DN*/
                        
                        conditionQuery += "where gr.id not in(select goodsreceipt from grdetails gd where ( gd.vendorquotationdetail!='' and gd.vendorquotationdetail is NOT NULL) "
                                + "or ( gd.grorderdetails!='' and gd.grorderdetails is NOT NULL) or (gd.purchaseorderdetail!='' and gd.purchaseorderdetail is NOT NULL) and gd.company=? "
                                + "union (select goodsreceipt from grdetails where id in (select videtails from grodetails grodet where grodet.videtails is NOT NULL ))) "
                                + "and gr.company=? and ((invoiceamountdue is NOT NULL  and  ROUND(invoiceamountdue) < ROUND(invoiceamount))) and ( je.entrydate >=? and je.entrydate <=? )"
                                + "and gr.isconsignment =? and gr.isfixedassetinvoice=? and gr.deleteflag =? and gr.approvestatuslevel = ? "
                                
                                + "union (SELECT distinct grnumber, gr.id, gr.cashtransaction from goodsreceipt gr inner join journalentry je on gr.journalentry = je.id where gr.id in(select goodsreceipt from grdetails gd "
                                + "inner join prdetails prd  where  prd.videtails = gd.id ) "
                                + "and gr.id not in(select goodsreceipt from grdetails gd where ( gd.vendorquotationdetail!='' and gd.vendorquotationdetail is NOT NULL) "
                                + "or ( gd.grorderdetails!='' and gd.grorderdetails is NOT NULL) or (gd.purchaseorderdetail!='' and gd.purchaseorderdetail is NOT NULL)) "
                                + " and gr.company=?  and ( je.entrydate >=? and je.entrydate <=? ) "
                                + "and gr.isconsignment =? and gr.isfixedassetinvoice=? and gr.deleteflag =? and gr.approvestatuslevel = ?) "
                               
                                + "UNION (SELECT distinct grnumber, gr.id, gr.cashtransaction from goodsreceipt gr "
                                + "where (openingbalanceamountdue is NOT NULL  and ROUND(openingbalanceamountdue) < ROUND(originalopeningbalanceamount) and isopeningbalenceinvoice=1) "
                                + "and  ( gr.creationdate >=? and gr.creationdate <=? ) and gr.isconsignment =? and gr.isfixedassetinvoice=? and gr.deleteflag =? and gr.approvestatuslevel = ? and gr.company=?) ";
                      
                        /* parameters for fetching normal invoice that linked with MP/DN */
                        params.add(requestParams.get("companyid").toString());
                        params.add(requestParams.get("companyid").toString());
                        params.add(dateparams.get(Constants.REQ_startdate));
                        params.add(dateparams.get(Constants.REQ_enddate));
                        params.add(Constants.SQL_FALSE);
                        params.add(Constants.SQL_FALSE);
                        params.add(Constants.SQL_FALSE);
                        params.add(Constants.INVOICEAPPROVED);

                        /* parameters for fetching normal invoice that linked with PR */
                        params.add(requestParams.get("companyid").toString());
                        params.add(dateparams.get(Constants.REQ_startdate));
                        params.add(dateparams.get(Constants.REQ_enddate));
                        params.add(Constants.SQL_FALSE);
                        params.add(Constants.SQL_FALSE);
                        params.add(Constants.SQL_FALSE);
                        params.add(Constants.INVOICEAPPROVED);

                        /* parameters for fetching opening invoice that linked with MP/DN */
                        params.add(dateparams.get(Constants.REQ_startdate));
                        params.add(dateparams.get(Constants.REQ_enddate));
                        params.add(Constants.SQL_FALSE);
                        params.add(Constants.SQL_FALSE);
                        params.add(Constants.SQL_FALSE);
                        params.add(Constants.INVOICEAPPROVED);
                        params.add(requestParams.get("companyid").toString());
                       
                    
                    }
                    
                    query += conditionQuery;
                }
            }
            
            if (requestParams.get("isAdvanceSearch") != null && Boolean.parseBoolean(requestParams.get("isAdvanceSearch").toString())){
                query += " inner join goodsreceiptlinking grol on grol.docid = gr.id ";
                if(requestParams.containsKey("sourceflag") && requestParams.get("sourceflag") != null && !StringUtil.isNullOrEmpty(requestParams.get("sourceflag").toString())){
                    query += " and grol.sourceflag =? ";
                    params.add(requestParams.get("sourceflag").toString());
                }
                if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null && !StringUtil.isNullOrEmpty(requestParams.get("moduleid").toString())) {
                    query += " and grol.moduleid =? ";
                    params.add(requestParams.get("moduleid").toString());
                }
                if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                    query += " and gr.company =? ";
                    params.add(requestParams.get("companyid").toString());
                }
            }
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null && !excludeChecks) {
                query += " and ( je.entrydate >=? and je.entrydate <=? )";
                params.add(dateparams.get(Constants.REQ_startdate));
                params.add(dateparams.get(Constants.REQ_enddate));
            }
            if (!excludeChecks) {
                query += " and gr.isconsignment =? and gr.isfixedassetinvoice=? and gr.deleteflag =? and gr.approvestatuslevel = ? ";
                params.add(Constants.SQL_FALSE);
                params.add(Constants.SQL_FALSE);
                params.add(Constants.SQL_FALSE);
                params.add(Constants.INVOICEAPPROVED);
            }
            detailList = executeSQLQuery( query, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(AccLinkDataDaoImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getGoodsReceipt(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        boolean isAdvanceSearch =  (requestParams.containsKey("isAdvanceSearch") && requestParams.get("isAdvanceSearch")!= null) ? Boolean.parseBoolean(requestParams.get("isAdvanceSearch").toString()) : false;
        String query = "select distinct gronumber, gro.id from grorder gro ";
        if (requestParams.containsKey("isFromPO") && requestParams.get("isFromPO") != null) {
            boolean isFromPO = (boolean) requestParams.get("isFromPO");
            if (isFromPO) {
                if(requestParams.containsKey("number") && requestParams.containsKey("companyid") ){
                    query += " inner join (select grorder from grodetails grod inner join "
                            + " ( select pd.id from podetails pd inner join purchaseorder po on pd.purchaseorder=po.id where po.ponumber=? and po.company=? ) "
                            + " as innertable1 on innertable1.id = grod.podetails ) as innertable2 on innertable2.grorder= gro.id";
                    if (requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
                        params.add(requestParams.get("number").toString());
                    }
                    if (requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                        params.add(requestParams.get("companyid").toString());
                    }
                }
            }
        }
        if (requestParams.containsKey("isFromPI") && requestParams.get("isFromPI") != null) {
            boolean isFromPI = (boolean) requestParams.get("isFromPI");
            if (isFromPI) {
                if(requestParams.containsKey("number") && requestParams.containsKey("companyid") ){
                    query += " inner join ( select grorder from grodetails grod inner join "
                            + " ( select grd.id from grdetails grd inner join goodsreceipt gr on  grd.goodsreceipt= gr.id where gr.grnumber=? and gr.company=? )"
                            + " as innertable1 on innertable1.id = grod.videtails ) as innertable2 on innertable2.grorder = gro.id ";
                    if (requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
                        params.add(requestParams.get("number").toString());
                    }
                    if (requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                        params.add(requestParams.get("companyid").toString());
                    }
                }
            }
        }
        if (requestParams.containsKey("isStartFromGR") && requestParams.get("isStartFromGR") != null) {
            boolean isStartFromGR = (boolean) requestParams.get("isStartFromGR");
            if (isStartFromGR) {
                query += " inner join ( ";
                if(requestParams.containsKey("GRWithinDateRange") && requestParams.get("GRWithinDateRange") != null){
                    
                    if (requestParams.containsKey("POModuleId") && requestParams.get("POModuleId") != null && !StringUtil.isNullOrEmpty(requestParams.get("POModuleId").toString())) {
                        query += " select distinct grol.docid from goodsreceiptorderlinking grol inner join purchaseorder po on po.id= grol.linkeddocid where moduleid = ?  ";
                        params.add(requestParams.get("POModuleId").toString());
                        
                        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                            query +=  " and po.company = ? " ;
                            params.add(requestParams.get("companyid").toString());
                        }
                        if (requestParams.containsKey("POsourceFlag") && requestParams.get("POsourceFlag") != null && !StringUtil.isNullOrEmpty(requestParams.get("POsourceFlag").toString())) {
                            query +=  " and grol.sourceflag = ? " ;
                            params.add(requestParams.get("POsourceFlag").toString());
                        }
                        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                            query += " and ( po.orderdate < ? or po.orderdate > ? )";
                            params.add(dateparams.get(Constants.REQ_startdate));
                            params.add(dateparams.get(Constants.REQ_enddate));
                        }
                    }
                } else{
                    query += " select distinct grol.docid from goodsreceiptorderlinking grol ";
                    int moduleid = -1;
                    if (requestParams.containsKey("invoiceModuleId") && requestParams.get("invoiceModuleId") != null && !StringUtil.isNullOrEmpty(requestParams.get("invoiceModuleId").toString())) {
                        moduleid = Integer.parseInt(requestParams.get("invoiceModuleId").toString());
                    }
                    if(moduleid==Constants.Acc_Vendor_Invoice_ModuleId){
                        query += " inner join goodsreceipt gr on gr.id = grol.linkeddocid ";
                    }else if(moduleid==Constants.Acc_Purchase_Return_ModuleId){
                        query += " inner join purchasereturn gr on gr.id = grol.linkeddocid ";
                    }
                    if (isAdvanceSearch){
                        query += " where gr.deleteflag = ? ";
                        params.add(Constants.SQL_FALSE);
                    }
                    
                    if (!isAdvanceSearch){
                        query += " inner join grdetails grd on grd.goodsreceipt = gr.id "
                            + " where ( grd.vendorquotationdetail='' or grd.vendorquotationdetail is NULL) and "
                            + " (grd.purchaseorderdetail='' or grd.purchaseorderdetail is NULL) ";
                    }
                    if (requestParams.containsKey("invoiceModuleId") && requestParams.get("invoiceModuleId") != null && !StringUtil.isNullOrEmpty(requestParams.get("invoiceModuleId").toString())) {
                        query +=  " and grol.moduleid = ? ";
                        params.add(requestParams.get("invoiceModuleId").toString());
                    }
                    if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                        query +=  " and gr.company = ? " ;
                        params.add(requestParams.get("companyid").toString());
                    }
                    if(requestParams.containsKey("sourceflag") && requestParams.get("sourceflag") != null && !StringUtil.isNullOrEmpty(requestParams.get("sourceflag").toString())){
                        query += " and grol.sourceflag =? ";
                        params.add(requestParams.get("sourceflag").toString());
                    }
                }
                query += "  ) as innertable on innertable.docid = gro.id where gro.deleteflag =?  ";
                params.add(Constants.SQL_FALSE);
            }
        }
       
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( gro.grorderdate >=? and gro.grorderdate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        if (requestParams.containsKey("isConsignment") && requestParams.get("isConsignment") != null) {
            query += " and gro.isconsignment = ? ";
            params.add(requestParams.get("isConsignment").toString());
        }
        if (requestParams.containsKey("isFixedassetGro") && requestParams.get("isFixedassetGro") != null) {
            query += " and gro.isfixedassetgro=? ";
            params.add(requestParams.get("isFixedassetGro"));
        }
        if (requestParams.containsKey("approveStatusLevel") && requestParams.get("approveStatusLevel") != null) {
            query += " and gro.approvestatuslevel=? ";
            params.add((int)requestParams.get("approveStatusLevel"));
        }
        if(requestParams.containsKey("excludePOLinkedGRO") && requestParams.get("excludePOLinkedGRO") != null){
            query += " and gro.id not in ( select linkeddocid from polinking ";
            if(requestParams.containsKey("groModuleId") && requestParams.get("groModuleId") != null){
                query += "where moduleid = ? ";
                params.add(requestParams.get("groModuleId").toString());
            }
            query += " ) ";
        }
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

     @Override
    public KwlReturnObject getPaymentInformation(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        boolean iscreditnote = false;
        String query = " select distinct paymentnumber from payment pay  ";
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " inner join journalentry je on pay.journalentry = je.id ";
        }
        if( requestParams.containsKey("iscreditnote") && requestParams.get("iscreditnote") != null ){
            iscreditnote = (boolean) requestParams.get("iscreditnote");
        }
        if(requestParams.containsKey("number") && requestParams.get("number") != null && requestParams.containsKey("companyid") && requestParams.get("companyid") != null ){
            query += " inner join (select pd.payment from paymentdetail pd inner join goodsreceipt gr on pd.goodsreceipt= gr.id where gr.grnumber=?  and gr.company=? "
                    + " union "
                    + " select lp.payment from linkdetailpayment lp inner join goodsreceipt gr on lp.goodsreceipt= gr.id where gr.grnumber=?  and gr.company=? "
                    + " ) as innertable on innertable.payment = pay.id ";
            params.add(requestParams.get("number").toString());
            params.add(requestParams.get("companyid").toString());
            params.add(requestParams.get("number").toString());
            params.add(requestParams.get("companyid").toString());
        }
        if(iscreditnote){
            query += " inner join creditnotpayment cnp on pay.id = cnp.paymentid ";
        }
        query += " where pay.deleteflag =? ";
        params.add(Constants.SQL_FALSE);
        if( requestParams.containsKey("companyid") && requestParams.get("companyid") != null ){
            query += " and pay.company = ? ";
            params.add(requestParams.get("companyid").toString());
        }
        if( requestParams.containsKey("excludePaymentsUsedInInvoice") && requestParams.get("excludePaymentsUsedInInvoice") != null ){
            query += " and pay.id not in ( ";
            query += " select distinct pd.payment from paymentdetail pd inner join goodsreceipt gr on pd.goodsreceipt= gr.id ";
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                query += "  inner join journalentry grJe on gr.journalentry = grJe.id ";
            }
            query += " where gr.deleteflag =? ";
            params.add(Constants.SQL_FALSE);
            if(requestParams.containsKey("companyid") && requestParams.get("companyid") != null){
                query += " and gr.company = ? ";
                params.add(requestParams.get("companyid").toString());
            }
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                query += " and ( grJe.entrydate >=? and grJe.entrydate <=? )";
                params.add(dateparams.get(Constants.REQ_startdate));
                params.add(dateparams.get(Constants.REQ_enddate));
            }
            query += " UNION ";
            query += " select distinct lp.payment from linkdetailpayment lp inner join goodsreceipt gr on lp.goodsreceipt= gr.id ";
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                query += "  inner join journalentry grJe1 on gr.journalentry = grJe1.id ";
            }
            query += " where gr.deleteflag =? ";
            params.add(Constants.SQL_FALSE);
            if(requestParams.containsKey("companyid") && requestParams.get("companyid") != null){
                query += " and gr.company = ? ";
                params.add(requestParams.get("companyid").toString());
            }
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                query += " and ( grJe1.entrydate >=? and grJe1.entrydate <=? )";
                params.add(dateparams.get(Constants.REQ_startdate));
                params.add(dateparams.get(Constants.REQ_enddate));
            }
            query += " ) ";
        }
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( je.entrydate >=? and je.entrydate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getDebitNote(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        String query = " select distinct dn.dnnumber,dn.id,dn.dntype from debitnote dn ";
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " inner join journalentry je on dn.journalentry = je.id ";
        }
        if(requestParams.containsKey("isFromPR") && requestParams.get("isFromPR") != null){
            query += " inner join purchasereturn pr on dn.purchasereturn = pr.id where pr.deleteflag = ? ";
            params.add(Constants.SQL_FALSE);
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                query += " and pr.company = ? ";
                params.add(requestParams.get("companyid").toString());
            }
            
            if (requestParams.containsKey("number") && requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
                query += " and pr.prnumber = ? ";
                params.add(requestParams.get("number").toString());
            }
        } else if(requestParams.containsKey("isFromPayment") && requestParams.get("isFromPayment") != null){
            query += " inner join debitnotepayment dnp on dn.id = dnp.dnid "
                  +  " inner join receipt rec on rec.id = dnp.receiptid where rec.deleteflag = ? ";
            params.add(Constants.SQL_FALSE);
        } else{
            query += " inner join ( select debitNote from dndetails dnd inner join goodsreceipt gdr on dnd.goodsreceipt= gdr.id where gdr.grnumber=? and gdr.company=? )"
                    + " as innertable on innertable.debitNote = dn.id where dn.deleteflag = ? ";
            if (requestParams.containsKey("number") && requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
                params.add(requestParams.get("number").toString());
            }
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                params.add(requestParams.get("companyid").toString());
            }
            params.add(Constants.SQL_FALSE);
        }
        if (requestParams.containsKey("paymentNumber") && requestParams.get("paymentNumber") != null && !StringUtil.isNullOrEmpty(requestParams.get("paymentNumber").toString())) {
            query += " and rec.receiptnumber = ? ";
            params.add(requestParams.get("paymentNumber").toString());
        }
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( je.entrydate >=? and je.entrydate <=? ) and je.approvestatuslevel = ? ";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
            params.add(Constants.INVOICEAPPROVED);
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            query += " and dn.company = ? ";
            params.add(requestParams.get("companyid").toString());
        }
        query += " and dn.deleteflag =? ";
        params.add(Constants.SQL_FALSE);
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getInvoiceJE(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        /*
         * je.id, je.entrydate and entryno are required by JE link click function to view JE details  ERP-22014
         */
        String query = " select entryno, je.id, je.entrydate from journalentry je inner join goodsreceipt gr on je.id = gr.journalentry where gr.grnumber= ? and gr.company = ? ";
        if (requestParams.containsKey("number") && requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
            params.add(requestParams.get("number").toString());
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            params.add(requestParams.get("companyid").toString());
        }
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( je.entrydate >=? and je.entrydate <=? ) and je.approvestatuslevel = ?  ";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
            params.add(Constants.INVOICEAPPROVED);
        }
        query += " and je.deleteflag =? ";
        params.add(Constants.SQL_FALSE);
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getPaymentJE(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        /*
         * je.id, je.entrydate and entryno are required by JE link click function to view JE details  ERP-22014
         */
        String query = " select entryno, je.id, je.entrydate from journalentry je inner join payment p on je.id = p.journalentry where p.paymentnumber= ? and  p.company= ? ";
        if (requestParams.containsKey("number") && requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
            params.add(requestParams.get("number").toString());
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            params.add(requestParams.get("companyid").toString());
        }
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( je.entrydate >=? and je.entrydate <=? ) and je.approvestatuslevel = ? ";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
            params.add(Constants.INVOICEAPPROVED);
        }
        query += " and je.deleteflag =? ";
        params.add(Constants.SQL_FALSE);
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getDebitNoteJE(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        /*
         * je.id, je.entrydate and entryno are required by JE link click function to view JE details  ERP-22014
         */
        String query = " select entryno, je.id, je.entrydate from journalentry je inner join debitnote dn on je.id = dn.journalentry where dn.dnnumber= ? and  dn.company= ? ";
        if (requestParams.containsKey("number") && requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
            params.add(requestParams.get("number").toString());
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            params.add(requestParams.get("companyid").toString());
        }
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( je.entrydate >=? and je.entrydate <=? ) and je.approvestatuslevel = ? ";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
            params.add(Constants.INVOICEAPPROVED);
        }
        query += " and je.deleteflag =? ";
        params.add(Constants.SQL_FALSE);
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getCqlinked(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        String query = " select distinct q.quotationnumber,cql.moduleid,cql.docid  from quotation q "
                + "inner join cqlinking cql on cql.docid = q.id and q.company=? and q.isleasequotation =? and q.quotationtype = ? and q.approvestatuslevel=? ";
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            params.add(requestParams.get("companyid").toString());
        }
        params.add(0);  // isleasequotation
        params.add(0);  // quotationtype
        params.add(Constants.INVOICEAPPROVED);
        if(requestParams.containsKey("sourceflag") && requestParams.get("sourceflag") != null && !StringUtil.isNullOrEmpty(requestParams.get("sourceflag").toString())){
            query += " and cql.sourceflag =? ";
            params.add(requestParams.get("sourceflag").toString());
        }
        if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null && !StringUtil.isNullOrEmpty(requestParams.get("moduleid").toString())) {
            query += " and cql.moduleid =? ";
            params.add(requestParams.get("moduleid").toString());
        }
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( quotationdate >=? and quotationdate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getSalesOrder(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        String query = "";
        String conditionQuery = "";
        boolean isCallFromPO = false;
        boolean poLinkedSo = false;
        boolean onlyPOLinkedSO = false;
        if(requestParams.containsKey("isCallFromPO") && requestParams.get("isCallFromPO") != null){
            isCallFromPO = (boolean) requestParams.get("isCallFromPO");
        }
        if(requestParams.containsKey("poLinkedSo") && requestParams.get("poLinkedSo") != null){
            poLinkedSo = (boolean) requestParams.get("poLinkedSo");
        }
        if(requestParams.containsKey("onlyPOLinkedSO") && requestParams.get("onlyPOLinkedSO") != null){
            onlyPOLinkedSO = (boolean) requestParams.get("onlyPOLinkedSO");
        }
        if (requestParams.containsKey("isStartFromSalesOrder") && requestParams.get("isStartFromSalesOrder") != null) {
            query += " select distinct innertable.docid, jointable.sonumber from salesorder jointable ";
            conditionQuery += " inner join  ( "
                           + " select sol.docid from solinking sol ";
            if(requestParams.containsKey("SalesOrderWithinDateRange") && requestParams.get("SalesOrderWithinDateRange") != null){
                if (requestParams.containsKey("SOModuleid") && requestParams.get("SOModuleid") != null) {
                    conditionQuery += "  inner join ( "
                                   + "  select cql.linkeddocid from cqlinking cql inner join quotation cq on cql.docid = cq.id where cql.moduleid = ? ";
                    params.add(requestParams.get("SOModuleid").toString());
                    if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                        conditionQuery += " and ( cq.quotationdate < ? or cq.quotationdate > ? ) ";
                        params.add(dateparams.get(Constants.REQ_startdate));
                        params.add(dateparams.get(Constants.REQ_enddate));
                    }
                    conditionQuery +=  " ) as outOfRangeCQ on outOfRangeCQ.linkeddocid = sol.docid ";
                }
                
            } else{
                if (requestParams.containsKey("SOModuleid") && requestParams.get("SOModuleid") != null && !StringUtil.isNullOrEmpty(requestParams.get("SOModuleid").toString())) {
                    conditionQuery += " where sol.docid not in( ";
                    conditionQuery += " select linkeddocid from cqlinking cq where cq.moduleid = ?  ";
                    conditionQuery += " ) ";
                    params.add(requestParams.get("SOModuleid").toString());
                     if (requestParams.containsKey("sourceflag") && requestParams.get("sourceflag") != null && !StringUtil.isNullOrEmpty(requestParams.get("sourceflag").toString())) {
                        conditionQuery+= " and sol.sourceflag = ? ";
                        params.add(requestParams.get("sourceflag").toString());
                    }
                }
            }
            conditionQuery += " ) as innertable on innertable.docid = jointable.id  where jointable.deleteflag = ? ";
            params.add(Constants.SQL_FALSE);
            query+=conditionQuery;
        } else if( isCallFromPO ){
                if( poLinkedSo ){
                    query = " select distinct sonumber from purchaseorder po "
                      +  " inner join podetails pod on pod.purchaseorder = po.id "
                      +  " inner join sodetails sod on sod.id = pod.salesorderdetailid"
                      +  " inner join salesorder jointable on jointable.id = sod.salesorder";
                    query += " where jointable.deleteflag = ? "; 
                    params.add(Constants.SQL_FALSE); //deleteflag
                } else{
                    query += " select distinct jointable.sonumber from salesorder jointable "
                          + " inner join sodetails sod on sod.salesorder = jointable.id"
                          + " inner join podetails pod on pod.id = sod.purchaseorderdetailid "
                          + " inner join purchaseorder po on po.id = pod.purchaseorder";
                    query += " where jointable.deleteflag = ? "; 
                    params.add(Constants.SQL_FALSE); //deleteflag
                }
                if(requestParams.containsKey("poNumber") && requestParams.get("poNumber") != null){
                    query += " and ponumber = ? ";
                    params.add(requestParams.get("poNumber")); 
                }
                if(requestParams.containsKey("soWithinDateRange") && requestParams.get("soWithinDateRange") != null){
                    if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                        query += " and ( po.orderdate < ? or po.orderdate > ? )";
                        params.add(dateparams.get(Constants.REQ_startdate));
                        params.add(dateparams.get(Constants.REQ_enddate));
                    }
                }
        }else if(onlyPOLinkedSO){
            query = " select distinct jointable.id,jointable.sonumber from salesorder jointable "
                  + " inner join sodetails sod on sod.salesorder = jointable.id "
                  + " where (purchaseorderdetailid!='' and purchaseorderdetailid is NOT NULL) ";
        } else {
            query = " select distinct sol.docid, jointable.sonumber from solinking sol  "
                    + " inner join salesorder jointable on sol.docid = jointable.id   "
                    + " where jointable.deleteflag = ? ";
            params.add(Constants.SQL_FALSE); //deleteflag
            if (requestParams.containsKey("sourceflag") && requestParams.get("sourceflag") != null && !StringUtil.isNullOrEmpty(requestParams.get("sourceflag").toString())) {
                query += " and sol.sourceflag = ? ";
                params.add(requestParams.get("sourceflag").toString());
            }
            if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null && !StringUtil.isNullOrEmpty(requestParams.get("moduleid").toString())) {
                query += " and moduleid = ? ";
                params.add(requestParams.get("moduleid").toString());
            }
            if (requestParams.containsKey("id") && requestParams.get("id") != null && !StringUtil.isNullOrEmpty(requestParams.get("id").toString())) {
                query += " and sol.linkeddocid = ? ";
                params.add(requestParams.get("id").toString());
            }
        }
        if (requestParams.containsKey("isConsignment") && requestParams.get("isConsignment") != null) {
            query += " and jointable.isconsignment = ? ";
            params.add(requestParams.get("isConsignment").toString());
        }
        if (requestParams.containsKey("isLeaseSO") && requestParams.get("isLeaseSO") != null ) {
            query += " and jointable.isleaseso = ? ";
            params.add((int)requestParams.get("isLeaseSO"));
        }
        if (requestParams.containsKey("isReplacementSo") && requestParams.get("isReplacementSo") != null) {
            query += " and jointable.isreplacementso = ? ";
            params.add((int)requestParams.get("isReplacementSo"));
        }
        if (requestParams.containsKey("leaseOrMaintenanceSO") && requestParams.get("leaseOrMaintenanceSO") != null) {
            query += " and jointable.leaseormaintenanceso = ? ";
            params.add((int)requestParams.get("leaseOrMaintenanceSO"));
        }
        if (requestParams.containsKey("approveStatusLevel") && requestParams.get("approveStatusLevel") != null) {
            query += " and jointable.approvestatuslevel = ? ";
            params.add((int)requestParams.get("approveStatusLevel"));
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            query += " and jointable.company = ? ";
            params.add(requestParams.get("companyid").toString());
        }
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( jointable.orderdate >= ? and jointable.orderdate <= ? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getDeliveryOrder(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        String query = "";
        String conditionQuery = "";
        if (requestParams.containsKey("isStartFromDeliveryOrder") && requestParams.get("isStartFromDeliveryOrder") != null) {
            query += " select distinct jointable.id, jointable.donumber from deliveryorder jointable "
                  + " inner join ( ";
                conditionQuery += " select dol.docid from dolinking dol ";
                if( requestParams.containsKey("DOWithinDateRange") && requestParams.get("DOWithinDateRange") != null){
                    if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                        conditionQuery = " select dol.docid from dolinking dol inner join solinking sol on sol.linkeddocid = dol.docid "
                                       + " inner join salesorder so on sol.docid = so.id where orderdate < ? or orderdate > ? ";
                        params.add(dateparams.get(Constants.REQ_startdate));
                        params.add(dateparams.get(Constants.REQ_enddate));
                        conditionQuery += " UNION select dol.docid from dolinking dol inner join invoicelinking invl on invl.linkeddocid = dol.docid "
                                       + " inner join invoice inv on invl.docid = inv.id inner join journalentry je on inv.journalentry = je.id where ( je.entrydate < ? or je.entrydate > ? ) ";
                        
                        params.add(dateparams.get(Constants.REQ_startdate));
                        params.add(dateparams.get(Constants.REQ_enddate));
                        
                    }
                } else{
                    if (requestParams.containsKey("DOModuleId") && requestParams.get("DOModuleId") != null) {
                        conditionQuery += " where dol.docid not in ("
                                + " select linkeddocid from solinking so where so.moduleid = ? ";
//                                + " union "
//                                + " select linkeddocid from invoicelinking invl where invl.moduleid = ?  ";
                        params.add(requestParams.get("DOModuleId").toString());
//                        params.add(requestParams.get("DOModuleId").toString());
//                        if(requestParams.containsKey("invoiceSourceFlag") && requestParams.get("invoiceSourceFlag") != null){
//                            conditionQuery += " and invl.sourceflag = ? ";
//                            params.add(requestParams.get("invoiceSourceFlag").toString());
//                        }
                        conditionQuery += " )  ";
//                        if(requestParams.containsKey("sourceflag") && requestParams.get("sourceflag") != null && !StringUtil.isNullOrEmpty(requestParams.get("sourceflag").toString())){
//                            conditionQuery += " and dol.sourceflag= ? ";
//                            params.add(requestParams.get("sourceflag").toString());
//                        }
                    }
                }
                if( requestParams.containsKey("includeInvoiceLinking") && requestParams.get("includeInvoiceLinking") != null){
                    conditionQuery += " UNION "
                                   + " select dol.docid from dolinking dol inner join ( "
                                   + " select invl.docid from invoicelinking invl ";
                    if(requestParams.containsKey("invoiceModuleId") && requestParams.get("invoiceModuleId") != null){
                        conditionQuery += " where invl.docid not in( "
                                       + " select linkeddocid  from solinking so where so.moduleid = ? "
                                       + " UNION "
                                       + " select linkeddocid from cqlinking cq where cq.moduleid =  ? ";
                        conditionQuery += " ) ";
                        params.add(requestParams.get("invoiceModuleId").toString());
                        params.add(requestParams.get("invoiceModuleId").toString());
                        
                        if(requestParams.containsKey("invoiceSourceFlag") && requestParams.get("invoiceSourceFlag") != null){
                            conditionQuery += "and invl.sourceflag = ? ";
                            params.add(requestParams.get("invoiceSourceFlag").toString());
                        }
                    }
                    
                    conditionQuery += " ) as DoLinkedWithInvoice on DoLinkedWithInvoice.docid = dol.linkeddocid ";
                    
                    if( requestParams.containsKey("invoiceModuleId") && requestParams.get("invoiceModuleId") != null){
                        conditionQuery += " where dol.moduleid = ? ";
                        params.add(requestParams.get("invoiceModuleId").toString());
                        
                        if( requestParams.containsKey("DOSourceFlag") && requestParams.get("DOSourceFlag") != null){
                            conditionQuery += " and dol.sourceflag = ? ";
                            params.add(requestParams.get("DOSourceFlag").toString());
                        }
                    }
                    
                }
            query += conditionQuery;
            query += " ) as innertable1 on innertable1.docid =  jointable.id where jointable.deleteflag=?  ";
            params.add(Constants.SQL_FALSE); //deleteflag
        } else {
            query = " select distinct sol.docid, jointable.donumber from dolinking sol  inner join deliveryorder jointable on sol.docid = jointable.id  where jointable.deleteflag=?  ";
            
            params.add(Constants.SQL_FALSE); //deleteflag
            if (requestParams.containsKey("id") && requestParams.get("id") != null && !StringUtil.isNullOrEmpty(requestParams.get("id").toString())) {
                query += " and sol.linkeddocid = ? ";
                params.add(requestParams.get("id").toString());
            }
            if(requestParams.containsKey("sourceflag") && requestParams.get("sourceflag") != null && !StringUtil.isNullOrEmpty(requestParams.get("sourceflag").toString())){
                query += " and sol.sourceflag =? ";
                params.add(requestParams.get("sourceflag").toString());
            }
            if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null && !StringUtil.isNullOrEmpty(requestParams.get("moduleid").toString())) {
                query += " and sol.moduleid =? ";
                params.add(requestParams.get("moduleid").toString());
            }
        }
        if (requestParams.containsKey("isConsignment") && requestParams.get("isConsignment") != null) {
            query += " and jointable.isconsignment = ? ";
            params.add(requestParams.get("isConsignment").toString());
        }
        if (requestParams.containsKey("isLeaseDO") && requestParams.get("isLeaseDO") != null ) {
            query += " and jointable.isleasedo = ? ";
            params.add((int)requestParams.get("isLeaseDO"));
        }
        if (requestParams.containsKey("isFixedAssetDO") && requestParams.get("isFixedAssetDO") != null) {
            query += " and jointable.isfixedassetdo = ? ";
            params.add((int)requestParams.get("isFixedAssetDO"));
        }
        if (requestParams.containsKey("approveStatusLevel") && requestParams.get("approveStatusLevel") != null) {
            query += " and jointable.approvestatuslevel = ? ";
            params.add((int)requestParams.get("approveStatusLevel"));
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            query += " and jointable.company = ? ";
            params.add(requestParams.get("companyid").toString());
        }
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( orderdate >=? and orderdate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getInvoices(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        String query = "";
        String conditionQuery = "";
        if (requestParams.containsKey("isStartFromInvoice") && requestParams.get("isStartFromInvoice") != null) {
            query = " select distinct jointable.id,jointable.invoicenumber, jointable.cashtransaction from invoice jointable ";
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                query += " inner join journalentry je on jointable.journalentry = je.id ";
            }
            query += " inner join ( ";
            
            if(requestParams.containsKey("InvoiceWithinDateRange") && requestParams.get("InvoiceWithinDateRange") != null){
                if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                    conditionQuery += " select invl.docid from invoicelinking invl inner join solinking sol on sol.linkeddocid = invl.docid inner join salesorder so on so.id = sol.docid where so.orderdate < ? or so.orderdate > ? ";
                    
                    params.add(dateparams.get(Constants.REQ_startdate));
                    params.add(dateparams.get(Constants.REQ_enddate));
                    
                    conditionQuery += " UNION select invl.docid from invoicelinking invl inner join dolinking dol on dol.linkeddocid = invl.docid inner join deliveryorder do on do.id = dol.docid where do.orderdate < ? or do.orderdate > ? ";
                    if (requestParams.containsKey("DOSourceFlag") && requestParams.get("DOSourceFlag") != null && !StringUtil.isNullOrEmpty(requestParams.get("DOSourceFlag").toString())) {
                        conditionQuery += " and dol.sourceflag = ? ";
                        params.add(requestParams.get("DOSourceFlag").toString());
                    }
                    params.add(dateparams.get(Constants.REQ_startdate));
                    params.add(dateparams.get(Constants.REQ_enddate));
                    
                    conditionQuery += " UNION select invl.docid from invoicelinking invl inner join cqlinking cql on cql.linkeddocid = invl.docid inner join quotation cq on cq.id = cql.docid where cq.quotationdate < ? or cq.quotationdate > ? ";
                    
                    params.add(dateparams.get(Constants.REQ_startdate));
                    params.add(dateparams.get(Constants.REQ_enddate));
                }
               
            }else{
                if(requestParams.containsKey("invoiceModuleId") && requestParams.get("invoiceModuleId") != null){
                    conditionQuery  += " select docid from invoicelinking invl "
                                    + " where invl.docid  not in "
                                    + " ( "
                                    + " select linkeddocid from solinking where moduleid = ? "
                                    + " union "
                                    + " select linkeddocid from dolinking where moduleid = ? "
                                    + " union "
                                    + " select linkeddocid from cqlinking where moduleid = ? "
                                    + " ) ";
                    params.add(requestParams.get("invoiceModuleId"));
                    params.add(requestParams.get("invoiceModuleId"));
                    params.add(requestParams.get("invoiceModuleId"));
                    if(requestParams.containsKey("invoiceSourceFlag") && requestParams.get("invoiceSourceFlag") != null){
                        conditionQuery += " and invl.sourceflag = ? ";
                        params.add(requestParams.get("invoiceSourceFlag"));
                    }
                }
            }
            if(requestParams.containsKey("addReceiptModuleCheck") && requestParams.get("addReceiptModuleCheck") != null){
                conditionQuery  += " union "
                                + " select inv1.id from invoice inv1 inner join receiptdetails resd on inv1.id = resd.invoice "
                                + " where resd.invoice IS NOT NULL and resd.invoice != '' ";
                if(requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())){
                    conditionQuery  += " and inv1.company = ? ";
                    params.add(requestParams.get("companyid"));
                }
                if(requestParams.containsKey("invoiceModuleId") && requestParams.get("invoiceModuleId") != null){
                    conditionQuery  += " and inv1.id not in ( "
                            + " select linkeddocid from solinking where moduleid = ? "
                            + " UNION "
                            + " select linkeddocid from dolinking where moduleid = ? "
                            + " UNION "
                            + " select linkeddocid from cqlinking where moduleid = ? "
                            + " ) ";
                    params.add(requestParams.get("invoiceModuleId"));
                    params.add(requestParams.get("invoiceModuleId"));
                    params.add(requestParams.get("invoiceModuleId"));
                }
            }
            query += conditionQuery;
            
            query += " ) as innertable on innertable.docid = jointable.id ";
            query += " where jointable.deleteflag=? ";
            params.add(Constants.SQL_FALSE); //deleteflag

        } else {
            query = " select distinct sol.docid, jointable.invoicenumber, jointable.cashtransaction from invoicelinking sol  inner join invoice jointable on sol.docid = jointable.id ";
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                query += " inner join journalentry je on jointable.journalentry = je.id ";
            }
            query += " where jointable.deleteflag=? ";
            
            params.add(Constants.SQL_FALSE); //deleteflag
            if (requestParams.containsKey("id") && requestParams.get("id") != null && !StringUtil.isNullOrEmpty(requestParams.get("id").toString())) {
                query += " and sol.linkeddocid = ?";
                params.add(requestParams.get("id").toString());
            }
            
            if(requestParams.containsKey("sourceflag") && requestParams.get("sourceflag") != null && !StringUtil.isNullOrEmpty(requestParams.get("sourceflag").toString())){
                query += " and sol.sourceflag =? ";
                params.add(requestParams.get("sourceflag").toString());
            }
            if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null && !StringUtil.isNullOrEmpty(requestParams.get("moduleid").toString())) {
                query += " and sol.moduleid =? ";
                params.add(requestParams.get("moduleid").toString());
            }
        }
        if (requestParams.containsKey("isConsignment") && requestParams.get("isConsignment") != null) {
            query += " and jointable.isconsignment = ? ";
            params.add(requestParams.get("isConsignment").toString());
        }
        if (requestParams.containsKey("isFixedAssetLeaseInvoice") && requestParams.get("isFixedAssetLeaseInvoice") != null ) {
            query += " and jointable.isfixedassetleaseinvoice = ? ";
            params.add(requestParams.get("isFixedAssetLeaseInvoice").toString());
        }
        if (requestParams.containsKey("isFixedAssetInvoice") && requestParams.get("isFixedAssetInvoice") != null) {
            query += " and jointable.isfixedassetinvoice = ? ";
            params.add(requestParams.get("isFixedAssetInvoice").toString());
        }
        if (requestParams.containsKey("approveStatusLevel") && requestParams.get("approveStatusLevel") != null) {
            query += " and jointable.approvestatuslevel = ? ";
            params.add((int)requestParams.get("approveStatusLevel"));
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            query += " and jointable.company = ? ";
            params.add(requestParams.get("companyid").toString());
        }
        if (requestParams.containsKey("isDraft") && requestParams.get("isDraft") != null) {
            query += " and jointable.isdraft = ? ";
            params.add(requestParams.get("isDraft").toString());
        }
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( je.entrydate >=? and je.entrydate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getCreditNote(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        String query = "";
        query = " select distinct jointable.id, jointable.cnnumber,jointable.cntype from creditnote jointable ";
        if(requestParams.containsKey("isFromSR") && requestParams.get("isFromSR") != null){
            query += " inner join salesreturn sr on jointable.salesreturn = sr.id ";
        }else if(requestParams.containsKey("isFromPayment") && requestParams.get("isFromPayment") != null){
            query += " inner join creditnotpayment cnp on jointable.id = cnp.cnid "
                  +  " inner join payment pay on pay.id = cnp.paymentid ";
        }else{
            query += " inner join creditnotelinking sol on sol.docid = jointable.id ";
        }
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " inner join journalentry je on jointable.journalentry = je.id ";
        }
        query += "  where jointable.deleteflag = ? ";
        params.add(Constants.SQL_FALSE);
        if (requestParams.containsKey("id") && requestParams.get("id") != null && !StringUtil.isNullOrEmpty(requestParams.get("id").toString())) {
            query += " and sol.linkeddocid = ? ";
            params.add(requestParams.get("id").toString());
        }
        if (requestParams.containsKey("sourceflag") && requestParams.get("sourceflag") != null && !StringUtil.isNullOrEmpty(requestParams.get("sourceflag").toString())) {
            query += " and sol.sourceflag = ? ";
            params.add(requestParams.get("sourceflag").toString());
        }
        if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null && !StringUtil.isNullOrEmpty(requestParams.get("moduleid").toString())) {
            query += " and moduleid = ? ";
            params.add(requestParams.get("moduleid").toString());
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            query += " and jointable.company = ? ";
            params.add(requestParams.get("companyid").toString());
        }
        if (requestParams.containsKey("number") && requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
            query += " and sr.srnumber = ? ";
            params.add(requestParams.get("number").toString());
        }
        if (requestParams.containsKey("paymentNumber") && requestParams.get("paymentNumber") != null && !StringUtil.isNullOrEmpty(requestParams.get("paymentNumber").toString())) {
            query += " and pay.paymentnumber = ? ";
            params.add(requestParams.get("paymentNumber").toString());
        }
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( je.entrydate >=? and je.entrydate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }
public KwlReturnObject getPaymentInformation1(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        String query = " select distinct paymentnumber from payment pay  ";
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " inner join journalentry je on pay.journalentry = je.id ";
        }
        boolean iscreditnote = false;
        if( requestParams.containsKey("iscreditnote") && requestParams.get("iscreditnote") != null ){
            iscreditnote = (boolean) requestParams.get("iscreditnote");
        }
        if(requestParams.containsKey("number") && requestParams.get("number") != null && requestParams.containsKey("companyid") && requestParams.get("companyid") != null ){
            query += " inner join (select pd.payment from paymentdetail pd inner join goodsreceipt gr on pd.goodsreceipt= gr.id where gr.grnumber=?  and gr.company=? "
                    + " union "
                    + " select lp.payment from linkdetailpayment lp inner join goodsreceipt gr on lp.goodsreceipt= gr.id where gr.grnumber=?  and gr.company=? "
                    + " ) as innertable on innertable.payment = pay.id ";
            params.add(requestParams.get("number").toString());
            params.add(requestParams.get("companyid").toString());
            params.add(requestParams.get("number").toString());
            params.add(requestParams.get("companyid").toString());
        }
        if(iscreditnote){
            query += " inner join creditnotpayment cnp on pay.id = cnp.paymentid ";
        }
        query += " where pay.deleteflag =? ";
        params.add(Constants.SQL_FALSE);
        if( requestParams.containsKey("companyid") && requestParams.get("companyid") != null ){
            query += " and pay.company = ? ";
            params.add(requestParams.get("companyid").toString());
        }
        if( requestParams.containsKey("excludePaymentsUsedInInvoice") && requestParams.get("excludePaymentsUsedInInvoice") != null ){
            query += " and pay.id not in ( ";
            query += " select distinct pd.payment from paymentdetail pd inner join goodsreceipt gr on pd.goodsreceipt= gr.id ";
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                query += "  inner join journalentry grJe on gr.journalentry = grJe.id ";
            }
            query += " where gr.deleteflag =? ";
            params.add(Constants.SQL_FALSE);
            if(requestParams.containsKey("companyid") && requestParams.get("companyid") != null){
                query += " and gr.company = ? ";
                params.add(requestParams.get("companyid").toString());
            }
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                query += " and ( grJe.entrydate >=? and grJe.entrydate <=? )";
                params.add(dateparams.get(Constants.REQ_startdate));
                params.add(dateparams.get(Constants.REQ_enddate));
            }
            query += " UNION ";
            query += " select distinct lp.payment from linkdetailpayment lp inner join goodsreceipt gr on lp.goodsreceipt= gr.id ";
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                query += "  inner join journalentry grJe1 on gr.journalentry = grJe1.id ";
            }
            query += " where gr.deleteflag =? ";
            params.add(Constants.SQL_FALSE);
            if(requestParams.containsKey("companyid") && requestParams.get("companyid") != null){
                query += " and gr.company = ? ";
                params.add(requestParams.get("companyid").toString());
            }
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                query += " and ( grJe1.entrydate >=? and grJe1.entrydate <=? )";
                params.add(dateparams.get(Constants.REQ_startdate));
                params.add(dateparams.get(Constants.REQ_enddate));
            }
            query += " ) ";
        }
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( je.entrydate >=? and je.entrydate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }
    @Override
    public KwlReturnObject getReceivedPayments(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        boolean isdebitnote = false;
        
        String query = " select distinct receiptnumber from receipt rec  ";
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " inner join journalentry je on rec.journalentry = je.id ";
        }
        if( requestParams.containsKey("isdebitnote") && requestParams.get("isdebitnote") != null ){
            isdebitnote = (boolean) requestParams.get("isdebitnote");
        }
        if(isdebitnote){
             query += " inner join debitnotepayment dnp on rec.id = dnp.receiptid";
        } else{
            query += " inner join (select rd.receipt from receiptdetails rd inner join invoice inv on rd.invoice= inv.id where inv.deleteflag = ? ";
                    params.add(Constants.SQL_FALSE);
                    if (requestParams.containsKey("invoiceid") && requestParams.get("invoiceid") != null && !StringUtil.isNullOrEmpty(requestParams.get("invoiceid").toString())) {
                        query += " and inv.id=? ";
                        params.add(requestParams.get("invoiceid").toString());
                    }
                    if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                        query += " and inv.company=?  ";
                        params.add(requestParams.get("companyid").toString());
                    }
            query += " UNION ";
            query += " select lp.receipt from linkdetailreceipt lp inner join invoice inv on lp.invoice= inv.id where inv.deleteflag = ?  ";
                    params.add(Constants.SQL_FALSE);
                    if (requestParams.containsKey("invoiceid") && requestParams.get("invoiceid") != null && !StringUtil.isNullOrEmpty(requestParams.get("invoiceid").toString())) {
                        query += " and inv.id=? ";
                        params.add(requestParams.get("invoiceid").toString());
                    }
                    if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                        query += " and inv.company=?  ";
                        params.add(requestParams.get("companyid").toString());
                    }
            query += " ) as innertable on innertable.receipt = rec.id ";
            
        }
        
        query += " where rec.deleteflag = ? ";
        params.add(Constants.SQL_FALSE);
        
        if( requestParams.containsKey("excludePaymentsUsedInInvoice") && requestParams.get("excludePaymentsUsedInInvoice") != null ){
            query += " and rec.id not in ( ";
            query += " select rd.receipt from receiptdetails rd inner join invoice inv on rd.invoice= inv.id ";
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                query += "  inner join journalentry inJe on inv.journalentry = inJe.id ";
            }
            query += " where inv.deleteflag =? ";
            params.add(Constants.SQL_FALSE);
            if(requestParams.containsKey("companyid") && requestParams.get("companyid") != null){
                query += " and inv.company = ? ";
                params.add(requestParams.get("companyid").toString());
            }
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                query += " and ( inJe.entrydate >=? and inJe.entrydate <=? )";
                params.add(dateparams.get(Constants.REQ_startdate));
                params.add(dateparams.get(Constants.REQ_enddate));
            }
            query += " UNION ";
            query += " select lp.receipt from linkdetailreceipt lp inner join invoice inv on lp.invoice= inv.id ";
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                query += "  inner join journalentry inJe1 on inv.journalentry = inJe1.id ";
            }
            query += " where inv.deleteflag =? ";
            params.add(Constants.SQL_FALSE);
            if(requestParams.containsKey("companyid") && requestParams.get("companyid") != null){
                query += " and inv.company = ? ";
                params.add(requestParams.get("companyid").toString());
            }
            if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
                query += " and ( inJe1.entrydate >=? and inJe1.entrydate <=? )";
                params.add(dateparams.get(Constants.REQ_startdate));
                params.add(dateparams.get(Constants.REQ_enddate));
            }
            query += " ) ";
        }
        
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( je.entrydate >=? and je.entrydate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            query += " and rec.company= ? ";
            params.add(requestParams.get("companyid").toString());
        }
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getSalesInvoiceJE(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        /*
         * je.id, je.entrydate and entryno are required by JE link click function to view JE details  ERP-22014
         */
        String query = " select distinct entryno, je.id, je.entrydate from journalentry je inner join invoice inv on je.id = inv.journalentry where inv.id= ? and inv.company = ? and je.approvestatuslevel=? ";
        if (requestParams.containsKey("id") && requestParams.get("id") != null && !StringUtil.isNullOrEmpty(requestParams.get("id").toString())) {
            params.add(requestParams.get("id").toString());
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            params.add(requestParams.get("companyid").toString());
        }
        params.add(Constants.INVOICEAPPROVED);
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( je.entrydate >=? and je.entrydate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        query += " and je.deleteflag =? ";
        params.add(Constants.SQL_FALSE);
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getDeliveryOrderJE(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        /*
         * je.id, je.entrydate and entryno are required by JE link click function to view JE details  ERP-22014
         */
        String query = " select distinct entryno, je.id, je.entrydate from journalentry je inner join deliveryorder do on je.id = do.inventoryje where do.id= ? and do.company = ? and je.approvestatuslevel=? ";
        if (requestParams.containsKey("id") && requestParams.get("id") != null && !StringUtil.isNullOrEmpty(requestParams.get("id").toString())) {
            params.add(requestParams.get("id").toString());
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            params.add(requestParams.get("companyid").toString());
        }
        params.add(Constants.APPROVED_STATUS_LEVEL);
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( je.entrydate >=? and je.entrydate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        query += " and je.deleteflag =? ";
        params.add(Constants.SQL_FALSE);
        List detailList = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getSalesPaymentJE(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        /*
         * je.id, je.entrydate and entryno are required by JE link click function to view JE details  ERP-22014
         */
        String query = " select distinct entryno, je.id, je.entrydate from journalentry je inner join receipt rec on je.id = rec.journalentry where rec.receiptnumber= ? and  rec.company= ? and je.approvestatuslevel=? ";
        if (requestParams.containsKey("number") && requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
            params.add(requestParams.get("number").toString());
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            params.add(requestParams.get("companyid").toString());
        }
        params.add(Constants.INVOICEAPPROVED);
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( je.entrydate >=? and je.entrydate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        query += " and je.deleteflag =? ";
        params.add(Constants.SQL_FALSE);
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getCreditNoteJE(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        /*
         * je.id, je.entrydate and entryno are required by JE link click function to view JE details  ERP-22014
         */
        String query = " select distinct entryno, je.id, je.entrydate from journalentry je inner join creditnote cn on je.id = cn.journalentry where cn.cnnumber= ? and  cn.company= ? and je.approvestatuslevel=? ";
        if (requestParams.containsKey("number") && requestParams.get("number") != null && !StringUtil.isNullOrEmpty(requestParams.get("number").toString())) {
            params.add(requestParams.get("number").toString());
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            params.add(requestParams.get("companyid").toString());
        }
        params.add(Constants.INVOICEAPPROVED);
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( je.entrydate >=? and je.entrydate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        query += " and je.deleteflag =? ";
        params.add(Constants.SQL_FALSE);
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }
    
        @Override
    public KwlReturnObject getPurchaseReturn(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        String query = " select distinct pr.prnumber from purchasereturn pr inner join ( select prd.purchasereturn from prdetails prd ";
        if(requestParams.containsKey("number") && requestParams.containsKey("companyid") && requestParams.get("companyid") != null && requestParams.get("number") != null){
            if (requestParams.containsKey("isFromGR") && requestParams.get("isFromGR") != null){
               query += " inner join ( select grod.id from grodetails grod inner join grorder gr on gr.id = grod.grorder where gr.gronumber = ? and gr.company =? )as innertable on innertable.id = prd.grdetails ";
            } else if (requestParams.containsKey("isFromPI") && requestParams.get("isFromPI") != null){
               query += " inner join ( select grd.id from grdetails grd inner join goodsreceipt gr on gr.id = grd.goodsreceipt where gr.grnumber = ? and gr.company = ? )as innertable on innertable.id = prd.videtails ";
            }
            params.add(requestParams.get("number").toString());
            params.add(requestParams.get("companyid").toString());
        }
        
        query+=" ) as prdetailstable on prdetailstable.purchasereturn = pr.id where pr.deleteflag = ? ";
        params.add(Constants.SQL_FALSE);
        if (requestParams.containsKey("isConsignment") && requestParams.get("isConsignment") != null) {
            query += " and pr.isconsignment= ? ";
            params.add(requestParams.get("isConsignment").toString());
        }
        if (requestParams.containsKey("isFixedAsset") && requestParams.get("isFixedAsset") != null ) {
            query += " and pr.isfixedasset = ? ";
            params.add((int)requestParams.get("isFixedAsset"));
        }
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( orderdate >=? and orderdate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }
    @Override
    public KwlReturnObject getSalesReturn(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {

        ArrayList params = new ArrayList();
        String query = "";
        query += "  select distinct srl.docid, jointable.srnumber from salesreturnlinking srl inner join salesreturn jointable on srl.docid = jointable.id "
              + "  where jointable.deleteflag = ? ";
        params.add(Constants.SQL_FALSE);
        
        if (requestParams.containsKey("id") && requestParams.get("id") != null && !StringUtil.isNullOrEmpty(requestParams.get("id").toString())) {
            query += " and srl.linkeddocid = ? ";
            params.add(requestParams.get("id").toString());
        }
        if (requestParams.containsKey("moduleid") && requestParams.get("moduleid") != null && !StringUtil.isNullOrEmpty(requestParams.get("moduleid").toString())) {
            query += " and srl.moduleid = ? ";
            params.add(requestParams.get("moduleid").toString());
        }
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
            query += " and jointable.company = ? ";
            params.add(requestParams.get("companyid").toString());
        }
         if (requestParams.containsKey("isConsignment") && requestParams.get("isConsignment") != null) {
            query += " and jointable.isconsignment= ? ";
            params.add(requestParams.get("isConsignment").toString());
        }
        if (requestParams.containsKey("isFixedAsset") && requestParams.get("isFixedAsset") != null ) {
            query += " and jointable.isfixedasset = ? ";
            params.add((int)requestParams.get("isFixedAsset"));
        }
        if (requestParams.containsKey("isLeaseSalesReturn") && requestParams.get("isLeaseSalesReturn") != null ) {
            query += " and jointable.isleasesalesreturn = ? ";
            params.add((int)requestParams.get("isLeaseSalesReturn"));
        }
        if (dateparams.containsKey(Constants.REQ_startdate) && dateparams.containsKey(Constants.REQ_enddate) && dateparams.get(Constants.REQ_startdate) != null && dateparams.get(Constants.REQ_enddate) != null) {
            query += " and ( orderdate >=? and orderdate <=? )";
            params.add(dateparams.get(Constants.REQ_startdate));
            params.add(dateparams.get(Constants.REQ_enddate));
        }
        List detailList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }
    
    @Override
    public KwlReturnObject checkEntryForTransactionInLinkingTableForForwardReference(String moduleName, String docid) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(docid);
        String hqlQuery = "from "+moduleName+"Linking as linkmodinfo where linkmodinfo.DocID.ID=? and (linkmodinfo.SourceFlag=0";
        if(moduleName==Constants.Acc_GoodsReceipt_modulename || moduleName==Constants.Acc_PurchaseInvoice_modulename){
            hqlQuery+= " and moduleid="+ Constants.Acc_Vendor_Invoice_ModuleId +" or (linkmodinfo.SourceFlag=1 and moduleid="+Constants.Acc_Vendor_Invoice_ModuleId+")";
        }
        hqlQuery+= ")";
        list = executeQuery( hqlQuery, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getDOLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        String sqlQuery = "";
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
        int documentType=(Integer)requestParams.get("documentType");
        boolean isSiParentOnly=requestParams.get("isSiParentOnly")!=null?(Boolean)requestParams.get("isSiParentOnly"):false;
        params.add(companyId);
        params.add(documentNo);
        if (documentType == Constants.Acc_Delivery_Order_ModuleId && !isSiParentOnly) {
            sqlQuery = "select do.id,do.donumber from deliveryorder do  where do.company=? and do.donumber=? and do.deleteflag = 'F'";
        } else if (isSiParentOnly) {
            sqlQuery = "select do.id,do.donumber from dolinking dol inner join deliveryorder do on do.id=dol.docid where do.company=? and dol.linkeddocno=? and dol.sourceflag=1 and do.deleteflag = 'F'";
        } else {
            sqlQuery = "select do.id,do.donumber from dolinking dol inner join deliveryorder do on do.id=dol.docid where do.company=? and dol.linkeddocno=? and dol.sourceflag=0 and do.deleteflag = 'F'";
        }

        List detailList = executeSQLQuery(sqlQuery, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getSOLinkingInfo(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String query = "";
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
        int documentType=(Integer)requestParams.get("documentType");
        params.add(companyId);
        params.add(documentNo);
        if (documentType == Constants.Acc_Sales_Order_ModuleId) {
            query = "select so.id,so.sonumber from salesorder so where so.company=? and so.sonumber=? and so.deleteflag = 'F'";
        } else {
            query = "select so.id,so.sonumber from solinking sol inner join salesorder so on so.id=sol.docid where so.company=? and sol.linkeddocno=? and sol.sourceflag=0 and so.deleteflag = 'F'";
        }

        List detailList = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }


    @Override
    public KwlReturnObject getCQLinkingInfo(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String sqlQuery = "";
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
        int documentType=(Integer)requestParams.get("documentType");
        params.add(companyId);
        params.add(documentNo);
        if (documentType == Constants.Acc_Customer_Quotation_ModuleId) {
            sqlQuery = " select distinct q.quotationnumber,cql.moduleid,cql.docid  from quotation q inner join cqlinking cql on cql.docid = q.id and q.company=? and q.quotationnumber=? and q.deleteflag = 'F'";
        } else {
            sqlQuery = "select cq.quotationnumber,cql.moduleid,cql.docid from cqlinking cql inner join quotation cq on cq.id=cql.docid where cq.company=? and cql.linkeddocno=? and cql.sourceflag=0 and cq.deleteflag = 'F'";
        }

        List detailList = executeSQLQuery(sqlQuery, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getSILinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        String query = "";
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
        int documentType=(Integer)requestParams.get("documentType");
        params.add(companyId);
        params.add(documentNo);
        if (documentType == Constants.Acc_Invoice_ModuleId) {
            query = "select inv.id,inv.invoicenumber,inv.cashtransaction from invoice inv where inv.company=? and inv.invoicenumber=? and inv.deleteflag = 'F'";
        } else {
            query = "select inv.id,inv.invoicenumber,inv.cashtransaction from invoicelinking invl inner join invoice inv on inv.id=invl.docid where inv.company=? and invl.linkeddocno=? and invl.sourceflag=0 and inv.deleteflag = 'F'";
        }

        List detailList = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getSRLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
       int documentType=(Integer)requestParams.get("documentType");
        params.add(companyId);
        params.add(documentNo);

        String sqlQuery = "";
        if (documentType == Constants.Acc_Sales_Return_ModuleId) {
            sqlQuery = "select srn.id from salesreturn srn where srn.company=? and srn.srnumber=? and srn.deleteflag = 'F'";
        } else {
            sqlQuery = "select srn.srnumber,srn.id from salesreturn srn inner join salesreturnlinking  srnl on srn.id=srnl.docid where srn.company=? and srnl.linkeddocno=? and srnl.sourceflag=0 and srn.deleteflag = 'F'";
        }

        List detailList = executeSQLQuery(sqlQuery, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getCNLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
        params.add(companyId);
        params.add(documentNo);
        String query = "select cn.id,cn.cnnumber from creditnote cn where cn.company=? and cn.cnnumber=? and cn.deleteflag = 'F'";

        List detailList = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getDNLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
        params.add(companyId);
        params.add(documentNo);
        String query = "select dn.id,dn.dnnumber from debitnote dn where dn.company=? and dn.dnnumber=?";

        List detailList = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getRPLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        String query = "";
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
        boolean isCheckPayment = (Boolean) requestParams.get("isCheckPayment");
        params.add(companyId);
        params.add(documentNo);
        if (isCheckPayment) {
            query = "select rp.receiptnumber from receipt rp where rp.company=? and rp.receiptNumber=? and rp.deleteflag = 'F'";
        } else {
            query = "select rp.receiptnumber from receipt rp inner join debitnotelinking dnl on rp.id=dnl.linkeddocid where rp.company=? and dnl.linkeddocno=? and rp.deleteflag = 'F'";
        }

        List detailList = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getPOLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        String query = "";
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
       int documentType=(Integer)requestParams.get("documentType");
        
        params.add(companyId);
        params.add(documentNo);
        if (documentType==Constants.Acc_Purchase_Order_ModuleId) {
            query = "select po.ponumber, po.id from purchaseorder po where po.company=? and po.ponumber=?";
        } else {
            query = "select po.ponumber, po.id from polinking pol inner join purchaseorder po on po.id=pol.docid where po.company=? and pol.linkeddocno=? and pol.sourceflag=0";
        }

        List detailList = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getPILinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        String query = "";
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
        int documentType=(Integer)requestParams.get("documentType");
        params.add(companyId);
        params.add(documentNo);
        if (documentType==Constants.Acc_Vendor_Invoice_ModuleId) {
            query = "select gr.grnumber, gr.id, gr.cashtransaction from goodsreceipt gr where gr.company=? and gr.grnumber=?";
        } else {
            query = "select gr.grnumber, gr.id, gr.cashtransaction from goodsreceiptlinking grl inner join goodsreceipt gr on gr.id=grl.docid where gr.company=? and grl.linkeddocno=? and grl.sourceflag=0 group by gr.id";
        }

        List detailList = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getGRLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        String sqlQuery = "";
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
        int documentType=(Integer)requestParams.get("documentType");
        boolean isOnlyPiParent=requestParams.get("isOnlyPiParent")!=null?(Boolean)requestParams.get("isOnlyPiParent"):false;
        params.add(companyId);
        params.add(documentNo);      
        
        if (documentType==Constants.Acc_Goods_Receipt_ModuleId && !isOnlyPiParent) {
            sqlQuery = "select gro.gronumber,gro.id from grorder gro  where gro.company=? and gro.gronumber=?";
        } else if(isOnlyPiParent){
            sqlQuery = "select gro.gronumber,gro.id from goodsreceiptorderlinking grol inner join grorder gro on gro.id=grol.docid where gro.company=? and grol.linkeddocno=? and grol.sourceflag=1";
        }else{
            sqlQuery = "select gro.gronumber,gro.id from goodsreceiptorderlinking grol inner join grorder gro on gro.id=grol.docid where gro.company=? and grol.linkeddocno=? and grol.sourceflag=0";
        }

        List detailList = executeSQLQuery(sqlQuery, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getVQLinkingInfo(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String sqlQuery = "";
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
        int documentType=(Integer)requestParams.get("documentType");
        params.add(companyId);
        params.add(documentNo);
        if (documentType==Constants.Acc_Vendor_Quotation_ModuleId) {
            sqlQuery = " select distinct vq.quotationnumber,vq.id  from vendorquotation vq inner join vqlinking vql on vql.docid = vq.id and vq.company=? and vq.quotationnumber=?";
        } else {
            sqlQuery = "select vq.quotationnumber,vq.id from vqlinking vql inner join vendorquotation vq on vq.id=vql.docid where vq.company=? and vql.linkeddocno=? and vql.sourceflag=0";
        }

        List detailList = executeSQLQuery(sqlQuery, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getRequisitionLinkingInfo(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String sqlQuery = "";
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
         int documentType=(Integer)requestParams.get("documentType");
        params.add(companyId);
        params.add(documentNo);
        if (documentType==Constants.Acc_Purchase_Requisition_ModuleId) {
            sqlQuery = " select distinct prq.prnumber  from purchaserequisition prq inner join purchaserequisitionlinking prql on prql.docid = prq.id and prq.company=? and prq.prnumber=?";
        } else {
            sqlQuery = "select prq.prnumber from purchaserequisitionlinking prql inner join purchaserequisition prq on prq.id=prql.docid where prq.company=? and prql.linkeddocno=? and prql.sourceflag=0";
        }

        List detailList = executeSQLQuery(sqlQuery, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }
    
    @Override
    public KwlReturnObject getPRLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
         int documentType=(Integer)requestParams.get("documentType");
        params.add(companyId);
        params.add(documentNo);
        String sqlQuery = "";
        if (documentType==Constants.Acc_Purchase_Return_ModuleId) {
            sqlQuery = "select prn.id from purchasereturn prn where prn.company=? and prn.prnumber=?";
        } else {
            sqlQuery = "select prn.prnumber,prn.id from purchasereturn prn inner join purchasereturnlinking  prnl on prn.id=prnl.docid where prn.company=? and prnl.linkeddocno=? and prnl.sourceflag=0";
        }

        List detailList = executeSQLQuery(sqlQuery, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getMPLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        String query = "";
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
        boolean isCheckPayment = (Boolean) requestParams.get("isCheckPayment");
        params.add(companyId);
        params.add(documentNo);
        if (isCheckPayment) {
            query = "select pay.paymentnumber from payment pay where pay.company=? and pay.paymentNumber=? ";
        } else {
            query = "select distinct pay.paymentnumber from payment pay inner join creditnotelinking cnl on pay.id=cnl.linkeddocid where pay.company=? and cnl.linkeddocno=? ";
        }

        List detailList = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject getRFQLinkingInfo(HashMap<String, Object> requestParams, HashMap<String, Object> dateparams) throws ServiceException {
        ArrayList params = new ArrayList();
        String documentNo = (String) requestParams.get("documentNo");
        String companyId = (String) requestParams.get("companyId");
        params.add(companyId);
        params.add(documentNo);
        String query = "select rfq.rfqnumber from requestforquotation rfq  where rfq.company=? and rfq.rfqNumber=? ";
        List detailList = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject checkFetchedPIhavePredecessor(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String invoiceID = (String) requestParams.get("invoiceID");
        String companyId = (String) requestParams.get("companyId");
        params.add(companyId);
        params.add(invoiceID);
        String query = "select  gr.grnumber from goodsreceipt gr inner join goodsreceiptlinking grl on gr.id=grl.docid where gr.company=? and grl.docid=? and grl.sourceflag=1 ";
        List detailList = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }

    @Override
    public KwlReturnObject checkFetchedSIhavePredecessor(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String invoiceID = (String) requestParams.get("invoiceID");
        String companyId = (String) requestParams.get("companyId");
        params.add(companyId);
        params.add(invoiceID);
        String query = "select  inv.invoicenumber from invoice inv inner join invoicelinking invl on inv.id=invl.docid where inv.company=? and invl.docid=? and invl.sourceflag=1 ";
        List detailList = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", "", detailList, detailList.size());
    }
}
