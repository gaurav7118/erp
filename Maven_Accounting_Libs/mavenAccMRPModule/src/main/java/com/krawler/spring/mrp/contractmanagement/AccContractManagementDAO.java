/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.contractmanagement;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccContractManagementDAO {

    public KwlReturnObject saveMasterContract(Map<String, Object> requestParams);
    
    public KwlReturnObject saveContractMapping(Map<String, Object> requestParams);

    public KwlReturnObject saveContractAddressDetails(Map<String, Object> requestParams);

    public KwlReturnObject saveMRPDocuments(HashMap<String, Object> dataMap) throws ServiceException;

    public int saveFileMapping(Map<String, Object> filemap) throws ServiceException;

    public KwlReturnObject getTemporarySavedFiles(Map<String, Object> filemap) throws ServiceException;

    public KwlReturnObject getMappedFilesResult(Map<String, Object> filemap) throws ServiceException;

    public void deleteTemporaryMappedFiles(String savedFilesMappingId, String companyid) throws ServiceException;

    public KwlReturnObject SaveUpdateObject(Object object) throws ServiceException;

    public KwlReturnObject getDocumentIdFromMappingId(String mappingId, String companyId) throws ServiceException;

    public KwlReturnObject saveContractDetails(Map<String, Object> requestParams);

    public KwlReturnObject isMasterContractIDAlreadyPresent(Map<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getMasterContracts(Map<String, Object> request) throws ServiceException;

    public KwlReturnObject getMRPContractDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getMasterContractDetails(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAddressDetails(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteMasterContracts(Map<String, Object> requestParams) throws SessionExpiredException, AccountingException, ServiceException;
    
//    public KwlReturnObject deleteMasterContractsPermanently(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getMRPContractLinkedinSO(Map<String, Object> request) throws ServiceException;
    public KwlReturnObject getParentContractID(Map<String, Object> request) throws ServiceException;
    
    public KwlReturnObject getMasterContractRows(Map<String, Object> request) throws ServiceException;
    
    public KwlReturnObject deleteMasterContractMapping(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject deleteMasterContractDocumentMapping(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject deleteBillingAddressDetails(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject deleteShippingAddressDetails(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject deleteMasterContractDetails(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject deleteMasterContract(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getMasterContractAttachments(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteMasterContractCustomData(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject deleteMasterContractDetailsCustomData(Map<String, Object> dataMap) throws ServiceException;
}
