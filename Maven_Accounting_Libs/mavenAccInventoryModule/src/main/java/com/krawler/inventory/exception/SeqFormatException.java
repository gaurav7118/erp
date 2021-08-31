/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.exception;

/**
 *
 * @author Vipin Gupta
 */
public class SeqFormatException extends Exception {

    private Type type;

    public SeqFormatException(Type type) {
        super(type.toString());
        this.type = type;
    }
    public SeqFormatException(String message) {
        super(message);
    }

    public SeqFormatException() {
        this(Type.NOT_FOUND);
    }

    public Type getType() {
        return type;
    }

    public static enum Type {

        NOT_FOUND("Sequence format not found."),
        NOT_VALID("Sequence format is not valid."),
        ALREADY_EXISTS("Sequence format is already exists."),
        ALREADY_INACTIVE("Sequence format is already deactive."),
        ALREADY_ACTIVE("Sequence format is already active."),
        EXPIRED("Sequence format is already expired. Please use another sequence format."),
        DEFAULT_NOT_FOUND("No default Sequence Format found. Please add a default format for selected module."),
        INACTIVE_FOR_DEFAULT("You can set Sequence format as default only for active sequence format.");
        private String message;

        Type(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message.toString();
        }
    }
}
