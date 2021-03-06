package it.polimi.meteocal.boundary;

import it.polimi.meteocal.utils.MessageUtility;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import it.polimi.meteocal.utils.DateUtils;
import it.polimi.meteocal.utils.Utility;
import java.util.Date;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Fabiuz
 */
@Named
@RequestScoped
public class GenericUserBean {

    @Inject
    UserManager userManager;
    
    private User user;
    
    /**
     * Creates a new instance of GenericUserBean
     */
    public GenericUserBean() {
    }
    
    @PostConstruct
    public void init() {
        if(this.user == null && existsParam("email")){
            setUserByParam();
        }
        else{
            user=userManager.getLoggedUser();
        }
    }
    
    /**
     * Load the user from the email
     */
    public void setUserByParam() throws RuntimeException {
        if(this.existsParam("email")){
            String email = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("email");
            this.user=userManager.findByEmail(email);
        }else{
            throw new RuntimeException();
        }
    }
    
    /**
     * Check if the parameter exists
     * @param param
     * @return 
     */
    public boolean existsParam(String param){
        Map<String,String> map=FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        return map.containsKey(param);
    }

    /**
     * Return the current loaded user
     * @return the user
     */
    public User getUser() {
        return user;
    }
    
    /**
     * Return the formatted MM/DD/YYYY birthdate of the current user
     * @return formatted birthdate
     */
    public String getBirthdateFormatted(){
        Date userBirth = user.getBirthDate();
        if(userBirth!=null)
            return DateUtils.formatNumericDate(user.getBirthDate());
        return "n.a.";
    }

    /**
     * Set the current user
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }
    
     /**
      * Get the picture of the current user
     * @return the picture
     */
    public StreamedContent getPicture() {        
        return Utility.getPictureFromUser(user);
    }
    
    /**
     * This method adds the current user to the loggedUser's favorites.
     */
    public void addToFavorite(){
        User loggedUser=userManager.getLoggedUser();
        if(user!=null && !user.equals(loggedUser) 
                && !loggedUser.getContacts().contains(user)) {
            loggedUser.getContacts().add(user);
            userManager.update(loggedUser);  
            MessageUtility.addInfo("contactsMessage","User added to favorites");
        }
    }
    
    /**
     * This method remove the current user from the loggedUser's favorites.
     */
    public void removeFromFavorite(){
        User loggedUser=userManager.getLoggedUser();
        if(user!=null && !user.equals(loggedUser) 
                && loggedUser.getContacts().contains(user)) {
            loggedUser.getContacts().remove(user);
            userManager.update(loggedUser);  
            MessageUtility.addInfo("contactsMessage","User removed from favorites");
        }
    }
    
    /**
     * This method says if a user is a contact for current user
     * @return true if the current user is contact for the logged one
     */
    public Boolean isContact() {
        User loggedUser=userManager.getLoggedUser();
        return loggedUser.getContacts().contains(user);
    }
}
