/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

import java.util.Date;

/**
 *
 * @author krawler
 */
public class LabelValue implements Comparable {

    String label = "";
    String value = "";
    String extravalue = "";
    String taxname = "";
    String taxamount = "";
    Date creationDate = null;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExtravalue() {
        return extravalue;
    }

    public void setExtravalue(String extravalue) {
        this.extravalue = extravalue;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getTaxamount() {
        return taxamount;
    }

    public void setTaxamount(String taxamount) {
        this.taxamount = taxamount;
    }

    public String getTaxname() {
        return taxname;
    }

    public void setTaxname(String taxname) {
        this.taxname = taxname;
    }
    
    @Override
    public int compareTo(Object obj) {
        LabelValue st = (LabelValue) obj;

        if (this.creationDate == null) {
            return -1;
        } else if(st.creationDate == null) {
            return 1;
        }
        
        if (this.creationDate.compareTo(st.creationDate) == 0) {
            return 0;
        } else if (this.creationDate.compareTo(st.creationDate) > 0) {
            return 1;
        } else {
            return -1;
        }
    }
}
