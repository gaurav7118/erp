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

import java.io.*;
import java.util.*;
import com.krawler.common.service.ServiceException;
import com.krawler.esp.fileparser.pdfparser.KWLPdfParser;
import com.krawler.esp.fileparser.wordparser.ExtractWordFile;
import com.krawler.esp.fileparser.wordparser.FastSavedException;
import com.krawler.esp.utils.*;

public class CreateIndex {

    public CreateIndex() {
    }

    public void IndexDocument(String filePath, String Author,
            String Datemodified, String DocId, String fname,
            String contentType, String revisionno) {
        File f = new File(filePath);
        try {
            String plaintext = "";
            long Size = f.length();
            ArrayList<Object> fileDetails = new ArrayList<Object>();
            fileDetails.add(fname);
            fileDetails.add(Author);
            fileDetails.add(Datemodified);
            fileDetails.add(Size);

            if (contentType.equals("application/vnd.ms-excel")) {
            } else if (contentType.equals("application/msword")) {

                ExtractWordFile wParser = new ExtractWordFile();
                plaintext = wParser.extractText(filePath);
            } else if (contentType.equals("application/pdf")) {

                KWLPdfParser pdfParse = new KWLPdfParser();
                plaintext = pdfParse.getPlaintextpdf(filePath);

            } else if (contentType.equals("text/plain") || contentType.equals("text/xml") || contentType.equals("text/css") || contentType.equals("text/html") || contentType.equals("text/cs") || contentType.equals("text/x-javascript") || contentType.equals("File")) {
                FileInputStream fin = new FileInputStream(f);
                byte[] b = new byte[(int) f.length()];
                fin.read(b);
                String s = new String(b);
                plaintext = s;
            }
            fileDetails.add(contentType);
            fileDetails.add(DocId);
            fileDetails.add(plaintext);
            fileDetails.add(revisionno);
            KrawlerIndexCreator kwlIndex = new KrawlerIndexCreator();
            if (revisionno.equals("-")) {
                kwlIndex.DeleteIndex(DocId);
            }
            ArrayList<DocumentFields> docfieldArray = kwlIndex.setDocFields(fileDetails);
            kwlIndex.CreateIndex(docfieldArray);
            docfieldArray.clear();
            System.out.print(fname + " added in index");

        } catch (FastSavedException e) {
            System.out.print(e);
        } catch (IOException e) {
            System.out.print(e);
        } catch (ServiceException e) {
            System.out.print(e);
        }
    }

    public void indexAlert(KrawlerIndexCreator kwlIndex, ArrayList<Object> indexFieldDetails, ArrayList<String> indexFieldName) {

        //  KrawlerIndexCreator kwlIndex = new KrawlerIndexCreator();
        ArrayList<DocumentFields> docfieldArray = kwlIndex.setindexFields(indexFieldDetails, indexFieldName);
        kwlIndex.CreateIndex(docfieldArray);
        docfieldArray.clear();

    }
}
