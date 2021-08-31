    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.workcentremanagement;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
/**
 *
 * @author krawler
 */
public interface AccWorkCentreServiceDAO {
    
     public JSONObject getWorkCentreDataandColumnModel(Map<String, Object> requestParams);
     public KwlReturnObject saveWorkCentre(Map<String, Object> requestParams) throws AccountingException, ServiceException;
     public KwlReturnObject deleteWorkCentres(Map<String, Object> dataMap) throws ServiceException;
     public JSONArray getWCCombo(Map <String,Object> map) throws ServiceException;
     public JSONObject deleteWorkcentre(Map<String, Object> requestParams) throws ServiceException;
     public JSONObject deleteWorkCentrePermanently(Map<String, Object> requestParams) throws ServiceException;
     public JSONObject exportWorkCentre(Map<String, Object> requestParams) throws ServiceException;
     
      public JSONObject getWorkcentresForCombo(Map<String, Object> requestParms)throws ServiceException;
}
