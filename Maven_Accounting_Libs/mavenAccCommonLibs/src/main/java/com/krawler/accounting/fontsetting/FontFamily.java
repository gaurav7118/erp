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

import java.util.HashMap;
import java.util.Map;

import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;

public class FontFamily {

    private Font primaryFont;
    private Map<FontContext, Font> fontsMap = new HashMap<FontContext, Font>();

    public void addFont(FontContext context, Font font) {
        if (font.getBaseFont() == null) {
            BaseFont bf = font.getCalculatedBaseFont(true);
            font = new Font(bf, font.getSize(), font.getCalculatedStyle(), font.getColor());
        }
        fontsMap.put(context, font);
        if (primaryFont == null) {
            primaryFont = font;
        }
    }

    public void addFont(FontContext context, Font font, boolean isPrimary) {
        if (font.getBaseFont() == null) {
            BaseFont bf = font.getCalculatedBaseFont(true);
            font = new Font(bf, font.getSize(), font.getCalculatedStyle(), font.getColor());
        }
        fontsMap.put(context, font);
        if (isPrimary) {
            primaryFont = font;
        }
    }

    public Font getPrimaryFont() {
        return primaryFont;
    }

    public Font getFont(FontContext context) {
        if (fontsMap.containsKey(context)) {
            return fontsMap.get(context);
        }
        return primaryFont;
    }
}
