/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.sa.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.InventoryModules;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.model.approval.consignmentimpl.ConsignmentDAOImpl;
import com.krawler.inventory.model.approval.sa.SAApproval;
import com.krawler.inventory.model.approval.sa.SAApprovalDAO;
import com.krawler.inventory.model.approval.sa.SADetailApproval;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vipin Gupta
 */
public class SAApprovalDAOImpl extends BaseDAO implements SAApprovalDAO {

    public SAApproval getSAApproval(String saApprovalId) throws ServiceException {
        return (SAApproval) get(SAApproval.class, saApprovalId);
    }

    public SADetailApproval getSADetailApproval(String saDetailApprovalId) throws ServiceException {
        return (SADetailApproval) get(SADetailApproval.class, saDetailApprovalId);
    }

    @Override
    public List<SAApproval> getStockAdjutmentApprovalList(String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM SAApproval ");
        List params = new ArrayList();
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" WHERE (stockAdjustment.product.productid LIKE ? OR stockAdjustment.product.name LIKE ? OR stockAdjustment.transactionNo LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }

        hql.append(" ORDER BY stockAdjustment.product.productid ");
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
    public List<SADetailApproval> getStockAdjutmentDetailApprovalList(SAApproval saApproval, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM SADetailApproval WHERE saApproval = ? ORDER BY approvalStatus ");
        List params = new ArrayList();
        params.add(saApproval);

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
        super.saveOrUpdate(object);
    }

    @Override
    public int getAttachmentCount(Company company, String moduleWiseMainId) throws ServiceException {
        
        int attachmentCount = 0;
        List dataList = new ArrayList();
        String fetchDateQry = " SELECT count(*) AS count FROM in_documentcompmap  WHERE modulewiseid = ? AND company = ? AND module = ? ";
        List params = new ArrayList();
        params.add(moduleWiseMainId);
        params.add(company.getCompanyID());
        params.add(InventoryModules.QA_INSPECTION_APPROVAL.ordinal());

        try {
            dataList = executeSQLQuery(fetchDateQry, params.toArray());
            if (!dataList.isEmpty() && dataList != null) {
                attachmentCount = ((BigInteger) dataList.get(0)).intValue();
            }

        } catch (Exception ex) {
            Logger.getLogger(ConsignmentDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return attachmentCount;
        }
    }
}
