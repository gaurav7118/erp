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
package com.krawler.esp.fileparser.pdfparser;

//import org.pdfbox.encryption.DocumentEncryption;
//import org.pdfbox.pdfparser.PDFParser;
//import org.pdfbox.pdmodel.PDDocument;
//import org.pdfbox.util.PDFTextStripper;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.pdfbox.encryption.DocumentEncryption;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class KWLPdfParser {

    PDDocument pdf = null;
    String text = null;
    String title = null;

    public KWLPdfParser() {
    }

    public String getPlaintextpdf(String filepath) {
        try {
            InputStream iStream = new BufferedInputStream(new FileInputStream(
                    filepath));
            PDFParser parser = new PDFParser(iStream);
            parser.parse();
            pdf = parser.getPDDocument();

            if (pdf.isEncrypted()) {
                DocumentEncryption decryptor = new DocumentEncryption(pdf);

                decryptor.decryptDocument("");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(pdf);
            if (pdf != null) {
                pdf.close();
            }
        } catch (Exception Ex) {
            text = "";
        }
        return text;
    }
}
