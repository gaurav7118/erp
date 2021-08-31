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
public class AvalaraDeleteItemOperation extends AvalaraIntegrationUtil implements IntegrationOperation {

    @Override
    public JSONObject executeOperation(JSONObject requestJobj, JSONObject integrationAccountDetails) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        boolean success = false;
        String msg = "";
        try {
            String avalaraItemId = requestJobj.optString("avalaraItemId");
            if (!StringUtil.isNullOrEmpty(avalaraItemId)) {
                String avalaraCompanyId = integrationAccountDetails.optJSONObject(IntegrationConstants.configJson) != null ? integrationAccountDetails.optJSONObject(IntegrationConstants.configJson).optString("avalaraCompanyId") : null;
                String restServiceUrl = integrationAccountDetails.optString(IntegrationConstants.restServiceUrl);
                restServiceUrl += IntegrationConstants.api_version2 + IntegrationConstants.companies + "/" + avalaraCompanyId + "/" + IntegrationConstants.items + "/" + avalaraItemId;
                Map<String, String> headersMap = createHeadersMap(integrationAccountDetails);
                Map<String, Object> otherReqPropertiesMap = createOtherReqPropertiesMap();

                JSONObject resJobj = postRequestToRestService(restServiceUrl, Constants.DELETE, "", IntegrationConstants.integrationPartyId_AVALARA, headersMap, otherReqPropertiesMap);

                if (!StringUtil.isNullOrEmpty(resJobj.optString(IntegrationConstants.response, null))) {
                    JSONArray responseJarr = new JSONArray(resJobj.getString(IntegrationConstants.response));
                    if (responseJarr.length() == 0) {
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
                throw new AccountingException(" Item(s) could not be deleted in AvaTax. ");
            }
        } catch (JSONException | ServiceException | AccountingException ex){
            Logger.getLogger(AvalaraDeleteItemOperation.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            returnJobj.put(Constants.RES_success, success);
            returnJobj.put(Constants.RES_msg, msg);
        }
        return returnJobj;
    }

}
