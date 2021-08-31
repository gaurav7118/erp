/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.common;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface FieldManagerService {
    
    public JSONArray getColumnHeadersConfigList(JSONObject paramJobj)throws ServiceException ;
}
