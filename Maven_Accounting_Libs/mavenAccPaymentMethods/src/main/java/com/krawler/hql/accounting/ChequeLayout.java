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
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLDateFormat;
import java.util.Date;

/**
 *
 * @author training
 */
public class ChequeLayout {

    private String ID;
    private String coordinateinfo;
    private PaymentMethod paymentmethod; // payment method 
    private KWLDateFormat dateFormat;   // Date format for check
    private String appendcharacter;
    private boolean isnewlayout;
    private boolean activateExtraFields; //Other field for US
    private boolean addCharacterInCheckDate; //Add character '/' or '-' in Check Date

    public String getAppendcharacter() {
        return appendcharacter;
    }

    public void setAppendcharacter(String appendcharacter) {
        this.appendcharacter = appendcharacter;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getCoordinateinfo() {
        return coordinateinfo;
    }

    public void setCoordinateinfo(String coordinateinfo) {
        this.coordinateinfo = coordinateinfo;
    }

    public PaymentMethod getPaymentmethod() {
        return paymentmethod;
    }

    public void setPaymentmethod(PaymentMethod paymentmethod) {
        this.paymentmethod = paymentmethod;
    }

    public KWLDateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(KWLDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public boolean isIsnewlayout() {
        return isnewlayout;
    }

    public void setIsnewlayout(boolean isnewlayout) {
        this.isnewlayout = isnewlayout;
    }

    public boolean isActivateExtraFields() {
        return activateExtraFields;
    }

    public void setActivateExtraFields(boolean activateExtraFields) {
        this.activateExtraFields = activateExtraFields;
    }
    public boolean isAddCharacterInCheckDate() {
        return addCharacterInCheckDate;
    }
    public void setAddCharacterInCheckDate(boolean addCharacterInCheckDate) {
        this.addCharacterInCheckDate = addCharacterInCheckDate;
    }
}
