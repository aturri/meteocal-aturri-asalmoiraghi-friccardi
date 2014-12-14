/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.business.logic;

import it.polimi.meteocal.business.entity.Group;
import it.polimi.meteocal.business.entity.User;
import java.security.Principal;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Andrea
 */
@Stateless
public class UserManager {

    @PersistenceContext
    EntityManager em;
    
    @Inject
    Principal principal;

    public void save(User user) {
        user.setGroupName(Group.USER);
        em.persist(user);
    }

    public void unregister() {
        em.remove(getLoggedUser());
    }

    public User getLoggedUser() {
        return em.find(User.class, principal.getName());
    }
    
}
