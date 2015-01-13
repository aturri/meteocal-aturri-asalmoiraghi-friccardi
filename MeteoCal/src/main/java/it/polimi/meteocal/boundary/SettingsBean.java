/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template exportedFile, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.control.Utility;
import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author Fabiuz
 */
@Named
@RequestScoped
public class SettingsBean {

    @EJB
    UserManager userManager;
    
    private User user;
    
    private String oldPassword;
    private String newPassword;
    private DefaultStreamedContent exportedFile;
    private UploadedFile uploadedFile;
    
    public SettingsBean() {
    }
    
    public String importData(){
        //move the uploadedFile in the common folder for the application
        OutputStream outputStream=null;
            try {
            outputStream = new FileOutputStream("_import.xml");
            InputStream inputStream=this.getUploadedFile().getInputstream();
            byte[] buffer = new byte[4096];          
            int bytesRead = 0;  
            while(true) {                          
                bytesRead = inputStream.read(buffer);  
                if(bytesRead > 0) {  
                    outputStream.write(buffer, 0, bytesRead);  
                }else {  
                    break;  
                }                         
            }  
            outputStream.flush();
            /*try {
                File fXmlFile = new File("/Users/mkyong/staff.xml");
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fXmlFile);
                
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(SettingsBean.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(SettingsBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            */
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SettingsBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SettingsBean.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                outputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(SettingsBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return "";
    }
    
    /**
     * This function create the exportedFile that have to exported
 NB:il percorso in cui posso scrivere parte da netbeans/glassfish-4.1/
      fuori dal quella cartella non ho i permessi
     * @return an empty string
     */
    public String export(){
        User currentUser=userManager.getLoggedUser();
        try {
            //Read the user data
            try (FileWriter out = new FileWriter(currentUser.getEmail()+"_export.xml")) {
                //Read the user data
                out.write("<user>\n");

                out.write("\t<email>"+currentUser.getEmail()+"</email>\n");
                out.write("\t<name>"+currentUser.getName()+"</name>\n");
                out.write("\t<surname>"+currentUser.getSurname()+"</surname>\n");
                out.write("\t<birthday>"+currentUser.getBirthDate()+"</birthday>\n");
                out.write("\t<city>"+currentUser.getCity()+"</city>\n");
                out.write("\t<address>"+currentUser.getAddress()+"</address>\n");
                out.write("\t<gender>"+currentUser.getGender()+"</gender>\n");
                out.write("\t<privatecalendar>"+currentUser.getPrivateCalendar()+"</privatecalendar>\n");

                //for to insert events data into xml exportedFile
                Set<Event> listOfEventInCalendar=currentUser.getEvents();
                out.write("\t<events>\n");
                for(Event event:listOfEventInCalendar){
                    out.write("\t\t<event>\n");
                    out.write("\t\t\t<title>"+event.getTitle()+"</title>\n");
                    out.write("\t\t\t<description>"+event.getDescription()+"</description>\n");
                    out.write("\t\t\t<locationinfo>"+event.getLocationInfo()+"</locationinfo>\n");
                    out.write("\t\t\t<city>"+event.getCity()+"</city>\n");
                    out.write("\t\t\t<address>"+event.getAddress()+"</address>\n");
                    out.write("\t\t\t<creator>"+event.getCreatedEvent()+"</creator>\n");
                    out.write("\t\t\t<createdevent>"+event.getCreatedEvent()+"</createdevent>\n");
                    out.write("\t\t\t<begindate>"+event.getBeginDate()+"</begindate>\n");
                    out.write("\t\t\t<enddate>"+event.getEndDate()+"</enddate>\n");
                    out.write("\t\t\t<publicevent>"+event.getPublicEvent()+"</publicevent>\n");
                    out.write("\t\t\t<indoor>"+event.getIndoor()+"</indoor>\n");
                    out.write("\t\t</event>\n");
                }
                out.write("\t</events>\n");
                out.write("</user>");
                out.flush();
                out.close();
                InputStream stream = new FileInputStream(currentUser.getEmail()+"_export.xml");
                this.exportedFile=new DefaultStreamedContent(stream,"text/xml", currentUser.getEmail()+"_export.xml");
            }
        } catch (IOException ex) {
            Logger.getLogger(SettingsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        MessageBean.addInfo("ExportMessages","Data  correctly exported");
        return "";
    }


    public String changePassword(){
        //verify the old password is correct
        String old=Utility.getHashSHA256(this.oldPassword);
        User currentUser=userManager.getLoggedUser();
        String saved=currentUser.getPassword();
        if(!saved.equals(old)){
            MessageBean.addError("PasswordMessages", "Insert the right password before change it");
        }else{
            //password update
            //NB: we don't need to hash the new password. This is computed from glassfish automatically
            currentUser.setPassword(this.newPassword);
            userManager.update(currentUser);
            MessageBean.addInfo("PasswordMessages","Password correctly changed");
        }
        return "";
    }
    
    public String changeUsersData(){
        userManager.update(this.user);
        MessageBean.addInfo("UserDataMessages","User's data correctly changed");
        return "";
    }
    
    public String changeCalendarVisibilityData(){
        userManager.update(this.user);
        MessageBean.addInfo("VisibilityMessages","Calendar's visibility correctly changed");
        return "";
    }
    
    public String changeTheme(){
        userManager.update(this.user);
        MessageBean.addInfo("ThemeMessages","Theme correctly changed");
        return "";
    }
    
    /**
     * @return the oldPassword
     */
    public String getOldPassword() {
        return oldPassword;
    }

    /**
     * @param oldPassword the oldPassword to set
     */
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    /**
     * @return the newPassword
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * @param newPassword the newPassword to set
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    /**
     * @return the user
     */
    public User getUser() {
        if(this.user==null){
            this.user=userManager.getLoggedUser();
        }
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the exportedFile
     */
    public DefaultStreamedContent getFile() {
        this.export();
        return exportedFile;
    }

    /**
     * @param file the exportedFile to set
     */
    public void setFile(DefaultStreamedContent file) {
        this.exportedFile = file;
    }

    /**
     * @return the uploadedFile
     */
    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    /**
     * @param uploadedFile the uploadedFile to set
     */
    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

}
