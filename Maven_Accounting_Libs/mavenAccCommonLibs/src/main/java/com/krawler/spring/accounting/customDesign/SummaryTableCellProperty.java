/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customDesign;

/**
 *
 * @author krawler
 */
public class SummaryTableCellProperty {
    private String data;
    private String colspan;
    private String rowspan;
    private String style;

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }
    

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


    public String getColspan() {
        return colspan;
    }

    public void setColspan(String colspan) {
        this.colspan = colspan;
    }


    public String getRowspan() {
        return rowspan;
    }

    public void setRowspan(String rowspan) {
        this.rowspan = rowspan;
    }


}
