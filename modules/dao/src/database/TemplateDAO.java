package database;

import models.EmailTemplate;
import models.MailType;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 01/03/14.
 */
public interface TemplateDAO {

    public EmailTemplate getTemplate(int templateID) throws DataAccessException;
    public EmailTemplate getTemplate(MailType type) throws DataAccessException;
    public int getAmountOfTemplates(Filter filter) throws DataAccessException;
    public List<EmailTemplate> getTemplateList(FilterField orderBy, boolean asc, int page, int pageSize, Filter filter) throws DataAccessException;
    public void updateTemplate(int templateID, String templateBody, String templateSubject, boolean templateSendMail ) throws DataAccessException;

}
