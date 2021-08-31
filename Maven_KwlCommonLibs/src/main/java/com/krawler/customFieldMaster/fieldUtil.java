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

import com.krawler.customFieldMaster.fieldUtil.validationError;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 *
 * @author krawler
 */
public class fieldUtil {

    public static class validationError {

        private int errorCode;
        private String errorMessage;
        private boolean error;

        public validationError(int errorCode, String errorMessage, boolean error) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.error = error;
        }

        public boolean isError() {
            return error;
        }

        public void setError(boolean error) {
            this.error = error;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public void appendError(String errorMessage) {
            this.setErrorMessage(this.getErrorMessage() + "," + errorMessage);
        }
    }

    public static List<validationError> ValidateField(fieldStructure _field) {
        validationError validfield = new validationError(0, "", false);
        boolean isInValid = false;
        List<validationError> errorlist = new ArrayList<validationError>();
        switch (_field.getValidation_type()) {
            case ALPHABATE:
                isInValid = java.util.regex.Pattern.matches("([a-zA-Z]*)", _field.getField_value());
                break;
            case ALPHANUMERIC:
                isInValid = java.util.regex.Pattern.matches("([a-zA-Z0-9]*)", _field.getField_value());
                break;
            case EMAIL:
                isInValid = java.util.regex.Pattern.matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[_A-Za-z0-9-]+)", _field.getField_value());
                break;
            case URL:
                isInValid = java.util.regex.Pattern.matches("\\b(https?|ftp|file)://[-A-Z0-9+&@#/%?=~_|!:,.;]*[-A-Z0-9+&@#/%=~_|]", _field.getField_value());
                break;
            case CUSTOM:
                isInValid = java.util.regex.Pattern.matches(_field.getCustom_regex(), _field.getField_value());
                break;

        }
        if (!isInValid) {

            validfield.setError(true);
            validfield.setErrorCode(1);
            validfield.setErrorMessage(ResourceBundle.getBundle("errorMessages").getString("invalidinput"));
            errorlist.add(validfield);

        }
        if (_field.getField_value().length() > _field.getMax_length()) {
            validfield.setError(true);
            validfield.setErrorCode(2);
            validfield.setErrorMessage(ResourceBundle.getBundle("errorMessages").getString("maxlength"));
            errorlist.add(validfield);
        }
        return errorlist;
    }

    public static void main(String[] args) {
        /*
         * fieldStructure f = new fieldStructure(0, "ss", 2, "dd",
         * fieldStructure.ValidationType.EMAIL,
         * fieldStructure.FieldType.TextField, "","");
         * f.setField_value("sd_f.sdf@dd.com"); List<validationError> list =
         * ValidateField(f);
        System.out.print(list.get(0).getErrorMessage());
         */
    }
}
