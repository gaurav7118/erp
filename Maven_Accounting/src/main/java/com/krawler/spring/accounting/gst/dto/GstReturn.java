/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.gst.dto;

import com.krawler.common.admin.Company;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class GstReturn {

    final public static String FILE_UPLOAD_DIRECTORY = "gstr2a";
    final public static String ENTITYID_KEY = "entityid";
    final public static String GSTR2AJSON_KEY = "gstr2aJSON";
    final public static String UPLOADFILE_KEY = "uploadFileName";
    final public static String MONTH_KEY = "month";
    final public static String YEAR_KEY = "year";
    
    private String id;
    private Date uploadedon;
    private Company companyid;
    private String entityID;
    private String gstr2a;
    private String uploadFileName;
    private int month;
    private int year;

    public Company getCompanyid() {
        return companyid;
    }

    public void setCompanyid(Company companyid) {
        this.companyid = companyid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getUploadedon() {
        return uploadedon;
    }

    public void setUploadedon(Date uploadedon) {
        this.uploadedon = uploadedon;
    }

    public String getEntityID() {
        return entityID;
    }

    public void setEntityID(String entityID) {
        this.entityID = entityID;
    }

    public String getGstr2a() {
        return gstr2a;
    }

    public void setGstr2a(String gstr2a) {
        this.gstr2a = gstr2a;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public void setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
