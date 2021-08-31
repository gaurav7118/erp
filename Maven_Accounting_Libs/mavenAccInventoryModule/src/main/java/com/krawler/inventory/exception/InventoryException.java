/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.exception;

/**
 *
 * @author Vipin Gupta
 */
public class InventoryException extends RuntimeException {

    private Type type;

    public InventoryException(String message) {
        super(message);
    }

    public InventoryException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public InventoryException(Type type) {
        super(type.toString());
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public static enum Type {

        NULL, INVALID, ALREADY_EXISTS;
    }
}
