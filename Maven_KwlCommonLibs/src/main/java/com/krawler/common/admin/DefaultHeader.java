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

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author krawler
 */
// TODO : Sandeep
//      remove validateType : Done
//      map xType values
public class DefaultHeader {

    private String id;
    private String defaultHeader;           // Display Name
    private String moduleName;
    private String pojoheadername;          // DB name[For filter]
    private String recordname;              // Data Index
    private String xtype;                   // 0:String, 1:Integer, 2:Boolean, 3:Date, 4:Ref/combo, 5,6,7,8:String, 9:Double, 10:email
    private int flag;
    private String configid;
    private Modules module;
    private String pojoMethodName;          // to call setter methods
    private String validateType;            // ref,double,integer,date,email [ref : for foreign key],[default date format is yyyy-MM-dd ]
    private int maxLength;
    private boolean mandatory;              // True: Force to map column
    private boolean hbmNotNull;             // not-null value from .Hbm.xml file {<column not-null="????"/>}
    private String defaultValue;            // [now : For new date]
    private String refModule_PojoClassName; // pojo class name
    private String refDataColumn_HbmName;   // .hbm.xml data column name value {<column name="????"/>}
    private String refFetchColumn_HbmName;  // .hbm.xml fetch column name value {<column name="????"/>}
    private boolean allowImport;            // TRUE to show column in mapping interface OR False to hide
    private boolean required;
    private boolean islineitem;
    private boolean isbatchdetail;
    private boolean isdocumentimport;
    private boolean customflag;
    private String dbcolumnname;           // Entry of database column name (same as in DB Table)
    private String dbTableName;            //Entry of database table name 
    private Set headerinfo = new HashSet();
    private boolean allowMapping;
    private boolean allowAdvanceSearch;
    private String dataIndex;              //entry of column dataindex
    private String rendererType;           //entry of column renderer type for exporting data like currecy,rowcurrency,date etc.    
    private boolean isreadonly;    // In Module Form if given field is non editable once that Master is created and is used in other transactions. its value will be true for such Master Form Field else False
    private String reftablename;           // Entry of Ref database column name (same as in DB Table)
    private String reftablefk;
    private String formFieldName;          // this property is added to  map keys (used while creating datamap before saving any object like customer) with default_header entry.
    private boolean conditionalMandetory; // is set true for fields which are conditionaly become mandatory e.g In Product Master 'Stock UOM'. Stock UOM is mandatory except Service type product.
    private String refModuleId;
    private boolean iscustomreport;
    private String countryID; // It will be zoro  for those record whci are common for all company amd for specific country field we need to provide contryID. Example for Malasian 137 
    private boolean isDefaultFieldMappings; //field added for Custom Report Builder;value true means meta data mapping for this field is in customreportsdefaultfieldsmapping table exists
    private boolean isDataIndex; //field added for Custom Report Builder;value true means this value will be included in the select clause while building report sql query otherwise won't
    private boolean allowindocumentdesigner;
    private boolean allowinotherapplication;
    private String subModuleFlag;
    private boolean allowcrossmodule;

    public String getSubModuleFlag() {
        return subModuleFlag;
    }

    public void setSubModuleFlag(String subModuleFlag) {
        this.subModuleFlag = subModuleFlag;
    }

    public String getCountryID() {
        return countryID;
    }

    public void setCountryID(String countryID) {
        this.countryID = countryID;
    }

    public boolean isIscustomreport() {
        return iscustomreport;
    }

    public void setIscustomreport(boolean iscustomreport) {
        this.iscustomreport = iscustomreport;
    }
    public boolean isAllowindocumentdesigner() {
        return allowindocumentdesigner;
    }

    public void setAllowindocumentdesigner(boolean allowindocumentdesigner) {
        this.allowindocumentdesigner = allowindocumentdesigner;
    }
  
    public boolean isAllowinotherapplication() {
        return allowinotherapplication;
    }

    public void setAllowinotherapplication(boolean allowinotherapplication) {
        this.allowinotherapplication = allowinotherapplication;
    }
   
    public boolean isIsreadonly() {
        return isreadonly;
    }

    public void setIsreadonly(boolean isreadonly) {
        this.isreadonly = isreadonly;
    }
    
    
    public String getFormFieldName() {
        return formFieldName;
    }

    public void setFormFieldName(String formFieldName) {
        this.formFieldName = formFieldName;
    }
    
    public String getReftablename() {
        return reftablename;
    }

    public void setReftablename(String reftablename) {
        this.reftablename = reftablename;
    }
    
    public String getReftablefk() {
        return reftablefk;
    }

    public void setReftablefk(String reftablefk) {
        this.reftablefk = reftablefk;
    }
  
     public String getRefModuleId() {
        return refModuleId;
    }

    public void setRefModuleId(String refModuleId) {
        this.refModuleId = refModuleId;
    }
    
    public String getReftabledatacolumn() {
        return reftabledatacolumn;
    }

    public void setReftabledatacolumn(String reftabledatacolumn) {
        this.reftabledatacolumn = reftabledatacolumn;
    }
    private String reftabledatacolumn;            //Entry of Ref database table name 

    public boolean isAllowAdvanceSearch() {
        return allowAdvanceSearch;
    }

    public void setAllowAdvanceSearch(boolean allowAdvanceSearch) {
        this.allowAdvanceSearch = allowAdvanceSearch;
    }

    public String getDbTableName() {
        return dbTableName;
    }

    public void setDbTableName(String dbTableName) {
        this.dbTableName = dbTableName;
    }      
    
    public String getDataIndex() {
        return dataIndex;
    }

    public void setDataIndex(String dataIndex) {
        this.dataIndex = dataIndex;
    }

    public String getRendererType() {
        return rendererType;
    }

    public void setRendererType(String rendererType) {
        this.rendererType = rendererType;
    }

    public boolean isCustomflag() {
        return customflag;
    }

    public void setCustomflag(boolean customflag) {
        this.customflag = customflag;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isHbmNotNull() {
        return hbmNotNull;
    }

    public void setHbmNotNull(boolean hbmNotNull) {
        this.hbmNotNull = hbmNotNull;
    }

    public boolean isAllowImport() {
        return allowImport;
    }

    public void setAllowImport(boolean allowImport) {
        this.allowImport = allowImport;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public Modules getModule() {
        return module;
    }

    public void setModule(Modules module) {
        this.module = module;
    }

    public String getPojoMethodName() {
        return pojoMethodName;
    }

    public void setPojoMethodName(String pojoMethodName) {
        this.pojoMethodName = pojoMethodName;
    }

    public String getRefDataColumn_HbmName() {
        return refDataColumn_HbmName;
    }

    public void setRefDataColumn_HbmName(String refDataColumn_HbmName) {
        this.refDataColumn_HbmName = refDataColumn_HbmName;
    }

    public String getRefFetchColumn_HbmName() {
        return refFetchColumn_HbmName;
    }

    public void setRefFetchColumn_HbmName(String refFetchColumn_HbmName) {
        this.refFetchColumn_HbmName = refFetchColumn_HbmName;
    }

    public String getRefModule_PojoClassName() {
        return refModule_PojoClassName;
    }

    public void setRefModule_PojoClassName(String refModule_PojoClassName) {
        this.refModule_PojoClassName = refModule_PojoClassName;
    }

    public String getValidateType() {
        return validateType;
    }

    public void setValidateType(String validateType) {
        this.validateType = validateType;
    }

    public String getConfigid() {
        return configid;
    }

    public void setConfigid(String configid) {
        this.configid = configid;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getRecordname() {
        return recordname;
    }

    public void setRecordname(String recordname) {
        this.recordname = recordname;
    }

    public String getDefaultHeader() {
        return defaultHeader;
    }

    public void setDefaultHeader(String defaultHeader) {
        this.defaultHeader = defaultHeader;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getPojoheadername() {
        return pojoheadername;
    }

    public void setPojoheadername(String pojoheadername) {
        this.pojoheadername = pojoheadername;
    }

    public String getXtype() {
        return xtype;
    }

    public void setXtype(String xtype) {
        this.xtype = xtype;
    }

    public String getDbcolumnname() {
        return dbcolumnname;
    }

    public void setDbcolumnname(String dbcolumnname) {
        this.dbcolumnname = dbcolumnname;
    }

    public Set getHeaderinfo() {
        return headerinfo;
    }

    public void setHeaderinfo(Set headerinfo) {
        this.headerinfo = headerinfo;
    }

    public boolean isAllowMapping() {
        return allowMapping;
    }

    public void setAllowMapping(boolean allowMapping) {
        this.allowMapping = allowMapping;
    }

    public boolean isIsbatchdetail() {
        return isbatchdetail;
    }

    public void setIsbatchdetail(boolean isbatchdetail) {
        this.isbatchdetail = isbatchdetail;
    }

    public boolean isIslineitem() {
        return islineitem;
    }

    public void setIslineitem(boolean islineitem) {
        this.islineitem = islineitem;
    }
    
    public boolean isIsdocumentimport() {
        return isdocumentimport;
    }

    public void setIsdocumentimport(boolean isdocumentimport) {
        this.isdocumentimport = isdocumentimport;
    }

    public boolean isConditionalMandetory() {
        return conditionalMandetory;
}

    public void setConditionalMandetory(boolean conditionalMandetory) {
        this.conditionalMandetory = conditionalMandetory;
    }
    
    public boolean isIsDataIndex() {
        return isDataIndex;
    }

    public void setIsDataIndex(boolean isDataIndex) {
        this.isDataIndex = isDataIndex;
    }
    
    public boolean getIsDataIndex() {
        return isDataIndex;
    }
    
    public boolean isIsDefaultFieldMappings() {
        return isDefaultFieldMappings;
    }

    public void setIsDefaultFieldMappings(boolean isDefaultFieldMappings) {
        this.isDefaultFieldMappings = isDefaultFieldMappings;
    }

    public boolean isAllowcrossmodule() {
        return allowcrossmodule;
    }

    public void setAllowcrossmodule(boolean allowcrossmodule) {
        this.allowcrossmodule = allowcrossmodule;
    }
}
