/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.cyclecount.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.Producttype;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.cyclecount.CycleCount;
import com.krawler.inventory.model.cyclecount.CycleCountCalendar;
import com.krawler.inventory.model.cyclecount.CycleCountDAO;
import com.krawler.inventory.model.cyclecount.CycleCountStatus;
import com.krawler.inventory.model.frequency.Frequency;
import com.krawler.inventory.model.ist.InterStoreTransferStatus;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockrequest.RequestStatus;
import com.krawler.inventory.model.store.Store;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.Serializable;
import java.text.ParseException;
import java.util.*;

/**
 *
 * @author Vipin Gupta
 */
public class CycleCountDAOImpl extends BaseDAO implements CycleCountDAO {

    @Override
    public Object getObject(Class entityClass, Serializable id) {
        return get(entityClass, id);
    }

    @Override
    public List<CycleCountCalendar> getCycleCountCalendarForMonth(Company company, Date fromDate, Date toDate) throws ServiceException {
        StringBuilder hql = new StringBuilder(" FROM CycleCountCalendar WHERE company = ? AND DATE(date)>= ? AND DATE(date)<= ? ");
        List params = new ArrayList();
        params.add(company);
        params.add(fromDate);
        params.add(toDate);
        hql.append(" ORDER BY date ");

        List list = executeQuery(hql.toString(), params.toArray());

        return list;
    }

    @Override
    public void saveOrUpdate(Object object) throws ServiceException {
        super.saveOrUpdate(object);
    }

    @Override
    public List<Frequency> getAllFrequencies() throws ServiceException {
        StringBuilder hql = new StringBuilder(" FROM Frequency ORDER BY id  ");
        return executeQuery(hql.toString());
    }

    @Override
    public CycleCountCalendar getCycleCountCalendar(Company company, Date date) throws ServiceException {
        CycleCountCalendar ccl = null;
        StringBuilder hql = new StringBuilder(" FROM CycleCountCalendar WHERE company = ? AND DATE(date) = ? ");
        List params = new ArrayList();
        params.add(company);
        params.add(date);

        Paging paging = new Paging(0, 1);
        List<CycleCountCalendar> list = executeQueryPaging(hql.toString(), params.toArray(), paging);
        if (!list.isEmpty()) {
            ccl = (CycleCountCalendar) list.get(0);
        }
        return ccl;
    }

    @Override
    public List<CycleCount> getCycleCountReport(Store store, Date businessDate, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM CycleCount WHERE store = ? AND businessDate = ? AND status = ? ");
        List params = new ArrayList();
        params.add(store);
        params.add(businessDate);
        params.add(CycleCountStatus.DONE);

        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (product.name LIKE ? OR product.productid LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }

        hql.append(" ORDER BY businessDate DESC ");
        List list = executeQuery(hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging(hql.toString(), params.toArray(), paging);
        }
        return list;
    }

    @Override
    public List<CycleCount> getCycleCountReport(Company company, Store store, Date fromDate, Date toDate, String searchString, Paging paging,Map<String, Object> requestParams) throws ServiceException, JSONException, ParseException {
        StringBuilder hql = new StringBuilder("FROM CycleCount WHERE company = ? AND businessDate >= ? AND businessDate <= ? AND status = ?");
        List params = new ArrayList();
        params.add(company);
        params.add(fromDate);
        params.add(toDate);
        params.add(CycleCountStatus.DONE);
        if (store != null) {
            hql.append(" AND store = ? ");
            params.add(store);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (product.name LIKE ? OR product.productid LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        /*
         Advance Search Component
         */
        String appendCase = "and";
        String mySearchFilterString = "";
        String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
        if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
            if (requestParams.get("filterConjuctionCriteria").toString().trim().equalsIgnoreCase("OR")) {
                filterConjuctionCriteria = com.krawler.common.util.Constants.or;
            }
        }
        String Searchjson = "";
        String searchDefaultFieldSQL = "";
        if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
            Searchjson = requestParams.get("searchJson").toString();
            if (!StringUtil.isNullOrEmpty(Searchjson)) {
                JSONObject serachJobj = new JSONObject(Searchjson);
                JSONArray customSearchFieldArray = new JSONArray();
                JSONArray defaultSearchFieldArray = new JSONArray();
                StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);

                if (customSearchFieldArray.length() > 0) {
                    /*
                     Advance Search For Custom fields
                     */
                    requestParams.put(Constants.Searchjson, Searchjson);
                    requestParams.put(Constants.moduleid, Constants.Acc_CycleCount_ModuleId);
                    requestParams.put(Constants.appendCase, appendCase);
                    requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                    mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(requestParams, false).get(Constants.myResult));
                    if (mySearchFilterString.contains("c.AccJEDetailCustomData")) {
                        mySearchFilterString = mySearchFilterString.replaceAll("c.AccJEDetailCustomData", "cycleCountCustomData");
                    }
                    StringUtil.insertParamAdvanceSearchString1((ArrayList) params, Searchjson);
                }
                mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
            }
        }
        hql.append(mySearchFilterString);
        hql.append(" ORDER BY businessDate DESC, store.abbreviation ASC, product.productid ASC ");
        List list = executeQuery(hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging(hql.toString(), params.toArray(), paging);
        }
        return list;
    }

    @Override
    public List<Object[]> getCCCalendarProducts(Company company, Date date) {
        String hql = "SELECT DISTINCT p, cu.name, iu.name, su.name, "
                + " p.packaging.casingUomValue, p.packaging.innerUomValue, p.packaging.stockUomValue  FROM Product p  "
                + " LEFT JOIN p.packaging.casingUoM cu "
                + " LEFT JOIN p.packaging.innerUoM iu "
                + " LEFT JOIN p.packaging.stockUoM su "
                + " INNER JOIN p.cycleCountFrequencies ccf "
                + " , CycleCountCalendar ccc INNER JOIN ccc.frequencies frq"
                + " WHERE p.deleted = :del AND p.company = :c AND ccc.company = p.company AND ccc.date = :d AND ccf = frq";

        Map params = new HashMap();
        params.put("del", false);
        params.put("c", company);
        params.put("d", date);
        return executeCollectionQuery(hql, params);
    }
    
    @Override
    public List<Object[]> getCCCalendarExtraProducts(Company company, Date date) {
        String hql1 = "SELECT DISTINCT p.ID FROM Product p  "
                + " INNER JOIN p.cycleCountFrequencies ccf "
                + " , CycleCountCalendar ccc INNER JOIN ccc.frequencies frq"
                + " WHERE p.company = :c AND ccc.company = p.company AND ccc.date = :d AND ccf = frq";
        
        String hql = "SELECT DISTINCT p, cu.name, iu.name, su.name, "
                + " p.packaging.casingUomValue, p.packaging.innerUomValue, p.packaging.stockUomValue  FROM Product p  "
                + " LEFT JOIN p.packaging.casingUoM cu "
                + " LEFT JOIN p.packaging.innerUoM iu "
                + " LEFT JOIN p.packaging.stockUoM su "
                + " WHERE p.deleted = :del AND p.company = :c AND p.countable = :countable AND (p.iswarehouseforproduct = :iswh OR p.islocationforproduct = :isloc) AND p.deleted = :isdel AND p.producttype.ID IN (:prodtypes) AND p.ID NOT IN("+hql1+")";
        
        Map params = new HashMap();
        params.put("del", false);
        params.put("c", company);
        params.put("d", date);
        params.put("countable", true);
        params.put("iswh", true);
        params.put("isloc", true);
        params.put("isdel", false);
        List producttypes = new ArrayList();
        producttypes.add(Producttype.INVENTORY_PART);
        producttypes.add(Producttype.ASSEMBLY);
        params.put("prodtypes", producttypes);
        return executeCollectionQuery(hql, params);
    }
    
    @Override
    public List<Object[]> getCycleCountDraftExtraProducts(Company company, Date date) throws ServiceException{
        String hql = "SELECT DISTINCT p, cu.name, iu.name, su.name, "
                + " p.packaging.casingUomValue, p.packaging.innerUomValue, p.packaging.stockUomValue "
                + " FROM CycleCount cc "
                + " INNER JOIN cc.product p  "
                + " LEFT JOIN p.packaging.casingUoM cu "
                + " LEFT JOIN p.packaging.innerUoM iu "
                + " LEFT JOIN p.packaging.stockUoM su "
                + " WHERE p.deleted = :del AND cc.company = :c AND DATE(cc.businessDate) = DATE(:d) AND cc.status = :status AND cc.extraItem = :isextra";

        Map params = new HashMap();
        params.put("del", false);
        params.put("c", company);
        params.put("d", date);
        params.put("status", CycleCountStatus.DRAFT);
        params.put("isextra", true);
        return executeCollectionQuery(hql, params);
    }

    @Override
    public Map<String, Double> getProductsSystemQty(List<String> productList, Store store) {
        Map<String, Double> pSysQty = new HashMap<>();

        String hql = "SELECT s.product.ID, SUM(s.quantity) FROM Stock s"
                + " WHERE s.store = :str AND s.product.ID IN (:plist )"
                + " GROUP BY s.product.ID ";
        Map params = new HashMap();
        params.put("str", store);
        params.put("plist", productList);

        List<Object[]> list = executeCollectionQuery(hql, params);
        for (Object[] objs : list) {
            String p = objs[0] != null ? (String) objs[0] : null;
            double qty = objs[1] != null ? (Double) objs[1] : 0;
            pSysQty.put(p, qty);
        }
        return pSysQty;
    }

    @Override
    public void removeCycleCountForProduct(Product product, Store store, Date businessDate, boolean isDraft) throws ServiceException {
        StringBuilder hql = new StringBuilder("DELETE FROM CycleCount WHERE store = ? AND businessDate = ? AND status = ? ");
        List params = new ArrayList();
        params.add(store);
        params.add(businessDate);
        if (isDraft) {
            params.add(CycleCountStatus.DRAFT);
        } else {
            params.add(CycleCountStatus.DONE);
        }
        if (product != null) {
            hql.append(" AND product = ? ");
            params.add(product);
        }
        executeUpdate(hql.toString(), params.toArray());
    }

    @Override
    public List<Object[]> getAllCycleCountStatusReport(String companyId, Date businessDate, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("SELECT s.abbrev, s.description, COUNT(cc.product) FROM in_storemaster s ");
        hql.append(" LEFT JOIN in_cyclecount cc ON cc.store = s.id AND cc.company = s.company AND cc.businessdate = ? ");
        hql.append(" WHERE  cc.status <> 0 AND s.company = ? ");
        hql.append(" GROUP BY s.id ");
        List params = new ArrayList();
        params.add(businessDate);
        params.add(companyId);
        List list = executeSQLQuery(hql.toString(), params.toArray());
        paging.setTotalRecord(list.size());
        return list;
    }

    @Override
    public List<Object[]> getCycleCountStatusReport(String userId, Date businessDate, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("SELECT s.abbrev, s.description, COUNT(cc.product) FROM in_storemaster s ");
        hql.append(" LEFT JOIN in_cyclecount cc ON cc.store = s.id");
        hql.append(" LEFT JOIN in_store_user su ON s.id = su.storeid ");
        hql.append(" LEFT JOIN in_store_executive se ON s.id = se.storeid ");
        hql.append(" LEFT JOIN extracompanypreferences ecp ON ecp.id = s.company  ");
        hql.append(" WHERE cc.status <> 0 AND (se.userid = ? OR su.userid = ?) AND cc.businessdate = ? ");
        hql.append(" AND s.id NOT IN (ecp.inspectionstore, ecp.repairstore )");
        hql.append(" GROUP BY s.id ");
        List params = new ArrayList();
        params.add(userId);
        params.add(userId);
        params.add(businessDate);
        
        List list;
        if(paging != null){
            int count = 0;
            if(paging.isValid()){
                String countQuery = "SELECT 1 "+hql.toString().substring(hql.toString().indexOf("FROM"));
                list = executeSQLQuery(countQuery, params.toArray());
                count = list.size();
                list = executeSQLQueryPaging(hql.toString(), params.toArray(),paging);
            }else{
                list = executeSQLQuery(hql.toString(), params.toArray());
                count = list.size();
            }
            paging.setTotalRecord(count);
        }else{
            list = executeSQLQuery(hql.toString(), params.toArray());
        }
        return list;
    }

    @Override
    public boolean isCycleCountDone(String storeId, Date businessDate) throws ServiceException {
        StringBuilder hql = new StringBuilder("SELECT 1 FROM CycleCount WHERE store.id = ? AND DATE(businessDate) >= DATE(?) AND status <> ? ");
        List params = new ArrayList();
        params.add(storeId);
        params.add(businessDate);
        params.add(CycleCountStatus.DRAFT);
        List list = executeQuery(hql.toString(), params.toArray());
        boolean done = false;
        if (!list.isEmpty()) {
            done = true;
        }
        return done;
    }

    @Override
    public boolean isCycleCountDoneForProduct(String storeId, Date businessDate, String productId) throws ServiceException {
        StringBuilder hql = new StringBuilder("SELECT 1 FROM CycleCount WHERE status <> ? AND store.id = ? AND DATE(businessDate) >= DATE(?) AND product.id = ? ");
        List params = new ArrayList();
        params.add(CycleCountStatus.DRAFT);
        params.add(storeId);
        params.add(businessDate);
        params.add(productId);
        List list = executeQuery(hql.toString(), params.toArray());
        boolean done = false;
        if (!list.isEmpty()) {
            done = true;
        }
        return done;
    }

    @Override
    public Date getLastCycleCountDate(String storeId) throws ServiceException {
        String hql = "SELECT MAX(businessDate) FROM CycleCount WHERE store.id = ? AND status <> ? GROUP BY store ";
        List list = executeQuery(hql, new Object[]{storeId, CycleCountStatus.DRAFT});
        Date date = null;
        if (!list.isEmpty()) {
            date = list.get(0) != null ? (Date) list.get(0) : null;
        }
        return date;
    }

    @Override
    public List<Object[]> getCycleCountDraftList(String userId, String searchString, Paging paging,String inspectionStore,String repairStore) throws ServiceException {
        StringBuilder hql = new StringBuilder("SELECT cc.store, cc.businessDate FROM CycleCount cc ");
        hql.append(" LEFT JOIN cc.store.storeManagerSet sm ");
        hql.append(" LEFT JOIN cc.store.storeExecutiveSet se ");
        hql.append(" ,ExtraCompanyPreferences ecp ");
        hql.append(" WHERE cc.status = ? AND (se.userID = ? OR sm.userID = ? )");
        hql.append(" AND ecp.id = cc.store.company.companyID ");
        if(!StringUtil.isNullOrEmpty(inspectionStore) && !StringUtil.isNullOrEmpty(repairStore)){
            hql.append(" AND cc.store.id NOT IN (ecp.inspectionStore, ecp.repairStore )");
        }
        List params = new ArrayList();
        params.add(CycleCountStatus.DRAFT);
        params.add(userId);
        params.add(userId);
        if(!StringUtil.isNullOrEmpty(searchString)){
            hql.append(" AND (cc.store.abbreviation LIKE ? OR cc.store.description LIKE ? )");
            params.add("%"+searchString+"%");
            params.add("%"+searchString+"%");
        }
        hql.append(" GROUP BY cc.store, cc.businessDate ");
        
        List list;
        if(paging != null){
            int count = 0;
            if(paging.isValid()){
                String countQuery = "SELECT 1 "+hql.toString().substring(hql.toString().indexOf("FROM"));
                list = executeQuery(countQuery, params.toArray());
                count = list.size();
                hql.append(" ORDER BY cc.store.abbreviation ASC, cc.businessDate ASC");
                list = executeQueryPaging(hql.toString(), params.toArray(),paging);
            }else{
                hql.append(" ORDER BY cc.store.abbreviation ASC, cc.businessDate ASC");
                list = executeQuery(hql.toString(), params.toArray());
                count = list.size();
            }
            paging.setTotalRecord(count);
        }else{
            hql.append(" ORDER BY cc.store.abbreviation ASC, cc.businessDate ASC");
            list = executeQuery(hql.toString(), params.toArray());
        }
        return list;
    }

    @Override
    public List<CycleCount> getCycleCountDraftItemList(String storeId, Date businessDate) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM CycleCount ");
        hql.append(" WHERE status = ? AND store.id = ? AND businessDate = ? ");
        List params = new ArrayList();
        params.add(CycleCountStatus.DRAFT);
        params.add(storeId);
        params.add(businessDate);
        List list = executeQuery(hql.toString(), params.toArray());
        return list;
    }
}
