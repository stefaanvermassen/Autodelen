package models;

/**
 * Created by Stefaan Vermassen on 26/04/14.
 */
public enum RefuelStatus {
    CREATED("Gelieve een bewijs in te dienen"),
    REQUEST("Wachten op goedkeuring"),
    ACCEPTED("Aanvraag goedgekeurd"),
    REFUSED("Aanvraag geweigerd");

    // Enum definition
    private String description;

    private RefuelStatus(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
