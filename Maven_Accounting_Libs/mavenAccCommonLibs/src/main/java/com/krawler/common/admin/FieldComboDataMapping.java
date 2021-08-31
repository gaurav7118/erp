/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class FieldComboDataMapping {

    private String id;
    private FieldComboData parent;
    private FieldComboData child;

    public FieldComboData getChild() {
        return child;
    }

    public void setChild(FieldComboData child) {
        this.child = child;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FieldComboData getParent() {
        return parent;
    }

    public void setParent(FieldComboData parent) {
        this.parent = parent;
    }
}
