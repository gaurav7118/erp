/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.companypreferances;

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.CompanyReportConfiguration;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class CompanyReportConfigurationImpl extends BaseDAO implements CompanyReportConfigurationDAO{
    
    public KwlReturnObject getCompanyReportConfiguration(String companyid, String type) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            list = executeQuery("from CompanyReportConfiguration crf where crf.company.companyID = ? and crf.type = ?", new Object[]{companyid, type});
        } catch (ServiceException ex) {
            Logger.getLogger(CompanyReportConfigurationImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw ServiceException.FAILURE("getCompanyReportConfiguration : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }  
    
    
    public void saveCompanyReportConfiguration(Map<String, Object> requestMap) throws ServiceException {        
        try {
            if(requestMap!=null && !requestMap.isEmpty()){                
                if(requestMap.containsKey(Constants.companyKey) && requestMap.containsKey("format") && requestMap.containsKey("type") && requestMap.get(Constants.companyKey)!=null && requestMap.get("format")!=null && requestMap.get("type")!=null){
                    
//                    deleteConfiguration(requestMap);
                    CompanyReportConfiguration reportConfig = new CompanyReportConfiguration();
                    if (requestMap.containsKey("id") && requestMap.get("id") != null && !StringUtil.isNullOrEmpty(requestMap.get("id").toString())) {
                        reportConfig = (CompanyReportConfiguration) get(CompanyReportConfiguration.class, requestMap.get("id").toString());                        
                    }
                    Company company = (Company)get(Company.class,requestMap.get(Constants.companyKey).toString());
                    reportConfig.setCompany(company);
                    reportConfig.setFormat(requestMap.get("format").toString());                    
                    reportConfig.setType(requestMap.get("type").toString());
                    saveOrUpdate(reportConfig);
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(CompanyReportConfigurationImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw ServiceException.FAILURE("saveCompanyReportConfiguration : " + ex.getMessage(), ex);
        }
        
    } 
    public KwlReturnObject deleteConfiguration(Map<String, Object> requestMap) throws ServiceException {
        int numRows = 0;
        String companyId = "";
        if (requestMap.containsKey(Constants.companyKey)) {
            companyId = (String) requestMap.get(Constants.companyKey);
        }
        ArrayList params = new ArrayList();
        params.add(companyId);
        params.add(requestMap.get("type"));
        String query = "delete from CompanyReportConfiguration cpc where cpc.company.companyID=? and cpc.type=?";
        numRows += executeUpdate(query, params.toArray());
        return new KwlReturnObject(true, "Company Report Configuration has been deleted successfully.", null, null, numRows);

    }
    
}
