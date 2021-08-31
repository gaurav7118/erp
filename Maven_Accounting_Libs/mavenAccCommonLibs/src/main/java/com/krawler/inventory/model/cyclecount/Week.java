/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.cyclecount;

/**
 *
 * @author Vipin Gupta
 */
public enum Week {

    SUNDAY("Sunday"),
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday");
    String name;

    private Week(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
