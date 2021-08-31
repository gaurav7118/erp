/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.ups;

import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.accounting.integration.common.IntegrationOperation;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class UpsCostEstimationOperation extends UpsIntegrationUtil implements IntegrationOperation {
    @Override
    public JSONObject executeOperation (JSONObject requestJobj, JSONObject integrationAccountDetails) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        try {
            String restServiceUrl = integrationAccountDetails.optString(IntegrationConstants.restServiceUrl);
            restServiceUrl += IntegrationConstants.shipContext;
            Map<String, String> headersMap = createHeadersMap();
            Map<String, Object> otherReqPropertiesMap = createOtherReqPropertiesMap();
            JSONObject payloadJson = getPayloadForCostEstimation(requestJobj,integrationAccountDetails);

            JSONObject resJobj = postRequestToRestService(restServiceUrl, Constants.POST, payloadJson, IntegrationConstants.integrationPartyId_UPS, headersMap, otherReqPropertiesMap);

            if (resJobj.optInt(IntegrationConstants.responseCode) <= IntegrationConstants.responseCode_299) {
                returnJobj = new JSONObject(resJobj.optString(IntegrationConstants.response));
            }
        } catch (JSONException | ServiceException ex){
            Logger.getLogger(UpsCostEstimationOperation.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
        return returnJobj;
    }
}
