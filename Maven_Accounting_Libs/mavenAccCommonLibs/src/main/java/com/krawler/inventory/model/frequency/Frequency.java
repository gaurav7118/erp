/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.frequency;

/**
 *
 * @author Vipin Gupta
 */
public class Frequency {
    
    public static final int DAILY = 0;
    public static final int WEEKLY = 1;
    public static final int FORTNIGHT = 2;
    public static final int MONTHLY = 3;

    private Integer id;
    private String name;
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
