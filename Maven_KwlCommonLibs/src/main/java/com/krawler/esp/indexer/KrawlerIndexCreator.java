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
package com.krawler.esp.indexer;

import com.krawler.common.service.ServiceException;
import com.krawler.esp.Search.SearchBean;
import com.krawler.esp.utils.DocumentFields;
import java.io.*;
import java.util.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.Hits;
import com.krawler.spring.storageHandler.storageHandlerImpl;

public class KrawlerIndexCreator {

    private String indexPath;
    private StandardAnalyzer KWLAnalyzer;
    private static String[] fields = {"FileName", "Author", "DateModified",
        "Size", "Type", "DocumentId", "PlainText", "Revision No"};

    public KrawlerIndexCreator() throws ServiceException {
        try {
            this.indexPath = storageHandlerImpl.GetDocIndexPath();
        } catch (Exception e) {
            throw ServiceException.FAILURE(
                    "KrawlerIndexCreator [WebConfig Error]", e);
        }
        KWLAnalyzer = new StandardAnalyzer();
    }

    public void setIndexPath(String path) {
        indexPath = path;
    }

    /**
     * docdetails array in order
     * "FileName","Author","DateModified","RevisionNumber","Size","Type","Documentid","PlainText"
     *
     * @param docdetails
     */
    public ArrayList<DocumentFields> setindexFields(ArrayList<Object> indexdetails, ArrayList<String> indexFields) {

        ArrayList<DocumentFields> indexfieldArray = new ArrayList<DocumentFields>();
        Iterator itr = indexdetails.iterator();
        int i = 0;
        while (itr.hasNext()) {

            DocumentFields docFields = new DocumentFields();
            docFields.SetFieldName(indexFields.get(i));
            docFields.SetFieldValue(itr.next().toString());
            indexfieldArray.add(docFields);
            i++;
        }
        return indexfieldArray;
    }

    public ArrayList<DocumentFields> setDocFields(ArrayList<Object> docdetails) {

        ArrayList<DocumentFields> docfieldArray = new ArrayList<DocumentFields>();
        Iterator itr = docdetails.iterator();
        int i = 0;
        while (itr.hasNext()) {

            DocumentFields docFields = new DocumentFields();
            docFields.SetFieldName(fields[i]);
            docFields.SetFieldValue(itr.next().toString());
            docfieldArray.add(docFields);
            i++;
        }
        return docfieldArray;
    }

    public int CreateIndex(ArrayList<DocumentFields> DocFields) {
        Document doc = new Document();
        Iterator<DocumentFields> itr = DocFields.iterator();

        while (itr.hasNext()) {
            DocumentFields tempfield = itr.next();
            Field docfield = new Field(tempfield.GetFieldName(), tempfield.GetFieldValue(), Field.Store.YES, Field.Index.TOKENIZED);
            doc.add(docfield);
        }

        try {
            boolean CreateIndex = true;
            File f = new File(this.indexPath + "/segments");
            if (f.exists()) {
                CreateIndex = false;
            }

            IndexWriter indWriter = new IndexWriter(this.indexPath,
                    this.KWLAnalyzer, CreateIndex);
            indWriter.addDocument(doc);
            indWriter.close();
        } catch (Exception ex) {
            return 0;
        }

        return 1;
    }

    public void DeleteIndex(String documentId) {
        Term t = new Term("DocumentId", documentId);
        try {
            IndexReader docInRead = IndexReader.open(this.indexPath);
            docInRead.deleteDocuments(t);
            docInRead.close();
            IndexWriter inw = new IndexWriter(this.indexPath, KWLAnalyzer,
                    false);
            inw.optimize();
            inw.close();
        } catch (Exception ex) {
            System.out.print(ex.toString());
        }

    }

    public void deleteAlertIndex(String alertId) {
        Term t = new Term("alertId", alertId);
        try {
            IndexReader docInRead = IndexReader.open(this.indexPath);
            docInRead.deleteDocuments(t);
            docInRead.close();
            IndexWriter inw = new IndexWriter(this.indexPath, KWLAnalyzer,
                    false);
            inw.optimize();
            inw.close();
        } catch (Exception ex) {
            System.out.print(ex.toString());
        }

    }

    public void updateDocument(String documentId, String fieldName,
            String fieldValue) {
        try {
            SearchBean sbean = new SearchBean();
            Hits hresult = sbean.skynetsearch(documentId, "DocumentId");
            Document doc = hresult.doc(0);
            ArrayList<DocumentFields> docfields = new ArrayList<DocumentFields>();
            for (int i = 0; i < fields.length; i++) {
                DocumentFields docFields = new DocumentFields();
                docFields.SetFieldName(fields[i]);
                if (fields[i].equalsIgnoreCase(fieldName)) {
                    docFields.SetFieldValue(fieldValue);
                } else {
                    docFields.SetFieldValue(doc.get(fields[i]));
                }
                docfields.add(docFields);
            }
            DeleteIndex(documentId);
            CreateIndex(docfields);

        } catch (Exception ex) {
            System.out.print(ex.toString());
        }

    }
}
