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
/*
 * SearchBean.java
 *
 * Created on June 5, 2007, 12:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.krawler.esp.Search;

import java.io.IOException;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import javax.servlet.ServletContext;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 *
 * @author shrinivas
 */
public class SearchBean {

    //SDP-5529- removed WhitespaceAnalyzer and used StandardAnalyzer. 
    //StandardAnalyzer will search string like if user provides "PIAS SDS WAFER" 
    //then it will seach as 3 different token
    private final StandardAnalyzer KWLAnalyzer = new StandardAnalyzer();
    /**
     * Creates a new instance of SearchBean
     */
    public SearchBean() {
    }

    public static SearchBean get(ServletContext app) {
        SearchBean bean = (SearchBean) app.getAttribute("SkyNetSearch");
        if (bean == null) {
            bean = new SearchBean();
            app.setAttribute("SkyNetSearch", bean);

        }
        return bean;
    }

    public Hits skynetsearch(String query, String Field, String indexPath) {
        String indexfield = Field + ":";
        String querytext = indexfield + query.trim();
        Hits result = null;

        try {

            String[] search_fields = {Field};
            //String indexPath = StorageHandler.GetDocIndexPath();
            IndexSearcher searcher = new IndexSearcher(indexPath);
            KeywordAnalyzer analyzer = new KeywordAnalyzer();
            Query lucenequery = MultiFieldQueryParser.parse(query,
                    search_fields, analyzer);
            // QueryParser queryparse = new QueryParser(query,analyzer);
            // Query lucenequery = queryparse.parse(querytext);
            result = searcher.search(lucenequery);

        } catch (IOException e) {
            Logger.getLogger(SearchBean.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception ex) {
            Logger.getLogger(SearchBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public Hits skynetsearch(String query, String Field) {
        String indexfield = Field + ":";
        String querytext = indexfield + query.trim();
        Hits result = null;

        try {

            String[] search_fields = {Field};
            String indexPath = storageHandlerImpl.GetDocIndexPath();
            IndexSearcher searcher = new IndexSearcher(indexPath);
            KeywordAnalyzer analyzer = new KeywordAnalyzer();
            Query lucenequery = MultiFieldQueryParser.parse(query,
                    search_fields, analyzer);
            // QueryParser queryparse = new QueryParser(query,analyzer);
            // Query lucenequery = queryparse.parse(querytext);
            result = searcher.search(lucenequery);

        } catch (IOException e) {
            Logger.getLogger(SearchBean.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception ex) {
            Logger.getLogger(SearchBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    public Hits skynetsearchMulti(String query, String[] Field, String indexPath) {
        Hits result = null;
        try {
            IndexSearcher searcher = new IndexSearcher(indexPath);
            KeywordAnalyzer analyzer = new KeywordAnalyzer();
            MultiFieldQueryParser multiparser = new MultiFieldQueryParser(Field, analyzer);
            multiparser.setDefaultOperator(QueryParser.Operator.OR);
            Query lucenequery = multiparser.parse(query);
            result = searcher.search(lucenequery);
        } catch (IOException e) {
            Logger.getLogger(SearchBean.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception ex) {
            Logger.getLogger(SearchBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }
    
     public Hits searchIndexWithSort(String query, String[] Field, String indexPath, Sort sort) {
        Hits result = null;
        try {
            IndexSearcher searcher = new IndexSearcher(indexPath);
            MultiFieldQueryParser multiparser = new MultiFieldQueryParser(Field, this.KWLAnalyzer);
            multiparser.setDefaultOperator(QueryParser.Operator.OR);
            Query lucenequery = multiparser.parse(query);
            if (sort == null) {
                result = searcher.search(lucenequery);
            } else {
                result = searcher.search(lucenequery, sort);
            }
        } catch (Exception ex) {
            Logger.getLogger(SearchBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}