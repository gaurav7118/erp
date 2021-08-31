/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.jobwork;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccJobWorkDao {

    public KwlReturnObject saveJobWork(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveForecastTemplate(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject saveForecastTemplateProductMapping(Map<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getForecastTemplate(Map<String, Object> requestParams) throws ServiceException;
    
    public double getSalesQtyforModules(Map<String, Object> requestParams) throws ServiceException;
    
    public double getInvoiceQtyforModules(Map<String, Object> requestParams) throws ServiceException;
    
    public double getDeliveryOrderQtyforModules(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getJobWorkOrders(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteJobWorkOrdersPerm(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteJobWorkOrdersTemp(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getForecastNumberCount(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteForecastTemplateProductMapping(Map<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject deleteForecast(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getChallanReport(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getJWProductSummaryReportIN(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getConsumeChallan(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getConsumeQtyForProduct(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getJWProductSummaryReportOUT(Map<String, Object> requestParams) throws ServiceException;
    

}
