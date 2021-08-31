/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.iras;

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
public class IRASIntegrationService extends IRASIntegrationUtil implements IntegrationService {

    private static final Map<String, IntegrationOperation> irasOperationsMap = new HashMap();

    static {
        irasOperationsMap.put(IntegrationConstants.iras_TransactionListing_Operation, new IRASTransactionListingOperation());
        irasOperationsMap.put(IntegrationConstants.iras_TokenGeneration_Operation, new IRASTokenGenerationOperation());
        irasOperationsMap.put(IntegrationConstants.iras_SingPassAuthCodeGeneration_Operation, new IRASSingPassAuthCodeGenerationOperation());
        irasOperationsMap.put(IntegrationConstants.iras_GSTForm5Submission_Operation, new IRASGSTForm5SubmissionOperation());
    }

    private IntegrationOperation getOperation(String operationId) throws AccountingException {
        IntegrationOperation operation = null;
        if (!StringUtil.isNullOrEmpty(operationId) && irasOperationsMap.containsKey(operationId)) {
            operation = irasOperationsMap.get(operationId);
        } else {
            throw new AccountingException("No operation with operationId found in list of operations.");
        }
        return operation;
    }

    @Override
    public JSONObject processRequest(JSONObject requestJobj, JSONObject integrationAccountDetails) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        try {
            String operationId = requestJobj.optString(IntegrationConstants.integrationOperationIdKey);
            if (!StringUtil.isNullOrEmpty(operationId)) {
                returnJobj = getOperation(operationId).executeOperation(requestJobj, integrationAccountDetails);
            }
        } catch (JSONException | ServiceException | AccountingException ex) {
            Logger.getLogger(IRASIntegrationService.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
        return returnJobj;
    }

}
