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

import com.krawler.common.util.Constants;

public class FieldParams {

    private String id;
    private int maxlength;
    private int isessential;
    private int fieldtype;
    private int validationtype;
    private String customregex;
    private String fieldname;
    private String fieldlabel;
    private String fieldtooltip;
    private String companyid;
    private int moduleid;
    private String iseditable;
    private String comboname;
    private String comboid;
    private int moduleflag;
    private int colnum;
    private int refcolnum;
    private Company company;
    private Integer oldid;
    private int sendNotification;
    private int customfield;
    private int customcolumn;
    private String notificationDays;
    private int isforproject;
    private int isforeclaim;    //For eclaim related, refer ticket ERP-17187
    private String relatedmoduleid;
    private String relatedmodulepdfwidth;
    private int isfortask;
    private String parentid;
    private FieldParams parent;
    private int mapwithtype; // For LMS only, Default value 1, 1-None, 2-LMS - Program, 3-LMS - Session, 4-LMS - Course, 5-LMS - Trainer
    private int isActivated  = 1;
    private String defaultValue;
    private int sequence;
//    private boolean isForMultiEntity;//For MultiEntity Flow (SDP-2715)-  1-MultiEntity Dimension Field/0-Normal custom field/dimension
    private FieldParams propagatedfieldparamID;
    private boolean isForSalesCommission;
    private boolean isAutoPopulateDefaultValue;//SDP-5276 - To auto-populate default value in document entry form.
    private boolean isForKnockOff;//ERP-32814 : ERM-88 Forward Invoice dimension data to its knock off document level
//    private boolean isForGSTRuleMapping;  //   ERP-32829  For GST To Location dimensions
    private int GSTMappingColnum; //  ERP-32829  To save data in EntitybasedLineLevelTermRate 
    private int GSTConfigType; 
    private int relatedModuleIsAllowEdit;   //ERM-177 / ERP-34804 To save the weather to allow user to edit product's custom field/dimension in various documents forms where products can be used.
    private int allowInDocumentDesigner;

    public int getAllowInDocumentDesigner() {
        return allowInDocumentDesigner;
    }

    public void setAllowInDocumentDesigner(int allowInDocumentDesigner) {
        this.allowInDocumentDesigner = allowInDocumentDesigner;
    }

    public int getRelatedModuleIsAllowEdit() {
        return relatedModuleIsAllowEdit;
    }
    
    public void setRelatedModuleIsAllowEdit(int relatedModuleIsAllowEdit) {
        this.relatedModuleIsAllowEdit = relatedModuleIsAllowEdit;
    }
    
        
    public int getGSTConfigType() {
        return GSTConfigType;
    }

    public void setGSTConfigType(int GSTConfigType) {
        this.GSTConfigType = GSTConfigType;
    }

//    public boolean isIsForGSTRuleMapping() {
//        return isForGSTRuleMapping;
//    }

//    public void setIsForGSTRuleMapping(boolean isForGSTRuleMapping) {
//        this.isForGSTRuleMapping = isForGSTRuleMapping;
//    }

    public int getGSTMappingColnum() {
        return GSTMappingColnum;
    }

    public void setGSTMappingColnum(int GSTMappingColnum) {
        this.GSTMappingColnum = GSTMappingColnum;
    }

    public FieldParams getPropagatedfieldparamID() {
        return propagatedfieldparamID;
    }

    public void setPropagatedfieldparamID(FieldParams propagatedfieldparamID) {
        this.propagatedfieldparamID = propagatedfieldparamID;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getIsActivated() {
        return isActivated;
    }

    public void setIsActivated(int isActivated) {
        this.isActivated = isActivated;
    }    

    public String getnotificationDays() {
        return notificationDays;
    }

    public void setnotificationDays(String notificationDays) {
        this.notificationDays = notificationDays;
    }

    public int getsendNotification() {
        return sendNotification;
    }

    public void setsendNotification(int sendNotification) {
        this.sendNotification = sendNotification;
    }
    // following 3 fields added for auto number custom column
    private int startingnumber;
    private String prefix;
    private String suffix;
    // End

    public Integer getOldid() {
        return oldid;
    }

    public void setOldid(Integer oldid) {
        this.oldid = oldid;
    }

    public int getRefcolnum() {
        return refcolnum;
    }

    public void setRefcolnum(int refcolnum) {
        this.refcolnum = refcolnum;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public int getColnum() {
        return colnum;
    }

    public void setColnum(int colnum) {
        this.colnum = colnum;
    }

    public String getComboid() {
        return comboid;
    }

    public void setComboid(String comboid) {
        this.comboid = comboid;
    }

    public String getComboname() {
        return comboname;
    }

    public void setComboname(String comboname) {
        this.comboname = comboname;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }

    public int getisforproject() {
        return isforproject;
    }

    public void setisforproject(int isforproject) {
        this.isforproject = isforproject;
    }

    public String getCustomregex() {
        return customregex;
    }

    public void setCustomregex(String customregex) {
        this.customregex = customregex;
    }

    public String getFieldlabel() {
        return fieldlabel;
    }

    public void setFieldlabel(String fieldlabel) {
        this.fieldlabel = fieldlabel;
    }

    public String getFieldtooltip() {
        return fieldtooltip;
    }

    public void setFieldtooltip(String fieldtooltip) {
        this.fieldtooltip = fieldtooltip;
    }
    
    public String getFieldname() {
        return fieldname;
    }

    public void setFieldname(String fieldname) {
        this.fieldname = fieldname;
    }

    public int getFieldtype() {
        return fieldtype;
    }

    public void setFieldtype(int fieldtype) {
        this.fieldtype = fieldtype;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIseditable() {
        return iseditable;
    }

    public void setIseditable(String iseditable) {
        this.iseditable = iseditable;
    }

    public int getCustomfield() {
        return customfield;
    }

    public void setCustomfield(int customfield) {
        this.customfield = customfield;
    }

    public int getCustomcolumn() {
        return customcolumn;
    }

    public void setCustomcolumn(int customcolumn) {
        this.customcolumn = customcolumn;
    }

    public int getIsessential() {
        return isessential;
    }

    public void setIsessential(int isessential) {
        this.isessential = isessential;
    }

    public int getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(int maxlength) {
        this.maxlength = maxlength;
    }

    public int getModuleflag() {
        return moduleflag;
    }

    public void setModuleflag(int moduleflag) {
        this.moduleflag = moduleflag;
    }

    public int getModuleid() {
        return moduleid;
    }

    public void setModuleid(int moduleid) {
        this.moduleid = moduleid;
    }

    public int getValidationtype() {
        return validationtype;
    }

    public void setValidationtype(int validationtype) {
        this.validationtype = validationtype;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getStartingnumber() {
        return startingnumber;
    }

    public void setStartingnumber(int startingnumber) {
        this.startingnumber = startingnumber;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getRelatedmoduleid() {
        return relatedmoduleid;
    }

    public void setRelatedmoduleid(String relatedmoduleid) {
        this.relatedmoduleid = relatedmoduleid;
    }

    public String getRelatedmodulepdfwidth() {
        return relatedmodulepdfwidth;
    }

    public void setRelatedmodulepdfwidth(String relatedmodulepdfwidth) {
        this.relatedmodulepdfwidth = relatedmodulepdfwidth;
    }

    public FieldParams getParent() {
        return parent;
    }

    public void setParent(FieldParams parent) {
        this.parent = parent;
    }

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public int getisfortask() {
        return isfortask;
    }

    public void setisfortask(int isfortask) {
        this.isfortask = isfortask;
    }

    public int getmapwithtype() {
        return mapwithtype;
    }

    public void setmapwithtype(int mapwithtype) {
        this.mapwithtype = mapwithtype;
    }
    
    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }  

    public int getIsforeclaim() {
        return isforeclaim;
    }

    public void setIsforeclaim(int isforeclaim) {
        this.isforeclaim = isforeclaim;
    }

//    public boolean isIsForMultiEntity() {
//        return isForMultiEntity;
//    }
//
//    public void setIsForMultiEntity(boolean isForMultiEntity) {
//        this.isForMultiEntity = isForMultiEntity;
//    }

    public boolean isIsForSalesCommission() {
        return isForSalesCommission;
    }

    public void setIsForSalesCommission(boolean isForSalesCommission) {
        this.isForSalesCommission = isForSalesCommission;
    }


    public boolean isIsAutoPopulateDefaultValue() {
        return isAutoPopulateDefaultValue;
    }

    public void setIsAutoPopulateDefaultValue(boolean isAutoPopulateDefaultValue) {
        this.isAutoPopulateDefaultValue = isAutoPopulateDefaultValue;
    }

    public boolean isIsForKnockOff() {
        return isForKnockOff;
    }

    public void setIsForKnockOff(boolean isForKnockOff) {
        this.isForKnockOff = isForKnockOff;
    }
   
    public boolean isFieldOfGivenGSTConfigType(String fieldCategory) {

        if (fieldCategory.equalsIgnoreCase(Constants.IsForGSTRuleMapping)) {
            
            return GSTConfigType == Constants.GST_CONFIG_ISFORGST;
        
        } else if (fieldCategory.equalsIgnoreCase(Constants.isformultientity)) {
            
            return GSTConfigType == Constants.GST_CONFIG_ISFORMULTIENTITY;
       
        } else if (fieldCategory.equalsIgnoreCase(Constants.GSTProdCategory)) {
           
            return GSTConfigType==Constants.GST_CONFIG_MANDETORY_FIELD; 
        } else if (fieldCategory.equalsIgnoreCase(Constants.isEWayRelatedFields)) {           
            return GSTConfigType==Constants.EWAYFIELDS_GSTCONFIGTYPE; 
        }
        return false;
    }
   
}
