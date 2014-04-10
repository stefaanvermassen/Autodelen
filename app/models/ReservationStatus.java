/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

/**
 *
 * @author Laurent
 */
public enum ReservationStatus {
    REQUEST("Wachten op goedkeuring"),
    ACCEPTED("Aanvraag goedgekeurd"),
    REFUSED("Aanvraag geweigerd"),
    CANCELLED("Aanvraag geannuleerd"),
    REQUEST_DETAILS("Wachten op informatie rit"),
    FINISHED("Rit beëindigd");

    // Enum definition
    private String description;

    private ReservationStatus(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
