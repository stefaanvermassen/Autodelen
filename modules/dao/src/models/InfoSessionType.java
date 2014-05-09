package models;

/**
 * Created by HannesM on 14/03/14.
 */
public enum InfoSessionType {
    NORMAL("Normaal"),
    OWNER("Eigenaar"),
    OTHER("Ander");

    // Enum implementation
    private String description;

    private InfoSessionType(String description) {
        this.description = description;
    }

    public String getDescription(){
        return description;
    }

    public static InfoSessionType getTypeFromString(String s) {
        for(InfoSessionType t : values()) {
            if(t.getDescription().equals(s)) {
                return t;
            }
        }
        return null;
    }
}
