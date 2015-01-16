/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template exportedFile, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.boundary;

import it.polimi.meteocal.control.EventController;
import it.polimi.meteocal.control.ImportExportController;
import it.polimi.meteocal.utils.Utility;
import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import it.polimi.meteocal.exception.EventOverlapException;
import it.polimi.meteocal.exception.IllegalEventDateException;
import it.polimi.meteocal.exception.IllegalInvitedUserException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.UploadedFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Fabiuz
 */
@Named
@RequestScoped
public class SettingsBean {
    
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-M-d HH:mm zzz");

    @Inject
    UserManager userManager;
    
    @Inject
    EventController eventController;
    
    @Inject
    ImportExportController importExportController;
    
    
    private User user;
    
    private String oldPassword;
    private String newPassword;
    private DefaultStreamedContent exportedFile;
    private UploadedFile uploadedFile;
    
    public SettingsBean() {
    }
    
    /**
     * This function read the uploadedfile and try to insert the events into the calendar's user
     *  NB: i conflitti temporali tra gli eventi memorizzati nel file xml non vengono contorllati, 
     *      semplicemente se ci sono conflitti di questo tipo viene preso il primo evento e 
     *      gli altri che creano conflitti non vengono importati, ma questo non viene segnalato in alcun modo; 
     *      invece vengono segnalati conflitti tra eventi in xml e nel calendario dell'utente
     * @return 
     */
    public String importData(){
        //move the uploadedFile in the common folder for the application
        OutputStream outputStream=null;
        List<Event> importedEvents=new ArrayList<>();
        User current=userManager.getLoggedUser();
            try {
                outputStream = new FileOutputStream(current.getEmail()+"_import.xml");
                InputStream inputStream=this.uploadedFile.getInputstream();
                byte[] buffer = new byte[4096];          
                int bytesRead;  
                while(true) {                          
                    bytesRead = inputStream.read(buffer);  
                    if(bytesRead > 0) {  
                        outputStream.write(buffer, 0, bytesRead);  
                    }else {  
                        break;  
                    }                         
                }
                outputStream.flush();
                inputStream.close();
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
            try {
                File xmlFile = new File(current.getEmail()+"_import.xml");
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(xmlFile);
                
                //here start the core of import
                //the initial data(regarding the user will be skipped.
                //The events will be fully read
                NodeList eventList = doc.getElementsByTagName("event");
                Event event;
                for (int i = 0; i < eventList.getLength(); i++) {
                    event=new Event();
                    
                    Node eventNode=eventList.item(i);
                    if (eventNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element element=(Element) eventNode;
                        System.out.println("Processing event with title : " + element.getElementsByTagName("title").item(0).getTextContent());
                        event.setTitle(element.getElementsByTagName("title").item(0).getTextContent());
                        if(element.getElementsByTagName("description").item(0)!=null){
                            event.setDescription(element.getElementsByTagName("description").item(0).getTextContent());
                        }
                        if(element.getElementsByTagName("locationinfo").item(0)!=null){
                            event.setLocationInfo(element.getElementsByTagName("locationinfo").item(0).getTextContent());
                        }
                        if(element.getElementsByTagName("city").item(0)!=null){
                            event.setCity(element.getElementsByTagName("city").item(0).getTextContent());
                        }
                        if(element.getElementsByTagName("address").item(0)!=null){
                            event.setAddress(element.getElementsByTagName("address").item(0).getTextContent());
                        }
                        event.setCreator(current);
                        try {
                            event.setCreatedEvent(new Date());
                            event.setBeginDate(FORMATTER.parse(element.getElementsByTagName("begindate").item(0).getTextContent()));
                            event.setEndDate(FORMATTER.parse(element.getElementsByTagName("enddate").item(0).getTextContent()));
                        } catch (ParseException ex) {
                            Logger.getLogger(SettingsBean.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        event.setPublicEvent(Boolean.FALSE);
                        event.setIndoor(Utility.stringToBoolean(element.getElementsByTagName("indoor").item(0).getTextContent()));
                        
                        if(!eventController.isLegalEvent(event, null)){
                            this.controlAndDeleteFile(xmlFile);
                            System.out.println("One or more events can't be imported");
                            //Message on website that say:"One or more events can't be imported"
                            return "";
                        }
                        importedEvents.add(event);
                    }
                }
                this.controlAndDeleteFile(xmlFile);
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                Logger.getLogger(SettingsBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //Now we can put them into the DB
            for(Event event:importedEvents){
               try {
                    eventController.createEvent(event, null);
                } catch (EventOverlapException | IllegalInvitedUserException | IllegalEventDateException ex) {
                    Logger.getLogger(SettingsBean.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //Message that says "All the event are been correctly imported"
        return "";
    }
    
    /**
     * Delete the file and controls that it is a file with write permission
     * @param file file to be deleted
     */
    private void controlAndDeleteFile(File file){
        if(file.canWrite()&&file.isFile()){
            System.out.println("Permission are ok to delete the file");
        }
        if(file.delete()){
            System.out.println("File deletion complete");
        }else{
            System.out.println("The events are uploaded, but the uploaded file can't be removed...");
        }
    }
    
    
    /**
     * This function create the exportedFile that have to exported
     *  NB:il percorso in cui posso scrivere parte da netbeans/glassfish-4.1/
     *      fuori dal quella cartella non ho i permessi
     * @return an empty string
     */
    public String export(){
        User currentUser=userManager.getLoggedUser();
        try {
            importExportController.createFileToExport(currentUser.getEmail()+"_export.xml");
            InputStream stream = new FileInputStream(currentUser.getEmail()+"_export.xml");
            this.exportedFile=new DefaultStreamedContent(stream,"text/xml", currentUser.getEmail()+"_export.xml");
            //i can't close the stream at this point because it is downloading
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
