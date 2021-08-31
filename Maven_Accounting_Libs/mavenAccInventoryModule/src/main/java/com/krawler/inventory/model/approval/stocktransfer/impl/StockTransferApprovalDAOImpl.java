/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.stocktransfer.impl;

import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.approval.ApprovalType;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferApproval;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferApprovalDAO;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferDetailApproval;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public class StockTransferApprovalDAOImpl extends BaseDAO implements StockTransferApprovalDAO {

    @Override
    public List<StockTransferApproval> getStockTransferApprovalList(TransactionModule transactionModule, String searchString, Paging paging) throws ServiceException {
//        StringBuilder hql = new StringBuilder("SELECT sta FROM StockTransferApproval sta , InterStoreTransferRequest ist, StockRequest sr WHERE (sta.stockTransferId = ist.id OR sta.stockTransferId = sr.id) ");
        StringBuilder hql = new StringBuilder("SELECT isa.id,isa.approval_status,isa.approval_type,isa.inspector,isa.quantity,isa.stocktransferid,isa.transaction_module FROM in_stocktrasfer_approval isa LEFT JOIN in_interstoretransfer ist ON(isa.stocktransferid = ist.id)  LEFT JOIN in_goodsrequest igr ON (isa.stocktransferid = igr.id)  ");

        List params = new ArrayList();
        if (transactionModule != null) {
            hql.append(" WHERE isa.transaction_module = ? ");
            params.add(transactionModule.ordinal());
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (ist.product.productid LIKE ? OR igr.product.id LIKE ?) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        List list = executeSQLQuery( hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            if (paging.isValid() && totalCount > paging.getLimit()) {
                list = executeQueryPaging( hql.toString(), params.toArray(), paging);
            }
        }
        List<StockTransferApproval> staList = new ArrayList<StockTransferApproval>();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objs = (Object[]) itr.next();
            StockTransferApproval sta = new StockTransferApproval();
            sta.setId((String) objs[0]);
            sta.setApprovalStatus((ApprovalStatus) ApprovalStatus.values()[(Integer) objs[1]]);
            sta.setApprovalType((ApprovalType) ApprovalType.values()[(Integer) objs[2]]);
//            sta.setInspector((User) objs[3]);
            sta.setQuantity((Double) objs[4]);
            sta.setStockTransferId((String) objs[5]);
            sta.setTransactionModule((TransactionModule) TransactionModule.values()[(Integer) objs[6]]);


            staList.add(sta);
        }
        return staList;

    }

    @Override
    public List<StockTransferDetailApproval> getStockTransferDetailApprovalList(StockTransferApproval stockTransferApproval, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM StockTransferDetailApproval WHERE stockTransferApproval = ?");
        List params = new ArrayList();
        params.add(stockTransferApproval);
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
    public void saveOrUpdate(Object object) throws ServiceException {
        save(object);
    }

    @Override
    public StockTransferDetailApproval getStockTransferDetailApproval(String id) {
        return (StockTransferDetailApproval) get(StockTransferDetailApproval.class, id);
    }
}
