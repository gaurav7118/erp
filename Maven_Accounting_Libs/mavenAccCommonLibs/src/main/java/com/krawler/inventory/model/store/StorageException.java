/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.store;

/**
 *
 * @author Vipin Gupta
 */
public class StorageException extends RuntimeException{

    private Type type;
    
    public StorageException(String message) {
        super(message);
    }
    public StorageException(Type type, String message) {
        super(message);
        this.type = type;
    }
    public StorageException(Type type) {
        super(type.toString());
        this.type = type;
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
    
    public static enum Type{
        NULL, INVALID, ALREADY_EXISTS;
    } 
    
    
}
