/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.util;

/**
 *
 * @author krawler
 */
public class FieldConstants {

    public static final String Crm_getcombodata = "SELECT `id`,`value` from fieldcombodata  where fieldid = ? and deleteflag=? ";
    public static final String Crm_fc = "fieldManager.getComboData";
    public static final String Crm_fieldid = "fieldid";
    public static final String Crm_deleteflag = "deleteflag";
    public static final String Crm_flag = "flag";
    public static final String Crm_id = "id";
    public static final String Crm_name = "name";
    public static final String Crm_FieldComboData = "FieldComboData";
    public static final String Crm_tablename = "tablename";
    public static final String Crm_maxlength = "maxlength";
}
