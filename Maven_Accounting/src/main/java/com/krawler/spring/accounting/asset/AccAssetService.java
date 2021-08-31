/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.asset;

import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.AssetDetails;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author swapnil.khandre
 */
public interface AccAssetService {

    public JSONObject getAssetDetails(HttpServletRequest request, boolean isexport) throws SessionExpiredException, ParseException, AccountingException;

    public JSONObject getAssetSummeryReportGridInfo(JSONObject requestParams) throws JSONException, ServiceException;

    public JSONObject getAssetSummeryReportDetails(JSONObject jobject) throws JSONException, ServiceException;

    public JSONArray getAssetSummeryReportDetailsJSON(JSONArray DataJArr, JSONObject jobject) throws ServiceException, SessionExpiredException, AccountingException;

    public JSONObject exportAssetSummary(JSONObject paramJobj) throws ServiceException;

    public JSONArray getAssetDepreciation(Map<String, Object> request) throws SessionExpiredException, AccountingException, JSONException;

    public void getAssetStraightLineDepreciation(Map<String, Object> request, AssetDetails ad, JSONArray finalJArr, ExtraCompanyPreferences extraCompanyPreferences) throws SessionExpiredException, AccountingException;

    public void getDoubleDeclineDepreciation(Map<String, Object> request, AssetDetails ad, JSONArray finalJArr, ExtraCompanyPreferences extraCompanyPreferences) throws SessionExpiredException, AccountingException;
    
    public JSONArray getAssetDepreciationDetails(HttpServletRequest request, boolean isexport) throws SessionExpiredException, AccountingException, ParseException;

}
