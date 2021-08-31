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
public class AvalaraAddressValidationOperation extends AvalaraIntegrationUtil implements IntegrationOperation {
    @Override
    public JSONObject executeOperation(JSONObject requestJobj, JSONObject integrationAccountDetails) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(IntegrationConstants.addressesForValidationWithAvalara))) {
                String restServiceUrl = integrationAccountDetails.optString(IntegrationConstants.restServiceUrl);
                restServiceUrl += IntegrationConstants.api_version2 + IntegrationConstants.addresses + "/" + IntegrationConstants.resolve;
                Map<String, String> headersMap = createHeadersMap(integrationAccountDetails);
                Map<String, Object> otherReqPropertiesMap = createOtherReqPropertiesMap();

                /**
                 * **********Validation of Billing Address : Start**************
                 */
                JSONObject payloadJson = getPayloadForBillingAddressValidation(requestJobj);

                JSONObject resJobj = postRequestToRestService(restServiceUrl, Constants.POST, payloadJson, IntegrationConstants.integrationPartyId_AVALARA, headersMap, otherReqPropertiesMap);
                /*
                 * messages or error key comes in response if and only if when address could not be validated
                 * 'messages' key comes when request is successful but address si found invalid
                 * 'error' key comes in response when request fails for some reason and ErrorStream is read from HttpUrlConnection instead of Response Stream
                 */
                if (!StringUtil.isNullOrEmpty(resJobj.optString(IntegrationConstants.response, null)) && !(new JSONObject(resJobj.getString(IntegrationConstants.response)).has("messages") || new JSONObject(resJobj.getString(IntegrationConstants.response)).has(IntegrationConstants.error))) {
                    success = true;
                    msg += "<b>Billling Address: </b>Validation Successful.<br><br>";
                } else {
                    success = false;
                    msg += "<b>Billling Address: </b>Validation Failed. Please check the details.<br><br>";
                }
                /**
                 * **********Validation of Billing Address : End**************
                 */

                /**
                 * **********Validation of Shipping Address : Start**************
                 */
                payloadJson = getPayloadForShippingAddressValidation(requestJobj);

                resJobj = postRequestToRestService(restServiceUrl, Constants.POST, payloadJson, IntegrationConstants.integrationPartyId_AVALARA, headersMap, otherReqPropertiesMap);
                /*
                 * messages or error key comes in response if and only if when address could not be validated
                 * 'messages' key comes when request is successful but address si found invalid
                 * 'error' key comes in response when request fails for some reason and ErrorStream is read from HttpUrlConnection instead of Response Stream
                 */
                if (!StringUtil.isNullOrEmpty(resJobj.optString(IntegrationConstants.response, null)) && !(new JSONObject(resJobj.getString(IntegrationConstants.response)).has("messages") || new JSONObject(resJobj.getString(IntegrationConstants.response)).has(IntegrationConstants.error))) {
                    msg += "<b>Shipping Address: </b>Validation Successful.<br>";
                } else {
                    success = false;
                    msg += "<b>Shipping Address: </b>Validation Failed. Please check the details.<br>";
                }
                /**
                 * **********Validation of Shipping Address : End**************
                 */
            }
            if (StringUtil.isNullOrEmpty(msg)) {//If address details is null or empty then set success equal to false and msg equal to failure message
                success = false;
                msg = "Validation Failed. Please check the details.";
            }
        } catch (JSONException | ServiceException ex){
            Logger.getLogger(AvalaraAddressValidationOperation.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            returnJobj.put(Constants.RES_success, success);
            returnJobj.put(Constants.RES_msg, msg);
        }
        return returnJobj;
    }
}
