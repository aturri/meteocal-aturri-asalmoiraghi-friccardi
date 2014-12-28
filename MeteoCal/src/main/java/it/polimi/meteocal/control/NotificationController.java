/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.control;

import it.polimi.meteocal.entityManager.UserManager;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Andrea
 */
@Stateless
public class NotificationController {

    @EJB
    UserManager userManager;
    
    private final Map<KindOfEmail,String> messages=new EnumMap<>(KindOfEmail.class);

    /**
     * Initialize the Map with default subjects and messages
     */
    @PostConstruct
    private void init(){
        messages.put(KindOfEmail.INVITEDTOEVENT, ""
                + "You are invited from %s %s to partecipate to %s. Click for further details.");
        messages.put(KindOfEmail.EVENTCANCELLED, ""
                + "%s event has been cancelled!");
        messages.put(KindOfEmail.EVENTUPDATED, ""
                + "%s event has been updated. Click for further details.");         
    }    
}
