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
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class AvalaraChangeDocCodeOperation extends AvalaraIntegrationUtil implements IntegrationOperation {

    @Override
    public JSONObject executeOperation(JSONObject requestJobj, JSONObject integrationAccountDetails) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        boolean success = false;
        String msg = "";
        try {
            String oldDocCode = requestJobj.optString("oldDocCode");
            String newDocCode = requestJobj.optString("newDocCode");
            if (!StringUtil.isNullOrEmpty(oldDocCode) && !StringUtil.isNullOrEmpty(newDocCode)) {
                String restServiceUrl = integrationAccountDetails.optString(IntegrationConstants.restServiceUrl);
                restServiceUrl += IntegrationConstants.api_version2 + IntegrationConstants.companies + "/" + integrationAccountDetails.optString(IntegrationConstants.userName) + "/" + IntegrationConstants.transactions + "/" + oldDocCode + "/" + IntegrationConstants.changecode;
                Map<String, String> headersMap = createHeadersMap(integrationAccountDetails);
                Map<String, Object> otherReqPropertiesMap = createOtherReqPropertiesMap();
                JSONObject payloadJson = getPayloadForChangeDocCode(requestJobj, integrationAccountDetails);

                JSONObject resJobj = postRequestToRestService(restServiceUrl, Constants.POST, payloadJson, IntegrationConstants.integrationPartyId_AVALARA, headersMap, otherReqPropertiesMap);

                if (!StringUtil.isNullOrEmpty(resJobj.optString(IntegrationConstants.response, null))) {
                    JSONObject responseJobj = new JSONObject(resJobj.getString(IntegrationConstants.response));
                    if (responseJobj.has(IntegrationConstants.code) && StringUtil.equal(responseJobj.optString(IntegrationConstants.code), newDocCode)) {
                        success = true;
                    } else {
                        success = false;
                    }
                } else {
                    success = false;
                }
            }
            if (success) {
                msg = Constants.RES_success;
            } else {
                throw new AccountingException(" Doc Code could not be changed. ");
            }
        } catch (JSONException | ServiceException | AccountingException ex){
            Logger.getLogger(AvalaraChangeDocCodeOperation.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            returnJobj.put(Constants.RES_success, success);
            returnJobj.put(Constants.RES_msg, msg);
        }
        return returnJobj;
    }
}
