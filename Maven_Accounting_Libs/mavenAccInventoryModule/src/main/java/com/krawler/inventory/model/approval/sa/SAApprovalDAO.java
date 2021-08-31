/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.sa;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.inventory.model.stockout.StockAdjustment;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public interface SAApprovalDAO {
    
    public SAApproval getSAApproval(String saApprovalId) throws ServiceException;
    
    public SADetailApproval getSADetailApproval(String saDetailApprovalId) throws ServiceException;

    public List<SAApproval> getStockAdjutmentApprovalList(String searchString, Paging paging) throws ServiceException ;

    public List<SADetailApproval> getStockAdjutmentDetailApprovalList(SAApproval saApproval, Paging paging) throws ServiceException ;

    public void saveOrUpdate(Object object) throws ServiceException ;

    public int getAttachmentCount(Company company, String moduleWiseMainId) throws ServiceException;

}
