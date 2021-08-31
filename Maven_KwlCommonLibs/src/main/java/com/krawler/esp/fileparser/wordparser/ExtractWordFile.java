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
package com.krawler.esp.fileparser.wordparser;

import org.apache.poi.hpsf.*;
import org.apache.poi.hwpf.model.*;
import org.apache.poi.hwpf.sprm.*;
import org.apache.poi.poifs.eventfilesystem.*;
import org.apache.poi.poifs.filesystem.*;
import org.apache.poi.util.LittleEndian;

import java.util.*;
import java.io.*;

public class ExtractWordFile {

    public ExtractWordFile() {
    }

    public String extractText(String filepath) throws FastSavedException,
            IOException {
        InputStream iStream = new BufferedInputStream(new FileInputStream(
                filepath));

        ArrayList text = new ArrayList();
        POIFSFileSystem fsys = new POIFSFileSystem(iStream);

        // load our POIFS document streams.
        DocumentEntry headerProps = (DocumentEntry) fsys.getRoot().getEntry(
                "WordDocument");
        DocumentInputStream din = fsys.createDocumentInputStream("WordDocument");
        byte[] header = new byte[headerProps.getSize()];

        din.read(header);
        din.close();

        int info = LittleEndian.getShort(header, 0xa);
        if ((info & 0x4) != 0) {
            throw new FastSavedException(
                    "Fast-saved files are unsupported at this time");
        }
        if ((info & 0x100) != 0) {
            System.out.println("This document is password protected");
        }

        // determine the version of Word this document came from.
        int nFib = LittleEndian.getShort(header, 0x2);
        // Get the information we need from the header
        boolean useTable1 = (info & 0x200) != 0;

        // get the location of the piece table
        int complexOffset = LittleEndian.getInt(header, 0x1a2);

        // determine which table stream we must use.
        String tableName = null;
        if (useTable1) {
            tableName = "1Table";
        } else {
            tableName = "0Table";
        }

        DocumentEntry table = (DocumentEntry) fsys.getRoot().getEntry(tableName);
        byte[] tableStream = new byte[table.getSize()];

        din = fsys.createDocumentInputStream(tableName);

        din.read(tableStream);
        din.close();

        int chpOffset = LittleEndian.getInt(header, 0xfa);
        int chpSize = LittleEndian.getInt(header, 0xfe);
        int fcMin = LittleEndian.getInt(header, 0x18);

        ComplexFileTable cft = new ComplexFileTable(header, tableStream,
                complexOffset, fcMin);
        TextPieceTable tpt = cft.getTextPieceTable();
        switch (nFib) {
            case 101:
            case 102:
            case 103:
            case 104:
                // this is a Word 6.0 doc send it to the extractor for that version.
                Word6Extractor oldExtractor = new Word6Extractor();
                return oldExtractor.extractText(header, tpt);
        }
        CHPBinTable cbt = new CHPBinTable(header, tableStream, chpOffset,
                chpSize, fcMin, tpt);
        // load our text pieces and our character runs

        List textPieces = tpt.getTextPieces();

        // make the POIFS objects available for garbage collection
        din = null;
        fsys = null;
        table = null;
        headerProps = null;

        List textRuns = cbt.getTextRuns();
        Iterator runIt = textRuns.iterator();
        Iterator textIt = textPieces.iterator();

        TextPiece currentPiece = (TextPiece) textIt.next();
        int currentTextStart = currentPiece.getStart();
        int currentTextEnd = currentPiece.getEnd();

        WordTextBuffer finalTextBuf = new WordTextBuffer();

        // iterate through all text runs extract the text only if they haven't
        // been
        // deleted
        while (runIt.hasNext()) {
            CHPX chpx = (CHPX) runIt.next();
            boolean deleted = isDeleted(chpx.getGrpprl());
            if (deleted) {
                continue;
            }

            int runStart = chpx.getStart();
            int runEnd = chpx.getEnd();

            while (runStart >= currentTextEnd) {
                currentPiece = (TextPiece) textIt.next();
                currentTextStart = currentPiece.getStart();
                currentTextEnd = currentPiece.getEnd();
            }

            if (runEnd < currentTextEnd) {
                String str = currentPiece.substring(
                        runStart - currentTextStart, runEnd - currentTextStart);
                finalTextBuf.append(str);
            } else if (runEnd > currentTextEnd) {
                while (runEnd > currentTextEnd) {
                    String str = currentPiece.substring(runStart - currentTextStart, currentTextEnd - currentTextStart);
                    finalTextBuf.append(str);
                    if (textIt.hasNext()) {
                        currentPiece = (TextPiece) textIt.next();
                        currentTextStart = currentPiece.getStart();
                        runStart = currentTextStart;
                        currentTextEnd = currentPiece.getEnd();
                    } else {
                        return finalTextBuf.toString();
                    }
                }
                String str = currentPiece.substring(0, runEnd - currentTextStart);
                finalTextBuf.append(str);
            } else {
                String str = currentPiece.substring(
                        runStart - currentTextStart, runEnd - currentTextStart);
                if (textIt.hasNext()) {
                    currentPiece = (TextPiece) textIt.next();
                    currentTextStart = currentPiece.getStart();
                    currentTextEnd = currentPiece.getEnd();
                }
                finalTextBuf.append(str);
            }
        }
        return finalTextBuf.toString();
    }

    private boolean isDeleted(byte[] grpprl) {
        SprmIterator iterator = new SprmIterator(grpprl, 0);
        while (iterator.hasNext()) {
            SprmOperation op = iterator.next();
            // 0 is the operation that signals a FDelRMark operation
            if (op.getOperation() == 0 && op.getOperand() != 0) {
                return true;
            }
        }
        return false;
    }

    public Properties extractProperties(InputStream in) throws IOException {

        PropertiesBroker propertiesBroker = new PropertiesBroker();
        POIFSReader reader = new POIFSReader();
        reader.registerListener(new PropertiesReaderListener(propertiesBroker),
                "\005SummaryInformation");
        reader.read(in);
        return propertiesBroker.getProperties();
    }

    class PropertiesReaderListener implements POIFSReaderListener {

        private PropertiesBroker propertiesBroker;
        private Properties metaData = new Properties();

        public PropertiesReaderListener(PropertiesBroker propertiesBroker) {
            this.propertiesBroker = propertiesBroker;
        }

        public void processPOIFSReaderEvent(POIFSReaderEvent event) {

            SummaryInformation si = null;
            Properties properties = new Properties();

            try {
                si = (SummaryInformation) PropertySetFactory.create(event.getStream());
            } catch (Exception ex) {
                properties = null;
            }

            Date tmp = null;

            String title = si.getTitle();
            String applicationName = si.getApplicationName();
            String author = si.getAuthor();
            int charCount = si.getCharCount();
            String comments = si.getComments();
            Date createDateTime = si.getCreateDateTime();
            long editTime = si.getEditTime();
            String keywords = si.getKeywords();
            String lastAuthor = si.getLastAuthor();
            Date lastPrinted = si.getLastPrinted();
            Date lastSaveDateTime = si.getLastSaveDateTime();
            int pageCount = si.getPageCount();
            String revNumber = si.getRevNumber();
            int security = si.getSecurity();
            String subject = si.getSubject();
            String template = si.getTemplate();
            int wordCount = si.getWordCount();

            /*
             * Dates are being stored in millis since the epoch to aid
             * localization
             */
            if (title != null) {
                properties.setProperty("Title", title);
            }
            if (applicationName != null) {
                properties.setProperty("Application-Name", applicationName);
            }
            if (author != null) {
                properties.setProperty("Author", author);
            }
            if (charCount != 0) {
                properties.setProperty("Character Count", charCount + "");
            }
            if (comments != null) {
                properties.setProperty("Comments", comments);
            }
            if (createDateTime != null) {
                properties.setProperty("Creation-Date", createDateTime.getTime()
                        + "");
            }
            if (editTime != 0) {
                properties.setProperty("Edit-Time", editTime + "");
            }
            if (keywords != null) {
                properties.setProperty("Keywords", keywords);
            }
            if (lastAuthor != null) {
                properties.setProperty("Last-Author", lastAuthor);
            }
            if (lastPrinted != null) {
                properties.setProperty("Last-Printed", lastPrinted.getTime()
                        + "");
            }
            if (lastSaveDateTime != null) {
                properties.setProperty("Last-Save-Date", lastSaveDateTime.getTime()
                        + "");
            }
            if (pageCount != 0) {
                properties.setProperty("Page-Count", pageCount + "");
            }
            if (revNumber != null) {
                properties.setProperty("Revision-Number", revNumber);
            }
            if (security != 0) {
                properties.setProperty("Security", security + "");
            }
            if (subject != null) {
                properties.setProperty("Subject", subject);
            }
            if (template != null) {
                properties.setProperty("Template", template);
            }
            if (wordCount != 0) {
                properties.setProperty("Word-Count", wordCount + "");
            }
            propertiesBroker.setProperties(properties);

            // si.getThumbnail(); // can't think of a sensible way of turning
            // this into a string.
        }
    }

    class PropertiesBroker {

        private Properties properties;
        private int timeoutMillis = 2 * 1000;

        public synchronized Properties getProperties() {

            long start = new Date().getTime();
            long now = start;

            while (properties == null && now - start < timeoutMillis) {
                try {
                    wait(timeoutMillis / 10);
                } catch (InterruptedException e) {
                }
                now = new Date().getTime();
            }

            notifyAll();

            return properties;
        }

        public synchronized void setProperties(Properties properties) {
            this.properties = properties;
            notifyAll();
        }
    }
}
