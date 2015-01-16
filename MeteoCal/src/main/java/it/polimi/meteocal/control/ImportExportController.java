/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.control;

import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.primefaces.model.DefaultStreamedContent;

/**
 *
 * @author Fabiuz
 */
@Stateless
public class ImportExportController {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-M-d HH:mm zzz");

    
    @EJB
    UserManager userManager;
    
    /**
     * This function create the file to be exported
     * @param filename the name of the file (or the pathname)
     * @throws java.io.IOException
     */
    public void createFileToExport(String filename) throws IOException{
        User currentUser=userManager.getLoggedUser();
        //Read the user data
        try (FileWriter out = new FileWriter(filename)) {
            //Read the user data
            out.write("<user>\n");
            out.write("\t<email>"+currentUser.getEmail()+"</email>\n");
            out.write("\t<name>"+currentUser.getName()+"</name>\n");
            out.write("\t<surname>"+currentUser.getSurname()+"</surname>\n");
            if(null!=currentUser.getBirthDate()){
                out.write("\t<birthday>"+currentUser.getBirthDate()+"</birthday>\n");
            }
            if(null!=currentUser.getCity()){
                out.write("\t<city>"+currentUser.getCity()+"</city>\n");
            }
            if(null!=currentUser.getAddress()){
                out.write("\t<address>"+currentUser.getAddress()+"</address>\n");
            }
            out.write("\t<gender>"+currentUser.getGender()+"</gender>\n");
            out.write("\t<privatecalendar>"+currentUser.getPrivateCalendar()+"</privatecalendar>\n");

            //for to insert events data into xml exportedFile
            Set<Event> listOfEventInCalendar=currentUser.getEvents();
            out.write("\t<events>\n");
            for(Event event:listOfEventInCalendar){
                out.write("\t\t<event>\n");
                out.write("\t\t\t<title>"+event.getTitle()+"</title>\n");
                if(null!=event.getDescription()){
                    out.write("\t\t\t<description>"+event.getDescription()+"</description>\n");
                }
                if(null!=event.getLocationInfo()){
                    out.write("\t\t\t<locationinfo>"+event.getLocationInfo()+"</locationinfo>\n");
                }
                if(null!=event.getCity()){
                    out.write("\t\t\t<city>"+event.getCity()+"</city>\n");
                }
                if(null!=event.getAddress()){
                    out.write("\t\t\t<address>"+event.getAddress()+"</address>\n");
                }
                out.write("\t\t\t<creator>"+event.getCreator().getEmail()+"</creator>\n");
                out.write("\t\t\t<createdevent>"+FORMATTER.format(event.getCreatedEvent())+"</createdevent>\n");
                out.write("\t\t\t<begindate>"+FORMATTER.format(event.getBeginDate())+"</begindate>\n");
                out.write("\t\t\t<enddate>"+FORMATTER.format(event.getEndDate())+"</enddate>\n");
                out.write("\t\t\t<publicevent>"+event.getPublicEvent()+"</publicevent>\n");
                out.write("\t\t\t<indoor>"+event.getIndoor()+"</indoor>\n");
                out.write("\t\t</event>\n");
            }
            out.write("\t</events>\n");
            out.write("</user>");
            out.flush();
            out.close();
        }
    }
}
