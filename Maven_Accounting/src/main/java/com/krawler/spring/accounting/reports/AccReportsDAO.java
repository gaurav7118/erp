/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccReportsDAO {

    public KwlReturnObject getLedgerInfo(Map<String, Object> requestParams) throws ServiceException;

    public List getSOAInfo(JSONObject requestObj) throws ServiceException, JSONException;
}
