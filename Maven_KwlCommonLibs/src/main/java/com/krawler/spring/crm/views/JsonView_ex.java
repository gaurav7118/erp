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
package com.krawler.spring.crm.views;

import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.view.AbstractView;

/*
 * An XML view resolver. This view is invoked if view returned by controller
 * matches this view's bean name in the configuration file views.xml.
 */
public class JsonView_ex extends AbstractView {

    /**
     * Apache commons-logging.jar logger - uncomment to enable
     */
    //protected final Log logger = LogFactory.getLog(getClass());
    /**
     * Renders the view by marshalling the model data (set in the controller)
     * into XML and writing the XML to the response output stream.
     */
    protected void renderMergedOutputModel(Map map,
            HttpServletRequest request, HttpServletResponse response) throws Exception {

        //logger.info("Start rendering of " + this.getBeanName());

        // this is the business model data (typically a POJO) that was set and returned by the controller
        String model = (String) map.get("model");
        
        try {
            JSONObject jobj = new JSONObject(model);
            /*
             This block will be called to redirect messages of 'system failure'
             */
            if (jobj.has("msg")) {
                if (jobj.getString("msg").toLowerCase().contains("system failure:")) {
                    jobj.remove("msg");
                    jobj.put("msg", "Some problem occurred while performing the operation. Please try again later.");
                    model = jobj.toString();
                }
            }
        } catch (JSONException je) {
            model = (String) map.get("model");
        }
        // write the XML data to the response
        response.getOutputStream().write(model.getBytes());
//    	response.setContentType("text/html; charset=ISO-8859-1");
        response.setContentType("text/html; charset=UTF-8");
    }
}
