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

package com.krawler.spring.accounting.gst.services.gstr2;

import com.krawler.spring.accounting.gst.services.GSTR1ServicesImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;

/**
 *
 * @author krawler
 */
public class GSTR2AServicesImpl{

    GSTR1ServicesImpl services = new GSTR1ServicesImpl();
    
    public JSONObject getB2BInvoices(String gstin, String ret_period, char action_required, String ctin, String from_time) throws JSONException {
        return services.getB2BInvoices(gstin, ret_period, action_required, ctin, from_time);
    }

    public JSONObject getCDNInvoices(String gstin, String ret_period, String action_required, String from_time) throws JSONException{
        return services.getCDNRInvoices(gstin, ret_period, action_required, from_time);
    }
}