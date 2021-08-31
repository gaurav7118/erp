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
package com.krawler.spring.accounting.gst.dto.gstr2;

/**
 *
 * @author krawler
 */
public class NILItem {

    private double cpddr;	//      Value of supplies received from Compounding Dealer	Decimal(15,2)
    private double exptdsply;	//	Value of exempted supplies received 	Decimal(15,2)
    private double ngsply;	//	Total Non GST outward supplies	Decimal(15,2)
    private double nilsply;	//	Nil Rated Supply	Decimal(15,2)

    public double getCpddr() {
        return cpddr;
    }

    public void setCpddr(double cpddr) {
        this.cpddr = cpddr;
    }

    public double getExptdsply() {
        return exptdsply;
    }

    public void setExptdsply(double exptdsply) {
        this.exptdsply = exptdsply;
    }

    public double getNgsply() {
        return ngsply;
    }

    public void setNgsply(double ngsply) {
        this.ngsply = ngsply;
    }

    public double getNilsply() {
        return nilsply;
    }

    public void setNilsply(double nilsply) {
        this.nilsply = nilsply;
    }

}
