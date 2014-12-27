/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

//import com.google.common.base.Charsets;
//import com.google.common.hash.Hashing;
import it.polimi.meteocal.control.KindOfEmail;
import it.polimi.meteocal.control.MailController;
import it.polimi.meteocal.control.NavigationBean;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
import javax.mail.Session;

/**
 *
 * @author Fabiuz
 */
@Named
@RequestScoped
public class RecoverPasswordBean {

    @Resource(name = "mail/mailSession")
    private Session mailSession;
    private MailController mailControl;
    
    @EJB
    private UserManager userManager;
    
    private String email;
    private String password1;
    private String password2;
    private String code;
    /**
     * Creates a new instance of RecoverPasswordBean
     */
    public RecoverPasswordBean() {
    }
    
    public String recoverPassword(){
        if(!userManager.existsUser(email)){
            MessageBean.addError("User not found");
            return "";
        }
        else{
            User user=userManager.findByEmail(email);
            mailControl=new MailController(mailSession);
            try {
                this.mailControl.sendMail(email,KindOfEmail.FORGOTTENPASSWORD,null );
            } catch (MessagingException | UnsupportedEncodingException ex) {
                Logger.getLogger(RecoverPasswordBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            MessageBean.addInfo("Recover link sent to the specified email");
        }
        return NavigationBean.redirectToIndex();
    }
    
    public String setNewPassword(){
        Map<String, String> params =FacesContext.getCurrentInstance().
                   getExternalContext().getRequestParameterMap();
        String codeFromEmail = params.get("code");
        String emailFromEmail=params.get("email");
        System.out.println(""+ params.keySet());

        
        if(!userManager.existsUser(emailFromEmail)){
            MessageBean.addError("User not found");
            return "";
        }
        //verifica che il codice sia corretto
        User user=userManager.findByEmail(emailFromEmail);
        String aux1=this.getCodeFromUser(user);
        if(!aux1.equals(codeFromEmail)){
            MessageBean.addError("The code is not valid");
            return "";
        }
        //verifica che la password siano corrette
        if(!this.password1.equals(this.password2)){
            MessageBean.addError("password don't match");
            //returns null implies that the page is the same
            return "";
        }
        
        //Set the new password
        user.setPassword(password1);
        userManager.update(user);
        
        return NavigationBean.redirectToIndex();
    }

    /**
     * @return the email
     */
    public String getEmail() {
        if(this.email==null){
            Map<String, String> params =FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            String emailFromEmail = params.get("email");
            if(emailFromEmail!=null){
                this.email=emailFromEmail;
            }
        }
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the password1
     */
    public String getPassword1() {
        return password1;
    }

    /**
     * @param password1 the password1 to set
     */
    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    /**
     * @return the password2
     */
    public String getPassword2() {
        return password2;
    }

    /**
     * @param password2 the password2 to set
     */
    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public String getCode() {
        if(this.code==null){
            String codeFromEmail = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("code");
            if(codeFromEmail==null){
            } else {
                this.code=codeFromEmail;
            }
        }
        return this.code;
    }
    
    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }
    
    
    
    /**
     * 
     * @param user
     * @return the string that represents the code
     */
    private String getCodeFromUser(User user){
        String string=user.getEmail()+user.getCity()+user.getLastAccess().toString()+user.getPassword();
        
        MessageDigest md=null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(RecoverPasswordBean.class.getName()).log(Level.SEVERE, null, ex);
        }
         
        for(int i=0;i<string.length();i++){
            md.update(string.getBytes());
        }
        
        byte[] mdbytes = md.digest();
        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        System.out.println("Hex format : " + sb.toString());

        return sb.toString();
    }
    
    
}
