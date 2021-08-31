/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockout.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.NewBatchSerial;
import com.krawler.common.admin.NewProductBatch;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.stockmovement.StockMovement;
import com.krawler.inventory.model.approval.sa.SADetailApproval;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.stockout.AdjustmentStatus;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockout.StockAdjustmentDAO;
import com.krawler.inventory.model.stockout.StockAdjustmentDraft;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vipin Gupta
 */
public class StockAdjustmentDAOimpl extends BaseDAO implements StockAdjustmentDAO {

    @Override
    public void saveOrUpdateAdjustment(StockAdjustment stockAdjustment) throws ServiceException {
        super.saveOrUpdate(stockAdjustment);
    }

    @Override
    public StockAdjustment getStockAdjustmentById(String id) throws ServiceException {
        return (StockAdjustment) get(StockAdjustment.class, id);
    } 

    @Override
    public List<StockAdjustment> getStockAdjustmentBySequenceNo(Company company, String sequenceNo) throws ServiceException {

        //and isdeleted=?  removed from hql to get the transaction
        StringBuilder hql = new StringBuilder("FROM StockAdjustment WHERE company = ? AND transactionNo = ? ");
        List params = new ArrayList();
        params.add(company);
        params.add(sequenceNo);
//        params.add(false);
        List<StockAdjustment> list = executeQuery( hql.toString(), params.toArray());

        return list;
    }
    
    @Override
    public double getTotalAmountOFSABySequenceNo(Company company,String sequenceNo) throws ServiceException{
        double amount=0;
        StringBuilder hql = new StringBuilder("select SUM(finalQuantity * pricePerUnit) FROM StockAdjustment WHERE company = ? AND transactionNo = ? GROUP BY transactionNo");
        List params = new ArrayList();
        params.add(company);
        params.add(sequenceNo);
        List list = executeQuery(hql.toString(), params.toArray());
        if(list != null && !list.isEmpty()){
            amount=(double)list.get(0);
        }
        return amount;
    }
    
    public void deleteSAPermanently(StockAdjustment stockAdjustment) throws ClassNotFoundException, ServiceException {
        List params = new ArrayList();
        List<String> smList = getStockMovementbySAid(stockAdjustment.getId());
        for(String smid : smList) {
            params.clear();
            Class cls = Class.forName(StockMovement.class.getName());
            StockMovement sm = (StockMovement)get(cls, smid);
            delete(sm);
        }
        delete(stockAdjustment);
        
    }
    public List<String> getStockMovementbySAid(String said) throws ServiceException {
        StringBuilder hql = new StringBuilder("select id FROM in_stockmovement WHERE modulerefid = ? ");
        List params = new ArrayList();
        params.add(said);
        List<String> list = executeSQLQuery( hql.toString(), params.toArray());

        return list;
    }
     public List getStockAdjustmentByProductBuild(Company company, String productbuild) throws ServiceException {

        //and isdeleted=?  removed from hql to get the transaction
//        StringBuilder hql = new StringBuilder("FROM productBuild WHERE company = ? AND ID = ? ");
        String query = "SELECT stockadjustment from productbuild where company= ? AND id = ? ";
        List params = new ArrayList();
        params.add(company.getCompanyID());
        params.add(productbuild);
//        params.add(false);
        List list = executeSQLQuery(query, params.toArray());

        return list;
    }
    @Override
    public List<StockAdjustment> getStockAdjustmentList(Company company, Set<Store> storeSet, Product product, Set<AdjustmentStatus> status, String adjustmentType, Date fromDate, Date toDate, String searchString, Paging paging, HashMap<String, Object> requestParams) throws ServiceException {
        //AND isdeleted='F'    removed from hql to display all records in grid  
        StringBuilder hql = new StringBuilder("select distinct sa FROM StockAdjustment sa  WHERE sa.company = ? AND sa.transactionNo not like 'R%'");
        List params = new ArrayList();
        params.add(company);
        if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND sa.store.id IN ( ");
            Iterator<Store> itr = storeSet.iterator();
            while (itr.hasNext()) {
                Store store = itr.next();
                if (first) {
                    storeIn.append("'").append(store.getId()).append("'");
                    first = false;
                } else {
                    storeIn.append(",").append("'").append(store.getId()).append("'");
                }
            }
            storeIn.append(")");
            hql.append(storeIn);
        }
        if (product != null) {
            hql.append(" AND sa.product = ? ");
            params.add(product);
        }
//        if (status != null) {
//            hql.append(" AND sa.status = ? ");
//            params.add(status);
//        }
        
        if (status != null && !status.isEmpty()) {
            boolean firstStatus = true;
            StringBuilder statusIn = new StringBuilder(" AND sa.status IN ( ");
            Iterator<AdjustmentStatus> itr = status.iterator();
            while (itr.hasNext()) {
                AdjustmentStatus stat = itr.next();
                if (firstStatus) {
                    statusIn.append("'").append(stat.ordinal()).append("'");
                    firstStatus = false;
                } else {
                    statusIn.append(",").append("'").append(stat.ordinal()).append("'");
                }
            }
            statusIn.append(")");
            hql.append(statusIn);
        }
        if (fromDate != null && toDate != null && (fromDate.before(toDate) || fromDate.equals(toDate))) {
            hql.append(" AND sa.businessDate >= ? AND sa.businessDate <= ? ");
            params.add(fromDate);
            params.add(toDate);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
//            hql.append(" AND (sa.product.name LIKE ? OR sa.product.productid LIKE ? OR sad.serialNames LIKE ? OR sad.finalSerialNames LIKE ? OR sa.transactionNo LIKE ? ) ");
            hql.append(" AND (sa.product.name LIKE ? OR sa.product.productid LIKE ?   OR sa.transactionNo LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
//            params.add("%" + searchString + "%");
//            params.add("%" + searchString + "%");
        }
        if (!StringUtil.isNullOrEmpty(adjustmentType)) {
            hql.append(" AND sa.adjustmentType = ? ");
            params.add(adjustmentType);
        }
        if (requestParams.containsKey("adjustmentReason") && requestParams.get("adjustmentReason") != null && !StringUtil.isNullOrEmpty((String) requestParams.get("adjustmentReason"))) {
            hql.append(" AND sa.reason = ? ");
            params.add((String) requestParams.get("adjustmentReason"));
        }
        if (requestParams.containsKey("isJobWorkInReciever") && requestParams.get("isJobWorkInReciever") != null) {
            hql.append(" AND sa.isJobWorkIn = ? ");
            if (Boolean.parseBoolean(requestParams.get("isJobWorkInReciever").toString())) {
                params.add(true);
            } else {
                params.add(false);
            }
        }
        if (requestParams.containsKey("stockAdjustmentID") && requestParams.get("stockAdjustmentID") != null && !StringUtil.isNullOrEmpty((String) requestParams.get("stockAdjustmentID"))) {
            hql.append(" AND sa.id = ? ");
            params.add((String) requestParams.get("stockAdjustmentID"));
        }
        if (requestParams.containsKey("type") && requestParams.get("type") != null && !StringUtil.isNullOrEmpty((String)requestParams.get("type")) && Integer.parseInt(requestParams.get("type").toString())==2) {
            hql.append(" Group By sa.transactionNo ");
        }
        hql.append(" ORDER BY sa.createdOn DESC, sa.transactionNo DESC ");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
        }
        return list;

    }

    @Override
    public List<StockAdjustment> getStockAdjustmentSummary(Company company, Set<Store> storeSet, Product product, AdjustmentStatus status, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("SELECT sa.product, sa.store, SUM(sa.quantity), SUM(sa.quantity * sa.pricePerUnit) ,sa.packaging,sa.adjustmentType FROM StockAdjustment sa WHERE sa.company = ? AND isdeleted='F' ");
        List params = new ArrayList();
        params.add(company);
        if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND sa.store.id IN ( ");
            Iterator<Store> itr = storeSet.iterator();
            while (itr.hasNext()) {
                Store store = itr.next();
                if (first) {
                    storeIn.append("'").append(store.getId()).append("'");
                    first = false;
                } else {
                    storeIn.append(",").append("'").append(store.getId()).append("'");
                }
            }
            storeIn.append(")");
            hql.append(storeIn.toString());
        }
        if (product != null) {
            hql.append(" AND sa.product = ? ");
            params.add(product);
        }
        if (status != null) {
            hql.append(" AND sa.status = ? ");
            params.add(status);
        }
        if (fromDate != null && toDate != null && (fromDate.before(toDate) || fromDate.equals(toDate))) {
            hql.append(" AND sa.businessDate >= ? AND sa.businessDate <= ? ");
            params.add(fromDate);
            params.add(toDate);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (sa.product.name LIKE ? OR sa.product.productid LIKE ? OR sa.transactionNo LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        if (storeSet.size() == 1) {
            hql.append(" GROUP BY sa.product.productid,sa.store.abbreviation");
        } else {
            hql.append(" GROUP BY sa.product.productid");
        }
//        hql.append(" ORDER BY product.productid DESC ");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
        }
        List saList = new ArrayList();
        if (!list.isEmpty()) {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] objs = (Object[]) itr.next();
                Product p = (Product) objs[0];
                Store s = (Store) objs[1];
                double totalQuantity = (Double) (objs[2] != null ? objs[2] : 0);
                double totalAmount = (Double) (objs[3] != null ? objs[3] : 0);
                double perUnitPrice = totalQuantity != 0 ? Math.abs(totalAmount / totalQuantity) : 0;
                Packaging packaging = (Packaging) objs[4];
                String adjustmentType = (String) (objs[5] != null ? objs[5] : "");
                if (p != null) {
                    StockAdjustment sa = new StockAdjustment(p, s, p.getUnitOfMeasure(), totalQuantity, perUnitPrice, null);
                    if (packaging != null) {
                        sa.setPackaging(packaging);
                    }
                    sa.setAdjustmentType(adjustmentType);
                    saList.add(sa);
                }

            }
        }
        return saList;
    }

    @Override
    public void saveOrUpdateDraft(StockAdjustmentDraft stockAdjustmentDraft) throws ServiceException {
        super.saveOrUpdate(stockAdjustmentDraft);
    }

    @Override
    public StockAdjustmentDraft getStockAdjustmentDraftById(String id) throws ServiceException {
        return (StockAdjustmentDraft) get(StockAdjustmentDraft.class, id);
    }

    @Override
    public List<StockAdjustmentDraft> getStockAdjustmentDraftList(Company company, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM StockAdjustmentDraft WHERE company = ? ");
        List params = new ArrayList();
        params.add(company);
        hql.append("ORDER BY bussinessDate DESC");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
        }
        return list;
    }

    @Override
    public void removeDraft(StockAdjustmentDraft stockAdjustmentDraft) throws ServiceException {
        delete(stockAdjustmentDraft);
    }

    @Override
    public StockAdjustmentDraft getDraftByStoreAndBussinessDate(Company company, Store store, Date businessDate) throws ServiceException {
        StockAdjustmentDraft stockAdjustmentDraft = null;
        String hql = "FROM StockAdjustmentDraft WHERE company = ? AND store = ? AND bussinessDate = ? ";
        List params = new ArrayList();
        params.add(company);
        params.add(store);
        params.add(businessDate);
        List list = executeQuery( hql, params.toArray());
        if (list != null) {
            stockAdjustmentDraft = (StockAdjustmentDraft) list.get(0);
        }
        return stockAdjustmentDraft;
    }

    @Override
    public void saveSADetailInTemporaryTable(String product, String store, String location, String batchName, String serialName, Date mfgdate, Date expdate, Date warrantyexpfromdate, Date warrantyexptodate,String sku) throws ServiceException {

        String sqlQry = " INSERT INTO in_temp_stockadjustmentdetail "
                + " (id,product,store,location,batchname,serialname,mfgdate,expdate,warrantyexpfromdate,warrantyexptodate,used,skufield) "
                + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?) ";

        String id = UUID.randomUUID().toString();
        List params = new ArrayList();
        params.add(id);
        params.add(product);
        params.add(store);
        params.add(location);
        params.add(batchName);
        params.add(serialName);
        params.add(mfgdate);
        params.add(expdate);
        params.add(warrantyexpfromdate);
        params.add(warrantyexptodate);
        params.add(0);
        params.add(sku);
        try {
            executeSQLUpdate( sqlQry, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(StockAdjustmentDAOimpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public List getBatchSerialDataFromTemporaryTable(String product, String store, String location, String batchName, String serialName) throws ServiceException {
        List dataList = new ArrayList();
        String fetchDateQry = " SELECT id,mfgdate,expdate,warrantyexpfromdate,warrantyexptodate FROM in_temp_stockadjustmentdetail WHERE product=? AND store= ? AND location= ? AND batchname=? ";
        List params = new ArrayList();
        params.add(product);
        params.add(store);
        params.add(location);
        params.add(batchName);

        if (!StringUtil.isNullOrEmpty(serialName)) {
            fetchDateQry += " AND serialname= ?";
            params.add(serialName);
        }

        try {
            dataList = executeSQLQuery( fetchDateQry, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(StockAdjustmentDAOimpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return dataList;
        }
    }

    @Override
    public void setBatchSerialDateDetail(Company company) throws ServiceException {

        try {
            String getQry1 = " SELECT sd.id, sd.product, sd.store, sd.location, sd.batchname, sd.serialname, sd.mfgdate,sd.expdate,sd.warrantyexpfromdate,sd.warrantyexptodate,skufield "
                    + " FROM in_temp_stockadjustmentdetail sd "
                    + " INNER JOIN in_storemaster s ON sd.store = s.id "
                    + " WHERE sd.used = 0 AND s.company =?";
            List getQryParams = new ArrayList();
            getQryParams.add(company.getCompanyID());
            List list = executeSQLQuery( getQry1, getQryParams.toArray());
            for (Object object : list) {
                if (object != null) {
                    Object[] objs = (Object[]) object;
                    String tempId = (String) objs[0];
                    String productId = (String) objs[1];
                    String storeId = (String) objs[2];
                    String locationId = (String) objs[3];
                    String batchName = (String) objs[4];
                    String serialName = (String) objs[5];
                    String skuVale = (String) objs[10];
                    Product product = (Product) get(Product.class, productId);
                    Date[] dates =product.getWarrantyperiod()>0? getTodayDateAndFutureDate(product.getWarrantyperiod()):null; // change this to product.getwarrantyinterval
                    Date today = product.getWarrantyperiod()>0?dates[0]:null;
                    Date futureDate =dates!=null? dates[1]:null;

                    Date mfgDate = today;
                    Date expDate = futureDate;
                    Date warrantyExpFromDate = today;
                    Date warrantyExpToDate = futureDate;

                    mfgDate = objs[6] != null ? (Date) objs[6] : today;
                    expDate = objs[7] != null ? (Date) objs[7] : futureDate;
                    warrantyExpFromDate = objs[8] != null ? (Date) objs[8] : today;
                    warrantyExpToDate = objs[9] != null ? (Date) objs[9] : futureDate;
                    List<NewBatchSerial> serialList = null;
                    List<NewProductBatch> batchList = null;
                    String batchNameParam = batchName == null ? "" : batchName;
                    if (!StringUtil.isNullOrEmpty(serialName)) {
                        String hql = " FROM NewBatchSerial  WHERE   (batch IS NOT NULL AND location IS NOT NULL AND batch.warehouse IS NOT NULL) AND  company=? AND product = ? AND serialname = ? AND batch.batchname = ? ";
                        serialList = executeQuery( hql, new Object[]{company, productId, serialName, batchNameParam});
                        for (NewBatchSerial srl : serialList) {
                            srl.getBatch().setMfgdate(mfgDate);
                            srl.getBatch().setExpdate(expDate);
                            srl.setExpfromdate(warrantyExpFromDate);
                            srl.setExptodate(warrantyExpToDate);
                            srl.setSkufield(skuVale);
                            super.saveOrUpdate(srl);
                        }
                    } else {
                        String hql = " FROM NewProductBatch  WHERE (location IS NOT NULL AND warehouse IS NOT NULL)  AND company=? AND product = ? AND batchname = ? ";
//                       String hqlpb = " FROM NewProductBatch  WHERE  product= ? AND batchname = ? ";  
                        batchList = executeQuery( hql, new Object[]{company, productId, batchNameParam});
                        for (NewProductBatch srl : batchList) {
                            srl.setMfgdate(mfgDate);
                            srl.setExpdate(expDate);
                            super.saveOrUpdate(srl);
                        }
                    }



                    String deleteQry = " DELETE FROM  in_temp_stockadjustmentdetail WHERE id= ?";
                    int numRows = executeSQLUpdate( deleteQry, new String[]{tempId});
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(StockAdjustmentDAOimpl.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    private Date[] getTodayDateAndFutureDate(int timeinterval) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        int warrantyDays = timeinterval;
        Date[] returnDates = null;

        try {
            Date d1 = Calendar.getInstance().getTime();
            Date today;
            today = df.parse(df.format(d1));
            Calendar prodExpiryDate = Calendar.getInstance();
            prodExpiryDate.setTime(today);
            prodExpiryDate.add(Calendar.DATE, warrantyDays);
            Date futureDate = df.parse(df.format(prodExpiryDate.getTime()));

            returnDates = new Date[]{today, futureDate};
        } catch (ParseException ex) {
            Logger.getLogger(StockAdjustmentDAOimpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return returnDates;
    }
    
    @Override
    public KwlReturnObject getStockAdjustmentJEs(Map<String, Object> request) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = (String) request.get(Constants.companyKey);

            ArrayList params = new ArrayList();
            String condition = "";
            params.add(companyid);

            String jeIds = (String) request.get("jeIds");
            if (!StringUtil.isNullOrEmpty(jeIds)) {
                condition += " and journalEntry.ID IN (" + jeIds + ")";
            }
            if (request.containsKey("inventoryjeid") && request.get("inventoryjeid") != null && !StringUtil.isNullOrEmpty(request.get("inventoryjeid").toString())) {
                String inventoryjeid = request.get("inventoryjeid").toString();
                if (!StringUtil.isNullOrEmpty(inventoryjeid)) {
                    condition += " and inventoryJE.ID = '" + inventoryjeid + "' ";
                }
            }
            String query = "from StockAdjustment where company.companyID=? " + condition;
            list = executeQuery(query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            Logger.getLogger(StockAdjustmentDAOimpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("StockAdjustmentDAOimpl.getStockAdjustmentJEs:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }

    @Override
    public List<StockAdjustment> getStockAdjustmentRows(Map<String, Object> request) throws ServiceException {
        StringBuilder hql = new StringBuilder("select distinct sa FROM StockAdjustment sa left join sa.stockAdjustmentDetail sad "
                + " WHERE sa.company = ?  and isdeleted='F' ");
        List params = new ArrayList();
        if (request.containsKey("company")) {
            params.add((Company) request.get("company"));
        }
        if (request.containsKey("transactionno") && !StringUtil.isNullOrEmpty(request.get("transactionno").toString())) {
            hql.append(" and sa.transactionNo=? ");
            params.add(request.get("transactionno"));
        }
        if (request.containsKey("transactionID")) {
            String notinquery = (String) request.get("transactionID");
            notinquery = AccountingManager.getFilterInString(notinquery);
            hql.append(" AND sa.id in  " + notinquery);
//            params.add(request.get("transactionID"));
        }
        List list = executeQuery(hql.toString(), params.toArray());

        return list;

    }

    @Override
    public List<SADetailApproval> getStockAdjustmentApprovalDetail(String stockAdjustmentDetailID) throws ServiceException {
        List<SADetailApproval> list = null;
        String query = "from SADetailApproval where stockAdjustmentDetail.id=? ";
        list = executeQuery(query, new Object[]{stockAdjustmentDetailID});
        return list;
    }
    /**
     * 
     * @param nObject
     * @return
     * @Desc : Check whether Stock adjustment created while Creating GRN of Job Work FG
     * and Stock out for ingredients
     * @throws ServiceException 
     */
    public KwlReturnObject getSAfromGRN(JSONObject nObject) throws ServiceException {
        String said = nObject.optString("said");
        String companyid = nObject.optString("companyid");
        String selQuery = "select id from grodstockoutistmapping where stockadjustment=?";
        List list = executeSQLQuery(selQuery, new Object[]{said});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * 
     * @param nObject
     * @return
     * @Desc : Check whether Stock adjustment created while Creating DO of  
     * and Stock out for ingredients
     * @throws ServiceException 
     */
    public KwlReturnObject getSAfromDO(JSONObject nObject) throws ServiceException {
        String said = nObject.optString("said");
        String companyid = nObject.optString("companyid");
        String selQuery = "SELECT id FROM dodistmapping   WHERE stockadjustment=?";
        List list = executeSQLQuery(selQuery, new Object[]{said});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    /**
     * @param params
     * @return
     * @Desc Check whether the stock adjustment document is created for QC flow.
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject checkStockAdjustmentForQC(JSONObject params) throws ServiceException {
        int cnt=0;
        String sqlQuery = "select count(sa.id) from in_stockadjustment sa "
                + "where sa.id = ? and "
                + "(sa.rejectedgrodetailistmapping is not null or sa.rejectedapproveddodqcistmapping is not null or sa.rejecteddodqcistmapping is not null or sa.approveddodqcistmapping is not null)";
       List list = executeSQLQuery(sqlQuery, new Object[]{params.optString("said")});
       cnt=(list!=null&&list.size()>0)?(Integer.parseInt(list.get(0).toString())):0;
        return new KwlReturnObject(true, "", null, list, cnt);
    }
}
