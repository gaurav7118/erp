/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.spring.permissionHandler;

import com.krawler.utils.json.base.JSONObject;
import com.krawler.common.admin.ProjectActivity;
import com.krawler.common.admin.ProjectFeature;
import com.krawler.utils.json.base.JSONException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Karthik
 */
public class permissionHandler {

    public static JSONObject getAllPermissionJson(List ll, HttpServletRequest request, int totalSize) {
        JSONObject jobj = new JSONObject();
        JSONObject fjobj = new JSONObject();
        try {
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                String fName = ((ProjectFeature) row[0]).getFeatureName();
                ProjectActivity activity = (ProjectActivity) row[1];
                if (!fjobj.has(fName)) {
                    fjobj.put(fName, new JSONObject());
                }
                JSONObject temp = fjobj.getJSONObject(fName);
                if (activity != null) {
                    temp.put(activity.getActivityName(), (int) Math.pow(2, temp.length()));
                }
            }
            jobj.put("Perm", fjobj);
            jobj.put("count", totalSize);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }

    public static JSONObject getRolePermissionJson(List ll, JSONObject jobj) {
        JSONObject ujobj = new JSONObject();
        try {
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                ujobj.put(row[0].toString(), row[1]);
            }
            jobj.put("UPerm", ujobj);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }

    public static boolean isPermitted(JSONObject perms, String featureName, String activityName) throws JSONException {
        int perm = perms.getJSONObject("Perm").getJSONObject(featureName).optInt(activityName);
        int uperm = perms.getJSONObject("UPerm").optInt(featureName);
        if (perm != 0 && ((perm & uperm) == perm)) {
            return true;
        }
        return false;
    }
}
