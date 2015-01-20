/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.polimi.meteocal.control;

import it.polimi.meteocal.boundary.SettingsBean;
import it.polimi.meteocal.entity.Event;
import it.polimi.meteocal.entity.User;
import it.polimi.meteocal.entityManager.UserManager;
import it.polimi.meteocal.utils.Utility;
import java.io.File;
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
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Fabiuz
 */
@Stateless
public class ImportExportController {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm zzz");

    @Inject
    EventController eventController;
    
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
    
        /**
     * Delete the file and controls that it is a file with write permission
     * @param file file to be deleted
     */
    public void controlAndDeleteFile(File file){
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
     * Useful to move the uploaded file into the common file folder (common for import/export).
     * NB: inputStream won't be close in this function.
     * @param filename destination file name (or pathname)
     * @param inputStream stream of the just uploaded file
     */
    public void saveUploadedFileIntoTheCorrectFolder(String filename, InputStream inputStream){
        try {
            try (OutputStream outputStream = new FileOutputStream(filename)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while(0<=(bytesRead = inputStream.read(buffer))) {
                    outputStream.write(buffer, 0, bytesRead);                         
                }
                outputStream.flush();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SettingsBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SettingsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Read the specified file and create a list of Event to be imported
     * @param filename filename of the file to be read (you can specified a different pathname)
     * @return the list of event, null if XML file has something wrong
     */
    public List<Event> readXmlFile(String filename){
        List<Event> importedEvents=new ArrayList<>();
        try {
            File xmlFile = new File(filename);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            //here start the core of import
            //the initial data(remember that the data user will be skipped).
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
                    event.setCreator(userManager.getLoggedUser());
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
                        controlAndDeleteFile(xmlFile);
                        System.out.println("One or more events can't be imported");
                        return null;
                    }
                    importedEvents.add(event);
                }
            }
            controlAndDeleteFile(xmlFile);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(SettingsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return importedEvents;
    }
}
