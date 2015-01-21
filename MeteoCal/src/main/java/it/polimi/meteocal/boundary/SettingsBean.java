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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.imageio.stream.FileImageOutputStream;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import org.primefaces.event.CaptureEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author Fabiuz
 */
@Named
@RequestScoped
public class SettingsBean {

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
    private UploadedFile uploadedPicture;

    public SettingsBean() {
    }

    public String importPicture() {
        int i = (int) uploadedPicture.getSize();
        User currentUser = userManager.getLoggedUser();
        if (i > 0) {
            try {
                importExportController.saveUploadedFileIntoTheCorrectFolder(currentUser.getEmail() + "_picture.png", uploadedPicture.getInputstream());
            } catch (IOException ex) {
                MessageBean.addError("PictureMessages", "Error during picture upload");
                Logger.getLogger(SettingsBean.class.getName()).log(Level.INFO, "Image not uploaded");
            }
            try {
                byte[] b = Utility.getBytesFromFile(currentUser.getEmail() + "_picture.png");
                currentUser.setPicture(b);
                currentUser.setPictureType(uploadedPicture.getContentType());
                userManager.update(currentUser);
                this.importExportController.controlAndDeleteFile(new File(currentUser.getEmail() + "_picture.png"));
                MessageBean.addInfo("PictureMessages", "Picture correctly changed");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SettingsBean.class.getName()).log(Level.INFO, "Image not uploaded");
                MessageBean.addError("PictureMessages", "Error during picture upload");
            } catch (IOException ex) {
                Logger.getLogger(SettingsBean.class.getName()).log(Level.INFO, "Image not uploaded");
                MessageBean.addError("PictureMessages", "Error during picture upload");
            }
        } else {
            currentUser.setPicture(null);
            currentUser.setPictureType(null);
            userManager.update(currentUser);
            MessageBean.addWarning("PictureMessages", "Default picture loaded");
        }
        return "";
    }

    /**
     * This function read the uploaded file and try to insert the events into
     * the calendar's user NB: i conflitti temporali tra gli eventi memorizzati
     * nel file xml non vengono contorllati, semplicemente se ci sono conflitti
     * di questo tipo viene preso il primo evento e gli altri che creano
     * conflitti non vengono importati, ma questo non viene segnalato in alcun
     * modo; invece vengono segnalati conflitti tra eventi in xml e nel
     * calendario dell'utente
     *
     * @return
     */
    public String importData() {
        //move the uploadedFile in the common folder for the application
        try {
            importExportController.saveUploadedFileIntoTheCorrectFolder(userManager.getLoggedUser().getEmail() + "_import.xml", this.uploadedFile.getInputstream());
        } catch (IOException ex) {
            MessageBean.addError("ExportMessages","An error occurred while uploading the file!");
        }
        List<Event> importedEvents = this.importExportController.readXmlFile(userManager.getLoggedUser().getEmail() + "_import.xml");
        if (importedEvents == null) {
            MessageBean.addError("ExportMessages","One or more events cannot be imported. No event has been imported!");
            return "";
        }
        //Now we can put them into the DB
        for (Event event : importedEvents) {
            try {
                eventController.createEvent(event, null);
            } catch (EventOverlapException | IllegalInvitedUserException | IllegalEventDateException ex) {
                Logger.getLogger(SettingsBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        MessageBean.addInfo("ExportMessages","Events are correctly imported.");
        return "";
    }

    /**
     * This function create the exportedFile that have to exported NB:il
     * percorso in cui posso scrivere parte da netbeans/glassfish-4.1/ fuori dal
     * quella cartella non ho i permessi
     *
     * @return an empty string
     */
    public String export() {
        User currentUser = userManager.getLoggedUser();
        try {
            importExportController.createFileToExport(currentUser.getEmail() + "_export.xml");
            InputStream stream = new FileInputStream(currentUser.getEmail() + "_export.xml");
            this.exportedFile = new DefaultStreamedContent(stream, "text/xml", currentUser.getEmail() + "_export.xml");
            //i can't close the stream at this point because it is downloading
            MessageBean.addInfo("ExportMessages", "Calendar  correctly exported");

        } catch (IOException ex) {
            Logger.getLogger(SettingsBean.class.getName()).log(Level.SEVERE, null, ex);
            MessageBean.addError("ExportMessages", "Error during exporting calendar");

        }
        return "";
    }

    public String changePassword() {
        //verify the old password is correct
        String old = Utility.getHashSHA256(this.oldPassword);
        User currentUser = userManager.getLoggedUser();
        String saved = currentUser.getPassword();
        if (!saved.equals(old)) {
            MessageBean.addError("PasswordMessages", "Insert the right password before change it");
        } else {
            //password update
            //NB: we don't need to hash the new password. This is computed from glassfish automatically
            currentUser.setPassword(this.newPassword);
            userManager.update(currentUser);
            MessageBean.addInfo("PasswordMessages", "Password correctly changed");
        }
        return "";
    }

    public String changeUsersData() {
        System.out.println("qui");
        userManager.update(this.user);
        System.out.println("dopo");

        MessageBean.addInfo("UserDataMessages", "User's data correctly changed");
        return "";
    }

    public String changeCalendarVisibilityData() {
        userManager.update(this.user);
        MessageBean.addInfo("VisibilityMessages", "Calendar's visibility correctly changed");
        return "";
    }

    public String changeTheme() {
        userManager.update(this.user);
        MessageBean.addInfo("ThemeMessages", "Theme correctly changed");
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
        if (this.user == null) {
            this.user = userManager.getLoggedUser();
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

    /**
     * @return the uploadedPicture
     */
    public UploadedFile getUploadedPicture() {
        return uploadedPicture;
    }

    /**
     * @param uploadedPicture the uploadedPicture to set
     */
    public void setUploadedPicture(UploadedFile uploadedPicture) {
        this.uploadedPicture = uploadedPicture;
    }

    /**
     * @return the picture of the logged User
     */
    public StreamedContent getPicture() {
        return Utility.getPictureFromUser(userManager.getLoggedUser());
    }

    private String filename;

    private String getRandomImageName() {
        int i = (int) (Math.random() * 10000000);

        return String.valueOf(i);
    }

    public String getFilename() {
        return filename;
    }

    //To improve the update, we could use a number to identify the attempt of the user in shotting photo
    public void oncapture(CaptureEvent captureEvent) {
        File f=new File(this.pathOfPhotocam()+File.separator+userManager.getLoggedUser().getEmail() + "_photocam.jpeg");
        f.delete();
        
        byte[] data = captureEvent.getData();
        String path=this.pathOfPhotocam();
        
        System.out.println(path);
        String pathAndNameFile=path+ File.separator+userManager.getLoggedUser().getEmail() + "_photocam.jpeg";
        FileImageOutputStream imageOutput;
        try {
            imageOutput = new FileImageOutputStream(new File(pathAndNameFile));
            imageOutput.write(data, 0, data.length);
            imageOutput.close();
        } catch (IOException e) {
            throw new FacesException("Error in writing captured image.", e);
        }
    }

    /**
     * Says if the photocam file exists (it controls the current user)
     *
     * @return
     */
    public boolean existsFile() {
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        String newPathName = servletContext.getRealPath("") + File.separator + "resources"
                + File.separator + "images" + File.separator + "photocam" + File.separator + userManager.getLoggedUser().getEmail() + "_photocam.jpeg";
        File f = new File(newPathName);
        return f.exists();
    }
    
    public String pathOfPhotocam(){
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        String newPathName = servletContext.getRealPath("") + File.separator +".."+File.separator+".."+File.separator+ 
                                "src"+File.separator+"main"+File.separator+"webapp"+File.separator+"resources" +
                                File.separator + "images" + File.separator + "photocam" ;
        return newPathName;
    }
    
    public String importPictureFromWebcam(){
        System.out.println("trying to save");
        User currentUser=userManager.getLoggedUser();
        try {
             currentUser.setPicture(Utility.getBytesFromFile(this.pathOfPhotocam() + File.separator + userManager.getLoggedUser().getEmail() + "_photocam.jpeg"));
        } catch (IOException ex) {
            Logger.getLogger(SettingsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        currentUser.setPictureType("image/jpg");
        userManager.update(currentUser);
        importExportController.controlAndDeleteFile(new File(this.pathOfPhotocam() + File.separator + userManager.getLoggedUser().getEmail() + "_photocam.jpeg"));
        return "";
    }
}
