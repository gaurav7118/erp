/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.threshold.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.threshold.ProductThreshold;
import com.krawler.inventory.model.threshold.ThresholdDAO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vipin Gupta
 */
public class ThresholdDAOImpl extends BaseDAO implements ThresholdDAO {

    @Override
    public void saveOrUpdateProductThreshold(ProductThreshold productThreshold) {
        try {
            super.saveOrUpdate(productThreshold);
        } catch (ServiceException ex) {
            Logger.getLogger(ThresholdDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public ProductThreshold getProductThreshold(String thresholdId) {
        return (ProductThreshold) get(ProductThreshold.class, thresholdId);
    }

    @Override
    public ProductThreshold getProductThreshold(Product product, Store store) throws ServiceException {
        String hql = "FROM ProductThreshold WHERE company = ? AND product = ? AND store = ? ";
        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(product);
        params.add(store);
        List<ProductThreshold> list = executeQuery( hql, params.toArray());
        ProductThreshold pt = null;
        if (!list.isEmpty()) {
            pt = list.get(0);
        }
        return pt;
    }

    @Override
    public List<ProductThreshold> getAllThresholdList(Company company, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("SELECT IF(pt = null, new ProductThreshold(p, s, 0), pt) FROM Product p JOIN Store s ON p.company = s.company LEFT JOIN ProductThreshold pt ON pt.product = p.id AND s.id = pt.store WHERE p.company = ? ");
        List params = new ArrayList();
        params.add(company);
        if (StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (p.productid LIKE ? OR p.name LIKE ? )");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        hql.append(" ORDER BY p.productid ASC, s.abbreviation ASC");
        List<ProductThreshold> list = executeQuery( hql.toString(), params.toArray());
        if (paging != null) {
            paging.setTotalRecord(list.size());
            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
        }
        return list;
    }

    @Override
    public List<ProductThreshold> getStoreWiseThresholdList(Store store, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("SELECT IF(pt = null, new ProductThreshold(p, s, 0), pt) FROM Product p JOIN Store s ON p.company = s.company LEFT JOIN ProductThreshold pt ON pt.product = p.id AND s.id = pt.store WHERE p.company = ? AND s.store = ? ");
        List params = new ArrayList();
        params.add(store.getCompany());
        params.add(store);
        if (StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (p.productid LIKE ? OR p.name LIKE ? )");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        hql.append(" ORDER BY p.productid ASC");
        List<ProductThreshold> list = executeQuery( hql.toString(), params.toArray());
        if (paging != null) {
            paging.setTotalRecord(list.size());
            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
        }
        return list;
    }

    @Override
    public List<ProductThreshold> getProductWiseThresholdList(Product product, String searchString, Paging paging) throws ServiceException {

        StringBuilder sqlqry = new StringBuilder("SELECT s.id AS storeid , IF(pt.threshold_limit IS NULL,0,pt.threshold_limit)  AS threshold_limit FROM  product p INNER JOIN in_storemaster s ON p.company=s.company LEFT JOIN in_product_threshold pt ON p.id=pt.product AND s.id=pt.store WHERE  p.company= ?  AND p.id = ? ");

        List params = new ArrayList();
        params.add(product.getCompany().getCompanyID());
        params.add(product.getID());
        if (!StringUtil.isNullOrEmpty(searchString)) {
            sqlqry.append(" AND ( s.abbrev LIKE ? OR s.description LIKE ? )");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        sqlqry.append(" ORDER BY s.abbrev ");

        List list = executeSQLQuery( sqlqry.toString(), params.toArray());
        if (paging != null) {
            if (paging.isValid() && paging.getLimit() < list.size()) {
                Integer[] limitparams = new Integer[]{paging.getOffset(), paging.getLimit()};
                list = executeSQLQueryPaging( sqlqry.toString(), params.toArray(), limitparams);
            }

        }

        if (list.size() > 0) {
            Iterator it = list.iterator();
            List<ProductThreshold> returnList = new ArrayList<ProductThreshold>();
            while (it.hasNext()) {
                Object[] obj = (Object[]) it.next();

                String storeId = (String) obj[0];
                Store store = (Store) get(Store.class, storeId);

                double thlimit = (Double) obj[1];

                ProductThreshold pt = new ProductThreshold(product, store, thlimit);
                returnList.add(pt);
            }
            list = returnList;
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getThresholdStockList(Company company, Store store, String searchString, Paging paging) throws ServiceException {
        List<Map<String, Object>> thresholdStock = new ArrayList<Map<String, Object>>();
        StringBuilder hql = new StringBuilder("SELECT tb1.pid AS pid, tb1.pcode AS pcode, tb1.pname AS pname, tb1.thlimit AS thlimit, IF(tb2.inhandqty IS NULL , 0, tb2.inhandqty) AS inhandqty  "
                +" FROM (SELECT p.company AS cid, sm.id AS sid, p.id AS pid, p.productid AS pcode, p.name AS pname, IF(pth.id IS NULL, 0, pth.threshold_limit) AS thlimit,p.deleteflag  FROM product p  "
                +" INNER JOIN in_storemaster sm ON p.company = sm.company  AND  sm.id = ? LEFT JOIN in_product_threshold pth ON pth.store = sm.id AND pth.product = p.id AND p.iswarehouseforproduct = 'T'AND p.islocationforproduct = 'T' AND sm.id = ?  ) tb1  "
                +" LEFT JOIN (SELECT product, store, SUM(quantity) AS inhandqty FROM in_stock WHERE store = ? GROUP BY product, store) tb2 ON tb1.sid = tb2.store AND tb1.pid = tb2.product  "
                +" WHERE  tb1.sid = ? AND tb1.deleteflag ='F' AND ((tb1.thlimit > tb2.inhandqty AND tb2.inhandqty IS NOT NULL) OR (tb2.inhandqty IS NULL AND tb1.thlimit > 0) ) ");
        List params = new ArrayList();
        params.add(store.getId());
        params.add(store.getId());
        params.add(store.getId());
        params.add(store.getId());
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (tb1.pcode LIKE ? OR tb1.pname LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        hql.append(" ORDER BY tb1.pcode ");
        List list = executeSQLQuery( hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            if (paging.isValid() && paging.getLimit() < list.size()) {
                hql.append(" LIMIT ?,? ");
                params.add(paging.getOffset());
                params.add(paging.getLimit());
                list = executeSQLQuery( hql.toString(), params.toArray());
            }
        }
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objs = (Object[]) itr.next();
            String pid = (String) objs[0];
            String pcode = (String) objs[1];
            String pname = (String) objs[2];
            Double thlimit = (Double) objs[3];
            Double inHand = (Double) objs[4];

            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("productId", pid);
            dataMap.put("productCode", pcode);
            dataMap.put("productName", pname);
            dataMap.put("thresholdLimit", thlimit);
            dataMap.put("inhandQuantity", inHand);

            thresholdStock.add(dataMap);
        }
        return thresholdStock;
    }
}
