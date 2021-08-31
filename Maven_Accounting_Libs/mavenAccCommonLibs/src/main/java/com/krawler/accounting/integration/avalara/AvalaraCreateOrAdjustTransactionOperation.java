/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.avalara;

import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.accounting.integration.common.IntegrationOperation;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class AvalaraCreateOrAdjustTransactionOperation extends AvalaraIntegrationUtil implements IntegrationOperation {

    @Override
    public JSONObject executeOperation(JSONObject requestJobj, JSONObject integrationAccountDetails) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        JSONArray prodTermJarr = new JSONArray();
        boolean success = false;
        String msg = "";
        try {
            String restServiceUrl = integrationAccountDetails.optString(IntegrationConstants.restServiceUrl);
            restServiceUrl += IntegrationConstants.api_version2 + IntegrationConstants.transactions + "/" + IntegrationConstants.createoradjust;
            Map<String, String> headersMap = createHeadersMap(integrationAccountDetails);
            Map<String, Object> otherReqPropertiesMap = createOtherReqPropertiesMap();
            JSONObject payloadJson = getPayloadForCreateOrAdjustTransaction(requestJobj, integrationAccountDetails);
            
            JSONObject resJobj = postRequestToRestService(restServiceUrl, Constants.POST, payloadJson, IntegrationConstants.integrationPartyId_AVALARA, headersMap, otherReqPropertiesMap);
            
            if (resJobj.optInt(IntegrationConstants.responseCode) <= IntegrationConstants.responseCode_299) {
                if (!StringUtil.isNullOrEmpty(resJobj.optString(IntegrationConstants.response, null))) {
                    JSONObject responseJobj = new JSONObject(resJobj.getString(IntegrationConstants.response));
                    if (responseJobj.has(Constants.Acc_id) && responseJobj.has(Constants.status)) {
                        prodTermJarr = processTaxDetails(responseJobj, requestJobj, integrationAccountDetails);
                        returnJobj.put(IntegrationConstants.avalaraDocCode, responseJobj.optString(IntegrationConstants.code));
                        success = true;
                        msg = Constants.RES_success;
                    } else if (responseJobj.has(IntegrationConstants.error)) {
                        //Put some code here
                        success = false;
                        msg = Constants.RES_failure;
                    } else {
                        success = false;
                        msg = Constants.RES_failure;
                    }
                } else {
                    success = false;
                    msg = Constants.RES_failure;
                }
            }
        } catch (JSONException | ServiceException | AccountingException ex) {
            Logger.getLogger(AvalaraCreateOrAdjustTransactionOperation.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            returnJobj.put("prodTermArray", prodTermJarr);
            returnJobj.put(Constants.RES_success, success);
            returnJobj.put(Constants.RES_msg, msg);
        }
        return returnJobj;
    }
}
