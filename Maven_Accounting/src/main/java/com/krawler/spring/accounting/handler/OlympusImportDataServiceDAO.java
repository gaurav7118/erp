package com.krawler.spring.accounting.handler;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface OlympusImportDataServiceDAO {
    public JSONObject importMasterLicense(String baseUrl, String olympusSubdomain) throws JSONException;
    
    public JSONObject importMasterLicense(String filePath, String baseUrl, String olympusSubdomain) throws JSONException;

    public JSONObject importSupplimenteryLicense(String baseUrl, String olympusSubdomain, boolean isSuppOneFile) throws JSONException;
    
    public JSONObject importSupplimenteryLicense(String filePath, String baseUrl, String olympusSubdomain, boolean isSuppOneFile) throws JSONException;

    public JSONObject importCustomer(String baseUrl, boolean isBillingAddress, String olympusSubdomain) throws JSONException;
    
    public JSONObject importCustomer(String filePath, String baseUrl, boolean isBillingAddress, String olympusSubdomain) throws JSONException;

    public JSONObject importProducts(String baseUrl, String olympusSubdomain) throws JSONException;
    
    public JSONObject importProducts(String filePath, String baseUrl, String olympusSubdomain) throws JSONException;

    public JSONObject importStockMovementInData(HttpServletRequest request,String olympusSubdomain, String type, String baseUrl) throws JSONException;
    
    public JSONObject importStockMovementInData(HttpServletRequest request, String filePath, String olympusSubdomain, String type, String baseUrl) throws JSONException;

    public void checkImportInfo(String olympusSubdomain, int type, String filepath) throws ServiceException;

    public void addImportFilePath(String filePath, String subdomain, String moduleName, boolean deleteAllPreviousPath) throws ServiceException;
    
    public String[] getImportFilePaths(String subdomain, String moduleName) throws ServiceException;
}
