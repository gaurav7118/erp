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

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.hwpf.model.*;

/**
 * This class holds all of the character formatting properties from a Word
 * 6.0/95 document.
 *
 * @author Ryan Ackley
 */
public class Word6CHPBinTable {

    /**
     * List of character properties.
     */
    ArrayList _textRuns = new ArrayList();

    /**
     * Constructor used to read a binTable in from a Word document.
     *
     * @param documentStream The POIFS "WordDocument" stream from a Word
     * document
     * @param offset The offset of the Chp bin table in the main stream.
     * @param size The size of the Chp bin table in the main stream.
     * @param fcMin The start of text in the main stream.
     */
    public Word6CHPBinTable(byte[] documentStream, int offset, int size,
            int fcMin) throws UnsupportedEncodingException {
        PlexOfCps binTable = new PlexOfCps(documentStream, offset, size, 2);

        int length = binTable.length();
        for (int x = 0; x < length; x++) {
            GenericPropertyNode node = binTable.getProperty(x);

            int pageNum = LittleEndian.getShort((byte[]) node.getBytes());
            int pageOffset = POIFSConstants.LARGER_BIG_BLOCK_SIZE * pageNum;
            byte[] tableStream = null;
            int size1 = 0;
            TextPieceTable tpttemp = new TextPieceTable(documentStream, tableStream, pageOffset, size1, fcMin);
            CHPFormattedDiskPage cfkp = new CHPFormattedDiskPage(
                    documentStream, pageOffset, fcMin, tpttemp);

            int fkpSize = cfkp.size();

            for (int y = 0; y < fkpSize; y++) {
                _textRuns.add(cfkp.getCHPX(y));
            }
        }
    }

    public Word6CHPBinTable(byte[] documentStream, int offset, int size,
            int fcMin, TextPieceTable tpt) {
        PlexOfCps binTable = new PlexOfCps(documentStream, offset, size, 2);

        int length = binTable.length();
        for (int x = 0; x < length; x++) {
            GenericPropertyNode node = binTable.getProperty(x);

            int pageNum = LittleEndian.getShort((byte[]) node.getBytes());
            int pageOffset = POIFSConstants.LARGER_BIG_BLOCK_SIZE * pageNum;

            CHPFormattedDiskPage cfkp = new CHPFormattedDiskPage(
                    documentStream, pageOffset, fcMin, tpt);

            int fkpSize = cfkp.size();

            for (int y = 0; y < fkpSize; y++) {
                _textRuns.add(cfkp.getCHPX(y));
            }
        }
    }

    public List getTextRuns() {
        return _textRuns;
    }
}
