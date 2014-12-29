/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.exception;

/**
 *
 * @author Fabiuz
 */
public class InvalidArgumentException extends RuntimeException {

    /**
     * Creates a new instance of <code>NewException</code> without detail
     * message.
     */
    public InvalidArgumentException() {
    }

    /**
     * Constructs an instance of <code>NewException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public InvalidArgumentException(String msg) {
        super(msg);
    }
}
