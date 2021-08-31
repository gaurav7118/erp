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

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;

/**
 *
 * @author Karthik
 */
public class permissionHandlerDAOImpl extends BaseDAO implements permissionHandlerDAO {

    public KwlReturnObject getFeatureList() throws ServiceException {
        int dl = 0;
        List ll = null;
        try {
            String Hql = "select featureID, featureName, displayFeatureName,orderNo from ProjectFeature order by orderNo";
            ll = executeQuery( Hql);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getFeatureList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    /**
     * ERP-41687 Return a All Role list related company change in old query to
     * get condition based result
     *
     * @param requestJobj
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject getRoleList(JSONObject requestJobj) throws ServiceException {
        List ll = null;
        int dl = 0;
        ArrayList params = new ArrayList();
        try {
            String order_by = requestJobj.optString(Constants.orderBy);
            String order_type = requestJobj.optString(Constants.orderType);
            String Hql = "from Rolelist r where ( company is null or company.companyID= ? ) ";
            params.add(requestJobj.optString(Constants.companyKey));
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.ss))) {
                String searchString = "%" + requestJobj.optString(Constants.ss) + "%";
                params.add(searchString);
                Hql += " and ( r.rolename like ? ";
                params.add(searchString);
                Hql += " or r.description like ? ";
                params.add(searchString);
                Hql += " or r.displayrolename like ? ) ";
            }
            if (!StringUtil.isNullOrEmpty(order_by)) {
                if (order_by.equals("rolename")) {
                    Hql += " order by r.rolename ";
                } else if (order_by.equals("displayrolename")) {
                    Hql += " order by r.displayrolename ";
                } else if (order_by.equals("description")) {
                    Hql += " order by r.description ";
                } else {
                    Hql += " order by r.roleid ";
                }
                if (!StringUtil.isNullOrEmpty(order_type) && (order_type.equalsIgnoreCase(Constants.ascending) || order_type.equalsIgnoreCase(Constants.descending))) {
                    Hql += " " + order_type;
                }
            } else {
                Hql += " order by r.roleid ";
                if (!StringUtil.isNullOrEmpty(order_type) && (order_type.equalsIgnoreCase(Constants.ascending) || order_type.equalsIgnoreCase(Constants.descending))) {
                    Hql += " " + order_type;
                }
            }
            if (!StringUtil.isNullOrEmpty(requestJobj.optString(Constants.start)) && !StringUtil.isNullOrEmpty(requestJobj.optString(Constants.limit))) {
                ll = executeQueryPaging(Hql, params.toArray(), new Integer[]{requestJobj.optInt(Constants.start), requestJobj.optInt(Constants.limit)});
            } else {
                ll = executeQuery(Hql, params.toArray());
            }
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getRoleList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getReportUserList(String userid, String companyid) throws ServiceException {
        List list = null;
        String mysqlQuery = "";
        int count = 0;
        ArrayList params = new ArrayList();
        params.add(userid);
        params.add(companyid);
        mysqlQuery = "select reportrolemap.reportid,reportrolemap.id from reportrolemap inner join users on users.userid=reportrolemap.userid where reportrolemap.userid=? and users.company=?";
        list = executeSQLQuery( mysqlQuery, params.toArray());
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getRoleofUser(String userid) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String Hql = "select roleId.roleid, roleId.displayrolename from RoleUserMapping where userId.userID=?";
            ll = executeQuery( Hql, userid);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getRoleofUser", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getActivityList() throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String Hql = "select feature.featureID, activityID, activityName, displayActivityName, parent.activityID from ProjectActivity order by orderNo";
            ll = executeQuery( Hql);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getActivityList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject saveFeatureList(HashMap<String, Object> requestParams) throws ServiceException {
        int dl = 0;
        List ll = null;
        try {
            String id = requestParams.containsKey("featureid") && requestParams.get("featureid") != null ? requestParams.get("featureid").toString() : "";
            ProjectFeature feature;
            if (!StringUtil.isNullOrEmpty(id)) {
                feature = (ProjectFeature) load(ProjectFeature.class, id);
            } else {
                feature = new ProjectFeature();
            }
            if (requestParams.containsKey("featurename") && requestParams.get("featurename") != null) {
                feature.setFeatureName(requestParams.get("featurename").toString());
            }
            if (requestParams.containsKey("displayfeaturename") && requestParams.get("displayfeaturename") != null) {
                feature.setDisplayFeatureName(requestParams.get("displayfeaturename").toString());
            }
            saveOrUpdate(feature);
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.saveFeatureList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject saveRoleList(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String id = requestParams.containsKey("roleid") && requestParams.get("roleid") != null ? requestParams.get("roleid").toString() : "";
            Rolelist role;
            if (!StringUtil.isNullOrEmpty(id)) {
                role = (Rolelist) load(Rolelist.class, id);
            } else {
                role = new Rolelist();
            }
            if (requestParams.containsKey("rolename") && requestParams.get("rolename") != null) {
                role.setRolename(requestParams.get("rolename").toString());
            }
            if (requestParams.containsKey("displayrolename") && requestParams.get("displayrolename") != null) {
                role.setDisplayrolename(requestParams.get("displayrolename").toString());
            }
            role.setCompany((Company) load(Company.class, requestParams.get("companyid").toString()));
            saveOrUpdate(role);

            RoleUserMapping rum = new RoleUserMapping();
            rum.setRoleId(role);
            if (requestParams.containsKey("userid") && requestParams.get("userid") != null) {
                User user = (User) load(User.class, requestParams.get("userid").toString());
                user.setRoleID(role.getRoleid());
            }
            saveOrUpdate(rum);
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.saveRoleList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject saveActivityList(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String id = requestParams.containsKey("activityid") && requestParams.get("activityid") != null ? requestParams.get("activityid").toString() : "";
            ProjectActivity activity;
            ProjectFeature feature = null;
            if (!StringUtil.isNullOrEmpty(id)) {
                activity = (ProjectActivity) load(ProjectActivity.class, id);
            } else {
                activity = new ProjectActivity();
                feature = (ProjectFeature) load(ProjectFeature.class, requestParams.get("featureid").toString());
                activity.setFeature(feature);
            }
            if (requestParams.containsKey("activityname") && requestParams.get("activityname") != null) {
                activity.setActivityName(requestParams.get("activityname").toString());
            }
            if (requestParams.containsKey("displayactivityname") && requestParams.get("displayactivityname") != null) {
                activity.setDisplayActivityName(requestParams.get("displayactivityname").toString());
            }
            saveOrUpdate(activity);
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.saveActivityList", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject deleteFeature(HashMap<String, Object> requestParams) throws ServiceException {
        int dl = 0;
        List ll = null;
        try {
            String id = requestParams.containsKey("featureid") && requestParams.get("featureid") != null ? requestParams.get("featureid").toString() : "";
            ProjectFeature feature;
            if (!StringUtil.isNullOrEmpty(id)) {
                feature = (ProjectFeature) load(ProjectFeature.class, id);
                delete(feature);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.deleteFeature", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject deleteRole(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        String Hql = "";
        Rolelist role = null;
        String msg = "";
        try {
            String id = requestParams.containsKey("roleid") && requestParams.get("roleid") != null ? requestParams.get("roleid").toString() : "";
            if (!StringUtil.isNullOrEmpty(id)) {
                role = (Rolelist) load(Rolelist.class, id);
                Hql = "from RoleUserMapping where roleId=?";
                ll = executeQuery( Hql, role);
                if (ll.size() > 0) {
                    msg = "acc.rolemanagement.deleterole";
                } else {
                    delete(role);
                    msg = "Role deleted successfully.";
                }
            }
            ll = new ArrayList();
            ll.add(msg);
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.deleteRole", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject deleteActivity(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String id = requestParams.containsKey("activityid") && requestParams.get("activityid") != null ? requestParams.get("activityid").toString() : "";
            ProjectActivity activity;
            if (StringUtil.isNullOrEmpty(id)) {
                activity = (ProjectActivity) load(ProjectActivity.class, id);
                delete(activity);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.deleteActivity", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getActivityFeature() throws ServiceException {
        List ll = null;
        int dl = 0;
        try {
            String Hql = "select pf, pa from ProjectActivity pa right outer join pa.feature pf order by pa.orderNo";
            ll = executeQuery( Hql);
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getActivityFeature", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getUserPermission(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        String Hql = "";
        ArrayList params = null;
        try {
            String userid = requestParams.containsKey("userid") && requestParams.get("userid") != null ? requestParams.get("userid").toString() : "";
            String roleid = requestParams.containsKey("roleid") && requestParams.get("roleid") != null ? requestParams.get("roleid").toString() : "";
            String companyid = requestParams.containsKey("companyid") && requestParams.get("companyid") != null ? requestParams.get("companyid").toString() : "";
            boolean allowedit= requestParams.containsKey("allowedit")&&requestParams.get("allowedit") != null?Boolean.parseBoolean( requestParams.get("allowedit").toString()):false ;
            boolean isdefaultHeaderMap= requestParams.containsKey(Constants.isdefaultHeaderMap)&&requestParams.get(Constants.isdefaultHeaderMap) != null?Boolean.parseBoolean( requestParams.get(Constants.isdefaultHeaderMap).toString()):false ;
            String mappingid = null;
            String temp = roleid;

            if (!StringUtil.isNullOrEmpty(userid)) {
                String Hql1 = "from RoleUserMapping where userId.userID=? ";
                ll = executeQuery( Hql1, userid);
                if (ll != null) {
                    Iterator itr = ll.iterator();
                    if (itr.hasNext()) {
                        RoleUserMapping rmapping = (RoleUserMapping) itr.next();
                        roleid = rmapping.getRoleId().getRoleid();
                        mappingid = rmapping.getId();
                    }
                }
            }
            if (!StringUtil.isNullOrEmpty(temp) && temp.equals(roleid) || StringUtil.isNullOrEmpty(temp)) {
                Hql = " select feature.featureName, permissionCode, feature.featureID from UserPermission up";
                String condition = "";
                params = new ArrayList();

                if (!StringUtil.isNullOrEmpty(roleid)) {
                    condition += (condition.length() == 0 ? " where " : " and ") + " role.roleid=? ";
                    params.add(roleid);
                }

                if (!StringUtil.isNullOrEmpty(mappingid)) {
                    condition += (condition.length() == 0 ? " where " : " and ") + " roleUserMapping.id=? ";
                    params.add(mappingid);
                }

                ll = executeQuery( Hql + condition, params.toArray());
                dl = ll.size();
            }
            if (dl == 0 && (roleid.equals(Rolelist.COMPANY_ADMIN)&&!allowedit)) {
                if (isdefaultHeaderMap) {
                    Hql = " select DISTINCT feature.featureName, permissionCode, feature.featureID from UserPermission up where role.roleid=? ";
                } else {
                    Hql = " select feature.featureName, permissionCode, feature.featureID from UserPermission up where role.roleid=? ";
                }
                
                ll = executeQuery( Hql, roleid);
                dl = ll.size();
            } else if (dl == 0) {
                temp=StringUtil.isNullOrEmpty(temp)?roleid:temp;
                if(temp.equals(Rolelist.COMPANY_ADMIN)){
                  Hql = " select feature.featureName, permissionCode, feature.featureID from UserPermission up where role.roleid=? ";  
                  ll = executeQuery( Hql, new Object[]{temp});
                }else{
                Hql = " select feature.featureName, permissionCode, feature.featureID from RolePermission up where role.roleid=? and company.companyID=?";
                ll = executeQuery( Hql, new Object[]{temp, companyid});
                 }
               
                dl = ll.size();
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getUserPermission", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }
            
  public boolean isSuperAdmin(String userid, String companyid) throws ServiceException {
        boolean admin = false;
        try {
            // Hardcoded id of admin user and admin company.
            if (userid.equals("ff808081227d4f5801227d535ebb0009") && companyid.equals("ff808081227d4f5801227d535eba0008")) {
                admin = true;
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.isSuperAdmin", e);
        }
        return admin;
    }

    public KwlReturnObject setPermissions(HashMap<String, Object> requestParams, String[] features, String[] permissions) throws ServiceException {
        List ll = null;
        List<UserPermission> oldPermissions=null;
        String permissionChangeDetailStr="";
        int dl = 0;
        String rid = "";
        String Hql = "";
        try {
            String id = requestParams.containsKey("userid") && requestParams.get("userid") != null ? requestParams.get("userid").toString() : "";
            String roleId = requestParams.containsKey("roleid") && requestParams.get("roleid") != null ? requestParams.get("roleid").toString() : "";

            Hql = "select id from RoleUserMapping where userId.userID=?";
            ll = executeQuery( Hql, id);
            rid = ll.get(0).toString();
            
            //fetch previous permissions first to compare with newer/edited for managing audit trail messages
            
            Hql = "from UserPermission where roleUserMapping.id=?";
            oldPermissions = (List<UserPermission>)executeQuery( Hql, rid);
            
            Hql = "delete from UserPermission where roleUserMapping.id=?";
            executeUpdate( Hql, new Object[]{rid});

            Hql = "delete from RoleUserMapping where userId.userID=?";
            executeUpdate( Hql, id);

//            Hql = "select id from RoleUserMapping where userId.userID=? ";
//            ll = executeQuery( Hql, id);

            //  rid = ll.get(0).toString();
            RoleUserMapping rum = new RoleUserMapping();//) load(RoleUserMapping.class, rid);
            Rolelist role = (Rolelist) load(Rolelist.class, roleId);
            User user = (User) load(User.class, id);
            rum.setUserId(user);
            rum.setRoleId(role);
            save(rum);

//            Hql = "select id from RoleUserMapping where userId.userID=? ";
//            ll = executeQuery( Hql, id);
//            rid = ll.get(0).toString();
//            rum = (RoleUserMapping) load(RoleUserMapping.class, rid);

            user.setRoleID(roleId);
            save(user);

            if (!(roleId.equals(Rolelist.COMPANY_ADMIN))) {
//                Hql = "delete from UserPermission where role.roleid=?";
//                executeUpdate( Hql, roleId);

                for (int i = 0; i < features.length; i++) {
//                    if (permissions[i].equals("0")) {
//                        continue;
//                    }
                    ProjectFeature projFeature = (ProjectFeature) load(ProjectFeature.class, features[i]);
                    UserPermission permission = new UserPermission();
                    permission.setRole(role);
                    permission.setFeature(projFeature);
                    permission.setPermissionCode(Long.parseLong(permissions[i]));
                    permission.setRoleUserMapping(rum);
                    save(permission);

                    try {
                        Set<ProjectActivity> activities = permission.getFeature().getActivities();

                        if (oldPermissions != null && !oldPermissions.isEmpty()) {
                            UserPermission oldUP = oldPermissions.get(i);
                            if (oldUP.getFeature() == projFeature && oldUP.getPermissionCode() != permission.getPermissionCode()) {
                                
                                permissionChangeDetailStr += " ( <b>Feature Name :</b> " + projFeature.getDisplayFeatureName();
                                String changePermissionFrom="";
                                String changePermissionTo="";
                                for (ProjectActivity pa : activities) {
                                    double d1 = oldUP.getPermissionCode();
                                    double d2 = permission.getPermissionCode();
                                    double powVal = Math.pow(2, pa.getOrderNo() - 1);
                                    //System.out.println("Feature : " + projFeature.getDisplayFeatureName() + " Old Permission code : " + oldUP.getPermissionCode() + "  New Permission Code : " + permission.getPermissionCode());
                                    
                                    if(((long) (d1) & (long) (powVal)) == (long) powVal){ // if true ie. was given permission  in old permission
                                        if("".equals(changePermissionFrom)){
                                            changePermissionFrom += " , <b>Old Permissions :</b> " +pa.getDisplayActivityName();
                                        }else{
                                            changePermissionFrom += " , "+pa.getDisplayActivityName();
                                        }
                                    }else if(d1 == 0 && "".equals(changePermissionFrom)){
                                        changePermissionFrom += " , <b>Old Permissions :</b> " ;
                                    }
                                    
                                    if(((long) (d2) & (long) (powVal)) == (long) powVal){ // if true ie. was given permission  in new permission
                                        if("".equals(changePermissionTo)){
                                            changePermissionTo += " , <b>New Permissions :</b> " +pa.getDisplayActivityName();
                                        }else{
                                            changePermissionTo += " , "+pa.getDisplayActivityName();
                                        }
                                    }else if(d2 == 0 && "".equals(changePermissionTo)){
                                        changePermissionTo += " , <b>New Permissions :</b> ";
                                    }
                                    
//                                    if (((long) (d1) & (long) (powVal)) != ((long) (d2) & (long) (powVal))) {
//                                        System.out.println(projFeature.getDisplayFeatureName());
//                                        System.out.println(pa.getDisplayActivityName());
//                                        System.out.println("OLD PERMISSION :" + (((long) (d1) & (long) (powVal)) == (long) powVal));
//                                        System.out.println("NEW PERMISSION :" + (((long) (d2) & (long) (powVal)) == (long) powVal));
//                                    }
                                }
                                permissionChangeDetailStr += (changePermissionFrom +"   "+changePermissionTo+ " ) ");
                            }
                        }

                    } catch (Exception e) {
                        System.out.println("ERROR : " + e.getMessage());
                    }
                }
            }
            UserLogin userLogin = (UserLogin) load(UserLogin.class, id);
            ll = new ArrayList();
            ll.add(userLogin);
//        } catch (Exception e) {
//            throw ServiceException.FAILURE("permissionHandlerDAOImpl.setPermissions", e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new KwlReturnObject(true, permissionChangeDetailStr, "", ll, dl);
    }
   
    @Override
    public void deleteUserPermissions(String roleid) throws ServiceException {
        try {
            String Query = "delete from UserPermission where role.roleid=?";
            executeUpdate( Query, roleid);
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.deleteUserPermissions", e);
        }
    }

    @Override
    public User getCreator(String companyid) {
        User creator = null;

        Company company = (Company) get(Company.class, companyid);
        if (company != null) {
            creator = company.getCreator();
        }

        return creator;
    }
      public KwlReturnObject getRolePermission(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        ArrayList params = null;
        try {
            String companyid = requestParams.containsKey("companyid") && requestParams.get("companyid") != null ? requestParams.get("companyid").toString() : "";
            String roleid = requestParams.containsKey("roleid") && requestParams.get("roleid") != null ? requestParams.get("roleid").toString() : "";
            
            String Hql = " select feature.featureName, permissionCode, feature.featureID from RolePermission up";
            String condition = "";
            params = new ArrayList();

            if (!StringUtil.isNullOrEmpty(roleid)) {
                condition += (condition.length() == 0 ? " where " : " and ") + "role.roleid=? ";
                params.add(roleid);
            }

            if (!StringUtil.isNullOrEmpty(companyid)) {
                condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=? ";
                params.add(companyid);
            }

            ll = executeQuery( Hql + condition, params.toArray());
            dl = ll.size();

            if (dl == 0 && (roleid.equals(Rolelist.COMPANY_ADMIN))) {
                Hql = " select feature.featureName, permissionCode, feature.featureID from RolePermission up where role.roleid=? ";
                ll = executeQuery( Hql, roleid);
                dl = ll.size();
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.getUserPermission", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }
      
    @Override
    public void assignRoles(String userid, String roleid) throws ServiceException {
        String query = "";
        try {
            User user = (User) get(User.class, userid);
            user.setRoleID(roleid);
            user.setDeleteflag(0);

            String Hql = "from RoleUserMapping where userId.userID=? ";
            List ll = executeQuery(Hql, user.getUserID());
            Iterator itr = ll.iterator();
            if (itr.hasNext()) {
                RoleUserMapping rmapping = (RoleUserMapping) itr.next();
                rmapping.setRoleId((Rolelist) get(Rolelist.class, roleid));
                update(rmapping);
            }

//            if (roleid.equals(Role.COMPANY_ADMIN)) {  // commenting this code as it updating the Company Creator on role add/change
//                query = "select company.companyID from User u where u.userID=?";
//                List result1 = executeQuery(query, user.getUserID());
//                if (result1 != null && result1.size() > 0) {
//                    query = "update Company set creator.userID=? where companyID=?";
//                    executeUpdate(query, new Object[]{user.getUserID(), result1.get(0)});
//                }
//            }
            update(user);
        } catch (Exception e) {
            throw ServiceException.FAILURE("permissionHandlerDAOImpl.assignRoles", e);
        }
    }
}