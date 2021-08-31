/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class ImportFileDetails {
    private String ID;//Unique UUID
    private String fileName;//Name of file to be imported
    private String fileNameSuffixDateFormat;//DateFormat for date to be used as suffix for file-name. This field is to be used when there is varying date suffix in filename.
    private String serverUrl;//URL or IP of FTP server
    private int serverPort;//Port to be used when connecting to FTP server
    private String subDirectory;//Directory path on server in which file resides. Must start with a '/'(slash character). For example '/home/krawler'
    private String userName;//Username for server access
    private String passKey;//Password for server access
    private Company company;//company for which import is to be done
    private Modules module;//module in which import is to be done

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileNameSuffixDateFormat() {
        return fileNameSuffixDateFormat;
    }

    public void setFileNameSuffixDateFormat(String fileNameSuffixDateFormat) {
        this.fileNameSuffixDateFormat = fileNameSuffixDateFormat;
    }
    
    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getSubDirectory() {
        return subDirectory;
    }

    public void setSubDirectory(String subDirectory) {
        this.subDirectory = subDirectory;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassKey() {
        return passKey;
    }

    public void setPassKey(String passKey) {
        this.passKey = passKey;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Modules getModule() {
        return module;
    }

    public void setModule(Modules module) {
        this.module = module;
    }
}
