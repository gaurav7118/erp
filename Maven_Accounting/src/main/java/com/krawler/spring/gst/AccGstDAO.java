/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.gst;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccGstDAO {

    public KwlReturnObject getGstFormGenerationHistory(Map<String, Object> map);
    
    public List saveGstFormGenerationHistory(Map<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject saveEntityMapping(Map<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject getEntityDetails(Map<String, Object> requestMap) throws ServiceException;
    
    public List getMultiEntityForCombo(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteEntityMapping(Map<String,Object>requestParams) throws ServiceException;
    
    public KwlReturnObject getGSTFormGenerationHistoryDetails(Map<String, Object> map) throws ServiceException;
    
    public KwlReturnObject deleteGSTFileGenerationHistory(Map<String,Object>requestParams) throws ServiceException;    
    
    public KwlReturnObject getGSTForm5Details(Map<String,Object>requestParams) throws ServiceException;    
    
    public KwlReturnObject getTransactionListingDetails(Map<String,Object>requestParams) throws ServiceException;    
}
