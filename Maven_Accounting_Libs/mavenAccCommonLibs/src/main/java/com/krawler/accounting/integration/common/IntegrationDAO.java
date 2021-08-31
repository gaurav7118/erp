/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.common;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;

/**
 *
 * @author krawler
 * DAO Interface for Third Party Service Integration.
 * All common methods used in Integration should be put here.
 * Methods from this interface can be used by all Integration services for example AvalaraIntegrationService, UpsIntegrationService.
 */
public interface IntegrationDAO {

    public void saveOrUpdateIntegrationAccountDetails(JSONObject paramsJobj) throws ServiceException;

    public KwlReturnObject getIntegrationAccountDetails(JSONObject paramsJobj) throws ServiceException;
    
    public List getTransactionDetailTaxMapping(JSONObject paramsJobj) throws ServiceException;

    public void saveTransactionDetailTaxMapping(JSONObject paramsJobj) throws ServiceException;
    
    public void deleteTransactionDetailTaxMapping(JSONObject paramsJobj) throws ServiceException;
}
