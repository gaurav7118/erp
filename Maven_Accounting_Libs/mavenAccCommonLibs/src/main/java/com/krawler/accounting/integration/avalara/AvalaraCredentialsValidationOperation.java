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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class AvalaraCredentialsValidationOperation extends AvalaraIntegrationUtil implements IntegrationOperation {

    @Override
    public JSONObject executeOperation(JSONObject requestJobj, JSONObject integrationAccountDetails) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        boolean success = false;
        String msg = "Validation Failed. Please check the details.";
        try {
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(IntegrationConstants.credentialsData))) {
                JSONObject credentialsJobj = new JSONObject(requestJobj.optString(IntegrationConstants.credentialsData));
                String accountNumber = credentialsJobj.optString(IntegrationConstants.accountNumber);
                String companyCode = credentialsJobj.optString(IntegrationConstants.userName);
                String restServiceUrl = credentialsJobj.optString(IntegrationConstants.restServiceUrl);
                restServiceUrl += IntegrationConstants.api_version2 + IntegrationConstants.companies + "?$filter=" + URLEncoder.encode("(companyCode eq '" + companyCode + "')", "UTF-8");
                Map<String, String> headersMap = createHeadersMap(credentialsJobj);//Create headers map from credentialsJobj because credentials which are to be validated are in credentialsJobj
                Map<String, Object> otherReqPropertiesMap = createOtherReqPropertiesMap();
                
                JSONObject resJobj = postRequestToRestService(restServiceUrl, Constants.GET, "", IntegrationConstants.integrationPartyId_AVALARA, headersMap, otherReqPropertiesMap);
                
                if (!StringUtil.isNullOrEmpty(resJobj.optString(IntegrationConstants.response, null))) {
                    JSONObject responseJobj = new JSONObject(resJobj.getString(IntegrationConstants.response));
                    if (responseJobj.has("@recordsetCount") && responseJobj.has(Constants.VALUE)) {
                        if (responseJobj.optInt("@recordsetCount") == 1) {
                            JSONArray valueJarr = responseJobj.optJSONArray(Constants.VALUE);
                            if (valueJarr != null && valueJarr.length() != 0) {
                                JSONObject valueJobj = valueJarr.optJSONObject(0);
                                if (valueJobj != null && valueJobj.has("accountId") && StringUtil.equal(valueJobj.optString("accountId"), accountNumber)) {
                                    returnJobj.put("avalaraCompanyId", valueJobj.optString(Constants.Acc_id));
                                    success = true;
                                    msg = "Validation Successful.";
                                }
                            }
                        } else if (responseJobj.optInt("@recordsetCount") == 0) {
                            success = false;
                            msg = "Company With Company Code could not be found. Please check Company Code.";
                        }
                    } else if (responseJobj.has(IntegrationConstants.error)) {
                        JSONObject errorJobj = responseJobj.optJSONObject(IntegrationConstants.error);
                        if (errorJobj != null && errorJobj.has(IntegrationConstants.code) && StringUtil.equal(errorJobj.optString(IntegrationConstants.code), "AuthenticationException")) {
                            success = false;
                            msg = "Could not authenticate with credentials. Please check Account Number and License Key.";
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AvalaraCredentialsValidationOperation.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(AvalaraCredentialsValidationOperation.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
        returnJobj.put(Constants.RES_success, success);
        returnJobj.put(Constants.RES_msg, msg);
        return returnJobj;
    }
}
