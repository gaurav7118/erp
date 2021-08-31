/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.permissionHandler;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

/**
 *
 * @author krawler
 */
public interface permissionHandlerService {

    /**
     * Below Method returns user role name, roll id and display name in JSON
     * array And Below method moved from permissionHandlerController.
     *
     * @param requestJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject getRoles(JSONObject requestJobj) throws ServiceException, JSONException;
}
