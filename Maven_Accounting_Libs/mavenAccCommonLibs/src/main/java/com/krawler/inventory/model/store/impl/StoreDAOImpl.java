/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.store.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.InventoryWarehouse;
import com.krawler.common.admin.StoreMaster;
import com.krawler.common.admin.User;
import com.krawler.common.admin.UserLogin;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreDAO;
import com.krawler.inventory.model.store.StoreType;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import java.util.*;

/**
 *
 * @author Vipin Gupta
 */
public class StoreDAOImpl extends BaseDAO implements StoreDAO {

    @Override
    public InventoryWarehouse getERPWarehouse(String storeId) throws ServiceException {
        return (InventoryWarehouse) get(InventoryWarehouse.class, storeId);
    }

    @Override
    public Store getStoreById(String storeId) {
        Object obj = get(Store.class, storeId);
        Store store = (Store) obj;
        return store;
    }

    public Store getStoreByCode(Company company, String analysisCode) throws ServiceException {
        Store store = null;
        String hql = "FROM Store WHERE company = ? AND analysisCode = ?";
        List params = new ArrayList();
        params.add(company);
        params.add(analysisCode);
        List list = executeQuery(hql, params.toArray());
        if (!list.isEmpty()) {
            store = (Store) list.get(0);
        }
        return store;
    }

    @Override
    public Store getStoreByAbbreviation(Company company, String abbreviation) throws ServiceException {
        Store store = null;
        String hql = "FROM Store WHERE company = ? AND abbreviation = ?";
        List params = new ArrayList();
        params.add(company);
        params.add(abbreviation);
        List list = executeQuery(hql, params.toArray());
        if (!list.isEmpty()) {
            store = (Store) list.get(0);
        }
        return store;
    }

    @Override
    public List<Store> getStores(Company company, String searchString, Paging paging,boolean isForAvailableWarehouse) throws ServiceException {
        return getStores(company, null, null, searchString, paging,isForAvailableWarehouse, false, false);
    }

    @Override
    public List<Store> getStores(Company company, Boolean isActive, StoreType[] storeTypes, String searchString, Paging paging, boolean isForAvailableWarehouse, boolean includeQAAndRepairStore,boolean includePickandPackStore) throws ServiceException {
        StringBuilder hql = new StringBuilder("SELECT s FROM Store s ");
        if (!includeQAAndRepairStore || !includePickandPackStore) {
            hql.append(", ExtraCompanyPreferences ecp, CompanyAccountPreferences comp ");
        }
        hql.append("WHERE s.company = ? ");
        List params = new ArrayList();
        params.add(company);
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (s.abbreviation LIKE ? OR s.description LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        if (isActive != null) {
            hql.append(" AND s.active = ? ");
            params.add(isActive.booleanValue());
        }
        if (storeTypes != null && storeTypes.length > 0) {
            boolean first = true;
            StringBuilder typeIn = new StringBuilder(" AND s.storeType IN ( ");
            for (StoreType st : storeTypes) {
                if (first) {
                    typeIn.append(st.ordinal());
                    first = false;
                } else {
                    typeIn.append(",").append(st.ordinal());
                }
            }
            typeIn.append(")");
            hql.append(typeIn);
        }
        if (isForAvailableWarehouse) {
            String storeNotIn = " AND s.id NOT IN (SELECT warehouseid.id FROM ExciseDetailsTemplateMap WHERE companyid=?) ";
            hql.append(storeNotIn);
            params.add(company);
        }
        if (!includeQAAndRepairStore) {
            hql.append(" AND ecp.id = ? and comp.ID  = ? ");
            StringBuilder storeIds = getQAAndRepairStoreId(company);
            if (!StringUtil.isNullOrEmpty(storeIds.toString())) {
                hql.append(" AND s.id NOT IN ( ").append(storeIds.toString()).append(") ");
            }
            params.add(company.getCompanyID());
            params.add(company.getCompanyID());
        }
        if (!includePickandPackStore) {
            hql.append(" AND ecp.id = ? and comp.ID  = ? ");
            StringBuilder storeIds = getpackStoreId(company);
            if (!StringUtil.isNullOrEmpty(storeIds.toString())) {
                hql.append(" AND s.id NOT IN ( ").append(storeIds.toString()).append(") ");
            }
            params.add(company.getCompanyID());
            params.add(company.getCompanyID());
        }
        
        hql.append(" ORDER BY s.abbreviation ");
        List list = executeQuery(hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null && paging.isValid()) {
            list = executeQueryPaging(hql.toString(), params.toArray(), paging);
            paging.setTotalRecord(totalCount);
        }
        return list;
    }

    @Override
    public Store getDefaultStore(Company company) throws ServiceException {
        Store store = null;
        StringBuilder hql = new StringBuilder(" FROM Store WHERE company = ? AND defaultStore = ?");
        List params = new ArrayList();
        params.add(company);
        params.add(true);

        Paging paging = new Paging(0, 1);
        List list = executeQueryPaging(hql.toString(), params.toArray(), paging);
        if (!list.isEmpty()) {
            store = (Store) list.get(0);
        }
        return store;
    }

    @Override
    public List<Store> getStoresByStoreManagers(User user, Boolean isActive, StoreType[] storeTypes, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder(" SELECT s FROM Store AS s join s.storeManagerSet AS m WHERE s.company = ? AND m.id in (?)");
        List params = new ArrayList();
        params.add(user.getCompany());
        params.add(user.getUserID());
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (s.abbreviation LIKE ? OR s.description LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        if (isActive != null) {
            hql.append(" AND s.active = ? ");
            params.add(isActive.booleanValue());
        }
        if (storeTypes != null && storeTypes.length > 0) {
            boolean first = true;
            StringBuilder typeIn = new StringBuilder(" AND s.storeType IN ( ");
            for (StoreType st : storeTypes) {
                if (first) {
                    typeIn.append(st.ordinal());
                    first = false;
                } else {
                    typeIn.append(",").append(st.ordinal());
                }
            }
            typeIn.append(")");
            hql.append(typeIn);
        }

        hql.append(" ORDER BY s.abbreviation ");
        List list = executeQuery(hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null && paging.isValid()) {
            list = executeQueryPaging(hql.toString(), params.toArray(), paging);
            paging.setTotalRecord(totalCount);
        }
        return list;
    }

    @Override
    public List<Store> getStoresByStoreExecutives(User user, Boolean isActive, StoreType[] storeTypes, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder(" SELECT s FROM Store AS s join s.storeExecutiveSet AS m WHERE s.company = ? AND m.id = ? ");
        List params = new ArrayList();
        params.add(user.getCompany());
        params.add(user.getUserID());
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (s.abbreviation LIKE ? OR s.description LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        if (isActive != null) {
            hql.append(" AND s.active = ? ");
            params.add(isActive.booleanValue());
        }
        if (storeTypes != null && storeTypes.length > 0) {
            boolean first = true;
            StringBuilder typeIn = new StringBuilder(" AND s.storeType IN ( ");
            for (StoreType st : storeTypes) {
                if (first) {
                    typeIn.append(st.ordinal());
                    first = false;
                } else {
                    typeIn.append(",").append(st.ordinal());
                }
            }
            typeIn.append(")");
            hql.append(typeIn);
        }

        hql.append(" ORDER BY s.abbreviation ");
        List list = executeQuery(hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null && paging.isValid()) {
            list = executeQueryPaging(hql.toString(), params.toArray(), paging);
            paging.setTotalRecord(totalCount);
        }
        return list;
    }

    @Override
    public List<InventoryWarehouse> getStoresByQAPerson(User user) throws ServiceException {
        StringBuilder hql = new StringBuilder(" SELECT crar.inventoryWarehouse FROM ConsignmentRequestApprovalRule  crar , ConsignmentRequestApproverMapping cram  WHERE crar.ID=cram.consignmentRequestRule.ID AND cram.approver.userID= ? AND crar.company.companyID = ? ");
        List params = new ArrayList();
        params.add(user.getUserID());
        params.add(user.getCompany().getCompanyID());
        List list = executeQuery(hql.toString(), params.toArray());
        return list;
    }

    @Override
    public Store getOtherStoreByAbbreviation(Store store) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM Store WHERE company = ? AND abbreviation = ? AND id <> ? ");
        List params = new ArrayList();
        params.add(store.getCompany());
        params.add(store.getAbbreviation());
        params.add(store.getId());
        List list = executeQueryPaging(hql.toString(), params.toArray(), new Paging(0, 1));
        Store otherStore = null;
        if (!list.isEmpty()) {
            otherStore = (Store) list.get(0);
        }
        return otherStore;
    }

    @Override
    public List<Store> getStoresByStoreExecutivesAndManagers(User user, Boolean isActive, StoreType[] storeTypes, String searchString, Paging paging, boolean includeQAAndRepairStore,boolean includePickandPackStore) throws ServiceException {
        StringBuilder hql = new StringBuilder(" SELECT s FROM Store AS s "
                + "left join s.storeExecutiveSet AS e"
                + " left join s.storeManagerSet m "
                + "WHERE s.company = ? AND (m.userID = ? OR e.userID=?) ");

        List params = new ArrayList();
        params.add(user.getCompany());
        params.add(user.getUserID());
        params.add(user.getUserID());
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (s.abbreviation LIKE ? OR s.description LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        if (isActive != null) {
            hql.append(" AND s.active = ? ");
            params.add(isActive.booleanValue());
        }
        if (storeTypes != null && storeTypes.length > 0) {
            boolean first = true;
            StringBuilder typeIn = new StringBuilder(" AND s.storeType IN ( ");
            for (StoreType st : storeTypes) {
                if (first) {
                    typeIn.append(st.ordinal());
                    first = false;
                } else {
                    typeIn.append(",").append(st.ordinal());
                }
            }
            typeIn.append(")");
            hql.append(typeIn);
        }
        if (!includeQAAndRepairStore) {
            StringBuilder storeIds = getQAAndRepairStoreId(user.getCompany());
            if (!StringUtil.isNullOrEmpty(storeIds.toString())) {
                hql.append(" AND s.id NOT IN ( ").append(storeIds.toString()).append(") ");
            }
        }
        if (!includePickandPackStore) {
            StringBuilder storeIds = getpackStoreId(user.getCompany());
            if (!StringUtil.isNullOrEmpty(storeIds.toString())) {
                hql.append(" AND s.id NOT IN ( ").append(storeIds.toString()).append(") ");
            }
        }
        hql.append(" Group BY s ");
        hql.append(" ORDER BY s.abbreviation ");
        List list = executeQuery(hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null && paging.isValid()) {
            list = executeQueryPaging(hql.toString(), params.toArray(), paging);
            paging.setTotalRecord(totalCount);
        }
        return list;
    }
    
    @Override
    public List<InventoryWarehouse> getWarehouseByStoreExecutivesAndManagers(User user, Boolean isActive, Set<Store> storeTypes) throws ServiceException {
        StringBuilder hql = new StringBuilder(" SELECT s FROM InventoryWarehouse AS s "
                + "WHERE s.company = ?  ");
        List params = new ArrayList();
        params.add(user.getCompany());
        if (storeTypes != null && storeTypes.size() > 0) {
            boolean first = true;
            StringBuilder typeIn = new StringBuilder(" AND s.id IN ( ");
            for (Store st : storeTypes) {
                if (first) {
//                    typeIn.append(st.getId());
                    typeIn.append("'").append(st.getId()).append("'");
                    first = false;
                } else {
//                    typeIn.append(",").append(st.getId());
                     typeIn.append(",'").append(st.getId()).append("'");
                }
            }
            typeIn.append(")");
            hql.append(typeIn);
        }
        List list = executeQuery(hql.toString(), params.toArray());
        return list;
    }

    @Override
    public String getMovementTypeName(String movementTypeId) throws ServiceException {
        StringBuilder hql = new StringBuilder("SELECT value FROM masteritem WHERE id = ? ");
        List params = new ArrayList();
        params.add(movementTypeId);
        List list = executeSQLQuery(hql.toString(), params.toArray());
        String movementTypeName = "";
        if (!list.isEmpty()) {
            movementTypeName = (String) list.get(0);
        }
        return movementTypeName;
    }

    @Override
    public String getUnitNameFromWarehouseid(String warehouseid) throws ServiceException {
        StringBuilder hql = new StringBuilder("SELECT unitname FROM excisedetailstemplatemap WHERE warehouseid = ? ");
        List params = new ArrayList();
        params.add(warehouseid);
        List list = executeSQLQuery(hql.toString(), params.toArray());
        String unitName = "";
        if (!list.isEmpty()) {
            unitName = (String) list.get(0);
        }
        return unitName;
    }
    public KwlReturnObject getInventoryProductDetails(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            ArrayList params = new ArrayList();
            String mysqlQuery = "";
            int moduleid = 0;
            String transactiono = (String) requestParams.get("transactionno");
            String companyid = (String) requestParams.get(Constants.companyid);

            if (requestParams.containsKey(Constants.moduleid)) {
                moduleid = (Integer) requestParams.get(Constants.moduleid);
            }

            if (moduleid == Constants.Acc_Stock_Adjustment_ModuleId) {//For Stock Adjustment
                mysqlQuery = "select product,uom,costcenter,packaging,quantity,reason,remark,amount,adjustment_type,id from in_stockadjustment where seqno=? and company=?";
            } else if (moduleid == Constants.Acc_InterStore_ModuleId || moduleid == Constants.Acc_InterLocation_ModuleId) {
                mysqlQuery = "select product,uom,costCenter,packaging,orderedqty,acceptedqty,remark,id from in_interstoretransfer where transactionno=? and company=?";
            } else if (moduleid == Constants.Acc_Stock_Request_ModuleId || moduleid == Constants.Inventory_ModuleId) {//For Stock Request,Stock Issue
                mysqlQuery = "select product,uom,costCenter,packaging,orderedqty,projectno,remark,id from in_goodsrequest where transactionno=? and company=?";
            }

            params.add(transactiono);
            params.add(companyid);
            List list = executeSQLQuery(mysqlQuery, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "StoreDAOImpl.getProductDetails:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    @Override
    public KwlReturnObject getInventoryBatchDetailsid(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {

            String Stockrequestbillid = (String) requestParams.get("stockrequestbillid");
            ArrayList params = new ArrayList();
            String mysqlQuery = "select id from in_sr_detail where stockRequest=?";
            params.add(Stockrequestbillid);
            List list = executeSQLQuery(mysqlQuery, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "StoreDAOImpl.getInventoryBatchDetailsid:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    @Override
    public StoreMaster getStoreMaster(String storeMasterId) throws ServiceException {
        return (StoreMaster) get(StoreMaster.class, storeMasterId);
    }

    @Override
    public StoreMaster getStoreMasterByName(String name, String company, int type) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM StoreMaster WHERE name=? and company.companyID = ? AND type = ? ");
        List params = new ArrayList();
        params.add(name);
        params.add(company);
        params.add(type);
        List list = executeQueryPaging(hql.toString(), params.toArray(), new Paging(0, 1));
        StoreMaster storeMaster = null;
        if (!list.isEmpty()) {
            storeMaster = (StoreMaster) list.get(0);
        }
        return storeMaster;
    }
//      @Override

    @Override
    public List<UserLogin> saveStoreManagerMapping(String companyId, String[] userId) throws ServiceException {

        List list = null;
        if (userId.length > 0) {
            StringBuilder hql = new StringBuilder("FROM UserLogin WHERE user.company.companyID = ?  ");
            List params = new ArrayList();
//            params.add(userId);
            params.add(companyId);
            
            if (userId.length > 0) {
                boolean first = true;
                StringBuilder typeIn = new StringBuilder(" AND userName IN ( ");
                for (String st : userId) {
                    if (first) {
                        typeIn.append("'").append(st).append("'");
                        first = false;
                    } else {
                        typeIn.append(",'").append(st).append("'");
                    }
                }
                typeIn.append(")");
                hql.append(typeIn);
            }

            list = executeQuery(hql.toString(), params.toArray());

        }
        return list;
    }
    @Override
    public List<Store> getStoreMapping(String companyId, String[] storeiDs) throws ServiceException {

        List list = null;
        if (storeiDs.length > 0) {
            StringBuilder hql = new StringBuilder("FROM Store WHERE company.companyID = ? ");
            List params = new ArrayList();
            params.add(companyId);
            
            if (storeiDs.length > 0) {
                boolean first = true;
                StringBuilder typeIn = new StringBuilder(" AND abbreviation IN ( ");
                for (String st : storeiDs) {
                    if (first) {
                        typeIn.append("'"+st+"'");
                        first = false;
                    } else {
                        typeIn.append(",").append("'"+st+"'");
                    }
                }
                typeIn.append(")");
                hql.append(typeIn);
            }

            list = executeQuery(hql.toString(), params.toArray());

        }
        return list;
    }
    @Override
    public List<Store> getStoresByUser(String userId, Boolean isActive, StoreType[] storeTypes, boolean excludeQARepair, String searchString, Paging paging,boolean includePickandPackStore) throws ServiceException{
        
        StringBuilder hql = new StringBuilder("SELECT s FROM Store s INNER JOIN s.storeManagerSet sm INNER JOIN s.storeExecutiveSet se ");
        if(excludeQARepair || !includePickandPackStore){
            hql.append(", ExtraCompanyPreferences ecp ");
        }
        Map params = new HashMap();
        hql.append(" WHERE (sm.userID = :uid OR se.userID = :uid) ");
        params.put("uid", userId);
        if (isActive != null) {
            hql.append(" AND s.active = :act ");
            params.put("act", isActive.booleanValue());
        }
        if(storeTypes != null && storeTypes.length > 0){
            hql.append(" AND s.storeType IN (:stypes)");
            params.put("stypes", Arrays.asList(storeTypes));
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (s.abbreviation LIKE :ss OR s.description LIKE :ss ) ");
            params.put("ss","%" + searchString + "%");
        }
        if (excludeQARepair) {
            hql.append(" AND ecp.id = s.company ");
            hql.append(" AND (ecp.activateQAApprovalFlow = false OR (ecp.activateQAApprovalFlow = true ");
            hql.append(" AND s.id NOT IN (ecp.inspectionStore, ecp.repairStore )) )");
        }
        if (!includePickandPackStore) {
            hql.append(" AND ecp.id = s.company ");
//            hql.append(" AND ecp.pickpackship = true ");
            hql.append(" AND s.id NOT IN (case when ecp.packingstore is null then '' else ecp.packingstore end )");
        }
//        if (!includePickandPackStore) {
//            StringBuilder storeIds = getpackStoreId(user.getCompany());
//            if (!StringUtil.isNullOrEmpty(storeIds.toString())) {
//                hql.append(" AND s.id NOT IN ( ").append(storeIds.toString()).append(") ");
//            }
//        }
        hql.append(" GROUP BY s");
        
        List list;
        if (paging != null){ 
            int totalCount = 0;
            if (paging.isValid()) {
                String countQry = "SELECT 1 " + hql.toString().substring(hql.toString().indexOf("FROM"));
                list = executeCollectionQuery(countQry, params);
                totalCount = list.size();
                
                hql.append(" ORDER BY s.abbreviation ");
                list = executeCollectionQuery(hql.toString(), params, paging);
            }else{
                hql.append(" ORDER BY s.abbreviation ");
                list = executeCollectionQuery(hql.toString(), params);
                totalCount = list.size();
            }
            paging.setTotalRecord(totalCount);
        }else{
            hql.append(" ORDER BY s.abbreviation ");
            list = executeCollectionQuery(hql.toString(), params);
        }
        return list;
    }
    
    /**
     * Method used to get QA and Repair Store IDs.
     *
     * @param company
     * @return StringBuilder
     * @throws ServiceException
     */
    @Override
    public StringBuilder getQAAndRepairStoreId(Company company) throws ServiceException {
        StringBuilder storeIds = new StringBuilder();
        String hql = "select ecp.inspectionStore as inspectionStore, ecp.repairStore as repairStore from "
                + "ExtraCompanyPreferences ecp, CompanyAccountPreferences comp "
                + "where ecp.id = ? and comp.ID  = ? AND (comp.qaApprovalFlowInDO = ? or comp.qaApprovalFlow = ?)";
        List params = new ArrayList();
        params.add(company.getCompanyID());
        params.add(company.getCompanyID());
        params.add(true);
        params.add(true);
        List list = executeQuery(hql, params.toArray());
        if (list != null && !list.isEmpty()) {
            Object[] obj = (Object[]) list.get(0);
            if (obj != null && obj.length > 0) {
                storeIds.append("'").append((String) obj[0]).append("','").append((String) obj[1]).append("'");
            }
        }
        return storeIds;
    }
    
     /**
     * Method used to get pacK Store ID.
     *
     * @param company
     * @return StringBuilder
     * @throws ServiceException
     */
    public StringBuilder getpackStoreId(Company company) throws ServiceException {
        StringBuilder storeIds = new StringBuilder();
        String hql1 ="select ecp.packingstore as packingstore from ExtraCompanyPreferences ecp "
                +" where ecp.id = ? ";
        List params = new ArrayList();
        String Id;
        params.add(company.getCompanyID());
        List list = executeQuery(hql1, params.toArray());
        if (list != null && !list.isEmpty()) {
                Id=(String)list.get(0);
                if(Id!=null)
                {
                    storeIds.append("'").append(Id).append("'");
                }
            
        }
        return storeIds;
    }
    public double getProductQuantityUnderParticularStore(String storeID, String company) throws ServiceException {
        double quantity = 0;
        ArrayList params = new ArrayList();

        String mysqlQuery = "SELECT SUM(quantity) as qty FROM in_stock WHERE store=? AND company=?";
        params.add(storeID);
        params.add(company);
        List list = executeSQLQuery(mysqlQuery, params.toArray());
        int totalCount = list.size();
        if (list != null && !list.isEmpty()) {
            quantity =list.get(0)!=null? (Double) list.get(0):0;
}
        return authHandler.roundQuantity(quantity,company);
    }
    
    //    ERP-41259 : Don't allow user is deactivate the warehouse if any of its related transaction is in Pending status
    public double getQuantityPendingUnderParticularStore(String storeID, String companyID) throws ServiceException {
        
        double quantity = 0;
        List params = new ArrayList();
        List returnList = new ArrayList();
        
        String consqry = "";
        String stockOut = "";
        String stockRequest = "";
        String stockTransfer = "";
        String goodsReceipt = "";
        String goodsReceiptRepair = "";
        String workOrder = "";
        String workOrderRepair = "";
        String deliveryOrder = "";
        String deliveryOrderRepair = "";
        String salesorderpending="";
        String purchaseorderpending="";
        String allquery = "";
        
        if(!StringUtil.isNullOrEmpty(storeID) && !StringUtil.isNullOrEmpty(companyID))
        {
            consqry=" SELECT p.productid,p.name,c.transactionno AS transactionno ,c.returnquantity AS qty,"
                    + "'consignment' AS module,str.description AS storename, p.description from in_consignment c "
                    + "INNER JOIN in_consignmentdetails cdtl ON c.id=cdtl.consignment  "
                    + "LEFT JOIN product p   ON c.product= p.id  AND  c.company=p.company  "
                    + "LEFT JOIN in_storemaster str ON str.id=c.store  "
                    + "LEFT JOIN uom u ON u.id=c.uom AND u.company=c.company  "
                    + "WHERE c.company=?  AND (c.approval_status='0' OR cdtl.repair_status=4 OR cdtl.repair_status=7 )AND c.store =? ";
            params.add(companyID);
            params.add(storeID);
            
            stockOut="SELECT p.productid,p.name,sa.seqno AS transactionno,sap.quantity AS qty,"
                    + "'stockadjustment' AS module,str.description AS storename,p.description from in_sa_approval sap "
                    + "INNER JOIN in_sa_detail_approval sadtl ON sap.id=sadtl.sa_approval "
                    + "INNER JOIN in_stockadjustment sa   ON sap.stock_adjustment= sa.id  "
                    + "LEFT JOIN product p   ON sa.product= p.id  AND  sa.company=p.company   "
                    + "LEFT JOIN in_storemaster str ON str.id=sa.store  "
                    + "LEFT JOIN uom u ON u.id=sa.uom AND u.company=sa.company "
                    + "WHERE sa.company=?  AND (sap.approval_status='0' OR sadtl.repair_status=4 OR sadtl.repair_status=7 )AND sa.store=?";
            params.add(companyID);
            params.add(storeID);
            
            stockRequest="SELECT p.productid,p.name,gr.transactionno AS transactionno,stkapr.quantity AS qty,"
                    + "'stockrequest' AS module,str.description AS storename,p.description from in_stocktransfer_approval stkapr "
                    + "INNER JOIN in_stocktransfer_detail_approval stkdtl ON stkapr.id=stkdtl.stocktransfer_approval  "
                    + "INNER JOIN in_goodsrequest gr ON gr.id=stkapr.stocktransferid  "
                    + "LEFT JOIN product p  ON gr.product= p.id AND  gr.company= p.company "
                    + "LEFT JOIN in_storemaster str ON str.id=gr.fromstore  "
                    + "LEFT JOIN uom u ON u.id=gr.uom AND u.company=gr.company   "
                    + "WHERE gr.company=? AND stkapr.transaction_module=0   AND (stkapr.approval_status ='0' OR stkdtl.repair_status=4 OR stkdtl.repair_status=7 ) AND gr.tostore =?";
            params.add(companyID);
            params.add(storeID);
            
            stockTransfer="SELECT p.productid,p.name,gr.transactionno AS transactionno,stkapr.quantity AS qty,"
                    + "'stocktransfer'  AS module,str.description AS storename,p.description from in_stocktransfer_approval stkapr "
                    + "INNER JOIN in_stocktransfer_detail_approval stkdtl ON stkapr.id=stkdtl.stocktransfer_approval  "
                    + "INNER JOIN in_interstoretransfer gr ON gr.id=stkapr.stocktransferid  "
                    + "LEFT JOIN product p   ON gr.product= p.id AND  gr.company=p.company  "
                    + "LEFT JOIN in_storemaster str ON str.id=gr.tostore  "
                    + "LEFT JOIN uom u ON u.id=gr.uom AND u.company=gr.company  "
                    + "WHERE gr.company=?  AND (stkapr.approval_status ='0' OR stkdtl.repair_status=4 OR stkdtl.repair_status=7 ) AND gr.fromstore=? ";
            params.add(companyID);
            params.add(storeID);
            
            goodsReceipt="select p.productid,p.name,gro.gronumber AS transactionno, if(gro.approvestatuslevel = 11,grodistmapping.quantitydue , grodistmapping.actualquantity)  as qty,"
                    + "'goodsreceipt' as module, str.description AS storename,p.description from grodetails grod "
                    + "INNER JOIN grorder gro on gro.id = grod.grorder "
                    + "INNER JOIN grodetailistmapping grodistmapping on grod.id = grodistmapping.grodetail "
                    + "INNER JOIN product p on grod.product = p.id "
                    + "INNER JOIN inventory inv on inv.id = grod.id "
                    + "INNER JOIN uom u on u.id = inv.uom "
                    + "LEFT JOIN locationbatchdocumentmapping lbdm on grod.id=lbdm.documentid "
                    + "INNER JOIN newproductbatch npb on lbdm.batchmapid=npb.id "
                    + "INNER JOIN in_interstoretransfer inst on inst. fromstore = npb.warehouse and grodistmapping.istrequest = inst.id "
                    + "INNER JOIN in_storemaster str on str.id = npb.warehouse "
                    + "where grod.company = ?  and if(gro.approvestatuslevel = 11,grodistmapping.quantitydue , grodistmapping.actualquantity) > 0  AND npb.warehouse = ? ";
            params.add(companyID);
            params.add(storeID);
            
            goodsReceiptRepair=" select grod.product as productid,p.name as name, gro.gronumber AS transactionno,rm.rejectedquantity as qty ,"
                    + "'goodsreceipt' as module,'' as storename, p.description as description from repairgrodistmapping rm "
                    + "INNER JOIN grodetailistmapping grodistmapping on grodistmapping.id = rm.grodistmapping "
                    + "INNER JOIN grodetails grod on grodistmapping.grodetail = grod.id "
                    + "INNER JOIN product p on grod.product = p.id "
                    + "INNER JOIN grorder gro on gro.id = grod.grorder "
                    + "LEFT JOIN locationbatchdocumentmapping lbdm on grod.id=lbdm.documentid "
                    + "INNER JOIN newproductbatch npb on lbdm.batchmapid=npb.id "
                    + "where gro.company = ? AND npb.warehouse =?  AND rm.rejectedquantitydue <> 0";
            params.add(companyID);
            params.add(storeID);
            
            workOrder=" select p.productid,p.name,wo.workorderid AS transactionno,wocdistmapping.quantitydue as qty,"
                    + "'Work Order' as module, str.description AS storename,p.description from workordercomponentdetail wocd "
                    + "INNER JOIN workorder wo on wo.id = wocd.workorder "
                    + "INNER JOIN wocdetailistmapping wocdistmapping on wocd.id = wocdistmapping.wocdetail "
                    + "INNER JOIN product p on wocd.product = p.id "
                    + "INNER JOIN uom u on u.id = p.unitOfMeasure "
                    + "LEFT JOIN locationbatchdocumentmapping lbdm on wocd.id=lbdm.documentid "
                    + "INNER JOIN newproductbatch npb on lbdm.batchmapid=npb.id "
                    + "INNER JOIN in_interstoretransfer inst on inst. fromstore = npb.warehouse and wocdistmapping.istrequest = inst.id "
                    + "INNER JOIN in_storemaster str on str.id = npb.warehouse "
                    + "where wo.company = ?  and wocdistmapping.quantitydue > 0  AND npb.warehouse =? ";
            params.add(companyID);
            params.add(storeID);
            
            workOrderRepair=" select  wocd.product as productid,p.name as name, wo.workorderid AS transactionno,rwocdm.rejectedquantity as qty ,"
                    + "'Work Order' as module,'' as storename, p.description as description from repairwocdistmapping rwocdm "
                    + "inner join wocdetailistmapping wocdistmapping on wocdistmapping.id = rwocdm.wocdistmapping "
                    + "inner join workordercomponentdetail wocd on wocdistmapping.wocdetail = wocd.id "
                    + "inner join product p on wocd.product = p.id "
                    + "inner join workorder wo on wo.id = wocd.workorder "
                    + "left join locationbatchdocumentmapping lbdm on wocd.id=lbdm.documentid "
                    + "inner join newproductbatch npb on lbdm.batchmapid=npb.id "
                    + "where wo.company = ? AND npb.warehouse =? AND rwocdm.rejectedquantitydue <> 0";
            params.add(companyID);
            params.add(storeID);
            
            deliveryOrder="select p.productid,p.name,do.donumber AS transactionno,dodistmapping.quantitydue as qty,"
                    + "'deliveryorder' as module, str.description AS storename,p.description from dodqcistmapping dodistmapping "
                    + "INNER JOIN dodetails dod on  dod.id = dodistmapping.dodetailid "
                    + "INNER JOIN deliveryorder do on do.id = dod.deliveryorder "
                    + "INNER JOIN in_interstoretransfer inst on dodistmapping.qcistrequest = inst.id "
                    + "INNER JOIN product p on inst.product = p.id "
                    + "INNER JOIN uom u on u.id = inst.uom "
                    + "INNER JOIN in_storemaster str on str.id = inst.fromstore "
                    + "where do.company = ?  and dodistmapping.quantitydue > 0  AND str.id= ? ";
            params.add(companyID);
            params.add(storeID);
            
            deliveryOrderRepair=" select p.productid as productid, p.name as name, do.donumber AS transactionno,rm.quantitydue as qty,"
                    + "'deliveryorder' as module, str.description as storename ,p.description as description from rejectdodistmapping rm "
                    + "inner join dodqcistmapping dodm on dodm.id = rm.dodqcistmapping "
                    + "inner join dodetails dod on dodm.dodetailid = dod.id "
                    + "inner join product p on dod.product = p.id "
                    + "inner join deliveryorder do on do.id = dod.deliveryorder "
                    + "inner join in_interstoretransfer inst on dodm.qcistrequest = inst.id "
                    + "inner join uom u on u.id = inst.uom "
                    + "inner join in_storemaster str on str.id = inst.fromstore "
                    + "where do.company = ? AND str.id =? AND rm.quantitydue <> 0";
            params.add(companyID);
            params.add(storeID);
            
            salesorderpending ="select p.productid,p.name,so.sonumber AS transactionno,sod.quantity as qty," +
                    "'salesorder' as module, str.description AS storename, p.description from salesorder so " +
                    "INNER JOIN sodetails sod on sod.salesorder = so.id " +
                    "INNER JOIN  locationbatchdocumentmapping lbdm on sod.id = lbdm.documentid " +
                    "INNER JOIN newproductbatch npb on npb.id = lbdm.batchmapid " +
                    "INNER JOIN product p on p.id = sod.product " +
                    "INNER JOIN in_storemaster str on str.id = npb.warehouse " +
                    "INNER JOIN uom u on u.id = sod.uom " +
                    "where so.company =? and  npb.warehouse =? and so.approvestatuslevel <> 11 ";
            params.add(companyID);
            params.add(storeID);
            
            purchaseorderpending = "select p.productid,p.name,po.ponumber AS transactionno,pod.quantity as qty," +
                    "'purchaseorder' as module, str.description AS storename, p.description from purchaseorder po " +
                    "INNER JOIN podetails pod on pod.purchaseorder = po.id " +
                    "INNER JOIN  locationbatchdocumentmapping lbdm on pod.id = lbdm.documentid " +
                    "INNER JOIN newproductbatch npb on npb.id = lbdm.batchmapid " +
                    "INNER JOIN product p on p.id = pod.product " +
                    "INNER JOIN in_storemaster str on str.id = npb.warehouse " +
                    "INNER JOIN uom u on u.id = pod.uom " +
                    "where po.company =? and  npb.warehouse =? and po.approvestatuslevel <> 11";
            params.add(companyID);
            params.add(storeID);
            
            allquery= consqry +" UNION "+ stockOut +" UNION "+ stockRequest +" UNION "+ stockTransfer +" UNION "+ goodsReceipt +
                    " UNION "+ goodsReceiptRepair +" UNION "+ workOrder +" UNION "+ workOrderRepair +" UNION "+ deliveryOrder +
                    " UNION "+ deliveryOrderRepair +" UNION " + salesorderpending +" UNION " + purchaseorderpending;
            
        }
        
        if(!StringUtil.isNullOrEmpty(allquery)){
            
            String selectQuery ="SELECT tb.productid,tb.name,tb.transactionno,tb.qty,tb.module,tb.storename,tb.description FROM ( " 
                    + allquery + " ) as tb ORDER BY tb.transactionno DESC ";
            returnList = executeSQLQuery( selectQuery, params.toArray());
        
        }
         if (returnList != null && !returnList.isEmpty()) {
             
            for(Object records : returnList){
                Object[] obj = (Object[]) records;
                quantity += obj[3]!=null? (Double) obj[3]:0;
            }
        }
        return authHandler.roundQuantity(quantity,companyID);
    }

    /**
     * Get the stockmovement ids for a particular store by passing its id the function refers in_stockmovement table
     * to check if a store has been used in any transactions
     * @param storeid
     * @param companyid
     * @return List
     * @throws ServiceException
     */
    @Override
    public List<Object> getTransactionCountForStoreId(String storeid, String companyid,boolean isForStockRequest) throws ServiceException {
        List<Object> resultlist = new ArrayList<>();
        List params = new ArrayList();
        params.add(storeid);
        params.add(companyid);
        String query = "select id from in_stockmovement where store=? and company=?";
        if(isForStockRequest){
            query="select id from in_goodsrequest where fromstore=? and company=?";
        }
        resultlist = executeSQLQuery(query, params.toArray());
        return resultlist;
    }
    
    /**
     * Get the stockmovement ids for a particular store by passing its id the function refers in_stockmovement table
     * to check if a store has been used in any transactions
     * @param storeid
     * @param companyid
     * @return List
     * @throws ServiceException
     */
    @Override
     public List<Object> getProductCountForStoreId(String storeid, String companyid) throws ServiceException {
        List<Object> resultlist = new ArrayList<>();
        List params = new ArrayList();
        params.add(storeid);
        params.add(companyid);
        String query = "select id from product where warehouse=? and company=?";
        resultlist = executeSQLQuery(query, params.toArray());
        return resultlist;
}
}
