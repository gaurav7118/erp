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

import java.util.Date;

/**
 *
 * @author krawler
 */
public class ImportLog {

    public static String failureTag = "_Failure"; // Used to save failure record file. e.g filenmae= abc.csv => abc_Failure.csv
    private String id;
    private String fileName;
    private String storageName;
    private String log;
    private String type;
    private String failureFileType;
    private int totalRecs;
//    private int imported;
    private int rejected;
    private Date importDate;
    private Modules module;
    private User user;
    private Company company;

    public String getFailureFileType() {
        return failureFileType;
    }

    public void setFailureFileType(String failureFileType) {
        this.failureFileType = failureFileType;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Modules getModule() {
        return module;
    }

    public void setModule(Modules module) {
        this.module = module;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public int getRejected() {
        return rejected;
    }

    public void setRejected(int rejected) {
        this.rejected = rejected;
    }

    public int getTotalRecs() {
        return totalRecs;
    }

    public void setTotalRecs(int totalRecs) {
        this.totalRecs = totalRecs;
    }

    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    //Modified functions
    public static String getActualFileName(String storageName) {
        String ext = storageName.substring(storageName.lastIndexOf("."));
        String actualName = storageName.substring(0, storageName.lastIndexOf("_"));
        actualName = actualName + ext;
        return actualName;
    }

    public int getImported() {
        return totalRecs - rejected;
    }

    public String getFailureFileName() {
        String failureFileName = storageName;
        String ext = failureFileName.substring(failureFileName.lastIndexOf("."));
        failureFileName = failureFileName.substring(0, failureFileName.lastIndexOf("."));
        return failureFileName + failureTag + ext;
    }
    
}
