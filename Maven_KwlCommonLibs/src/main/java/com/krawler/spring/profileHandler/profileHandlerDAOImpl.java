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
package com.krawler.spring.profileHandler;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.spring.authHandler.authHandler;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Karthik
 */
public class profileHandlerDAOImpl extends BaseDAO implements profileHandlerDAO {

    private APICallHandlerService apiCallHandlerService; 

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }

    public String getUserFullName(String userid) throws ServiceException {
        String name = "";
        List ll = new ArrayList();
        try {
            String SELECT_USER_INFO = "select u.firstName, u.lastName from User as u "
                    + "where u.userID = ?  and u.deleteflag=0 ";
            ll = executeQuery( SELECT_USER_INFO, new Object[]{userid});
            name = profileHandler.getUserFullName(ll);
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.getUserFullName", e);
        }
        return name;
    }

    public KwlReturnObject getUserDetails(HashMap<String, Object> requestParams, ArrayList filter_names, ArrayList filter_params) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        int start = 0;
        int limit = 0;
        String serverSearch = "";
        try {
            if (requestParams.containsKey("start") && !StringUtil.isNullOrEmpty(requestParams.get("start").toString())) {
                start = Integer.parseInt(requestParams.get("start").toString());
            }
            if (requestParams.containsKey("limit") && !StringUtil.isNullOrEmpty(requestParams.get("limit").toString())) {
                limit = Integer.parseInt(requestParams.get("limit").toString());
            }
            if (requestParams.containsKey("ss") && !StringUtil.isNullOrEmpty(requestParams.get("ss").toString())) {
                serverSearch = requestParams.get("ss").toString();
            }
            String SELECT_USER_INFO = "from User u ";
            String filterQuery = StringUtil.filterQuery(filter_names, "where");
            SELECT_USER_INFO += filterQuery;

            if (!StringUtil.isNullOrEmpty(serverSearch)) {
                SELECT_USER_INFO +=" and (concat_ws(' ',TRIM(u.firstName),TRIM(u.lastName)) like  '%" + serverSearch + "%'";
                SELECT_USER_INFO += " or u.userLogin.userName like '%" + serverSearch + "%')";
            }
            ll = executeQuery( SELECT_USER_INFO, filter_params.toArray());
            dl = ll.size();
            if (requestParams.containsKey("start") && !StringUtil.isNullOrEmpty(requestParams.get("start").toString()) && requestParams.containsKey("limit") && !StringUtil.isNullOrEmpty(requestParams.get("limit").toString())) {
                ll = executeQueryPaging( SELECT_USER_INFO, filter_params.toArray(), new Integer[]{start, limit});
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.getAllUserDetails", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getAllManagers(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        String companyid = "";
        try {
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = requestParams.get("companyid").toString();
            }
            String role = " and ( bitwise_and( roleID , 2 ) = 2 ) ";
            String SELECT_USER_INFO = "from User u where company.companyID=?  and deleteflag=0 " + role;
            ll = executeQuery( SELECT_USER_INFO, new Object[]{companyid});
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.getAllManagers", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject saveUser(HashMap<String, Object> requestParams) throws ServiceException {
        String id = "";
        String dateid = "";
        User user = null;
        UserLogin ul=null;
        String userid="";
        boolean userLoginFlag=false;
        List list = new ArrayList();
        try {
            if (requestParams.containsKey("userid") && requestParams.get("userid") != null) {
                id = requestParams.get("userid").toString();
                user = (User) load(User.class, id);
            } else {
                user = new User();
                userid = UUID.randomUUID().toString();
                user.setUserID(userid);
            }
            if (requestParams.containsKey("dateformat") && requestParams.get("dateformat") != null) {
                dateid = requestParams.get("dateformat").toString();
                user.setDateFormat((KWLDateFormat) load(KWLDateFormat.class, dateid));
            }
            if (requestParams.containsKey("userlogin") && requestParams.get("userlogin") != null) {
                String userLoginId = requestParams.get("userlogin").toString();
                user.setUserLogin((UserLogin) load(UserLogin.class, userLoginId));
            }
            if (requestParams.containsKey("image") && requestParams.get("image") != null) {
                String image = requestParams.get("image").toString();
                user.setImage(image);
            }
            if (requestParams.containsKey("firstName") && requestParams.get("firstName") != null) {
                String firstName = requestParams.get("firstName").toString();
                user.setFirstName(firstName);
            }
            if (requestParams.containsKey("lastName") && requestParams.get("lastName") != null) {
                String lastName = requestParams.get("lastName").toString();
                user.setLastName(lastName);
            }
            if (requestParams.containsKey("role") && requestParams.get("role") != null) {
                String role = requestParams.get("role").toString();
                user.setRoleID(role);
            }
            if (requestParams.containsKey("emailID") && requestParams.get("emailID") != null) {
                String emailID = requestParams.get("emailID").toString();
                user.setEmailID(emailID);
            }
            if (requestParams.containsKey("address") && requestParams.get("address") != null) {
                String address = requestParams.get("address").toString();
                user.setAddress(address);
            }
            if (requestParams.containsKey("designation") && requestParams.get("designation") != null) {
                String designation = requestParams.get("designation").toString();
                user.setDesignation(designation);
            }
            if (requestParams.containsKey("contactno") && requestParams.get("contactno") != null) {
                String contactNumber = requestParams.get("contactno").toString();
                user.setContactNumber(contactNumber);
            }
            if (requestParams.containsKey("aboutUser") && requestParams.get("aboutUser") != null) {
                String aboutUser = requestParams.get("aboutUser").toString();
                user.setAboutUser(aboutUser);
            }
            if (requestParams.containsKey("userStatus") && requestParams.get("userStatus") != null) {
                String userStatus = requestParams.get("userStatus").toString();
                user.setUserStatus(userStatus);
            }
            if (requestParams.containsKey("timeZone") && requestParams.get("timeZone") != null) {
                String timeZone = requestParams.get("timeZone").toString();
                user.setTimeZone((KWLTimeZone) load(KWLTimeZone.class, timeZone));
            }
            if (requestParams.containsKey("company") && requestParams.get("company") != null) {
                String company = requestParams.get("company").toString();
                user.setCompany((Company) load(Company.class, company));
            }
            if (requestParams.containsKey("fax") && requestParams.get("fax") != null) {
                String fax = requestParams.get("fax").toString();
                user.setFax(fax);
            }
            if (requestParams.containsKey("alternateContactNumber") && requestParams.get("alternateContactNumber") != null) {
                String alternateContactNumber = requestParams.get("alternateContactNumber").toString();
                user.setAlternateContactNumber(alternateContactNumber);
            }
            if (requestParams.containsKey("phpBBID") && requestParams.get("phpBBID") != null) {
                int phpBBID = Integer.parseInt(requestParams.get("phpBBID").toString());
                user.setPhpBBID(phpBBID);
            }
            if (requestParams.containsKey("panNumber") && requestParams.get("panNumber") != null) {
                String panNumber = requestParams.get("panNumber").toString();
                user.setPanNumber(panNumber);
            }
            if (requestParams.containsKey("ssnNumber") && requestParams.get("ssnNumber") != null) {
                String ssnNumber = requestParams.get("ssnNumber").toString();
                user.setSsnNumber(ssnNumber);
            }
            if (requestParams.containsKey("dateFormat") && requestParams.get("dateFormat") != null) {
                String dateFormat = requestParams.get("dateFormat").toString();
                user.setDateFormat((KWLDateFormat) load(KWLDateFormat.class, dateFormat));
            }
            if (requestParams.containsKey("timeformat") && requestParams.get("timeformat") != null) {
                int timeformat = Integer.parseInt(requestParams.get("timeformat").toString());
                user.setTimeformat(timeformat);
            }
            if (requestParams.containsKey("createdon") && requestParams.get("createdon") != null) {
                Date created = (Date) requestParams.get("createdon");
                user.setCreatedon(created);
            }
            if (requestParams.containsKey("updatedon") && requestParams.get("updatedon") != null) {
                Date updatedon = (Date) requestParams.get("updatedon");
                user.setCreatedon(updatedon);
            }
            if (requestParams.containsKey("deleteflag") && requestParams.get("deleteflag") != null) {
                int deleteflag = Integer.parseInt(requestParams.get("deleteflag").toString());
                user.setDeleteflag(deleteflag);
            }
            if (requestParams.containsKey("callwith") && requestParams.get("callwith") != null) {
                int callwith = Integer.parseInt(requestParams.get("callwith").toString());
                user.setCallwith(callwith);
            }
            if (requestParams.containsKey("userhash") && requestParams.get("userhash") != null) {
                String user_hash = requestParams.get("userhash").toString();
                user.setUser_hash(user_hash);
            }
            saveOrUpdate(user);
            list.add(user);
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.saveUser", e);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    public KwlReturnObject addUser(HashMap<String, Object> requestParams) throws ServiceException {
        String id = "";
        String dateid = "";
        User user = null;
        UserLogin ul=null;
        String userid="";
        boolean userLoginFlag=false;
        List list = new ArrayList();
        try {
            if (requestParams.containsKey("userid") && requestParams.get("userid") != null) {
                id = requestParams.get("userid").toString();
                user = (User) get(User.class, id);
                if(user == null){
                    user = new User();
                    user.setUserID(id);
                }
            } else {
                user = new User();
                userid = UUID.randomUUID().toString();
                user.setUserID(userid);
            }
            if (requestParams.containsKey("dateformat") && requestParams.get("dateformat") != null) {
                dateid = requestParams.get("dateformat").toString();
                user.setDateFormat((KWLDateFormat) get(KWLDateFormat.class, dateid));
            }
            if (requestParams.containsKey("userlogin") && requestParams.get("userlogin") != null) {
                String userLoginId = requestParams.get("userlogin").toString();
                user.setUserLogin((UserLogin) get(UserLogin.class, userLoginId));
            }
            if (requestParams.containsKey("image") && requestParams.get("image") != null) {
                String image = requestParams.get("image").toString();
                user.setImage(image);
            }
            if (requestParams.containsKey("firstName") && requestParams.get("firstName") != null) {
                String firstName = requestParams.get("firstName").toString();
                user.setFirstName(firstName);
            }
            if (requestParams.containsKey("lastName") && requestParams.get("lastName") != null) {
                String lastName = requestParams.get("lastName").toString();
                user.setLastName(lastName);
            }
            if (requestParams.containsKey("role") && requestParams.get("role") != null) {
                String role = requestParams.get("role").toString();
                user.setRoleID(role);
            }
            if (requestParams.containsKey("emailID") && requestParams.get("emailID") != null) {
                String emailID = requestParams.get("emailID").toString();
                user.setEmailID(emailID);
            }
            if (requestParams.containsKey("address") && requestParams.get("address") != null) {
                String address = requestParams.get("address").toString();
                user.setAddress(address);
            }
            if (requestParams.containsKey("designation") && requestParams.get("designation") != null) {   
                String designation = requestParams.get("designation").toString();
                user.setDesignation(designation);
            }
//            if (requestParams.containsKey("department") && requestParams.get("department") != null) { //reverted for ERM-581
//                String department = requestParams.get("department").toString();
//                user.setDepartment(department);
//            }
            if (requestParams.containsKey("employeeid") && requestParams.get("employeeid") != null) {
                String employeeid = requestParams.get("employeeid").toString();
                user.setEmployeeId(employeeid);
            }
            
            if (requestParams.containsKey("contactno") && requestParams.get("contactno") != null) {
                String contactNumber = requestParams.get("contactno").toString();
                user.setContactNumber(contactNumber);
            }
            if (requestParams.containsKey("aboutUser") && requestParams.get("aboutUser") != null) {
                String aboutUser = requestParams.get("aboutUser").toString();
                user.setAboutUser(aboutUser);
            }
            if (requestParams.containsKey("userStatus") && requestParams.get("userStatus") != null) {
                String userStatus = requestParams.get("userStatus").toString();
                user.setUserStatus(userStatus);
            }
            if (requestParams.containsKey("timeZone") && requestParams.get("timeZone") != null) {
                String timeZone = requestParams.get("timeZone").toString();
                user.setTimeZone((KWLTimeZone) get(KWLTimeZone.class, timeZone));
            }
            if (requestParams.containsKey("company") && requestParams.get("company") != null) {
                String company = requestParams.get("company").toString();
                user.setCompany((Company) get(Company.class, company));
            }
            if (requestParams.containsKey("fax") && requestParams.get("fax") != null) {
                String fax = requestParams.get("fax").toString();
                user.setFax(fax);
            }
            if (requestParams.containsKey("alternateContactNumber") && requestParams.get("alternateContactNumber") != null) {
                String alternateContactNumber = requestParams.get("alternateContactNumber").toString();
                user.setAlternateContactNumber(alternateContactNumber);
            }
            if (requestParams.containsKey("phpBBID") && requestParams.get("phpBBID") != null) {
                int phpBBID = Integer.parseInt(requestParams.get("phpBBID").toString());
                user.setPhpBBID(phpBBID);
            }
            if (requestParams.containsKey("panNumber") && requestParams.get("panNumber") != null) {
                String panNumber = requestParams.get("panNumber").toString();
                user.setPanNumber(panNumber);
            }
            if (requestParams.containsKey("ssnNumber") && requestParams.get("ssnNumber") != null) {
                String ssnNumber = requestParams.get("ssnNumber").toString();
                user.setSsnNumber(ssnNumber);
            }
            if (requestParams.containsKey("dateFormat") && requestParams.get("dateFormat") != null) {
                String dateFormat = requestParams.get("dateFormat").toString();
                user.setDateFormat((KWLDateFormat) get(KWLDateFormat.class, dateFormat));
            }
            if (requestParams.containsKey("timeformat") && requestParams.get("timeformat") != null) {
                int timeformat = Integer.parseInt(requestParams.get("timeformat").toString());
                user.setTimeformat(timeformat);
            }
            if (requestParams.containsKey("createdon") && requestParams.get("createdon") != null) {
                Date created = (Date) requestParams.get("createdon");
                user.setCreatedon(created);
            }
            if (requestParams.containsKey("updatedon") && requestParams.get("updatedon") != null) {
                Date updatedon = (Date) requestParams.get("updatedon");
                user.setCreatedon(updatedon);
            }
            if (requestParams.containsKey("deleteflag") && requestParams.get("deleteflag") != null) {
                int deleteflag = Integer.parseInt(requestParams.get("deleteflag").toString());
                user.setDeleteflag(deleteflag);
            }
            if (requestParams.containsKey("callwith") && requestParams.get("callwith") != null) {
                int callwith = Integer.parseInt(requestParams.get("callwith").toString());
                user.setCallwith(callwith);
            }
            if (requestParams.containsKey("userhash") && requestParams.get("userhash") != null) {
                String user_hash = requestParams.get("userhash").toString();
                user.setUser_hash(user_hash);
            }
            saveOrUpdate(user);
            list.add(user);
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.saveUser", e);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
    
    @Override
    public KwlReturnObject addCompany(HashMap<String, Object> requestParams) throws ServiceException {
        Company company = null;
        User user = null;
        List list = new ArrayList();
        try {
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                String id = requestParams.get("companyid").toString();
                company = (Company) get(Company.class, id);
                if(company == null){
                    company = new Company();
                    company.setCompanyID(id);
                }
            } else {
                company = new Company();
                String companyid = UUID.randomUUID().toString();
                company.setCompanyID(companyid);
            }
            if (requestParams.containsKey("creator") && requestParams.get("creator") != null) {
                String creatorid = requestParams.get("creator").toString();
                user = (User) get(User.class, creatorid);
                if(user == null){
                    user = new User();
                    user.setUserID(creatorid);
                }
                company.setCreator(user);
            }
            if (requestParams.containsKey("country") && requestParams.get("country") != null) {
                String country = requestParams.get("country").toString();
                company.setCountry((Country) get(Country.class, country));
            }
            if (requestParams.containsKey("timezone") && requestParams.get("timezone") != null) {
                String timezone = requestParams.get("timezone").toString();
                company.setTimeZone((KWLTimeZone) get(KWLTimeZone.class, timezone));
            }
            if (requestParams.containsKey("currency") && requestParams.get("currency") != null) {
                String currency = requestParams.get("currency").toString();
                company.setCurrency((KWLCurrency) get(KWLCurrency.class, currency));
            }
            if (requestParams.containsKey("address") && requestParams.get("address") != null) {
                String address = requestParams.get("address").toString();
                company.setAddress(address);
            }
            if (requestParams.containsKey("deleted") && requestParams.get("deleted") != null) {
                Integer deleted = (Integer) requestParams.get("deleted");
                company.setDeleted(deleted);
            }
            if (requestParams.containsKey("createdon") && requestParams.get("createdon") != null) {
                Date createdon = (Date) requestParams.get("createdon");
                company.setCreatedOn(createdon);
            }
            if (requestParams.containsKey("modifiedon") && requestParams.get("modifiedon") != null) {
                Date modifiedon = (Date) requestParams.get("modifiedon");
                company.setModifiedOn(modifiedon);
            }
            if (requestParams.containsKey("subdomain") && requestParams.get("subdomain") != null) {
                String subdomain = requestParams.get("subdomain").toString();
                company.setSubDomain(subdomain);
            }
            if (requestParams.containsKey("companyname") && requestParams.get("companyname") != null) {
                String companyname = requestParams.get("companyname").toString();
                company.setCompanyName(companyname);
            }
            if (requestParams.containsKey("emailid") && requestParams.get("emailid") != null) {
                String emailid = requestParams.get("emailid").toString();
                company.setEmailID(emailid);
            }
            if (requestParams.containsKey("isactivated") && requestParams.get("isactivated") != null) {
                boolean isActivated = (Boolean)requestParams.get("isactivated");
                company.setActivated(isActivated);
            }
            if (requestParams.containsKey("switchpref") && requestParams.get("switchpref") != null) {
                int switchpref = (Integer) requestParams.get("switchpref");
                company.setSwitchpref(switchpref);
            }
            if (requestParams.containsKey("storeinvoiceamountdue") && requestParams.get("storeinvoiceamountdue") != null) {
                boolean storeinvoiceamountdue = (Boolean) requestParams.get("storeinvoiceamountdue");
                company.setStoreinvoiceamountdue(storeinvoiceamountdue);
            }
            if (requestParams.containsKey("referralkey") && requestParams.get("referralkey") != null) {
                int referralkey = (Integer) requestParams.get("referralkey");
                company.setReferralkey(referralkey);
            }
            if (requestParams.containsKey("companylogo") && requestParams.get("companylogo") != null) {
                String image = requestParams.get("companylogo").toString();
                company.setCompanyLogo(image);
            }
            if (requestParams.containsKey("city") && requestParams.get("city") != null) {
                String city = requestParams.get("city").toString();
                company.setCity(city);
            }
            if (requestParams.containsKey("state") && !StringUtil.isNullOrEmpty((String)requestParams.get("state"))) {
                String state = requestParams.get("state").toString();
                company.setState((State) get(State.class, state));
            }
            if (requestParams.containsKey("phone") && requestParams.get("phone") != null) {
                String phone = requestParams.get("phone").toString();
                company.setPhoneNumber(phone);
            }
            if (requestParams.containsKey("fax") && requestParams.get("fax") != null) {
                String fax = requestParams.get("fax").toString();
                company.setFaxNumber(fax);
            }
            if (requestParams.containsKey("zip") && requestParams.get("zip") != null) {
                String zip = requestParams.get("zip").toString();
                company.setZipCode(zip);
            }
            if (requestParams.containsKey("website") && requestParams.get("website") != null) {
                String website = requestParams.get("website").toString();
                company.setCompanyLogo(website);
            }
            saveOrUpdate(company);
            list.add(company);
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.addCompany", e);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
//    public void addUserLogin(HashMap<String, Object> requestParams) throws ServiceException {
//        String userLoginId = "";
//        UserLogin userLogin = null;
//        try {
//            if (requestParams.containsKey("userloginid") && requestParams.get("userloginid") != null) {
//                userLoginId = requestParams.get("userloginid").toString();
//                userLogin = (UserLogin) get(UserLogin.class, userLoginId);
//                if(userLogin == null){
//                    userLogin = new UserLogin();
//                    userLogin.setUserID(userLoginId);
//                }
//            } else {
//                userLogin = new UserLogin();
//            }
//            userLogin.setLastActivityDate(new Date());
//            if (requestParams.containsKey("userName") && requestParams.get("userName") != null) {
//                String userName = requestParams.get("userName").toString();
//                userLogin.setUserName(userName);
//            }
//            if (requestParams.containsKey("password") && requestParams.get("password") != null) {
//                String password = requestParams.get("password").toString();
//                userLogin.setPassword(password);
//            }
//            if (requestParams.containsKey("saveStandAloneUserLogin") && requestParams.get("saveStandAloneUserLogin") != null && Boolean.parseBoolean(requestParams.get("saveStandAloneUserLogin").toString())) {
//                if (requestParams.containsKey("user") && requestParams.get("user") != null) {
//                    userLogin.setUser((User) requestParams.get("user"));
//                }
//            }
//            save(userLogin);
//          
//        } catch (Exception e) {
//            throw ServiceException.FAILURE("profileHandlerDAOImpl.saveUserLogin", e);
//        }
//    }
//    
    @Override
    public KwlReturnObject updateCompany(HashMap<String, Object> requestParams) throws ServiceException {
        Company company = null;
        UserLogin ul=null;
        boolean userLoginFlag=false;
        List list = new ArrayList();
        try {
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                String id = requestParams.get("companyid").toString();
                company = (Company) get(Company.class, id);
            } else {
                company = new Company();
                String companyid = UUID.randomUUID().toString();
                company.setCompanyID(companyid);
            }
            if (requestParams.containsKey("creator") && requestParams.get("creator") != null) {
                String creatorid = requestParams.get("creator").toString();
                company.setCreator((User) get(User.class, creatorid));
            }
            if (requestParams.containsKey("country") && requestParams.get("country") != null) {
                String country = requestParams.get("country").toString();
                company.setCountry((Country) get(Country.class, country));
            }
            if (requestParams.containsKey("timezone") && requestParams.get("timezone") != null) {
                String timezone = requestParams.get("timezone").toString();
                company.setTimeZone((KWLTimeZone) get(KWLTimeZone.class, timezone));
            }
            if (requestParams.containsKey("currency") && requestParams.get("currency") != null) {
                String currency = requestParams.get("currency").toString();
                company.setCurrency((KWLCurrency) get(KWLCurrency.class, currency));
            }
            if (requestParams.containsKey("address") && requestParams.get("address") != null) {
                String address = requestParams.get("address").toString();
                company.setAddress(address);
            }
            if (requestParams.containsKey("deleted") && requestParams.get("deleted") != null) {
                Integer deleted = (Integer) requestParams.get("deleted");
                company.setDeleted(deleted);
            }
            if (requestParams.containsKey("createdon") && requestParams.get("createdon") != null) {
                Date createdon = (Date) requestParams.get("createdon");
                company.setCreatedOn(createdon);
            }
            if (requestParams.containsKey("modifiedon") && requestParams.get("modifiedon") != null) {
                Date modifiedon = (Date) requestParams.get("modifiedon");
                company.setModifiedOn(modifiedon);
            }
            if (requestParams.containsKey("subdomain") && requestParams.get("subdomain") != null) {
                String subdomain = requestParams.get("subdomain").toString();
                company.setSubDomain(subdomain);
            }
            if (requestParams.containsKey("companyname") && requestParams.get("companyname") != null) {
                String companyname = requestParams.get("companyname").toString();
                company.setCompanyName(companyname);
            }
            if (requestParams.containsKey("emailid") && requestParams.get("emailid") != null) {
                String emailid = requestParams.get("emailid").toString();
                company.setEmailID(emailid);
            }
            if (requestParams.containsKey("isactivated") && requestParams.get("isactivated") != null) {
                boolean isActivated = (Boolean)requestParams.get("isactivated");
                company.setActivated(isActivated);
            }
            if (requestParams.containsKey("switchpref") && requestParams.get("switchpref") != null) {
                int switchpref = (Integer) requestParams.get("switchpref");
                company.setSwitchpref(switchpref);
            }
            if (requestParams.containsKey("storeinvoiceamountdue") && requestParams.get("storeinvoiceamountdue") != null) {
                boolean storeinvoiceamountdue = (Boolean) requestParams.get("storeinvoiceamountdue");
                company.setStoreinvoiceamountdue(storeinvoiceamountdue);
            }
            if (requestParams.containsKey("referralkey") && requestParams.get("referralkey") != null) {
                int referralkey = (Integer) requestParams.get("referralkey");
                company.setReferralkey(referralkey);
            }
            if (requestParams.containsKey("smtpflow") && requestParams.get("smtpflow") != null) {
                int smtpflow = Integer.parseInt(requestParams.get("smtpflow").toString());
                company.setSmtpflow(smtpflow);
            }
            if (requestParams.containsKey("smtppassword") && requestParams.get("smtppassword") != null) {
                String smtppassword = (String) requestParams.get("smtppassword");
                company.setSmtppassword(smtppassword);
            }
            if (requestParams.containsKey("mailserveraddress") && requestParams.get("mailserveraddress") != null) {
                String mailserveraddress = (String) requestParams.get("mailserveraddress");
                company.setMailserveraddress(mailserveraddress);
            }
            if (requestParams.containsKey("mailserverport") && requestParams.get("mailserverport") != null) {
                String mailserverport = (String) requestParams.get("mailserverport");
                company.setMailserverport(mailserverport);
            }
            if (requestParams.containsKey("city") && requestParams.get("city") != null) {
                String city = (String) requestParams.get("city");
                company.setCity(city);
            }
            if (requestParams.containsKey("state") && !StringUtil.isNullOrEmpty((String)requestParams.get("state"))) {
                String state = (String) requestParams.get("state");
                Object o = get(State.class, state);
                company.setState(o instanceof State ? (State) o : null);
            }
            if (requestParams.containsKey("phone") && requestParams.get("phone") != null) {
                String phone = (String) requestParams.get("phone");
                company.setPhoneNumber(phone);
            }
            if (requestParams.containsKey("fax") && requestParams.get("fax") != null) {
                String fax = (String) requestParams.get("fax");
                company.setFaxNumber(fax);
            }
            if (requestParams.containsKey("zip") && requestParams.get("zip") != null) {
                String zip = (String) requestParams.get("zip");
                company.setZipCode(zip);
            }
            if (requestParams.containsKey("website") && requestParams.get("website") != null) {
                String website = (String) requestParams.get("website");
                company.setWebsite(website);
            }
            saveOrUpdate(company);
            list.add(company);
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.saveUser", e);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
     /**
     * Description : This Method is used to rollback the all Default Company SetUp Data
     * @param <requestParams> used to get request parameters
     * @return :void
     */
    @Override
    public void deleteCompanySetUpData(Map<String, Object> requestParams) throws ServiceException {
       
        int totalCount=0;
        String mysqlQuery ="";
        try {
            String companyid = (String) requestParams.get("companyid");
                
            mysqlQuery="delete from in_storemaster  where company='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from  in_location where company='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from paymentmethod  where company='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from compaccpreferences where id='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from taxlist where company='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from tax where company='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            /**
             * Delete GST Rules
             * ERP-35391
             */
            mysqlQuery="delete from prodcategorygstmapping where entitytermrate in "
                    + " (select id from entitybasedlineleveltermsrate where linelevelterms in "
                    + " (select id from linelevelterms where company='" + companyid + "'))";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from entitybasedlineleveltermsrate where linelevelterms in "
                    + " (select id from linelevelterms where company='" + companyid + "')";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from linelevelterms where company='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from account where company='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from accgroup where company='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from sequenceformat where company='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from tax1099category  where company='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from  exchangeratedetails where company='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            /*
            *Delete Dimension and Masters Data
            */
            mysqlQuery="delete from fieldcombodata where fieldid in(select id from  fieldparams where companyid='" + companyid + "')";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from addressfielddimensionmapping where company='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from  fieldparams where companyid='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from  masteritem where company='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from extracompanypreferences  where id='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from indiacompliancecompanypreferences  where id='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
            mysqlQuery="delete from mrpcompanypreferences  where id='" + companyid + "'";
            totalCount = executeSQLUpdate(mysqlQuery);
            
        } catch (Exception ex) {
            Logger.getLogger(profileHandlerDAOImpl.class.getName()).log(Level.SEVERE, "Exception in deleting the company setup data", ex);
        }
        
    }

    public void deleteUser(String id) throws ServiceException {
        try {
            User u = (User) load(User.class, id);
            if (u.getUserID().equals(u.getCompany().getCreator().getUserID())) {
                throw new Exception("Cannot delete Company Administrator");
            }
            
            
            /**
             * After disabling the user, deleting the entries related to the
             * user in role user mapping and userpermission table to free up the
             * space in db
             */
            String deleteUserPermission = "delete u from userpermission u inner join role_user_mapping rum on rum.id = u.roleUserMapping where rum.userId='" + id + "'";
            executeSQLUpdate(deleteUserPermission);

            String deleteRoleUserMapping = "delete from role_user_mapping where userId='" + id + "'";
            executeSQLUpdate(deleteRoleUserMapping);

            UserLogin userLogin = (UserLogin) load(UserLogin.class, id);
            /* 'User' is used in many tables as a foreign key or normal string. Due to high dependency of multiple table on 'users' table,
             * we have set delete flag = 1 for user instead of deleting them from database. 
             * Also, to allow end user to create new user with same name and emal id, we are changing the username and emailid in following way--
             */
            String userName = userLogin.getUserName();
            String emailId= u.getEmailID();
            
            u.setEmailID(emailId+"_del");
            userLogin.setUserName(userName+"_del");
            u.setDeleteflag(1);
            
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.deleteUser", e);
        }
    }

    public void saveUserLogin(HashMap<String, Object> requestParams) throws ServiceException {
        String userLoginId = "";
        UserLogin userLogin = null;
        try {
            if (requestParams.containsKey("userloginid") && requestParams.get("userloginid") != null) {
                userLoginId = requestParams.get("userloginid").toString();
                userLogin = (UserLogin) load(UserLogin.class, userLoginId);
            } else {
                userLogin = new UserLogin();
            }
            if (requestParams.containsKey("lastlogindate") && requestParams.get("lastlogindate") != null) {
                Date lastlogindate = (Date)requestParams.get("lastlogindate");
                userLogin.setLastActivityDate(lastlogindate);
            }
            if (requestParams.containsKey("userName") && requestParams.get("userName") != null) {
                String userName = requestParams.get("userName").toString();
                userLogin.setUserName(userName);
            }
            if (requestParams.containsKey("password") && requestParams.get("password") != null) {
                String password = requestParams.get("password").toString();
                userLogin.setPassword(password);
            }
            if (requestParams.containsKey("saveStandAloneUserLogin") && requestParams.get("saveStandAloneUserLogin") != null && Boolean.parseBoolean(requestParams.get("saveStandAloneUserLogin").toString())) {
                if (requestParams.containsKey("user") && requestParams.get("user") != null) {
                    userLogin.setUser((User) requestParams.get("user"));
                }
            }
            save(userLogin);
          
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.saveUserLogin", e);
        }
    }

    public void saveUserLastLogin(HashMap<String, Object> requestParams) throws ServiceException {
        String userLoginId = "";
        UserLogin userLogin = null;
        try {
            if (requestParams.containsKey("userid") && requestParams.get("userid") != null) {
                userLoginId = requestParams.get("userid").toString();
                userLogin = (UserLogin) load(UserLogin.class, userLoginId);
                //Do not convert new Date() to UTC
                userLogin.setLastActivityDate(new Date());
                update(userLogin);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.saveUserLogin", e);
        }
    }

    public KwlReturnObject changePassword(HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String password = "";
        String userid = "";
        List ll = new ArrayList();
        int dl = 0;
        try {
            if (requestParams.containsKey("currentpassword") && requestParams.get("currentpassword") != null) {
                password = requestParams.get("currentpassword").toString();
            }
            if (requestParams.containsKey("userid") && requestParams.get("userid") != null) {
                userid = requestParams.get("userid").toString();
            }
            jobj.put("remoteapikey", storageHandlerImpl.GetRemoteAPIKey());
            if (password == null || password.length() <= 0) {
                jobj.put("msg", "Invalid Password");
            } else {
                User user = (User) load(User.class, userid);
                UserLogin userLogin = user.getUserLogin();
                String currentpass = userLogin.getPassword();
                if (StringUtil.equal(password, currentpass)) {
                    userLogin.setPassword(password);
                    saveOrUpdate(userLogin);
                    jobj.put("msg", "New Password has been successfully set");
                    jobj.put("tf", true);
                } else {
                    jobj.put("msg", "Please enter current password ");
                    jobj.put("tf", false);
                }
            }
            ll.add(jobj);
        } catch (Exception e) {
            throw ServiceException.FAILURE("ProfileHandler.changePassword", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject changeUserPassword(String platformURL, HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        List ll = new ArrayList();
        int dl = 0;
        boolean success = false;
        try {
            String password = (String) requestParams.get("currentpassword");
            String newpassword = (String) requestParams.get("changepassword");
            String userid = (String) requestParams.get("userid");
            String companyid = (String) requestParams.get("companyid");
            String remoteapikey = (String) requestParams.get("remoteapikey");

            if (password == null || password.length() <= 0) {
                msg = "Invalid Password";
            } else {
                if (!StringUtil.isNullOrEmpty(platformURL) && !Boolean.parseBoolean(StorageHandler.getStandalone())) {
                    JSONObject userData = new JSONObject();
                    userData.put("pwd", newpassword);
                    userData.put("oldpwd", password);
                    userData.put("userid", userid);
                    userData.put("remoteapikey", remoteapikey);
                    String action = "3";
                    JSONObject resObj = apiCallHandlerService.restPostMethod(platformURL, userData.toString());
//                    JSONObject resObj = apiCallHandlerService.callApp( platformURL, userData, companyid, action);
                    if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                        User user = (User) load(User.class, userid);
                        UserLogin userLogin = user.getUserLogin();
                        userLogin.setPassword(newpassword);
                        saveOrUpdate(userLogin);
                        msg = "New Password has been successfully set";
                        success = true;
                    } else {
                        /**
                         * To get the error message sent from Apps.
                         * ERP-41621.
                         */
                        if (!StringUtil.isNullOrEmpty(resObj.optString("message"))) {
                            msg = resObj.optString("message");
                        } else {
                            msg = "Error in changing Password";
                        }
                    }
                } else {
                    User user = (User) load(User.class, userid);
                    UserLogin userLogin = user.getUserLogin();
                    String currentpass = userLogin.getPassword();
                    if (StringUtil.equal(password, currentpass)) {
                        userLogin.setPassword(newpassword);
                        saveOrUpdate(userLogin);
                        msg = "New Password has been successfully set";
                        success = true;
                    } else {
                        /**
                         * As per comment in SDP-16201 message is changed 
                         */
                        msg = "The Current Password you entered is incorrect.";
                    }
                }
            }
            jobj.put("success", success);
            jobj.put("msg", msg);
            ll.add(jobj);
        } catch (Exception e) {
            throw ServiceException.FAILURE("ProfileHandler.changeUserPassword", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public String getUser_hash(String userid) throws ServiceException {
        String res = "";
        try {
            JSONObject resObj = new JSONObject();
            User user = (User) load(User.class, userid);
            resObj.put("userhash", user.getUser_hash());
            resObj.put("subdomain", user.getCompany().getSubDomain());
            res = resObj.toString();
        } catch (Exception e) {
            throw ServiceException.FAILURE("authHandlerDAOImpl.getUser_hash", e);
        }
        return res;
    }

    public boolean checkDuplicateEmailID(String emailid, String companyid, String userid) throws ServiceException {
        boolean emailExist = false;
        List ll = new ArrayList();
        try {
            String SELECT_USER_INFO = "select u.emailID from User as u where u.emailID = ?  and u.deleteflag=0 and u.company.companyID=? and u.userID != ?";
            ll = executeQuery( SELECT_USER_INFO, new Object[]{emailid, companyid, userid});
            if (ll.size() > 0) {
                emailExist = true;
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.checkDuplicateEmailID", e);
        }
        return emailExist;
    }
    
    @Override
    public boolean isUserExist(String username, String companyid)
            throws ServiceException {
        String hql = "select count(*) from UserLogin ul where ul.userName=? and ul.user.company.companyID = ?";
        List l = executeQuery(hql, new Object[]{username, companyid});
        if (Integer.parseInt(l.get(0).toString()) == 0) {
            return false;
        } else {
            return true;
        }
    }
    
    @Override
     public KwlReturnObject getUserWithUserName(String userid, boolean isUserName) throws ServiceException {
        int count = 0;
        String query = "";
        if (!isUserName) {
            query = "from UserLogin u where u.userID=?";
        } else {
            query = "from UserLogin u where u.userName=?";
        }
        List list = executeQuery(query, userid);
        return new KwlReturnObject(true, "", "", list, list.size());
    }

    @Override
    public List getUserExistWithUserID(String userid) throws ServiceException {
        List list = new ArrayList();
        String query = "from User where userID=?";
        List usl = executeQuery(query, new Object[]{userid});
        return list;
    }
    
    /**
     * Description : This Method is used to check the transaction is created or not in System
     * @param <companyid> used to get companyid parameters
     * @return :boolean
     */
    @Override
    public boolean isTransactionCreated(String companyid) throws ServiceException {
        List list = null;
        int totalCount = 0;
        boolean isTransactionMade = false;
        try {
            String mysqlQuery = "select inv.id from invoice inv inner join journalentry je on inv.journalentry=je.id where inv.company='" + companyid + "'"
                    + " UNION select so.id from salesorder so  where so.company='" + companyid + "'"
                    + " UNION select dor.id from deliveryorder dor  where  dor.company='" + companyid + "'"
                    + " UNION select cn.id from creditnote cn inner join journalentry je on cn.journalentry=je.id where cn.company='" + companyid + "'"
                    + " UNION select sr.id from salesreturn sr  where  sr.company='" + companyid + "'"
                    + " UNION select rp.id from receipt rp inner join journalentry je on rp.journalentry=je.id where rp.company='" + companyid + "'"
                    + " UNION select qo.id from quotation qo  where  qo.company='" + companyid + "'"
                    + " UNION select gr.id from goodsreceipt gr inner join journalentry je on gr.journalentry=je.id where  gr.company='" + companyid + "'"
                    + " UNION select po.id from purchaseorder po  where  po.company='" + companyid + "'"
                    + " UNION select gro.id from grorder gro  where  gro.company='" + companyid + "'"
                    + " UNION select dn.id from debitnote dn inner join journalentry je on dn.journalentry=je.id where dn.company='" + companyid + "'"
                    + " UNION select pr.id from purchasereturn pr  where  pr.company='" + companyid + "'"
                    + " UNION select mp.id from payment mp inner join journalentry je on mp.journalentry=je.id where mp.company='" + companyid + "'"
                    + " UNION select vq.id from vendorquotation vq  where vq.company='" + companyid + "'"
                    + " UNION select cust.id from customer cust  where cust.company='" + companyid + "'"
                    + " UNION select ven.id from vendor ven  where ven.company='" + companyid + "'"
                    + " UNION select p.id from product p  where p.company='" + companyid + "'";

            list = executeSQLQuery(mysqlQuery);
            totalCount = list.size();
            if (totalCount > 0) {
                isTransactionMade = true;
            }
        } catch (Exception ex) {
            Logger.getLogger(profileHandlerDAOImpl.class.getName()).log(Level.SEVERE, "Exception when checking the transcation is created or not  ", ex);
        }
        return isTransactionMade;
    }
   
    @Override
    public KwlReturnObject saveRoleUserMapping(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        RoleUserMapping rum= new RoleUserMapping();
        User user=null;
        try {
            if (requestParams.containsKey("userid") && requestParams.get("userid") != null) {
                String id = requestParams.get("userid").toString();
                user = (User) load(User.class, id);
                rum.setUserId(user);
            } else {
                if (requestParams.containsKey("user") && requestParams.get("user") != null) {
                    user = (User) requestParams.get("user");
                    rum.setUserId(user);
                }
            }
        
        if(requestParams.containsKey("roleid") && requestParams.get("roleid")!=null){
            String roleid= requestParams.get("roleid").toString();
            rum.setRoleId((Rolelist) load(Rolelist.class, roleid));
        } 
        save(rum);
        list.add(rum);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.saveRoleUserMapping", ex);
        }
    return new KwlReturnObject(true, "", "", list, list.size());
    }

    @Override
    public KwlReturnObject resetUserPassword(String userId, String newPassword) throws ServiceException {
        try {
            User user = (User) load(User.class, userId);
            UserLogin userLogin = user.getUserLogin();
            if(!StringUtil.isNullOrEmpty(newPassword)){
                userLogin.setPassword(newPassword);
}
            saveOrUpdate(userLogin);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.resetUserPassword", ex);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", null, 0);

    }
      public KwlReturnObject setUserDepartment(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        String department = "";
        User user=null;
        try {
            if (requestParams.containsKey("userID") && requestParams.get("userID") != null) {
                String id = requestParams.get("userID").toString();
                user = (User) load(User.class, id);  
            }  
        
        if(requestParams.containsKey("department") && requestParams.get("department")!=null){
                department = requestParams.get("department").toString();            
        } if(department==""){
            user.setDepartment(null);
        }else{
            user.setDepartment(department);
        }
        saveOrUpdate(user);
        list.add(user);
        
        } catch (Exception ex) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.setUserDepartment", ex);
        }
    return new KwlReturnObject(true,KwlReturnMsg.S01, "", list, list.size());
    }
       public boolean checkIsUserApprover(String userId) throws ServiceException{
        boolean isApprover=false;
        List list = new ArrayList();
        try {
            String query = "select id from multilevelapprovalruletargetusers where userid=?";
            ArrayList params = new ArrayList();
            params.add(userId);
            list = executeSQLQuery(query,params.toArray());
            if(list.size()>0 && list!=null)
            isApprover=true;
            else
            isApprover=false;
            return isApprover;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("checkIsUserApprover : " + ex.getMessage(), ex);
        }
        
       }
       

    @Override
    public String getUserIdFromUserName(String userName, String companyId) throws ServiceException {
        String userId = "";
        List ll = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(userName);
            params.add(companyId);
            String Hql = "select user.userID from User user, UserLogin userlogin where user.userID = userlogin.userID and userlogin.userName = ? and user.company.companyID = ?";
            ll = executeQuery(Hql, params.toArray());
            Iterator ite = ll.iterator();
            if (ite.hasNext()) {
                userId = (String) ite.next();
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.getUserIdFromUserName", e);
        }
        return userId;
    }
    /**
     * @Desc : Get User Group Name in which user exist
     * @param map
     * @return
     * @throws ServiceException
     */
    public String getUserGroupForUser(Map<String, String> map) throws ServiceException {
        StringBuilder grpName = new StringBuilder();
        String returnName = "";
        List ll = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(map.get("userid"));
            String Hql = "select ug.Name from UsersGroupMapping ugm inner join ugm.usersGroup ug where ugm.user.userID=?";
            ll = executeQuery(Hql, params.toArray());
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                grpName.append((String) ite.next());
                grpName.append(",");
            }
            returnName = grpName.length() > 1 ? grpName.substring(0, grpName.length() - 1) : "";
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandlerDAOImpl.getUserIdFromUserName", e);
        }
        return returnName;
    }
    
    @SuppressWarnings("finally")
    public String getSysEmailIdByCompanyID(String companyid) {
        String emailId = "admin@deskera.com";
        try {
            Company company = (Company) get(Company.class, companyid);
            if (company != null) {
                emailId = company.getEmailID();
                if (StringUtil.isNullOrEmpty(emailId)) {
                emailId = "admin@deskera.com";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return emailId;
        }
    }    
    
    public String getStateidByStateName(String statename) {
        String stateid = "";
        List ll = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(statename);
            String Hql = "select id from state where statename = ?";
            ll = executeSQLQuery(Hql, params.toArray());
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                stateid = (String) ite.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return stateid;
        }
    }

    @Override
    public void saveOrUpdatePasswordPolicy(JSONObject jobj) throws ServiceException {
        try {
            String policyid = jobj.optString("policyid");
            PasswordPolicy passwordPolicy = (PasswordPolicy) get(PasswordPolicy.class, policyid);
            if (passwordPolicy == null) {
                passwordPolicy = new PasswordPolicy();
                passwordPolicy.setPolicyid(jobj.optString("policyid"));
            }
            passwordPolicy.setCompanyid((Company) get(Company.class, jobj.optString(Constants.companyKey)));
            passwordPolicy.setDefpass(jobj.optString("defpass"));
            passwordPolicy.setMaxchar(jobj.optInt("maxchar"));
            passwordPolicy.setMinalphabet(jobj.optInt("minalphabet"));
            passwordPolicy.setMinchar(jobj.optInt("minchar"));
            passwordPolicy.setMinnum(jobj.optInt("minnum"));
            passwordPolicy.setPpass(jobj.optString("ppass"));
            passwordPolicy.setSetpolicy(jobj.optInt("setpolicy"));
            passwordPolicy.setSpecialchar(jobj.optInt("specialchar"));
            saveOrUpdate(passwordPolicy);
        } catch (ServiceException ex) {
            Logger.getLogger(profileHandlerDAOImpl.class.getName()).log(Level.SEVERE, "Exception in saving/updating password policy.", ex);
            throw ex;
        }
    }

    @Override
    public KwlReturnObject getPasswordPolicy(String companyid) throws ServiceException {
        String hqlQuery = "from PasswordPolicy where companyid.companyID = ? ";
        List paramsList = new ArrayList();
        paramsList.add(companyid);
        List list = executeQuery(hqlQuery, paramsList.toArray());
        return new KwlReturnObject(true, "", null, list, 0);
    }
    
    /**
     * To delete password policy against company.
     * @param jobj
     * @return
     * @throws ServiceException 
     */
    @Override
    public int deletePasswordPolicy(JSONObject jobj) throws ServiceException {
        int count = 0;
        try {
            String hqlQuery = "delete from PasswordPolicy where companyid.companyID = ?";
            count = executeUpdate(hqlQuery, jobj.optString(Constants.companyKey));
        } catch (ServiceException ex) {
            Logger.getLogger(profileHandlerDAOImpl.class.getName()).log(Level.SEVERE, "Exception in deleting password policy.", ex);
            throw ex;
        }
        return count;
    } 
}
