package it.polimi.meteocal.boundary;
 
import it.polimi.meteocal.boundary.service.ThemeService;
import it.polimi.meteocal.entity.Theme;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty; 

@ManagedBean
public class ThemeSwitcherBean {
 
    /**
     * The list of themes
     */
    private List<Theme> themes;
     
    @ManagedProperty("#{themeService}")
    private ThemeService service;
 
    @PostConstruct
    public void init() {
        themes = service.getThemes();
    }
    
    /**
     * Get the list of available themes
     * @return the list of themes
     */
    public List<Theme> getThemes() {
        return themes;
    } 
 
    /**
     * Set the theme service 
     * @param service 
     */
    public void setService(ThemeService service) {
        this.service = service;
    }
}