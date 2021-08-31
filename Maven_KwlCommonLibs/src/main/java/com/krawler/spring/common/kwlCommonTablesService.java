/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.common;

import com.krawler.common.admin.*;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

/**
 *
 * @author krawler
 */
public interface kwlCommonTablesService {
    
    public JSONObject getAllTimeZones(JSONObject paramJObj) throws ServletException;

    public JSONObject getAllCurrencies(JSONObject paramJObj) throws ServletException;

    public JSONObject getAllTimeZonesJson(List<KWLTimeZone> ll, int totalSize);
     
    public JSONObject getLandingCostCategory(JSONObject paramJObj) throws ServletException ;
    
    public JSONObject getSourceDocumentTermsInLinkingDocument(JSONObject paramJObj) throws ServletException ;

    public JSONObject getLandingCostCategoryJson(List<LandingCostCategory> listData);

    public JSONObject getAllCurrenciesJson(List<KWLCurrency> currencylist, int totalSize);

    public JSONObject getAllCountries(JSONObject paramJObj) throws ServletException;

    public JSONObject getAllCountriesJson(List<Country> countryList, int totalSize);

    public JSONObject getAllStates(JSONObject paramJObj) throws ServletException;

    public JSONObject getAllStatesJson(List<State> stateList, int totalSize);

    public JSONObject getSubdomainListFromCountry(JSONObject paramJObj) throws ServletException ;
      
    public JSONObject getSubdomainListFromCountryJson(List<Company> companyList, int totalSize);

    public JSONObject getAllDateFormats(JSONObject paramJObj)throws ServletException ;
    
    public JSONObject getAllDateFormatsJson(List ll, JSONObject paramJObj, int totalSize);
    
    public JSONObject getAllInventoryStores(JSONObject paramJObj) throws ServletException ;
}
