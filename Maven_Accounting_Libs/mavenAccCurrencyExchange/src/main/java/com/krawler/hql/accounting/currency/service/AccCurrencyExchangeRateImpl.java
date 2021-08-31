/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting.currency.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONObject;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Map;
import com.krawler.esp.utils.ConfigReader;
/**
 *
 * @author krawler
 */
public class AccCurrencyExchangeRateImpl implements AccCurrencyExchangeRate{

    @Override
    public JSONObject getUpdatedExchangeRates(Map request) throws ServiceException {
        JSONObject objectToReturn = new JSONObject();
        
        try{
            if(request.containsKey("baseCurrency") && request.get("baseCurrency")!=null){
            String companyBaseCurrency = request.get("baseCurrency").toString();    
            String apiKey =ConfigReader.getinstance().get("API_Key_JSONRATES");//"jr-c8828253c8630269f3a86251aed03a0f";
            URL url = new URL("http://jsonrates.com/get/?" + "base="+companyBaseCurrency+ "&amount=1" +"&apiKey="+apiKey);
            URLConnection uc = url.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            InputStream is= null;
            is = uc.getInputStream();
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(is));
            String res = StringUtil.DecodeText(in.readLine());
            objectToReturn = new JSONObject(res);
            
            }
        } catch(Exception ex){
            throw ServiceException.FAILURE("accCurrencyExchangerateImpl.getUpdatedExchangeRates : " + ex.getMessage(), ex);
        }   
        return objectToReturn;
    }
    
}
