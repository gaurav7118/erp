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
public class AvalaraUpdateItemOperation extends AvalaraIntegrationUtil implements IntegrationOperation {

    @Override
    public JSONObject executeOperation(JSONObject requestJobj, JSONObject integrationAccountDetails) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        boolean success = false;
        String msg = "";
        try {
            String failureMsg = "Item(s) could not be updated in AvaTax.";
            String avalaraItemId = requestJobj.optString("avalaraItemId");
            if (!StringUtil.isNullOrEmpty(avalaraItemId)) {
                String avalaraCompanyId = integrationAccountDetails.optJSONObject(IntegrationConstants.configJson) != null ? integrationAccountDetails.optJSONObject(IntegrationConstants.configJson).optString("avalaraCompanyId") : null;
                String restServiceUrl = integrationAccountDetails.optString(IntegrationConstants.restServiceUrl);
                restServiceUrl += IntegrationConstants.api_version2 + IntegrationConstants.companies + "/" + avalaraCompanyId + "/" + IntegrationConstants.items + "/" + avalaraItemId;
                Map<String, String> headersMap = createHeadersMap(integrationAccountDetails);
                Map<String, Object> otherReqPropertiesMap = createOtherReqPropertiesMap();
                JSONObject payloadJson = getPayloadForUpdateItem(requestJobj, integrationAccountDetails);

                JSONObject resJobj = postRequestToRestService(restServiceUrl, Constants.PUT, payloadJson, IntegrationConstants.integrationPartyId_AVALARA, headersMap, otherReqPropertiesMap);

                if (!StringUtil.isNullOrEmpty(resJobj.optString(IntegrationConstants.response, null))) {
                    JSONObject responseJobj = new JSONObject(resJobj.getString(IntegrationConstants.response));
                    if (resJobj.optInt(IntegrationConstants.responseCode, 0) <= IntegrationConstants.responseCode_299) {
                        if (responseJobj.has("itemCode") && StringUtil.equal(responseJobj.optString("itemCode"), payloadJson.optString("itemCode"))) {
                            returnJobj.put("resultJobj", responseJobj);
                            success = true;
                        } else {
                            success = false;
                        }
                    } else {
                        success = false;
                        if (responseJobj.optJSONObject(IntegrationConstants.error) != null && responseJobj.optJSONObject(IntegrationConstants.error).optJSONArray("details") != null && responseJobj.optJSONObject(IntegrationConstants.error).optJSONArray("details").length() != 0) {
                            JSONObject detailJobj = responseJobj.optJSONObject(IntegrationConstants.error).optJSONArray("details").optJSONObject(0);
                            if (detailJobj != null && !StringUtil.isNullOrEmpty(detailJobj.optString("description", null))) {
                                failureMsg += " " + detailJobj.getString("description");
                            }
                        }
                    }
                } else {
                    success = false;
                }
            }
            if (success) {
                msg = Constants.RES_success;
            } else {
                throw new AccountingException(" " + failureMsg + " ");
            }
        } catch (JSONException | ServiceException | AccountingException ex){
            Logger.getLogger(AvalaraUpdateItemOperation.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            returnJobj.put(Constants.RES_success, success);
            returnJobj.put(Constants.RES_msg, msg);
        }
        return returnJobj;
    }
}
