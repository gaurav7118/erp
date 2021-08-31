/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.ups;

import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.accounting.integration.common.IntegrationOperation;
import com.krawler.accounting.integration.common.IntegrationService;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;

/**
 *
 * @author krawler
 */
public class UpsIntegrationService extends UpsIntegrationUtil implements IntegrationService {
    
    private static final Map<String, IntegrationOperation> upsOperationsMap = new HashMap();
    static {
        upsOperationsMap.put(IntegrationConstants.ups_costEstimation, new UpsCostEstimationOperation());
        upsOperationsMap.put(IntegrationConstants.ups_shipping, new UpsShippingOperation());
        upsOperationsMap.put(IntegrationConstants.ups_labelRecovery, new UpsLabelRecoveryOperation());
    }
    
    private IntegrationOperation getOperation (String operationId) throws AccountingException {
        IntegrationOperation operation = null;
        if (!StringUtil.isNullOrEmpty(operationId) && upsOperationsMap.containsKey(operationId)) {
            operation = upsOperationsMap.get(operationId);
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
