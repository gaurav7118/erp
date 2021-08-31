/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.util;

/**
 *
 * @author krawler
 */
public enum ItemReusability {

    REUSABLE, DISPOSABLE;

    public String getStringName() {
        String name = "";
        switch (this) {
            case REUSABLE:
                name = "Reusable";
                break;
            case DISPOSABLE:
                name = "Disposable";
                break;
        }
        return name;
    }
}
