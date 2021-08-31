/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.companypreferances;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface CompanyReportConfigurationDAO {

    public KwlReturnObject getCompanyReportConfiguration(String companyid, String type) throws ServiceException;

    public void saveCompanyReportConfiguration(Map<String, Object> requestMap) throws ServiceException;

}
