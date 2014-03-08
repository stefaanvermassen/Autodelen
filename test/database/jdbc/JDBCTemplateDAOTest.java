package database.jdbc;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import models.EmailTemplate;
import models.User;

import org.junit.Before;
import org.junit.Test;

import database.DataAccessContext;
import database.DatabaseHelper;
import database.TemplateDAO;

public class JDBCTemplateDAOTest {
	
	private TemplateDAO templateDAO;
	private List<EmailTemplate> templates;

	@Before
    public void setUp() throws Exception {
        DataAccessContext context = DatabaseHelper.getDataAccessProvider().getDataAccessContext();
        templateDAO = context.getTemplateDAO();
        templates = templateDAO.getAllTemplates();
    }
	
	@Test
	public void testTemplateDAO() throws Exception {
		getTemplateTest();
		updateTemplateTest();
	}
	
	public void updateTemplateTest() throws Exception{
		Iterator<EmailTemplate> it = templates.iterator();
		Scanner sc = new Scanner(new File("test/database/random_text.txt"));
        sc.useDelimiter("\\t|\\r\\n");
        sc.nextLine(); //skip header first time
        while(sc.hasNext() && it.hasNext()) {
            String body = sc.next();
            EmailTemplate template = (EmailTemplate) it.next();
            
            templateDAO.updateTemplate(template.getId(), body);
            EmailTemplate template2 = templateDAO.getTemplate(template.getId());
            
            assertEquals(template2.getBody(), body);
            assertNotEquals(template.getBody(), template2.getBody());
        }
        sc.close();
	}
	
	public void getTemplateTest(){
		for(EmailTemplate template : templates){
			int id = template.getId();
			String body = template.getBody();
			String title = template.getTitle();
			List<String> tags = template.getUsableTags();
			
			EmailTemplate template2 = templateDAO.getTemplate(id);
			assertEquals(id, template2.getId());
			assertEquals(body,template2.getBody());
			assertEquals(title,template2.getTitle());
			assertTrue(tags.containsAll(template2.getUsableTags()));
			assertTrue(template2.getUsableTags().containsAll(tags));
		}
	}
}
