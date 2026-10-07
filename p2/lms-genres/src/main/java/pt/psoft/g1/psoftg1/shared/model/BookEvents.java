package pt.psoft.g1.psoftg1.shared.model;

public enum BookEvents {
  // Routing keys must match what LMS-Books publishes (uppercase constants)
  BOOK_CREATED("BOOK_CREATED"),
  BOOK_UPDATED("BOOK_UPDATED"),
  BOOK_DELETED("BOOK_DELETED");

  private final String eventName;

  BookEvents(String eventName) {
    this.eventName = eventName;
  }

  public String getEventName() {
    return eventName;
  }
}
