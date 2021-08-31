/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.account;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.CustomerVendorMapping;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author krawler
 */
public interface accCusVenMapDAO {
    public CustomerVendorMapping checkCustomerMappingExists(String customerid) throws ServiceException;
    public CustomerVendorMapping checkVendorMappingExists(String vendorid) throws ServiceException;
    public KwlReturnObject saveUpdateCustomerVendorMapping(JSONObject accjson) throws ServiceException;
    public KwlReturnObject getCustomerVendorMapping(HashMap<String, Object> filterParams) throws ServiceException;  
    public KwlReturnObject saveTermForTax(HashMap<String, Object> hm) throws ServiceException;
    public List getTerms(String tax) throws ServiceException;
    public List deleteTermForTax(String tax) throws ServiceException;
    public boolean isCustomerUsedInTransactions(String accountid, String companyid) throws ServiceException;
    public boolean isVendorUsedInTransactions(String accountid, String companyid) throws ServiceException;
    public boolean isVendorUsedInTDSTransactions(String accountid, String companyid) throws ServiceException;
    public boolean isVendorTDSInterestPayableAccUsedInTrans(String VendorTDSInterestPayableAccount, String companyid) throws ServiceException;
}
