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
package com.krawler.spring.authHandler;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Karthik
 */
public class authHandlerDAOImpl extends BaseDAO implements authHandlerDAO {

    private sessionHandlerImpl sessionHandlerImplObj;

    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }

    public KwlReturnObject verifyLogin(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        String username = "";
        String userid = "";
        String passwd = "";
        String subdomain = "";
        boolean successflag = false;
        boolean usernameflag = false;
        boolean subdomainflag = false;
        try {
            List params = new ArrayList();
            String filterString = "";

            if (requestParams.containsKey("user") && !StringUtil.isNullOrEmpty(requestParams.get("user").toString())) {
                username = requestParams.get("user").toString();
                params.add(username);
                filterString += " and u.userLogin.userName = ?";
                usernameflag = true;
            }
            if (requestParams.containsKey("userid") && !StringUtil.isNullOrEmpty(requestParams.get("userid").toString())) {
                userid = requestParams.get("userid").toString();
                params.add(userid);
                filterString += " and u.userID = ?";
                usernameflag = true;
                subdomainflag = true;
            }
            if (requestParams.containsKey("pass") && !StringUtil.isNullOrEmpty(requestParams.get("pass").toString())) {
                passwd = requestParams.get("pass").toString();
                params.add(passwd);
                filterString += " and u.userLogin.password = ?";
            }
            if (requestParams.containsKey("subdomain") && !StringUtil.isNullOrEmpty(requestParams.get("subdomain").toString())) {
                subdomain = requestParams.get("subdomain").toString();
                params.add(subdomain);
                filterString += " and u.company.subDomain=?";
                subdomainflag = true;
            }
            if (usernameflag && subdomainflag) {
                String Hql = "select u, u.userLogin, u.company from User as u where u.company.deleted=0 and u.deleteflag = 0 and u.company.activated = true " + filterString;
                ll = executeQuery(Hql, params.toArray());
                dl = ll.size();
                if (dl > 0) {
                    successflag = true;
                }
                KWLTimeZone timeZone = (KWLTimeZone) load(KWLTimeZone.class, "1");
                KWLDateFormat dateFormat = (KWLDateFormat) load(KWLDateFormat.class, "1");
                KWLCurrency currency = (KWLCurrency) load(KWLCurrency.class, "1");

                List<RoleUserMapping> roleUserMappingList = new ArrayList<RoleUserMapping>();
                if (!ll.isEmpty()) {
                    Object[] objects = (Object[]) ll.get(0);
                    User user = (User) objects[0];
                    roleUserMappingList = find("from RoleUserMapping where userId.userID='" + user.getUserID() + "'");
                }
                ll.add(timeZone);
                ll.add(dateFormat);
                ll.add(currency);
                if (!roleUserMappingList.isEmpty()) {
                    ll.add(roleUserMappingList.get(0));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw ServiceException.FAILURE("authHandlerDAOImpl.verifyLogin", e);
        }
        return new KwlReturnObject(successflag, successflag ? KwlReturnMsg.S01 : KwlReturnMsg.F01, "", ll, ll.size());
    }

    public KwlReturnObject getPreferences(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = new ArrayList();
        int dl = 0;
        String timeZoneId = "";
        String dateFormatId = "";
        String currencyId = "";
        try {
            if (requestParams.containsKey("timezoneid") && !StringUtil.isNullOrEmpty(requestParams.get("timezoneid").toString())) {
                timeZoneId = requestParams.get("timezoneid").toString();
            }
            if (requestParams.containsKey("dateformatid") && !StringUtil.isNullOrEmpty(requestParams.get("dateformatid").toString())) {
                dateFormatId = requestParams.get("dateformatid").toString();
            }
            if (requestParams.containsKey("currencyid") && !StringUtil.isNullOrEmpty(requestParams.get("currencyid").toString())) {
                currencyId = requestParams.get("currencyid").toString();
            }

            KWLTimeZone timeZone = (KWLTimeZone) load(KWLTimeZone.class, timeZoneId);
            KWLDateFormat dateFormat = (KWLDateFormat) load(KWLDateFormat.class, dateFormatId);
            KWLCurrency currency = (KWLCurrency) load(KWLCurrency.class, currencyId);

            ll.add(timeZone);
            ll.add(dateFormat);
            ll.add(currency);
        } catch (Exception e) {
            throw ServiceException.FAILURE("authHandlerDAOImpl.getPreferences", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public DateFormat getUserDateFormatter(String formatid, String diff, boolean onlydate) {
        KWLDateFormat df = (KWLDateFormat) get(KWLDateFormat.class, formatid);
        String format = df.getJavaForm();
        int pos = format.length();
        if (onlydate) {
            pos = df.getJavaSeperatorPosition();
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format.substring(0, pos));
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT" + diff));
        return sdf;
    }

    @Override
    @Deprecated
    public String getFormattedCurrency(double value, String currencyid) {
        DecimalFormat df = null;
        String str = authHandler.getCompleteDFStringForAmount("#,##0.");
        df = new DecimalFormat(str);
        KWLCurrency c = (KWLCurrency) get(KWLCurrency.class, currencyid);
        String sym;
        try {
            sym = new Character((char) Integer.parseInt(c.getHtmlcode(), 16)).toString();
        } catch (Exception e) {
            sym = c.getHtmlcode();
        }
        return sym + " " + df.format(value);
    }
    
    @Override
    public String getFormattedCurrency(double value, String currencyid, String companyid) {
        DecimalFormat df = null;
        String str = authHandler.getCompleteDFStringForAmount("#,##0.", companyid);
        df = new DecimalFormat(str);
        KWLCurrency c = (KWLCurrency) get(KWLCurrency.class, currencyid);
        String sym;
        try {
            sym = new Character((char) Integer.parseInt(c.getHtmlcode(), 16)).toString();
        } catch (Exception e) {
            sym = c.getHtmlcode();
        }
        return sym + " " + df.format(value);
    }

    public String getFormattedCurrency(double value, String currencyid,boolean isCurrencyCode) {
        DecimalFormat df = null;
        String str = authHandler.getCompleteDFStringForAmount("#,##0.");
        df = new DecimalFormat(str);
        KWLCurrency c = (KWLCurrency) get(KWLCurrency.class, currencyid);
        String sym;
        try {
            if (isCurrencyCode && currencyid.equals("1")) {
                sym = c.getCurrencyCode();
            }
            else if (currencyid.equals("10")) {
                sym = c.getHtmlcode();
            }
            else {
                sym = new Character((char) Integer.parseInt(c.getHtmlcode(), 16)).toString();
            }
        } catch (Exception e) {
            sym = c.getHtmlcode();
        }
        return sym + " " + df.format(value);
    }
    
    public String getFormattedCurrency(double value, String currencyid,boolean isCurrencyCode, String companyid) {
        DecimalFormat df = null;
        String str = authHandler.getCompleteDFStringForAmount("#,##0.");
        df = new DecimalFormat(str);
        KWLCurrency c = (KWLCurrency) get(KWLCurrency.class, currencyid);
        String sym;
        try {
            if (isCurrencyCode && currencyid.equals("1")) {
                sym = c.getCurrencyCode();
            }
            else if (currencyid.equals("10")) {
                sym = c.getHtmlcode();
            }
            else {
                sym = new Character((char) Integer.parseInt(c.getHtmlcode(), 16)).toString();
            }
        } catch (Exception e) {
            sym = c.getHtmlcode();
        }
        return sym + " " + df.format(value);
    }
    
    @Override
    public String getFormattedCurrencyWithSign(double value, String currencyid) {
        DecimalFormat df = null;
        String str = authHandler.getCompleteDFStringForAmount("#,##0.");
        df = new DecimalFormat(str);
        KWLCurrency c = (KWLCurrency) get(KWLCurrency.class, currencyid);
        String sym;
        try {
            sym = new Character((char) Integer.parseInt(c.getHtmlcode(), 16)).toString();
        } catch (Exception e) {
            sym = c.getHtmlcode();
        }

        String returnString = "";

        if (value > 0) {
            returnString = sym + " " + df.format(value);
        } else {
            value = value * -1;
            returnString = "(" + sym + " " + df.format(value) + ")";
        }

        return returnString;
    }

    public String getCurrency(String currencyid) {
        KWLCurrency c = (KWLCurrency) get(KWLCurrency.class, currencyid);
        String sym;
        try {
            sym = new Character((char) Integer.parseInt(c.getHtmlcode(), 16)).toString();
        } catch (Exception e) {
            sym = c.getHtmlcode();
        }
        return "(" + sym + ")";
    }

    public String getCurrency(String currencyid,boolean isCurrencyCode) {
        KWLCurrency c = (KWLCurrency) get(KWLCurrency.class, currencyid);
        String sym;
        try {
            if (isCurrencyCode && currencyid.equals("1")) {
                sym = c.getCurrencyCode();
            }
            else if(currencyid.equals("10")){
                sym = c.getHtmlcode();
            }
            else {
                sym = new Character((char) Integer.parseInt(c.getHtmlcode(), 16)).toString();
            }
        } catch (Exception e) {
            sym = c.getHtmlcode();
        }
        return "(" + sym + ")";
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

    public String getCompanyAddress(String companyid) {
        String address = "";
        try {
            Company company = (Company) get(Company.class, companyid);
            if (company != null) {
                String addrs = company.getAddress();
                if (!StringUtil.isNullOrEmpty(addrs)) {
                    address += ",\n" + addrs;
                }

                String city = company.getCity();
                if (!StringUtil.isNullOrEmpty(city)) {
                    address += ",\n" + city;
                }

                String state = company.getState() != null? company.getState().getStateName():"";
                if (!StringUtil.isNullOrEmpty(state)) {
                    address += ",\n" + state;
                }

                String country = company.getState() != null? company.getCountry().getCountryName():"";
                if (!StringUtil.isNullOrEmpty(country)) {
                    address += ",\n" + country;
                }

                String zip = company.getZipCode();
                if (!StringUtil.isNullOrEmpty(zip)) {
                    address += ",\n" + zip;
                }
            }
            if (address.startsWith(",")) {
                address = address.replaceFirst(",", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return address;
        }
    }
/*  ------Get Role ID by Role Name--------*/
    @Override
    public String getRoleIdByRoleName(HashMap<String, Object> requestParams) throws ServiceException {
        List params = new ArrayList();
        String hqlQuery = "select rol.roleid from Rolelist rol where rol.rolename=? and (rol.company is null or rol.company.companyID=? )";
        params.add(requestParams.get("roleName"));
        params.add(requestParams.get("companyid"));
        List ll = executeQuery(hqlQuery, params.toArray());
        return ll.size()>0 ? ll.get(0).toString():"";
}

    /*  ------Get Permission Group by Permission Name--------*/
    @Override
    public String gefeateatureIdByFeatureName(String permissionGroup) throws ServiceException {
        List params = new ArrayList();
        String hqlQuery = "select prjf.featureID from ProjectFeature prjf where prjf.displayFeatureName=?";
        params.add(permissionGroup);
        List ll = executeQuery(hqlQuery, params.toArray());
        return ll.size() > 0 ? ll.get(0).toString() : "";

    }

     /*  ------Get Permission ID against a Permission Group--------*/
    @Override
    public String getActivityIdByActivityName(String activityName, String featureID) throws ServiceException {
        List params = new ArrayList();
        String hqlQuery = "select pract.activityID from ProjectActivity pract where pract.displayActivityName=? and pract.feature.featureID=?";
        params.add(activityName);
        params.add(featureID);
        List ll = executeQuery(hqlQuery, params.toArray());
        return ll.size() > 0 ? ll.get(0).toString() : "";
    }
/*--------- Function to save Permission against each permission Group for a particular Role-----------*/
    public KwlReturnObject setRolePermissions(HashMap<String, Object> requestParams, String featureid, Long permissions) throws ServiceException {
        List ll = null;

        String permissionChangeDetailStr = "";

        int dl = 0;

        try {

            String roleId = requestParams.containsKey("roleid") && requestParams.get("roleid") != null ? requestParams.get("roleid").toString() : "";
            String companyId = requestParams.get("companyid").toString();
            Company company = (Company) get(Company.class, companyId);

            String rolename = requestParams.containsKey("rolename") && requestParams.get("rolename") != null ? requestParams.get("rolename").toString() : "";
            boolean isEdit = requestParams.containsKey("isEdit") && requestParams.get("isEdit") != null ? Boolean.parseBoolean(requestParams.get("isEdit").toString()) : false;


            Rolelist addrole  = (Rolelist) get(Rolelist.class, roleId);

            /*-----  If role is not Company Admin-----*/
            if (!(roleId.equals(Rolelist.COMPANY_ADMIN))) {

                ProjectFeature projFeature = (ProjectFeature) load(ProjectFeature.class, featureid);

                RolePermission permission = new RolePermission();
                permission.setRole(addrole);

                permission.setFeature(projFeature);
                permission.setPermissionCode(permissions);

                permission.setCompany(company);
                try {
                    if (isEdit) {
                        saveOrUpdate(permission);
                    } else {
                        save(permission);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new KwlReturnObject(true, permissionChangeDetailStr, "", ll, dl);
    }
    @Override
    public void checkLockDatePeroid(Date orderDate, String companyId) throws ServiceException,DataInvalidateException {
        List ll = new ArrayList();
        try {
            String sql = "select if(startdate <= ? and enddate >= ? ,'true','false') as isactive,startdate,enddate from accounitng_period_lock_info where company = ?  and peridclosed = 'T'";
            ll = executeSQLQuery( sql, new Object[]{orderDate,orderDate, companyId}); 
            if (!ll.isEmpty()) {
                for (Object Object : ll) {
                    Object objects[] = (Object[]) Object;;
                    if (Boolean.parseBoolean((String) objects[0])) {
                        throw new DataInvalidateException("Transaction date belongs to locked period period name <b>(" + (Date) objects[1] + "</b> to <b>" + (Date) objects[2] + ")</b> so it could not be saved. You can reopen the locked period <b>(" + (Date) objects[1] + "</b> to <b>" + (Date) objects[2] + ")</b> from Accounting Period settings and then proceed.");
                    }
                }
            }
        } catch (DataInvalidateException ex) {
            throw ex;
        } catch (Exception e) {
            Logger.getLogger(authHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("authHandlerDAOImpl.checkLockDatePeroid", e);
        }
    }
}
