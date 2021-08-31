/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.writeOffInvoice;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface accWriteOffServiceDao {
    
    public KwlReturnObject saveInvoiceWriteOff(HashMap<String,Object> hashMap) throws ServiceException;
    public KwlReturnObject getWrittenOfInvoices(HashMap<String,Object> map) throws ServiceException;
    public KwlReturnObject getWriteOffJEs(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getReverseWriteOffJEs(HashMap<String, Object> request) throws ServiceException;
    public KwlReturnObject getInvoiceWriteOffEntries(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getJEFromInvoiceWriteOff(String jeId,String CompanyId,Boolean isReverseJe) throws ServiceException;
    public KwlReturnObject saveReceiptWriteOff(HashMap<String,Object> hashMap) throws ServiceException;
    public KwlReturnObject getWrittenOfReceipts(HashMap<String,Object> map) throws ServiceException;
    public KwlReturnObject getJEFromReceiptWriteOff(String jeId,String CompanyId,Boolean isReverseJe) throws ServiceException;
    
}
