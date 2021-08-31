/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.sequence.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.exception.SeqFormatException;
import com.krawler.inventory.model.sequence.*;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vipin Gupta
 */
public class SeqDAOImpl extends BaseDAO implements SeqDAO {

    @Override
    public SeqFormat getSeqFormat(String seqFormatId) throws ServiceException {
        return (SeqFormat) get(SeqFormat.class, seqFormatId);
    }

    @Override
    public SeqFormat getDefaultSeqFormat(Company company, SeqModule seqModule) throws ServiceException {
        SeqFormat seqFormat = null;
        String hql = "FROM SeqFormat WHERE company = ? AND seqModule = ? AND defaultFormat = ? ";
        List params = new ArrayList();
        params.add(company);
        params.add(seqModule);
        params.add(true);
        Paging paging = new Paging(0, 1);
        List list = executeQueryPaging(hql, params.toArray(), paging);
        if (!list.isEmpty()) {
            seqFormat = (SeqFormat) list.get(0);
        }
        return seqFormat;
    }

    @Override
    public SeqModule getSeqModule(Integer seqModuleId) throws ServiceException {
        return (SeqModule) get(SeqModule.class, seqModuleId);
    }

    @Override
    public List<SeqModule> getSeqModules(Company company, Boolean isActive, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM SeqModule ");
        List params = new ArrayList();
        if (isActive != null) {
            hql.append(" WHERE active = ? ");
            params.add(isActive);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            if (params.isEmpty()) {
                hql.append(" WHERE name LIKE  ? ");
            } else {
                hql.append(" AND name LIKE  ? ");
            }
            params.add("%" + searchString + "%");
        }
        hql.append(" ORDER BY name ");
        List list = executeQuery(hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            if (paging.isValid()) {
                list = executeQueryPaging(hql.toString(), params.toArray(), paging);
            }
        }
        return list;
    }

    @Override
    public List<SeqFormat> getSeqFormats(Company company, SeqModule seqModule, Boolean isActive, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM SeqFormat WHERE company = ?");
        List params = new ArrayList();
        params.add(company);
        if (seqModule != null) {
            hql.append(" AND seqModule = ? ");
            params.add(seqModule);
        } else {
            hql.append(" AND seqModule.active = ? ");
            params.add(true);
        }
        if (isActive != null) {
            hql.append(" AND active = ? ");
            params.add(isActive);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND ( prefix LIKE = ? or suffix = ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        hql.append(" ORDER BY prefix ");
        List list = executeQuery(hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            if (paging.isValid()) {
                list = executeQueryPaging(hql.toString(), params.toArray(), paging);
            }
        }
        return list;
    }

    @Override
    public void saveOrUpdate(Object object) throws ServiceException {
        super.saveOrUpdate(object);
    }

    @Override
    public SeqNumber getLastUsedSeqNumber(SeqFormat seqFormat) throws ServiceException {
        SeqNumber seqNumber = null;
        String hql = "FROM SeqNumber WHERE seqFormat = ? ";
        List params = new ArrayList();
        params.add(seqFormat);
        Paging paging = new Paging(0, 1);
        List list = executeQueryPaging(hql, params.toArray(), paging);
        if (!list.isEmpty()) {
            seqNumber = (SeqNumber) list.get(0);
        }
        return seqNumber;
    }

    @Override
    public boolean getExistingSeqNumber(String seqFormat, Company company, ModuleConst moduleConst) throws ServiceException, SeqFormatException {
        boolean isexist = false;

        String query = "";
        List params = new ArrayList();
        if (moduleConst.equals(ModuleConst.STOCK_ADJUSTMENT)) {
            query = " FROM StockAdjustment WHERE transactionNo=? AND company= ? ";

        } else if (moduleConst.equals(ModuleConst.INTER_LOCATION_TRANSFER) || moduleConst.equals(ModuleConst.INTER_STORE_TRANSFER)) {
            query = " FROM InterStoreTransferRequest WHERE transactionModule=? AND transactionNo=? AND company= ? ";
            if (moduleConst.equals(ModuleConst.INTER_LOCATION_TRANSFER)) {
                params.add(TransactionModule.INTER_LOCATION_TRANSFER);
            } else if (moduleConst.equals(ModuleConst.INTER_STORE_TRANSFER)) {
                params.add(TransactionModule.INTER_STORE_TRANSFER);
            }
        } else if (moduleConst.equals(ModuleConst.STOCK_REQUEST) || moduleConst.equals(ModuleConst.ISSUE_NOTE)) {
            query = " FROM StockRequest WHERE module=? AND transactionNo=? AND company= ? ";
            if (moduleConst.equals(ModuleConst.STOCK_REQUEST)) {
                params.add(TransactionModule.STOCK_REQUEST);
            } else if (moduleConst.equals(ModuleConst.ISSUE_NOTE)) {
                params.add(TransactionModule.ISSUE_NOTE);
            }
        } else if (moduleConst.equals(ModuleConst.CYCLE_COUNT)) {
            query = " FROM CycleCount WHERE transactionNo=? AND company= ? ";
        }
        params.add(seqFormat);
        params.add(company);
        List list = executeQuery(query, params.toArray());
        if (list.size() > 0) {
            isexist = true;
        }
        return isexist;
    }
    
    @Override
    public List checkInvSequenceFormat(Map<String, Object> filterParams) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(filterParams.get("companyid"));
        String module = (String) filterParams.get("module");
        String columnName ="";
        if(module.equalsIgnoreCase("in_interstoretransfer") || module.equalsIgnoreCase("in_goodsrequest") || module.equalsIgnoreCase("in_cyclecount")){
            columnName = "transactionno";
        } else if(module.equalsIgnoreCase("in_stockadjustment")){
            columnName = "seqno";
        }
        String prefix = (String) filterParams.get("prefix");
        String prefixDateFormat = (String) filterParams.get("prefixDateFormat");
        String suffix = (String) filterParams.get("suffix");
        String suffixDateFormat = (String) filterParams.get("suffixDateFormat");
        List l = new ArrayList();
        String startWith=prefix+prefixDateFormat;
        String endWith=suffix+suffixDateFormat;
        String query = "SELECT * FROM "+module+" WHERE "+columnName+" LIKE '" + startWith + "%' AND "+columnName+" LIKE '%" + endWith + "' and company=?";
        l =  executeSQLQuery(query, params.toArray());
        return l;
    }  
    @Override
    public List getExistingISTSeqNumbers(Map<String, Object> seqParams) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(seqParams.get("companyid"));
        String query = "SELECT transactionNo FROM in_interstoretransfer WHERE transaction_module=2  AND company= ? ";
        List l =  executeSQLQuery(query, params.toArray());
        return l;
    }  
    @Override
    public String deleteInvSequenceFormatNumber(String id) throws ServiceException {
        String returnStr = "";
        int count = 0;
        try {
            String query = "delete from in_seqformat where id = ?";
            count = executeSQLUpdate(query, new Object[]{id});
        } catch (Exception e) {
            Logger.getLogger(SeqDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        if (count == 1) {
            returnStr = "deleted";
        } else {
            returnStr = "Active Record can't be deleted.";
        }
        return returnStr;
    }    
}
