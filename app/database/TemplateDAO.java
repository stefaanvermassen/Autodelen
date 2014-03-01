package database;

import models.EmailTemplate;

/**
 * Created by Stefaan Vermassen on 01/03/14.
 */
public interface TemplateDAO {

    public EmailTemplate getTemplate(String title) throws DataAccessException;
    public void updateTemplate(EmailTemplate template) throws DataAccessException;

}
