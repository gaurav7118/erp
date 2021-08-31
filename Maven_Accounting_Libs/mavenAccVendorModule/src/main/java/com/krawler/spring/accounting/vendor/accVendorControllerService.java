/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.vendor;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.CIMBReceivingDetails;
import com.krawler.hql.accounting.IBGReceivingBankDetails;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Atul
 */
public interface accVendorControllerService {

    public IBGReceivingBankDetails saveIBGReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException;

    public HashMap<String, Object> getIBGReceivingBankDetailsRequestParamsMap(HttpServletRequest request) throws SessionExpiredException;

    public JSONArray getIBGReceivingBankDetailsForVendor(HttpServletRequest request) throws SessionExpiredException, ServiceException;

    public void deleteIBGReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException;

    public IBGReceivingBankDetails saveIBGReceivingBankDetailsJSON(HashMap<String, Object> ibgMap);
    
    public CIMBReceivingDetails saveCIMBReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException;
    
    public HashMap<String, Object> getCIMBReceivingBankDetailsRequestParamsMap(HttpServletRequest request) throws SessionExpiredException;
    
    public void deleteCIMBReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException;

    public CIMBReceivingDetails saveCIMBReceivingBankDetailsJSON(HashMap<String, Object> ibgMap);
    
    public JSONArray getCIMBReceivingBankDetailsForVendor(HttpServletRequest request) throws SessionExpiredException, ServiceException;
    
    public JSONObject saveOCBCReceivingBankDetails(JSONObject paramsObj) throws ServiceException;

    public JSONObject getOCBCReceivingBankDetails(JSONObject paramsObj) throws ServiceException;
    
    public JSONObject deleteOCBCReceivingBankDetails(JSONObject paramsObj) throws ServiceException;
    
    public JSONObject getVendorsForCombo(JSONObject paramJobj);
}
