/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.profileHandler;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class profileHandlerServiceImpl implements profileHandlerService, MessageSourceAware {
    private profileHandlerDAO profileHandlerDAOObj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private String successView;
    private APICallHandlerService apiCallHandlerService;
    private MessageSource messageSource;
    private auditTrailDAO auditTrailObj;    
    
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    
    public void setprofileHandlerDAO(profileHandlerDAO profileHandlerDAOObj1) {
        this.profileHandlerDAOObj = profileHandlerDAOObj1;
    }

    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }
   
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }
    
  @Override  
    public JSONObject getAllUserDetails(JSONObject paramJobj)
            throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            String lid = StringUtil.checkForNull(paramJobj.optString(Constants.lid));
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            requestParams.put("lid", lid);
            requestParams.put("start", StringUtil.checkForNull(paramJobj.optString("start",null)));
            requestParams.put("limit", StringUtil.checkForNull(paramJobj.optString("limit",null)));
            requestParams.put("ss", StringUtil.checkForNull(paramJobj.optString("ss",null)));

            ArrayList filter_names = new ArrayList();
            ArrayList filter_params = new ArrayList();
            filter_names.add("u.company.companyID");
            filter_params.add(companyid);
            filter_names.add("u.deleteflag");
            filter_params.add(0);
            if (!StringUtil.isNullOrEmpty(lid)) {
                filter_names.add("u.userID");
                filter_params.add(lid);
            }

            kmsg = profileHandlerDAOObj.getUserDetails(requestParams, filter_names, filter_params);
            jobj = getUserDetailsJson(kmsg.getEntityList(), paramJobj, kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
  
    public JSONObject getUserDetailsJson(List<User> userList, JSONObject paramJobj, int totalSize) {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {
            boolean isApprover=false;
            boolean isFromCustomReportBuilder=paramJobj.optBoolean("isFromCustomReportBuilder",false);
            String timeFormatId = paramJobj.optString(Constants.timeformat);
            String timeZoneDiff = paramJobj.optString(Constants.timezonedifference);
            String usersVisibilityFlow=paramJobj.optString("usersVisibilityFlow","false");
            boolean uservisibility=Boolean.parseBoolean(usersVisibilityFlow);
            Map<String,String> map=new HashMap();
            for (User user:userList) {
                UserLogin ul = user.getUserLogin();
                JSONObject obj = new JSONObject();
                obj.put("userid", user.getUserID());
                obj.put("username", ul.getUserName());
                obj.put("fname", user.getFirstName());
                obj.put("lname", user.getLastName());
                obj.put("fullname", user.getFirstName()+" "+user.getLastName());
                obj.put("image", user.getImage());
                obj.put("emailid", user.getEmailID());
                obj.put("lastlogin", (ul.getLastActivityDate() == null ? "" : authHandler.getUserDateFormatterWithoutTimeZone(paramJobj).format(ul.getLastActivityDate())));
                obj.put("aboutuser", user.getAboutUser());
                obj.put("address", user.getAddress());
                obj.put("contactno", user.getContactNumber());
                obj.put("formatid", (user.getDateFormat() == null ? "" : user.getDateFormat().getFormatID()));
                obj.put("tzid", (user.getTimeZone() == null ? Constants.NEWYORK_TIMEZONE_ID : user.getTimeZone().getTimeZoneID())); // 23 is id of New York Time Zone. [default]
                obj.put("callwithid", user.getCallwith());
                obj.put("timeformat", (user.getTimeformat() != 1 && user.getTimeformat() != 2) ? 2 : user.getTimeformat()); // 2 is id for '24 hour timeformat'. [default]
                obj.put("fullname", user.getFirstName() + " " + user.getLastName());
                obj.put("department",user.getDepartment() == null ? "":user.getDepartment());               
                obj.put("designation",user.getDesignation()==null ? "":user.getDesignation());
                obj.put("employeeid",user.getEmployeeId()==null ? "":user.getEmployeeId());
                kmsg = permissionHandlerDAOObj.getRoleofUser(user.getUserID());
                Iterator ite2 = kmsg.getEntityList().iterator();
                while (ite2.hasNext()) {
                    Object[] row = (Object[]) ite2.next();
                    obj.put("roleid", row[0]);
                    obj.put("rolename", row[1]);
                }
                isApprover = profileHandlerDAOObj.checkIsUserApprover(user.getUserID());
                obj.put("isApprover", isApprover);
                
                /**
                 * Add user group info of user
                 */
                if (uservisibility) {
                    map.put("userid", user.getUserID());
                    String grpName = profileHandlerDAOObj.getUserGroupForUser(map);
                    obj.put("usergroup", grpName);
                }
                jarr.put(obj);
            }
            //Added as part of ERP-37913
            if(isFromCustomReportBuilder){
                JSONObject obj = new JSONObject();
                obj.put("fullname", Constants.CURRENT_USER);
                obj.put("userid", Constants.CURRENT_USER);
                jarr.put(obj);
            }
            jobj.put("data", jarr);
            jobj.put("count", totalSize);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
    
    public JSONObject getUserDetailsFromApps(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONArray jarr = null;
            String accURL = StorageHandler.getAccURL();
            String companyid = paramJobj.optString("companyid");
            String userid = paramJobj.optString("userid");
            JSONObject userData = new JSONObject();
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("companyid", companyid);
            userData.put("userid",userid);
            userData.put("appid",3);
            userData.put("requesturl", accURL);
            userData.put("converttoJArray", true);
            String accRestURL = URLUtil.buildRestURL(Constants.PLATFORM_URL);
            String endpoint = accRestURL + "company/user";
            JSONObject resObj = apiCallHandlerService.restGetMethod(endpoint, userData.toString());

            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                jarr =  resObj.getJSONArray("data");
            }
            if (jarr != null && jarr.length() > 0) {
                jobj.put("data",jarr);
//                jobj.put("count", jobj.length());
                msg = "Data fetched successfully";
                issuccess = true;
            } else {
                msg = "Error occurred while fetching data ";
            }
        } catch (Exception ex) {
            msg += " " + ex.getMessage();
            Logger.getLogger(profileHandlerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                msg += " " + ex.getMessage();
                Logger.getLogger(profileHandlerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class})
    public JSONObject getPasswordPolicy(JSONObject requestJobj) throws ServiceException, JSONException {
        List<PasswordPolicy> passwordPolicyList = null;
        String companyid = requestJobj.optString(Constants.companyKey);
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            KwlReturnObject resultKwlObj = profileHandlerDAOObj.getPasswordPolicy(companyid);
            passwordPolicyList = resultKwlObj.getEntityList();
            if (passwordPolicyList != null && !passwordPolicyList.isEmpty()) {
                PasswordPolicy passwordPolicy = passwordPolicyList.get(0);
                if (passwordPolicy != null) {
                    JSONObject passwordPolicyJobj = new JSONObject();
                    passwordPolicyJobj.put("policyid", passwordPolicy.getPolicyid());
                    passwordPolicyJobj.put(Constants.companyKey, passwordPolicy.getCompanyid().getCompanyID());
                    passwordPolicyJobj.put("minchar", passwordPolicy.getMinchar());
                    passwordPolicyJobj.put("maxchar", passwordPolicy.getMaxchar());
                    passwordPolicyJobj.put("minnum", passwordPolicy.getMinnum());
                    passwordPolicyJobj.put("minalphabet", passwordPolicy.getMinalphabet());
                    passwordPolicyJobj.put("specialchar", passwordPolicy.getSpecialchar());
                    passwordPolicyJobj.put("defpass", passwordPolicy.getDefpass());
                    passwordPolicyJobj.put("ppass", passwordPolicy.getPpass());
                    passwordPolicyJobj.put("setpolicy", passwordPolicy.getSetpolicy());
                    jobj.put(Constants.RES_data, passwordPolicyJobj);
                }
                issuccess = true;
                msg = " ";
            } else {
                issuccess = false;
                msg = messageSource.getMessage("acc.field.noPasswordPolicySet", null, Locale.forLanguageTag(requestJobj.optString(Constants.language)));
            }
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            issuccess = false;
            throw ex;
        } catch (Exception ex) {
            msg = ex.getMessage();
            issuccess = false;
            Logger.getLogger(profileHandlerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            jobj.put(Constants.RES_success, issuccess);
            jobj.put(Constants.RES_msg, msg);
        }
        return jobj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class})
    public JSONObject saveOrUpdatePasswordPolicy(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject response = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            KwlReturnObject resultKwlObj = profileHandlerDAOObj.getPasswordPolicy(paramJobj.optString(Constants.companyKey));
            List passwordPolicyList = resultKwlObj.getEntityList();
            int deletedRowsCnt = 0;
            if (passwordPolicyList != null && !passwordPolicyList.isEmpty()) {
                deletedRowsCnt = profileHandlerDAOObj.deletePasswordPolicy(paramJobj);
            }
            paramJobj.put("minchar", paramJobj.optInt("minchar", 4));
            paramJobj.put("maxchar", paramJobj.optInt("maxchar", 32));
            paramJobj.put("minnum", paramJobj.optInt("minnum", 0));
            paramJobj.put("minalphabet", paramJobj.optInt("minalphabet", 0));
            paramJobj.put("specialchar", paramJobj.optString("specialchar").equalsIgnoreCase("on") ? 1 : 0);
            paramJobj.put("setpolicy", paramJobj.optString("setpolicy").equalsIgnoreCase("on") ? 1 : 0);
            profileHandlerDAOObj.saveOrUpdatePasswordPolicy(paramJobj);
            msg = messageSource.getMessage("acc.field.passwordPolicySavedSuccessfully", null, (Locale) paramJobj.get(Constants.locale));
            issuccess = true;
            String userid = paramJobj.has(Constants.useridKey) && !(StringUtil.isNullOrEmpty(paramJobj.optString(Constants.useridKey))) ? paramJobj.optString(Constants.useridKey) : paramJobj.optString(Constants.creatoridKey);
            String userfullname = paramJobj.has(Constants.userfullname) ? paramJobj.optString(Constants.userfullname) : paramJobj.optString(Constants.creatorUserName);
            Map<String, Object> auditParamsMap = new HashMap();
            auditParamsMap.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
            auditParamsMap.put(Constants.useridKey, userid);
            auditParamsMap.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditParamsMap.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            if (deletedRowsCnt != 0) {
                auditTrailObj.insertAuditLog(AuditAction.PASSWORD_POLICY_UPDATED, "User " + userfullname + " has updated password policy. ", auditParamsMap, paramJobj.optString("policyid"));
            } else {
                auditTrailObj.insertAuditLog(AuditAction.PASSWORD_POLICY_ADDED, "User " + userfullname + " has added password policy. ", auditParamsMap, paramJobj.optString("policyid"));
            }
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            issuccess = false;
            Logger.getLogger(profileHandlerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } catch (Exception ex) {
            msg = ex.getMessage();
            issuccess = false;
            Logger.getLogger(profileHandlerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            response.put(Constants.RES_msg, msg);
            response.put(Constants.RES_success, issuccess);
        }
        return response;
    }
}
