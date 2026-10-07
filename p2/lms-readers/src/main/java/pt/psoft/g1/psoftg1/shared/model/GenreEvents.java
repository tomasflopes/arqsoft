package pt.psoft.g1.psoftg1.shared.model;

/**
 * Enum para eventos de Genre no sistema AMQP
 */
public enum GenreEvents {
    GENRE_CREATED("Genre.Created"),
    GENRE_UPDATED("Genre.Updated"),
    GENRE_DELETED("Genre.Deleted");

    private final String eventName;

    GenreEvents(String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
}
