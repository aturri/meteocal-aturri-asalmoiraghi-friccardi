/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.control;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

/**
 * This is the class that send the Email. The only task of this class is send the email, 
 * not create or setting anything
 * @author Fabiuz
 */
public class SendEmailThread implements Runnable{
    
    private Message message;
    
    @Override
    public void run() {
        if(null!=this.getMessage()){
            try {
                Transport.send(getMessage());
                System.out.println("I've sent an email to "+ Arrays.toString(message.getAllRecipients()));
            } catch (MessagingException ex) {
                Logger.getLogger(SendEmailThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            Logger.getLogger(SendEmailThread.class.getName()).log(Level.SEVERE, "Non puoi mandare email se prima non setti il messaggio");
        }
    }

    /**
     * @return the message
     */
    public Message getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(Message message) {
        this.message = message;
    }
    
    
}
