/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.documentdetails;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.fileupload.FileItem;

/**
 *
 * @author krawler
 */
public interface accDetailsDAO {

    //CommentDao
    public KwlReturnObject addComments(HashMap<String, Object> requestParam) throws ServiceException;

    public KwlReturnObject editComments(HashMap<String, Object> requestParam) throws ServiceException;

    public KwlReturnObject getComments(HashMap requestParams) throws ServiceException;

    public KwlReturnObject deleteComments(String id) throws ServiceException;

    //document dao
    public KwlReturnObject getDocuments(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject saveDocuments(HashMap<String, Object> requestParams) throws ServiceException;

//    public void parseRequest(List fileItems, HashMap<String, String> arrParam, ArrayList<FileItem> fi, boolean fileUpload) throws ServiceException;
//    public KwlReturnObject uploadFile(FileItem fi, String userid) throws ServiceException;
//    public void saveDocumentMapping(JSONObject jobj) throws ServiceException;
    public KwlReturnObject downloadDocument(String id) throws ServiceException;

//    public KwlReturnObject getDocumentList(HashMap<String, Object> requestParams, StringBuffer usersList) throws ServiceException;
//    public KwlReturnObject addTag(HashMap<String, Object> requestParams) throws ServiceException;
//    public KwlReturnObject getDocumentsForTable(HashMap<String, Object> queryParams, boolean allflag) throws ServiceException;
//    public KwlReturnObject documentSearch(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject deleteDocument(String docsId) throws ServiceException;
    
    public KwlReturnObject getQuotationDocumentList(HashMap<String, Object> requestParams) throws ServiceException;
    
    /**
     * description : delete shared documnets between Accounting & other Apps.
     * @param Docs ID
     * @return boolean
     */
    public boolean deleteSharedDocuments(String docid);
    
    public KwlReturnObject getTransactionDocuments(HashMap<String, Object> dataMap) throws ServiceException;
}
