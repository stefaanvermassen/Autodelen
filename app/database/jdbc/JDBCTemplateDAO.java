package database.jdbc;

import database.DataAccessException;
import database.TemplateDAO;
import models.EmailTemplate;
import models.MailType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 01/03/14.
 */
public class JDBCTemplateDAO implements TemplateDAO {

    private Connection connection;
    private PreparedStatement getTemplateByIdStatement;
    private PreparedStatement getTagsByTemplateIdStatement;
    private PreparedStatement getAllTemplatesStatement;
    private PreparedStatement updateTemplateStatement;

    public JDBCTemplateDAO(Connection connection) {
        this.connection = connection;
    }

    private PreparedStatement getTemplateByTitleStatement() throws SQLException {
        if (getTemplateByIdStatement == null) {
            getTemplateByIdStatement = connection.prepareStatement("SELECT template_id, template_title, template_body " +
                    "FROM Templates WHERE template_id = ?;");
        }
        return getTemplateByIdStatement;
    }

    private PreparedStatement getAllTemplatesStatement() throws SQLException {
        if (getAllTemplatesStatement == null) {
            getAllTemplatesStatement = connection.prepareStatement("SELECT template_id, template_title, template_body " +
                    "FROM Templates;");
        }
        return getAllTemplatesStatement;
    }

    private PreparedStatement getUpdateTemplateStatement() throws SQLException {
        if (updateTemplateStatement == null) {
            updateTemplateStatement = connection.prepareStatement("UPDATE Templates SET template_body = ? WHERE template_id = ?;");
        }
        return updateTemplateStatement;
    }

    public EmailTemplate populateEmailTemplate(ResultSet rs) throws SQLException {
        return new EmailTemplate(rs.getInt("template_id"), rs.getString("template_title"), rs.getString("template_body"), getUsableTags(rs.getInt("template_id")));
    }

    private PreparedStatement getTagsByTemplateIdStatement() throws SQLException {
        if (getTagsByTemplateIdStatement == null) {
            getTagsByTemplateIdStatement = connection.prepareStatement("SELECT template_tag_body " +
                    "FROM templatetagassociations JOIN templatetags ON templatetagassociations.template_tag_id = templatetags.template_tag_id"
                    + " WHERE template_id = ?;");
        }
        return getTagsByTemplateIdStatement;
    }

    private List<String> getUsableTags(int templateId) throws DataAccessException {
        try {
            PreparedStatement ps = getTagsByTemplateIdStatement();
            ps.setInt(1,templateId);
            try (ResultSet rs = ps.executeQuery()) {
                return populateTagList(rs);
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading emailtags resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch templatetags by templateid.", ex);
        }

    }

    public static List<String> populateTagList(ResultSet rs) throws SQLException {
        List<String> usableTags = new ArrayList<>();
        while (rs.next()) {
            usableTags.add(rs.getString("template_tag_body"));
        }
        return usableTags;
    }


    @Override
    public EmailTemplate getTemplate(int templateID) throws DataAccessException {
        try {
            PreparedStatement ps = getTemplateByTitleStatement();
            ps.setInt(1,templateID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return populateEmailTemplate(rs);
                } else {
                    return null;
                }
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading emailtemplate resultset", ex);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Could not fetch emailtemplate by id.", ex);
        }
    }

    @Override
    public EmailTemplate getTemplate(MailType type) throws DataAccessException {
        return getTemplate(type.getKey());
    }

    @Override
    public List<EmailTemplate> getAllTemplates() throws DataAccessException {
        List<EmailTemplate> templates = new ArrayList<>();
        try{
            PreparedStatement ps = getAllTemplatesStatement();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    templates.add(populateEmailTemplate(rs));
                }
                return templates;
            } catch (SQLException ex) {
                throw new DataAccessException("Error reading template resultset", ex);
            }

        }catch (SQLException ex) {
            throw new DataAccessException("Could not fetch templates.", ex);
        }
    }

    @Override
    public void updateTemplate(int templateID, String templateBody) throws DataAccessException {
        try {
            PreparedStatement ps = getUpdateTemplateStatement();
            ps.setString(1, templateBody);
            ps.setInt(2,templateID);
            if(ps.executeUpdate() == 0)
                throw new DataAccessException("No rows were affected when updating template.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
