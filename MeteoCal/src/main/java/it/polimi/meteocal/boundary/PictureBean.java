package it.polimi.meteocal.boundary;

import it.polimi.meteocal.entity.User;
import java.io.ByteArrayInputStream;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Fabiuz
 */
@Named(value = "pictureBean")
@Dependent
public class PictureBean {
    
    /**
     * Creates a new instance of PictureBean
     */
    public PictureBean() {
    }
    
    public StreamedContent getPictureFromUser(User user){
        if(user.getPicture()==null||user.getPictureType()==null){
            return new DefaultStreamedContent();
        }
        StreamedContent picture;
        picture = new DefaultStreamedContent(new ByteArrayInputStream(user.getPicture()),user.getPictureType());
        if(picture==null){
            return new DefaultStreamedContent();
        }
        return picture;
    }
    
    
    
}
