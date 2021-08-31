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
package com.krawler.customFieldMaster;

/**
 *
 * @author krawler
 */
public class fieldStructure {

    private int essential;
    private String field_name;
    private int max_length;
    private String field_label;
    private String field_value;
    private String custom_regex;
    private String companyid;
    private String moduleid;

    public enum FieldType {

        TextField,
        NumberField,
        DateField,
        ComboBox,
        Currency,
        CheckBox,
        Radio,
        TextArea,
        TimeField
    }
    private FieldType field_type;

    public enum ValidationType {

        ALPHABATE,
        ALPHANUMERIC,
        EMAIL,
        URL,
        CUSTOM
    }
    private ValidationType validation_type;

    public fieldStructure(int essential, String field_name, int max_length, String field_label, ValidationType validation_type, FieldType field_type, String field_value, String custom_error, String companyid, String moduleid) {
        this.essential = essential;
        this.field_name = field_name;
        this.max_length = max_length;
        this.field_label = field_label;
        this.validation_type = validation_type;
        this.field_type = field_type;
        this.field_value = field_value;
        this.custom_regex = custom_error;
        this.companyid = companyid;
        this.moduleid = moduleid;
    }

    public void setCustom_regex(String custom_regex) {
        this.custom_regex = custom_regex;
    }

    public String getCustom_regex() {
        return custom_regex;
    }

    public String getField_value() {
        return field_value;
    }

    public int getEssential() {
        return essential;
    }

    public String getField_label() {
        return field_label;
    }

    public String getField_name() {
        return field_name;
    }

    public FieldType getField_type() {
        return field_type;
    }

    public int getMax_length() {
        return max_length;
    }

    public void setField_value(String field_value) {
        this.field_value = field_value;
    }

    public ValidationType getValidation_type() {
        return validation_type;
    }

    public void setEssential(int essential) {
        this.essential = essential;
    }

    public void setField_label(String field_label) {
        this.field_label = field_label;
    }

    public void setField_name(String field_name) {
        this.field_name = field_name;
    }

    public void setField_type(FieldType field_type) {
        this.field_type = field_type;
    }

    public void setMax_length(int max_length) {
        this.max_length = max_length;
    }

    public void setValidation_type(ValidationType validation_type) {
        this.validation_type = validation_type;
    }
}
