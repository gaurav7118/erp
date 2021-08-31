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
package com.krawler.accounting.fontsetting;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Chunk;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Utilities;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.FontFactory;

public class FontFamilySelector {

    List<FontFamily> families = new ArrayList<FontFamily>();

    public void addFontFamily(FontFamily fontFamily) {
        Font font = fontFamily.getPrimaryFont();

        if (font.getBaseFont() == null) {
            BaseFont bf = font.getCalculatedBaseFont(true);
            font = new Font(bf, font.getSize(), font.getCalculatedStyle(), font.getColor());
        }

        families.add(fontFamily);
    }

    public Phrase process(String text, FontContext context, Color color, Float size, Integer style) {
        int fsize = families.size();
        if (fsize == 0) {
            throw new IndexOutOfBoundsException("No font is defined.");
        }
        if (text == null) {
            text = "";
        }
        char cc[] = text.toCharArray();
        int len = cc.length;
        StringBuffer sb = new StringBuffer();
        Font font = null;
        int lastidx = -1;
        Phrase ret = new Phrase();
        for (int k = 0; k < len; ++k) {
            char c = cc[k];
            if (c == '\n' || c == '\r') {
                sb.append(c);
                continue;
            }

            if (Utilities.isSurrogatePair(cc, k)) {
                int u = Utilities.convertToUtf32(cc, k);
                for (int f = 0; f < fsize; ++f) {
                    font = (Font) families.get(f).getPrimaryFont();
                    if (font.getBaseFont().charExists(u)) {
                        if (lastidx != f) {
                            if (sb.length() > 0 && lastidx != -1) {
                                FontFamily fm = families.get(lastidx);
                                Font fnt = fm.getFont(context);
                                applyPropertiesToFont(fnt, color, size, style);
                                Chunk ck = new Chunk(sb.toString(), fnt);
                                ret.add(ck);
                                sb.setLength(0);
                            }
                            lastidx = f;
                        }
                        sb.append(c);
                        sb.append(cc[++k]);
                        break;
                    }
                }
            } else {
                for (int f = 0; f < fsize; ++f) {
                    font = (Font) families.get(f).getPrimaryFont();
                    if (font.getBaseFont().charExists(c)) {
                        if (lastidx != f) {
                            if (sb.length() > 0 && lastidx != -1) {
                                FontFamily fm = families.get(lastidx);
                                Font fnt = fm.getFont(context);
                                applyPropertiesToFont(fnt, color, size, style);
                                Chunk ck = new Chunk(sb.toString(), fnt);
                                ret.add(ck);
                                sb.setLength(0);
                            }
                            lastidx = f;
                        }
                        sb.append(c);
                        break;
                    }
                }
            }
        }
        if (sb.length() > 0) {
            FontFamily fm = families.get(lastidx == -1 ? 0 : lastidx);
            Font fnt = fm.getFont(context);
            applyPropertiesToFont(fnt, color, size, style);
            Chunk ck = new Chunk(sb.toString(), fnt);
            ret.add(ck);
        }
        return ret;
    }
    
    
    public Phrase process(String text, FontContext context, Font Basefont) {
        
        List<FontFamily> familiesTemp = new ArrayList<FontFamily>();
        
        FontFamily fontFamily = new FontFamily();
        fontFamily.addFont(context, Basefont, true);
        
       
        
        familiesTemp.addAll(families);
        
        int fsize = familiesTemp.size();
        if (fsize == 0) {
            throw new IndexOutOfBoundsException("No font is defined.");
        }
        if (text == null) {
            text = "";
        }
        char cc[] = text.toCharArray();
        int len = cc.length;
        StringBuffer sb = new StringBuffer();
        Font font = null;
        int lastidx = -1;
        Phrase ret = new Phrase();
        for (int k = 0; k < len; ++k) {
            char c = cc[k];
            if (c == '\n' || c == '\r') {
                sb.append(c);
                continue;
            }

            if (Utilities.isSurrogatePair(cc, k)) {
                int u = Utilities.convertToUtf32(cc, k);
                for (int f = 0; f < fsize; ++f) {
                    font = (Font) familiesTemp.get(f).getPrimaryFont();
                    if (font.getBaseFont().charExists(u)) {
                        if (lastidx != f) {
                            if (sb.length() > 0 && lastidx != -1) {
                                FontFamily fm = familiesTemp.get(lastidx);
                                Font fnt = fm.getFont(context);
                                Font tempFont = applyPropertiesToFont(fnt, Basefont.getColor(), Basefont.getSize(), Basefont.getStyle(), Basefont.isBold());
                                Chunk ck = new Chunk(sb.toString(), tempFont);
                                ret.add(ck);
                                sb.setLength(0);
                            }
                            lastidx = f;
                        }
                        sb.append(c);
                        sb.append(cc[++k]);
                        break;
                    }
                }
            } else {
                for (int f = 0; f < fsize; ++f) {
                    font = (Font) familiesTemp.get(f).getPrimaryFont();
                    if (font.getBaseFont().charExists(c)) {
                        if (lastidx != f) {
                            if (sb.length() > 0 && lastidx != -1) {
                                FontFamily fm = familiesTemp.get(lastidx);
                                Font fnt = fm.getFont(context);
                                
                                Font tempFont = applyPropertiesToFont(fnt, Basefont.getColor(), Basefont.getSize(), Basefont.getStyle(), Basefont.isBold());
                                Chunk ck = new Chunk(sb.toString(), tempFont);
                                ret.add(ck);
                                sb.setLength(0);
                            }
                            lastidx = f;
                        }
                        sb.append(c);
                        break;
                    }
                }
            }
        }
        if (sb.length() > 0) {
            FontFamily fm = familiesTemp.get(lastidx == -1 ? 0 : lastidx);
            Font fnt = fm.getFont(context);
            Font tempFont = applyPropertiesToFont(fnt, Basefont.getColor(), Basefont.getSize(), Basefont.getStyle(), Basefont.isBold());
            Chunk ck = new Chunk(sb.toString(), tempFont);
            ret.add(ck);
        }
        return ret;
    }
    
    public Phrase process(String str, FontContext ctx, Color color, Float size) {
        return process(str, ctx, color, size, null);
    }

    public Phrase process(String str, FontContext ctx, Color color, Integer style) {
        return process(str, ctx, color, null, style);
    }

    public Phrase process(String str, FontContext ctx, Float size, Integer style) {
        return process(str, ctx, null, size, style);
    }

    public Phrase process(String str, FontContext ctx, Color color) {
        return process(str, ctx, color, null, null);
    }

    public Phrase process(String str, FontContext ctx, Float size) {
        return process(str, ctx, null, size, null);
    }

    public Phrase process(String str, FontContext ctx, Integer style) {
        return process(str, ctx, null, null, style);
    }

    public Phrase process(String str, FontContext ctx) {
        return process(str, ctx, null, null, null);
    }

    private void applyPropertiesToFont(Font font, Color color, Float size, Integer style) {
        if (color != null) {
            font.setColor(color);
        }
        if (size != null) {
            font.setSize(size);
        }
        if (style != null) {
            font.setStyle(style);
        }
    }
    
    private Font applyPropertiesToFont(Font font, Color color, Float size, Integer style, boolean isbold) {

        Font tempFont = null;//font;

        tempFont = new Font(font.getBaseFont(), size, ((isbold)?Font.BOLD : style), color);
        
        return tempFont;

    }

}
