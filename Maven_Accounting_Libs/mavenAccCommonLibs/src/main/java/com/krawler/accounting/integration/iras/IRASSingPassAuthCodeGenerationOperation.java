/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.iras;

import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.accounting.integration.common.IntegrationOperation;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
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
public class IRASSingPassAuthCodeGenerationOperation extends IRASIntegrationUtil implements IntegrationOperation {

    @Override
    public JSONObject executeOperation(JSONObject requestJobj, JSONObject integrationAccountDetails) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        boolean success = false;
        String msg = "";
        try {
//            String restServiceUrl = IntegrationConstants.iras_Integration_Common_Url;
            String restServiceUrl = integrationAccountDetails.optString(IntegrationConstants.restServiceUrl);
            restServiceUrl += IntegrationConstants.iras_SingPassAuthCodeGeneration_context;
            Map<String, String> headersMap = createHeadersMap(integrationAccountDetails);
            Map<String, Object> otherReqPropertiesMap = createOtherReqPropertiesMap();
            String payload = getPayloadForSingPassAuthCodeGeneration(requestJobj, integrationAccountDetails);
            if (!payload.isEmpty()) {
                restServiceUrl+=payload;
            }
            JSONObject resJobj = postRequestToRestService(restServiceUrl, Constants.GET, payload, IntegrationConstants.integrationPartyId_IRAS, headersMap, otherReqPropertiesMap);

            if (resJobj.optBoolean(Constants.RES_success)) {
                returnJobj.put(Constants.RES_data, new JSONObject(resJobj.optString(IntegrationConstants.response)));
                returnJobj.put(IntegrationConstants.responseCode, resJobj.optString(IntegrationConstants.responseCode));
                success = true;
                msg = Constants.RES_success;
            } else {
                success = false;
                msg = Constants.RES_failure;
            }

        } catch (JSONException ex) {
            Logger.getLogger(IRASGSTForm5SubmissionOperation.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            returnJobj.put(Constants.RES_success, success);
            returnJobj.put(Constants.RES_msg, msg);
        }
        return returnJobj;
    }

}
