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
package com.krawler.spring.companyDetails;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.FileUploadHandler;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.fileupload.FileItem;

/**
 *
 * @author Karthik
 */
public class companyDetailsDAOImpl extends BaseDAO implements companyDetailsDAO {

    private storageHandlerImpl storageHandlerImplObj;

    public void setstorageHandlerImpl(storageHandlerImpl storageHandlerImplObj1) {
        this.storageHandlerImplObj = storageHandlerImplObj1;
    }

    public KwlReturnObject getCompanyInformation(HashMap<String, Object> requestParams, ArrayList filter_names, ArrayList filter_params) throws ServiceException {
        List ll = null;
        int dl = 0;
        String companyid = "";
        try {
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = requestParams.get("companyid").toString();
            }
            String query = "from Company c ";
//            String query = "select c,cpr from CompanyPreferences c right outer join c.company cpr ";
            String filterQuery = StringUtil.filterQuery(filter_names, "where");
            query += filterQuery;

            ll = executeQuery( query, new Object[]{companyid});
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("companyDetailsDAOImpl.getCompanyInformation", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getCompanyHolidays(HashMap<String, Object> requestParams, ArrayList filter_names, ArrayList filter_params) throws ServiceException {
        List ll = null;
        int dl = 0;
        String companyid = "";
        try {
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = requestParams.get("companyid").toString();
            }
            String query = "from CompanyHoliday c ";
            String filterQuery = StringUtil.filterQuery(filter_names, "where");
            query += filterQuery;

            ll = executeQuery( query, new Object[]{companyid});
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("companyDetailsDAOImpl.getCompanyInformation", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public void updateCompany(HashMap hm) throws ServiceException {
        String companyid = "";
        DateFormat dateformat = null;
        try {
            if (hm.containsKey("companyid") && hm.get("companyid") != null) {
                companyid = hm.get("companyid").toString();
            }
            if (hm.containsKey("dateformat") && hm.get("dateformat") != null) {
                dateformat = (DateFormat) hm.get("dateformat");
            }
            Company company = (Company) load(Company.class, companyid);
            if (hm.containsKey("companyname") && hm.get("companyname") != null) {
                company.setCompanyName((String) hm.get("companyname"));
            }
            if (hm.containsKey("address") && hm.get("address") != null) {
                company.setAddress((String) hm.get("address"));
            }
            if (hm.containsKey("city") && hm.get("city") != null) {
                company.setCity((String) hm.get("city"));
            }
            if (hm.containsKey("state") && !StringUtil.isNullOrEmpty((String)hm.get("state"))) {
                company.setState((State) load(State.class, (String) hm.get("state")));
            }
            if (hm.containsKey("zip") && hm.get("zip") != null) {
                company.setZipCode((String) hm.get("zip"));
            }
            if (hm.containsKey("phone") && hm.get("phone") != null) {
                company.setPhoneNumber((String) hm.get("phone"));
            }
            if (hm.containsKey("fax") && hm.get("fax") != null) {
                company.setFaxNumber((String) hm.get("fax"));
            }
            if (hm.containsKey("website") && hm.get("website") != null) {
                company.setWebsite((String) hm.get("website"));
            }
            if (hm.containsKey("mail") && hm.get("mail") != null) {
                company.setEmailID((String) hm.get("mail"));
            }
            if (hm.containsKey("domainname") && hm.get("domainname") != null) {
                company.setSubDomain((String) hm.get("domainname"));
            }
            if (hm.containsKey("country") && hm.get("country") != null) {
                company.setCountry((Country) load(Country.class, (String) hm.get("country")));
            }
            if (hm.containsKey("currency") && hm.get("currency") != null) {
                company.setCurrency((KWLCurrency) load(KWLCurrency.class, (String) hm.get("currency")));
            }
            if (hm.containsKey("timezone") && hm.get("timezone") != null) {
                KWLTimeZone timeZone = (KWLTimeZone) load(KWLTimeZone.class, (String) hm.get("timezone"));
                company.setTimeZone(timeZone);
            }
            if (hm.containsKey("modifydate") && hm.get("modifydate") != null) {
                company.setModifiedOn((java.util.Date) hm.get("modifydate"));
            }
            
            if (hm.containsKey("holidays") && hm.get("holidays") != null) {
                JSONArray jArr = new JSONArray((String) hm.get("holidays"));
                Set<CompanyHoliday> holidays = company.getHolidays();
                holidays.clear();
                DateFormat formatter = dateformat;
                for (int i = 0; i < jArr.length(); i++) {
                    CompanyHoliday day = new CompanyHoliday();
                    JSONObject obj = jArr.getJSONObject(i);
                    day.setDescription(obj.getString("description"));
                    day.setHolidayDate(formatter.parse(obj.getString("day")));
                    day.setCompany(company);
                    holidays.add(day);
                }
            }
            if (hm.containsKey("logo") && hm.get("logo") != null) {
                String imageName = ((FileItem) (hm.get("logo"))).getName();
                if (imageName != null && imageName.length() > 0) {
                    String fileName = companyid + FileUploadHandler.getCompanyImageExt();
                    company.setCompanyLogo(Constants.ImgBasePath + fileName);
                    new FileUploadHandler().uploadImage((FileItem) hm.get("logo"),
                            fileName,
                            storageHandlerImplObj.GetProfileImgStorePath(), 130, 25, true, false);
                }
            }
            update(company);
        } catch (Exception e) {
            throw ServiceException.FAILURE("companyDetailsDAOImpl.updateCompany", e);
        }
    }

    public void deleteCompany(HashMap<String, Object> requestParams) throws ServiceException {
        String companyid = "";
        try {
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = requestParams.get("companyid").toString();
            }

            Company company = (Company) load(Company.class, companyid);
            company.setDeleted(1);

            update(company);
        } catch (Exception e) {
            throw ServiceException.FAILURE("companyDetailsDAOImpl.deleteCompany", e);
        }
    }

    public String getSubDomain(String companyid) throws ServiceException {
        String subdomain = "";
        try {
            Company company = (Company) get(Company.class, companyid);
            subdomain = company.getSubDomain();
        } catch (Exception e) {
            throw ServiceException.FAILURE("companyDetailsDAOImpl.getSubDomain", e);
        }
        return subdomain;
    }

    @Override
    public List getChildCompanies(String companyid) throws ServiceException {
        List ll = new ArrayList();
        try {
            String Hql = "select childcompanyid, company.subdomain from companymapping inner join company on companymapping.childcompanyid = company.companyid where companymapping.companyid = ?";
            ll = executeSQLQuery( Hql, new Object[]{companyid});
        } catch (Exception e) {
            throw ServiceException.FAILURE("companyDetailsDAOImpl.getSubDomain", e);
        }
        return ll;
    }

    public String getCompanyid(String domain) throws ServiceException {
        String companyId = "";
        List ll = new ArrayList();
        try {
            String Hql = "select companyID from Company where subDomain = ?";
            ll = executeQuery( Hql, new Object[]{domain});
            Iterator ite = ll.iterator();
            if (ite.hasNext()) {
                companyId = (String) ite.next();
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("companyDetailsDAOImpl.getCompanyid", e);
        }
        return companyId;
    }

    @Override
    public boolean IsChildCompany(String parentSubdomain, String currentSubdomain) throws ServiceException {
        boolean isChildExist = false;
        try {
            String Hql = "select childcompanyid from companymapping "
                    + "where companyid = (select companyid from company where subdomain = ?) and childcompanyid = (select companyid from company where subdomain =?)";
            List ll = executeSQLQuery( Hql, new Object[]{parentSubdomain, currentSubdomain});
            if (ll.size() > 0) {
                isChildExist = true;
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("companyDetailsDAOImpl.IsChildCompany", e);
        }
        return isChildExist;
    }

    /*
     * Method for fetching companies with Singapore country and non SGD currency
     */ 
    @Override
    public KwlReturnObject getSingaporeCompaniesWithDifferentCurrency() throws ServiceException{
        List ll = new ArrayList();
        try {
            String Hql = "From Company where country = '203' and currency is not 6 ";
            ll = executeQuery( Hql, new Object[]{});
        } catch (Exception e) {
            throw ServiceException.FAILURE("companyDetailsDAOImpl.getSubDomain", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, ll.size());
    }
    
    @Override
    public List getAllCompanyList(JSONObject json) throws ServiceException {
        List companyList = new ArrayList();
        try {
            List params = new ArrayList();
            String getCompanyQuery = "select companyid,subdomain,currency from company where activated = 'T'";
            if (!StringUtil.isNullOrEmpty(json.optString("subdomain"))) {
                getCompanyQuery += " and subdomain = ? ";
                params.add(json.opt("subdomain"));
            }
            companyList = executeSQLQuery(getCompanyQuery, params.toArray());
        } catch (Exception e) {
            throw ServiceException.FAILURE("", e);
        }
        return companyList;
    }
  
    @Override
    public KwlReturnObject getCompanyProductList(JSONObject json) throws ServiceException {
        String valuationQuery = "select subdomain, productid,price,currencyid from companyproductpricelist";
        List params = new ArrayList();
        if (!StringUtil.isNullOrEmpty(json.optString("subdomain"))) {
            valuationQuery += " where subdomain = ? ";
            params.add(json.optString("subdomain"));
        }
        List jsonList = executeSQLQuery(valuationQuery, params.toArray());
        return new KwlReturnObject(true, "", "", jsonList, jsonList.size());
    }
    
}
