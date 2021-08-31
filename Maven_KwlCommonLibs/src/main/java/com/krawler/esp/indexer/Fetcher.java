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
 * Fetcher.java
 *
 * Created on November 14, 2007, 1:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.krawler.esp.indexer;

/**
 *
 * @author sagar
 */
import java.util.ArrayList;
import javax.servlet.*;
import java.util.Hashtable;

public class Fetcher implements Runnable {

    /**
     * Creates a new instance of Fetcher
     */
    private com.krawler.esp.indexer.CreateIndex indexCreate;
    ArrayList filequeue = new ArrayList();
    ArrayList filequeue1 = new ArrayList();
    boolean working;

    public boolean isWorking() {
        return working;
    }

    public boolean add(Object o) {
        return filequeue.add(o);
    }

    public boolean add1(Object o) {
        return filequeue1.add(o);
    }

    public void clear() {
        filequeue.clear();
    }

    public boolean contains(Object o) {
        return filequeue.contains(o);
    }

    public boolean remove(Object o) {
        return filequeue.remove(o);
    }

    public boolean isEmpty() {
        return filequeue.isEmpty();
    }

    public void run() {
        while (!filequeue.isEmpty()) {
            this.working = true;
            Hashtable tempfileinfo = (Hashtable) filequeue.get(0);
            indexCreate.IndexDocument(tempfileinfo.get("FilePath").toString(),
                    tempfileinfo.get("Author").toString(), tempfileinfo.get(
                    "DateModified").toString(), tempfileinfo.get(
                    "DocumentId").toString(), tempfileinfo.get(
                    "FileName").toString(), tempfileinfo.get("Type").toString(), tempfileinfo.get("Revision No").toString());
            /*
             * indexCreate.IndexDocument(tempfileinfo.get(3).toString(),
             * tempfileinfo.get(0).toString(), tempfileinfo.get(2) .toString(),
             * tempfileinfo.get(1).toString(), tempfileinfo.get(4).toString(),
             * tempfileinfo.get(5) .toString());
             */

            filequeue.remove(tempfileinfo);

        }
        this.working = false;

    }

    public void run1() {
        while (!filequeue1.isEmpty()) {
            this.working = true;
            Hashtable tempfileinfo = (Hashtable) filequeue.get(0);
            indexCreate.IndexDocument(tempfileinfo.get("FilePath").toString(),
                    tempfileinfo.get("Author").toString(), tempfileinfo.get(
                    "DateModified").toString(), tempfileinfo.get(
                    "DocumentId").toString(), tempfileinfo.get(
                    "FileName").toString(), tempfileinfo.get("Type").toString(), tempfileinfo.get("Revision No").toString());
            /*
             * indexCreate.IndexDocument(tempfileinfo.get(3).toString(),
             * tempfileinfo.get(0).toString(), tempfileinfo.get(2) .toString(),
             * tempfileinfo.get(1).toString(), tempfileinfo.get(4).toString(),
             * tempfileinfo.get(5) .toString());
             */

            filequeue.remove(tempfileinfo);

        }
        this.working = false;

    }

    public static Fetcher get(ServletContext app) {

        Fetcher bean = (Fetcher) app.getAttribute("fetch");
        if (bean == null) {

            bean = new Fetcher();
            app.setAttribute("fetch", bean);
        }

        return bean;

    }

    public Fetcher() {
        indexCreate = new CreateIndex();

    }
}
