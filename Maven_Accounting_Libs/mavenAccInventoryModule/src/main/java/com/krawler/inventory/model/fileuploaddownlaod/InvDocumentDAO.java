/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.fileuploaddownlaod;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public interface InvDocumentDAO {
    
    public KwlReturnObject saveInventoryDocuments(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getInventoryDocuments(HashMap<String, Object> hashMap)throws ServiceException;

    public KwlReturnObject deleteInventoryDocument(String docID)throws ServiceException;
    
}
