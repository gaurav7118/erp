/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.consignmentimpl;

import com.krawler.common.admin.ApprovalType;
import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.approval.consignment.Consignment;
import com.krawler.inventory.model.approval.consignment.ConsignmentApprovalDetails;
import com.krawler.inventory.model.approval.consignmentservice.ConsignmentDAO;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.common.KwlReturnObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class ConsignmentDAOImpl extends BaseDAO implements ConsignmentDAO {

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Consignment getConsingmentById(String consignmentId) {
        return (Consignment) get(Consignment.class, consignmentId);
    }

    @Override
    public void saveOrUpdateConsignment(Object object) throws ServiceException {
        super.saveOrUpdate(object);
    }

    @Override
    public List<Consignment> getConsingmentList(String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM Consignment ");
        List params = new ArrayList();
//        if (!StringUtil.isNullOrEmpty(searchString)) {
//            hql.append(" WHERE (Consignment.product.productid LIKE ? OR Consignment.product.name LIKE ? OR Consignment.transactionNo LIKE ? ) ");
//            params.add("%" + searchString + "%");
//            params.add("%" + searchString + "%");
//            params.add("%" + searchString + "%");
//        }
//
        hql.append(" ORDER BY product ");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            if (paging.isValid() && totalCount > paging.getLimit()) {
                list = executeQueryPaging( hql.toString(), params.toArray(), paging);
            }
        }
        return list;
    }

    @Override
    public ConsignmentApprovalDetails getConsingmentDetailsById(String consignmentId) {
        return (ConsignmentApprovalDetails) get(ConsignmentApprovalDetails.class, consignmentId);
    }

    @Override
    public int isQAApprovePermissionForUser(Company company, String storeid, String locationid, String userId) throws ServiceException {
        int ruleCount = 0;
        List dataList = new ArrayList();
        String fetchDateQry = "Select cr FROM ConsignmentRequestApprovalRule cr INNER JOIN cr.inventoryLocationsSet location,ConsignmentRequestApproverMapping cram  WHERE cr.inventoryWarehouse.id = ? AND cr.company.companyID = ? AND cr.approvalType = ? AND location.id = ? AND cr.ID=cram.consignmentRequestRule.ID AND cram.approver.userID= ? ";
        List params = new ArrayList();
        params.add(storeid);
        params.add(company.getCompanyID());
        params.add(ApprovalType.QAAPPROVAL);
        params.add(locationid);
        params.add(userId);
        try {
            dataList = executeQuery( fetchDateQry, params.toArray());
            if (!dataList.isEmpty() && dataList != null) {
                ruleCount = dataList.size();
            }
        } catch (Exception ex) {
            Logger.getLogger(ConsignmentDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return ruleCount;
        }
    }

    @Override
    public void deletePreviousConsignmentQAForSR(Company company, String salesReturnId) throws ServiceException{
        String delQry = " DELETE from Consignment c WHERE  c.company=? AND c.moduleRefId=? ";
        List params = new ArrayList();
        params.add(company);
        params.add(salesReturnId);
        try {
            executeUpdate(delQry, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(ConsignmentDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
     @Override
    public String getSalesReturnMemo(Company company, String salesReturnId) throws ServiceException {
        String memo = "";
        String selQry = " select memo from salesreturn where company=? and id=? ";
        List params = new ArrayList();
        params.add(company);
        params.add(salesReturnId);
        try {
            List list = executeSQLQuery(selQry, params.toArray());
            if (list != null && !list.isEmpty()) {
                memo = (String) list.get(0);
            }

        } catch (Exception ex) {
            Logger.getLogger(ConsignmentDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return memo;
    }
    
    @Override
    public KwlReturnObject getAllQAList(String companyId, Date fromDate, Date toDate, String moduleType, String statusType, Set<Store> storeSet, String searchString, Paging paging,String tzdiff,boolean isQAapprovalForBuildAssembly, boolean isisJobWorkOrderInQA) throws ServiceException {

        KwlReturnObject retObj = new KwlReturnObject(false, null, null, null, 0);
        List params = new ArrayList();
        String consqry = "";
        String DoQuery="";
        String stockRequest = "";
        String stockOut = "";
        String interStore = "";
        String allquery = "";
        String consignmentStatus = "";
        String stockoutStatus = "";
        String stockRequestStatus = "";
        String consignmentStoreFilter = "";
        String buildAssemblyStoreFilter = "";
        String stockoutStoreFilter = "";
        String stockStoreFilter = "";
        String stockTransferStoreFilter = "";
        String consignmentDateFilter = "";
        String stockoutDateFilter = "";
        String stocktransferDateFilter = "";

        int total_rec = 0;
        String consignmentSearchString = "";
        String stockoutSearchString = "";
        String stockSearchString = "";
        StringBuilder groConditionQuery = new StringBuilder();
        StringBuilder doConditionQuery = new StringBuilder();
        StringBuilder woQAConditionQuery = new StringBuilder();
        
         String buildAssemblySearchString = "";
        String buildAssQAStatusQuery="";
        if (!StringUtil.isNullOrEmpty(statusType)) {
            consignmentStatus = " AND c.approval_status='" + statusType + "'";
            stockoutStatus = " AND sap.approval_status='" + statusType + "'";
            stockRequestStatus = " AND stkapr.approval_status ='" + statusType + "'";
            if (statusType == "0") {
                buildAssQAStatusQuery = " and (pb.approvedquantity + pb.rejectedquantity) != pb.quantity "; // QA Pending
            } else {
                buildAssQAStatusQuery = " and (pb.approvedquantity + pb.rejectedquantity) = pb.quantity "; // QA Completed
                
            }
        }
        if (!storeSet.isEmpty() && storeSet != null) {
            boolean first = true;
            for (Store store : storeSet) {
                if (first) {
                    consignmentStoreFilter = " AND c.store in ( '" + store.getId() + "'";
                    stockoutStoreFilter = " AND sa.store in ( '" + store.getId() + "'";
                    stockStoreFilter = " AND gr.tostore in ( '" + store.getId() + "'";
                    stockTransferStoreFilter = " AND gr.fromstore in ( '" + store.getId() + "'";
                    buildAssemblyStoreFilter = " AND qa.warehouse in ( '" + store.getId() + "' ";
                    groConditionQuery.append(" AND npb.warehouse in ( '").append(store.getId()).append("' ");
                    woQAConditionQuery.append(" AND npb.warehouse in ( '").append(store.getId()).append("' ");
                    doConditionQuery.append(" AND str.id in ( '").append(store.getId()).append("' ");
                    first = false;
                } else {
                    consignmentStoreFilter += ",'" + store.getId() + "'";
                    stockoutStoreFilter += ",'" + store.getId() + "'";
                    stockStoreFilter += ",'" + store.getId() + "'";
                    stockTransferStoreFilter += ",'" + store.getId() + "'";
                    buildAssemblyStoreFilter += ",'" + store.getId() + "'";
                    groConditionQuery.append(",'").append(store.getId()).append("'");
                    woQAConditionQuery.append(",'").append(store.getId()).append("'");
                    doConditionQuery.append(",'").append(store.getId()).append("'");
                }
            }
            consignmentStoreFilter += ") ";
            stockoutStoreFilter += ") ";
            stockStoreFilter += ") ";
            stockTransferStoreFilter += ") ";
            buildAssemblyStoreFilter += ") ";
            groConditionQuery.append(")");
            woQAConditionQuery.append(")");
            doConditionQuery.append(")");
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            consignmentSearchString = " AND (p.productid LIKE '%" + searchString + "%' OR p.name LIKE '%" + searchString + "%' OR  c.transactionno LIKE '%" + searchString + "%')";
            stockoutSearchString = " AND (p.productid LIKE '%" + searchString + "%' OR p.name LIKE '%" + searchString + "%' OR sa.seqno LIKE '%" + searchString + "%')";
            stockSearchString = " AND (p.productid LIKE '%" + searchString + "%' OR p.name LIKE '%" + searchString + "%' OR gr.transactionno LIKE '%" + searchString + "%')";
            buildAssemblySearchString = " AND (p.productid LIKE '%" + searchString + "%' OR p.name LIKE '%" + searchString + "%' OR pb.refno LIKE '%" + searchString + "%')";
            groConditionQuery.append(" AND (p.productid LIKE '%").append(searchString).append("%' OR p.name LIKE '%").append(searchString).append("%' OR gro.gronumber LIKE '%").append(searchString).append("%')");
            woQAConditionQuery.append(" AND (p.productid LIKE '%").append(searchString).append("%' OR p.name LIKE '%").append(searchString).append("%' OR wo.workordername LIKE '%").append(searchString).append("%' OR wo.workorderid LIKE '%").append(searchString).append("%')");
            doConditionQuery.append(" AND (p.productid LIKE '%").append(searchString).append("%' OR p.name LIKE '%").append(searchString).append("%' OR do.donumber LIKE '%").append(searchString).append("%')");
        }
        if (fromDate != null && toDate != null) {
            // First convert GMT date into User's timezone date & then compare.
            consignmentDateFilter = " AND Date(convert_tz(c.createdon,'+00:00','"+ tzdiff +"')) >='" + df.format(fromDate) + "' AND Date(convert_tz(c.createdon,'+00:00','"+ tzdiff +"')) <= '" + df.format(toDate) + "'";
            stockoutDateFilter = " AND  Date(convert_tz(sap.createdon,'+00:00','"+ tzdiff +"')) >='" + df.format(fromDate) + "' AND Date(convert_tz(sap.createdon,'+00:00','"+ tzdiff +"')) <= '" + df.format(toDate) + "'";
            stocktransferDateFilter = " AND  Date(convert_tz(stkapr.createdon,'+00:00','"+ tzdiff +"')) >='" + df.format(fromDate) + "' AND Date(convert_tz(stkapr.createdon,'+00:00','"+ tzdiff +"')) <= '" + df.format(toDate) + "'";
            groConditionQuery.append(" AND gro.grorderdate >='").append(df.format(fromDate)).append("' AND  gro.grorderdate <= '").append(df.format(toDate)).append("'");
            woQAConditionQuery.append(" AND wo.workorderdate >='").append(df.format(fromDate)).append("' AND  wo.workorderdate <= '").append(df.format(toDate)).append("'");
            doConditionQuery.append(" AND do.orderdate >='").append(df.format(fromDate)).append("' AND do.orderdate <= '").append(df.format(toDate)).append("'");
        }
        List returnList = new ArrayList();
        if ("CONSIGNMENT".equals(moduleType) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType))) {
            consqry = "SELECT c.id, p.productid,p.name,c.transactionno AS transactionno ,c.returnquantity AS qty,p.id AS pid,c.approval_status AS status,"
                    + " u.name AS uomname,'consignment' AS module,str.description AS storename, null AS packaging,c.createdon,c.customer, c.store as transactionid,p.description from in_consignment c "
                    + " LEFT JOIN product p   ON c.product= p.id  AND  c.company=p.company "
                    + " LEFT JOIN in_storemaster str ON str.id=c.store "
                    + " LEFT JOIN uom u ON u.id=c.uom AND u.company=c.company WHERE c.company=? " + consignmentStatus + consignmentStoreFilter + consignmentSearchString + consignmentDateFilter;

            params.add(companyId);
            allquery = consqry;
        }
        
          if (isQAapprovalForBuildAssembly && isisJobWorkOrderInQA &&  (moduleType.equals(Constants.BUILD_ASSEMBLY_QA_APPROVAL) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType)))) {
            DoQuery = " select pb.id, p.productid, p.`name`, pb.refno AS transactionno,pb.quantity  AS qty, p.id AS pid, (case when (pb.approvedquantity + pb.rejectedquantity) != pb.quantity  then 0 else 3 end)  as status, "
                    + " u.name AS uomname, 'BuildAssemblyQA' as module, str.abbrev as storename, p.packaging as packaging ,pb.createdon as createdon, so.customer as customer, pb.id as transactionid,p.description from productbuild pb "
                    + " inner join product p on p.id=pb.product "
                    + " inner join qa_of_buildassemblyproduct qa  on qa.prbuildid=pb.id "
                    + " LEFT JOIN in_storemaster str ON str.id=qa.warehouse "
                    + " LEFT JOIN uom u ON u.id=p.unitOfMeasure "
                    + " inner join  salesorder so on so.id=pb.jobworkorderid "
                    + " where pb.company=? and pb.isbuild='T' and pb.ispendingforqa='T'  "+buildAssQAStatusQuery + buildAssemblyStoreFilter + buildAssemblySearchString+" group by pb.id";
  
            params.add(companyId);
            allquery = DoQuery;
        }
        if ("STOCK ADJUSTMENT".equals(moduleType) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType))) {
            /*
            * Initially no Customer was saved, therefor fetching Null in place of that.
            * Now customer is added in in_sa_approval therefore fetching customer id.
            */
            stockOut = "SELECT sap.id, p.productid,p.name,sa.seqno AS transactionno,sap.quantity AS qty,p.id AS pid,sap.approval_status AS status,u.name AS uomname, "
                    + " 'stockout' AS module,str.description AS storename,sa.packaging As packaging,sap.createdon,sap.customer AS customer, sap.stock_adjustment as transactionid,p.description from in_sa_approval sap "
                    + " INNER JOIN in_stockadjustment sa   ON sap.stock_adjustment= sa.id "
                    + " LEFT JOIN product p   ON sa.product= p.id  AND  sa.company=p.company  "
                    + " LEFT JOIN in_storemaster str ON str.id=sa.store "
                    + " LEFT JOIN uom u ON u.id=sa.uom AND u.company=sa.company WHERE sa.company=? " + stockoutStatus + stockoutStoreFilter + stockoutSearchString + stockoutDateFilter;
            params.add(companyId);
            allquery = stockOut;
        }
        if ("STOCK REQUEST".equals(moduleType) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType))) {

            stockRequest = "SELECT stkapr.id,p.productid,p.name,gr.transactionno AS transactionno,stkapr.quantity AS qty,p.id AS pid,"
                    + " stkapr.approval_status AS status,u.name AS uomname,'stockrequest' AS module,str.description AS storename,gr.packaging AS packaging,stkapr.createdon,null AS customer, stkapr.stocktransferid as transactionid,p.description from in_stocktransfer_approval stkapr "
                    + " INNER JOIN in_goodsrequest gr ON gr.id=stkapr.stocktransferid "
                    + " LEFT JOIN product p   ON gr.product= p.id AND  gr.company= p.company"
                    + " LEFT JOIN in_storemaster str ON str.id=gr.fromstore "
                    + " LEFT JOIN uom u ON u.id=gr.uom AND u.company=gr.company "
                    + "  WHERE gr.company=? AND stkapr.transaction_module=0  " + stockRequestStatus + stockStoreFilter + stockSearchString + stocktransferDateFilter;

            params.add(companyId);
            allquery = stockRequest;
        }
        if ("INTER STORE TRANSFER".equals(moduleType) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType))) {
            interStore = "SELECT stkapr.id,p.productid,p.name,gr.transactionno AS transactionno,stkapr.quantity AS qty,p.id AS pid,"
                    + " stkapr.approval_status AS status,u.name AS uomname,'stocktransfer'  AS module,str.description AS storename,gr.packaging AS packaging,stkapr.createdon,null AS customer, stkapr.stocktransferid as transactionid,p.description from in_stocktransfer_approval stkapr "
                    + " INNER JOIN in_interstoretransfer gr ON gr.id=stkapr.stocktransferid "
                    + " LEFT JOIN product p   ON gr.product= p.id AND  gr.company=p.company "
                    + " LEFT JOIN in_storemaster str ON str.id=gr.tostore "
                    + " LEFT JOIN uom u ON u.id=gr.uom AND u.company=gr.company "
                    + "  WHERE gr.company=? " + stockRequestStatus + stockTransferStoreFilter + stockSearchString + stocktransferDateFilter;

            params.add(companyId);
            allquery = interStore;
        }
        StringBuilder grnQuery = new StringBuilder();
        if ("GOOD RECEIPT".equals(moduleType) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType))) {
            grnQuery.append("select grodistmapping.istrequest as id,p.productid,p.name,gro.gronumber AS transactionno,grodistmapping.actualquantity as qty,p.id AS pid, (case when (grodistmapping.quantitydue > 0.0) then '0' else '3' end) as status, u.name as uomname,'goodsreceipt' as module, str.description AS storename, p.packaging AS packaging, gro.grorderdate as createdon,gro.vendor AS customer, grod.grorder as transactionid,p.description from grodetails grod "
                    + "inner join grorder gro on gro.id = grod.grorder "
                    + "inner join grodetailistmapping grodistmapping on grod.id = grodistmapping.grodetail "
                    + "inner join product p on grod.product = p.id "
                    + "inner join inventory inv on inv.id = grod.id "
                    + "inner join uom u on u.id = inv.uom "
                    + "left join locationbatchdocumentmapping lbdm on grod.id=lbdm.documentid "
                    + "inner join newproductbatch npb on lbdm.batchmapid=npb.id "
                    + "inner join in_interstoretransfer inst on inst. fromstore = npb.warehouse and grodistmapping.istrequest = inst.id "
                    + "inner join in_storemaster str on str.id = npb.warehouse where grod.company = ? ");
            if (!StringUtil.isNullOrEmpty(statusType)) {
                if (statusType.equals("0")) { // PENDING
                    grnQuery.append(" and grodistmapping.quantitydue > 0 ");
                } else if (statusType.equals("3")) { // APPROVED
                    grnQuery.append(" and grodistmapping.quantitydue = 0 ");
                }
            }
            /*
            GRN that are approved that need to fetched  in QA Approval report 
            approval status 11 is to completely approved record's 
            */
            groConditionQuery.append(" AND gro.approvestatuslevel = 11 ");
            grnQuery.append(groConditionQuery.toString());
            params.add(companyId);
            allquery = grnQuery.toString();
        }
        
        StringBuilder woQAQuery = new StringBuilder();
        if (Constants.MRP_WORK_ORDER.equals(moduleType) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType))) {
            woQAQuery.append("select wocdistmapping.istrequest as id,p.productid,p.name,wo.workorderid AS transactionno,wocdistmapping.actualquantity as qty,p.id AS pid, (case when (wocdistmapping.quantitydue > 0.0) then '0' else '3' end) as status, u.name as uomname,'Work Order' as module, str.description AS storename, p.packaging AS packaging, inst.createdon as createdon,wo.customer AS customer, wocd.workorder as transactionid,p.description from workordercomponentdetail wocd "
                    + "inner join workorder wo on wo.id = wocd.workorder "
                    + "inner join wocdetailistmapping wocdistmapping on wocd.id = wocdistmapping.wocdetail "
                    + "inner join product p on wocd.product = p.id "
//                    + "inner join inventory inv on inv.id = wocd.id "
                    + "inner join uom u on u.id = p.unitOfMeasure "
                    + "left join locationbatchdocumentmapping lbdm on wocd.id=lbdm.documentid "
                    + "inner join newproductbatch npb on lbdm.batchmapid=npb.id "
                    + "inner join in_interstoretransfer inst on inst. fromstore = npb.warehouse and wocdistmapping.istrequest = inst.id "
                    + "inner join in_storemaster str on str.id = npb.warehouse where wo.company = ? ");
            if (!StringUtil.isNullOrEmpty(statusType)) {
                if (statusType.equals("0")) { // PENDING
                    woQAQuery.append(" and wocdistmapping.quantitydue > 0 ");
                } else if (statusType.equals("3")) { // APPROVED
                    woQAQuery.append(" and wocdistmapping.quantitydue = 0 ");
                }
            }
            /*
            GRN that are approved that need to fetched  in QA Approval report 
            approval status 11 is to completely approved record's 
            */
//            groConditionQuery.append(" AND gro.approvestatuslevel = 11 ");
            woQAQuery.append(woQAConditionQuery.toString());
            params.add(companyId);
            allquery = woQAQuery.toString();
        }
        
        
        StringBuilder doQuery = new StringBuilder();
        if ("DELIVERY ORDER".equals(moduleType) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType))) {
            doQuery.append("select dodistmapping.qcistrequest as id,p.productid,p.name,do.donumber AS transactionno,dodistmapping.quantity as qty,p.id AS pid, (case when (dodistmapping.quantitydue > 0.0) then '0' else '3' end) as status, u.name as uomname,'deliveryorder' as module, str.description AS storename, p.packaging AS packaging, do.orderdate as createdon,do.customer AS customer, dod.deliveryorder as transactionid,p.description "
                    + "from dodqcistmapping dodistmapping "
                    + "inner join dodetails dod on  dod.id = dodistmapping.dodetailid "
                    + "inner join deliveryorder do on do.id = dod.deliveryorder "
                    + "inner join in_interstoretransfer inst on dodistmapping.qcistrequest = inst.id "
                    + "inner join product p on inst.product = p.id "
                    + "inner join uom u on u.id = inst.uom "
                    + "inner join in_storemaster str on str.id = inst.fromstore where do.company = ? ");
            if (!StringUtil.isNullOrEmpty(statusType)) {
                if (statusType.equals("0")) { // PENDING
                    doQuery.append(" and dodistmapping .quantitydue > 0 ");
                } else if (statusType.equals("3")) { // APPROVED
                    doQuery.append(" and dodistmapping .quantitydue = 0 ");
                }
            }
            /*
            DO that are approved that need to fetched  in QA Approval report 
            approval status 11 is set to completely approved record's 
            */
            doConditionQuery.append(" AND do.approvestatuslevel = 11 ");
            doQuery.append(doConditionQuery.toString());
            params.add(companyId);
            allquery = doQuery.toString();
        }
        
        if ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType)) {
            if(isQAapprovalForBuildAssembly && isisJobWorkOrderInQA){
               DoQuery =" UNION  "+DoQuery;
            }
            if (!StringUtil.isNullOrEmpty(grnQuery.toString())) {
                grnQuery = new StringBuilder(" UNION " + grnQuery.toString());
            }
            if (!StringUtil.isNullOrEmpty(woQAQuery.toString())) {
                woQAQuery = new StringBuilder(" UNION " + woQAQuery.toString());
            }
            if (!StringUtil.isNullOrEmpty(doQuery.toString())) {
                doQuery = new StringBuilder(" UNION " + doQuery.toString());
            }            
            allquery = consqry +DoQuery + " UNION  " + stockOut + " UNION  " + stockRequest + " UNION  " + interStore + grnQuery.toString() + woQAQuery.toString() + doQuery.toString();
        }

        String seleQuery = "SELECT tb.id,tb.productid,tb.name,tb.transactionno,tb.qty,tb.pid,tb.status,tb.uomname,tb.module,tb.storename,tb.packaging,tb.createdon,tb.customer, tb.transactionid,tb.description FROM (" + allquery + ") as tb ORDER BY tb.createdon DESC,tb.transactionno DESC ";
        returnList = executeSQLQuery( seleQuery, params.toArray());
        total_rec = returnList.size();

        if (paging != null) {
            seleQuery += " LIMIT " + paging.getOffset() + "," + paging.getLimit();
        }
        returnList = executeSQLQuery( seleQuery, params.toArray());
        if (!returnList.isEmpty()) {
            retObj = new KwlReturnObject(true, null, null, returnList, total_rec);
        }

        return retObj;
    }
//    @Override

    public KwlReturnObject getAllQARepairPendingList(String companyId, Date fromDate, Date toDate, String moduleType, String statusType, Set<Store> storeSet, String searchString, Paging paging,String tzdiff,boolean isQAapprovalForBuildAssembly) throws ServiceException {

        KwlReturnObject retObj = new KwlReturnObject(false, null, null, null, 0);
        List params = new ArrayList();
        String consqry = "";
        String stockRequest = "";
        String stockOut = "";
        String interStore = "";
        String allquery = "";
        String consignmentStatus = "";
        String stockoutStatus = "";
        String stockRequestStatus = "";
        String consignmentStoreFilter = "";
        String buildAssemblyStoreFilter = "";
        String stockoutStoreFilter = "";
        String stockStoreFilter = "";
        String stockTransferStoreFilter = "";
        String consignmentDateFilter = "";
        String stockoutDateFilter = "";
        String stocktransferDateFilter = "";

        int total_rec = 0;
        String consignmentSearchString = "";
        String stockoutSearchString = "";
        String stockSearchString = "";
        String DoQuery="";
        String buildAssemblySearchString = "";
        StringBuilder groConditionQuery = new StringBuilder();
        StringBuilder doConditionQuery = new StringBuilder();
        StringBuilder woQARPConditionQuery = new StringBuilder();

//        if (!StringUtil.isNullOrEmpty(statusType)) {
        consignmentStatus = " AND (cdtl.repair_status=4 OR cdtl.repair_status=7) ";
        stockoutStatus = " AND (sadtl.repair_status=4 OR sadtl.repair_status=7)  ";
        stockRequestStatus = " AND (stkdtl.repair_status=4 OR stkdtl.repair_status=7) ";
//        }
        if (!storeSet.isEmpty() && storeSet != null) {
            boolean first = true;
            for (Store store : storeSet) {
                if (first) {
                    consignmentStoreFilter = " AND c.store in ( '" + store.getId() + "'";
                    stockoutStoreFilter = " AND sa.store in ( '" + store.getId() + "'";
                    stockStoreFilter = " AND gr.tostore in ( '" + store.getId() + "'";
                    stockTransferStoreFilter = " AND gr.fromstore in ( '" + store.getId() + "'";
                    buildAssemblyStoreFilter = " AND qa.warehouse in ( '" + store.getId() + "' ";
                    groConditionQuery.append(" AND npb.warehouse in ( '").append(store.getId()).append("' ");
                    woQARPConditionQuery.append(" AND npb.warehouse in ( '").append(store.getId()).append("' ");
                    doConditionQuery.append(" AND str.id in ( '").append(store.getId()).append("' ");
                    first = false;
                } else {
                    consignmentStoreFilter += ",'" + store.getId() + "'";
                    stockoutStoreFilter += ",'" + store.getId() + "'";
                    stockStoreFilter += ",'" + store.getId() + "'";
                    buildAssemblyStoreFilter += ",'" + store.getId() + "'";
                    stockTransferStoreFilter += ",'" + store.getId() + "'";

                    groConditionQuery.append(",'").append(store.getId()).append("'");
                    woQARPConditionQuery.append(",'").append(store.getId()).append("'");
                    doConditionQuery.append(",'").append(store.getId()).append("'");
                }
            }
            consignmentStoreFilter += ") ";
            stockoutStoreFilter += ") ";
            stockStoreFilter += ") ";
            stockTransferStoreFilter += ") ";
            buildAssemblyStoreFilter += ") ";
            groConditionQuery.append(")");
            woQARPConditionQuery.append(")");
            doConditionQuery.append(")");
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            consignmentSearchString = " AND (p.productid LIKE '%" + searchString + "%' OR  tb.transactionno LIKE '%" + searchString + "%' OR p.name LIKE '%" + searchString + "%')";
            buildAssemblySearchString= " AND (p.productid LIKE '%" + searchString + "%' OR  pb.refno LIKE '%" + searchString + "%' OR p.name LIKE '%" + searchString + "%')";
             groConditionQuery.append(" AND (p.productid LIKE '%").append(searchString).append("%' OR p.name LIKE '%").append(searchString).append("%' OR gro.gronumber LIKE '%").append(searchString).append("%')");
             woQARPConditionQuery.append(" AND (p.productid LIKE '%").append(searchString).append("%' OR p.name LIKE '%").append(searchString).append("%' OR wo.workordername LIKE '%").append(searchString).append("%' OR wo.workorderid LIKE '%").append(searchString).append("%')");
             doConditionQuery.append(" AND (p.productid LIKE '%").append(searchString).append("%' OR p.name LIKE '%").append(searchString).append("%' OR do.donumber LIKE '%").append(searchString).append("%')");
        }
        if (fromDate != null && toDate != null) {
            // First convert GMT date into User's timezone date & then compare.
            consignmentDateFilter = " AND  Date(convert_tz(c.createdon,'+00:00','"+ tzdiff +"')) >='" + df.format(fromDate) + "' AND Date(convert_tz(c.createdon,'+00:00','"+ tzdiff +"')) <= '" + df.format(toDate) + "'";
            stockoutDateFilter = " AND  Date(convert_tz(sap.createdon,'+00:00','"+ tzdiff +"')) >='" + df.format(fromDate) + "' AND Date(convert_tz(sap.createdon,'+00:00','"+ tzdiff +"')) <= '" + df.format(toDate) + "'";
            stocktransferDateFilter = " AND  Date(convert_tz(stkapr.createdon,'+00:00','"+ tzdiff +"')) >='" + df.format(fromDate) + "' AND Date(convert_tz(stkapr.createdon,'+00:00','"+ tzdiff +"')) <= '" + df.format(toDate) + "'";
            groConditionQuery.append(" AND gro.grorderdate >='").append(df.format(fromDate)).append("' AND  gro.grorderdate <= '").append(df.format(toDate)).append("'");
            woQARPConditionQuery.append(" AND wo.workorderdate >='").append(df.format(fromDate)).append("' AND  wo.workorderdate <= '").append(df.format(toDate)).append("'");
            doConditionQuery.append(" AND do.orderdate >='").append(df.format(fromDate)).append("' AND do.orderdate <= '").append(df.format(toDate)).append("'");
        }
        List returnList = new ArrayList();
        if ("CONSIGNMENT".equals(moduleType) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType))) {
            consqry = "SELECT c.id,c.transactionno AS transactionno ,c.product AS productid,c.company,"
                    + "  'consignment' AS module,c.createdon from in_consignment c "
                    + "  INNER JOIN in_consignmentdetails cdtl ON c.id=cdtl.consignment "
                    + "  WHERE c.company=? " + consignmentStatus + consignmentStoreFilter + consignmentDateFilter;

            params.add(companyId);
            allquery = consqry;
        }
            if (isQAapprovalForBuildAssembly && (moduleType.equals(Constants.BUILD_ASSEMBLY_QA_APPROVAL) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType)))) {
            DoQuery = " select pb.id, pb.refno AS transactionno ,pb.product AS productid,pb.company, "
                    + " 'BuildAssemblyQA' AS module,date(pb.entrydate) as createdon from productbuild pb "
                    + " inner join qa_of_buildassemblyproduct qa  on qa.prbuildid=pb.id "
                    +   " inner join product p on p.id=pb.product "
                    + " where pb.company=? and pb.isbuild='T' and pb.ispendingforqa='T'  and pb.rejectedquantity >  0 " +buildAssemblyStoreFilter;

            params.add(companyId);
            allquery = DoQuery;
        }
        if ("STOCK ADJUSTMENT".equals(moduleType) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType))) {
            stockOut = "SELECT sap.id,sa.seqno AS transactionno,sa.product AS productid,sa.company, "
                    + " 'stockout' AS module,sap.createdon  from in_sa_approval sap "
                    + " INNER JOIN in_sa_detail_approval sadtl ON sap.id=sadtl.sa_approval "
                    + " INNER JOIN in_stockadjustment sa   ON sap.stock_adjustment= sa.id "
                    + " WHERE sa.company=? " + stockoutStatus + stockoutStoreFilter + stockoutDateFilter;
            params.add(companyId);
            allquery = stockOut;
        }
        if ("STOCK REQUEST".equals(moduleType) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType))) {

            stockRequest = "SELECT stkapr.id,gr.transactionno AS transactionno,gr.product AS productid,"
                    + " gr.company,'stockrequest' AS module,stkapr.createdon from in_stocktransfer_approval stkapr "
                    + " INNER JOIN in_stocktransfer_detail_approval stkdtl ON stkapr.id=stkdtl.stocktransfer_approval "
                    + " INNER JOIN in_goodsrequest gr ON gr.id=stkapr.stocktransferid "
                    + " WHERE gr.company=? AND stkapr.transaction_module=0  " + stockRequestStatus + stockStoreFilter + stocktransferDateFilter;

            params.add(companyId);
            allquery = stockRequest;
        }
        if ("INTER STORE TRANSFER".equals(moduleType) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType))) {
            interStore = "SELECT stkapr.id,gr.transactionno AS transactionno,gr.product AS productid,"
                    + " gr.company,'stocktransfer' AS module,stkapr.createdon from in_stocktransfer_approval stkapr "
                    + " INNER JOIN in_stocktransfer_detail_approval stkdtl ON stkapr.id=stkdtl.stocktransfer_approval "
                    + " INNER JOIN in_interstoretransfer gr ON gr.id=stkapr.stocktransferid "
                    + " WHERE gr.company=? " + stockRequestStatus + stockTransferStoreFilter + stocktransferDateFilter;

            params.add(companyId);
            allquery = interStore;
        }
        StringBuilder grnQuery = new StringBuilder();
        if ("GOOD RECEIPT".equals(moduleType) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType))) {
            grnQuery.append("select rm.id, gro.gronumber AS transactionno,grod.product as productid,gro.company,'goodsreceipt' as module,gro.grorderdate as createdon from "
                    + "repairgrodistmapping rm inner join grodetailistmapping grodistmapping on grodistmapping.id = rm.grodistmapping "
                    + "inner join grodetails grod on grodistmapping.grodetail = grod.id "
                    + "inner join product p on grod.product = p.id "
                    + "inner join grorder gro on gro.id = grod.grorder "
                    + "left join locationbatchdocumentmapping lbdm on grod.id=lbdm.documentid "
                    + "inner join newproductbatch npb on lbdm.batchmapid=npb.id "
                    + "where gro.company = ?");
            params.add(companyId);
            grnQuery.append(groConditionQuery.toString());
            allquery = grnQuery.toString();
        }
        StringBuilder woQuery = new StringBuilder();
        if (Constants.MRP_WORK_ORDER.equals(moduleType) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType))) {
            woQuery.append("select rwocdm.id, wo.workorderid AS transactionno,wocd.product as productid,wo.company,'Work Order' as module,wo.workorderdate as createdon from "
                    + "repairwocdistmapping rwocdm inner join wocdetailistmapping wocdistmapping on wocdistmapping.id = rwocdm.wocdistmapping "
                    + "inner join workordercomponentdetail wocd on wocdistmapping.wocdetail = wocd.id "
                    + "inner join product p on wocd.product = p.id "
                    + "inner join workorder wo on wo.id = wocd.workorder "
                    + "left join locationbatchdocumentmapping lbdm on wocd.id=lbdm.documentid "
                    + "inner join newproductbatch npb on lbdm.batchmapid=npb.id "
                    + "where wo.company = ?");
            params.add(companyId);
            woQuery.append(woQARPConditionQuery.toString());
            allquery = woQuery.toString();
        }
        StringBuilder doQuery = new StringBuilder();
        if ("DELIVERY ORDER".equals(moduleType) || ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType))) {
            doQuery.append("select rm.id,do.donumber AS transactionno,p.id as productid,do.company,'deliveryorder' as module, do.orderdate as createdon from "
                    + "rejectdodistmapping rm inner join dodqcistmapping dodm on dodm.id = rm.dodqcistmapping "
                    + "inner join dodetails dod on dodm.dodetailid = dod.id "
                    + "inner join product p on dod.product = p.id "
                    + "inner join deliveryorder do on do.id = dod.deliveryorder "
                    + "inner join in_interstoretransfer inst on dodm.qcistrequest = inst.id "
                    + "inner join uom u on u.id = inst.uom "
                    + "inner join in_storemaster str on str.id = inst.fromstore "
                    + "where do.company = ?");
            params.add(companyId);
            doQuery.append(doConditionQuery.toString());
            allquery = doQuery.toString();
        }
        if ("ALL".equals(moduleType) || StringUtil.isNullOrEmpty(moduleType)) {
            if(isQAapprovalForBuildAssembly){
               DoQuery =" UNION  "+DoQuery;
            }
            if (!StringUtil.isNullOrEmpty(grnQuery.toString())) {
                grnQuery = new StringBuilder(" UNION " + grnQuery.toString());
            }
            if (!StringUtil.isNullOrEmpty(woQuery.toString())) {
                woQuery = new StringBuilder(" UNION " + woQuery.toString());
            }
            if (!StringUtil.isNullOrEmpty(doQuery.toString())) {
                doQuery = new StringBuilder(" UNION " + doQuery.toString());
            }
            allquery = consqry+  DoQuery+ " UNION  " + stockOut + " UNION  " + stockRequest + " UNION  " + interStore + grnQuery.toString() + woQuery.toString() + doQuery.toString();
        }

        String seleQuery = "SELECT tb.id,tb.transactionno,tb.productid,tb.company,tb.module,p.name,p.productid AS pid FROM (" + allquery + ") as tb "
                + " LEFT JOIN product p ON tb.productid= p.id AND  tb.company=p.company "
                + " WHERE tb.company='" + companyId + "'" + consignmentSearchString
                + " ORDER BY tb.createdon DESC ";

        returnList = executeSQLQuery( seleQuery, params.toArray());
        total_rec = returnList.size();

        if (paging != null) {
            seleQuery += " LIMIT " + paging.getOffset() + "," + paging.getLimit();
        }
        returnList = executeSQLQuery( seleQuery, params.toArray());
        if (!returnList.isEmpty()) {
            retObj = new KwlReturnObject(true, null, null, returnList, total_rec);
        }

        return retObj;
    }

    @Override
    public KwlReturnObject getAllRepairList(String companyId, String statusType, Store store, Date fromDate, Date toDate, String searchString, Paging paging,String tzdiff,boolean  isQAapprovalForBuildAssembly) throws ServiceException {

        KwlReturnObject retObj = new KwlReturnObject(false, null, null, null, 0);
        List returnList = new ArrayList(); 
        int totalCount = 0;
        
        try {
            List params = new ArrayList();
            String buidAssemblyqrFilterQuery="";
            String buildAssemblyStoreFilter="";
            String buidAssemblySearchFitler="";
            String consignQuery = "SELECT p.productid,p.id AS pid,cnsdtl.batchName,cnsdtl.serialName AS srno,if(cnsdtl.returnqty=0,cnsdtl.quantity,cnsdtl.returnqty) AS quantity,cnsdtl.repair_status,"
                    + " cns.transactionno AS tranno,p.`name` AS pname,str.description AS storename,cnsdtl.modifiedon AS date,ln.name AS locationname,cnsdtl.reason,cnsdtl.repairedon as rdate, cnsdtl.id as transactionid, cns.id as parent,'consignment' as module,p.description "
                    + " FROM in_consignmentdetails cnsdtl  "
                    + " INNER JOIN in_consignment cns ON cns.id=cnsdtl.consignment INNER JOIN  product p   ON cns.product= p.id AND "
                    + " cns.company=p.company "
                    + " LEFT JOIN in_storemaster str ON str.id=cns.store "
                    + " LEFT JOIN in_location ln ON ln.id=cnsdtl.location "
                    + " where cns.company=? AND ";

            if (!StringUtil.isNullOrEmpty(statusType)) {
                consignQuery += " cnsdtl.repair_status='" + statusType + "' ";
                buidAssemblyqrFilterQuery+= " and qa.approval_status='" + statusType + "' ";
                
            } else {
                consignQuery += " (cnsdtl.repair_status=6 OR cnsdtl.repair_status=5) ";
                buidAssemblyqrFilterQuery+=" and (qa.approval_status=5 || qa.approval_status=6) ";
            }
            params.add(companyId);
            if (store != null) {
                consignQuery += " AND cns.store = ?";
                params.add(store.getId());
            }
            if (fromDate != null && toDate != null) {
                // First convert GMT date into User's timezone date & then compare.
                consignQuery += " AND Date(convert_tz(cnsdtl.modifiedon,'+00:00','"+ tzdiff +"')) BETWEEN ? AND ? ";
                params.add(fromDate);
                params.add(toDate);
            }
            if (!StringUtil.isNullOrEmpty(searchString)) {
                consignQuery += " AND ( p.productid LIKE ? OR p.name LIKE ? OR cns.transactionno LIKE ?) ";
                params.add("%" + searchString + "%");
                params.add("%" + searchString + "%");
                params.add("%" + searchString + "%");
                buidAssemblySearchFitler += " AND (p.productid LIKE '%" + searchString + "%' OR  pb.refno LIKE '%" + searchString + "%' OR p.name LIKE '%" + searchString + "%') ";
            }
            String buidAssemblyqr="";
            if(isQAapprovalForBuildAssembly){
               
             buidAssemblyqr="select p.productid,p.id AS pid,qa.batchname as batchName,qa.serialname as srno, qa.quantity,qa.approval_status as repair_status,pb.refno as tranno,p.`name` AS pname,"
                        + " inw.description AS storename,qa.inspectiondate as date,inl.name as locationname,qa.reapirreason as reason, qa.repairdate as rdate, qa.id as transactionid, pb.id as parent,'buildassembly' as module,p.description "
                        + " from qa_of_buildassemblyproduct qa "
                        + " inner join productbuild pb on pb.id=qa.prbuildid  "
                        + " inner join product p on pb.product=p.id "
                        + " inner join in_storemaster inw on inw.id=qa.warehouse "
                        + " inner join inventorylocation inl on inl.id=qa.location "
                        + " where   pb.company= ? " + buidAssemblyqrFilterQuery + buidAssemblySearchFitler;

             params.add(companyId);
            if (store != null) {
                buildAssemblyStoreFilter += " AND qa.warehouse = ? ";
                 params.add(store.getId());
                 buidAssemblyqr +=buildAssemblyStoreFilter;
            }
            }
//            returnList = executeSQLQuery( consignQuery, params.toArray());
            String saQuery = "SELECT p.productid,p.id AS pid,sad.batchname,sadtl.serialname AS srno,if(sadtl.returnqty=0,sadtl.quantity,sadtl.returnqty) AS quantity,sadtl.repair_status,stk.seqno AS tranno,p.`name` AS pname,str.description AS storename,"
                    + " sadtl.modifiedon AS date,ln.name AS locationname,sadtl.reason,sadtl.repairedon as rdate, sadtl.id as transactionid, sad.id as parent,'saapproval' as module,p.description "
                    + " FROM in_sa_detail_approval sadtl  INNER JOIN in_sa_detail sad  ON sad.id=sadtl.stock_adjustment_detail "
                    + " INNER JOIN in_stockadjustment stk ON stk.id=sad.stockadjustment "
                    + " INNER JOIN  product p   ON stk.product= p.id AND stk.company=p.company "
                    + " LEFT JOIN in_location ln ON ln.id=sad.location "
                    + " LEFT JOIN in_storemaster str ON str.id=stk.store WHERE  stk.company=? AND  ";

            if (!StringUtil.isNullOrEmpty(statusType)) {
                saQuery += " sadtl.repair_status='" + statusType + "' ";
            } else {
                saQuery += " (sadtl.repair_status=6 OR sadtl.repair_status=5) ";
            }

            params.add(companyId);
            if (store != null) {
                saQuery += " AND stk.store = ?";
                params.add(store.getId());
            }
            if (fromDate != null && toDate != null) {
                saQuery += " AND DATE(sadtl.modifiedon) BETWEEN ? AND ? ";
                params.add(fromDate);
                params.add(toDate);
            }
            if (!StringUtil.isNullOrEmpty(searchString)) {
                saQuery += " AND ( p.productid LIKE ? OR p.name LIKE ? OR stk.seqno LIKE ?) ";
                params.add("%" + searchString + "%");
                params.add("%" + searchString + "%");
                params.add("%" + searchString + "%");
            }

            String stockRequest = "SELECT  p.productid,p.id AS pid ,istdl.batchname,stkdtl.serialname AS srno,stkdtl.returnqty,stkdtl.repair_status,gr.transactionno AS tranno,"
                    + " p.`name` AS pname,str.description AS storename,stkdtl.modifiedon AS date,ln.name AS locationname,stkdtl.reason,stkdtl.repairedon as rdate, stkdtl.id as transactionid, stktr.id as parent,'stocktransferrequest' as module,p.description"
                    + " FROM in_stocktransfer_detail_approval  stkdtl  "
                    + " INNER JOIN in_stocktransfer_approval stktr ON stkdtl.stocktransfer_approval=stktr.id   AND stktr.transaction_module=0 "
                    + " INNER JOIN in_goodsrequest gr ON gr.id=stktr.stocktransferid "
                    + " INNER JOIN in_sr_detail istdl ON istdl.stockrequest=stktr.stocktransferid "
                    + " LEFT JOIN product p   ON gr.product= p.id AND  gr.company=p.company  "
                    + " LEFT JOIN in_storemaster str ON str.id=gr.fromstore "
                    + " LEFT JOIN in_location ln ON ln.id=gr.fromlocation "
                    + " WHERE gr.company=? AND  ";

            if (!StringUtil.isNullOrEmpty(statusType)) {
                stockRequest += " stkdtl.repair_status='" + statusType + "' ";
            } else {
                stockRequest += " (stkdtl.repair_status=6 OR stkdtl.repair_status=5) ";
            }
            params.add(companyId);
            if (store != null) {
                stockRequest += " AND gr.fromstore = ?";
                params.add(store.getId());
            }
            if (fromDate != null && toDate != null) {
                stockRequest += " AND DATE(stkdtl.modifiedon) BETWEEN ? AND ? ";
                params.add(fromDate);
                params.add(toDate);
            }
            if (!StringUtil.isNullOrEmpty(searchString)) {
                stockRequest += " AND ( p.productid LIKE ? OR p.name LIKE ? OR gr.transactionno LIKE ?) ";
                params.add("%" + searchString + "%");
                params.add("%" + searchString + "%");
                params.add("%" + searchString + "%");
            }


//            String interStock = "SELECT  p.productid,p.id AS pid ,istdl.batchname,stkdtl.serialname AS srno, if(stkdtl.returnqty=0,stkdtl.quantity,stkdtl.returnqty) AS quantity,stkdtl.repair_status,gr.transactionno AS tranno,"
//                    + " p.`name` AS pname,str.description AS storename,DATE(stkdtl.modifiedon) AS date,ln.name AS locationname,stkdtl.reason FROM in_stocktransfer_detail_approval  stkdtl  "
//                    + " INNER JOIN in_stocktransfer_approval stktr ON stkdtl.stocktransfer_approval=stktr.id   AND stktr.transaction_module=2   "
//                    + " INNER JOIN in_interstoretransfer gr ON gr.id=stktr.stocktransferid "
//                    + " INNER JOIN in_ist_detail istdl ON istdl.istrequest=stktr.stocktransferid "
//                    + " LEFT JOIN product p   ON gr.product= p.id AND  gr.company=p.company  "
//                    + " LEFT JOIN in_storemaster str ON str.id=gr.tostore "
//                    + " LEFT JOIN in_location ln ON ln.id=istdl.delivered_location "
//                    + " WHERE gr.company=?  AND ";

            String interStock = " SELECT p.productid,p.id AS pid ,istdl.batchname,stkdtl.serialname AS srno, if(stkdtl.returnqty=0,stkdtl.quantity,stkdtl.returnqty) AS quantity,stkdtl.repair_status,gr.transactionno AS tranno,"
                    + " p.`name` AS pname,str.description AS storename,stkdtl.modifiedon AS date,ln.name AS locationname,stkdtl.reason,stkdtl.repairedon as rdate, stkdtl.id as transactionid, stktr.id as parent,'interstocktransfer' as module,p.description "
                    + " FROM in_stocktransfer_detail_approval stkdtl"
                    + " INNER JOIN in_ist_detail istdl ON istdl.id=stkdtl.stocktransfer_detail_id"
                    + " INNER JOIN in_stocktransfer_approval stktr ON stkdtl.stocktransfer_approval=stktr.id   AND stktr.transaction_module=2"
                    + " INNER JOIN in_interstoretransfer gr ON gr.id=stktr.stocktransferid  "
                    + " LEFT JOIN product p   ON gr.product= p.id AND  gr.company=p.company "
                    + " LEFT JOIN in_storemaster str ON str.id=gr.tostore  "
                    + " LEFT JOIN in_location ln ON ln.id=istdl.delivered_location"
                    + " WHERE gr.company=? AND  ";

            if (!StringUtil.isNullOrEmpty(statusType)) {
                interStock += " stkdtl.repair_status='" + statusType + "' ";
            } else {
                interStock += " (stkdtl.repair_status=6 OR stkdtl.repair_status=5) ";
            }
            
            params.add(companyId);
            if (store != null) {
                interStock += " AND gr.tostore = ?";
                params.add(store.getId());
            }
            if (fromDate != null && toDate != null) {
                interStock += " AND DATE(stkdtl.modifiedon) BETWEEN ? AND ? ";
                params.add(fromDate);
                params.add(toDate);
            }
            if (!StringUtil.isNullOrEmpty(searchString)) {
                interStock += " AND ( p.productid LIKE ? OR p.name LIKE ? OR gr.transactionno LIKE ?) ";
                params.add("%" + searchString + "%");
                params.add("%" + searchString + "%");
                params.add("%" + searchString + "%");
            }

            if(isQAapprovalForBuildAssembly){
            buidAssemblyqr=" UNION ALL " + buidAssemblyqr;
            }
            
            StringBuilder grnISTQuery = new StringBuilder();
            StringBuilder grnSAQuery = new StringBuilder();
            if (true) { // Query for QA Approval in GRN
                
                if (StringUtil.isNullOrEmpty(statusType) || (!StringUtil.isNullOrEmpty(statusType) && "5".equals(statusType))) { 
                    /**
                     * ApprovalStatus.REPAIRDONE
                     */
                    params.add(companyId);
                    grnISTQuery.append(" select p.productid, p.id as pid,npb.batchname,outistdetail.delivered_serialnames as srno,"
                            + "outistdetail.deliveredqty as quantity,'5', "
                            + "gro.gronumber AS tranno,p.name as pname,str.description as storename,"
                            + "gro.grorderdate as date,ln.name as locationname, outist.remark,outist.businessdate as rdate,"
                            + "outistdetail.id as transactionid,outist.id as parent,'goodsreceipt' as module,p.description "
                            + "from repairgrodistmapping rm inner join in_interstoretransfer outist on rm.id = outist.repairgrodetailistmapping "
                            + "inner join in_ist_detail outistdetail on outist.id = outistdetail.istrequest "
                            + "inner join grodetailistmapping grodistmapping on grodistmapping.id = rm.grodistmapping "
                            + "inner join grodetails grod on grodistmapping.grodetail = grod.id "
                            + "inner join product p on grod.product = p.id "
                            + "inner join grorder gro on gro.id = grod.grorder "
                            + "left join locationbatchdocumentmapping lbdm on grod.id=lbdm.documentid "
                            + "inner join newproductbatch npb on lbdm.batchmapid=npb.id "
                            + "inner join in_storemaster str on str.id = npb.warehouse and str.company = gro.company "
                            + "inner join in_location ln on ln.id = npb.location and ln.company = gro.company where gro.company = ?");
                    StringBuilder grnISTCondition = new StringBuilder();
                    if (!StringUtil.isNullOrEmpty(searchString)) {
                        grnISTCondition.append(" AND ( p.productid LIKE ? OR p.name LIKE ? OR gro.gronumber LIKE ?)");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                    }
                    if (fromDate != null && toDate != null) {
                        grnISTCondition.append(" AND gro.grorderdate BETWEEN ? AND ? ");
                        params.add(fromDate);
                        params.add(toDate);
                    }
                    if (store != null) {
                        params.add(store.getId());
                        grnISTCondition.append(" AND npb.warehouse = ?");
                    }  
                    grnISTQuery.append(grnISTCondition);
                }
                if (!StringUtil.isNullOrEmpty(grnISTQuery.toString())) {
                    grnISTQuery = new StringBuilder(" UNION ALL " + grnISTQuery.toString());
                }
                if (StringUtil.isNullOrEmpty(statusType) || (!StringUtil.isNullOrEmpty(statusType) && "6".equals(statusType))) {
                    /**
                     * ApprovalStatus.REPAIRREJECT
                     */
                    params.add(companyId);
                    grnSAQuery.append(" select p.productid, p.id as pid,npb.batchname,sad.finalserialnames as srno,sad.quantity as quantity,'6', gro.gronumber AS tranno,p.name as pname,str.description as storename,gro.grorderdate as date,ln.name as locationname, sa.reason,sa.bussinessdate as rdate,sad.id as transactionid,sa.id as parent,'goodsreceipt' as module,p.description from repairgrodistmapping rm "
                            + "inner join"
                            + " in_stockadjustment sa on rm.id = sa.rejectedgrodetailistmapping "
                            + "inner join"
                            + " in_sa_detail sad on sa.id = sad.stockadjustment "
                            + "inner join"
                            + " grodetailistmapping grodistmapping on grodistmapping.id = rm.grodistmapping "
                            + "inner join"
                            + " grodetails grod on grodistmapping.grodetail = grod.id "
                            + "inner join"
                            + " product p on grod.product = p.id "
                            + "inner join"
                            + " grorder gro on gro.id = grod.grorder left join locationbatchdocumentmapping lbdm on grod.id=lbdm.documentid "
                            + "inner join"
                            + " newproductbatch npb on lbdm.batchmapid=npb.id "
                            + "inner join"
                            + " in_storemaster str on str.id = npb.warehouse and str.company = gro.company "
                            + "inner join"
                            + " in_location ln on ln.id = npb.location where gro.company = ?");
                    StringBuilder grnSACondition = new StringBuilder();
                    if (!StringUtil.isNullOrEmpty(searchString)) {

                        grnSACondition.append(" AND ( p.productid LIKE ? OR p.name LIKE ? OR gro.gronumber LIKE ?)");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                    }
                    if (fromDate != null && toDate != null) {
                        grnSACondition.append(" AND gro.grorderdate BETWEEN ? AND ? ");
                        params.add(fromDate);
                        params.add(toDate);
                    }
                    if (store != null) {
                        params.add(store.getId());
                        grnSACondition.append(" AND npb.warehouse = ?");
                    }                   
                    grnSAQuery.append(grnSACondition);
                }
                if (!StringUtil.isNullOrEmpty(grnSAQuery.toString())) {
                    grnSAQuery = new StringBuilder(" UNION ALL " + grnSAQuery.toString());
                }
            }
            
            // WORK ORDER 
            StringBuilder woISTQuery = new StringBuilder();
            StringBuilder woSAQuery = new StringBuilder();
            if (true) { // Query for QA Approval in WO
                
                if (StringUtil.isNullOrEmpty(statusType) || (!StringUtil.isNullOrEmpty(statusType) && "5".equals(statusType))) { 
                    /**
                     * ApprovalStatus.REPAIRDONE
                     */
                    params.add(companyId);
                    woISTQuery.append(" select p.productid, p.id as pid,outistdetail.batchname,outistdetail.delivered_serialnames as srno,"
                            + "outistdetail.deliveredqty as quantity,'5', "
                            + "wo.workorderid AS tranno,p.name as pname,str.description as storename,"
                            + "outist.businessdate as date,ln.name as locationname, outist.remark,outist.businessdate as rdate,"
                            + "outistdetail.id as transactionid,outist.id as parent,'Work Order' as module,p.description "
                            + "from repairwocdistmapping rwocdm inner join in_interstoretransfer outist on rwocdm.id = outist.repairwocdistmapping "
                            + "inner join in_ist_detail outistdetail on outist.id = outistdetail.istrequest "
                            + "inner join wocdetailistmapping wocdistmapping on wocdistmapping.id = rwocdm.wocdistmapping "
                            + "inner join workordercomponentdetail wocd on wocdistmapping.wocdetail = wocd.id "
                            + "inner join product p on wocd.product = p.id "
                            + "inner join workorder wo on wo.id = wocd.workorder "
                            + "left join locationbatchdocumentmapping lbdm on wocd.id=lbdm.documentid "
                            + "inner join newproductbatch npb on lbdm.batchmapid=npb.id "
                            + "inner join in_storemaster str on str.id = outist.tostore and str.company = wo.company "
                            + "inner join in_location ln on ln.id = outistdetail.delivered_location and ln.company = wo.company where wo.company = ?");
                    StringBuilder woISTCondition = new StringBuilder();
                    if (!StringUtil.isNullOrEmpty(searchString)) {
                        woISTCondition.append(" AND ( p.productid LIKE ? OR p.name LIKE ? OR wo.workordername LIKE ? OR wo.workorderid LIKE ?)");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                        
                    }
                    if (fromDate != null && toDate != null) {
                        woISTCondition.append(" AND wo.workorderdate BETWEEN ? AND ? ");
                        params.add(fromDate);
                        params.add(toDate);
                    }
                    if (store != null) {
                        params.add(store.getId());
                        woISTCondition.append(" AND npb.warehouse = ?");
                    }
                    
                    woISTCondition.append(" GROUP BY outistdetail.id");
                    woISTQuery.append(woISTCondition);
                }
                if (!StringUtil.isNullOrEmpty(woISTQuery.toString())) {
                    woISTQuery = new StringBuilder(" UNION ALL " + woISTQuery.toString());
                }
                if (StringUtil.isNullOrEmpty(statusType) || (!StringUtil.isNullOrEmpty(statusType) && "6".equals(statusType))) {
                    /**
                     * ApprovalStatus.REPAIRREJECT
                     */
                    params.add(companyId);
                    woSAQuery.append(" select p.productid, p.id as pid,sad.batchname,sad.finalserialnames as srno,sad.quantity as quantity,'6', wo.workorderid AS tranno,p.name as pname,str.description as storename,sa.bussinessdate as date,ln.name as locationname, sa.reason,sa.bussinessdate as rdate,sad.id as transactionid,sa.id as parent,'Work Order' as module,p.description from repairwocdistmapping rwocdm "
                            + "inner join"
                            + " in_stockadjustment sa on rwocdm.id = sa.rejectedwocdistmapping "
                            + "inner join"
                            + " in_sa_detail sad on sa.id = sad.stockadjustment "
                            + "inner join"
                            + " wocdetailistmapping wocdistmapping on wocdistmapping.id = rwocdm.wocdistmapping "
                            + "inner join"
                            + " workordercomponentdetail wocd on wocdistmapping.wocdetail = wocd.id "
                            + "inner join"
                            + " product p on wocd.product = p.id "
                            + "inner join"
                            + " workorder wo on wo.id = wocd.workorder left join locationbatchdocumentmapping lbdm on wocd.id=lbdm.documentid "
                            + "inner join"
                            + " in_interstoretransfer inst on wocdistmapping.istrequest = inst.id "
                            + "inner join"
                            + " in_ist_detail instdetail on inst.id = instdetail.istrequest "
                            + "inner join"
                            + " newproductbatch npb on lbdm.batchmapid=npb.id "
                            + "inner join"
                            + " in_storemaster str on str.id = inst.fromstore and str.company = wo.company "
                            + "inner join"
                            + " in_location ln on ln.id = instdetail.issued_location where wo.company = ?");
                    StringBuilder woSACondition = new StringBuilder();
                    if (!StringUtil.isNullOrEmpty(searchString)) {

                        woSACondition.append(" AND ( p.productid LIKE ? OR p.name LIKE ? OR wo.workordername LIKE ? OR wo.workorderid LIKE ?)");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                        
                    }
                    if (fromDate != null && toDate != null) {
                        woSACondition.append(" AND wo.workorderdate BETWEEN ? AND ? ");
                        params.add(fromDate);
                        params.add(toDate);
                    }
                    if (store != null) {
                        params.add(store.getId());
                        woSACondition.append(" AND npb.warehouse = ?");
                    }
                    
                    woSACondition.append(" GROUP BY sad.id");
                    woSAQuery.append(woSACondition);
                }
                if (!StringUtil.isNullOrEmpty(woSAQuery.toString())) {
                    woSAQuery = new StringBuilder(" UNION ALL " + woSAQuery.toString());
                }
            }
            
            
            StringBuilder doApproved = new StringBuilder();
            StringBuilder doRejected = new StringBuilder();
            StringBuilder doPicked = new StringBuilder();
            
            if (true) {
                /**
                 * Stock Repair Report for Delivery Order.
                 */
                if (StringUtil.isNullOrEmpty(statusType) || (!StringUtil.isNullOrEmpty(statusType) && "5".equals(statusType))) {
                    /**
                     * ApprovalStatus.REPAIRDONE
                     */
                    params.add(companyId);
                    doApproved.append(" select p.productid, p.id as pid,sad.batchname,sad.finalserialnames as srno,sad.quantity as quantity,'5', do.donumber AS tranno,p.name as pname,str.description as storename,do.orderdate as date, ln.name as locationname, inst.remark,sa.bussinessdate as rdate,sad.id as transactionid,sa.id as parent,'deliveryorder' as module,p.description from rejectdodistmapping rm")
                            .append(" INNER JOIN ")
                            .append(" in_stockadjustment sa on rm.id = sa.rejectedapproveddodqcistmapping")
                            .append(" INNER JOIN")
                            .append(" in_sa_detail sad on sa.id = sad.stockadjustment")
                            .append(" INNER JOIN")
                            .append(" dodqcistmapping dodqcistm on dodqcistm.id = rm.dodqcistmapping")
                            .append(" INNER JOIN")
                            .append(" dodetails dod on dodqcistm.dodetailid = dod.id")
                            .append(" INNER JOIN")
                            .append(" product p on dod.product = p.id")
                            .append(" INNER JOIN")
                            .append(" deliveryorder do on do.id = dod.deliveryorder")
                            .append(" INNER JOIN")
                            .append(" in_interstoretransfer inst on dodqcistm.qcistrequest = inst.id")
                            .append(" INNER JOIN")
                            .append(" in_ist_detail istdqc on inst.id = istdqc.istrequest")
                            .append(" INNER JOIN")
                            .append(" uom u on u.id = inst.uom")
                            .append(" INNER JOIN")
                            .append(" in_location ln on ln.id = istdqc.issued_location ")
                            .append(" INNER JOIN")
                            .append(" in_storemaster str on str.id = inst.fromstore where do.company = ? ");

                    StringBuilder doApprovedCondition = new StringBuilder();
                    if (!StringUtil.isNullOrEmpty(searchString)) {
                        doApprovedCondition.append(" AND ( p.productid LIKE ? OR p.name LIKE ? OR do.donumber LIKE ?)");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                    }
                    if (fromDate != null && toDate != null) {
                        doApprovedCondition.append(" AND do.orderdate BETWEEN ? AND ? ");
                        params.add(fromDate);
                        params.add(toDate);
                    }
                    if (store != null) {
                        params.add(store.getId());
                        doApprovedCondition.append(" AND str.id = ?");
                    }
                    doApproved.append(doApprovedCondition);
                    doApproved.append(" group by sad.id ");
                    
                    params.add(companyId);
                    doPicked.append(" select p.productid, p.id as pid,istd.batchname,istd.issued_serialnames as srno,istd.deliveredqty as quantity,'5', do.donumber AS tranno,p.name as pname,str.description as storename,do.orderdate as date, ln.name as locationname, ist.remark,ist.businessdate as rdate,istd.id as transactionid,ist.id as parent,'deliveryorder' as module,p.description ")
                            .append(" from rejectdodistmapping rm ")
                            .append(" INNER JOIN")
                            .append(" dodistmapping dodistm on dodistm.pickrejecteddodqcistmapping = rm.id ")
                            .append(" INNER JOIN")
                            .append(" in_interstoretransfer ist on dodistm.ist = ist.id ")
                            .append(" INNER JOIN")
                            .append(" in_ist_detail istd on ist.id = istd.istrequest ")
                            .append(" INNER JOIN")
                            .append(" dodqcistmapping dodqcistm on dodqcistm.id = rm.dodqcistmapping ")
                            .append(" INNER JOIN")
                            .append(" dodetails dod on dodqcistm.dodetailid = dod.id ")
                            .append(" INNER JOIN")
                            .append(" product p on dod.product = p.id ")
                            .append(" INNER JOIN")
                            .append(" deliveryorder do on do.id = dod.deliveryorder ")
                            .append(" INNER JOIN")
                            .append(" in_interstoretransfer instqc on dodqcistm.qcistrequest = instqc.id ")
                            .append(" INNER JOIN")
                            .append(" in_ist_detail istdqc on instqc.id = istdqc.istrequest ")
                            .append(" INNER JOIN")
                            .append(" uom u on u.id = instqc.uom ")
                            .append(" INNER JOIN")
                            .append(" in_location ln on ln.id = istdqc.issued_location  ")
                            .append(" INNER JOIN")
                            .append(" in_storemaster str on str.id = instqc.fromstore where do.company = ?");
                    StringBuilder doPickedCondition = new StringBuilder();
                    if (!StringUtil.isNullOrEmpty(searchString)) {
                        doPickedCondition.append(" AND ( p.productid LIKE ? OR p.name LIKE ? OR do.donumber LIKE ?)");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                    }
                    if (fromDate != null && toDate != null) {
                        doPickedCondition.append(" AND do.orderdate BETWEEN ? AND ? ");
                        params.add(fromDate);
                        params.add(toDate);
                    }
                    if (store != null) {
                        params.add(store.getId());
                        doPickedCondition.append(" AND str.id = ?");
                    }
                    doPicked.append(doPickedCondition);
                    doPicked.append(" group by istd.id ");
                    
                } 
                if (StringUtil.isNullOrEmpty(statusType) || (!StringUtil.isNullOrEmpty(statusType) && "6".equals(statusType))) {
                    /**
                     * ApprovalStatus.REPAIRREJECT
                     */
                    params.add(companyId);
                    doRejected.append(" select p.productid, p.id as pid,sad.batchname,sad.finalserialnames as srno,sad.quantity as quantity,'6', do.donumber AS tranno,p.name as pname,str.description as storename,do.orderdate as date,ln.name as locationname, sa.reason,sa.bussinessdate as rdate,sad.id as transactionid,sa.id as parent,'deliveryorder' as module,p.description from rejectdodistmapping rm ")
                            .append(" inner join  in_stockadjustment sa on rm.id = sa.rejecteddodqcistmapping ")
                            .append(" inner join ")
                            .append(" in_sa_detail sad on sa.id = sad.stockadjustment ")
                            .append(" inner join ")
                            .append(" dodqcistmapping dodqcistm on dodqcistm.id = rm.dodqcistmapping ")
                            .append(" inner join ")
                            .append(" dodetails dod on dodqcistm.dodetailid = dod.id ")
                            .append(" inner join ")
                            .append(" product p on dod.product = p.id ")
                            .append(" inner join ")
                            .append(" deliveryorder do on do.id = dod.deliveryorder ")
                            .append(" inner join in_interstoretransfer inst on dodqcistm.qcistrequest = inst.id ")
                            .append(" INNER JOIN")
                            .append(" in_ist_detail istdqc on inst.id = istdqc.istrequest")
                            .append(" inner join uom u on u.id = inst.uom ")
                            .append(" INNER JOIN")
                            .append(" in_location ln on ln.id = istdqc.issued_location ")
                            .append(" inner join in_storemaster str on str.id = inst.fromstore where do.company = ?");

                    StringBuilder doRejectedCondition = new StringBuilder();
                    if (!StringUtil.isNullOrEmpty(searchString)) {
                        doRejectedCondition.append(" AND ( p.productid LIKE ? OR p.name LIKE ? OR do.donumber LIKE ?)");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                        params.add("%" + searchString + "%");
                    }
                    if (fromDate != null && toDate != null) {
                        doRejectedCondition.append(" AND do.orderdate BETWEEN ? AND ? ");
                        params.add(fromDate);
                        params.add(toDate);
                    }
                    if (store != null) {
                        params.add(store.getId());
                        doRejectedCondition.append(" AND str.id = ?");
                    }
                    doRejected.append(doRejectedCondition);
                    doRejected.append(" group by sad.id ");
                }
                if(!StringUtil.isNullOrEmpty(doApproved.toString())){
                    doApproved = new StringBuilder(" UNION ALL " + doApproved.toString());
                }
                if(!StringUtil.isNullOrEmpty(doPicked.toString())){
                    doPicked = new StringBuilder(" UNION ALL " + doPicked.toString());
                }
                if(!StringUtil.isNullOrEmpty(doRejected.toString())){
                    doRejected = new StringBuilder(" UNION ALL " + doRejected.toString());
                }
            }  
            
            
            String allQuery = consignQuery + buidAssemblyqr + " UNION ALL " + saQuery + " UNION ALL " + stockRequest + " UNION ALL " + interStock + grnISTQuery.toString() + grnSAQuery.toString() + woISTQuery.toString() + woSAQuery.toString() + doApproved.toString() + doPicked.toString() + doRejected.toString();

            String seleQuery = "SELECT tb.productid,tb.pid,tb.batchName,tb.srno,tb.quantity,tb.repair_status,tb.tranno,tb.pname,tb.storename,tb.date,tb.locationname,tb.reason,tb.rdate,tb.transactionid,tb.parent,tb.module,tb.description FROM (" + allQuery + ") as tb ORDER BY tb.date DESC ";
            returnList = executeSQLQuery( seleQuery, params.toArray());

            totalCount = returnList.size();
//            if (paging != null) {
//                paging.setTotalRecord(totalCount);
//                returnList = executeQueryPaging( seleQuery, params.toArray(), paging);
//            }
            if (paging != null) {
                seleQuery += " LIMIT " + paging.getOffset() + "," + paging.getLimit();
            }
            returnList = executeSQLQuery( seleQuery, params.toArray());

        } catch (Exception ex) {
            Logger.getLogger(ConsignmentDAOImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        if (!returnList.isEmpty()) {
            retObj = new KwlReturnObject(true, null, null, returnList, totalCount);
        }
        return retObj;
    }

    @Override
    public KwlReturnObject getSODetails(HashMap<String, Object> requestParams) throws ServiceException {
        List list = null;
        String mysqlQuery = "";
        int count = 0;
        ArrayList params = new ArrayList();

//        params.add((String) requestParams.get("productid"));
//        params.add((String) requestParams.get("companyid"));
//        params.add(new Date());
//        mysqlQuery = "select sodetails.id,sodetails.lockquantitydue from sodetails "
//                + " inner join salesorder on sodetails.salesorder=salesorder.id "
//                + " where sodetails.product=? and salesorder.lockquantityflag=1 "
//                + " and  salesorder.isconsignment='T' and sodetails.lockquantitydue>0 "
//                + " and salesorder.company=? AND salesorder.fromdate is NOT NULL AND salesorder.fromdate >= ? "
//                + " order by sodetails.product,salesorder.fromdate ";
//        
        params.add((String) requestParams.get("salesOrderId"));

        mysqlQuery = "select sodetails.id,sodetails.lockquantitydue from sodetails "
                + " WHERE sodetails.salesorder= ? ";


        list = executeSQLQuery( mysqlQuery, params.toArray());
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public boolean isPendingForApproval(String consignmentReturnId) throws ServiceException {
        String hql = "SELECT 1 FROM Consignment c INNER JOIN c.consignmentApprovalDetails cd WHERE c.moduleRefId = ? AND (cd.approvalStatus = ? OR cd.repairStatus = ?)";
        
        List params = new ArrayList();
        params.add(consignmentReturnId);
        params.add(ApprovalStatus.PENDING);
        params.add(ApprovalStatus.REPAIRPENDING);
        List list = executeQuery( hql, params.toArray());
        boolean pending = false;
        if(!list.isEmpty()){
            pending = true;
        }
        return pending;
    }
    
    @Override
    public String getSalesPersonEmailIdBySRDetailId(String srDetailid,String companyId) throws ServiceException{
        String salesPersonId=null;
        List list = null;
        String mysqlQuery = "SELECT mi.emailid from srdetails srd  INNER JOIN dodetails dod ON dod.id=srd.dodetails INNER JOIN sodetails sod ON sod.id=dod.sodetails INNER JOIN salesorder so ON  so.id=sod.salesorder INNER JOIN masteritem mi ON mi.id=so.salesperson WHERE srd.id=? AND srd.company=? ";
        ArrayList params = new ArrayList();
        params.add(srDetailid);
        params.add(companyId);
        list = executeSQLQuery( mysqlQuery, params.toArray());
        if(list != null && !list.isEmpty()) {
            salesPersonId=(String)list.get(0);
        }
        return salesPersonId;
    }

    @Override
    public KwlReturnObject getBuildProductsAssemblyQaDetails(Map<String, String> requestMap) throws ServiceException {
        List params = new ArrayList();
        List list = Collections.EMPTY_LIST;
        try {
            String conditionHql = "where";
            if (requestMap.containsKey("productbuildid") && !StringUtil.isNullOrEmpty(requestMap.get("productbuildid"))) {

                conditionHql += "  aspa.prBuild.id= ? ";
                params.add(requestMap.get("productbuildid"));
            }
            if (requestMap.containsKey("qadetailid") && !StringUtil.isNullOrEmpty(requestMap.get("qadetailid"))) {

                conditionHql += " and aspa.id= ? ";
                params.add(requestMap.get("qadetailid"));
            }
            String hql = "FROM AssemblyProductApprovalDetails  aspa  " + conditionHql;

            list = executeQuery(hql, params.toArray());

        } catch (Exception e) {
            throw ServiceException.FAILURE("ConsignmentDAOImpl.getBuildProductsAssemblyQaDetails", e);
        }

        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
    @Override
    public KwlReturnObject getConsignmentObjectByTransactionNumber(Map<String, Object> requestMap) throws ServiceException {
        List params = new ArrayList();
        List list = Collections.EMPTY_LIST;
        try {
            String conditionHql = "where";
            if (requestMap.containsKey(Constants.companyKey) && requestMap.get(Constants.companyKey)!= null && !StringUtil.isNullOrEmpty(requestMap.get(Constants.companyKey).toString())) {
                conditionHql += "  con.company.companyID= ? ";
                params.add(requestMap.get(Constants.companyKey));
            }
            if (requestMap.containsKey("transactionno") && requestMap.get("transactionno")!= null && !StringUtil.isNullOrEmpty(requestMap.get("transactionno").toString())) {

                conditionHql += " and con.transactionNo= ? ";
                params.add(requestMap.get("transactionno"));
            }
            String hql = "FROM Consignment con " + conditionHql;

            list = executeQuery(hql, params.toArray());

        } catch (Exception e) {
            throw ServiceException.FAILURE("ConsignmentDAOImpl.getConsignmentIdsByTransactionNumber", e);
        }

        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
    @Override
    public KwlReturnObject getBuildAssemblyObjectByTransactionNumber(Map<String, Object> requestMap) throws ServiceException {
        List params = new ArrayList();
        List list = Collections.EMPTY_LIST;
        try {
            String conditionHql = "where";
            if (requestMap.containsKey(Constants.companyKey) && requestMap.get(Constants.companyKey)!= null && !StringUtil.isNullOrEmpty(requestMap.get(Constants.companyKey).toString())) {
                conditionHql += "  apdb.prBuild.company.companyID= ? ";
                params.add(requestMap.get(Constants.companyKey));
            }
            if (requestMap.containsKey("transactionno") && requestMap.get("transactionno")!= null && !StringUtil.isNullOrEmpty(requestMap.get("transactionno").toString())) {

                conditionHql += " and apdb.prBuild.refno= ? ";
                params.add(requestMap.get("transactionno"));
            }
            String hql = "FROM AssemblyProductApprovalDetails apdb " + conditionHql;

            list = executeQuery(hql, params.toArray());

        } catch (Exception e) {
            throw ServiceException.FAILURE("ConsignmentDAOImpl.getBuildAssemblyObjectByTransactionNumber", e);
        }

        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
    @Override
    public KwlReturnObject getSAApprovedObjectByTransactionNumber(Map<String, Object> requestMap) throws ServiceException {
        List params = new ArrayList();
        List list = Collections.EMPTY_LIST;
        try {
            String conditionHql = "where";
            if (requestMap.containsKey(Constants.companyKey) && requestMap.get(Constants.companyKey)!= null && !StringUtil.isNullOrEmpty(requestMap.get(Constants.companyKey).toString())) {
                conditionHql += "  saa.stockAdjustment.company.companyID= ? ";
                params.add(requestMap.get(Constants.companyKey));
            }
            if (requestMap.containsKey("transactionno") && requestMap.get("transactionno")!= null && !StringUtil.isNullOrEmpty(requestMap.get("transactionno").toString())) {

                conditionHql += " and saa.stockAdjustment.transactionNo= ? ";
                params.add(requestMap.get("transactionno"));
            }
            String hql = "FROM SAApproval saa " + conditionHql;

            list = executeQuery(hql, params.toArray());

        } catch (Exception e) {
            throw ServiceException.FAILURE("ConsignmentDAOImpl.getSAApprovedObjectByTransactionNumber", e);
        }

        return new KwlReturnObject(true, "", "", list, list.size());
    }
    @Override
    public KwlReturnObject getStoreOrInterStockObjectByTransactionNumber(Map<String, Object> requestMap) throws ServiceException {
        List params = new ArrayList();
        List list = Collections.EMPTY_LIST;
        try {
            String conditionHql = "where";
            if (requestMap.containsKey(Constants.companyKey) && requestMap.get(Constants.companyKey)!= null && !StringUtil.isNullOrEmpty(requestMap.get(Constants.companyKey).toString())) {
                conditionHql += "  inst.company.companyID= ? ";
                params.add(requestMap.get(Constants.companyKey));
            }
            if (requestMap.containsKey("transactionno") && requestMap.get("transactionno")!= null && !StringUtil.isNullOrEmpty(requestMap.get("transactionno").toString())) {

                conditionHql += " and inst.transactionNo= ? ";
                params.add(requestMap.get("transactionno"));
            }
            String hql = "FROM InterStoreTransferRequest inst " + conditionHql;

            list = executeQuery(hql, params.toArray());

        } catch (Exception e) {
            throw ServiceException.FAILURE("ConsignmentDAOImpl.getStoreOrInterStockObjectByTransactionNumber", e);
        }

        return new KwlReturnObject(true, "", "", list, list.size());
    }
    @Override
    public KwlReturnObject getStockTransfer(Map<String, Object> requestMap) throws ServiceException {
        List params = new ArrayList();
        List list = Collections.EMPTY_LIST;
        try {
            String conditionHql = "where";
            if (requestMap.containsKey("transactionid") && requestMap.get("transactionid")!= null && !StringUtil.isNullOrEmpty(requestMap.get("transactionid").toString())) {
                conditionHql += " stapp.stockTransferId= ? ";
                params.add(requestMap.get("transactionid"));
            }
            String hql = "FROM StockTransferApproval stapp " + conditionHql;

            list = executeQuery(hql, params.toArray());

        } catch (Exception e) {
            throw ServiceException.FAILURE("ConsignmentDAOImpl.getStoreOrInterStockObjectByTransactionNumber", e);
        }

        return new KwlReturnObject(true, "", "", list, list.size());
    }
    @Override
    public KwlReturnObject getStockTransferDetail(Map<String, Object> requestMap) throws ServiceException {
        List params = new ArrayList();
        List list = Collections.EMPTY_LIST;
        try {
            String conditionHql = "where";
            if (requestMap.containsKey("transactionid") && requestMap.get("transactionid")!= null && !StringUtil.isNullOrEmpty(requestMap.get("transactionid").toString())) {

                conditionHql += " stappd.stockTransferDetailId= ? ";
                params.add(requestMap.get("transactionid"));
            }
            String hql = "FROM StockTransferDetailApproval stappd " + conditionHql;

            list = executeQuery(hql, params.toArray());

        } catch (Exception e) {
            throw ServiceException.FAILURE("ConsignmentDAOImpl.getStockTransferDetail", e);
        }

        return new KwlReturnObject(true, "", "", list, list.size());
    }
}
