/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.avalara;

import com.krawler.accounting.integration.ups.UpsIntegrationService;
import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.accounting.integration.common.IntegrationOperation;
import com.krawler.accounting.integration.common.IntegrationService;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class AvalaraIntegrationService extends AvalaraIntegrationUtil implements IntegrationService {
    private static final Map<String, IntegrationOperation> avalaraOperationsMap = new HashMap();
    static {
        avalaraOperationsMap.put(IntegrationConstants.avalara_addressValidation, new AvalaraAddressValidationOperation());
        avalaraOperationsMap.put(IntegrationConstants.avalara_cancelTax, new AvalaraCancelTaxOperation());
        avalaraOperationsMap.put(IntegrationConstants.avalara_changeDocCode, new AvalaraChangeDocCodeOperation());
        avalaraOperationsMap.put(IntegrationConstants.avalara_createItems, new AvalaraCreateItemsOperation());
        avalaraOperationsMap.put(IntegrationConstants.avalara_createOrAdjustTransaction, new AvalaraCreateOrAdjustTransactionOperation());
        avalaraOperationsMap.put(IntegrationConstants.avalara_credentialsValidation, new AvalaraCredentialsValidationOperation());
        avalaraOperationsMap.put(IntegrationConstants.avalara_deleteItem, new AvalaraDeleteItemOperation());
        avalaraOperationsMap.put(IntegrationConstants.avalara_getTransaction, new AvalaraGetTransactionOperation());
        avalaraOperationsMap.put(IntegrationConstants.avalara_updateItem, new AvalaraUpdateItemOperation());
    }
    
    private IntegrationOperation getOperation (String operationId) throws AccountingException {
        IntegrationOperation operation = null;
        if (!StringUtil.isNullOrEmpty(operationId) && avalaraOperationsMap.containsKey(operationId)) {
            operation = avalaraOperationsMap.get(operationId);
        } else {
            throw new AccountingException("No operation with operationId found in list of operations.");
        }
        return operation;
    }
    
    @Override
    public JSONObject processRequest (JSONObject requestJobj, JSONObject integrationAccountDetails) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        try {
            String operationId = requestJobj.optString(IntegrationConstants.integrationOperationIdKey);
            if (!StringUtil.isNullOrEmpty(operationId)) {
                returnJobj = getOperation(operationId).executeOperation(requestJobj, integrationAccountDetails);
            }
        } catch (JSONException | ServiceException | AccountingException ex) {
            Logger.getLogger(UpsIntegrationService.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
        return returnJobj;
    }
}
