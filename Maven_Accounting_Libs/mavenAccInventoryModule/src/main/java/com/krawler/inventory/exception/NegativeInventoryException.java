/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.exception;

/**
 *
 * @author krawler
 */
public class NegativeInventoryException extends RuntimeException {

    private Type type;

    public NegativeInventoryException(String message) {
        super(message);
    }

    public NegativeInventoryException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public NegativeInventoryException(Type type) {
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

        ALLOW, WARN, BLOCK;
    }
}
