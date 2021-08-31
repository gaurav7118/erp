/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.fileuploaddownlaod.impl;

import com.krawler.common.service.ServiceException;
import com.krawler.inventory.model.fileuploaddownlaod.InvDocumentDAO;
import com.krawler.inventory.model.fileuploaddownlaod.InvDocumentService;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;

/**
 *
 * @author krawler
 */
public class InvDocumentServiceImpl implements InvDocumentService{
    
    private InvDocumentDAO invDocumentDAO;

    public void setDocumentDAO(InvDocumentDAO documentDAO) {
        this.invDocumentDAO = documentDAO;
    }
    
    @Override
    public KwlReturnObject saveInventoryDocuments(HashMap<String, Object> hashMap) throws ServiceException {
        return  invDocumentDAO.saveInventoryDocuments(hashMap);
    }

    @Override
    public KwlReturnObject getInventoryDocuments(HashMap<String, Object> hashMap) throws ServiceException {
        return  invDocumentDAO.getInventoryDocuments(hashMap);
    }

    @Override
    public KwlReturnObject deleteInventoryDocument(String docID) throws ServiceException {
        return invDocumentDAO.deleteInventoryDocument(docID);
    }
    
}
