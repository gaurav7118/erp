/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.iras;

import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.accounting.integration.common.IntegrationUtil;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public class IRASIntegrationUtil extends IntegrationUtil {

    public Map<String, String> createHeadersMap(JSONObject integrationAccountDetails) {
        Map<String, String> headersMap = new HashMap();
        headersMap.put("X-IBM-Client-Id", integrationAccountDetails.optString(IntegrationConstants.userName));
        headersMap.put("X-IBM-Client-Secret", integrationAccountDetails.optString(IntegrationConstants.passKey));
        headersMap.put(IntegrationConstants.content_type, "application/json");
        headersMap.put(IntegrationConstants.accept, "application/json");
        return headersMap;
    }

    public Map<String, Object> createOtherReqPropertiesMap() {
        Map<String, Object> otherReqPropertiesMap = new HashMap();
        otherReqPropertiesMap.put(IntegrationConstants.doOutput, true);
        return otherReqPropertiesMap;
    }

    public String getPayloadForSingPassAuthCodeGeneration(JSONObject paramsjobj, JSONObject integrationAccountDetails) throws JSONException {
        String response = "";
        String scope = "?scope=" + paramsjobj.optString("scope");
//        String callback_url = "&callback_url=" + getProxyServerCallbackUrl();
        String callback_url = "&callback_url=http://sandbox.deskera.com/irasdemo/callback.jsp";
        String state = "&state=" + paramsjobj.optString("state");
        response = scope + callback_url + state;
        return response;
    }

    public String getPayloadForTokenGeneration(JSONObject paramsjobj, JSONObject integrationAccountDetails) throws JSONException {
        String response = "";
        String scope = "?scope=GSTReturnsSub+GSTTransListSub";
//        String callback_url = "&callback_url=" + getProxyServerCallbackUrl();
        //Temperory change
        String callback_url = "&callback_url=http://sandbox.deskera.com/irasdemo/callback.jsp";
        String code = "&code=" + paramsjobj.optString("code");
        response = scope + callback_url + code;
        return response;
    }
    
    private String getProxyServerCallbackUrl () {
        return ConfigReader.getinstance().get("deskeraProxyURL") + "ACCReports/irascallback.do";
    }

}
