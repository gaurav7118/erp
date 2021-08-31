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
 * @author krawler-user
 */
public class User {

    private String userID;
    private UserLogin userLogin;
    private String image;
    private String firstName;
    private String lastName;
    private String roleID;
    private String emailID;
    private String address;
    private String designation;
    private String contactNumber;
    private String aboutUser;
    private String userStatus;
    private KWLTimeZone timeZone;
    private Company company;
    private String fax;
    private String alternateContactNumber;
    private int phpBBID;
    private String panNumber;
    private String ssnNumber;
    private KWLDateFormat dateFormat;
    private int timeformat;
    private Date createdon;
    private Date updatedon;
    private int deleteflag;
    private int callwith;
    private String user_hash;
    private String department;
    private String employeeId;

    public String getUser_hash() {
        return user_hash;
    }

    public void setUser_hash(String user_hash) {
        this.user_hash = user_hash;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getAboutUser() {
        return aboutUser;
    }

    public void setAboutUser(String aboutUser) {
        this.aboutUser = aboutUser;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlternateContactNumber() {
        return alternateContactNumber;
    }

    public void setAlternateContactNumber(String alternateContactNumber) {
        this.alternateContactNumber = alternateContactNumber;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public KWLDateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(KWLDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getEmailID() {
        return emailID;
    }

    public void setEmailID(String emailID) {
        this.emailID = emailID;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public int getPhpBBID() {
        return phpBBID;
    }

    public void setPhpBBID(int phpBBID) {
        this.phpBBID = phpBBID;
    }

    public String getRoleID() {
        return roleID;
    }

    public void setRoleID(String roleID) {
        this.roleID = roleID;
    }

    public String getSsnNumber() {
        return ssnNumber;
    }

    public void setSsnNumber(String ssnNumber) {
        this.ssnNumber = ssnNumber;
    }

    public KWLTimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(KWLTimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public UserLogin getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(UserLogin userLogin) {
        this.userLogin = userLogin;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public Date getCreatedon() {
        return createdon;
    }

    public void setCreatedon(Date createdon) {
        this.createdon = createdon;
    }

    public Date getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(Date updatedon) {
        this.updatedon = updatedon;
    }

    public int getDeleteflag() {
        return deleteflag;
    }

    public void setDeleteflag(int deleteflag) {
        this.deleteflag = deleteflag;
    }

    public int getCallwith() {
        return callwith;
    }

    public void setCallwith(int callwith) {
        this.callwith = callwith;
    }

    public int getTimeformat() {
        return timeformat;
    }

    public void setTimeformat(int timeformat) {
        this.timeformat = timeformat;
    }
    
    public String getStringObj(String objName) {
        String obj = "";
        if (objName.equals("fname")) {
            obj = this.firstName;
        }
        if (objName.equals("lname")) {
            obj = this.lastName;
        }
        if (objName.equals("phone")) {
            obj = this.contactNumber;
        }
        if (objName.equals("email")) {
            obj = this.emailID;
        }
        return obj;
    }
    
    public String getFullName(){
        StringBuilder sb = new StringBuilder();
        if(this.firstName != null){
            sb.append(this.firstName);
        }
        if(sb.length() > 0 && this.lastName != null){
            sb.append(" ").append(this.lastName);
        }else{
            sb.append(this.lastName);
        }
        return sb.toString().trim();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String emplyoeeId) {
        this.employeeId = emplyoeeId;
    }
}
