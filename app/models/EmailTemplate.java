package models;

import java.util.List;

/**
 * Created by Stefaan Vermassen on 01/03/14.
 */
public class EmailTemplate {

    private int id;
    private String title;
    private String body;
    private List<String> tags;

    public EmailTemplate(int id, String title, String body, List<String> tags){
        this.id = id;
        this.title = title;
        this.body = body;
        this.tags = tags;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public List<String> getUsableTags(){
        return tags;
    }

}
