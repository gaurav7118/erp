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
 * @author krawler
 */
public class PdfTemplateConfig {

    private String ID;
    private int module;
    private String pdfFooter;
    private String pdfHeader;
    private String pdfPreText;
    private String pdfPostText;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public int getModule() {
        return module;
    }

    public void setModule(int module) {
        this.module = module;
    }

    public String getPdfFooter() {
        return pdfFooter;
    }

    public void setPdfFooter(String pdfFooter) {
        this.pdfFooter = pdfFooter;
    }

    public String getPdfHeader() {
        return pdfHeader;
    }

    public void setPdfHeader(String pdfHeader) {
        this.pdfHeader = pdfHeader;
    }

    public String getPdfPostText() {
        return pdfPostText;
    }

    public void setPdfPostText(String pdfPostText) {
        this.pdfPostText = pdfPostText;
    }

    public String getPdfPreText() {
        return pdfPreText;
    }

    public void setPdfPreText(String pdfPreText) {
        this.pdfPreText = pdfPreText;
    }
}
