package it.polimi.meteocal.boundary;

import it.polimi.meteocal.entity.User;
import java.io.ByteArrayInputStream;
import javax.ejb.Stateless;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Fabiuz
 */
@Stateless
public class PictureBean{
    
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
        return picture;
    }
    
    
    
}
