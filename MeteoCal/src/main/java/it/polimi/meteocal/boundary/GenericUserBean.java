package it.polimi.meteocal.boundary;

import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import it.polimi.meteocal.utils.Utility;
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
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }
    
     /**
     * @return the picture
     */
    public StreamedContent getPicture() {        
        return Utility.getPictureFromUser(user);
    }
}
