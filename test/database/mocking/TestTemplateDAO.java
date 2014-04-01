package database.mocking;

import java.util.ArrayList;
import java.util.List;

import models.EmailTemplate;
import models.MailType;
import database.DataAccessException;
import database.TemplateDAO;

public class TestTemplateDAO implements TemplateDAO{
	
	private List<EmailTemplate> list;
	
	public TestTemplateDAO(){
		list = new ArrayList<>();
	}

	@Override
	public EmailTemplate getTemplate(int templateID) throws DataAccessException {
		for(EmailTemplate template : list){
			if(template.getId() == templateID){
				return new EmailTemplate(templateID, template.getTitle(), template.getBody(), template.getUsableTags(), template.getSubject(), template.getSendMail()
						, template.getSendMailChangeable());
			}
		}
		return null;
	}

	@Override
	public EmailTemplate getTemplate(MailType type) throws DataAccessException {
		for(EmailTemplate template : list){
			if(template.getId() == type.getKey()){
				return new EmailTemplate(template.getId(), template.getTitle(), template.getBody(), template.getUsableTags(), template.getSubject(), template.getSendMail()
						, template.getSendMailChangeable());
			}
		}
		return null;
	}

	@Override
	public List<EmailTemplate> getAllTemplates() throws DataAccessException {
		return list;
	}

	@Override
	public void updateTemplate(int templateID, String templateBody,
			String templateSubject, boolean templateSendMail)
			throws DataAccessException {
		for(EmailTemplate template : list){
			if(template.getId() == templateID){
				list.add(new EmailTemplate(templateID, template.getTitle(), templateBody, template.getUsableTags(), templateSubject, templateSendMail, template.getSendMailChangeable()));
				list.remove(template);
			}
		}
		
	}

		
}
