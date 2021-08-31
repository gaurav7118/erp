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
public class NotificationRules {

    private String ID;
    private int moduleId;
    private int beforeafter;
    private int days;
    private String fieldid;
    private String templateid;
    private String emailids;
    private String mailcontent;
    private String mailsubject;
    private Company company;
    private String users;
    private boolean MailToSalesPerson;
    private boolean MailToStoreManager;
    private boolean MailToContactPerson;
    private boolean MailToAssignedTo;
    private boolean mailToCreator;
    private boolean MailToAssignedPersons;
    private NotifictionRulesRecurringDetail recurringDetail;
    private String mailbodysqlquery;
    private String mailbodyjson;
    private String mailsubjectsqlquery;
    private String mailsubjectjson;
    private boolean mailtoshippingemail; // flag for send mail to shipping address email or billing address email
    private String senderid;
    private String hyperlinkText;

    public String getMailbodyjson() {
        return mailbodyjson;
    }

    public boolean isMailtoshippingemail() {
        return mailtoshippingemail;
    }

    public void setMailtoshippingemail(boolean mailtoshippingemail) {
        this.mailtoshippingemail = mailtoshippingemail;
    }

    public void setMailbodyjson(String mailbodyjson) {
        this.mailbodyjson = mailbodyjson;
    }

    public String getMailbodysqlquery() {
        return mailbodysqlquery;
    }

    public void setMailbodysqlquery(String mailbodysqlquery) {
        this.mailbodysqlquery = mailbodysqlquery;
    }

    public String getMailsubjectjson() {
        return mailsubjectjson;
    }

    public void setMailsubjectjson(String mailsubjectjson) {
        this.mailsubjectjson = mailsubjectjson;
    }

    public String getMailsubjectsqlquery() {
        return mailsubjectsqlquery;
    }

    public void setMailsubjectsqlquery(String mailsubjectsqlquery) {
        this.mailsubjectsqlquery = mailsubjectsqlquery;
    }

    public NotifictionRulesRecurringDetail getRecurringDetail() {
        return recurringDetail;
    }

    public void setRecurringDetail(NotifictionRulesRecurringDetail recurringDetail) {
        this.recurringDetail = recurringDetail;
    }

    public boolean isMailToCreator() {
        return mailToCreator;
    }

    public void setMailToCreator(boolean mailToCreator) {
        this.mailToCreator = mailToCreator;
    }

    public boolean isMailToAssignedPersons() {
        return MailToAssignedPersons;
    }

    public void setMailToAssignedPersons(boolean MailToAssignedPersons) {
        this.MailToAssignedPersons = MailToAssignedPersons;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getBeforeafter() {
        return beforeafter;
    }

    public void setBeforeafter(int beforeafter) {
        this.beforeafter = beforeafter;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public String getFieldid() {
        return fieldid;
    }

    public void setFieldid(String fieldid) {
        this.fieldid = fieldid;
    }

    public String getEmailids() {
        return emailids;
    }

    public void setEmailids(String emailids) {
        this.emailids = emailids;
    }

    public String getMailcontent() {
        return mailcontent;
    }

    public void setMailcontent(String mailcontent) {
        this.mailcontent = mailcontent;
    }

    public String getMailsubject() {
        return mailsubject;
    }

    public void setMailsubject(String mailsubject) {
        this.mailsubject = mailsubject;
    }

    public boolean isMailToSalesPerson() {
        return MailToSalesPerson;
    }

    public void setMailToSalesPerson(boolean MailToSalesPerson) {
        this.MailToSalesPerson = MailToSalesPerson;
    }

    public boolean isMailToStoreManager() {
        return MailToStoreManager;
    }

    public void setMailToStoreManager(boolean MailToStoreManager) {
        this.MailToStoreManager = MailToStoreManager;
    }

    public boolean isMailToAssignedTo() {
        return MailToAssignedTo;
    }

    public void setMailToAssignedTo(boolean MailToAssignedTo) {
        this.MailToAssignedTo = MailToAssignedTo;
    }

    public String getTemplateid() {
        return templateid;
    }

    public void setTemplateid(String templateid) {
        this.templateid = templateid;
    }
    
    public boolean isMailToContactPerson() {
        return MailToContactPerson;
    }

    public void setMailToContactPerson(boolean MailToContactPerson) {
        this.MailToContactPerson = MailToContactPerson;
    }

    public String getSenderid() {
        return senderid;
    }

    public void setSenderid(String senderid) {
        this.senderid = senderid;
    }

    public String getHyperlinkText() {
        return hyperlinkText;
    }

    public void setHyperlinkText(String hyperlinkText) {
        this.hyperlinkText = hyperlinkText;
    }
    
}
