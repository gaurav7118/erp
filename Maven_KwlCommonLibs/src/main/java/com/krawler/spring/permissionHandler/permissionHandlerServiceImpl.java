/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.permissionHandler;

import com.krawler.common.admin.Rolelist;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class permissionHandlerServiceImpl implements permissionHandlerService {

    private permissionHandlerDAO permissionHandlerDAOObj;
    private Object Constans;

    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }

    /**
     * Below Method returns user role name, roll id and display name in JSON
     * array And Below method moved from permissionHandlerController.
     *
     * @param requestJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public JSONObject getRoles(JSONObject requestJobj) throws ServiceException, JSONException {
        KwlReturnObject kmsg = null;
        boolean issuccess = false;
        JSONObject jobj = new JSONObject();
        String msg = "Failure";
        try {
            kmsg = permissionHandlerDAOObj.getRoleList(requestJobj);
            jobj = getRoleJson(kmsg.getEntityList(), kmsg.getRecordTotalCount());
            JSONArray DataJArr = jobj.getJSONArray("data");
            int count = DataJArr.length();
            JSONArray pagedJson = DataJArr;
            jobj.put("data", pagedJson);
            jobj.put("count", count);
            issuccess = true;
            msg = "Success";
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            issuccess = false;
            throw ex;
        } catch (Exception ex) {
            msg = ex.getMessage();
            issuccess = false;
            Logger.getLogger(permissionHandlerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            jobj.put(Constants.RES_msg, msg);
            jobj.put(Constants.RES_success, issuccess);
        }
        return jobj;
    }

    private JSONObject getRoleJson(List ll, int totalSize) {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        try {
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                Rolelist rl = (Rolelist) ite.next();
                JSONObject obj = new JSONObject();
                obj.put("roleid", rl.getRoleid());
                obj.put("rolename", rl.getRolename());
                obj.put("displayrolename", rl.getDisplayrolename());
                obj.put("desc", rl.getDescription());
                jarr.put(obj);
            }
            jobj.put("data", jarr);
            jobj.put("count", totalSize);
        } catch (Exception ex) {
            Logger.getLogger(permissionHandlerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

}
