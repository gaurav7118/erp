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
package com.krawler.common.admin;

/**
 *
 * @author krawler-user
 */
public class KWLDateFormat {

    private String formatID;
    private String name;
    private String scriptForm;
    private String javaForm;
    private int javaSeperatorPosition;
    private int scriptSeperatorPosition;

    public String getFormatID() {
        return formatID;
    }

    public void setFormatID(String formatID) {
        this.formatID = formatID;
    }

    public String getJavaForm() {
        return javaForm;
    }

    public void setJavaForm(String javaForm) {
        this.javaForm = javaForm;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScriptForm() {
        return scriptForm;
    }

    public void setScriptForm(String scriptForm) {
        this.scriptForm = scriptForm;
    }

    public int getJavaSeperatorPosition() {
        return javaSeperatorPosition;
    }

    public void setJavaSeperatorPosition(int javaSeperatorPosition) {
        this.javaSeperatorPosition = javaSeperatorPosition;
    }

    public int getScriptSeperatorPosition() {
        return scriptSeperatorPosition;
    }

    public void setScriptSeperatorPosition(int scriptSeperatorPosition) {
        this.scriptSeperatorPosition = scriptSeperatorPosition;
    }
}
