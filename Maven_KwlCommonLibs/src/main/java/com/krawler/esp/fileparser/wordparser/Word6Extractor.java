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

import org.apache.poi.util.LittleEndian;
import org.apache.poi.hwpf.model.*;
import com.krawler.esp.fileparser.*;
import java.util.*;
import java.io.*;

public class Word6Extractor {

    public Word6Extractor() {
    }

    public String extractText(byte[] mainStream) throws IOException {
        int fcMin = LittleEndian.getInt(mainStream, 0x18);
        int fcMax = LittleEndian.getInt(mainStream, 0x1C);

        int chpTableOffset = LittleEndian.getInt(mainStream, 0xb8);
        int chpTableSize = LittleEndian.getInt(mainStream, 0xbc);

        // get a list of character properties
        Word6CHPBinTable chpTable = new Word6CHPBinTable(mainStream,
                chpTableOffset, chpTableSize, fcMin);
        List textRuns = chpTable.getTextRuns();

        // iterate through the
        WordTextBuffer finalTextBuf = new WordTextBuffer();
        Iterator runsIt = textRuns.iterator();
        while (runsIt.hasNext()) {
            CHPX chpx = (CHPX) runsIt.next();
            int runStart = chpx.getStart() + fcMin;
            int runEnd = chpx.getEnd() + fcMin;

            if (!isDeleted(chpx.getGrpprl())) {
                String s = new String(mainStream, runStart, Math.min(runEnd,
                        fcMax)
                        - runStart, "Cp1252");
                finalTextBuf.append(s);
                if (runEnd >= fcMax) {
                    break;
                }
            }
        }

        return finalTextBuf.toString();
    }

    public String extractText(byte[] mainStream, TextPieceTable tpt) throws IOException {
        int fcMin = LittleEndian.getInt(mainStream, 0x18);
        int fcMax = LittleEndian.getInt(mainStream, 0x1C);

        int chpTableOffset = LittleEndian.getInt(mainStream, 0xb8);
        int chpTableSize = LittleEndian.getInt(mainStream, 0xbc);

        // get a list of character properties
        Word6CHPBinTable chpTable = new Word6CHPBinTable(mainStream,
                chpTableOffset, chpTableSize, fcMin, tpt);
        List textRuns = chpTable.getTextRuns();

        // iterate through the
        WordTextBuffer finalTextBuf = new WordTextBuffer();
        Iterator runsIt = textRuns.iterator();
        while (runsIt.hasNext()) {
            CHPX chpx = (CHPX) runsIt.next();
            int runStart = chpx.getStart() + fcMin;
            int runEnd = chpx.getEnd() + fcMin;

            if (!isDeleted(chpx.getGrpprl())) {
                String s = new String(mainStream, runStart, Math.min(runEnd,
                        fcMax)
                        - runStart, "Cp1252");
                finalTextBuf.append(s);
                if (runEnd >= fcMax) {
                    break;
                }
            }
        }

        return finalTextBuf.toString();
    }

    /**
     * Used to determine if a run of text has been deleted.
     *
     * @param grpprl The list of sprms for this run of text.
     * @return
     */
    private boolean isDeleted(byte[] grpprl) {
        int offset = 0;
        boolean deleted = false;
        while (offset < grpprl.length) {
            switch (LittleEndian.getUnsignedByte(grpprl, offset++)) {
                case 65:
                    deleted = grpprl[offset++] != 0;
                    break;
                case 66:
                    offset++;
                    break;
                case 67:
                    offset++;
                    break;
                case 68:
                    offset += grpprl[offset];
                    break;
                case 69:
                    offset += 2;
                    break;
                case 70:
                    offset += 4;
                    break;
                case 71:
                    offset++;
                    break;
                case 72:
                    offset += 2;
                    break;
                case 73:
                    offset += 3;
                    break;
                case 74:
                    offset += grpprl[offset];
                    break;
                case 75:
                    offset++;
                    break;
                case 80:
                    offset += 2;
                    break;
                case 81:
                    offset += grpprl[offset];
                    break;
                case 82:
                    offset += grpprl[offset];
                    break;
                case 83:
                    break;
                case 85:
                    offset++;
                    break;
                case 86:
                    offset++;
                    break;
                case 87:
                    offset++;
                    break;
                case 88:
                    offset++;
                    break;
                case 89:
                    offset++;
                    break;
                case 90:
                    offset++;
                    break;
                case 91:
                    offset++;
                    break;
                case 92:
                    offset++;
                    break;
                case 93:
                    offset += 2;
                    break;
                case 94:
                    offset++;
                    break;
                case 95:
                    offset += 3;
                    break;
                case 96:
                    offset += 2;
                    break;
                case 97:
                    offset += 2;
                    break;
                case 98:
                    offset++;
                    break;
                case 99:
                    offset++;
                    break;
                case 100:
                    offset++;
                    break;
                case 101:
                    offset++;
                    break;
                case 102:
                    offset++;
                    break;
                case 103:
                    offset += grpprl[offset];
                    break;
                case 104:
                    offset++;
                    break;
                case 105:
                    offset += grpprl[offset];
                    break;
                case 106:
                    offset += grpprl[offset];
                    break;
                case 107:
                    offset += 2;
                    break;
                case 108:
                    offset += grpprl[offset];
                    break;
                case 109:
                    offset += 2;
                    break;
                case 110:
                    offset += 2;
                    break;
                case 117:
                    offset++;
                    break;
                case 118:
                    offset++;
                    break;

            }
        }
        return deleted;
    }
}
