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
package com.krawler.spring.chartXmlSettings;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnMsg;
import com.krawler.spring.common.KwlReturnObject;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 *
 * @author Karthik
 */
public class chartXmlSettingDAOImpl implements chartXmlSettingDAO {

    private HibernateTemplate hibernateTemplate;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.hibernateTemplate = new HibernateTemplate(sessionFactory);
    }

    public KwlReturnObject getBarChartXML(HashMap<String, Object> requestParams) throws ServiceException {
        String xmlString = "";
        List ll = new ArrayList();
        String title = requestParams.containsKey("title") ? requestParams.get("title").toString() : "";
        String units = requestParams.containsKey("unit") ? requestParams.get("unit").toString() : "";
        String unitPosition = requestParams.containsKey("unitposition") ? requestParams.get("unitposition").toString() : "right";
        String graphColor = requestParams.get("graphcolor").toString();
        String rotateChart = requestParams.get("rotatechart").toString();
        String doubleBar = requestParams.get("doublebar").toString();
        String title1 = requestParams.get("title1").toString();
        String title2 = requestParams.get("title2").toString();
        try {
            if (StringUtil.isNullOrEmpty(graphColor)) {
                graphColor = "FF6600";
            }
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            Element root = doc.createElement("settings");
            doc.appendChild(root);

            Element sep = doc.createElement("thousands_separator");
            sep.appendChild(doc.createTextNode(","));
            root.appendChild(sep);

            Element colorPalette = doc.createElement("colors");
            colorPalette.appendChild(doc.createTextNode("#FF6600,#FCD202,#B0DE09,#0D8ECF,#2A0CD0,#CD0D74,#CC0000,#00CC00,#0000CC,#DDDDDD,#999999,#333333,#990000"));
            root.appendChild(colorPalette);

            if (!StringUtil.isNullOrEmpty(rotateChart)) {
                Element chartType = doc.createElement("type");
                chartType.appendChild(doc.createTextNode("bar"));
                root.appendChild(chartType);
            }

            Element redraw = doc.createElement("redraw");
            redraw.appendChild(doc.createTextNode("1"));
            root.appendChild(redraw);

            Element plot_area = doc.createElement("plot_area");
            root.appendChild(plot_area);
            Element alpha = doc.createElement("alpha");
            alpha.appendChild(doc.createTextNode("51"));
            plot_area.appendChild(alpha);
            Element border_alpha = doc.createElement("border_alpha");
            border_alpha.appendChild(doc.createTextNode("55"));
            plot_area.appendChild(border_alpha);
            Element margins = doc.createElement("margins");
            plot_area.appendChild(margins);
            Element left = doc.createElement("left");
            left.appendChild(doc.createTextNode("100"));
            margins.appendChild(left);
            Element right = doc.createElement("right");
            right.appendChild(doc.createTextNode("80"));
            margins.appendChild(right);

            Element grid = doc.createElement("grid");
            root.appendChild(grid);

            Element category = doc.createElement("category");
            grid.appendChild(category);
            Element color = doc.createElement("color");
            color.appendChild(doc.createTextNode("3399FF"));
            category.appendChild(color);
            Element cat_alpha = doc.createElement("alpha");
            cat_alpha.appendChild(doc.createTextNode("80"));
            category.appendChild(cat_alpha);
            Element dashed = doc.createElement("dashed");
            dashed.appendChild(doc.createTextNode("1"));
            category.appendChild(dashed);

            Element value = doc.createElement("value");
            grid.appendChild(value);
            Element val_color = doc.createElement("color");
            val_color.appendChild(doc.createTextNode("6666FF"));
            value.appendChild(val_color);
            Element val_cat_alpha = doc.createElement("alpha");
            val_cat_alpha.appendChild(doc.createTextNode("72"));
            value.appendChild(val_cat_alpha);
            Element val_dashed = doc.createElement("dashed");
            val_dashed.appendChild(doc.createTextNode("1"));
            value.appendChild(val_dashed);
            Element fil_color = doc.createElement("fill_color");
            fil_color.appendChild(doc.createTextNode("0099CC"));
            value.appendChild(fil_color);

            Element axes = doc.createElement("axes");
            root.appendChild(axes);

            Element axes_category = doc.createElement("category");
            axes.appendChild(axes_category);
            Element width = doc.createElement("width");
            width.appendChild(doc.createTextNode("1"));
            axes_category.appendChild(width);
            Element axes_color = doc.createElement("color");
            axes_color.appendChild(doc.createTextNode("333333"));
            axes_category.appendChild(color);

            Element axes_value = doc.createElement("value");
            axes.appendChild(value);
            Element val_width = doc.createElement("width");
            val_width.appendChild(doc.createTextNode("1"));
            axes_value.appendChild(val_width);

            Element values = doc.createElement("values");
            root.appendChild(values);

            Element values_val = doc.createElement("value");
            values.appendChild(values_val);
            Element min = doc.createElement("min");
            min.appendChild(doc.createTextNode("0"));
            values_val.appendChild(min);
            Element unit = doc.createElement("unit");
            unit.appendChild(doc.createTextNode(units));
            values_val.appendChild(unit);
            Element unit_position = doc.createElement("unit_position");
            unit_position.appendChild(doc.createTextNode(unitPosition));
            values_val.appendChild(unit_position);

            Element legend = doc.createElement("legend");
            root.appendChild(legend);

            Element enabled = doc.createElement("enabled");
            enabled.appendChild(doc.createTextNode("0"));
            legend.appendChild(enabled);

            Element border_color = doc.createElement("border_color");
            border_color.appendChild(doc.createTextNode("FF0033"));
            legend.appendChild(border_color);

            Element legend_border_alpha = doc.createElement("border_alpha");
            legend_border_alpha.appendChild(doc.createTextNode("24"));
            legend.appendChild(legend_border_alpha);

            Element align = doc.createElement("align");
            align.appendChild(doc.createTextNode("center"));
            legend.appendChild(align);

            Element key = doc.createElement("key");
            legend.appendChild(key);
            Element key_border_color = doc.createElement("border_color");
            key_border_color.appendChild(doc.createTextNode("FF0000"));
            key.appendChild(key_border_color);

            Element depth = doc.createElement("depth");
            depth.appendChild(doc.createTextNode("15"));
            root.appendChild(depth);

            Element column = doc.createElement("column");
            root.appendChild(column);

            Element column_width = doc.createElement("width");
            column_width.appendChild(doc.createTextNode("85"));
            column.appendChild(column_width);

            Element balloon_text = doc.createElement("balloon_text");
            balloon_text.appendChild(doc.createTextNode(units + "{value}"));
            column.appendChild(balloon_text);

            Element grow_time = doc.createElement("grow_time");
            grow_time.appendChild(doc.createTextNode("6"));
            column.appendChild(grow_time);

            Element graphs = doc.createElement("graphs");
            root.appendChild(graphs);

            Element graph0 = doc.createElement("graph");
            graph0.setAttribute("gid", "0");
            graphs.appendChild(graph0);

            if (!StringUtil.isNullOrEmpty(doubleBar)) {
                Element graph0_title = doc.createElement("title");
                graph0_title.appendChild(doc.createTextNode(title1));
                graph0.appendChild(graph0_title);
            }

            Element graphs_color = doc.createElement("color");
            graphs_color.appendChild(doc.createTextNode(graphColor));
            graph0.appendChild(graphs_color);

            if (!StringUtil.isNullOrEmpty(doubleBar)) {
                Element graph1 = doc.createElement("graph");
                graph1.setAttribute("gid", "1");
                graphs.appendChild(graph1);

                Element graph1_title = doc.createElement("title");
                graph1_title.appendChild(doc.createTextNode(title2));
                graph1.appendChild(graph1_title);
            }

            Element labels = doc.createElement("labels");
            root.appendChild(labels);

            Element label = doc.createElement("label");
            label.setAttribute("lid", "1");
            labels.appendChild(label);

            Element text = doc.createElement("text");
            text.appendChild(doc.createTextNode(title));
            label.appendChild(text);
            Element label1_y = doc.createElement("y");
            label1_y.appendChild(doc.createTextNode("18"));
            label.appendChild(label1_y);
            Element label1_text_color = doc.createElement("text_color");
            label1_text_color.appendChild(doc.createTextNode("000000"));
            label.appendChild(label1_text_color);
            Element label1_text_size = doc.createElement("text_size");
            label1_text_size.appendChild(doc.createTextNode("16"));
            label.appendChild(label1_text_size);
            Element label1_align = doc.createElement("align");
            label1_align.appendChild(doc.createTextNode("center"));
            label.appendChild(label1_align);

            Element label1 = doc.createElement("label");
            label1.setAttribute("lid", "0");
            labels.appendChild(label1);

            Element label2_x = doc.createElement("x");
            label2_x.appendChild(doc.createTextNode("100"));
            label1.appendChild(label2_x);
            Element label2_y = doc.createElement("y");
            label2_y.appendChild(doc.createTextNode("600"));
            label1.appendChild(label2_y);
            Element rotate = doc.createElement("rotate");
            rotate.appendChild(doc.createTextNode("true"));
            label1.appendChild(rotate);
            Element label2_text_size = doc.createElement("text_size");
            label2_text_size.appendChild(doc.createTextNode("16"));
            label1.appendChild(label2_text_size);
            Element label2_align = doc.createElement("align");
            label2_align.appendChild(doc.createTextNode("center"));
            label1.appendChild(label2_align);

            Element label2 = doc.createElement("label");
            label2.setAttribute("lid", "2");
            labels.appendChild(label2);

            Element label3_y = doc.createElement("y");
            label3_y.appendChild(doc.createTextNode("625"));
            label2.appendChild(label3_y);
            Element label3_text_size = doc.createElement("text_size");
            label3_text_size.appendChild(doc.createTextNode("16"));
            label2.appendChild(label3_text_size);
            Element label3_align = doc.createElement("align");
            label3_align.appendChild(doc.createTextNode("center"));
            label2.appendChild(label3_align);

            Element strings = doc.createElement("strings");
            root.appendChild(strings);

            Element no_data = doc.createElement("no_data");
            no_data.appendChild(doc.createTextNode("There are no reports for you at this time"));
            strings.appendChild(no_data);

            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();

            //Create string from XML tree

            StringWriter sw = new StringWriter();
            StreamResult res = new StreamResult(sw);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, res);
            xmlString = sw.toString().substring(54, sw.toString().length()); //Remove <?xml ?> tag for charts
            ll.add(xmlString);
        } catch (AbstractMethodError e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (TransformerConfigurationException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (TransformerException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, 0);
    }

    public KwlReturnObject getPieChartXML(HashMap<String, Object> requestParams) throws ServiceException {
        String xmlString = "";
        List ll = new ArrayList();
        String title = requestParams.containsKey("title") ? requestParams.get("title").toString() : "";
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();

            Element root = doc.createElement("settings");
            doc.appendChild(root);

            Element t_sep = doc.createElement("thousands_separator");
            t_sep.appendChild(doc.createTextNode(","));
            root.appendChild(t_sep);
            Element d_sep = doc.createElement("thousands_separator");
            d_sep.appendChild(doc.createTextNode(","));
            root.appendChild(d_sep);
            Element redraw = doc.createElement("redraw");
            redraw.appendChild(doc.createTextNode("1"));
            root.appendChild(redraw);

            Element balloon = doc.createElement("balloon");
            root.appendChild(balloon);

            Element show = doc.createElement("show");
            show.appendChild(doc.createTextNode("{title} ({value})  {percents}%    <br>{description} "));
            balloon.appendChild(show);

            Element legend = doc.createElement("legend");
            root.appendChild(legend);
            Element enabled = doc.createElement("enabled");
            enabled.appendChild(doc.createTextNode("0"));
            legend.appendChild(enabled);
            Element align = doc.createElement("align");
            align.appendChild(doc.createTextNode("center"));
            legend.appendChild(align);

            Element pie = doc.createElement("pie");
            root.appendChild(pie);

            Element pie_y = doc.createElement("y");
            pie_y.appendChild(doc.createTextNode("50%"));
            pie.appendChild(pie_y);
            Element pie_height = doc.createElement("height");
            pie_height.appendChild(doc.createTextNode("35"));
            pie.appendChild(pie_height);
            Element pie_angle = doc.createElement("angle");
            pie_angle.appendChild(doc.createTextNode("15"));
            pie.appendChild(pie_angle);

            Element animation = doc.createElement("animation");
            root.appendChild(animation);

            Element pull_out_time = doc.createElement("pull_out_time");
            pull_out_time.appendChild(doc.createTextNode("0.5"));
            animation.appendChild(pull_out_time);

            Element data_labels = doc.createElement("data_labels");
            root.appendChild(data_labels);

            Element data_labels_show = doc.createElement("show");
            data_labels_show.appendChild(doc.createTextNode("{title} ({value})"));
            data_labels.appendChild(data_labels_show);
            Element data_labels_maxWidth = doc.createElement("max_width");
            data_labels_maxWidth.appendChild(doc.createTextNode("140"));
            data_labels.appendChild(data_labels_maxWidth);

            Element labels = doc.createElement("labels");
            root.appendChild(labels);

            Element label = doc.createElement("label");
            label.setAttribute("lid", "0");
            labels.appendChild(label);

            Element label_text = doc.createElement("text");
            label_text.appendChild(doc.createTextNode(title));
            label.appendChild(label_text);
            Element label_text_size = doc.createElement("text_size");
            label_text_size.appendChild(doc.createTextNode("15"));
            label.appendChild(label_text_size);
            Element label_align = doc.createElement("align");
            label_align.appendChild(doc.createTextNode("center"));
            label.appendChild(label_align);

            Element strings = doc.createElement("strings");
            root.appendChild(strings);

            Element no_data = doc.createElement("no_data");
            no_data.appendChild(doc.createTextNode("There are no reports for you at this time"));
            strings.appendChild(no_data);

            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();

            //Create string from XML tree

            StringWriter sw = new StringWriter();
            StreamResult res = new StreamResult(sw);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, res);
            xmlString = sw.toString().substring(54, sw.toString().length()); //Remove <?xml ?> tag for charts
            ll.add(xmlString);
        } catch (AbstractMethodError e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (TransformerConfigurationException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } catch (TransformerException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return new KwlReturnObject(true, KwlReturnMsg.S01, "", ll, 0);
    }
}
