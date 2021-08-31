/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.stocktransfer;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public interface StockTransferApprovalDAO {

    public List<StockTransferApproval> getStockTransferApprovalList(TransactionModule transactionModule, String searchString, Paging paging) throws ServiceException;

    public List<StockTransferDetailApproval> getStockTransferDetailApprovalList(StockTransferApproval stockTransferApproval, Paging paging) throws ServiceException;

    public void saveOrUpdate(Object object) throws ServiceException;

    public StockTransferDetailApproval getStockTransferDetailApproval(String id);
}
