/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.common;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.IntegrationDetails;
import com.krawler.common.admin.IntegrationParty;
import com.krawler.common.admin.TransactionDetailAvalaraTaxMapping;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author krawler
 * DAO class for Third Party Service Integration.
 * All common methods used in Integration should be put here.
 * Methods from this interface can be used by all Integration services for example AvalaraIntegrationService, UpsIntegrationService.
 */
public class IntegrationDAOImpl extends BaseDAO implements IntegrationDAO {

    /**
     * To save or update integration account credentials and config into database
     * @param paramsJobj
     * @throws ServiceException 
     */
    @Override
    public void saveOrUpdateIntegrationAccountDetails(JSONObject paramsJobj) throws ServiceException {
        IntegrationDetails integrationDetails = new IntegrationDetails();
        if (!StringUtil.isNullOrEmpty(paramsJobj.optString(Constants.ID))) {
            integrationDetails = (IntegrationDetails) get(IntegrationDetails.class, paramsJobj.optString(Constants.ID));
        }
        if (paramsJobj.has(Constants.companyKey)) {
            Company company = (Company) get(Company.class, paramsJobj.optString(Constants.companyKey));
            integrationDetails.setCompany(company);
        }
        if (paramsJobj.has(IntegrationConstants.integrationPartyIdKey)) {
            IntegrationParty integrationParty = (IntegrationParty) get(IntegrationParty.class, paramsJobj.optInt(IntegrationConstants.integrationPartyIdKey));
            integrationDetails.setIntegrationParty(integrationParty);
        }
        if (paramsJobj.has(IntegrationConstants.userName)) {
            integrationDetails.setUserName(paramsJobj.optString(IntegrationConstants.userName));
        }
        if (paramsJobj.has(IntegrationConstants.passKey)) {
            integrationDetails.setPassKey(paramsJobj.optString(IntegrationConstants.passKey));
        }
        if (paramsJobj.has(IntegrationConstants.licenseKey)) {
            integrationDetails.setLicenseKey(paramsJobj.optString(IntegrationConstants.licenseKey));
        }
        if (paramsJobj.has(IntegrationConstants.accountNumber)) {
            integrationDetails.setAccountNumber(paramsJobj.optString(IntegrationConstants.accountNumber));
        }
        if (paramsJobj.has(IntegrationConstants.restServiceUrl)) {
            integrationDetails.setRestServiceUrl(paramsJobj.optString(IntegrationConstants.restServiceUrl));
        }
        if (paramsJobj.has(IntegrationConstants.configJson)) {
            integrationDetails.setConfigJson(paramsJobj.optString(IntegrationConstants.configJson));
        }
        saveOrUpdate(integrationDetails);
    }

    /**
     * To fetch integration account credentials and config from database
     * @param paramsJobj
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject getIntegrationAccountDetails(JSONObject paramsJobj) throws ServiceException {
        StringBuilder hqlQuery = new StringBuilder("from IntegrationDetails where company.companyID = ? ");
        List<Object> paramsList = new ArrayList<>();
        paramsList.add(paramsJobj.optString(Constants.companyKey));
        if (paramsJobj.has(IntegrationConstants.integrationPartyIdKey)) {
            hqlQuery.append(" and integrationPartyId = ? ");
            paramsList.add(paramsJobj.optInt(IntegrationConstants.integrationPartyIdKey));
        }
        List list = executeQuery(hqlQuery.toString(), paramsList.toArray());
        return new KwlReturnObject(true, "", null, list, 0);
    }
    
    /**
     * Method to fetch TransactionDetailAvalaraTaxMapping from database
     * @param paramsJobj
     * @return
     * @throws ServiceException 
     */
    @Override
    public List getTransactionDetailTaxMapping(JSONObject paramsJobj) throws ServiceException {
        List<Object> paramsList = new ArrayList<>();
        String hqlQuery = " from TransactionDetailAvalaraTaxMapping tdatm where tdatm.parentRecordID = ? ";
        paramsList.add(paramsJobj.optString(IntegrationConstants.parentRecordID));
        return executeQuery(hqlQuery, paramsList.toArray());
    }

    /**
     * Method to save TransactionDetailAvalaraTaxMapping into database
     * @param paramsJobj
     * @throws ServiceException 
     */
    @Override
    public void saveTransactionDetailTaxMapping(JSONObject paramsJobj) throws ServiceException {
        TransactionDetailAvalaraTaxMapping transactionDetailAvalaraTaxMapping = null;
        String id = paramsJobj.optString(IntegrationConstants.parentRecordID);
        if (!StringUtil.isNullOrEmpty(id)) {
            transactionDetailAvalaraTaxMapping = (TransactionDetailAvalaraTaxMapping) get(TransactionDetailAvalaraTaxMapping.class, id);
        }
        if (transactionDetailAvalaraTaxMapping == null) {
            transactionDetailAvalaraTaxMapping = new TransactionDetailAvalaraTaxMapping();
            transactionDetailAvalaraTaxMapping.setParentRecordID(id);
        }
        if (paramsJobj.has(IntegrationConstants.avalaraTaxDetails)) {
            transactionDetailAvalaraTaxMapping.setAvalaraTaxDetails(paramsJobj.optString(IntegrationConstants.avalaraTaxDetails));
        }
        saveOrUpdate(transactionDetailAvalaraTaxMapping);
    }
    
    /**
     * Method to delete TransactionDetailTaxMapping from database
     * @param paramsJobj
     * @throws ServiceException 
     */
    @Override
    public void deleteTransactionDetailTaxMapping(JSONObject paramsJobj) throws ServiceException {
        String parentRecordIDs = paramsJobj.optString(IntegrationConstants.parentRecordID);
        String contractDeleteQuery = "delete from transactiondetailavalarataxmapping where parentrecordid in (" + parentRecordIDs + ")";
        List paramsList = new ArrayList();
        int num = executeSQLUpdate( contractDeleteQuery, paramsList.toArray());
    }
}
