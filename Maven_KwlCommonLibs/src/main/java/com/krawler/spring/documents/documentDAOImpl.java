/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.spring.documents;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.Docmap;
import com.krawler.common.admin.Docs;
import com.krawler.common.admin.User;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.fileupload.FileItem;

/**
 *
 * @author Karthik
 */
public class documentDAOImpl extends BaseDAO implements documentDAO {

    private storageHandlerImpl storageHandlerImplObj;

    public void setstorageHandlerImpl(storageHandlerImpl storageHandlerImplObj1) {
        this.storageHandlerImplObj = storageHandlerImplObj1;
    }

    public KwlReturnObject getDocuments(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        String recid = "";
        try {
            if (requestParams.containsKey("recid") && requestParams.get("recid") != null) {
                recid = requestParams.get("recid").toString();
            }
            String Hql = "select dm.docid FROM com.krawler.common.admin.Docmap dm where dm.recid=? ";
            ll = executeQuery( Hql, new Object[]{recid});
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE("documentDAOImpl.getDocuments : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public void parseRequest(List fileItems, HashMap<String, String> arrParam, ArrayList<FileItem> fi, boolean fileUpload) throws ServiceException {

        FileItem fi1 = null;
        for (Iterator k = fileItems.iterator(); k.hasNext();) {
            fi1 = (FileItem) k.next();
            if (fi1.isFormField()) {
                arrParam.put(fi1.getFieldName(), fi1.getString());
            } else {
                if (fi1.getSize() != 0) {
                    fi.add(fi1);
                    fileUpload = true;
                }
            }
        }
    }

    public KwlReturnObject uploadFile(FileItem fi, String userid) throws ServiceException {
        Docs docObj = new Docs();
        List ll = new ArrayList();
        int dl = 0;
        try {
            String fileName = new String(fi.getName().getBytes(), "UTF8");
            String Ext = "";
            String a = "";

            if (fileName.contains(".")) {
                Ext = fileName.substring(fileName.indexOf(".") + 1, fileName.length());
                a = Ext.toUpperCase();
            }

            User userObj = (User) get(User.class, userid);
            docObj.setDocname(fileName);
            docObj.setUser(userObj);
            docObj.setStorename("");
            docObj.setDoctype(a + " " + "File");
            docObj.setUploadedon(new Date());
            docObj.setStorageindex(1);
            docObj.setDocsize(fi.getSize() + "");

            save(docObj);

            String fileid = docObj.getId();
            if (Ext.length() > 0) {
                fileid = fileid + Ext;
            }
            docObj.setStorename(fileid);

            update(docObj);

            ll.add(docObj);

//            String temp = "/home/trainee";
            String temp = storageHandlerImplObj.GetDocStorePath();
            uploadFile(fi, temp, fileid);
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public void uploadFile(FileItem fi, String destinationDirectory, String fileName) throws ServiceException {
        try {
            File destDir = new File(destinationDirectory);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            File uploadFile = new File(destinationDirectory + "/" + fileName);
            fi.write(uploadFile);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("documentDAOImpl.uploadFile", ex);
        }

    }

    public void saveDocumentMapping(JSONObject jobj) throws ServiceException {
        try {
            Docmap docMap = new Docmap();

            if (jobj.has("docid") && !StringUtil.isNullOrEmpty(jobj.getString("docid"))) {
                Docs doc = (Docs) get(Docs.class, jobj.getString("docid"));
                docMap.setDocid(doc);
                if (jobj.has("companyid") && !StringUtil.isNullOrEmpty(jobj.getString("companyid"))) {
                    Company company = (Company) get(Company.class, jobj.getString("companyid"));
                    doc.setCompany(company);
                }
                if (jobj.has("userid") && !StringUtil.isNullOrEmpty(jobj.getString("userid"))) {
                    User user = (User) get(User.class, jobj.getString("userid"));
                    doc.setUser(user);
                }
            }
            if (jobj.has("refid")) {
                docMap.setRecid(jobj.getString("refid"));
            }
            if (jobj.has("map")) {
                docMap.setRelatedto(jobj.getString("map"));
            }
            save(docMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("documentDAOImpl.saveDocumentMapping", ex);
        }
    }

    public KwlReturnObject downloadDocument(String id) throws ServiceException {

        List ll = null;
        int dl = 0;
        try {
            ll = executeQuery( "FROM "
                    + "com.krawler.common.admin.Docmap AS crmdocs1 where crmdocs1.docid.docid =?", new Object[]{id});
            dl = ll.size();
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getDocumentList(HashMap<String, Object> requestParams, StringBuffer usersList) throws ServiceException {
        String tagSearch = requestParams.containsKey("tag") ? requestParams.get("tag").toString() : "";
        String quickSearch = requestParams.containsKey("ss") ? requestParams.get("ss").toString() : "";
        int start = 0;
        int limit = 20;
        int dl = 0;
        Object[] params = null;
        if (requestParams.containsKey("start") && requestParams.containsKey("limit") && !StringUtil.isNullOrEmpty(requestParams.get("start").toString())) {
            start = Integer.parseInt(requestParams.get("start").toString());
            limit = Integer.parseInt(requestParams.get("limit").toString());
        }
        List ll = null;
        try {
            String companyid = requestParams.get("companyid").toString();
            String Hql = "select c from com.krawler.common.admin.Docmap c where c.docid.company.companyID=? ";
            params = new Object[]{companyid};

            if (!StringUtil.isNullOrEmpty(tagSearch)) {
                tagSearch = tagSearch.replaceAll("'", "");
                Hql += " and c.docid.tags like '%" + tagSearch + "%' ";
            }
            if (!StringUtil.isNullOrEmpty(quickSearch)) {
                Hql += " and c.docid.docname like '" + quickSearch + "%' ";
            }

            String selectInQuery = Hql + " and c.docid.userid.userID in (" + usersList + ") order by c.docid.uploadedon desc ";
            ll = executeQuery( selectInQuery, params);
            dl = ll.size();

            ll = executeQueryPaging( selectInQuery, params, new Integer[]{start, limit});
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("documentDAOImpl.getDocumentList", ex);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject addTag(HashMap<String, Object> requestParams) throws ServiceException {
        String tag = requestParams.containsKey("tag") ? requestParams.get("tag").toString() : "";
        List ll = new ArrayList();
        int dl = 0;
        try {
            String tags[] = tag.split(",,");
            Docs c = (Docs) get(Docs.class, tags[0]);
            c.setTags(tags[1]);
            ll.add(c);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("documentDAOImpl.addTag", ex);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }

    public KwlReturnObject getDocumentsForTable(HashMap<String, Object> queryParams, boolean allflag) throws ServiceException {
        KwlReturnObject kmsg = null;
        try {
            kmsg = getTableData( queryParams, allflag);
        } catch (Exception e) {
            throw ServiceException.FAILURE("documentDAOImpl.getDocumentsForTable : " + e.getMessage(), e);
        }
        return kmsg;
    }

    public KwlReturnObject documentSearch(HashMap<String, Object> requestParams) throws ServiceException {
        List ll = null;
        int dl = 0;
        String MyQuery1 = "";
        String MyQuery = "";
        String querytxt = "";
        String companyid = "";
        try {
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = requestParams.get("companyid").toString();
            }
            if (requestParams.containsKey("keyword") && requestParams.get("keyword") != null) {
                querytxt = requestParams.get("keyword").toString();
            }

            Pattern p = Pattern.compile("^(?i)tag:[[\\s]*([\\w\\s]+[(/|\\{1})]?)*[\\s]*[\\w]+[\\s]*]*$");
            Matcher m = p.matcher(querytxt);
            boolean b = m.matches();
            ArrayList filter_params = new ArrayList();

            if (!b) {
                MyQuery = querytxt;
                MyQuery1 = querytxt;
                if (querytxt.length() > 2) {
                    MyQuery = querytxt + "%";
                    MyQuery1 = "% " + MyQuery;
                }
                String Hql = "select c from com.krawler.common.admin.Docs c  where c.company.companyID=? and c.deleteflag=0";
                Hql = Hql + "and ( c.docname like ? or c.docsize like ? or c.doctype like ? or c.userid.firstName like ? or c.userid.lastName like ? ) ";
                filter_params.add(companyid);
                filter_params.add(MyQuery);
                filter_params.add(MyQuery);
                filter_params.add(MyQuery);
                filter_params.add(MyQuery);
                filter_params.add(MyQuery);

                ll = executeQuery( Hql, filter_params.toArray());
                dl = ll.size();
            }
        } catch (ServiceException e) {
            throw ServiceException.FAILURE("documentDAOImpl.documentSearch", e);
        } catch (Exception e) {
            throw ServiceException.FAILURE("documentDAOImpl.documentSearch", e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, dl);
    }
}
